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

package com.cryptomorin.xseries.test.benchmark.reflection;

import com.cryptomorin.xseries.reflection.ReflectiveNamespace;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.proxy.ReflectiveProxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Optional;

public final class ReflectionBenchmarkCommons {
    public static Method rawJava() throws ReflectiveOperationException {
        Class<?> clazz = Class.forName("com.cryptomorin.xseries.test.benchmark.reflection.ReflectionBenchmarkTargetMethod");
        return clazz.getDeclaredMethod("hello", String.class, int.class, boolean.class);
    }

    public static MethodHandle methodHandles() throws ReflectiveOperationException {
        Class<?> clazz = Class.forName("com.cryptomorin.xseries.test.benchmark.reflection.ReflectionBenchmarkTargetMethod");
        return MethodHandles.lookup().findVirtual(clazz, "hello", MethodType.methodType(Optional.class, String.class, int.class, boolean.class));
    }

    public static MethodHandle rawJavaToMethodHandle() throws ReflectiveOperationException {
        return MethodHandles.lookup().unreflect(rawJava());
    }

    public static Method XReflection_I_Functional() throws ReflectiveOperationException {
        return XReflection.classHandle()
                .inPackage("com.cryptomorin.xseries.test.benchmark.reflection")
                .named("ReflectionBenchmarkTargetMethod")
                .method()
                .named("hello").returns(Optional.class).parameters(String.class, int.class, boolean.class).reflectJvm();
    }

    public static Method XReflection_II_StringAPI() throws ReflectiveOperationException {
        ReflectiveNamespace ns = XReflection.namespaced();

        return ns.classHandle("package com.cryptomorin.xseries.test.benchmark.reflection; public final class ReflectionBenchmarkTargetMethod {}")
                .method("public Optional<String> hello(String firstArg, int secondArg, boolean thirdArg);")
                .reflectJvm();
    }

    public static ReflectiveProxy<ReflectionBenchmarkTargetMethodProxy> XReflection_III_Proxy() {
        return XReflection.proxify(ReflectionBenchmarkTargetMethodProxy.class);
    }

    public static Void XReflection_Stage_IV() {
        return null;
    }
}
