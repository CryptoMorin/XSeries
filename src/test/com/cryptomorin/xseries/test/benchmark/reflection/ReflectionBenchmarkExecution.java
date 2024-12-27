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

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.test.Constants;
import org.openjdk.jmh.annotations.*;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark                      Mode  Cnt     Score    Error  Units
 * XReflection_III_Proxy_Unbound  avgt   15  1729.622 ± 61.606  ns/op
 * XReflection_III_Proxy_Bound    avgt   15   275.213 ±  1.201  ns/op
 * XReflection_IV_Private         avgt   15    75.363 ±  2.344  ns/op
 * XReflection_I_Functional       avgt   15    74.245 ±  2.866  ns/op
 * callSite                       avgt   15    73.429 ±  0.767  ns/op
 * MethodHandle_invokeExact       avgt   15    72.900 ±  1.408  ns/op
 * MethodHandle_invoke            avgt   15    72.805 ±  0.861  ns/op
 * rawReflection                  avgt   15    72.917 ±  2.892  ns/op
 * rawJavaToMethodHandle          avgt   15    72.810 ±  1.087  ns/op
 * XReflection_II_StringAPI       avgt   15    72.483 ±  1.787  ns/op
 * preBoundcallSite               avgt   15    72.099 ±  3.508  ns/op
 * direct                         avgt   15    72.081 ±  1.826  ns/op
 * XReflection_IV_Public          avgt   15    71.431 ±  0.830  ns/op
 * <p>
 * Total time: 00:52:32
 * JDK 21 - no args
 * JMH 1.37
 * Blackhole mode: compiler (auto-detected, use -Djmh.blackhole.autoDetect=false to disable)
 * Warmup: 3 iterations, 10 s each
 * Measurement: 5 iterations, 10 s each
 * Timeout: 10 min per iteration
 * Threads: 1 thread, will synchronize iterations
 * <p>
 * These results are obviously not that different and in the parts where one method is expected to
 * be faster than another, we assume it's because of a benchmarking error. The main point of this
 * benchmark was to prove that XReflection adds no overhead, except for the proxy method
 * {@link XReflection#proxify(Class)} which is expected and is fixed when using ASM-generated
 * classes.
 * Also do not that we technically should test different method signatures and field/constructor accesses too,
 * but writing these tests take some time, if anyone's willing to do it, sure.
 *
 * @see ReflectionBenchmarkSetup
 * @see ReflectionBenchmarkCommons
 * @see ReflectionBenchmarkTargetMethodProxy
 */
@SuppressWarnings({"unchecked", "FieldMayBeStatic"})
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Threads(3)
@Fork(value = 3, warmups = 0)
public class ReflectionBenchmarkExecution {
    private static final ReflectionBenchmarkTargetMethod INSTANCE;
    private static final Method RAW_JAVA;
    private static final MethodHandle METHOD_HANDLE;
    private static final MethodHandle RAW_JAVA_TO_METHOD_HANDLE;
    private static final Method XREFLECTION_I;
    private static final Method XREFLECTION_II;
    private static final ReflectionBenchmarkTargetMethodProxy XREFLECTION_III_BOUND;
    private static final ReflectionBenchmarkTargetMethodProxy XREFLECTION_IV;
    private static final ReflectionBenchmarkTargetMethodProxy XREFLECTION_III_UNBOUND;
    private static final CallSite CALL_SITE;
    private static final ReflectionBenchmarkTargetMethodProxy PRE_BOUND_CALL_SITE;

    private final String firstArg = "test test";
    private final int secArg = 4545;
    private final boolean thirdArg = true;

    static {
        Constants.disableXReflectionMinecraft();

        ReflectionBenchmarkTargetMethod instace;
        Method rawJava;
        MethodHandle methodHandles;
        MethodHandle rawJavaToMethodHandle;
        Method XReflection_I_Functional;
        Method XReflection_II_StringAPI;
        ReflectionBenchmarkTargetMethodProxy XReflection_III_Proxy_Unbound;
        ReflectionBenchmarkTargetMethodProxy XReflection_III_Proxy_Bound;
        ReflectionBenchmarkTargetMethodProxy XReflection_IV;
        CallSite callSite;
        ReflectionBenchmarkTargetMethodProxy preBoundCallSite;

        try {
            instace = new ReflectionBenchmarkTargetMethod();
            rawJava = ReflectionBenchmarkCommons.rawJava();
            methodHandles = ReflectionBenchmarkCommons.methodHandles();
            rawJavaToMethodHandle = ReflectionBenchmarkCommons.rawJavaToMethodHandle();
            XReflection_I_Functional = ReflectionBenchmarkCommons.XReflection_I_Functional();
            XReflection_II_StringAPI = ReflectionBenchmarkCommons.XReflection_II_StringAPI();
            XReflection_III_Proxy_Unbound = ReflectionBenchmarkCommons.XReflection_III_Proxy();
            XReflection_III_Proxy_Bound = ReflectionBenchmarkCommons.XReflection_III_Proxy().bindTo(instace);
            XReflection_IV = ReflectionBenchmarkCommons.XReflection_IV_Proxy();
            callSite = ReflectionBenchmarkCommons.callSite();
            preBoundCallSite = (ReflectionBenchmarkTargetMethodProxy) ReflectionBenchmarkCommons.callSite().dynamicInvoker().invoke(instace);
        } catch (Throwable ex) {
            throw new IllegalStateException("Failed to init", ex);
        }

        INSTANCE = instace;
        RAW_JAVA = rawJava;
        METHOD_HANDLE = methodHandles;
        RAW_JAVA_TO_METHOD_HANDLE = rawJavaToMethodHandle;
        XREFLECTION_I = XReflection_I_Functional;
        XREFLECTION_II = XReflection_II_StringAPI;
        XREFLECTION_III_UNBOUND = XReflection_III_Proxy_Unbound;
        XREFLECTION_III_BOUND = XReflection_III_Proxy_Bound;
        XREFLECTION_IV = XReflection_IV.bindTo(instace);
        CALL_SITE = callSite;
        PRE_BOUND_CALL_SITE = preBoundCallSite;
    }

    @Setup(Level.Trial)
    public void setup() {
        Constants.disableXReflectionMinecraft();
    }

    @Benchmark
    public Optional<String> direct() {
        return INSTANCE.hello(firstArg, secArg, thirdArg);
    }

    @Benchmark
    public Optional<String> rawReflection() throws Throwable {
        return (Optional<String>) RAW_JAVA.invoke(INSTANCE, firstArg, secArg, thirdArg);
    }

    @Benchmark
    public Optional<String> MethodHandle_invoke() throws Throwable {
        return (Optional<String>) METHOD_HANDLE.invoke(INSTANCE, firstArg, secArg, thirdArg);
    }

    @Benchmark
    public Optional<String> MethodHandle_invokeExact() throws Throwable {
        return (Optional<String>) METHOD_HANDLE.invokeExact(INSTANCE, firstArg, secArg, thirdArg);
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
    public Optional<String> XReflection_III_Proxy_Unbound() {
        return XREFLECTION_III_UNBOUND.bindTo(INSTANCE).hello(firstArg, secArg, thirdArg);
    }

    @Benchmark
    public Optional<String> XReflection_III_Proxy_Bound() {
        return XREFLECTION_III_BOUND.hello(firstArg, secArg, thirdArg);
    }

    @Benchmark
    public Optional<String> XReflection_IV_Public() {
        return XREFLECTION_IV.hello(firstArg, secArg, thirdArg);
    }

    @Benchmark
    public Optional<String> XReflection_IV_Private() {
        return XREFLECTION_IV.helloPrivate(firstArg, secArg, thirdArg);
    }

    @Benchmark
    public Optional<String> callSite() throws Throwable {
        ReflectionBenchmarkTargetMethodProxy gen = (ReflectionBenchmarkTargetMethodProxy) CALL_SITE.dynamicInvoker().invoke(INSTANCE);
        return gen.hello(firstArg, secArg, thirdArg);
    }

    @Benchmark
    public Optional<String> preBoundcallSite() throws Throwable {
        return PRE_BOUND_CALL_SITE.hello(firstArg, secArg, thirdArg);
    }
}
