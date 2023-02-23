/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Crypto Morin
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
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
 *
 * @author Crypto Morin
 * @version 4.0.0
 * @see XMaterial
 * @see ReflectionUtils
 * @see SkullCacheListener
 */
public class SkullUtils {
    protected static final MethodHandle
            CRAFT_META_SKULL_PROFILE_GETTER, CRAFT_META_SKULL_PROFILE_SETTER,
            CRAFT_META_SKULL_BLOCK_SETTER;

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

    /**
     * The value after this URL is probably an SHA-252 value that Mojang uses to unique identify player skins.
     * <br>
     * This <a href="https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin/Cape">wiki</a> documents how to
     * get base64 information from player's UUID.
     */
    private static final String TEXTURES = "https://textures.minecraft.net/texture/";

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle profileSetter = null, profileGetter = null, blockSetter = null;

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
        } catch (NoSuchFieldException | IllegalAccessException e) {
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

        CRAFT_META_SKULL_PROFILE_SETTER = profileSetter;
        CRAFT_META_SKULL_PROFILE_GETTER = profileGetter;
        CRAFT_META_SKULL_BLOCK_SETTER = blockSetter;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    public static ItemStack getSkull(@Nonnull UUID id) {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (SUPPORTS_UUID) meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
        else meta.setOwner(Bukkit.getOfflinePlayer(id).getName());

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
    @Nonnull
    public static SkullMeta applySkin(@Nonnull ItemMeta head, @Nonnull String identifier) {
        SkullMeta meta = (SkullMeta) head;
        // @formatter:off
        switch (detectSkullValueType(identifier)) {
            case UUID: return applySkin(head, Bukkit.getOfflinePlayer(UUID.fromString(identifier)));
            case NAME: return applySkin(head, Bukkit.getOfflinePlayer(identifier));
            case BASE64:       return setSkullBase64(meta, identifier);
            case TEXTURE_URL:  return setSkullBase64(meta, encodeTexturesURL(identifier));
            case TEXTURE_HASH: return setSkullBase64(meta, encodeTexturesURL(TEXTURES + identifier));
            case UNKNOWN:      return setSkullBase64(meta, INVALID_BASE64);
            default: throw new AssertionError("Unknown skull value");
        }
        // @formatter:on
    }

    @Nonnull
    protected static SkullMeta setSkullBase64(@Nonnull SkullMeta head, @Nonnull String value) {
        if (value == null || value.isEmpty()) throw new IllegalArgumentException("Skull value cannot be null or empty");
        GameProfile profile = profileFromBase64(value);

        try {
            CRAFT_META_SKULL_PROFILE_SETTER.invoke(head, profile);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return head;
    }

    @Nonnull
    public static GameProfile profileFromBase64(String value) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", value));
        return profile;
    }

    @Nonnull
    public static GameProfile profileFromPlayer(OfflinePlayer player) {
        return new GameProfile(player.getUniqueId(), player.getName());
    }

    @Nonnull
    public static GameProfile detectProfileFromString(String identifier) {
        // @formatter:off sometimes programming is just art that a machine can't understand :)
        switch (detectSkullValueType(identifier)) {
            case UUID:         return new GameProfile(UUID.fromString(               identifier), null);
            case NAME:         return new GameProfile(null,                          identifier);
            case BASE64:       return profileFromBase64(                             identifier);
            case TEXTURE_URL:  return profileFromBase64(encodeTexturesURL(           identifier));
            case TEXTURE_HASH: return profileFromBase64(encodeTexturesURL(TEXTURES + identifier));
            case UNKNOWN:      return profileFromBase64(INVALID_BASE64); // This can't be cached because the caller might change it.
            default: throw new AssertionError("Unknown skull value");
        }
        // @formatter:on
    }

    public static ValueType detectSkullValueType(String identifier) {
        try {
            UUID.fromString(identifier);
            return ValueType.UUID;
        } catch (IllegalArgumentException ignored) {}

        if (isUsername(identifier)) return ValueType.NAME;
        if (identifier.contains("textures.minecraft.net")) return ValueType.TEXTURE_URL;
        if (identifier.length() > 100 && isBase64(identifier)) return ValueType.BASE64;

        // We'll just "assume" that it's a textures.minecraft.net hash without the URL part.
        if (MOJANG_SHA256_APPROX.matcher(identifier).matches()) return ValueType.TEXTURE_HASH;

        return ValueType.UNKNOWN;
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
    private static boolean isBase64(@Nonnull String base64) {
        try {
            Base64.getDecoder().decode(base64);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
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
                if (!property.getValue().isEmpty()) return property.getValue();
            }
        }

        return null;
    }

    /**
     * https://help.minecraft.net/hc/en-us/articles/360034636712
     *
     * @param name the username to check.
     *
     * @return true if the string matches the Minecraft username rule, otherwise false.
     */
    private static boolean isUsername(@Nonnull String name) {
        int len = name.length();
        if (len > 16) return false; // Yes, in the old Minecraft 1 letter usernames were a thing.

        // For some reasons Apache's Lists.charactersOf is faster than character indexing for small strings.
        for (char ch : Lists.charactersOf(name)) {
            if (ch != '_' && !(ch >= 'A' && ch <= 'Z') && !(ch >= 'a' && ch <= 'z') && !(ch >= '0' && ch <= '9')) return false;
        }
        return true;
    }

    public enum ValueType {NAME, UUID, BASE64, TEXTURE_URL, TEXTURE_HASH, UNKNOWN}
}
