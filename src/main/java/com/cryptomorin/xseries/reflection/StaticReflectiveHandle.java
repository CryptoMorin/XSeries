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

package com.cryptomorin.xseries.reflection;

import com.cryptomorin.xseries.reflection.jvm.objects.ReflectedObject;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ReflectiveHandle} with a fixed value.
 *
 * @since 14.1.0
 */
public class StaticReflectiveHandle<T> implements ReflectiveHandle<T> {
    private final T reflected;
    private final ReflectiveHandle<ReflectedObject> jvm;

    public StaticReflectiveHandle(T reflected, ReflectedObject jvm) {
        this.reflected = reflected;
        this.jvm = new StaticReflectiveHandle<>(jvm);
    }

    private StaticReflectiveHandle(T reflected) {
        this.reflected = reflected;
        this.jvm = null;
    }

    @Override
    public ReflectiveHandle<T> copy() {
        return this;
    }

    @Override
    public @NotNull T reflect() throws ReflectiveOperationException {
        return reflected;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull ReflectiveHandle<ReflectedObject> jvm() {
        return jvm == null ? (ReflectiveHandle<ReflectedObject>) this : jvm;
    }
}
