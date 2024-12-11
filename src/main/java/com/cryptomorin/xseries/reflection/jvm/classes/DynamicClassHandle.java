/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Crypto Morin
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

package com.cryptomorin.xseries.reflection.jvm.classes;

import com.cryptomorin.xseries.reflection.ReflectiveNamespace;
import com.cryptomorin.xseries.reflection.XReflection;
import com.google.common.base.Strings;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @see StaticClassHandle
 */
public class DynamicClassHandle extends ClassHandle {
    protected ClassHandle parent;
    protected String packageName;
    protected final Set<String> classNames = new HashSet<>(5);
    protected int array;

    public DynamicClassHandle(ReflectiveNamespace namespace) {
        super(namespace);
    }

    public DynamicClassHandle inPackage(@Pattern(PackageHandle.JAVA_PACKAGE_PATTERN) @NotNull String packageName) {
        Objects.requireNonNull(packageName, "Null package name");
        this.packageName = packageName;
        return this;
    }

    public DynamicClassHandle inPackage(@NotNull PackageHandle packageHandle) {
        // noinspection PatternValidation
        return inPackage(packageHandle, "");
    }

    public DynamicClassHandle inPackage(@NotNull PackageHandle packageHandle,
                                        @Pattern(PackageHandle.JAVA_PACKAGE_PATTERN) @NotNull String packageName) {
        Objects.requireNonNull(packageHandle, "Null package handle type");
        Objects.requireNonNull(packageName, "Null package handle name");
        if (parent != null)
            throw new IllegalStateException("Cannot change package of an inner class: " + packageHandle + " -> " + packageName);
        this.packageName = packageHandle.getPackage(packageName);
        return this;
    }

    public DynamicClassHandle named(@Pattern(PackageHandle.JAVA_IDENTIFIER_PATTERN) @NotNull String... classNames) {
        Objects.requireNonNull(classNames);
        for (String className : this.classNames) {
            Objects.requireNonNull(className, () -> "Cannot add null class name from: " + Arrays.toString(classNames) + " to " + this);
        }
        this.classNames.addAll(Arrays.asList(classNames));
        return this;
    }

    public String[] reflectClassNames() {
        if (this.parent == null) Objects.requireNonNull(packageName, "Package name is null");
        String[] classNames = new String[this.classNames.size()];
        Class<?> parent = this.parent == null ? null : XReflection.of(this.parent.unreflect()).asArray(0).unreflect();

        int i = 0;
        for (String className : this.classNames) {
            @SuppressWarnings("NonConstantStringShouldBeStringBuffer")
            String clazz;
            if (parent == null) clazz = packageName + '.' + className;
            else clazz = parent.getName() + '$' + className;

            if (array != 0) clazz = Strings.repeat("[", array) + 'L' + clazz + ';';
            classNames[i++] = clazz;
        }

        return classNames;
    }

    @Override
    public DynamicClassHandle clone() {
        DynamicClassHandle handle = new DynamicClassHandle(namespace);
        handle.array = this.array;
        handle.parent = this.parent;
        handle.packageName = this.packageName;
        handle.classNames.addAll(this.classNames);
        return handle;
    }

    @Override
    public Class<?> reflect() throws ClassNotFoundException {
        String[] classNames = reflectClassNames();
        if (classNames.length == 0) throw new IllegalStateException("No class name specified for " + this);

        ClassNotFoundException errors = null;
        for (String className : classNames) {
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

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                (parent == null ? "" : parent + " -> ") +
                (parent == null ? packageName : (packageName == null ? "" : packageName)) +
                '(' + String.join("|", classNames) + ')' +
                (array == 0 ? "" : "[" + array + ']') +
                " }";
    }
}
