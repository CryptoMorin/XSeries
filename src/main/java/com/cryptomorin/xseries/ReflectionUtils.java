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
package com.cryptomorin.xseries;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>ReflectionUtils</b> - Reflection handler for NMS and CraftBukkit.<br>
 * Caches the packet related methods and is asynchronous.
 * <p>
 * This class does not handle null checks as most of the requests are from the
 * other utility classes that already handle null checks.
 * <p>
 * <a href="https://wiki.vg/Protocol">Clientbound Packets</a> are considered fake
 * updates to the client without changing the actual data. Since all the data is handled
 * by the server.
 * <p>
 * A useful resource used to compare mappings is <a href="https://minidigger.github.io/MiniMappingViewer/#/spigot">Mini's Mapping Viewer</a>
 * Another one is <a href="https://mappings.cephx.dev/1.20.6/net/minecraft/server/network/ServerPlayerConnection.html">Cephx</a>.
 *
 * @author Crypto Morin
 * @version 10.0.0
 */
public final class ReflectionUtils {
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
    public static final String NMS_VERSION = findNMSVersionString();

    @Nullable
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
                // handler for NMS and CraftBukkit for softwares like catmc.
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
        Matcher bukkitVer = Pattern
                // <patch> is optional for first releases like "1.8-R0.1-SNAPSHOT"
                .compile("^(?<major>\\d+)\\.(?<minor>\\d+)\\.(?<patch>\\d+)?")
                .matcher(Bukkit.getBukkitVersion());
        if (bukkitVer.find()) { // matches() won't work, we just want to match the start using "^"
            try {
                // group(0) gives the whole matched string, we just want the captured group.
                String patch = bukkitVer.group("patch");
                MAJOR_NUMBER = Integer.parseInt(bukkitVer.group("major"));
                MINOR_NUMBER = Integer.parseInt(bukkitVer.group("minor"));
                PATCH_NUMBER = Integer.parseInt((patch == null || patch.isEmpty()) ? "0" : patch);
            } catch (Throwable ex) {
                throw new RuntimeException("Failed to parse minor number: " + bukkitVer + ' ' + getVersionInformation(), ex);
            }
        } else {
            throw new IllegalStateException("Cannot parse server version: \"" + Bukkit.getBukkitVersion() + '"');
        }
    }

    /**
     * Gets the full version information of the server. Useful for including in errors.
     *
     * @since 7.0.0
     */
    public static String getVersionInformation() {
        // Bukkit.getServer().getMinecraftVersion() is for Paper
        return "(NMS: " + NMS_VERSION + " | " +
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
        };

        if (minorVersion > patches.length) return null;
        return patches[minorVersion - 1];
    }

    /**
     * Mojang remapped their NMS in 1.17: <a href="https://www.spigotmc.org/threads/spigot-bungeecord-1-17.510208/#post-4184317">Spigot Thread</a>
     */
    public static final String
            CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName(),
            NMS_PACKAGE = v(17, "net.minecraft").orElse("net.minecraft.server." + NMS_VERSION);
    public static final MinecraftMapping SUPPORTED_MAPPING;
    /**
     * A nullable public accessible field only available in {@code EntityPlayer}.
     * This can be null if the player is offline.
     */
    private static final MethodHandle PLAYER_CONNECTION;
    /**
     * Responsible for getting the NMS handler {@code EntityPlayer} object for the player.
     * {@code CraftPlayer} is simply a wrapper for {@code EntityPlayer}.
     * Used mainly for handling packet related operations.
     * <p>
     * This is also where the famous player {@code ping} field comes from!
     */
    private static final MethodHandle GET_HANDLE;
    /**
     * Sends a packet to the player's client through a {@code NetworkManager} which
     * is where {@code ProtocolLib} controls packets by injecting channels!
     */
    private static final MethodHandle SEND_PACKET;

    static {
        MinecraftClassHandle entityPlayer = ofMinecraft()
                .inPackage(MinecraftPackage.NMS, "server.level")
                .map(MinecraftMapping.MOJANG, "ServerPlayer")
                .map(MinecraftMapping.SPIGOT, "EntityPlayer");
        MinecraftClassHandle craftPlayer = ofMinecraft()
                .inPackage(MinecraftPackage.CB, "entity")
                .named("CraftPlayer");
        MinecraftClassHandle playerConnection = ofMinecraft()
                .inPackage(MinecraftPackage.NMS, "server.network")
                .map(MinecraftMapping.MOJANG, "ServerPlayerConnection")
                .map(MinecraftMapping.SPIGOT, "PlayerConnection");
        MinecraftClassHandle playerConnectionImpl = ofMinecraft()
                .inPackage(MinecraftPackage.NMS, "server.network")
                .map(MinecraftMapping.MOJANG, "ServerGamePacketListenerImpl")
                .map(MinecraftMapping.SPIGOT, "PlayerConnection");
        MinecraftClassHandle packetClass = ofMinecraft()
                .inPackage(MinecraftPackage.NMS, "network.protocol")
                .map(MinecraftMapping.SPIGOT, "Packet");

        if (ofMinecraft()
                .inPackage(MinecraftPackage.NMS, "server.level")
                .map(MinecraftMapping.MOJANG, "ServerPlayer")
                .exists()) {
            SUPPORTED_MAPPING = MinecraftMapping.MOJANG;
        } else if (ofMinecraft()
                .inPackage(MinecraftPackage.NMS, "server.level")
                .map(MinecraftMapping.MOJANG, "EntityPlayer")
                .exists()) {
            SUPPORTED_MAPPING = MinecraftMapping.SPIGOT;
        } else {
            throw new RuntimeException("Unknown Minecraft mapping " + getVersionInformation(), entityPlayer.catchError());
        }

        PLAYER_CONNECTION = entityPlayer
                // .getterField(v(20, 5, "connection").v(20, "c").v(17, "b").orElse("playerConnection"))
                .getterField()
                .returns(playerConnectionImpl)
                .map(MinecraftMapping.MOJANG, "connection")
                .map(MinecraftMapping.OBFUSCATED, v(20, "c").v(17, "b").orElse("playerConnection"))
                .unreflect();
        SEND_PACKET = playerConnection
                .method()
                .returns(void.class)
                .parameters(packetClass)
                .map(MinecraftMapping.MOJANG, "send")
                .map(MinecraftMapping.OBFUSCATED, v(20, 2, "b").v(18, "a").orElse("sendPacket"))
                .unreflect();
        GET_HANDLE = craftPlayer
                .method()
                .named("getHandle")
                .returns(entityPlayer)
                .unreflect();
    }

    private ReflectionUtils() {
    }

    /**
     * Gives the {@code handle} object if the server version is equal or greater than the given version.
     * This method is purely for readability and should be always used with {@link VersionHandler#orElse(Object)}.
     *
     * @see #v(int, int, Object)
     * @see VersionHandler#orElse(Object)
     * @since 5.0.0
     */
    public static <T> VersionHandler<T> v(int version, T handle) {
        return new VersionHandler<>(version, handle);
    }

    /**
     * Overload for {@link #v(int, T)} that supports patch versions
     *
     * @since 9.5.0
     */
    public static <T> VersionHandler<T> v(int version, int patch, T handle) {
        return new VersionHandler<>(version, patch, handle);
    }

    public static <T> VersionHandler<T> v(int version, Callable<T> handle) {
        return new VersionHandler<>(version, handle);
    }

    public static <T> VersionHandler<T> v(int version, int patch, Callable<T> handle) {
        return new VersionHandler<>(version, patch, handle);
    }

    /**
     * Checks whether the server version is equal or greater than the given version.
     *
     * @param minorNumber the version to compare the server version with.
     * @return true if the version is equal or newer, otherwise false.
     * @see #MINOR_NUMBER
     * @since 4.0.0
     */
    public static boolean supports(int minorNumber) {
        return MINOR_NUMBER >= minorNumber;
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
    public static boolean supportsPatch(int patchNumber) {
        return PATCH_NUMBER >= patchNumber;
    }

    /**
     * Get a NMS (net.minecraft.server) class which accepts a package for 1.17 compatibility.
     *
     * @param packageName the 1.17+ package name of this class.
     * @param name        the name of the class.
     * @return the NMS class or null if not found.
     * @throws RuntimeException if the class could not be found.
     * @deprecated use {@link #ofMinecraft()} instead.
     * @see #getNMSClass(String)
     * @since 4.0.0
     */
    @Nonnull
    @Deprecated
    public static Class<?> getNMSClass(@Nullable String packageName, @Nonnull String name) {
        if (packageName != null && supports(17)) name = packageName + '.' + name;

        try {
            return Class.forName(NMS_PACKAGE + '.' + name);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get a NMS {@link #NMS_PACKAGE} class.
     *
     * @param name the name of the class.
     * @return the NMS class or null if not found.
     * @throws RuntimeException if the class could not be found.
     * @see #getNMSClass(String, String)
     * @since 1.0.0
     */
    @Nonnull
    public static Class<?> getNMSClass(@Nonnull String name) {
        return getNMSClass(null, name);
    }

    /**
     * Sends a packet to the player asynchronously if they're online.
     * Packets are thread-safe.
     *
     * @param player  the player to send the packet to.
     * @param packets the packets to send.
     * @return the async thread handling the packet.
     * @see #sendPacketSync(Player, Object...)
     * @since 1.0.0
     */
    @Nonnull
    public static CompletableFuture<Void> sendPacket(@Nonnull Player player, @Nonnull Object... packets) {
        return CompletableFuture.runAsync(() -> sendPacketSync(player, packets))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    /**
     * Sends a packet to the player synchronously if they're online.
     *
     * @param player  the player to send the packet to.
     * @param packets the packets to send.
     * @see #sendPacket(Player, Object...)
     * @since 2.0.0
     */
    public static void sendPacketSync(@Nonnull Player player, @Nonnull Object... packets) {
        try {
            Object handle = GET_HANDLE.invoke(player);
            Object connection = PLAYER_CONNECTION.invoke(handle);

            // Checking if the connection is not null is enough. There is no need to check if the player is online.
            if (connection != null) {
                for (Object packet : packets) SEND_PACKET.invoke(connection, packet);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Nullable
    public static Object getHandle(@Nonnull Player player) {
        Objects.requireNonNull(player, "Cannot get handle of null player");
        try {
            return GET_HANDLE.invoke(player);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Object getConnection(@Nonnull Player player) {
        Objects.requireNonNull(player, "Cannot get connection of null player");
        try {
            Object handle = GET_HANDLE.invoke(player);
            return PLAYER_CONNECTION.invoke(handle);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    /**
     * Get a CraftBukkit (org.bukkit.craftbukkit) class.
     *
     * @param name the name of the class to load.
     * @return the CraftBukkit class or null if not found.
     * @throws RuntimeException if the class could not be found.
     * @since 1.0.0
     * @deprecated use {@link #ofMinecraft()} instead.
     */
    @Nonnull
    @Deprecated
    public static Class<?> getCraftClass(@Nonnull String name) {
        try {
            return Class.forName(CRAFTBUKKIT_PACKAGE + '.' + name);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gives an array version of a class. For example if you wanted {@code EntityPlayer[]} you'd use:
     * <pre>{@code
     *     Class EntityPlayer = ReflectionUtils.getNMSClass("...", "EntityPlayer");
     *     Class EntityPlayerArray = ReflectionUtils.toArrayClass(EntityPlayer);
     * }</pre>
     *
     * @param clazz the class to get the array version of. You could use for multi-dimensions arrays too.
     * @throws RuntimeException if the class could not be found.
     */
    @Nonnull
    public static Class<?> toArrayClass(Class<?> clazz) {
        try {
            return Class.forName("[L" + clazz.getName() + ';');
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Cannot find array class for class: " + clazz, ex);
        }
    }

    /**
     * @since v9.0.0
     */
    @ApiStatus.Experimental
    public static MinecraftClassHandle ofMinecraft() {
        return new MinecraftClassHandle();
    }

    /**
     * @since v9.0.0
     */
    @ApiStatus.Experimental
    public static ClassHandle classHandle() {
        return new ClassHandle();
    }

    /**
     * @since v9.0.0
     */
    @SafeVarargs
    @ApiStatus.Experimental
    public static <T, H extends Handle<T>> AggregateHandle<T, H> any(H... handles) {
        return new AggregateHandle<>(Arrays.asList(handles));
    }

    /**
     * Paper started using Mojang-mapped names since 1.20.5
     */
    public enum MinecraftMapping {
        MOJANG, OBFUSCATED, SPIGOT;
    }

    public enum MinecraftPackage {
        NMS(NMS_PACKAGE), CB(CRAFTBUKKIT_PACKAGE), SPIGOT("org.spigotmc");

        private final String packageName;

        MinecraftPackage(String packageName) {this.packageName = packageName;}
    }

    public interface Handle<T> {
        default boolean exists() {
            try {
                reflect();
                return true;
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }

        default ReflectiveOperationException catchError() {
            try {
                reflect();
                return null;
            } catch (ReflectiveOperationException ex) {
                return ex;
            }
        }

        default T unreflect() {
            try {
                return reflect();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        T reflect() throws ReflectiveOperationException;
    }

    @ApiStatus.Experimental
    public static class MinecraftClassHandle extends ClassHandle {

        public MinecraftClassHandle inPackage(MinecraftPackage minecraftPackage, String packageName) {
            this.packageName = minecraftPackage.packageName;
            if (minecraftPackage != MinecraftPackage.NMS || supports(17)) this.packageName += '.' + packageName;
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

    public static final class AggregateHandle<T, H extends Handle<T>> implements Handle<T> {
        private final List<H> handles;
        private Consumer<H> handleModifier;

        public AggregateHandle() {
            this.handles = new ArrayList<>(5);
        }

        public AggregateHandle(Collection<H> handles) {
            this.handles = new ArrayList<>(handles.size());
            this.handles.addAll(handles);
        }

        public AggregateHandle<T, H> or(H handle) {
            this.handles.add(handle);
            return this;
        }

        public AggregateHandle<T, H> modify(Consumer<H> handleModifier) {
            this.handleModifier = handleModifier;
            return this;
        }

        @Override
        public T reflect() throws ReflectiveOperationException {
            ClassNotFoundException errors = null;

            for (H handle : handles) {
                if (handleModifier != null) handleModifier.accept(handle);
                try {
                    return handle.reflect();
                } catch (ClassNotFoundException ex) {
                    if (errors == null)
                        errors = new ClassNotFoundException("None of the aggregate handles were successful");
                    errors.addSuppressed(ex);
                }
            }

            throw errors;
        }
    }

    public abstract static class MemberHandle implements Handle<MethodHandle> {
        protected boolean makeAccessible;
        protected final ClassHandle clazz;
        protected final MethodHandles.Lookup lookup = MethodHandles.lookup();

        protected MemberHandle(ClassHandle clazz) {this.clazz = clazz;}

        public MemberHandle makeAccessible() {
            this.makeAccessible = true;
            return this;
        }

        public final MethodHandle unreflect() {
            try {
                return reflect();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        public abstract MethodHandle reflect() throws ReflectiveOperationException;

        public abstract <T extends AccessibleObject & Member> T reflectJvm() throws ReflectiveOperationException;

        protected <T extends AccessibleObject> T handleAccessible(T accessibleObject) {
            if (this.makeAccessible) accessibleObject.setAccessible(true);
            return accessibleObject;
        }
    }

    public abstract static class NamedMemberHandle extends MemberHandle {
        protected Class<?> returnType;
        protected boolean isStatic;
        protected final List<String> names = new ArrayList<>(5);

        protected NamedMemberHandle(ClassHandle clazz) {
            super(clazz);
        }

        public NamedMemberHandle map(MinecraftMapping mapping, String name) {
            this.names.add(name);
            return this;
        }

        public NamedMemberHandle asStatic() {
            this.isStatic = true;
            return this;
        }

        public NamedMemberHandle returns(Class<?> clazz) {
            this.returnType = clazz;
            return this;
        }

        public NamedMemberHandle returns(ClassHandle clazz) {
            this.returnType = clazz.unreflect();
            return this;
        }

        public MemberHandle named(String... names) {
            this.names.addAll(Arrays.asList(names));
            return this;
        }
    }

    public static class ConstructorMemberHandle extends MemberHandle {
        protected Class<?>[] parameterTypes = new Class[0];

        protected ConstructorMemberHandle(ClassHandle clazz) {
            super(clazz);
        }

        public ConstructorMemberHandle parameters(Class<?>... parameterTypes) {
            this.parameterTypes = parameterTypes;
            return this;
        }

        public ConstructorMemberHandle parameters(ClassHandle... parameterTypes) {
            this.parameterTypes = Arrays.stream(parameterTypes).map(ClassHandle::unreflect).toArray(Class[]::new);
            return this;
        }


        @Override
        public MethodHandle reflect() throws NoSuchMethodException, IllegalAccessException {
            if (makeAccessible) {
                return lookup.unreflectConstructor(reflectJvm());
            } else {
                return lookup.findConstructor(clazz.unreflect(), MethodType.methodType(void.class, this.parameterTypes));
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Constructor<?> reflectJvm() throws NoSuchMethodException {
            return handleAccessible(clazz.unreflect().getDeclaredConstructor(parameterTypes));
        }
    }

    public static class MethodMemberHandle extends NamedMemberHandle {
        protected Class<?>[] parameterTypes = new Class[0];

        protected MethodMemberHandle(ClassHandle clazz) {
            super(clazz);
        }

        public MethodMemberHandle parameters(ClassHandle... parameterTypes) {
            this.parameterTypes = Arrays.stream(parameterTypes).map(ClassHandle::unreflect).toArray(Class[]::new);
            return this;
        }

        public MethodMemberHandle returns(Class<?> clazz) {
            super.returns(clazz);
            return this;
        }

        public MethodMemberHandle asStatic() {
            super.asStatic();
            return this;
        }

        public MethodMemberHandle parameters(Class<?>... parameterTypes) {
            this.parameterTypes = parameterTypes;
            return this;
        }

        @Override
        public MethodHandle reflect() throws NoSuchMethodException, IllegalAccessException {
            return lookup.unreflect(reflectJvm());
        }

        public MethodMemberHandle named(String... names) {
            super.named(names);
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Method reflectJvm() throws NoSuchMethodException {
            NoSuchMethodException errors = null;
            Method method = null;

            Class<?> clazz = this.clazz.unreflect();
            for (String name : this.names) {
                try {
                    method = clazz.getDeclaredMethod(name, parameterTypes);
                    if (method.getReturnType() != this.returnType) {
                        throw new NoSuchMethodException("Method named '" + name + "' was found but the types don't match: " + this.returnType + " != " + method);
                    }
                } catch (NoSuchMethodException ex) {
                    method = null;
                    if (errors == null) errors = new NoSuchMethodException("None of the fields were found");
                    errors.addSuppressed(ex);
                }
            }

            if (method == null) throw errors;
            return handleAccessible(method);
        }
    }

    public static class FieldMemberHandle extends NamedMemberHandle {
        protected Boolean getter;

        protected FieldMemberHandle(ClassHandle clazz) {
            super(clazz);
        }

        public FieldMemberHandle named(String... names) {
            super.named(names);
            return this;
        }

        public FieldMemberHandle getter() {
            this.getter = true;
            return this;
        }

        public FieldMemberHandle asStatic() {
            super.asStatic();
            return this;
        }

        public FieldMemberHandle setter() {
            this.getter = false;
            return this;
        }

        @Override
        public FieldMemberHandle returns(Class<?> clazz) {
            super.returns(clazz);
            return this;
        }

        @Override
        public FieldMemberHandle returns(ClassHandle clazz) {
            super.returns(clazz);
            return this;
        }

        @Override
        public MethodHandle reflect() throws NoSuchFieldException, IllegalAccessException {
            if (this.getter == null)
                throw new IllegalStateException("Not specified whether the field is a getter or setter");

            if (getter) {
                return lookup.unreflectGetter(reflectJvm());
            } else {
                return lookup.unreflectSetter(reflectJvm());
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Field reflectJvm() throws NoSuchFieldException {
            NoSuchFieldException errors = null;
            Field field = null;

            Class<?> clazz = this.clazz.unreflect();
            for (String name : this.names) {
                try {
                    field = clazz.getDeclaredField(name);
                    if (field.getType() != this.returnType) {
                        throw new NoSuchFieldException("Field named '" + name + "' was found but the types don't match: " + this.returnType + " != " + field.getType());
                    }
                } catch (NoSuchFieldException ex) {
                    field = null;
                    if (errors == null) errors = new NoSuchFieldException("None of the fields were found");
                    errors.addSuppressed(ex);
                }
            }

            if (field == null) throw errors;
            return handleAccessible(field);
        }
    }

    @ApiStatus.Experimental
    public static class ClassHandle implements Handle<Class<?>> {
        protected String packageName;
        protected final List<String> classNames = new ArrayList<>(5);
        protected boolean array;

        public ClassHandle inPackage(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public ClassHandle named(String... clazzNames) {
            this.classNames.addAll(Arrays.asList(clazzNames));
            return this;
        }

        public ClassHandle asArray() {
            this.array = true;
            return this;
        }

        public MethodMemberHandle method() {
            return new MethodMemberHandle(this);
        }

        public FieldMemberHandle getterField() {
            return new FieldMemberHandle(this).getter();
        }

        public FieldMemberHandle setterField() {
            return new FieldMemberHandle(this).setter();
        }

        public ConstructorMemberHandle constructor(Class<?>... parameters) {
            return new ConstructorMemberHandle(this).parameters(parameters);
        }

        public ConstructorMemberHandle constructor(ClassHandle... parameters) {
            return new ConstructorMemberHandle(this).parameters(parameters);
        }

        public String[] reflectClassNames() {
            Objects.requireNonNull(packageName, "Package name is null");
            String[] classNames = new String[this.classNames.size()];

            for (int i = 0; i < this.classNames.size(); i++) {
                @SuppressWarnings("NonConstantStringShouldBeStringBuffer")
                String clazz = packageName + '.' + this.classNames.get(i);
                if (array) clazz = "[L" + clazz + ';';

                classNames[i] = clazz;
            }

            return classNames;
        }

        @Override
        public Class<?> reflect() throws ClassNotFoundException {
            ClassNotFoundException errors = null;

            for (String className : reflectClassNames()) {
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException ex) {
                    if (errors == null) errors = new ClassNotFoundException("None of the classes were found");
                    errors.addSuppressed(ex);
                }
            }

            throw errors;
        }
    }

    public static final class VersionHandler<T> {
        private int version, patch;
        private T handle;
        // private RuntimeException errors;

        private VersionHandler(int version, T handle) {
            this(version, 0, handle);
        }

        private VersionHandler(int version, int patch, T handle) {
            if (supports(version, patch)) {
                this.version = version;
                this.patch = patch;
                this.handle = handle;
            }
        }

        private VersionHandler(int version, int patch, Callable<T> handle) {
            if (supports(version, patch)) {
                this.version = version;
                this.patch = patch;

                try {
                    this.handle = handle.call();
                } catch (Exception ignored) {
                }
            }
        }

        private VersionHandler(int version, Callable<T> handle) {
            this(version, 0, handle);
        }

        public VersionHandler<T> v(int version, T handle) {
            return v(version, 0, handle);
        }

        private boolean checkVersion(int version, int patch) {
            if (version == this.version && patch == this.patch)
                throw new IllegalArgumentException("Cannot have duplicate version handles for version: " + version + '.' + patch);
            return version > this.version && patch >= this.patch && supports(version, patch);
        }

        public VersionHandler<T> v(int version, int patch, Callable<T> handle) {
            if (!checkVersion(version, patch)) return this;

            try {
                this.handle = handle.call();
            } catch (Exception ignored) {
            }

            this.version = version;
            this.patch = patch;
            return this;
        }

        public VersionHandler<T> v(int version, int patch, T handle) {
            if (checkVersion(version, patch)) {
                this.version = version;
                this.patch = patch;
                this.handle = handle;
            }
            return this;
        }

        /**
         * If none of the previous version checks matched, it'll return this object.
         */
        public T orElse(T handle) {
            return this.version == 0 ? handle : this.handle;
        }

        public T orElse(Callable<T> handle) {
            if (this.version == 0) {
                try {
                    return handle.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return this.handle;
        }
    }
}
