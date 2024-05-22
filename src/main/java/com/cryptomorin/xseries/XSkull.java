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
package com.cryptomorin.xseries;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.MethodMemberHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftClassHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>XSkull</b> - Apply skull texture from different sources.<br><br>
 * Skull Meta: <a href="https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/meta/SkullMeta.html">hub.spigotmc.org/.../SkullMeta</a><br>
 * Mojang API: <a href="https://wiki.vg/Mojang_API">wiki.vg/Mojang_API</a><br><br>
 * <p>
 * Some websites to get custom heads:
 * <ul>
 *     <li><a href="https://minecraft-heads.com/">minecraft-heads.com</a></li>
 * </ul>
 * <p>
 * The basic premise behind this API is that the final skull data is contained in a {@link GameProfile}
 * either by ID, name or encoded textures URL property.
 * <p>
 * Different versions of Minecraft client handle this differently. In newer versions the client seem
 * to prioritize the texture property over the set UUID and name, in older versions however using the
 * same UUID for all GameProfiles caused all skulls (that use base64) to look the same.
 * The client is responsible for caching skull textures. If the download were to fail (either because of
 * connection issues or invalid values) the client will cache that skull UUID and the skull
 * will remain as a steve head until the client is completely restarted.
 * I don't know if this cache system works across other servers or is just specific to one server.
 *
 * @author Crypto Morin
 * @version 8.0.0
 * @see XMaterial
 * @see XReflection
 */
public final class XSkull {

    private static final Logger LOGGER = LogManager.getLogger("XSkull");
    private static final Object USER_CACHE, MINECRAFT_SESSION_SERVICE;

    private static final MethodHandle
        FILL_PROFILE_PROPERTIES, GET_PROFILE_BY_NAME, GET_PROFILE_BY_UUID, CACHE_PROFILE,
        CRAFT_META_SKULL_PROFILE_GETTER, CRAFT_META_SKULL_PROFILE_SETTER,
        CRAFT_META_SKULL_BLOCK_SETTER, PROPERTY_GET_VALUE;

    private static final ExecutorService PROFILE_EXECUTOR = Executors.newFixedThreadPool(2, new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger();
        @Override
        public Thread newThread(final @Nonnull Runnable run) {
            final Thread ret = new Thread(run);
            ret.setName("Profile Lookup Executor #" + this.count.getAndIncrement());
            ret.setUncaughtExceptionHandler((thread, throwable) ->
                    LOGGER.error("Uncaught exception in thread {}", thread.getName(), throwable));
            return ret;
        }
    });

    /**
     * Some people use this without quotes surrounding the keys, not sure what that'd work.
     */
    private static final String VALUE_PROPERTY = "{\"textures\":{\"SKIN\":{\"url\":\"";

    /**
     * We'll just return an x shaped hardcoded skull.<br>
     * <a href="https://minecraft-heads.com/custom-heads/miscellaneous/58141-cross">minecraft-heads.com</a>
     */
    private static final String INVALID_SKULL_VALUE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzEwNTkxZTY5MDllNmEyODFiMzcxODM2ZTQ2MmQ2N2EyYzc4ZmEwOTUyZTkxMGYzMmI0MWEyNmM0OGMxNzU3YyJ9fX0=";

    /**
     * They don't seem to use anything complicated, but the length is inconsistent for some reason.
     * It doesn't seem like uppercase characters are used either.
     */
    private static final Pattern MOJANG_SHA256_APPROX = Pattern.compile("[0-9a-z]{55,70}");

    /**
     * The ID and name of the GameProfiles are immutable, so we're good to cache them.
     * The key is the SHA value.
     */
    private static final Map<String, GameProfile> MOJANG_SHA_FAKE_PROFILES = new HashMap<>();
    /**
     * In v1.20.2 there were some changes to the Mojang API.
     * Before that version, both UUID and name fields couldn't be null, only one of them.
     * It gave the error: {@code Name and ID cannot both be blank}
     * Here, "blank" is null for UUID, and {@code Character.isWhitespace} for the name field.
     */
    private static final String GAME_PROFILE_DEFAULT_NAME = "XSeries";
    private static final UUID GAME_PROFILE_DEFAULT_UUID = new UUID(0, 0);
    private static final GameProfile DEFAULT_PROFILE = new GameProfile(GAME_PROFILE_DEFAULT_UUID, GAME_PROFILE_DEFAULT_NAME);
    private static final boolean NULLABILITY_RECORD_UPDATE = XReflection.supports(20, 2);

    /**
     * The value after this URL is probably an SHA-252 value that Mojang uses to unique identify player skins.
     * <br>
     * This <a href="https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin/Cape">wiki</a> documents how to
     * get base64 information from player's UUID.
     */
    private static final String TEXTURES = "https://textures.minecraft.net/texture/";

    static {
        Object userCache = null, minecraftSessionService = null;
        MethodHandle fillProfileProperties = null, getProfileByName = null, getProfileByUUID = null, cacheProfile = null;
        MethodHandle profileSetter = null, profileGetterFromMeta = null, propGetval = null;

        try {
            MinecraftClassHandle CraftMetaSkull = XReflection.ofMinecraft()
                    .inPackage(MinecraftPackage.CB, "inventory")
                    .named("CraftMetaSkull");
            profileGetterFromMeta = CraftMetaSkull.getterField().named("profile").returns(GameProfile.class).makeAccessible().reflect();

            try {
                // https://github.com/CryptoMorin/XSeries/issues/169
                profileSetter = CraftMetaSkull.method().named("setProfile")
                        .parameters(GameProfile.class)
                        .returns(void.class).makeAccessible().reflect();
            } catch (NoSuchMethodException e) {
                profileSetter = CraftMetaSkull.setterField().named("profile").returns(GameProfile.class).makeAccessible().reflect();
            }

            MinecraftClassHandle serverClassHandle = XReflection.ofMinecraft()
                    .inPackage(MinecraftPackage.NMS, "server")
                    .named("MinecraftServer");

            MinecraftClassHandle userCacheClassHandle = XReflection.ofMinecraft()
                    .inPackage(MinecraftPackage.NMS, "server.players")
                    .named("UserCache");

            Object minecraftServer = serverClassHandle.method()
                    .named("getServer")
                    .returns(serverClassHandle)
                    .reflect()
                    .invoke();

            minecraftSessionService = serverClassHandle.method()
                    .named("az", "ao", "am")
                    .returns(MinecraftSessionService.class)
                    .reflect()
                    .invoke(minecraftServer);

            userCache = serverClassHandle.method().named("getUserCache", "ar", "ap")
                    .returns(userCacheClassHandle).reflect()
                    .invoke(minecraftServer);

            if (!NULLABILITY_RECORD_UPDATE) {
                fillProfileProperties = XReflection.of(MinecraftSessionService.class).method()
                        .named("fillProfileProperties")
                        .parameters(GameProfile.class, boolean.class)
                        .returns(GameProfile.class)
                        .reflect();
            }
            MethodMemberHandle profileByName = userCacheClassHandle.method()
                    .named("getProfile", "a").parameters(String.class);
            MethodMemberHandle profileByUUID = userCacheClassHandle.method()
                    .named("a").parameters(UUID.class);
            try {
                getProfileByName = profileByName.returns(GameProfile.class).reflect();
                getProfileByUUID = profileByUUID.returns(GameProfile.class).reflect();
            } catch (Throwable throwable) {
                getProfileByName = profileByName.returns(Optional.class).reflect();
                getProfileByUUID = profileByUUID.returns(Optional.class).reflect();
            }

            cacheProfile = userCacheClassHandle.method()
                    .named("a")
                    .parameters(GameProfile.class)
                    .returns(void.class)
                    .reflect();

        } catch (Throwable throwable) {
            LOGGER.error("Unable to get required fields/methods", throwable);
        }

        MinecraftClassHandle CraftSkull = XReflection.ofMinecraft()
                .inPackage(MinecraftPackage.CB, "block")
                .named("CraftSkull");

        if (!XReflection.supports(20, 2)) {
            propGetval = XReflection.of(Property.class).method().named("getValue").returns(String.class).unreflect();
        }
        USER_CACHE = userCache;
        MINECRAFT_SESSION_SERVICE = minecraftSessionService;
        FILL_PROFILE_PROPERTIES = fillProfileProperties;
        GET_PROFILE_BY_NAME = getProfileByName;
        GET_PROFILE_BY_UUID = getProfileByUUID;
        CACHE_PROFILE = cacheProfile;
        PROPERTY_GET_VALUE = propGetval;
        CRAFT_META_SKULL_PROFILE_SETTER = profileSetter;
        CRAFT_META_SKULL_PROFILE_GETTER = profileGetterFromMeta;
        CRAFT_META_SKULL_BLOCK_SETTER = CraftSkull.setterField().named("profile").returns(GameProfile.class).makeAccessible().unreflect(); // CraftSkull private final GameProfile profile;
    }

    /**
     * Applies a skin to the player head ItemStack using the given meta and player UUID.<br>
     *
     * <pre>{@code
     *   ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
     *   XSkull.applySkinFromId(meta, uuid).thenAcceptAsync(updatedMeta -> {
     *      head.setItemMeta(updatedMeta);
     *      // Additional processing...
     *   }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
     * }</pre>
     *
     * @param meta The meta to apply the skin to.
     * @param uuid The UUID of the player whose skin will be applied.
     * @return A CompletableFuture representing the asynchronous operation.
     */
    @Nonnull
    public static CompletableFuture<SkullMeta> applySkinFromId(@Nonnull SkullMeta meta, @Nonnull UUID uuid) {
        assert ObjectUtils.allNotNull(meta, uuid) : "Arguments can not be null or empty";
        return profileFromId(uuid).thenApplyAsync((profile) -> {
            if (profile == DEFAULT_PROFILE) return meta;
            setProfile(meta, profile);
            return meta;
        }, PROFILE_EXECUTOR);
    }

    /**
     * Applies a skin to the player head ItemStack using the given meta and player name.<br>
     *
     * <pre>{@code
     *   ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
     *   XSkull.applySkinFromUsername(meta, uuid).thenAcceptAsync(updatedMeta -> {
     *      head.setItemMeta(updatedMeta);
     *      // Additional processing...
     *   }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
     * }</pre>
     *
     * @param meta The meta to apply the skin to.
     * @param name The name of the player whose skin will be applied.
     * @return A CompletableFuture representing the asynchronous operation.
     */
    @Nonnull
    public static CompletableFuture<SkullMeta> applySkinFromUsername(@Nonnull SkullMeta meta, @Nonnull String name) {
        assert ObjectUtils.allNotNull(meta, name) : "Arguments can not be null or empty";
        return profileFromUsername(name).thenApplyAsync((profile) -> {
            if (profile == DEFAULT_PROFILE) return meta;
            setProfile(meta, profile);
            return meta;
        }, PROFILE_EXECUTOR);
    }

    @Nonnull
    public static SkullMeta applySkinFromBase64(@Nonnull SkullMeta head, @Nonnull String value, String mojangSHA) {
        assert ObjectUtils.allNotNull(head, value) || !value.isEmpty() : "Arguments can not be null or empty";
        GameProfile profile = profileFromBase64(value, mojangSHA);
        setProfile(head, profile);
        return head;
    }

    /**
     * Directly setting the profile is not compatible with {@link SkullMeta#setOwningPlayer(OfflinePlayer)},
     * and should be reset by calling {@code setProfile(head, null)}.
     * <br><br>
     * Newer client versions give profiles a higher priority over UUID and name.
     */
    public static void setProfile(SkullMeta head, GameProfile profile) {
        try {
            CRAFT_META_SKULL_PROFILE_SETTER.invoke(head, profile);
        } catch (Throwable throwable) {
            LOGGER.error("Unable to set profile", throwable);
        }
    }

    @Nonnull
    public static GameProfile profileFromBase64(String base64, String mojangSHA) {
        GameProfile profile = MOJANG_SHA_FAKE_PROFILES.get(mojangSHA);
        if (profile != null) return profile;
        // Creates an id from its hash for consistency after restarts
        UUID uuid = UUID.nameUUIDFromBytes(mojangSHA.getBytes(StandardCharsets.UTF_8));
        profile = new GameProfile(uuid, GAME_PROFILE_DEFAULT_NAME);
        profile.getProperties().put("textures", new Property("textures", base64));
        MOJANG_SHA_FAKE_PROFILES.put(mojangSHA, profile);
        return profile;
    }

    /**
     * Applies a skin to a {@link SkullMeta} given an identifier.
     * The identifier can be a UUID, player username, texture Base64, texture URL, or texture hash.
     *
     * <br><br>
     * For UUID or player name identifiers, this method internally uses {@link CompletableFuture},
     * but is run on the current thread. <br><br>
     * The methods {@link #applySkinFromId(SkullMeta, UUID)} and
     * {@link #applySkinFromUsername(SkullMeta, String)} can be used to apply them asynchronously.
     * <br><br>
     *
     * @param head       The SkullMeta object to which the skin will be applied.
     * @param identifier The identifier representing a skin.
     * @return The modified SkullMeta object with the applied texture.
     */
    @Nonnull
    @SuppressWarnings("UnusedReturnValue")
    public static SkullMeta applySkin(@Nonnull ItemMeta head, @Nonnull String identifier) {
        SkullMeta meta = (SkullMeta) head;
        // @formatter:off
        SkullValue result = detectSkullValueType(identifier);
        try {
            switch (result.valueType) {
                case UUID:         return applySkinFromId(meta,                                      (UUID) result.object).get();
                case NAME:         return applySkinFromUsername(meta,                              (String) result.object).get();
                case BASE64:       return applySkinFromBase64(meta, identifier,                    (String) result.object);
                case TEXTURE_URL:  return applySkinFromBase64(meta, encodeTexturesURL(identifier), (String) result.object);
                case TEXTURE_HASH: return applySkinFromBase64(meta, encodeTexturesURL(TEXTURES + identifier),  identifier);
                case UNKNOWN:      return applySkinFromBase64(meta, INVALID_SKULL_VALUE,                      INVALID_SKULL_VALUE);
                default: throw new AssertionError("Unknown skull value");
            }
        // @formatter:on
        } catch (Throwable throwable) {
            LOGGER.error("Unable to apply skin", throwable);
            return meta;
        }
    }

    @Nonnull
    public static GameProfile detectProfileFromString(String identifier) {
        // @formatter:off sometimes programming is just art that a machine can't understand :)
        SkullValue result = detectSkullValueType(identifier);
        try {
            switch (result.valueType) {
                case UUID:         return profileFromId(                                                (UUID) result.object).get();
                case NAME:         return profileFromUsername(                                        (String) result.object).get();
                case BASE64:       return profileFromBase64(                             identifier,  (String) result.object);
                case TEXTURE_URL:  return profileFromBase64(encodeTexturesURL(           identifier), (String) result.object);
                case TEXTURE_HASH: return profileFromBase64(encodeTexturesURL(TEXTURES + identifier), identifier);
                case UNKNOWN:      return profileFromBase64(INVALID_SKULL_VALUE,                      INVALID_SKULL_VALUE);
                // This can't be cached because the caller might change it.
                default: throw new AssertionError("Unknown skull value");
            }
        // @formatter:on
        } catch (Throwable throwable) {
            LOGGER.error("Unable to get profile from input", throwable);
            return DEFAULT_PROFILE;
        }
    }

    @Nonnull
    public static SkullValue detectSkullValueType(@Nonnull String identifier) {
        try {
            UUID id = UUID.fromString(identifier);
            return new SkullValue(ValueType.UUID, id);
        } catch (IllegalArgumentException ignored) {
        }

        if (isUsername(identifier)) return new SkullValue(ValueType.NAME, identifier);
        if (identifier.contains("textures.minecraft.net")) {
            return new SkullValue(ValueType.TEXTURE_URL, extractMojangSHAFromBase64(identifier));
        }
        if (identifier.length() > 100) {
            String hash = decodeBase64(identifier).map(XSkull::extractMojangSHAFromBase64).orElse(null);
            if (hash != null) return new SkullValue(ValueType.BASE64, hash);
        }

        // We'll just "assume" that it's a textures.minecraft.net hash without the URL part.
        if (MOJANG_SHA256_APPROX.matcher(identifier).matches())
            return new SkullValue(ValueType.TEXTURE_HASH, identifier);

        return new SkullValue(ValueType.UNKNOWN, identifier);
    }

    @SuppressWarnings("unused")
    public static void setSkin(@Nonnull Block block, @Nonnull String value) {
        Objects.requireNonNull(block, "Can't set skin of null block");
        BlockState state = block.getState();
        setSkin(state, value);
        state.update(true);
    }

    public static void setSkin(@Nonnull BlockState state, @Nonnull String value) {
        setSkin(state, detectProfileFromString(value));
    }

    public static void setSkin(@Nonnull BlockState state, @Nonnull GameProfile profile) {
        Objects.requireNonNull(state, "Can't set skin of null block state");
        Objects.requireNonNull(profile, "Can't set skin of block with null GameProfile");

        if (!(state instanceof Skull))
            throw new IllegalArgumentException("Cannot set skin of a block that is not a skull: " + state);
        Skull skull = (Skull) state;

        try {
            CRAFT_META_SKULL_BLOCK_SETTER.invoke(skull, profile);
        } catch (Throwable e) {
            throw new RuntimeException("Error while setting block skin with value: " + state + " with profiel " + profile, e);
        }
    }

    @Nullable
    public static String getSkinValue(@Nonnull ItemMeta skull) {
        Objects.requireNonNull(skull, "Skull ItemStack cannot be null");
        try {
            GameProfile profile = (GameProfile) CRAFT_META_SKULL_PROFILE_GETTER.invoke((SkullMeta) skull);
            return Optional.ofNullable(Iterables.getFirst(profile.getProperties().get("textures"), null))
                    .map(XSkull::getPropertyValue).orElse(null);
        } catch (Throwable ex) {
            LOGGER.error("Unable to get profile from skull meta");
        }
        return null;
    }

    public static String encodeTexturesURL(String url) {
        // String.format bad!
        return encodeBase64(VALUE_PROPERTY + url + "\"}}}");
    }

    @Nonnull
    private static String encodeBase64(@Nonnull String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Tries to decode the string as a Base64 value.
     * @return the decoded Base64 string if it is a Base64 string.
     */
    private static Optional<String> decodeBase64(@Nonnull String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return Optional.of(new String(bytes, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private static CompletableFuture<GameProfile> profileFromId(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            GameProfile profile = getCachedProfileById(uuid);
            if (profile == DEFAULT_PROFILE || hasTextures(profile)) return profile;
            profile = fetchProfile(profile);
            return profile;
        }, PROFILE_EXECUTOR);
    }

    private static CompletableFuture<GameProfile> profileFromUsername(String name) {
        return CompletableFuture.supplyAsync(() -> {
            GameProfile profile = getCachedProfileByName(name);
            if (profile == DEFAULT_PROFILE || hasTextures(profile)) return profile;
            profile = fetchProfile(profile);
            return profile;
        }, PROFILE_EXECUTOR);
    }

    /**
     * They changed {@link Property} to a Java record in 1.20.2
     *
     * @since 4.0.1
     */
    private static String getPropertyValue(Property property) {
        if (NULLABILITY_RECORD_UPDATE) return property.value();
        try {
            return (String) PROPERTY_GET_VALUE.invoke(property);
        } catch (Throwable throwable) {
            LOGGER.error("Unable to get a texture value", throwable);
        }
        return null;
    }

    private static String extractMojangSHAFromBase64(String decodedBase64) {
        // Example: http://textures.minecraft.net/texture/e5461a215b325fbdf892db67b7bfb60ad2bf1580dc968a15dfb304ccd5e74db
        // Will not work reliably if NBT is passed: {"textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/74133f6ac3be2e2499a784efadcfffeb9ace025c3646ada67f3414e5ef3394"}}}
        Matcher matcher = MOJANG_SHA256_APPROX.matcher(decodedBase64);
        if (matcher.find()) return matcher.group();
        else throw new IllegalArgumentException("Invalid Base64 skull value: " + decodedBase64);
    }

    /**
     * <a href="https://web.archive.org/web/20200804174636/https://help.minecraft.net/hc/en-us/articles/360034636712">help.minecraft.net/.../360034636712</a>
     *
     * @param name the username to check.
     * @return true if the string matches the Minecraft username rule, otherwise false.
     */
    private static boolean isUsername(@Nonnull String name) {
        int len = name.length();
        if (len > 16) return false; // Yes, in the old Minecraft 1 letter usernames were a thing.

        // For some reason Apache's Lists.charactersOf is faster than character indexing for small strings.
        for (char ch : Lists.charactersOf(name)) {
            if (ch != '_' && !(ch >= 'A' && ch <= 'Z') && !(ch >= 'a' && ch <= 'z') && !(ch >= '0' && ch <= '9'))
                return false;
        }
        return true;
    }

    private static boolean hasTextures(GameProfile profile) {
        return Iterables.getFirst(profile.getProperties().get("textures"), null) != null;
    }

    private static GameProfile getCachedProfileByName(@Nonnull String name) {
        try {
            @Nullable Object profile = GET_PROFILE_BY_NAME.invoke(USER_CACHE, name);
            if (profile instanceof Optional) profile = ((Optional<?>) profile).orElse(null);
            return profile == null ? new GameProfile(GAME_PROFILE_DEFAULT_UUID, name) : sanitizeProfile((GameProfile) profile);
        } catch (Throwable throwable) {
            LOGGER.error("Unable to get profile by username", throwable);
            return DEFAULT_PROFILE;
        }
    }

    private static GameProfile getCachedProfileById(@Nonnull UUID uuid) {
        try {
            @Nullable Object profile = GET_PROFILE_BY_UUID.invoke(USER_CACHE, uuid);
            if (profile instanceof Optional) profile = ((Optional<?>) profile).orElse(null);
            return profile == null ? new GameProfile(uuid, GAME_PROFILE_DEFAULT_NAME) : sanitizeProfile((GameProfile) profile);
        } catch (Throwable throwable) {
            LOGGER.error("Unable to get profile by uuid", throwable);
            return DEFAULT_PROFILE;
        }
    }

    private static void cacheProfile(GameProfile profile) {
        try {
            CACHE_PROFILE.invoke(USER_CACHE, profile);
        } catch (Throwable throwable) {
            LOGGER.error("Unable to cache this profile", throwable);
        }
    }

    private static GameProfile fetchProfile(GameProfile profile) {
        if (!NULLABILITY_RECORD_UPDATE) {
            try {
                profile = (GameProfile) FILL_PROFILE_PROPERTIES.invoke(MINECRAFT_SESSION_SERVICE, profile, false);
            } catch (Throwable throwable) {
                LOGGER.error("Unable to fetch profile properties", throwable);
            }
        } else {
            com.mojang.authlib.yggdrasil.ProfileResult result = ((MinecraftSessionService) MINECRAFT_SESSION_SERVICE)
                    .fetchProfile(profile.getId(), false);
            if (result != null) profile = result.profile();
        }
        return sanitizeProfile(profile);
    }

    @SuppressWarnings("deprecation")
    private static GameProfile sanitizeProfile(GameProfile profile) {
        return Optional.ofNullable(Iterables.getFirst(profile.getProperties().get("textures"), null))
            .map(XSkull::getPropertyValue).flatMap(XSkull::decodeBase64)
            .map(decoded -> {
                JsonObject jsonObject = new JsonParser().parse(decoded).getAsJsonObject();
                // Remove timestamp for consistency after restarts
                if (!jsonObject.has("timestamp")) return profile;
                GameProfile clone = new GameProfile(profile.getId(), profile.getName());
                JsonObject texture = new JsonObject();
                texture.add("textures", jsonObject.get("textures"));
                String base64 = encodeBase64(texture.toString());
                clone.getProperties().put("textures", new Property("textures", base64));
                cacheProfile(clone);
                return clone;
            }).orElse(profile);
    }

    public static final class SkullValue {
        public final ValueType valueType;
        public final Object object;

        private SkullValue(ValueType valueType, Object object) {
            this.valueType = valueType;
            this.object = object;
        }
    }

    public enum ValueType {NAME, UUID, BASE64, TEXTURE_URL, TEXTURE_HASH, UNKNOWN}

}
