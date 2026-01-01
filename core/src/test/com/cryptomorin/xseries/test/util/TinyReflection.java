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

package com.cryptomorin.xseries.test.util;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import com.cryptomorin.xseries.test.server.DummyBasicServer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * I still have my doubts whether ReflectionUtils will function correctly.
 * Some methods here are temporary and might switch to ReflectionUtils soon.
 */
public final class TinyReflection {
    public static final Class<?> CraftBukkit$Main;

    static {
        try {
            CraftBukkit$Main = Class.forName("org.bukkit.craftbukkit.Main");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find Bukkit's main class", e);
        }
    }

    public static final Logger LOGGER = Logger.getLogger(CraftBukkit$Main.getCanonicalName());
    public static final String VERSION;
    public static final Object CraftItemFactoryInstance;
    public static final String IMPL_VER, BUKKIT_VER;
    public static Object CraftMagicNumbersInstance;

    static {
        String version = null;
        try {
            version = Files.readAllLines(Paths.get(DummyBasicServer.class.getResource("version.txt").toURI())).get(0);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        VERSION = version;

        Object craftItemFactory = null, craftMagicNumbers = null;
        String bukkitVer = null;

        try {
            craftItemFactory = Arrays.stream(craft("inventory.CraftItemFactory").getMethods()).filter(x -> x.getName().equals("instance")).findFirst().get().invoke(null);
            bukkitVer = (String) craft("util.Versioning").getMethod("getBukkitVersion").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        BUKKIT_VER = bukkitVer;
        IMPL_VER = craft("CraftServer").getPackage().getImplementationVersion();
        CraftItemFactoryInstance = craftItemFactory;
        CraftMagicNumbersInstance = craftMagicNumbers;
    }

    private TinyReflection() {}

    public static Object getCraftMagicNumberInstance() {
        if (CraftMagicNumbersInstance != null) return CraftMagicNumbersInstance;
        try {
            return CraftMagicNumbersInstance = craft("util.CraftMagicNumbers").getField("INSTANCE").get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException("Cannot find CraftMagicNumbers", e);
        }
    }

    public static String getMCVersion() {
        try {
            Class<?> GameVersionClass = Class.forName("com.mojang.bridge.game.GameVersion");
            Object gameVersion = Arrays.stream(XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS).named("MinecraftVersion").unreflect().getMethods())
                    .filter(x -> x.getReturnType() == GameVersionClass).findFirst().get().invoke(null);
            return (String) gameVersion.getClass().getDeclaredMethod("getName").invoke(gameVersion);
        } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException |
                 NoSuchMethodException e) {
            throw new IllegalStateException("Cannot get Minecraft version", e);
        }
    }

    private static Class<?> craft(String str) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + VERSION + '.' + str);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot find craftbukkit class", e);
        }
    }

    @Deprecated
    protected static void bootstrap() {
        try {
            XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "server").named("DispenserRegistry").unreflect()
                    .getMethod("init").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException("Cannot init server", e);
        }
    }
}
