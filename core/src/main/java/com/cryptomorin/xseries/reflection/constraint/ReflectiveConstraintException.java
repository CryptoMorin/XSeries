/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2026 Crypto Morin
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
import com.cryptomorin.xseries.reflection.XAccessFlag;

import java.util.Optional;

/**
 * Thrown when a {@link ReflectiveConstraint#appliesTo(ReflectiveHandle, Object)}
 * returns {@link Optional#empty()} or {@code false}.
 *
 * @since 12.0.0
 */
public class ReflectiveConstraintException extends RuntimeException {
    private final ReflectiveConstraint constraint;
    private final ReflectiveConstraint.Result result;

    private ReflectiveConstraintException(ReflectiveConstraint constraint, ReflectiveConstraint.Result result, String message) {
        super(message);
        this.constraint = constraint;
        this.result = result;
    }

    public ReflectiveConstraint getConstraint() {
        return constraint;
    }

    public ReflectiveConstraint.Result getResult() {
        return result;
    }

    public static ReflectiveConstraintException create(ReflectiveConstraint constraint,
                                                       ReflectiveConstraint.Result result,
                                                       ReflectiveHandle<?> handle, Object jvm) {
        String message;
        switch (result) {
            case MATCHED:
                throw new IllegalArgumentException("Cannot create an exception if results are successful: " + constraint + " -> MATCHED");
            case INCOMPATIBLE:
                message = "The constraint " + constraint + " cannot be applied to " + handle;
                break;
            case NOT_MATCHED:
                message = "Found " + handle + " with JVM " + jvm
                        + ", however it doesn't match the constraint: "
                        + constraint + " - " + XAccessFlag.getModifiers(jvm).map(XAccessFlag::toString).orElse("[NO MODIFIER]");
                break;
            default:
                throw new AssertionError("Unknown reflective constraint result: " + result);
        }

        return new ReflectiveConstraintException(constraint, result, message);
    }
}
