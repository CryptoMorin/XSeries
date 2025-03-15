package com.cryptomorin.xseries.inventory;

import org.bukkit.inventory.InventoryView;

/**
 * java.lang.IncompatibleClassChangeError: Found class org.bukkit.inventory.InventoryView, but interface was expected
 */
public final class XInventoryView {
    private static final boolean USE_INTERFACE = InventoryView.class.isInterface();

    private XInventoryView() {}

    public static BukkitInventoryView of(InventoryView inventoryView) {
        if (USE_INTERFACE) return new NewInventoryView(inventoryView);
        else return new OldInventoryView(inventoryView);
    }
}
