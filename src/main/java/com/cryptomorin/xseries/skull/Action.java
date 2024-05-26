package com.cryptomorin.xseries.skull;

import com.mojang.authlib.GameProfile;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class Action<T> {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2, new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger();
        @Override
        public Thread newThread(final @Nonnull Runnable run) {
            final Thread thread = new Thread(run);
            thread.setName("Profile Lookup Executor #" + this.count.getAndIncrement());
            thread.setUncaughtExceptionHandler((t, throwable) ->
                    XSkull.LOGGER.debug("Uncaught exception in thread {}", t.getName(), throwable));
            return thread;
        }
    });

    private final Instruction<T> instruction;

    protected Action(Instruction<T> instruction) {
        this.instruction = instruction;
    }

    public T apply() {
        GameProfile profile = instruction.type != null
                ? XSkull.getProfile(instruction.type, instruction.input)
                : XSkull.getProfile(InputType.get(instruction.input), instruction.input);
        return instruction.setter.apply(profile);
    }

    public CompletableFuture<T> applyAsync() {
        return CompletableFuture.supplyAsync(this::apply, EXECUTOR);
    }
}
