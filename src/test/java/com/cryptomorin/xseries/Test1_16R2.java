package com.cryptomorin.xseries;

import com.cryptomorin.xseries.versions.Spigot_1_16R2;
import com.cryptomorin.xseries.versions.Spigot_1_16R3;
import org.junit.jupiter.api.Test;

final class Test1_16R2 extends Spigot_1_16R2 {

    @Test
    void testXMaterial() {
        runServer(() -> {
            System.out.println(XMaterial.getVersion());
        });
    }
}
