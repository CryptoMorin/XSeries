package com.cryptomorin.xseries.reflection.minecraft;

import com.cryptomorin.xseries.reflection.XReflection;

public enum MinecraftPackage {
    NMS(XReflection.NMS_PACKAGE), CB(XReflection.CRAFTBUKKIT_PACKAGE), SPIGOT("org.spigotmc");

    private final String packageId;

    MinecraftPackage(String packageName) {this.packageId = packageName;}

    public String getPackageId() {
        return packageId;
    }
}
