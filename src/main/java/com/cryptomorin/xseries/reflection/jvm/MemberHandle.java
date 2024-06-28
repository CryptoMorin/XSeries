package com.cryptomorin.xseries.reflection.jvm;

import com.cryptomorin.xseries.reflection.ReflectiveHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;
import org.intellij.lang.annotations.Language;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 * This class should not be used directly.
 * <p>
 * Any object that is a member of a {@link Class}.
 */
public abstract class MemberHandle implements ReflectiveHandle<MethodHandle> {
    protected boolean makeAccessible, isFinal;
    protected final ClassHandle clazz;

    protected MemberHandle(ClassHandle clazz) {this.clazz = clazz;}

    /**
     * Returns the class associated with this member.
     * This is not meant to be used directly.
     */
    public ClassHandle getClassHandle() {
        return clazz;
    }

    /**
     * If this member is known to be private or a final field.
     */
    public MemberHandle makeAccessible() {
        this.makeAccessible = true;
        return this;
    }

    /**
     * Changes the signature of this handle according to the given java code (see {@link com.cryptomorin.xseries.reflection.parser.ReflectionParser})
     * This overrides the current declaration, but names will be kept and any new names specified in the signature will also be added.
     */
    public abstract MemberHandle signature(@Language("Java") String declaration);

    public abstract MethodHandle reflect() throws ReflectiveOperationException;

    /**
     * It's preferred to use one of the {@link MethodHandle} methods instead.
     * This method should only be used for special cases when a direct JVM object is needed.
     * @see #reflect()
     */
    public abstract <T extends AccessibleObject & Member> T reflectJvm() throws ReflectiveOperationException;

    /**
     * Handles private/final declarations.
     */
    protected <T extends AccessibleObject & Member> T handleAccessible(T accessibleObject) throws ReflectiveOperationException {
        // Package-private classes or private inner classes.
        if (this.makeAccessible || Modifier.isPrivate(accessibleObject.getDeclaringClass().getModifiers()))
            accessibleObject.setAccessible(true);
        return accessibleObject;
    }

    @Override
    public abstract MemberHandle clone();
}
