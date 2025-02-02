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

package com.cryptomorin.xseries.reflection.minecraft;

import com.cryptomorin.xseries.reflection.ReflectiveNamespace;
import com.cryptomorin.xseries.reflection.jvm.classes.DynamicClassHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.PackageHandle;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Pattern;

/**
 * A specialized type of {@link DynamicClassHandle} which currently doesn't
 * have any extra functionalities.
 */
public class MinecraftClassHandle extends DynamicClassHandle {
    public MinecraftClassHandle(ReflectiveNamespace namespace) {
        super(namespace);
    }

    public MinecraftClassHandle inPackage(MinecraftPackage minecraftPackage) {
        super.inPackage(minecraftPackage);
        return this;
    }

    public MinecraftClassHandle inPackage(MinecraftPackage minecraftPackage, @Pattern(PackageHandle.JAVA_PACKAGE_PATTERN) String packageName) {
        super.inPackage(minecraftPackage, packageName);
        return this;
    }

    @Override
    public MinecraftClassHandle inner(@Language(value = "Java", suffix = "{}") String declaration) {
        return inner(namespace.ofMinecraft(declaration));
    }

    public MinecraftClassHandle named(@Pattern(PackageHandle.JAVA_IDENTIFIER_PATTERN) String... clazzNames) {
        super.named(clazzNames);
        return this;
    }

    public MinecraftClassHandle map(MinecraftMapping mapping, @Pattern(PackageHandle.JAVA_IDENTIFIER_PATTERN) String className) {
        this.classNames.add(className);
        return this;
    }

    @Override
    public MinecraftClassHandle copy() {
        MinecraftClassHandle handle = new MinecraftClassHandle(namespace);
        handle.array = this.array;
        handle.parent = this.parent;
        handle.packageName = this.packageName;
        handle.classNames.addAll(this.classNames);
        return handle;
    }
}
