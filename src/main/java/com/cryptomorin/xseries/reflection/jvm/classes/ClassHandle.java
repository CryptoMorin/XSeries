package com.cryptomorin.xseries.reflection.jvm.classes;

import com.cryptomorin.xseries.reflection.ReflectiveHandle;
import com.cryptomorin.xseries.reflection.ReflectiveNamespace;
import com.cryptomorin.xseries.reflection.jvm.ConstructorMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.FieldMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.MethodMemberHandle;
import com.cryptomorin.xseries.reflection.parser.ReflectionParser;
import org.intellij.lang.annotations.Language;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

/**
 * @see DynamicClassHandle
 * @see StaticClassHandle
 */
public abstract class ClassHandle implements ReflectiveHandle<Class<?>> {
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

    @Nonnull
    public abstract Set<String> getPossibleNames();

    public DynamicClassHandle inner(@Language("Java") String declaration) {
        return inner(namespace.classHandle(declaration));
    }

    /**
     * @param handle the handle to put the inner class information in.
     * @return the same object as the one provided in the parameter.
     * @param <T> the type of the class handle.
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
