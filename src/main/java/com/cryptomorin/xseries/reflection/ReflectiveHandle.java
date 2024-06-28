package com.cryptomorin.xseries.reflection;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * Represents an object that can be used for reflection operations.
 * @param <T> The JVM type associated with this handle (e.g. {@link Class}, {@link java.lang.reflect.Field},
 *            {@link java.lang.reflect.Method}, or {@link java.lang.reflect.Constructor})
 * @see AggregateReflectiveHandle
 * @see com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle
 * @see com.cryptomorin.xseries.reflection.jvm.MethodMemberHandle
 * @see com.cryptomorin.xseries.reflection.jvm.FieldMemberHandle
 * @see com.cryptomorin.xseries.reflection.jvm.ConstructorMemberHandle
 */
public interface ReflectiveHandle<T> extends Cloneable {
    /**
     * Creates a new handle with the same properties.
     */
    @ApiStatus.Experimental
    ReflectiveHandle<T> clone();

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
     * @deprecated I don't think there's going to be any practical use for this method.
     */
    @Nullable
    @ApiStatus.Obsolete
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
     * @throws ReflectiveOperationException throws silently.
     */
    @SuppressWarnings("JavadocDeclaration")
    @Nonnull
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
    @Nullable
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
    @Nonnull
    T reflect() throws ReflectiveOperationException;
}
