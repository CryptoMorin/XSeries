package com.cryptomorin.xseries.reflection.jvm.classes;

import com.cryptomorin.xseries.reflection.Handle;
import com.cryptomorin.xseries.reflection.jvm.ConstructorMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.FieldMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.MethodMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.ReflectiveNamespace;
import com.cryptomorin.xseries.reflection.parser.ReflectionParser;
import org.intellij.lang.annotations.Language;

import java.util.Objects;
import java.util.Set;

public abstract class ClassHandle implements Handle<Class<?>> {
    protected final ReflectiveNamespace namespace;

    protected ClassHandle(ReflectiveNamespace namespace) {
        this.namespace = namespace;
        namespace.link(this);
    }

    public abstract ClassHandle asArray(int dimensions);

    public final ClassHandle asArray() {
        return asArray(1);
    }

    public abstract boolean isArray();

    public abstract Set<String> getPossibleNames();

    public DynamicClassHandle inner(@Language("Java") String declaration) {
        return inner(namespace.classHandle(declaration));
    }

    public <T extends DynamicClassHandle> T inner(T handle) {
        Objects.requireNonNull(handle, "Inner handle is null");
        if (this == handle) throw new IllegalArgumentException("Same instance: " + this);
        handle.parent = this;
        namespace.link(this);
        return handle;
    }

    public int getDimensionCount() {
        int count = -1;
        Class<?> clazz = unreflect();
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

    public MethodMemberHandle method(@Language("Java") String declaration) {
        return createParser(declaration).parseMethod(method());
    }

    private ReflectionParser createParser(@Language("Java") String declaration) {
        this.unreflect(); // Add to namespace.
        return new ReflectionParser(declaration).imports(this.namespace);
    }

    public FieldMemberHandle field() {
        return new FieldMemberHandle(this);
    }

    public FieldMemberHandle field(@Language("Java") String declaration) {
        return createParser(declaration).parseField(field());
    }

    public ConstructorMemberHandle constructor(@Language("Java") String declaration) {
        return createParser(declaration).parseConstructor(constructor());
    }

    public FieldMemberHandle getterField() {
        return field().getter();
    }

    public FieldMemberHandle setterField() {
        return field().setter();
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
}
