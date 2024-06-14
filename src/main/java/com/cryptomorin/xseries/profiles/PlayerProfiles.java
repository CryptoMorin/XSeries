package com.cryptomorin.xseries.profiles;

import com.cryptomorin.xseries.reflection.XReflection;
import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    public static final String DEFAULT_PROFILE_NAME = "XSeries";
    private static final Property XSERIES_GAMEPROFILE_SIGNATURE = new Property(DEFAULT_PROFILE_NAME, "true");
    public static final String TEXTURES_PROPERTY = "textures";


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
     * Older clients such as v1.8.9 cannot correctly load HTTPS textures.
     * Not sure if plugins like ViaVersion handle this properly.
     */
    public static final String TEXTURES_BASE_URL = XReflection.v(9, "https").orElse("http") +
            "://textures.minecraft.net/texture/";

    public static Optional<Property> getTextureProperty(GameProfile profile) {
        return Optional.ofNullable(Iterables.getFirst(profile.getProperties().get(TEXTURES_PROPERTY), null));
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
        return getTextureProperty(profile).map(PlayerProfiles::getPropertyValue).orElse(null);
    }

    /**
     * Retrieves the value of a {@link Property}, handling differences between versions.
     *
     * @param property The {@link Property} from which to retrieve the value.
     * @return The value of the {@link Property}.
     * @since 4.0.1
     */
    public static String getPropertyValue(Property property) {
        if (ProfilesCore.NULLABILITY_RECORD_UPDATE) return property.value();
        try {
            return (String) ProfilesCore.PROPERTY_GET_VALUE.invoke(property);
        } catch (Throwable throwable) {
            throw new RuntimeException("Unable to get a texture value: " + property, throwable);
        }
    }

    /**
     * Checks if the provided {@link GameProfile} has a texture property.
     *
     * @param profile The {@link GameProfile} to check.
     * @return {@code true} if the profile has a texture property, {@code false} otherwise.
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
     *           to ensure consistency after restarts.
     */
    @Nonnull
    public static GameProfile profileFromHashAndBase64(String hash, String base64) {
        java.util.UUID uuid = java.util.UUID.nameUUIDFromBytes(hash.getBytes(StandardCharsets.UTF_8));
        GameProfile profile = PlayerProfiles.createNamelessGameProfile(uuid);
        PlayerProfiles.addTexturesProperty(profile, base64);
        return profile;
    }

    /**
     * Sanitizes the provided {@link GameProfile} by removing unnecessary timestamp data
     * and caches the profile.
     *
     * @param profile The {@link GameProfile} to be sanitized.
     * @return The sanitized {@link GameProfile}.
     */
    @SuppressWarnings("deprecation")
    public static GameProfile sanitizeProfile(GameProfile profile) {
        JsonObject jsonObject = Optional.ofNullable(getSkinValue(profile)).map(PlayerProfiles::decodeBase64)
                .map((decoded) -> new JsonParser().parse(decoded).getAsJsonObject())
                .orElse(null);

        if (jsonObject == null || !jsonObject.has("timestamp")) return profile;
        JsonObject texture = new JsonObject();
        texture.add(TEXTURES_PROPERTY, jsonObject.get(TEXTURES_PROPERTY));

        // The stored cache UUID must be according to online/offline servers.
        UUID id;
        if (PlayerUUIDs.isOnlineMode()) {
            id = profile.getId();
        } else {
            id = PlayerUUIDs.getOfflineUUID(profile.getName());
            PlayerUUIDs.ONLINE_TO_OFFLINE.put(profile.getId(), id);
        }

        GameProfile clone = new GameProfile(id, profile.getName());
        addTexturesProperty(clone, encodeBase64(texture.toString()));
        signXSeries(clone);
        return clone;
    }

    public static void addTexturesProperty(GameProfile profile, String texture) {
        Property property = new Property(TEXTURES_PROPERTY, texture);
        profile.getProperties().put(TEXTURES_PROPERTY, property);
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

    public static GameProfile signXSeries(GameProfile profile) {
        // Just as an indicator that this is not a vanilla-created profile.
        PropertyMap properties = profile.getProperties();
        properties.put(DEFAULT_PROFILE_NAME, XSERIES_GAMEPROFILE_SIGNATURE);
        return profile;
    }

    public static GameProfile createNamelessGameProfile(UUID id) {
        return signXSeries(new GameProfile(id, DEFAULT_PROFILE_NAME));
    }
}
