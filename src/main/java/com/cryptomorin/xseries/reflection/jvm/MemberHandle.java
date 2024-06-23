package com.cryptomorin.xseries.reflection.jvm;

import com.cryptomorin.xseries.reflection.ReflectiveHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;
import org.intellij.lang.annotations.Language;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

public abstract class MemberHandle implements ReflectiveHandle<MethodHandle> {
    protected boolean makeAccessible, isFinal;
    protected final ClassHandle clazz;

    protected MemberHandle(ClassHandle clazz) {this.clazz = clazz;}

    public ClassHandle getClassHandle() {
        return clazz;
    }

    public MemberHandle makeAccessible() {
        this.makeAccessible = true;
        return this;
    }

    public abstract MemberHandle signature(@Language("Java") String declaration);

    public abstract MethodHandle reflect() throws ReflectiveOperationException;

    public abstract <T extends AccessibleObject & Member> T reflectJvm() throws ReflectiveOperationException;

    protected <T extends AccessibleObject & Member> T handleAccessible(T accessibleObject) throws ReflectiveOperationException {
        // Package-private classes or private inner classes.
        if (this.makeAccessible || Modifier.isPrivate(accessibleObject.getDeclaringClass().getModifiers()))
            accessibleObject.setAccessible(true);
        return accessibleObject;
    }
}
