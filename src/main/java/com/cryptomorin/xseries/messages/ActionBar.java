/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Crypto Morin
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
package com.cryptomorin.xseries.messages;

import com.cryptomorin.xseries.ReflectionUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * A reflection API for action bars in Minecraft.
 * Fully optimized - Supports 1.8.8+ and above.
 * Requires ReflectionUtils.
 * Messages are not colorized by default.
 * <p>
 * Action bars are text messages that appear above
 * the player's <a href="https://minecraft.gamepedia.com/Heads-up_display">hotbar</a>
 * Note that this is different than the text appeared when switching between items.
 * Those messages show the item's name and are different from action bars.
 * The only natural way of displaying action bars is when mounting.
 * <p>
 * Action bars cannot fade or stay like titles.
 * For static Action bars you'll need to send the packet every
 * 2 seconds (40 ticks) for it to stay on the screen without fading.
 * <p>
 * PacketPlayOutTitle: https://wiki.vg/Protocol#Title
 *
 * @author Crypto Morin
 * @version 2.1.0
 * @see ReflectionUtils
 */
public class ActionBar {
    private static final boolean SIXTEEN;
    private static final boolean SPIGOT;
    /**
     * ChatComponentText JSON message builder.
     */
    private static final MethodHandle CHAT_COMPONENT_TEXT;
    /**
     * PacketPlayOutChat
     */
    private static final MethodHandle PACKET_PLAY_OUT_CHAT;
    /**
     * GAME_INFO enum constant.
     */
    private static final Object CHAT_MESSAGE_TYPE;

    static {
        boolean exists = false;
        if (Material.getMaterial("KNOWLEDGE_BOOK") != null) {
            try {
                Class.forName("org.spigotmc.SpigotConfig");
                exists = true;
            } catch (ClassNotFoundException ignored) {
            }
        }
        SPIGOT = exists;
    }

    static {
        boolean sixteen;
        try {
            Class.forName("org.bukkit.entity.Zoglin");
            sixteen = true;
        } catch (ClassNotFoundException ignored) {
            sixteen = false;
        }
        SIXTEEN = sixteen;
    }

    static {
        MethodHandle packet = null;
        MethodHandle chatComp = null;
        Object chatMsgType = null;

        if (!SPIGOT) {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> packetPlayOutChatClass = ReflectionUtils.getNMSClass("PacketPlayOutChat");
            Class<?> iChatBaseComponentClass = ReflectionUtils.getNMSClass("IChatBaseComponent");

            try {
                // Game Info Message Type
                Class<?> chatMessageTypeClass = Class.forName(ReflectionUtils.NMS + "ChatMessageType");

                // Packet Constructor
                MethodType type;
                if (SIXTEEN) type = MethodType.methodType(void.class, iChatBaseComponentClass, chatMessageTypeClass, UUID.class);
                else type = MethodType.methodType(void.class, iChatBaseComponentClass, chatMessageTypeClass);

                for (Object obj : chatMessageTypeClass.getEnumConstants()) {
                    String name = obj.toString();
                    if (name.equals("GAME_INFO") || name.equalsIgnoreCase("ACTION_BAR")) {
                        chatMsgType = obj;
                        break;
                    }
                }

                // JSON Message Builder
                Class<?> chatComponentTextClass = ReflectionUtils.getNMSClass("ChatComponentText");
                chatComp = lookup.findConstructor(chatComponentTextClass, MethodType.methodType(void.class, String.class));

                packet = lookup.findConstructor(packetPlayOutChatClass, type);
            } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException ignored) {
                try {
                    // Game Info Message Type
                    chatMsgType = (byte) 2;

                    // JSON Message Builder
                    Class<?> chatComponentTextClass = ReflectionUtils.getNMSClass("ChatComponentText");
                    chatComp = lookup.findConstructor(chatComponentTextClass, MethodType.methodType(void.class, String.class));

                    // Packet Constructor
                    packet = lookup.findConstructor(packetPlayOutChatClass, MethodType.methodType(void.class, iChatBaseComponentClass, byte.class));
                } catch (NoSuchMethodException | IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }

        CHAT_MESSAGE_TYPE = chatMsgType;
        CHAT_COMPONENT_TEXT = chatComp;
        PACKET_PLAY_OUT_CHAT = packet;
    }

    /**
     * Sends an action bar to a player.
     *
     * @param player  the player to send the action bar to.
     * @param message the message to send.
     * @see #sendActionBar(JavaPlugin, Player, String, long)
     * @since 1.0.0
     */
    public static void sendActionBar(@Nonnull Player player, @Nullable String message) {
        Objects.requireNonNull(player, "Cannot send action bar to null player");
        if (SPIGOT) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
            return;
        }

        try {
            Object component = CHAT_COMPONENT_TEXT.invoke(message);
            Object packet;
            if (SIXTEEN) packet = PACKET_PLAY_OUT_CHAT.invoke(component, CHAT_MESSAGE_TYPE, player.getUniqueId());
            else packet = PACKET_PLAY_OUT_CHAT.invoke(component, CHAT_MESSAGE_TYPE);

            ReflectionUtils.sendPacket(player, packet);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Sends an action bar all the online players.
     *
     * @param message the message to send.
     * @see #sendActionBar(Player, String)
     * @since 1.0.0
     */
    public static void sendPlayersActionBar(@Nullable final String message) {
        Bukkit.getOnlinePlayers().forEach(p-> sendActionBar(p, message));
    }

    /**
     * Clear the action bar by sending an empty message
     *
     * @param player   the player to send the action bar to.
     * @see #sendActionBar(Player, String)
     * @since 2.1.0
     */
    public static void clearActionBar(Player player){
        sendActionBar(player, " ");
    }

    /**
     * Clear the action bar by sending an empty message to all the online players
     *
     * @see #clearActionBar(Player player)
     * @since 2.1.0
     */
    public static void clearPlayersActionBar(){
        Bukkit.getOnlinePlayers().forEach(ActionBar::clearActionBar);
    }

    /**
     * Sends an action bar to a player for a specific amount of ticks.
     * Plugin instance should be changed in this method for the schedulers.
     * <p>
     * If the caller returns true, the action bar will continue.
     * If the caller returns false, action bar will not be sent anymore.
     *
     * @param player   the player to send the action bar to.
     * @param message  the message to send. The message will not be updated.
     * @param callable the condition for the action bar to continue.
     * @see #sendActionBar(JavaPlugin, Player, String, long)
     * @since 1.0.0
     */
    public static void sendActionBarWhile(@Nonnull JavaPlugin plugin, @Nonnull Player player, @Nullable String message, @Nonnull Callable<Boolean> callable) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (!callable.call()) {
                        cancel();
                        return;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                sendActionBar(player, message);
            }
            // Re-sends the messages every 2 seconds so it doesn't go away from the player's screen.
        }.runTaskTimerAsynchronously(plugin, 0L, 40L);
    }

    /**
     * Sends an action bar to a player for a specific amount of ticks.
     * <p>
     * If the caller returns true, the action bar will continue.
     * If the caller returns false, action bar will not be sent anymore.
     *
     * @param player   the player to send the action bar to.
     * @param message  the message to send. The message will be updated.
     * @param callable the condition for the action bar to continue.
     * @see #sendActionBarWhile(JavaPlugin, Player, String, Callable)
     * @since 1.0.0
     */
    public static void sendActionBarWhile(@Nonnull JavaPlugin plugin, @Nonnull Player player, @Nullable Callable<String> message, @Nonnull Callable<Boolean> callable) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (!callable.call()) {
                        cancel();
                        return;
                    }
                    sendActionBar(player, message.call());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            // Re-sends the messages every 2 seconds so it doesn't go away from the player's screen.
        }.runTaskTimerAsynchronously(plugin, 0L, 40L);
    }

    /**
     * Sends an action bar to a player for a specific amount of ticks.
     *
     * @param player   the player to send the action bar to.
     * @param message  the message to send.
     * @param duration the duration to keep the action bar in ticks.
     * @see #sendActionBarWhile(JavaPlugin, Player, String, Callable)
     * @since 1.0.0
     */
    public static void sendActionBar(@Nonnull JavaPlugin plugin, @Nonnull Player player, @Nullable String message, long duration) {
        if (duration < 1) return;

        new BukkitRunnable() {
            long repeater = duration;

            @Override
            public void run() {
                sendActionBar(player, message);
                repeater -= 40L;
                if (repeater - 40L < -20L) cancel();
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 40L);
    }
}
