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
