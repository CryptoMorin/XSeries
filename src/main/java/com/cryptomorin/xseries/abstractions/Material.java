package com.cryptomorin.xseries.abstractions;

/**
 * Experimental class for XMaterial Bukkit+Forge abstraction.
 */
interface Material {
    String name();

    boolean isSupported();

    default Material or(Material other) {
        return this.isSupported() ? this : other;
    }

    boolean equals(Object other);

    int hashCode();
}
