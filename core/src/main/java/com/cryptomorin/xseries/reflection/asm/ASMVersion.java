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

package com.cryptomorin.xseries.reflection.asm;

import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;

final class ASMVersion {
    private ASMVersion() {}

    protected static final int
            LATEST_ASM_OPCODE_VERSION, USED_ASM_OPCODE_VERSION,
            CURRENT_JAVA_VERSION, CURRENT_JAVA_FILE_FORMAT, USED_JAVA_FILE_FORMAT, LATEST_SUPPORTED_JAVA_CLASS_FILE_FORMAT_VERSION;

    static {
        CURRENT_JAVA_VERSION = getJavaVersion();
        CURRENT_JAVA_FILE_FORMAT = javaVersionToClassFileFormat(CURRENT_JAVA_VERSION);

        int latestAsm = 0;
        int latestJava = 0;

        try {
            for (Field field : Opcodes.class.getDeclaredFields()) {
                String name = field.getName();
                if (name.contains("EXPERIMENTAL")) continue;

                if (name.startsWith("ASM")) {
                    latestAsm = Math.max(latestAsm, field.getInt(null));
                }
                if (name.startsWith("V") && name.length() <= 4) {
                    latestJava = Math.max(latestJava, field.getInt(null));
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }

        // Due to maybe unwanted obfuscation of classes or future-proofing Opcodes class format?
        if (latestAsm == 0) latestAsm = Opcodes.ASM6;
        if (latestJava == 0) latestJava = Opcodes.V1_8;

        LATEST_ASM_OPCODE_VERSION = latestAsm;
        LATEST_SUPPORTED_JAVA_CLASS_FILE_FORMAT_VERSION = latestJava;

        int usedAsmVersion = latestAsm;
        int usedJavaVersion = Math.min(CURRENT_JAVA_FILE_FORMAT, LATEST_SUPPORTED_JAVA_CLASS_FILE_FORMAT_VERSION);
        try {
            String asmVer = System.getProperty("xseries.xreflection.asm.version");
            String javaVersion = System.getProperty("xseries.xreflection.asm.javaVersion");

            if (asmVer != null) {
                usedAsmVersion = Integer.parseInt(asmVer);
                System.out.println("[XSeries/XReflection] Using custom ASM version: " + usedAsmVersion);
            }
            if (javaVersion != null) {
                usedJavaVersion = Integer.parseInt(javaVersion);
                System.out.println("[XSeries/XReflection] Using custom ASM Java target version: " + usedJavaVersion);
            }
        } catch (SecurityException ignored) {
            // If we don't have access to system properties, don't care.
        }

        USED_ASM_OPCODE_VERSION = usedAsmVersion;
        USED_JAVA_FILE_FORMAT = usedJavaVersion;
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    protected static int getASMOpcodeVersion(int asmVersion) {
        return asmVersion << 16 | 0 << 8;
    }

    protected static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf('.');
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    protected static int javaVersionToClassFileFormat(int version) {
        return 0 << 16 | (44 + version);
    }
}
