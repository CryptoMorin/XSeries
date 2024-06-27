package com.github.cryptomorin.test;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.classes.DynamicClassHandle;
import com.cryptomorin.xseries.reflection.parser.ReflectionParser;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@SuppressWarnings("All")
public final class ReflectionTests {
    private final String test = "A";

    private static final class A {
        private static final class B {
            private static final class C {
                public final AtomicInteger atomicField = new AtomicInteger();
            }
        }
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

    public static void parser() {
        GameProfile profiler = (GameProfile) Proxy.newProxyInstance(ReflectionParser.class.getClassLoader(), new Class[]{GameProfile.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("Invoked from proxy: " + proxy.getClass() + " as " + method + " with args " + Arrays.toString(args));
                return "hey";
            }
        });

        System.out.println("Before replace: " + profiler.field_getter_test() + profiler.test);
        profiler.field_setter_test("B");
        System.out.println("After replace: " + profiler.field_getter_test());

        Arrays.stream(ReflectionParser.class.getDeclaredFields())
                .filter(x -> x.getType() == Pattern.class)
                .filter(x -> Modifier.isStatic(x.getModifiers()))
                .forEach(x -> {
                    try {
                        x.setAccessible(true);
                        System.out.println("field is " + x.getName() + " -> " + x.get(null));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
        DynamicClassHandle clazz = new ReflectionParser("package com.github.cryptomorin.test; public final class ReflectionTests {}")
                .parseClass(XReflection.classHandle());
        try {
            XReflection.of(ReflectionTests.class).constructor("public ReflectionTests(String test, int other);").reflect();
            XReflection.of(ReflectionTests.class).field("private final String test;").getter().reflect();
            XReflection.of(ReflectionTests.class).field("private final String test;").getter().reflect();

            // Inner class test
            MethodHandle innerinnerinnerField = XReflection.namespaced().imports(AtomicInteger.class)
                    .of(ReflectionTests.class)
                    .inner("private static final class A {}")
                    .inner("private static final class B {}")
                    .inner("private static final class C {}")
                    .field("public final AtomicInteger atomicField;")
                    .getter().reflect();
            System.err.println("inner inner inner field: " + innerinnerinnerField);

            XReflection.namespaced()
                    .of(ReflectionTests.class)
                    .inner("public interface GameProfile {}")
                    .method("void field_setter_test(String test);")
                    .reflect();

            Object enumConstant = XReflection.namespaced()
                    .of(ReflectionTests.class)
                    .inner("public enum EnumTest {}")
                    .enums().named("A")
                    .getEnumConstant();

            System.out.println("Enum found: " + enumConstant);

            Object res = new ReflectionParser("private String[] split(char ch, int limit, boolean withDelimiters);")
                    .parseMethod(clazz.method()).unreflect().invoke(new ReflectionTests(), ',', 2, true);
            System.err.println("------------------------------------------------ " + res);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
