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

package com.cryptomorin.xseries.test.reflection;

import com.cryptomorin.xseries.reflection.XAccessFlag;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.constraint.ClassTypeConstraint;
import com.cryptomorin.xseries.reflection.constraint.ReflectiveConstraintException;
import com.cryptomorin.xseries.reflection.constraint.VisibilityConstraint;
import com.cryptomorin.xseries.test.util.XLogger;
import org.junit.jupiter.api.Assertions;

public class ReflectiveConstraintTests {
    private static void testing(String msg) {
        XLogger.log("[Constraints] Testing " + msg + "...");
    }

    public static void test() {
        testXReflection_constraint_visibility();
        testXReflection_constraint_classType();
        testXReflection_constraint_visibility_dontFail();
        testXReflection_constraint_classType_dontFail();
    }

    private static void testXReflection_constraint_visibility() {
        testing("invalid VisibilityConstraint.PROTECTED");
        Assertions.assertThrows(ReflectiveConstraintException.class, () -> {
            XReflection.of(String.class)
                    .constraint(VisibilityConstraint.PROTECTED)
                    .reflect();
        }, () -> "VisibilityConstraint check failed. " + XAccessFlag.toString(String.class.getModifiers()));
    }

    private static void testXReflection_constraint_visibility_dontFail() {
        testing("valid VisibilityConstraint.PROTECTED");
        Assertions.assertDoesNotThrow(() -> {
            // Might be implementation specific?
            XReflection.classHandle()
                    .inPackage("java.lang")
                    .named("Terminator")
                    .constraint(VisibilityConstraint.PROTECTED)
                    .reflect();
        }, "VisibilityConstraint check failed");
    }

    private static void testXReflection_constraint_classType() {
        testing("invalid ClassTypeConstraint.ENUM");
        Assertions.assertThrows(ReflectiveConstraintException.class, () -> {
            XReflection.of(String.class)
                    .constraint(ClassTypeConstraint.ENUM)
                    .reflect();
        }, "ClassTypeConstraint check failed.");
    }

    private static void testXReflection_constraint_classType_dontFail() {
        testing("valid ClassTypeConstraint.ENUM");
        Assertions.assertDoesNotThrow(() -> {
            XReflection.of(ClassTypeConstraint.class)
                    .constraint(ClassTypeConstraint.ENUM)
                    .reflect();
        }, "ClassTypeConstraint check succeeded successfully.");
    }
}
