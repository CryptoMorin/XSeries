package com.cryptomorin.xseries.profiles.mojang;

import com.cryptomorin.xseries.profiles.ProfilesCore;
import com.cryptomorin.xseries.profiles.exceptions.MojangAPIException;
import com.cryptomorin.xseries.profiles.exceptions.MojangAPIRetryException;
import com.cryptomorin.xseries.reflection.XReflection;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import org.bukkit.Bukkit;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

/**
 * Sends HTTP requests to Mojang API endpoints.
 * @see MojangAPI
 * @see MojangAPIException
 * @see MojangAPIRetryException
 */
@ApiStatus.Internal
public class MinecraftClient {
    private static final AtomicInteger SESSION_ID = new AtomicInteger();
    private static final Proxy PROXY = ProfilesCore.PROXY == null ? Proxy.NO_PROXY : ProfilesCore.PROXY;
    private static final Gson GSON = new Gson();
    private static final RateLimiter TOTAL_REQUESTS = new RateLimiter(Integer.MAX_VALUE, Duration.ofMinutes(10));
    /**
     * For example:
     * XSeries/11.2.0 (X11; Linux x86_64; Oracle Corporation; 21.0.0) Paper/1.21-R0.1-SNAPSHOT 1.21-9-4ea696f (MC: 1.21)
     */
    private static final String USER_AGENT = "XSeries/" + XReflection.XSERIES_VERSION +
            " (" + System.getProperty("os.name") + "; " + System.getProperty("os.version") + "; " +
            System.getProperty("java.vendor") + "; " + System.getProperty("java.version") + ") " +
            Bukkit.getName() + '/' + Bukkit.getBukkitVersion() + ' ' + Bukkit.getVersion();
    private final String method;
    private final URI baseURL;
    private final RateLimiter rateLimiter;

    @SuppressWarnings("ReturnOfInnerClass")
    public Session session(@Nullable ProfileRequestConfiguration config) {
        Session session = new Session();
        if (config != null) config.configure(session);
        return session;
    }

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

    @SuppressWarnings("ReturnOfInnerClass")
    public final class Session {
        private final int sessionId = SESSION_ID.getAndIncrement();
        private Duration
                connectTimeout = Duration.ofSeconds(10),
                readTimeout = Duration.ofSeconds(10),
                retryDelay = Duration.ofSeconds(5);
        private int retries;
        private boolean waitInQueue = true;
        private Object body;
        private String append;
        private HttpURLConnection connection;
        private BiFunction<Session, Throwable, Boolean> errorHandler;

        private void debug(String message, Object... vars) {
            Object[] variables = XReflection.concatenate(new Object[]{sessionId, append}, vars);
            ProfilesCore.debug("[MinecraftClient-{}][{}] " + message, variables);
        }

        public Session exceptionally(BiFunction<Session, Throwable, Boolean> errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public Session waitInQueue(boolean wait) {
            this.waitInQueue = wait;
            return this;
        }

        public Session retry(int retries, Duration delay) {
            this.retries = retries;
            this.retryDelay = delay;
            return this;
        }

        public Session body(Object body) {
            validateMethod("POST");
            this.body = Objects.requireNonNull(body);
            return this;
        }

        public Session append(@Nonnull String append) {
            this.append = Objects.requireNonNull(append);
            return this;
        }

        public Session timeout(Duration connectTimeout, Duration readTimeout) {
            this.connectTimeout = connectTimeout;
            this.readTimeout = readTimeout;
            return this;
        }

        private void validateMethod(String method) {
            if (!MinecraftClient.this.method.equals(method))
                throw new UnsupportedOperationException(
                        "Cannot " + method + " with a client using method " + MinecraftClient.this);
        }

        @Nullable
        public JsonElement request() throws IOException, MojangAPIException {
            try {
                JsonElement response = request0();
                debug("Received response: {}", response);
                return response == null ? null : (response.isJsonNull() ? null : response);
            } catch (Exception ex) {
                if (retries > 0) {
                    retries--;
                    if (!(ex instanceof MojangAPIRetryException) ||
                            ((MojangAPIRetryException) ex).getReason() != MojangAPIRetryException.Reason.RATELIMITED) {
                        try {
                            Thread.sleep(retryDelay.toMillis());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return request();
                }
                if (errorHandler == null) throw ex;
                else {
                    Boolean shouldRetry = errorHandler.apply(this, ex);
                    if (shouldRetry == null || shouldRetry) return request();
                    else throw ex;
                }
            }
        }

        @Nullable
        private JsonElement request0() throws IOException, MojangAPIException {
            if (waitInQueue) {
                rateLimiter.acquireOrWait();
            } else {
                if (!rateLimiter.acquire())
                    throw new MojangAPIRetryException(MojangAPIRetryException.Reason.RATELIMITED,
                            "Rate limit has been hit! " + rateLimiter + totalReq());
            }

            connection = (HttpURLConnection)
                    (append == null ? baseURL : baseURL.resolve(append)).toURL().openConnection(PROXY);
            connection.setRequestMethod(method);
            connection.setConnectTimeout((int) connectTimeout.toMillis());
            connection.setReadTimeout((int) readTimeout.toMillis());
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);

            // Not used by the default authlib's client, but we're going to
            // add it anyway just for the sake of networking and Mojang's server stats (if any?)
            connection.setRequestProperty("User-Agent", USER_AGENT);

            // The token is only used for modifying operations like uploading a new skin.
            // if (this.accessToken != null) {
            //     connection.setRequestProperty("Authorization", "Bearer " + this.accessToken);
            // }

            if (body != null) {
                connection.setDoOutput(true);
                String stringBody = GSON.toJson(body);
                debug("Writing body {} to {}", stringBody, connection.getURL());
                byte[] bodyBytes = stringBody.getBytes(StandardCharsets.UTF_8);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));

                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(bodyBytes);
                }
            } else {
                connection.setDoOutput(false);
            }

            debug("Sending request to {}", connection.getURL());

            try {
                return connectionStreamToJson(false);
            } catch (Throwable ex) {
                MojangAPIException exception;
                try {
                    switch (connection.getResponseCode()) {
                        case HttpURLConnection.HTTP_NOT_FOUND:
                            return null;
                        case 429: // Too many requests
                            String rateLimitBefore = rateLimiter.toString();
                            rateLimiter.instantRateLimit();
                            throw new MojangAPIRetryException(MojangAPIRetryException.Reason.RATELIMITED,
                                    "Rate limit has been hit (server confirmed): " + rateLimitBefore + " -> " + rateLimitBefore + totalReq());
                    }
                    if (ex instanceof SocketException && ex.getMessage().toLowerCase(Locale.ENGLISH).contains("connection reset")) {
                        throw new MojangAPIRetryException(MojangAPIRetryException.Reason.CONNECTION_RESET, "Connection was closed", ex);
                    }
                    JsonElement errorJson = connectionStreamToJson(true);
                    exception = new MojangAPIException(errorJson == null ? "[NO ERROR RESPONSE]" : errorJson.toString(), ex);
                } catch (MojangAPIRetryException rethrowEx) {
                    throw rethrowEx;
                } catch (Throwable errorEx) {
                    exception = new MojangAPIException("Failed to read both normal response " +
                            "and error response from '" + connection.getURL() + '\'');
                    exception.addSuppressed(ex);
                    exception.addSuppressed(errorEx);
                }

                throw exception;
            }
        }

        private JsonElement connectionStreamToJson(boolean error) throws IOException, RuntimeException {
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
                        String rawResponse = CharStreams.toString(new InputStreamReader(
                                error ? connection.getErrorStream() : connection.getInputStream(),
                                Charsets.UTF_8)
                        );
                        throw new RuntimeException((error ? "error response" : "normal response")
                                + " is not a JSON object '"
                                + connection.getResponseCode() + " - " + connection.getResponseMessage() + "': " +
                                rawResponse, ex);
                    }
                    return json;
                }
            }
        }
    }
}
