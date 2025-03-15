package com.cryptomorin.xseries.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public abstract class BukkitInventoryView {
    public abstract Inventory getTopInventory();

    public abstract Inventory getBottomInventory();

    public abstract HumanEntity getPlayer();

    public abstract InventoryType getType();

    public abstract void setItem(int slot, ItemStack item);

    public abstract ItemStack getItem(int slot);

    public abstract void setCursor(ItemStack item);

    public abstract ItemStack getCursor();

    public abstract int convertSlot(int slot);

    public abstract void close();

    public abstract int countSlots();

    public abstract String getTitle();

    public abstract InventoryView object();

    @Override
    public int hashCode() {
        return object().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj instanceof BukkitInventoryView) return object().equals(((BukkitInventoryView) obj).object());
        else return object().equals(obj);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '(' + object().toString() + ')';
    }
}