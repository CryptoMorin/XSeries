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

package com.cryptomorin.xseries.profiles.lock;

import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A registry that queues locks per-object using {@link KeyedLock}.
 *
 * @param <K> the type of object used to identify what the locks are associated to.
 */
@ApiStatus.Internal
public final class KeyedLockMap<K> {
    private final Map<K, NulledKeyedLock<K, ?>> locks = new HashMap<>();

    private NulledKeyedLock<K, ?> createLock(K key) {
        return new NulledKeyedLock<>(this, key);
    }

    /**
     * @see #lock(Object, Function)
     */
    public <V> KeyedLock<K, V> lock(K key, Supplier<V> fetcher) {
        Objects.requireNonNull(fetcher, "Value fetcher is null");
        return lock(key, (k) -> fetcher.get());
    }

    /**
     * This method will return a finalized lock if the fetcher already
     * has the value, which prevents the unnecessary synchronization and
     * creation of locks, otherwise the associated key will be locked
     * and the value can be retrieved again with {@link KeyedLock#getOrRetryValue()}
     * after the lock was released by other threads.
     */
    @SuppressWarnings("unchecked")
    public <V> KeyedLock<K, V> lock(K key, Function<K, V> fetcher) {
        Objects.requireNonNull(fetcher, "Value fetcher is null");
        Objects.requireNonNull(key, "Key is null");

        V value = fetcher.apply(key);
        if (value != null) return new FinalizedKeyedLock<>(value);

        NulledKeyedLock<K, V> lock;

        synchronized (locks) {
            lock = (NulledKeyedLock<K, V>) locks.computeIfAbsent(key, this::createLock);
            boolean isNew = lock.pendingTasks == 0;
            lock.pendingTasks++;

            // We can't share ReferentialKeyedLock, because the fetcher may calculate different values
            // although the underlying cache is related.
            // e.g. PlayerUUIDs USERNAME_TO_ONLINE & OFFLINE_TO_ONLINE are both linked together
            // and their values are expected to be updated together, but obviously they give
            // different information about the same thing.
            // ------------------------------------------------------------------------------------------
            // The statement "lock.lock()"
            // needs to be called as the first statement before literally anything else, otherwise
            // the cache will fail and attempt to cache the same key twice. To reproduce this, you
            // can simply put a random statement before this (can be reproduced by XSkullRequestQueueTest
            // which will occasionally fail, so you might have to test 2-3 times), for example:
            // {
            // try {
            //     Thread.sleep(1000);
            // } catch (InterruptedException e) {
            //     throw new RuntimeException(e);
            // }
            // // (Or even just System.out.println("Hello"); works too sometimes)
            // lock.lock();
            // }
            // However there is another way to fix this other than forcing the lock as the first statement,
            // and it's that we should always return a ReferentialKeyedLock instead of NulledKeyedLock directly
            // for the first lock.
            // This issue happens because other threads might lock it faster than the one which requested it,
            // so the raw NulledKeyedLock should belong to them but since the pendingTasks check was done here,
            // it'll be used for the first request.
            //
            // Note that the cache will still be null in very rare cases after the first request cached it.
            // This could be because we use a normal HashMap (for the cache which is referenced by the fetcher function,
            // not the map that holds the locks in this class) which is not thread-safe, but we're already
            // synchronizing the keys, so this might be because of some internal rehashing that is happening
            // which causes this incompatibility. However, the chances of this happening is very, very small in
            // most cases (almost 1 out of 100,000 requests) that is not worth the performance sacrifice of
            // using Collections.synchronizedMap() or ConcurrentHashMap.
            // These results have been confirmed by many repeated iterations of XSkullRequestQueueTest.
            if (isNew) {
                // We lock here because as soon as we exit the synchronized block other threads
                // have a tiny chance of acquiring this lock faster than us... somehow...
                // Also, we add this check as a form of assertion to make sure everything is going
                // as we expect.
                if (lock.tryLock()) return lock;
                else throw new IllegalStateException("Expected first lock holder to be free: " + lock);
            }
        }

        lock.lock();
        return new ReferentialKeyedLock<>(lock, fetcher);
    }

    @SuppressWarnings("resource")
    void unlock(NulledKeyedLock<K, ?> lock) {
        synchronized (locks) {
            lock.pendingTasks--; // Not atomic by itself but we're already synchronized.
            if (lock.pendingTasks <= 0)
                locks.remove(lock.key);
        }

        // There is no reason to synchronize this.
        lock.lock.unlock();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '(' + locks + ')';
    }
}
