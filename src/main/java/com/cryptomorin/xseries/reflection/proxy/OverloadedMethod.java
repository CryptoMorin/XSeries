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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The descriptor map should only refer to parameter descriptors and should not contain the return value descriptor,
 * this causes less memory usage and more performance.
 */
@ApiStatus.Internal
public abstract class OverloadedMethod<T> {
    public abstract T get(Supplier<String> descriptor);

    public abstract Collection<T> getOverloads();

    /**
     * Similar to ASM's {@link org.objectweb.asm.Type#getArgumentTypes()}.
     * However, This is not actually a standard descriptor, it aims to minimize the
     * string as much as possible without conflicts.
     */
    static String getParameterDescriptor(Class<?>[] parameters) {
        StringBuilder stringBuilder = new StringBuilder(parameters.length * 10);

        for (Class<?> parameter : parameters) {
            Class<?> currentClass = parameter;
            while (currentClass.isArray()) {
                stringBuilder.append('[');
                currentClass = currentClass.getComponentType();
            }

            if (currentClass.isPrimitive()) {
                stringBuilder.append(getDescriptor(currentClass));
            } else if (currentClass == String.class) {
                stringBuilder.append('@');
            } else {
                stringBuilder.append(currentClass.getName());
            }
        }

        return stringBuilder.toString();
    }

    static char getDescriptor(Class<?> currentClass) {
        if (currentClass == Integer.TYPE) return 'I';
        else if (currentClass == Void.TYPE) return 'V';
        else if (currentClass == Boolean.TYPE) return 'Z';
        else if (currentClass == Byte.TYPE) return 'B';
        else if (currentClass == Character.TYPE) return 'C';
        else if (currentClass == Short.TYPE) return 'S';
        else if (currentClass == Double.TYPE) return 'D';
        else if (currentClass == Float.TYPE) return 'F';
        else if (currentClass == Long.TYPE) return 'J';
        else throw new AssertionError("Unknown primitive: " + currentClass);
    }

    public static final class Builder<T> {
        private final Map<String, Map<String, T>> descriptorMap = new HashMap<>(10);
        private final Function<T, String> descritporProcessor;

        public Builder(Function<T, String> descritporProcessor) {this.descritporProcessor = descritporProcessor;}

        public void add(T method, String name) {
            Map<String, T> descriptors = descriptorMap.computeIfAbsent(name, k -> new HashMap<>(3));
            String descriptor = descritporProcessor.apply(method);
            if (descriptors.put(descriptor, method) != null) {
                throw new IllegalArgumentException("Method named '" + name + "' with descriptor '" + descriptor + "' was already added: " + descriptors);
            }
        }

        public ClassOverloadedMethods<T> build() {
            HashMap<String, OverloadedMethod<T>> nameMapped = new HashMap<>(descriptorMap.size());
            ClassOverloadedMethods<T> classOverloadedMethods = new ClassOverloadedMethods<>(nameMapped);
            build(nameMapped);
            return classOverloadedMethods;
        }

        public void build(Map<String, OverloadedMethod<T>> nameMapped) {
            for (Map.Entry<String, Map<String, T>> method : descriptorMap.entrySet()) {
                OverloadedMethod<T> overload;
                if (method.getValue().size() == 1) {
                    overload = new Single<>(method.getValue().values().iterator().next());
                } else {
                    overload = new Multi<>(method.getValue());
                }

                nameMapped.put(method.getKey(), overload);
            }
        }
    }

    private static final class Single<T> extends OverloadedMethod<T> {
        private final T object;

        public Single(T object) {this.object = object;}

        @Override
        public T get(Supplier<String> descriptor) {
            return object;
        }

        @Override
        public Collection<T> getOverloads() {
            return Collections.singleton(object);
        }

        @Override
        public String toString() {
            return "Single";
        }
    }

    private static final class Multi<T> extends OverloadedMethod<T> {
        private final Map<String, T> descriptorMap;

        public Multi(Map<String, T> descriptorMap) {this.descriptorMap = descriptorMap;}

        @Override
        public T get(Supplier<String> descriptor) {
            return descriptorMap.get(descriptor.get());
        }

        @Override
        public Collection<T> getOverloads() {
            return descriptorMap.values();
        }

        @Override
        public String toString() {
            return "Multi(" + descriptorMap.keySet() + ')';
        }
    }
}
