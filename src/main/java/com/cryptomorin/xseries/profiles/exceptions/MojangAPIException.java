package com.cryptomorin.xseries.profiles.exceptions;

public final class MojangAPIException extends RuntimeException {
    public MojangAPIException(String message) {
        super(message);
    }

    public MojangAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}