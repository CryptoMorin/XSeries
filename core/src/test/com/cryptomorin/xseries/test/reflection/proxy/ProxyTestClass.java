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

package com.cryptomorin.xseries.test.reflection.proxy;

import com.cryptomorin.xseries.reflection.jvm.objects.ReflectedObject;
import org.jetbrains.annotations.NotNull;

@TestAnnotation(reference = String.class, filter = true, type = ReflectedObject.Type.FIELD)
@TestAnnotation2({100, 200})
public class ProxyTestClass {
    public static final int finalId = 555;
    public static int id = 500;
    public int date;
    private String operationField;

    public ProxyTestClass(String operationField) {
        this(operationField, 2025);
    }

    public ProxyTestClass(String operationField, int date) {
        this.operationField = operationField;
        this.date = date;
    }

    protected static boolean isBeyond555() {
        return id > 555;
    }

    @Deprecated
    @TestAnnotation(values = {@TestAnnotation2(3), @TestAnnotation2({4, 10})})
    @TestAnnotation(index = 1)
    @TestAnnotation(name = "toaster")
    @TestAnnotation(reference = String.class, filter = true, type = ReflectedObject.Type.FIELD)
    private ProxyTestClass(@TestAnnotation(index = 3) int date) {
        this("OperationPrimus", 2027);
    }

    public int compareTo(@Deprecated @NotNull ProxyTestClass other) {
        return Integer.compare(this.date, other.date);
    }

    public static StringBuilder doStaticThings(int times) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; i++) {
            builder.append(i);
        }
        return builder;
    }

    @SuppressWarnings("MethodMayBeStatic")
    private StringBuilder doSomethingPrivate(int length) {
        return new StringBuilder(length).append(operationField).append(finalId);
    }

    public void doSomething(String add, boolean add2) {
        operationField += "doSomething" + add + add2;
    }

    public String getSomething(String add, boolean add2) {
        return operationField + add + add2;
    }

    public String getSomething(String add, short add2) {
        return add + add2;
    }

    public String getSomething(String add) throws IllegalArgumentException, IllegalStateException {
        if ("!".equals(add)) throw new IllegalArgumentException("Invalid argument: " + add);
        if (operationField == null) throw new IllegalStateException("Operation is not set: " + add);

        return add + add;
    }

    public int getSomething(String add, int add2) {
        return Integer.parseInt(add) * add2;
    }

    public static String getSomething0(String add, boolean add2) {
        return "aaaa" + add + add2;
    }
}
