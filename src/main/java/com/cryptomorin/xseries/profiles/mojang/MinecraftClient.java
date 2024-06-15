package com.cryptomorin.xseries.profiles.mojang;

import com.cryptomorin.xseries.profiles.ProfilesCore;
import com.cryptomorin.xseries.profiles.exceptions.APIRetryException;
import com.cryptomorin.xseries.profiles.exceptions.MojangAPIException;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import org.intellij.lang.annotations.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;

public class MinecraftClient {
    private static final RateLimiter TOTAL_REQUESTS = new RateLimiter(Integer.MAX_VALUE, Duration.ofMinutes(10));
    private final String method;
    private final URI baseURL;
    private final RateLimiter rateLimiter;
    private static final Gson GSON = new Gson();

    public MinecraftClient(@Pattern("GET|POST|PUT|DELETE") String method, String baseURL, RateLimiter rateLimiter) {
        this.method = method;
        try {
            this.baseURL = new URI(baseURL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        this.rateLimiter = rateLimiter;
    }

    private static String totalReq() {
        return " (total: " + TOTAL_REQUESTS.getEffectiveRequestsCount() + ')';
    }

    private static void postBody(HttpURLConnection connection, Object body) throws IOException {
        String stringBody = GSON.toJson(body);
        ProfilesCore.debug("Writing body {} to {}", stringBody, connection.getURL());
        postBodyBytes(connection, stringBody.getBytes(StandardCharsets.UTF_8));
    }

    private static void postBodyBytes(HttpURLConnection connection, byte[] body) throws IOException {
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("Content-Length", String.valueOf(body.length));
        // if (this.accessToken != null) {
        //     connection.setRequestProperty("Authorization", "Bearer " + this.accessToken);
        // }

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(body);
        }
    }

    @Nullable
    public JsonElement post(@Nonnull Object body) throws IOException {
        validateMethod("POST");
        Objects.requireNonNull(body);
        return request(null, body);
    }

    @Nullable
    public JsonElement request(@Nonnull String append) throws IOException {
        validateMethod("GET");
        Objects.requireNonNull(append);
        return request(append, null);
    }

    private void validateMethod(String method) {
        if (!this.method.equals(method))
            throw new UnsupportedOperationException("Cannot " + method + " with a client using method " + this.method);
    }

    @Nullable
    private JsonElement request(@Nullable String append, @Nullable Object body) throws IOException {
        if (!rateLimiter.acquire())
            throw new APIRetryException(APIRetryException.Reason.RATELIMITED, "Rate limit has been hit! " + rateLimiter + totalReq());

        HttpURLConnection connection = (HttpURLConnection) (append == null ? baseURL : baseURL.resolve(append)).toURL().openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(10 * 1000); // 10 seconds
        connection.setReadTimeout(20 * 1000); // 20 seconds
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setAllowUserInteraction(false);
        if (body != null) {
            connection.setDoOutput(true);
            postBody(connection, body);
        } else {
            connection.setDoOutput(false);
        }

        ProfilesCore.debug("Sending request to {}", connection.getURL());

        try {
            return connectionStreamToJson(connection, false);
        } catch (Throwable ex) {
            MojangAPIException exception;
            try {
                switch (connection.getResponseCode()) {
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        return null;
                    case 429: // Too many requests
                        String rateLimitBefore = rateLimiter.toString();
                        rateLimiter.instantRateLimit();
                        throw new APIRetryException(APIRetryException.Reason.RATELIMITED,
                                "Rate limit has been hit (server confirmed): " + rateLimitBefore + " -> " + rateLimitBefore + totalReq());
                }
                if (ex instanceof SocketException && ex.getMessage().toLowerCase(Locale.ENGLISH).contains("connection reset")) {
                    throw new APIRetryException(APIRetryException.Reason.CONNECTION_RESET, "Connection was closed", ex);
                }
                JsonElement errorJson = connectionStreamToJson(connection, true);
                exception = new MojangAPIException(errorJson == null ? "[NO ERROR RESPONSE]" : errorJson.toString(), ex);
            } catch (APIRetryException rethrowEx) {
                throw rethrowEx;
            } catch (Throwable errorEx) {
                exception = new MojangAPIException("Failed to read both normal response and error response from '" + connection.getURL() + '\'');
                exception.addSuppressed(ex);
                exception.addSuppressed(errorEx);
            }

            throw exception;
        }
    }

    private static JsonElement connectionStreamToJson(HttpURLConnection connection, boolean error) throws IOException, RuntimeException {
        try (
                InputStream inputStream = error ? connection.getErrorStream() : connection.getInputStream();
        ) {
            if (error && inputStream == null) return null;
            try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                Exception ex = null;
                JsonElement json;
                try {
                    json = Streams.parse(reader);
                } catch (Exception e) {
                    ex = e;
                    json = null;
                }
                if (json == null) {
                    // For UUID_TO_PROFILE, this happens when HTTP Code 204 (No Content) is given.
                    // And that happens if the UUID doesn't exist in Mojang servers. (E.g. cracked UUIDs)
                    throw new RuntimeException((error ? "error response" : "normal response") + " is not a JSON object '"
                            + connection.getResponseCode() + " - " + connection.getResponseMessage() + "': " +
                            CharStreams.toString(new InputStreamReader(error ? connection.getErrorStream() : connection.getInputStream(), Charsets.UTF_8)), ex);
                }
                return json;
            }
        }
    }
}
