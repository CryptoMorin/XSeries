/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Crypto Morin
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

package com.cryptomorin.xseries.reflection.constraint;

import com.cryptomorin.xseries.reflection.ReflectiveHandle;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A set of checks performed on a {@link ReflectiveHandle} to determine whether it meets
 * certain requirements based on its JVM signature before we can use it.
 * Usually these are conditions that the standard Java's reflection API
 * doesn't care about. For example, {@link Class#forName(String)} doesn't
 * care whether the class is an {@code enum/interface/abstract}, but you could
 * add these additional checks using {@link ClassTypeConstraint}.
 * <p>
 * If the check against {@link #appliesTo(ReflectiveHandle, Object)} fails,
 * a {@link ReflectiveConstraintException} should be thrown.
 * <p>
 * All constraints must be incompatible with each other. Menaing that two different
 * constraints from the same class/category should not be able to co-exist.
 * <p>
 * This class should not be directly used in most cases.
 *
 * @see VisibilityConstraint
 * @see ClassTypeConstraint
 * @since 12.0.0
 */
@ApiStatus.Experimental
public interface ReflectiveConstraint {
    /**
     * The category name of this constraint.
     */
    @Contract(pure = true)
    String category();

    /**
     * The name of this constraint.
     */
    @Contract(pure = true)
    String name();

    /**
     * Whether this constraint can be applied and applies to the given item.
     * <p>
     * Usually a {@link ReflectiveConstraintException} should be thrown if the result is
     * either {@link Result#INCOMPATIBLE} or {@link Result#NOT_MATCHED}.
     *
     * @param handle the reflective handle of the object.
     * @param jvm    the corresponding JVM object (not {@link java.lang.invoke.MethodHandle}) of the handle.
     * @return Refer to {@link Result} for details.
     */
    @NotNull
    @Contract(pure = true)
    Result appliesTo(@NotNull ReflectiveHandle<?> handle, @NotNull Object jvm);

    enum Result {
        /**
         * This constraint cannot be applied to the given JVM object al all.
         */
        INCOMPATIBLE,

        /**
         * The constraint is compatible with the given JVM object, however the
         * JVM object did not meet the requirements for this constraint.
         */
        NOT_MATCHED,

        /**
         * The constraint is compatible and matched with the given JVM object.
         */
        MATCHED;

        /**
         * Simple method that should only be used if this constraint is compatible ({@link #INCOMPATIBLE} is not possible).
         */
        @ApiStatus.Internal
        public static Result of(boolean test) {
            return test ? Result.MATCHED : Result.NOT_MATCHED;
        }
    }
}
