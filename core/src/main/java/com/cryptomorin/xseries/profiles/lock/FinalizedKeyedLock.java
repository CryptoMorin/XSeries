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

/**
 * A lock that doesn't do anything because it already has the value it needs.
 */
@ApiStatus.Internal
final class FinalizedKeyedLock<K, V> implements KeyedLock<K, V> {
    private final V value;

    protected FinalizedKeyedLock(V value) {this.value = value;}

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '(' + value + ')';
    }

    // @formatter:off
    @Override public V getOrRetryValue() {return value;} // This is a finalized lock, value is already available
    @Override public void lock() {} // No-op: Lock is already finalized
    @Override public void unlock() {} // No-op: Lock is already finalized
    @Override public void close() {} // No-op: Nothing to close for a finalized lock
}
