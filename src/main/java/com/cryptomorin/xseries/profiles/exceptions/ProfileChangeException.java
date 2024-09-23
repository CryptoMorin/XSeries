package com.cryptomorin.xseries.profiles.exceptions;

import com.cryptomorin.xseries.profiles.builder.ProfileInstruction;

/**
 * Aggregate error container for {@link ProfileInstruction#apply()}.
 * All issues are added as suppressed exceptions ({@link Throwable#getSuppressed()}).
 */
public final class ProfileChangeException extends ProfileException {
    public ProfileChangeException(String message) {
        super(message);
    }
}