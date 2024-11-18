package com.cryptomorin.xseries.reflection;

import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.DynamicClassHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.StaticClassHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftClassHandle;
import com.cryptomorin.xseries.reflection.parser.ReflectionParser;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * This class is mostly only useful if you're planning to use string-based API (see {@link ReflectionParser}),
 * other than that most of the work is done behind the scenes, and it's not needed to use it directly.
 * You can initiate this class using {@link XReflection#namespaced()}. Like other reflection classes, this should
 * be used as a temporary class and you should cache the results of {@link ReflectiveHandle} classes.
 * <p>
 * This class is used just like {@link com.cryptomorin.xseries.reflection.XReflection} except that it
 * allows enhanced performance and security checks. Performance because a single reflection lookup
 * object is used for all reflections that use this namespace, security because of the same lookup
 * object, the {@link MethodHandles#lookup()} which is caller sensitive.
 * <p>
 * This class also provides an import statement feature. For example look at the following code:
 * <pre>{@code
 *     XReflection.of(Test.class).method("public List<String> getNames();").unreflect();
 * }</pre>
 * Assuming that the class and the method exist, this works, it knows about the "List" type from {@link java.util.List}
 * because it's predefined and hardcoded. But if we look at another code:
 * <pre>{@code
 *      XReflection.of(Test.class).method("public MyCustomClass getCustomData();").unreflect();
 * }</pre>
 *  This will fail, because it doesn't know where {@code MyCustomClass} is, you could give it the fully qualified name:
 * <pre>{@code
 *      XReflection.of(Test.class).method("public my.package.MyCustomClass getCustomData();").unreflect();
 * }</pre>
 * But what if you want to keep using this type a lot? It makes the code look very ugly. That is what this class is for:
 * <pre>{@code
 *      ReflectiveNamespace ns = XReflection.namespaced().imports(MyCustomClass.class);
 *      ns.of(Test.class).method("public MyCustomClass getCustomData();").unreflect();
 * }</pre>
 * <br>
 * Also, all the types that are passed to or parsed from this namespace
 * (e.g. from {@link #of(Class)}, {@link #ofMinecraft(String)}, {@link #classHandle(String)})
 * are imported automatically. Making this a powerful mini-IDE!
 * <p><br>
 * Note that sometimes you need to import remapped classes manually if you're going to be using {@link #of(Class)}
 * since these class names are going to be remapped at runtime, and if you use the obfuscated names in
 * string-based API signatures, it'll fail:
 * <pre>{@code
 *      ReflectiveNamespace ns = XReflection.namespaced();
 *
 *      // If you don't do the following, then this whole thing won't work.
 *      // The reason why adding this manual import works is because "MinecraftKey" class
 *      // will be remapped to "ResourceLocation" at runtime, so the following code will be
 *      // translated to ns.imports("MinecraftKey", ResourceLocation.class);
 *      ns.imports("MinecraftKey", MinecraftKey.class);
 *
 *      // Alternatively you could just use "ResourceLocation" instead of "MinecraftKey" here in the string.
 *      ns.of(MinecraftKey.class).method("public static MinecraftKey fromNamespaceAndPath(String namespace, String path);").unreflect();
 * }</pre>
 */
public class ReflectiveNamespace {
    private final Map<String, Class<?>> imports = new HashMap<>();
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private final Set<ClassHandle> handles = Collections.newSetFromMap(new IdentityHashMap<>());

    protected ReflectiveNamespace() {}

    /**
     * Imports the specified classes into this namespace so the names can be used directly.
     * (For more info read {@link ReflectiveNamespace}.
     * <p>
     * This can also override predefined Java standard types if the names are the same.
     */
    public ReflectiveNamespace imports(@NotNull Class<?>... classes) {
        for (Class<?> clazz : classes) {
            imports(clazz.getSimpleName(), clazz);
        }
        return this;
    }

    /**
     * Imports a class with a custom name.
     * @param name the custom name of the class.
     * @param clazz the actual definition of the class.
     * @see #imports(Class[])
     */
    public ReflectiveNamespace imports(@NotNull String name, @NotNull Class<?> clazz) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(clazz);
        this.imports.put(name, clazz);
        return this;
    }

    @NotNull
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
    public void link(ClassHandle handle) {
        if (handle.getNamespace() != this) throw new IllegalArgumentException("Not the same namespace");
        this.handles.add(handle);
    }

    @NotNull
    @ApiStatus.Internal
    public MethodHandles.Lookup getLookup() {
        return lookup;
    }


    /**
     * Same as {@link XReflection#of(Class)}
     * @since v11.0.0
     */
    public StaticClassHandle of(Class<?> clazz) {
        imports(clazz);
        return new StaticClassHandle(this, clazz);
    }

    /**
     * Similar to {@link XReflection#classHandle()}
     * @since v11.0.0
     */
    public DynamicClassHandle classHandle(@Language("Java") String declaration) {
        DynamicClassHandle classHandle = new DynamicClassHandle(this);
        return new ReflectionParser(declaration).imports(this).parseClass(classHandle);
    }

    /**
     * Similar to {@link XReflection#ofMinecraft()}
     * @since v11.0.0
     */
    public MinecraftClassHandle ofMinecraft(@Language("Java") String declaration) {
        MinecraftClassHandle classHandle = new MinecraftClassHandle(this);
        return new ReflectionParser(declaration).imports(this).parseClass(classHandle);
    }
}
