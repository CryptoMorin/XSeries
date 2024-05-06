/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Crypto Morin
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
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enchantment support with multiple aliases.
 * Uses EssentialsX enchantment list for aliases.
 * Enchantment levels do not start from 0, they start from 1
 * <p>
 * EssentialsX Enchantment: https://github.com/Bukkit/Bukkit/blob/master/src/main/java/org/bukkit/enchantments/Enchantment.java
 * Enchantment: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/enchantments/Enchantment.html
 * Enchanting: https://minecraft.wiki/w/Enchanting
 *
 * @author Crypto Morin
 * @version 3.0.0
 * @see Enchantment
 */
public enum XEnchantment {
    AQUA_AFFINITY("WATER_WORKER", "WATER_WORKER", "AQUA_AFFINITY", "WATER_MINE"),
    BANE_OF_ARTHROPODS("DAMAGE_ARTHROPODS", "ARDMG", "BANE_OF_ARTHROPOD", "ARTHROPOD"),
    BINDING_CURSE("BINDING_CURSE", "BIND_CURSE", "BINDING", "BIND"),
    BLAST_PROTECTION("PROTECTION_EXPLOSIONS", "BLAST_PROTECT", "EXPLOSIONS_PROTECTION", "EXPLOSION_PROTECTION", "BLAST_PROTECTION"),
    BREACH,
    CHANNELING("CHANNELLING", "CHANELLING", "CHANELING", "CHANNEL"),
    DENSITY,
    DEPTH_STRIDER("DEPTH", "STRIDER"),
    EFFICIENCY("DIG_SPEED", "MINE_SPEED", "CUT_SPEED"),
    FEATHER_FALLING("PROTECTION_FALL", "FALL_PROT", "FEATHER_FALL", "FALL_PROTECTION", "FEATHER_FALLING"),
    FIRE_ASPECT("FIRE", "MELEE_FIRE", "MELEE_FLAME"),
    FIRE_PROTECTION("PROTECTION_FIRE", "FIRE_PROT", "FIRE_PROTECT", "FIRE_PROTECTION", "FLAME_PROTECTION", "FLAME_PROTECT", "FLAME_PROT"),
    FLAME("ARROW_FIRE", "FLAME_ARROW", "FIRE_ARROW"),
    FORTUNE("LOOT_BONUS_BLOCKS", "BLOCKS_LOOT_BONUS", "FORT"),
    FROST_WALKER("FROST", "WALKER"),
    IMPALING("IMPALE", "OCEAN_DAMAGE", "OCEAN_DMG"),
    INFINITY("ARROW_INFINITE", "INF_ARROWS", "INFINITE_ARROWS", "INFINITE", "UNLIMITED", "UNLIMITED_ARROWS"),
    KNOCKBACK("K_BACK"),
    LOOTING("LOOT_BONUS_MOBS", "MOB_LOOT", "MOBS_LOOT_BONUS"),
    LOYALTY("LOYAL", "RETURN"),
    LUCK_OF_THE_SEA("LUCK", "LUCK_OF_SEA", "LUCK_OF_SEAS", "ROD_LUCK"),
    LURE("ROD_LURE"),
    MENDING,
    MULTISHOT("TRIPLE_SHOT"),
    PIERCING,
    POWER("ARROW_DAMAGE", "ARROW_POWER"),
    PROJECTILE_PROTECTION("PROTECTION_PROJECTILE", "PROJECTILE_PROTECTION", "PROJ_PROT"),
    PROTECTION("PROTECTION_ENVIRONMENTAL", "PROTECT"),
    PUNCH("ARROW_KNOCKBACK", "ARROWKB", "ARROW_PUNCH"),
    QUICK_CHARGE("QUICKCHARGE", "QUICK_DRAW", "FAST_CHARGE", "FAST_DRAW"),
    RESPIRATION("OXYGEN", "BREATH", "BREATHING"),
    RIPTIDE("RIP", "TIDE", "LAUNCH"),
    SHARPNESS("DAMAGE_ALL", "ALL_DAMAGE", "ALL_DMG", "SHARP"),
    SILK_TOUCH("SOFT_TOUCH"),
    SMITE("DAMAGE_UNDEAD", "UNDEAD_DAMAGE"),
    SOUL_SPEED("SPEED_SOUL", "SOUL_RUNNER"),
    SWEEPING_EDGE("SWEEPING", "SWEEPING_EDGE", "SWEEP_EDGE"),
    SWIFT_SNEAK("SNEAK_SWIFT"),
    THORNS("HIGHCRIT", "THORN", "HIGHERCRIT"),
    UNBREAKING("DURABILITY", "DURA"),
    VANISHING_CURSE("VANISHING_CURSE", "VANISH_CURSE", "VANISHING", "VANISH"),
    WIND_BURST;

    /**
     * Cached list of {@link XEnchantment#values()} to avoid allocating memory for
     *
     * @since 1.0.0
     */
    public static final XEnchantment[] VALUES = values();

    /**
     * Entity types that {@link #SMITE} enchantment is effective against.
     * This set is unmodifiable.
     *
     * @since 1.2.0
     */
    public static final Set<EntityType> EFFECTIVE_SMITE_ENTITIES;
    /**
     * Entity types that {@link #BANE_OF_ARTHROPODS} enchantment is effective against.
     * This set is unmodifiable.
     *
     * @since 1.2.0
     */
    public static final Set<EntityType> EFFECTIVE_BANE_OF_ARTHROPODS_ENTITIES;

    static {
        EntityType bee = Enums.getIfPresent(EntityType.class, "BEE").orNull();
        EntityType phantom = Enums.getIfPresent(EntityType.class, "PHANTOM").orNull();
        EntityType drowned = Enums.getIfPresent(EntityType.class, "DROWNED").orNull();
        EntityType witherSkeleton = Enums.getIfPresent(EntityType.class, "WITHER_SKELETON").orNull();
        EntityType skeletonHorse = Enums.getIfPresent(EntityType.class, "SKELETON_HORSE").orNull();
        EntityType stray = Enums.getIfPresent(EntityType.class, "STRAY").orNull();
        EntityType husk = Enums.getIfPresent(EntityType.class, "HUSK").orNull();

        Set<EntityType> arthorposEffective = EnumSet.of(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH, EntityType.ENDERMITE);
        if (bee != null) arthorposEffective.add(bee);
        EFFECTIVE_BANE_OF_ARTHROPODS_ENTITIES = Collections.unmodifiableSet(arthorposEffective);

        Set<EntityType> smiteEffective = EnumSet.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.WITHER);
        if (phantom != null) smiteEffective.add(phantom);
        if (drowned != null) smiteEffective.add(drowned);
        if (witherSkeleton != null) smiteEffective.add(witherSkeleton);
        if (skeletonHorse != null) smiteEffective.add(skeletonHorse);
        if (stray != null) smiteEffective.add(stray);
        if (husk != null) smiteEffective.add(husk);
        EFFECTIVE_SMITE_ENTITIES = Collections.unmodifiableSet(smiteEffective);
    }

    @Nullable
    private final Enchantment enchantment;

    /**
     * If an enchantment has {@code self} as it means that
     * the vanilla enchantment name matches the Bukkit name.
     *
     * @see NamespacedKey#getKey()
     */
    XEnchantment(@Nonnull String... aliases) {
        Enchantment enchantment = getBukkitEnchant(this.name());

        Data.NAMES.put(this.name(), this);
        for (String legacy : aliases) {
            Data.NAMES.put(legacy, this);
            if (enchantment == null) {
                enchantment = getBukkitEnchant(legacy);
            }
        }

        this.enchantment = enchantment;
    }

    @SuppressWarnings("deprecation")
    private static Enchantment getBukkitEnchant(String name) {
        if (Data.IS_SUPER_FLAT) {
            return Registry.ENCHANTMENT.get(NamespacedKey.minecraft(name.toLowerCase(Locale.ENGLISH)));
        } else if (Data.ISFLAT) {
            return Enchantment.getByKey(NamespacedKey.minecraft(name.toLowerCase(Locale.ENGLISH)));
        } else {
            return Enchantment.getByName(name);
        }
    }

    /**
     * Checks if {@link #SMITE Smite} is effective
     * against this type of mob.
     *
     * @param type the type of the mob.
     * @return true if smite enchantment is effective against the mob, otherwise false.
     * @since 1.1.0
     */
    public static boolean isSmiteEffectiveAgainst(@Nullable EntityType type) {
        return type != null && EFFECTIVE_SMITE_ENTITIES.contains(type);
    }

    /**
     * Checks if {@link #BANE_OF_ARTHROPODS Bane of Arthropods} is effective
     * against this type of mob.
     *
     * @param type the type of the mob.
     * @return true if Bane of Arthropods enchantment is effective against the mob, otherwise false.
     * @since 1.1.0
     */
    public static boolean isArthropodsEffectiveAgainst(@Nullable EntityType type) {
        return type != null && EFFECTIVE_BANE_OF_ARTHROPODS_ENTITIES.contains(type);
    }

    /**
     * Attempts to build the string like an enum name.<br>
     * Removes all the spaces, numbers and extra non-English characters. Also removes some config/in-game based strings.
     * While this method is hard to maintain, it's extremely efficient. It's approximately more than x5 times faster than
     * the normal RegEx + String Methods approach for both formatted and unformatted material names.
     *
     * @param name the enchantment name to format.
     * @return an enum name.
     * @since 1.0.0
     */
    @Nonnull
    private static String format(@Nonnull String name) {
        int len = name.length();
        char[] chs = new char[len];
        int count = 0;
        boolean appendUnderline = false;

        for (int i = 0; i < len; i++) {
            char ch = name.charAt(i);

            if (!appendUnderline && count != 0 && (ch == '-' || ch == ' ' || ch == '_') && chs[count] != '_')
                appendUnderline = true;
            else {
                if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) {
                    if (appendUnderline) {
                        chs[count++] = '_';
                        appendUnderline = false;
                    }
                    chs[count++] = (char) (ch & 0x5f);
                }
            }
        }

        return new String(chs, 0, count);
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
        if (enchantment == null || enchantment.isEmpty())
            throw new IllegalArgumentException("Enchantment name cannot be null or empty");
        return Optional.ofNullable(Data.NAMES.get(format(enchantment)));
    }

    /**
     * Gets an enchantment from Vanilla and bukkit names.
     * There are also some aliases available.
     *
     * @param enchantment the enchantment.
     * @return an enchantment.
     * @throws IllegalArgumentException may be thrown as an unexpected exception.
     * @since 1.0.0
     */
    @Nonnull
    @SuppressWarnings("deprecation")
    public static XEnchantment matchXEnchantment(@Nonnull Enchantment enchantment) {
        Objects.requireNonNull(enchantment, "Cannot parse XEnchantment of a null enchantment");
        return Objects.requireNonNull(Data.NAMES.get(enchantment.getName()), () -> "Unsupported enchantment: " + enchantment.getName());
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

        meta.addStoredEnchant(this.enchantment, level, true);
        book.setItemMeta(meta);
        return book;
    }

    /**
     * Parse the Vanilla enchantment.
     *
     * @return a Vanilla  enchantment.
     * @since 1.0.0
     */
    @Nullable
    public Enchantment getEnchant() {
        return this.enchantment;
    }

    /**
     * Checks if this enchantment is supported and registered in the current Minecraft version.
     * <p>
     * An invocation of this method yields exactly the same result as the expression:
     * <p>
     * <blockquote>
     * {@link #getEnchant()} != null
     * </blockquote>
     *
     * @return true if the current version has this enchantment, otherwise false.
     * @since 1.0.0
     */
    public boolean isSupported() {
        return enchantment != null;
    }

    /**
     * Checks if this enchantment is supported in the current version and
     * returns itself if yes.
     * <p>
     * In the other case, the alternate enchantment will get returned,
     * no matter if it is supported or not.
     *
     * @param alternateEnchantment the enchantment to get if this one is not supported.
     * @return this enchantment or the {@code alternateEnchantment} if not supported.
     */
    @Nullable
    public XEnchantment or(@Nullable XEnchantment alternateEnchantment) {
        return isSupported() ? this : alternateEnchantment;
    }

    /**
     * In most cases you should be using {@link #name()} instead.
     *
     * @return a friendly readable string name.
     */
    @Override
    @Nonnull
    public String toString() {
        return Arrays.stream(name().split("_"))
                .map(t -> t.charAt(0) + t.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    /**
     * Used for data that need to be accessed during enum initialization.
     *
     * @since 2.0.0
     */
    private static final class Data {
        private static final boolean ISFLAT, IS_SUPER_FLAT;
        private static final Map<String, XEnchantment> NAMES = new HashMap<>();

        static {
            boolean flat, superFlat;
            try {
                Class<?> namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
                Class<?> enchantmentClass = Class.forName("org.bukkit.enchantments.Enchantment");
                enchantmentClass.getDeclaredMethod("getByKey", namespacedKeyClass);
                flat = true;
            } catch (ClassNotFoundException | NoSuchMethodException ex) {
                flat = false;
            }

            try {
                Class.forName("org.bukkit.Registry");
                superFlat = true;
            } catch (ClassNotFoundException ex) {
                superFlat = false;
            }

            ISFLAT = flat;
            IS_SUPER_FLAT = superFlat;
        }
    }
}
