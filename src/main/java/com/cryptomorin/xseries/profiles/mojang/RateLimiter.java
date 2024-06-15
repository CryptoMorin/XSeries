package com.cryptomorin.xseries.profiles.mojang;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

@ApiStatus.Internal
public final class RateLimiter {
    private final ConcurrentLinkedQueue<Long> requests = new ConcurrentLinkedQueue<>();
    private final int maxRequests;
    private final long per;

    RateLimiter(int maxRequests, Duration per) {
        this.maxRequests = maxRequests;
        this.per = per.toMillis();
    }

    @CanIgnoreReturnValue
    @Unmodifiable
    private ConcurrentLinkedQueue<Long> getRequests() {
        if (requests.isEmpty()) return requests;

        // Implementing a cleanup delay is practically not any different
        // from just letting this loop to happen in terms of performance.
        long now = System.currentTimeMillis();
        Iterator<Long> iter = requests.iterator();
        while (iter.hasNext()) {
            long requestedAt = iter.next();
            long diff = now - requestedAt;
            if (diff > per) iter.remove();
            else break; // Requests are ordered, so if this fails, the others will fail too.
        }

        return requests;
    }

    public int getRemainingRequests() {
        return Math.max(0, maxRequests - getRequests().size());
    }

    public int getEffectiveRequestsCount() {
        return getRequests().size();
    }

    public void instantRateLimit() {
        long now = System.currentTimeMillis();
        for (int i = 0; i < getRemainingRequests(); i++) {
            requests.add(now);
        }
    }

    public boolean acquire() {
        if (getRemainingRequests() <= 0) {
            return false;
        } else {
            requests.add(System.currentTimeMillis());
            return true;
        }
    }

    public Duration timeUntilNextFreeRequest() {
        if (getRemainingRequests() == 0) {
            long now = System.currentTimeMillis();
            long oldestRequestedAt = requests.peek();
            long diff = now - oldestRequestedAt;
            return Duration.ofMillis(per - diff);
        }
        return Duration.ZERO;
    }

    public synchronized void acquireOrWait() {
        long sleepUntil = timeUntilNextFreeRequest().toMillis();
        if (sleepUntil == 0) return;
        try {
            Thread.sleep(sleepUntil);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "[total=" + getRequests().size() +
                ", remaining=" + getRemainingRequests() +
                ", maxRequests=" + maxRequests +
                ", per=" + per +
                ']';
    }
}