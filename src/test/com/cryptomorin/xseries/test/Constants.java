/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Crypto Morin
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

package com.cryptomorin.xseries.test;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.test.util.XLogger;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Constants {
    private Constants() {}

    public static final Object LOCK = new Object();

    @SuppressWarnings("ConstantValue")
    public static Path getTestPath() {
        // It's inside "XSeries\target\tests" folder.
        XLogger.log("System test path is " + System.getProperty("user.dir"));
        if (Bukkit.getServer() == null) {
            return Paths.get(System.getProperty("user.dir"));
        } else {
            return Bukkit.getWorldContainer().toPath();
        }
    }

    /**
     * This sends unnecessary requests to Mojang and also delays out work too,
     * so let's not test when it's not needed.
     */
    public static final boolean TEST_MOJANG_API = false;

    public static final boolean TEST_MOJANG_API_BULK = false;

    public static final boolean TEST = true;

    public static final boolean BENCHMARK = false;

    public static void disableXReflectionMinecraft() {
        System.setProperty(XReflection.DISABLE_MINECRAFT_CAPABILITIES_PROPERTY, "");
    }

    public static World getMainWorld() {
        return Bukkit.getWorlds().get(0);
    }
}
