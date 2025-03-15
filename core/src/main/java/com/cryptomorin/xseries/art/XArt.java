package com.cryptomorin.xseries.art;

import org.bukkit.Art;

public class XArt {
    private XArt() {}

    private static final boolean USE_INTERFACE = Art.class.isInterface();

    public static BukkitArt of(Art art) {
        if (USE_INTERFACE) return new NewArt(art);
        else return new OldArt(art);
    }
}
