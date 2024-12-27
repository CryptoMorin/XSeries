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

package com.cryptomorin.xseries.reflection.aggregate;

import com.cryptomorin.xseries.reflection.ReflectiveHandle;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.objects.ReflectedObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Most reflection APIs already provide a name-based fallback system that can be used (like {@link com.cryptomorin.xseries.reflection.jvm.classes.DynamicClassHandle#named(String...)})
 * but sometimes the signature of certain JVM declarations just change entirely, that's where this class could be useful.
 * <p>
 * It simply tries the {@link #reflect()} method of each handle until one of them returns without throwing an exception (order matters).
 *
 * <h2>Usage</h2>
 * <pre>{@code
 *    MethodMemberHandle profileByName = XReflection.of(GameProfileCache.class).method().named("getProfile", "a");
 *    MethodHandle getProfileByName = XReflection.anyOf(
 *        () -> profileByName.signature("public GameProfile get(String username);"),
 *        () -> profileByName.signature("public Optional<GameProfile> get(String username);")
 *    ).reflect();
 * }</pre>
 *
 * @param <T> the JVM type of {@link H}
 * @param <H> the types of handles that are going to be checked.
 * @see ReflectiveHandle
 */
public class AggregateReflectiveHandle<T, H extends ReflectiveHandle<T>> implements ReflectiveHandle<T> {
    private final List<Callable<H>> handles;
    private Consumer<H> handleModifier;

    /**
     * Use {@link XReflection#anyOf(Callable[])} instead.
     */
    @ApiStatus.Internal
    public AggregateReflectiveHandle(Collection<Callable<H>> handles) {
        this.handles = new ArrayList<>(handles.size());
        this.handles.addAll(handles);
    }

    /**
     * @see #or(Callable)
     */
    public AggregateReflectiveHandle<T, H> or(@NotNull H handle) {
        return or(() -> handle);
    }

    /**
     * @see #or(ReflectiveHandle)
     */
    public AggregateReflectiveHandle<T, H> or(@NotNull Callable<H> handle) {
        this.handles.add(handle);
        return this;
    }

    /**
     * Action performed on all the handles before being checked.
     */
    public AggregateReflectiveHandle<T, H> modify(@Nullable Consumer<H> handleModifier) {
        this.handleModifier = handleModifier;
        return this;
    }

    public H getHandle() {
        ClassNotFoundException errors = null;

        for (Callable<H> handle : handles) {
            H handled;
            try {
                handled = handle.call();
                if (handleModifier != null) handleModifier.accept(handled);
                if (!handled.exists()) handled.reflect(); // If it doesn't exist, throw the error to catch.
                return handled;
            } catch (Throwable ex) {
                if (errors == null)
                    errors = new ClassNotFoundException("None of the aggregate handles were successful");
                errors.addSuppressed(ex);
            }
        }

        throw XReflection.throwCheckedException(XReflection.relativizeSuppressedExceptions(errors));
    }

    @Override
    public AggregateReflectiveHandle<T, H> copy() {
        AggregateReflectiveHandle<T, H> handle = new AggregateReflectiveHandle<>(new ArrayList<>(handles));
        handle.handleModifier = this.handleModifier;
        return handle;
    }

    @Override
    public @NotNull ReflectiveHandle<ReflectedObject> jvm() {
        return getHandle().jvm();
    }

    @Override
    public T reflect() throws ReflectiveOperationException {
        ClassNotFoundException errors = null;

        for (Callable<H> handle : handles) {
            // Find a handler that works without reflecting.
            H handled;
            try {
                handled = handle.call();
                if (handleModifier != null) handleModifier.accept(handled);
            } catch (Throwable ex) {
                if (errors == null)
                    errors = new ClassNotFoundException("None of the aggregate handles were successful");
                errors.addSuppressed(ex);
                continue;
            }

            // Reflect the working handler.
            try {
                return handled.reflect();
            } catch (Throwable ex) {
                if (errors == null)
                    errors = new ClassNotFoundException("None of the aggregate handles were successful");
                errors.addSuppressed(ex);
            }
        }

        throw XReflection.relativizeSuppressedExceptions(errors);
    }
}
