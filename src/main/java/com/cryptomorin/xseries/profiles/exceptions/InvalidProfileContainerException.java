package com.cryptomorin.xseries.profiles.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * When a provided item/block cannot contain skull textures.
 */
public final class InvalidProfileContainerException extends ProfileException {
    private final Object container;

    public InvalidProfileContainerException(Object container, String message) {
        super(message);
        this.container = container;
    }

    /**
     * A {@link org.bukkit.inventory.ItemStack} or {@link org.bukkit.block.Block}.
     */
    @NotNull
    public Object getContainer() {
        return container;
    }
}