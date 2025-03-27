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

import com.cryptomorin.xseries.base.XModule;
import com.cryptomorin.xseries.base.XRegistry;
import com.cryptomorin.xseries.base.annotations.XInfo;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * <b>XBiome</b> - Cross-version support for biome names.<br>
 * Biomes: https://minecraft.wiki/w/Biome
 * Biome: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/Biome.html
 * <p>
 * The ordering of this enum class matters and should not be changed due to
 * <a href="https://minecraft.wiki/w/Java_Edition_1.18">1.18 removed biomes issue.</a>
 *
 * @author Crypto Morin
 * @version 7.0.0
 * @see Biome
 */
public final class XBiome extends XModule<XBiome, Biome> {
    public static final XRegistry<XBiome, Biome> REGISTRY =
            new XRegistry<>(Biome.class, XBiome.class, () -> Registry.BIOME, XBiome::new, XBiome[]::new);

    public static final XBiome
            WINDSWEPT_HILLS = std("WINDSWEPT_HILLS", "MOUNTAINS", "EXTREME_HILLS"),
            SNOWY_PLAINS = std("SNOWY_PLAINS", "SNOWY_TUNDRA", "ICE_FLATS", "ICE_PLAINS"),
            SPARSE_JUNGLE = std("SPARSE_JUNGLE", "JUNGLE_EDGE", "JUNGLE_EDGE"),
            STONY_SHORE = std("STONY_SHORE", "STONE_SHORE", "STONE_BEACH"),
            CHERRY_GROVE = std("CHERRY_GROVE"),
            PALE_GARDEN = std("PALE_GARDEN"),
            OLD_GROWTH_PINE_TAIGA = std("OLD_GROWTH_PINE_TAIGA", "GIANT_TREE_TAIGA", "REDWOOD_TAIGA", "MEGA_TAIGA"),
            WINDSWEPT_FOREST = std("WINDSWEPT_FOREST", "WOODED_MOUNTAINS", "EXTREME_HILLS_WITH_TREES", "EXTREME_HILLS_PLUS"),
            WOODED_BADLANDS = std("WOODED_BADLANDS", "WOODED_BADLANDS_PLATEAU", "MESA_ROCK", "MESA_PLATEAU_FOREST"),
            WINDSWEPT_GRAVELLY_HILLS = std("WINDSWEPT_GRAVELLY_HILLS", "GRAVELLY_MOUNTAINS", "MUTATED_EXTREME_HILLS", "EXTREME_HILLS_MOUNTAINS"),
            OLD_GROWTH_BIRCH_FOREST = std("OLD_GROWTH_BIRCH_FOREST", "TALL_BIRCH_FOREST", "MUTATED_BIRCH_FOREST", "BIRCH_FOREST_MOUNTAINS"),
            OLD_GROWTH_SPRUCE_TAIGA = std("OLD_GROWTH_SPRUCE_TAIGA", "GIANT_SPRUCE_TAIGA", "MUTATED_REDWOOD_TAIGA", "MEGA_SPRUCE_TAIGA"),
            WINDSWEPT_SAVANNA = std("WINDSWEPT_SAVANNA", "SHATTERED_SAVANNA", "MUTATED_SAVANNA", "SAVANNA_MOUNTAINS"),
            MEADOW = std("MEADOW"),
            MANGROVE_SWAMP = std("MANGROVE_SWAMP"),
            DEEP_DARK = std("DEEP_DARK"),
            GROVE = std("GROVE"),
            SNOWY_SLOPES = std("SNOWY_SLOPES"),
            FROZEN_PEAKS = std("FROZEN_PEAKS"),
            JAGGED_PEAKS = std("JAGGED_PEAKS"),
            STONY_PEAKS = std("STONY_PEAKS"),
            BADLANDS = std("BADLANDS", "MESA"),
            BADLANDS_PLATEAU = std(WOODED_BADLANDS, "BADLANDS_PLATEAU", "MESA_CLEAR_ROCK", "MESA_PLATEAU"),
            BEACH = std("BEACH", "BEACHES"),
            BIRCH_FOREST = std(OLD_GROWTH_BIRCH_FOREST, "BIRCH_FOREST"),
            BIRCH_FOREST_HILLS = std(OLD_GROWTH_BIRCH_FOREST, "BIRCH_FOREST_HILLS"),
            COLD_OCEAN = std("COLD_OCEAN"),
            DARK_FOREST = std("DARK_FOREST", "ROOFED_FOREST"),
            DARK_FOREST_HILLS = std("DARK_FOREST_HILLS", "MUTATED_ROOFED_FOREST", "ROOFED_FOREST_MOUNTAINS"),
            DEEP_COLD_OCEAN = std("DEEP_COLD_OCEAN", "COLD_DEEP_OCEAN"),
            DEEP_FROZEN_OCEAN = std("DEEP_FROZEN_OCEAN", "FROZEN_DEEP_OCEAN"),
            DEEP_LUKEWARM_OCEAN = std("DEEP_LUKEWARM_OCEAN", "LUKEWARM_DEEP_OCEAN"),
            DEEP_OCEAN = std("DEEP_OCEAN"),
            DEEP_WARM_OCEAN = std("DEEP_WARM_OCEAN", "WARM_DEEP_OCEAN"),
            DESERT = std("DESERT"),
            DESERT_HILLS = std("DESERT_HILLS"),
            DESERT_LAKES = std("DESERT_LAKES", "MUTATED_DESERT", "DESERT_MOUNTAINS"),
            END_BARRENS = std(World.Environment.THE_END, "END_BARRENS", "SKY_ISLAND_BARREN"),
            END_HIGHLANDS = std(World.Environment.THE_END, "END_HIGHLANDS", "SKY_ISLAND_HIGH"),
            END_MIDLANDS = std(World.Environment.THE_END, "END_MIDLANDS", "SKY_ISLAND_MEDIUM"),
            ERODED_BADLANDS = std("ERODED_BADLANDS", "MUTATED_MESA", "MESA_BRYCE"),
            FLOWER_FOREST = std("FLOWER_FOREST", "MUTATED_FOREST"),
            FOREST = std("FOREST"),
            FROZEN_OCEAN = std("FROZEN_OCEAN"),
            FROZEN_RIVER = std("FROZEN_RIVER"),
            GIANT_SPRUCE_TAIGA = std(OLD_GROWTH_SPRUCE_TAIGA, "GIANT_SPRUCE_TAIGA", "MUTATED_REDWOOD_TAIGA", "MEGA_SPRUCE_TAIGA"),
            GIANT_SPRUCE_TAIGA_HILLS = std(OLD_GROWTH_SPRUCE_TAIGA, "GIANT_SPRUCE_TAIGA_HILLS", "MUTATED_REDWOOD_TAIGA_HILLS", "MEGA_SPRUCE_TAIGA_HILLS"),
            GIANT_TREE_TAIGA = std(OLD_GROWTH_PINE_TAIGA, "GIANT_TREE_TAIGA", "REDWOOD_TAIGA", "MEGA_TAIGA"),
            GIANT_TREE_TAIGA_HILLS = std(OLD_GROWTH_PINE_TAIGA, "GIANT_TREE_TAIGA_HILLS", "REDWOOD_TAIGA_HILLS", "MEGA_TAIGA_HILLS"),
            ICE_SPIKES = std("ICE_SPIKES", "MUTATED_ICE_FLATS", "ICE_PLAINS_SPIKES"),
            JUNGLE = std("JUNGLE"),
            JUNGLE_HILLS = std("JUNGLE_HILLS"),
            LUKEWARM_OCEAN = std("LUKEWARM_OCEAN"),
            MODIFIED_BADLANDS_PLATEAU = std(WOODED_BADLANDS, "MODIFIED_BADLANDS_PLATEAU", "MUTATED_MESA_CLEAR_ROCK", "MESA_PLATEAU"),
            MODIFIED_GRAVELLY_MOUNTAINS = std(WINDSWEPT_GRAVELLY_HILLS, "MODIFIED_GRAVELLY_MOUNTAINS", "MUTATED_EXTREME_HILLS_WITH_TREES", "EXTREME_HILLS_MOUNTAINS"),
            MODIFIED_JUNGLE = std("MODIFIED_JUNGLE", "MUTATED_JUNGLE", "JUNGLE_MOUNTAINS"),
            MODIFIED_JUNGLE_EDGE = std(SPARSE_JUNGLE, "MODIFIED_JUNGLE_EDGE", "MUTATED_JUNGLE_EDGE", "JUNGLE_EDGE_MOUNTAINS"),
            MODIFIED_WOODED_BADLANDS_PLATEAU = std(WOODED_BADLANDS, "MODIFIED_WOODED_BADLANDS_PLATEAU", "MUTATED_MESA_ROCK", "MESA_PLATEAU_FOREST_MOUNTAINS"),
            MOUNTAIN_EDGE = std(SPARSE_JUNGLE, "MOUNTAIN_EDGE", "SMALLER_EXTREME_HILLS"),
            MUSHROOM_FIELDS = std("MUSHROOM_FIELDS", "MUSHROOM_ISLAND"),
            MUSHROOM_FIELD_SHORE = std(STONY_SHORE, "MUSHROOM_FIELD_SHORE", "MUSHROOM_ISLAND_SHORE", "MUSHROOM_SHORE"),
            SOUL_SAND_VALLEY = std(World.Environment.NETHER, "SOUL_SAND_VALLEY"),
            CRIMSON_FOREST = std(World.Environment.NETHER, "CRIMSON_FOREST"),
            WARPED_FOREST = std(World.Environment.NETHER, "WARPED_FOREST"),
            BASALT_DELTAS = std(World.Environment.NETHER, "BASALT_DELTAS"),
            NETHER_WASTES = std(World.Environment.NETHER, "NETHER_WASTES", "NETHER", "HELL"),
            OCEAN = std("OCEAN"),
            PLAINS = std("PLAINS"),
            RIVER = std("RIVER"),
            SAVANNA = std("SAVANNA"),
            SAVANNA_PLATEAU = std(WINDSWEPT_SAVANNA, "SAVANNA_ROCK", "SAVANNA_PLATEAU"),
            SHATTERED_SAVANNA_PLATEAU = std(WINDSWEPT_SAVANNA, "SHATTERED_SAVANNA_PLATEAU", "MUTATED_SAVANNA_ROCK", "SAVANNA_PLATEAU_MOUNTAINS"),
            SMALL_END_ISLANDS = std(World.Environment.THE_END, "SMALL_END_ISLANDS", "SKY_ISLAND_LOW"),
            SNOWY_BEACH = std("SNOWY_BEACH", "COLD_BEACH"),
            SNOWY_MOUNTAINS = std(WINDSWEPT_HILLS, "SNOWY_MOUNTAINS", "ICE_MOUNTAINS"),
            SNOWY_TAIGA = std("SNOWY_TAIGA", "TAIGA_COLD", "COLD_TAIGA"),
            SNOWY_TAIGA_HILLS = std("SNOWY_TAIGA_HILLS", "TAIGA_COLD_HILLS", "COLD_TAIGA_HILLS"),
            SNOWY_TAIGA_MOUNTAINS = std(WINDSWEPT_FOREST, "SNOWY_TAIGA_MOUNTAINS", "MUTATED_TAIGA_COLD", "COLD_TAIGA_MOUNTAINS"),
            SUNFLOWER_PLAINS = std("SUNFLOWER_PLAINS", "MUTATED_PLAINS"),
            SWAMP = std("SWAMP", "SWAMPLAND"),
            SWAMP_HILLS = std("SWAMP_HILLS", "MUTATED_SWAMPLAND", "SWAMPLAND_MOUNTAINS"),
            TAIGA = std("TAIGA"),
            TAIGA_HILLS = std("TAIGA_HILLS"),
            TAIGA_MOUNTAINS = std(WINDSWEPT_FOREST, "TAIGA_MOUNTAINS", "MUTATED_TAIGA");

    /**
     * Why add it in the first place if it was removed in a few builds?
     */
    @XInfo(since = "1.21.3", removedSince = "1.21.3")
    public static final XBiome CUSTOM = std("CUSTOM");

    /**
     * Removed from 1.18
     */
    public static final XBiome
            TALL_BIRCH_FOREST = std(OLD_GROWTH_BIRCH_FOREST, "TALL_BIRCH_FOREST", "MUTATED_BIRCH_FOREST", "BIRCH_FOREST_MOUNTAINS");

    /**
     * Removed from 1.18
     */
    public static final XBiome
            TALL_BIRCH_HILLS = std(OLD_GROWTH_BIRCH_FOREST, "TALL_BIRCH_HILLS", "MUTATED_BIRCH_FOREST_HILLS", "MESA_PLATEAU_FOREST_MOUNTAINS");

    public static final XBiome
            THE_END = std(World.Environment.THE_END, "THE_END", "SKY"),
            THE_VOID = std("THE_VOID", "VOID"),
            WARM_OCEAN = std("WARM_OCEAN"),
            WOODED_BADLANDS_PLATEAU = std("WOODED_BADLANDS_PLATEAU", "MESA_ROCK", "MESA_PLATEAU_FOREST"),
            WOODED_HILLS = std("WOODED_HILLS", "FOREST_HILLS"),
            WOODED_MOUNTAINS = std("WOODED_MOUNTAINS", "EXTREME_HILLS_WITH_TREES", "EXTREME_HILLS_PLUS"),
            BAMBOO_JUNGLE = std("BAMBOO_JUNGLE"),
            BAMBOO_JUNGLE_HILLS = std("BAMBOO_JUNGLE_HILLS"),
            DRIPSTONE_CAVES = std("DRIPSTONE_CAVES"),
            LUSH_CAVES = std("LUSH_CAVES");

    private static final boolean World_getMaxHeight$SUPPORTED, World_getMinHeight$SUPPORTED;

    static {
        boolean maxHeight = false, minHeight = false;
        try {
            // Around v1.16.0
            World.class.getMethod("getMaxHeight");
            maxHeight = true;
        } catch (Exception ignored) {
        }
        try {
            // Around v1.17.0
            World.class.getMethod("getMinHeight");
            minHeight = true;
        } catch (Exception ignored) {
        }
        World_getMaxHeight$SUPPORTED = maxHeight;
        World_getMinHeight$SUPPORTED = minHeight;
    }

    @Nullable
    private final World.Environment environment;

    public XBiome(World.Environment environment, Biome biome, String[] names) {
        super(biome, names);
        this.environment = environment;
    }

    private XBiome(Biome biome, String[] names) {
        this(null, biome, names);
    }

    static {
        REGISTRY.discardMetadata();
    }

    /**
     * Gets the environment (world type) which this biome originally belongs to.
     * If the biome is non-standard (registered by another program) this will return {@link Optional#empty()}.
     *
     * @return the environment that this biome belongs to.
     * @since 4.0.0
     */
    public Optional<World.Environment> getEnvironment() {
        return Optional.ofNullable(environment);
    }

    /**
     * Parses the XBiome as a {@link Biome} based on the server version.
     *
     * @return the vanilla biome.
     * @since 1.0.0
     * @deprecated use {@link #get()} instead.
     */
    @Nullable
    @Deprecated
    public Biome getBiome() {
        return get();
    }

    /**
     * Sets the biome of the chunk.
     * If the chunk is not generated/loaded already, it'll be generated and loaded.
     * Note that this doesn't send any update packets to the nearby clients.
     *
     * @param chunk the chunk to change the biome.
     * @return the async task handling this operation.
     * @since 1.0.0
     */
    @NotNull
    public CompletableFuture<Void> setBiome(@NotNull Chunk chunk) {
        Biome biome = get();
        Objects.requireNonNull(biome, () -> "Unsupported biome: " + this.name());
        Objects.requireNonNull(chunk, "Cannot set biome of null chunk");
        if (!chunk.isLoaded() && !chunk.load(true)) {
            throw new IllegalStateException("Could not load chunk at " + chunk.getX() + ", " + chunk.getZ());
        }
        int heightMax = World_getMaxHeight$SUPPORTED ? chunk.getWorld().getMaxHeight() : 1;
        int heightMin = World_getMinHeight$SUPPORTED ? chunk.getWorld().getMinHeight() : 0;

        // Apparently setBiome is thread-safe.
        return CompletableFuture.runAsync(() -> {
            for (int x = 0; x < 16; x++) {
                // y loop for 1.16+ support (vertical biomes).
                // As of now increasing it by 4 seems to work.
                // This should be the minimal size of the vertical biomes.
                for (int y = heightMin; y < heightMax; y += 4) {
                    for (int z = 0; z < 16; z++) {
                        Block block = chunk.getBlock(x, y, z);
                        if (block.getBiome() != biome) block.setBiome(biome);
                    }
                }
            }
        }).exceptionally((result) -> {
            result.printStackTrace();
            return null;
        });
    }

    /**
     * Change the biome in the selected region.
     * Unloaded chunks will be ignored.
     * Note that this doesn't send any update packets to the nearby clients.
     *
     * @param start the start position.
     * @param end   the end position.
     * @since 1.0.0
     */
    @NotNull
    public CompletableFuture<Void> setBiome(@NotNull Location start, @NotNull Location end) {
        Biome biome = get();

        Objects.requireNonNull(start, "Start location cannot be null");
        Objects.requireNonNull(end, "End location cannot be null");
        Objects.requireNonNull(biome, () -> "Unsupported biome: " + this.name());

        World world = start.getWorld(); // Avoid getting from weak reference in a loop.
        if (!world.getUID().equals(end.getWorld().getUID()))
            throw new IllegalArgumentException("Location worlds mismatch");
        int heightMax = World_getMaxHeight$SUPPORTED ? world.getMaxHeight() : 1;
        int heightMin = World_getMinHeight$SUPPORTED ? world.getMinHeight() : 0;

        // Apparently setBiome is thread-safe.
        return CompletableFuture.runAsync(() -> {
            for (int x = start.getBlockX(); x < end.getBlockX(); x++) {
                // y loop for 1.16+ support (vertical biomes).
                // As of now increasing it by 4 seems to work.
                // This should be the minimal size of the vertical biomes.
                for (int y = heightMin; y < heightMax; y += 4) {
                    for (int z = start.getBlockZ(); z < end.getBlockZ(); z++) {
                        Block block = new Location(world, x, y, z).getBlock();
                        if (block.getBiome() != biome) block.setBiome(biome);
                    }
                }
            }
        }).exceptionally((result) -> {
            result.printStackTrace();
            return null;
        });
    }


    @NotNull
    public static XBiome of(@NotNull Biome biome) {
        return REGISTRY.getByBukkitForm(biome);
    }

    public static Optional<XBiome> of(@NotNull String biome) {
        return REGISTRY.getByName(biome);
    }

    /**
     * @deprecated Use {@link #getValues()} instead.
     */
    @Deprecated
    public static XBiome[] values() {
        return REGISTRY.values();
    }

    @NotNull
    @Unmodifiable
    public static Collection<XBiome> getValues() {
        return REGISTRY.getValues();
    }

    @NotNull
    private static XBiome std(@NotNull World.Environment environment, @NotNull String... names) {
        return REGISTRY.std(bukkit -> new XBiome(environment, bukkit, names), names);
    }

    @NotNull
    private static XBiome std(@Nullable XBiome newVersion, @NotNull String... names) {
        return REGISTRY.std(bukkit -> new XBiome(null, bukkit, names), newVersion, names);
    }

    @NotNull
    private static XBiome std(@NotNull String... names) {
        return REGISTRY.std(bukkit -> new XBiome(World.Environment.NORMAL, bukkit, names), names);
    }
}