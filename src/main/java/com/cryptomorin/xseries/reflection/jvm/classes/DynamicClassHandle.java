package com.cryptomorin.xseries.reflection.jvm.classes;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DynamicClassHandle extends ClassHandle {
    protected String packageName;
    protected final List<String> classNames = new ArrayList<>(5);
    protected int array;

    public DynamicClassHandle inPackage(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public DynamicClassHandle named(String... clazzNames) {
        this.classNames.addAll(Arrays.asList(clazzNames));
        return this;
    }

    public String[] reflectClassNames() {
        Objects.requireNonNull(packageName, "Package name is null");
        String[] classNames = new String[this.classNames.size()];

        for (int i = 0; i < this.classNames.size(); i++) {
            @SuppressWarnings("NonConstantStringShouldBeStringBuffer")
            String clazz = packageName + '.' + this.classNames.get(i);
            if (array != 0) clazz = Strings.repeat("[", array) + 'L' + clazz + ';';

            classNames[i] = clazz;
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

        throw errors;
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
}
