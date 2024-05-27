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
package com.cryptomorin.xseries.skull;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.FieldMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.MethodMemberHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftClassHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
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

    static final Logger LOGGER = LogManager.getLogger("XSkull");
    private static final Object USER_CACHE, MINECRAFT_SESSION_SERVICE;

    private static final MethodHandle
        FILL_PROFILE_PROPERTIES, GET_PROFILE_BY_NAME, GET_PROFILE_BY_UUID, CACHE_PROFILE,
        CRAFT_META_SKULL_PROFILE_GETTER, CRAFT_META_SKULL_PROFILE_SETTER,
        CRAFT_SKULL_PROFILE_SETTER, CRAFT_SKULL_PROFILE_GETTER,
        PROPERTY_GET_VALUE;

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
     * In v1.20.2 there were some changes to the Mojang API.
     * Before that version, both UUID and name fields couldn't be null, only one of them.
     * It gave the error: {@code Name and ID cannot both be blank}
     * Here, "blank" is null for UUID, and {@code Character.isWhitespace} for the name field.
     */
    private static final String PROFILE_DEFAULT_NAME = "XSeries";
    private static final UUID PROFILE_DEFAULT_UUID = new UUID(0, 0);
    private static final boolean NULLABILITY_RECORD_UPDATE = XReflection.supports(20, 2);

    /**
     * The value after this URL is probably an SHA-252 value that Mojang uses to unique identify player skins.
     * <br>
     * This <a href="https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin/Cape">wiki</a> documents how to
     * get base64 information from player's UUID.
     */
    static final String TEXTURES = "http://textures.minecraft.net/texture/";

    static {
        Object userCache = null, minecraftSessionService = null;
        MethodHandle fillProfileProperties = null, getProfileByName = null, getProfileByUUID = null, cacheProfile = null;
        MethodHandle profileSetterMeta = null, profileGetterMeta = null, getPropertyValue = null;

        try {
            MinecraftClassHandle CraftMetaSkull = XReflection.ofMinecraft()
                    .inPackage(MinecraftPackage.CB, "inventory")
                    .named("CraftMetaSkull");
            profileGetterMeta = CraftMetaSkull.getterField().named("profile").returns(GameProfile.class).makeAccessible().reflect();

            try {
                // https://github.com/CryptoMorin/XSeries/issues/169
                profileSetterMeta = CraftMetaSkull.method().named("setProfile")
                        .parameters(GameProfile.class)
                        .returns(void.class).makeAccessible().reflect();
            } catch (NoSuchMethodException e) {
                profileSetterMeta = CraftMetaSkull.setterField().named("profile").returns(GameProfile.class).makeAccessible().reflect();
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
                    .named("az", "ao", "am", "aD")
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
            LOGGER.debug("Unable to get required fields/methods", throwable);
        }

        MinecraftClassHandle CraftSkull = XReflection.ofMinecraft()
                .inPackage(MinecraftPackage.CB, "block")
                .named("CraftSkull");

        FieldMemberHandle craftProfile = CraftSkull.field().named("profile").returns(GameProfile.class);

        if (!NULLABILITY_RECORD_UPDATE) {
            getPropertyValue = XReflection.of(Property.class).method().named("getValue").returns(String.class).unreflect();
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
        CRAFT_SKULL_PROFILE_SETTER = craftProfile.setter().makeAccessible().unreflect();
        CRAFT_SKULL_PROFILE_GETTER = craftProfile.getter().makeAccessible().unreflect();
    }

    /**
     * Creates an {@link Instruction} for an {@link ItemStack}.
     *
     * @param stack The {@link ItemStack} to set the profile for.
     * @return An {@link Instruction} that sets the profile for the given {@link ItemStack}.
     */
    public static Instruction<ItemStack> of(ItemStack stack) {
        return new Instruction<>((profile) -> XSkull.setProfile(stack, profile));
    }

    /**
     * Creates an {@link Instruction} for an {@link ItemMeta}.
     *
     * @param meta The {@link ItemMeta} to set the profile for.
     * @return An {@link Instruction} that sets the profile for the given {@link ItemMeta}.
     */
    public static Instruction<ItemMeta> of(ItemMeta meta) {
        return new Instruction<>((profile) -> XSkull.setProfile(meta, profile));
    }

    /**
     * Creates an {@link Instruction} for a {@link Block}.
     *
     * @param block The {@link Block} to set the profile for.
     * @return An {@link Instruction} that sets the profile for the given {@link Block}.
     */
    public static Instruction<Block> of(Block block) {
        return new Instruction<>((profile -> XSkull.setProfile(block, profile)));
    }

    /**
     * Creates an {@link Instruction} for a {@link BlockState}.
     *
     * @param state The {@link BlockState} to set the profile for.
     * @return An {@link Instruction} that sets the profile for the given {@link BlockState}.
     */
    public static Instruction<BlockState> of(BlockState state) {
        return new Instruction<>((profile -> XSkull.setProfile(state, profile)));
    }


    /**
     * Retrieves the skin value from the given {@link ItemMeta}.
     *
     * @param meta The {@link ItemMeta} to retrieve the skin value from.
     * @return The skin value as a {@link String}, or {@code null} if not found.
     * @throws NullPointerException if {@code meta} is {@code null}.
     */
    @Nullable
    public static String getSkinValue(@Nonnull ItemMeta meta) {
        Objects.requireNonNull(meta, "Skull meta cannot be null");
        GameProfile profile = getProfile(meta);
        return profile == null ? null : getSkinValue(profile);
    }

    /**
     * Retrieves the skin value from the given {@link BlockState}.
     *
     * @param state The {@link BlockState} to retrieve the skin value from.
     * @return The skin value as a {@link String}, or {@code null} if not found.
     * @throws NullPointerException if {@code state} is {@code null}.
     */
    @Nullable
    public static String getSkinValue(@Nonnull BlockState state) {
        Objects.requireNonNull(state, "Block state cannot be null");
        GameProfile profile = getProfile(state);
        return profile == null ? null : getSkinValue(profile);
    }

    /**
     * Retrieves the skin value from the given {@link GameProfile}.
     *
     * @param profile The {@link GameProfile} to retrieve the skin value from.
     * @return The skin value as a {@link String}, or {@code null} if not found.
     * @throws NullPointerException if {@code profile} is {@code null}.
     */
    @Nullable
    public static String getSkinValue(@Nonnull GameProfile profile) {
        Objects.requireNonNull(profile, "Game profile cannot be null");
        return Optional.ofNullable(Iterables.getFirst(profile.getProperties().get("textures"), null))
                .map(XSkull::getPropertyValue).orElse(null);
    }

    /**
     * Retrieves a {@link GameProfile} based on the provided input string.
     *
     * @param input The input string to retrieve the profile for.
     * @return The {@link GameProfile} corresponding to the input.
     * @throws NullPointerException if {@code input} is {@code null}.
     */
    @Nonnull
    public GameProfile getProfile(@Nonnull String input) {
        Objects.requireNonNull(input, "Input cannot be null");
        return getProfile(InputType.get(input), input);
    }

    /**
     * Retrieves a {@link GameProfile} based on the provided input type and input string.
     *
     * @param type The type of the input.
     * @param input The input string to retrieve the profile for.
     * @return The {@link GameProfile} corresponding to the input type and input string.
     *         Returns a profile with an invalid skull value if the type is {@code null}.
     * @throws NullPointerException if {@code input} is {@code null}.
     * @throws AssertionError if the input type is unknown.
     */
    @Nonnull
    public static GameProfile getProfile(@Nullable InputType type, @Nonnull String input) {
        Objects.requireNonNull(input, "Input cannot be null");
        if (type == null) return profileFromBase64(INVALID_SKULL_VALUE);
        switch (type) {
            case UUID:         return profileFromUUID(UUID.fromString(input));
            case USERNAME:     return profileFromUsername(input);
            case BASE64:       return profileFromBase64(input);
            case TEXTURE_URL:  return profileFromURL(input);
            case TEXTURE_HASH: return profileFromHash(input);
            default: throw new AssertionError("Unknown skull value");
        }
    }

    /**
     * Retrieves a {@link GameProfile} from the given {@link ItemStack}.
     *
     * @param stack The {@link ItemStack} to retrieve the profile from.
     * @return The {@link GameProfile} of the item, or {@code null} if not found.
     * @throws NullPointerException if {@code stack} is {@code null}.
     */
    @Nullable
    @SuppressWarnings("DataFlowIssue")
    public static GameProfile getProfile(@Nonnull ItemStack stack) {
        Objects.requireNonNull(stack, "Item stack cannot be null");
        ItemMeta meta = stack.getItemMeta();
        return getProfile(meta);
    }

    /**
     * Retrieves a {@link GameProfile} from the given {@link ItemMeta}.
     *
     * @param meta The {@link ItemMeta} to retrieve the profile from.
     * @return The {@link GameProfile} of the item meta, or {@code null} if not found.
     * @throws NullPointerException if {@code meta} is {@code null}.
     */
    @Nullable
    public static GameProfile getProfile(@Nonnull ItemMeta meta) {
        Objects.requireNonNull(meta, "Item meta cannot be null");
        try {
            return (GameProfile) CRAFT_META_SKULL_PROFILE_GETTER.invoke((SkullMeta) meta);
        } catch (Throwable throwable) {
            LOGGER.debug("Unable to get profile from skull meta", throwable);
        }
        return null;
    }

    /**
     * Retrieves a {@link GameProfile} from the given {@link Block}.
     *
     * @param block The {@link Block} to retrieve the profile from.
     * @return The {@link GameProfile} of the block, or {@code null} if not found.
     * @throws NullPointerException if {@code block} is {@code null}.
     */
    @Nullable
    public static GameProfile getProfile(@Nonnull Block block) {
        Objects.requireNonNull(block, "Block cannot be null");
        BlockState state = block.getState();
        return getProfile(state);
    }

    /**
     * Retrieves a {@link GameProfile} from the given {@link BlockState}.
     *
     * @param state The {@link BlockState} to retrieve the profile from.
     * @return The {@link GameProfile} of the block state, or {@code null} if not found.
     * @throws NullPointerException if {@code state} is {@code null}.
     */
    @Nullable
    public static GameProfile getProfile(@Nonnull BlockState state) {
        Objects.requireNonNull(state, "Block state cannot be null");
        try {
            return (GameProfile) CRAFT_SKULL_PROFILE_GETTER.invoke(state);
        } catch (Throwable throwable) {
            LOGGER.debug("Unable to get profile from block state", throwable);
        }
        return null;
    }

    /**
     * Sets the {@link GameProfile} on the given {@link ItemStack}.
     *
     * @param stack The {@link ItemStack} to set the profile on.
     * @param profile The {@link GameProfile} to set.
     * @return The {@link ItemStack} with the profile set.
     * @throws NullPointerException if {@code stack} or {@code profile} is {@code null}.
     */
    @Nonnull
    public static ItemStack setProfile(@Nonnull ItemStack stack, @Nullable GameProfile profile) {
        Objects.requireNonNull(stack, "Item stack cannot be null");
        ItemMeta meta = stack.getItemMeta();
        setProfile(Objects.requireNonNull(meta), profile);
        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * Directly sets the {@link GameProfile} on the given {@link ItemMeta}.
     * <p>
     * Note: Directly setting the profile is not compatible with {@link SkullMeta#setOwningPlayer(OfflinePlayer)},
     * and should be reset by calling {@code setProfile(meta, null)}.
     * <br><br>
     * Newer client versions give profiles a higher priority over UUID.
     * </p>
     *
     * @param meta The {@link ItemMeta} to set the profile on.
     * @param profile The {@link GameProfile} to set.
     * @return The {@link ItemMeta} with the profile set.
     * @throws NullPointerException if {@code meta} is {@code null}.
     */
    public static ItemMeta setProfile(@Nonnull ItemMeta meta, @Nullable GameProfile profile) {
        Objects.requireNonNull(meta, "Item meta cannot be null");
        try {
            CRAFT_META_SKULL_PROFILE_SETTER.invoke(meta, profile);
        } catch (Throwable throwable) {
            LOGGER.debug("Unable to set profile", throwable);
        }
        return meta;
    }

    /**
     * Sets the {@link GameProfile} on the given {@link Block}.
     *
     * @param block The {@link Block} to set the profile on.
     * @param profile The {@link GameProfile} to set.
     * @return The {@link Block} with the profile set.
     * @throws NullPointerException if {@code block} is {@code null}.
     */
    public static Block setProfile(@Nonnull Block block, @Nullable GameProfile profile) {
        Objects.requireNonNull(block, "Block cannot be null");
        BlockState state = block.getState();
        setProfile(state, profile);
        state.update(true);
        return block;
    }

    /**
     * Sets the {@link GameProfile} on the given {@link BlockState}.
     *
     * @param state The {@link BlockState} to set the profile on.
     * @param profile The {@link GameProfile} to set.
     * @return The {@link BlockState} with the profile set.
     * @throws NullPointerException if {@code state} is {@code null}.
     */
    public static BlockState setProfile(@Nonnull BlockState state, @Nullable GameProfile profile) {
        Objects.requireNonNull(state, "Block state cannot be null");
        try {
            CRAFT_SKULL_PROFILE_SETTER.invoke((Skull) state, profile);
        } catch (Throwable throwable) {
            LOGGER.debug("Error while setting skull profile", throwable);
        }
        return state;
    }


    /**
     * Retrieves or fetches a {@link GameProfile} based on the provided UUID.
     * If the profile has a texture, returns the cached profile; otherwise, fetches the profile.
     *
     * @param uuid The UUID of the profile to retrieve or fetch.
     * @return The {@link GameProfile} corresponding to the UUID.
     */
    private static GameProfile profileFromUUID(UUID uuid) {
        GameProfile profile = getCachedProfileByUUID(uuid);
        if (hasTextures(profile)) return profile;
        profile = fetchProfile(profile);
        return profile;
    }

    /**
     * Retrieves or fetches a {@link GameProfile} based on the provided username.
     * If the profile has a texture, returns the cached profile; otherwise, fetches the profile.
     *
     * @param name The username of the profile to retrieve or fetch.
     * @return The {@link GameProfile} corresponding to the username.
     */
    private static GameProfile profileFromUsername(String name) {
        GameProfile profile = getCachedProfileByUsername(name);
        if (hasTextures(profile)) return profile;
        profile = fetchProfile(profile);
        return profile;
    }

    /**
     * Retrieves a {@link GameProfile} based on the provided base64 string.
     *
     * @param base64 The base64 string representing the profile's textures.
     * @return The {@link GameProfile} corresponding to the base64 string.
     * @throws NullPointerException if the base64 string is not valid.
     */
    private static GameProfile profileFromBase64(String base64) {
        String hash = decodeBase64(base64).map(XSkull::extractTextureHash).orElse(null);
        Objects.requireNonNull(hash, "Not a valid base64 string");
        return profileFromHashAndBase64(hash, base64);
    }

    /**
     * Retrieves a {@link GameProfile} based on the provided URL.
     *
     * @param url The URL representing the profile's textures.
     * @return The {@link GameProfile} corresponding to the URL.
     */
    private static GameProfile profileFromURL(String url) {
        String hash = extractTextureHash(url);
        return profileFromHash(hash);
    }

    /**
     * Retrieves a {@link GameProfile} based on the provided texture hash.
     *
     * @param hash The texture hash used to construct the profile's textures.
     * @return The {@link GameProfile} corresponding to the texture hash.
     */
    private static GameProfile profileFromHash(String hash) {
        String base64 = encodeBase64(VALUE_PROPERTY + TEXTURES + hash + "\"}}}");
        return profileFromHashAndBase64(hash, base64);
    }

    /**
     * Constructs a {@link GameProfile} using the provided texture hash and base64 string.
     *
     * @param hash The texture hash used to construct the profile's textures.
     * @param base64 The base64 string representing the profile's textures.
     * @return The constructed {@link GameProfile}.
     */
    private static GameProfile profileFromHashAndBase64(String hash, String base64) {
        // Creates an id from its hash for consistency after restarts
        UUID uuid = UUID.nameUUIDFromBytes(hash.getBytes(StandardCharsets.UTF_8));
        GameProfile profile = new GameProfile(uuid, PROFILE_DEFAULT_NAME);
        profile.getProperties().put("textures", new Property("textures", base64));
        return profile;
    }

    /**
     * Retrieves a cached {@link GameProfile} by username from the user cache.
     * If the profile is not found in the cache, creates a new profile with the provided name.
     *
     * @param name The username of the profile to retrieve from the cache.
     * @return The cached {@link GameProfile} corresponding to the username, or a new profile if not found.
     */
    private static GameProfile getCachedProfileByUsername(String name) {
        try {
            @Nullable Object profile = GET_PROFILE_BY_NAME.invoke(USER_CACHE, name);
            if (profile instanceof Optional) profile = ((Optional<?>) profile).orElse(null);
            return profile == null ? new GameProfile(PROFILE_DEFAULT_UUID, name) : sanitizeProfile((GameProfile) profile);
        } catch (Throwable throwable) {
            LOGGER.debug("Unable to get profile by username", throwable);
            return profileFromBase64(INVALID_SKULL_VALUE);
        }
    }

    /**
     * Retrieves a cached {@link GameProfile} by UUID from the user cache.
     * If the profile is not found in the cache, creates a new profile with the provided UUID.
     *
     * @param uuid The UUID of the profile to retrieve from the cache.
     * @return The cached {@link GameProfile} corresponding to the UUID, or a new profile if not found.
     */
    private static GameProfile getCachedProfileByUUID(UUID uuid) {
        try {
            @Nullable Object profile = GET_PROFILE_BY_UUID.invoke(USER_CACHE, uuid);
            if (profile instanceof Optional) profile = ((Optional<?>) profile).orElse(null);
            return profile == null ? new GameProfile(uuid, PROFILE_DEFAULT_NAME) : sanitizeProfile((GameProfile) profile);
        } catch (Throwable throwable) {
            LOGGER.debug("Unable to get profile by UUID", throwable);
            return profileFromBase64(INVALID_SKULL_VALUE);
        }
    }

    /**
     * Caches the provided {@link GameProfile} in the user cache.
     *
     * @param profile The {@link GameProfile} to cache.
     */
    private static void cacheProfile(GameProfile profile) {
        try {
            CACHE_PROFILE.invoke(USER_CACHE, profile);
        } catch (Throwable throwable) {
            LOGGER.debug("Unable to cache this profile", throwable);
        }
    }

    /**
     * Fetches additional properties for the given {@link GameProfile} if possible.
     *
     * @param profile The {@link GameProfile} for which properties are to be fetched.
     * @return The updated {@link GameProfile} with fetched properties, sanitized for consistency.
     */
    private static GameProfile fetchProfile(GameProfile profile) {
        if (!NULLABILITY_RECORD_UPDATE) {
            try {
                profile = (GameProfile) FILL_PROFILE_PROPERTIES.invoke(MINECRAFT_SESSION_SERVICE, profile, false);
            } catch (Throwable throwable) {
                LOGGER.debug("Unable to fetch profile properties", throwable);
            }
        } else {
            com.mojang.authlib.yggdrasil.ProfileResult result = ((MinecraftSessionService) MINECRAFT_SESSION_SERVICE)
                    .fetchProfile(profile.getId(), false);
            if (result != null) profile = result.profile();
        }
        return sanitizeProfile(profile);
    }

    /**
     * Sanitizes the provided {@link GameProfile} by removing unnecessary timestamp data
     * and caches the sanitized profile.
     *
     * @param profile The {@link GameProfile} to be sanitized.
     * @return The sanitized {@link GameProfile}.
     */
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

    /**
     * Retrieves the value of a {@link Property}, handling differences between versions.
     *
     * @param property The {@link Property} from which to retrieve the value.
     * @return The value of the {@link Property}.
     * @since 4.0.1
     */
    private static String getPropertyValue(Property property) {
        if (NULLABILITY_RECORD_UPDATE) return property.value();
        try {
            return (String) PROPERTY_GET_VALUE.invoke(property);
        } catch (Throwable throwable) {
            LOGGER.debug("Unable to get a texture value", throwable);
        }
        return null;
    }

    /**
     * Extracts the texture hash from the provided input string.
     *
     * @param input The input string containing the texture hash.
     * @return The extracted texture hash.
     * @throws IllegalArgumentException if the input string does not contain a valid texture hash.
     */
    private static String extractTextureHash(String input) {
        // Example: http://textures.minecraft.net/texture/e5461a215b325fbdf892db67b7bfb60ad2bf1580dc968a15dfb304ccd5e74db
        // Will not work reliably if NBT is passed: {"textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/74133f6ac3be2e2499a784efadcfffeb9ace025c3646ada67f3414e5ef3394"}}}
        Matcher matcher = MOJANG_SHA256_APPROX.matcher(input);
        if (matcher.find()) return matcher.group();
        else throw new IllegalArgumentException("Invalid input: " + input);
    }

    /**
     * Checks if the provided {@link GameProfile} has a texture property.
     *
     * @param profile The {@link GameProfile} to check.
     * @return {@code true} if the profile has a texture property, {@code false} otherwise.
     */
    private static boolean hasTextures(GameProfile profile) {
        return Iterables.getFirst(profile.getProperties().get("textures"), null) != null;
    }

    /**
     * Encodes the provided string into Base64 format.
     *
     * @param str The string to encode.
     * @return The Base64 encoded string.
     */
    private static String encodeBase64(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Tries to decode the string as a Base64 value.
     *
     * @param base64 The Base64 string to decode.
     * @return An {@link Optional} containing the decoded Base64 string if it is a valid Base64 string, or empty if not.
     */
    private static Optional<String> decodeBase64(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return Optional.of(new String(bytes, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
