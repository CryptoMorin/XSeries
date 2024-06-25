package com.cryptomorin.xseries.profiles.exceptions;

import com.cryptomorin.xseries.profiles.builder.ProfileInstruction;

/**
 * Aggregate error container for {@link ProfileInstruction#apply()}.
 */
public final class ProfileChangeException extends ProfileException {
    public ProfileChangeException(String message) {
        super(message);
    }

    public ProfileChangeException(String message, Throwable cause) {
        super(message, cause);
    }
}