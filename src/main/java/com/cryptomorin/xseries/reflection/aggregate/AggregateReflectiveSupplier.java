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
 * <h2>Usage</h2>
 * <pre>{@code
 *    String name = XReflection.any(
 *        XReflection.of(GameProfileCache.class).method().named("getProfile", "a"), "test"
 *    ).or(
 *        XReflection.of(GameProfileCache.class).method().named("getProfile", "a"), "test2"
 *    ).get();
 * }</pre>
 * @param <H> the types of handles that are going to be checked.
 * @see AggregateReflectiveHandle
 */
@ApiStatus.Experimental
public class AggregateReflectiveSupplier<H extends ReflectiveHandle<?>, O> {
    private final List<ReflectivePair> handles = new ArrayList<>();
    private Consumer<H> handleModifier;

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
