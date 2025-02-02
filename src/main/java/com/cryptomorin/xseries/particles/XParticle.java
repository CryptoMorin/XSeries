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

package com.cryptomorin.xseries.particles;

import com.cryptomorin.xseries.base.XBase;
import com.cryptomorin.xseries.base.XRegistry;
import com.cryptomorin.xseries.base.annotations.XChange;
import com.cryptomorin.xseries.base.annotations.XInfo;
import com.cryptomorin.xseries.base.annotations.XMerge;
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
    @XChange(version = "v1.20.5", from = "VILLAGER_ANGRY", to = "ANGRY_VILLAGER")
    ANGRY_VILLAGER("VILLAGER_ANGRY"),

    ASH,

    /**
     * BLOCK_CRACK, BLOCK_DUST -> BLOCK (v1.20.5)
     */
    @XMerge(version = "1.20.5", name = "BLOCK_DUST")
    BLOCK("BLOCK_CRACK"),

    @XInfo(since = "1.21.4")
    BLOCK_CRUMBLE,

    /**
     * BARRIER, LIGHT -> BLOCK_MARKER (v1.18)
     * Now controlled with {@code BlockData.class} for what to display.
     */
    @XMerge(version = "1.17", name = "LIGHT")
    BLOCK_MARKER("BARRIER"),

    @XChange(version = "v1.20.5", from = "WATER_BUBBLE", to = "BUBBLE")
    BUBBLE("WATER_BUBBLE"),

    BUBBLE_COLUMN_UP,
    BUBBLE_POP,
    CAMPFIRE_COSY_SMOKE,
    CAMPFIRE_SIGNAL_SMOKE,

    @XMerge(version = "1.20", name = "FALLING_CHERRY_LEAVES")
    @XMerge(version = "1.20", name = "LANDING_CHERRY_LEAVES")
    CHERRY_LEAVES("DRIPPING_CHERRY_LEAVES"),

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

    @XChange(version = "v1.20.5", from = "DRIP_LAVA", to = "DRIPPING_LAVA")
    DRIPPING_LAVA("DRIP_LAVA"),

    DRIPPING_OBSIDIAN_TEAR,

    @XChange(version = "v1.20.5", from = "DRIP_WATER", to = "DRIPPING_WATER")
    DRIPPING_WATER("DRIP_WATER"),

    @XChange(version = "v1.20.5", from = "REDSTONE", to = "DUST")
    DUST("REDSTONE"),

    DUST_COLOR_TRANSITION,
    DUST_PILLAR,
    DUST_PLUME,

    @XChange(version = "v1.20.5", from = "SPELL", to = "EFFECT")
    EFFECT("SPELL"),

    EGG_CRACK,

    @XChange(version = "v1.20.5", from = "MOB_APPEARANCE", to = "ELDER_GUARDIAN")
    ELDER_GUARDIAN("MOB_APPEARANCE"),

    ELECTRIC_SPARK,

    @XChange(version = "v1.20.5", from = "ENCHANTMENT_TABLE", to = "ENCHANT")
    ENCHANT("ENCHANTMENT_TABLE"),

    @XChange(version = "v1.20.5", from = "CRIT_MAGIC", to = "ENCHANTED_HIT")
    ENCHANTED_HIT("CRIT_MAGIC"),

    END_ROD,

    /**
     * SPELL_MOB_AMBIENT -> SPELL_MOB -> ENTITY_EFFECT (v1.20.5)
     * The name was changed multiple times during the parity update
     *
     * @see <a href="https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/diff/src/main/java/org/bukkit/Particle.java?until=1113e50a392b36253c4ae458a6d3d73e04841111">...</a>
     */
    @XMerge(version = "1.20.5", name = "SPELL_MOB_AMBIENT")
    ENTITY_EFFECT("SPELL_MOB"),

    /**
     * It was just removed...
     * https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/diff/src/main/java/org/bukkit/Particle.java?until=f8b2086d60942eb2cd7ac25a2a1408cb790c222c
     */
    @XInfo(since = "1.0.0", removedSince = "1.13")
    @Deprecated
    FOOTSTEP,

    /**
     * @see #FOOTSTEP same fate.
     */
    @XInfo(since = "1.0.0", removedSince = "1.13")
    @Deprecated
    ITEM_TAKE,

    @XChange(version = "v1.20.5", from = "EXPLOSION_LARGE", to = "EXPLOSION")
    EXPLOSION("EXPLOSION_LARGE"),

    @XChange(version = "v1.20.5", from = "EXPLOSION_HUGE", to = "EXPLOSION_EMITTER")
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

    @XChange(version = "v1.20.5", from = "FIREWORKS_SPARK", to = "FIREWORK")
    FIREWORK("FIREWORKS_SPARK"),

    @XChange(version = "v1.20.5", from = "WATER_WAKE", to = "FISHING")
    FISHING("WATER_WAKE"),

    FLAME,
    FLASH,
    GLOW,
    GLOW_SQUID_INK,
    GUST,
    GUST_EMITTER_LARGE,
    GUST_EMITTER_SMALL,

    @XChange(version = "v1.20.5", from = "VILLAGER_HAPPY", to = "HAPPY_VILLAGER")
    HAPPY_VILLAGER("VILLAGER_HAPPY"),

    HEART,
    INFESTED,

    @XChange(version = "v1.20.5", from = "SPELL_INSTANT", to = "INSTANT_EFFECT")
    INSTANT_EFFECT("SPELL_INSTANT"),

    @XChange(version = "v1.20.5", from = "ITEM_CRACK", to = "ITEM")
    ITEM("ITEM_CRACK"),

    ITEM_COBWEB,

    @XChange(version = "v1.20.5", from = "SLIME", to = "ITEM_SLIME")
    ITEM_SLIME("SLIME"),

    /**
     * SNOWBALL, SNOW_SHOVEL -> ITEM_SNOWBALL (v1.20.5)
     */
    @XMerge(version = "1.20.5", name = "SNOW_SHOVEL")
    ITEM_SNOWBALL("SNOWBALL"),

    LANDING_HONEY,
    LANDING_LAVA,
    LANDING_OBSIDIAN_TEAR,

    @XChange(version = "v1.20.5", from = "SMOKE_LARGE", to = "LARGE_SMOKE")
    LARGE_SMOKE("SMOKE_LARGE"),

    LAVA,

    @XChange(version = "v1.20.5", from = "TOWN_AURA", to = "MYCELIUM")
    MYCELIUM("TOWN_AURA"),

    NAUTILUS,
    NOTE,
    OMINOUS_SPAWNING,
    PALE_OAK_LEAVES,

    @XChange(version = "1.20.5", from = "EXPLOSION_NORMAL", to = "POOF")
    POOF("EXPLOSION_NORMAL"),

    PORTAL,
    RAID_OMEN,

    @XChange(version = "1.20.5", from = "WATER_DROP", to = "RAIN")
    RAIN("WATER_DROP"),

    REVERSE_PORTAL,
    SCRAPE,
    SCULK_CHARGE,
    SCULK_CHARGE_POP,
    SCULK_SOUL,
    SHRIEK,
    SMALL_FLAME,
    SMALL_GUST,

    @XChange(version = "v1.20.5", from = "SMOKE_NORMAL", to = "SMOKE")
    SMOKE("SMOKE_NORMAL"),

    SNEEZE,
    SNOWFLAKE,
    SONIC_BOOM,
    SOUL,
    SOUL_FIRE_FLAME,
    SPIT,

    @XChange(version = "v1.20.5", from = "WATER_SPLASH", to = "SPLASH")
    SPLASH("WATER_SPLASH"),

    SPORE_BLOSSOM_AIR,
    SQUID_INK,
    SWEEP_ATTACK,

    @XChange(version = "v1.20.5", from = "TOTEM", to = "TOTEM_OF_UNDYING")
    TOTEM_OF_UNDYING("TOTEM"),

    @XInfo(since = "1.21.4")
    TRAIL,

    TRIAL_OMEN,
    TRIAL_SPAWNER_DETECTION,
    TRIAL_SPAWNER_DETECTION_OMINOUS,

    /**
     * Since the beginning, this was marked as a duplicate of {@link #UNDERWATER}:
     * https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/Particle.java?until=5010ed00d3f83b7c7acbf4c1b16f7c89f309eb9a&untilPath=src%2Fmain%2Fjava%2Forg%2Fbukkit%2FParticle.java#17-18
     * <p>
     * however, {@code SUSPENDED_DEPTH} specifically, was marked with "boolean register"
     * as {@code false}, and this field is used in the registry:
     * https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/Registry.java?until=7c0ec598f00807145ad78d2666d95da26836d625&untilPath=src%2Fmain%2Fjava%2Forg%2Fbukkit%2FRegistry.java#215
     *
     * @see #UNDERWATER
     */
    @XChange(version = "1.20.5", from = "SUSPENDED/SUSPENDED_DEPTH", to = "UNDERWATER")
    @XMerge(version = "1.20.5", name = "SUSPENDED_DEPTH")
    UNDERWATER("SUSPENDED"),

    VAULT_CONNECTION,
    VIBRATION,
    WARPED_SPORE,
    WAX_OFF,
    WAX_ON,
    WHITE_ASH,
    WHITE_SMOKE,

    @XChange(version = "1.20.5", from = "SPELL_WITCH", to = "WITCH")
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
