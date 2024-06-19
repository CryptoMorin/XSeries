package com.cryptomorin.xseries.abstractions;

import org.jetbrains.annotations.ApiStatus;

/**
 * Experimental class for XMaterial Bukkit+Forge abstraction.
 */
@ApiStatus.Experimental
interface Material {
    String name();

    boolean isSupported();

    default Material or(Material other) {
        return this.isSupported() ? this : other;
    }

    boolean equals(Object other);

    int hashCode();
}
