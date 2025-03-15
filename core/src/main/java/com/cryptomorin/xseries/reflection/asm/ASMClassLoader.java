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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * TODO How does {@link java.lang.reflect.Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)} define
 *      the generated class into another class loader? I'm assuming it has special privilege that can bypass checks.
 *
 * @since 14.0.0
 */
@SuppressWarnings("unused")
final class ASMClassLoader extends ClassLoader {
    private static final String DEFINE_CLASS = "defineClass";

    protected ASMClassLoader() {}

    protected Class<?> defineClass(String name, byte[] bytes) {
        return super.defineClass(asmTypeToBinary(name), bytes, 0, bytes.length);
    }

    @SuppressWarnings("JavaReflectionInvocation")
    private static Class<?> defineClassLombockTransplanter(String className, byte[] bytecode, ClassLoader classLoader) {
        // https://github.com/projectlombok/lombok/blob/master/src/eclipseAgent/lombok/eclipse/agent/EclipseLoaderPatcherTransplants.java
        try {
            /* Since Java 16 reflective access to ClassLoader.defineClass is no longer permitted. The recommended solution
             * is to use MethodHandles.lookup().defineClass which is useless here because it is limited to classes in the
             * same package. Fortunately this code gets transplanted into a ClassLoader and we can call the parent method
             * using a MethodHandle. To support old Java versions we use a reflective version of the code snippet below.
             *
             * Lookup lookup = MethodHandles.lookup();
             * MethodType type = MethodType.methodType(Class.class, new Class[] {String.class, byte[].class, int.class, int.class});
             * MethodHandle method = lookup.findVirtual(original.getClass(), DEFINE_CLASS, type);
             * shadowClassLoaderClass = (Class) method.invokeWithArguments(original, "lombok.launch.ShadowClassLoader", bytes, new Integer(0), new Integer(len)})
             */
            Class<?> methodHandles = Class.forName("java.lang.invoke.MethodHandles");
            Class<?> methodHandle = Class.forName("java.lang.invoke.MethodHandle");
            Class<?> methodType = Class.forName("java.lang.invoke.MethodType");
            Class<?> methodHandlesLookup = Class.forName("java.lang.invoke.MethodHandles$Lookup");
            Method lookupMethod = methodHandles.getDeclaredMethod("lookup");
            Method methodTypeMethod = methodType.getDeclaredMethod("methodType", Class.class, Class[].class);
            Method findVirtualMethod = methodHandlesLookup.getDeclaredMethod("findVirtual", Class.class, String.class, methodType);
            Method invokeMethod = methodHandle.getDeclaredMethod("invokeWithArguments", Object[].class);

            Object lookup = lookupMethod.invoke(null);
            Object type = methodTypeMethod.invoke(null, Class.class, new Class[]{String.class, byte[].class, int.class, int.class});

            // Caused by: java.lang.IllegalAccessException: symbolic reference class is not accessible: class jdk.internal.loader.ClassLoaders$AppClassLoader, from class com.cryptomorin.xseries.reflection.asm.XReflectASM (unnamed module @51b279c9)
            // 	at java.base/java.lang.invoke.MemberName.makeAccessException(MemberName.java:894)
            // 	at java.base/java.lang.invoke.MethodHandles$Lookup.checkSymbolicClass(MethodHandles.java:3787)
            // 	at java.base/java.lang.invoke.MethodHandles$Lookup.resolveOrFail(MethodHandles.java:3747)
            // 	at java.base/java.lang.invoke.MethodHandles$Lookup.findVirtual(MethodHandles.java:2767)
            // 	... 15 more
            Object method = findVirtualMethod.invoke(lookup, classLoader.getClass(), DEFINE_CLASS, type);
            return (Class<?>) invokeMethod.invoke(method, new Object[]
                    {new Object[]{classLoader, className, bytecode, 0, bytecode.length}});
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            // Ignore, old Java
            throw new IllegalStateException(e);
        }
    }

    private static Class<?> defineClassJLA(String generatedClassName, byte[] bytecode) {
        try {
            // DelegatingClassLoader
            //   static Class<?> defineClass(String name, byte[] bytes, int off, int len, ClassLoader parentClassLoader)
            Class<?> ClassDefiner = Class.forName("jdk.internal.reflect.ClassDefiner");
            Method meth = ClassDefiner.getDeclaredMethod(DEFINE_CLASS, String.class, byte[].class, int.class, int.class, ClassLoader.class);
            return (Class<?>) meth.invoke(null, generatedClassName, bytecode, 0, bytecode.length, XReflectASM.class.getClassLoader());
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String asmTypeToBinary(String dirPath) {
        return dirPath.replace('/', '.');
    }

    private static Class<?> methodHandleLoadClass(byte[] bytecode) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            // Java 9+
            MethodHandle defineClass = lookup.findVirtual(MethodHandles.Lookup.class, DEFINE_CLASS,
                    MethodType.methodType(Class.class, byte[].class));

            return (Class<?>) defineClass.invoke(lookup, bytecode);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings({"UnnecessaryBoxing", "CachedNumberConstructorCall"})
    private static Class<?> loadClass(String className, byte[] bytecode) {
        // Override defineClass (as it is protected) and define the class.
        Class<?> clazz;
        try {
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            Class<?> cls = Class.forName("java.lang.ClassLoader");
            java.lang.reflect.Method method =
                    cls.getDeclaredMethod(
                            DEFINE_CLASS,
                            String.class, byte[].class, int.class, int.class);

            // Protected method invocation.
            method.setAccessible(true);
            try {
                Object[] args = {className, bytecode, Integer.valueOf(0), Integer.valueOf(bytecode.length)};
                clazz = (Class<?>) method.invoke(loader, args);
            } finally {
                method.setAccessible(false);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load class " + className + " into the system class loader", e);
        }
        return clazz;
    }
}
