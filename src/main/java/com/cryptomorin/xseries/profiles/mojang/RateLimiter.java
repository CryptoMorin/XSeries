package com.cryptomorin.xseries.profiles.mojang;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Used for {@link MojangAPI} requests.
 * The rate limits make things difficult specially in BungeeCord servers where
 * a single dedicated server is used for multiple subservers.
 * This will specially not work well at all if in shared hosting
 * that can make requests almost impossible with all the servers
 * in a single node.
 * Another issue is that XSeries is mostly intended to be shaded by individual plugins
 * so while we are directly helping the internal Mojang cache, there are some
 * requests that Mojang doesn't have a cache for, so this creates another big
 * problem of having to use separate caches that are not shared.
 * <p>
 * However, these are all untested speculation. But one can't imagine how else
 * they'd identify a request other than the sender's IP because the default
 * client used by Mojang doesn't even use a {@code User-Agent}.
 * According to a comment by a Mojang web service admin in <a href="https://bugs.mojang.com/browse/WEB-7008?focusedId=1319363&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-1319363">this JIRA issue</a>
 * shared VPS are indeed an issue due to shared IPs.
 * <p>
 * According to <a href="https://wiki.vg/Mojang_API">Mojang API</a> the rate limit
 * is around 600 requests per 10 (i.e. 1 request per second) for most endpoints.
 * However <a href="https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin.2FCape">UUID to Profile and Skin/Cape</a>
 * is around 200 requests per minute.
 */
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