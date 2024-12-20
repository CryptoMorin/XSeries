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

package com.cryptomorin.xseries.reflection.proxy;

import com.cryptomorin.xseries.reflection.ReflectiveHandle;
import com.cryptomorin.xseries.reflection.XAccessFlag;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.aggregate.VersionHandle;
import com.cryptomorin.xseries.reflection.jvm.*;
import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.DynamicClassHandle;
import com.cryptomorin.xseries.reflection.proxy.annotations.Class;
import com.cryptomorin.xseries.reflection.proxy.annotations.Constructor;
import com.cryptomorin.xseries.reflection.proxy.annotations.Field;
import com.cryptomorin.xseries.reflection.proxy.annotations.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The basis of this class is that you create an {@code interface} class and annotate it using
 * {@link Class}, {@link Field}, {@link Constructor}, etc... annotations to mark how
 * these methods are resolved for different versions. Everything is accessed through methods,
 * even constructors and fields are accessed in forms of methods.
 * <p>
 * Due to the fact that this method uses annotations and Java's {@link Proxy} mechanism, the startup
 * and the invocation of the methods is pretty slow compared to the normal {@link XReflection APIs.
 *
 * @since 13.0.0
 * @see ReflectiveProxyObject
 * @see XReflection#proxify(Class)
 */
@ApiStatus.Experimental
public final class ReflectiveProxy<T extends ReflectiveProxyObject> implements InvocationHandler {
    private static final Map<java.lang.Class<?>, ReflectiveProxy<?>> PROXIFIED_CLASS_LOADER = new IdentityHashMap<>();
    private static final ClassLoader CLASS_LOADER = ReflectiveProxy.class.getClassLoader();

    private final java.lang.Class<?> targetClass;
    private final java.lang.Class<T> proxyClass;
    private T proxy;
    private final Object instance;
    private final Map<Method, ProxifiedObject> handles;
    private final Map<String, ProxifiedObject> nameMapped;

    private static final class ProxifiedObject {
        private final MethodHandle handle;
        private final boolean isStatic, isConstructor;
        private final ReflectiveProxy<?> rType;
        private final ReflectiveProxy<?>[] pTypes;

        private ProxifiedObject(MethodHandle handle, boolean isStatic, boolean isConstructor, ReflectiveProxy<?> rType, ReflectiveProxy<?>[] pTypes) {
            this.handle = handle;
            this.isStatic = isStatic;
            this.isConstructor = isConstructor;
            this.rType = rType;
            this.pTypes = pTypes;
        }
    }

    private ReflectiveProxy(java.lang.Class<?> targetClass, java.lang.Class<T> proxyClass, Object instance, Map<Method, ProxifiedObject> handles, Map<String, ProxifiedObject> nameMapped) {
        this.targetClass = targetClass;
        this.proxyClass = proxyClass;
        this.instance = instance;
        this.handles = handles;
        this.nameMapped = nameMapped;
    }

    private ReflectiveProxy(java.lang.Class<?> targetClass, java.lang.Class<T> proxyClass, Object instance, Map<Method, ProxifiedObject> handles) {
        this.targetClass = targetClass;
        this.proxyClass = proxyClass;
        this.instance = instance;
        this.handles = handles;
        this.nameMapped = new HashMap<>(handles.size());
        populateNames();
    }

    private void populateNames() {
        for (Map.Entry<Method, ProxifiedObject> entry : handles.entrySet()) {
            this.nameMapped.put(entry.getKey().getName(), entry.getValue());
        }
    }

    private static final class Unwrapped {
        private final java.lang.Class<?>[] translated;
        private final ReflectiveProxy<ReflectiveProxyObject>[] proxies;

        private Unwrapped(java.lang.Class<?>[] translated, ReflectiveProxy<ReflectiveProxyObject>[] proxies) {
            this.translated = translated;
            this.proxies = proxies;
        }
    }

    @SuppressWarnings("unchecked")
    private static Unwrapped unwrap(java.lang.Class<?>[] classes) {
        java.lang.Class<?>[] unwrapped = new java.lang.Class[classes.length];
        ReflectiveProxy<ReflectiveProxyObject>[] proxies = new ReflectiveProxy[classes.length];
        boolean changed = false;

        for (int i = 0; i < classes.length; i++) {
            java.lang.Class<?> clazz = classes[i];
            ReflectiveProxy<ReflectiveProxyObject> unwrap = unwrap(clazz);

            if (unwrap != null) {
                changed = true;
                unwrapped[i] = unwrap.targetClass;
                proxies[i] = unwrap;
            } else {
                unwrapped[i] = clazz;
            }
        }

        if (changed) return new Unwrapped(unwrapped, proxies);
        else return new Unwrapped(classes, null);
    }

    @SuppressWarnings("unchecked")
    private static ReflectiveProxy<ReflectiveProxyObject> unwrap(java.lang.Class<?> clazz) {
        if (ReflectiveProxyObject.class.isAssignableFrom(clazz)) {
            return proxify((java.lang.Class<ReflectiveProxyObject>) clazz);
        }

        return null;
    }

    private static void checkInterfaceClass(java.lang.Class<?> interfaceClass) {
        Objects.requireNonNull(interfaceClass, "Interface class is null");

        if (!interfaceClass.isInterface())
            throw new IllegalArgumentException("Cannot proxify non-interface class: " + interfaceClass);

        if (!ReflectiveProxyObject.class.isAssignableFrom(interfaceClass))
            throw new IllegalArgumentException("The provided interface class must extend ReflectiveProxyObject interface");
    }

    @ApiStatus.Internal
    public static <T extends ReflectiveProxyObject> ReflectiveProxy<T> proxify(@NotNull java.lang.Class<T> interfaceClass) {
        checkInterfaceClass(interfaceClass);

        ReflectiveProxy<?> loaded = PROXIFIED_CLASS_LOADER.get(interfaceClass);
        if (loaded != null) // noinspection unchecked
            return (ReflectiveProxy<T>) loaded;

        Class reflectClass = interfaceClass.getAnnotation(Class.class);
        ReflectMinecraftPackage mcClass = interfaceClass.getAnnotation(ReflectMinecraftPackage.class);

        if (reflectClass == null && mcClass == null) {
            throw new IllegalArgumentException("Proxy interface is not annotated with @ReflectClass or @ReflectMinecraftClass");
        }
        if (reflectClass != null && mcClass != null) {
            throw new IllegalArgumentException("Proxy interface cannot contain both @ReflectClass or @ReflectMinecraftClass");
        }

        DynamicClassHandle classHandle;
        boolean ignoreCurrentName;
        if (reflectClass != null) {
            classHandle = XReflection.classHandle();
            classHandle.inPackage(reflectClass.packageName());
            ignoreCurrentName = reflectClass.ignoreCurrentName();
        } else {
            classHandle = XReflection.ofMinecraft();
            classHandle.inPackage(mcClass.type(), mcClass.packageName());
            ignoreCurrentName = mcClass.ignoreCurrentName();
        }
        if (!ignoreCurrentName) classHandle.named(interfaceClass.getSimpleName());
        reflectNames(classHandle, interfaceClass);

        java.lang.Class<?> targetClass = classHandle.unreflect();
        Method[] interfaceMethods = interfaceClass.getDeclaredMethods();
        Map<Method, ProxifiedObject> mappedHandles = new IdentityHashMap<>(interfaceMethods.length);

        // Prevent circular dependency:
        // interface A {
        //    B convert();
        // }
        // interface B {
        //    A convert();
        // }
        ReflectiveProxy<T> reflectiveProxy = new ReflectiveProxy<>(targetClass, interfaceClass, null, mappedHandles);
        PROXIFIED_CLASS_LOADER.put(interfaceClass, reflectiveProxy);

        for (Method method : interfaceMethods) {
            if (method.isAnnotationPresent(Ignore.class)) continue;

            boolean asStatic = method.isAnnotationPresent(Static.class);
            boolean asFinal = method.isAnnotationPresent(Final.class);
            boolean ctor = false;
            ReflectiveProxy<ReflectiveProxyObject> rType = null;
            ReflectiveProxy<ReflectiveProxyObject>[] pTypes = null;

            MemberHandle handle;
            if (method.isAnnotationPresent(Constructor.class)) {
                ctor = true;
                java.lang.Class<?> returnType = method.getReturnType();
                if (returnType != targetClass && returnType != interfaceClass && returnType != Object.class) {
                    throw new IllegalArgumentException("Method marked with @Constructor must return Object.class, " + targetClass + " or " + interfaceClass);
                }

                rType = unwrap(returnType);

                Unwrapped parameters = unwrap(method.getParameterTypes());
                pTypes = parameters.proxies;

                handle = classHandle.constructor(parameters.translated);
                if (asStatic) throw new IllegalArgumentException("Constructor cannot be static: " + method);
                if (asFinal) throw new IllegalArgumentException("Constructor cannot be final: " + method);
            } else if (method.isAnnotationPresent(Field.class)) {
                FieldMemberHandle field = classHandle.field();
                if (method.getReturnType() == void.class) {
                    field.setter();
                    if (method.getParameterCount() != 1) {
                        throw new IllegalStateException("Field setter method must have only one parameter: " + method);
                    }

                    java.lang.Class<?> parameterType = method.getParameterTypes()[0];
                    ReflectiveProxy<ReflectiveProxyObject> pTypeReflect = unwrap(parameterType);
                    if (pTypeReflect != null) // noinspection unchecked
                        pTypes = new ReflectiveProxy[]{pTypeReflect};

                    field.returns(pTypeReflect != null ? pTypeReflect.targetClass : parameterType);
                } else {
                    field.getter();
                    if (method.getParameterCount() != 0) {
                        throw new IllegalStateException("Field getter method must not have any parameters: " + method);
                    }

                    rType = unwrap(method.getReturnType());
                    field.returns(rType != null ? rType.targetClass : method.getReturnType());
                }

                if (asStatic) field.asStatic();
                if (asFinal) field.asFinal();
                handle = field;
            } else {
                rType = unwrap(method.getReturnType());
                Unwrapped pTypesReflect = unwrap(method.getParameterTypes());
                pTypes = pTypesReflect.proxies;

                MethodMemberHandle methHandle = classHandle.method()
                        .returns(rType != null ? rType.targetClass : method.getReturnType())
                        .parameters(pTypesReflect.translated);
                if (asStatic) methHandle = methHandle.asStatic();
                if (asFinal) throw new IllegalArgumentException("Declaring method as final has no effect: " + method);
                handle = methHandle;
            }

            boolean visibilitySet = false;
            if (method.isAnnotationPresent(Private.class)) {
                visibilitySet = true;
                handle.getAccessFlags().add(XAccessFlag.PRIVATE);
            }
            if (method.isAnnotationPresent(Protected.class)) {
                if (visibilitySet)
                    throw new IllegalArgumentException("Cannot have two visibility modifier private and protected for " + method);
                handle.getAccessFlags().add(XAccessFlag.PRIVATE); // Yes, we use private again here
            }

            if (handle instanceof NameableReflectiveHandle) {
                ((NameableReflectiveHandle) handle).named(method.getName());
                reflectNames((NameableReflectiveHandle) handle, method);
            }

            MethodHandle reflected;
            try {
                reflected = handle.reflect();
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException("Failed to map " + method, e);
            }
            mappedHandles.put(method, new ProxifiedObject(
                    reflected,
                    handle.getAccessFlags().contains(XAccessFlag.STATIC),
                    ctor, rType, pTypes));
        }

        reflectiveProxy.populateNames();
        reflectiveProxy.proxy = reflectiveProxy.createProxy();
        return reflectiveProxy;
    }

    private static void reflectNames(NameableReflectiveHandle handle, AnnotatedElement annotated) {
        MappedMinecraftName[] mapped = annotated.getDeclaredAnnotationsByType(MappedMinecraftName.class);
        ReflectName[] rawNames = annotated.getDeclaredAnnotationsByType(ReflectName.class);

        for (MappedMinecraftName mcMapped : mapped) {
            reflectNames0(handle, mcMapped.names());
        }
        reflectNames0(handle, rawNames);
    }

    @ApiStatus.Internal
    private static void reflectNames0(NameableReflectiveHandle handle, ReflectName[] reflectedNames) {
        if (reflectedNames.length == 0) return;
        VersionHandle<String[]> versionControl = null;
        String[] chosen = null;

        int index = 0;
        for (ReflectName name : reflectedNames) {
            index++;
            if (chosen != null)
                throw new IllegalArgumentException("Cannot contain more tha one @ReflectName if no version is specified");

            if (!name.version().isEmpty()) {
                if (index == reflectedNames.length)
                    throw new IllegalArgumentException("Last @ReflectName should not contain version");

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
                    throw new IllegalArgumentException("One of @ReflectName doesn't contain a version.");
                } else {
                    chosen = versionControl.orElse(name.value());
                }
            } else {
                chosen = name.value();
            }
        }

        handle.named(chosen);
    }

    public static <T extends ReflectiveProxyObject> ReflectiveProxy<T> proxify(@NotNull java.lang.Class<T> interfaceClass,
                                                                               @NotNull ClassHandle targetClass,
                                                                               @NotNull ReflectiveHandle<?>... handles) {
        checkInterfaceClass(interfaceClass);

        ReflectiveProxy<?> loaded = PROXIFIED_CLASS_LOADER.get(interfaceClass);
        if (loaded != null) // noinspection unchecked
            return (ReflectiveProxy<T>) loaded;

        Set<ReflectiveHandle<?>> remainingHandles = Collections.newSetFromMap(new IdentityHashMap<>());
        remainingHandles.addAll(Arrays.asList(handles));

        Method[] interfaceMethods = interfaceClass.getDeclaredMethods();
        Map<Method, ProxifiedObject> mappedHandles = new IdentityHashMap<>(interfaceMethods.length);
        for (Method method : interfaceMethods) {
            if (method.isAnnotationPresent(Ignore.class)) continue;

            String name = method.getName();
            Iterator<ReflectiveHandle<?>> iter = remainingHandles.iterator();
            while (iter.hasNext()) {
                ReflectiveHandle<?> next = iter.next();
                if (next instanceof FieldMemberHandle) {
                    FieldMemberHandle field = (FieldMemberHandle) next;
                    if (field.getPossibleNames().stream().anyMatch(x -> x.equals(name))) {
                        iter.remove();
                        mappedHandles.put(method, new ProxifiedObject(
                                field.unreflect(),
                                field.getAccessFlags().contains(XAccessFlag.STATIC),
                                false, null, null));
                        break;
                    }
                } else if (next instanceof MethodMemberHandle) {
                    MethodMemberHandle methodHandle = (MethodMemberHandle) next;
                    if (methodHandle.getPossibleNames().stream().anyMatch(x -> x.equals(name))) {
                        iter.remove();
                        mappedHandles.put(method, new ProxifiedObject(
                                methodHandle.unreflect(),
                                methodHandle.getAccessFlags().contains(XAccessFlag.STATIC),
                                false, null, null));
                        break;
                    }
                } else if (next instanceof ConstructorMemberHandle &&
                        method.getReturnType() == interfaceClass &&
                        name.equals(interfaceClass.getName())) {

                    ConstructorMemberHandle ctor = (ConstructorMemberHandle) next;
                    if (ctor.getParameterTypes().length != method.getParameterCount()) continue;

                    java.lang.reflect.Constructor<?> constructor;
                    try {
                        constructor = ctor.reflectJvm();
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("Failed to map " + method, e);
                    }

                    int index = 0;
                    for (Parameter parameter : method.getParameters()) {
                        if (constructor.getParameters()[index].getType() != parameter.getType()) {
                            index = -1;
                            break;
                        }
                        index++;
                    }
                    if (index == -1) continue;

                    iter.remove();
                    mappedHandles.put(method, new ProxifiedObject(
                            ctor.unreflect(), false, true, null, null));
                    break;
                }
            }
        }

        ReflectiveProxy<T> reflectiveProxy = new ReflectiveProxy<>(targetClass.unreflect(), interfaceClass, null, mappedHandles);
        reflectiveProxy.proxy = reflectiveProxy.createProxy();
        PROXIFIED_CLASS_LOADER.put(interfaceClass, reflectiveProxy);
        return reflectiveProxy;
    }

    @SuppressWarnings("unchecked")
    private @NotNull T createProxy() {
        return (T) Proxy.newProxyInstance(CLASS_LOADER, new java.lang.Class[]{this.proxyClass}, this);
    }

    @NotNull
    public T proxy() {
        return proxy;
    }

    @Nullable
    public Object instance() {
        return instance;
    }

    /**
     * Returns a new {@link ReflectiveProxyObject} that's linked to a new {@link ReflectiveProxy} with the given instance.
     * @param instance the instance to bind.
     */
    @NotNull
    public T bindTo(@NotNull Object instance) {
        if (this.instance != null)
            throw new IllegalStateException("This proxy object already has an instance bound to it: " + this);

        if (!this.targetClass.isAssignableFrom(instance.getClass()))
            throw new IllegalArgumentException("The given instance doesn't match the target class: " + instance + " -> " + this);

        Objects.requireNonNull(instance, "Instance cannot be null");

        Map<Method, ProxifiedObject> handles = new IdentityHashMap<>(this.handles.size());
        Map<ProxifiedObject, ProxifiedObject> cache = new IdentityHashMap<>(this.nameMapped.size());

        for (Map.Entry<Method, ProxifiedObject> entry : this.handles.entrySet()) {
            ProxifiedObject unbound = entry.getValue();
            ProxifiedObject bound = cache.get(unbound);
            if (bound == null) {
                MethodHandle insert;
                if (unbound.isStatic || unbound.isConstructor) {
                    insert = unbound.handle;
                } else {
                    try {
                        // insert = MethodHandles.insertArguments(unbound.handle, 0, instance);
                        insert = unbound.handle.bindTo(instance);
                    } catch (Exception e) {
                        throw new IllegalStateException("Failed to bind " + instance + " to " + entry.getKey() + " -> " + unbound.handle + " (static=" + unbound.isStatic + ", constructor=" + unbound.isConstructor + ')', e);
                    }
                }
                bound = new ProxifiedObject(insert, unbound.isStatic, unbound.isConstructor, unbound.rType, unbound.pTypes);
                cache.put(unbound, bound);
            }

            handles.put(entry.getKey(), bound);
        }

        Map<String, ProxifiedObject> nameMapped = new HashMap<>(this.nameMapped.size());
        for (Map.Entry<Method, ProxifiedObject> entry : handles.entrySet()) {
            nameMapped.put(entry.getKey().getName(), entry.getValue());
        }

        ReflectiveProxy<T> bound = new ReflectiveProxy<>(targetClass, proxyClass, instance, handles, nameMapped);
        return bound.createProxy();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("εντολοδόχος")) return this;

        ProxifiedObject reflectedHandle = handles.get(method);

        if (reflectedHandle == null) {
            // This happens because we're using IdentityHashMap
            reflectedHandle = nameMapped.get(method.getName());
            if (reflectedHandle == null) {
                throw new IllegalStateException("Unknown target method: " + method + " (" + method.hashCode() + ") Available: ["
                        + handles.keySet().stream().map(x -> x.getName() + "::" + x.hashCode()).collect(Collectors.joining(", ")) + ']');
            } else {
                // Cache the direct reference
                handles.put(method, reflectedHandle);
            }
        }

        if (!reflectedHandle.isStatic && !reflectedHandle.isConstructor && instance == null)
            throw new IllegalStateException("Cannot invoke non-static non-constructor member handle with when no instance is set");

        if (reflectedHandle.isConstructor && instance != null)
            throw new IllegalStateException("Cannot invoke constructor twice");

        Object result;

        if (reflectedHandle.pTypes != null) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof ReflectiveProxyObject) {
                    // noinspection NonAsciiCharacters
                    args[i] = ((ReflectiveProxyObject) arg).εντολοδόχος().instance();
                }
            }
        }

        try {
            // MethodHandle#invoke is a special case due to its @PolymorphicSignature nature.
            // The signature of the method is simply a placeholder which is replaced by JVM.
            // We use invokeWithArguments which accepts working with Object.class
            if (args == null) result = reflectedHandle.handle.invoke();
            else result = reflectedHandle.handle.invokeWithArguments(args);
        } catch (Throwable ex) {
            throw new IllegalStateException("Failed to execute " + method + " -> "
                    + reflectedHandle.handle + " with args "
                    + (args == null ? "null" : Arrays.stream(args).map(x -> (x == null ? "null" : (x + " (" + x.getClass().getSimpleName() + ')')))), ex);
        }

        if (reflectedHandle.rType != null) {
            result = reflectedHandle.rType.bindTo(result);
        }

        return result;
    }

    @Override
    public String toString() {
        return "ReflectiveProxy(" +
                "proxyClass=" + proxyClass +
                ", proxy=" + proxy +
                ", instance=" + instance +
                ", nameMapped=" + nameMapped +
                ')';
    }
}
