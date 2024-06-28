package com.cryptomorin.xseries.reflection.constraint;

import com.cryptomorin.xseries.reflection.ReflectiveHandle;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface ReflectiveConstraint {
    String category();

    String name();

    boolean appliesTo(ReflectiveHandle<?> handle);
}
