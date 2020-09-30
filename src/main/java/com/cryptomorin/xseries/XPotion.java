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

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Potion type support for multiple aliases.
 * Uses EssentialsX potion list for aliases.
 * <p>
 * Duration: The duration of the effect in ticks. Values 0 or lower are treated as 1. Optional, and defaults to 1 tick.
 * Amplifier: The amplifier of the effect, with level I having value 0. Optional, and defaults to level I.
 * <p>
 * EssentialsX Potions: https://github.com/EssentialsX/Essentials/blob/2.x/Essentials/src/com/earth2me/essentials/Potions.java
 * Status Effect: https://minecraft.gamepedia.com/Status_effect
 * Potions: https://minecraft.gamepedia.com/Potion
 *
 * @author Crypto Morin
 * @version 2.0.0
 * @see PotionEffect
 * @see PotionEffectType
 * @see PotionType
 */
public enum XPotion {
    ABSORPTION("ABSORB"),
    BAD_OMEN("OMEN_BAD", "PILLAGER"),
    BLINDNESS("BLIND"),
    CONDUIT_POWER("CONDUIT", "POWER_CONDUIT"),
    CONFUSION("NAUSEA", "SICKNESS", "SICK"),
    DAMAGE_RESISTANCE("RESISTANCE", "ARMOR", "DMG_RESIST", "DMG_RESISTANCE"),
    DOLPHINS_GRACE("DOLPHIN", "GRACE"),
    FAST_DIGGING("HASTE", "SUPER_PICK", "DIGFAST", "DIG_SPEED", "QUICK_MINE", "SHARP"),
    FIRE_RESISTANCE("FIRE_RESIST", "RESIST_FIRE", "FIRE_RESISTANCE"),
    GLOWING("GLOW", "SHINE", "SHINY"),
    HARM("INJURE", "DAMAGE", "HARMING", "INFLICT"),
    HEAL("HEALTH", "INSTA_HEAL", "INSTANT_HEAL", "INSTA_HEALTH", "INSTANT_HEALTH"),
    HEALTH_BOOST("BOOST_HEALTH", "BOOST", "HP"),
    HERO_OF_THE_VILLAGE("HERO", "VILLAGE_HERO"),
    HUNGER("STARVE", "HUNGRY"),
    INCREASE_DAMAGE("STRENGTH", "BULL", "STRONG", "ATTACK"),
    INVISIBILITY("INVISIBLE", "VANISH", "INVIS", "DISAPPEAR", "HIDE"),
    JUMP("LEAP", "JUMP_BOOST"),
    LEVITATION("LEVITATE"),
    LUCK("LUCKY"),
    NIGHT_VISION("VISION", "VISION_NIGHT"),
    POISON("VENOM"),
    REGENERATION("REGEN"),
    SATURATION("FOOD"),
    SLOW("SLOWNESS", "SLUGGISH"),
    SLOW_DIGGING("FATIGUE", "DULL", "DIGGING", "SLOW_DIG", "DIG_SLOW"),
    SLOW_FALLING("SLOW_FALL", "FALL_SLOW"),
    SPEED("SPRINT", "RUNFAST", "SWIFT", "FAST"),
    UNLUCK("UNLUCKY"),
    WATER_BREATHING("WATER_BREATH", "UNDERWATER_BREATHING", "UNDERWATER_BREATH", "AIR"),
    WEAKNESS("WEAK"),
    WITHER("DECAY");

    /**
     * Cached list of {@link XPotion#values()} to avoid allocating memory for
     * calling the method every time.
     * This list is unmodifiable.
     *
     * @since 1.0.0
     */
    public static final List<XPotion> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
    /**
     * An unmodifiable set of "bad" potion effects.
     *
     * @since 1.1.0
     */
    public static final Set<XPotion> DEBUFFS = Collections.unmodifiableSet(EnumSet.of(
            BAD_OMEN, BLINDNESS, CONFUSION, HARM, HUNGER, LEVITATION, POISON, SATURATION,
            SLOW, SLOW_DIGGING, SLOW_FALLING, UNLUCK, WEAKNESS, WITHER));

    private static final Pattern FORMAT_PATTERN = Pattern.compile("\\d+|\\W+");
    private final PotionEffectType type;

    XPotion(@Nonnull String... aliases) {
        this.type = PotionEffectType.getByName(this.name());
        Data.NAMES.put(this.name(), this);
        for (String legacy : aliases) Data.NAMES.put(legacy, this);
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
     * Parses a potion effect type from the given string.
     * Supports type IDs.
     *
     * @param potion the type of the type's ID of the potion effect type.
     * @return a potion effect type.
     * @since 1.0.0
     */
    @Nonnull
    public static Optional<XPotion> matchXPotion(@Nonnull String potion) {
        Validate.notEmpty(potion, "Cannot match XPotion of a null or empty potion effect type");
        PotionEffectType idType = getIdFromString(potion);
        if (idType != null) {
            XPotion type = Data.NAMES.get(idType.getName());
            if (type == null) throw new NullPointerException("Unsupported potion effect type ID: " + idType);
            return Optional.of(type);
        }
        return Optional.ofNullable(Data.NAMES.get(format(potion)));
    }

    /**
     * Parses the XPotion for this potion effect.
     *
     * @param type the potion effect type.
     * @return the XPotion of this potion effect.
     * @throws IllegalArgumentException may be thrown as an unexpected exception.
     * @since 1.0.0
     */
    @Nonnull
    public static XPotion matchXPotion(@Nonnull PotionEffectType type) {
        Objects.requireNonNull(type, "Cannot match XPotion of a null potion effect type");
        try {
            return valueOf(type.getName());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported PotionEffectType: " + type.getName(), ex.getCause());
        }
    }

    /**
     * Parses the type ID if available.
     *
     * @param type the ID of the potion effect type.
     * @return a potion effect type from the ID, or null if it's not an ID or the effect is not found.
     * @since 1.0.0
     */
    @Nullable
    @SuppressWarnings("deprecation")
    private static PotionEffectType getIdFromString(@Nonnull String type) {
        try {
            int id = Integer.parseInt(type);
            return PotionEffectType.getById(id);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Parse a {@link PotionEffect} from a string, usually from config.
     * Supports potion type IDs.
     * <pre>
     *     WEAKNESS, 30, 1
     *     SLOWNESS 200 10
     *     1, 10000, 100
     * </pre>
     *
     * @param potion the potion string to parse.
     * @return a potion effect, or null if the potion type is wrong.
     * @see #parsePotion(int, int)
     * @since 1.0.0
     */
    @Nullable
    public static PotionEffect parsePotionEffectFromString(@Nullable String potion) {
        if (Strings.isNullOrEmpty(potion) || potion.equalsIgnoreCase("none")) return null;
        String[] split = StringUtils.split(StringUtils.deleteWhitespace(potion), ',');
        if (split.length == 0) split = StringUtils.split(potion, ' ');

        Optional<XPotion> typeOpt = matchXPotion(split[0]);
        if (!typeOpt.isPresent()) return null;
        PotionEffectType type = typeOpt.get().parsePotionEffectType();
        if (type == null) return null;

        int duration = 2400; // 20 ticks * 60 seconds * 2 minutes
        int amplifier = 0;
        if (split.length > 1) {
            duration = NumberUtils.toInt(split[1]) * 20;
            if (split.length > 2) amplifier = NumberUtils.toInt(split[2]) - 1;
        }

        return new PotionEffect(type, duration, amplifier);
    }

    /**
     * Add a list of potion effects to a player from a string list, usually from config.
     *
     * @param player  the player to add potion effects to.
     * @param effects the list of potion effects to parse and add to the player.
     * @see #parsePotionEffectFromString(String)
     * @since 1.0.0
     */
    public static void addPotionEffectsFromString(@Nonnull Player player, @Nonnull List<String> effects) {
        if (effects == null || effects.isEmpty()) return;
        Objects.requireNonNull(player, "Cannot add potion effects to null player");

        for (String effect : effects) {
            PotionEffect potionEffect = parsePotionEffectFromString(effect);
            if (potionEffect != null) player.addPotionEffect(potionEffect);
        }
    }

    /**
     * Throws a splash potion from the target entity.
     * This method is only compatible for 1.9+
     *
     * @param entity  the entity to throw the potion from.
     * @param color   the color of the potion's bottle.
     * @param effects the effects of the potion.
     * @return a thrown splash potion.
     * @since 1.0.0
     */
    @Nonnull
    public static ThrownPotion throwPotion(@Nonnull LivingEntity entity, @Nullable Color color, @Nullable PotionEffect... effects) {
        Objects.requireNonNull(entity, "Cannot throw potion from null entity");
        @SuppressWarnings("deprecation")
        ItemStack potion = Material.getMaterial("SPLASH_POTION") == null ?
                new ItemStack(Material.POTION, 1, (short) 16398) : // or 16384?
                new ItemStack(Material.SPLASH_POTION);
        // TODO check why the fuck Lingering potion isn't supported.

        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setColor(color);
        if (effects != null) for (PotionEffect effect : effects) meta.addCustomEffect(effect, true);
        potion.setItemMeta(meta);

        ThrownPotion thrownPotion = entity.launchProjectile(ThrownPotion.class);
        thrownPotion.setItem(potion);
        return thrownPotion;
    }

    /**
     * Builds an item with the given type, color and effects.
     * This method is only compatible for 1.9+
     * <p>
     * The item type must be one of the following:
     * <pre>
     *     {@link Material#POTION}
     *     {@link Material#SPLASH_POTION}
     *     {@link Material#LINGERING_POTION}
     *     {@link Material#TIPPED_ARROW}
     * </pre>
     *
     * @param type    the type of the potion.
     * @param color   the color of the potion's bottle.
     * @param effects the effects of the potion.
     * @return an item with the specified effects.
     * @since 1.0.0
     */
    @Nonnull
    public static ItemStack buildItemWithEffects(@Nonnull Material type, @Nullable Color color, @Nullable PotionEffect... effects) {
        Objects.requireNonNull(type, "Cannot build an effected item with null type");
        Validate.isTrue(canHaveEffects(type), "Cannot build item with " + type.name() + " potion type");

        ItemStack item = new ItemStack(type);
        PotionMeta meta = (PotionMeta) item.getItemMeta();

        meta.setColor(color);
        meta.setDisplayName(type == Material.POTION ? "Potion" : type == Material.SPLASH_POTION ? "Splash Potion" :
                type == Material.TIPPED_ARROW ? "Tipped Arrow" : "Lingering Potion");
        if (effects != null) for (PotionEffect effect : effects) meta.addCustomEffect(effect, true);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Checks if a material is a potion.
     * This method does not check for {@code LEGACY} materials.
     * You should avoid using them or use XMaterial instead.
     *
     * @param material the material to check.
     * @return true if the material is a potion, otherwise false.
     * @since 1.0.0
     */
    public static boolean canHaveEffects(@Nullable Material material) {
        return material != null && (material.name().endsWith("POTION") || material.name().startsWith("TI")); // TIPPED_ARROW
    }

    /**
     * Parses the potion effect type.
     *
     * @return the parsed potion effect type.
     * @see #getPotionType()
     * @since 1.0.0
     */
    @Nullable
    public PotionEffectType parsePotionEffectType() {
        return this.type;
    }

    /**
     * Checks if this potion is supported in the current Minecraft version.
     * <p>
     * An invocation of this method yields exactly the same result as the expression:
     * <p>
     * <blockquote>
     * {@link #parsePotionEffectType()} != null
     * </blockquote>
     *
     * @return true if the current version has this potion effect type, otherwise false.
     * @since 1.0.0
     */
    public boolean isSupported() {
        return this.parsePotionEffectType() != null;
    }

    /**
     * Gets the PotionType from this PotionEffectType.
     * Usually for potion items.
     *
     * @return a potion type for potions.
     * @see #parsePotionEffectType()
     * @since 1.0.0
     * @deprecated not for removal. Use {@link PotionEffectType} instead.
     */
    @Nullable
    @Deprecated
    public PotionType getPotionType() {
        PotionEffectType type = this.parsePotionEffectType();
        return type == null ? null : PotionType.getByEffect(type);
    }

    /**
     * Builds a potion effect with the given duration and amplifier.
     *
     * @param duration  the duration of the potion effect.
     * @param amplifier the amplifier of the potion effect.
     * @return a potion effect.
     * @see #parsePotionEffectFromString(String)
     * @since 1.0.0
     */
    @Nullable
    public PotionEffect parsePotion(int duration, int amplifier) {
        PotionEffectType type = this.parsePotionEffectType();
        return type == null ? null : new PotionEffect(type, duration, amplifier);
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

    /**
     * Used for datas that need to be accessed during enum initilization.
     *
     * @since 2.0.0
     */
    private static final class Data {
        private static final Map<String, XPotion> NAMES = new HashMap<>();
    }
}
