package com.cryptomorin.xseries.reflection.minecraft.capabilities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class MinecraftCapability {
    protected final Set<? extends MinecraftCapability> dependencies = new HashSet<>();

    public Set<? extends MinecraftCapability> getDependencies() {
        return Collections.unmodifiableSet(dependencies);
    }

    public boolean dependsOn(Class<? extends MinecraftCapability> minecraftCapability) {
        for (MinecraftCapability dependency : dependencies) {
            if (dependency.getClass() == minecraftCapability) return true;
            if (dependency.dependsOn(minecraftCapability)) return true;
        }
        return false;
    }
}
