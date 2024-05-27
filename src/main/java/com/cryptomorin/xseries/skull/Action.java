package com.cryptomorin.xseries.skull;

import com.mojang.authlib.GameProfile;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The {@code Action} class represents an action that can be performed on a {@link GameProfile}.
 * It uses an {@link Instruction} to define how the action is applied.
 *
 * @param <T> The type of the result produced by the action.
 */
public class Action<T> {

    /**
     * An executor service with a fixed thread pool of size 2, used for asynchronous operations.
     */
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

    /**
     * The instruction that defines how the action is applied.
     */
    private final Instruction<T> instruction;

    /**
     * Constructs an {@code Action} with the specified instruction.
     *
     * @param instruction The instruction that defines how the action is applied.
     */
    protected Action(Instruction<T> instruction) {
        this.instruction = instruction;
    }

    /**
     * Applies the instruction to a {@link GameProfile} and returns the result.
     *
     * @return The result of applying the instruction to the {@link GameProfile}.
     */
    public T apply() {
        GameProfile profile = instruction.type != null
                ? XSkull.getProfile(instruction.type, instruction.input)
                : XSkull.getProfile(InputType.get(instruction.input), instruction.input);
        return instruction.setter.apply(profile);
    }

    /**
     * Asynchronously applies the instruction to a {@link GameProfile} and returns a {@link CompletableFuture}
     * that will complete with the result. This method is designed for non-blocking execution,
     * allowing tasks to be performed in the background without slowing down the server's main thread.
     *
     * <p>Usage example:</p>
     * <pre>{@code
     *   XSkull.of(XMaterial.PLAYER_HEAD.parseItem())
     *      .profile("75714f9975d9934f062129b154aed949ce52df10c03099b60803cdb5485c2917").applyAsync()
     *      .thenAcceptAsync(itemStack -> {
     *          // Additional processing...
     *      }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
     * }</pre>
     *
     * @return A {@link CompletableFuture} that will complete with the result of applying the instruction.
     */
    public CompletableFuture<T> applyAsync() {
        return CompletableFuture.supplyAsync(this::apply, EXECUTOR);
    }
}
