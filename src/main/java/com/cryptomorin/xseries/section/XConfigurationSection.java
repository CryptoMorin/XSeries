package com.cryptomorin.xseries.section;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * An interface to wrap Bukkit's configuration section.
 */
public interface XConfigurationSection {
    /**
     * Gets a set containing all keys in this section.
     *
     * @param deep Whether or not to get a deep list, as opposed to a shallow
     *             list.
     * @return Set of keys contained within this ConfigurationSection.
     */
    @Nonnull
    Set<String> getKeys(boolean deep);

    /**
     * Checks if this {@link XConfigurationSection} contains the given path.
     *
     * @param path Path to check for existence.
     * @return True if this section contains the requested path, either via
     * default or being set.
     * @throws IllegalArgumentException Thrown when path is null.
     */
    boolean contains(@Nonnull String path);

    /**
     * Checks if this {@link XConfigurationSection} has a value set for the
     * given path.
     *
     * @param path Path to check for existence.
     * @return True if this section contains the requested path, regardless of
     * having a default.
     * @throws IllegalArgumentException Thrown when path is null.
     */
    boolean isSet(@Nonnull String path);

    /**
     * Sets the specified path to the given value.
     *
     * @param path  Path of the object to set.
     * @param value New value to set the path to.
     */
    void set(@Nonnull String path, @Nullable Object value);

    /**
     * Creates an empty {@link XConfigurationSection} at the specified path.
     *
     * @param path Path to create the section at.
     * @return Newly created section
     */
    @Nonnull
    XConfigurationSection createSection(@Nonnull String path);

    /**
     * Gets the requested String by path.
     *
     * @param path Path of the String to get.
     * @return Requested String.
     */
    @Nullable
    String getString(@Nonnull String path);

    /**
     * Gets the requested int by path.
     *
     * @param path Path of the int to get.
     * @return Requested int.
     */
    int getInt(@Nonnull String path);

    /**
     * Gets the requested int by path, returning a default value if not found.
     *
     * @param path Path of the int to get.
     * @param def  The default value to return if the path is not found or is
     *             not an int.
     * @return Requested int.
     */
    int getInt(@Nonnull String path, int def);

    /**
     * Gets the requested boolean by path.
     *
     * @param path Path of the boolean to get.
     * @return Requested boolean.
     */
    boolean getBoolean(@Nonnull String path);

    /**
     * Gets the requested double by path.
     *
     * @param path Path of the double to get.
     * @return Requested double.
     */
    double getDouble(@Nonnull String path);

    /**
     * Gets the requested double by path, returning a default value if not
     * found.
     *
     * @param path Path of the double to get.
     * @param def The default value to return if the path is not found or is
     *     not a double.
     * @return Requested double.
     */
    double getDouble(@Nonnull String path, double def);

    /**
     * Gets the requested long by path.
     *
     * @param path Path of the long to get.
     * @return Requested long.
     */
    long getLong(@Nonnull String path);

    /**
     * Gets the requested List of String by path.
     *
     * @param path Path of the List to get.
     * @return Requested List of String.
     */
    @Nonnull
    List<String> getStringList(@Nonnull String path);

    /**
     * Gets the requested ConfigurationSection by path.
     *
     * @param path Path of the ConfigurationSection to get.
     * @return Requested ConfigurationSection.
     */
    @Nullable
    XConfigurationSection getConfigurationSection(@Nonnull String path);
}
