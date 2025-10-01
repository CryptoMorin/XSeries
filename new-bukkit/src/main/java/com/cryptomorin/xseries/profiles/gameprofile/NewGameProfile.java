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

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.function.Consumer;

public final class NewGameProfile extends MojangGameProfile {
    private static final Field PropertyMap_properties$setter;

    static {
        Field props;
        try {
            props = PropertyMap.class.getDeclaredField("properties");
            props.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        PropertyMap_properties$setter = props;
    }

    protected NewGameProfile(GameProfile object) {
        super(object);
    }

    @Override
    public UUID id() {
        return object.id();
    }

    @Override
    public String name() {
        return object.name();
    }

    @Override
    public PropertyMap properties() {
        return object.properties();
    }

    @Override
    public MojangGameProfile copy() {
        ListMultimap<String, Property> copiedProperties = MultimapBuilder.hashKeys().arrayListValues().build();
        copiedProperties.putAll(this.properties());

        GameProfile clone = new GameProfile(id(), name(), new PropertyMap(copiedProperties));
        return new NewGameProfile(clone);
    }

    @Override
    public void addProperty(String name, String value) {
        addProperty(new Property(name, value));
    }

    @Override
    public void addProperty(Property property) {
        modifyProperties(x -> x.put(property.name(), property));
    }

    @Override
    public void removeProperty(String name) {
        modifyProperties(x -> x.removeAll(name));
    }

    @Override
    public void setProperty(String name, String value) {
        modifyProperties(x -> {
            x.removeAll(name);
            x.put(name, new Property(name, value));
        });
    }

    private void modifyProperties(Consumer<Multimap<String, Property>> modifier) {
        try {
            ListMultimap<String, Property> newProps = MultimapBuilder.hashKeys().arrayListValues().build();
            PropertyMap properties = this.properties();

            newProps.putAll(properties);
            modifier.accept(newProps);
            PropertyMap_properties$setter.set(properties, ImmutableMultimap.copyOf(newProps));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
