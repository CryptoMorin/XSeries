package com.cryptomorin.xseries.art;

import org.bukkit.Art;

class NewArt extends BukkitArt {
    private final Art art;

    public NewArt(Object art) {this.art = (Art) art;}

    @Override
    public int getBlockWidth() {
        return art.getBlockWidth();
    }

    @Override
    public int getBlockHeight() {
        return art.getBlockHeight();
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getKey() {
        return art.getKey().getKey();
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
