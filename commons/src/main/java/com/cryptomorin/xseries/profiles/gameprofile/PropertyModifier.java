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

import com.google.common.collect.Multimap;
import com.mojang.authlib.properties.Property;

public final class PropertyModifier {
    private final Multimap<String, Property> properties;

    public PropertyModifier(Multimap<String, Property> properties) {this.properties = properties;}

    public void setProperty(String name, String value) {
        properties.removeAll(name);
        properties.put(name, new Property(name, value));
    }

    public void setProperty(String name, Property value) {
        properties.removeAll(name);
        properties.put(name, value);
    }

    public void removeProperty(String name) {
        properties.removeAll(name);
    }

    public Multimap<String, Property> getProperties() {
        return properties;
    }
}
