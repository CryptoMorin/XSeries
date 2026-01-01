/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2026 Crypto Morin
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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A version of {@link AggregateReflectiveHandle} that returns an object associated with a
 * {@link ReflectiveHandle} instead of the handle itself.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 *    String name = XReflection.any(
 *        XReflection.of(GameProfileCache.class).method().named("getProfile", "a"), "test"
 *    ).or(
 *        XReflection.of(GameProfileCache.class).method().named("profile", "b"), "test2"
 *    ).get();
 * }</pre>
 *
 * @param <H> the types of handles that are going to be checked.
 * @see AggregateReflectiveHandle
 * @see VersionHandle
 */
@ApiStatus.Experimental
public class AggregateReflectiveSupplier<H extends ReflectiveHandle<?>, O> {
    private final List<ReflectivePair> handles = new ArrayList<>();
    private Consumer<H> handleModifier;

    /**
     * Use {@link XReflection#supply(ReflectiveHandle, Object)} instead.
     */
    @ApiStatus.Internal
    public AggregateReflectiveSupplier() {}

    private final class ReflectivePair {
        private final Callable<H> handle;
        private final Supplier<O> object;

        private ReflectivePair(Callable<H> handle, Supplier<O> object) {
            this.handle = handle;
            this.object = object;
        }
    }

    public AggregateReflectiveSupplier<H, O> or(@NotNull H handle, O object) {
        return or(() -> handle, object);
    }

    public AggregateReflectiveSupplier<H, O> or(@NotNull Callable<H> handle, O object) {
        return or(handle, () -> object);
    }

    public AggregateReflectiveSupplier<H, O> or(@NotNull H handle, Supplier<O> object) {
        return or(() -> handle, object);
    }

    public AggregateReflectiveSupplier<H, O> or(@NotNull Callable<H> handle, Supplier<O> object) {
        this.handles.add(new ReflectivePair(handle, object));
        return this;
    }

    /**
     * Action performed on all the handles before being checked.
     */
    public AggregateReflectiveSupplier<H, O> modify(@Nullable Consumer<H> handleModifier) {
        this.handleModifier = handleModifier;
        return this;
    }

    public O get() {
        ClassNotFoundException errors = null;

        for (ReflectivePair pair : handles) {
            H handled;
            try {
                handled = pair.handle.call();
                if (handleModifier != null) handleModifier.accept(handled);
                if (!handled.exists()) handled.reflect(); // If it doesn't exist, throw the error to catch.
                return pair.object.get();
            } catch (Throwable ex) {
                if (errors == null)
                    errors = new ClassNotFoundException("None of the aggregate handles were successful");
                errors.addSuppressed(ex);
            }
        }

        throw XReflection.throwCheckedException(XReflection.relativizeSuppressedExceptions(errors));
    }
}
