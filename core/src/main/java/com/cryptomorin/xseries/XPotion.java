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
import com.cryptomorin.xseries.base.annotations.XInfo;
import com.google.common.base.Strings;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

/**
 * Potion type support for multiple aliases.
 * Uses EssentialsX potion list for aliases.
 * <p>
 * <b>Duration:</b> The duration of the effect in ticks. Values 0 or lower are treated as 1. Optional, and defaults to 1 tick.
 * <br>
 * <b>Amplifier:</b> The amplifier of the effect, with level I having value 0. Optional, and defaults to level I.
 * <p>
 * There are two types of classes that we should distinguish between each other.
 * A <b>Potion</b> is an {@link ItemStack} (with meta {@link PotionMeta}) that can have one or multiple {@link PotionEffectType}s.
 * <ul>
 *     <li>{@link PotionEffectType}: These are all the available Minecraft effects. Some of these effects cannot be gained in form of potions.</li>
 *     <li>{@link PotionType}: A potion type doesn't only contain information about a {@link PotionEffectType} but also its amplifier and duration.
 *        <ul>
 *            <li>A PotionType with enhanced amplifier (level 2 amplifier) starts with the prefix {@code STRONG} (using {@link XMaterial#GLOWSTONE} within a {@link org.bukkit.block.BrewingStand}). These potions cannot have extended duration.</li>
 *            <li>A PotionType with extended duration (base duration multiplied by {@code 2.666}) starts with the prefix {@code LONG} (using {@link XMaterial#REDSTONE} within a {@link org.bukkit.block.BrewingStand}). These potions cannot be enhanced.</li>
 *            <li>{@link PotionType#AWKWARD} A potion with no effects (using {@link XMaterial#NETHER_WART} within a {@link org.bukkit.block.BrewingStand})</li>
 *            <li>{@link PotionType#MUNDANE} Created when using {@link XMaterial#GLOWSTONE} on an {@link PotionType#AWKWARD} potion within a {@link org.bukkit.block.BrewingStand}.</li>
 *            <li>{@link PotionType#THICK} Created when using {@link XMaterial#REDSTONE} on an {@link PotionType#AWKWARD} potion within a {@link org.bukkit.block.BrewingStand}.</li>
 *            <li>{@link PotionType#TURTLE_MASTER} There is no {@link PotionEffectType} for this potion type because it's a combination of {@link PotionEffectType#SLOWNESS} and {@link PotionEffectType#RESISTANCE}, not a completely different PotionEffectType of itself.</li>
 *        </ul>
 *     </li>
 *     <li>{@link PotionEffect} is a custom type of {@link PotionType} (with custom amplifier and duration) that is not defined by Vanilla potions.</li>
 * </ul>
 * With that being said, this class is mainly a {@link PotionEffectType} mapping. However, it does provide a few utilities
 * for {@link PotionType} and maps them when possible.
 * <p>
 * EssentialsX Potions: https://github.com/EssentialsX/Essentials/blob/2.x/Essentials/src/com/earth2me/essentials/Potions.java
 * Status Effect: https://minecraft.wiki/w/Status_effect
 * Potions: https://minecraft.wiki/w/Potion
 *
 * @author Crypto Morin
 * @version 5.0.0
 * @see PotionEffect
 * @see PotionEffectType
 * @see PotionType
 */
public enum XPotion implements XBase<XPotion, PotionEffectType> {
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
    INSTANT_HEALTH("HEALTH", "INSTA_HEAL", "INSTANT_HEAL", "INSTA_HEALTH", "HEAL", /* Added because of PotionType, but it's vague */ "HEALING"),
    INVISIBILITY("INVISIBLE", "VANISH", "INVIS", "DISAPPEAR", "HIDE"),
    JUMP_BOOST("LEAP", /* Added because of PotionType.LEAPING */ "LEAPING", "JUMP"),
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
    SPEED("SPRINT", "RUNFAST", "SWIFT", /* Added because of PotionType.SWIFTNESS */ "SWIFTNESS", "FAST"),
    STRENGTH("INCREASE_DAMAGE", "BULL", "STRONG", "ATTACK"),
    TRIAL_OMEN,
    UNLUCK("UNLUCKY"),
    WATER_BREATHING("WATER_BREATH", "UNDERWATER_BREATHING", "UNDERWATER_BREATH", "AIR"),
    WEAKNESS("WEAK"),
    WEAVING,
    WIND_CHARGED,
    WITHER("DECAY"),

    @XInfo(since = "1.21.11") BREATH_OF_THE_NAUTILUS,
    ;

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
     * @deprecated Use {@link XTag#DEBUFFS} instead.
     */
    @Deprecated
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
            if (pot.potionEffectType != null)
                POTIONEFFECTTYPE_MAPPING[pot.potionEffectType.getId()] = pot;
    }

    public static final XRegistry<XPotion, PotionEffectType> REGISTRY = Data.REGISTRY;
    private final PotionEffectType potionEffectType;
    private final PotionType potionType;

    @SuppressWarnings("deprecation")
    XPotion(@NotNull String... aliases) {
        PotionEffectType tempType = PotionEffectType.getByName(this.name());
        for (String legacy : aliases) {
            if (tempType == null) tempType = PotionEffectType.getByName(legacy);
        }
        if (this.name().equals("TURTLE_MASTER")) tempType = findSlowness(); // Bukkit uses this too.
        this.potionEffectType = tempType;

        // This basically just loops all the types and tries to match them
        // against a registry.
        // There are no potions for some effects.
        this.potionType = PotionType.getByEffect(potionEffectType);
        Data.REGISTRY.stdEnum(this, aliases, potionEffectType);

        if (potionType != null) {
            String basePotionType = potionType.name();
            String strongPotionType = "STRONG_" + basePotionType;
            String longPotionType = "LONG_" + basePotionType;

            Data.POTION_TYPE_MAPPING.put(potionType, this);
            try {
                Data.POTION_TYPE_MAPPING.put(PotionType.valueOf(strongPotionType), this);
                Data.REGISTRY.registerName(strongPotionType, this);
            } catch (IllegalArgumentException ignored) {
            }
            try {
                Data.POTION_TYPE_MAPPING.put(PotionType.valueOf(longPotionType), this);
                Data.REGISTRY.registerName(longPotionType, this);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    static {
        REGISTRY.discardMetadata();
    }

    private static final class Data {
        private static final Map<PotionType, XPotion> POTION_TYPE_MAPPING = new EnumMap<>(PotionType.class);
        private static final XRegistry<XPotion, PotionEffectType> REGISTRY =
                new XRegistry<>(PotionEffectType.class, XPotion.class, () -> Registry.EFFECT, null, XPotion[]::new);
    }

    private static PotionEffectType findSlowness() {
        // This is here because it's not safe to access other
        // enum members inside the constructor.
        return Stream.of("SLOWNESS", "SLOW", "SLUGGISH")
                .map(PotionEffectType::getByName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot find slowness potion type"));
    }

    /**
     * Parses a potion effect type from the given string.
     * Supports type IDs.
     *
     * @param potion the type of the type's ID of the potion effect type.
     * @return a potion effect type.
     * @since 1.0.0
     * @deprecated Use {@link #of(String)} instead.
     */
    @NotNull
    @Deprecated
    public static Optional<XPotion> matchXPotion(@NotNull String potion) {
        return of(potion);
    }

    @NotNull
    public static XPotion of(@NotNull PotionType potion) {
        return Data.POTION_TYPE_MAPPING.get(potion);
    }

    public static Optional<XPotion> of(@NotNull String potion) {
        if (potion == null || potion.isEmpty())
            throw new IllegalArgumentException("Cannot match XPotion of a null or empty potion effect type");
        PotionEffectType idType = fromId(potion);
        if (idType != null) {
            Optional<XPotion> type = REGISTRY.getByName(idType.getName());
            if (!type.isPresent())
                throw new UnsupportedOperationException("Unsupported potion effect type ID: " + idType);
            return type;
        }

        // The getName() for some reasons returns enum-like names for some potion types
        // and Minecraft namespaced names for some others.
        return REGISTRY.getByName(potion);
    }

    /**
     * @deprecated Use {@link #of(PotionType)} instead.
     */
    @Deprecated
    public static XPotion matchXPotion(@NotNull PotionType type) {
        return of(type);
    }

    /**
     * Parses the XPotion for this potion effect.
     *
     * @param type the potion effect type.
     * @return the XPotion of this potion effect.
     * @throws IllegalArgumentException may be thrown as an unexpected exception.
     * @since 1.0.0
     * @deprecated Use {@link #of(PotionEffectType)} instead.
     */
    @NotNull
    @Deprecated
    public static XPotion matchXPotion(@NotNull PotionEffectType type) {
        return of(type);
    }

    /**
     * Parses the XPotion for this potion effect.
     *
     * @param type the potion effect type.
     * @return the XPotion of this potion effect.
     * @throws IllegalArgumentException may be thrown as an unexpected exception.
     * @since 1.0.0
     */
    @NotNull
    public static XPotion of(@NotNull PotionEffectType type) {
        Objects.requireNonNull(type, "Cannot match XPotion of a null potion effect type");
        // return POTIONEFFECTTYPE_MAPPING[type.getId()];
        return REGISTRY.getByBukkitForm(type);
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
    private static PotionEffectType fromId(@NotNull String type) {
        try {
            int id = Integer.parseInt(type);
            return PotionEffectType.getById(id);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static List<String> split(@NotNull String str, @SuppressWarnings("SameParameterValue") char separatorChar) {
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
     *     SLOWNESS, 200, 10
     *     1, 10000, 100, %50
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

        Optional<XPotion> typeOpt = of(split.get(0));
        if (!typeOpt.isPresent()) return null;
        PotionEffectType type = typeOpt.get().potionEffectType;
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
    public static void addEffects(@NotNull LivingEntity entity, @Nullable List<String> effects) {
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
    @NotNull
    public static ThrownPotion throwPotion(@NotNull LivingEntity entity, @Nullable Color color, @Nullable PotionEffect... effects) {
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
    @NotNull
    public static ItemStack buildItemWithEffects(@NotNull Material type, @Nullable Color color, @Nullable PotionEffect... effects) {
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
        return this.potionEffectType;
    }

    @Override
    public String[] getNames() {
        return new String[]{name()};
    }

    @Override
    public @Nullable PotionEffectType get() {
        return this.potionEffectType;
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
        return potionType;
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
        return potionEffectType == null ? null : new PotionEffect(potionEffectType, duration, amplifier - 1);
    }

    /**
     * In most cases you should be using {@link #name()} instead.
     *
     * @return a friendly readable string name.
     */
    @Override
    public String toString() {
        return this.friendlyName();
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
            return XPotion.of(effect.getType());
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
