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

package com.cryptomorin.xseries.paper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApiStatus.Internal
public final class AdventureAPIFactory {
    private static final MiniMessage DEFAULT_MINI_MESSAGE = MiniMessage.miniMessage();

    public static void setDisplayNameFromString(ItemMeta meta, String miniMessageText) {
        Component displayName = DEFAULT_MINI_MESSAGE.deserialize(miniMessageText);
        meta.displayName(displayName);
    }

    public static void setLoreFromString(ItemMeta meta, List<String> miniMessageLoreLines) {
        List<Component> lore = miniMessageLoreLines.stream()
                .map(line -> line.isEmpty() ? Component.empty() : DEFAULT_MINI_MESSAGE.deserialize(line))
                .collect(Collectors.toList());

        meta.lore(lore);
    }

    public static void setDisplayName(ItemMeta meta, Component displayname) {
        meta.displayName(displayname);
    }

    public static void setLore(ItemMeta meta, List<? extends Component> lore) {
        meta.lore(lore);
    }

    public static String displayName(ItemMeta meta, Function<List<? extends Component>, List<String>> miniMessageHandler) {
        return miniMessageHandler.apply(Collections.singletonList(meta.displayName())).get(0);
    }

    public static List<String> lore(ItemMeta meta, Function<List<? extends Component>, List<String>> miniMessageHandler) {
        return miniMessageHandler.apply(meta.lore());
    }
}
