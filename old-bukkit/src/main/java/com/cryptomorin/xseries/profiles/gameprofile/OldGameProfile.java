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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import java.util.UUID;

public final class OldGameProfile extends MojangGameProfile {
    protected OldGameProfile(GameProfile object) {
        super(object);
    }

    @Override
    public UUID id() {
        return object.getId();
    }

    @Override
    public String name() {
        return object.getName();
    }

    @Override
    public PropertyMap properties() {
        return object.getProperties();
    }

    @Override
    public MojangGameProfile copy() {
        GameProfile clone = new GameProfile(id(), name());
        clone.getProperties().putAll(properties());
        return new OldGameProfile(clone);
    }

    @Override
    public void addProperty(String name, String value) {
        addProperty(new Property(name, value));
    }

    @Override
    public void addProperty(Property property) {
        properties().put(property.getName(), property);
    }

    @Override
    public void removeProperty(String name) {
        properties().removeAll(name);
    }

    @Override
    public void setProperty(String name, String value) {
        removeProperty(name);
        addProperty(name, value);
    }
}
