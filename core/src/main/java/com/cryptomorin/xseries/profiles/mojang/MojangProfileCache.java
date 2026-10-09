/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2026 Crypto Morin
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
import com.cryptomorin.xseries.profiles.gameprofile.MojangGameProfile;
import com.cryptomorin.xseries.profiles.gameprofile.XGameProfile;
import com.cryptomorin.xseries.reflection.ReflectiveNamespace;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.classes.DynamicClassHandle;
import com.google.common.base.Strings;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.*;
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
        private static final MethodHandle ProfileResult_ctor;
        private static final MethodHandle ProfileResult_profile;
        private static final Map<String, Object /* ProfileActionType */> ProfileActionTypes = new HashMap<>();

        static {
            ReflectiveNamespace ns = XReflection.namespaced();
            ns.imports(GameProfile.class);

            DynamicClassHandle ProfileResult = ns.ofMinecraft()
                    .inPackage("com.mojang.authlib.services")
                    .named("ProfileResult");

            ProfileResult_ctor = ProfileResult.constructor("public ProfileResult(GameProfile profile, Set<ProfileActionType> actions)").unreflect();
            ProfileResult_profile = ProfileResult.method("public GameProfile profile()").unreflect();

            DynamicClassHandle ProfileActionType = ns.ofMinecraft()
                    .inPackage("com.mojang.authlib.services")
                    .named("ProfileActionType");
            for (Object actionType : ProfileActionType.unreflect().getEnumConstants()) {
                Enum<?> enumConstant = (Enum<?>) actionType;
                ProfileActionTypes.put(enumConstant.name(), actionType);
            }
        }

        private final LoadingCache<UUID, Optional<Object /* ProfileResult */>> insecureProfiles;

        @SuppressWarnings("unchecked")
        ProfileResultCache(LoadingCache<?, ?> insecureProfiles) {
            this.insecureProfiles = (LoadingCache<UUID, Optional<Object /* ProfileResult */>>) insecureProfiles;
        }

        @Override
        void cache(PlayerProfile playerProfile) {
            if (playerProfile.exists()) {
                Object profileResult;
                try {
                    Set<Object> actions = playerProfile.profileActions.stream()
                            .map(ProfileActionTypes::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    profileResult = ProfileResult_ctor.invoke(playerProfile.fetchedGameProfile, actions);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                insecureProfiles.put(playerProfile.realUUID, Optional.of(profileResult));
            } else {
                insecureProfiles.put(playerProfile.realUUID, Optional.empty());
            }
        }

        @SuppressWarnings("OptionalAssignedToNull")
        @Override
        Optional<GameProfile> get(UUID realId, GameProfile gameProfile) {
            Optional<Object /* ProfileResult */> cache = insecureProfiles.getIfPresent(realId);
            return cache == null ? null : cache.map(x -> {
                try {
                    return (GameProfile) ProfileResult_profile.invoke(x);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
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
            MojangGameProfile profile = XGameProfile.of(gameProfile);
            String profileName = profile.name();
            if (Strings.isNullOrEmpty(profileName) || profileName.equals(PlayerProfiles.XSERIES_SIG))
                return null;

            GameProfile cache = insecureProfiles.getIfPresent(XGameProfile.create(realId, profile.name()).object());
            if (cache == PlayerProfiles.NIL) return Optional.empty();
            return cache == null ? null : Optional.of(cache);
        }
    }
}
