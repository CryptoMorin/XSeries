/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Crypto Morin
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

package com.cryptomorin.xseries.base;

import com.google.common.base.Preconditions;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

class XNamespacedKey {
    private static final boolean SUPPORTS_NamespacedKey_fromString;

    static {
        boolean supportsFromString;

        try {
            Class<?> NamespacedKey = Class.forName("org.bukkit.NamespacedKey");
            NamespacedKey.getDeclaredMethod("fromString", String.class);
            supportsFromString = true;
        } catch (Throwable ex) {
            supportsFromString = false;
        }

        SUPPORTS_NamespacedKey_fromString = supportsFromString;
    }

    private static boolean isValidNamespaceChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '.' || c == '_' || c == '-';
    }

    private static boolean isValidKeyChar(char c) {
        return isValidNamespaceChar(c) || c == '/';
    }

    private static boolean isValidNamespace(String namespace) {
        int len = namespace.length();
        if (len == 0) {
            return false;
        }

        for (int i = 0; i < len; i++) {
            if (!isValidNamespaceChar(namespace.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean isValidKey(String key) {
        int len = key.length();
        if (len == 0) {
            return false;
        }

        for (int i = 0; i < len; i++) {
            if (!isValidKeyChar(key.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    protected static NamespacedKey fromString(@NotNull String string) {
        if (SUPPORTS_NamespacedKey_fromString) return NamespacedKey.fromString(string);

        Preconditions.checkArgument(string != null && !string.isEmpty(), "Input string must not be empty or null");
        String[] components = string.split(":", 3);
        if (components.length > 2) {
            return null;
        } else {
            String key = components.length == 2 ? components[1] : "";
            String namespace;
            if (components.length == 1) {
                namespace = components[0];
                if (!namespace.isEmpty() && isValidKey(namespace)) {
                    return NamespacedKey.minecraft(namespace);
                } else {
                    return null;
                }
            } else if (components.length == 2 && !isValidKey(key)) {
                return null;
            } else {
                namespace = components[0];
                if (namespace.isEmpty()) {
                    return NamespacedKey.minecraft(key);
                } else {
                    return !isValidNamespace(namespace) ? null : new NamespacedKey(namespace, key);
                }
            }
        }
    }
}
