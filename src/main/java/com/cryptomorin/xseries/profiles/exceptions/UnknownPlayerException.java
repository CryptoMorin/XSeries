package com.cryptomorin.xseries.profiles.exceptions;

import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.jetbrains.annotations.NotNull;

/**
 * If a specific player-identifying {@link Profileable} is not found.
 * This can be an unknown username or UUID. (Invalid usernames and UUIDs are handled by {@link InvalidProfileException}).
 */
public final class UnknownPlayerException extends InvalidProfileException {
    private final Object unknownObject;

    public UnknownPlayerException(Object unknownObject, String message) {
        super(unknownObject.toString(), message);
        this.unknownObject = unknownObject;
    }

    /**
     * An invalid or unknown username ({@link String}) or an unknown {@link java.util.UUID}.
     */
    @NotNull
    public Object getUnknownObject() {
        return unknownObject;
    }
}