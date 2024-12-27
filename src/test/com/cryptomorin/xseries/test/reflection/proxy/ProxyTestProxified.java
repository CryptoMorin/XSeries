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

package com.cryptomorin.xseries.test.reflection.proxy;

import com.cryptomorin.xseries.reflection.proxy.ReflectiveProxyObject;
import com.cryptomorin.xseries.reflection.proxy.annotations.Class;
import com.cryptomorin.xseries.reflection.proxy.annotations.*;
import org.jetbrains.annotations.NotNull;

@Class(target = ProxyTestClass.class)
public interface ProxyTestProxified extends ReflectiveProxyObject {
    // Fields
    @Static
    @Final
    @Field
    int finalId();

    @Static
    @Field
    int id();

    @Field
    int date();

    @Private
    @Field
    String operationField();

    @Static
    StringBuilder doStaticThings(int times);

    // Constructors
    @SuppressWarnings("MethodNameSameAsClassName")
    @Constructor
    ProxyTestClass ProxyTestProxified(String operationField, int date);

    @SuppressWarnings("MethodNameSameAsClassName")
    @Constructor
    ProxyTestProxified ProxyTestProxified(String operationField);

    @SuppressWarnings("MethodNameSameAsClassName")
    @Private
    @Constructor
    ProxyTestProxified ProxyTestProxified(int date);

    @Private
    StringBuilder doSomethingPrivate(int length);

    // Methods
    @ReflectName("doSomething")
    void iForgotTheName(String add, boolean add2);

    // Overload test
    String getSomething(String add, boolean add2);

    String getSomething(String add, short add2);

    String getSomething(String add);

    int getSomething(String add, int add2);

    @Override
    @NotNull
    ProxyTestProxified bindTo(@NotNull Object instance);
}
