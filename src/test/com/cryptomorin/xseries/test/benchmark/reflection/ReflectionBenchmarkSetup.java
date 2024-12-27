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

import com.cryptomorin.xseries.reflection.asm.XReflectASM;
import com.cryptomorin.xseries.reflection.proxy.ReflectiveProxy;
import com.cryptomorin.xseries.test.Constants;
import org.openjdk.jmh.annotations.*;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark                         Mode  Cnt     Score    Error  Units
 * XReflectIV.XReflection_IV_Proxy   avgt   10  1781.023 ± 70.874  us/op (With verifier + write to local file)
 * callSite                          avgt   15   305.147 ± 41.640  us/op
 * XReflectIII.XReflection_IV_Proxy  avgt   10    24.412 ±  0.845  us/op
 * XReflection_II_StringAPI          avgt   15    15.760 ±  4.344  us/op
 * XReflection_I_Functional          avgt   15     4.522 ±  0.511  us/op
 * methodHandles                     avgt   15     3.731 ±  0.160  us/op
 * rawJavaToMethodHandle             avgt   15     2.552 ±  0.195  us/op
 * rawJava                           avgt   15     1.692 ±  0.151  us/op
 * <p>
 * JMH version: 1.37
 * VM version: JDK 21.0.3 (blackhole mode + no args)
 * <p>
 * These result seem to make complete sense. XReflection IV and callsite take the top spot because they both
 * use ASM-generated codes. Do note however that when this benchmark was generated, XReflection IV had the
 * ASM verifier running as well and the resulted bytecode was also printed to a separate class for analysis,
 * meaning that the expected generation phase is expected to be much lower than what is shown.
 * Next is the proxy class, which also generates a class, but it only delegates everything
 * to the {@link java.lang.reflect.InvocationHandler#invoke(Object, Method, Object[])} method, but the fact that it's
 * much faster than lambda's callsite is a bit confusing. Next is the XReflection II String API which is expected as well.
 * Since XReflection I is a wrapper for Java's reflection API, it's of course expected to take more time. Finally,
 * MethodHandles are a bit slower than raw java since they have to generate polymorphic signatures, however it
 * makes their execution much faster.
 *
 * @see ReflectionBenchmarkCommons
 * @see ReflectionBenchmarkExecution
 * @see ReflectionBenchmarkTargetMethodProxy
 */
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Threads(3)
@Fork(3)
public class ReflectionBenchmarkSetup {
    @Setup(Level.Trial)
    public void setup() {
        Constants.disableXReflectionMinecraft();
    }

    @Benchmark
    public Method rawJava() throws ReflectiveOperationException {
        return ReflectionBenchmarkCommons.rawJava();
    }

    @Benchmark
    public MethodHandle methodHandles() throws ReflectiveOperationException {
        return ReflectionBenchmarkCommons.methodHandles();
    }

    @Benchmark
    public MethodHandle rawJavaToMethodHandle() throws ReflectiveOperationException {
        return ReflectionBenchmarkCommons.rawJavaToMethodHandle();
    }

    @Benchmark
    public CallSite callSite() throws ReflectiveOperationException, LambdaConversionException {
        return ReflectionBenchmarkCommons.callSite();
    }

    @Benchmark
    public Method XReflection_I_Functional() throws ReflectiveOperationException {
        return ReflectionBenchmarkCommons.XReflection_I_Functional();
    }

    @Benchmark
    public Method XReflection_II_StringAPI() throws ReflectiveOperationException {
        return ReflectionBenchmarkCommons.XReflection_II_StringAPI();
    }

    @State(Scope.Benchmark)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Warmup(iterations = 2)
    @Measurement(iterations = 4)
    @Threads(1) // Because we use Invocation setup method
    @Fork(2)
    public static class XReflectIII {
        private Map<Class<?>, ReflectiveProxy<?>> proxyMap;

        @SuppressWarnings("unchecked")
        @Setup(Level.Trial)
        public void setupBenchmark() {
            Constants.disableXReflectionMinecraft();
            try {
                Field field = ReflectiveProxy.class.getDeclaredField("PROXIFIED_CLASS_LOADER0");
                field.setAccessible(true);
                proxyMap = (Map<Class<?>, ReflectiveProxy<?>>) field.get(null);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        @Setup(Level.Invocation)
        public void setup() {
            proxyMap.clear();
        }

        @Benchmark
        public ReflectionBenchmarkTargetMethodProxy XReflection_IV_Proxy() {
            return ReflectionBenchmarkCommons.XReflection_III_Proxy();
        }
    }

    @State(Scope.Benchmark)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Warmup(iterations = 1)
    @Measurement(iterations = 3)
    @Threads(1) // Because we use Invocation setup method
    @Fork(2)
    public static class XReflectIV {
        private MethodHandle classLoader, ctor;
        private Map<Class<?>, XReflectASM<?>> processed;

        @SuppressWarnings("unchecked")
        @Setup(Level.Trial)
        public void setupBenchmark() throws Exception {
            Constants.disableXReflectionMinecraft();

            Field field = XReflectASM.class.getDeclaredField("PROCESSED");
            field.setAccessible(true);
            processed = (Map<Class<?>, XReflectASM<?>>) field.get(null);

            // TODO make XReflectASM accept custom class loaders.
            Field classLoaderField = XReflectASM.class.getDeclaredField("CLASS_LOADER");
            classLoaderField.setAccessible(true);
            classLoader = MethodHandles.lookup().unreflectSetter(classLoaderField);

            Class<?> clazz = Class.forName("com.cryptomorin.xseries.reflection.asm.ASMClassLoader");
            Constructor<?> classLoaderCtor = clazz.getDeclaredConstructor();
            classLoaderCtor.setAccessible(true);
            ctor = MethodHandles.lookup().unreflectConstructor(classLoaderCtor);
        }

        @Setup(Level.Invocation)
        public void setup() throws Throwable {
            // Note: make sure to make CLASS_LOADER non-final when running this test.
            classLoader.invokeExact(ctor.invokeExact());
            processed.clear();
        }

        @Benchmark
        public ReflectionBenchmarkTargetMethodProxy XReflection_IV_Proxy() {
            return ReflectionBenchmarkCommons.XReflection_IV_Proxy();
        }
    }
}
