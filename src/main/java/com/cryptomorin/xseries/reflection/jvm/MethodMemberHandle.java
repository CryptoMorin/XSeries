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
import com.cryptomorin.xseries.reflection.jvm.classes.PackageHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftMapping;
import com.cryptomorin.xseries.reflection.parser.ReflectionParser;
import org.intellij.lang.annotations.Pattern;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A handle for using reflection for {@link Method}.
 */
public class MethodMemberHandle extends FlaggedNamedMemberHandle {
    protected ClassHandle[] parameterTypes = new ClassHandle[0];

    public MethodMemberHandle(ClassHandle clazz) {
        super(clazz);
    }

    /**
     * Overrides any previously set parameters.
     */
    public MethodMemberHandle parameters(ClassHandle... parameterTypes) {
        this.parameterTypes = parameterTypes;
        return this;
    }

    public MethodMemberHandle returns(Class<?> clazz) {
        super.returns(clazz);
        return this;
    }

    public MethodMemberHandle returns(ClassHandle clazz) {
        super.returns(clazz);
        return this;
    }

    public MethodMemberHandle asStatic() {
        super.asStatic();
        return this;
    }

    public MethodMemberHandle parameters(Class<?>... parameterTypes) {
        this.parameterTypes = Arrays.stream(parameterTypes).map(XReflection::of).toArray(ClassHandle[]::new);
        return this;
    }

    @Override
    public MethodHandle reflect() throws ReflectiveOperationException {
        return clazz.getNamespace().getLookup().unreflect(reflectJvm());
    }

    @Override
    public MethodMemberHandle signature(String declaration) {
        return new ReflectionParser(declaration).imports(clazz.getNamespace()).parseMethod(this);
    }

    public MethodMemberHandle map(MinecraftMapping mapping, @Pattern(PackageHandle.JAVA_IDENTIFIER_PATTERN) String name) {
        super.map(mapping, name);
        return this;
    }

    public MethodMemberHandle named(@Pattern(PackageHandle.JAVA_IDENTIFIER_PATTERN) String... names) {
        super.named(names);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Method reflectJvm() throws ReflectiveOperationException {
        Objects.requireNonNull(returnType, "Return type not specified");
        if (names.isEmpty()) throw new IllegalStateException("No names specified");

        NoSuchMethodException errors = null;
        Method method = null;

        Class<?> clazz = this.clazz.reflect();
        Class<?>[] parameterTypes = FlaggedNamedMemberHandle.getParameters(this, this.parameterTypes);
        Class<?> returnType = getReturnType();

        for (String name : this.names) {
            if (method != null) break;
            try {
                method = clazz.getDeclaredMethod(name, parameterTypes);
                if (method.getReturnType() != returnType) {
                    throw new NoSuchMethodException("Method named '" + name + "' was found but the return types don't match: " + this.returnType + " != " + method.getReturnType());
                }
            } catch (NoSuchMethodException ignored) {
                NoSuchMethodException realEx;

                try {
                    // Maybe the method was moved to a superclass?
                    // We won't be able to get it if it's private/protected tho.
                    method = clazz.getMethod(name, parameterTypes);
                    if (method.getReturnType() != returnType) {
                        throw new NoSuchMethodException("Method named '" + name + "' was found but the return types don't match: " + this.returnType + " != " + method.getReturnType());
                    }
                    continue;
                } catch (NoSuchMethodException ex2) {
                    realEx = ex2; // Might give more info?
                    method = null;
                }

                if (errors == null) errors = new NoSuchMethodException("None of the methods were found for " + this);
                errors.addSuppressed(realEx);
            }
        }

        if (method == null) throw XReflection.relativizeSuppressedExceptions(errors);
        return handleAccessible(method);
    }

    @Override
    public MethodMemberHandle clone() {
        MethodMemberHandle handle = new MethodMemberHandle(clazz);
        handle.returnType = this.returnType;
        handle.parameterTypes = this.parameterTypes;
        handle.isFinal = this.isFinal;
        handle.makeAccessible = this.makeAccessible;
        handle.names.addAll(this.names);
        return handle;
    }

    @Override
    public String toString() {
        String str = this.getClass().getSimpleName() + '{';
        if (makeAccessible) str += "protected/private ";
        if (isFinal) str += "final ";
        if (returnType != null) str += returnType + " ";
        str += String.join("/", names);
        str += '(' + Arrays.stream(parameterTypes).map(ClassHandle::toString).collect(Collectors.joining(", ")) + ')';
        return str + '}';
    }
}
