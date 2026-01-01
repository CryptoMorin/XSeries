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

package com.cryptomorin.xseries.test.reflection.asm;

import com.cryptomorin.xseries.reflection.asm.XReflectASM;
import com.cryptomorin.xseries.test.util.XLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public final class Hacks {
    /**
     * You also have to open java.base/java.lang to the module that this code is in,
     * but you can just do that with a --add-opens argument.
     */
    private static void addOpenOptions() {
        // Caused by: java.lang.reflect.InaccessibleObjectException: Unable to make private void java.lang.Module.implAddExportsOrOpens(java.lang.String,java.lang.Module,boolean,boolean) accessible: module java.base does not "opens java.lang" to unnamed module @51b279c9
        // 	at java.base/java.lang.reflect.AccessibleObject.throwInaccessibleObjectException(AccessibleObject.java:391)
        // 	at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:367)
        // 	at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:315)
        // 	at java.base/java.lang.reflect.Method.checkCanSetAccessible(Method.java:203)
        // 	at java.base/java.lang.reflect.Method.setAccessible(Method.java:197)

        try {
            final Module unnamedModule = XReflectASM.class.getClassLoader().getUnnamedModule();
            final Method method = Module.class.getDeclaredMethod("implAddExportsOrOpens", String.class, Module.class, boolean.class, boolean.class);
            method.setAccessible(true);

            ModuleLayer.boot().modules().forEach(module -> {
                try {
                    final Set<String> packages = module.getPackages();
                    for (String eachPackage : packages) {
                        method.invoke(module, eachPackage, unnamedModule, true, true);
                        XLogger.log("--add-open " + module.getName() + '/' + eachPackage + '=' + unnamedModule.toString());
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
