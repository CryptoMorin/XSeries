package com.cryptomorin.xseries.profiles.exceptions;

/**
 * If any unknown non-recoverable network issues occur, unless it's a {@link MojangAPIRetryException}
 */
public class MojangAPIException extends ProfileException {
    public MojangAPIException(String message) {
        super(message);
    }

    public MojangAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}