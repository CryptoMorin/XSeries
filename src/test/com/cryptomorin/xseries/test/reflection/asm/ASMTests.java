/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.cryptomorin.xseries.test.reflection.asm;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.asm.XReflectASM;
import com.cryptomorin.xseries.test.Constants;
import com.cryptomorin.xseries.test.reflection.proxy.ProxyTestProxified;
import com.cryptomorin.xseries.test.reflection.proxy.ProxyTests;
import com.cryptomorin.xseries.test.util.XLogger;

public final class ASMTests {
    public static void test() {
        XLogger.log("[ASM] Testing XReflectASM generation...");
        try {
            XLogger.log("[ASM] asm exists? " + Class.forName("org.objectweb.asm.Opcodes"));
            XLogger.log("[ASM] asm exists2? " + Class.forName("org.objectweb.asm.ClassWriter"));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("ASM is not running!", e);
        }

        XReflectASM<ProxyTestProxified> asm = XReflectASM.proxify(ProxyTestProxified.class);
        asm.writeToFile(Constants.getTestPath());
        ProxyTestProxified factoryInstance = asm.create();
        ProxyTests.normalProxyTest(factoryInstance);

        if (XReflection.supports(20))
            ProxyTests.minecraftProxyTest((clazz) -> XReflectASM.proxify(clazz).create());
    }
}
