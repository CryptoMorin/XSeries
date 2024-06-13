package com.cryptomorin.xseries.profiles.exceptions;

public final class PlayerProfileNotFoundException extends RuntimeException {
    public PlayerProfileNotFoundException(String message) {
        super(message);
    }
}