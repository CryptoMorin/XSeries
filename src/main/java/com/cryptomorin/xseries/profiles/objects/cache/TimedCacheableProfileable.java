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

import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * Any {@link Profileable} that can have its results cached temporarily.
 * This class should not be used directly.
 */
@ApiStatus.Internal
public abstract class TimedCacheableProfileable extends CacheableProfileable {
    private long lastUpdate;

    /**
     * The amount of time the cached results of this profile can be used until it's re-evaluated.
     * By default, it uses the internal cache's expiration date (6 hours)
     * {@link Duration#ZERO} or negative durations should not be used.
     */
    @NotNull
    protected Duration expiresAfter() {
        return Duration.ofHours(6);
    }

    /**
     * @return true if this profile hasn't been cached yet or the cache is expired.
     */
    @Override
    public final boolean hasExpired(boolean renew) {
        if (super.hasExpired(renew)) return true;
        if (cache == null && lastError == null) return true;

        Duration expiresAfter = expiresAfter();
        if (expiresAfter.isZero()) return false;

        long now = System.currentTimeMillis();
        if (lastUpdate == 0) {
            if (renew) lastUpdate = now;
            return true;
        } else {
            long diff = now - lastUpdate;
            if (diff >= expiresAfter.toMillis()) {
                if (renew) lastUpdate = now;
                return true;
            } else {
                return false;
            }
        }
    }
}
