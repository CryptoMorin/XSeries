/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2026 Crypto Morin
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

package com.cryptomorin.xseries.test.util;

import java.util.concurrent.CompletableFuture;

public final class XLogger {
    public static void log(String message) {
        // We should probably use an actual logger?
        System.out.println("[XSeries] " + message);
    }

    public static CompletableFuture<Void> logTimingsAsync(String prefix, Runnable runnable) {
        return CompletableFuture.runAsync(() -> logTimings(prefix, runnable));
    }

    public static void logTimings(String prefix, Runnable runnable) {
        long before = System.currentTimeMillis();
        runnable.run();
        long after = System.currentTimeMillis();
        long diff = after - before;
        log('[' + prefix + "] Took " + diff + "ms");
    }
}
