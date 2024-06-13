package com.cryptomorin.xseries.profiles;

import com.cryptomorin.xseries.profiles.exceptions.MojangAPIException;
import com.cryptomorin.xseries.profiles.exceptions.PlayerProfileNotFoundException;
import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.CharStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class MojangAPI {
    /**
     * Used for older versions.
     */
    private static final Cache<UUID, GameProfile> INSECURE_PROFILES = CacheBuilder.newBuilder()
            .expireAfterWrite(6L, TimeUnit.HOURS).build();

    /**
     * "requireSecure" parameter basically means ignore the cache and also use "unsigned=false" parameter.
     */
    private static final boolean REQUIRE_SECURE_PROFILES = false;

    /**
     * According to <a href="https://wiki.vg/Mojang_API">Mojang API</a> the rate limit
     * is around 600 requests per 10 (i.e. 1 request per second) for most endpoints.
     * However <a href="https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin.2FCape">UUID to Profile and Skin/Cape</a>
     * is around 200 requests per minute.
     * <p>
     * If we want to use UUID_TO_PROFILE_RATELIMIT we'd have to copy and paste fetchProfile()
     * because that method catches HTTP exceptions.
     */
    private static final RateLimiter
            UUID_RATELIMIT = new RateLimiter(600, Duration.ofMinutes(10)),
            UUID_TO_PROFILE_RATELIMIT = new RateLimiter(200, Duration.ofMinutes(1));

    /**
     * @return null if a player with that username is not found.
     */
    @Nullable
    protected static UUID requestUsernameToUUID(@Nonnull String username) throws IOException {
        JsonObject userJson = requestUsernameToUUIDData(username);
        if (userJson == null) return null;

        JsonElement idElement = userJson.get("id");
        if (idElement == null)
            throw new RuntimeException("No 'id' field for UUID request for '" + username + "': " + userJson);

        return PlayerUUIDs.UUIDFromDashlessString(idElement.getAsString());
    }

    /**
     * Retrieves a cached {@link GameProfile} by username from the user cache.
     * If the profile is not found in the cache, creates a new profile with the provided name.
     *
     * @param username The username of the profile to retrieve from the cache.
     * @return The cached {@link GameProfile} corresponding to the username, or a new profile if not found.
     */
    private static GameProfile getCachedProfileByUsername(String username) {
        try {
            // Expires after every month calendar.add(2, 1); (Persists between restarts)
            @Nullable Object profile = ProfilesCore.GET_PROFILE_BY_NAME.invoke(ProfilesCore.USER_CACHE, username);
            if (profile instanceof Optional) profile = ((Optional<?>) profile).orElse(null);
            GameProfile gameProfile = profile == null ? new GameProfile(PlayerUUIDs.IDENTITY_UUID, username) : PlayerProfiles.sanitizeProfile((GameProfile) profile);
            ProfilesCore.debug("The cached profile for {} -> {}", username, profile);
            return gameProfile;
        } catch (Throwable throwable) {
            ProfilesCore.LOGGER.error("Unable to get cached profile by username: " + username, throwable);
            return null;
        }
    }

    /**
     * Retrieves a cached {@link GameProfile} by UUID from the user cache.
     * If the profile is not found in the cache, creates a new profile with the provided UUID.
     *
     * @param uuid The UUID of the profile to retrieve from the cache.
     * @return The cached {@link GameProfile} corresponding to the UUID, or a new profile if not found.
     */
    @Nonnull
    protected static GameProfile getCachedProfileByUUID(UUID uuid) {
        uuid = PlayerUUIDs.isOnlineMode() ? uuid : PlayerUUIDs.ONLINE_TO_OFFLINE.getOrDefault(uuid, uuid);
        try {
            @Nullable Object profile = ProfilesCore.GET_PROFILE_BY_UUID.invoke(ProfilesCore.USER_CACHE, uuid);
            if (profile instanceof Optional) profile = ((Optional<?>) profile).orElse(null);
            ProfilesCore.debug("The cached profile for {} -> {}", uuid, profile);
            return profile == null ? PlayerProfiles.createNamelessGameProfile(uuid) : PlayerProfiles.sanitizeProfile((GameProfile) profile);
        } catch (Throwable throwable) {
            ProfilesCore.LOGGER.error("Unable to get cached profile by UUID: " + uuid, throwable);
            return PlayerProfiles.createNamelessGameProfile(uuid);
        }
    }

    /**
     * Caches the provided {@link GameProfile} in the user cache.
     * These caches are also stored in {@code usercache.json} (file specified in net.minecraft.server.Services).
     *
     * @param profile The {@link GameProfile} to cache.
     */
    private static void cacheProfile(GameProfile profile) {
        try {
            ProfilesCore.CACHE_PROFILE.invoke(ProfilesCore.USER_CACHE, profile);
            ProfilesCore.debug("Profile is now cached: {}", profile);
        } catch (Throwable throwable) {
            ProfilesCore.LOGGER.error("Unable to cache profile: " + profile, throwable);
        }
    }

    /**
     * Fetches additional properties for the given {@link GameProfile} if possible (like the texture).
     *
     * @param profile The {@link GameProfile} for which properties are to be fetched.
     * @return The updated {@link GameProfile} with fetched properties, sanitized for consistency.
     * @throws IllegalArgumentException if a player with the specified profile properties (username and UUID) doesn't exist.
     */
    @Nonnull
    protected static GameProfile fetchProfile(@Nonnull GameProfile profile) {
        if (!ProfilesCore.NULLABILITY_RECORD_UPDATE) {
            GameProfile old = profile;
            GameProfile cached = INSECURE_PROFILES.getIfPresent(profile.getId());
            GameProfile newProfile;

            try {
                /* This URL endpoint still works as of 1.20.6
                 * URL url = HttpAuthenticationService.constantURL("https://sessionserver.mojang.com/session/minecraft/profile/" + UUIDTypeAdapter.fromUUID(profile.getId()));
                 * url = HttpAuthenticationService.concatenateURL(url, "unsigned=" + !requireSecure);
                 * GameProfile result = new GameProfile(response.getId(), response.getName());
                 * result.getProperties().putAll(response.getProperties());
                 * profile.getProperties().putAll(response.getProperties());
                 */
                // These results are not cached unlike the newer fetchProfile()
                if (cached == null) {
                    newProfile = (GameProfile) ProfilesCore.FILL_PROFILE_PROPERTIES
                            .invoke(ProfilesCore.MINECRAFT_SESSION_SERVICE, profile, REQUIRE_SECURE_PROFILES);
                    if (profile == newProfile) {
                        // Returns the same instance if any error occurs.
                        throw new PlayerProfileNotFoundException("Player with the given properties not found: " + profile);
                    }
                } else {
                    newProfile = (profile = cached);
                }
                ProfilesCore.debug("Filled properties: {} -> {}", old, newProfile);
            } catch (PlayerProfileNotFoundException ex) {
                throw ex;
            } catch (Throwable throwable) {
                throw new RuntimeException("Unable to fetch profile properties: " + profile, throwable);
            }
        } else {
            // Get real UUID for offline players
            UUID realUUID;
            if (profile.getName().equals(PlayerProfiles.DEFAULT_PROFILE_NAME)) {
                // We will assume that the requested UUID is the real one
                // since the server cache didn't find it and that player never
                // joined this server.
                // There is no way to tell if this is fake or real UUID, the
                // closest we can get is to just request it from the server
                // and see if it exists. (We can't reverse UUID.nameUUIDFromBytes)
                realUUID = profile.getId();
            } else {
                realUUID = PlayerUUIDs.getRealUUIDOfPlayer(profile.getName(), profile.getId());
                if (realUUID == null) {
                    throw new PlayerProfileNotFoundException("Player with the given properties not found: " + profile);
                }
            }

            // Implemented by YggdrasilMinecraftSessionService
            // fetchProfile(UUID profileId, boolean requireSecure) -> fetchProfileUncached(UUID profileId, boolean requireSecure)
            // This cache expireAfterWrite every 6 hours.
            com.mojang.authlib.yggdrasil.ProfileResult result = ((MinecraftSessionService) ProfilesCore.MINECRAFT_SESSION_SERVICE)
                    .fetchProfile(realUUID, REQUIRE_SECURE_PROFILES);
            if (result != null) {
                profile = result.profile();
                ProfilesCore.debug("Yggdrasil provided profile is {} with actions {} for {}", result.profile(), result.actions(), profile);
            } else {
                ProfilesCore.debug("Yggdrasil provided profile is null with actions for {}", profile);
            }
        }

        profile = PlayerProfiles.sanitizeProfile(profile);
        PlayerProfiles.signXSeries(profile);
        cacheProfile(profile);
        return profile;
    }

    /**
     * @return null if a player with this username doesn't exist.
     */
    @Nullable
    private static JsonObject requestUsernameToUUIDData(@Nonnull String username) throws IOException {
        if (!UUID_RATELIMIT.acquire())
            throw new IllegalStateException("Rate limit has been hit! " + UUID_RATELIMIT);

        HttpURLConnection connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + username).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10 * 1000); // 10 seconds
        connection.setReadTimeout(30 * 1000); // 30 seconds
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setAllowUserInteraction(false);
        ProfilesCore.debug("Sending request to {}", connection.getURL());

        try {
            return connectionStreamToJson(connection, false).getAsJsonObject();
        } catch (Throwable ex) {
            MojangAPIException exception;
            try {
                switch (connection.getResponseCode()) {
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        return null;
                    case 429: // Too many requests
                        UUID_RATELIMIT.instantRateLimit();
                }
                if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) return null;
                if (ex instanceof SocketException && ex.getMessage().toLowerCase(Locale.ENGLISH).contains("connection reset")) {
                    // TODO handle reconnecting for future requests.
                    throw ex;
                }
                exception = new MojangAPIException(connectionStreamToJson(connection, true).toString(), ex);
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
                JsonReader reader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        ) {
            JsonElement json = Streams.parse(reader);
            if (json == null || !json.isJsonObject()) {
                // For UUID_TO_PROFILE, this happens when HTTP Code 204 (No Content) is given.
                // And that happens if the UUID doesn't exist in Mojang servers. (E.g. cracked UUIDs)
                throw new RuntimeException((error ? "error" : "normal response") + " is not a JSON object with response '"
                        + connection.getResponseCode() + ": " + connection.getResponseMessage() + "': " +
                        CharStreams.toString(new InputStreamReader(error ? connection.getErrorStream() : connection.getInputStream(), Charsets.UTF_8)));
            }
            return json.getAsJsonObject();
        }
    }
}
