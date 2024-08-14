package com.cryptomorin.xseries.reflection.jvm;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;
import com.cryptomorin.xseries.reflection.parser.ReflectionParser;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A handle for using reflection for {@link Constructor}.
 */
public class ConstructorMemberHandle extends MemberHandle {
    protected ClassHandle[] parameterTypes = new ClassHandle[0];

    public ConstructorMemberHandle(ClassHandle clazz) {
        super(clazz);
    }

    public ConstructorMemberHandle parameters(Class<?>... parameterTypes) {
        this.parameterTypes = Arrays.stream(parameterTypes).map(XReflection::of).toArray(ClassHandle[]::new);
        return this;
    }

    public ConstructorMemberHandle parameters(ClassHandle... parameterTypes) {
        this.parameterTypes = parameterTypes;
        return this;
    }

    @Override
    public MethodHandle reflect() throws ReflectiveOperationException {
        if (isFinal) throw new UnsupportedOperationException("Constructor cannot be final: " + this);
        if (makeAccessible) {
            return clazz.getNamespace().getLookup().unreflectConstructor(reflectJvm());
        } else {
            Class<?>[] parameterTypes = FlaggedNamedMemberHandle.getParameters(this, this.parameterTypes);
            return clazz.getNamespace().getLookup().findConstructor(clazz.unreflect(),
                    MethodType.methodType(void.class, parameterTypes));
        }
    }

    @Override
    public ConstructorMemberHandle signature(String declaration) {
        return new ReflectionParser(declaration).imports(clazz.getNamespace()).parseConstructor(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Constructor<?> reflectJvm() throws ReflectiveOperationException {
        Class<?>[] parameterTypes = FlaggedNamedMemberHandle.getParameters(this, this.parameterTypes);
        return handleAccessible(clazz.unreflect().getDeclaredConstructor(parameterTypes));
    }

    @Override
    public ConstructorMemberHandle clone() {
        ConstructorMemberHandle handle = new ConstructorMemberHandle(clazz);
        handle.parameterTypes = this.parameterTypes;
        handle.isFinal = this.isFinal;
        handle.makeAccessible = this.makeAccessible;
        return handle;
    }

    @Override
    public String toString() {
        String str = this.getClass().getSimpleName() + '{';
        if (makeAccessible) str += "protected/private ";
        str += clazz.toString() + ' ';
        str += '(' + Arrays.stream(parameterTypes).map(ClassHandle::toString).collect(Collectors.joining(", ")) + ')';
        return str + '}';
    }
}
