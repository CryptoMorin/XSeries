package com.cryptomorin.xseries;

import org.junit.jupiter.api.Test;

import java.io.IOException;

final class XMaterialTests extends TestSpigot {

    @Test
    void testXMaterial() throws InterruptedException, IOException {
        runServer(() -> {
            System.out.println(XMaterial.getVersion());
        });
    }
}
