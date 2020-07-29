/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Crypto Morin
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

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Enchantment support with multiple aliases.
 * Uses EssentialsX enchantment list for aliases.
 * Enchantment levels do not start from 0, they start from 1
 * <p>
 * EssentialsX Enchantment: https://github.com/Bukkit/Bukkit/blob/master/src/main/java/org/bukkit/enchantments/Enchantment.java
 * Enchantment: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/enchantments/Enchantment.html
 * Enchanting: https://minecraft.gamepedia.com/Enchanting
 *
 * @author Crypto Morin
 * @version 1.1.1
 * @see Enchantment
 */
public enum XEnchantment {
    ARROW_DAMAGE("POWER", "ARROW_DAMAGE", "ARROW_POWER", "AD"),
    ARROW_FIRE("FLAME", "FLAME_ARROW", "FIRE_ARROW", "AF"),
    ARROW_INFINITE("INFINITY", "INF_ARROWS", "INFINITE_ARROWS", "INFINITE", "UNLIMITED", "UNLIMITED_ARROWS", "AI"),
    ARROW_KNOCKBACK("PUNCH", "ARROW_KNOCKBACK", "ARROWKB", "ARROW_PUNCH", "AK"),
    BINDING_CURSE(true, "BINDING_CURSE", "BIND_CURSE", "BINDING", "BIND"),
    CHANNELING(true, "CHANNELLING", "CHANELLING", "CHANELING", "CHANNEL"),
    DAMAGE_ALL("SHARPNESS", "ALL_DAMAGE", "ALL_DMG", "SHARP", "DAL"),
    DAMAGE_ARTHROPODS("BANE_OF_ARTHROPODS", "ARDMG", "BANE_OF_ARTHROPOD", "ARTHROPOD", "DAR"),
    DAMAGE_UNDEAD("SMITE", "UNDEAD_DAMAGE", "DU"),
    DEPTH_STRIDER(true, "DEPTH", "STRIDER"),
    DIG_SPEED("EFFICIENCY", "MINE_SPEED", "CUT_SPEED", "DS", "EFF"),
    DURABILITY("UNBREAKING", "DURA"),
    FIRE_ASPECT(true, "FIRE", "MELEE_FIRE", "MELEE_FLAME", "FA"),
    FROST_WALKER(true, "FROST", "WALKER"),
    IMPALING(true, "IMPALE", "OCEAN_DAMAGE", "OCEAN_DMG"),
    SOUL_SPEED(true, "SPEED_SOUL", "SOUL_RUNNER"),
    KNOCKBACK(true, "K_BACK", "KB"),
    LOOT_BONUS_BLOCKS("FORTUNE", "BLOCKS_LOOT_BONUS", "FORT", "LBB"),
    LOOT_BONUS_MOBS("LOOTING", "MOB_LOOT", "MOBS_LOOT_BONUS", "LBM"),
    LOYALTY(true, "LOYAL", "RETURN"),
    LUCK("LUCK_OF_THE_SEA", "LUCK_OF_SEA", "LUCK_OF_SEAS", "ROD_LUCK"),
    LURE(true, "ROD_LURE"),
    MENDING(true),
    MULTISHOT(true, "TRIPLE_SHOT"),
    OXYGEN("RESPIRATION", "BREATH", "BREATHING", "O2", "O"),
    PIERCING(true),
    PROTECTION_ENVIRONMENTAL("PROTECTION", "PROTECT", "PROT"),
    PROTECTION_EXPLOSIONS("BLAST_PROTECTION", "BLAST_PROTECT", "EXPLOSIONS_PROTECTION", "EXPLOSION_PROTECTION", "BLAST_PROTECTION", "PE"),
    PROTECTION_FALL("FEATHER_FALLING", "FALL_PROT", "FEATHER_FALL", "FALL_PROTECTION", "FEATHER_FALLING", "PFA"),
    PROTECTION_FIRE("FIRE_PROTECTION", "FIRE_PROT", "FIRE_PROTECT", "FIRE_PROTECTION", "FLAME_PROTECTION", "FLAME_PROTECT", "FLAME_PROT", "PF"),
    PROTECTION_PROJECTILE("PROJECTILE_PROTECTION", "PROJECTILE_PROTECTION", "PROJ_PROT", "PP"),
    QUICK_CHARGE("QUICKCHARGE", "QUICK_DRAW", "FAST_CHARGE", "FAST_DRAW"),
    RIPTIDE(true, "RIP", "TIDE", "LAUNCH"),
    SILK_TOUCH(true, "SOFT_TOUCH", "ST"),
    SWEEPING_EDGE("SWEEPING", "SWEEPING_EDGE", "SWEEP_EDGE"),
    THORNS(true, "HIGHCRIT", "THORN", "HIGHERCRIT", "T"),
    VANISHING_CURSE(true, "VANISHING_CURSE", "VANISH_CURSE", "VANISHING", "VANISH"),
    WATER_WORKER("AQUA_AFFINITY", "WATER_WORKER", "AQUA_AFFINITY", "WATER_MINE", "WW");

    /**
     * An immutable cached list of {@link XEnchantment#values()} to avoid allocating memory for
     * calling the method every time.
     *
     * @since 1.0.0
     */
    public static final EnumSet<XEnchantment> VALUES = EnumSet.allOf(XEnchantment.class);

    /**
     * Java Edition 1.13/Flattening Update
     * https://minecraft.gamepedia.com/Java_Edition_1.13/Flattening
     */
    private static final boolean ISFLAT;
    private static final Pattern FORMAT_PATTERN = Pattern.compile("\\d+|\\W+");

    static {
        boolean flat;
        try {
            Class<?> namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
            Class<?> enchantmentClass = Class.forName("org.bukkit.enchantments.Enchantment");
            enchantmentClass.getDeclaredMethod("getByKey", namespacedKeyClass);
            flat = true;
        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            flat = false;
        }
        ISFLAT = flat;
    }

    /**
     * If an enchantment has {@code self} as true, it means that
     * the vanilla enchantment name matches the Bukkit name.
     *
     * @see NamespacedKey#getKey()
     */
    private final boolean self;
    private final String[] aliases;

    XEnchantment(String... names) {
        this(false, names);
    }

    XEnchantment(boolean self, String... aliases) {
        this.self = self;
        this.aliases = aliases;
    }

    /**
     * Checks if {@link #DAMAGE_UNDEAD Smite} is effective
     * against this type of mob.
     *
     * @param type the type of the mob.
     * @return true if smite enchantment is effective against the mob, otherwise false.
     * @since 1.1.0
     */
    public static boolean isSmiteEffectiveAgainst(EntityType type) {
        return Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.WITHER, EntityType.WITHER_SKELETON,
                EntityType.SKELETON_HORSE, EntityType.STRAY, EntityType.HUSK, EntityType.PHANTOM, EntityType.DROWNED).contains(type);
    }

    /**
     * Checks if {@link #DAMAGE_ARTHROPODS Bane of Arthropods} is effective
     * against this type of mob.
     *
     * @param type the type of the mob.
     * @return true if Bane of Arthropods enchantment is effective against the mob, otherwise false.
     * @since 1.1.0
     */
    public static boolean isArthropodsEffectiveAgainst(EntityType type) {
        if (Arrays.asList(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH, EntityType.ENDERMITE).contains(type)) return true;
        else if (Enums.getIfPresent(EntityType.class, "BEE").isPresent()) return type == EntityType.BEE;
        return false;
    }

    /**
     * Attempts to build the string like an enum name.
     * Removes all the spaces, numbers and extra non-English characters. Also removes some config/in-game based strings.
     *
     * @param name the material name to modify.
     * @return a Material enum name.
     * @since 1.0.0
     */
    @Nonnull
    private static String format(@Nonnull String name) {
        return FORMAT_PATTERN.matcher(
                name.trim().replace('-', '_').replace(' ', '_')).replaceAll("").toUpperCase(Locale.ENGLISH);
    }

    /**
     * Gets an enchantment from Vanilla and bukkit names.
     * There are also some aliases available.
     *
     * @param enchantment the name of the enchantment.
     * @return an enchantment.
     * @since 1.0.0
     */
    @Nonnull
    public static Optional<XEnchantment> matchXEnchantment(@Nonnull String enchantment) {
        Validate.notEmpty(enchantment, "Enchantment name cannot be null or empty");
        enchantment = format(enchantment);

        for (XEnchantment value : VALUES)
            if (value.name().equals(enchantment) || value.anyMatchAliases(enchantment)) return Optional.of(value);
        return Optional.empty();
    }

    /**
     * Gets an enchantment from Vanilla and bukkit names.
     * There are also some aliases available.
     *
     * @param enchantment the enchantment.
     * @return an enchantment.
     * @throws IllegalArgumentException may be thrown as an unexpeceted exception.
     * @since 1.0.0
     */
    @Nonnull
    @SuppressWarnings("deprecation")
    public static XEnchantment matchXEnchantment(@Nonnull Enchantment enchantment) {
        Objects.requireNonNull(enchantment, "Cannot parse XEnchantment of a null enchantment");
        try {
            return valueOf(enchantment.getName());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported Enchantment: " + enchantment.getName(), ex.getCause());
        }
    }

    /**
     * Adds an unsafe enchantment to the given item from a string.
     * <p>
     * <blockquote><pre>
     *    ItemStack item = ...;
     *    addEnchantFromString(item, "unbreaking, 10");
     *    addEnchantFromString(item, "mending");
     * </pre></blockquote>
     * <p>
     * Note that if you set your item's meta {@link ItemStack#setItemMeta(ItemMeta)} the enchantment
     * will be removed.
     * You need to use {@link ItemMeta#addEnchant(Enchantment, int, boolean)} instead.
     * You can use the {@link #matchXEnchantment(String)} method in this case.
     *
     * @param item        the item to add the enchantment to.
     * @param enchantment the enchantment string containing the enchantment name and level (optional)
     * @return an enchanted {@link ItemStack} or the item itself without enchantment added if enchantment type is null.
     * @see #matchXEnchantment(String)
     * @since 1.0.0
     */
    @Nonnull
    public static ItemStack addEnchantFromString(ItemStack item, String enchantment) {
        Objects.requireNonNull(item, "Cannot add enchantment to null ItemStack");
        if (Strings.isNullOrEmpty(enchantment) || enchantment.equalsIgnoreCase("none")) return item;

        String[] split = StringUtils.split(StringUtils.deleteWhitespace(enchantment), ',');
        if (split.length == 0) split = StringUtils.split(enchantment, ' ');

        Optional<XEnchantment> enchantOpt = matchXEnchantment(split[0]);
        if (enchantOpt.isPresent()) return item;
        Enchantment enchant = enchantOpt.get().parseEnchantment();
        if (enchant == null) return null;

        int lvl = 1;
        try {
            if (split.length > 1) lvl = Integer.parseInt(split[1]);
        } catch (NumberFormatException ignored) {
        }

        item.addUnsafeEnchantment(enchant, lvl);
        return item;
    }

    /**
     * Gets the enchanted book of this enchantment.
     *
     * @param level the level of this enchantment.
     * @return an enchanted book.
     * @since 1.0.0
     */
    @Nonnull
    public ItemStack getBook(int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();

        meta.addStoredEnchant(this.parseEnchantment(), level, true);
        book.setItemMeta(meta);
        return book;
    }

    /**
     * Checks if the formatted enchantment name matches any of the aliases.
     *
     * @param enchantment the formatted enchant name.
     * @return true if the aliases conntain the enchantment name, otherwise false.
     * @since 1.0.0
     */
    private boolean anyMatchAliases(String enchantment) {
        for (String alias : aliases)
            if (enchantment.equals(alias) || enchantment.equals(StringUtils.remove(alias, '_'))) return true;
        return false;
    }

    /**
     * Gets the Minecraft Vanilla name of this enchantment.
     *
     * @return the Minecraft Vanilla name.
     * @see Enchantment#getByKey(NamespacedKey)
     * @since 1.0.0
     */
    @Nonnull
    public String getVanillaName() {
        return this.self ? this.name() : this.aliases[0];
    }

    /**
     * Parse the Vanilla enchantment.
     *
     * @return a Vanilla  enchantment.
     * @since 1.0.0
     */
    @Nullable
    @SuppressWarnings("deprecation")
    public Enchantment parseEnchantment() {
        return ISFLAT ? Enchantment.getByKey(NamespacedKey.minecraft(this.getVanillaName().toLowerCase(Locale.ENGLISH)))
                : Enchantment.getByName(this.name());
    }

    /**
     * Checks if this enchantment is supported and registered in the current Minecraft version.
     * <p>
     * An invocation of this method yields exactly the same result as the expression:
     * <p>
     * <blockquote>
     * {@link #parseEnchantment()} != null
     * </blockquote>
     *
     * @return true if the current version has this enchantment, otherwise false.
     * @since 1.0.0
     */
    public boolean isSupported() {
        return parseEnchantment() != null;
    }

    @Nonnull
    public String[] getAliases() {
        return aliases;
    }

    /**
     * In most cases your should be using {@link #name()} instead.
     *
     * @return a friendly readable string name.
     */
    @Override
    public String toString() {
        return WordUtils.capitalize(this.name().replace('_', ' ').toLowerCase(Locale.ENGLISH));
    }
}
