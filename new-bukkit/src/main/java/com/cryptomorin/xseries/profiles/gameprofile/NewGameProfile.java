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

package com.cryptomorin.xseries.profiles.gameprofile;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;

public final class NewGameProfile extends MojangGameProfile {
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
    public MojangGameProfile copy(@Nullable Consumer<PropertyModifier> propertyModifier) {
        // We unfortunately can't just modify GameProfile's "properties" using reflection since it's a record.
        // We can't modify the PropertyMap's "properties" field either because of the existance of PropertyMap.EMPTY
        PropertyMap properties = this.properties();
        ListMultimap<String, Property> copiedProperties = MultimapBuilder.hashKeys(properties.size()).arrayListValues(1).build();

        copiedProperties.putAll(properties);
        if (propertyModifier != null) propertyModifier.accept(new PropertyModifier(copiedProperties));

        GameProfile clone = new GameProfile(id(), name(), new PropertyMap(copiedProperties));
        return new NewGameProfile(clone);
    }
}
