package com.cryptomorin.xseries.reflection.jvm.classes;

import com.cryptomorin.xseries.reflection.ReflectiveNamespace;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

/**
 * A class handle that was parsed from string, the name is known, but it doesn't exist
 * at runtime.
 * @see StaticClassHandle
 */
public class UnknownClassHandle extends ClassHandle {
    private final String name;

    public UnknownClassHandle(ReflectiveNamespace namespace, String name) {
        super(namespace);
        this.name = name;
    }

    @Override
    public Set<String> getPossibleNames() {
        return Collections.singleton(name);
    }

    @Override
    public UnknownClassHandle asArray(int dimensions) {
        return new UnknownClassHandle(namespace, name + "[]");
    }

    @Override
    public boolean isArray() {
        return this.name.endsWith("[]");
    }

    @Override
    public UnknownClassHandle clone() {
        return new UnknownClassHandle(namespace, this.name);
    }

    @NotNull
    @Override
    public Class<?> reflect() throws ReflectiveOperationException {
        throw new ReflectiveOperationException("Unknown class: " + name);
    }

    @Override
    public String toString() {
        return "UnknownClassHandle(" + name + ')';
    }
}
