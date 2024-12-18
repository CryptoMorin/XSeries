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

package com.cryptomorin.xseries.test.server;

import com.cryptomorin.xseries.test.util.ResourceHelper;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Disabled;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This doesn't work...
 */
@Disabled("JUnit attempts to this class, but BiomeProvider isn't loaded into the classloader yet.")
public class EmbeddedPlugin implements InvocationHandler {
    private Plugin delegate;
    private final PluginDescriptionFile description = new PluginDescriptionFile("XSeriesPlugin", "1.0.0", "com.github.cryptomorin.test.XSeriesPlugin");
    private final YamlConfiguration config = new YamlConfiguration();
    private final File folder = DummyAbstractServer.HERE.toPath().resolve("plugins").resolve("XSeries").toFile();
    private boolean naggable = true;
    private PluginLogger logger;

    public static Plugin createInstance() {
        EmbeddedPlugin embed = new EmbeddedPlugin();
        Plugin plugin = (Plugin) java.lang.reflect.Proxy.newProxyInstance(
                Plugin.class.getClassLoader(),
                new Class[]{Plugin.class},
                embed);
        embed.delegate = plugin;
        return plugin;
    }


    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method meth = this.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        return meth.invoke(this, args);
    }

    @NotNull
    public File getDataFolder() {
        return folder;
    }

    @NotNull
    public PluginDescriptionFile getDescription() {
        return description;
    }

    @NotNull
    public FileConfiguration getConfig() {
        return config;
    }

    @Nullable

    public InputStream getResource(@NotNull String s) {
        return ResourceHelper.getResource(s);
    }

    public void saveConfig() {
        try {
            config.save(new File(folder, "config.yml"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void saveDefaultConfig() {
        throw new UnsupportedOperationException();
    }

    public void saveResource(@NotNull String s, boolean b) {
        throw new UnsupportedOperationException();
    }

    public void reloadConfig() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public PluginLoader getPluginLoader() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public Server getServer() {
        return Bukkit.getServer();
    }

    public boolean isEnabled() {
        return true;
    }

    public void onDisable() {}

    public void onLoad() {}

    public void onEnable() {}

    public boolean isNaggable() {
        return naggable;
    }

    public void setNaggable(boolean b) {
        this.naggable = b;
    }

    @Nullable
    public org.bukkit.generator.ChunkGenerator getDefaultWorldGenerator(@NotNull String s, @Nullable String s1) {
        return null;
    }

    // @Nullable
    // public org.bukkit.generator.BiomeProvider getDefaultBiomeProvider(@NotNull String s, @Nullable String s1) {
    //     return null;
    // }

    @NotNull
    public Logger getLogger() {
        // Accesses the server, so don't initialize until needed.
        if (logger == null) logger = new PluginLogger(delegate);
        return logger;
    }

    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return false;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }

    // public com.avaje.ebean.EbeanServer getDatabase() {
    //     return null;
    // }
}
