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

package com.cryptomorin.xseries.test.reflection.asm;

import com.cryptomorin.xseries.reflection.proxy.ReflectiveProxyObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * This class is merely a playground for ASM generation code samples.
 */
@SuppressWarnings("all")
public class ASMGeneratedSample implements ReflectiveProxyObject {
    public static final int somefield = Integer.parseInt("4");
    public Object instance;

    public ASMGeneratedSample(Object instace) {

    }

    int getField() {
        return somefield;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (this.instance == null) return false;
        if (obj == null) return false;

        if (obj instanceof ASMGeneratedSample) {
            return this.instance.equals(((ASMGeneratedSample) obj).instance());
        }
        if (obj instanceof StringBuilder) {
            return this.instance.equals(obj);
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (this.instance == null) return this.hashCode();
        return this.instance.hashCode();
    }

    @Override
    public ASMGeneratedSample instance() {
        return (ASMGeneratedSample) instance;
    }

    @Override
    public @NotNull Class<?> getTargetClass() {
        return null;
    }

    @Override
    public @NotNull boolean isInstance(@Nullable Object object) {
        return false;
    }

    private Object doubleCtor() {
        return new ASMGeneratedSample(new StringBuilder(3));
    }

    private void varArgField(Class... test) {

    }

    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle handle;

    {
        try {
            handle = lookup.findStatic(Class.class, "forName", MethodType.methodType(Class.class, String.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private String testPrivate() throws Throwable {
        return (String) handle.invoke(instance, 3, true);
    }

    private void callVarArg() {
        varArgField(String.class, int.class, void.class);
    }

    @Override
    public ReflectiveProxyObject bindTo(Object instance) {
        return new ASMGeneratedSample(null);
    }

    public ASMGeneratedSample bindTo(ASMGeneratedSample instance) {
        if (this.instance != null) {
            throw new UnsupportedOperationException("bindTo() must be called from the factory object, not on an instance");
        }

        if (instance == null) {
            throw new IllegalArgumentException("Cannot bind to null instance");
        }

        return new ASMGeneratedSample(instance);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(test=" + instance + ')';
    }
}
