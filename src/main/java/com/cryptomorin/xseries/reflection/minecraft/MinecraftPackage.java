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
