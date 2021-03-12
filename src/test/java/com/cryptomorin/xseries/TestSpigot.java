package com.cryptomorin.xseries;

import net.minecraft.server.v1_16_R3.MinecraftServer;
import org.bukkit.craftbukkit.Main;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
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

    protected static void runServer(final Runnable tests) throws InterruptedException {
        final File here = new File(System.getProperty("user.dir"));
        final Path path = here.toPath();
        final Thread thread = new Thread(() -> {
            System.setProperty("com.mojang.eula.agree", "true");
            Main.main(new String[]{
                    "nogui"
            });
        });
        thread.start();
        while (!checkTpsFilled()) {
            Thread.sleep(1000L);
        }
        tests.run();
        MinecraftServer.getServer().safeShutdown(false);
        thread.interrupt();
    }
}
