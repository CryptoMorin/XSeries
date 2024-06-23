package com.cryptomorin.xseries.profiles.exceptions;

/**
 * Represents any known errors that can occur for the profiles API.
 */
public class ProfileException extends RuntimeException {
    public ProfileException() {
    }

    public ProfileException(String message) {
        super(message);
    }

    public ProfileException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProfileException(Throwable cause) {
        super(cause);
    }

    public ProfileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
