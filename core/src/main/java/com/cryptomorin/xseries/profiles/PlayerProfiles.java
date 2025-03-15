/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Crypto Morin
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

import com.cryptomorin.xseries.profiles.objects.transformer.ProfileTransformer;
import com.cryptomorin.xseries.reflection.XReflection;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@ApiStatus.Internal
public final class PlayerProfiles {
    /**
     * In v1.20.2 there were some changes to the Mojang API.
     * Before that version, both UUID and name fields couldn't be null, only one of them.
     * It gave the error: {@code Name and ID cannot both be blank}
     * Here, "blank" is null for UUID, and {@code Character.isWhitespace} for the name field.
     */
    public static final String XSERIES_SIG = "XSeries";

    /**
     * The signature value represents the version of XSeries library.
     * It's not needed to change it every time, but it should be changed
     * if the XSeries internals are changed.
     */
    private static final Property XSERIES_GAMEPROFILE_SIGNATURE = new Property(XSERIES_SIG, XReflection.XSERIES_VERSION);
    private static final String TEXTURES_PROPERTY = "textures";

    public static final GameProfile NIL = createGameProfile(PlayerUUIDs.IDENTITY_UUID, XSERIES_SIG);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    /**
     * Some people use this without quotes surrounding the keys, not sure if that'd work.
     */
    public static final String TEXTURES_NBT_PROPERTY_PREFIX = "{\"textures\":{\"SKIN\":{\"url\":\"";

    /**
     * The value after this URL is probably an SHA-252 value that Mojang uses to unique identify player skins.
     * <br>
     * This <a href="https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin/Cape">wiki</a> documents how to
     * get base64 information from player's UUID.
     * <p>
     * Older clients such as v1.8.9 (only tested for v1.8.9) cannot correctly load HTTPS textures.
     * Not sure if plugins like ViaVersion handle this properly.
     * Also, the {@link com.cryptomorin.xseries.profiles.mojang.MojangAPI} UUID_TO_PROFILE
     * returns HTTP for texture URL when the Base64 is decoded, so we can keep it consistent
     * when it's not explicitly defined by the user.
     *
     * @see #getTextureValue(GameProfile)
     */
    public static final String TEXTURES_BASE_URL = "http://textures.minecraft.net/texture/";

    public static Optional<Property> getTextureProperty(GameProfile profile) {
        // This is the property with Base64 encoded value.
        return Optional.ofNullable(Iterables.getFirst(profile.getProperties().get(TEXTURES_PROPERTY), null));
    }

    /**
     * Retrieves the skin value from the given {@link GameProfile}.
     *
     * @param profile The {@link GameProfile} to retrieve the skin value from.
     * @return The skin value as a {@link String}, or {@code null} if not found.
     * @throws NullPointerException if {@code profile} is {@code null}.
     * @see #getTextureProperty(GameProfile)
     */
    @Nullable
    public static String getTextureValue(@NotNull GameProfile profile) {
        Objects.requireNonNull(profile, "Game profile cannot be null");
        return getTextureProperty(profile).map(PlayerProfiles::getPropertyValue).orElse(null);
    }

    @Nullable
    public static String getOriginalValue(@Nullable GameProfile profile) {
        if (profile == null) return null;

        String original = ProfileTransformer.IncludeOriginalValue.getOriginalValue(profile);
        if (original != null) return original;

        return getTextureValue(profile);
    }

    /**
     * Retrieves the value of a {@link Property}, handling differences between versions.
     *
     * @since 4.0.1
     */
    @NotNull
    public static String getPropertyValue(@NotNull Property property) {
        if (ProfilesCore.NULLABILITY_RECORD_UPDATE) return property.value();
        try {
            return (String) ProfilesCore.Property_getValue.invoke(property);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("Unable to get a property value: " + property, throwable);
        }
    }

    /**
     * Checks if the provided {@link GameProfile} has a texture property.
     *
     * @param profile The {@link GameProfile} to check.
     * @return {@code true} if the profile has a texture property, {@code false} otherwise.
     * @see #getTextureProperty(GameProfile)
     */
    public static boolean hasTextures(GameProfile profile) {
        return getTextureProperty(profile).isPresent();
    }

    /**
     * Constructs a {@link GameProfile} using the provided texture hash and base64 string.
     *
     * @param hash   The texture hash used to construct the profile's textures.
     * @param base64 The base64 string representing the profile's textures.
     * @return The constructed {@link GameProfile}.
     * @implNote This method creates a {@link GameProfile} with a UUID derived from the provided hash
     * to ensure consistency after restarts.
     */
    @NotNull
    public static GameProfile profileFromHashAndBase64(String hash, String base64) {
        java.util.UUID uuid = java.util.UUID.nameUUIDFromBytes(hash.getBytes(StandardCharsets.UTF_8));
        GameProfile profile = PlayerProfiles.createNamelessGameProfile(uuid);
        PlayerProfiles.setTexturesProperty(profile, base64);
        return profile;
    }

    @SuppressWarnings("deprecation")
    public static void removeTimestamp(GameProfile profile) {
        JsonObject jsonObject = Optional.ofNullable(getTextureValue(profile)).map(PlayerProfiles::decodeBase64)
                .map((decoded) -> new JsonParser().parse(decoded).getAsJsonObject())
                .orElse(null);

        if (jsonObject == null || !jsonObject.has("timestamp")) return;
        jsonObject.remove("timestamp");

        // Mojang's format is pretty-printed, so let's keep that.
        setTexturesProperty(profile, encodeBase64(GSON.toJson(jsonObject)));
    }

    /**
     * Tries to unwrap a {@link GameProfile} from a {@link net.minecraft.world.item.component.ResolvableProfile}.
     */
    @Nullable
    public static GameProfile unwrapProfile(@Nullable Object profile) throws Throwable {
        if (profile == null) return null;
        if (!(profile instanceof GameProfile) && ProfilesCore.ResolvableProfile_gameProfile != null) {
            // Unwrap from ResolvableProfile
            profile = ProfilesCore.ResolvableProfile_gameProfile.invoke(profile);
        }
        return (GameProfile) profile;
    }

    @Nullable
    public static Object wrapProfile(@Nullable GameProfile profile) throws Throwable {
        if (profile == null) return null;
        if (ProfilesCore.ResolvableProfile$bukkitSupports) {
            return ProfilesCore.ResolvableProfile$constructor.invoke(profile);
        } else {
            return profile;
        }
    }

    /**
     * Uses the online/offline UUID depending on {@link Bukkit#getOnlineMode()}.
     *
     * @param profile must have complete name and UUID
     * @return may return the same or a new profile.
     */
    public static GameProfile sanitizeProfile(GameProfile profile) {
        // We could remove the unnecessary timestamp data, but let's keep it there, the texture is Base64 encoded anyway.
        // It doesn't affect it in terms of performance.
        // The timestamp property is the last time the values have been updated, this
        // is instant in most cases, but are sometimes a few minutes? (or hours?) behind
        // because of Mojang server's cache.

        // The stored cache UUID must be according to online/offline servers.
        if (PlayerUUIDs.isOnlineMode()) return profile;
        UUID offlineId = PlayerUUIDs.getOfflineUUID(profile.getName());
        PlayerUUIDs.ONLINE_TO_OFFLINE.put(profile.getId(), offlineId);

        GameProfile clone = createGameProfile(offlineId, profile.getName());
        clone.getProperties().putAll(profile.getProperties());
        return clone;
    }

    public static GameProfile clone(GameProfile gameProfile) {
        GameProfile clone = new GameProfile(gameProfile.getId(), gameProfile.getName());
        clone.getProperties().putAll(gameProfile.getProperties());
        return clone;
    }

    public static void setTexturesProperty(GameProfile profile, String texture) {
        Property property = new Property(TEXTURES_PROPERTY, texture);
        PropertyMap properties = profile.getProperties();
        properties.asMap().remove(TEXTURES_PROPERTY);
        properties.put(TEXTURES_PROPERTY, property);
    }

    /**
     * Encodes the provided string into Base64 format.
     *
     * @param str The string to encode.
     * @return The Base64 encoded string.
     */
    public static String encodeBase64(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Tries to decode the string as a Base64 value.
     *
     * @param base64 The Base64 string to decode.
     * @return the decoded Base64 string if it is a valid Base64 string, or null if not.
     */
    @Nullable
    public static String decodeBase64(String base64) {
        Objects.requireNonNull(base64, "Cannot decode null string");
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static GameProfile createGameProfile(UUID uuid, String username) {
        return signXSeries(new GameProfile(uuid, username));
    }

    /**
     * All {@link GameProfile} created/modified by this library should have a special signature for debugging
     * purposes, specially since we're directly messing with the server's internal cache
     * it should be there in case something goes wrong.
     */
    public static GameProfile signXSeries(GameProfile profile) {
        // Just as an indicator that this is not a vanilla-created profile.
        PropertyMap properties = profile.getProperties();
        // I don't think a single profile is being signed multiple times.
        // Even if it was, it might be helpful?
        // properties.asMap().remove(DEFAULT_PROFILE_NAME); // Remove previous versions if any.
        properties.put(XSERIES_SIG, XSERIES_GAMEPROFILE_SIGNATURE);
        return profile;
    }

    public static GameProfile createNamelessGameProfile(UUID id) {
        return createGameProfile(id, XSERIES_SIG);
    }
}
