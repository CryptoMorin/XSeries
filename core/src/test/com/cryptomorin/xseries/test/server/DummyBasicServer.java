/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2026 Crypto Morin
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

package com.cryptomorin.xseries.test.server;

import com.cryptomorin.xseries.test.util.TinyReflection;
import com.cryptomorin.xseries.test.util.XLogger;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@SuppressWarnings("NewClassNamingConvention")
public class DummyBasicServer extends DummyAbstractServer implements InvocationHandler {
    @Override
    protected InvocationHandler main() {
        return new DummyBasicServer();
    }

    private static boolean run = false;

    @Test
    void test() {
        if (run) throw new IllegalStateException("Server is already running");
        run = true;
        XLogger.log("Running tests... with Java " + System.getProperty("java.version"));
        runServer();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        switch (method.getName()) {
            case "getItemFactory":
                return TinyReflection.CraftItemFactoryInstance;
            case "getName":
                return "XSeries Unit Test";
            case "getVersion":
                return TinyReflection.IMPL_VER + " (MC: " + TinyReflection.getMCVersion() + ')';
            case "getBukkitVersion":
                return TinyReflection.BUKKIT_VER;
            case "getLogger":
                return TinyReflection.LOGGER;
            case "getUnsafe":
                return TinyReflection.getCraftMagicNumberInstance();
            default:
                // case "createBlockData":
                throw new UnsupportedOperationException(String.valueOf(method));
        }
    }
}
