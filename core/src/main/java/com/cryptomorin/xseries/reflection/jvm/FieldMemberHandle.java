/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2026 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.cryptomorin.xseries.reflection.jvm;

import com.cryptomorin.xseries.reflection.ReflectiveHandle;
import com.cryptomorin.xseries.reflection.XAccessFlag;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.DynamicClassHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.PackageHandle;
import com.cryptomorin.xseries.reflection.jvm.objects.ReflectedObject;
import com.cryptomorin.xseries.reflection.jvm.objects.ReflectedObjectHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftMapping;
import com.cryptomorin.xseries.reflection.parser.ReflectionParser;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A handle for using reflection for {@link Field}.
 */
public class FieldMemberHandle extends FlaggedNamedMemberHandle {
    public static final MethodHandle MODIFIERS_FIELD;

    public static final DynamicClassHandle VarHandle = XReflection.classHandle()
            .inPackage("java.lang.invoke")
            .named("VarHandle");

    //     public final native
    //     @MethodHandle.PolymorphicSignature
    //     @IntrinsicCandidate
    //     void set(Object... args);
    public static final MethodHandle VAR_HANDLE_SET = VarHandle.method()
            .named("set").returns(void.class).parameters(Object[].class).reflectOrNull();
    private static final Object MODIFIERS_VAR_HANDLE;

    static {
        Object modVarHandle = null;
        MethodHandle modifierFieldJvm = null;

        try {
            modifierFieldJvm = XReflection.of(Field.class).field().setter()
                    .named("modifiers").returns(int.class).unreflect();
        } catch (Exception ignored) {
            // Java 18+
        }

        try {
            VarHandle.reflect();

            // MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
            MethodHandle PRIVATE_LOOKUP_IN = XReflection.of(MethodHandles.class).method()
                    .named("privateLookupIn").returns(MethodHandles.Lookup.class).parameters(Class.class, MethodHandles.Lookup.class).reflect();
            // lookup.findVarHandle(Field.class, "modifiers", int.class);
            MethodHandle FIND_VAR_HANDLE = VarHandle.method()
                    .named("findVarHandle").returns(MethodHandles.Lookup.class).parameters(Class.class, String.class, Class.class).reflect();

            MethodHandles.Lookup lookup = (MethodHandles.Lookup) PRIVATE_LOOKUP_IN.invoke(Field.class, MethodHandles.lookup());
            FIND_VAR_HANDLE.invoke(lookup, Field.class, "modifiers", int.class);
        } catch (Throwable ignored) {
        }

        MODIFIERS_VAR_HANDLE = modVarHandle;
        MODIFIERS_FIELD = modifierFieldJvm;
    }

    protected Boolean getter;

    public FieldMemberHandle(ClassHandle clazz) {
        super(clazz);
    }

    public boolean isGetter() {
        if (getter == null)
            throw new IllegalStateException("Not specified whether the field handle is a getter or setter");
        return getter;
    }

    public FieldMemberHandle getter() {
        this.getter = true;
        return this;
    }

    public FieldMemberHandle asStatic() {
        super.asStatic();
        return this;
    }

    public FieldMemberHandle asFinal() {
        this.accessFlags.add(XAccessFlag.FINAL);
        return this;
    }

    public FieldMemberHandle makeAccessible() {
        super.makeAccessible();
        return this;
    }

    public FieldMemberHandle setter() {
        this.getter = false;
        return this;
    }

    @Override
    public FieldMemberHandle returns(Class<?> clazz) {
        super.returns(clazz);
        return this;
    }

    @Override
    public FieldMemberHandle returns(ClassHandle clazz) {
        super.returns(clazz);
        return this;
    }

    @Override
    public FieldMemberHandle copy() {
        FieldMemberHandle handle = new FieldMemberHandle(clazz);
        handle.returnType = this.returnType;
        handle.getter = this.getter;
        handle.accessFlags.addAll(this.accessFlags);
        handle.names.addAll(this.names);
        return handle;
    }

    @Override
    public MethodHandle reflect() throws ReflectiveOperationException {
        Objects.requireNonNull(getter, "Not specified whether the field handle is a getter or setter");
        Field jvm = reflectJvm();

        if (getter) {
            return clazz.getNamespace().getLookup().unreflectGetter(jvm);
        } else {
            return clazz.getNamespace().getLookup().unreflectSetter(jvm);
        }
    }

    @Override
    public boolean exists() {
        try {
            // Avoid checking for getter property.
            // This helps when using this handle easier with AggregateReflectiveHandle.
            reflectJvm();
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    @Override
    public FieldMemberHandle signature(@Language(value = "Java", suffix = ";") String declaration) {
        return new ReflectionParser(declaration).imports(clazz.getNamespace()).parseField(this);
    }

    public FieldMemberHandle map(MinecraftMapping mapping, @Pattern(PackageHandle.JAVA_IDENTIFIER_PATTERN) String name) {
        super.map(mapping, name);
        return this;
    }

    public FieldMemberHandle named(@Pattern(PackageHandle.JAVA_IDENTIFIER_PATTERN) String... names) {
        super.named(names);
        return this;
    }

    @Override
    protected <T extends AccessibleObject & Member> T handleAccessible(T field) throws ReflectiveOperationException {
        field = super.handleAccessible(field);
        if (field == null) return null;
        if ((getter != null && !getter) && accessFlags.contains(XAccessFlag.FINAL) && accessFlags.contains(XAccessFlag.STATIC)) {
            try {
                int unfinalModifiers = XAccessFlag.FINAL.remove(field.getModifiers());
                if (MODIFIERS_VAR_HANDLE != null) {
                    VAR_HANDLE_SET.invoke(MODIFIERS_VAR_HANDLE, field, unfinalModifiers);
                } else if (MODIFIERS_FIELD != null) {
                    MODIFIERS_FIELD.invoke(field, unfinalModifiers);
                } else {
                    throw new IllegalAccessException("Current Java version doesn't support modifying final fields. " + this);
                }
            } catch (Throwable e) {
                throw new ReflectiveOperationException("Cannot unfinal field " + this, e);
            }
        }
        return field;
    }

    @Nullable
    public Object get(Object instance) {
        try {
            return getter().reflectJvm().get(instance);
        } catch (ReflectiveOperationException ex) {
            throw XReflection.throwCheckedException(ex);
        }
    }

    @Nullable
    public Object getStatic() {
        try {
            return asStatic().getter().reflectJvm().get(null);
        } catch (ReflectiveOperationException ex) {
            throw XReflection.throwCheckedException(ex);
        }
    }

    @Override
    public @NotNull ReflectiveHandle<ReflectedObject> jvm() {
        return new ReflectedObjectHandle(() -> ReflectedObject.of(reflectJvm()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Field reflectJvm() throws ReflectiveOperationException {
        Objects.requireNonNull(returnType, "Return type not specified");
        if (names.isEmpty()) throw new IllegalStateException("No names specified");
        NoSuchFieldException errors = null;
        Field field = null;

        Class<?> clazz = this.clazz.reflect();
        Class<?> returnType = getReturnType();

        for (String name : this.names) {
            if (field != null) break;
            try {
                field = clazz.getDeclaredField(name);
                if (field.getType() != returnType) {
                    throw new NoSuchFieldException("Field named '" + name + "' was found but the types don't match: " + field + " != " + this);
                }
                // This might be a bit too strict?
                // if (this.accessFlags.contains(XAccessFlag.FINAL) && !Modifier.isFinal(field.getModifiers())) {
                //     throw new NoSuchFieldException("Field named '" + name + "' was found but it's not final: " + field + " != " + this);
                // }
            } catch (NoSuchFieldException ex) {
                field = null;
                if (errors == null) errors = new NoSuchFieldException("None of the fields were found for " + this);
                errors.addSuppressed(ex);
            }
        }

        if (field == null) throw XReflection.relativizeSuppressedExceptions(errors);
        return handleAccessible(field);
    }

    @Override
    public String toString() {
        String str = this.getClass().getSimpleName() + '{';
        str += accessFlags.stream().map(x -> x.name().toLowerCase(Locale.ENGLISH)).collect(Collectors.joining(" "));
        if (returnType != null) str += returnType + " ";
        str += String.join("/", names);
        return str + '}';
    }
}
