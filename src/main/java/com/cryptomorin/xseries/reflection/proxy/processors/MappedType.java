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

package com.cryptomorin.xseries.reflection.proxy.processors;

import com.cryptomorin.xseries.reflection.proxy.ReflectiveProxyObject;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

public final class MappedType {
    public static final Map<Class<? extends ReflectiveProxyObject>, Class<?>> LOOK_AHEAD = new IdentityHashMap<>();

    public final Class<?> synthetic, real;

    public MappedType(Class<?> synthetic, Class<?> real) {
        this.synthetic = synthetic;
        this.real = real;
    }

    public boolean isDifferent() {
        return synthetic != real;
    }

    public static Class<?>[] getRealTypes(MappedType[] types) {
        return Arrays.stream(types).map(x -> x.real).toArray(Class[]::new);
    }

    public static Class<?> getMappedTypeOrCreate(Class<? extends ReflectiveProxyObject> synthetic) {
        Class<?> real = MappedType.LOOK_AHEAD.get(synthetic);
        if (real == null) {
            ReflectiveAnnotationProcessor processor = new ReflectiveAnnotationProcessor(synthetic);
            processor.processTargetClass();
            real = processor.getTargetClass();
            if (real == null) {
                throw new IllegalStateException("Look ahead type " + synthetic + " could not be processed.");
            }
        }

        return real;
    }

    @Override
    public String toString() {
        return "MappedType[synthetic: " + synthetic + ", real: " + real + ']';
    }
}
