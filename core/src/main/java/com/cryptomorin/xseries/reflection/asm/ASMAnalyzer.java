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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.SimpleVerifier;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.PrintWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

/**
 * Custom analyzer because ASM's default analyzer will complain if a class is used that's not loaded.
 * We can't do anything about that because some classes contain references to other proxy classes
 * that we have to generate as well, we need to ignore these classes.
 */
final class ASMAnalyzer {
    // static void printAnalyzerResult(MethodNode method, Analyzer<BasicValue> analyzer, PrintWriter printWriter)
    private static final MethodHandle CheckClassAdapter_printAnalyzerResult;

    private ASMAnalyzer() {}

    static {
        MethodHandle printAnalyzerResult;
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            Method printer = CheckClassAdapter.class.getDeclaredMethod("printAnalyzerResult", MethodNode.class, Analyzer.class, PrintWriter.class);
            printer.setAccessible(true);
            printAnalyzerResult = lookup.unreflect(printer);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            printAnalyzerResult = null;
        }
        CheckClassAdapter_printAnalyzerResult = printAnalyzerResult;
    }

    protected static Throwable findCause(Throwable exception, Predicate<Throwable> findCause) {
        Set<Throwable> circularCause = Collections.newSetFromMap(new IdentityHashMap<>(5));

        Throwable cause = exception;
        do {
            if (!circularCause.add(cause)) return null;
            if (findCause.test(cause)) return cause;
            cause = cause.getCause();
        } while (cause != null);

        return null;
    }

    protected static void verify(
            final ClassReader classReader,
            final ClassLoader loader,
            final boolean printResults,
            final PrintWriter printWriter) {
        ClassNode classNode = new ClassNode();
        classReader.accept(
                new CheckClassAdapter(ASMVersion.LATEST_ASM_OPCODE_VERSION, classNode, false) {},
                ClassReader.SKIP_DEBUG);

        Type syperType = classNode.superName == null ? null : Type.getObjectType(classNode.superName);
        List<MethodNode> methods = classNode.methods;

        List<Type> interfaces = new ArrayList<>();
        for (String interfaceName : classNode.interfaces) {
            interfaces.add(Type.getObjectType(interfaceName));
        }

        for (MethodNode method : methods) {
            SimpleVerifier verifier =
                    new SimpleVerifier(
                            Type.getObjectType(classNode.name),
                            syperType,
                            interfaces,
                            (classNode.access & Opcodes.ACC_INTERFACE) != 0);
            Analyzer<BasicValue> analyzer = new Analyzer<>(verifier);
            if (loader != null) {
                verifier.setClassLoader(loader);
            }

            boolean hasError;
            try {
                analyzer.analyze(classNode.name, method);
                hasError = false;
            } catch (AnalyzerException e) {
                ClassNotFoundException cls = (ClassNotFoundException) findCause(e, cause -> cause instanceof ClassNotFoundException);
                if (cls == null || cls.getMessage() == null || !cls.getMessage().contains("XSeriesGen")) {
                    hasError = true;
                    e.printStackTrace(printWriter);
                } else {
                    hasError = false;
                }
            }

            if (printResults || hasError) {
                try {
                    CheckClassAdapter_printAnalyzerResult.invokeExact(method, analyzer, printWriter);
                } catch (Throwable e) {
                    throw new IllegalStateException("Cannot write bytecode instructions: ", e);
                }
            }
        }

        printWriter.flush();
    }
}
