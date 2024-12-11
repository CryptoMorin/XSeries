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

package com.cryptomorin.xseries.reflection.minecraft;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.classes.PackageHandle;
import org.intellij.lang.annotations.Pattern;

/**
 * Common Minecraft packages.
 */
public enum MinecraftPackage implements PackageHandle {
    NMS(XReflection.NMS_PACKAGE), CB(XReflection.CRAFTBUKKIT_PACKAGE),
    BUKKIT("org.bukkit"), SPIGOT("org.spigotmc");

    private final String packageId;

    MinecraftPackage(String packageName) {this.packageId = packageName;}

    @Override
    public String packageId() {
        return name();
    }

    @Override
    public String getBasePackageName() {
        return packageId;
    }

    @Override
    public String getPackage(@Pattern(PackageHandle.JAVA_PACKAGE_PATTERN) String packageName) {
        if (packageName.startsWith(".") || packageName.endsWith("."))
            throw new IllegalArgumentException("Package name must not start or end with a dot: " + packageName + " (" + this + ')');
        if (!packageName.isEmpty() && (this != MinecraftPackage.NMS || XReflection.supports(17))) {
            return packageId + '.' + packageName;
        }
        return packageId;
    }
}
