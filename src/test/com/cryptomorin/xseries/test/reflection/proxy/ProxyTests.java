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

package com.cryptomorin.xseries.test.reflection.proxy;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.proxy.ReflectiveProxy;
import com.cryptomorin.xseries.reflection.proxy.ReflectiveProxyObject;
import com.cryptomorin.xseries.reflection.proxy.generator.XProxifier;
import com.cryptomorin.xseries.test.Constants;
import com.cryptomorin.xseries.test.reflection.proxy.minecraft.BlockPos;
import com.cryptomorin.xseries.test.reflection.proxy.minecraft.CraftWorld;
import com.cryptomorin.xseries.test.util.XLogger;
import org.bukkit.World;

import java.lang.invoke.*;
import java.util.function.Function;

import static com.cryptomorin.xseries.test.util.XLogger.log;
import static org.junit.jupiter.api.Assertions.*;

public final class ProxyTests {
    public static void test() {
        XLogger.log("[Proxy] Testing ReflectiveProxy generation...");
        normalProxyTest(ReflectiveProxy.proxify(ProxyTestProxified.class).proxy());
        if (XReflection.supports(20)) minecraftProxyTest((x) -> ReflectiveProxy.proxify(x).proxy());

        new XProxifier(ProxyTestClass.class).writeTo(Constants.getTestPath());
        // new XProxifier(
        //         XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "server.level")
        //                 .map(MinecraftMapping.MOJANG, "ServerPlayer")
        //                 .map(MinecraftMapping.SPIGOT, "EntityPlayer")
        //                 .map(MinecraftMapping.OBFUSCATED, "are")
        //                 .unreflect()
        // ).writeTo(Constants.getTestPath());

        testCreateLambda();
        testCreateXReflectionLambda();
    }

    public static void normalProxyTest(ProxyTestProxified factoryProxy) {
        assertSame(factoryProxy.getTargetClass(), ProxyTestClass.class);

        // Final member tests.
        int initialValue = ProxyTestClass.id;
        assertEquals(ProxyTestClass.finalId, factoryProxy.finalId());
        assertEquals(ProxyTestClass.finalId, factoryProxy.finalId());
        assertEquals(ProxyTestClass.finalId, factoryProxy.finalId());
        assertEquals(ProxyTestClass.id, factoryProxy.id());
        assertFalse(factoryProxy.isBeyond555());
        factoryProxy.id(777);
        assertEquals(777, factoryProxy.id());
        assertTrue(factoryProxy.isBeyond555());
        factoryProxy.id(initialValue); // We don't want other tests to fail
        assertEquals("0123456789", factoryProxy.doStaticThings(10).toString());

        ProxyTestClass instance = factoryProxy.ProxyTestProxified("OperationTestum", 2025);
        ProxyTestProxified unusInstance = factoryProxy.bindTo(instance);

        // isInstance() test
        assertTrue(factoryProxy.isInstance(instance));
        assertTrue(factoryProxy.isInstance(unusInstance.instance()));
        assertFalse(factoryProxy.isInstance(unusInstance));

        // First instance member tests
        assertEquals("OperationTestum", unusInstance.operationField());
        assertEquals(2025, unusInstance.date());
        assertEquals("OperationTestum12false", unusInstance.getSomething("12", false));
        unusInstance.iForgotTheName("20", true);
        assertEquals("OperationTestumdoSomething20true", unusInstance.operationField());
        // noinspection StringBufferReplaceableByString
        assertEquals(
                new StringBuilder(10).append(unusInstance.operationField()).append(factoryProxy.finalId()).toString(),
                unusInstance.doSomethingPrivate(10).toString()
        );
        unusInstance.operationField("SomeValue");
        assertEquals("SomeValue", unusInstance.operationField());

        // Cannot invoke constructor twice
        assertThrows(Exception.class, () -> unusInstance.ProxyTestProxified("OperationDuoTestum"));

        // Second instance member tests
        ProxyTestProxified duoInstance = factoryProxy.ProxyTestProxified("OperationDuoTestum");
        assertTrue(factoryProxy.isInstance(duoInstance.instance()));
        assertEquals("0123456789", factoryProxy.doStaticThings(10).toString());
        assertEquals("OperationDuoTestum", duoInstance.operationField());
        assertEquals("soosoo", duoInstance.getSomething("soo"));
        assertEquals(400, duoInstance.getSomething("20", 20));
        assertNotSame(unusInstance.instance(), duoInstance.instance());
    }

    public static void minecraftProxyTest(Function<Class<? extends ReflectiveProxyObject>, ReflectiveProxyObject> proxifier) {
        World bukkitWorld = Constants.getMainWorld();
        // WorldServer nmsWorld = ((org.bukkit.craftbukkit.v1_21_R3.CraftWorld) bukkitWorld).getHandle();
        // boolean changed = nmsWorld.a(new BlockPosition(45, 34, 23), true);

        BlockPos BlockPos = (com.cryptomorin.xseries.test.reflection.proxy.minecraft.BlockPos) proxifier.apply(BlockPos.class);
        CraftWorld CraftWorld = (com.cryptomorin.xseries.test.reflection.proxy.minecraft.CraftWorld) proxifier.apply(CraftWorld.class);

        CraftWorld craftWorld = CraftWorld.bindTo(bukkitWorld);
        BlockPos pos = BlockPos.BlockPos(34, 45, 23);

        boolean changed = craftWorld.getHandle().removeBlock(pos, true);
        log("[MC-Proxy] Block Changed? " + changed);
    }

    private static void testCreateXReflectionLambda() {
        try {
            CallSite callSite = XReflection.of(ProxyTestClass.class).method()
                    .named("getSomething")
                    .returns(String.class)
                    .parameters(String.class, boolean.class)
                    .toLambda(ProxyTestProxified.class, "getSomething");

            ProxyTestClass operationLambda = new ProxyTestClass("OperationLambda", 2026);
            ProxyTestProxified result = (ProxyTestProxified) callSite.dynamicInvoker().invoke(operationLambda);
            log("[XReflection-LambdaMetafactory] Result: " + result.getSomething("fork", true));
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    private static void testCreateLambda() {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType factoryType = MethodType.methodType(String.class, String.class, boolean.class);

        try {
            MethodHandle method = lookup.findVirtual(ProxyTestClass.class, "getSomething", factoryType);

            // String getSomething(String add, boolean add2);
            CallSite callSite = LambdaMetafactory.metafactory(lookup,
                    "getSomething",
                    MethodType.methodType(
                            ProxyTestProxified.class, // the interface to implement
                            ProxyTestClass.class // captured parameters/variables
                    ),
                    factoryType, // Signature and return type of method to be implemented by the function object.
                    method, // MethodHandle
                    factoryType); // Enforced signature (same as normal signature most of the time) helpful when dealing with generics

            ProxyTestClass operationLambda = new ProxyTestClass("OperationLambda", 2026);
            ProxyTestProxified result = (ProxyTestProxified) callSite.dynamicInvoker().invoke(operationLambda);
            log("[LambdaMetafactory] Result: " + result.getSomething("faf", true));
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }
}
