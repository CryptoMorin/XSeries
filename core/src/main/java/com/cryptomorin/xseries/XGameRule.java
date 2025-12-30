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

    COMMAND_BLOCK_OUTPUT(/* v1.21.11+ */ "command_block_output", "commandBlockOutput"),
    ADVANCE_TIME(/* v1.21.11+ */ "advance_time", "doDaylightCycle"),
    ENTITY_DROPS(/* v1.21.11+ */ "entity_drops", "doEntityDrops"),
    MOB_DROPS(/* v1.21.11+ */ "mob_drops", "doMobLoot"),
    SPAWN_MOBS(/* v1.21.11+ */ "spawn_mobs", "doMobSpawning"),
    BLOCK_DROPS(/* v1.21.11+ */ "block_drops", "doTileDrops"),
    KEEP_INVENTORY(/* v1.21.11+ */ "keep_inventory", "keepInventory"),
    LOG_ADMIN_COMMANDS(/* v1.21.11+ */ "log_admin_commands", "logAdminCommands"),
    MOB_GRIEFING(/* v1.21.11+ */ "mob_griefing", "mobGriefing"),
    NATURAL_HEALTH_REGENERATION(/* v1.21.11+ */ "natural_health_regeneration", "naturalRegeneration"),
    REDUCED_DEBUG_INFO(/* v1.21.11+ */ "reduced_debug_info", "reducedDebugInfo"),
    SEND_COMMAND_FEEDBACK(/* v1.21.11+ */ "send_command_feedback", "sendCommandFeedback"),
    SHOW_DEATH_MESSAGES(/* v1.21.11+ */ "show_death_messages", "showDeathMessages"),
    RANDOM_TICK_SPEED(/* v1.21.11+ */ "random_tick_speed", "randomTickSpeed"),

    @XInfo(since = "1.9")
    SPECTATORS_GENERATE_CHUNKS(/* v1.21.11+ */ "spectators_generate_chunks", "spectatorsGenerateChunks"),
    RESPAWN_RADIUS(/* v1.21.11+ */ "respawn_radius", "spawnRadius"),
    ELYTRA_MOVEMENT_CHECK(/* v1.21.11+ */ "elytra_movement_check", "disableElytraMovementCheck"),

    @XInfo(since = "1.11")
    ADVANCE_WEATHER(/* v1.21.11+ */ "advance_weather", "doWeatherCycle"),
    MAX_ENTITY_CRAMMING(/* v1.21.11+ */ "max_entity_cramming", "maxEntityCramming"),

    @XInfo(since = "1.12")
    LIMITED_CRAFTING(/* v1.21.11+ */ "limited_crafting", "doLimitedCrafting"),
    MAX_COMMAND_SEQUENCE_LENGTH(/* v1.21.11+ */ "max_command_sequence_length", "maxCommandChainLength"),
    SHOW_ADVANCEMENT_MESSAGES(/* v1.21.11+ */ "show_advancement_messages", "announceAdvancements"),

    @XInfo(since = "1.12", removedSince = "1.13")
    GAME_LOOP_FUNCTION("gameLoopFunction"),

    @XInfo(since = "1.14.3")
    RAIDS(/* v1.21.11+ */ "raids", "disableRaids"),

    @XInfo(since = "1.15")
    SPAWN_PHANTOMS(/* v1.21.11+ */ "spawn_phantoms", "doInsomnia"),
    IMMEDIATE_RESPAWN(/* v1.21.11+ */ "immediate_respawn", "doImmediateRespawn"),
    DROWNING_DAMAGE(/* v1.21.11+ */ "drowning_damage", "drowningDamage"),
    FALL_DAMAGE(/* v1.21.11+ */ "fall_damage", "fallDamage"),
    FIRE_DAMAGE(/* v1.21.11+ */ "fire_damage", "fireDamage"),

    @XInfo(since = "1.15.2")
    SPAWN_PATROLS(/* v1.21.11+ */ "spawn_patrols", "doPatrolSpawning"),
    SPAWN_WANDERING_TRADERS(/* v1.21.11+ */ "spawn_wandering_traders", "doTraderSpawning"),

    @XInfo(since = "1.16.1")
    FORGIVE_DEAD_PLAYERS(/* v1.21.11+ */ "forgive_dead_players", "forgiveDeadPlayers"),
    UNIVERSAL_ANGER(/* v1.21.11+ */ "universal_anger", "universalAnger"),

    @XInfo(since = "1.17")
    FREEZE_DAMAGE(/* v1.21.11+ */ "freeze_damage", "freezeDamage"),
    PLAYERS_SLEEPING_PERCENTAGE(/* v1.21.11+ */ "players_sleeping_percentage", "playersSleepingPercentage"),

    @XInfo(since = "1.19")
    SPAWN_WARDENS(/* v1.21.11+ */ "spawn_wardens", "doWardenSpawning"),

    @XInfo(since = "1.19.3")
    BLOCK_EXPLOSION_DROP_DECAY(/* v1.21.11+ */ "block_explosion_drop_decay", "blockExplosionDropDecay"),
    MOB_EXPLOSION_DROP_DECAY(/* v1.21.11+ */ "mob_explosion_drop_decay", "mobExplosionDropDecay"),
    TNT_EXPLOSION_DROP_DECAY(/* v1.21.11+ */ "tnt_explosion_drop_decay", "tntExplosionDropDecay"),
    WATER_SOURCE_CONVERSION(/* v1.21.11+ */ "water_source_conversion", "waterSourceConversion"),
    LAVA_SOURCE_CONVERSION(/* v1.21.11+ */ "lava_source_conversion", "lavaSourceConversion"),
    GLOBAL_SOUND_EVENTS(/* v1.21.11+ */ "global_sound_events", "globalSoundEvents"),
    MAX_SNOW_ACCUMULATION_HEIGHT(/* v1.21.11+ */ "max_snow_accumulation_height", "snowAccumulationHeight"),

    @XInfo(since = "1.19.4")
    SPREAD_VINES(/* v1.21.11+ */ "spread_vines", "doVinesSpread"),
    MAX_BLOCK_MODIFICATIONS(/* v1.21.11+ */ "max_block_modifications", "commandModificationBlockLimit"),

    @XInfo(since = "1.20.2")
    ENDER_PEARLS_VANISH_ON_DEATH(/* v1.21.11+ */ "ender_pearls_vanish_on_death", "enderPearlsVanishOnDeath"),

    @XInfo(since = "1.20.3")
    PROJECTILES_CAN_BREAK_BLOCKS(/* v1.21.11+ */ "projectiles_can_break_blocks", "projectilesCanBreakBlocks"),
    MAX_COMMAND_FORKS(/* v1.21.11+ */ "max_command_forks", "maxCommandForkCount"),
    PLAYERS_NETHER_PORTAL_DEFAULT_DELAY(/* v1.21.11+ */ "players_nether_portal_default_delay", "playersNetherPortalDefaultDelay"),
    PLAYERS_NETHER_PORTAL_CREATIVE_DELAY(/* v1.21.11+ */ "players_nether_portal_creative_delay", "playersNetherPortalCreativeDelay"),

    @XInfo(since = "1.20.5", removedSince = "1.21.9")
    SPAWN_CHUNK_RADIUS("spawnChunkRadius"),

    @XInfo(since = "1.21.2")
    PLAYER_MOVEMENT_CHECK(/* v1.21.11+ */ "player_movement_check", "disablePlayerMovementCheck"),
    MAX_MINECART_SPEED(/* v1.21.11+ */ "max_minecart_speed", "minecartMaxSpeed"),

    @XInfo(since = "1.21.5")
    TNT_EXPLODES(/* v1.21.11+ */ "tnt_explodes", "tntExplodes"),

    @XInfo(since = "1.21.5", removedSince = "1.21.11")
    ALLOW_FIRE_TICKS_AWAY_FROM_PLAYER("allowFireTicksAwayFromPlayer"),

    @XInfo(since = "1.21.6")
    LOCATOR_BAR(/* v1.21.11+ */ "locator_bar", "locatorBar"),

    @XInfo(since = "1.21.9")
    PVP("pvp"),
    ALLOW_ENTERING_NETHER_USING_PORTALS(/* v1.21.11+ */ "allow_entering_nether_using_portals", "allowEnteringNetherUsingPortals"),
    SPAWN_MONSTERS(/* v1.21.11+ */ "spawn_monsters", "spawnMonsters"),
    COMMAND_BLOCKS_WORK(/* v1.21.11+ */ "command_blocks_work", "commandBlocksEnabled"),
    SPAWNER_BLOCKS_WORK(/* v1.21.11+ */ "spawner_blocks_work", "spawnerBlocksEnabled"),

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
