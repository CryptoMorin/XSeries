package com.cryptomorin.xseries;

import org.junit.jupiter.api.Test;

final class XMaterialTests extends TestSpigot {

    @Test
    void testXMaterial() throws Exception {
        runServer(() -> {
            System.out.println(XMaterial.getVersion());
        });
    }
}
