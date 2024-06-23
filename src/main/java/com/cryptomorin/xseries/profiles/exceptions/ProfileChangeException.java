package com.cryptomorin.xseries.profiles.exceptions;

public final class ProfileChangeException extends ProfileException {
    public ProfileChangeException(String message) {
        super(message);
    }

    public ProfileChangeException(String message, Throwable cause) {
        super(message, cause);
    }
}