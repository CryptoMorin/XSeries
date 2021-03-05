package com.cryptomorin.xseries.section;

import lombok.experimental.Delegate;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
        ConfigurationSection createSection(String path);
        ConfigurationSection getConfigurationSection(String path);
    }
}
