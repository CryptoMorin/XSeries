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
package com.cryptomorin.xseries.base;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * An abstract version of {@link XBase} which implements more default methods for convenience.
 *
 * @param <XForm>      the class type associated with the Bukkit type defined by XSeries.
 * @param <BukkitForm> the Bukkit class type associated with the XForm.
 */
public abstract class XModule<XForm extends XModule<XForm, BukkitForm>, BukkitForm> implements XBase<XForm, BukkitForm> {
    private final BukkitForm bukkitForm;
    private final String[] names;

    protected XModule(BukkitForm bukkitForm, String[] names) {
        this.bukkitForm = bukkitForm;
        this.names = names;
        // this.names = new String[names.length + 1];
        // System.arraycopy(names, 0, names, 1, names.length);
    }

    /**
     * Should be used for saving data.
     */
    @NotNull
    @Override
    public final String name() {
        return names[0];
    }

    @ApiStatus.Experimental
    protected void setEnumName(XRegistry<XForm, BukkitForm> registry, String enumName) {
        if (names[0] != null)
            throw new IllegalStateException("Enum name already set " + enumName + " -> " + Arrays.toString(names));
        names[0] = enumName;

        BukkitForm newForm = registry.getBukkit(names);
        if (bukkitForm != newForm) {
            // noinspection unchecked
            registry.std((XForm) this);
        }
    }

    @ApiStatus.Internal
    @Override
    public String[] getNames() {
        return names;
    }

    @Nullable
    @Override
    public final BukkitForm get() {
        return bukkitForm;
    }

    @Override
    public final String toString() {
        return (isSupported() ? "" : "!") + getClass().getSimpleName() + '(' + name() + ')';
    }

    /**
     * Identity hash code.
     */
    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * Identity comparison. Should use {@code ==} instead.
     */
    @Override
    @Deprecated
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }
}
