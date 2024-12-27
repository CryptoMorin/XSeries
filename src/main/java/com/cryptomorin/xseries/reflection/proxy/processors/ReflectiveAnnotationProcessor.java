/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Crypto Morin
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

package com.cryptomorin.xseries.reflection.proxy.processors;

import com.cryptomorin.xseries.reflection.ReflectiveHandle;
import com.cryptomorin.xseries.reflection.XAccessFlag;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.aggregate.VersionHandle;
import com.cryptomorin.xseries.reflection.jvm.FieldMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.MemberHandle;
import com.cryptomorin.xseries.reflection.jvm.MethodMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.NameableReflectiveHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.DynamicClassHandle;
import com.cryptomorin.xseries.reflection.proxy.ClassOverloadedMethods;
import com.cryptomorin.xseries.reflection.proxy.OverloadedMethod;
import com.cryptomorin.xseries.reflection.proxy.ReflectiveProxy;
import com.cryptomorin.xseries.reflection.proxy.ReflectiveProxyObject;
import com.cryptomorin.xseries.reflection.proxy.annotations.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.Class;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;

@ApiStatus.Internal
public final class ReflectiveAnnotationProcessor {
    private final Class<? extends ReflectiveProxyObject> interfaceClass;
    private ClassOverloadedMethods<ProxyMethodInfo> mapped;
    private Function<ProxyMethodInfo, String> descriptorProcessor;

    private Class<?> targetClass;

    public ReflectiveAnnotationProcessor(Class<? extends ReflectiveProxyObject> interfaceClass) {
        ReflectiveProxy.checkInterfaceClass(interfaceClass);
        this.interfaceClass = interfaceClass;
    }

    private void error(String msg) {
        error(msg, null);
    }

    private void error(String msg, Throwable ex) {
        throw new IllegalStateException(msg + " (Proxified Interface: " + interfaceClass + ')', ex);
    }

    protected static boolean isAnnotationInherited(Class<?> clazz, Method method, Class<? extends Annotation> annotation) {
        try {
            Method superMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
            if (superMethod.isAnnotationPresent(annotation)) return true;
        } catch (NoSuchMethodException ignored) {
        }

        for (Class<?> superInterface : clazz.getInterfaces()) {
            if (isAnnotationInherited(superInterface, method, annotation)) return true;
        }

        return false;
    }

    public void loadDependencies(Function<Class<?>, Boolean> isLoaded) {
        for (OverloadedMethod<ProxyMethodInfo> overloads : mapped.mappings().values()) {
            for (ProxyMethodInfo overload : overloads.getOverloads()) {
                loadDependency(overload.rType, isLoaded);
                for (MappedType pType : overload.pTypes) {
                    loadDependency(pType, isLoaded);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadDependency(MappedType type, Function<Class<?>, Boolean> isLoaded) {
        if (ReflectiveProxyObject.class.isAssignableFrom(type.synthetic)
                && type.synthetic != interfaceClass
                && !isLoaded.apply(type.synthetic)) {
            XReflection.proxify((Class<? extends ReflectiveProxyObject>) type.synthetic);
        }
    }

    public void process(Function<ProxyMethodInfo, String> descriptorProcessor) {
        this.descriptorProcessor = descriptorProcessor;
        ClassHandle classHandle = processTargetClass();
        Method[] interfaceMethods = interfaceClass.getMethods(); // It's an interface, all are public
        OverloadedMethod.Builder<ProxyMethodInfo> mappedHandles = new OverloadedMethod.Builder<>(descriptorProcessor);

        for (Method method : interfaceMethods) {
            // Java doesn't inherit annotations for interfaces...
            if (isAnnotationInherited(interfaceClass, method, Ignore.class)) continue;

            boolean asStatic = method.isAnnotationPresent(Static.class);
            boolean asFinal = method.isAnnotationPresent(Final.class);
            MappedType rType;
            MappedType[] pTypes = new MappedType[0];

            MemberHandle handle;
            if (method.isAnnotationPresent(Constructor.class)) {
                Class<?> returnType = method.getReturnType();
                if (returnType != targetClass && returnType != interfaceClass && returnType != Object.class) {
                    error("Method marked with @Constructor must return Object.class, " + targetClass + " or " + interfaceClass);
                }

                rType = unwrap(returnType);
                pTypes = unwrap(method.getParameterTypes());

                handle = classHandle.constructor(MappedType.getRealTypes(pTypes));
                if (asStatic) error("Constructor cannot be static: " + method);
                if (asFinal) error("Constructor cannot be final: " + method);
            } else if (method.isAnnotationPresent(Field.class)) {
                FieldMemberHandle field = classHandle.field();
                if (method.getReturnType() == void.class) {
                    field.setter();
                    if (method.getParameterCount() != 1) {
                        error("Field setter method must have only one parameter: " + method);
                    }

                    Class<?> parameterType = method.getParameterTypes()[0];
                    rType = unwrap(parameterType);

                    field.returns(rType.real);
                } else {
                    field.getter();
                    if (method.getParameterCount() != 0) {
                        error("Field getter method must not have any parameters: " + method);
                    }

                    rType = unwrap(method.getReturnType());
                    field.returns(rType.real);
                }

                if (asStatic) field.asStatic();
                if (asFinal) field.asFinal();
                handle = field;
            } else {
                rType = unwrap(method.getReturnType());
                pTypes = unwrap(method.getParameterTypes());

                MethodMemberHandle methHandle = classHandle.method()
                        .returns(rType.real)
                        .parameters(MappedType.getRealTypes(pTypes));
                if (asStatic) methHandle = methHandle.asStatic();
                if (asFinal) error("Declaring method as final has no effect: " + method);
                handle = methHandle;
            }

            boolean visibilitySet = false;
            if (method.isAnnotationPresent(Private.class)) {
                visibilitySet = true;
                handle.getAccessFlags().add(XAccessFlag.PRIVATE);
            }
            if (method.isAnnotationPresent(Protected.class)) {
                if (visibilitySet)
                    error("Cannot have two visibility modifier private and protected for " + method);
                handle.getAccessFlags().add(XAccessFlag.PRIVATE); // Yes, we use private again here
            }

            if (handle instanceof NameableReflectiveHandle) {
                ((NameableReflectiveHandle) handle).named(method.getName());
                reflectNames((NameableReflectiveHandle) handle, method);
            }

            ReflectiveHandle<MethodHandle> cached = handle.cached();
            try {
                cached.reflect();
            } catch (ReflectiveOperationException e) {
                error("Failed to map " + method, e);
            }

            ProxyMethodInfo methodInfo = new ProxyMethodInfo(cached, method, rType, pTypes);
            mappedHandles.add(methodInfo, method.getName());
        }

        this.mapped = mappedHandles.build();
    }

    public @NotNull ClassHandle processTargetClass() {
        com.cryptomorin.xseries.reflection.proxy.annotations.Class reflectClass =
                interfaceClass.getAnnotation(com.cryptomorin.xseries.reflection.proxy.annotations.Class.class);
        ReflectMinecraftPackage mcClass = interfaceClass.getAnnotation(ReflectMinecraftPackage.class);

        if (reflectClass == null && mcClass == null) {
            error("Proxy interface is not annotated with @Class or @ReflectMinecraftPackage");
        }
        if (reflectClass != null && mcClass != null) {
            error("Proxy interface cannot contain both @Class or @ReflectMinecraftPackage");
        }

        ClassHandle classHandle;
        boolean ignoreCurrentName;
        if (reflectClass != null) {
            if (reflectClass.target() != void.class) {
                classHandle = XReflection.of(reflectClass.target());
                ignoreCurrentName = true;
            } else {
                DynamicClassHandle dynClassHandle = XReflection.classHandle();
                classHandle = dynClassHandle;
                dynClassHandle.inPackage(reflectClass.packageName());
                ignoreCurrentName = reflectClass.ignoreCurrentName();
            }
        } else {
            DynamicClassHandle dynClassHandle = XReflection.ofMinecraft();
            classHandle = dynClassHandle;
            dynClassHandle.inPackage(mcClass.type(), mcClass.packageName());
            ignoreCurrentName = mcClass.ignoreCurrentName();
        }

        if (classHandle instanceof DynamicClassHandle) {
            DynamicClassHandle dynamicClassHandle = (DynamicClassHandle) classHandle;
            if (!ignoreCurrentName) dynamicClassHandle.named(interfaceClass.getSimpleName());
            reflectNames(dynamicClassHandle, interfaceClass);
        }

        this.targetClass = classHandle.unreflect();

        // Prevent circular dependency:
        // interface A {
        //    B convert();
        // }
        // interface B {
        //    A convert();
        // }
        MappedType.LOOK_AHEAD.put(interfaceClass, targetClass);

        return classHandle;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public ClassOverloadedMethods<ProxyMethodInfo> getMapped() {
        return mapped;
    }

    private void reflectNames(NameableReflectiveHandle handle, AnnotatedElement annotated) {
        MappedMinecraftName[] mapped = annotated.getDeclaredAnnotationsByType(MappedMinecraftName.class);
        ReflectName[] rawNames = annotated.getDeclaredAnnotationsByType(ReflectName.class);

        for (MappedMinecraftName mcMapped : mapped) {
            reflectNames0(handle, mcMapped.names());
        }
        reflectNames0(handle, rawNames);
    }

    private void reflectNames0(NameableReflectiveHandle handle, ReflectName[] reflectedNames) {
        if (reflectedNames.length == 0) return;
        VersionHandle<String[]> versionControl = null;
        String[] chosen = null;

        int index = 0;
        for (ReflectName name : reflectedNames) {
            index++;
            if (chosen != null)
                error("Cannot contain more tha one @ReflectName if no version is specified");

            if (!name.version().isEmpty()) {
                if (index == reflectedNames.length)
                    error("Last @ReflectName should not contain version");

                int[] semVer = Arrays.stream(name.version().split("\\.")).mapToInt(Integer::parseInt).toArray();
                if (versionControl == null) {
                    if (semVer.length == 1) versionControl = XReflection.v(semVer[0], name.value());
                    if (semVer.length == 2) versionControl = XReflection.v(semVer[1], name.value());
                    if (semVer.length == 3) versionControl = XReflection.v(semVer[1], semVer[2], name.value());
                } else {
                    if (semVer.length == 1) versionControl.v(semVer[0], name.value());
                    if (semVer.length == 2) versionControl.v(semVer[1], name.value());
                    if (semVer.length == 3) versionControl.v(semVer[1], semVer[2], name.value());
                }
            } else if (versionControl != null) {
                if (index != reflectedNames.length) {
                    error("One of @ReflectName doesn't contain a version.");
                } else {
                    chosen = versionControl.orElse(name.value());
                }
            } else {
                chosen = name.value();
            }
        }

        handle.named(chosen);
    }

    private MappedType[] unwrap(Class<?>[] classes) {
        MappedType[] unwrapped = new MappedType[classes.length];

        for (int i = 0; i < classes.length; i++) {
            Class<?> clazz = classes[i];
            unwrapped[i] = unwrap(clazz);
        }

        return unwrapped;
    }

    @SuppressWarnings("unchecked")
    private MappedType unwrap(Class<?> clazz) {
        if (clazz == interfaceClass) {
            // Commonly used for constructors, so we treat it specially.
            return new MappedType(interfaceClass, targetClass);
        }

        if (ReflectiveProxyObject.class.isAssignableFrom(clazz)) {
            Class<?> real = MappedType.getMappedTypeOrCreate((Class<? extends ReflectiveProxyObject>) clazz);
            return new MappedType(clazz, real);
        }

        return new MappedType(clazz, clazz);
    }
}
