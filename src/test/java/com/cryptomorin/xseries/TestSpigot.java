package com.cryptomorin.xseries;

import net.minecraft.server.v1_16_R3.MinecraftServer;
import org.bukkit.craftbukkit.Main;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

abstract class TestSpigot {

    private static Field recentTps;

    private static boolean checkTpsFilled() {
        if (recentTps == null) {
            try {
                recentTps = MinecraftServer.class.getDeclaredField("recentTps");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        final MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            return false;
        }
        double[] recentTps = null;
        try {
            recentTps = (double[]) TestSpigot.recentTps.get(server);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (recentTps == null) {
            return false;
        }
        return recentTps[0] != 0;
    }

    protected static void runServer(final Runnable tests) throws InterruptedException, IOException {
        final File here = new File(System.getProperty("user.dir"));
        final Path path = here.toPath();
        final File before = here.getParentFile();
        final Path pathBefore = before.toPath();
        final Path testClassesPath = pathBefore.resolve("test-classes");
        final Path serverProperties = testClassesPath.resolve("server.properties");
        final Path bukkitYml = testClassesPath.resolve("bukkit.yml");
        Files.deleteIfExists(path.resolve("world"));
        Files.deleteIfExists(path.resolve("world_nether"));
        Files.deleteIfExists(path.resolve("world_the_end"));
        final Thread thread = new Thread(() -> {
            System.setProperty("com.mojang.eula.agree", "true");
            Main.main(new String[]{
                    "nogui",
                    "noconsole",
                    "--config=" + serverProperties.toString(),
                    "--bukkit-settings=" + bukkitYml.toString()
            });
        });
        thread.start();
        while (!checkTpsFilled()) {
            Thread.sleep(1000L);
        }
        tests.run();
        thread.interrupt();
    }
}
