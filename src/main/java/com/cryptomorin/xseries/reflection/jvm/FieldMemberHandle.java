package com.cryptomorin.xseries.reflection.jvm;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.DynamicClassHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.PackageHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftMapping;
import com.cryptomorin.xseries.reflection.parser.ReflectionParser;
import org.intellij.lang.annotations.Pattern;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Objects;

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

    public FieldMemberHandle getter() {
        this.getter = true;
        return this;
    }

    public FieldMemberHandle asStatic() {
        super.asStatic();
        return this;
    }

    public FieldMemberHandle asFinal() {
        this.isFinal = true;
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
    public FieldMemberHandle clone() {
        FieldMemberHandle handle = new FieldMemberHandle(clazz);
        handle.returnType = this.returnType;
        handle.getter = this.getter;
        handle.isFinal = this.isFinal;
        handle.makeAccessible = this.makeAccessible;
        handle.names.addAll(this.names);
        return handle;
    }

    @Override
    public MethodHandle reflect() throws ReflectiveOperationException {
        Field jvm = reflectJvm();
        if (getter) {
            return clazz.getNamespace().getLookup().unreflectGetter(jvm);
        } else {
            return clazz.getNamespace().getLookup().unreflectSetter(jvm);
        }
    }

    @Override
    public FieldMemberHandle signature(String declaration) {
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
        if (isFinal && isStatic) {
            try {
                int unfinalModifiers = field.getModifiers() & ~Modifier.FINAL;
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

    @SuppressWarnings("unchecked")
    @Override
    public Field reflectJvm() throws ReflectiveOperationException {
        Objects.requireNonNull(returnType, "Return type not specified");
        Objects.requireNonNull(getter, "Not specified whether the method is a getter or setter");
        if (names.isEmpty()) throw new IllegalStateException("No names specified");
        NoSuchFieldException errors = null;
        Field field = null;

        Class<?> clazz = this.clazz.reflect();
        for (String name : this.names) {
            if (field != null) break;
            try {
                field = clazz.getDeclaredField(name);
                if (field.getType() != this.returnType) {
                    throw new NoSuchFieldException("Field named '" + name + "' was found but the types don't match: " + field + " != " + this);
                }
                if (isFinal && !Modifier.isFinal(field.getModifiers())) {
                    throw new NoSuchFieldException("Field named '" + name + "' was found but it's not final: " + field + " != " + this);
                }
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
        if (makeAccessible) str += "protected/private ";
        if (isFinal) str += "final ";
        if (returnType != null) str += returnType.getSimpleName() + ' ';
        str += String.join("/", names);
        return str + '}';
    }
}
