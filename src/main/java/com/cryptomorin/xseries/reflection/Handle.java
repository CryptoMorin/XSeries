package com.cryptomorin.xseries.reflection;

public interface Handle<T> {
    default boolean exists() {
        try {
            reflect();
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    default ReflectiveOperationException catchError() {
        try {
            reflect();
            return null;
        } catch (ReflectiveOperationException ex) {
            return ex;
        }
    }

    default T unreflect() {
        try {
            return reflect();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    T reflect() throws ReflectiveOperationException;
}
