package com.cryptomorin.xseries.reflection.jvm.classes;

import com.cryptomorin.xseries.reflection.XReflection;
import com.google.common.base.Strings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DynamicClassHandle extends ClassHandle {
    protected String packageName;
    protected final Set<String> classNames = new HashSet<>(5);
    protected int array;

    public DynamicClassHandle inPackage(String packageName) {
        Objects.requireNonNull(packageName, "Null package name");
        this.packageName = packageName;
        return this;
    }

    public DynamicClassHandle named(String... classNames) {
        Objects.requireNonNull(classNames);
        for (String className : this.classNames) {
            Objects.requireNonNull(className, () -> "Cannot add null class name from: " + Arrays.toString(classNames) + " to " + this);
        }
        this.classNames.addAll(Arrays.asList(classNames));
        return this;
    }

    public String[] reflectClassNames() {
        Objects.requireNonNull(packageName, "Package name is null");
        String[] classNames = new String[this.classNames.size()];

        int i = 0;
        for (String className : this.classNames) {
            @SuppressWarnings("NonConstantStringShouldBeStringBuffer")
            String clazz = packageName + '.' + className;
            if (array != 0) clazz = Strings.repeat("[", array) + 'L' + clazz + ';';
            classNames[i++] = clazz;
        }

        return classNames;
    }

    @Override
    public Class<?> reflect() throws ClassNotFoundException {
        ClassNotFoundException errors = null;

        for (String className : reflectClassNames()) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ex) {
                if (errors == null) errors = new ClassNotFoundException("None of the classes were found");
                errors.addSuppressed(ex);
            }
        }

        throw XReflection.relativizeSuppressedExceptions(errors);
    }

    @Override
    public DynamicClassHandle asArray(int dimension) {
        if (dimension < 0) throw new IllegalArgumentException("Array dimension cannot be negative: " + dimension);
        this.array = dimension;
        return this;
    }

    @Override
    public boolean isArray() {
        return this.array > 0;
    }

    @Override
    public Set<String> getPossibleNames() {
        return classNames;
    }
}
