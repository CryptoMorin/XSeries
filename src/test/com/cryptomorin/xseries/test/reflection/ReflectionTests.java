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

package com.cryptomorin.xseries.test.reflection;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.classes.DynamicClassHandle;
import com.cryptomorin.xseries.reflection.parser.ReflectionParser;
import com.cryptomorin.xseries.test.util.XLogger;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static com.cryptomorin.xseries.test.util.XLogger.log;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

@SuppressWarnings("All")
public final class ReflectionTests {
    private final String test = "A";
    private static final String STATIC_TEST = "B";
    private static final ReflectionTests INSTANCE = new ReflectionTests();

    private static final class A<T> {
        private static final class B {
            private static final class C {
                public final AtomicInteger atomicField = new AtomicInteger();
            }
        }
    }

    public static EnumTest privateLookupIn(Class<?> targetClass, EnumTest caller) throws IllegalAccessException {
        XLogger.log("privateLookupIn!");
        return EnumTest.C;
    }

    public enum EnumTest {
        A, B, C;

        public static final int D = 9934343;
    }

    public ReflectionTests() {
    }

    public ReflectionTests(String test, int other) {

    }

    private String[] split(char ch, int limit, boolean withDelimiters) {
        return new String[]{"lim" + limit, ch + "a", "withDel" + withDelimiters};
    }

    public interface GameProfile {
        String test = "1";

        void field_setter_test(String test);

        String field_getter_test();
    }

    public static void test() {
        Arrays.stream(ReflectionParser.class.getDeclaredFields())
                .filter(x -> x.getType() == Pattern.class)
                .filter(x -> Modifier.isStatic(x.getModifiers()))
                .forEach(x -> {
                    try {
                        x.setAccessible(true);
                        log("field is " + x.getName() + " -> " + x.get(null));
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Failed to perform field test", e);
                    }
                });
        DynamicClassHandle clazz = new ReflectionParser("package com.cryptomorin.xseries.test.reflection; public final class ReflectionTests {}")
                .parseClass(XReflection.classHandle());
        try {
            XReflection.of(ReflectionTests.class).constructor("public ReflectionTests(String test, int other)").reflect();
            assertSame(XReflection.of(ReflectionTests.class).field("private final String test").getter().get(INSTANCE), INSTANCE.test);
            assertSame(XReflection.of(ReflectionTests.class).field("private static final String STATIC_TEST").getter().getStatic(), STATIC_TEST);

            // Inner class test
            MethodHandle innerinnerinnerField = XReflection.namespaced().imports(AtomicInteger.class)
                    .of(ReflectionTests.class)
                    .inner("private static final class A<T>")
                    .inner("private static final class B")
                    .inner("private static final class C")
                    .field("public final AtomicInteger atomicField;")
                    .getter().reflect();
            log("inner inner inner field: " + innerinnerinnerField);

            XReflection.namespaced()
                    .of(ReflectionTests.class)
                    .inner("public interface GameProfile")
                    .method("void field_setter_test(String test);")
                    .reflect();

            Object enumConstant = XReflection.namespaced()
                    .of(ReflectionTests.class)
                    .inner("public enum EnumTest")
                    .enums().named("A")
                    .getEnumConstant();

            assertSame(EnumTest.A, enumConstant);

            assertDoesNotThrow(() -> {
                // TODO we need to somehow add support for "ReflectionTests.EnumTest" type in strings.
                XReflection.of(ReflectionTests.class)
                        .method("public static EnumTest privateLookupIn(Class<?> targetClass, EnumTest caller) throws IllegalAccessException")
                        .reflect();
            }, "Couldn't parse inner-classed throwable method of privateLookupIn");

            assertDoesNotThrow(() -> {
                new ReflectionParser("private String[] split(char ch, int limit, boolean withDelimiters);")
                        .parseMethod(clazz.method()).unreflect().invoke(new ReflectionTests(), ',', 2, true);
            }, "Method ReflectionParser and invokation failed");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("ReflectionParser test failed", e);
        }
    }
}
