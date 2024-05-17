package com.cryptomorin.xseries.reflection.jvm.classes;

import com.cryptomorin.xseries.reflection.Handle;
import com.cryptomorin.xseries.reflection.jvm.ConstructorMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.FieldMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.MethodMemberHandle;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public abstract class ClassHandle implements Handle<Class<?>> {
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

    public MethodMemberHandle method() {
        return new MethodMemberHandle(this);
    }

    public FieldMemberHandle field() {
        return new FieldMemberHandle(this);
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
