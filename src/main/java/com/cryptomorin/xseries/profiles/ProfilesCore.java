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

import java.lang.invoke.MethodHandle;

public final class ProfilesCore {
    protected static final Logger LOGGER = LogManager.getLogger("XSkull");
    protected static final Object USER_CACHE, MINECRAFT_SESSION_SERVICE;

    protected static final MethodHandle
            FILL_PROFILE_PROPERTIES, GET_PROFILE_BY_NAME, GET_PROFILE_BY_UUID, CACHE_PROFILE,
            CRAFT_META_SKULL_PROFILE_GETTER, CRAFT_META_SKULL_PROFILE_SETTER,
            CRAFT_SKULL_PROFILE_SETTER, CRAFT_SKULL_PROFILE_GETTER,
            PROPERTY_GET_VALUE;

    /**
     * In v1.20.2, Mojang switched to {@code record} class types for their {@link Property} class.
     */
    public static final boolean NULLABILITY_RECORD_UPDATE = XReflection.supports(20, 2);

    static {
        Object userCache, minecraftSessionService;
        MethodHandle fillProfileProperties = null, getProfileByName, getProfileByUUID, cacheProfile;
        MethodHandle profileSetterMeta, profileGetterMeta, getPropertyValue = null;

        ReflectiveNamespace ns = XReflection.namespaced()
                .imports(GameProfile.class, MinecraftSessionService.class);
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

            MinecraftClassHandle GameProfileCache = ns.ofMinecraft(
                    "package nms.server.players; public class GameProfileCache {}"
            ).map(MinecraftMapping.SPIGOT, "UserCache");

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
    }

    public static void debug(String mainMessage, Object... variables) {
        LOGGER.debug(mainMessage, variables);
    }
}
