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

package com.cryptomorin.xseries.test.benchmark;

import com.cryptomorin.xseries.test.Constants;
import com.cryptomorin.xseries.test.util.XLogger;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkMain extends Thread implements Thread.UncaughtExceptionHandler {
    public static void main(String[] args) {
        // noinspection CallToThreadRun
        new BenchmarkMain().run();
    }

    public BenchmarkMain() {
        super("JMH Benchmark Thread");
        setUncaughtExceptionHandler(this);
    }

    @Override
    public void run() {
        // Note it appears that the configured settings here cannot be
        // overridden by JMH annotations.
        Options opt = new OptionsBuilder()
                .include(RegistryBenchmark.class.getSimpleName())
                // .include(ReflectionBenchmarkSetup.class.getSimpleName())
                // .include(ReflectionBenchmarkExecution.class.getSimpleName())
                // .include(GameProfileBenchmark.class.getSimpleName())
                // .forks(0) // Terrible, but we need the server state...
                .shouldFailOnError(true)
                .mode(Mode.AverageTime)
                .build();

        try {
            new Runner(opt).run();
        } catch (RunnerException e) {
            throw new RuntimeException(e);
        } finally {
            XLogger.log("JMH Benchmarking is done, unlocking...");
            synchronized (Constants.LOCK) {
                Constants.LOCK.notifyAll();
            }
            XLogger.log("JMH All done, exiting JMH mode...");
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        XLogger.log("JMH Benchmark thread threw an error: ");
        e.printStackTrace();
    }
}