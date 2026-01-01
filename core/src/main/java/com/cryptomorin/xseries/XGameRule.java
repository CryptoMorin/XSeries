/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2026 Crypto Morin
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
import org.bukkit.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;

/**
 * The Bukkit {@link GameRule} class itself was added around Minecraft v1.13
 * so it's recommended to use the {@link #getValue(World)} and {@link #setValue(World, Object)}
 * of this class instead of interacting with {@link GameRule} object directly or the methods
 * provided in {@link World} as they've been changed.
 *
 * @param <T> The type of the value that this gamerule requires.
 * @author Almighty-Satan
 */
@SuppressWarnings("JavaLangInvokeHandleSignature")
public final class XGameRule<T> implements XBase<XGameRule<T>, String> {

    private static final boolean SUPPORTS_GameRule;
    private static final MethodHandle GameRule_getByName;
    private static final MethodHandle World_getGameRuleValue;
    private static final MethodHandle World_setGameRuleValue;

    static {
        boolean supportsGameRuleClass = true;
        MethodHandle getByName = null;
        MethodHandle getGameRuleValue = null;
        MethodHandle setGameRuleValue = null;

        MethodHandles.Lookup methodHandles = MethodHandles.lookup();
        try {
            getByName = methodHandles.findStatic(GameRule.class, "getByName", MethodType.methodType(GameRule.class, String.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (NoClassDefFoundError e) {
            supportsGameRuleClass = false;

            try {
                getGameRuleValue = methodHandles.findVirtual(World.class, "getGameRuleValue", MethodType.methodType(String.class, String.class));
                setGameRuleValue = methodHandles.findVirtual(World.class, "setGameRuleValue", MethodType.methodType(boolean.class, String.class, String.class));
            } catch (NoSuchMethodException | IllegalAccessException ex) {
                throw new IllegalStateException("Game rules are not supported by your server", ex);
            }
        }
        SUPPORTS_GameRule = supportsGameRuleClass;
        GameRule_getByName = getByName;
        World_getGameRuleValue = getGameRuleValue;
        World_setGameRuleValue = setGameRuleValue;
    }

    /**
     * Whether command blocks should notify admins when they perform commands.
     * Controls if command block executions are broadcast to ops in chat.
     */
    public static final XGameRule<Boolean> COMMAND_BLOCK_OUTPUT = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "command_block_output", "commandBlockOutput");

    /**
     * Whether the daylight cycle and moon phases progress.
     * Controls if time advances with day/night cycle.
     */
    public static final XGameRule<Boolean> ADVANCE_TIME = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "advance_time", "doDaylightCycle");

    /**
     * Whether non-mob entities (like boats, minecarts, items from item frames) drop items when broken.
     */
    public static final XGameRule<Boolean> ENTITY_DROPS = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "entity_drops", "doEntityDrops");

    /**
     * Whether mobs drop their loot (items and experience) when killed.
     */
    public static final XGameRule<Boolean> MOB_DROPS = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "mob_drops", "doMobLoot");

    /**
     * Whether mobs spawn naturally.
     * Does not affect spawners or structure spawns.
     */
    public static final XGameRule<Boolean> SPAWN_MOBS = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "spawn_mobs", "doMobSpawning");

    /**
     * Whether blocks drop items when broken (not by explosions or creative mode).
     */
    public static final XGameRule<Boolean> BLOCK_DROPS = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "block_drops", "doTileDrops");

    /**
     * Whether players keep their inventory and experience after death.
     */
    public static final XGameRule<Boolean> KEEP_INVENTORY = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "keep_inventory", "keepInventory");

    /**
     * Whether admin commands (like /give, /tp) are logged to the server log.
     */
    public static final XGameRule<Boolean> LOG_ADMIN_COMMANDS = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "log_admin_commands", "logAdminCommands");

    /**
     * Whether mobs can grief the world (e.g., creepers explode blocks, endermen pick up blocks).
     */
    public static final XGameRule<Boolean> MOB_GRIEFING = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "mob_griefing", "mobGriefing");

    /**
     * Whether players naturally regenerate health when hunger is full.
     */
    public static final XGameRule<Boolean> NATURAL_HEALTH_REGENERATION = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "natural_health_regeneration", "naturalRegeneration");

    /**
     * Whether reduced debug screen info is shown (hides coordinates, etc.).
     */
    public static final XGameRule<Boolean> REDUCED_DEBUG_INFO = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "reduced_debug_info", "reducedDebugInfo");

    /**
     * Whether command feedback (success/failure messages) is sent to the player.
     */
    public static final XGameRule<Boolean> SEND_COMMAND_FEEDBACK = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "send_command_feedback", "sendCommandFeedback");

    /**
     * Whether death messages are shown in chat.
     */
    public static final XGameRule<Boolean> SHOW_DEATH_MESSAGES = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "show_death_messages", "showDeathMessages");

    /**
     * Controls the rate of random block ticks (crop growth, leaf decay, etc.).
     * Default is 3; higher values make processes faster.
     */
    public static final XGameRule<Integer> RANDOM_TICK_SPEED = new XGameRule<>(Integer.class, /* v1.21.11+ */ "random_tick_speed", "randomTickSpeed");

    /**
     * Whether spectators generate new chunks when moving around.
     */
    @XInfo(since = "1.9")
    public static final XGameRule<Boolean> SPECTATORS_GENERATE_CHUNKS = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "spectators_generate_chunks", "spectatorsGenerateChunks");

    /**
     * Whether the server checks for elytra flight cheating (speed/movement validation).
     * Set to false to disable the check.
     */
    @XInfo(since = "1.9")
    public static final XGameRule<Boolean> ELYTRA_MOVEMENT_CHECK = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "elytra_movement_check", "disableElytraMovementCheck");

    /**
     * The radius around world spawn where players respawn if no bed.
     */
    @XInfo(since = "1.9")
    public static final XGameRule<Integer> RESPAWN_RADIUS = new XGameRule<>(Integer.class, /* v1.21.11+ */ "respawn_radius", "spawnRadius");

    /**
     * Whether weather cycles (rain, thunder) progress naturally.
     */
    @XInfo(since = "1.11")
    public static final XGameRule<Boolean> ADVANCE_WEATHER = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "advance_weather", "doWeatherCycle");

    /**
     * Maximum number of entities that can be crammed in one block before taking suffocation damage.
     */
    @XInfo(since = "1.11")
    public static final XGameRule<Integer> MAX_ENTITY_CRAMMING = new XGameRule<>(Integer.class, /* v1.21.11+ */ "max_entity_cramming", "maxEntityCramming");

    /**
     * Whether crafting recipes require the exact arrangement as discovered in the recipe book.
     */
    @XInfo(since = "1.12")
    public static final XGameRule<Boolean> LIMITED_CRAFTING = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "limited_crafting", "doLimitedCrafting");

    /**
     * Whether advancement completion messages are announced in chat.
     */
    @XInfo(since = "1.12")
    public static final XGameRule<Boolean> SHOW_ADVANCEMENT_MESSAGES = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "show_advancement_messages", "announceAdvancements");

    /**
     * Maximum length of a command chain (function or command block chain).
     */
    @XInfo(since = "1.12")
    public static final XGameRule<Integer> MAX_COMMAND_SEQUENCE_LENGTH = new XGameRule<>(Integer.class, /* v1.21.11+ */ "max_command_sequence_length", "maxCommandChainLength");

    /**
     * The function that runs every game tick.
     */
    @XInfo(since = "1.12", removedSince = "1.13")
    public static final XGameRule<String> GAME_LOOP_FUNCTION = new XGameRule<>(String.class, "gameLoopFunction");

    /**
     * Whether raids can occur (inverted: true disables raids).
     */
    @XInfo(since = "1.14.3")
    public static final XGameRule<Boolean> RAIDS = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "raids", "disableRaids");

    /**
     * Whether phantoms spawn from insomnia.
     */
    @XInfo(since = "1.15")
    public static final XGameRule<Boolean> SPAWN_PHANTOMS = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "spawn_phantoms", "doInsomnia");

    /**
     * Whether players respawn immediately without the death screen.
     */
    @XInfo(since = "1.15")
    public static final XGameRule<Boolean> IMMEDIATE_RESPAWN = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "immediate_respawn", "doImmediateRespawn");

    /**
     * Whether players take drowning damage.
     */
    @XInfo(since = "1.15")
    public static final XGameRule<Boolean> DROWNING_DAMAGE = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "drowning_damage", "drowningDamage");

    /**
     * Whether players take fall damage.
     */
    @XInfo(since = "1.15")
    public static final XGameRule<Boolean> FALL_DAMAGE = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "fall_damage", "fallDamage");

    /**
     * Whether players take fire/lava damage.
     */
    @XInfo(since = "1.15")
    public static final XGameRule<Boolean> FIRE_DAMAGE = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "fire_damage", "fireDamage");

    /**
     * Whether pillager patrols spawn naturally.
     */
    @XInfo(since = "1.15.2")
    public static final XGameRule<Boolean> SPAWN_PATROLS = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "spawn_patrols", "doPatrolSpawning");

    /**
     * Whether wandering traders spawn naturally.
     */
    @XInfo(since = "1.15.2")
    public static final XGameRule<Boolean> SPAWN_WANDERING_TRADERS = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "spawn_wandering_traders", "doTraderSpawning");

    /**
     * Whether angered neutral mobs forgive players who die and respawn.
     */
    @XInfo(since = "1.16.1")
    public static final XGameRule<Boolean> FORGIVE_DEAD_PLAYERS = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "forgive_dead_players", "forgiveDeadPlayers");

    /**
     * Whether angered neutral mobs attack any nearby player (universal anger).
     */
    @XInfo(since = "1.16.1")
    public static final XGameRule<Boolean> UNIVERSAL_ANGER = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "universal_anger", "universalAnger");

    /**
     * Whether players take freeze damage in powder snow.
     */
    @XInfo(since = "1.17")
    public static final XGameRule<Boolean> FREEZE_DAMAGE = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "freeze_damage", "freezeDamage");

    /**
     * Percentage of players that must sleep to skip the night.
     */
    @XInfo(since = "1.17")
    public static final XGameRule<Integer> PLAYERS_SLEEPING_PERCENTAGE = new XGameRule<>(Integer.class, /* v1.21.11+ */ "players_sleeping_percentage", "playersSleepingPercentage");

    /**
     * Whether wardens spawn naturally in ancient cities.
     */
    @XInfo(since = "1.19")
    public static final XGameRule<Boolean> SPAWN_WARDENS = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "spawn_wardens", "doWardenSpawning");

    /**
     * Whether block explosion drops have decay (chance to not drop based on distance).
     */
    @XInfo(since = "1.19.3")
    public static final XGameRule<Boolean> BLOCK_EXPLOSION_DROP_DECAY = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "block_explosion_drop_decay", "blockExplosionDropDecay");

    /**
     * Whether mob (creeper/ghast) explosion drops have decay.
     */
    @XInfo(since = "1.19.3")
    public static final XGameRule<Boolean> MOB_EXPLOSION_DROP_DECAY = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "mob_explosion_drop_decay", "mobExplosionDropDecay");

    /**
     * Whether TNT explosion drops have decay.
     */
    @XInfo(since = "1.19.3")
    public static final XGameRule<Boolean> TNT_EXPLOSION_DROP_DECAY = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "tnt_explosion_drop_decay", "tntExplosionDropDecay");

    /**
     * Whether water source blocks convert to flowing water under certain conditions.
     */
    @XInfo(since = "1.19.3")
    public static final XGameRule<Boolean> WATER_SOURCE_CONVERSION = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "water_source_conversion", "waterSourceConversion");

    /**
     * Whether lava source blocks convert to flowing lava under certain conditions.
     */
    @XInfo(since = "1.19.3")
    public static final XGameRule<Boolean> LAVA_SOURCE_CONVERSION = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "lava_source_conversion", "lavaSourceConversion");

    /**
     * Whether certain sound events are heard globally by all players.
     */
    @XInfo(since = "1.19.3")
    public static final XGameRule<Boolean> GLOBAL_SOUND_EVENTS = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "global_sound_events", "globalSoundEvents");

    /**
     * Maximum height snow can accumulate in a single block.
     */
    @XInfo(since = "1.19.3")
    public static final XGameRule<Integer> MAX_SNOW_ACCUMULATION_HEIGHT = new XGameRule<>(Integer.class, /* v1.21.11+ */ "max_snow_accumulation_height", "snowAccumulationHeight");

    /**
     * Whether vines spread to adjacent blocks.
     */
    @XInfo(since = "1.19.4")
    public static final XGameRule<Boolean> SPREAD_VINES = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "spread_vines", "doVinesSpread");

    /**
     * Maximum number of block modifications allowed in a single command (anti-grief).
     */
    @XInfo(since = "1.19.4")
    public static final XGameRule<Integer> MAX_BLOCK_MODIFICATIONS = new XGameRule<>(Integer.class, /* v1.21.11+ */ "max_block_modifications", "commandModificationBlockLimit");

    /**
     * Whether ender pearls disappear when the player dies.
     */
    @XInfo(since = "1.20.2")
    public static final XGameRule<Boolean> ENDER_PEARLS_VANISH_ON_DEATH = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "ender_pearls_vanish_on_death", "enderPearlsVanishOnDeath");

    /**
     * Whether projectiles (arrows, tridents, etc.) can break certain blocks.
     */
    @XInfo(since = "1.20.3")
    public static final XGameRule<Boolean> PROJECTILES_CAN_BREAK_BLOCKS = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "projectiles_can_break_blocks", "projectilesCanBreakBlocks");

    /**
     * Maximum number of command forks allowed.
     */
    @XInfo(since = "1.20.3")
    public static final XGameRule<Integer> MAX_COMMAND_FORKS = new XGameRule<>(Integer.class, /* v1.21.11+ */ "max_command_forks", "maxCommandForkCount");

    /**
     * Default delay (in ticks) for players entering Nether portals in survival/adventure.
     */
    @XInfo(since = "1.20.3")
    public static final XGameRule<Integer> PLAYERS_NETHER_PORTAL_DEFAULT_DELAY = new XGameRule<>(Integer.class, /* v1.21.11+ */ "players_nether_portal_default_delay", "playersNetherPortalDefaultDelay");

    /**
     * Delay (in ticks) for players in creative mode entering Nether portals.
     */
    @XInfo(since = "1.20.3")
    public static final XGameRule<Integer> PLAYERS_NETHER_PORTAL_CREATIVE_DELAY = new XGameRule<>(Integer.class, /* v1.21.11+ */ "players_nether_portal_creative_delay", "playersNetherPortalCreativeDelay");

    /**
     * Radius of spawn chunks that are always loaded.
     */
    @XInfo(since = "1.20.5", removedSince = "1.21.9")
    public static final XGameRule<Integer> SPAWN_CHUNK_RADIUS = new XGameRule<>(Integer.class, "spawnChunkRadius");

    /**
     * Whether the server performs player movement validation checks.
     */
    @XInfo(since = "1.21.2")
    public static final XGameRule<Boolean> PLAYER_MOVEMENT_CHECK = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "player_movement_check", "disablePlayerMovementCheck");

    /**
     * Maximum speed of minecarts.
     */
    @XInfo(since = "1.21.2")
    public static final XGameRule<Integer> MAX_MINECART_SPEED = new XGameRule<>(Integer.class, /* v1.21.11+ */ "max_minecart_speed", "minecartMaxSpeed");

    /**
     * Whether primed TNT explodes.
     */
    @XInfo(since = "1.21.5")
    public static final XGameRule<Boolean> TNT_EXPLODES = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "tnt_explodes", "tntExplodes");

    /**
     * Whether fire continues to tick/update when far from players.
     */
    @XInfo(since = "1.21.5", removedSince = "1.21.11")
    public static final XGameRule<Boolean> ALLOW_FIRE_TICKS_AWAY_FROM_PLAYER = new XGameRule<>(Boolean.class, "allowFireTicksAwayFromPlayer");

    /**
     * Whether the locator bar (on maps?) is shown.
     */
    @XInfo(since = "1.21.6")
    public static final XGameRule<Boolean> LOCATOR_BAR = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "locator_bar", "locatorBar");

    /**
     * Whether PvP (player vs player damage) is enabled.
     */
    @XInfo(since = "1.21.9")
    public static final XGameRule<Boolean> PVP = new XGameRule<>(Boolean.class, "pvp");

    /**
     * Whether players can enter the Nether using portals.
     */
    @XInfo(since = "1.21.9")
    public static final XGameRule<Boolean> ALLOW_ENTERING_NETHER_USING_PORTALS = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "allow_entering_nether_using_portals", "allowEnteringNetherUsingPortals");

    /**
     * Whether hostile monsters spawn naturally.
     */
    @XInfo(since = "1.21.9")
    public static final XGameRule<Boolean> SPAWN_MONSTERS = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "spawn_monsters", "spawnMonsters");

    /**
     * Whether command blocks are enabled and can execute commands.
     */
    @XInfo(since = "1.21.9")
    public static final XGameRule<Boolean> COMMAND_BLOCKS_WORK = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "command_blocks_work", "commandBlocksEnabled");

    /**
     * Whether monster spawners work and spawn mobs.
     */
    @XInfo(since = "1.21.9")
    public static final XGameRule<Boolean> SPAWNER_BLOCKS_WORK = new XGameRule<>(Boolean.class, /* v1.21.11+ */ "spawner_blocks_work", "spawnerBlocksEnabled");

    /**
     * Radius around players where fire spreads faster or is allowed.
     */
    @XInfo(since = "1.21.11")
    public static final XGameRule<Integer> FIRE_SPREAD_RADIUS_AROUND_PLAYER = new XGameRule<>(Integer.class, "fire_spread_radius_around_player");

    /**
     * Legacy rule: Whether fire spreads and burns blocks.
     * Removed in 1.21.11 (replaced by newer fire rules).
     *
     * @see #FIRE_SPREAD_RADIUS_AROUND_PLAYER
     * @see #ALLOW_FIRE_TICKS_AWAY_FROM_PLAYER
     */
    @XInfo(since = ".", removedSince = "1.21.11")
    public static final XGameRule<Boolean> DO_FIRE_TICK = new XGameRule<>(Boolean.class, "doFireTick");

    private final String[] names;
    private final Class<?> type;
    @Nullable private final String usableName;
    @Nullable private final Object gamerule;

    @SuppressWarnings("deprecation")
    private XGameRule(@NotNull Class<T> type, @NotNull String... names) {
        this.names = names;
        this.type = type;

        if (Data.SUPPORTS_Registry_GAME_RULE) {
            GameRule<?> gameRule = Arrays.stream(names)
                    .map(Data::getGameRule)
                    .filter(Objects::nonNull)
                    .findAny().orElse(null);
            this.gamerule = gameRule;

            if (gameRule != null) {
                NamespacedKey key = gameRule.getKeyOrNull();
                if (key == null)
                    throw new IllegalStateException("Game rule " + gameRule + " of " + this + " has no key");
                this.usableName = key.getKey();
            } else {
                this.usableName = null;
            }

            if (gameRule != null && type != gameRule.getType()) {
                new IllegalStateException("Game rule type mismatch: "
                        + this + " (" + type + ") != "
                        + gameRule + " (" + gameRule.getType() + ')').printStackTrace();
            }
        } else if (SUPPORTS_GameRule) {
            GameRule<?> gameRule = Arrays.stream(names)
                    .map(x -> {
                        try {
                            return (GameRule<?>) GameRule_getByName.invokeExact(x);
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(Objects::nonNull)
                    .findAny().orElse(null);
            this.gamerule = gameRule;
            this.usableName = gameRule.getName();
        } else {
            World world = Bukkit.getWorlds().get(0);
            this.usableName = Arrays.stream(names).filter(world::isGameRule).findAny().orElse(null);
            this.gamerule = null;
        }

        if (usableName != null) Data.NAME_MAPPING.put(usableName.toLowerCase(Locale.ENGLISH), this);
        if (gamerule != null) Data.OBJECT_MAPPING.put((GameRule<?>) gamerule, this);
        Arrays.stream(names).forEach(x -> Data.NAME_MAPPING.put(x.toLowerCase(Locale.ENGLISH), this));
    }

    private static final class Data {
        private static final boolean SUPPORTS_Registry_GAME_RULE;
        private static final Map<String, XGameRule<?>> NAME_MAPPING = new HashMap<>();
        private static final Map<GameRule<?>, XGameRule<?>> OBJECT_MAPPING = new HashMap<>();

        static {
            boolean supported = true;

            try {
                NamespacedKey.class.getName();
                Registry.class.getName();
            } catch (NoClassDefFoundError ex) {
                supported = false;
            }

            if (supported) {
                try {
                    Registry.class.getDeclaredField("GAME_RULE");
                } catch (NoSuchFieldException ex) {
                    supported = false;
                }
            }

            SUPPORTS_Registry_GAME_RULE = supported;
        }

        @Nullable
        private static GameRule<?> getGameRule(String name) {
            NamespacedKey key;
            try {
                key = NamespacedKey.minecraft(name);
            } catch (IllegalArgumentException invalidKeyEx) {
                return null;
            }
            return Registry.GAME_RULE.get(key);
        }
    }

    @Override
    public @NotNull String name() {
        return this.names[0];
    }

    @Override
    public String[] getNames() {
        return this.names;
    }

    @Override
    public @Nullable String get() {
        return this.usableName;
    }

    @Override
    public boolean isSupported() {
        return XBase.super.isSupported() && (!SUPPORTS_GameRule || this.gamerule != null);
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
     * @throws UnsupportedOperationException if {@link #isSupported()} is {@code false}
     */
    @SuppressWarnings("unchecked")
    public @Nullable T getValue(@NotNull World world) {
        Objects.requireNonNull(world, "World is null");

        if (!isSupported())
            throw new UnsupportedOperationException("Game rule not supported on this version!");

        if (SUPPORTS_GameRule) {
            GameRule<T> rule = (GameRule<T>) this.gamerule;
            return world.getGameRuleValue(rule);
        } else {
            String value;
            try {
                value = (String) World_getGameRuleValue.invokeExact(world, this.usableName);
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
     * @throws UnsupportedOperationException if {@link #isSupported()} is {@code false}
     * @throws IllegalArgumentException if {@code value} parameter is not an instance of {@link #getType()}
     */
    public boolean setValue(@NotNull World world, @NotNull T value) {
        Objects.requireNonNull(world, "World is null");
        Objects.requireNonNull(world, "Value is null");

        if (!isSupported())
            throw new UnsupportedOperationException("Game rule not supported on this version!");

        if (!this.type.isInstance(value))
            throw new IllegalArgumentException("Invalid type for GameRule " + name() + ": " + value + " (" + value.getClass() + ") expected " + type.getName());

        if (SUPPORTS_GameRule) {
            @SuppressWarnings("unchecked")
            GameRule<T> rule = (GameRule<T>) this.gamerule;
            return world.setGameRule(rule, value);
        } else {
            try {
                return (boolean) World_setGameRuleValue.invokeExact(world, this.usableName, value.toString());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String toString() {
        return "XGameRule(" + this.usableName + ')';
    }

    @SuppressWarnings("unchecked")
    public static <T> XGameRule<T> of(@NotNull GameRule<T> bukkit) {
        return (XGameRule<T>) Data.OBJECT_MAPPING.get(bukkit);
    }

    public static Optional<XGameRule<?>> of(@NotNull String bukkit) {
        return Optional.ofNullable(Data.NAME_MAPPING.get(bukkit.toLowerCase(Locale.ENGLISH)));
    }

    @NotNull
    @Unmodifiable
    public static Collection<XGameRule<?>> getValues() {
        return Collections.unmodifiableCollection(Data.OBJECT_MAPPING.values());
    }
}
