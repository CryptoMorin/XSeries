package com.cryptomorin.xseries.particles;

import org.bukkit.Particle;

import java.util.*;

/**
 * <b>XParticle</b> - Particle enum for <b>XSeries</b>
 * <p>
 * This class is mainly used to support {@link Particle}, especially for the "parity change" by
 * Spigot in 1.20.5 (see <a href="https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/diff/src/main/java/org/bukkit/Particle.java?until=8a34e009148cc297bcc9eb5c250fc4f5b071c4a7">...</a>).
 */
@SuppressWarnings("UnstableApiUsage")
public enum XParticle {
    /**
     * EXPLOSION_NORMAL -> POOF (v1.20.5)
     */
    POOF("EXPLOSION_NORMAL"),
    /**
     * EXPLOSION_LARGE -> EXPLOSION (v1.20.5)
     */
    EXPLOSION("EXPLOSION_LARGE"),
    /**
     * EXPLOSION_HUGE -> EXPLOSION_EMITTER (v1.20.5)
     */
    EXPLOSION_EMITTER("EXPLOSION_HUGE"),
    /**
     * FIREWORKS_SPARK -> FIREWORK (v1.20.5)
     */
    FIREWORK("FIREWORKS_SPARK"),
    /**
     * WATER_BUBBLE -> BUBBLE (v1.20.5)
     */
    BUBBLE("WATER_BUBBLE"),
    /**
     * WATER_SPLASH -> SPLASH (v1.20.5)
     */
    SPLASH("WATER_SPLASH"),
    /**
     * WATER_WAKE -> FISHING (v1.20.5)
     */
    FISHING("WATER_WAKE"),
    /**
     * SUSPENDED -> UNDERWATER (v1.20.5)
     */
    UNDERWATER("SUSPENDED"),
    CRIT,
    /**
     * CRIT_MAGIC -> ENCHANTED_HIT (v1.20.5)
     */
    ENCHANTED_HIT("CRIT_MAGIC"),
    /**
     * SMOKE_NORMAL -> SMOKE (v1.20.5)
     */
    SMOKE("SMOKE_NORMAL"),
    /**
     * SMOKE_LARGE -> LARGE_SMOKE (v1.20.5)
     */
    LARGE_SMOKE("SMOKE_LARGE"),
    /**
     * SPELL -> EFFECT (v1.20.5)
     */
    EFFECT("SPELL"),
    /**
     * SPELL_INSTANT -> INSTANT_EFFECT (v1.20.5)
     */
    INSTANT_EFFECT("SPELL_INSTANT"),
    /**
     * SPELL_MOB_AMBIENT -> SPELL_MOB -> ENTITY_EFFECT (v1.20.5)
     * The name was changed multiple times during the parity update
     *
     * @see <a href="https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/diff/src/main/java/org/bukkit/Particle.java?until=1113e50a392b36253c4ae458a6d3d73e04841111">...</a>
     */
    ENTITY_EFFECT("SPELL_MOB", "SPELL_MOB_AMBIENT"),
    /**
     * SPELL_WITCH -> WITCH (v1.20.5)
     */
    WITCH("SPELL_WITCH"),
    /**
     * DRIP_WATER -> DRIPPING_WATER (v1.20.5)
     */
    DRIPPING_WATER("DRIP_WATER"),
    /**
     * DRIP_LAVA -> DRIPPING_LAVA (v1.20.5)
     */
    DRIPPING_LAVA("DRIP_LAVA"),
    /**
     * VILLAGER_ANGRY -> ANGRY_VILLAGER (v1.20.5)
     */
    ANGRY_VILLAGER("VILLAGER_ANGRY"),
    /**
     * VILLAGER_HAPPY -> HAPPY_VILLAGER (v1.20.5)
     */
    HAPPY_VILLAGER("VILLAGER_HAPPY"),
    /**
     * TOWN_AURA -> MYCELIUM (v1.20.5)
     */
    MYCELIUM("TOWN_AURA"),
    NOTE,
    PORTAL,
    /**
     * ENCHANTMENT_TABLE -> ENCHANT (v1.20.5)
     */
    ENCHANT("ENCHANTMENT_TABLE"),
    FLAME,
    LAVA,
    CLOUD,
    /**
     * REDSTONE -> DUST (v1.20.5)
     */
    DUST("REDSTONE"),
    /**
     * SNOWBALL, SNOW_SHOVEL -> ITEM_SNOWBALL (v1.20.5)
     */
    ITEM_SNOWBALL("SNOWBALL", "SNOW_SHOVEL"),
    /**
     * SLIME -> ITEM_SLIME (v1.20.5)
     */
    ITEM_SLIME("SLIME"),
    HEART,
    /**
     * ITEM_CRACK -> ITEM (v1.20.5)
     */
    ITEM("ITEM_CRACK"),
    /**
     * BLOCK_CRACK, BLOCK_DUST -> BLOCK (v1.20.5)
     */
    BLOCK("BLOCK_CRACK", "BLOCK_DUST"),
    /**
     * WATER_DROP -> RAIN (v1.20.5)
     */
    RAIN("WATER_DROP"),
    /**
     * MOB_APPEARANCE -> ELDER_GUARDIAN (v1.20.5)
     */
    ELDER_GUARDIAN("MOB_APPEARANCE"),
    DRAGON_BREATH,
    END_ROD,
    DAMAGE_INDICATOR,
    SWEEP_ATTACK,
    FALLING_DUST,
    /**
     * TOTEM -> TOTEM_OF_UNDYING (v1.20.5)
     */
    TOTEM_OF_UNDYING("TOTEM"),
    SPIT,
    SQUID_INK,
    BUBBLE_POP,
    CURRENT_DOWN,
    BUBBLE_COLUMN_UP,
    NAUTILUS,
    DOLPHIN,
    SNEEZE,
    CAMPFIRE_COSY_SMOKE,
    CAMPFIRE_SIGNAL_SMOKE,
    COMPOSTER,
    FLASH,
    FALLING_LAVA,
    LANDING_LAVA,
    FALLING_WATER,
    DRIPPING_HONEY,
    FALLING_HONEY,
    LANDING_HONEY,
    FALLING_NECTAR,
    SOUL_FIRE_FLAME,
    ASH,
    CRIMSON_SPORE,
    WARPED_SPORE,
    SOUL,
    DRIPPING_OBSIDIAN_TEAR,
    FALLING_OBSIDIAN_TEAR,
    LANDING_OBSIDIAN_TEAR,
    REVERSE_PORTAL,
    WHITE_ASH,
    DUST_COLOR_TRANSITION,
    VIBRATION,
    FALLING_SPORE_BLOSSOM,
    SPORE_BLOSSOM_AIR,
    SMALL_FLAME,
    SNOWFLAKE,
    DRIPPING_DRIPSTONE_LAVA,
    FALLING_DRIPSTONE_LAVA,
    DRIPPING_DRIPSTONE_WATER,
    FALLING_DRIPSTONE_WATER,
    GLOW_SQUID_INK,
    GLOW,
    WAX_ON,
    WAX_OFF,
    ELECTRIC_SPARK,
    SCRAPE,
    SONIC_BOOM,
    SCULK_SOUL,
    SCULK_CHARGE,
    SCULK_CHARGE_POP,
    SHRIEK,
    CHERRY_LEAVES,
    EGG_CRACK,
    DUST_PLUME,
    WHITE_SMOKE,
    GUST,
    SMALL_GUST,
    GUST_EMITTER_LARGE,
    GUST_EMITTER_SMALL,
    TRIAL_SPAWNER_DETECTION,
    TRIAL_SPAWNER_DETECTION_OMINOUS,
    VAULT_CONNECTION,
    INFESTED,
    ITEM_COBWEB,
    DUST_PILLAR,
    OMINOUS_SPAWNING,
    RAID_OMEN,
    TRIAL_OMEN,
    /**
     * BARRIER, LIGHT -> BLOCK_MARKER (v1.18)
     */
    BLOCK_MARKER("BARRIER", "LIGHT");

    private final Particle particle;

    XParticle(String... alts) {
        Particle testParticle = tryGetParticle(this.name());
        Data.NAME_MAPPING.put(this.name(), this);

        for (String alt : alts) {
            if (testParticle == null) testParticle = tryGetParticle(alt);
            Data.NAME_MAPPING.put(alt, this);
        }

        this.particle = testParticle;
        if (particle != null) Data.BUKKIT_MAPPING.put(particle, this);
    }

    /**
     * Returns the bukkit particle.
     *
     * @return the particle
     */
    public Particle get() {
        return particle;
    }

    /**
     * Returns if the particle is supported.
     *
     * @return true if the particle is supported
     */
    public boolean isSupported() {
        return particle != null;
    }

    /**
     * Returns this particle if it is supported, otherwise returns the particle argument you passed.
     *
     * @param other the particle to return if this particle is not supported
     * @return this particle if it is supported, otherwise returns the particle argument you passed
     */
    public XParticle or(XParticle other) {
        return this.isSupported() ? this : other;
    }

    /**
     * Returns the XParticle associated with the given bukkit particle.
     *
     * @param particle the bukkit particle to match
     * @return the XParticle associated with the given bukkit particle
     * @throws UnsupportedOperationException if the given particle does not exist.
     */
    public static XParticle of(Particle particle) {
        Objects.requireNonNull(particle, "Cannot match null particle");
        XParticle mapping = Data.BUKKIT_MAPPING.get(particle);
        if (mapping != null) return mapping;
        throw new UnsupportedOperationException("Unknown particle: " + particle);
    }

    /**
     * Returns the XParticle associated with the given particle name.
     *
     * @param particle the particle name to match
     * @return the XParticle associated with the given particle name
     */
    public static Optional<XParticle> of(String particle) {
        Objects.requireNonNull(particle, "Cannot match null particle");
        return Optional.ofNullable(Data.NAME_MAPPING.get(particle));
    }

    private static Particle tryGetParticle(String particle) {
        try {
            return Particle.valueOf(particle);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static final class Data {
        private static final Map<String, XParticle> NAME_MAPPING = new HashMap<>();
        private static final Map<Particle, XParticle> BUKKIT_MAPPING = new EnumMap<>(Particle.class);
    }
}
