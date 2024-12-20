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

import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark                 Mode  Cnt   Score          Error  Units
 * XReflection_III_Proxy     avgt    3   281.869 ±     9.477  ns/op
 * XReflection_II_StringAPI  avgt    3    76.855 ±     4.507  ns/op
 * XReflection_I_Functional  avgt    3    77.562 ±     0.480  ns/op
 * methodHandles             avgt    3    74.166 ±     5.673  ns/op
 * rawJava                   avgt    3    76.460 ±     2.280  ns/op
 * rawJavaToMethodHandle     avgt    3    74.778 ±     6.053  ns/op
 */
@SuppressWarnings({"unchecked", "FieldMayBeStatic"})
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@Threads(1)
public class ReflectionBenchmarkExecution {
    private static final ReflectionBenchmarkTargetMethod INSTANCE;
    private static final Method RAW_JAVA;
    private static final MethodHandle METHOD_HANDLE;
    private static final MethodHandle RAW_JAVA_TO_METHOD_HANDLE;
    private static final Method XREFLECTION_I;
    private static final Method XREFLECTION_II;
    private static final ReflectionBenchmarkTargetMethodProxy XREFLECTION_III;

    private final String firstArg = "test test";
    private final int secArg = 4545;
    private final boolean thirdArg = true;

    static {
        ReflectionBenchmarkTargetMethod instace;
        Method rawJava;
        MethodHandle methodHandles;
        MethodHandle rawJavaToMethodHandle;
        Method XReflection_I_Functional;
        Method XReflection_II_StringAPI;
        ReflectionBenchmarkTargetMethodProxy XReflection_III_Proxy;

        try {
            instace = new ReflectionBenchmarkTargetMethod();
            rawJava = ReflectionBenchmarkCommons.rawJava();
            methodHandles = ReflectionBenchmarkCommons.methodHandles();
            rawJavaToMethodHandle = ReflectionBenchmarkCommons.rawJavaToMethodHandle();
            XReflection_I_Functional = ReflectionBenchmarkCommons.XReflection_I_Functional();
            XReflection_II_StringAPI = ReflectionBenchmarkCommons.XReflection_II_StringAPI();
            XReflection_III_Proxy = ReflectionBenchmarkCommons.XReflection_III_Proxy().bindTo(instace);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to init", ex);
        }

        INSTANCE = instace;
        RAW_JAVA = rawJava;
        METHOD_HANDLE = methodHandles;
        RAW_JAVA_TO_METHOD_HANDLE = rawJavaToMethodHandle;
        XREFLECTION_I = XReflection_I_Functional;
        XREFLECTION_II = XReflection_II_StringAPI;
        XREFLECTION_III = XReflection_III_Proxy;
    }

    @Benchmark
    public Optional<String> rawJava() throws Throwable {
        return (Optional<String>) RAW_JAVA.invoke(INSTANCE, firstArg, secArg, thirdArg);
    }

    @Benchmark
    public Optional<String> methodHandles() throws Throwable {
        return (Optional<String>) METHOD_HANDLE.invoke(INSTANCE, firstArg, secArg, thirdArg);
    }

    @Benchmark
    public Optional<String> rawJavaToMethodHandle() throws Throwable {
        return (Optional<String>) RAW_JAVA_TO_METHOD_HANDLE.invoke(INSTANCE, firstArg, secArg, thirdArg);
    }

    @Benchmark
    public Optional<String> XReflection_I_Functional() throws Throwable {
        return (Optional<String>) XREFLECTION_I.invoke(INSTANCE, firstArg, secArg, thirdArg);
    }

    @Benchmark
    public Optional<String> XReflection_II_StringAPI() throws Throwable {
        return (Optional<String>) XREFLECTION_II.invoke(INSTANCE, firstArg, secArg, thirdArg);
    }

    @Benchmark
    public Optional<String> XReflection_III_Proxy() throws Throwable {
        return XREFLECTION_III.hello(firstArg, secArg, thirdArg);
    }
}
