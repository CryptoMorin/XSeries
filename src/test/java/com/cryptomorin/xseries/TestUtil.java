package com.cryptomorin.xseries;

import org.junit.jupiter.api.Assertions;

public final class TestUtil {

    public static final Runnable USUAL_TEST = () -> {
        testXMaterial();
    };

    private TestUtil() {
    }

    public static void testXMaterial() {
        Assertions.assertTrue(XMaterial.matchXMaterial("AIR").isPresent());
    }
}
