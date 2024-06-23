package com.cryptomorin.xseries.profiles.exceptions;

import com.cryptomorin.xseries.profiles.objects.Profileable;

/**
 * If a specific player-identifying {@link Profileable} is not found.
 */
public final class UnknownPlayerException extends InvalidProfileException {
    public UnknownPlayerException(String message) {
        super(message);
    }
}