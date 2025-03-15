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

package com.cryptomorin.xseries.profiles.builder;

import com.cryptomorin.xseries.profiles.ProfileLogger;
import com.cryptomorin.xseries.profiles.exceptions.InvalidProfileException;
import com.cryptomorin.xseries.profiles.exceptions.ProfileChangeException;
import com.cryptomorin.xseries.profiles.exceptions.ProfileException;
import com.cryptomorin.xseries.profiles.mojang.PlayerProfileFetcherThread;
import com.cryptomorin.xseries.profiles.mojang.ProfileRequestConfiguration;
import com.cryptomorin.xseries.profiles.objects.DelegateProfileable;
import com.cryptomorin.xseries.profiles.objects.ProfileContainer;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import com.mojang.authlib.GameProfile;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Represents an instruction that sets a property of a {@link GameProfile}.
 * It uses a {@link #profileContainer} to define how to set the property and
 * a {@link #profileable} to define what to set in the property.
 *
 * @param <T> The type of the result produced by the {@link #profileContainer} function.
 */
public final class ProfileInstruction<T> implements DelegateProfileable {
    /**
     * The function called that applies the given {@link #profileable} to an object that supports it
     * such as {@link ItemStack}, {@link SkullMeta} or a {@link BlockState}.
     */
    private final ProfileContainer<T> profileContainer;
    /**
     * The main profile to set.
     */
    private Profileable profileable;
    /**
     * All fallback profiles to try if the main one fails.
     */
    private final List<Profileable> fallbacks = new ArrayList<>();
    private Consumer<ProfileFallback<T>> onFallback;
    private ProfileRequestConfiguration profileRequestConfiguration;

    private boolean lenient = false;

    protected ProfileInstruction(ProfileContainer<T> profileContainer) {
        this.profileContainer = profileContainer;
    }

    /**
     * Removes the profile and skin texture from the item/block.
     */
    @NotNull
    @Contract(mutates = "this")
    public T removeProfile() {
        profileContainer.setProfile(null);
        return profileContainer.getObject();
    }

    @ApiStatus.Experimental
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public ProfileInstruction<T> profileRequestConfiguration(ProfileRequestConfiguration config) {
        this.profileRequestConfiguration = config;
        return this;
    }

    /**
     * Fails silently if any of the {@link ProfileException} errors occur.
     * Mainly affects {@link Profileable#detect(String)}
     */
    @NotNull
    @Contract(value = "-> this", mutates = "this")
    public ProfileInstruction<T> lenient() {
        this.lenient = true;
        return this;
    }

    /**
     * The current profile of the item/block (not the profile provided in {@link #profile(Profileable)})
     */
    @Override
    @Nullable
    public GameProfile getProfile() {
        // Just here to handle the JavaDocs.
        return profileContainer.getProfile();
    }

    @Override
    @Contract(pure = true)
    public Profileable getDelegateProfile() {
        return profileContainer;
    }

    /**
     * Sets the texture profile to be set to the item/block. Use one of the
     * static methods of {@link Profileable} class.
     */
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public ProfileInstruction<T> profile(@NotNull Profileable profileable) {
        this.profileable = Objects.requireNonNull(profileable, "Profileable is null");
        return this;
    }

    /**
     * A list of fallback profiles in order. If the profile set in {@link #profile(Profileable)} fails,
     * these profiles will be tested in order until a correct one is found,
     * also if any of the fallback profiles are used, {@link #onFallback} will be called too.
     *
     * @see #apply()
     */
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public ProfileInstruction<T> fallback(@NotNull Profileable... fallbacks) {
        Objects.requireNonNull(fallbacks, "fallbacks array is null");
        this.fallbacks.addAll(Arrays.asList(fallbacks));
        return this;
    }

    /**
     * Called when any of the {@link #fallback(Profileable...)} profiles are used,
     * this is also called if no fallback profile is provided, but the main one {@link #profile(Profileable)} fails.
     *
     * @see #onFallback(Runnable)
     */
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public ProfileInstruction<T> onFallback(@Nullable Consumer<ProfileFallback<T>> onFallback) {
        this.onFallback = onFallback;
        return this;
    }

    /**
     * @see #onFallback(Consumer)
     */
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public ProfileInstruction<T> onFallback(@NotNull Runnable onFallback) {
        Objects.requireNonNull(onFallback, "onFallback runnable is null");
        this.onFallback = (fallback) -> onFallback.run();
        return this;
    }

    /**
     * Sets the profile generated by the instruction to the result type synchronously.
     * This is recommended if your code is already not on the main thread, or if you know
     * that the skull texture doesn't need additional requests.
     *
     * <h2>What are these additional requests?</h2>
     * This only applies to offline mode (cracked) servers. Since these servers use
     * a cracked version of the player UUIDs and not their real ones, the real UUID
     * needs to be known by requesting it from Mojang servers and this request which
     * requires internet connection, will delay things a lot.
     *
     * @return The result after setting the generated profile.
     * @throws ProfileChangeException If any type of {@link ProfileException} occurs, they will be accumulated
     *                                in form of suppressed exceptions ({@link Exception#getSuppressed()}) in this single exception
     *                                starting from the main profile, followed by the fallback profiles.
     */
    @NotNull
    public T apply() {
        Objects.requireNonNull(profileable, "No profile was set");
        ProfileChangeException exception = null;

        List<Profileable> tries = new ArrayList<>(2 + fallbacks.size());
        tries.add(profileable);
        tries.addAll(fallbacks);
        if (lenient) tries.add(XSkull.getDefaultProfile());

        boolean success = false;
        boolean tryingFallbacks = false;
        for (Profileable profileable : tries) {
            try {
                GameProfile gameProfile = profileable.getDisposableProfile();
                if (gameProfile != null) {
                    profileContainer.setProfile(gameProfile);
                    success = true;
                    break;
                } else {
                    if (exception == null) {
                        exception = new ProfileChangeException("Could not set the profile for " + profileContainer);
                    }
                    exception.addSuppressed(new InvalidProfileException(profileable.toString(), "Profile doesn't have a value: " + profileable));
                    tryingFallbacks = true;
                }
            } catch (ProfileException ex) {
                if (exception == null) {
                    exception = new ProfileChangeException("Could not set the profile for " + profileContainer);
                }
                exception.addSuppressed(ex);
                tryingFallbacks = true;
            }
        }

        if (exception != null) {
            if (success || lenient) ProfileLogger.debug("apply() silenced exception {}", exception);
            else throw exception;
        }

        T object = profileContainer.getObject();
        if (tryingFallbacks && this.onFallback != null) {
            ProfileFallback<T> fallback = new ProfileFallback<>(this, object, exception);
            this.onFallback.accept(fallback);
            object = fallback.getObject();
        }
        return object;
    }

    /**
     * Asynchronously applies the instruction to generate a {@link GameProfile} and returns a {@link CompletableFuture}.
     * This method is designed for non-blocking execution, allowing tasks to be performed
     * in the background without blocking the server's main thread.
     * This method will always execute async, even if the results are cached.
     * <br>
     * <h2>Reference Issues</h2>
     * Note that while these methods apply to the item/block instances, passing these instances
     * to certain methods, for example {@link org.bukkit.inventory.Inventory#setItem(int, ItemStack)}
     * will create a NMS copy of that instance and use that instead. Which means if for example
     * you're going to be using an item for an inventory, you'd have to set the item again
     * manually to the inventory once this method is done.
     * <pre>{@code
     * Inventory inventory = ...;
     * XSkull.createItem().profile(player).applyAsync()
     *     .thenAcceptAsync(item -> inventory.setItem(slot, item));
     * }</pre>
     * <p>
     * To make this cleaner, you could change the first line of the item's lore to something like "Loading..."
     * and set it to the inventory right away so the player knows that the data is not fully loaded.
     * Once this method is done, you could change the lore back and set the item back to the inventory.
     * (The lore is preferred because it has less text limit compared to the title, it also gives the player
     * all the textual information they need rather than the visual information if you're in a hurry)
     * <br><br><br>
     * <h2>Usage example:</h2>
     * <pre>{@code
     *   XSkull.createItem().profile(player).applyAsync()
     *      .thenAcceptAsync(result -> {
     *          // Additional processing...
     *      }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
     * }</pre>
     *
     * @return A {@link CompletableFuture} that will complete asynchronously.
     */
    @NotNull
    public CompletableFuture<T> applyAsync() {
        return CompletableFuture.supplyAsync(this::apply, PlayerProfileFetcherThread.EXECUTOR);
    }
}