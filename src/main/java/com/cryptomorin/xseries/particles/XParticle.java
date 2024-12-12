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

package com.cryptomorin.xseries.particles;

import com.cryptomorin.xseries.base.XBase;
import com.cryptomorin.xseries.base.XRegistry;
import com.cryptomorin.xseries.base.annotations.XChange;
import org.bukkit.Particle;

import java.util.Optional;

/**
 * <b>XParticle</b> - Particle enum for <b>XSeries</b>
 * <p>
 * This class is mainly used to support {@link Particle}, especially for the "parity change" by
 * Spigot in 1.20.5 (see <a href="https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/diff/src/main/java/org/bukkit/Particle.java?until=8a34e009148cc297bcc9eb5c250fc4f5b071c4a7">...</a>).
 *
 * @author Crypto Morin, Collin Barber
 */
public enum XParticle implements XBase<XParticle, Particle> {
    /**
     * VILLAGER_ANGRY -> ANGRY_VILLAGER (v1.20.5)
     */
    ANGRY_VILLAGER("VILLAGER_ANGRY"),
    ASH,
    /**
     * BLOCK_CRACK, BLOCK_DUST -> BLOCK (v1.20.5)
     */
    BLOCK("BLOCK_CRACK", "BLOCK_DUST"),
    /**
     * Added v1.21.4
     */
    BLOCK_CRUMBLE,
    /**
     * BARRIER, LIGHT -> BLOCK_MARKER (v1.18)
     */
    BLOCK_MARKER("BARRIER", "LIGHT"),
    /**
     * WATER_BUBBLE -> BUBBLE (v1.20.5)
     */
    BUBBLE("WATER_BUBBLE"),
    BUBBLE_COLUMN_UP,
    BUBBLE_POP,
    CAMPFIRE_COSY_SMOKE,
    CAMPFIRE_SIGNAL_SMOKE,
    CHERRY_LEAVES,
    CLOUD,
    COMPOSTER,
    CRIMSON_SPORE,
    CRIT,
    CURRENT_DOWN,
    DAMAGE_INDICATOR,
    DOLPHIN,
    DRAGON_BREATH,
    DRIPPING_DRIPSTONE_LAVA,
    DRIPPING_DRIPSTONE_WATER,
    DRIPPING_HONEY,
    /**
     * DRIP_LAVA -> DRIPPING_LAVA (v1.20.5)
     */
    DRIPPING_LAVA("DRIP_LAVA"),
    DRIPPING_OBSIDIAN_TEAR,
    /**
     * DRIP_WATER -> DRIPPING_WATER (v1.20.5)
     */
    DRIPPING_WATER("DRIP_WATER"),
    /**
     * REDSTONE -> DUST (v1.20.5)
     */
    DUST("REDSTONE"),
    DUST_COLOR_TRANSITION,
    DUST_PILLAR,
    DUST_PLUME,
    /**
     * SPELL -> EFFECT (v1.20.5)
     */
    EFFECT("SPELL"),
    EGG_CRACK,
    /**
     * MOB_APPEARANCE -> ELDER_GUARDIAN (v1.20.5)
     */
    ELDER_GUARDIAN("MOB_APPEARANCE"),
    ELECTRIC_SPARK,
    /**
     * ENCHANTMENT_TABLE -> ENCHANT (v1.20.5)
     */
    ENCHANT("ENCHANTMENT_TABLE"),
    /**
     * CRIT_MAGIC -> ENCHANTED_HIT (v1.20.5)
     */
    ENCHANTED_HIT("CRIT_MAGIC"),
    END_ROD,
    /**
     * SPELL_MOB_AMBIENT -> SPELL_MOB -> ENTITY_EFFECT (v1.20.5)
     * The name was changed multiple times during the parity update
     *
     * @see <a href="https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/diff/src/main/java/org/bukkit/Particle.java?until=1113e50a392b36253c4ae458a6d3d73e04841111">...</a>
     */
    ENTITY_EFFECT("SPELL_MOB", "SPELL_MOB_AMBIENT"),
    /**
     * EXPLOSION_LARGE -> EXPLOSION (v1.20.5)
     */
    EXPLOSION("EXPLOSION_LARGE"),
    /**
     * EXPLOSION_HUGE -> EXPLOSION_EMITTER (v1.20.5)
     */
    EXPLOSION_EMITTER("EXPLOSION_HUGE"),
    FALLING_DRIPSTONE_LAVA,
    FALLING_DRIPSTONE_WATER,
    FALLING_DUST,
    FALLING_HONEY,
    FALLING_LAVA,
    FALLING_NECTAR,
    FALLING_OBSIDIAN_TEAR,
    FALLING_SPORE_BLOSSOM,
    FALLING_WATER,
    /**
     * FIREWORKS_SPARK -> FIREWORK (v1.20.5)
     */
    FIREWORK("FIREWORKS_SPARK"),
    /**
     * WATER_WAKE -> FISHING (v1.20.5)
     */
    FISHING("WATER_WAKE"),
    FLAME,
    FLASH,
    GLOW,
    GLOW_SQUID_INK,
    GUST,
    GUST_EMITTER_LARGE,
    GUST_EMITTER_SMALL,
    /**
     * VILLAGER_HAPPY -> HAPPY_VILLAGER (v1.20.5)
     */
    HAPPY_VILLAGER("VILLAGER_HAPPY"),
    HEART,
    INFESTED,
    /**
     * SPELL_INSTANT -> INSTANT_EFFECT (v1.20.5)
     */
    INSTANT_EFFECT("SPELL_INSTANT"),
    /**
     * ITEM_CRACK -> ITEM (v1.20.5)
     */
    ITEM("ITEM_CRACK"),
    ITEM_COBWEB,
    /**
     * SLIME -> ITEM_SLIME (v1.20.5)
     */
    ITEM_SLIME("SLIME"),
    /**
     * SNOWBALL, SNOW_SHOVEL -> ITEM_SNOWBALL (v1.20.5)
     */
    ITEM_SNOWBALL("SNOWBALL", "SNOW_SHOVEL"),
    LANDING_HONEY,
    LANDING_LAVA,
    LANDING_OBSIDIAN_TEAR,
    /**
     * SMOKE_LARGE -> LARGE_SMOKE (v1.20.5)
     */
    LARGE_SMOKE("SMOKE_LARGE"),
    LAVA,
    /**
     * TOWN_AURA -> MYCELIUM (v1.20.5)
     */
    MYCELIUM("TOWN_AURA"),
    NAUTILUS,
    NOTE,
    OMINOUS_SPAWNING,
    PALE_OAK_LEAVES,
    @XChange(version = "1.20.5", from = "EXPLOSION_NORMAL", to = "POOF")
    POOF("EXPLOSION_NORMAL"),
    PORTAL,
    RAID_OMEN,
    /**
     * WATER_DROP -> RAIN (v1.20.5)
     */
    RAIN("WATER_DROP"),
    REVERSE_PORTAL,
    SCRAPE,
    SCULK_CHARGE,
    SCULK_CHARGE_POP,
    SCULK_SOUL,
    SHRIEK,
    SMALL_FLAME,
    SMALL_GUST,
    /**
     * SMOKE_NORMAL -> SMOKE (v1.20.5)
     */
    SMOKE("SMOKE_NORMAL"),
    SNEEZE,
    SNOWFLAKE,
    SONIC_BOOM,
    SOUL,
    SOUL_FIRE_FLAME,
    SPIT,
    /**
     * WATER_SPLASH -> SPLASH (v1.20.5)
     */
    SPLASH("WATER_SPLASH"),
    SPORE_BLOSSOM_AIR,
    SQUID_INK,
    SWEEP_ATTACK,
    /**
     * TOTEM -> TOTEM_OF_UNDYING (v1.20.5)
     */
    TOTEM_OF_UNDYING("TOTEM"),
    /**
     * Added v1.21.4
     */
    TRAIL,
    TRIAL_OMEN,
    TRIAL_SPAWNER_DETECTION,
    TRIAL_SPAWNER_DETECTION_OMINOUS,
    /**
     * SUSPENDED -> UNDERWATER (v1.20.5)
     */
    UNDERWATER("SUSPENDED"),
    VAULT_CONNECTION,
    VIBRATION,
    WARPED_SPORE,
    WAX_OFF,
    WAX_ON,
    WHITE_ASH,
    WHITE_SMOKE,
    /**
     * SPELL_WITCH -> WITCH (v1.20.5)
     */
    WITCH("SPELL_WITCH");

    public static final XRegistry<XParticle, Particle> REGISTRY = Data.REGISTRY;

    private final Particle particle;

    XParticle(String... names) {
        this.particle = Data.REGISTRY.stdEnum(this, names);
    }

    @Override
    public String[] getNames() {
        return new String[]{name()};
    }

    @Override
    public Particle get() {
        return particle;
    }

    /**
     * Returns the XParticle associated with the given bukkit particle.
     *
     * @param particle the bukkit particle to match
     * @return the XParticle associated with the given bukkit particle
     * @throws UnsupportedOperationException if the given particle does not exist.
     */
    public static XParticle of(Particle particle) {
        return REGISTRY.getByBukkitForm(particle);
    }

    /**
     * Returns the XParticle associated with the given particle name.
     *
     * @param particle the particle name to match
     * @return the XParticle associated with the given particle name
     */
    public static Optional<XParticle> of(String particle) {
        return REGISTRY.getByName(particle);
    }

    private static final class Data {
        private static final XRegistry<XParticle, Particle> REGISTRY =
                new XRegistry<>(Particle.class, XParticle.class, XParticle[]::new);
    }
}
