package com.cryptomorin.xseries.reflection.jvm;

import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.PackageHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftMapping;
import org.intellij.lang.annotations.Pattern;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Any reflective JVM object that has a name, like {@link java.lang.reflect.Field} or {@link java.lang.reflect.Method}
 * the other being {@link java.lang.reflect.Constructor} which doesn't have a name.
 */
public abstract class NamedMemberHandle extends MemberHandle {
    protected Class<?> returnType;
    protected boolean isStatic;
    protected final Set<String> names = new HashSet<>(5);

    protected NamedMemberHandle(ClassHandle clazz) {
        super(clazz);
    }

    public NamedMemberHandle map(MinecraftMapping mapping, @Pattern(PackageHandle.JAVA_IDENTIFIER_PATTERN) String name) {
        this.names.add(name);
        return this;
    }

    public NamedMemberHandle asStatic() {
        this.isStatic = true;
        return this;
    }

    public NamedMemberHandle returns(Class<?> clazz) {
        this.returnType = clazz;
        return this;
    }

    public NamedMemberHandle returns(ClassHandle clazz) {
        this.returnType = clazz.unreflect();
        return this;
    }

    public MemberHandle named(@Pattern(PackageHandle.JAVA_IDENTIFIER_PATTERN) String... names) {
        this.names.addAll(Arrays.asList(names));
        return this;
    }
}
