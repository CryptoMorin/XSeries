package com.cryptomorin.xseries.art;

import org.bukkit.Art;

class OldArt extends BukkitArt {
    private final Art art;

    public OldArt(Object art) {this.art = (Art) art;}

    @Override
    public int getBlockWidth() {
        return art.getBlockWidth();
    }

    @Override
    public int getBlockHeight() {
        return art.getBlockHeight();
    }

    @Override
    public String getKey() {
        return art.name();
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getId() {
        return art.getId();
    }

    @Override
    public Art object() {
        return art;
    }
}
