package com.cryptomorin.xseries.inventory;

import com.cryptomorin.xseries.AbstractReferencedClass;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public abstract class BukkitInventoryView extends AbstractReferencedClass<InventoryView> {
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
}