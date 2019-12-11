/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Crypto Morin
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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/* References
 *
 * * * GitHub: https://github.com/CryptoMorin/XSeries/blob/master/ZEnchantment.java
 * * XSeries: https://www.spigotmc.org/threads/378136/
 * EssentialsX Enchantment: https://github.com/Bukkit/Bukkit/blob/master/src/main/java/org/bukkit/enchantments/Enchantment.java
 * Enchantment: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/enchantments/Enchantment.html
 * Enchanting: https://minecraft.gamepedia.com/Enchanting
 */

/**
 * Up to 1.15 enchantment support with multiple aliases.
 * Uses EssentialsX enchantment list for aliases.
 * This class will not be updated.
 * <p>
 * Enchantment levels do not start from 0, they start from 1
 *
 * @author Crypto Morin
 * @version 1.0.0
 * @see Enchantment
 */
@SuppressWarnings("deprecation")
public final class ZEnchantment {
    public static final ImmutableMap<String, Enchantment> ENCHANTMENTS;
    public static final ImmutableMap<String, Enchantment> ALIASENCHANTMENTS;

    /**
     * Java Edition 1.13/Flattening Update
     * https://minecraft.gamepedia.com/Java_Edition_1.13/Flattening
     */
    private static final boolean ISFLAT;

    static {
        ImmutableMap.Builder<String, Enchantment> builder = ImmutableMap.builder();
        ImmutableMap.Builder<String, Enchantment> aliasBuilder = ImmutableMap.builder();

        builder.put("alldamage", Enchantment.DAMAGE_ALL);
        builder.put("sharpness", Enchantment.DAMAGE_ALL);
        aliasBuilder.put("alldmg", Enchantment.DAMAGE_ALL);
        aliasBuilder.put("sharp", Enchantment.DAMAGE_ALL);
        aliasBuilder.put("dal", Enchantment.DAMAGE_ALL);

        builder.put("ardmg", Enchantment.DAMAGE_ARTHROPODS);
        builder.put("baneofarthropods", Enchantment.DAMAGE_ARTHROPODS);
        aliasBuilder.put("baneofarthropod", Enchantment.DAMAGE_ARTHROPODS);
        aliasBuilder.put("arthropod", Enchantment.DAMAGE_ARTHROPODS);
        aliasBuilder.put("dar", Enchantment.DAMAGE_ARTHROPODS);

        builder.put("undeaddamage", Enchantment.DAMAGE_UNDEAD);
        builder.put("smite", Enchantment.DAMAGE_UNDEAD);
        aliasBuilder.put("du", Enchantment.DAMAGE_UNDEAD);

        builder.put("digspeed", Enchantment.DIG_SPEED);
        builder.put("efficiency", Enchantment.DIG_SPEED);
        aliasBuilder.put("minespeed", Enchantment.DIG_SPEED);
        aliasBuilder.put("cutspeed", Enchantment.DIG_SPEED);
        aliasBuilder.put("ds", Enchantment.DIG_SPEED);
        aliasBuilder.put("eff", Enchantment.DIG_SPEED);

        builder.put("durability", Enchantment.DURABILITY);
        builder.put("unbreaking", Enchantment.DURABILITY);
        aliasBuilder.put("dura", Enchantment.DURABILITY);
        aliasBuilder.put("d", Enchantment.DURABILITY);

        builder.put("thorns", Enchantment.THORNS);
        builder.put("highcrit", Enchantment.THORNS);
        aliasBuilder.put("thorn", Enchantment.THORNS);
        aliasBuilder.put("highercrit", Enchantment.THORNS);
        aliasBuilder.put("t", Enchantment.THORNS);

        builder.put("fireaspect", Enchantment.FIRE_ASPECT);
        builder.put("fire", Enchantment.FIRE_ASPECT);
        aliasBuilder.put("meleefire", Enchantment.FIRE_ASPECT);
        aliasBuilder.put("meleeflame", Enchantment.FIRE_ASPECT);
        aliasBuilder.put("fa", Enchantment.FIRE_ASPECT);

        builder.put("knockback", Enchantment.KNOCKBACK);
        aliasBuilder.put("kback", Enchantment.KNOCKBACK);
        aliasBuilder.put("kb", Enchantment.KNOCKBACK);
        aliasBuilder.put("k", Enchantment.KNOCKBACK);

        builder.put("fortune", Enchantment.LOOT_BONUS_BLOCKS);
        aliasBuilder.put("blockslootbonus", Enchantment.LOOT_BONUS_BLOCKS);
        aliasBuilder.put("fort", Enchantment.LOOT_BONUS_BLOCKS);
        aliasBuilder.put("lbb", Enchantment.LOOT_BONUS_BLOCKS);

        builder.put("mobloot", Enchantment.LOOT_BONUS_MOBS);
        builder.put("looting", Enchantment.LOOT_BONUS_MOBS);
        aliasBuilder.put("mobslootbonus", Enchantment.LOOT_BONUS_MOBS);
        aliasBuilder.put("lbm", Enchantment.LOOT_BONUS_MOBS);

        builder.put("breath", Enchantment.OXYGEN);
        builder.put("respiration", Enchantment.OXYGEN);
        aliasBuilder.put("breathing", Enchantment.OXYGEN);
        aliasBuilder.put("oxygen", Enchantment.OXYGEN);
        aliasBuilder.put("o", Enchantment.OXYGEN);

        builder.put("protection", Enchantment.PROTECTION_ENVIRONMENTAL);
        builder.put("protect", Enchantment.PROTECTION_ENVIRONMENTAL);
        aliasBuilder.put("prot", Enchantment.PROTECTION_ENVIRONMENTAL);
        aliasBuilder.put("p", Enchantment.PROTECTION_ENVIRONMENTAL);

        builder.put("blastprotect", Enchantment.PROTECTION_EXPLOSIONS);
        aliasBuilder.put("explosionsprotection", Enchantment.PROTECTION_EXPLOSIONS);
        aliasBuilder.put("explosionprotection", Enchantment.PROTECTION_EXPLOSIONS);
        aliasBuilder.put("expprot", Enchantment.PROTECTION_EXPLOSIONS);
        aliasBuilder.put("blastprotection", Enchantment.PROTECTION_EXPLOSIONS);
        aliasBuilder.put("bprotection", Enchantment.PROTECTION_EXPLOSIONS);
        aliasBuilder.put("bprotect", Enchantment.PROTECTION_EXPLOSIONS);
        aliasBuilder.put("pe", Enchantment.PROTECTION_EXPLOSIONS);

        builder.put("fallprot", Enchantment.PROTECTION_FALL);
        builder.put("featherfall", Enchantment.PROTECTION_FALL);
        aliasBuilder.put("fallprotection", Enchantment.PROTECTION_FALL);
        aliasBuilder.put("featherfalling", Enchantment.PROTECTION_FALL);
        aliasBuilder.put("pfa", Enchantment.PROTECTION_FALL);

        builder.put("fireprot", Enchantment.PROTECTION_FIRE);
        builder.put("fireprotect", Enchantment.PROTECTION_FIRE);
        aliasBuilder.put("fireprotection", Enchantment.PROTECTION_FIRE);
        aliasBuilder.put("flameprotection", Enchantment.PROTECTION_FIRE);
        aliasBuilder.put("flameprotect", Enchantment.PROTECTION_FIRE);
        aliasBuilder.put("flameprot", Enchantment.PROTECTION_FIRE);
        aliasBuilder.put("pf", Enchantment.PROTECTION_FIRE);

        builder.put("projectileprotection", Enchantment.PROTECTION_PROJECTILE);
        builder.put("projprot", Enchantment.PROTECTION_PROJECTILE);
        aliasBuilder.put("pp", Enchantment.PROTECTION_PROJECTILE);

        builder.put("silktouch", Enchantment.SILK_TOUCH);
        aliasBuilder.put("softtouch", Enchantment.SILK_TOUCH);
        aliasBuilder.put("st", Enchantment.SILK_TOUCH);

        builder.put("waterworker", Enchantment.WATER_WORKER);
        builder.put("aquaaffinity", Enchantment.WATER_WORKER);
        aliasBuilder.put("watermine", Enchantment.WATER_WORKER);
        aliasBuilder.put("ww", Enchantment.WATER_WORKER);

        aliasBuilder.put("firearrow", Enchantment.ARROW_FIRE);
        builder.put("flame", Enchantment.ARROW_FIRE);
        builder.put("flamearrow", Enchantment.ARROW_FIRE);
        aliasBuilder.put("af", Enchantment.ARROW_FIRE);

        builder.put("arrowdamage", Enchantment.ARROW_DAMAGE);
        builder.put("power", Enchantment.ARROW_DAMAGE);
        aliasBuilder.put("arrowpower", Enchantment.ARROW_DAMAGE);
        aliasBuilder.put("ad", Enchantment.ARROW_DAMAGE);

        builder.put("arrowknockback", Enchantment.ARROW_KNOCKBACK);
        aliasBuilder.put("arrowkb", Enchantment.ARROW_KNOCKBACK);
        builder.put("punch", Enchantment.ARROW_KNOCKBACK);
        aliasBuilder.put("arrowpunch", Enchantment.ARROW_KNOCKBACK);
        aliasBuilder.put("ak", Enchantment.ARROW_KNOCKBACK);

        aliasBuilder.put("infinitearrows", Enchantment.ARROW_INFINITE);
        builder.put("infarrows", Enchantment.ARROW_INFINITE);
        builder.put("infinity", Enchantment.ARROW_INFINITE);
        aliasBuilder.put("infinite", Enchantment.ARROW_INFINITE);
        aliasBuilder.put("unlimited", Enchantment.ARROW_INFINITE);
        aliasBuilder.put("unlimitedarrows", Enchantment.ARROW_INFINITE);
        aliasBuilder.put("ai", Enchantment.ARROW_INFINITE);

        builder.put("luck", Enchantment.LUCK);
        aliasBuilder.put("luckofsea", Enchantment.LUCK);
        aliasBuilder.put("luckofseas", Enchantment.LUCK);
        aliasBuilder.put("rodluck", Enchantment.LUCK);

        builder.put("lure", Enchantment.LURE);
        aliasBuilder.put("rodlure", Enchantment.LURE);

        // 1.8
        try {
            Enchantment depthStrider = Enchantment.getByName("DEPTH_STRIDER");
            if (depthStrider != null) {
                builder.put("depthstrider", depthStrider);
                aliasBuilder.put("depth", depthStrider);
                aliasBuilder.put("strider", depthStrider);
            }
        } catch (IllegalArgumentException ignored) {
        }

        // 1.9
        try {
            Enchantment frostWalker = Enchantment.getByName("FROST_WALKER");
            if (frostWalker != null) {
                builder.put("frostwalker", frostWalker);
                aliasBuilder.put("frost", frostWalker);
                aliasBuilder.put("walker", frostWalker);
            }

            Enchantment mending = Enchantment.getByName("MENDING");
            if (mending != null) builder.put("mending", mending);
        } catch (IllegalArgumentException ignored) {
        }

        // 1.11
        try {
            Enchantment bindingCurse = Enchantment.getByName("BINDING_CURSE");
            if (bindingCurse != null) {
                builder.put("bindingcurse", bindingCurse);
                aliasBuilder.put("bindcurse", bindingCurse);
                aliasBuilder.put("binding", bindingCurse);
                aliasBuilder.put("bind", bindingCurse);
            }

            Enchantment vanishingCurse = Enchantment.getByName("VANISHING_CURSE");
            if (vanishingCurse != null) {
                builder.put("vanishingcurse", vanishingCurse);
                aliasBuilder.put("vanishcurse", vanishingCurse);
                aliasBuilder.put("vanishing", vanishingCurse);
                aliasBuilder.put("vanish", vanishingCurse);
            }

            Enchantment sweeping = Enchantment.getByName("SWEEPING_EDGE");
            if (sweeping != null) {
                builder.put("sweepingedge", sweeping);
                aliasBuilder.put("sweepedge", sweeping);
                aliasBuilder.put("sweeping", sweeping);
            }
        } catch (IllegalArgumentException ignored) {
        }


        // Flattening
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

        if (ISFLAT) {
            // 1.13
            Enchantment loyalty = Enchantment.getByName("LOYALTY");
            if (loyalty != null) {
                builder.put("loyalty", loyalty);
                aliasBuilder
                        .put("loyal", loyalty)
                        .put("return", loyalty);
            }

            Enchantment impaling = Enchantment.getByName("IMPALING");
            if (impaling != null) {
                builder.put("impaling", impaling);
                aliasBuilder
                        .put("impale", impaling)
                        .put("oceandamage", impaling)
                        .put("oceandmg", impaling);
            }

            Enchantment riptide = Enchantment.getByName("RIPTIDE");
            if (riptide != null) {
                builder.put("riptide", riptide);
                aliasBuilder.put("rip", riptide);
                aliasBuilder.put("tide", riptide);
                aliasBuilder.put("launch", riptide);
            }

            Enchantment channelling = Enchantment.getByName("CHANNELING");
            if (channelling != null) {
                builder.put("channelling", channelling);
                aliasBuilder.put("chanelling", channelling);
                aliasBuilder.put("channeling", channelling);
                aliasBuilder.put("chaneling", channelling);
                aliasBuilder.put("channel", channelling);
            }

            // 1.14
            try {
                Enchantment multishot = Enchantment.getByName("MULTISHOT");
                if (multishot != null) {
                    builder.put("multishot", multishot);
                    aliasBuilder.put("tripleshot", multishot);
                }

                Enchantment quickCharge = Enchantment.getByName("QUICK_CHARGE");
                if (quickCharge != null) {
                    builder.put("quickcharge", quickCharge);
                    aliasBuilder.put("quickdraw", quickCharge);
                    aliasBuilder.put("fastcharge", quickCharge);
                    aliasBuilder.put("fastdraw", quickCharge);
                }

                Enchantment piercing = Enchantment.getByName("PIERCING");
                if (piercing != null) builder.put("piercing", piercing);
            } catch (IllegalArgumentException ignored) {
            }
        }

        ENCHANTMENTS = builder.build();
        ALIASENCHANTMENTS = aliasBuilder.build();
    }

    /**
     * Gets an enchantment from Vanilla and bukkit names.
     * There are also some aliases available.
     *
     * @param enchant the name of the enchantment.
     * @return an enchantment.
     * @since 1.0.0
     */
    @Nullable
    public static Optional<Enchantment> getByName(@Nonnull String enchant) {
        Validate.notEmpty(enchant, "Enchantment name cannot be null or empty");
        Enchantment enchantment = null;
        enchant = StringUtils.deleteWhitespace(StringUtils.lowerCase(enchant, Locale.ENGLISH));

        if (ISFLAT) enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchant)); // 1.13+ only
        if (enchantment == null) enchantment = Enchantment.getByName(enchant);
        if (enchantment == null) enchantment = ENCHANTMENTS.get(enchant);
        if (enchantment == null) enchantment = ALIASENCHANTMENTS.get(enchant);

        return Optional.ofNullable(enchantment);
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
     * You can use the {@link #getByName(String)} method in this case.
     *
     * @param item        the item to add the enchantment to.
     * @param enchantment the enchantment string containing the enchantment name and level (optional)
     * @return an enchanted {@link ItemStack}.
     * @since 1.0.0
     */
    @Nonnull
    public static ItemStack addEnchantFromString(@Nonnull ItemStack item, @Nonnull String enchantment) {
        Objects.requireNonNull(item, "Cannot add enchantment to null ItemStack");
        Validate.notEmpty(enchantment, "Cannot add null or empty enchantment to item");

        String[] split = enchantment.contains(",") ? StringUtils.deleteWhitespace(enchantment).split(",") : enchantment.replaceAll("  +", " ").split(" ");
        Optional<Enchantment> enchant = getByName(split[0]);
        if (!enchant.isPresent()) return item;

        int lvl = 1;
        if (split.length > 1) lvl = Integer.parseInt(split[1]);

        item.addUnsafeEnchantment(enchant.get(), lvl);
        return item;
    }

    @Nonnull
    public static ImmutableList<String> getEnchantments() {
        return ENCHANTMENTS.keySet().asList();
    }

    @Nonnull
    public static ImmutableList<String> getEnchantmentAliases() {
        return ALIASENCHANTMENTS.keySet().asList();
    }

    /**
     * Includes both {@link #getEnchantments()} and {@link #getEnchantmentAliases()}<br>
     * Should be saved in memory if you want to use it multiple times.
     *
     * @return concatenation of the main and the aliases lists.
     * @since 1.0.0
     */
    @Nonnull
    public static List<String> getAllEnchantmentNames() {
        return Stream.concat(ALIASENCHANTMENTS.keySet().stream(), ENCHANTMENTS.keySet().stream()).collect(Collectors.toList());
    }
}
