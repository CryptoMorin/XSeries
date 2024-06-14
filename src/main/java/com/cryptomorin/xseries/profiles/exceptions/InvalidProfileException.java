package com.cryptomorin.xseries.profiles.exceptions;

public class InvalidProfileException extends RuntimeException {
    public InvalidProfileException(String message) {
        super(message);
    }

    public InvalidProfileException(String message, Throwable cause) {
        super(message, cause);
    }
}