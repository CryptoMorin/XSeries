/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Crypto Morin
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
 *
 * @author Crypto Morin
 * @version 3.0.0
 */
public class ReflectionUtils {
    /**
     * We use reflection mainly to avoid writing a new class for version barrier.
     * The version barrier is for NMS that uses the Minecraft version as the main package name.
     * <p>
     * E.g. EntityPlayer in 1.15 is in the class {@code net.minecraft.server.v1_15_R1}
     * but in 1.14 it's in {@code net.minecraft.server.v1_14_R1}
     * In order to maintain cross-version compatibility we cannot import these classes.
     * <p>
     * Performance is not a concern for these specific statically initialized values.
     */
    public static final String
            VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3],
            CRAFTBUKKIT = "org.bukkit.craftbukkit." + VERSION + '.',
            NMS = "net.minecraft.server." + VERSION + '.';

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
        Class<?> entityPlayer = getNMSClass("EntityPlayer");
        Class<?> craftPlayer = getCraftClass("entity.CraftPlayer");
        Class<?> playerConnection = getNMSClass("PlayerConnection");

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle sendPacket = null;
        MethodHandle getHandle = null;
        MethodHandle connection = null;
        try {
            connection = lookup.findGetter(entityPlayer, "playerConnection", playerConnection);
            getHandle = lookup.findVirtual(craftPlayer, "getHandle", MethodType.methodType(entityPlayer));
            sendPacket = lookup.findVirtual(playerConnection, "sendPacket", MethodType.methodType(void.class, getNMSClass("Packet")));
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        PLAYER_CONNECTION = connection;
        SEND_PACKET = sendPacket;
        GET_HANDLE = getHandle;
    }

    private ReflectionUtils() {}

    /**
     * Get a NMS (net.minecraft.server) class.
     *
     * @param name the name of the class.
     *
     * @return the NMS class or null if not found.
     * @since 1.0.0
     */
    @Nullable
    public static Class<?> getNMSClass(@Nonnull String name) {
        try {
            return Class.forName(NMS + name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Sends a packet to the player asynchronously if they're online.
     * Packets are thread-safe.
     *
     * @param player  the player to send the packet to.
     * @param packets the packets to send.
     *
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
     *
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
     *
     * @return the CraftBukkit class or null if not found.
     * @since 1.0.0
     */
    @Nullable
    public static Class<?> getCraftClass(@Nonnull String name) {
        try {
            return Class.forName(CRAFTBUKKIT + name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
