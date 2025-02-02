/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Crypto Morin
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

package com.cryptomorin.xseries.reflection.asm;

import com.cryptomorin.xseries.reflection.XAccessFlag;
import com.cryptomorin.xseries.reflection.jvm.FieldMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.objects.ReflectedObject;
import com.cryptomorin.xseries.reflection.proxy.ClassOverloadedMethods;
import com.cryptomorin.xseries.reflection.proxy.OverloadedMethod;
import com.cryptomorin.xseries.reflection.proxy.ReflectiveProxyObject;
import com.cryptomorin.xseries.reflection.proxy.processors.MappedType;
import com.cryptomorin.xseries.reflection.proxy.processors.ProxyMethodInfo;
import com.cryptomorin.xseries.reflection.proxy.processors.ReflectiveAnnotationProcessor;
import com.google.common.collect.Streams;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

import static org.objectweb.asm.commons.Method.getMethod;

/**
 * This class should not be accessed directly, use {@link com.cryptomorin.xseries.reflection.XReflection#proxify(Class)} instead.
 * <p>
 * This is basically similar to <a href="https://github.com/EsotericSoftware/reflectasm">ReflectASM</a>
 * (with another <a href="https://github.com/hyee/ReflectASM">updated version of it by hyee</a>), except that
 * instead of relying on the same reflection-based API where members are accessed by their name in forms of strings
 * and parameters are passed to get the object, we use actual interfaces which increases readability and performance.
 * <p>
 * Also something similar, specifically with Minecraft in mind was created named
 * <a href="https://github.com/AngryCarrot789/REghZyASMWrappers/tree/master">REghZyASMWrappers</a>.
 * <p>
 * TODO We could also
 * <a href="https://bukkit.org/threads/tutorial-extreme-beyond-reflection-asm-replacing-loaded-classes.99376/">replace loaded classes</a>,
 * but that sounds too unreliable.
 * That means we can't really use real constructors, fields or static members as it'd require replacing
 * the caller with a method, and replacing classes isn't really something most plugins are willing to setup.
 * But using ASM, we can use non-final abstract/non-interface classes.
 * <p>
 * I haven't confirmed this. But it seems like Paper's remapper also has a system that attempts to
 * optimize some reflection access by generating a proxy for them?
 * <p>
 * You can see the a class that similarly resembles the generated class in the test sources named {@code ASMGeneratedSample}.
 * We could also take inspiration from {@code java.lang.invoke.InnerClassLambdaMetafactory} which generates something similar.
 * <p>
 * TODO Cleanup this class, it's really crowded in here.
 *
 * @see ReflectiveProxyObject
 * @see com.cryptomorin.xseries.reflection.proxy.ReflectiveProxy
 * @see com.cryptomorin.xseries.reflection.XReflection#proxify(Class)
 * @since 14.0.0
 */
@SuppressWarnings("JavadocLinkAsPlainText")
@ApiStatus.Internal
public final class XReflectASM<T extends ReflectiveProxyObject> extends ClassVisitor {
    // https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.checkcast
    // JVM Operand stack notation meaning:
    // Operand Stack
    //   ..., x, y, z →
    //   ..., rx, ry, rz
    //
    // x, y, z are popped from the stack and used for the instruction.
    // rx, ry, rz are results of the instruction that are pushed to the stack.
    // -----------------------------------------------------------------------
    // iconst_<i>
    // Operand Stack
    //   ..., →
    //   ..., <i>
    //
    // An instruction with no operand, gives back 1 result.
    // -----------------------------------------------------------------------
    // invokevirtual
    // Operand Stack
    //   ..., objectref, [arg1, [arg2 ...]] →
    //   ...
    //
    // An instruction with many operands, but no results.

    /**
     * Generating with the latest format might yield better performance in some cases?
     * We can't use {@code ClassFileFormatVersion.latest().major()}, it's only avaible
     * on Java 20.
     * Perhaps it should match the templateClass's class file format version?
     */
    private static final int JAVA_VERSION = ASMVersion.USED_JAVA_FILE_FORMAT;

    /**
     * Any reason for us to use a lower version? We won't use experimental versions though.
     */
    private static final int ASM_VERSION = ASMVersion.USED_ASM_OPCODE_VERSION;

    // private static final String ASM_ACESSOR = Type.getInternalName(ReflectiveProxyObject.class);

    /**
     * The name of all constructors.
     */
    private static final String CONSTRUCTOR_NAME = "<init>";

    /**
     * The name of {@code static { }} blocks, which can be merged using
     * {@link org.objectweb.asm.commons.StaticInitMerger} to a single one.
     */
    private static final String STATIC_BLOCK = "<clinit>";

    private static final String INSTANCE_FIELD = "instance";

    private static final String METHOD_HANDLE_PREFIX = "H_";

    /**
     * In binary format so relocation can happen
     */
    private static final String XSERIES_ANNOTATIONS = 'L' + "com.cryptomorin.xseries.reflection.proxy.annotations".replace('.', '/');

    /**
     * We must use a constant, known string for our generated classes, because we can reference multiple
     * proxy objects for parameter or return types.
     * The full path of the class is in the following format: {@code <originalPackage>.generated.<originalName>[SUFFIX]}
     * which can be formatted using {@link #getGeneratedClassPath(Class)}.
     */
    private static final String
            GENERATED_CLASS_PACKAGE_PREFIX = "generated",
            GENERATED_CLASS_SUFFIX = "_XSeriesGen_" + ASM_VERSION + '_' + JAVA_VERSION;

    /**
     * CHECKCAST instructions can also be dropped when using this class.
     * <p>
     * Groovy:
     * https://github.com/apache/groovy/blob/183b8fbf0248a2dceffbba684e50c3c2c060e46c/src/main/org/codehaus/groovy/reflection/SunClassLoader.java#L70
     * https://github.com/apache/groovy/pull/1932/commits/0cfa37331f23434e03e1544d0c25dcf153f174eb#diff-5d522ba8f58ed93edb17610539e9e521576fd969f52b4c286c7a884c06ae97ed
     * <p>
     * Lombok:
     * https://github.com/projectlombok/lombok/issues/2681
     * https://github.com/projectlombok/lombok/blob/master/src/core/lombok/javac/apt/LombokProcessor.java
     * https://github.com/projectlombok/lombok/blob/master/src/launch/lombok/launch/ShadowClassLoader.java
     * https://github.com/projectlombok/lombok/blob/master/src/eclipseAgent/lombok/eclipse/agent/EclipseLoaderPatcherTransplants.java
     * <p>
     * Powermock:
     * https://github.com/powermock/powermock/issues/901
     * <p>
     * magic-accessor:
     * https://github.com/nnym/magic-accessor
     * https://github.com/nnym/magic-accessor/blob/master/source/net/auoeke/magic/Definer.java
     * <p>
     * Subclasses are bypassed?
     * https://github.com/andreho/magic/blob/master/src/main/java/sun/reflect/Magic.java
     * <p>
     * AdvisedTesting:
     * https://github.com/advisedtesting/AdvisedTesting/blob/master/AdviseStaticEvictingClassloader/src/main/java/com/github/advisedtesting/classloader/RunInClassLoaderInterceptor.java#L81
     * <p>
     * Apache Druid:
     * Java License violation? Bridge method by defining a fake class inside sun.reflect package.
     * https://github.com/apache/druid/pull/4079/files/35341a56bbb62a7f2c9bacad308b757b6d185c2c#diff-0c6ffa130344eb9cedd7f34453ced55855416a44f9bc3cc2241e33badfbbee88
     * https://www.oracle.com/downloads/licenses/binary-code-license.html
     * <p>
     * Using Burningwave Core?
     * https://github.com/burningwave/core
     * https://burningwave.github.io/jvm-driver/
     * <p>
     * JEPs:
     * <a href="https://openjdk.org/jeps/416">JEP 416: Reimplement Core Reflection with Method Handles</a>
     * It was removed in JDK 22
     * https://github.com/openjdk/jdk/pull/21571
     * All started because of project Jigsaw https://openjdk.org/projects/jigsaw/
     */
    @SuppressWarnings("unused")
    private static final String MAGIC_ACCESSOR_IMPL;

    static {
        String magicAccessor;
        try {
            Class.forName("sun.reflect.MagicAccessorImpl");
            magicAccessor = "sun/reflect/MagicAccessorImpl";
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("jdk.internal.reflect.MagicAccessorImpl");
                magicAccessor = "jdk/internal/reflect/MagicAccessorImpl";
            } catch (ClassNotFoundException ex) {
                IllegalStateException state = new IllegalStateException("Cannot find MagicAccessorImpl class");
                state.addSuppressed(e);
                state.addSuppressed(ex);
                throw state;
            }
        }

        MAGIC_ACCESSOR_IMPL = magicAccessor;
    }

    /**
     * Read {@link #GENERATED_CLASS_PACKAGE_PREFIX} for more info.
     */
    private static String getGeneratedClassPath(Class<?> clazz) {
        return clazz.getPackage().getName() + '.' + GENERATED_CLASS_PACKAGE_PREFIX + '.' + clazz.getSimpleName() + GENERATED_CLASS_SUFFIX;
    }

    /**
     * Find a way to use {@link #MAGIC_ACCESSOR_IMPL}
     */
    private static final String SUPER_CLASS = Type.getInternalName(Object.class);

    private static final ASMClassLoader CLASS_LOADER = new ASMClassLoader();

    private static final Map<Class<?>, XReflectASM<?>> PROCESSED = new IdentityHashMap<>();

    private final ClassWriter classWriter;
    private final ClassReader classReader;

    private final Class<T> templateClass;
    private final Class<?> targetClass;
    private final Type templateClassType, targetClassType, generatedClassType;
    private final String generatedClassName, generatedClassPath;

    private Class<?> loaded;
    private byte[] bytecode;

    private final ClassOverloadedMethods<ASMProxyInfo> mapped;

    private static final class ASMProxyInfo {
        private final ProxyMethodInfo info;
        private final String methodHandleName;

        private ASMProxyInfo(ProxyMethodInfo info, String methodHandleName) {
            this.info = info;
            this.methodHandleName = methodHandleName;
        }

        private boolean isInaccessible() {
            return methodHandleName != null;
        }
    }

    private static String descriptorProcessor(ProxyMethodInfo info) {
        Type rType = Type.getType(info.rType.synthetic);
        Type[] pTypes = Arrays.stream(info.pTypes).map(x -> Type.getType(x.synthetic)).toArray(Type[]::new);
        return Type.getMethodDescriptor(rType, pTypes);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ReflectiveProxyObject> XReflectASM<T> proxify(Class<T> interfaceClass) {
        {
            XReflectASM<?> cache = PROCESSED.get(interfaceClass);
            if (cache != null) return (XReflectASM<T>) cache;
        }

        ReflectiveAnnotationProcessor processor = new ReflectiveAnnotationProcessor(interfaceClass);
        processor.process(XReflectASM::descriptorProcessor);

        XReflectASM<T> asm = new XReflectASM<>(interfaceClass, processor.getTargetClass(), processor.getMapped());
        PROCESSED.put(interfaceClass, asm);

        processor.loadDependencies(PROCESSED::containsKey); // Generated all classes that this class requires.
        asm.generate();

        return asm;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public T create() {
        Class<?> proxified = loadClass();
        try {
            Optional<Constructor<?>> ctor = Arrays.stream(proxified.getDeclaredConstructors())
                    .filter(x -> XAccessFlag.PUBLIC.isSet(x.getModifiers()) && x.getParameterCount() == 1)
                    .findFirst();
            if (!ctor.isPresent())
                throw new IllegalStateException("Cannot find appropriate constructor for " + Arrays.toString(proxified.getDeclaredConstructors()));

            return (T) ctor.get().newInstance(new Object[]{null});
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Couldn't initialize proxified ASM class: " + templateClass + " -> " + proxified, e);
        }
    }

    public void verify(boolean silent) {
        generate();
        PrintWriter pw = new PrintWriter(silent ? System.err : System.out);
        ASMAnalyzer.verify(new ClassReader(bytecode), XReflectASM.class.getClassLoader(), !silent, pw);
    }

    public void writeToFile(Path folder) {
        generate();
        try {
            Files.write(folder.resolve(generatedClassName + ".class"), bytecode, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write generated file", e);
        }
    }

    public XReflectASM(Class<T> templateClass, Class<?> targetClass, ClassOverloadedMethods<ProxyMethodInfo> mapped) {
        super(ASM_VERSION);
        this.mapped = mapTypes(mapped);

        try {
            this.classReader = new ClassReader(templateClass.getName());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read class: " + templateClass, e);
        }
        this.cv = this.classWriter = new ClassWriter(this.classReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        this.templateClass = templateClass;
        this.templateClassType = Type.getType(templateClass);

        this.targetClass = targetClass;
        this.targetClassType = Type.getType(targetClass);

        this.generatedClassName = templateClass.getSimpleName() + GENERATED_CLASS_SUFFIX;
        this.generatedClassPath = getGeneratedClassPath(templateClass);
        this.generatedClassType = Type.getType('L' + generatedClassPath.replace('.', '/') + ';');
    }

    public void generate() {
        if (bytecode != null) return;
        this.classReader.accept(this, 0); // ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG
        this.bytecode = this.classWriter.toByteArray();
    }

    public byte[] getBytecode() {
        return bytecode;
    }

    private static boolean shouldRemoveAnnotation(String descriptor) {
        return descriptor.startsWith(XSERIES_ANNOTATIONS);
    }

    private static ClassOverloadedMethods<ASMProxyInfo> mapTypes(ClassOverloadedMethods<ProxyMethodInfo> mapped) {
        OverloadedMethod.Builder<ASMProxyInfo> asmMapped = new OverloadedMethod.Builder<>(x -> descriptorProcessor(x.info));
        for (Map.Entry<String, OverloadedMethod<ProxyMethodInfo>> overloads : mapped.mappings().entrySet()) {
            Collection<ProxyMethodInfo> overloaded = overloads.getValue().getOverloads();

            int overloadIndex = 0;
            for (ProxyMethodInfo overload : overloaded) {
                ReflectedObject jvm = overload.handle.jvm().unreflect();
                if (!jvm.accessFlags().contains(XAccessFlag.PUBLIC)) {
                    String name;
                    switch (jvm.type()) {
                        case CONSTRUCTOR:
                            name = "$init$" + (overloaded.size() == 1 ? "" : "_" + overloadIndex++);
                            break;
                        case FIELD:
                            FieldMemberHandle field = (FieldMemberHandle) overload.handle.unwrap();
                            name = jvm.name() + '_' + (field.isGetter() ? "getter" : "setter");
                            break;
                        case METHOD:
                            name = jvm.name() + (overloaded.size() == 1 ? "" : "_" + overloadIndex++);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected JVM type: " + jvm);
                    }

                    asmMapped.add(new ASMProxyInfo(overload, name), overloads.getKey());
                } else {
                    asmMapped.add(new ASMProxyInfo(overload, null), overloads.getKey());
                }
            }
        }

        return asmMapped.build();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (shouldRemoveAnnotation(descriptor)) return null;
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        if (shouldRemoveAnnotation(descriptor)) return null;
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        classWriter.visit(
                /* version    */ JAVA_VERSION,
                /* access     */ Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER | Opcodes.ACC_FINAL,
                /* name       */ generatedClassType.getInternalName(),
                /* signature  */ null,
                /* supername  */ SUPER_CLASS,
                /* interfaces */ new String[]{templateClassType.getInternalName()}
        );
    }

    @Override
    public void visitSource(String source, String debug) {
        classWriter.visitSource(generatedClassName + ".java", null);

        boolean needsStaticInit = false;
        for (OverloadedMethod<ASMProxyInfo> method : this.mapped.mappings().values()) {
            for (ASMProxyInfo overload : method.getOverloads()) {
                if (overload.isInaccessible()) {
                    needsStaticInit = true;
                    writeMethodHandleField(overload.methodHandleName);
                }
            }
        }
        writePrivateFinalField(false, INSTANCE_FIELD, targetClass);

        if (needsStaticInit) initStaticFields();
        writeConstructor(); // initialize instance and private/final reflection accessors
    }

    @Override
    public void visitEnd() {
        generateGetTargetClass();
        generateIsInstance();
        generateNewArraySingleDim();
        generateNewArrayMultiDim();
        generateInstance();
        generateBindTo();

        generateEquals();
        generateHashCode();

        generateToString(); // For debugging purposes.
        super.visitEnd();
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        // Replace all field calls with a generated getter/setter synthetic method.
        // We can't use this without replacing the caller class with manipulated bytecode...
        // noinspection ConstantValue
        if (true) {
            throw new UnsupportedOperationException("Raw fields are not supported");
        }

        Type type = Type.getType(descriptor);
        // Also check if method exists or not, rename

        { // Setter
            String methodName = "set" + name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
            MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, methodName, "(" + type + ")V", null, null);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(type.getOpcode(Opcodes.ILOAD), 1);
            mv.visitFieldInsn(Opcodes.PUTFIELD, templateClassType.getInternalName(), name, descriptor);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
        }
        { // Getter
            String methodName = "get" + name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
            MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, methodName, "()" + descriptor, null, null);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, templateClassType.getInternalName(), name, descriptor);
            mv.visitInsn(templateClassType.getOpcode(Opcodes.IRETURN));
            mv.visitMaxs(0, 0);
        }

        return null;
    }

    @SuppressWarnings("ReturnOfInnerClass")
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (CONSTRUCTOR_NAME.equals(name) || STATIC_BLOCK.equals(name)) return null;

        ASMProxyInfo handle = mapped.get(name, () -> descriptor, true);
        if (handle == null) return null;

        return new MethodRewriter(handle, access, name, descriptor, signature, exceptions);
    }

    /**
     * Computes the size of the arguments and of the return value of a method
     * where {@code long} and {@code double} count as two.
     * It also accounts for the implied {@code this} parameter if it's a member method.
     */
    private static int magicMaxs(String descriptor, boolean staticMethod) {
        return Type.getArgumentsAndReturnSizes(descriptor) >> 2 + (staticMethod ? -1 : 0);
    }

    public static Type getType(String className) {
        return Type.getType('L' + className.replace('.', '/') + ';');
    }

    private static Type[] convert(MappedType[] pTypes) {
        return Arrays.stream(pTypes).map(x -> Type.getType(x.real)).toArray(Type[]::new);
    }

    private final class MethodRewriter extends MethodVisitor {
        private final ASMProxyInfo handle;
        private final GeneratorAdapter adapter;
        private final String descriptor;

        MethodRewriter(ASMProxyInfo handle, int access, String name, String descriptor, String signature, String[] exceptions) {
            super(ASM_VERSION, XReflectASM.super.visitMethod(XAccessFlag.ABSTRACT.remove(access), name, descriptor, signature, exceptions));
            this.handle = handle;
            this.adapter = new GeneratorAdapter(mv, access, name, descriptor);
            this.descriptor = descriptor;
            generateCode();
        }

        private void generateCode() {
            adapter.visitCode();

            // We shouldn't generate this.instance null checks, it'll add extra bytecode,
            // we will just let the invoker handler it.

            ReflectedObject.Type type;
            boolean isInterface = targetClass.isInterface();
            boolean isStatic;
            String name;

            try {
                ReflectedObject obj = handle.info.handle.jvm().reflect();
                type = obj.type();
                name = obj.name();
                isStatic = obj.accessFlags().contains(XAccessFlag.STATIC);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }

            Label label0 = new Label();
            adapter.visitLabel(label0);

            // We only disallow constructors from being invoked in instance objects, static
            // members are fine to be used from instance objects, just like standard Java.
            if (type == ReflectedObject.Type.CONSTRUCTOR) {
                adapter.loadThis();
                getInstance(adapter);

                Label label1 = new Label();
                adapter.ifNull(label1);
                // if (this.instance != null) {
                adapter.throwException(Type.getType(UnsupportedOperationException.class), "Constructor method must be called from the factory object, not on an instance");
                // } else {
                adapter.visitLabel(label1);
                // }
            }

            Type syntheticReturnType, realReturnType; // If needsConversion is true, what type should we convert into?
            boolean needsConversion; // Should the origial type be converted or returned as it is?
            MappedType rType = handle.info.rType;
            if (rType.isDifferent()) {
                if (rType.synthetic.isAssignableFrom(rType.real)) {
                    syntheticReturnType = realReturnType = null;
                    needsConversion = false;
                } else if (ReflectiveProxyObject.class.isAssignableFrom(rType.synthetic)) {
                    needsConversion = true;
                    realReturnType = Type.getType(rType.real);
                    syntheticReturnType = getType(getGeneratedClassPath(rType.synthetic));
                    adapter.newInstance(syntheticReturnType);
                    adapter.dup();
                } else {
                    throw new VerifyError("Cannot convert return type "
                            + rType.synthetic + " to " + rType.real
                            + " in proxy method " + handle.info.interfaceMethod);
                }
            } else {
                syntheticReturnType = realReturnType = null;
                needsConversion = false;
            }

            if (handle.isInaccessible()) {
                adapter.getStatic(generatedClassType, METHOD_HANDLE_PREFIX + handle.methodHandleName, Type.getType(MethodHandle.class));
            }

            // Load the object to call the method on.
            if (type == ReflectedObject.Type.CONSTRUCTOR) {
                if (!handle.isInaccessible()) {
                    adapter.newInstance(targetClassType);
                    adapter.dup();
                }
            } else if (!isStatic) {
                adapter.loadThis();
                getInstance(adapter);
            }

            // Push parameter local variables to operand stack.
            int operandIndex = 1; // +1 for "this"
            Type[] argumentTypes = adapter.getArgumentTypes();
            for (int i = 0; i < argumentTypes.length; ++i) {
                Type argumentType = argumentTypes[i];
                MappedType pType = handle.info.pTypes[i];

                if (pType.isDifferent()) {
                    if (!ReflectiveProxyObject.class.isAssignableFrom(pType.synthetic)) {
                        throw new VerifyError("Cannot convert parameter type "
                                + pType.synthetic + " to " + pType.real
                                + " in proxy method " + handle.info.interfaceMethod);
                    }

                    // instance.method(arg1, arg2, arg3.instance(), arg4, ...);
                    //                             ^^^^^^^^^^^^^^^
                    adapter.visitVarInsn(argumentType.getOpcode(Opcodes.ILOAD), operandIndex);

                    // We can't optimize this to INVOKEVIRTUAL because we don't know about the other generated class at this point.
                    // We technically can since this only requires the internal name of it, however we're not going to risk
                    // any class loader issues. Optimizing to direct class will also remove the need for CHECKCAST below.
                    adapter.invokeInterface(
                            Type.getType(ReflectiveProxyObject.class),
                            getMethod("Object instance()")
                    );

                    // instance.method(arg1, arg2, (TargetClass) arg3.instance(), arg4, ...);
                    //                             ^^^^^^^^^^^^^
                    adapter.checkCast(Type.getType(pType.real));
                } else {
                    // Normal parameter, directly load as an operand.
                    adapter.visitVarInsn(argumentType.getOpcode(Opcodes.ILOAD), operandIndex);
                }

                operandIndex += argumentType.getSize();
            }


            // For private methods/fields/constructors we use MethodHandles.
            // Polymorphic signature baby! We can do whatever we want! No casts needed.
            // We also use invokeExact instead of invoke, we will do all needed conversions ourselves.
            // Since we're generating a bytecode ourselves, we don't need to generate a try-catch for
            // the MethodHandle invoke methods either, the class verifier allows that.
            String invokeExact = "invokeExact";
            switch (type) {
                case METHOD:
                    if (handle.isInaccessible()) {
                        adapter.invokeVirtual(Type.getType(MethodHandle.class),
                                new org.objectweb.asm.commons.Method(invokeExact,
                                        Type.getType(handle.info.rType.real),
                                        Streams.concat(
                                                isStatic ? Stream.of() : Stream.of(targetClassType),
                                                Arrays.stream(handle.info.pTypes).map(x -> Type.getType(x.real))
                                        ).toArray(Type[]::new)));
                    } else {
                        adapter.visitMethodInsn(
                                isStatic ? Opcodes.INVOKESTATIC : (isInterface ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL),
                                targetClassType.getInternalName(),
                                name,
                                Type.getMethodDescriptor(Type.getType(rType.real), convert(handle.info.pTypes)),
                                isInterface
                        );
                    }
                    break;
                case FIELD:
                    boolean isSetter = argumentTypes.length != 0;
                    Type fieldDescriptor;
                    if (argumentTypes.length != 0) {
                        fieldDescriptor = adapter.getArgumentTypes()[0];
                    } else {
                        fieldDescriptor = adapter.getReturnType();
                    }

                    if (handle.isInaccessible()) {
                        List<Type> parameters = new ArrayList<>(3);
                        if (!isStatic) parameters.add(targetClassType);
                        if (isSetter) parameters.add(Type.getType(handle.info.pTypes[0].real));

                        adapter.invokeVirtual(Type.getType(MethodHandle.class),
                                new org.objectweb.asm.commons.Method(invokeExact,
                                        Type.getType(handle.info.rType.real),
                                        parameters.toArray(new Type[0])
                                ));
                    } else {
                        int fieldCode;
                        if (isSetter) {
                            if (isStatic) fieldCode = Opcodes.PUTSTATIC;
                            else fieldCode = Opcodes.PUTFIELD;
                        } else {
                            if (isStatic) fieldCode = Opcodes.GETSTATIC;
                            else fieldCode = Opcodes.GETFIELD;
                        }

                        adapter.visitFieldInsn(
                                /* opcode     */ fieldCode,
                                /* owner      */ targetClassType.getInternalName(),
                                /* name       */ name,
                                /* descriptor */ fieldDescriptor.getDescriptor()
                        );
                    }
                    break;
                case CONSTRUCTOR:
                    if (handle.isInaccessible()) {
                        adapter.invokeVirtual(Type.getType(MethodHandle.class),
                                new org.objectweb.asm.commons.Method(invokeExact, targetClassType, convert(handle.info.pTypes)));
                    } else {
                        adapter.visitMethodInsn(
                                Opcodes.INVOKESPECIAL,
                                targetClassType.getInternalName(),
                                CONSTRUCTOR_NAME,
                                Type.getMethodDescriptor(Type.getType(void.class), argumentTypes),
                                false
                        );
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown ReflectedObject type: " + type);
            }

            if (needsConversion) {
                // return new OtherGeneratedProxyClass(new TargetClassConstructor(...));
                adapter.invokeConstructor(syntheticReturnType, new org.objectweb.asm.commons.Method(
                        CONSTRUCTOR_NAME, Type.VOID_TYPE, new Type[]{realReturnType}));
            }
            adapter.returnValue();

            Label label1 = new Label();
            adapter.visitLabel(label1);
            if (!isStatic && type != ReflectedObject.Type.CONSTRUCTOR) {
                visitThis(adapter, label0, label1);
            }

            int magicMaxs = magicMaxs(descriptor, isStatic);
            adapter.visitMaxs(magicMaxs, magicMaxs);
            adapter.visitEnd();
        }

        @Override
        public void visitCode() {
            // Don't retain any of the existing code, we will generate our own.
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
            if (shouldRemoveAnnotation(descriptor)) return null;
            return super.visitParameterAnnotation(parameter, descriptor, visible);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (shouldRemoveAnnotation(descriptor)) return null;
            return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
            if (shouldRemoveAnnotation(descriptor)) return null;
            return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
        }
    }

    private void getInstance(MethodVisitor mv) {
        mv.visitFieldInsn(Opcodes.GETFIELD, generatedClassType.getInternalName(), INSTANCE_FIELD, targetClassType.getDescriptor());
    }

    private void writeConstructor() {
        GeneratorAdapter mv = createMethod(
                /* access     */ Opcodes.ACC_PUBLIC,
                /* name       */ CONSTRUCTOR_NAME,
                /* descriptor */ Type.getMethodDescriptor(Type.getType(void.class), targetClassType)
        );

        Label label0 = new Label();
        mv.visitLabel(label0);
        // mv.visitLineNumber(33, label0);
        mv.loadThis();
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, SUPER_CLASS, CONSTRUCTOR_NAME, "()V", false);
        mv.loadThis();
        mv.loadArg(0);
        mv.putField(generatedClassType, INSTANCE_FIELD, targetClassType);
        mv.returnValue();

        Label label1 = new Label();
        mv.visitLabel(label1);
        visitThis(mv, label0, label1);
        mv.visitLocalVariable(INSTANCE_FIELD, targetClassType.getDescriptor(), null, label0, label1, 1);

        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    private void writeMethodHandleField(String name) {
        writePrivateFinalField(true, METHOD_HANDLE_PREFIX + name, MethodHandle.class);
    }

    private void writePrivateFinalField(boolean asStatic, String name, Class<?> type) {
        int access = Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL;

        if (asStatic) access = access | Opcodes.ACC_STATIC;

        FieldVisitor fv = classWriter.visitField(access, name, Type.getDescriptor(type), null, null);
        fv.visitEnd();
    }

    private void initStaticFields() {
        GeneratorAdapter mv = createMethod(Opcodes.ACC_STATIC, STATIC_BLOCK, "()V");
        mv.visitCode();

        Label start = new Label();
        Label end = new Label();
        Label catchException = new Label();
        mv.visitTryCatchBlock(start, end, catchException, "java/lang/Throwable");

        mv.visitLabel(start);
        int targetClass = mv.newLocal(Type.getType(Class.class));
        {
            mv.visitLdcInsn(this.targetClass.getName()); // Class name
            mv.invokeStatic(Type.getType(Class.class), getMethod("Class forName(String)"));
            mv.storeLocal(targetClass);
        }

        Type ASMPrivateLookup = Type.getType(ASMPrivateLookup.class);
        int lookup = mv.newLocal(ASMPrivateLookup);
        {
            mv.newInstance(ASMPrivateLookup);
            mv.dup();
            mv.loadLocal(targetClass);
            mv.invokeConstructor(ASMPrivateLookup, getMethod("void <init>(Class)"));
            mv.storeLocal(lookup);
        }

        for (OverloadedMethod<ASMProxyInfo> method : this.mapped.mappings().values()) {
            for (ASMProxyInfo overload : method.getOverloads()) {
                if (!overload.isInaccessible()) continue;
                ReflectedObject jvm = overload.info.handle.jvm().unreflect();

                Label unitLabel = new Label();
                mv.visitLabel(unitLabel);

                // These are inaccessible, we can't use MethodHandle directly.
                switch (jvm.type()) {
                    case CONSTRUCTOR: {
                        mv.loadLocal(lookup);

                        ArrayInsnGenerator pTypes = new ArrayInsnGenerator(mv, Class.class, overload.info.pTypes.length);
                        for (MappedType pType : overload.info.pTypes) {
                            pTypes.add(() -> mv.push(Type.getType(pType.real)));
                        }

                        mv.invokeVirtual(ASMPrivateLookup, getMethod("java.lang.invoke.MethodHandle findConstructor(Class[])"));
                        break;
                    }
                    case FIELD: {
                        FieldMemberHandle field = (FieldMemberHandle) overload.info.handle.unwrap();

                        mv.loadLocal(lookup);
                        mv.push(jvm.name());
                        mv.push(Type.getType(overload.info.rType.real));
                        mv.push(field.isGetter());

                        mv.invokeVirtual(ASMPrivateLookup, getMethod("java.lang.invoke.MethodHandle findField(String, Class, boolean)"));
                        break;
                    }
                    case METHOD: {
                        mv.loadLocal(lookup);
                        mv.push(jvm.name());
                        mv.push(Type.getType(overload.info.rType.real));

                        ArrayInsnGenerator pTypes = new ArrayInsnGenerator(mv, Class.class, overload.info.pTypes.length);
                        for (MappedType pType : overload.info.pTypes) {
                            pTypes.add(() -> mv.push(Type.getType(pType.real)));
                        }

                        mv.invokeVirtual(ASMPrivateLookup, getMethod("java.lang.invoke.MethodHandle findMethod(String, Class, Class[])"));
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unknown ReflectedObject type: " + jvm);
                }

                mv.visitFieldInsn(
                        Opcodes.PUTSTATIC,
                        generatedClassType.getInternalName(),
                        METHOD_HANDLE_PREFIX + overload.methodHandleName,
                        Type.getDescriptor(MethodHandle.class)
                );
            }
        }

        mv.visitLabel(end);
        Label noExceptionThrown = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, noExceptionThrown);
        mv.visitLabel(catchException);
        int ex = mv.newLocal(Type.getType(Throwable.class));
        mv.storeLocal(ex);

        Label label6 = new Label();
        mv.visitLabel(label6);
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
        mv.visitInsn(Opcodes.DUP);
        String StringBuilder = "java/lang/StringBuilder";
        mv.visitTypeInsn(Opcodes.NEW, StringBuilder);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, StringBuilder, CONSTRUCTOR_NAME, "()V", false);
        mv.visitLdcInsn("Failed to get inaccessible members for ");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, StringBuilder, "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitLdcInsn(generatedClassType);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, StringBuilder, "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, StringBuilder, "toString", "()Ljava/lang/String;", false);
        mv.loadLocal(ex);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", CONSTRUCTOR_NAME, "(Ljava/lang/String;Ljava/lang/Throwable;)V", false);
        mv.visitInsn(Opcodes.ATHROW);

        mv.visitLabel(noExceptionThrown);
        mv.visitInsn(Opcodes.RETURN);

        mv.visitLocalVariable("targetClass", Type.getDescriptor(Class.class), "Ljava/lang/Class<*>;", start, noExceptionThrown, targetClass);
        mv.visitLocalVariable("lookup", Type.getDescriptor(ASMPrivateLookup.class), null, start, noExceptionThrown, lookup);
        mv.visitLocalVariable("ex", Type.getDescriptor(Throwable.class), null, label6, noExceptionThrown, ex);

        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private void generateInstance() {
        // If we use the target class as a return type, then we'd also have to create a synthetic method as well
        // classWriter.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE | Opcodes.ACC_SYNTHETIC, "instance", "()Ljava/lang/Object;", null, null);
        GeneratorAdapter mv = createMethod(Opcodes.ACC_PUBLIC, INSTANCE_FIELD, Type.getMethodDescriptor(Type.getType(Object.class)));

        Label label0 = new Label();
        mv.visitLabel(label0);
        mv.visitLineNumber(33, label0);

        // return this.instance;
        mv.loadThis();
        getInstance(mv);
        mv.returnValue();

        Label label1 = new Label();
        mv.visitLabel(label1);
        visitThis(mv, label0, label1);

        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private void generateBindTo() {
        GeneratorAdapter mv = createMethod(
                Opcodes.ACC_PUBLIC,
                "bindTo",
                Type.getMethodDescriptor(templateClassType, Type.getType(Object.class))
        );

        Label label0 = mv.newLabel();
        mv.visitLabel(label0);

        Label label1 = new Label();

        mv.loadThis();
        getInstance(mv);
        mv.visitJumpInsn(Opcodes.IFNULL, label1);
        // if (this.instance != null) {
        mv.throwException(Type.getType(UnsupportedOperationException.class), "bindTo() must be called from the factory object, not on an instance");
        // } else {
        mv.visitLabel(label1);
        // }

        // mv.visitLineNumber(41, label1);
        // mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

        mv.newInstance(generatedClassType);
        mv.dup();
        mv.loadArg(0);
        mv.checkCast(targetClassType);
        mv.invokeConstructor(generatedClassType, new org.objectweb.asm.commons.Method(
                CONSTRUCTOR_NAME, Type.VOID_TYPE, new Type[]{targetClassType}));
        mv.returnValue();

        Label label3 = new Label();
        mv.visitLabel(label3);
        visitThis(mv, label0, label3);
        mv.visitLocalVariable(INSTANCE_FIELD, targetClassType.getDescriptor(), null, label0, label3, 1);

        mv.visitMaxs(3, 2);
        mv.visitEnd();
    }

    private void generateHashCode() {
        GeneratorAdapter mv = createMethod(Opcodes.ACC_PUBLIC, "hashCode", "()I");

        // if (this.instance == null) return this.hashCode();
        Label label0 = mv.newLabel();
        mv.visitLabel(label0);
        mv.loadThis();
        getInstance(mv);

        Label label1 = mv.newLabel();
        mv.ifNonNull(label1);
        mv.loadThis();
        mv.invokeVirtual(generatedClassType, getMethod("int hashCode()"));
        mv.returnValue();

        // return this.instance.hashCode();
        mv.visitLabel(label1);
        // mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.loadThis();
        getInstance(mv);
        mv.invokeVirtual(targetClassType, getMethod("int hashCode()"));
        mv.returnValue();

        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private void generateEquals() {
        /*
            if (this == obj) return true;
            if (this.instance == null) return false;
            if (obj == null) return false;

            if (obj instanceof TemplateClass) {
                return this.instance.equals(((TestHiS) obj).instance());
            }
            if (obj instanceof TargetClass) {
                return this.instance.equals(obj);
            }

            return false;
         */
        GeneratorAdapter mv = createMethod(Opcodes.ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z");

        Label label0 = new Label();
        mv.visitLabel(label0);
        // mv.visitLineNumber(39, label0);
        mv.loadThis();
        mv.loadArg(0);

        Label label1 = new Label();
        mv.visitJumpInsn(Opcodes.IF_ACMPNE, label1);
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitLabel(label1);
        // mv.visitLineNumber(40, label1);
        // mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.loadThis();
        getInstance(mv);

        Label label2 = new Label();
        mv.visitJumpInsn(Opcodes.IFNONNULL, label2);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitLabel(label2);
        // mv.visitLineNumber(41, label2);
        // mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.loadArg(0);

        Label label3 = new Label();
        mv.visitJumpInsn(Opcodes.IFNONNULL, label3);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitLabel(label3);
        // mv.visitLineNumber(43, label3);
        // mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.loadArg(0);
        mv.instanceOf(templateClassType);
        Label label4 = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, label4);
        Label label5 = new Label();
        mv.visitLabel(label5);
        // mv.visitLineNumber(44, label5);
        mv.loadThis();
        getInstance(mv);
        mv.loadArg(0);
        mv.checkCast(templateClassType);
        mv.invokeInterface(templateClassType, getMethod("Object instance();"));
        mv.invokeVirtual(targetClassType, getMethod("boolean equals(Object);"));
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitLabel(label4);
        // mv.visitLineNumber(46, label4);
        // mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.loadArg(0);
        mv.instanceOf(targetClassType);
        Label label6 = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, label6);
        Label label7 = new Label();
        mv.visitLabel(label7);
        // mv.visitLineNumber(47, label7);
        mv.loadThis();
        getInstance(mv);
        mv.loadArg(0);
        mv.invokeVirtual(targetClassType, getMethod("boolean equals(Object);"));
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitLabel(label6);
        // mv.visitLineNumber(50, label6);
        // mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitInsn(Opcodes.IRETURN);

        Label label8 = new Label();
        mv.visitLabel(label8);
        visitThis(mv, label0, label8);
        mv.visitLocalVariable("obj", "Ljava/lang/Object;", null, label0, label8, 1);

        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    private void generateGetTargetClass() {
        GeneratorAdapter mv = createMethod(Opcodes.ACC_PUBLIC, "Class getTargetClass()");
        mv.push(targetClassType);
        mv.returnValue();

        mv.visitMaxs(1, 0);
        mv.visitEnd();
    }

    private void generateIsInstance() {
        GeneratorAdapter mv = createMethod(Opcodes.ACC_PUBLIC, "boolean isInstance(Object)");
        mv.loadArg(0);
        mv.instanceOf(targetClassType);
        mv.returnValue();

        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private void generateNewArraySingleDim() {
        GeneratorAdapter mv = createMethod(Opcodes.ACC_PUBLIC, "Object[] newArray(int)");

        Label startLabel = new Label();
        mv.visitLabel(startLabel);
        mv.loadArg(0);
        mv.newArray(targetClassType);
        mv.visitInsn(Opcodes.ARETURN);

        Label endLabel = new Label();
        mv.visitLabel(endLabel);
        visitThis(mv, startLabel, endLabel);
        mv.visitLocalVariable("length", "I", null, startLabel, endLabel, 1);

        mv.visitMaxs(1, 2);
        mv.visitEnd();
    }

    private void generateNewArrayMultiDim() {
        GeneratorAdapter mv = createMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_VARARGS, "Object[] newArray(int[])");

        Label label0 = new Label();
        mv.visitLabel(label0);
        mv.loadArg(0);
        mv.arrayLength();

        // switch (dimensions.length) { }
        Label case1 = new Label();
        Label case2 = new Label();
        Label case3 = new Label();
        Label defaultCase = new Label();
        mv.visitTableSwitchInsn(1, 3, defaultCase, case1, case2, case3);

        // case 1: return new TargetClass[dimensions[0]];
        mv.visitLabel(case1);
        mv.loadArg(0);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitInsn(Opcodes.IALOAD);
        mv.newArray(targetClassType);
        mv.visitInsn(Opcodes.ARETURN);

        // case 2: return new TargetClass[dimensions[0]][dimensions[1]];
        mv.visitLabel(case2);
        mv.loadArg(0);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitInsn(Opcodes.IALOAD);
        mv.loadArg(0);
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitInsn(Opcodes.IALOAD);
        mv.visitMultiANewArrayInsn("[[" + targetClassType.getDescriptor(), 2);
        mv.visitInsn(Opcodes.ARETURN);

        // case 3: return new TargetClass[dimensions[0]][dimensions[1]][dimensions[2]];
        mv.visitLabel(case3);
        mv.loadArg(0);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitInsn(Opcodes.IALOAD);
        mv.loadArg(0);
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitInsn(Opcodes.IALOAD);
        mv.loadArg(0);
        mv.visitInsn(Opcodes.ICONST_2);
        mv.visitInsn(Opcodes.IALOAD);
        mv.visitMultiANewArrayInsn("[[[" + targetClassType.getDescriptor(), 3);
        mv.visitInsn(Opcodes.ARETURN);

        // default:
        mv.visitLabel(defaultCase);
        mv.push(targetClassType);
        mv.loadArg(0);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/reflect/Array", "newInstance", "(Ljava/lang/Class;[I)Ljava/lang/Object;", false);
        mv.checkCast(Type.getType(Object[].class));
        mv.visitInsn(Opcodes.ARETURN);

        Label endLabel = new Label();
        mv.visitLabel(endLabel);
        visitThis(mv, label0, endLabel);
        mv.visitLocalVariable("dimensions", "[I", null, label0, endLabel, 1);

        mv.visitMaxs(4, 2);
        mv.visitEnd();
    }

    private void generateToString() {
        // return this.getClass().getSimpleName() + "(instance=" + this.instance + ')';
        Type StringBuilder = Type.getType(java.lang.StringBuilder.class);
        GeneratorAdapter mv = createMethod(Opcodes.ACC_PUBLIC, "String toString()");

        Label start = mv.newLabel();
        mv.visitLabel(start);

        mv.newInstance(StringBuilder);
        mv.dup();
        mv.invokeConstructor(StringBuilder, getMethod("void <init>()"));

        mv.loadThis();
        mv.invokeVirtual(Type.getType(Object.class), getMethod("Class getClass()"));
        mv.invokeVirtual(Type.getType(Class.class), getMethod("String getSimpleName()"));
        mv.invokeVirtual(StringBuilder, getMethod("StringBuilder append(String)"));

        mv.push("(instance=");
        mv.invokeVirtual(StringBuilder, getMethod("StringBuilder append(String)"));

        mv.loadThis();
        getInstance(mv);
        mv.invokeVirtual(StringBuilder, getMethod("StringBuilder append(Object)"));

        mv.push(')');
        mv.invokeVirtual(StringBuilder, getMethod("StringBuilder append(char)"));

        mv.invokeVirtual(StringBuilder, getMethod("String toString()"));
        mv.returnValue();

        Label end = new Label();
        mv.visitLabel(end);
        visitThis(mv, start, end);

        mv.visitMaxs(2, 1);
        mv.visitEnd();
    }

    private GeneratorAdapter createMethod(int access, String descriptor) {
        Method desc = getMethod(descriptor);
        return createMethod(access, desc.getName(), desc.getDescriptor());
    }

    private GeneratorAdapter createMethod(int access,
                                          @Pattern("(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)|(<init>)|(<clinit>)") String name,
                                          String descriptor) {
        GeneratorAdapter method = new GeneratorAdapter(
                classWriter.visitMethod(access, name, descriptor, null, null),
                access, name, descriptor
        );
        method.visitCode();
        return method;
    }

    private void visitThis(MethodVisitor mv, Label start, Label end) {
        mv.visitLocalVariable("this", generatedClassType.getDescriptor(), null, start, end, 0);
    }

    @NotNull
    public Class<?> loadClass() {
        if (this.loaded != null) return this.loaded;

        // return AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
        //     public Class<?> run() {
        //         return newLoadClass(generatedClassName, bytecode);
        //     }
        // });

        generate();
        verify(true); // Silently verifies the class unless there's an issue, in which case uses System.err
        // writeToFile(Paths.get(System.getProperty("user.home") + "/Desktop/"));
        return this.loaded = CLASS_LOADER.defineClass(generatedClassPath, bytecode);
    }
}
