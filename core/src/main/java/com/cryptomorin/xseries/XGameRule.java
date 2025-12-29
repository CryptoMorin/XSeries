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
import com.cryptomorin.xseries.base.annotations.XInfo;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

public enum XGameRule implements XBase<XGameRule, String> {

    COMMAND_BLOCK_OUTPUT("commandBlockOutput", /* v1.21.11+ */ "command_block_output"),
    ADVANCE_TIME("doDaylightCycle", /* v1.21.11+ */ "advance_time"),
    ENTITY_DROPS("doEntityDrops", /* v1.21.11+ */ "entity_drops"),
    MOB_DROPS("doMobLoot", /* v1.21.11+ */ "mob_drops"),
    SPAWN_MOBS("doMobSpawning", /* v1.21.11+ */ "spawn_mobs"),
    BLOCK_DROPS("doTileDrops", /* v1.21.11+ */ "block_drops"),
    KEEP_INVENTORY("keepInventory", /* v1.21.11+ */ "keep_inventory"),
    LOG_ADMIN_COMMANDS("logAdminCommands", /* v1.21.11+ */ "log_admin_commands"),
    MOB_GRIEFING("mobGriefing", /* v1.21.11+ */ "mob_griefing"),
    NATURAL_HEALTH_REGENERATION("naturalRegeneration", /* v1.21.11+ */ "natural_health_regeneration"),
    REDUCED_DEBUG_INFO("reducedDebugInfo", /* v1.21.11+ */ "reduced_debug_info"),
    SEND_COMMAND_FEEDBACK("sendCommandFeedback", /* v1.21.11+ */ "send_command_feedback"),
    SHOW_DEATH_MESSAGES("showDeathMessages", /* v1.21.11+ */ "show_death_messages"),
    RANDOM_TICK_SPEED("randomTickSpeed", /* v1.21.11+ */ "random_tick_speed"),

    @XInfo(since = "1.9")
    SPECTATORS_GENERATE_CHUNKS("spectatorsGenerateChunks", /* v1.21.11+ */ "spectators_generate_chunks"),
    RESPAWN_RADIUS("spawnRadius", /* v1.21.11+ */ "respawn_radius"),
    ELYTRA_MOVEMENT_CHECK("disableElytraMovementCheck", /* v1.21.11+ */ "elytra_movement_check"),

    @XInfo(since = "1.11")
    ADVANCE_WEATHER("doWeatherCycle", /* v1.21.11+ */ "advance_weather"),
    MAX_ENTITY_CRAMMING("maxEntityCramming", /* v1.21.11+ */ "max_entity_cramming"),

    @XInfo(since = "1.12")
    LIMITED_CRAFTING("doLimitedCrafting", /* v1.21.11+ */ "limited_crafting"),
    MAX_COMMAND_SEQUENCE_LENGTH("maxCommandChainLength", /* v1.21.11+ */ "max_command_sequence_length"),
    SHOW_ADVANCEMENT_MESSAGES("announceAdvancements", /* v1.21.11+ */ "show_advancement_messages"),

    @XInfo(since = "1.12", removedSince = "1.13")
    GAME_LOOP_FUNCTION("gameLoopFunction"),

    @XInfo(since = "1.14.3")
    RAIDS("disableRaids", /* v1.21.11+ */ "raids"),

    @XInfo(since = "1.15")
    SPAWN_PHANTOMS("doInsomnia", /* v1.21.11+ */ "spawn_phantoms"),
    IMMEDIATE_RESPAWN("doImmediateRespawn", /* v1.21.11+ */ "immediate_respawn"),
    DROWNING_DAMAGE("drowningDamage", /* v1.21.11+ */ "drowning_damage"),
    FALL_DAMAGE("fallDamage", /* v1.21.11+ */ "fall_damage"),
    FIRE_DAMAGE("fireDamage", /* v1.21.11+ */ "fire_damage"),

    @XInfo(since = "1.15.2")
    SPAWN_PATROLS("doPatrolSpawning", /* v1.21.11+ */ "spawn_patrols"),
    SPAWN_WANDERING_TRADERS("doTraderSpawning", /* v1.21.11+ */ "spawn_wandering_traders"),

    @XInfo(since = "1.16.1")
    FORGIVE_DEAD_PLAYERS("forgiveDeadPlayers", /* v1.21.11+ */ "forgive_dead_players"),
    UNIVERSAL_ANGER("universalAnger", /* v1.21.11+ */ "universal_anger"),

    @XInfo(since = "1.17")
    FREEZE_DAMAGE("freezeDamage", /* v1.21.11+ */ "freeze_damage"),
    PLAYERS_SLEEPING_PERCENTAGE("playersSleepingPercentage", /* v1.21.11+ */ "players_sleeping_percentage"),

    @XInfo(since = "1.19")
    SPAWN_WARDENS("doWardenSpawning", /* v1.21.11+ */ "spawn_wardens"),

    @XInfo(since = "1.19.3")
    BLOCK_EXPLOSION_DROP_DECAY("blockExplosionDropDecay", /* v1.21.11+ */ "block_explosion_drop_decay"),
    MOB_EXPLOSION_DROP_DECAY("mobExplosionDropDecay", /* v1.21.11+ */ "mob_explosion_drop_decay"),
    TNT_EXPLOSION_DROP_DECAY("tntExplosionDropDecay", /* v1.21.11+ */ "tnt_explosion_drop_decay"),
    WATER_SOURCE_CONVERSION("waterSourceConversion", /* v1.21.11+ */ "water_source_conversion"),
    LAVA_SOURCE_CONVERSION("lavaSourceConversion", /* v1.21.11+ */ "lava_source_conversion"),
    GLOBAL_SOUND_EVENTS("globalSoundEvents", /* v1.21.11+ */ "global_sound_events"),
    MAX_SNOW_ACCUMULATION_HEIGHT("snowAccumulationHeight", /* v1.21.11+ */ "max_snow_accumulation_height"),

    @XInfo(since = "1.19.4")
    SPREAD_VINES("doVinesSpread", /* v1.21.11+ */ "spread_vines"),
    MAX_BLOCK_MODIFICATIONS("commandModificationBlockLimit", /* v1.21.11+ */ "max_block_modifications"),

    @XInfo(since = "1.20.2")
    ENDER_PEARLS_VANISH_ON_DEATH("enderPearlsVanishOnDeath", /* v1.21.11+ */ "ender_pearls_vanish_on_death"),

    @XInfo(since = "1.20.3")
    PROJECTILES_CAN_BREAK_BLOCKS("projectilesCanBreakBlocks", /* v1.21.11+ */ "projectiles_can_break_blocks"),
    MAX_COMMAND_FORKS("maxCommandForkCount", /* v1.21.11+ */ "max_command_forks"),
    PLAYERS_NETHER_PORTAL_DEFAULT_DELAY("playersNetherPortalDefaultDelay", /* v1.21.11+ */ "players_nether_portal_default_delay"),
    PLAYERS_NETHER_PORTAL_CREATIVE_DELAY("playersNetherPortalCreativeDelay", /* v1.21.11+ */ "players_nether_portal_creative_delay"),

    @XInfo(since = "1.20.5", removedSince = "1.21.9")
    SPAWN_CHUNK_RADIUS("spawnChunkRadius"),

    @XInfo(since = "1.21.2")
    PLAYER_MOVEMENT_CHECK("disablePlayerMovementCheck", /* v1.21.11+ */ "player_movement_check"),
    MAX_MINECART_SPEED("minecartMaxSpeed", /* v1.21.11+ */ "max_minecart_speed"),

    @XInfo(since = "1.21.5")
    TNT_EXPLODES("tntExplodes", /* v1.21.11+ */ "tnt_explodes"),

    @XInfo(since = "1.21.5", removedSince = "1.21.11")
    ALLOW_FIRE_TICKS_AWAY_FROM_PLAYER("allowFireTicksAwayFromPlayer"),

    @XInfo(since = "1.21.6")
    LOCATOR_BAR("locatorBar", /* v1.21.11+ */ "locator_bar"),

    @XInfo(since = "1.21.9")
    PVP("pvp"),
    ALLOW_ENTERING_NETHER_USING_PORTALS("allowEnteringNetherUsingPortals", /* v1.21.11+ */ "allow_entering_nether_using_portals"),
    SPAWN_MONSTERS("spawnMonsters", /* v1.21.11+ */ "spawn_monsters"),
    COMMAND_BLOCKS_WORK("commandBlocksEnabled", /* v1.21.11+ */ "command_blocks_work"),
    SPAWNER_BLOCKS_WORK("spawnerBlocksEnabled", /* v1.21.11+ */ "spawner_blocks_work"),

    @XInfo(since = "1.21.11")
    FIRE_SPREAD_RADIUS_AROUND_PLAYER("fire_spread_radius_around_player"),

    @XInfo(since = "?", removedSince = "1.21.11")
    DO_FIRE_TICK("doFireTick");

    private final String[] names;
    private final String value;

    XGameRule(String... names) {
        this.names = names;
        World world = Bukkit.getWorlds().get(0);
        this.value = Arrays.stream(names).filter(world::isGameRule).findAny().orElse(null);
    }

    @Override
    public String[] getNames() {
        return this.names;
    }

    @Override
    public @Nullable String get() {
        return this.value;
    }
}
