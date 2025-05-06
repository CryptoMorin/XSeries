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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An ItemFlag can hide some attributes from {@link ItemStack}.
 * These are also known as {@code HideFlags} in Vanilla Minecraft.
 * <p>
 * You might find {@link #decorationOnly(ItemMeta)} useful if you're using this for GUIs.
 */
public enum XItemFlag implements XBase<XItemFlag, ItemFlag> {
    /**
     * Setting to show/hide potion effects, book and firework information, map tooltips, patterns of banners.
     *
     * @see #HIDE_STORED_ENCHANTMENTS for hiding stored enchants (like on enchanted books)
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
     * @see #HIDE_STORED_ENCHANTMENTS for enchantment books.
     * @see #HIDE_ENCHANTMENTS
     * @see #HIDE_ENCHANTMENT_GLINT_OVERRIDE
     * @see #HIDE_ENCHANTABLE
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
    @XChange(version = "1.21.5", from = "HIDE_STORED_ENCHANTS", to = "HIDE_STORED_ENCHANTMENTS")
    HIDE_STORED_ENCHANTMENTS("HIDE_STORED_ENCHANTS"),

    /**
     * This is not the same as {@link XEnchantment#UNBREAKING} enchant.
     * For the blue "Unbreakable" text below attributes.
     *
     * @see ItemMeta#setUnbreakable(boolean)
     */
    HIDE_UNBREAKABLE,

    @XInfo(since = "1.21.5")
    HIDE_CUSTOM_DATA,
    @XInfo(since = "1.21.5")
    HIDE_MAX_STACK_SIZE,
    @XInfo(since = "1.21.5")
    HIDE_MAX_DAMAGE,
    @XInfo(since = "1.21.5")
    HIDE_DAMAGE,
    @XInfo(since = "1.21.5")
    HIDE_CUSTOM_NAME,
    @XInfo(since = "1.21.5")
    HIDE_ITEM_NAME,
    @XInfo(since = "1.21.5")
    HIDE_ITEM_MODEL,
    @XInfo(since = "1.21.5")
    HIDE_LORE,
    @XInfo(since = "1.21.5")
    HIDE_RARITY,
    @XInfo(since = "1.21.5")
    HIDE_ENCHANTMENTS,
    @XInfo(since = "1.21.5")
    HIDE_CAN_PLACE_ON,
    @XInfo(since = "1.21.5")
    HIDE_CAN_BREAK,
    @XInfo(since = "1.21.5")
    HIDE_ATTRIBUTE_MODIFIERS,
    @XInfo(since = "1.21.5")
    HIDE_CUSTOM_MODEL_DATA,
    @XInfo(since = "1.21.5")
    HIDE_TOOLTIP_DISPLAY,
    @XInfo(since = "1.21.5")
    HIDE_REPAIR_COST,
    @XInfo(since = "1.21.5")
    HIDE_CREATIVE_SLOT_LOCK,
    @XInfo(since = "1.21.5")
    HIDE_ENCHANTMENT_GLINT_OVERRIDE,
    @XInfo(since = "1.21.5")
    HIDE_INTANGIBLE_PROJECTILE,
    @XInfo(since = "1.21.5")
    HIDE_FOOD,
    @XInfo(since = "1.21.5")
    HIDE_CONSUMABLE,
    @XInfo(since = "1.21.5")
    HIDE_USE_REMAINDER,
    @XInfo(since = "1.21.5")
    HIDE_USE_COOLDOWN,
    @XInfo(since = "1.21.5")
    HIDE_DAMAGE_RESISTANT,
    @XInfo(since = "1.21.5")
    HIDE_TOOL,
    @XInfo(since = "1.21.5")
    HIDE_WEAPON,
    @XInfo(since = "1.21.5")
    HIDE_ENCHANTABLE,
    @XInfo(since = "1.21.5")
    HIDE_EQUIPPABLE,
    @XInfo(since = "1.21.5")
    HIDE_REPAIRABLE,
    @XInfo(since = "1.21.5")
    HIDE_GLIDER,
    @XInfo(since = "1.21.5")
    HIDE_TOOLTIP_STYLE,
    @XInfo(since = "1.21.5")
    HIDE_DEATH_PROTECTION,
    @XInfo(since = "1.21.5")
    HIDE_BLOCKS_ATTACKS,
    @XInfo(since = "1.21.5")
    HIDE_DYED_COLOR,
    @XInfo(since = "1.21.5")
    HIDE_MAP_COLOR,
    @XInfo(since = "1.21.5")
    HIDE_MAP_ID,
    @XInfo(since = "1.21.5")
    HIDE_MAP_DECORATIONS,
    @XInfo(since = "1.21.5")
    HIDE_MAP_POST_PROCESSING,
    @XInfo(since = "1.21.5")
    HIDE_CHARGED_PROJECTILES,
    @XInfo(since = "1.21.5")
    HIDE_BUNDLE_CONTENTS,
    @XInfo(since = "1.21.5")
    HIDE_POTION_CONTENTS,
    @XInfo(since = "1.21.5")
    HIDE_POTION_DURATION_SCALE,
    @XInfo(since = "1.21.5")
    HIDE_SUSPICIOUS_STEW_EFFECTS,
    @XInfo(since = "1.21.5")
    HIDE_WRITABLE_BOOK_CONTENT,
    @XInfo(since = "1.21.5")
    HIDE_WRITTEN_BOOK_CONTENT,
    @XInfo(since = "1.21.5")
    HIDE_TRIM,
    @XInfo(since = "1.21.5")
    HIDE_DEBUG_STICK_STATE,
    @XInfo(since = "1.21.5")
    HIDE_ENTITY_DATA,
    @XInfo(since = "1.21.5")
    HIDE_BUCKET_ENTITY_DATA,
    @XInfo(since = "1.21.5")
    HIDE_BLOCK_ENTITY_DATA,
    @XInfo(since = "1.21.5")
    HIDE_INSTRUMENT,
    @XInfo(since = "1.21.5")
    HIDE_PROVIDES_TRIM_MATERIAL,
    @XInfo(since = "1.21.5")
    HIDE_OMINOUS_BOTTLE_AMPLIFIER,
    @XInfo(since = "1.21.5")
    HIDE_JUKEBOX_PLAYABLE,
    @XInfo(since = "1.21.5")
    HIDE_PROVIDES_BANNER_PATTERNS,
    @XInfo(since = "1.21.5")
    HIDE_RECIPES,
    @XInfo(since = "1.21.5")
    HIDE_LODESTONE_TRACKER,
    @XInfo(since = "1.21.5")
    HIDE_FIREWORK_EXPLOSION,
    @XInfo(since = "1.21.5")
    HIDE_FIREWORKS,
    @XInfo(since = "1.21.5")
    HIDE_PROFILE,
    @XInfo(since = "1.21.5")
    HIDE_NOTE_BLOCK_SOUND,
    @XInfo(since = "1.21.5")
    HIDE_BANNER_PATTERNS,
    @XInfo(since = "1.21.5")
    HIDE_BASE_COLOR,
    @XInfo(since = "1.21.5")
    HIDE_POT_DECORATIONS,
    @XInfo(since = "1.21.5")
    HIDE_CONTAINER,
    @XInfo(since = "1.21.5")
    HIDE_BLOCK_STATE,
    @XInfo(since = "1.21.5")
    HIDE_BEES,
    @XInfo(since = "1.21.5")
    HIDE_LOCK,
    @XInfo(since = "1.21.5")
    HIDE_CONTAINER_LOOT,
    @XInfo(since = "1.21.5")
    HIDE_BREAK_SOUND,
    @XInfo(since = "1.21.5")
    HIDE_VILLAGER_VARIANT,
    @XInfo(since = "1.21.5")
    HIDE_WOLF_VARIANT,
    @XInfo(since = "1.21.5")
    HIDE_WOLF_SOUND_VARIANT,
    @XInfo(since = "1.21.5")
    HIDE_WOLF_COLLAR,
    @XInfo(since = "1.21.5")
    HIDE_FOX_VARIANT,
    @XInfo(since = "1.21.5")
    HIDE_SALMON_SIZE,
    @XInfo(since = "1.21.5")
    HIDE_PARROT_VARIANT,
    @XInfo(since = "1.21.5")
    HIDE_TROPICAL_FISH_PATTERN,
    @XInfo(since = "1.21.5")
    HIDE_TROPICAL_FISH_BASE_COLOR,
    @XInfo(since = "1.21.5")
    HIDE_TROPICAL_FISH_PATTERN_COLOR,
    @XInfo(since = "1.21.5")
    HIDE_MOOSHROOM_VARIANT,
    @XInfo(since = "1.21.5")
    HIDE_RABBIT_VARIANT,
    @XInfo(since = "1.21.5")
    HIDE_PIG_VARIANT,
    @XInfo(since = "1.21.5")
    HIDE_COW_VARIANT,
    @XInfo(since = "1.21.5")
    HIDE_CHICKEN_VARIANT,
    @XInfo(since = "1.21.5")
    HIDE_FROG_VARIANT,
    @XInfo(since = "1.21.5")
    HIDE_HORSE_VARIANT,
    @XInfo(since = "1.21.5")
    HIDE_PAINTING_VARIANT,
    @XInfo(since = "1.21.5")
    HIDE_LLAMA_VARIANT,
    @XInfo(since = "1.21.5")
    HIDE_AXOLOTL_VARIANT,
    @XInfo(since = "1.21.5")
    HIDE_CAT_VARIANT,
    @XInfo(since = "1.21.5")
    HIDE_CAT_COLLAR,
    @XInfo(since = "1.21.5")
    HIDE_SHEEP_COLOR,
    @XInfo(since = "1.21.5")
    HIDE_SHULKER_COLOR;

    public static final XRegistry<XItemFlag, ItemFlag> REGISTRY = Data.REGISTRY;
    private static final ItemFlag[] NONE_DECORATION_FLAGS = Arrays.stream(XItemFlag.values())
            .filter(x -> x != HIDE_LORE && x != HIDE_ITEM_NAME && x != HIDE_CUSTOM_NAME)
            .filter(XBase::isSupported)
            .map(XItemFlag::get)
            .toArray(ItemFlag[]::new);

    private final ItemFlag itemFlag;

    XItemFlag(String... names) {
        this.itemFlag = Data.REGISTRY.stdEnum(this, names);
    }

    private static final class Data {
        private static final XRegistry<XItemFlag, ItemFlag> REGISTRY =
                new XRegistry<>(ItemFlag.class, XItemFlag.class, XItemFlag[]::new);
    }

    static {
        REGISTRY.discardMetadata();
    }

    @NotNull
    public static XItemFlag of(@NotNull ItemFlag itemFlag) {
        return REGISTRY.getByBukkitForm(itemFlag);
    }

    @NotNull
    public static Optional<XItemFlag> of(@NotNull String itemFlag) {
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
    @Nullable
    public ItemFlag get() {
        return itemFlag;
    }

    @Contract(mutates = "param1")
    public void set(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(itemFlag);
        item.setItemMeta(meta);
    }

    @Contract(mutates = "param1")
    public void set(@NotNull ItemMeta meta) {
        meta.addItemFlags(itemFlag);
    }

    @Contract(mutates = "param1")
    public void removeFrom(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        removeFrom(meta);
        item.setItemMeta(meta);
    }

    @Contract(mutates = "param1")
    public void removeFrom(@NotNull ItemMeta meta) {
        meta.removeItemFlags(itemFlag);
    }

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public static Set<XItemFlag> getFlags(@NotNull ItemStack item) {
        return getFlags(item.getItemMeta());
    }

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public static Set<XItemFlag> getFlags(@NotNull ItemMeta meta) {
        return meta.getItemFlags().stream().map(XItemFlag::of).collect(Collectors.toSet());
    }

    @Contract(mutates = "param1")
    public boolean has(@NotNull ItemStack item) {
        return has(item.getItemMeta());
    }

    @Contract(mutates = "param1")
    public boolean has(@NotNull ItemMeta meta) {
        return meta.getItemFlags().contains(itemFlag);
    }

    /**
     * @deprecated The name of this method is misleading as of {@code v1.21.5}. Use {@link #decorationOnly(ItemMeta)} instead.
     */
    @Deprecated
    @Contract(mutates = "param1")
    public static void hideEverything(@NotNull ItemMeta meta) {
        decorationOnly(meta);
    }

    /**
     * This is mostly meant for items inside GUIs. It hides everything
     * that would be considered redundant/metadata to show to the player.
     * For example, the item attributes, blocks it can break/place, item id, etc.
     * There are some rare cases where some of these attributes could be considered
     * useful even in a GUI, but not in most cases when used as decoration.
     *
     * @since 13.2.0
     */
    @Contract(mutates = "param1")
    public static void decorationOnly(@NotNull ItemMeta meta) {
        meta.addItemFlags(NONE_DECORATION_FLAGS);
    }
}
