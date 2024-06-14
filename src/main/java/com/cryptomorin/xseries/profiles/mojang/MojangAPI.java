package com.cryptomorin.xseries.profiles.mojang;

import com.cryptomorin.xseries.profiles.PlayerProfiles;
import com.cryptomorin.xseries.profiles.PlayerUUIDs;
import com.cryptomorin.xseries.profiles.ProfileInputType;
import com.cryptomorin.xseries.profiles.ProfilesCore;
import com.cryptomorin.xseries.profiles.exceptions.PlayerProfileNotFoundException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
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
     * https://wiki.vg/Mojang_API#Username_to_UUID
     * According to <a href="https://wiki.vg/Mojang_API">Mojang API</a> the rate limit
     * is around 600 requests per 10 (i.e. 1 request per second) for most endpoints.
     * However <a href="https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin.2FCape">UUID to Profile and Skin/Cape</a>
     * is around 200 requests per minute.
     * <p>
     * If we want to use UUID_TO_PROFILE_RATELIMIT we'd have to copy and paste fetchProfile()
     * because that method catches HTTP exceptions.
     */
    private static final MinecraftClient USERNAME_TO_UUID = new MinecraftClient(
            "GET",
            "https://api.mojang.com/users/profiles/minecraft/",
            new RateLimiter(600, Duration.ofMinutes(10))
    );

    /**
     * https://wiki.vg/Mojang_API#Usernames_to_UUIDs
     */
    private static final MinecraftClient USERNAMES_TO_UUIDS = new MinecraftClient(
            "POST",
            "https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname",
            new RateLimiter(600, Duration.ofMinutes(10))
    );

    private static final MinecraftClient UUID_TO_PROFILE = new MinecraftClient(
            "GET",
            "https://api.mojang.com/users/profiles/minecraft/",
            new RateLimiter(200, Duration.ofMinutes(1))
    );

    /**
     * @return null if a player with that username is not found.
     */
    @Nullable
    public static UUID requestUsernameToUUID(@Nonnull String username) throws IOException {
        JsonObject userJson = USERNAME_TO_UUID.request(username);
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
        // Unused because of limitations, use profileFromUsername(String username) instead.
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

    public static Optional<GameProfile> profileFromUsername(String username) {
        try {
            return profileFromUsername0(username);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<GameProfile> profileFromUsername0(String username) throws Throwable {
        String normalized = username.toLowerCase(Locale.ROOT);
        Object usercache_usercacheentry = ProfilesCore.UserCache_profilesByName.get(normalized);
        boolean flag = false;

        if (usercache_usercacheentry != null && (new Date()).getTime() >=
                ((Date) ProfilesCore.UserCacheEntry_getExpirationDate.invoke(usercache_usercacheentry)).getTime()) {
            GameProfile gameProfile = (GameProfile) ProfilesCore.UserCacheEntry_getProfile.invoke(usercache_usercacheentry);
            ProfilesCore.UserCache_profilesByName.remove(gameProfile.getName().toLowerCase(Locale.ROOT));
            ProfilesCore.UserCache_profilesByUUID.remove(gameProfile.getId());
            flag = true;
            usercache_usercacheentry = null;
        }

        Optional<GameProfile> optional;

        if (usercache_usercacheentry != null) {
            if (ProfilesCore.UserCacheEntry_setLastAccess != null) {
                // usercache_usercacheentry.setLastAccess(this.getNextOperation());
                long nextOperation = (long) ProfilesCore.UserCache_getNextOperation.invoke(ProfilesCore.USER_CACHE);
                ProfilesCore.UserCacheEntry_setLastAccess.invoke(usercache_usercacheentry, nextOperation);
            }
            optional = Optional.of((GameProfile) ProfilesCore.UserCacheEntry_getProfile.invoke(usercache_usercacheentry));
        } else {
            // optional = lookupGameProfile(this.profileRepository, username); // CraftBukkit - use correct case for offline players
            UUID realUUID = PlayerUUIDs.getRealUUIDOfPlayer(username);
            if (realUUID == null) return Optional.empty();

            optional = Optional.of(PlayerProfiles.signXSeries(new GameProfile(realUUID, username)));
            // this.add((GameProfile) optional.get());
            cacheProfile(optional.get());
            flag = false;
        }

        // Should we implement this too?
        // Maybe we can even make our own method to save real UUIDs too, but that'd need a lot of work,
        // and it wouldn't be reliable if the config option is disabled.
        // if (flag && !org.spigotmc.SpigotConfig.saveUserCacheOnStopOnly) { // Spigot - skip saving if disabled
        //     YggdrasilGameProfileRepository.save();
        // }

        return optional;
    }

    private Map<UUID, String> findProfilesByNames(Collection<String> usernames) {
        if (usernames == null || usernames.isEmpty()) throw new IllegalArgumentException("Usernames are null or empty");
        for (String username : usernames) {
            if (username == null || !ProfileInputType.USERNAME.pattern.matcher(username).matches()) {
                throw new IllegalArgumentException("One of the requested usernames is invalid: " + username + " in " + usernames);
            }
        }

        Map<UUID, String> mapped = new HashMap<>(usernames.size());
        Iterable<List<String>> partition = Iterables.partition(
                new HashSet<>(usernames), // remove duplicate names
                (usernames.size() / 10) + (usernames.size() % 10 > 0 ? 1 : 0)
        );

        for (List<String> request : partition) {
            // TODO
        }

        return mapped;
    }

    /**
     * Retrieves a cached {@link GameProfile} by UUID from the user cache.
     * If the profile is not found in the cache, creates a new profile with the provided UUID.
     *
     * @param uuid The UUID of the profile to retrieve from the cache.
     * @return The cached {@link GameProfile} corresponding to the UUID, or a new profile if not found.
     */
    @Nonnull
    public static GameProfile getCachedProfileByUUID(UUID uuid) {
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
    public static GameProfile fetchProfile(@Nonnull GameProfile profile) {
        GameProfile original = profile;
        if (!ProfilesCore.NULLABILITY_RECORD_UPDATE) {
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
                ProfilesCore.debug("Filled properties: {} -> {}", original, newProfile);
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
                throw new PlayerProfileNotFoundException("fetchProfile could not find " + realUUID + " from " + original);
            }
        }

        profile = PlayerProfiles.sanitizeProfile(profile);
        PlayerProfiles.signXSeries(profile);
        cacheProfile(profile);
        return profile;
    }
}
