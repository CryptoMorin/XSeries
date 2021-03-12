package com.cryptomorin.xseries;

import net.minecraft.server.v1_16_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.Main;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

abstract class TestSpigot {

    private static Field recentTps;

    private static boolean checkTpsFilled() throws Exception {
        if (recentTps == null) {
            recentTps = MinecraftServer.class.getDeclaredField("recentTps");
        }
        return MinecraftServer.getServer() != null &&
                ((double[]) TestSpigot.recentTps.get(MinecraftServer.getServer()))[0] != 0;
    }

    protected static void runServer(final Runnable tests) {
        try {
            final File here = new File(System.getProperty("user.dir"));
            final Path path = here.toPath();
            final File before = here.getParentFile();
            final Path pathBefore = before.toPath();
            final Path testClassesPath = pathBefore.resolve("test-classes");
            final Path serverProperties = testClassesPath.resolve("server.properties");
            final Path bukkitYml = testClassesPath.resolve("bukkit.yml");
            final Path spigotYml = testClassesPath.resolve("spigot.yml");
            Files.deleteIfExists(path.resolve("world"));
            Files.deleteIfExists(path.resolve("world_nether"));
            Files.deleteIfExists(path.resolve("world_the_end"));
            final Thread thread = new Thread(() -> {
                System.setProperty("com.mojang.eula.agree", "true");
                Main.main(new String[]{
                        "nogui",
                        "noconsole",
                        "--config=" + serverProperties.toString(),
                        "--bukkit-settings=" + bukkitYml.toString(),
                        "--spigot-settings=" + spigotYml.toString()
                });
            });
            thread.start();
            while (!checkTpsFilled()) {
                Thread.sleep(5L);
            }
            Thread.sleep(1000L);
            tests.run();
            Thread.sleep(1000L);
            Bukkit.shutdown();
            thread.interrupt();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
