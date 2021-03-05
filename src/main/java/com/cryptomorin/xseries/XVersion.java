package com.cryptomorin.xseries;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

import javax.annotation.Nonnull;

/**
 * A class that contains versioning methods for Bukkit servers.
 *
 * @author portlek
 * @version 1.0.0
 */
public final class XVersion {

    /**
     * Gets the exact major version (..., 1.9, 1.10, ..., 1.14)
     * In most cases, you shouldn't be using this method.
     *
     * @param version Supports {@link Bukkit#getVersion()}, {@link Bukkit#getBukkitVersion()} and normal formats such as "1.14"
     * @return the exact major version.
     * @see #supports(int)
     * @see #getVersion()
     * @since 2.0.0
     */
    @Nonnull
    public static String getMajorVersion(@Nonnull String version) {
        Validate.notEmpty(version, "Cannot get major Minecraft version from null or empty string");

        // getVersion()
        int index = version.lastIndexOf("MC:");
        if (index != -1) {
            version = version.substring(index + 4, version.length() - 1);
        } else if (version.endsWith("SNAPSHOT")) {
            // getBukkitVersion()
            index = version.indexOf('-');
            version = version.substring(0, index);
        }

        // 1.13.2, 1.14.4, etc...
        int lastDot = version.lastIndexOf('.');
        if (version.indexOf('.') != lastDot) version = version.substring(0, lastDot);

        return version;
    }

    /**
     * Checks if the version is 1.13 Aquatic Update or higher.
     * An invocation of this method yields the cached result from the expression:
     * <p>
     * <blockquote>
     * {@link #supports(int) 13}}
     * </blockquote>
     *
     * @return true if 1.13 or higher.
     * @see #getVersion()
     * @see #supports(int)
     * @since 1.0.0
     */
    public static boolean isNewVersion() {
        return Data.IS_FLAT;
    }

    /**
     * The current version of the server.
     *
     * @return the current server version minor number.
     * @see #isNewVersion()
     * @since 2.0.0
     */
    public static int getVersion() {
        return Data.VERSION;
    }

    /**
     * Checks if the specified version is the same version or higher than the current server version.
     *
     * @param version the major version to be checked. "1." is ignored. E.g. 1.12 = 12 | 1.9 = 9
     * @return true of the version is equal or higher than the current version.
     * @since 2.0.0
     */
    public static boolean supports(int version) {
        return Data.VERSION >= version;
    }

    /**
     * Used for datas that need to be accessed during enum initilization.
     *
     * @since 9.0.0
     */
    private static final class Data {
        /**
         * The current version of the server in the a form of a major version.
         * If the static initialization for this fails, you know something's wrong with the server software.
         *
         * @since 1.0.0
         */
        private static final int VERSION = Integer.parseInt(getMajorVersion(Bukkit.getVersion()).substring(2));
        /**
         * Cached result if the server version is after the v1.13 flattening update.
         *
         * @since 3.0.0
         */
        private static final boolean IS_FLAT = supports(13);
    }
}
