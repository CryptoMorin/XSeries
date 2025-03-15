/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.cryptomorin.xseries.profiles.mojang;

import com.cryptomorin.xseries.profiles.PlayerProfiles;
import com.cryptomorin.xseries.profiles.PlayerUUIDs;
import com.cryptomorin.xseries.profiles.ProfileLogger;
import com.cryptomorin.xseries.profiles.ProfilesCore;
import com.cryptomorin.xseries.profiles.exceptions.MojangAPIException;
import com.cryptomorin.xseries.profiles.exceptions.UnknownPlayerException;
import com.cryptomorin.xseries.profiles.objects.ProfileInputType;
import com.cryptomorin.xseries.reflection.XReflection;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Note: <a href="https://wiki.vg/">Wiki.vg</a> is no longer availabe because it was
 * <a href="https://minecraft.wiki/w/Forum:Wiki.vg_merge">sunset on November 30, 2024</a>.
 * Now it's being merged into <a href="https://minecraft.wiki/">minecraft.wiki</a>.
 */
@ApiStatus.Internal
public final class MojangAPI {
    private static final MojangProfileCache MOJANG_PROFILE_CACHE = !ProfilesCore.NULLABILITY_RECORD_UPDATE ?
            new MojangProfileCache.GameProfileCache(ProfilesCore.YggdrasilMinecraftSessionService_insecureProfiles) :
            new MojangProfileCache.ProfileResultCache(ProfilesCore.YggdrasilMinecraftSessionService_insecureProfiles);
    /**
     * The 6hr expiration time is probably for players that update their skin, but 6hrs seems a bit too frequent.
     */
    private static final Cache<UUID, Optional<GameProfile>> INSECURE_PROFILES = CacheBuilder.newBuilder()
            .expireAfterWrite(6L, TimeUnit.HOURS).build();

    /**
     * "requireSecure" parameter basically means ignore the cache and also use "unsigned=false" parameter.
     */
    private static final boolean REQUIRE_SECURE_PROFILES = false;

    /**
     * https://wiki.vg/Mojang_API#Username_to_UUID
     */
    private static final MinecraftClient USERNAME_TO_UUID = new MinecraftClient(
            "GET",
            "https://api.mojang.com/users/profiles/minecraft/",
            new RateLimiter(600, Duration.ofMinutes(10))
    );

    /**
     * https://wiki.vg/Mojang_API#Usernames_to_UUIDs
     * <p>
     * This API was previously accessed from {@code https://sessionserver.mojang.com/session/minecraft/profile/lookup/bulk/byname}.
     */
    private static final MinecraftClient USERNAMES_TO_UUIDS = new MinecraftClient(
            "POST",
            "https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname",
            new RateLimiter(600, Duration.ofMinutes(10))
    );

    /**
     * https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin.2FCape
     */
    private static final MinecraftClient UUID_TO_PROFILE = new MinecraftClient(
            "GET",
            "https://sessionserver.mojang.com/session/minecraft/profile/",
            new RateLimiter(200, Duration.ofMinutes(1))
    );

    /**
     * @return null if a player with that username is not found.
     */
    @Nullable
    public static UUID requestUsernameToUUID(@NotNull String username) throws IOException {
        JsonElement requestElement = USERNAME_TO_UUID.session(null).append(username).request();
        if (requestElement == null) return null;

        JsonObject userJson = requestElement.getAsJsonObject();
        JsonElement idElement = userJson.get("id");
        if (idElement == null)
            throw new IllegalArgumentException("No 'id' field for UUID request for '" + username + "': " + userJson);

        return PlayerUUIDs.UUIDFromDashlessString(idElement.getAsString());
    }

    /**
     * This method is here just for study purposes.
     * Retrieves a cached {@link GameProfile} by username from the user cache.
     * If the profile is not found in the cache, creates a new profile with the provided name.
     *
     * @param username The username of the profile to retrieve from the cache.
     * @return The cached {@link GameProfile} corresponding to the username, or a new profile if not found.
     */
    @SuppressWarnings("unused")
    @ApiStatus.Obsolete
    private static GameProfile getCachedProfileByUsername(String username) {
        try {
            // Expires after every month calendar.add(2, 1); (Persists between restarts)
            @Nullable Object profile = ProfilesCore.GameProfileCache_get$profileByName$.invoke(ProfilesCore.USER_CACHE, username);
            if (profile instanceof Optional) profile = ((Optional<?>) profile).orElse(null);
            GameProfile gameProfile = profile == null ?
                    PlayerProfiles.createGameProfile(PlayerUUIDs.IDENTITY_UUID, username) :
                    PlayerProfiles.sanitizeProfile((GameProfile) profile);
            ProfileLogger.debug("The cached profile for {} -> {}", username, profile);
            return gameProfile;
        } catch (Throwable throwable) {
            ProfileLogger.LOGGER.error("Unable to get cached profile by username: {}", username, throwable);
            return null;
        }
    }

    public static Optional<GameProfile> getMojangCachedProfileFromUsername(String username) {
        try {
            return getMojangCachedProfileFromUsername0(username);
        } catch (Throwable e) {
            throw XReflection.throwCheckedException(e);
        }
    }

    private static Optional<GameProfile> getMojangCachedProfileFromUsername0(String username) throws Throwable {
        // For unknown reasons Minecraft's default UserCache methods use Locale.ROOT
        String normalized = username.toLowerCase(Locale.ENGLISH); // A-Z 0-9 _
        Object userCacheEntry = ProfilesCore.UserCache_profilesByName.get(normalized);
        Optional<GameProfile> optional;

        // We are supposed to be doing UserCacheEntry#getExpiration() check here
        // but the cache already has a regular cleanup task (PlayerList#placeNewPlayer),
        // we don't need that much accuracy.

        if (userCacheEntry != null) {
            // The side effects of setLastAccess() is really insignificant, it's used for UserCache#getTopMRUProfiles()
            // (MRU = Most Recently Used) which saves game profile cache to usercache.json from most accessed to least accessed.
            // This is because of "settings.user-cache-size" spigot.yml option.
            if (ProfilesCore.UserCacheEntry_setLastAccess != null && ProfilesCore.UserCache_getNextOperation != null) {
                // usercache_usercacheentry.setLastAccess(this.getNextOperation());
                long nextOperation = (long) ProfilesCore.UserCache_getNextOperation.invoke(ProfilesCore.USER_CACHE);
                ProfilesCore.UserCacheEntry_setLastAccess.invoke(userCacheEntry, nextOperation);
            }
            optional = Optional.of((GameProfile) ProfilesCore.UserCacheEntry_getProfile.invoke(userCacheEntry));
        } else {
            // optional = lookupGameProfile(this.profileRepository, username); // CraftBukkit - use correct case for offline players
            UUID realUUID = PlayerUUIDs.getRealUUIDOfPlayer(username);
            if (realUUID == null) return Optional.empty();
            GameProfile profile = PlayerProfiles.createGameProfile(
                    PlayerUUIDs.isOnlineMode() ? realUUID : PlayerUUIDs.getOfflineUUID(username),
                    username
            );
            optional = Optional.of(profile);
            // this.add((GameProfile) optional.get());
            cacheProfile(profile);
        }

        return optional;
    }

    /**
     * Occasionally a {@code 401 - Unauthorized} or {@code 403 - Forbidden} error is received which is not
     * likely caused by rate-limiting because first of all, it'd return {@code 429} if that was the case,
     * and secondly, the code was run with a 6hr delay. It's currently unknown why this happens.
     * In these cases, the raw URL returns this JSON response:
     * <pre>{@code
     * {
     *   "path" : "/minecraft/profile/lookup/bulk/byname",
     *   "error" : "METHOD_NOT_ALLOWED",
     *   "errorMessage" : "Method Not Allowed"
     * }
     * }</pre>
     *
     * @param usernames Case-insensitive list of usernames. Duplicates are ignored and cached names are not requested but returned.
     * @param config    Request configuration.
     * @return Map of players that exist.
     */
    public static Map<UUID, String> usernamesToUUIDs(@NotNull Collection<String> usernames, @Nullable ProfileRequestConfiguration config) {
        if (usernames == null || usernames.isEmpty()) throw new IllegalArgumentException("Usernames are null or empty");
        for (String username : usernames) {
            if (username == null || !ProfileInputType.USERNAME.pattern.matcher(username).matches()) {
                throw new IllegalArgumentException("One of the requested usernames is invalid: " + username + " in " + usernames);
            }
        }

        Map<UUID, String> mapped = new HashMap<>(usernames.size());
        Set<String> finalUsernames = new HashSet<>(usernames);
        {
            // Remove duplicate & cached names
            // TODO - Perhaps we could add another list PlayerUUIDs.LOWERCASE_TO_USERNAME
            //        for a Map<String, String> to access case-corrected usernames.
            Iterator<String> usernameIter = finalUsernames.iterator();
            while (usernameIter.hasNext()) {
                String username = usernameIter.next();
                UUID cached = PlayerUUIDs.USERNAME_TO_ONLINE.get(username);
                if (cached != null) {
                    usernameIter.remove();
                    mapped.put(cached, username);
                }
            }
        }

        if (finalUsernames.isEmpty()) return mapped;
        boolean onlineMode = PlayerUUIDs.isOnlineMode();

        // For some reason, the YggdrasilGameProfileRepository partitions names in pairs instead of 10s.
        // It also "normalizes" names with lowercase and sends the request.
        // This API entry case-corrects the usernames in its response.
        Iterable<List<String>> partition = Iterables.partition(finalUsernames, 10);
        for (List<String> batch : partition) {
            JsonArray response;
            try {
                // The wiki says that:
                // BadRequestException is returned when any of the usernames is null or otherwise invalid
                // But I'm not sure what that means in this context... but invalid usernames are just ignored,
                // and no response is contained in the final result regarding them.
                response = USERNAMES_TO_UUIDS.session(config).body(batch).request().getAsJsonArray();
            } catch (IOException ex) {
                throw new MojangAPIException("Failed to request UUIDs for username batch: " + batch, ex);
            }

            for (JsonElement element : response) {
                JsonObject obj = element.getAsJsonObject();
                String name = obj.get("name").getAsString();
                UUID realId = PlayerUUIDs.UUIDFromDashlessString(obj.get("id").getAsString());
                UUID offlineId = PlayerUUIDs.getOfflineUUID(name);

                PlayerUUIDs.USERNAME_TO_ONLINE.put(name, realId);
                PlayerUUIDs.ONLINE_TO_OFFLINE.put(realId, offlineId);
                PlayerUUIDs.OFFLINE_TO_ONLINE.put(offlineId, realId);
                if (!ProfilesCore.UserCache_profilesByName.containsKey(name)) {
                    cacheProfile(PlayerProfiles.createGameProfile(onlineMode ? realId : offlineId, name));
                }

                String prev = mapped.put(realId, name);
                if (prev != null)
                    throw new IllegalArgumentException("Got duplicate usernames for UUID: " + realId + " (" + prev + " -> " + name + ')');
            }
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
    @NotNull
    public static GameProfile getCachedProfileByUUID(UUID uuid) {
        uuid = PlayerUUIDs.isOnlineMode() ? uuid : PlayerUUIDs.ONLINE_TO_OFFLINE.getOrDefault(uuid, uuid);
        try {
            @Nullable Object profile = ProfilesCore.GameProfileCache_get$profileByUUID$.invoke(ProfilesCore.USER_CACHE, uuid);
            if (profile instanceof Optional) profile = ((Optional<?>) profile).orElse(null);
            ProfileLogger.debug("The cached profile for {} -> {}", uuid, profile);
            return profile == null ?
                    PlayerProfiles.createNamelessGameProfile(uuid) :
                    PlayerProfiles.sanitizeProfile((GameProfile) profile);
        } catch (Throwable throwable) {
            ProfileLogger.LOGGER.error("Unable to get cached profile by UUID: {}", uuid, throwable);
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
            ProfileLogger.debug("Profile is now cached: {}", profile);
        } catch (Throwable throwable) {
            ProfileLogger.LOGGER.error("Unable to cache profile {}", profile);
            throwable.printStackTrace();
        }
    }


    /**
     * Fetches additional properties for the given {@link GameProfile} if possible (like the texture) and caches the result.
     *
     * @param profile The {@link GameProfile} for which properties are to be fetched.
     * @return The updated {@link GameProfile} with fetched properties, sanitized for consistency.
     * @throws UnknownPlayerException if a player with the specified profile properties (username and UUID) doesn't exist.
     */
    @NotNull
    public static GameProfile getOrFetchProfile(@NotNull final GameProfile profile) throws UnknownPlayerException {
        // Get real UUID for offline players
        UUID realUUID;
        if (profile.getName().equals(PlayerProfiles.XSERIES_SIG)) {
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
                throw new UnknownPlayerException(profile.getName(), "Player with the given properties not found: " + profile);
            }
        }

        GameProfile cached = handleCache(profile, realUUID);
        if (cached != null) return cached;

        JsonElement request = requestProfile(profile, realUUID);
        JsonObject profileData = request.getAsJsonObject();
        List<String> profileActions = new ArrayList<>();
        GameProfile fetchedProfile = createGameProfile(profileData, profileActions);

        fetchedProfile = PlayerProfiles.sanitizeProfile(fetchedProfile);
        cacheProfile(fetchedProfile);

        INSECURE_PROFILES.put(realUUID, Optional.of(fetchedProfile));
        MOJANG_PROFILE_CACHE.cache(new PlayerProfile(realUUID, profile, fetchedProfile, profileActions));

        return fetchedProfile;
    }

    @SuppressWarnings("OptionalAssignedToNull")
    private static @Nullable GameProfile handleCache(@NotNull GameProfile profile, UUID realUUID) {
        Optional<GameProfile> cached = INSECURE_PROFILES.getIfPresent(realUUID);
        // noinspection OptionalAssignedToNull
        if (cached != null) {
            ProfileLogger.debug("Found cached profile from UUID ({}): {} -> {}", realUUID, profile, cached);
            if (cached.isPresent()) return cached.get();
            else throw new UnknownPlayerException(realUUID, "Player with the given properties not found: " + profile);
        }

        Optional<GameProfile> mojangCache = MOJANG_PROFILE_CACHE.get(realUUID, profile);
        if (mojangCache != null) {
            INSECURE_PROFILES.put(realUUID, mojangCache);
            if (mojangCache.isPresent()) return mojangCache.get();
            else throw new UnknownPlayerException(realUUID, "Player with the given properties not found: " + profile);
        }
        return null;
    }

    private static @NotNull JsonElement requestProfile(@NotNull GameProfile profile, UUID realUUID) {
        JsonElement request;
        try {
            request = UUID_TO_PROFILE.session(null)
                    .append(PlayerUUIDs.toUndashedUUID(realUUID) + "?unsigned=" + !REQUIRE_SECURE_PROFILES)
                    .request();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to request profile: " + profile + " with real UUID: " + realUUID, e);
        }
        if (request == null) {
            INSECURE_PROFILES.put(realUUID, Optional.empty());
            MOJANG_PROFILE_CACHE.cache(new PlayerProfile(realUUID, profile, null, null));
            throw new UnknownPlayerException(realUUID, "Player with the given properties not found: " + profile);
        }
        return request;
    }

    private static @NotNull GameProfile createGameProfile(JsonObject profileData, List<String> profileActions) {
        // Two main fields, name and UUID
        UUID id = PlayerUUIDs.UUIDFromDashlessString(profileData.get("id").getAsString());
        String name = profileData.get("name").getAsString();
        GameProfile fetchedProfile = PlayerProfiles.createGameProfile(id, name);

        // Basic properties
        JsonElement propertiesEle = profileData.get("properties");
        if (propertiesEle != null) {
            JsonArray props = propertiesEle.getAsJsonArray();
            PropertyMap properties = fetchedProfile.getProperties();
            for (JsonElement prop : props) {
                JsonObject obj = prop.getAsJsonObject();
                String propName = obj.get("name").getAsString();
                String propValue = obj.get("value").getAsString();
                JsonElement sig = obj.get("signature");

                Property property;
                if (sig != null) {
                    property = new Property(propName, propValue, sig.getAsString());
                } else {
                    property = new Property(propName, propValue);
                }

                properties.put(propName, property);
            }
        }

        // Create profile actions
        JsonElement profileActionsElement = profileData.get("profileActions");
        if (profileActionsElement != null) {
            for (JsonElement action : profileActionsElement.getAsJsonArray()) {
                profileActions.add(action.getAsString());
            }
        }

        return fetchedProfile;
    }
}
