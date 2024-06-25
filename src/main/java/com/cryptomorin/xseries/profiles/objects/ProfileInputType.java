package com.cryptomorin.xseries.profiles.objects;

import com.cryptomorin.xseries.profiles.PlayerProfiles;
import com.cryptomorin.xseries.profiles.exceptions.InvalidProfileException;
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Different types of input patterns that can be used for identifying
 * constructing, and validating various formats that can represent a {@link Profileable}.
 */
@SuppressWarnings("JavadocLinkAsPlainText")
public enum ProfileInputType {
    /**
     * Represents a texture hash pattern.
     * Mojang hashes length are inconsistent, and they don't seem to use uppercase characters.
     * <p>
     * Currently, the shortest observed hash value is (lenght: 57): 0a4050e7aacc4539202658fdc339dd182d7e322f9fbcc4d5f99b5718a
     * <p>
     * Example: e5461a215b325fbdf892db67b7bfb60ad2bf1580dc968a15dfb304ccd5e74db
     */
    TEXTURE_HASH(Pattern.compile("[0-9a-z]{55,70}")) {
        @Override
        public GameProfile getProfile(String textureHash) {
            String base64 = PlayerProfiles.encodeBase64(PlayerProfiles.TEXTURES_NBT_PROPERTY_PREFIX + PlayerProfiles.TEXTURES_BASE_URL + textureHash + "\"}}}");
            return PlayerProfiles.profileFromHashAndBase64(textureHash, base64);
        }
    },

    /**
     * Represents a texture URL pattern that includes the base URL followed by the texture hash pattern.
     * <p>
     * Example: http://textures.minecraft.net/texture/e5461a215b325fbdf892db67b7bfb60ad2bf1580dc968a15dfb304ccd5e74db
     */
    TEXTURE_URL(Pattern.compile("(?:https?://)?(?:textures\\.)?minecraft\\.net/texture/(?<hash>" + TEXTURE_HASH.pattern + ')', Pattern.CASE_INSENSITIVE)) {
        @Override
        public GameProfile getProfile(String textureUrl) {
            String hash = extractTextureHash(textureUrl);
            return TEXTURE_HASH.getProfile(hash);
        }
    },

    /**
     * Represents a Base64 encoded string pattern.
     * The base64 pattern that's checked is not a general base64 pattern, but a pattern that
     * closely represents the base64 genereated by the NBT data.
     */
    BASE64(Pattern.compile("[-A-Za-z0-9+/]{100,}={0,3}")) {
        @Override
        public GameProfile getProfile(String base64) {
            // The base64 string represents the textures.
            // There are 3 types of textures: SKIN - CAPE - ELYTRA (not present in v1.8)
            // Each can have a URL and an additional set of metadata (like model of skin, steve or alex, classic or slim)
            // (from authlib's MinecraftProfileTexture which exists in all versions 1.8-1.21)
            String decodedBase64 = PlayerProfiles.decodeBase64(base64);
            if (decodedBase64 == null)
                throw new InvalidProfileException("Not a base64 string: " + base64);

            String textureHash = extractTextureHash(decodedBase64);
            if (textureHash == null)
                throw new InvalidProfileException("Can't extract texture hash from base64: " + decodedBase64);

            return PlayerProfiles.profileFromHashAndBase64(textureHash, base64);
        }
    },

    /**
     * Represents a UUID pattern, following the standard UUID format.
     */
    UUID(Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
        // Case-insensitive flag doesn't work for some UUIDs.
        @Override
        public GameProfile getProfile(String uuidString) {
            return Profileable.of(java.util.UUID.fromString(uuidString)).getProfile();
        }
    },

    /**
     * Represents a username pattern, allowing alphanumeric characters and underscores, with a length of 1 to 16 characters.
     * Minecraft now requires the username to be at least 3 characters long, but older accounts are still around.
     * It also seems that there are a few inactive accounts that use spaces in their usernames?
     */
    USERNAME(Pattern.compile("[A-Za-z0-9_]{1,16}")) {
        @Override
        public GameProfile getProfile(String username) {
            return Profileable.username(username).getProfile();
        }
    };

    /**
     * The RegEx pattern associated with the input type.
     */
    @ApiStatus.Internal
    public final Pattern pattern;
    private static final ProfileInputType[] VALUES = values();

    ProfileInputType(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Retrieves a {@link GameProfile} based on the provided input string.
     *
     * @param input The input string to retrieve the profile for.
     * @return The {@link GameProfile} corresponding to the input string.
     */
    public abstract GameProfile getProfile(String input);

    /**
     * Returns the corresponding {@link ProfileInputType} for the given identifier, if it matches any pattern.
     *
     * @param identifier The string to be checked against the patterns.
     * @return The matching type, or {@code null} if no match is found.
     */
    @Nullable
    public static ProfileInputType typeOf(@Nonnull String identifier) {
        Objects.requireNonNull(identifier, "Identifier cannot be null");
        return Arrays.stream(VALUES)
                .filter(value -> value.pattern.matcher(identifier).matches())
                .findFirst().orElse(null);
    }

    /**
     * Extracts the texture hash from the provided input string.
     * <p>
     * Will not work reliably if NBT is passed: {"textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/74133f6ac3be2e2499a784efadcfffeb9ace025c3646ada67f3414e5ef3394"}}}
     * because there are several other texture types, see {@link #BASE64} inner comments.
     *
     * @param input The input string containing the texture hash.
     * @return The extracted texture hash.
     */
    @Nullable
    private static String extractTextureHash(String input) {
        Matcher matcher = ProfileInputType.TEXTURE_HASH.pattern.matcher(input);
        return matcher.find() ? matcher.group() : null;
    }
}