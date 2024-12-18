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

package com.cryptomorin.xseries.reflection.constraint;

import com.cryptomorin.xseries.reflection.ReflectiveHandle;
import com.cryptomorin.xseries.reflection.XAccessFlag;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Member;

/**
 * A constraint that controls the {@code public, protected, private} state of a {@link Class} or {@link Member}.
 * @since 12.0.0
 */
@ApiStatus.Experimental
public enum VisibilityConstraint implements ReflectiveConstraint {
    PUBLIC(XAccessFlag.PUBLIC), PRIVATE(XAccessFlag.PRIVATE), PROTECTED(XAccessFlag.PROTECTED);

    private final XAccessFlag accessFlag;

    VisibilityConstraint(XAccessFlag accessFlag) {this.accessFlag = accessFlag;}

    /**
     * @param handle the reflective handle of the object.
     * @param jvm A {@link Class} or {@link Member}.
     */
    @Override
    public Result appliesTo(ReflectiveHandle<?> handle, Object jvm) {
        int mods;

        if (jvm instanceof Class) {
            mods = ((Class<?>) jvm).getModifiers();

            if (this == PRIVATE) return Result.INCOMPATIBLE;
            if (this == PROTECTED) {
                // The so called "package-private" classes basically have no visibility flags.
                // Classes in general can only have the "public" visibility flag, so we simply
                // check if this is set or not.
                return Result.of(!XAccessFlag.PUBLIC.isSet(mods));
            }
        } else if (jvm instanceof Member) {
            // Fields, methods and constructors
            mods = ((Member) jvm).getModifiers();
        } else {
            return Result.INCOMPATIBLE;
        }

        return Result.of(accessFlag.isSet(mods));
    }

    @Override
    public String category() {
        return "Visibility";
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "::" + name();
    }
}
