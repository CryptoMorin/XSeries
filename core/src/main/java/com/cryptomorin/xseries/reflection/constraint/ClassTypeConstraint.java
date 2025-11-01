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
import com.cryptomorin.xseries.reflection.XAccessFlag;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * A constraint that controls what a
 *
 * @since 12.0.0
 */
@ApiStatus.Experimental
public enum ClassTypeConstraint implements ReflectiveConstraint {
    INTERFACE {
        @Override
        protected boolean test(Class<?> clazz) {
            return clazz.isInterface();
        }
    },

    ABSTRACT {
        @Override
        protected boolean test(Class<?> clazz) {
            return XAccessFlag.ABSTRACT.isSet(clazz.getModifiers());
        }
    },

    ENUM {
        @Override
        protected boolean test(Class<?> clazz) {
            return clazz.isEnum();
        }
    },

    RECORD {
        /**
         * What the fuck is this syntax??? Last time I checked we're not in kotlin...
         */
        private final transient MethodHandle isRecord;{
            MethodHandle isRecord0;
            try {
                isRecord0 = MethodHandles.lookup()
                        .findVirtual(Class.class, "isRecord", MethodType.methodType(boolean.class));
            } catch (NoSuchMethodException | IllegalAccessException ex) {
                isRecord0 = null;
            }
            isRecord = isRecord0;
        }

        @Override
        protected boolean test(Class<?> clazz) {
            try {
                return (boolean) isRecord.invoke(clazz);
            } catch (Throwable e) {
                throw new IllegalStateException("Cannot use Class#isRecord", e);
            }
        }
    },

    ANNOTATION {
        @Override
        protected boolean test(Class<?> clazz) {
            return clazz.isAnnotation();
        }
    };

    /**
     * We won't use {@link XAccessFlag} directly.
     */
    protected abstract boolean test(Class<?> clazz);

    /**
     * @param handle the reflective handle of the object.
     * @param jvm    A {@link Class}.
     */
    @Override
    public ReflectiveConstraint.Result appliesTo(ReflectiveHandle<?> handle, Object jvm) {
        if (jvm instanceof Class) {
            return Result.of(test((Class<?>) jvm));
        }
        return Result.INCOMPATIBLE;
    }

    @Override
    public String category() {
        return "ClassType";
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "::" + name();
    }
}
