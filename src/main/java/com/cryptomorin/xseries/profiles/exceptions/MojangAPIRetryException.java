package com.cryptomorin.xseries.profiles.exceptions;

/**
 * Due to being ratelimited or network issues that can be fixed if the request is sent later again.
 */
public final class MojangAPIRetryException extends MojangAPIException {
    public enum Reason {
        CONNECTION_RESET, CONNECTION_TIMEOUT, RATELIMITED
    }

    private final Reason reason;

    public MojangAPIRetryException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public MojangAPIRetryException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}