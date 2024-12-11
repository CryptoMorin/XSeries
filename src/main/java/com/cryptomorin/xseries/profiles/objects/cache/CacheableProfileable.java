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

package com.cryptomorin.xseries.profiles.objects.cache;

import com.cryptomorin.xseries.profiles.exceptions.MojangAPIRetryException;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import com.cryptomorin.xseries.reflection.XReflection;
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Any {@link Profileable} that can have its results cached.
 * This class should not be used directly.
 */
@ApiStatus.Internal
public abstract class CacheableProfileable implements Profileable {
    protected GameProfile cache;
    protected Throwable lastError;

    @Override
    public final synchronized GameProfile getProfile() {
        // Synchronized in case two threads try to access the
        // same profileable that is not cached yet. That way, other threads
        // will wait for the first one to cache the results so the other threads
        // can start accessing the cache instantly instead of sending multiple
        // requests for the same data.
        // This of course doesn't fix the issue if two separate Profileables
        // are used for a single value and both are somehow requested at the same time.
        if (hasExpired(true)) {
            lastError = null;
            cache = null;
        }

        if (lastError != null) {
            if (!(lastError instanceof MojangAPIRetryException)) {
                throw XReflection.throwCheckedException(lastError);
            }
        }

        if (cache == null) {
            try {
                cache = getProfile0();
                lastError = null;
            } catch (Throwable ex) {
                lastError = ex;
                throw ex;
            }
        }

        return cache;
    }

    /**
     * @return true if this profile hasn't been cached yet or the cache is expired.
     */
    public final boolean hasExpired() {
        return hasExpired(false);
    }

    protected boolean hasExpired(boolean renew) {
        return lastError instanceof MojangAPIRetryException;
    }

    @NotNull
    protected abstract GameProfile getProfile0();

    @Override
    public final String toString() {
        return this.getClass().getSimpleName() + "[cache=" + cache + ", lastError=" + lastError + ']';
    }
}
