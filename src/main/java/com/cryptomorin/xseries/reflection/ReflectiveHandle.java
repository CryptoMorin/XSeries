package com.cryptomorin.xseries.reflection;

/**
 * Represents an object that can be used for reflection operations.
 * @param <T> The JVM type associated with this handle (e.g. {@link Class} or {@link java.lang.reflect.Field})
 */
public interface ReflectiveHandle<T> {
    /**
     * @return true if this object exists at runtime, otherwise false.
     */
    default boolean exists() {
        try {
            reflect();
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    /**
     * Catches any {@link ReflectiveOperationException} thrown by {@link #reflect()}
     */
    default ReflectiveOperationException catchError() {
        try {
            reflect();
            return null;
        } catch (ReflectiveOperationException ex) {
            return ex;
        }
    }

    /**
     * An unchecked exception version of {@link #reflect()} (throws the original exception, not the a {@link RuntimeException})
     * @see #reflect()
     * @see #reflectOrNull()
     */
    default T unreflect() {
        try {
            return reflect();
        } catch (ReflectiveOperationException e) {
            throw XReflection.throwCheckedException(e);
        }
    }

    /**
     * Same as {@link #reflect()} except that it will return null if any {@link ReflectiveOperationException} errors occur.
     * @see #reflect()
     * @see #unreflect()
     */
    default T reflectOrNull() {
        try {
            return reflect();
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    /**
     * The heart of the class.
     * @return the final JVM object that exists in this runtime.
     * @throws ReflectiveOperationException if any errors occur while getting the object, including unknown objects, security issues, etc...
     * @see #reflectOrNull()
     * @see #unreflect()
     */
    T reflect() throws ReflectiveOperationException;
}
