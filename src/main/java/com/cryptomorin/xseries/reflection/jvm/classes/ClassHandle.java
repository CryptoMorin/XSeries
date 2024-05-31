package com.cryptomorin.xseries.reflection.jvm.classes;

import com.cryptomorin.xseries.reflection.Handle;
import com.cryptomorin.xseries.reflection.jvm.ConstructorMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.FieldMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.MethodMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.ReflectiveNamespace;
import com.cryptomorin.xseries.reflection.parser.ReflectionParser;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public abstract class ClassHandle implements Handle<Class<?>> {
    protected ReflectiveNamespace namespace = new ReflectiveNamespace();

    public abstract ClassHandle asArray(int dimensions);

    public final ClassHandle asArray() {
        return asArray(1);
    }

    public abstract boolean isArray();

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

    public void setNamespace(ReflectiveNamespace namespace) {
        ReflectiveNamespace prev = this.namespace;
        this.namespace = namespace;
        namespace.link(this);
        prev.unlink(this);
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
