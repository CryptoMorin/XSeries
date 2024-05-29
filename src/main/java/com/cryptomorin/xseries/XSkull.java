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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
import java.util.function.Function;
import java.util.function.Supplier;
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
     * In v1.20.2 there were some changes to the Mojang API.
     * Before that version, both UUID and name fields couldn't be null, only one of them.
     * It gave the error: {@code Name and ID cannot both be blank}
     * Here, "blank" is null for UUID, and {@code Character.isWhitespace} for the name field.
     */
    private static final String DEFAULT_PROFILE_NAME = "XSeries";
    private static final UUID DEFAULT_PROFILE_UUID = new UUID(0, 0);

    /**
     * We'll just return an x shaped hardcoded skull.<br>
     * <a href="https://minecraft-heads.com/custom-heads/miscellaneous/58141-cross">minecraft-heads.com</a>
     */
    private static final GameProfile DEFAULT_PROFILE = SkullInputType.BASE64.getProfile(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5l" +
        "Y3JhZnQubmV0L3RleHR1cmUvYzEwNTkxZTY5MDllNmEyODFiMzcxODM2ZTQ2MmQ2" +
        "N2EyYzc4ZmEwOTUyZTkxMGYzMmI0MWEyNmM0OGMxNzU3YyJ9fX0="
    );

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
            LOGGER.error("Unable to get required fields/methods", throwable);
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
     * Creates a {@link SkullInstruction} for an {@link ItemStack}.
     * This method initializes a new player head.
     *
     * @return A {@link SkullInstruction} that sets the profile for the generated {@link ItemStack}.
     */
    public static SkullInstruction<ItemStack> create() {
        return of(XMaterial.PLAYER_HEAD.parseItem());
    }

    /**
     * Creates a {@link SkullInstruction} for an {@link ItemStack}.
     *
     * @param stack The {@link ItemStack} to set the profile for.
     * @return A {@link SkullInstruction} that sets the profile for the given {@link ItemStack}.
     */
    public static SkullInstruction<ItemStack> of(ItemStack stack) {
        return new SkullInstruction<>((profile) -> setProfile(stack, profile));
    }

    /**
     * Creates a {@link SkullInstruction} for an {@link ItemMeta}.
     *
     * @param meta The {@link ItemMeta} to set the profile for.
     * @return An {@link SkullInstruction} that sets the profile for the given {@link ItemMeta}.
     */
    public static SkullInstruction<ItemMeta> of(ItemMeta meta) {
        return new SkullInstruction<>((profile) -> setProfile(meta, profile));
    }

    /**
     * Creates a {@link SkullInstruction} for a {@link Block}.
     *
     * @param block The {@link Block} to set the profile for.
     * @return An {@link SkullInstruction} that sets the profile for the given {@link Block}.
     */
    public static SkullInstruction<Block> of(Block block) {
        return new SkullInstruction<>((profile -> setProfile(block, profile)));
    }

    /**
     * Creates a {@link SkullInstruction} for a {@link BlockState}.
     *
     * @param state The {@link BlockState} to set the profile for.
     * @return An {@link SkullInstruction} that sets the profile for the given {@link BlockState}.
     */
    public static SkullInstruction<BlockState> of(BlockState state) {
        return new SkullInstruction<>((profile -> setProfile(state, profile)));
    }

    /**
     * Checks if the provided {@link GameProfile} has a texture property.
     *
     * @param profile The {@link GameProfile} to check.
     * @return {@code true} if the profile has a texture property, {@code false} otherwise.
     */
    public static boolean hasTextures(GameProfile profile) {
        return Iterables.getFirst(profile.getProperties().get("textures"), null) != null;
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
     * Retrieves a {@link GameProfile} from the given {@link UUID}.
     *
     * @param uuid The {@link UUID} to retrieve the profile from.
     * @return The {@link GameProfile} corresponding to the input.
     * @throws NullPointerException if {@code uuid} is {@code null}.
     */
    @Nonnull
    public static GameProfile getProfile(@Nonnull UUID uuid) {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        GameProfile profile = getCachedProfileByUUID(uuid);
        if (hasTextures(profile)) return profile;
        return fetchProfile(profile);
    }

    /**
     * Retrieves a {@link GameProfile} based on the provided input string.
     *
     * @param input The input string to retrieve the profile for.
     * @return The {@link GameProfile} corresponding to the input.
     * @throws NullPointerException if {@code input} is {@code null}.
     */
    @Nonnull
    public static GameProfile getProfile(@Nonnull String input) {
        Objects.requireNonNull(input, "Input cannot be null");
        return getProfile(SkullInputType.get(input), input);
    }

    /**
     * Retrieves a {@link GameProfile} based on the provided input type and input string.
     *
     * @param type  The type of the input.
     * @param input The input string to retrieve the profile for.
     * @return The {@link GameProfile} corresponding to the input type and input string.
     * Returns the default profile if the type is {@code null}.
     * @throws NullPointerException if {@code input} is {@code null}.
     * @implNote This method does not validate the input type.
     * If validation is required, consider using {@link XSkull#getProfile(String)}.
     */
    @Nonnull
    public static GameProfile getProfile(@Nullable SkullInputType type, @Nonnull String input) {
        Objects.requireNonNull(input, "Input cannot be null");
        return type == null ? getDefaultProfile() : type.getProfile(input);
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
            LOGGER.error("Unable to get profile from skull meta", throwable);
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
            LOGGER.error("Unable to get profile from block state", throwable);
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
            LOGGER.error("Unable to set profile", throwable);
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
            LOGGER.error("Error while setting skull profile", throwable);
        }
        return state;
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
            return profile == null ? new GameProfile(DEFAULT_PROFILE_UUID, name) : sanitizeProfile((GameProfile) profile);
        } catch (Throwable throwable) {
            LOGGER.error("Unable to get profile by username", throwable);
            return getDefaultProfile();
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
            return profile == null ? new GameProfile(uuid, DEFAULT_PROFILE_NAME) : sanitizeProfile((GameProfile) profile);
        } catch (Throwable throwable) {
            LOGGER.error("Unable to get profile by UUID", throwable);
            return getDefaultProfile();
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
            LOGGER.error("Unable to cache this profile", throwable);
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
                LOGGER.error("Unable to fetch profile properties", throwable);
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
     * and caches the profile.
     *
     * @param profile The {@link GameProfile} to be sanitized.
     * @return The sanitized {@link GameProfile}.
     */
    @SuppressWarnings("deprecation")
    private static GameProfile sanitizeProfile(GameProfile profile) {
        JsonObject jsonObject = Optional.ofNullable(getSkinValue(profile)).map(XSkull::decodeBase64)
                .map((decoded) -> new JsonParser().parse(decoded).getAsJsonObject())
                .orElse(null);
        if (jsonObject == null || !jsonObject.has("timestamp")) return profile;
        JsonObject texture = new JsonObject();
        texture.add("textures", jsonObject.get("textures"));
        GameProfile clone = new GameProfile(profile.getId(), profile.getName());
        Property property = new Property("textures", encodeBase64(texture.toString()));
        clone.getProperties().put("textures", property);
        cacheProfile(clone);
        return clone;
    }

    /**
     * Retrieves the default {@link GameProfile} used by XSkull.
     * This method creates a clone of the default profile to prevent modifications to the original.
     *
     * @return A clone of the default {@link GameProfile}.
     */
    private static GameProfile getDefaultProfile() {
        GameProfile clone = new GameProfile(DEFAULT_PROFILE.getId(), DEFAULT_PROFILE.getName());
        clone.getProperties().putAll(DEFAULT_PROFILE.getProperties());
        return clone;
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
            LOGGER.error("Unable to get a texture value", throwable);
        }
        return null;
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
    private static String decodeBase64(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            LOGGER.error("Not a valid base64 string", exception);
        }
        return null;
    }

    /**
     * The {@code Instruction} class represents an instruction that sets a property of a {@link GameProfile}.
     * It uses a {@link Function} to define how to set the property.
     *
     * @param <T> The type of the result produced by the setter function.
     */
    public static class SkullInstruction<T> {

        /**
         * A function that takes a {@link GameProfile} and produces a result of type {@code T}.
         */
        final protected Function<GameProfile, T> setter;
        protected Supplier<GameProfile> supplier;

        /**
         * Constructs a {@code SkullInstruction} with the specified setter function.
         *
         * @param setter A function that sets a property of a {@link GameProfile} and returns a result of type {@code T}.
         */
        SkullInstruction(Function<GameProfile, T> setter) {
            this.setter = setter;
        }

        /**
         * Provides a callback to retrieve a {@link GameProfile} from the specified input value.
         * The input type is resolved based on the value provided.
         * Returns a new {@link SkullAction} instance.
         *
         * @param input The input value used to retrieve the {@link GameProfile}.
         * @return A new {@link SkullAction} instance configured with this {@code SkullInstruction}.
         */
        public SkullAction<T> profile(String input) {
            this.supplier = () -> getProfile(input);
            return new SkullAction<>(this);
        }

        /**
         * Provides a callback to retrieve a {@link GameProfile} from the specified input type and value,
         * and returns a new {@link SkullAction} instance.
         *
         * @param type  The type of the input value.
         * @param input The input value to generate the {@link GameProfile}.
         * @return A new {@link SkullAction} instance configured with this {@code SkullInstruction}.
         */
        public SkullAction<T> profile(SkullInputType type, String input) {
            this.supplier = () -> getProfile(type, input);
            return new SkullAction<>(this);
        }

        /**
         * Provides a callback to retrieve a {@link GameProfile} from the specified UUID,
         * and returns a new {@link SkullAction} instance.
         *
         * @param uuid The UUID to generate the {@link GameProfile}.
         * @return A new {@link SkullAction} instance configured with this {@code SkullInstruction}.
         */
        public SkullAction<T> profile(UUID uuid) {
            this.supplier = () -> getProfile(uuid);
            return new SkullAction<>(this);
        }

        /**
         * Provides a callback to retrieve a {@link GameProfile} from the specified player,
         * and returns a new {@link SkullAction} instance.
         *
         * @param player The player to generate the {@link GameProfile}.
         * @return A new {@link SkullAction} instance configured with this {@code SkullInstruction}.
         */
        public SkullAction<T> profile(Player player) {
            this.supplier = () -> getProfile(SkullInputType.USERNAME, player.getName());
            return new SkullAction<>(this);
        }

        /**
         * Provides a callback to retrieve a {@link GameProfile} from the specified offline player,
         * and returns a new {@link SkullAction} instance. The profile lookup will depend on whether
         * the server is running in online mode.
         *
         * @param offlinePlayer The offline player to generate the {@link GameProfile}.
         * @return A new {@link SkullAction} instance configured with this {@code SkullInstruction}.
         */
        @SuppressWarnings("DataFlowIssue")
        public SkullAction<T> profile(OfflinePlayer offlinePlayer) {
            this.supplier = () ->
                    Bukkit.getOnlineMode()
                            ? getProfile(offlinePlayer.getUniqueId())
                            : getProfile(SkullInputType.USERNAME, offlinePlayer.getName());
            return new SkullAction<>(this);
        }

        /**
         * Provides a callback to retrieve a {@link GameProfile} from the specified {@link GameProfile}.
         * If the profile already has textures, it will be used directly. Otherwise, a new profile will be fetched
         * based on the UUID or username depending on the server's online mode.
         *
         * @param profile The profile to be used in the profile setting operation.
         * @return A new {@link SkullAction} instance configured with this {@code SkullInstruction}.
         */
        public SkullAction<T> profile(GameProfile profile) {
            if (hasTextures(profile)) {
                this.supplier = () -> profile;
                return new SkullAction<>(this);
            }
            this.supplier = () -> Bukkit.getOnlineMode()
                    ? getProfile(profile.getId())
                    : getProfile(SkullInputType.USERNAME, profile.getName());
            return new SkullAction<>(this);

        }
    }

    /**
     * Represents an action that handles both asynchronous and synchronous workflows
     * based on a given {@link SkullInstruction}.
     *
     * @param <T> The type of the result produced by the action.
     */
    public static class SkullAction<T> {
        /**
         * An executor service with a fixed thread pool of size 2, used for asynchronous operations.
         */
        private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2, new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger();

            @Override
            public Thread newThread(final @Nonnull Runnable run) {
                final Thread thread = new Thread(run);
                thread.setName("Profile Lookup Executor #" + this.count.getAndIncrement());
                thread.setUncaughtExceptionHandler((t, throwable) ->
                        LOGGER.error("Uncaught exception in thread {}", t.getName(), throwable));
                return thread;
            }
        });

        /**
         * The instruction that defines how the action is applied.
         */
        private final SkullInstruction<T> instruction;

        /**
         * Constructs a {@code SkullAction} with the specified instruction.
         *
         * @param instruction The instruction that defines how the action is applied.
         */
        protected SkullAction(SkullInstruction<T> instruction) {
            this.instruction = instruction;
        }

        /**
         * Sets the profile generated by the instruction to the result type.
         *
         * @return The result after setting the generated profile.
         */
        public T apply() {
            GameProfile profile = instruction.supplier.get();
            return instruction.setter.apply(profile);
        }

        /**
         * Asynchronously applies the instruction to generate a {@link GameProfile} and returns a {@link CompletableFuture}.
         * This method is designed for non-blocking execution, allowing tasks to be performed
         * in the background without blocking the server's main thread.
         *
         * <p>Usage example:</p>
         * <pre>{@code
         *   XSkull.create().profile(offlinePlayer).applyAsync()
         *      .thenAcceptAsync(result -> {
         *          // Additional processing...
         *      }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
         * }</pre>
         *
         * @return A {@link CompletableFuture} that will complete asynchronously.
         */
        public CompletableFuture<T> applyAsync() {
            return CompletableFuture.supplyAsync(this::apply, EXECUTOR);
        }
    }

    /**
     * The {@code SkullInputType} enum represents different types of input patterns that can be used for identifying
     * and validating various formats such as texture hashes, URLs, Base64 encoded strings, UUIDs, and usernames.
     */
    public enum SkullInputType {

        /**
         * Represents a texture hash pattern.
         * Mojang hashes length are inconsistent and doesn't seem to use uppercase characters
         */
        TEXTURE_HASH(Pattern.compile("[0-9a-z]{55,70}")) {
            @Override
            GameProfile getProfile(String hash) {
                String base64 = encodeBase64(VALUE_PROPERTY + TEXTURES + hash + "\"}}}");
                return profileFromHashAndBase64(hash, base64);
            }
        },

        /**
         * Represents a texture URL pattern that includes the base URL followed by the texture hash pattern.
         */
        TEXTURE_URL(Pattern.compile(Pattern.quote(TEXTURES) + "(?<hash>" + TEXTURE_HASH.pattern + ")")) {
            @Override
            GameProfile getProfile(String url) {
                String hash = extractTextureHash(url);
                return TEXTURE_HASH.getProfile(hash);
            }
        },

        /**
         * Represents a Base64 encoded string pattern.
         */
        BASE64(Pattern.compile("[-A-Za-z0-9+/]{100,}={0,3}")) {
            @Override
            GameProfile getProfile(String base64) {
                return Optional.ofNullable(decodeBase64(base64))
                        .map(SkullInputType::extractTextureHash)
                        .map((hash) -> profileFromHashAndBase64(hash, base64))
                        .orElseGet(XSkull::getDefaultProfile);
            }
        },

        /**
         * Represents a UUID pattern, following the standard UUID format.
         */
        UUID(Pattern.compile("[A-F\\d]{8}-[A-F\\d]{4}-4[A-F\\d]{3}-([89AB])[A-F\\d]{3}-[A-F\\d]{12}", Pattern.CASE_INSENSITIVE)) {
            @Override
            GameProfile getProfile(String input) {
                return XSkull.getProfile(java.util.UUID.fromString(input));
            }
        },

        /**
         * Represents a username pattern, allowing alphanumeric characters and underscores, with a length of 1 to 16 characters.
         */
        USERNAME(Pattern.compile("[A-Za-z0-9_]{1,16}")) {
            @Override
            GameProfile getProfile(String name) {
                GameProfile profile = getCachedProfileByUsername(name);
                if (hasTextures(profile)) return profile;
                return fetchProfile(profile);
            }
        };

        /**
         * The regex pattern associated with the input type.
         */
        public final Pattern pattern;
        private static final SkullInputType[] VALUES = values();

        /**
         * Constructs a {@code SkullInputType} with the specified regex pattern.
         *
         * @param pattern The regex pattern associated with the input type.
         */
        SkullInputType(Pattern pattern) {
            this.pattern = pattern;
        }

        /**
         * Retrieves a {@link GameProfile} based on the provided input string.
         *
         * @param input The input string to retrieve the profile for.
         * @return The {@link GameProfile} corresponding to the input string.
         */
        abstract GameProfile getProfile(String input);

        /**
         * Returns the corresponding {@code SkullInputType} for the given identifier, if it matches any pattern.
         *
         * @param identifier The string to be checked against the patterns.
         * @return The matching {@code InputType}, or {@code null} if no match is found.
         */
        @Nullable
        public static SkullInputType get(@Nonnull String identifier) {
            Objects.requireNonNull(identifier, "Identifier cannot be null");
            return Arrays.stream(VALUES)
                    .filter(value -> value.pattern.matcher(identifier).matches())
                    .findFirst().orElse(null);
        }

        /**
         * Constructs a {@link GameProfile} using the provided texture hash and base64 string.
         *
         * @param hash   The texture hash used to construct the profile's textures.
         * @param base64 The base64 string representing the profile's textures.
         * @return The constructed {@link GameProfile}.
         * @implNote This method creates a {@link GameProfile} with a UUID derived from the provided hash
         *           to ensure consistency after restarts.
         */
        private static GameProfile profileFromHashAndBase64(String hash, String base64) {
            UUID uuid = java.util.UUID.nameUUIDFromBytes(hash.getBytes(StandardCharsets.UTF_8));
            GameProfile profile = new GameProfile(uuid, DEFAULT_PROFILE_NAME);
            profile.getProperties().put("textures", new Property("textures", base64));
            return profile;
        }

        /**
         * Extracts the texture hash from the provided input string.
         *
         * @param input The input string containing the texture hash.
         * @return The extracted texture hash.
         */
        private static String extractTextureHash(String input) {
            // Example: http://textures.minecraft.net/texture/e5461a215b325fbdf892db67b7bfb60ad2bf1580dc968a15dfb304ccd5e74db
            // Will not work reliably if NBT is passed: {"textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/74133f6ac3be2e2499a784efadcfffeb9ace025c3646ada67f3414e5ef3394"}}}
            Matcher matcher = SkullInputType.TEXTURE_HASH.pattern.matcher(input);
            return matcher.find() ? matcher.group() : null;
        }
    }
}
