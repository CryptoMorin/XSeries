package com.cryptomorin.xseries;

import com.cryptomorin.xseries.versions.Spigot_1_16R3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class Test1_16R3 extends Spigot_1_16R3 {

    @Test
    void testXMaterial() {
        runServer(() -> {
            Assertions.assertTrue(XMaterial.matchXMaterial("AIR").isPresent());
        });
    }
}
