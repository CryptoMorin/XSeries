package com.cryptomorin.xseries.section;

import lombok.experimental.Delegate;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * A class that delegates {@link ConfigurationSection} for {@link XConfigurationSection}.
 *
 * @author portlek
 * @version 1.0.0
 */
public final class BukkitConfigurationSection implements XConfigurationSection {

    /**
     * The section to delegate.
     */
    @Nonnull
    @Delegate(excludes = Exclusions.class)
    private final ConfigurationSection section;

    /**
     * Ctor.
     *
     * @param section The section to delegate.
     */
    public BukkitConfigurationSection(@Nonnull ConfigurationSection section) {
        this.section = section;
    }

    @Nonnull
    @Override
    public XConfigurationSection createSection(@Nonnull String path) {
        return new BukkitConfigurationSection(this.section.createSection(path));
    }

    @Nullable
    @Override
    public XConfigurationSection getConfigurationSection(@Nonnull String path) {
        final ConfigurationSection section = this.section.getConfigurationSection(path);
        if (section == null) {
            return null;
        }
        return new BukkitConfigurationSection(section);
    }

    /**
     * An interface to exclude methods from the Bukkit's configuration section.
     */
    private interface Exclusions {
        Map<String, Object> getValues(boolean deep);
        boolean contains(String path, boolean ignoreDefault);
        String getCurrentPath();
        String getName();
        Configuration getRoot();
        ConfigurationSection getParent();
        Object get(String path);
        Object get(String path, Object def);
        ConfigurationSection createSection(String path);
        ConfigurationSection createSection(String path, Map<?, ?> map);
        String getString(String path, String def);
        boolean isString(String path);
        boolean isInt(String path);
        boolean getBoolean(String path, boolean def);
        boolean isBoolean(String path);
        boolean isDouble(String path);
        long getLong(String path, long def);
        boolean isLong(String path);
        List<?> getList(String path);
        List<?> getList(String path, List<?> def);
        boolean isList(String path);
        List<Integer> getIntegerList(String path);
        List<Boolean> getBooleanList(String path);
        List<Double> getDoubleList(String path);
        List<Float> getFloatList(String path);
        List<Long> getLongList(String path);
        List<Byte> getByteList(String path);
        List<Character> getCharacterList(String path);
        List<Short> getShortList(String path);
        List<Map<?, ?>> getMapList(String path);
        <T> T getObject(String path, Class<T> clazz);
        <T> T getObject(String path, Class<T> clazz, T def);
        <T extends ConfigurationSerializable> T getSerializable(String path, Class<T> clazz);
        <T extends ConfigurationSerializable> T getSerializable(String path, Class<T> clazz, T def);
        Vector getVector(String path);
        Vector getVector(String path, Vector def);
        boolean isVector(String path);
        OfflinePlayer getOfflinePlayer(String path);
        OfflinePlayer getOfflinePlayer(String path, OfflinePlayer def);
        boolean isOfflinePlayer(String path);
        ItemStack getItemStack(String path);
        ItemStack getItemStack(String path, ItemStack def);
        boolean isItemStack(String path);
        Color getColor(String path);
        Color getColor(String path, Color def);
        boolean isColor(String path);
        Location getLocation(String path);
        Location getLocation(String path, Location def);
        boolean isLocation(String path);
        ConfigurationSection getConfigurationSection(String path);
        boolean isConfigurationSection(String path);
        ConfigurationSection getDefaultSection();
        void addDefault(String path, Object value);
    }
}
