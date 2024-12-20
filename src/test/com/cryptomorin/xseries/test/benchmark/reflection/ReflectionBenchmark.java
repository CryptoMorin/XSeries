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

import com.cryptomorin.xseries.reflection.proxy.ReflectiveProxy;
import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark                 Mode  Cnt    Score         Error  Units
 * XReflection_II_StringAPI  avgt    4    11.832 ±     0.226  us/op
 * XReflection_III_Proxy     avgt    4     5.549 ±     0.286  us/op
 * XReflection_I_Functional  avgt    4     2.513 ±     0.296  us/op
 * methodHandles             avgt    4     2.761 ±     0.108  us/op
 * rawJavaToMethodHandle     avgt    4     1.713 ±     0.094  us/op
 * rawJava                   avgt    4     1.142 ±     0.029  us/op
 */
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 4)
@Measurement(iterations = 4)
@Threads(3)
public class ReflectionBenchmark {
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
    public Method XReflection_I_Functional() throws ReflectiveOperationException {
        return ReflectionBenchmarkCommons.XReflection_I_Functional();
    }

    @Benchmark
    public Method XReflection_II_StringAPI() throws ReflectiveOperationException {
        return ReflectionBenchmarkCommons.XReflection_II_StringAPI();
    }

    @Benchmark
    public ReflectiveProxy<ReflectionBenchmarkTargetMethodProxy> XReflection_III_Proxy() {
        return ReflectionBenchmarkCommons.XReflection_III_Proxy();
    }
}
