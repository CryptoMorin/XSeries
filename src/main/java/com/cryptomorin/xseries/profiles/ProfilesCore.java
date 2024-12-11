/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Crypto Morin
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

package com.cryptomorin.xseries.profiles;

import com.cryptomorin.xseries.reflection.ReflectiveNamespace;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.FieldMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.MethodMemberHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftClassHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftMapping;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.net.Proxy;
import java.util.Map;
import java.util.UUID;

import static com.cryptomorin.xseries.reflection.XReflection.v;

/**
 * Collection of NMS reflection needed to interact with the internal cache.
 */
@SuppressWarnings("unchecked")
@ApiStatus.Internal
public final class ProfilesCore {
    public static final Object USER_CACHE, MINECRAFT_SESSION_SERVICE;
    public static final Proxy PROXY;
    public static final LoadingCache<Object, Object> YggdrasilMinecraftSessionService_insecureProfiles;

    public static final Map<String, Object> UserCache_profilesByName;
    public static final Map<UUID, Object> UserCache_profilesByUUID;

    public static final MethodHandle
            MinecraftSessionService_fillProfileProperties, GameProfileCache_get$profileByName$, GameProfileCache_get$profileByUUID$, CACHE_PROFILE,
            CraftMetaSkull_profile$getter, CraftMetaSkull_profile$setter,
            CraftSkull_profile$setter, CraftSkull_profile$getter,
            Property_getValue,
            UserCache_getNextOperation,
            UserCacheEntry_getProfile, UserCacheEntry_setLastAccess,
            ResolvableProfile$constructor, ResolvableProfile_gameProfile;
    public static final boolean ResolvableProfile$bukkitSupports;

    /**
     * In v1.20.2, Mojang switched to {@code record} class types for their {@link Property} class.
     */
    public static final boolean NULLABILITY_RECORD_UPDATE = XReflection.supports(1, 20, 2);

    static {
        Object userCache, minecraftSessionService, insecureProfiles = null;
        Proxy proxy;
        MethodHandle fillProfileProperties = null, getProfileByName, getProfileByUUID, cacheProfile;
        MethodHandle profileSetterMeta, profileGetterMeta;
        MethodHandle newResolvableProfile = null, $ResolvableProfile_gameProfile = null;
        boolean bukkitUsesResolvableProfile = false;

        ReflectiveNamespace ns = XReflection.namespaced()
                .imports(GameProfile.class, MinecraftSessionService.class, LoadingCache.class);

        MinecraftClassHandle GameProfileCache = ns.ofMinecraft(
                "package nms.server.players; public class GameProfileCache {}"
        ).map(MinecraftMapping.SPIGOT, "UserCache");

        try {
            MinecraftClassHandle CraftMetaSkull = ns.ofMinecraft(
                    "package cb.inventory; class CraftMetaSkull extends CraftMetaItem implements SkullMeta {}"
            );

            // This class has existed since ~v1.20.5, however Bukkit has started using it in their
            // classes since Paper v1.20.1b78, its function is basically similar to our Profileable class.
            MinecraftClassHandle ResolvableProfile =
                    ns.ofMinecraft("package nms.world.item.component; public class ResolvableProfile {}");

            if (ResolvableProfile.exists()) {
                newResolvableProfile = ResolvableProfile.constructor("public ResolvableProfile(GameProfile gameProfile);").reflect();
                $ResolvableProfile_gameProfile = ResolvableProfile.method("public GameProfile gameProfile();")
                        .map(MinecraftMapping.OBFUSCATED, "f")
                        .reflect();

                bukkitUsesResolvableProfile = CraftMetaSkull.field("private ResolvableProfile profile;").exists();
            }

            // @formatter:off
            profileGetterMeta = XReflection.any(
                    CraftMetaSkull.field("private ResolvableProfile profile;"),
                    CraftMetaSkull.field("private GameProfile       profile;")
            ).modify(FieldMemberHandle::getter).reflect();
            // @formatter:on

            // @formatter:off
            // https://github.com/CryptoMorin/XSeries/issues/169
            // noinspection MethodMayBeStatic
            profileSetterMeta = XReflection.any(
                    CraftMetaSkull.method("private void setProfile(ResolvableProfile profile);"),
                    CraftMetaSkull.method("private void setProfile(GameProfile       profile);"),
                    CraftMetaSkull.field ("private                 GameProfile       profile ;").setter()
            ).reflect();
            // @formatter:on

            MinecraftClassHandle MinecraftServer = ns.ofMinecraft(
                    "package nms.server; public abstract class MinecraftServer {}"
            );

            // Added by Bukkit
            Object minecraftServer = MinecraftServer.method("public static MinecraftServer getServer();").reflect().invoke();

            minecraftSessionService = MinecraftServer.method("public MinecraftSessionService getSessionService();")
                    .named(/* 1.21.3 */ "aq", /* 1.19.4 */ "ay", /* 1.17.1 */ "getMinecraftSessionService", "az", "ao", "am", /* 1.20.4 */ "aD", /* 1.20.6 */ "ar")
                    .reflect().invoke(minecraftServer);

            {
                FieldMemberHandle insecureProfilesFieldHandle = ns.ofMinecraft("package com.mojang.authlib.yggdrasil;" +
                        "public class YggdrasilMinecraftSessionService implements MinecraftSessionService {}").field().getter();
                if (NULLABILITY_RECORD_UPDATE) {
                    insecureProfilesFieldHandle.signature("private final LoadingCache<UUID, Optional<ProfileResult>> insecureProfiles;");
                } else {
                    insecureProfilesFieldHandle.signature("private final LoadingCache<GameProfile, GameProfile> insecureProfiles;");
                }
                MethodHandle insecureProfilesField = insecureProfilesFieldHandle.reflectOrNull();
                if (insecureProfilesField != null) {
                    insecureProfiles = insecureProfilesField.invoke(minecraftSessionService);
                }
            }

            userCache = MinecraftServer.method("public GameProfileCache getProfileCache();")
                    .named("at", /* 1.21.3 */ "ar", /* 1.18.2 */ "ao", /* 1.20.4 */ "ap", /* 1.20.6 */ "au")
                    .map(MinecraftMapping.OBFUSCATED, /* 1.9.4 */ "getUserCache")
                    .reflect().invoke(minecraftServer);

            if (!NULLABILITY_RECORD_UPDATE) {
                fillProfileProperties = ns.of(MinecraftSessionService.class).method(
                        "public GameProfile fillProfileProperties(GameProfile profile, boolean flag);"
                ).reflect();
            }

            // noinspection MethodMayBeStatic
            UserCache_getNextOperation = GameProfileCache.method("private long getNextOperation();")
                    .map(MinecraftMapping.OBFUSCATED, v(21, "e").v(16, "d").orElse("d")).reflectOrNull();

            MethodMemberHandle profileByName = GameProfileCache.method().named(/* v1.17.1 */ "getProfile", "a");
            MethodMemberHandle profileByUUID = GameProfileCache.method().named(/* v1.17.1 */ "getProfile", "a");
            // @formatter:off
            getProfileByName = XReflection.anyOf(
                    () -> profileByName.signature("public          GameProfile  get(String username);"),
                    () -> profileByName.signature("public Optional<GameProfile> get(String username);")
            ).reflect();
            getProfileByUUID = XReflection.anyOf(
                    () -> profileByUUID.signature("public          GameProfile  get(UUID id);"),
                    () -> profileByUUID.signature("public Optional<GameProfile> get(UUID id);")
            ).reflect();
            // @formatter:on

            cacheProfile = GameProfileCache.method("public void add(GameProfile profile);")
                    .map(MinecraftMapping.OBFUSCATED, "a").reflect();

            try {
                // Some versions don't have the public getProxy() method. It's very very inconsistent...
                proxy = (Proxy) MinecraftServer.field("protected final java.net.Proxy proxy;").getter()
                        .map(MinecraftMapping.OBFUSCATED, v(20, 5, "h").v(20, 3, "i")
                                .v(19, "j")
                                .v(18, 2, "n").v(18, "o")
                                .v(17, "m")
                                .v(14, "proxy") // v1.14 -> v1.16
                                .v(13, "c").orElse(/* v1.8 and v1.9 */ "e"))
                        .reflect().invoke(minecraftServer);
            } catch (Throwable ex) {
                ProfileLogger.LOGGER.error("Failed to initialize server proxy settings", ex);
                proxy = null;
            }
        } catch (Throwable throwable) {
            throw XReflection.throwCheckedException(throwable);
        }

        MinecraftClassHandle CraftSkull = ns.ofMinecraft(
                "package cb.block; public class CraftSkull extends CraftBlockEntityState implements Skull {}"
        );

        FieldMemberHandle CraftSkull_profile = XReflection.any(
                CraftSkull.field("private ResolvableProfile profile;"),
                CraftSkull.field("private GameProfile profile;")
        ).getHandle();

        Property_getValue = NULLABILITY_RECORD_UPDATE ? null :
                ns.of(Property.class).method("public String getValue();").unreflect();

        PROXY = proxy;
        USER_CACHE = userCache;
        CACHE_PROFILE = cacheProfile;
        MINECRAFT_SESSION_SERVICE = minecraftSessionService;

        YggdrasilMinecraftSessionService_insecureProfiles = (LoadingCache<Object, Object>) insecureProfiles;
        MinecraftSessionService_fillProfileProperties = fillProfileProperties;

        GameProfileCache_get$profileByName$ = getProfileByName;
        GameProfileCache_get$profileByUUID$ = getProfileByUUID;

        CraftMetaSkull_profile$setter = profileSetterMeta;
        CraftMetaSkull_profile$getter = profileGetterMeta;

        CraftSkull_profile$setter = CraftSkull_profile.setter().unreflect();
        CraftSkull_profile$getter = CraftSkull_profile.getter().unreflect();

        ResolvableProfile$constructor = newResolvableProfile;
        ResolvableProfile_gameProfile = $ResolvableProfile_gameProfile;
        ResolvableProfile$bukkitSupports = bukkitUsesResolvableProfile;

        MinecraftClassHandle UserCacheEntry = GameProfileCache
                .inner("private static class GameProfileInfo {}")
                .map(MinecraftMapping.SPIGOT, "UserCacheEntry");

        UserCacheEntry_getProfile = UserCacheEntry.method("public GameProfile getProfile();")
                .map(MinecraftMapping.OBFUSCATED, "a").makeAccessible()
                .unreflect();
        UserCacheEntry_setLastAccess = UserCacheEntry.method("public void setLastAccess(long i);")
                .map(MinecraftMapping.OBFUSCATED, "a").reflectOrNull();

        try {
            // private final Map<String, UserCache.UserCacheEntry> profilesByName = Maps.newConcurrentMap();
            UserCache_profilesByName = (Map<String, Object>) GameProfileCache.field("private final Map<String, UserCache.UserCacheEntry> profilesByName;")
                    .getter().map(MinecraftMapping.OBFUSCATED, v(17, "e").v(16, 2, "c").v(9, "d").orElse("c"))
                    .reflect().invoke(userCache);
            // private final Map<UUID, UserCache.UserCacheEntry> profilesByUUID = Maps.newConcurrentMap();
            UserCache_profilesByUUID = (Map<UUID, Object>) GameProfileCache.field("private final Map<UUID, UserCache.UserCacheEntry> profilesByUUID;")
                    .getter().map(MinecraftMapping.OBFUSCATED, v(17, "f").v(16, 2, "d").v(9, "e").orElse("d"))
                    .reflect().invoke(userCache);

            // private final Deque<GameProfile> f = new LinkedBlockingDeque(); Removed in v1.16
            // MethodHandle deque = GameProfileCache.field("private final Deque<GameProfile> f;")
            //         .getter().reflectOrNull();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
