package com.cryptomorin.xseries.profiles.mojang;

import com.cryptomorin.xseries.profiles.PlayerProfiles;
import com.google.common.base.Strings;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileActionType;
import com.mojang.authlib.yggdrasil.ProfileResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A class to use and change the internal cache used for {@link GameProfile}.
 */
@ApiStatus.Internal
abstract class MojangProfileCache {
    abstract void cache(PlayerProfile playerProfile);

    /**
     * @return null if it's not in the cache. {@link Optional#empty()} if the player profile
     * didn't exist and that result was cached.
     */
    @Nullable
    abstract Optional<GameProfile> get(UUID realId, GameProfile gameProfile);

    protected static final class ProfileResultCache extends MojangProfileCache {
        private final LoadingCache<UUID, Optional<ProfileResult>> insecureProfiles;

        @SuppressWarnings("unchecked")
        ProfileResultCache(LoadingCache<?, ?> insecureProfiles) {
            this.insecureProfiles = (LoadingCache<UUID, Optional<ProfileResult>>) insecureProfiles;
        }

        @Override
        void cache(PlayerProfile playerProfile) {
            if (playerProfile.exists()) {
                ProfileResult profileResult = new ProfileResult(playerProfile.fetchedGameProfile,
                        playerProfile.profileActions.stream().map(x -> {
                            try {
                                return ProfileActionType.valueOf(x);
                            } catch (IllegalArgumentException ex) {
                                return null;
                            }
                        }).filter(Objects::nonNull).collect(Collectors.toSet()));
                insecureProfiles.put(playerProfile.realUUID, Optional.of(profileResult));
            } else {
                insecureProfiles.put(playerProfile.realUUID, Optional.empty());
            }
        }

        @SuppressWarnings("OptionalAssignedToNull")
        @Override
        Optional<GameProfile> get(UUID realId, GameProfile gameProfile) {
            Optional<ProfileResult> cache = insecureProfiles.getIfPresent(realId);
            return cache == null ? null : cache.map(ProfileResult::profile);
        }
    }

    @SuppressWarnings("OptionalAssignedToNull")
    protected static final class GameProfileCache extends MojangProfileCache {
        private final LoadingCache<GameProfile, GameProfile> insecureProfiles;

        @SuppressWarnings("unchecked")
        GameProfileCache(LoadingCache<?, ?> insecureProfiles) {
            this.insecureProfiles = (LoadingCache<GameProfile, GameProfile>) insecureProfiles;
        }

        @Override
        void cache(PlayerProfile playerProfile) {
            if (playerProfile.exists()) {
                insecureProfiles.put(playerProfile.requestedGameProfile, playerProfile.fetchedGameProfile);
            } else {
                insecureProfiles.put(playerProfile.requestedGameProfile, PlayerProfiles.NIL);
            }
        }

        @Override
        @Nullable
        Optional<GameProfile> get(UUID realId, GameProfile gameProfile) {
            // This is probably not going to work most of the time since the whole GameProfile
            // object is used to hash the key, and the name isn't always provided to us.
            String profileName = gameProfile.getName();
            if (Strings.isNullOrEmpty(profileName) || profileName.equals(PlayerProfiles.DEFAULT_PROFILE_NAME))
                return null;

            GameProfile cache = insecureProfiles.getIfPresent(new GameProfile(realId, gameProfile.getName()));
            if (cache == PlayerProfiles.NIL) return Optional.empty();
            return cache == null ? null : Optional.of(cache);
        }
    }
}
