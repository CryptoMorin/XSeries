package com.cryptomorin.xseries.profiles.mojang;

import com.cryptomorin.xseries.profiles.ProfilesCore;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@ApiStatus.Internal
public final class PlayerProfileFetcherThread implements ThreadFactory {
    /**
     * An executor service with a fixed thread pool of size 2, used for asynchronous operations.
     */
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2, new PlayerProfileFetcherThread());

    private static final AtomicInteger COUNT = new AtomicInteger();

    @Override
    public Thread newThread(@Nonnull final Runnable run) {
        final Thread thread = new Thread(run);
        thread.setName("Profile Lookup Executor #" + COUNT.getAndIncrement());
        thread.setUncaughtExceptionHandler((t, throwable) ->
                ProfilesCore.LOGGER.error("Uncaught exception in thread {}", t.getName(), throwable));
        return thread;
    }
}