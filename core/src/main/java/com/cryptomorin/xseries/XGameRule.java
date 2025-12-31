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
import org.bukkit.GameRule;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;

public enum XGameRule implements XBase<XGameRule, String> {

    COMMAND_BLOCK_OUTPUT(Boolean.class, /* v1.21.11+ */ "command_block_output", "commandBlockOutput"),
    ADVANCE_TIME(Boolean.class, /* v1.21.11+ */ "advance_time", "doDaylightCycle"),
    ENTITY_DROPS(Boolean.class, /* v1.21.11+ */ "entity_drops", "doEntityDrops"),
    MOB_DROPS(Boolean.class, /* v1.21.11+ */ "mob_drops", "doMobLoot"),
    SPAWN_MOBS(Boolean.class, /* v1.21.11+ */ "spawn_mobs", "doMobSpawning"),
    BLOCK_DROPS(Boolean.class, /* v1.21.11+ */ "block_drops", "doTileDrops"),
    KEEP_INVENTORY(Boolean.class, /* v1.21.11+ */ "keep_inventory", "keepInventory"),
    LOG_ADMIN_COMMANDS(Boolean.class, /* v1.21.11+ */ "log_admin_commands", "logAdminCommands"),
    MOB_GRIEFING(Boolean.class, /* v1.21.11+ */ "mob_griefing", "mobGriefing"),
    NATURAL_HEALTH_REGENERATION(Boolean.class, /* v1.21.11+ */ "natural_health_regeneration", "naturalRegeneration"),
    REDUCED_DEBUG_INFO(Boolean.class, /* v1.21.11+ */ "reduced_debug_info", "reducedDebugInfo"),
    SEND_COMMAND_FEEDBACK(Boolean.class, /* v1.21.11+ */ "send_command_feedback", "sendCommandFeedback"),
    SHOW_DEATH_MESSAGES(Boolean.class, /* v1.21.11+ */ "show_death_messages", "showDeathMessages"),
    RANDOM_TICK_SPEED(Integer.class, /* v1.21.11+ */ "random_tick_speed", "randomTickSpeed"),

    @XInfo(since = "1.9")
    SPECTATORS_GENERATE_CHUNKS(Boolean.class, /* v1.21.11+ */ "spectators_generate_chunks", "spectatorsGenerateChunks"),
    RESPAWN_RADIUS(Integer.class, /* v1.21.11+ */ "respawn_radius", "spawnRadius"),
    ELYTRA_MOVEMENT_CHECK(Boolean.class, /* v1.21.11+ */ "elytra_movement_check", "disableElytraMovementCheck"),

    @XInfo(since = "1.11")
    ADVANCE_WEATHER(Boolean.class, /* v1.21.11+ */ "advance_weather", "doWeatherCycle"),
    MAX_ENTITY_CRAMMING(Integer.class, /* v1.21.11+ */ "max_entity_cramming", "maxEntityCramming"),

    @XInfo(since = "1.12")
    LIMITED_CRAFTING(Boolean.class, /* v1.21.11+ */ "limited_crafting", "doLimitedCrafting"),
    MAX_COMMAND_SEQUENCE_LENGTH(Integer.class, /* v1.21.11+ */ "max_command_sequence_length", "maxCommandChainLength"),
    SHOW_ADVANCEMENT_MESSAGES(Boolean.class, /* v1.21.11+ */ "show_advancement_messages", "announceAdvancements"),

    @XInfo(since = "1.12", removedSince = "1.13")
    GAME_LOOP_FUNCTION(String.class, "gameLoopFunction"),

    @XInfo(since = "1.14.3")
    RAIDS(Boolean.class, /* v1.21.11+ */ "raids", "disableRaids"),

    @XInfo(since = "1.15")
    SPAWN_PHANTOMS(Boolean.class, /* v1.21.11+ */ "spawn_phantoms", "doInsomnia"),
    IMMEDIATE_RESPAWN(Boolean.class, /* v1.21.11+ */ "immediate_respawn", "doImmediateRespawn"),
    DROWNING_DAMAGE(Boolean.class, /* v1.21.11+ */ "drowning_damage", "drowningDamage"),
    FALL_DAMAGE(Boolean.class, /* v1.21.11+ */ "fall_damage", "fallDamage"),
    FIRE_DAMAGE(Boolean.class, /* v1.21.11+ */ "fire_damage", "fireDamage"),

    @XInfo(since = "1.15.2")
    SPAWN_PATROLS(Boolean.class, /* v1.21.11+ */ "spawn_patrols", "doPatrolSpawning"),
    SPAWN_WANDERING_TRADERS(Boolean.class, /* v1.21.11+ */ "spawn_wandering_traders", "doTraderSpawning"),

    @XInfo(since = "1.16.1")
    FORGIVE_DEAD_PLAYERS(Boolean.class, /* v1.21.11+ */ "forgive_dead_players", "forgiveDeadPlayers"),
    UNIVERSAL_ANGER(Boolean.class, /* v1.21.11+ */ "universal_anger", "universalAnger"),

    @XInfo(since = "1.17")
    FREEZE_DAMAGE(Boolean.class, /* v1.21.11+ */ "freeze_damage", "freezeDamage"),
    PLAYERS_SLEEPING_PERCENTAGE(Integer.class, /* v1.21.11+ */ "players_sleeping_percentage", "playersSleepingPercentage"),

    @XInfo(since = "1.19")
    SPAWN_WARDENS(Boolean.class, /* v1.21.11+ */ "spawn_wardens", "doWardenSpawning"),

    @XInfo(since = "1.19.3")
    BLOCK_EXPLOSION_DROP_DECAY(Boolean.class, /* v1.21.11+ */ "block_explosion_drop_decay", "blockExplosionDropDecay"),
    MOB_EXPLOSION_DROP_DECAY(Boolean.class, /* v1.21.11+ */ "mob_explosion_drop_decay", "mobExplosionDropDecay"),
    TNT_EXPLOSION_DROP_DECAY(Boolean.class, /* v1.21.11+ */ "tnt_explosion_drop_decay", "tntExplosionDropDecay"),
    WATER_SOURCE_CONVERSION(Boolean.class, /* v1.21.11+ */ "water_source_conversion", "waterSourceConversion"),
    LAVA_SOURCE_CONVERSION(Boolean.class, /* v1.21.11+ */ "lava_source_conversion", "lavaSourceConversion"),
    GLOBAL_SOUND_EVENTS(Boolean.class, /* v1.21.11+ */ "global_sound_events", "globalSoundEvents"),
    MAX_SNOW_ACCUMULATION_HEIGHT(Integer.class, /* v1.21.11+ */ "max_snow_accumulation_height", "snowAccumulationHeight"),

    @XInfo(since = "1.19.4")
    SPREAD_VINES(Boolean.class, /* v1.21.11+ */ "spread_vines", "doVinesSpread"),
    MAX_BLOCK_MODIFICATIONS(Integer.class, /* v1.21.11+ */ "max_block_modifications", "commandModificationBlockLimit"),

    @XInfo(since = "1.20.2")
    ENDER_PEARLS_VANISH_ON_DEATH(Boolean.class, /* v1.21.11+ */ "ender_pearls_vanish_on_death", "enderPearlsVanishOnDeath"),

    @XInfo(since = "1.20.3")
    PROJECTILES_CAN_BREAK_BLOCKS(Boolean.class, /* v1.21.11+ */ "projectiles_can_break_blocks", "projectilesCanBreakBlocks"),
    MAX_COMMAND_FORKS(Integer.class, /* v1.21.11+ */ "max_command_forks", "maxCommandForkCount"),
    PLAYERS_NETHER_PORTAL_DEFAULT_DELAY(Integer.class, /* v1.21.11+ */ "players_nether_portal_default_delay", "playersNetherPortalDefaultDelay"),
    PLAYERS_NETHER_PORTAL_CREATIVE_DELAY(Integer.class, /* v1.21.11+ */ "players_nether_portal_creative_delay", "playersNetherPortalCreativeDelay"),

    @XInfo(since = "1.20.5", removedSince = "1.21.9")
    SPAWN_CHUNK_RADIUS(Integer.class, "spawnChunkRadius"),

    @XInfo(since = "1.21.2")
    PLAYER_MOVEMENT_CHECK(Boolean.class, /* v1.21.11+ */ "player_movement_check", "disablePlayerMovementCheck"),
    MAX_MINECART_SPEED(Integer.class, /* v1.21.11+ */ "max_minecart_speed", "minecartMaxSpeed"),

    @XInfo(since = "1.21.5")
    TNT_EXPLODES(Boolean.class, /* v1.21.11+ */ "tnt_explodes", "tntExplodes"),

    @XInfo(since = "1.21.5", removedSince = "1.21.11")
    ALLOW_FIRE_TICKS_AWAY_FROM_PLAYER(Boolean.class, "allowFireTicksAwayFromPlayer"),

    @XInfo(since = "1.21.6")
    LOCATOR_BAR(Boolean.class, /* v1.21.11+ */ "locator_bar", "locatorBar"),

    @XInfo(since = "1.21.9")
    PVP(Boolean.class, "pvp"),
    ALLOW_ENTERING_NETHER_USING_PORTALS(Boolean.class, /* v1.21.11+ */ "allow_entering_nether_using_portals", "allowEnteringNetherUsingPortals"),
    SPAWN_MONSTERS(Boolean.class, /* v1.21.11+ */ "spawn_monsters", "spawnMonsters"),
    COMMAND_BLOCKS_WORK(Boolean.class, /* v1.21.11+ */ "command_blocks_work", "commandBlocksEnabled"),
    SPAWNER_BLOCKS_WORK(Boolean.class, /* v1.21.11+ */ "spawner_blocks_work", "spawnerBlocksEnabled"),

    @XInfo(since = "1.21.11")
    FIRE_SPREAD_RADIUS_AROUND_PLAYER(Integer.class, "fire_spread_radius_around_player"),

    @XInfo(since = "?", removedSince = "1.21.11")
    DO_FIRE_TICK(Boolean.class, "doFireTick");

    private static final boolean SUPPORTS_GameRule;
    private static final MethodHandle GameRule_getByName;
    private static final MethodHandle World_getGameRuleValue;
    private static final MethodHandle World_setGameRuleValue;

    static {
        boolean supportsGameRuleAPI = true;
        MethodHandle getByName = null;
        MethodHandle getGameRuleValue = null;
        MethodHandle setGameRuleValue = null;

        MethodHandles.Lookup methodHandles = MethodHandles.lookup();
        try {
            getByName = methodHandles.findStatic(GameRule.class, "getByName", MethodType.methodType(GameRule.class, String.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoClassDefFoundError e) {
            supportsGameRuleAPI = false;

            try {
                getGameRuleValue = methodHandles.findVirtual(World.class, "getGameRuleValue", MethodType.methodType(String.class, String.class));
                setGameRuleValue = methodHandles.findVirtual(World.class, "setGameRuleValue", MethodType.methodType(boolean.class, String.class, String.class));
            } catch (NoSuchMethodException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
        SUPPORTS_GameRule = supportsGameRuleAPI;
        GameRule_getByName = getByName;
        World_getGameRuleValue = getGameRuleValue;
        World_setGameRuleValue = setGameRuleValue;
    }

    private final String[] names;
    private final Class<?> type;
    private final String value;
    private Object rule;

    XGameRule(@NotNull Class<?> type, @NotNull String... names) {
        this.names = names;
        this.type = type;
        World world = Bukkit.getWorlds().get(0);
        this.value = Arrays.stream(names).filter(world::isGameRule).findAny().orElse(null);
    }

    // Can't access static fields from enum constructor
    static {
        if (SUPPORTS_GameRule) {
            try {
                for (XGameRule xGameRule : XGameRule.values())
                    if (xGameRule.value != null)
                        xGameRule.rule = GameRule_getByName.invoke(xGameRule.value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String[] getNames() {
        return this.names;
    }

    @Override
    public @Nullable String get() {
        return this.value;
    }

    @Override
    public boolean isSupported() {
        return XBase.super.isSupported() && (!SUPPORTS_GameRule || this.rule != null);
    }

    /**
     * Returns the type of this game rule (either {@link Boolean}, {@link Integer} or {@link String}).
     * 
     * @return The type of this game rule
     */
    public @NotNull Class<?> getType() {
        return this.type;
    }

    /**
     * Returns the value of this game rule.
     * 
     * @param world The world from which the value should be fetched
     * @return The value of this game rule
     * @param <T> The type of this game rule's value
     * @throws UnsupportedOperationException if {@link #isSupported()} is {@code false}
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getValue(@NotNull World world) {
        if (!isSupported())
            throw new UnsupportedOperationException("Game rule not supported on this version!");

        if (SUPPORTS_GameRule) {
            GameRule<?> rule = (GameRule<?>) this.rule;
            return (T) world.getGameRuleValue(rule);
        } else {
            String value;
            try {
                value = (String) World_getGameRuleValue.invoke(world, this.value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            if (this.type == Boolean.class)
                return (T) (Object) Boolean.parseBoolean(value);
            else if (this.type == Integer.class)
                return (T) (Object) Integer.parseInt(value);
            else
                return (T) value;
        }
    }

    /**
     * Set a game rule's value
     * 
     * @param world The world in which this game rule should be updated
     * @param value The new value
     * @return {@code true} if successful
     * @param <T> The type of this game rule's value
     * @throws UnsupportedOperationException if {@link #isSupported()} is {@code false}
     */
    public <T> boolean setValue(@NotNull World world, @NotNull T value) {
        if (!isSupported())
            throw new UnsupportedOperationException("Game rule not supported on this version!");

        if (SUPPORTS_GameRule) {
            @SuppressWarnings("unchecked")
            GameRule<T> rule = (GameRule<T>) this.rule;
            return world.setGameRule(rule, value);
        } else {
            try {
                return (boolean) World_setGameRuleValue.invoke(world, this.value, value.toString());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
