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

package com.cryptomorin.xseries.profiles.lock;

import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry that queues locks per-object using {@link KeyedLock}.
 *
 * @param <K> the type of object used to identify what the locks are associated to.
 */
@ApiStatus.Internal
public final class KeyedLockMap<K> {
    private final Map<K, KeyedLock<K>> locks = new HashMap<>();

    private KeyedLock<K> createLock(K key) {
        return new KeyedLock<>(this, key);
    }

    /**
     * Doesn't actually lock the object, {@link KeyedLock#lock()} should
     * be used immediatelly as the first statement after this method.
     * The main reason why this isn't done in this method is because
     * try-with-resources for a reference without delcaring a variable
     * is not supported in Java 8, so we'd have to suppress the unused
     * variable warning every time we use this method.
     */
    public KeyedLock<K> lock(K key) {
        synchronized (locks) {
            KeyedLock<K> lock = locks.computeIfAbsent(key, this::createLock);
            lock.pendingTasks++;
            return lock;
        }
    }

    @SuppressWarnings("resource")
    public void unlock(KeyedLock<K> lock) {
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
