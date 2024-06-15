package com.cryptomorin.xseries.profiles;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.FieldMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.MethodMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.ReflectiveNamespace;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftClassHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftMapping;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.net.Proxy;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;

import static com.cryptomorin.xseries.reflection.XReflection.v;

@SuppressWarnings("unchecked")
@ApiStatus.Internal
public final class ProfilesCore {
    public static final Logger LOGGER = LogManager.getLogger("XSkull");
    public static final Object USER_CACHE, MINECRAFT_SESSION_SERVICE;
    public static final Proxy PROXY;

    public static final Map<String, Object> UserCache_profilesByName;
    public static final Map<UUID, Object> UserCache_profilesByUUID;
    public static final Deque<GameProfile> UserCache_gameProfiles;

    public static final MethodHandle
            FILL_PROFILE_PROPERTIES, GET_PROFILE_BY_NAME, GET_PROFILE_BY_UUID, CACHE_PROFILE,
            CRAFT_META_SKULL_PROFILE_GETTER, CRAFT_META_SKULL_PROFILE_SETTER,
            CRAFT_SKULL_PROFILE_SETTER, CRAFT_SKULL_PROFILE_GETTER,
            PROPERTY_GET_VALUE,
            UserCache_getNextOperation,
            UserCacheEntry_CTOR, UserCacheEntry_getProfile, UserCacheEntry_getExpirationDate, UserCacheEntry_setLastAccess;

    /**
     * In v1.20.2, Mojang switched to {@code record} class types for their {@link Property} class.
     */
    public static final boolean NULLABILITY_RECORD_UPDATE = XReflection.supports(1, 20, 2);

    static {
        Object userCache, minecraftSessionService;
        Proxy proxy;
        MethodHandle fillProfileProperties = null, getProfileByName, getProfileByUUID, cacheProfile;
        MethodHandle profileSetterMeta, profileGetterMeta, getPropertyValue = null;

        ReflectiveNamespace ns = XReflection.namespaced()
                .imports(GameProfile.class, MinecraftSessionService.class);


        MinecraftClassHandle GameProfileCache = ns.ofMinecraft(
                "package nms.server.players; public class GameProfileCache {}"
        ).map(MinecraftMapping.SPIGOT, "UserCache");

        try {
            MinecraftClassHandle CraftMetaSkull = ns.ofMinecraft(
                    "package cb.inventory; class CraftMetaSkull extends CraftMetaItem implements SkullMeta {}"
            );
            profileGetterMeta = CraftMetaSkull.field("private GameProfile profile;").getter().reflect();

            try {
                // https://github.com/CryptoMorin/XSeries/issues/169
                // noinspection MethodMayBeStatic
                profileSetterMeta = CraftMetaSkull.method("private void setProfile(GameProfile profile);").reflect();
            } catch (NoSuchMethodException e) {
                profileSetterMeta = CraftMetaSkull.field("private GameProfile profile;").setter().reflect();
            }

            MinecraftClassHandle MinecraftServer = ns.ofMinecraft(
                    "package nms.server; public abstract class MinecraftServer {}"
            );

            // Added by Bukkit
            Object minecraftServer = MinecraftServer.method("public static MinecraftServer getServer();").reflect().invoke();

            minecraftSessionService = MinecraftServer.method("public MinecraftSessionService getSessionService();")
                    .named(/* 1.19.4 */ "ay", /* 1.17.1 */ "getMinecraftSessionService", "az", "ao", "am", /* 1.20.4 */ "aD", /* 1.20.6 */ "ar")
                    .reflect().invoke(minecraftServer);

            userCache = MinecraftServer.method("public GameProfileCache getProfileCache();")
                    .named("ar", /* 1.18.2 */ "ao", /* 1.20.4 */ "ap", /* 1.20.6 */ "au")
                    .map(MinecraftMapping.OBFUSCATED, /* 1.9.4 */ "getUserCache")
                    .reflect().invoke(minecraftServer);

            if (!NULLABILITY_RECORD_UPDATE) {
                fillProfileProperties = ns.of(MinecraftSessionService.class).method(
                        "public GameProfile fillProfileProperties(GameProfile profile, boolean flag);"
                ).reflect();
            }

            MethodMemberHandle profileByName = GameProfileCache.method().named(/* v1.17.1 */ "getProfile", "a");
            MethodMemberHandle profileByUUID = GameProfileCache.method().named(/* v1.17.1 */ "getProfile", "a");
            getProfileByName = XReflection.anyOf(
                    () -> profileByName.signature("public GameProfile get(String username);"),
                    () -> profileByName.signature("public Optional<GameProfile> get(String username);")
            ).reflect();
            getProfileByUUID = XReflection.anyOf(
                    () -> profileByUUID.signature("public GameProfile get(UUID id);"),
                    () -> profileByUUID.signature("public Optional<GameProfile> get(UUID id);")
            ).reflect();

            cacheProfile = GameProfileCache.method("public void add(GameProfile profile);")
                    .map(MinecraftMapping.OBFUSCATED, "a").reflect();

            // Some versions don't have the public getProxy() method. It's very inconsistent...
            proxy = (Proxy) MinecraftServer.field("protected final java.net.Proxy proxy;").getter()
                    .map(MinecraftMapping.OBFUSCATED, v(20, "h").v(17, "m")
                            .v(14, "proxy") // v1.14 -> v1.16
                            .v(13, "c").orElse(/* v1.8 and v1.9 */ "e"))
                    .unreflect().invoke(minecraftServer);
        } catch (Throwable throwable) {
            throw XReflection.throwCheckedException(throwable);
        }

        MinecraftClassHandle CraftSkull = ns.ofMinecraft(
                "package cb.block; public class CraftSkull extends CraftBlockEntityState implements Skull {}"
        );

        FieldMemberHandle craftProfile = CraftSkull.field("private GameProfile profile;");

        if (!NULLABILITY_RECORD_UPDATE) {
            getPropertyValue = ns.of(Property.class).method("public String getValue();").unreflect();
        }

        PROXY = proxy;
        USER_CACHE = userCache;
        MINECRAFT_SESSION_SERVICE = minecraftSessionService;
        FILL_PROFILE_PROPERTIES = fillProfileProperties;
        GET_PROFILE_BY_NAME = getProfileByName;
        GET_PROFILE_BY_UUID = getProfileByUUID;
        CACHE_PROFILE = cacheProfile;
        PROPERTY_GET_VALUE = getPropertyValue;
        CRAFT_META_SKULL_PROFILE_SETTER = profileSetterMeta;
        CRAFT_META_SKULL_PROFILE_GETTER = profileGetterMeta;
        CRAFT_SKULL_PROFILE_SETTER = craftProfile.setter().unreflect();
        CRAFT_SKULL_PROFILE_GETTER = craftProfile.getter().unreflect();

        // noinspection MethodMayBeStatic
        UserCache_getNextOperation = GameProfileCache.method("private long getNextOperation();")
                .map(MinecraftMapping.OBFUSCATED, v(21, "e").v(16, "d").orElse("d")).unreflect();

        MinecraftClassHandle UserCacheEntry = GameProfileCache
                .inner("private static class GameProfileInfo {}")
                .map(MinecraftMapping.SPIGOT, "UserCacheEntry");
        UserCacheEntry_CTOR = UserCacheEntry.constructor("private UserCacheEntry(GameProfile gameprofile, Date date);")
                .unreflect();
        UserCacheEntry_getProfile = UserCacheEntry.method("public GameProfile getProfile();")
                .map(MinecraftMapping.OBFUSCATED, "a").unreflect();
        UserCacheEntry_getExpirationDate = UserCacheEntry.method("public Date getExpirationDate();")
                .map(MinecraftMapping.OBFUSCATED, "b").unreflect();
        UserCacheEntry_setLastAccess = UserCacheEntry.method("public void setLastAccess(long i);")
                .map(MinecraftMapping.OBFUSCATED, "a").reflectOrNull();

        try {
            // private final Map<String, UserCache.UserCacheEntry> profilesByName = Maps.newConcurrentMap();
            UserCache_profilesByName = (Map<String, Object>) GameProfileCache.field("private final Map<String, UserCache.UserCacheEntry> profilesByName;")
                    .getter().map(MinecraftMapping.OBFUSCATED, v(21, "e").v(16, "c").orElse("d"))
                    .reflect().invoke(userCache);
            // private final Map<UUID, UserCache.UserCacheEntry> profilesByUUID = Maps.newConcurrentMap();
            UserCache_profilesByUUID = (Map<UUID, Object>) GameProfileCache.field("private final Map<UUID, UserCache.UserCacheEntry> profilesByUUID;")
                    .getter().map(MinecraftMapping.OBFUSCATED, v(21, "f").v(16, "d").orElse("e"))
                    .reflect().invoke(userCache);

            // private final Deque<GameProfile> f = new LinkedBlockingDeque(); Removed in v1.16
            MethodHandle deque = GameProfileCache.field("private final Deque<GameProfile> f;")
                    .getter().reflectOrNull();
            UserCache_gameProfiles = deque == null ? null : (Deque<GameProfile>) deque.invoke(userCache);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void debug(String mainMessage, Object... variables) {
        LOGGER.debug(mainMessage, variables);
    }
}
