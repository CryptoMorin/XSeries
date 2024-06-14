package com.cryptomorin.xseries.reflection.jvm.classes;

import com.cryptomorin.xseries.reflection.jvm.ReflectiveNamespace;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class StaticClassHandle extends ClassHandle {
    protected Class<?> clazz;

    public StaticClassHandle(ReflectiveNamespace namespace, Class<?> clazz) {
        super(namespace);
        this.clazz = clazz;
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
}
