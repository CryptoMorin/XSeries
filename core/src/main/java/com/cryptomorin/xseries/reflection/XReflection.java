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
package com.cryptomorin.xseries.reflection;

import com.cryptomorin.xseries.reflection.aggregate.AggregateReflectiveHandle;
import com.cryptomorin.xseries.reflection.aggregate.AggregateReflectiveSupplier;
import com.cryptomorin.xseries.reflection.aggregate.VersionHandle;
import com.cryptomorin.xseries.reflection.asm.XReflectASM;
import com.cryptomorin.xseries.reflection.constraint.ReflectiveConstraint;
import com.cryptomorin.xseries.reflection.jvm.MethodMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.DynamicClassHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.StaticClassHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftClassHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftMapping;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import com.cryptomorin.xseries.reflection.parser.ReflectionParser;
import com.cryptomorin.xseries.reflection.proxy.ReflectiveProxy;
import com.cryptomorin.xseries.reflection.proxy.ReflectiveProxyObject;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * General Java reflection handler, but specialized for Minecraft NMS/CraftBukkit reflection as well.
 * <p>
 * <h2>Starting Points</h2>
 * Basic reflection starting points are through the {@link com.cryptomorin.xseries.reflection.jvm.classes.ClassHandle}
 * methods:
 * <ul>
 *     <li>{@link #of(Class)}: For static classes with known type at compile time.</li>
 *     <li>{@link #classHandle()}: For general classes that have unknown type at compile time.</li>
 *     <li>{@link #ofMinecraft()}: Specialized for Minecraft-related classes.</li>
 *     <li>{@link #namespaced()}: String-based API for getting classes with Java code inside strings that is more readable.</li>
 * </ul>
 * <h2>Fallback</h2>
 * Some methods exist to choose between different values depending on the situation:
 * <ul>
 *     <li>{@link #v(int, Object)}: Basic Minecraft version-based value handler.</li>
 *     <li>{@link #any(ReflectiveHandle[])} and {@link #anyOf(Callable[])}: Advanced fallback-based support for all the reflection operations.</li>
 * </ul>
 *     <h2>Others</h2>
 * Also, there are a few other non-reflection APIs in this class that are a bit "hacky" which is why they're here.
 * <ul>
 *     <li>{@link #getVersionInformation()}: Useful string to include in your reflection related errors.</li>
 *     <li>{@link #throwCheckedException(Throwable)}: Force throw checked exceptions as unchecked.</li>
 *     <li>{@link #stacktrace(CompletableFuture)}: Add stacktrace information to {@link CompletableFuture}s.</li>
 *     <li>{@link #relativizeSuppressedExceptions(Throwable)}: Relativize the stacktrace of exceptions that are thrown from the same location.</li>
 * </ul>
 * <h2>XReflection API Stages</h2>
 * In general, XReflection's API is divided into five stages:
 * <ul>
 *     <li>
 *         <strong>Stage I (Raw Level):</strong> This is the a low-level API which offers the most customizable and performance.
 *         However, although it's much better in terms of readability compared to Java's raw reflection API,
 *         it's the least unreadable format of XReflection API. This API is accessed from
 *         {@link XReflection#of(Class)}, {@link XReflection#classHandle()} and {@link XReflection#ofMinecraft()}.
 *     </li>
 *     <li>
 *         <strong>Stage II (String API):</strong> This API is a lot more readable and offers the same performance
 *         compared to Stage I API, however it has an extra overhead for caching {@link ReflectiveHandle} for the
 *         first time. This API is typically accessed from {@link ReflectiveNamespace}'s string methods or the
 *         signature methods of its related APIs (e.g. {@link MethodMemberHandle#signature(String)}.
 *     </li>
 *     <li>
 *         <strong>Stage III (Proxification):</strong> This API is much more readable for invocation compared
 *         to the string API because it uses actual interface classes, however, whether its declaration API
 *         is more readable than the string API is debatable and depends on the use case. The biggest
 *         disadvantage of this API is its caching and invocation performance due to how its internal
 *         system is built. The invocation performance is mostly a deal-breaker in most cases.
 *         It's accessed from {@link XReflection#proxify(Class)}.
 *     </li>
 *     <li>
 *         <strong>Stage IV (ASMification):</strong> This is simply Stage III API, however instead of relying on
 *         Java's proxy system, an ASM-assisted code generation occurs in a new class loader, which makes this
 *         almost as fast as direct calls, even for inaccessible private methods due to the use of
 *         {@link java.lang.invoke.MethodHandle#invokeExact(Object...)} with polymorphic signature.
 *         This system is only used if at least ASM9 is detected during runtime (supports shading as well)
 *     </li>
 *     <li>
 *         <strong>Stage V (Compile-Time Remapper):</strong> ???
 *     </li>
 * </ul>
 *
 * <h3>Performance &amp; Caching</h3>
 * While the standard {@link XReflection} wrapper around Java's reflection API in general is pretty lightweight and
 * doesn't add that much of an overhead, you're expected to cache the results of {@link ReflectiveHandle#reflect()}
 * for optimum performance just like using normal reflection. That's because XReflection offers no form of cache,
 * even for the same handle. So you <b><u>should not cache {@link ReflectiveHandle}</u></b>, but you should <b>cache the
 * results (e.g. {@link ReflectiveHandle#reflect()}, {@link ReflectiveHandle#unreflect()}, ...)</b> instead.<br>
 * This is specially important to do if you're going to be using {@link ReflectionParser}
 * methods or {@link ReflectiveConstraint} since the former uses heavy
 * RegEx patterns which sacrifices a lot of performance for readability, which is really worth it; and the latter
 * adds a bit of extra overhead since it performs checks that the normal reflection API ignores.
 * <p>
 * As for {@link ReflectiveProxy}, you should make sure to always cache your result of {@link XReflection#proxify(Class)}.
 * That way you can invoke static methods and constructors from that one proxy and the subsequently created proxies
 * (for individual instances) will inherit that. Although these proxies are handled by an internal cache as well,
 * so it's not that important if you don't cache them.
 * <p>
 * The results of XReflection creation and execution phases are compared in {@code ReflectionBenchmarkSetup} and {@code ReflectionBenchmarkExecution}
 * respectively, and the results of the creation phase is only a nanosecond longer for Stage I API and expectedly, a few nanoseconds longer for
 * Stage II API, However there is no difference for the execution phase (maybe 1 or 2 nanoseconds which is the result of data error) but this can't
 * be said for Stage III Proxy API as one might expect, it's almost 4-20 times slower depending on how you're using the code.
 *
 * @author Crypto Morin
 * @version 14.0.0
 * @see com.cryptomorin.xseries.reflection.minecraft.MinecraftConnection
 * @see com.cryptomorin.xseries.reflection.minecraft.NMSExtras
 */
public final class XReflection {
    /**
     * We use reflection mainly to avoid writing a new class for version barrier.
     * The version barrier is for NMS that uses the Minecraft version as the main package name.
     * <p>
     * E.g. EntityPlayer in 1.15 is in the class {@code net.minecraft.server.v1_15_R1}
     * but in 1.14 it's in {@code net.minecraft.server.v1_14_R1}
     * In order to maintain cross-version compatibility we cannot import these classes.
     * <p>
     * Performance is not a concern for these specific statically initialized values.
     * <p>
     * <a href="https://www.spigotmc.org/wiki/spigot-nms-and-minecraft-versions-legacy/">Versions Legacy</a>
     * <p>
     * This will no longer work because of
     * <a href="https://forums.papermc.io/threads/paper-velocity-1-20-4.998/#post-2955">Paper no-relocation</a>
     * strategy.
     */
    @Nullable
    @ApiStatus.Internal
    public static final String NMS_VERSION = findNMSVersionString();

    /**
     * The current version of XSeries. Mostly used for the {@link com.cryptomorin.xseries.profiles.builder.XSkull} API.
     */
    @ApiStatus.Internal
    public static final String XSERIES_VERSION = "13.0.0";

    /**
     * System property ({@link System#getProperty(String)}) used to disable Minecraft capabilities
     * of this class. This is useful when working with Unit Tests that don't run a server or needing
     * to run forked processes (e.g. using JMH default forked mode for benchmarking.)
     * <p>
     * It's simply enough for the property to be present, but you can also specify a Minecraft version
     * for the class to use for {@link #supports(int)} and other similar version checking methods.
     */
    @TestOnly
    public static final String DISABLE_MINECRAFT_CAPABILITIES_PROPERTY = "xseries.xreflection.disable.minecraft";

    /**
     * Whether the internal systems can take advantage of ASM-powered systems
     * for maximizing performance for reflection access when possible.
     */
    @ApiStatus.Internal
    public static final boolean SUPPORTS_ASM;

    static {
        boolean supportsASM;
        try {
            // Should be handled by shading/relocation systems.
            Class.forName("org.objectweb.asm.ClassWriter");
            Class.forName("org.objectweb.asm.MethodVisitor");
            Class.forName("org.objectweb.asm.FieldVisitor");
            Class.forName("org.objectweb.asm.Constants");
            Class.forName("org.objectweb.asm.Opcodes");
            supportsASM = true;
        } catch (ClassNotFoundException e) {
            supportsASM = false;
        }
        SUPPORTS_ASM = supportsASM;
    }

    @Nullable
    @ApiStatus.Internal
    public static String findNMSVersionString() {
        // This needs to be right below VERSION because of initialization order.
        // This package loop is used to avoid implementation-dependant strings like Bukkit.getVersion() or Bukkit.getBukkitVersion()
        // which allows easier testing as well.
        String found = null;
        for (Package pack : Package.getPackages()) {
            String name = pack.getName();

            // .v because there are other packages.
            if (name.startsWith("org.bukkit.craftbukkit.v")) {
                found = pack.getName().split("\\.")[3];

                // Just a final guard to make sure it finds this important class.
                // As a protection for forge+bukkit implementation that tend to mix versions.
                // The real CraftPlayer should exist in the package.
                // Note: Doesn't seem to function properly. Will need to separate the version
                // handler for NMS and CraftBukkit for software like catmc.
                try {
                    Class.forName("org.bukkit.craftbukkit." + found + ".entity.CraftPlayer");
                    break;
                } catch (ClassNotFoundException e) {
                    found = null;
                }
            }
        }

        return found;
    }

    public static final int MAJOR_NUMBER;
    /**
     * The raw minor version number.
     * E.g. {@code v1_17_R1} to {@code 17}
     *
     * @see #supports(int)
     * @since 4.0.0
     */
    public static final int MINOR_NUMBER;
    /**
     * The raw patch version number. Refers to the <a href="https://en.wikipedia.org/wiki/Software_versioning">major.minor.patch version scheme</a>.
     * E.g.
     * <ul>
     *     <li>{@code v1.20.4} to {@code 4}</li>
     *     <li>{@code v1.18.2} to {@code 2}</li>
     *     <li>{@code v1.19.1} to {@code 1}</li>
     * </ul>
     * <p>
     * I'd not recommend developers to support individual patches at all. You should always support the latest patch.
     * For example, between v1.14.0, v1.14.1, v1.14.2, v1.14.3 and v1.14.4 you should only support v1.14.4
     * <p>
     * This can be used to warn server owners when your plugin will break on older patches.
     *
     * @see #supportsPatch(int)
     * @since 7.0.0
     */
    public static final int PATCH_NUMBER;

    static {
        /* Old way of doing this.
        String[] split = NMS_VERSION.substring(1).split("_");
        if (split.length < 1) {
            throw new IllegalStateException("Version number division error: " + Arrays.toString(split) + ' ' + getVersionInformation());
        }

        String minorVer = split[1];
        try {
            MINOR_NUMBER = Integer.parseInt(minorVer);
            if (MINOR_NUMBER < 0)
                throw new IllegalStateException("Negative minor number? " + minorVer + ' ' + getVersionInformation());
        } catch (Throwable ex) {
            throw new RuntimeException("Failed to parse minor number: " + minorVer + ' ' + getVersionInformation(), ex);
        }
         */

        // NMS_VERSION               = v1_20_R3
        // Bukkit.getBukkitVersion() = 1.20.4-R0.1-SNAPSHOT
        // Bukkit.getVersion()       = git-Paper-364 (MC: 1.20.4)

        String verProp = isMinecraftDisabled();
        if (verProp != null) {
            System.out.println("[XSeries/XReflection] Testing with hardcoded server version: "
                    + (verProp.isEmpty() ? "Disabled Minecraft Capabilities" : verProp));
            if (verProp.isEmpty() || verProp.equals("true")) {
                verProp = "1.21.4-R0.1-SNAPSHOT";
            }
        }

        Matcher bukkitVer = Pattern
                // <patch> is optional for first releases like "1.8-R0.1-SNAPSHOT"
                .compile("^(?<major>\\d+)\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+))?")
                .matcher(verProp != null ? verProp : Bukkit.getBukkitVersion());
        if (bukkitVer.find()) { // matches() won't work, we just want to match the start using "^"
            try {
                // group(0) gives the whole matched string, we just want the captured group.
                String patch = bukkitVer.group("patch");
                MAJOR_NUMBER = Integer.parseInt(bukkitVer.group("major"));
                MINOR_NUMBER = Integer.parseInt(bukkitVer.group("minor"));
                PATCH_NUMBER = Integer.parseInt((patch == null || patch.isEmpty()) ? "0" : patch);
            } catch (Throwable ex) {
                throw new IllegalStateException("Failed to parse minor number: " + bukkitVer + ' ' + getVersionInformation(), ex);
            }
        } else {
            throw new IllegalStateException("Cannot parse server version: \"" + Bukkit.getBukkitVersion() + '"');
        }
    }

    private static String isMinecraftDisabled() {
        try {
            return System.getProperty(DISABLE_MINECRAFT_CAPABILITIES_PROPERTY);
        } catch (SecurityException ignored) {
            return null;
        }
    }

    /**
     * Gets the full version information of the server. Useful for including in errors.
     * "NMS" might return "Unknown NMS", which means that they're running a Paper
     * server that removed the CraftBukkit NMS version guard.
     *
     * @since 7.0.0
     */
    @NotNull
    @Contract(pure = true)
    public static String getVersionInformation() {
        // Bukkit.getServer().getMinecraftVersion() is for Paper
        return "(NMS: " + (NMS_VERSION == null ? "Unknown NMS" : NMS_VERSION) + " | " +
                "Parsed: " + MAJOR_NUMBER + '.' + MINOR_NUMBER + '.' + PATCH_NUMBER + " | " +
                "Minecraft: " + Bukkit.getVersion() + " | " +
                "Bukkit: " + Bukkit.getBukkitVersion() + ')';
    }

    /**
     * Gets the latest known patch number of the given minor version.
     * For example: 1.14 -> 4, 1.17 -> 10
     * The latest version is expected to get newer patches, so make sure to account for unexpected results.
     *
     * @param minorVersion the minor version to get the patch number of.
     * @return the patch number of the given minor version if recognized, otherwise null.
     * @since 7.0.0
     */
    @Nullable
    @Contract(pure = true)
    public static Integer getLatestPatchNumberOf(int minorVersion) {
        if (minorVersion <= 0) throw new IllegalArgumentException("Minor version must be positive: " + minorVersion);

        // https://minecraft.wiki/w/Java_Edition_version_history
        // There are many ways to do this, but this is more visually appealing.
        int[] patches = {
                /* 1 */ 1,
                /* 2 */ 5,
                /* 3 */ 2,
                /* 4 */ 7,
                /* 5 */ 2,
                /* 6 */ 4,
                /* 7 */ 10,
                /* 8 */ 8, // I don't think they released a server version for 1.8.9
                /* 9 */ 4,

                /* 10 */ 2,//          ,_  _  _,
                /* 11 */ 2,//            \o-o/
                /* 12 */ 2,//           ,(.-.),
                /* 13 */ 2,//         _/ |) (| \_
                /* 14 */ 4,//           /\=-=/\
                /* 15 */ 2,//          ,| \=/ |,
                /* 16 */ 5,//        _/ \  |  / \_
                /* 17 */ 1,//            \_!_/
                /* 18 */ 2,
                /* 19 */ 4,
                /* 20 */ 6,
                /* 21 */ 4,
        };

        if (minorVersion > patches.length) return null;
        return patches[minorVersion - 1];
    }

    /**
     * Mojang remapped their NMS in 1.17: <a href="https://www.spigotmc.org/threads/spigot-bungeecord-1-17.510208/#post-4184317">Spigot Thread</a>
     */
    @ApiStatus.Internal
    public static final String
            CRAFTBUKKIT_PACKAGE = isMinecraftDisabled() != null ? null : Bukkit.getServer().getClass().getPackage().getName(),
            NMS_PACKAGE = v(17, "net.minecraft").orElse("net.minecraft.server." + NMS_VERSION);

    @ApiStatus.Internal
    @ApiStatus.Experimental
    @Unmodifiable
    public static final Set<MinecraftMapping> SUPPORTED_MAPPINGS;

    static {
        if (isMinecraftDisabled() != null) {
            SUPPORTED_MAPPINGS = Collections.unmodifiableSet(EnumSet.noneOf(MinecraftMapping.class));
        } else {
            SUPPORTED_MAPPINGS = Collections.unmodifiableSet(
                    supply(
                            ofMinecraft()
                                    .inPackage(MinecraftPackage.NMS, "server.level")
                                    .map(MinecraftMapping.MOJANG, "ServerPlayer"),
                            () -> EnumSet.of(MinecraftMapping.MOJANG)
                    ).or(
                            ofMinecraft()
                                    .inPackage(MinecraftPackage.NMS, "server.level")
                                    .map(MinecraftMapping.MOJANG, "EntityPlayer"),
                            () -> EnumSet.of(MinecraftMapping.SPIGOT, MinecraftMapping.OBFUSCATED)
                    ).get()
            );
        }
    }

    private XReflection() {}

    /**
     * Gives the {@code handle} object if the server version is equal or greater than the given version.
     * This method is purely for readability and should be always used with {@link VersionHandle#orElse(Object)}.
     *
     * @see #v(int, int, Object)
     * @see VersionHandle#orElse(Object)
     * @since 5.0.0
     */
    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static <T> VersionHandle<T> v(int version, T handle) {
        return new VersionHandle<>(version, handle);
    }

    /**
     * @since 9.5.0
     */
    @NotNull
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <T> VersionHandle<T> v(int version, int patch, T handle) {
        return new VersionHandle<>(version, patch, handle);
    }

    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static <T> VersionHandle<T> v(int version, Callable<T> handle) {
        return new VersionHandle<>(version, handle);
    }

    @NotNull
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <T> VersionHandle<T> v(int version, int patch, Callable<T> handle) {
        return new VersionHandle<>(version, patch, handle);
    }

    /**
     * Checks whether the server version is equal or greater than the given version.
     *
     * @param minorNumber the version to compare the server version with.
     * @return true if the version is equal or newer, otherwise false.
     * @see #MINOR_NUMBER
     * @since 4.0.0
     */
    @Contract(pure = true)
    public static boolean supports(int minorNumber) {
        return MINOR_NUMBER >= minorNumber;
    }

    /**
     * A more friendly version of {@link #supports(int, int)} for people with OCD.
     */
    @Contract(pure = true)
    public static boolean supports(int majorNumber, int minorNumber, int patchNumber) {
        if (majorNumber != 1) throw new IllegalArgumentException("Invalid major number: " + majorNumber);
        return supports(minorNumber, patchNumber);
    }

    /**
     * Checks whether the server version is equal or greater than the given version.
     *
     * @param minorNumber the minor version to compare the server version with.
     * @param patchNumber the patch number to compare the server version with.
     * @return true if the version is equal or newer, otherwise false.
     * @see #MINOR_NUMBER
     * @see #PATCH_NUMBER
     * @since 7.1.0
     */
    @Contract(pure = true)
    public static boolean supports(int minorNumber, int patchNumber) {
        return MINOR_NUMBER == minorNumber ? PATCH_NUMBER >= patchNumber : supports(minorNumber);
    }

    /**
     * Checks whether the server version is equal or greater than the given version.
     *
     * @param patchNumber the version to compare the server version with.
     * @return true if the version is equal or newer, otherwise false.
     * @see #PATCH_NUMBER
     * @since 7.0.0
     * @deprecated use {@link #supports(int, int)}
     */
    @Deprecated
    @Contract(pure = true)
    public static boolean supportsPatch(int patchNumber) {
        return PATCH_NUMBER >= patchNumber;
    }

    /**
     * Get a NMS (net.minecraft.server) class which accepts a package for 1.17 compatibility.
     *
     * @param packageName the 1.17+ package name of this class.
     * @param name        the name of the class.
     * @return the NMS class or null if not found.
     * @throws IllegalArgumentException if the class could not be found.
     * @see #getNMSClass(String)
     * @since 4.0.0
     * @deprecated use {@link #ofMinecraft()} instead.
     */
    @NotNull
    @Deprecated
    public static Class<?> getNMSClass(@Nullable String packageName, @NotNull String name) {
        if (packageName != null && supports(17)) name = packageName + '.' + name;

        try {
            return Class.forName(NMS_PACKAGE + '.' + name);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Get a NMS {@link #NMS_PACKAGE} class.
     *
     * @param name the name of the class.
     * @return the NMS class or null if not found.
     * @throws IllegalArgumentException if the class could not be found.
     * @see #getNMSClass(String, String)
     * @since 1.0.0
     * @deprecated use {@link #ofMinecraft()}
     */
    @NotNull
    @Deprecated
    public static Class<?> getNMSClass(@NotNull String name) {
        return getNMSClass(null, name);
    }

    /**
     * Get a CraftBukkit (org.bukkit.craftbukkit) class.
     *
     * @param name the name of the class to load.
     * @return the CraftBukkit class or null if not found.
     * @throws IllegalArgumentException if the class could not be found.
     * @since 1.0.0
     * @deprecated use {@link #ofMinecraft()} instead.
     */
    @NotNull
    @Deprecated
    public static Class<?> getCraftClass(@NotNull String name) {
        try {
            return Class.forName(CRAFTBUKKIT_PACKAGE + '.' + name);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Gives an array version of a class. For example if you wanted {@code EntityPlayer[]} you'd use:
     * <pre>{@code
     *     Class EntityPlayer = ReflectionUtils.getNMSClass("...", "EntityPlayer");
     *     Class EntityPlayerArray = ReflectionUtils.toArrayClass(EntityPlayer);
     * }</pre>
     * <p>
     * Note that this doesn't work on primitive classes.
     *
     * @param clazz the class to get the array version of. You could use for multi-dimensions arrays too.
     * @throws IllegalArgumentException if the class could not be found.
     * @see ClassHandle#asArray()
     */
    @NotNull
    @Contract(pure = true)
    public static Class<?> toArrayClass(@NotNull Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class is null");
        try {
            return Class.forName("[L" + clazz.getName() + ';');
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Cannot find array class for class: " + clazz, ex);
        }
    }

    /**
     * @since v9.0.0
     */
    @NotNull
    @Contract(value = "-> new", pure = true)
    public static MinecraftClassHandle ofMinecraft() {
        return new MinecraftClassHandle(new ReflectiveNamespace());
    }

    /**
     * @since v9.0.0
     */
    @NotNull
    @Contract(value = "-> new", pure = true)
    public static DynamicClassHandle classHandle() {
        return new DynamicClassHandle(new ReflectiveNamespace());
    }

    /**
     * @since v11.0.0
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static StaticClassHandle of(Class<?> clazz) {
        return new StaticClassHandle(new ReflectiveNamespace(), clazz);
    }


    /**
     * Read {@link ReflectiveNamespace} for more info.
     *
     * @since v11.0.0
     */
    @NotNull
    @Contract(value = "-> new", pure = true)
    public static ReflectiveNamespace namespaced() {
        return new ReflectiveNamespace();
    }

    /**
     * @since v9.0.0
     */
    @SafeVarargs
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <T, H extends ReflectiveHandle<T>> AggregateReflectiveHandle<T, H> any(H... handles) {
        return new AggregateReflectiveHandle<>(Arrays.stream(handles).map(x -> (Callable<H>) () -> x).collect(Collectors.toList()));
    }

    /**
     * @since v9.0.0
     */
    @SafeVarargs
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <T, H extends ReflectiveHandle<T>> AggregateReflectiveHandle<T, H> anyOf(Callable<H>... handles) {
        return new AggregateReflectiveHandle<>(Arrays.asList(handles));
    }

    /**
     * @since v11.3.0
     */
    @ApiStatus.Experimental
    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static <H extends ReflectiveHandle<?>, O> AggregateReflectiveSupplier<H, O> supply(H handle, O object) {
        AggregateReflectiveSupplier<H, O> supplier = new AggregateReflectiveSupplier<>();
        supplier.or(handle, object);
        return supplier;
    }

    /**
     * @since v11.3.0
     */
    @ApiStatus.Experimental
    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static <H extends ReflectiveHandle<?>, O> AggregateReflectiveSupplier<H, O> supply(H handle, Supplier<O> object) {
        AggregateReflectiveSupplier<H, O> supplier = new AggregateReflectiveSupplier<>();
        supplier.or(handle, object);
        return supplier;
    }

    /**
     * Relativize the stacktrace of exceptions that are thrown from the same location.
     * The suppressed exception's ({@link Throwable#getSuppressed()}) stacktrace are relativized against
     * the given exceptions stacktrace.
     * <p>
     * This is mostly useful when you have a trial-and-error mechanism that accumulates all the errors
     * to throw them in case all the attempts have failed. This removes unnecessary line information to
     * help the developer focus on important, non-repeated lines.
     *
     * @param ex  the exception to have it's suppressed exceptions relativized.
     * @param <T> the type of the exception.
     * @return the same exception.
     */
    @ApiStatus.Experimental
    @NotNull
    @Contract(value = "_ -> param1", mutates = "param1")
    public static <T extends Throwable> T relativizeSuppressedExceptions(@NotNull T ex) {
        Objects.requireNonNull(ex, "Cannot relativize null exception");
        final StackTraceElement[] EMPTY_STACK_TRACE_ARRAY = new StackTraceElement[0];
        StackTraceElement[] mainStackTrace = ex.getStackTrace();

        for (Throwable suppressed : ex.getSuppressed()) {
            StackTraceElement[] suppressedStackTrace = suppressed.getStackTrace();
            List<StackTraceElement> relativized = new ArrayList<>(10);

            for (int i = 0; i < suppressedStackTrace.length; i++) {
                if (mainStackTrace.length <= i) {
                    relativized = null;
                    break;
                }

                StackTraceElement mainTrace = mainStackTrace[i];
                StackTraceElement suppTrace = suppressedStackTrace[i];
                if (mainTrace.equals(suppTrace)) {
                    break;
                } else {
                    relativized.add(suppTrace);
                }
            }

            if (relativized != null) {
                // We might not know the line so let's not add this:
                // if (!relativized.isEmpty()) relativized.remove(relativized.size() - 1);
                suppressed.setStackTrace(relativized.toArray(EMPTY_STACK_TRACE_ARRAY));
            }
        }
        return ex;
    }

    @SuppressWarnings("unchecked")
    @Contract(value = "_ -> fail", pure = true)
    private static <T extends Throwable> void throwException(Throwable exception) throws T {
        throw (T) exception;
    }

    /**
     * Throws a checked exception (see {@link Exception}) silently without forcing the programmer to handle it. This is usually considered
     * a very bad practice, as those errors are meant to be handled, so please use sparingly. You should just
     * create a {@link RuntimeException} instead and putting the checked exception as a cause if necessary.
     * <h2>Usage</h2>
     * <pre>{@code
     *     void doStuff() throws IOException {}
     *
     *     void rethrowAsRuntime() {
     *         try {
     *             doStuff();
     *         } catch (IOException ex) {
     *             throw new RuntimeException(ex);
     *         }
     *     }
     *
     *     void ignoreTheLawsOfJavaQuantumMechanics() {
     *         try {
     *             doStuff();
     *         } catch (IOException ex) {
     *             throw XReflection.throwCheckedException(ex);
     *         }
     *     }
     * }</pre>
     *
     * @return {@code null}, but it's intended to be thrown, this is a hacky trick to stop the IDE
     * from complaining about non-terminating statements.
     */
    @Contract(value = "_ -> fail", pure = true)
    public static RuntimeException throwCheckedException(@NotNull Throwable exception) {
        Objects.requireNonNull(exception, "Cannot throw null exception");
        // The following commented statement is not needed because the exception was created somewhere else and the stacktrace reflects that.
        // exception.setStackTrace(Arrays.stream(exception.getStackTrace()).skip(1).toArray(StackTraceElement[]::new));
        throwException(exception);
        return null; // Trick the compiler to stfu for "throw" terminating statements.
    }

    /**
     * Adds the stacktrace of the current thread in case an error occurs in the given Future.
     */
    @ApiStatus.Experimental
    @Contract(value = "_ -> new", mutates = "param1")
    public static <T> CompletableFuture<T> stacktrace(@NotNull CompletableFuture<T> completableFuture) {
        StackTraceElement[] currentStacktrace = Thread.currentThread().getStackTrace();
        return completableFuture.whenComplete((value, ex) -> { // Gets called even when it's completed.
            if (ex == null) {
                // This happens if for example someone does:
                // completableFuture.exceptionally(e -> { e.printStackTrace(); return null; })
                completableFuture.complete(value);
                return;
            }

            try {
                // Remove these:
                // at java.base/java.util.concurrent.CompletableFuture.encodeThrowable(CompletableFuture.java:315)
                // at java.base/java.util.concurrent.CompletableFuture.completeThrowable(CompletableFuture.java:320)
                // at java.base/java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1770)
                // at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
                // at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
                // at java.base/java.lang.Thread.run(Thread.java:1583)
                StackTraceElement[] exStacktrace = ex.getStackTrace();
                if (exStacktrace.length >= 3) {
                    List<StackTraceElement> clearStacktrace = new ArrayList<>(Arrays.asList(exStacktrace));
                    Collections.reverse(clearStacktrace);

                    Iterator<StackTraceElement> iter = clearStacktrace.iterator();
                    List<String> watchClassNames = Arrays.asList("java.util.concurrent.CompletableFuture",
                            "java.util.concurrent.ThreadPoolExecutor", "java.util.concurrent.ForkJoinTask",
                            "java.util.concurrent.ForkJoinWorkerThread", "java.util.concurrent.ForkJoinPool");
                    List<String> watchMethodNames = Arrays.asList("postComplete", "encodeThrowable", "completeThrowable",
                            "tryFire", "run", "runWorker", "scan", "exec", "doExec", "topLevelExec", "uniWhenComplete");
                    while (iter.hasNext()) {
                        StackTraceElement stackTraceElement = iter.next();
                        String className = stackTraceElement.getClassName();
                        String methodName = stackTraceElement.getMethodName();

                        // Let's keep this just as an indicator.
                        if (className.equals(Thread.class.getName())) continue;

                        if (watchClassNames.stream().anyMatch(className::startsWith) &&
                                watchMethodNames.stream().anyMatch(methodName::equals)) {
                            iter.remove();
                        } else {
                            break;
                        }
                    }

                    Collections.reverse(clearStacktrace);
                    exStacktrace = clearStacktrace.toArray(new StackTraceElement[0]);
                }

                // Skip 2 -> the getStackTrace() method + this method
                StackTraceElement[] finalCurrentStackTrace = Arrays.stream(currentStacktrace).skip(2).toArray(StackTraceElement[]::new);
                ex.setStackTrace(concatenate(exStacktrace, finalCurrentStackTrace));
            } catch (Throwable ex2) {
                ex.addSuppressed(ex2);
            } finally {
                completableFuture.completeExceptionally(ex);
            }
        });
    }

    @ApiStatus.Internal
    @Contract(value = "_, _ -> new", pure = true)
    public static <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    private static final Map<Class<?>, ReflectiveProxyObject> PROXIFIED_CLASSES = new IdentityHashMap<>();

    /**
     * Returns a cached value if this interface is already proxified, otherwise proxifies and returns it.
     *
     * @see ReflectiveProxy
     * @see XReflectASM
     */
    @ApiStatus.Experimental
    @NotNull
    public static <T extends ReflectiveProxyObject> T proxify(@NotNull Class<T> interfaceClass) {
        ReflectiveProxy.checkInterfaceClass(interfaceClass);

        ReflectiveProxyObject loaded = PROXIFIED_CLASSES.get(interfaceClass);
        if (loaded != null) // noinspection unchecked
            return (T) loaded;

        T proxified;
        if (SUPPORTS_ASM) {
            proxified = XReflectASM.proxify(interfaceClass).create();
        } else {
            proxified = ReflectiveProxy.proxify(interfaceClass).proxy();
        }

        PROXIFIED_CLASSES.put(interfaceClass, proxified);
        return proxified;
    }
}
