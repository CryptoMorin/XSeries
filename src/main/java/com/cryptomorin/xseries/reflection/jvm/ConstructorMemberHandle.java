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

package com.cryptomorin.xseries.reflection.jvm;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;
import com.cryptomorin.xseries.reflection.parser.ReflectionParser;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A handle for using reflection for {@link Constructor}.
 */
public class ConstructorMemberHandle extends MemberHandle {
    protected ClassHandle[] parameterTypes = new ClassHandle[0];

    public ConstructorMemberHandle(ClassHandle clazz) {
        super(clazz);
    }

    public ConstructorMemberHandle parameters(Class<?>... parameterTypes) {
        this.parameterTypes = Arrays.stream(parameterTypes).map(XReflection::of).toArray(ClassHandle[]::new);
        return this;
    }

    public ConstructorMemberHandle parameters(ClassHandle... parameterTypes) {
        this.parameterTypes = parameterTypes;
        return this;
    }

    @Override
    public MethodHandle reflect() throws ReflectiveOperationException {
        if (isFinal) throw new UnsupportedOperationException("Constructor cannot be final: " + this);
        if (makeAccessible) {
            return clazz.getNamespace().getLookup().unreflectConstructor(reflectJvm());
        } else {
            Class<?>[] parameterTypes = FlaggedNamedMemberHandle.getParameters(this, this.parameterTypes);
            return clazz.getNamespace().getLookup().findConstructor(clazz.unreflect(),
                    MethodType.methodType(void.class, parameterTypes));
        }
    }

    @Override
    public ConstructorMemberHandle signature(String declaration) {
        return new ReflectionParser(declaration).imports(clazz.getNamespace()).parseConstructor(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Constructor<?> reflectJvm() throws ReflectiveOperationException {
        Class<?>[] parameterTypes = FlaggedNamedMemberHandle.getParameters(this, this.parameterTypes);
        return handleAccessible(clazz.unreflect().getDeclaredConstructor(parameterTypes));
    }

    @Override
    public ConstructorMemberHandle copy() {
        ConstructorMemberHandle handle = new ConstructorMemberHandle(clazz);
        handle.parameterTypes = this.parameterTypes;
        handle.isFinal = this.isFinal;
        handle.makeAccessible = this.makeAccessible;
        return handle;
    }

    @Override
    public String toString() {
        String str = this.getClass().getSimpleName() + '{';
        if (makeAccessible) str += "protected/private ";
        str += clazz.toString() + ' ';
        str += '(' + Arrays.stream(parameterTypes).map(ClassHandle::toString).collect(Collectors.joining(", ")) + ')';
        return str + '}';
    }
}
