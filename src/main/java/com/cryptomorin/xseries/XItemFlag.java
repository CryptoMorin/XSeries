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
package com.cryptomorin.xseries;

import com.cryptomorin.xseries.base.XBase;
import com.cryptomorin.xseries.base.XRegistry;
import com.cryptomorin.xseries.base.annotations.XChange;
import com.cryptomorin.xseries.base.annotations.XInfo;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public enum XItemFlag implements XBase<XItemFlag, ItemFlag> {
    /**
     * Setting to show/hide potion effects, book and firework information, map tooltips, patterns of banners.
     *
     * @see #HIDE_STORED_ENCHANTS for hiding stored enchants (like on enchanted books)
     * @see XPatternType
     * @see org.bukkit.inventory.meta.FireworkMeta
     * @see org.bukkit.inventory.meta.MapMeta
     * @see org.bukkit.inventory.meta.BookMeta
     */
    @XInfo(since = "1.20.4")
    @XChange(version = "1.21", from = "HIDE_POTION_EFFECTS", to = "HIDE_ADDITIONAL_TOOLTIP")
    HIDE_ADDITIONAL_TOOLTIP("HIDE_POTION_EFFECTS"),

    /**
     * Armor trim names.
     *
     * @see org.bukkit.inventory.meta.trim.ArmorTrim
     */
    HIDE_ARMOR_TRIM,

    /**
     * @see XAttribute
     * @see ItemMeta#hasAttributeModifiers()
     */
    HIDE_ATTRIBUTES,

    /**
     * Show or hide what block this item can break.
     * <p>
     * This tag is used when making adventure maps to allow a specific tool/item to break a block.
     * Apply the tag to any item/tool.
     * If the value is not a valid block or item it displays as "missingno".
     * <p>
     * Controlled by "CanDestroy" NBTTagList of NBTTagString of the block's namespaced material name.
     * {CanDestroy:["minecraft:stone","#minecraft:logs","minecraft:player_head[rotation=8]{SkullOwner:{Name:'Dinnerbone'}}"]}
     * <p>
     * Paper also provides its own API method for this from {@code ItemMeta.setCanDestroy(Set<Material> canDestroy)}.
     */
    HIDE_DESTROYS,

    /**
     * Dye names for colored leather armors.
     *
     * @see org.bukkit.inventory.meta.LeatherArmorMeta
     */
    HIDE_DYE,

    /**
     * @see XEnchantment
     * @see ItemMeta#getEnchants()
     * @see #HIDE_STORED_ENCHANTS for enchantment books.
     */
    HIDE_ENCHANTS,

    /**
     * List of blocks that this item (must be a placeable block) can be placed on.
     * <p>
     * This tag is used when making adventure maps to determine which block(s) the player can place a block on.
     * Also used on hoes to make them till dirt and on spawn eggs to place them.
     * If the value is not a valid block or item it displays as "missingno".
     * <p>
     * Controlled by "CanPlaceOn" NBTTagList of NBTTagString of the block's namespaced material name.
     * {CanPlaceOn:["minecraft:stone","#minecraft:logs","minecraft:player_head[rotation=8]{SkullOwner:{Name:'Dinnerbone'}}"]}
     * <p>
     * Paper also provides its own API method for this from {@code ItemMeta.setCanPlaceOn(Set<Material> canDestroy)}.
     */
    HIDE_PLACED_ON,

    /**
     * Setting to show/hide stored enchants on an item, such as enchantments on an enchanted book.
     * This functionality was split from {@link #HIDE_ADDITIONAL_TOOLTIP} in v1.21.3
     * <p>
     * <b>Related links:</b>
     * <ul>
     *     <li><a href="https://github.com/PaperMC/Paper/blob/c17ef64339ab27e5e50f331e53b00f6de45f7444/patches/server/0941-Fix-ItemFlags.patch#L7">ItemFlag Patch</a></li>
     *     <li><a href="https://github.com/PaperMC/Paper/blob/c17ef64339ab27e5e50f331e53b00f6de45f7444/patches/api/0457-Fix-ItemFlags.patch#L6">Backwards Compatibility Patch</a></li>
     * </ul>
     *
     * @see #HIDE_ENCHANTS for hiding actual enchants rather than the stored ones.
     * @see org.bukkit.inventory.meta.EnchantmentStorageMeta
     */
    @XInfo(since = "1.21.3")
    HIDE_STORED_ENCHANTS,

    /**
     * This is not the same as {@link XEnchantment#UNBREAKING} enchant.
     * For the blue "Unbreakable" text below attributes.
     *
     * @see ItemMeta#setUnbreakable(boolean)
     */
    HIDE_UNBREAKABLE;

    public static final XRegistry<XItemFlag, ItemFlag> REGISTRY = Data.REGISTRY;
    private static final ItemFlag[] BUKKIT_VALUES = ItemFlag.values();

    private final ItemFlag itemFlag;

    XItemFlag(String... names) {
        this.itemFlag = Data.REGISTRY.stdEnum(this, names);
    }

    private static final class Data {
        private static final XRegistry<XItemFlag, ItemFlag> REGISTRY =
                new XRegistry<>(ItemFlag.class, XItemFlag.class, XItemFlag[]::new);
    }

    public static XItemFlag of(ItemFlag itemFlag) {
        return REGISTRY.getByBukkitForm(itemFlag);
    }

    public static Optional<XItemFlag> of(String itemFlag) {
        return REGISTRY.getByName(itemFlag);
    }

    @NotNull
    @Unmodifiable
    public static Collection<XItemFlag> getValues() {
        return REGISTRY.getValues();
    }

    @Override
    public String[] getNames() {
        return new String[]{name()};
    }

    @Override
    public @Nullable ItemFlag get() {
        return itemFlag;
    }

    public void set(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(itemFlag);
        item.setItemMeta(meta);
    }

    public void set(ItemMeta meta) {
        meta.addItemFlags(itemFlag);
    }

    public void removeFrom(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        removeFrom(meta);
        item.setItemMeta(meta);
    }

    public void removeFrom(ItemMeta meta) {
        meta.removeItemFlags(itemFlag);
    }

    public Set<XItemFlag> getFlags(ItemStack item) {
        return getFlags(item.getItemMeta());
    }

    public Set<XItemFlag> getFlags(ItemMeta meta) {
        return meta.getItemFlags().stream().map(XItemFlag::of).collect(Collectors.toSet());
    }

    public boolean has(ItemStack item) {
        return has(item.getItemMeta());
    }

    public boolean has(ItemMeta meta) {
        return meta.getItemFlags().contains(itemFlag);
    }

    /**
     * Useful for decorative items.
     */
    public static void hideEverything(ItemMeta meta) {
        meta.addItemFlags(BUKKIT_VALUES);
    }
}
