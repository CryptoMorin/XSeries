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

package com.cryptomorin.xseries.reflection.jvm.objects;

import com.cryptomorin.xseries.reflection.XAccessFlag;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Represents a {@link Class}, {@link Constructor}, {@link Field} or {@link Method}.
 * The main purpose of this class is to create an abstraction layer for common methods
 * such as the annotation methods ({@link AnnotatedElement}), {@link #getModifiers()}, {@link #accessFlags()}, {@link #name()} and {@link #getDeclaringClass()}.
 * A proper instance can be created using the {@code of()} method.
 * @since 13.0.0
 */
public interface ReflectedObject extends AnnotatedElement {
    enum Type {CLASS, CONSTRUCTOR, METHOD, FIELD}

    static ReflectedObject of(Class<?> clazz) {
        return new ReflectedObjectClass(clazz);
    }

    static ReflectedObject of(Constructor<?> constructor) {
        return new ReflectedObjectConstructor(constructor);
    }

    static ReflectedObject of(Method method) {
        return new ReflectedObjectMethod(method);
    }

    static ReflectedObject of(Field field) {
        return new ReflectedObjectField(field);
    }

    /**
     * Gets the underlying JVM object which this class is a wrapper for.
     */
    Object unreflect();

    Type type();

    String name();

    Class<?> getDeclaringClass();

    /**
     * Returns the Java language modifiers for this class or interface, encoded
     * in an integer. The modifiers consist of the Java Virtual Machine's
     * constants for {@code public}, {@code protected},
     * {@code private}, {@code final}, {@code static},
     * {@code abstract} and {@code interface}; they should be decoded
     * using the methods of class {@code Modifier}.
     *
     * <p> If the underlying class is an array class:
     * <ul>
     * <li> its {@code public}, {@code private} and {@code protected}
     *      modifiers are the same as those of its component type
     * <li> its {@code abstract} and {@code final} modifiers are always
     *      {@code true}
     * <li> its interface modifier is always {@code false}, even when
     *      the component type is an interface
     * </ul>
     * If this {@code Class} object represents a primitive type or
     * void, its {@code public}, {@code abstract}, and {@code final}
     * modifiers are always {@code true}.
     * For {@code Class} objects representing void, primitive types, and
     * arrays, the values of other modifiers are {@code false} other
     * than as specified above.
     *
     * <p> The modifier encodings are defined in section {JVM section 4.1}
     * of <cite>The Java Virtual Machine Specification</cite>.
     *
     * @return the {@code int} representing the modifiers for this class
     * @see     java.lang.reflect.Modifier
     * @see <a
     * href="{@docRoot}/java.base/java/lang/reflect/package-summary.html#LanguageJvmModel">Java
     * programming language and JVM modeling in core reflection</a>
     * @since 1.1
     * JLS 8.1.1 Class Modifiers
     * JLS 9.1.1. Interface Modifiers
     * JVM 4.1 The {@code ClassFile} Structure
     */
    int getModifiers();

    /**
     * @see XAccessFlag
     */
    default Set<XAccessFlag> accessFlags() {
        return XAccessFlag.of(getModifiers());
    }
}
