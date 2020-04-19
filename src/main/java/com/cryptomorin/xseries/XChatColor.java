package com.cryptomorin.xseries;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A utility class for more ChatColor methods
 *
 * @author Silent
 * @version 1.0.0
 */
public class XChatColor {

    private static final Random r = new Random();

    /**
     * List of available chat colors.
     *
     * @since 1.0.0
     */
    public static final List<ChatColor> CHAT_COLORS = Arrays.asList(
            ChatColor.DARK_BLUE,
            ChatColor.DARK_GREEN,
            ChatColor.DARK_AQUA,
            ChatColor.DARK_RED,
            ChatColor.DARK_PURPLE,
            ChatColor.GOLD,
            ChatColor.BLUE,
            ChatColor.GREEN,
            ChatColor.AQUA,
            ChatColor.RED,
            ChatColor.LIGHT_PURPLE,
            ChatColor.YELLOW
    );

    /**
     * List of special color codes
     *
     * @since 1.0.0
     */
    public static final List<ChatColor> CHAT_COLORS_SPECIAL = Arrays.asList(
            ChatColor.BOLD,
            ChatColor.ITALIC,
            ChatColor.MAGIC,
            ChatColor.STRIKETHROUGH,
            ChatColor.UNDERLINE,
            ChatColor.RESET
    );

    /**
     * Get random color
     *
     * @return Random color
     * @since 1.0.0
     */
    public static ChatColor getRandom() {
        return CHAT_COLORS.get(r.nextInt(CHAT_COLORS.size()));
    }

    /**
     * Get matrix color
     *
     * @return Matrix color
     * @since 1.0.0
     */
    public static ChatColor getRandomMatrix() {
        return CHAT_COLORS_SPECIAL.get(r.nextInt(CHAT_COLORS_SPECIAL.size()));
    }

    /**
     * Make input matrix (with special color codes)
     *
     * @param input String to turn matrix
     * @return The string in matrix format
     * @since 1.0.0
     */
    public static String matrix(String input) {
        StringBuilder builder = new StringBuilder();

        for (char c : input.toCharArray()) {
            builder.append(XChatColor.getRandomMatrix()).append(c);
        }

        return builder.toString();
    }

    /**
     * Make input nyan (rainbow colors)
     *
     * @param input String to turn rainbow
     * @return The string in rainbow format
     * @since 1.0.0
     */
    public static String rainbow(String input) {
        StringBuilder builder = new StringBuilder();

        for (char c : input.toCharArray()) {
            builder.append(XChatColor.getRandom()).append(c);
        }

        return builder.toString();
    }

    /**
     * Translate color codes
     *
     * @param text Text to translate colors
     * @return translated text
     * @since 1.0.0
     */
    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

}
