/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2026 Crypto Morin
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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Objects;

import static com.cryptomorin.xseries.reflection.XReflection.ofMinecraft;
import static com.cryptomorin.xseries.reflection.XReflection.v;

/**
 * Provides general packet-related API for players.
 * <p>
 * <a href="https://wiki.vg/Protocol">Clientbound Packets</a> are considered fake
 * updates to the client without changing the actual data. Since all the data is handled
 * by the server.
 */
public final class MinecraftConnection {
    public static final MinecraftClassHandle ServerPlayer = ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "server.level")
            .map(MinecraftMapping.MOJANG, "ServerPlayer")
            .map(MinecraftMapping.SPIGOT, "EntityPlayer");
    public static final MinecraftClassHandle CraftPlayer = ofMinecraft()
            .inPackage(MinecraftPackage.CB, "entity")
            .named("CraftPlayer");
    public static final MinecraftClassHandle ServerPlayerConnection = ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "server.network")
            .map(MinecraftMapping.MOJANG, "ServerPlayerConnection")
            .map(MinecraftMapping.SPIGOT, "PlayerConnection");
    public static final MinecraftClassHandle ServerGamePacketListenerImpl = ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "server.network")
            .map(MinecraftMapping.MOJANG, "ServerGamePacketListenerImpl")
            .map(MinecraftMapping.SPIGOT, "PlayerConnection");
    public static final MinecraftClassHandle Packet = ofMinecraft()
            .inPackage(MinecraftPackage.NMS, "network.protocol")
            .map(MinecraftMapping.SPIGOT, "Packet");

    private static final MethodHandle PLAYER_CONNECTION = ServerPlayer
            // .getterField(v(20, 5, "connection").v(20, "c").v(17, "b").orElse("playerConnection"))
            .field().getter()
            .returns(ServerGamePacketListenerImpl)
            .map(MinecraftMapping.MOJANG, "connection")
            .map(MinecraftMapping.OBFUSCATED, v(21, 6, "g").v(21, 2, "f").v(20, "c").v(17, "b").orElse("playerConnection"))
            .unreflect();
    /**
     * Responsible for getting the NMS handler {@code EntityPlayer} object for the player.
     * {@code CraftPlayer} is simply a wrapper for {@code EntityPlayer}.
     * Used mainly for handling packet related operations.
     * <p>
     * This is also where the famous player {@code ping} field comes from!
     */
    private static final MethodHandle GET_HANDLE = CraftPlayer
            .method()
            .named("getHandle")
            .returns(ServerPlayer)
            .unreflect();
    /**
     * Sends a packet to the player's client through a {@code NetworkManager} which
     * is where {@code ProtocolLib} controls packets by injecting channels!
     */
    private static final MethodHandle SEND_PACKET = ServerPlayerConnection
            .method()
            .returns(void.class)
            .parameters(Packet)
            .map(MinecraftMapping.MOJANG, "send")
            .map(MinecraftMapping.OBFUSCATED, v(20, 2, "b").v(18, "a").orElse("sendPacket"))
            .unreflect();

    @NotNull
    public static Object getHandle(@NotNull Player player) {
        Objects.requireNonNull(player, "Cannot get handle of null player");
        try {
            return GET_HANDLE.invoke(player);
        } catch (Throwable throwable) {
            throw XReflection.throwCheckedException(throwable);
        }
    }

    /**
     * @return null if the player is no longer online.
     */
    @Nullable
    public static Object getConnection(@NotNull Player player) {
        Objects.requireNonNull(player, "Cannot get connection of null player");
        try {
            Object handle = GET_HANDLE.invoke(player);
            return PLAYER_CONNECTION.invoke(handle);
        } catch (Throwable throwable) {
            throw XReflection.throwCheckedException(throwable);
        }
    }

    /**
     * Sends a packet to the player asynchronously if they're online.
     * Packets are thread-safe.
     *
     * @param player  the player to send the packet to.
     * @param packets the packets to send.
     * @since 1.0.0
     */
    @NotNull
    public static void sendPacket(@NotNull Player player, @NotNull Object... packets) {
        Objects.requireNonNull(player, () -> "Can't send packet to null player: " + Arrays.toString(packets));
        Objects.requireNonNull(packets, () -> "Can't send null packets to player: " + player);
        try {
            Object handle = GET_HANDLE.invoke(player);
            Object connection = PLAYER_CONNECTION.invoke(handle);

            // Checking if the connection is not null is enough. There is no need to check if the player is online.
            if (connection != null) {
                for (Object packet : packets) {
                    Objects.requireNonNull(packet, "Null packet detected between packets array");
                    SEND_PACKET.invoke(connection, packet);
                }
            }
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to send packet to " + player + ": " + Arrays.toString(packets), throwable);
        }
    }
}
