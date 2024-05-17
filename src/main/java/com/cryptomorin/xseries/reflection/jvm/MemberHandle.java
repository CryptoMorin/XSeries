package com.cryptomorin.xseries.reflection.jvm;

import com.cryptomorin.xseries.reflection.Handle;
import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;

public abstract class MemberHandle implements Handle<MethodHandle> {
    protected boolean makeAccessible, isFinal;
    protected final ClassHandle clazz;
    protected final MethodHandles.Lookup lookup = MethodHandles.lookup();

    protected MemberHandle(ClassHandle clazz) {this.clazz = clazz;}

    public MemberHandle makeAccessible() {
        this.makeAccessible = true;
        return this;
    }

    public final MethodHandle unreflect() {
        try {
            return reflect();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public final MethodHandle reflectOrNull() {
        try {
            return reflect();
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    public abstract MethodHandle reflect() throws ReflectiveOperationException;

    public abstract <T extends AccessibleObject & Member> T reflectJvm() throws ReflectiveOperationException;

    protected <T extends AccessibleObject & Member> T handleAccessible(T accessibleObject) throws ReflectiveOperationException {
        if (this.makeAccessible) accessibleObject.setAccessible(true);
        return accessibleObject;
    }
}
