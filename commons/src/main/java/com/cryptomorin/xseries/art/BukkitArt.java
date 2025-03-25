package com.cryptomorin.xseries.art;

import com.cryptomorin.xseries.AbstractReferencedClass;
import org.bukkit.Art;

public abstract class BukkitArt extends AbstractReferencedClass<Art> {
    public abstract int getBlockWidth();

    public abstract int getBlockHeight();

    public abstract String getKey();

    public abstract int getId();
}
