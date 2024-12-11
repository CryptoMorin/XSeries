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

package com.cryptomorin.xseries.reflection.jvm;

import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.PackageHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftMapping;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class should not be used directly.
 * <p>
 * Any reflective JVM object that has a name, like {@link java.lang.reflect.Field} or {@link java.lang.reflect.Method}
 * the other being {@link java.lang.reflect.Constructor} which doesn't have a name.
 */
public abstract class NamedMemberHandle extends MemberHandle implements NamedReflectiveHandle {
    protected final Set<String> names = new HashSet<>(5);

    @NotNull
    @Override
    public Set<String> getPossibleNames() {
        return names;
    }

    protected NamedMemberHandle(ClassHandle clazz) {
        super(clazz);
    }

    public NamedMemberHandle map(MinecraftMapping mapping, @Pattern(PackageHandle.JAVA_IDENTIFIER_PATTERN) String name) {
        this.names.add(name);
        return this;
    }

    public NamedMemberHandle named(@Pattern(PackageHandle.JAVA_IDENTIFIER_PATTERN) String... names) {
        this.names.addAll(Arrays.asList(names));
        return this;
    }

    @Override
    public abstract NamedMemberHandle clone();
}
