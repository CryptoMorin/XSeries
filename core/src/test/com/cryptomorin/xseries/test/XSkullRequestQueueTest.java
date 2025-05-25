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

package com.cryptomorin.xseries.test;

import com.cryptomorin.xseries.profiles.lock.KeyedLock;
import com.cryptomorin.xseries.profiles.lock.KeyedLockMap;
import com.cryptomorin.xseries.profiles.lock.MojangRequestQueue;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.test.util.XLogger;
import org.junit.jupiter.api.Assertions;

import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

final class XSkullRequestQueueTest {
    private static final MethodHandle KeyedLock_pendingTasks = XReflection.of(KeyedLock.class)
            .field("protected int pendingTasks").getter().unreflect();

    @SuppressWarnings("rawtypes")
    private static final MethodHandle KeyedLockMap_locks = XReflection.of(KeyedLockMap.class)
            .field("private final Map locks").getter().unreflect();

    private static final Map<String, UUID> USERNAME_TO_UUID = new HashMap<>();
    private static final Map<String, AtomicInteger> NOT_CACHED_TIMES = Collections.synchronizedMap(new HashMap<>());
    private static final Set<KeyedLock<?>> USED_LOCKS = new HashSet<>();
    private static final boolean LOG = false;

    public static void test() {
        RequestThread a = thread("A", "notch");
        RequestThread b = thread("B", "notch");
        RequestThread c = thread("C", "notch");

        RequestThread a2 = thread("A2", "jack");
        RequestThread b2 = thread("B2", "jack");
        RequestThread c2 = thread("C2", "jack");
        RequestThread d2 = thread("D2", "jack");

        List<RequestThread> threads = Arrays.asList(a, b, c, a2, b2, c2, d2);
        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // It should be 2 (one for each) but it's possible for the first
        // thread to somehow quickly finish its job and exit or for other
        // threads to start with a delay because of current hardware pressure.
        XLogger.log("[XSkull Thread Queue] Used locks: " + USED_LOCKS);

        Map<String, KeyedLock<String>> locks = getLocks(MojangRequestQueue.USERNAME_REQUESTS);
        Assertions.assertTrue(locks.isEmpty(), () -> "Username requests not empty: " + locks);
        for (Map.Entry<String, AtomicInteger> cache : NOT_CACHED_TIMES.entrySet()) {
            int times = cache.getValue().get();
            Assertions.assertEquals(1, times, () -> "Cached more than once: " + cache.getKey() + " -> " + times);
        }
    }

    private static String blackhole(int i) {
        return Integer.toString(i);
    }

    @SuppressWarnings("unchecked")
    private static <K> Map<K, KeyedLock<K>> getLocks(KeyedLockMap<K> lockMap) {
        try {
            return (Map<K, KeyedLock<K>>) KeyedLockMap_locks.invokeExact(lockMap);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static int getPendingTasks(KeyedLock<?> lock) {
        try {
            return (int) KeyedLock_pendingTasks.invokeExact(lock);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static final class RequestThread extends Thread {
        private final String username;

        public RequestThread(String name, String username) {
            super("XSkull Thread Queue / " + name + " / " + username);
            this.username = username;
        }

        void log(String msg) {
            if (!LOG) return;
            XLogger.log('[' + getName() + "] " + msg);
        }

        @Override
        public void run() {
            log("Started");
            try (KeyedLock<String> lock = MojangRequestQueue.USERNAME_REQUESTS.lock(username)) {
                log("Lock acquired " + getPendingTasks(lock));
                lock.lock();
                USED_LOCKS.add(lock);
                log("Synchronized");
                UUID id = USERNAME_TO_UUID.get(username);
                if (id == null) {
                    log("Not cached");
                    NOT_CACHED_TIMES.compute(username, (k, v) -> {
                        if (v == null) v = new AtomicInteger();
                        v.incrementAndGet();
                        return v;
                    });
                    for (int i = 0; i < 1_000_000; i++) {
                        blackhole(i ^ 3);
                    }
                    id = UUID.randomUUID();
                    USERNAME_TO_UUID.put(username, id);
                } else {
                    log("Already cached: " + id);
                }
                log("Lock released " + getPendingTasks(lock));
            }
            log("Lock closed");
        }
    }

    private static RequestThread thread(String threadName, String username) {
        return new RequestThread(threadName, username);
    }
}
