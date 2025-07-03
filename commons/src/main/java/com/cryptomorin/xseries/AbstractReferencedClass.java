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

package com.cryptomorin.xseries;

/**
 * A class that delegates {@link Object} methods from {@link #object()}.
 *
 * @param <T>
 */
public abstract class AbstractReferencedClass<T> {
    protected abstract T object();

    /**
     * We cast to Object to force invokevirtual instead of invokeinterface.
     */
    protected final Object toVirtual() {
        return object();
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass().isInstance(obj.getClass())) {
            return toVirtual().equals(((AbstractReferencedClass<?>) obj).toVirtual());
        } else return object().equals(obj);
    }

    @Override
    public final int hashCode() {
        return toVirtual().hashCode();
    }

    @Override
    public final String toString() {
        return this.getClass().getSimpleName() + '(' + toVirtual().toString() + ')';
    }
}
