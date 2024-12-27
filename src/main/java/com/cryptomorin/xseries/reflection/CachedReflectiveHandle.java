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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ReflectiveHandle} that caches {@link ReflectiveHandle#reflect()} and {@link ReflectiveHandle#jvm()}.
 * Using this is not recommended. Please read {@link com.cryptomorin.xseries.reflection.XReflection}
 * <b>Performance & Caching</b> section for more information.
 *
 * @since 14.0.0
 */
@ApiStatus.Internal
class CachedReflectiveHandle<T> implements ReflectiveHandle<T> {
    private final ReflectiveHandle<T> delegate;
    private T cache;
    private CachedReflectiveHandle<ReflectedObject> jvm;

    CachedReflectiveHandle(ReflectiveHandle<T> delegate) {
        this.delegate = delegate;
    }

    public ReflectiveHandle<T> getDelegate() {
        return delegate;
    }

    @Override
    public ReflectiveHandle<T> copy() {
        return delegate.copy();
    }

    @Override
    public @NotNull T reflect() throws ReflectiveOperationException {
        return cache == null ? (cache = delegate.reflect()) : cache;
    }

    @Override
    public @NotNull ReflectiveHandle<ReflectedObject> jvm() {
        return jvm == null ? (jvm = new CachedReflectiveHandle<>(delegate.jvm())) : jvm;
    }
}
