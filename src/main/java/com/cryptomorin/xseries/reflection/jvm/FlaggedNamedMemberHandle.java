package com.cryptomorin.xseries.reflection.jvm;

import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;

/**
 * This class should not be used directly.
 * <p>
 * Any reflective JVM object that has a name and can have modifiers (public, private, final static, etc),
 * like {@link java.lang.reflect.Field} or {@link java.lang.reflect.Method}
 */
public abstract class FlaggedNamedMemberHandle extends NamedMemberHandle {
    protected Class<?> returnType;
    protected boolean isStatic;

    protected FlaggedNamedMemberHandle(ClassHandle clazz) {
        super(clazz);
    }

    public FlaggedNamedMemberHandle asStatic() {
        this.isStatic = true;
        return this;
    }

    public FlaggedNamedMemberHandle returns(Class<?> clazz) {
        this.returnType = clazz;
        return this;
    }

    public FlaggedNamedMemberHandle returns(ClassHandle clazz) {
        this.returnType = clazz.unreflect();
        return this;
    }
}
