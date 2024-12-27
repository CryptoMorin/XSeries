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

import com.cryptomorin.xseries.reflection.ReflectiveHandle;
import com.cryptomorin.xseries.reflection.ReflectiveNamespace;
import com.cryptomorin.xseries.reflection.constraint.ReflectiveConstraint;
import com.cryptomorin.xseries.reflection.constraint.ReflectiveConstraintException;
import com.cryptomorin.xseries.reflection.jvm.*;
import com.cryptomorin.xseries.reflection.jvm.objects.ReflectedObject;
import com.cryptomorin.xseries.reflection.jvm.objects.ReflectedObjectHandle;
import com.cryptomorin.xseries.reflection.parser.ReflectionParser;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @see DynamicClassHandle
 * @see StaticClassHandle
 */
public abstract class ClassHandle implements ReflectiveHandle<Class<?>>, NamedReflectiveHandle {
    protected final ReflectiveNamespace namespace;
    private final Map<Class<ReflectiveConstraint>, ReflectiveConstraint> constraints = new IdentityHashMap<>();

    protected ClassHandle(ReflectiveNamespace namespace) {
        this.namespace = namespace;
        namespace.link(this);
    }

    /**
     * @since 12.0.0
     */
    @SuppressWarnings("unchecked")
    @ApiStatus.Experimental
    public ClassHandle constraint(ReflectiveConstraint constraint) {
        this.constraints.put((Class<ReflectiveConstraint>) constraint.getClass(), constraint);
        return this;
    }

    protected <T extends Class<?>> T checkConstraints(T jvm) {
        for (ReflectiveConstraint constraint : this.constraints.values()) {
            ReflectiveConstraint.Result result = constraint.appliesTo(this, jvm);
            if (result != ReflectiveConstraint.Result.MATCHED) {
                throw ReflectiveConstraintException.create(constraint, result, this, jvm);
            }
        }
        return jvm;
    }

    public abstract ClassHandle asArray(int dimensions);

    public final ClassHandle asArray() {
        return asArray(1);
    }

    public abstract boolean isArray();

    public DynamicClassHandle inner(@Language(value = "Java", suffix = "{}") String declaration) {
        return inner(namespace.classHandle(declaration));
    }

    /**
     * @param handle the handle to put the inner class information in.
     * @param <T>    the type of the class handle.
     * @return the same object as the one provided in the parameter.
     */
    public <T extends DynamicClassHandle> T inner(T handle) {
        Objects.requireNonNull(handle, "Inner handle is null");
        if (this == handle) throw new IllegalArgumentException("Same instance: " + this);
        handle.parent = this;
        namespace.link(this);
        return handle;
    }

    /**
     * The array dimension of this class.
     *
     * @return -1 if this class cannot be found, 0 if not an array, otherwise a positive number.
     */
    public int getDimensionCount() {
        int count = -1;
        Class<?> clazz = reflectOrNull();
        if (clazz == null) return count;

        do {
            clazz = clazz.getComponentType();
            count++;
        } while (clazz != null);

        return count;
    }

    public ReflectiveNamespace getNamespace() {
        return namespace;
    }

    public MethodMemberHandle method() {
        return new MethodMemberHandle(this);
    }

    public MethodMemberHandle method(@Language(value = "Java", suffix = ";") String declaration) {
        return createParser(declaration).parseMethod(method());
    }

    public EnumMemberHandle enums() {
        return new EnumMemberHandle(this);
    }

    public FieldMemberHandle field() {
        return new FieldMemberHandle(this);
    }

    public FieldMemberHandle field(@Language(value = "Java", suffix = ";") String declaration) {
        return createParser(declaration).parseField(field());
    }

    public ConstructorMemberHandle constructor(@Language(value = "Java", suffix = ";") String declaration) {
        return createParser(declaration).parseConstructor(constructor());
    }

    public ConstructorMemberHandle constructor() {
        return new ConstructorMemberHandle(this);
    }

    public ConstructorMemberHandle constructor(Class<?>... parameters) {
        return constructor().parameters(parameters);
    }

    public ConstructorMemberHandle constructor(ClassHandle... parameters) {
        return constructor().parameters(parameters);
    }

    private ReflectionParser createParser(@Language("Java") String declaration) {
        return new ReflectionParser(declaration).imports(this.namespace);
    }

    @Override
    public abstract ClassHandle copy();

    @Override
    public @NotNull ReflectiveHandle<ReflectedObject> jvm() {
        return new ReflectedObjectHandle(() -> ReflectedObject.of(reflect()));
    }
}
