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

@ApiStatus.Internal
public final class KeyedLockMap<K> {
    private final Map<K, KeyedLock<K>> locks = new HashMap<>();

    private KeyedLock<K> createLock(K key) {
        return new KeyedLock<>(this, key);
    }

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
            lock.lock.unlock();
            lock.pendingTasks--;
            if (lock.pendingTasks <= 0)
                locks.remove(lock.key);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '(' + locks + ')';
    }
}
