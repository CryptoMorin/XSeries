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

package com.cryptomorin.xseries.test;

import com.cryptomorin.xseries.profiles.lock.KeyedLock;
import com.cryptomorin.xseries.profiles.lock.KeyedLockMap;
import com.cryptomorin.xseries.profiles.lock.MojangRequestQueue;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.test.util.XLogger;
import org.junit.jupiter.api.Assertions;

import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class XSkullRequestQueueTest {
    private static final MethodHandle KeyedLock_pendingTasks = XReflection.classHandle().inPackage("com.cryptomorin.xseries.profiles.lock").named("NulledKeyedLock")
            .field("protected int pendingTasks").getter().unreflect();

    private static final MethodHandle ReferentialKeyedLock_lock = XReflection.classHandle().inPackage("com.cryptomorin.xseries.profiles.lock").named("ReferentialKeyedLock")
            .field("protected NulledKeyedLock lock").getter().unreflect();

    @SuppressWarnings("rawtypes")
    private static final MethodHandle KeyedLockMap_locks = XReflection.of(KeyedLockMap.class)
            .field("private final Map locks").getter().unreflect();

    private static final MethodHandle Thread_onSpinWait = XReflection.of(Thread.class)
            .method("public static void onSpinWait()").reflectOrNull();

    /**
     * Not using {@link java.util.concurrent.ConcurrentHashMap} or {@link Collections#synchronizedMap(Map)}
     * for this map (using a raw {@link HashMap}) can reveal some issues (See {@code Cached more than once} assertion
     * below for more information)
     */
    private final Map<String, UUID> USERNAME_TO_UUID = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, AtomicInteger> NOT_CACHED_TIMES = Collections.synchronizedMap(new HashMap<>());
    private final Set<KeyedLock<?, ?>> USED_LOCKS = Collections.newSetFromMap(new LinkedHashMap<>());
    private final List<Throwable> threadExceptions = new CopyOnWriteArrayList<>();
    private static final boolean LOG = false;

    /**
     * Higher number of iterations may reveal some concurrency issues easier.
     * Note that when using higher iteration numbers, you should remove the
     * {@link #realTest()} because it sends many requests to Mojang and you will
     * get ratelimited easily.
     */
    private static final int TEST_ITERATIONS = 50;

    public static void createTests() {
        if (Thread_onSpinWait != null) {
            XLogger.log("Using the supported Thread.onSpinWait() for busy skull waiting");
        }

        for (int i = 0; i < TEST_ITERATIONS; i++) {
            if (LOG) {
                XLogger.log("************************** XSkullRequestQueueTest Iteration [" + i + "] **************************");
            }
            new XSkullRequestQueueTest().test();
        }
    }

    private List<RequestThread> threads(String name, int times) {
        List<RequestThread> threads = new ArrayList<>(times);

        for (int i = 0; i < times; i++) {
            threads.add(thread(Character.toString((char) ('A' + i)), name));
        }

        return threads;
    }

    public void test() {
        // This helps to reproduce thread unsafe behavior that ConcurrentModification cannot usually detect.
        // Thread constAdd = constantAdd();

        List<RequestThread> threads = Stream
                .of(
                        threads("notch", 3),
                        threads("jack", 4),
                        threads("bob_the_builder123", 5)
                )
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        // constAdd.start();
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
        // constAdd.interrupt();

        try {
            RequestThread finalThread = thread("Finalized", "notch").finalized();
            finalThread.start();
            finalThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (!threadExceptions.isEmpty()) {
            RuntimeException ex = new RuntimeException("One of the XSkull test threads threw an exception");
            threadExceptions.forEach(ex::addSuppressed);
            throw ex;
        }

        // It should be 2 (one for each) but it's possible for the first
        // thread to somehow quickly finish its job and exit or for other
        // threads to start with a delay because of current hardware pressure.
        XLogger.log("[XSkull Thread Queue] Used locks: " + USED_LOCKS);

        Map<String, KeyedLock<String, ?>> locks = getLocks(MojangRequestQueue.USERNAME_REQUESTS);
        Assertions.assertTrue(locks.isEmpty(), () -> "Username requests not empty: " + locks);

        // Currently the following test is the most important as it still happens in rare conditions.
        // You can read the long comment inside KeyedLockMap#lock method in order to understand this better.
        for (Map.Entry<String, AtomicInteger> cache : NOT_CACHED_TIMES.entrySet()) {
            int times = cache.getValue().get();
            Assertions.assertEquals(1, times, () -> "Cached more than once: " + cache.getKey());
        }

        realTest();
    }

    @SuppressWarnings("unused")
    private Thread constantAdd() {
        // For some unknown reason, using this will make the duplicated recalculation of an already cached entry more difficult if not impossible.
        Thread thread = new Thread(() -> {
            final long SLEEP_NS = 100_000; // 1ms = 1,000,000ns | ~1,000 requests
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            int total = 0;
            long lastTime = 0;

            while (true) {
                // Thread.sleep() might not have this accuracy.
                long currentTime = System.nanoTime();
                long passed = currentTime - lastTime;
                if (passed > SLEEP_NS) {
                    if (Thread.interrupted()) {
                        XLogger.log("XSkull constant add thread interrupted: " + total);
                        break;
                    }

                    lastTime = currentTime;
                    total++;
                    USERNAME_TO_UUID.put(randomString(rand), UUID.randomUUID());
                } else {
                    if (Thread_onSpinWait != null) {
                        try {
                            Thread_onSpinWait.invokeExact();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, "XSkull Constant Add");
        thread.setUncaughtExceptionHandler(exceptionHandler);
        return thread;
    }

    private static final char[] RANDOM_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_".toCharArray();

    private static String randomString(ThreadLocalRandom rnd) {
        int length = rnd.nextInt(4, 17);
        StringBuilder builder = new StringBuilder(length);

        while (builder.length() < length) {
            int index = (int) (rnd.nextFloat() * RANDOM_CHARS.length);
            builder.append(RANDOM_CHARS[index]);
        }

        return builder.toString();
    }

    private static void realTest() {
        // This can vary specially for the second request since
        // they have to wait for the first one to finish.
        // The best way to test this would be to somehow limit your
        // internet speed significantly.
        // Whatever the duration is, it should progressively get smaller and smaller.
        // 800ms -> 630ms -> 600ms -> 5ms -> 0ms
        List<CompletableFuture<Void>> profiles = new ArrayList<>(5);

        for (int i = 1; i <= 5; i++) {
            if (LOG) {
                profiles.add(XLogger.logTimingsAsync("Notch Async Lookup " + i, () -> Profileable.username("Hex_26").getProfile()));
            }
        }

        CompletableFuture.allOf(profiles.toArray(new CompletableFuture[0])).join();
    }

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    private static <K> Map<K, KeyedLock<K, ?>> getLocks(KeyedLockMap<K> lockMap) {
        try {
            return (Map<K, KeyedLock<K, ?>>) KeyedLockMap_locks.invoke(lockMap);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static int getPendingTasks(KeyedLock<?, ?> lock) {
        if (lock.getClass().getSimpleName().equals("FinalizedKeyedLock")) return -1;
        try {
            Object realLock = lock.getClass().getSimpleName().equals("ReferentialKeyedLock") ? ReferentialKeyedLock_lock.invoke(lock) : lock;
            return (int) KeyedLock_pendingTasks.invoke(realLock);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private final Thread.UncaughtExceptionHandler exceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            threadExceptions.add(new RuntimeException("Thread [" + thread.getName() + "] threw an exception", ex));
        }
    };

    private final class RequestThread extends Thread {
        private final String username;
        private boolean finalized;

        public RequestThread(String iteration, String username) {
            super("XSkull Thread Queue | " + username + " | " + iteration);
            this.username = username;
            setUncaughtExceptionHandler(exceptionHandler);
        }

        @SuppressWarnings("ReturnOfInnerClass")
        public RequestThread finalized() {
            this.finalized = true;
            return this;
        }

        void log(String msg) {
            if (!LOG) return;
            // Spigot's injected logger already prints the thread name as a prefix
            XLogger.log(msg);
        }

        @Override
        public void run() {
            log("Started");
            try (KeyedLock<String, UUID> lock = MojangRequestQueue.USERNAME_REQUESTS.lock(username, USERNAME_TO_UUID::get)) {
                log("Lock acquired " + lock);
                if (finalized) {
                    if (!lock.getClass().getSimpleName().equals("FinalizedKeyedLock"))
                        throw new IllegalStateException("Expected finalized lock for " + this.getName() + " but got " + lock);
                }

                USED_LOCKS.add(lock);
                //  lock.lock();
                log("Synchronized");
                UUID id = lock.getOrRetryValue();
                if (id == null) {
                    // log("Not cached " + USERNAME_TO_UUID); Causes ConcurrentModificationException because toString iterates all entries
                    log("Not cached");
                    NOT_CACHED_TIMES.compute(username, (k, v) -> {
                        if (v == null) v = new AtomicInteger();
                        v.incrementAndGet();
                        return v;
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    id = UUID.randomUUID();
                    USERNAME_TO_UUID.put(username, id);
                    // log("Cached: " + USERNAME_TO_UUID);
                } else {
                    log("Already cached: " + id);
                }
                log("Lock released " + getPendingTasks(lock));
            }
            log("Lock closed");
        }
    }

    private RequestThread thread(String threadName, String username) {
        return new RequestThread(threadName, username);
    }
}
