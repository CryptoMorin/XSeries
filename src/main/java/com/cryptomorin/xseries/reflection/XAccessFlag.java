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

package com.cryptomorin.xseries.reflection;

import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * This class is similar to Oracle's {@link java.lang.reflect.AccessFlag} default implementation which only exists on Java 20+
 * It also aims to add a few new APIs such as {@link #isSet(int)} and {@link #isSet(int, XAccessFlag...)}
 *
 * @since 12.0.0
 * @see java.lang.reflect.Modifier
 * @see java.lang.reflect.AccessFlag
 */
@ApiStatus.Experimental
public enum XAccessFlag {
    /**
     * The access flag {@code ACC_PUBLIC}, corresponding to the source
     * modifier {@link Modifier#PUBLIC public}, with a mask value of
     * <code>{@value "0x%04x" Modifier#PUBLIC}</code>.
     */
    PUBLIC(Modifier.PUBLIC, true, Location.CLASS, Location.FIELD, Location.METHOD, Location.INNER_CLASS),

    /**
     * The access flag {@code ACC_PRIVATE}, corresponding to the
     * source modifier {@link Modifier#PRIVATE private}, with a mask
     * value of <code>{@value "0x%04x" Modifier#PRIVATE}</code>.
     */
    PRIVATE(Modifier.PRIVATE, true, Location.FIELD, Location.METHOD, Location.INNER_CLASS),

    /**
     * The access flag {@code ACC_PROTECTED}, corresponding to the
     * source modifier {@link Modifier#PROTECTED protected}, with a mask
     * value of <code>{@value "0x%04x" Modifier#PROTECTED}</code>.
     */
    PROTECTED(Modifier.PROTECTED, true, Location.FIELD, Location.METHOD, Location.INNER_CLASS),

    /**
     * The access flag {@code ACC_STATIC}, corresponding to the source
     * modifier {@link Modifier#STATIC static}, with a mask value of
     * <code>{@value "0x%04x" Modifier#STATIC}</code>.
     */
    STATIC(Modifier.STATIC, true, Location.FIELD, Location.METHOD, Location.INNER_CLASS),

    /**
     * The access flag {@code ACC_FINAL}, corresponding to the source
     * modifier {@link Modifier#FINAL final}, with a mask
     * value of <code>{@value "0x%04x" Modifier#FINAL}</code>.
     */
    FINAL(Modifier.FINAL, true, Location.CLASS, Location.FIELD, Location.METHOD,
            Location.INNER_CLASS,     /* added in 1.1 */
            Location.METHOD_PARAMETER /* added in 8 */
    ),

    /**
     * The access flag {@code ACC_SUPER} with a mask value of {@code 0x0020}.
     *
     * @apiNote
     * In Java SE 8 and above, the JVM treats the {@code ACC_SUPER}
     * flag as set in every class file (JVM Section 4.1).
     */
    SUPER(0x0000_0020, false, Location.CLASS),

    /**
     * The module flag {@code ACC_OPEN} with a mask value of {@code 0x0020}.
     *
     * @see java.lang.module.ModuleDescriptor#isOpen
     */
    OPEN(0x0000_0020, false, Location.MODULE),

    /**
     * The module requires flag {@code ACC_TRANSITIVE} with a mask
     * value of {@code 0x0020}.
     *
     * @see java.lang.module.ModuleDescriptor.Requires.Modifier#TRANSITIVE
     */
    TRANSITIVE(0x0000_0020, false, Location.MODULE_REQUIRES),

    /**
     * The access flag {@code ACC_SYNCHRONIZED}, corresponding to the
     * source modifier {@link Modifier#SYNCHRONIZED synchronized}, with
     * a mask value of <code>{@value "0x%04x" Modifier#SYNCHRONIZED}</code>.
     */
    SYNCHRONIZED(Modifier.SYNCHRONIZED, true, Location.METHOD),

    /**
     * The module requires flag {@code ACC_STATIC_PHASE} with a mask
     * value of {@code 0x0040}.
     *
     * @see java.lang.module.ModuleDescriptor.Requires.Modifier#STATIC
     */
    STATIC_PHASE(0x0000_0040, false, Location.MODULE_REQUIRES),

    /**
     * The access flag {@code ACC_VOLATILE}, corresponding to the
     * source modifier {@link Modifier#VOLATILE volatile}, with a mask
     * value of <code>{@value "0x%04x" Modifier#VOLATILE}</code>.
     */
    VOLATILE(Modifier.VOLATILE, true, Location.FIELD),

    /**
     * The access flag {@code ACC_BRIDGE} with a mask value of
     * <code>{@value "0x%04x" Modifier#BRIDGE}</code>
     *
     * @see Method#isBridge()
     */
    BRIDGE(getPrivateMod("BRIDGE"), false, Location.METHOD),

    /**
     * The access flag {@code ACC_TRANSIENT}, corresponding to the
     * source modifier {@link Modifier#TRANSIENT transient}, with a
     * mask value of <code>{@value "0x%04x" Modifier#TRANSIENT}</code>.
     */
    TRANSIENT(Modifier.TRANSIENT, true, Location.FIELD),

    /**
     * The access flag {@code ACC_VARARGS} with a mask value of
     * <code>{@value "0x%04x" Modifier#VARARGS}</code>.
     *
     * @see Executable#isVarArgs()
     */
    VARARGS(getPrivateMod("VARARGS"), false, Location.METHOD),

    /**
     * The access flag {@code ACC_NATIVE}, corresponding to the source
     * modifier {@link Modifier#NATIVE native}, with a mask value of
     * <code>{@value "0x%04x" Modifier#NATIVE}</code>.
     */
    NATIVE(Modifier.NATIVE, true, Location.METHOD),

    /**
     * The access flag {@code ACC_INTERFACE} with a mask value of
     * {@code 0x0200}.
     *
     * @see Class#isInterface()
     */
    INTERFACE(Modifier.INTERFACE, false, Location.CLASS, Location.INNER_CLASS),

    /**
     * The access flag {@code ACC_ABSTRACT}, corresponding to the
     * source modifier {@link Modifier#ABSTRACT abstract}, with a mask
     * value of <code>{@value "0x%04x" Modifier#ABSTRACT}</code>.
     */
    ABSTRACT(Modifier.ABSTRACT, true, Location.CLASS, Location.METHOD, Location.INNER_CLASS),

    /**
     * The access flag {@code ACC_STRICT}, corresponding to the source
     * modifier {@link Modifier#STRICT strictfp}, with a mask value of
     * <code>{@value "0x%04x" Modifier#STRICT}</code>.
     *
     * @apiNote
     * The {@code ACC_STRICT} access flag is defined for class file
     * major versions 46 through 60, inclusive (JVM Section 4.6),
     * corresponding to Java SE 1.2 through 16.
     */
    STRICT(Modifier.STRICT, true),

    /**
     * The access flag {@code ACC_SYNTHETIC} with a mask value of
     * <code>{@value "0x%04x" Modifier#SYNTHETIC}</code>.
     *
     * @see Class#isSynthetic()
     * @see Executable#isSynthetic()
     * @see java.lang.module.ModuleDescriptor.Modifier#SYNTHETIC
     */
    SYNTHETIC(getPrivateMod("SYNTHETIC"), false,
            Location.CLASS, Location.FIELD, Location.METHOD,
            Location.INNER_CLASS,
            Location.METHOD_PARAMETER, // Added in 8

            // Module-related items added in 9:
            Location.MODULE, Location.MODULE_REQUIRES,
            Location.MODULE_EXPORTS, Location.MODULE_OPENS),

    /**
     * The access flag {@code ACC_ANNOTATION} with a mask value of
     * <code>{@value "0x%04x" Modifier#ANNOTATION}</code>.
     *
     * @see Class#isAnnotation()
     */
    ANNOTATION(getPrivateMod("ANNOTATION"), false, Location.CLASS, Location.INNER_CLASS),

    /**
     * The access flag {@code ACC_ENUM} with a mask value of
     * <code>{@value "0x%04x" Modifier#ENUM}</code>.
     *
     * @see Class#isEnum()
     */
    ENUM(getPrivateMod("ENUM"), false, Location.CLASS, Location.FIELD, Location.INNER_CLASS),

    /**
     * The access flag {@code ACC_MANDATED} with a mask value of
     * <code>{@value "0x%04x" Modifier#MANDATED}</code>.
     */
    MANDATED(getPrivateMod("MANDATED"), false,
            // From 8:
            Location.METHOD_PARAMETER,

            // Starting in 9:
            Location.MODULE, Location.MODULE_REQUIRES, Location.MODULE_EXPORTS, Location.MODULE_OPENS),

    /**
     * The access flag {@code ACC_MODULE} with a mask value of {@code 0x8000}.
     */
    MODULE(0x0000_8000, false, Location.CLASS);

    XAccessFlag(int mask, boolean sourceModifier, Location... locations) {
        this.mask = mask;
        this.sourceModifier = sourceModifier;
        this.locations = locations.length == 0 ?
                EnumSet.noneOf(Location.class) :
                EnumSet.copyOf(Arrays.asList(locations));
    }

    private static int getPrivateMod(String name) {
        // e.g. static final int MANDATED;
        try {
            // Try this solution which is not implementation-dependent.
            Class<?> AccessFlag = Class.forName("java.lang.reflect.AccessFlag");
            Field modField = AccessFlag.getDeclaredField(name);
            Object mod = modField.get(null);

            Method mask = AccessFlag.getDeclaredMethod("mask");
            return (int) mask.invoke(mod);
        } catch (Throwable ex) {
            try {
                return (int) Modifier.class.getDeclaredField(name).get(null);
            } catch (NoSuchFieldException | IllegalAccessException ex2) {
                return 0;
            }
        }
    }

    private final int mask;
    private final boolean sourceModifier;

    private final Set<Location> locations;

    /**
     * The corresponding integer mask for the access flag.
     */
    public int mask() {
        return mask;
    }

    /**
     * Whether the flag has a directly corresponding
     * modifier in the Java programming language.
     */
    public boolean sourceModifier() {
        return sourceModifier;
    }

    /**
     * Kinds of constructs the flag can be applied to in the
     * latest class file format version.
     */
    public Set<Location> locations() {
        return locations;
    }

    public boolean isSet(int mod) {
        return (mod & this.mask) != 0;
    }

    public static boolean isSet(int mod, XAccessFlag... modifiers) {
        for (XAccessFlag modifier : modifiers) {
            if (!modifier.isSet(mod)) return false;
        }
        return true;
    }

    public static String toString(int mods) {
        StringJoiner flags = new StringJoiner(" ", "Flags::" + mods + '(', ")");
        for (XAccessFlag accessFlag : XAccessFlag.values()) {
            if (accessFlag.isSet(mods)) flags.add(accessFlag.name().toLowerCase(Locale.ENGLISH));
        }
        return flags.toString();
    }

    /**
     * A location within a class file where flags can be applied.
     * <p>
     * Note that since these locations represent class file structures
     * rather than language structures many language structures, such
     * as constructors and interfaces, are <em>not</em> present.
     */
    public enum Location {
        /**
         * Class location.
         * JVM Section 4.1 The ClassFile Structure
         */
        CLASS,

        /**
         * Field location.
         * JVM Section 4.5 Fields
         */
        FIELD,

        /**
         * Method location.
         * JVM Section 4.6 Method
         */
        METHOD,

        /**
         * Inner class location.
         * JVM Section 4.7.6 The InnerClasses Attribute
         */
        INNER_CLASS,

        /**
         * Method parameter location.
         * JVM Section 4.7.24. The MethodParameters Attribute
         */
        METHOD_PARAMETER,

        /**
         * Module location
         * JVM Section 4.7.25. The Module Attribute
         */
        MODULE,

        /**
         * Module requires location
         * JVM Section 4.7.25. The Module Attribute
         */
        MODULE_REQUIRES,

        /**
         * Module exports location
         * JVM Section 4.7.25. The Module Attribute
         */
        MODULE_EXPORTS,

        /**
         * Module opens location
         * JVM Section 4.7.25. The Module Attribute
         */
        MODULE_OPENS;
    }
}
