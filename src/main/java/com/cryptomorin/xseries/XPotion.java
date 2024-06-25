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

import com.google.common.base.Strings;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Potion type support for multiple aliases.
 * Uses EssentialsX potion list for aliases.
 * <p>
 * Duration: The duration of the effect in ticks. Values 0 or lower are treated as 1. Optional, and defaults to 1 tick.
 * Amplifier: The amplifier of the effect, with level I having value 0. Optional, and defaults to level I.
 * <p>
 * EssentialsX Potions: https://github.com/EssentialsX/Essentials/blob/2.x/Essentials/src/com/earth2me/essentials/Potions.java
 * Status Effect: https://minecraft.wiki/w/Status_effect
 * Potions: https://minecraft.wiki/w/Potion
 *
 * @author Crypto Morin
 * @version 4.0.0
 * @see PotionEffect
 * @see PotionEffectType
 * @see PotionType
 */
public enum XPotion {
    ABSORPTION("ABSORB"),
    BAD_OMEN("OMEN_BAD", "PILLAGER"),
    BLINDNESS("BLIND"),
    CONDUIT_POWER("CONDUIT", "POWER_CONDUIT"),
    DARKNESS,
    DOLPHINS_GRACE("DOLPHIN", "GRACE"),
    FIRE_RESISTANCE("FIRE_RESIST", "RESIST_FIRE", "FIRE_RESISTANCE"),
    GLOWING("GLOW", "SHINE", "SHINY"),
    HASTE("FAST_DIGGING", "SUPER_PICK", "DIGFAST", "DIG_SPEED", "QUICK_MINE", "SHARP"),
    HEALTH_BOOST("BOOST_HEALTH", "BOOST", "HP"),
    HERO_OF_THE_VILLAGE("HERO", "VILLAGE_HERO"),
    HUNGER("STARVE", "HUNGRY"),
    INFESTED,
    INSTANT_DAMAGE("INJURE", "DAMAGE", "HARMING", "INFLICT", "HARM"),
    INSTANT_HEALTH("HEALTH", "INSTA_HEAL", "INSTANT_HEAL", "INSTA_HEALTH", "HEAL"),
    INVISIBILITY("INVISIBLE", "VANISH", "INVIS", "DISAPPEAR", "HIDE"),
    JUMP_BOOST("LEAP", "JUMP"),
    LEVITATION("LEVITATE"),
    LUCK("LUCKY"),
    MINING_FATIGUE("SLOW_DIGGING", "FATIGUE", "DULL", "DIGGING", "SLOW_DIG", "DIG_SLOW"),
    NAUSEA("CONFUSION", "SICKNESS", "SICK"),
    NIGHT_VISION("VISION", "VISION_NIGHT"),
    OOZING,
    POISON("VENOM"),
    RAID_OMEN,
    REGENERATION("REGEN"),
    RESISTANCE("DAMAGE_RESISTANCE", "ARMOR", "DMG_RESIST", "DMG_RESISTANCE"),
    SATURATION("FOOD"),
    SLOWNESS("SLOW", "SLUGGISH"),
    SLOW_FALLING("SLOW_FALL", "FALL_SLOW"),
    SPEED("SPRINT", "RUNFAST", "SWIFT", "FAST"),
    STRENGTH("INCREASE_DAMAGE", "BULL", "STRONG", "ATTACK"),
    TRIAL_OMEN,
    UNLUCK("UNLUCKY"),
    WATER_BREATHING("WATER_BREATH", "UNDERWATER_BREATHING", "UNDERWATER_BREATH", "AIR"),
    WEAKNESS("WEAK"),
    WEAVING,
    WIND_CHARGED,
    WITHER("DECAY");

    /**
     * Cached list of {@link XPotion#values()} to avoid allocating memory for
     * calling the method every time.
     *
     * @since 1.0.0
     */
    public static final XPotion[] VALUES = values();

    /**
     * An unmodifiable set of "bad" potion effects.
     *
     * @since 1.1.0
     */
    public static final Set<XPotion> DEBUFFS = Collections.unmodifiableSet(EnumSet.of(
            BAD_OMEN, BLINDNESS, NAUSEA, INSTANT_DAMAGE, HUNGER, LEVITATION, POISON,
            SLOWNESS, MINING_FATIGUE, UNLUCK, WEAKNESS, WITHER)
    );

    /**
     * Efficient mapping to get {@link XPotion} from a {@link PotionEffectType}
     * Note that <code>values.length + 1</code> is intentional as it allocates one useless space since IDs start from 1
     */
    private static final XPotion[] POTIONEFFECTTYPE_MAPPING = new XPotion[VALUES.length + 1];

    static {
        for (XPotion pot : VALUES)
            if (pot.type != null)
                POTIONEFFECTTYPE_MAPPING[pot.type.getId()] = pot;
    }

    private final PotionEffectType type;

    XPotion(@Nonnull String... aliases) {
        PotionEffectType tempType = PotionEffectType.getByName(this.name());
        Data.NAMES.put(this.name(), this);
        for (String legacy : aliases) {
            Data.NAMES.put(legacy, this);
            if (tempType == null) tempType = PotionEffectType.getByName(legacy);
        }
        this.type = tempType;
    }

    /**
     * Attempts to build the string like an enum name.<br>
     * Removes all the spaces, numbers and extra non-English characters. Also removes some config/in-game based strings.
     * While this method is hard to maintain, it's extremely efficient. It's approximately more than x5 times faster than
     * the normal RegEx + String Methods approach for both formatted and unformatted material names.
     *
     * @param name the potion effect type name to format.
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
     * Parses a potion effect type from the given string.
     * Supports type IDs.
     *
     * @param potion the type of the type's ID of the potion effect type.
     * @return a potion effect type.
     * @since 1.0.0
     */
    @Nonnull
    public static Optional<XPotion> matchXPotion(@Nonnull String potion) {
        if (potion == null || potion.isEmpty())
            throw new IllegalArgumentException("Cannot match XPotion of a null or empty potion effect type");
        PotionEffectType idType = fromId(potion);
        if (idType != null) {
            XPotion type = Data.NAMES.get(idType.getName());
            if (type == null) throw new NullPointerException("Unsupported potion effect type ID: " + idType);
            return Optional.of(type);
        }
        return Optional.ofNullable(Data.NAMES.get(format(potion)));
    }

    public static XPotion matchXPotion(@Nonnull PotionType type) {
        return matchXPotion(type.name()).orElseThrow(() -> new UnsupportedOperationException("PotionType " + type.name()));
    }

    /**
     * Parses the XPotion for this potion effect.
     *
     * @param type the potion effect type.
     * @return the XPotion of this potion effect.
     * @throws IllegalArgumentException may be thrown as an unexpected exception.
     * @since 1.0.0
     */
    @SuppressWarnings("deprecation")
    @Nonnull
    public static XPotion matchXPotion(@Nonnull PotionEffectType type) {
        Objects.requireNonNull(type, "Cannot match XPotion of a null potion effect type");
        return POTIONEFFECTTYPE_MAPPING[type.getId()];
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
    private static PotionEffectType fromId(@Nonnull String type) {
        try {
            int id = Integer.parseInt(type);
            return PotionEffectType.getById(id);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static List<String> split(@Nonnull String str, @SuppressWarnings("SameParameterValue") char separatorChar) {
        List<String> list = new ArrayList<>(5);
        boolean match = false, lastMatch = false;
        int len = str.length();
        int start = 0;

        for (int i = 0; i < len; i++) {
            if (str.charAt(i) == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }

                // This is important, it should not be i++
                start = i + 1;
                continue;
            }

            lastMatch = false;
            match = true;
        }

        if (match || lastMatch) {
            list.add(str.substring(start, len));
        }
        return list;
    }

    /**
     * Parse a {@link PotionEffect} from a string, usually from config.
     * Supports potion type IDs.
     * <br>
     * Format: <b>Potion, Duration (in seconds), Amplifier (level) [%chance]</b>
     * <pre>
     *     WEAKNESS, 30, 1
     *     SLOWNESS 200 10
     *     1, 10000, 100 %50
     * </pre>
     * The last argument can also include a chance (written in percent) which if not met, returns null.
     *
     * @param potion the potion string to parse.
     * @return a potion effect, or null if the potion type is wrong.
     * @see #buildPotionEffect(int, int)
     * @since 1.0.0
     */
    @Nullable
    public static Effect parseEffect(@Nullable String potion) {
        if (Strings.isNullOrEmpty(potion) || potion.equalsIgnoreCase("none")) return null;
        List<String> split = split(potion.replace(" ", ""), ',');
        if (split.isEmpty()) split = split(potion, ' ');

        double chance = 100;
        int chanceIndex = 0;
        if (split.size() > 2) {
            chanceIndex = split.get(2).indexOf('%');
            if (chanceIndex != -1) {
                try {
                    chance = Double.parseDouble(split.get(2).substring(chanceIndex + 1));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        Optional<XPotion> typeOpt = matchXPotion(split.get(0));
        if (!typeOpt.isPresent()) return null;
        PotionEffectType type = typeOpt.get().type;
        if (type == null) return null;

        int duration = 2400; // 20 ticks * 60 seconds * 2 minutes
        int amplifier = 0;
        if (split.size() > 1) {
            duration = toInt(split.get(1), 1) * 20;
            if (split.size() > 2)
                amplifier = toInt(chanceIndex <= 0 ? split.get(2) : split.get(2).substring(0, chanceIndex), 1) - 1;
        }

        return new Effect(new PotionEffect(type, duration, amplifier), chance);
    }

    private static int toInt(String str, @SuppressWarnings("SameParameterValue") int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * Add a list of potion effects to an entity from a string list, usually from config.
     *
     * @param entity  the entity to add potion effects to.
     * @param effects the list of potion effects to parse and add to the entity.
     * @see #parseEffect(String)
     * @since 1.0.0
     */
    public static void addEffects(@Nonnull LivingEntity entity, @Nullable List<String> effects) {
        Objects.requireNonNull(entity, "Cannot add potion effects to null entity");
        for (Effect effect : parseEffects(effects)) effect.apply(entity);
    }

    /**
     * @param effectsString a list of effects with a format following {@link #parseEffect(String)}
     * @return a list of parsed effets.
     * @since 3.0.0
     */
    public static List<Effect> parseEffects(@Nullable List<String> effectsString) {
        if (effectsString == null || effectsString.isEmpty()) return new ArrayList<>();
        List<Effect> effects = new ArrayList<>(effectsString.size());

        for (String effectStr : effectsString) {
            Effect effect = parseEffect(effectStr);
            if (effect != null) effects.add(effect);
        }

        return effects;
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
        // Why the fuck isn't Lingering potion supported?

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
        if (!canHaveEffects(type))
            throw new IllegalArgumentException("Cannot build item with " + type.name() + " potion type");

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
     * Checks if a material can have potion effects.
     * This method does not check for {@code LEGACY} materials.
     * You should avoid using them or use XMaterial instead.
     *
     * @param material the material to check.
     * @return true if the material is a potion, otherwise false.
     * @since 1.0.0
     */
    public static boolean canHaveEffects(@Nullable Material material) {
        return material != null && (material.name().endsWith("POTION") || material.name().startsWith("TIPPED_ARROW"));
    }

    /**
     * Parses the potion effect type.
     *
     * @return the parsed potion effect type.
     * @see #getPotionType()
     * @since 1.0.0
     */
    @Nullable
    public PotionEffectType getPotionEffectType() {
        return this.type;
    }

    /**
     * Checks if this potion is supported in the current Minecraft version.
     * <p>
     * An invocation of this method yields exactly the same result as the expression:
     * <p>
     * <blockquote>
     * {@link #getPotionEffectType()} != null
     * </blockquote>
     *
     * @return true if the current version has this potion effect type, otherwise false.
     * @since 1.0.0
     */
    public boolean isSupported() {
        return this.type != null;
    }

    /**
     * Checks if this potion is supported in the current version and
     * returns itself if yes.
     * <p>
     * In the other case, the alternate potion will get returned,
     * no matter if it is supported or not.
     *
     * @param alternatePotion the potion to get if this one is not supported.
     * @return this potion or the {@code alternatePotion} if not supported.
     */
    @Nullable
    public XPotion or(@Nullable XPotion alternatePotion) {
        return isSupported() ? this : alternatePotion;
    }

    /**
     * Gets the PotionType from this PotionEffectType.
     * Usually for potion items.
     *
     * @return a potion type for potions.
     * @see #getPotionEffectType()
     * @since 1.0.0
     */
    @Nullable
    public PotionType getPotionType() {
        return type == null ? null : PotionType.getByEffect(type);
    }

    /**
     * Builds a potion effect with the given duration and amplifier.
     *
     * @param duration  the duration of the potion effect in ticks.
     * @param amplifier the level of the potion effect (starting from 1).
     * @return a potion effect.
     * @see #parseEffect(String)
     * @since 1.0.0
     */
    @Nullable
    public PotionEffect buildPotionEffect(int duration, int amplifier) {
        return type == null ? null : new PotionEffect(type, duration, amplifier - 1);
    }

    /**
     * In most cases you should be using {@link #name()} instead.
     *
     * @return a friendly readable string name.
     */
    @Override
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
        private static final Map<String, XPotion> NAMES = new HashMap<>();
    }

    /**
     * For now, this merely acts as a chance wrapper for potion effects.
     *
     * @since 3.0.0
     */
    public static class Effect {
        private PotionEffect effect;
        private double chance;

        public Effect(PotionEffect effect, double chance) {
            this.effect = effect;
            this.chance = chance;
        }

        public XPotion getXPotion() {
            return XPotion.matchXPotion(effect.getType());
        }

        public double getChance() {
            return chance;
        }

        public boolean hasChance() {
            return chance >= 100 || ThreadLocalRandom.current().nextDouble(0, 100) <= chance;
        }

        public void setChance(double chance) {
            this.chance = chance;
        }

        public void apply(LivingEntity entity) {
            if (hasChance()) entity.addPotionEffect(effect);
        }

        public PotionEffect getEffect() {
            return effect;
        }

        public void setEffect(PotionEffect effect) {
            this.effect = effect;
        }
    }
}
