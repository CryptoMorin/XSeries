package com.cryptomorin.xseries.reflection.constraint;

import com.cryptomorin.xseries.reflection.ReflectiveHandle;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public enum VisibilityConstraint implements ReflectiveConstraint {
    PUBLIC {
        @Override
        public boolean appliesTo(ReflectiveHandle<?> handle) {
            return false;
        }
    };

    @Override
    public String category() {
        return "Visibility";
    }
}
