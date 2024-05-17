package com.cryptomorin.xseries.reflection.minecraft;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.classes.DynamicClassHandle;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class MinecraftClassHandle extends DynamicClassHandle {
    public MinecraftClassHandle inPackage(MinecraftPackage minecraftPackage) {
        return inPackage(minecraftPackage, "");
    }

    public MinecraftClassHandle inPackage(MinecraftPackage minecraftPackage, String packageName) {
        this.packageName = minecraftPackage.getPackageId();
        if (!packageName.isEmpty() && (minecraftPackage != MinecraftPackage.NMS || XReflection.supports(17))) {
            this.packageName += '.' + packageName;
        }
        return this;
    }

    public MinecraftClassHandle named(String... clazzNames) {
        super.named(clazzNames);
        return this;
    }

    public MinecraftClassHandle map(MinecraftMapping mapping, String className) {
        this.classNames.add(className);
        return this;
    }
}
