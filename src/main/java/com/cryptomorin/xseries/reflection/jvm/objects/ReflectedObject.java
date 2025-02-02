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

package com.cryptomorin.xseries.reflection.jvm.objects;

import com.cryptomorin.xseries.reflection.XAccessFlag;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.*;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a {@link Class}, {@link Constructor}, {@link Field} or {@link Method}.
 * The main purpose of this class is to create an abstraction layer for common methods
 * such as the annotation methods ({@link AnnotatedElement}), {@link #getModifiers()}, {@link #accessFlags()}, {@link #name()} and {@link #getDeclaringClass()}
 * as well as a common class representing such types and grouping them together instead of a vague {@link Object}.
 * A proper instance can be created using the {@code of()} method.
 * <p>
 * The {@link Object#toString()} (with a prefix), {@link Object#equals(Object)} and {@link Object#hashCode()} are delegated
 * to the underlying {@link #unreflect()} object.
 *
 * @since 13.0.0
 */
@ApiStatus.Experimental
public interface ReflectedObject extends AnnotatedElement {
    enum Type {CLASS, CONSTRUCTOR, METHOD, FIELD}

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    static ReflectedObject of(@NotNull Class<?> clazz) {
        return new ReflectedObjectClass(Objects.requireNonNull(clazz));
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    static ReflectedObject of(@NotNull Constructor<?> constructor) {
        return new ReflectedObjectConstructor(Objects.requireNonNull(constructor));
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    static ReflectedObject of(@NotNull Method method) {
        return new ReflectedObjectMethod(Objects.requireNonNull(method));
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    static ReflectedObject of(@NotNull Field field) {
        return new ReflectedObjectField(Objects.requireNonNull(field));
    }

    /**
     * Gets the underlying {@link Class}, {@link Constructor}, {@link Field} or {@link Method} object.
     */
    @NotNull
    @Contract(pure = true)
    Object unreflect();

    @NotNull
    @Contract(pure = true)
    Type type();

    /**
     * Gets the underlying JVM object which this class is a wrapper for.
     * Mostly useful as a friendly and compact alternative to {@link Object#toString()}.
     * <ul>
     *     <li><b>{@link Class}:</b> {@link Class#getSimpleName()}</li>
     *     <li><b>{@link Constructor}:</b> {@code <init>}</li>
     *     <li><b>{@link Method}:</b> {@link Method#getName()}</li>
     *     <li><b>{@link Field}:</b> {@link Field#getName()}</li>
     * </ul>
     */
    @NotNull
    @Contract(pure = true)
    String name();

    /**
     * The class that defines this reflected object.
     * This is obvious for {@link Constructor}, {@link Field} and {@link Method} objects,
     * but for {@link Class#getDeclaringClass()} it'd be enclosing class or {@code null} if none.
     * <p>
     * The subtilty with {@link Class#getDeclaringClass()} is that anonymous inner classes are not counted as member
     * of a class, whereas named inner classes are. Therefore, this method returns null for an anonymous class.
     * The alternative method {@link Class#getEnclosingClass()} works for both anonymous and named classes
     */
    @Nullable
    @Contract(pure = true)
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
     * <p> The modifier encodings are defined in section (JVM section 4.1)
     * of <cite>The Java Virtual Machine Specification</cite>.
     *
     * @return the {@code int} representing the modifiers for this class
     * @see java.lang.reflect.Modifier
     * @see XAccessFlag
     * @see #accessFlags()
     * @see <a
     * href="{@docRoot}/java.base/java/lang/reflect/package-summary.html#LanguageJvmModel">Java
     * programming language and JVM modeling in core reflection</a>
     * @since Java 1.1
     * JLS 8.1.1 Class Modifiers
     * JLS 9.1.1. Interface Modifiers
     * JVM 4.1 The {@code ClassFile} Structure
     */
    @Contract(pure = true)
    @MagicConstant(flagsFromClass = java.lang.reflect.Modifier.class)
    int getModifiers();

    /**
     * @return An unmodifiable set of the {@linkplain XAccessFlag access
     * flags} for this class, possibly empty
     *
     * <p> If the underlying class is an array class:
     * <ul>
     * <li> its {@code PUBLIC}, {@code PRIVATE} and {@code PROTECTED}
     *      access flags are the same as those of its component type
     * <li> its {@code ABSTRACT} and {@code FINAL} flags are present
     * <li> its {@code INTERFACE} flag is absent, even when the
     *      component type is an interface
     * </ul>
     * If this {@link Class} object represents a primitive type or
     * void, the flags are {@code PUBLIC}, {@code ABSTRACT}, and
     * {@code FINAL}.
     * For {@link Class} objects representing void, primitive types, and
     * arrays, access flags are absent other than as specified above.
     * @jvms 4.1 The ClassFile Structure
     * @jvms 4.7.6 The InnerClasses Attribute
     * @see XAccessFlag
     * @see java.lang.reflect.Modifier
     * @see #getModifiers()
     * @since Java 20
     */
    @NotNull
    @Unmodifiable
    @Contract(value = "-> new", pure = true)
    default Set<XAccessFlag> accessFlags() {
        return Collections.unmodifiableSet(XAccessFlag.of(getModifiers()));
    }
}
