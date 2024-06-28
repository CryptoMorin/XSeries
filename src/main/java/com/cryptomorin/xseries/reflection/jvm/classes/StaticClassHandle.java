package com.cryptomorin.xseries.reflection.jvm.classes;

import com.cryptomorin.xseries.reflection.ReflectiveNamespace;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * A class that is known at compile time and can be referenced directly.
 * <p>
 * The purpose of this handle is to use reflection on the members
 * (fields, methods and constructors) of the corresponding class.
 * @see DynamicClassHandle
 */
public class StaticClassHandle extends ClassHandle {
    @NotNull
    protected Class<?> clazz;

    public StaticClassHandle(ReflectiveNamespace namespace, @NotNull Class<?> clazz) {
        super(namespace);
        this.clazz = Objects.requireNonNull(clazz);
    }

    private Class<?> purifyClass() {
        Class<?> pureClazz = clazz;

        while (true) {
            Class<?> component = pureClazz.getComponentType();
            if (component != null) pureClazz = component;
            else break;
        }

        return Objects.requireNonNull(pureClazz);
    }

    public StaticClassHandle asArray(int dimension) {
        Class<?> arrayClass = purifyClass();
        if (dimension > 0) {
            for (int i = 0; i < dimension; i++) {
                arrayClass = Array.newInstance(arrayClass, 0).getClass();
            }
        }
        this.clazz = arrayClass;
        return this;
    }

    @Override
    public Class<?> reflect() throws ClassNotFoundException {
        return this.clazz;
    }

    @Override
    public boolean isArray() {
        return clazz.isArray();
    }

    @Override
    public Set<String> getPossibleNames() {
        return Collections.singleton(clazz.getSimpleName());
    }

    @Override
    public StaticClassHandle clone() {
        return new StaticClassHandle(namespace, this.clazz);
    }
}
