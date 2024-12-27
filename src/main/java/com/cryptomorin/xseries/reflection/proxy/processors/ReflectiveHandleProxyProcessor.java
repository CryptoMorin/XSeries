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
import com.cryptomorin.xseries.reflection.jvm.ConstructorMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.FieldMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.MethodMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;
import com.cryptomorin.xseries.reflection.proxy.ReflectiveProxy;
import com.cryptomorin.xseries.reflection.proxy.ReflectiveProxyObject;
import com.cryptomorin.xseries.reflection.proxy.annotations.Ignore;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@ApiStatus.Internal
public class ReflectiveHandleProxyProcessor {
    public static <T extends ReflectiveProxyObject> T proxify(@NotNull Class<T> interfaceClass,
                                                              @NotNull ClassHandle targetClass,
                                                              @NotNull ReflectiveHandle<?>... handles) {
        Set<ReflectiveHandle<?>> remainingHandles = Collections.newSetFromMap(new IdentityHashMap<>());
        remainingHandles.addAll(Arrays.asList(handles));

        Method[] interfaceMethods = interfaceClass.getMethods();
        Map<Method, ReflectiveProxy.ProxifiedObject> mappedHandles = new IdentityHashMap<>(interfaceMethods.length);
        for (Method method : interfaceMethods) {
            if (ReflectiveAnnotationProcessor.isAnnotationInherited(interfaceClass, method, Ignore.class)) continue;

            String name = method.getName();
            Iterator<ReflectiveHandle<?>> iter = remainingHandles.iterator();
            while (iter.hasNext()) {
                ReflectiveHandle<?> next = iter.next();
                if (next instanceof FieldMemberHandle) {
                    FieldMemberHandle field = (FieldMemberHandle) next;
                    if (field.getPossibleNames().stream().anyMatch(x -> x.equals(name))) {
                        iter.remove();
                        mappedHandles.put(method, new ReflectiveProxy.ProxifiedObject(
                                field.unreflect(), null,
                                field.getAccessFlags().contains(XAccessFlag.STATIC), false, null, null));
                        break;
                    }
                } else if (next instanceof MethodMemberHandle) {
                    MethodMemberHandle methodHandle = (MethodMemberHandle) next;
                    if (methodHandle.getPossibleNames().stream().anyMatch(x -> x.equals(name))) {
                        iter.remove();
                        mappedHandles.put(method, new ReflectiveProxy.ProxifiedObject(
                                methodHandle.unreflect(), null,
                                methodHandle.getAccessFlags().contains(XAccessFlag.STATIC), false, null, null));
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
                    mappedHandles.put(method, new ReflectiveProxy.ProxifiedObject(
                            ctor.unreflect(), null, false, true, null, null));
                    break;
                }
            }
        }

        // ReflectiveProxy<T> reflectiveProxy = new ReflectiveProxy<>(targetClass.unreflect(), interfaceClass, null, mappedHandles);
        // reflectiveProxy.proxy = reflectiveProxy.createProxy();
        // ReflectiveProxy.PROXIFIED_CLASS_LOADER.put(interfaceClass, reflectiveProxy.proxy);
        // return reflectiveProxy.proxy;
        return null;
    }
}
