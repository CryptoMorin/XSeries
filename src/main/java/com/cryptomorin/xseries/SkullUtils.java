/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Crypto Morin
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

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * <b>SkullUtils</b> - Apply skull texture from different sources.<br>
 * Skull Meta: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/meta/SkullMeta.html
 * Mojang API: https://wiki.vg/Mojang_API
 * <p>
 * Some websites to get custom heads:
 * <ul>
 *     <li>https://minecraft-heads.com/</li>
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
 * @version 5.0.0
 * @see XMaterial
 * @see ReflectionUtils
 */
public class SkullUtils {
    protected static final MethodHandle
            CRAFT_META_SKULL_PROFILE_GETTER, CRAFT_META_SKULL_PROFILE_SETTER,
            CRAFT_META_SKULL_BLOCK_SETTER, PROPERTY_GETVALUE;

    /**
     * Some people use this without quotes surrounding the keys, not sure what that'd work.
     */
    private static final String VALUE_PROPERTY = "{\"textures\":{\"SKIN\":{\"url\":\"";
    private static final boolean SUPPORTS_UUID = ReflectionUtils.supports(12);

    /**
     * We'll just return an x shaped hardcoded skull.
     * https://minecraft-heads.com/custom-heads/miscellaneous/58141-cross
     */
    private static final String INVALID_BASE64 =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzEwNTkxZTY5MDllNmEyODFiMzcxODM2ZTQ2MmQ2N2EyYzc4ZmEwOTUyZTkxMGYzMmI0MWEyNmM0OGMxNzU3YyJ9fX0=";

    /**
     * They don't seem to use anything complicated, but the length is inconsistent for some reasons.
     * It doesn't seem like uppercase characters are used either.
     */
    private static final Pattern MOJANG_SHA256_APPROX = Pattern.compile("[0-9a-z]{60,70}");

    private static final AtomicLong MOJANG_SHA_FAKE_ID_ENUMERATOR = new AtomicLong(1);

    /**
     * The ID and name of the GameProfiles are immutable, so we're good to cache them.
     * The key is the SHA value.
     */
    private static final Map<String, GameProfile> MOJANG_SHA_FAKE_PROFILES = new HashMap<>();
    private static final Map<String, GameProfile> NULL_PLAYERS = new HashMap<>();

    /**
     * In v1.20.2 there were some changes to the mojang API.
     * Before that version both UUID and name fields couldn't be null, only one of them.
     * It gave the error: {@code Name and ID cannot both be blank}
     * Here, "blank" is null for UUID, and {@code Character.isWhitespace} for the name field.
     */
    private static final boolean NULLABILITY_RECORD_UPDATE = ReflectionUtils.supports(20, 2);
    private static final UUID IDENTITY_UUID = new UUID(0, 0);
    private static final GameProfile NULL_PROFILE = new GameProfile(IDENTITY_UUID, "");
    /**
     * Does using a random UUID have any advantage?
     */
    private static final UUID GAME_PROFILE_EMPTY_UUID = NULLABILITY_RECORD_UPDATE ? IDENTITY_UUID : null;
    private static final String GAME_PROFILE_EMPTY_NAME = NULLABILITY_RECORD_UPDATE ? "" : null;

    /**
     * The value after this URL is probably an SHA-252 value that Mojang uses to unique identify player skins.
     * <br>
     * This <a href="https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin/Cape">wiki</a> documents how to
     * get base64 information from player's UUID.
     */
    private static final String TEXTURES = "https://textures.minecraft.net/texture/";

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle profileSetter = null, profileGetter = null, blockSetter = null, propGetval = null;

        try {
            Class<?> CraftMetaSkull = ReflectionUtils.getCraftClass("inventory.CraftMetaSkull");
            Field profile = CraftMetaSkull.getDeclaredField("profile");
            profile.setAccessible(true);
            profileGetter = lookup.unreflectGetter(profile);

            try {
                // https://github.com/CryptoMorin/XSeries/issues/169
                Method setProfile = CraftMetaSkull.getDeclaredMethod("setProfile", GameProfile.class);
                setProfile.setAccessible(true);
                profileSetter = lookup.unreflect(setProfile);
            } catch (NoSuchMethodException e) {
                profileSetter = lookup.unreflectSetter(profile);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            // CraftSkull private GameProfile profile;
            Class<?> CraftSkullBlock = ReflectionUtils.getCraftClass("block.CraftSkull");
            Field field = CraftSkullBlock.getDeclaredField("profile");
            field.setAccessible(true);
            blockSetter = lookup.unreflectSetter(field);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (!ReflectionUtils.supports(20, 2)) {
            try {
                //noinspection JavaLangInvokeHandleSignature
                propGetval = lookup.findVirtual(Property.class, "getValue", MethodType.methodType(String.class));
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        PROPERTY_GETVALUE = propGetval;
        CRAFT_META_SKULL_PROFILE_SETTER = profileSetter;
        CRAFT_META_SKULL_PROFILE_GETTER = profileGetter;
        CRAFT_META_SKULL_BLOCK_SETTER = blockSetter;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    public static ItemStack getSkull(@Nonnull UUID id) {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        OfflinePlayer player = Bukkit.getOfflinePlayer(id);
        if (SUPPORTS_UUID) meta.setOwningPlayer(player);
        else meta.setOwner(player.getName());

        head.setItemMeta(meta);
        return head;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    public static SkullMeta applySkin(@Nonnull ItemMeta head, @Nonnull OfflinePlayer identifier) {
        SkullMeta meta = (SkullMeta) head;
        if (SUPPORTS_UUID) {
            meta.setOwningPlayer(identifier);
        } else {
            meta.setOwner(identifier.getName());
        }
        return meta;
    }

    @Nonnull
    public static SkullMeta applySkin(@Nonnull ItemMeta head, @Nonnull UUID identifier) {
        return applySkin(head, Bukkit.getOfflinePlayer(identifier));
    }

    @SuppressWarnings("deprecation")
    private static SkullMeta applySkinFromName(SkullMeta head, String name) {
        GameProfile nullPlayer = NULL_PLAYERS.get(name);
        if (nullPlayer == NULL_PROFILE) {
            // There's no point in changing anything.
            return head;
        } else {
            // CraftServer#getOfflinePlayer() trick
            OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            UUID nullUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));

            if (player.getUniqueId().equals(nullUUID)) {
                NULL_PLAYERS.put(name, NULL_PROFILE);
                return head;
            } else {
                return applySkin(head, player);
            }
        }
    }

    /**
     * @param identifier Can be a player name, player UUID, Base64, or a minecraft.net skin link.
     */
    @Nonnull
    public static SkullMeta applySkin(@Nonnull ItemMeta head, @Nonnull String identifier) {
        SkullMeta meta = (SkullMeta) head;
        // @formatter:off
        StringSkullCache result = detectSkullValueType(identifier);
        switch (result.valueType) {
            case UUID: return applySkin(meta, Bukkit.getOfflinePlayer((UUID) result.object));
            case NAME: return applySkinFromName(meta, identifier);
            case BASE64:       return setSkullBase64(meta, identifier,                               extractMojangSHAFromBase64((String) result.object));
            case TEXTURE_URL:  return setSkullBase64(meta, encodeTexturesURL(identifier),            extractMojangSHAFromBase64(identifier));
            case TEXTURE_HASH: return setSkullBase64(meta, encodeTexturesURL(TEXTURES + identifier), identifier);
            case UNKNOWN:      return setSkullBase64(meta, INVALID_BASE64,                           INVALID_BASE64);
            default: throw new AssertionError("Unknown skull value");
        }
        // @formatter:on
    }

    @Nonnull
    public static SkullMeta setSkullBase64(@Nonnull SkullMeta head, @Nonnull String value, String MojangSHA) {
        if (value == null || value.isEmpty()) throw new IllegalArgumentException("Skull value cannot be null or empty");
        GameProfile profile = profileFromBase64(value, MojangSHA);

        try {
            CRAFT_META_SKULL_PROFILE_SETTER.invoke(head, profile);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return head;
    }

    @Nonnull
    public static GameProfile profileFromBase64(String base64, String MojangSHA) {
        // Use an empty string instead of null for the name parameter because it's now null-checked since 1.20.2.
        // It doesn't seem to affect functionality.
        GameProfile gp = MOJANG_SHA_FAKE_PROFILES.get(MojangSHA);
        if (gp != null) return gp;

        gp = new GameProfile(
                NULLABILITY_RECORD_UPDATE ? GAME_PROFILE_EMPTY_UUID : new UUID(MOJANG_SHA_FAKE_ID_ENUMERATOR.getAndIncrement(), 0), // UUID.randomUUID()
                GAME_PROFILE_EMPTY_NAME);
        gp.getProperties().put("textures", new Property("textures", base64));
        MOJANG_SHA_FAKE_PROFILES.put(MojangSHA, gp);
        return gp;
    }

    @Nonnull
    public static GameProfile profileFromPlayer(OfflinePlayer player) {
        return new GameProfile(player.getUniqueId(), player.getName());
    }

    @Nonnull
    public static GameProfile detectProfileFromString(String identifier) {
        // @formatter:off sometimes programming is just art that a machine can't understand :)
        StringSkullCache result = detectSkullValueType(identifier);
        switch (result.valueType) {
            case UUID:         return new GameProfile((UUID) result.object,          GAME_PROFILE_EMPTY_NAME);
            case NAME:         return new GameProfile(GAME_PROFILE_EMPTY_UUID,       identifier);
            case BASE64:       return profileFromBase64(                             identifier,  extractMojangSHAFromBase64((String) result.object));
            case TEXTURE_URL:  return profileFromBase64(encodeTexturesURL(           identifier), extractMojangSHAFromBase64(identifier));
            case TEXTURE_HASH: return profileFromBase64(encodeTexturesURL(TEXTURES + identifier), identifier);
            case UNKNOWN:      return profileFromBase64(INVALID_BASE64,                           INVALID_BASE64); // This can't be cached because the caller might change it.
            default: throw new AssertionError("Unknown skull value");
        }
        // @formatter:on
    }

    @Nonnull
    public static StringSkullCache detectSkullValueType(@Nonnull String identifier) {
        try {
            UUID id = UUID.fromString(identifier);
            return new StringSkullCache(ValueType.UUID, id);
        } catch (IllegalArgumentException ignored) {
        }

        if (isUsername(identifier)) return new StringSkullCache(ValueType.NAME);
        if (identifier.contains("textures.minecraft.net")) return new StringSkullCache(ValueType.TEXTURE_URL);
        if (identifier.length() > 100) {
            byte[] decoded = isBase64(identifier);
            if (decoded != null) return new StringSkullCache(ValueType.BASE64, new String(decoded));
        }

        // We'll just "assume" that it's a textures.minecraft.net hash without the URL part.
        if (MOJANG_SHA256_APPROX.matcher(identifier).matches()) return new StringSkullCache(ValueType.TEXTURE_HASH);

        return new StringSkullCache(ValueType.UNKNOWN);
    }

    public static void setSkin(@Nonnull Block block, @Nonnull String value) {
        Objects.requireNonNull(block, "Can't set skin of null block");

        BlockState state = block.getState();
        if (!(state instanceof Skull)) return;
        Skull skull = (Skull) state;

        GameProfile profile = detectProfileFromString(value);
        try {
            CRAFT_META_SKULL_BLOCK_SETTER.invoke(skull, profile);
        } catch (Throwable e) {
            throw new RuntimeException("Error while setting block skin with value: " + value, e);
        }

        skull.update(true);
    }

    public static String encodeTexturesURL(String url) {
        // String.format bad!
        return encodeBase64(VALUE_PROPERTY + url + "\"}}}");
    }

    @Nonnull
    private static String encodeBase64(@Nonnull String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    /**
     * While RegEx is a little faster for small strings, this always checks strings with a length
     * greater than 100, so it'll perform a lot better.
     */
    private static byte[] isBase64(@Nonnull String base64) {
        try {
            return Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        //return BASE64.matcher(base64).matches();
    }

    @Nullable
    public static String getSkinValue(@Nonnull ItemMeta skull) {
        Objects.requireNonNull(skull, "Skull ItemStack cannot be null");
        SkullMeta meta = (SkullMeta) skull;
        GameProfile profile = null;

        try {
            profile = (GameProfile) CRAFT_META_SKULL_PROFILE_GETTER.invoke(meta);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        if (profile != null && !profile.getProperties().get("textures").isEmpty()) {
            for (Property property : profile.getProperties().get("textures")) {
                String value = getPropertyValue(property);
                if (!value.isEmpty()) return value;
            }
        }

        return null;
    }

    /**
     * They changed {@link Property} to a Java record in 1.20.2
     *
     * @since 4.0.1
     */
    private static String getPropertyValue(Property property) {
        if (NULLABILITY_RECORD_UPDATE) {
            return property.value();
        } else {
            try {
                return (String) PROPERTY_GETVALUE.invoke(property);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String extractMojangSHAFromBase64(String decodedBase64) {
        // Example: {"textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/74133f6ac3be2e2499a784efadcfffeb9ace025c3646ada67f3414e5ef3394"}}}
        int startIndex = decodedBase64.lastIndexOf('/');
        int endIndex = decodedBase64.lastIndexOf('"');

        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            throw new IllegalArgumentException("Invalid Base64 skull value: " + decodedBase64);
        }

        try {
            return decodedBase64.substring(startIndex + 1, endIndex);
        } catch (IndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("Invalid Base64 skull value: " + decodedBase64, ex);
        }
    }

    private static final class StringSkullCache {
        private final ValueType valueType;
        private final Object object;

        private StringSkullCache(ValueType valueType) {
            this(valueType, null);
        }

        private StringSkullCache(ValueType valueType, Object object) {
            this.valueType = valueType;
            this.object = object;
        }
    }

    /**
     * https://help.minecraft.net/hc/en-us/articles/360034636712
     *
     * @param name the username to check.
     * @return true if the string matches the Minecraft username rule, otherwise false.
     */
    private static boolean isUsername(@Nonnull String name) {
        int len = name.length();
        if (len > 16) return false; // Yes, in the old Minecraft 1 letter usernames were a thing.

        // For some reasons Apache's Lists.charactersOf is faster than character indexing for small strings.
        for (char ch : Lists.charactersOf(name)) {
            if (ch != '_' && !(ch >= 'A' && ch <= 'Z') && !(ch >= 'a' && ch <= 'z') && !(ch >= '0' && ch <= '9'))
                return false;
        }
        return true;
    }

    public enum ValueType {NAME, UUID, BASE64, TEXTURE_URL, TEXTURE_HASH, UNKNOWN}
}
