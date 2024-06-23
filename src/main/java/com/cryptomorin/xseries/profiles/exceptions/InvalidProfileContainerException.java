package com.cryptomorin.xseries.profiles.exceptions;

/**
 * When a provided item/block cannot contain skull textures.
 */
public final class InvalidProfileContainerException extends ProfileException {
    public InvalidProfileContainerException(String message) {
        super(message);
    }

    public InvalidProfileContainerException(String message, Throwable cause) {
        super(message, cause);
    }
}