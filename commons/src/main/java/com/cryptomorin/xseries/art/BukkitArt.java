package com.cryptomorin.xseries.art;

import org.bukkit.Art;

public abstract class BukkitArt {
    public abstract int getBlockWidth();

    public abstract int getBlockHeight();

    public abstract String getKey();

    public abstract int getId();

    public abstract Art object();

    @Override
    public int hashCode() {
        return object().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj instanceof BukkitArt) return object().equals(((BukkitArt) obj).object());
        else return object().equals(obj);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '(' + object().toString() + ')';
    }
}
