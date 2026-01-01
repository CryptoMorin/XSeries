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

package com.cryptomorin.xseries.reflection.proxy;

import com.cryptomorin.xseries.reflection.XAccessFlag;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.objects.ReflectedObject;
import com.cryptomorin.xseries.reflection.proxy.annotations.Constructor;
import com.cryptomorin.xseries.reflection.proxy.annotations.Field;
import com.cryptomorin.xseries.reflection.proxy.annotations.Proxify;
import com.cryptomorin.xseries.reflection.proxy.processors.MappedType;
import com.cryptomorin.xseries.reflection.proxy.processors.ProxyMethodInfo;
import com.cryptomorin.xseries.reflection.proxy.processors.ReflectiveAnnotationProcessor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The basis of this class is that you create an {@code interface} class and annotate it using
 * {@link Proxify Class}, {@link Field}, {@link Constructor}, etc... annotations to mark how
 * these methods are resolved for different versions. Everything is accessed through methods,
 * even constructors and fields are accessed in forms of methods.
 * <p>
 * Due to the fact that this method uses annotations and Java's {@link Proxy} mechanism, the startup
 * and the invocation of the methods is pretty slow compared to the normal {@link XReflection} APIs.
 * <p>
 * It doesn't seem like we can use {@link java.lang.invoke.LambdaMetafactory LambdaMetafactory} for generating this
 * type of interface since they're not functional interfaces.
 *
 * @see ReflectiveProxyObject
 * @see com.cryptomorin.xseries.reflection.asm.XReflectASM
 * @see XReflection#proxify(Class)
 * @since 13.0.0
 */
@ApiStatus.Internal
public final class ReflectiveProxy<T extends ReflectiveProxyObject> implements InvocationHandler {
    private static final Map<Class<?>, ReflectiveProxy<?>> PROXIFIED_CLASS_LOADER0 = new IdentityHashMap<>();
    private static final ClassLoader CLASS_LOADER = ReflectiveProxy.class.getClassLoader();

    private final Class<?> targetClass;
    private final Class<T> proxyClass;
    private T proxy;
    private final Object instance;

    private final Map<Method, ProxifiedObject> handles;
    private final ClassOverloadedMethods<ProxifiedObject> nameMapped;

    @SuppressWarnings("unchecked")
    public static <T extends ReflectiveProxyObject> ReflectiveProxy<T> proxify(Class<T> interfaceClass) {
        ReflectiveProxy<?> proxified = PROXIFIED_CLASS_LOADER0.get(interfaceClass);
        if (proxified != null) return (ReflectiveProxy<T>) proxified;

        ReflectiveAnnotationProcessor processor = new ReflectiveAnnotationProcessor(interfaceClass);
        processor.process(ReflectiveProxy::descriptorProcessor);

        Set<Map.Entry<String, OverloadedMethod<ProxyMethodInfo>>> entries = processor.getMapped().mappings().entrySet();
        Map<Method, ProxifiedObject> handles = new IdentityHashMap<>(entries.size());
        OverloadedMethod.Builder<ProxifiedObject> nameMapped =
                new OverloadedMethod.Builder<>(ReflectiveProxy::descriptorProcessor);

        ReflectiveProxy<T> proxy = new ReflectiveProxy<>(processor.getTargetClass(), interfaceClass, null,
                handles, nameMapped.build());

        // Circular dependency
        PROXIFIED_CLASS_LOADER0.put(interfaceClass, proxy);

        for (Map.Entry<String, OverloadedMethod<ProxyMethodInfo>> mapping : entries) {
            for (ProxyMethodInfo overload : mapping.getValue().getOverloads()) {
                ReflectedObject jvm = overload.handle.jvm().unreflect();

                MethodHandle methodHandle = (MethodHandle) overload.handle.unreflect();
                methodHandle = createDynamicProxy(null, methodHandle);

                ProxifiedObject proxifiedObj = new ProxifiedObject(
                        methodHandle,
                        overload,
                        jvm.accessFlags().contains(XAccessFlag.STATIC),
                        jvm.type() == ReflectedObject.Type.CONSTRUCTOR,
                        overload.rType.isDifferent() ? proxify((Class<? extends ReflectiveProxyObject>) overload.rType.synthetic) : null,
                        Arrays.stream(overload.pTypes).anyMatch(MappedType::isDifferent) ?
                                Arrays.stream(overload.pTypes)
                                        .map(x -> x.isDifferent() ? proxify((Class<? extends ReflectiveProxyObject>) x.synthetic) : null)
                                        .toArray(ReflectiveProxy[]::new) :
                                null);

                // It appears that the Method object inside invoke that comes from the interface is not
                // identical to these methods, perhaps it's because the overridden method is different.
                // This is proved by the proxy.proxy.getClass().getInterfaces() below
                // Methods called from INVOKEINTERFACE
                handles.put(overload.interfaceMethod, proxifiedObj);
                nameMapped.add(proxifiedObj, mapping.getKey());
            }
        }

        nameMapped.build(proxy.nameMapped.mappings());
        proxy.proxy = proxy.createProxy();

        // Cache proxy methods (read invoke() for more info)
        // However, this cache is still not enough, and the method identity cache needs to
        // be manually updated.
        // Methods called from INVOKEVIRTUAL
        for (Method proxyMethod : proxy.proxy.getClass().getDeclaredMethods()) {
            ProxifiedObject matched = proxy.nameMapped.get(proxyMethod.getName(), () -> descriptorProcessor(proxyMethod), true);
            if (matched != null) handles.put(proxyMethod, matched);
        }
        for (Class<?> proxyInterfaces : proxy.proxy.getClass().getInterfaces()) {
            for (Method proxyMethod : proxyInterfaces.getDeclaredMethods()) {
                ProxifiedObject matched = proxy.nameMapped.get(proxyMethod.getName(), () -> descriptorProcessor(proxyMethod), true);
                if (matched != null) handles.put(proxyMethod, matched);
            }
        }

        return proxy;
    }

    private static MethodHandle createDynamicProxy(@Nullable Object bindInstance, MethodHandle methodHandle) {
        int parameterCount = methodHandle.type().parameterCount();
        int requireArgs = bindInstance != null ? 1 : 0;

        // bind the only parameter left and remove it.
        if (bindInstance != null) methodHandle = methodHandle.bindTo(bindInstance);

        if (parameterCount == requireArgs) {
            return methodHandle.asType(MethodType.methodType(Object.class));
        } else {
            return methodHandle
                    .asSpreader(Object[].class, parameterCount - requireArgs)
                    .asType(MethodType.methodType(Object.class, Object[].class));
        }
    }

    private static String descriptorProcessor(ProxifiedObject obj) {
        // We can't use the MethodHandle here because the parameter list might contain the descriptor for the receiver object.
        return OverloadedMethod.getParameterDescriptor(MappedType.getRealTypes(obj.proxyMethodInfo.pTypes));
    }

    private static String descriptorProcessor(ProxyMethodInfo obj) {
        return OverloadedMethod.getParameterDescriptor(MappedType.getRealTypes(obj.pTypes));
    }

    private static String descriptorProcessor(Method method) {
        return OverloadedMethod.getParameterDescriptor(method.getParameterTypes());
    }

    public static final class ProxifiedObject {
        private final MethodHandle handle;
        private final ProxyMethodInfo proxyMethodInfo;
        private final boolean isStatic, isConstructor;
        private final ReflectiveProxy<?> rType;
        private final ReflectiveProxy<?>[] pTypes;

        public ProxifiedObject(MethodHandle handle, ProxyMethodInfo proxyMethodInfo,
                               boolean isStatic, boolean isConstructor,
                               ReflectiveProxy<?> rType, ReflectiveProxy<?>[] pTypes) {
            this.handle = handle;
            this.proxyMethodInfo = proxyMethodInfo;
            this.isStatic = isStatic;
            this.isConstructor = isConstructor;
            this.rType = rType;
            this.pTypes = pTypes;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + '(' + proxyMethodInfo.interfaceMethod + ')';
        }
    }

    private ReflectiveProxy(Class<?> targetClass, Class<T> proxyClass, Object instance,
                            Map<Method, ProxifiedObject> handles,
                            ClassOverloadedMethods<ProxifiedObject> nameMapped) {
        this.targetClass = targetClass;
        this.proxyClass = proxyClass;
        this.instance = instance;
        this.handles = handles;
        this.nameMapped = nameMapped;
    }

    public static void checkInterfaceClass(Class<?> interfaceClass) {
        Objects.requireNonNull(interfaceClass, "Interface class is null");

        if (!interfaceClass.isInterface())
            throw new IllegalArgumentException("Cannot proxify non-interface class: " + interfaceClass);

        if (!ReflectiveProxyObject.class.isAssignableFrom(interfaceClass))
            throw new IllegalArgumentException("The provided interface class must extend ReflectiveProxyObject interface");
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    public @NotNull T createProxy() {
        // The generated class for proxyClass is already cached by the proxy handler and its
        // constructor is reused for subsequent calls.
        return (T) Proxy.newProxyInstance(CLASS_LOADER, new Class[]{this.proxyClass}, this);
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
     *
     * @param instance the instance to bind.
     */
    @NotNull
    public T bindTo(@NotNull Object instance) {
        if (this.instance != null)
            throw new IllegalStateException("This proxy object already has an instance bound to it: " + this);

        Objects.requireNonNull(instance, "Instance cannot be null");
        if (!this.targetClass.isAssignableFrom(instance.getClass()))
            throw new IllegalArgumentException("The given instance doesn't match the target class: " + instance + " -> " + this);

        // TODO optimize the copying...
        Map<Method, ProxifiedObject> handles = new IdentityHashMap<>(this.handles.size());
        Map<ProxifiedObject, ProxifiedObject> boundStates = new IdentityHashMap<>(this.nameMapped.mappings().size());
        OverloadedMethod.Builder<ProxifiedObject> nameMapped = new OverloadedMethod.Builder<>(ReflectiveProxy::descriptorProcessor);

        for (Map.Entry<String, OverloadedMethod<ProxifiedObject>> entry : this.nameMapped.mappings().entrySet()) {
            for (ProxifiedObject overload : entry.getValue().getOverloads()) {
                // noinspection UnnecessaryLocalVariable
                ProxifiedObject unbound = overload;
                ProxifiedObject bound = boundStates.get(unbound);
                if (bound == null) {
                    MethodHandle insert;
                    if (unbound.isStatic || unbound.isConstructor) {
                        nameMapped.add(unbound, entry.getKey());
                        continue;
                    } else {
                        try {
                            // insert = MethodHandles.insertArguments(unbound.handle, 0, instance);
                            // This is cached, no worries.
                            insert = (MethodHandle) unbound.proxyMethodInfo.handle.unreflect();

                            // We already checked for static and constructor members, all these handles are for instance members now.
                            if (insert.type().parameterCount() == 0) {
                                throw new IllegalStateException("Non-static, non-constructor with 0 arguments found: " + insert);
                            } else {
                                insert = createDynamicProxy(instance, insert);
                            }
                        } catch (Exception e) {
                            throw new IllegalStateException("Failed to bind " + instance + " to " + entry.getKey() + " -> " + unbound.handle + " (static=" + unbound.isStatic + ", constructor=" + unbound.isConstructor + ')', e);
                        }
                    }
                    bound = new ProxifiedObject(insert, unbound.proxyMethodInfo, unbound.isStatic, unbound.isConstructor, unbound.rType, unbound.pTypes);
                    boundStates.put(unbound, bound);
                }
                nameMapped.add(bound, entry.getKey());
            }
        }
        for (Map.Entry<Method, ProxifiedObject> entry : this.handles.entrySet()) {
            ProxifiedObject unbound = entry.getValue();
            if (unbound.isStatic || unbound.isConstructor) {
                handles.put(entry.getKey(), unbound);
            } else {
                ProxifiedObject bound = boundStates.get(unbound);
                if (bound == null) {
                    throw new IllegalStateException("Cannot find bound method for " + entry.getKey() + " (" + unbound + "::" + unbound.hashCode() + ") in "
                            + nameMapped.build() + " - " + boundStates.entrySet().stream()
                            .map(x -> x.getKey() + "::" + x.hashCode()).collect(Collectors.toList()));
                }
                handles.put(entry.getKey(), bound);
            }
        }

        ReflectiveProxy<T> bound = new ReflectiveProxy<>(targetClass, proxyClass, instance, handles, nameMapped.build());
        return bound.createProxy();
    }

    private static String getMethodList(Class<?> clazz, boolean declaredOnly) {
        return Arrays.stream(declaredOnly ? clazz.getDeclaredMethods() : clazz.getMethods())
                .map(x -> x.getName() + "::" + System.identityHashCode(x))
                .collect(Collectors.toList())
                .toString();
    }

    @Override
    public Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
        { // ReflectiveProxyObject & Object methods
            int paramCount = method.getParameterCount();
            String name = method.getName();

            if (paramCount == 0) {
                switch (name) {
                    case "instance":
                        return instance;
                    case "toString":
                        return instance == null ? proxyClass.toString() : instance.toString();
                    case "hashCode":
                        return instance == null ? proxyClass.hashCode() : instance.hashCode();
                    case "notify":
                        if (instance == null) proxyClass.notify();
                        else instance.notify();
                        return null;
                    case "notifyAll":
                        if (instance == null) proxyClass.notifyAll();
                        else instance.notifyAll();
                        return null;
                    case "wait":
                        if (instance == null) proxyClass.wait();
                        else instance.wait();
                        return null;
                    case "getTargetClass":
                        return targetClass;
                }
            } else if (paramCount == 1) {
                switch (name) {
                    case "bindTo":
                        return bindTo(args[0]);
                    case "isInstance":
                        return targetClass.isInstance(args[0]);
                    case "equals":
                        return instance == null ? proxyClass == args[0] : instance.equals(args[0]);
                    case "wait":
                        if (instance == null) proxyClass.wait((long) args[0]);
                        else instance.wait((long) args[0]);
                        return null;
                }
            } else if (paramCount == 2) {
                if (name.equals("wait")) {
                    if (instance == null) proxyClass.wait((long) args[0], (int) args[1]);
                    else instance.wait((long) args[0], (int) args[1]);
                    return null;
                }
            }
        }

        ProxifiedObject reflectedHandle = handles.get(method);

        if (reflectedHandle == null) {
            // This happens because we're using IdentityHashMap, and the method that belongs to
            // the proxy class that was generated isn't exactly identical to the interface method
            // that was overriden.
            // However, this should still not happen as we're checking the proxy methods as well,
            // this is just here to handle implementation-specific behaviors that might occur.
            reflectedHandle = nameMapped.get(method.getName(), () -> descriptorProcessor(method));

            // Exception is already thrown by get() if it's not found.

            // Cache the direct reference
            handles.put(method, reflectedHandle);
        }

        if (!reflectedHandle.isStatic && !reflectedHandle.isConstructor && instance == null)
            throw new IllegalStateException("Cannot invoke non-static non-constructor member handle with when no instance is set");

        if (reflectedHandle.isConstructor && instance != null)
            throw new IllegalStateException("Cannot invoke constructor twice");

        Object result;

        if (reflectedHandle.pTypes != null && args != null) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof ReflectiveProxyObject) {
                    args[i] = ((ReflectiveProxyObject) arg).instance();
                }
            }
        }

        try {
            // MethodHandle#invoke is a special case due to its @PolymorphicSignature nature.
            // The signature of the method is simply a placeholder which is replaced by JVM.
            // We use invokeWithArguments which accepts working with Object.class
            // But we already changed the method signature to (Object[])Object so we can safely
            // use invokeExact()
            if (args == null) {
                result = reflectedHandle.handle.invokeExact();
            } else {
                // result = reflectedHandle.handle.invokeWithArguments(args);
                result = reflectedHandle.handle.invoke(args);
            }
        } catch (Throwable ex) {
            throw new IllegalStateException("Failed to execute " + method + " -> "
                    + reflectedHandle.handle + " with args "
                    + (args == null ? "null" : Arrays.stream(args).map(x -> (x == null ? "null" : (x + " (" + x.getClass().getSimpleName() + ')')))), ex);
        }

        if (reflectedHandle.rType != null) {
            // assert reflectedHandle.rType == this;
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
