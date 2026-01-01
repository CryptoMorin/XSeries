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
package com.cryptomorin.xseries;

import com.cryptomorin.xseries.base.XModule;
import com.cryptomorin.xseries.base.XRegistry;
import org.bukkit.Registry;
import org.bukkit.block.banner.PatternType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Optional;

public final class XPatternType extends XModule<XPatternType, PatternType> {
    public static final XRegistry<XPatternType, PatternType> REGISTRY =
            new XRegistry<>(PatternType.class, XPatternType.class, () -> Registry.BANNER_PATTERN, XPatternType::new, XPatternType[]::new);

    public static final XPatternType
            BASE = std("base"),

    SQUARE_BOTTOM_LEFT = std("square_bottom_left"),
            SQUARE_BOTTOM_RIGHT = std("square_bottom_right"),
            SQUARE_TOP_LEFT = std("square_top_left"),
            SQUARE_TOP_RIGHT = std("square_top_right"),

    STRIPE_BOTTOM = std("stripe_bottom"),
            STRIPE_TOP = std("stripe_top"),
            STRIPE_LEFT = std("stripe_left"),
            STRIPE_RIGHT = std("stripe_right"),
            STRIPE_CENTER = std("stripe_center"),
            STRIPE_MIDDLE = std("stripe_middle"),
            STRIPE_DOWNRIGHT = std("stripe_downright"),
            STRIPE_DOWNLEFT = std("stripe_downleft"),

    SMALL_STRIPES = std("small_stripes", /* 1.19.4 */ "STRIPE_SMALL"),

    CROSS = std("cross"),
            STRAIGHT_CROSS = std("straight_cross"),

    TRIANGLE_BOTTOM = std("triangle_bottom"),
            TRIANGLE_TOP = std("triangle_top"),
            TRIANGLES_BOTTOM = std("triangles_bottom"),
            TRIANGLES_TOP = std("triangles_top"),

    DIAGONAL_LEFT = std("diagonal_left"),
            DIAGONAL_UP_RIGHT = std("diagonal_up_right", /* 1.19.4 */ "DIAGONAL_RIGHT_MIRROR"),
            DIAGONAL_UP_LEFT = std("diagonal_up_left", /* 1.19.4 */ "DIAGONAL_LEFT_MIRROR"),
            DIAGONAL_RIGHT = std("diagonal_right"),

    CIRCLE = std("circle", "CIRCLE_MIDDLE"),
            RHOMBUS = std("rhombus", "RHOMBUS_MIDDLE"),

    HALF_VERTICAL = std("half_vertical"),
            HALF_HORIZONTAL = std("half_horizontal"),
            HALF_VERTICAL_RIGHT = std("half_vertical_right", /* 1.19.4 */ "HALF_VERTICAL_MIRROR"),
            HALF_HORIZONTAL_BOTTOM = std("half_horizontal_bottom", /* 1.19.4 */ "HALF_HORIZONTAL_MIRROR"),

    BORDER = std("border"),
            CURLY_BORDER = std("curly_border"),
            CREEPER = std("creeper"),
            GRADIENT = std("gradient"),
            GRADIENT_UP = std("gradient_up"),
            BRICKS = std("bricks"),
            SKULL = std("skull"),
            FLOWER = std("flower"),
            MOJANG = std("mojang"),
            GLOBE = std("globe"),
            PIGLIN = std("piglin"),
            FLOW = std("flow"),
            GUSTER = std("guster");

    static {
        REGISTRY.discardMetadata();
    }

    private XPatternType(PatternType patternType, String[] names) {
        super(patternType, names);
    }

    public static XPatternType of(PatternType patternType) {
        return REGISTRY.getByBukkitForm(patternType);
    }

    public static Optional<XPatternType> of(String patternType) {
        return REGISTRY.getByName(patternType);
    }

    @NotNull
    @Unmodifiable
    public static Collection<XPatternType> getValues() {
        return REGISTRY.getValues();
    }

    private static XPatternType std(String... names) {
        return REGISTRY.std(names);
    }
}
