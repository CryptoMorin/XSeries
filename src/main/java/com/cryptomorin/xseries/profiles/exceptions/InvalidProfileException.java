package com.cryptomorin.xseries.profiles.exceptions;

import com.cryptomorin.xseries.profiles.objects.Profileable;

/**
 * if a given {@link Profileable} has incorrect value (more general than {@link UnknownPlayerException})
 */
public class InvalidProfileException extends ProfileException {
    public InvalidProfileException(String message) {
        super(message);
    }

    public InvalidProfileException(String message, Throwable cause) {
        super(message, cause);
    }
}