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

package com.cryptomorin.xseries.reflection.jvm.classes;

import com.cryptomorin.xseries.reflection.ReflectiveNamespace;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

/**
 * A class handle that was parsed from string, the name is known, but it doesn't exist
 * at runtime.
 *
 * @see StaticClassHandle
 */
public class UnknownClassHandle extends ClassHandle {
    private final String name;

    public UnknownClassHandle(ReflectiveNamespace namespace, String name) {
        super(namespace);
        this.name = name;
    }

    @Override
    public Set<String> getPossibleNames() {
        return Collections.singleton(name);
    }

    @Override
    public UnknownClassHandle asArray(int dimensions) {
        return new UnknownClassHandle(namespace, name + "[]");
    }

    @Override
    public boolean isArray() {
        return this.name.endsWith("[]");
    }

    @Override
    public UnknownClassHandle clone() {
        return new UnknownClassHandle(namespace, this.name);
    }

    @NotNull
    @Override
    public Class<?> reflect() throws ReflectiveOperationException {
        throw new ReflectiveOperationException("Unknown class: " + name);
    }

    @Override
    public String toString() {
        return "UnknownClassHandle(" + name + ')';
    }
}
