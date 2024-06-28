package com.cryptomorin.xseries.reflection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
 * <h2>Usage</h2>
 * <pre>{@code
 *    MethodMemberHandle profileByName = XReflection.of(GameProfileCache.class).method().named("getProfile", "a");
 *    MethodHandle getProfileByName = XReflection.anyOf(
 *        () -> profileByName.signature("public GameProfile get(String username);"),
 *        () -> profileByName.signature("public Optional<GameProfile> get(String username);")
 *    ).reflect();
 * }</pre>
 * @param <T> the JVM type of {@link H}
 * @param <H> the types of handles that are going to be checked.
 * @see ReflectiveHandle
 */
public class AggregateReflectiveHandle<T, H extends ReflectiveHandle<T>> implements ReflectiveHandle<T> {
    private final List<Callable<H>> handles;
    private Consumer<H> handleModifier;

    protected AggregateReflectiveHandle(Collection<Callable<H>> handles) {
        this.handles = new ArrayList<>(handles.size());
        this.handles.addAll(handles);
    }

    /**
     * @see #or(Callable)
     */
    public AggregateReflectiveHandle<T, H> or(@Nonnull H handle) {
        return or(() -> handle);
    }

    /**
     * @see #or(ReflectiveHandle)
     */
    public AggregateReflectiveHandle<T, H> or(@Nonnull Callable<H> handle) {
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

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public AggregateReflectiveHandle<T, H> clone() {
        AggregateReflectiveHandle<T, H> handle = new AggregateReflectiveHandle<>(new ArrayList<>(handles));
        handle.handleModifier = this.handleModifier;
        return handle;
    }

    @Override
    public T reflect() throws ReflectiveOperationException {
        ClassNotFoundException errors = null;

        for (Callable<H> handle : handles) {
            H handled;
            try {
                handled = handle.call();
            } catch (Throwable ex) {
                if (errors == null)
                    errors = new ClassNotFoundException("None of the aggregate handles were successful");
                errors.addSuppressed(ex);
                continue;
            }
            if (handleModifier != null) handleModifier.accept(handled);
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
