package com.cryptomorin.xseries.reflection.jvm;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Any declaration in Java that can be named (all except constructors).
 * This class should not be used directly.
 */
public interface NamedReflectiveHandle {
    @NotNull
    Set<String> getPossibleNames();
}
