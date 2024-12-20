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

package com.cryptomorin.xseries.reflection.jvm.objects;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

abstract class AbstractReflectedObject implements ReflectedObject {
    @Override
    public abstract AnnotatedElement unreflect();

    @Override
    public final <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return unreflect().getAnnotation(annotationClass);
    }

    @Override
    public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return unreflect().isAnnotationPresent(annotationClass);
    }

    @Override
    public final <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
        return unreflect().getAnnotationsByType(annotationClass);
    }

    @Override
    public final Annotation[] getAnnotations() {
        return unreflect().getAnnotations();
    }

    @Override
    public final <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass) {
        return unreflect().getDeclaredAnnotation(annotationClass);
    }

    @Override
    public final <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationClass) {
        return unreflect().getDeclaredAnnotationsByType(annotationClass);
    }

    @Override
    public final Annotation[] getDeclaredAnnotations() {
        return unreflect().getDeclaredAnnotations();
    }

    @Override
    public final String toString() {
        return this.getClass().getSimpleName() + '(' + unreflect() + ')';
    }
}