package com.cryptomorin.xseries.reflection.jvm;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;

/**
 * This class should not be used directly.
 * <p>
 * Any reflective JVM object that has a name and can have modifiers (public, private, final static, etc),
 * like {@link java.lang.reflect.Field} or {@link java.lang.reflect.Method}
 */
public abstract class FlaggedNamedMemberHandle extends NamedMemberHandle {
    protected ClassHandle returnType;
    protected boolean isStatic;

    protected FlaggedNamedMemberHandle(ClassHandle clazz) {
        super(clazz);
    }

    public FlaggedNamedMemberHandle asStatic() {
        this.isStatic = true;
        return this;
    }

    public FlaggedNamedMemberHandle returns(Class<?> clazz) {
        this.returnType = XReflection.of(clazz);
        return this;
    }

    public FlaggedNamedMemberHandle returns(ClassHandle clazz) {
        this.returnType = clazz;
        return this;
    }

    public static Class<?>[] getParameters(Object owner, ClassHandle[] parameterTypes) {
        Class<?>[] classes = new Class[parameterTypes.length];
        int i = 0;
        for (ClassHandle parameterType : parameterTypes) {
            try {
                classes[i++] = parameterType.unreflect();
            } catch (Throwable ex) {
                throw XReflection.throwCheckedException(new ReflectiveOperationException(
                        "Unknown parameter " + parameterType + " for " + owner, ex
                ));
            }
        }
        return classes;
    }

    protected Class<?> getReturnType() {
        try {
            return this.returnType.unreflect();
        } catch (Throwable ex) {
            throw XReflection.throwCheckedException(new ReflectiveOperationException(
                    "Unknown return type " + returnType + " for " + this
            ));
        }
    }
}
