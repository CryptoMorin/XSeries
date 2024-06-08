package com.cryptomorin.xseries.reflection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class AggregateHandle<T, H extends Handle<T>> implements Handle<T> {
    private final List<Callable<H>> handles;
    private Consumer<H> handleModifier;

    public AggregateHandle() {
        this.handles = new ArrayList<>(5);
    }

    public AggregateHandle(Collection<Callable<H>> handles) {
        this.handles = new ArrayList<>(handles.size());
        this.handles.addAll(handles);
    }

    public AggregateHandle<T, H> or(H handle) {
        return or(() -> handle);
    }

    public AggregateHandle<T, H> or(Callable<H> handle) {
        this.handles.add(handle);
        return this;
    }

    public AggregateHandle<T, H> modify(Consumer<H> handleModifier) {
        this.handleModifier = handleModifier;
        return this;
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
