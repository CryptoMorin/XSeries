/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.cryptomorin.xseries.reflection;

import com.cryptomorin.xseries.reflection.aggregate.AggregateReflectiveHandle;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an object that can be used for reflection operations.
 *
 * @param <T> The JVM type associated with this handle (e.g. {@link Class}, {@link java.lang.reflect.Field},
 *            {@link java.lang.reflect.Method}, or {@link java.lang.reflect.Constructor})
 * @see AggregateReflectiveHandle
 * @see com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle
 * @see com.cryptomorin.xseries.reflection.jvm.MethodMemberHandle
 * @see com.cryptomorin.xseries.reflection.jvm.FieldMemberHandle
 * @see com.cryptomorin.xseries.reflection.jvm.ConstructorMemberHandle
 */
public interface ReflectiveHandle<T> {
    /**
     * Creates a new handle with the same properties.
     */
    @ApiStatus.Experimental
    ReflectiveHandle<T> copy();

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
     *
     * @deprecated I don't think there's going to be any practical use for this method.
     */
    @Nullable
    @ApiStatus.Obsolete
    @Deprecated
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
     *
     * @throws ReflectiveOperationException throws silently.
     * @see #reflect()
     * @see #reflectOrNull()
     */
    @SuppressWarnings("JavadocDeclaration")
    @NotNull
    default T unreflect() {
        try {
            return reflect();
        } catch (ReflectiveOperationException e) {
            throw XReflection.throwCheckedException(e);
        }
    }

    /**
     * Same as {@link #reflect()} except that it will return null if any {@link ReflectiveOperationException} errors occur.
     *
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
     *
     * @return the final JVM object that exists in this runtime.
     * @throws ReflectiveOperationException if any errors occur while getting the object, including unknown objects, security issues, etc...
     * @see #reflectOrNull()
     * @see #unreflect()
     */
    @NotNull
    T reflect() throws ReflectiveOperationException;
}
