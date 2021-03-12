package com.cryptomorin.xseries;

import com.cryptomorin.xseries.versions.Spigot_1_16R1;
import org.junit.jupiter.api.Test;

final class Test1_16R1 extends Spigot_1_16R1 {

    @Test
    void testXMaterial() {
        runServer(() -> {
            System.out.println(XMaterial.getVersion());
        });
    }
}
