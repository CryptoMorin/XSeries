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

import com.cryptomorin.xseries.base.XModule;
import com.cryptomorin.xseries.base.XRegistry;
import com.cryptomorin.xseries.base.annotations.XInfo;
import com.google.common.base.Enums;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Enchantment support with multiple aliases.
 * Uses some EssentialsX enchantment list for aliases.
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
public final class XEnchantment extends XModule<XEnchantment, Enchantment> {
    private static final boolean ISFLAT, IS_SUPER_FLAT, USES_WRAPPER;

    static {
        boolean flat, superFlat, usesWrapper = false;
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

        for (Field field : Enchantment.class.getDeclaredFields()) {
            int mods = field.getModifiers();
            if (Modifier.isPublic(mods) && Modifier.isStatic(mods) && Modifier.isFinal(mods) && field.getType() == Enchantment.class) {
                try {
                    Object enchant = field.get(null);
                    // noinspection deprecation
                    if (enchant instanceof EnchantmentWrapper) {
                        usesWrapper = true;
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Cannot get enchantment field for " + field, e);
                }
            }
        }

        ISFLAT = flat;
        IS_SUPER_FLAT = superFlat;
        USES_WRAPPER = usesWrapper;
    }

    public static final XRegistry<XEnchantment, Enchantment> REGISTRY =
            new XRegistry<>(Enchantment.class, XEnchantment.class, () -> Registry.ENCHANTMENT, XEnchantment::new, XEnchantment[]::new);

    public static final XEnchantment
            AQUA_AFFINITY = std("WATER_WORKER", "WATER_WORKER", "AQUA_AFFINITY", "WATER_MINE"),
            BANE_OF_ARTHROPODS = std("BANE_OF_ARTHROPODS", "DAMAGE_ARTHROPODS", "BANE_OF_ARTHROPOD", "ARTHROPOD"),
            BINDING_CURSE = std("BINDING_CURSE", "BIND_CURSE", "BINDING", "BIND"),
            BLAST_PROTECTION = std("PROTECTION_EXPLOSIONS", "BLAST_PROTECT", "EXPLOSIONS_PROTECTION", "EXPLOSION_PROTECTION", "BLAST_PROTECTION"),
            BREACH = std("BREACH"),
            CHANNELING = std("CHANNELING", "CHANNELLING", "CHANELLING", "CHANELING", "CHANNEL"),
            DENSITY = std("DENSITY"),
            DEPTH_STRIDER = std("DEPTH_STRIDER", "DEPTH", "STRIDER"),
            EFFICIENCY = std("EFFICIENCY", "DIG_SPEED", "MINE_SPEED", "CUT_SPEED"),
            FEATHER_FALLING = std("PROTECTION_FALL", "FEATHER_FALL", "FALL_PROTECTION", "FEATHER_FALLING"),
            FIRE_ASPECT = std("FIRE_ASPECT", "FIRE", "MELEE_FIRE", "MELEE_FLAME"),
            FIRE_PROTECTION = std("PROTECTION_FIRE", "FIRE_PROT", "FIRE_PROTECT", "FIRE_PROTECTION", "FLAME_PROTECTION", "FLAME_PROTECT"),
            FLAME = std("FLAME", "ARROW_FIRE", "FLAME_ARROW", "FIRE_ARROW"),
            FORTUNE = std("FORTUNE", "LOOT_BONUS_BLOCKS", "BLOCKS_LOOT_BONUS"),
            FROST_WALKER = std("FROST_WALKER", "FROST", "WALKER"),
            IMPALING = std("IMPALING", "IMPALE", "OCEAN_DAMAGE"),
            INFINITY = std("INFINITY", "ARROW_INFINITE", "INFINITE_ARROWS", "INFINITE", "UNLIMITED_ARROWS"),
            KNOCKBACK = std("KNOCKBACK"),
            LOOTING = std("LOOTING", "LOOT_BONUS_MOBS", "MOB_LOOT", "MOBS_LOOT_BONUS"),
            LOYALTY = std("LOYALTY", "LOYAL", "RETURN"),
            LUCK_OF_THE_SEA = std("LUCK_OF_THE_SEA", "LUCK", "LUCK_OF_SEA", "LUCK_OF_SEAS", "ROD_LUCK"),
            LURE = std("LURE", "ROD_LURE"),
            MENDING = std("MENDING"),
            MULTISHOT = std("MULTISHOT", "TRIPLE_SHOT"),
            PIERCING = std("PIERCING"),
            POWER = std("POWER", "ARROW_DAMAGE", "ARROW_POWER"),
            PROJECTILE_PROTECTION = std("PROTECTION_PROJECTILE", "PROJECTILE_PROTECTION"),
            PROTECTION = std("PROTECTION", "PROTECTION_ENVIRONMENTAL", "PROTECT"),
            PUNCH = std("PUNCH", "ARROW_KNOCKBACK", "ARROW_PUNCH"),
            QUICK_CHARGE = std("QUICK_CHARGE", "QUICKCHARGE", "QUICK_DRAW", "FAST_CHARGE", "FAST_DRAW"),
            RESPIRATION = std("RESPIRATION", "OXYGEN", "BREATH", "BREATHING"),
            RIPTIDE = std("RIPTIDE", "RIP", "TIDE", "LAUNCH"),
            SHARPNESS = std("SHARPNESS", "DAMAGE_ALL", "ALL_DAMAGE", "ALL_DMG", "SHARP"),
            SILK_TOUCH = std("SILK_TOUCH", "SOFT_TOUCH"),
            SMITE = std("SMITE", "DAMAGE_UNDEAD", "UNDEAD_DAMAGE"),
            SOUL_SPEED = std("SOUL_SPEED", "SPEED_SOUL", "SOUL_RUNNER"),
            SWIFT_SNEAK = std("SWIFT_SNEAK", "SNEAK_SWIFT"),
            THORNS = std("THORNS", "HIGHCRIT", "THORN", "HIGHERCRIT"),
            UNBREAKING = std("UNBREAKING", "DURABILITY", "DURA"),
            VANISHING_CURSE = std("VANISHING_CURSE", "VANISH_CURSE", "VANISHING", "VANISH"),
            WIND_BURST = std("WIND_BURST");

    @XInfo(since = "1.11.1")
    public static final XEnchantment SWEEPING_EDGE = std("SWEEPING", "SWEEPING_EDGE", "SWEEP_EDGE");

    /**
     * Cached list of {@link XEnchantment#values()} to avoid allocating memory for
     *
     * @since 1.0.0
     * @deprecated Use {@link #REGISTRY} -> {@link XRegistry#getValues()} instead.
     */
    @Deprecated
    public static final XEnchantment[] VALUES = values();

    /**
     * Entity types that {@link #SMITE} enchantment is effective against.
     * This set is unmodifiable.
     *
     * @since 1.2.0
     * @deprecated Use {@link XTag#EFFECTIVE_SMITE_ENTITIES}
     */
    @Deprecated
    public static final Set<EntityType> EFFECTIVE_SMITE_ENTITIES;
    /**
     * Entity types that {@link #BANE_OF_ARTHROPODS} enchantment is effective against.
     * This set is unmodifiable.
     *
     * @since 1.2.0
     * @deprecated Use {@link XTag#EFFECTIVE_BANE_OF_ARTHROPODS_ENTITIES}
     */
    @Deprecated
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

    static {
        // Check std() for more info.
        // This is used because in newer versions (1.15+?) since the registry is used instead of values(),
        // it returns a list of CraftEnchantment instead of a list of EnchantmentWrapper which is used by the fields.
        if (USES_WRAPPER) {
            for (Field field : Enchantment.class.getDeclaredFields()) {
                int mods = field.getModifiers();
                if (Modifier.isPublic(mods) && Modifier.isStatic(mods) && Modifier.isFinal(mods) && field.getType() == Enchantment.class) {
                    try {
                        Object enchant = field.get(null);
                        if (enchant instanceof EnchantmentWrapper) {
                            EnchantmentWrapper wrapper = (EnchantmentWrapper) enchant;
                            XEnchantment mainMapping = REGISTRY.bukkitMapping().get(wrapper.getEnchantment());
                            Objects.requireNonNull(mainMapping, () -> "No main mapping found for Enchantment." + field.getName() + " (" + wrapper + ')');
                            REGISTRY.bukkitMapping().put(wrapper, mainMapping);
                        }
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Cannot get direct enchantment field for " + field, e);
                    }
                }
            }
        }
    }

    static {
        REGISTRY.discardMetadata();
    }

    private XEnchantment(Enchantment enchantment, String[] names) {
        super(enchantment, names);
    }

    @NotNull
    public static XEnchantment of(@NotNull Enchantment enchantment) {
        return REGISTRY.getByBukkitForm(enchantment);
    }

    public static Optional<XEnchantment> of(@NotNull String enchantment) {
        return REGISTRY.getByName(enchantment);
    }

    /**
     * @deprecated Use {@link #REGISTRY} -> {@link XRegistry#getValues()} instead.
     */
    @NotNull
    @Deprecated
    public static XEnchantment[] values() {
        return REGISTRY.values();
    }

    @SuppressWarnings("deprecation")
    @NotNull
    private static XEnchantment std(@NotNull String... names) {
        XEnchantment std = REGISTRY.std(names);
        if (USES_WRAPPER && std.isSupported()) {
            // In older versions, the Enchantment class was quite strange. It was an abstract class
            // and not an enum, and the static fields for the standard enchantments were inside a
            // "EnchantmentWrapper" class which had a getEnchantment() method. This method had the following syntax:
            //     public Enchantment getEnchantment() {
            //         return Enchantment.getById(this.getId());
            //     }
            // Which meant the ID mapping used a different enchantment instance. This instance was instead in a
            // "CraftEnchantment" class which had all the information and was registered in net.minecraft.server.Enchantment
            // class as a CraftBukkit modification. So basically EnchantmentWrapper was just a delegate for CraftEnchantment.
            // I have no idea why they did this back then...

            Enchantment enchantment = std.get();
            if (enchantment instanceof EnchantmentWrapper) {
                Enchantment wrapped = ((EnchantmentWrapper) enchantment).getEnchantment();
                REGISTRY.bukkitMapping().put(wrapped, std);
            }
        }
        return std;
    }

    /**
     * Here for study purposes.
     */
    @Deprecated
    private static Enchantment getBukkitEnchant(String name) {
        if (IS_SUPER_FLAT) {
            return Registry.ENCHANTMENT.get(NamespacedKey.minecraft(name.toLowerCase(Locale.ENGLISH)));
        } else if (ISFLAT) {
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
     * @deprecated Use {@link XTag#EFFECTIVE_SMITE_ENTITIES}
     */
    @Deprecated
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
     * @deprecated Use {@link XTag#EFFECTIVE_BANE_OF_ARTHROPODS_ENTITIES}
     */
    @Deprecated
    public static boolean isArthropodsEffectiveAgainst(@Nullable EntityType type) {
        return type != null && EFFECTIVE_BANE_OF_ARTHROPODS_ENTITIES.contains(type);
    }

    /**
     * Gets an enchantment from Vanilla and bukkit names.
     * There are also some aliases available.
     *
     * @param enchantment the name of the enchantment.
     * @return an enchantment.
     * @since 1.0.0
     * @deprecated Use {@link #of(String)} instead.
     */
    @NotNull
    @Deprecated
    public static Optional<XEnchantment> matchXEnchantment(@NotNull String enchantment) {
        if (enchantment == null || enchantment.isEmpty())
            throw new IllegalArgumentException("Enchantment name cannot be null or empty");
        return of(enchantment);
    }

    /**
     * Gets an enchantment from Vanilla and bukkit names.
     * There are also some aliases available.
     *
     * @param enchantment the enchantment.
     * @return an enchantment.
     * @throws IllegalArgumentException may be thrown as an unexpected exception.
     * @since 1.0.0
     * @deprecated Use {@link #of(Enchantment)} instead.
     */
    @NotNull
    @Deprecated
    public static XEnchantment matchXEnchantment(@NotNull Enchantment enchantment) {
        Objects.requireNonNull(enchantment, "Cannot parse XEnchantment of a null enchantment");
        return of(enchantment);
    }

    /**
     * Gets the enchanted book of this enchantment.
     *
     * @param level the level of this enchantment.
     * @return an enchanted book.
     * @since 1.0.0
     */
    @NotNull
    public ItemStack getBook(int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();

        meta.addStoredEnchant(this.get(), level, true);
        book.setItemMeta(meta);
        return book;
    }

    /**
     * Parse the Vanilla enchantment.
     *
     * @return a Vanilla  enchantment.
     * @since 1.0.0
     * @deprecated use {@link #get()} instead.
     */
    @Nullable
    @Deprecated
    public Enchantment getEnchant() {
        return get();
    }
}
