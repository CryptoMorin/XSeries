package com.cryptomorin.xseries.v1_16R2;

import com.cryptomorin.xseries.RunServer;
import net.minecraft.server.v1_16_R2.Main;
import net.minecraft.server.v1_16_R2.MinecraftServer;

import java.lang.reflect.Field;

public abstract class Spigot_1_16R2 extends RunServer {

    private Field recentTps;

    @Override
    protected boolean checkTpsFilled() throws Exception {
        if (recentTps == null) {
            recentTps = MinecraftServer.class.getDeclaredField("recentTps");
        }
        return MinecraftServer.getServer() != null &&
                ((double[]) recentTps.get(MinecraftServer.getServer()))[0] != 0;
    }

    @Override
    protected void main(String[] args) {
        Main.main(parseOptions(args));
    }
}
