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

package com.cryptomorin.xseries.reflection.jvm;

import com.cryptomorin.xseries.reflection.XAccessFlag;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;

/**
 * This class should not be used directly.
 * <p>
 * Any reflective JVM object that has a name and can have modifiers (public, private, final static, etc),
 * like {@link java.lang.reflect.Field} or {@link java.lang.reflect.Method}
 */
public abstract class FlaggedNamedMemberHandle extends NamedMemberHandle {
    protected ClassHandle returnType;

    protected FlaggedNamedMemberHandle(ClassHandle clazz) {
        super(clazz);
    }

    public FlaggedNamedMemberHandle asStatic() {
        this.accessFlags.add(XAccessFlag.STATIC);
        return this;
    }

    public FlaggedNamedMemberHandle returns(Class<?> clazz) {
        this.returnType = XReflection.of(clazz);
        return this;
    }

    public FlaggedNamedMemberHandle returns(ClassHandle clazz) {
        this.returnType = clazz;
        return this;
    }

    public static Class<?>[] getParameters(Object owner, ClassHandle[] parameterTypes) {
        Class<?>[] classes = new Class[parameterTypes.length];
        int i = 0;
        for (ClassHandle parameterType : parameterTypes) {
            try {
                classes[i++] = parameterType.unreflect();
            } catch (Throwable ex) {
                throw XReflection.throwCheckedException(new ReflectiveOperationException(
                        "Unknown parameter " + parameterType + " for " + owner, ex
                ));
            }
        }
        return classes;
    }

    protected Class<?> getReturnType() {
        try {
            return this.returnType.unreflect();
        } catch (Throwable ex) {
            throw XReflection.throwCheckedException(new ReflectiveOperationException(
                    "Unknown return type " + returnType + " for " + this
            ));
        }
    }
}
