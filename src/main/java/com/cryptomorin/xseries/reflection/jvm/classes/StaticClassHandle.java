package com.cryptomorin.xseries.reflection.jvm.classes;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class StaticClassHandle extends ClassHandle {
    protected Class<?> clazz;

    public StaticClassHandle(Class<?> clazz) {this.clazz = clazz;}

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
        for (int i = 0; i < dimension; i++) {
            arrayClass = Array.newInstance(arrayClass, 0).getClass();
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
}
