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

package com.cryptomorin.xseries.reflection.proxy;

import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class ClassOverloadedMethods<T> {
    private final Map<String, OverloadedMethod<T>> mapped;

    public ClassOverloadedMethods(Map<String, OverloadedMethod<T>> mapped) {
        this.mapped = mapped;
    }

    public Map<String, OverloadedMethod<T>> mappings() {
        return mapped;
    }

    public T get(String name, Supplier<String> descriptor) {
        return get(name, descriptor, false);
    }

    public T get(String name, Supplier<String> descriptor, boolean ignoreIfNull) {
        OverloadedMethod<T> overloads = mapped.get(name);
        if (overloads == null) {
            if (ignoreIfNull) return null;
            throw new IllegalArgumentException("Failed to find any method named '" + name + "' with descriptor '" + descriptor.get() + "' " + this);
        }

        T overload = overloads.get(descriptor);
        if (overload == null) {
            if (ignoreIfNull) return null;
            throw new IllegalArgumentException("Failed to find overloaded method named '" + name
                    + " with descriptor: '" + descriptor.get() + "' " + this);
        }

        return overload;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '(' + mapped + ')';
    }
}
