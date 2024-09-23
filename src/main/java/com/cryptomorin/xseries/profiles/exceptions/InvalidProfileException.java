package com.cryptomorin.xseries.profiles.exceptions;

import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.jetbrains.annotations.NotNull;

/**
 * If a given {@link Profileable} has incorrect value (more general than {@link UnknownPlayerException})
 * which mostly happens when a string value is passed to be handled by {@link com.cryptomorin.xseries.profiles.objects.ProfileInputType}.
 * E.g. invalid Base64, texture URLs, username, UUID, etc.
 * Though most of these invalid values are just recognized as an unknown string instead
 * of being invalid values of a specific input type.
 * <p>
 * Note: Unknown usernames and UUIDs are handled by {@link UnknownPlayerException} instead.
 */
public class InvalidProfileException extends ProfileException {
    private final String value;

    public InvalidProfileException(String value, String message) {
        super(message);
        this.value = value;
    }

    public InvalidProfileException(String value, String message, Throwable cause) {
        super(message, cause);
        this.value = value;
    }

    /**
     * The invalid value.
     */
    @NotNull
    public String getValue() {
        return value;
    }
}