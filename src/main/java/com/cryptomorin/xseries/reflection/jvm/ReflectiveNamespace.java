package com.cryptomorin.xseries.reflection.jvm;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.DynamicClassHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.StaticClassHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftClassHandle;
import com.cryptomorin.xseries.reflection.parser.ReflectionParser;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandles;
import java.util.*;

@ApiStatus.Experimental
public class ReflectiveNamespace {
    private final Map<String, Class<?>> imports = new HashMap<>();
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private final Set<ClassHandle> handles = Collections.newSetFromMap(new IdentityHashMap<>());

    public ReflectiveNamespace imports(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            this.imports.put(clazz.getSimpleName(), clazz);
        }
        return this;
    }

    public ReflectiveNamespace imports(String name, Class<?> clazz) {
        this.imports.put(name, clazz);
        return this;
    }

    @ApiStatus.Internal
    public Map<String, Class<?>> getImports() {
        for (ClassHandle handle : handles) {
            Class<?> clazz = handle.reflectOrNull();
            if (clazz == null) continue;

            for (String className : handle.getPossibleNames()) {
                this.imports.put(className, clazz);
            }
        }
        return this.imports;
    }

    @ApiStatus.Internal
    public MethodHandles.Lookup getLookup() {
        return lookup;
    }

    /**
     * @since v11.0.0
     */
    @ApiStatus.Experimental
    public StaticClassHandle of(Class<?> clazz) {
        imports(clazz);
        StaticClassHandle handle = new StaticClassHandle(clazz);
        handle.setNamespace(this);
        return handle;
    }

    public void link(ClassHandle handle) {
        if (handle.getNamespace() != this) throw new IllegalArgumentException("Not the same namespace");
        this.handles.add(handle);
    }

    public void unlink(ClassHandle handle) {
        if (handle.getNamespace() == this) throw new IllegalArgumentException("Same namespace");
        this.handles.remove(handle);
    }

    @ApiStatus.Experimental
    public DynamicClassHandle classHandle(@Language("Java") String declaration) {
        DynamicClassHandle classHandle = XReflection.classHandle();
        classHandle.setNamespace(this);
        return new ReflectionParser(declaration).imports(this).parseClass(classHandle);
    }

    @ApiStatus.Experimental
    public MinecraftClassHandle ofMinecraft(@Language("Java") String declaration) {
        MinecraftClassHandle classHandle = XReflection.ofMinecraft();
        classHandle.setNamespace(this);
        return new ReflectionParser(declaration).imports(this).parseClass(classHandle);
    }
}
