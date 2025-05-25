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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A lock associated to an object that keeps track of how many threads are
 * currently using this lock until none are left in which case it removes itself.
 * These locks should be registered in a {@link KeyedLockMap}.
 */
@ApiStatus.Internal
public final class KeyedLock<K> implements AutoCloseable {
    protected final Lock lock = new ReentrantLock();
    private final KeyedLockMap<K> map;
    protected final K key;

    /**
     * This does not need to be volatile because access
     * to this property is entirely synchronized (in {@link KeyedLockMap}) and
     * no two threads will access this at the same time.
     */
    protected int pendingTasks;

    public KeyedLock(KeyedLockMap<K> map, K key) {
        this.map = map;
        this.key = key;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        map.unlock(this);
    }

    @Override
    public void close() {
        this.unlock();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(key=" + key + ", pendingTasks=" + pendingTasks + ')';
    }
}
