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

package com.cryptomorin.xseries.profiles.gameprofile;

import com.cryptomorin.xseries.AbstractReferencedClass;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import java.util.UUID;

public abstract class MojangGameProfile extends AbstractReferencedClass<GameProfile> {
    protected final GameProfile object;

    protected MojangGameProfile(GameProfile object) {this.object = object;}

    @Override
    public final GameProfile object() {
        return object;
    }

    public abstract UUID id();

    public abstract String name();

    public abstract PropertyMap properties();

    public abstract void addProperty(Property property);

    public abstract void addProperty(String name, String value);

    public abstract void removeProperty(String name);

    public abstract void setProperty(String name, String value);

    public abstract MojangGameProfile copy();

    public Property getProperty(String name) {
        return Iterables.getFirst(this.properties().get(name), null);
    }
}
