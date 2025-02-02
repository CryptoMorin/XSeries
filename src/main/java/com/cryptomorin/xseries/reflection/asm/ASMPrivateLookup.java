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

package com.cryptomorin.xseries.reflection.asm;

import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Class used for {@link XReflectASM}'s generated class lookup. While we could generate the code inside the class itself,
 * there's no reason to do that if we can handle this outside.
 *
 * @since 14.0.0
 */
@ApiStatus.Internal
public final class ASMPrivateLookup {
    private final MethodHandles.Lookup lookup;
    private final Class<?> targetClass;

    public ASMPrivateLookup(Class<?> targetClass) {
        this.lookup = MethodHandles.lookup();
        this.targetClass = targetClass;
    }

    public MethodHandle findMethod(String name, Class<?> rType, Class<?>[] pTypes) throws IllegalAccessException {
        Method found = new ReflectionIterator<>(clazz -> clazz.getDeclaredMethod(name, pTypes)).find();
        if (found == null) {
            throw new IllegalArgumentException("Couldn't find method named '" + name + "' with type: "
                    + rType + " (" + Arrays.toString(pTypes) + ')');
        }
        found.setAccessible(true);
        return lookup.unreflect(found);
    }

    public MethodHandle findConstructor(Class<?>[] pTypes) throws IllegalAccessException {
        try {
            Constructor<?> found = targetClass.getDeclaredConstructor(pTypes);
            found.setAccessible(true);
            return lookup.unreflectConstructor(found);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Couldn't find constructor with type: "
                    + " (" + Arrays.toString(pTypes) + ')', e);
        }
    }

    public MethodHandle findField(String name, Class<?> rType, boolean getter) throws IllegalAccessException {
        Field found = new ReflectionIterator<>(clazz -> clazz.getDeclaredField(name)).find();
        if (found == null) {
            throw new IllegalArgumentException("Couldn't find field named '" + name + "' with type: " + rType);
        }
        found.setAccessible(true);
        return getter ? lookup.unreflectGetter(found) : lookup.unreflectSetter(found);
    }

    @FunctionalInterface
    private interface UnsafeFunction<I, O> {
        O apply(I input) throws Exception;
    }

    private final class ReflectionIterator<T> {
        private final UnsafeFunction<Class<?>, T> finder;

        private ReflectionIterator(UnsafeFunction<Class<?>, T> finder) {this.finder = finder;}

        private T find() {
            try {
                return iterate(targetClass);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        private T iterate(Class<?> clazz) throws Exception {
            if (clazz == Object.class) return null;

            try {
                return finder.apply(clazz);
            } catch (NoSuchMethodException | NoSuchFieldException ignored) {
                // Since it's an inaccessible member, that means we can't find it in interfaces,
                // so we're just going to climb the superclass hierarchy.
                return iterate(clazz.getSuperclass());
            }
        }
    }
}
