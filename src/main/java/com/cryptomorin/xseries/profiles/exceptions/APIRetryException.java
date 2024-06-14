package com.cryptomorin.xseries.profiles.exceptions;

public final class APIRetryException extends RuntimeException {
    public enum Reason {
        CONNECTION_RESET, CONNECTION_TIMEOUT, RATELIMITED
    }

    private final Reason reason;

    public APIRetryException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public APIRetryException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }
}