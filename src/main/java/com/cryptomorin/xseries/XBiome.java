/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Crypto Morin
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

import com.google.common.base.Enums;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * <b>XBiome</b> - Cross-version support for biome names.<br>
 * Biomes: https://minecraft.gamepedia.com/Biome
 * Biome: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/Biome.html
 * <p>
 * The ordering of this enum class matters and should not be changed due to
 * <a href="https://minecraft.fandom.com/wiki/Java_Edition_1.18">1.18 removed biomes issue.</a>
 *
 * @author Crypto Morin
 * @version 6.1.1
 * @see Biome
 */
public enum XBiome {
    WINDSWEPT_HILLS("MOUNTAINS", "EXTREME_HILLS"),
    SNOWY_PLAINS("SNOWY_TUNDRA", "ICE_FLATS", "ICE_PLAINS"),
    SPARSE_JUNGLE("JUNGLE_EDGE", "JUNGLE_EDGE"),
    STONY_SHORE("STONE_SHORE", "STONE_BEACH"),
    CHERRY_GROVE,
    OLD_GROWTH_PINE_TAIGA("GIANT_TREE_TAIGA", "REDWOOD_TAIGA", "MEGA_TAIGA"),
    WINDSWEPT_FOREST("WOODED_MOUNTAINS", "EXTREME_HILLS_WITH_TREES", "EXTREME_HILLS_PLUS"),
    WOODED_BADLANDS("WOODED_BADLANDS_PLATEAU", "MESA_ROCK", "MESA_PLATEAU_FOREST"),
    WINDSWEPT_GRAVELLY_HILLS("GRAVELLY_MOUNTAINS", "MUTATED_EXTREME_HILLS", "EXTREME_HILLS_MOUNTAINS"),
    OLD_GROWTH_BIRCH_FOREST("TALL_BIRCH_FOREST", "MUTATED_BIRCH_FOREST", "BIRCH_FOREST_MOUNTAINS"),
    OLD_GROWTH_SPRUCE_TAIGA("GIANT_SPRUCE_TAIGA", "MUTATED_REDWOOD_TAIGA", "MEGA_SPRUCE_TAIGA"),
    WINDSWEPT_SAVANNA("SHATTERED_SAVANNA", "MUTATED_SAVANNA", "SAVANNA_MOUNTAINS"),
    MEADOW,
    MANGROVE_SWAMP,
    DEEP_DARK,
    GROVE,
    SNOWY_SLOPES,
    FROZEN_PEAKS,
    JAGGED_PEAKS,
    STONY_PEAKS,
    CUSTOM,
    BADLANDS("MESA"),
    BADLANDS_PLATEAU(WOODED_BADLANDS, "MESA_CLEAR_ROCK", "MESA_PLATEAU"),
    BEACH("BEACHES"),
    BIRCH_FOREST(OLD_GROWTH_BIRCH_FOREST, "BIRCH_FOREST"),
    BIRCH_FOREST_HILLS(OLD_GROWTH_BIRCH_FOREST, "BIRCH_FOREST_HILLS"),
    COLD_OCEAN("COLD_OCEAN"),
    DARK_FOREST("ROOFED_FOREST"),
    DARK_FOREST_HILLS("MUTATED_ROOFED_FOREST", "ROOFED_FOREST_MOUNTAINS"),
    DEEP_COLD_OCEAN("COLD_DEEP_OCEAN"),
    DEEP_FROZEN_OCEAN("FROZEN_DEEP_OCEAN"),
    DEEP_LUKEWARM_OCEAN("LUKEWARM_DEEP_OCEAN"),
    DEEP_OCEAN("DEEP_OCEAN"),
    DEEP_WARM_OCEAN("WARM_DEEP_OCEAN"),
    DESERT("DESERT"),
    DESERT_HILLS("DESERT_HILLS"),
    DESERT_LAKES("MUTATED_DESERT", "DESERT_MOUNTAINS"),
    END_BARRENS(World.Environment.THE_END, "SKY_ISLAND_BARREN"),
    END_HIGHLANDS(World.Environment.THE_END, "SKY_ISLAND_HIGH"),
    END_MIDLANDS(World.Environment.THE_END, "SKY_ISLAND_MEDIUM"),
    ERODED_BADLANDS("MUTATED_MESA", "MESA_BRYCE"),
    FLOWER_FOREST("MUTATED_FOREST"),
    FOREST("FOREST"),
    FROZEN_OCEAN("FROZEN_OCEAN"),
    FROZEN_RIVER("FROZEN_RIVER"),
    GIANT_SPRUCE_TAIGA(OLD_GROWTH_SPRUCE_TAIGA, "MUTATED_REDWOOD_TAIGA", "MEGA_SPRUCE_TAIGA"),
    GIANT_SPRUCE_TAIGA_HILLS(OLD_GROWTH_SPRUCE_TAIGA, "MUTATED_REDWOOD_TAIGA_HILLS", "MEGA_SPRUCE_TAIGA_HILLS"),
    GIANT_TREE_TAIGA(OLD_GROWTH_PINE_TAIGA, "REDWOOD_TAIGA", "MEGA_TAIGA"),
    GIANT_TREE_TAIGA_HILLS(OLD_GROWTH_PINE_TAIGA, "REDWOOD_TAIGA_HILLS", "MEGA_TAIGA_HILLS"),
    ICE_SPIKES("MUTATED_ICE_FLATS", "ICE_PLAINS_SPIKES"),
    JUNGLE("JUNGLE"),
    JUNGLE_HILLS("JUNGLE_HILLS"),
    LUKEWARM_OCEAN("LUKEWARM_OCEAN"),
    MODIFIED_BADLANDS_PLATEAU(WOODED_BADLANDS, "MUTATED_MESA_CLEAR_ROCK", "MESA_PLATEAU"),
    MODIFIED_GRAVELLY_MOUNTAINS(WINDSWEPT_GRAVELLY_HILLS, "MUTATED_EXTREME_HILLS_WITH_TREES", "EXTREME_HILLS_MOUNTAINS"),
    MODIFIED_JUNGLE("MUTATED_JUNGLE", "JUNGLE_MOUNTAINS"),
    MODIFIED_JUNGLE_EDGE(SPARSE_JUNGLE, "MUTATED_JUNGLE_EDGE", "JUNGLE_EDGE_MOUNTAINS"),
    MODIFIED_WOODED_BADLANDS_PLATEAU(WOODED_BADLANDS, "MUTATED_MESA_ROCK", "MESA_PLATEAU_FOREST_MOUNTAINS"),
    MOUNTAIN_EDGE(SPARSE_JUNGLE, "SMALLER_EXTREME_HILLS"),
    MUSHROOM_FIELDS("MUSHROOM_ISLAND"),
    MUSHROOM_FIELD_SHORE(STONY_SHORE, "MUSHROOM_ISLAND_SHORE", "MUSHROOM_SHORE"),
    SOUL_SAND_VALLEY(World.Environment.NETHER),
    CRIMSON_FOREST(World.Environment.NETHER),
    WARPED_FOREST(World.Environment.NETHER),
    BASALT_DELTAS(World.Environment.NETHER),
    NETHER_WASTES(World.Environment.NETHER, "NETHER", "HELL"),
    OCEAN("OCEAN"),
    PLAINS("PLAINS"),
    RIVER("RIVER"),
    SAVANNA("SAVANNA"),
    SAVANNA_PLATEAU(WINDSWEPT_SAVANNA, "SAVANNA_ROCK", "SAVANNA_PLATEAU"),
    SHATTERED_SAVANNA_PLATEAU(WINDSWEPT_SAVANNA, "MUTATED_SAVANNA_ROCK", "SAVANNA_PLATEAU_MOUNTAINS"),
    SMALL_END_ISLANDS(World.Environment.THE_END, "SKY_ISLAND_LOW"),
    SNOWY_BEACH("COLD_BEACH"),
    SNOWY_MOUNTAINS(WINDSWEPT_HILLS, "ICE_MOUNTAINS"),
    SNOWY_TAIGA("TAIGA_COLD", "COLD_TAIGA"),
    SNOWY_TAIGA_HILLS("TAIGA_COLD_HILLS", "COLD_TAIGA_HILLS"),
    SNOWY_TAIGA_MOUNTAINS(WINDSWEPT_FOREST, "MUTATED_TAIGA_COLD", "COLD_TAIGA_MOUNTAINS"),
    SUNFLOWER_PLAINS("MUTATED_PLAINS"),
    SWAMP("SWAMPLAND"),
    SWAMP_HILLS("MUTATED_SWAMPLAND", "SWAMPLAND_MOUNTAINS"),
    TAIGA("TAIGA"),
    TAIGA_HILLS("TAIGA_HILLS"),
    TAIGA_MOUNTAINS(WINDSWEPT_FOREST, "MUTATED_TAIGA"),
    /**
     * Removed from 1.18
     */
    TALL_BIRCH_FOREST(OLD_GROWTH_BIRCH_FOREST, "MUTATED_BIRCH_FOREST", "BIRCH_FOREST_MOUNTAINS"),
    /**
     * Removed from 1.18
     */
    TALL_BIRCH_HILLS(OLD_GROWTH_BIRCH_FOREST, "MUTATED_BIRCH_FOREST_HILLS", "MESA_PLATEAU_FOREST_MOUNTAINS"),
    THE_END(World.Environment.THE_END, "SKY"),
    THE_VOID("VOID"),
    WARM_OCEAN("WARM_OCEAN"),
    WOODED_BADLANDS_PLATEAU("MESA_ROCK", "MESA_PLATEAU_FOREST"),
    WOODED_HILLS("FOREST_HILLS"),
    WOODED_MOUNTAINS("EXTREME_HILLS_WITH_TREES", "EXTREME_HILLS_PLUS"),
    BAMBOO_JUNGLE,
    BAMBOO_JUNGLE_HILLS,
    DRIPSTONE_CAVES,
    LUSH_CAVES;

    /**
     * A cached unmodifiable list of {@link XBiome#values()} to avoid allocating memory for
     *
     * @since 1.0.0
     */
    public static final XBiome[] VALUES = values();
    private static final boolean HORIZONTAL_SUPPORT = XMaterial.supports(16), EXTENDED_MINIMUM = XMaterial.supports(17);
    @Nullable
    private final Biome biome;
    @Nonnull
    private final World.Environment environment;

    XBiome(@Nonnull World.Environment environment, @Nonnull String... legacies) {
        this(environment, null, legacies);
    }

    XBiome(@Nonnull String... legacies) {
        this(World.Environment.NORMAL, legacies);
    }

    XBiome(@Nullable XBiome newVersion, @Nonnull String... legacies) {
        this(World.Environment.NORMAL, newVersion, legacies);
    }

    XBiome(@Nonnull World.Environment environment, @Nullable XBiome newVersion, @Nonnull String... legacies) {
        this.environment = environment;
        Data.NAMES.put(this.name(), this);
        for (String legacy : legacies) Data.NAMES.put(legacy, this);

        Biome biome = Enums.getIfPresent(Biome.class, this.name()).orNull();
        if (biome == null) {
            if (newVersion != null) biome = newVersion.biome;
            if (biome == null) {
                for (String legacy : legacies) {
                    biome = Enums.getIfPresent(Biome.class, legacy).orNull();
                    if (biome != null) break;
                }
            }
        }
        this.biome = biome;
    }

    /**
     * Attempts to build the string like an enum name.<br>
     * Removes all the spaces, numbers and extra non-English characters. Also removes some config/in-game based strings.
     * While this method is hard to maintain, it's extremely efficient. It's approximately more than x5 times faster than
     * the normal RegEx + String Methods approach for both formatted and unformatted material names.
     *
     * @param name the biome name to format.
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
     * Parses the XBiome with the given name.
     *
     * @param biome the name of the biome.
     * @return a matched XBiome.
     * @since 1.0.0
     */
    @Nonnull
    public static Optional<XBiome> matchXBiome(@Nonnull String biome) {
        if (biome == null || biome.isEmpty())
            throw new IllegalArgumentException("Cannot match XBiome of a null or empty biome name");
        return Optional.ofNullable(Data.NAMES.get(format(biome)));
    }

    /**
     * Parses the XBiome with the given bukkit biome.
     *
     * @param biome the Bukkit biome.
     * @return a matched biome.
     * @throws IllegalArgumentException may be thrown as an unexpected exception.
     * @since 1.0.0
     */
    @Nonnull
    public static XBiome matchXBiome(@Nonnull Biome biome) {
        Objects.requireNonNull(biome, "Cannot match XBiome of a null biome");
        return Objects.requireNonNull(Data.NAMES.get(biome.name()), () -> "Unsupported biome: " + biome.name());
    }

    /**
     * Gets the environment (world type) which this biome originally belongs to.
     *
     * @return the environment that this biome belongs to.
     * @since 4.0.0
     */
    @Nonnull
    public World.Environment getEnvironment() {
        return environment;
    }

    /**
     * Parses the XBiome as a {@link Biome} based on the server version.
     *
     * @return the vanilla biome.
     * @since 1.0.0
     */
    @Nullable
    public Biome getBiome() {
        return this.biome;
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
    @Nonnull
    public CompletableFuture<Void> setBiome(@Nonnull Chunk chunk) {
        Objects.requireNonNull(biome, () -> "Unsupported biome: " + this.name());
        Objects.requireNonNull(chunk, "Cannot set biome of null chunk");
        if (!chunk.isLoaded()) {
            if (!chunk.load(true))
                throw new IllegalStateException("Could not load chunk at " + chunk.getX() + ", " + chunk.getZ());
        }
        int heightMax = HORIZONTAL_SUPPORT ? chunk.getWorld().getMaxHeight() : 1;
        int heightMin = EXTENDED_MINIMUM ? chunk.getWorld().getMinHeight() : 0;

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
    @Nonnull
    public CompletableFuture<Void> setBiome(@Nonnull Location start, @Nonnull Location end) {
        Objects.requireNonNull(start, "Start location cannot be null");
        Objects.requireNonNull(end, "End location cannot be null");
        Objects.requireNonNull(biome, () -> "Unsupported biome: " + this.name());

        World world = start.getWorld(); // Avoid getting from weak reference in a loop.
        if (!world.getUID().equals(end.getWorld().getUID()))
            throw new IllegalArgumentException("Location worlds mismatch");
        int heightMax = HORIZONTAL_SUPPORT ? world.getMaxHeight() : 1;
        int heightMin = EXTENDED_MINIMUM ? world.getMinHeight() : 0;

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

    /**
     * Used for datas that need to be accessed during enum initilization.
     *
     * @since 3.0.0
     */
    private static final class Data {
        private static final Map<String, XBiome> NAMES = new HashMap<>();
    }
}