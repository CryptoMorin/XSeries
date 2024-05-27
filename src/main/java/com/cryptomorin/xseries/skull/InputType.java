package com.cryptomorin.xseries.skull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * The {@code InputType} enum represents different types of input patterns that can be used for identifying
 * and validating various formats such as texture hashes, URLs, Base64 encoded strings, UUIDs, and usernames.
 */
public enum InputType {

    /**
     * Represents a texture hash pattern.
     */
    TEXTURE_HASH(Pattern.compile("[0-9a-z]{55,70}")),

    /**
     * Represents a texture URL pattern that includes the base URL followed by the texture hash pattern.
     */
    TEXTURE_URL(Pattern.compile(Pattern.quote(XSkull.TEXTURES) + TEXTURE_HASH.pattern)),

    /**
     * Represents a Base64 encoded string pattern.
     */
    BASE64(Pattern.compile("[-A-Za-z0-9+/]{100,}={0,3}")),

    /**
     * Represents a UUID pattern, following the standard UUID format.
     */
    UUID(Pattern.compile("[A-F\\d]{8}-[A-F\\d]{4}-4[A-F\\d]{3}-([89AB])[A-F\\d]{3}-[A-F\\d]{12}", Pattern.CASE_INSENSITIVE)),

    /**
     * Represents a username pattern, allowing alphanumeric characters and underscores, with a length of 1 to 16 characters.
     */
    USERNAME(Pattern.compile("[A-Za-z0-9_]{1,16}"));

    /**
     * The regex pattern associated with the input type.
     */
    private final Pattern pattern;

    /**
     * Constructs an {@code InputType} with the specified regex pattern.
     *
     * @param pattern The regex pattern associated with the input type.
     */
    InputType(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Returns the corresponding {@code InputType} for the given identifier, if it matches any pattern.
     *
     * @param identifier The string to be checked against the patterns.
     * @return The matching {@code InputType}, or {@code null} if no match is found.
     */
    @Nullable
    public static InputType get(@Nonnull String identifier) {
        return Arrays.stream(InputType.values())
                .filter(value -> value.pattern.matcher(identifier).matches())
                .findFirst().orElse(null);
    }
}
