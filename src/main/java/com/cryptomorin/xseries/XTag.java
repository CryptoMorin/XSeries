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
package com.cryptomorin.xseries;

import com.cryptomorin.xseries.base.XBase;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A class used for grouping various things together based on a common trait.
 * This class is kind of memory-heavy and should not be initialized when not needed, on the other hand
 * if you're going to use this class, it's recommended to intialize it from before, since the startup
 * may take a while and cause a single lag spike.
 * <p>
 * The main usage of this class is simply done by choosing your category and using {@link #isTagged(XBase)} or {@link #getValues()}:
 * <pre>{@code
 * boolean isTagged = XTag.CATEGORY.isTagged(element);
 * Set<T> values = XTag.CATEGORY.getValues();
 * }</pre>
 * <p>
 * You may also want to make this easier as a configurable value for users to change using {@link #stringMatcher(Collection, Collection)}:
 * <pre>{@code
 * List<Matcher<E>> compiledMatchers = XTag.stringMatcher(elements); // for compiled matchers
 * boolean anyMatched = XTag.anyMatchString(element, compiledMatchers); // or pass the elements directly
 * }</pre>
 * <p>
 * You can also use some kind of registry-based system for tags using {@link #getTag(String)}.
 * However, not all methods here follow the common XTag system, and there are a few tags that
 * are only available as a static boolean method, such as {@link #isInteractable(XMaterial)} and {@link #isItem(XMaterial)}.
 */
public final class XTag<T extends XBase<?, ?>> {
    @NotNull
    public static final XTag<XMaterial> AIR = TagBuilder.simple(XMaterial.AIR, XMaterial.CAVE_AIR, XMaterial.VOID_AIR);

    @NotNull
    public static final XTag<XMaterial> INVENTORY_NOT_DISPLAYABLE;

    /**
     * Tag representing all acacia log and bark variants
     */
    @NotNull
    public static final XTag<XMaterial> ACACIA_LOGS;
    @NotNull
    public static final XTag<XMaterial> CORAL_FANS;
    @NotNull
    public static final XTag<XMaterial> ALIVE_CORAL_BLOCKS;
    /**
     * Tag representing all dead coral non-walled fans
     */
    @NotNull
    public static final XTag<XMaterial> ALIVE_CORAL_FANS;
    /**
     * Tag representing all non-dead coral plants
     */
    @NotNull
    public static final XTag<XMaterial> ALIVE_CORAL_PLANTS;
    /**
     *
     */
    @NotNull
    public static final XTag<XMaterial> ALIVE_CORAL_WALL_FANS;
    /**
     * Tag representing all possible blocks available for animals to spawn on
     */
    @NotNull
    public static final XTag<XMaterial> ANIMALS_SPAWNABLE_ON;
    /**
     * Tag representing all variants of anvil
     */
    @NotNull
    public static final XTag<XMaterial> ANVIL;
    /**
     * Tag representing all items that can tempt axolotl
     */
    @NotNull
    public static final XTag<XMaterial> AXOLOTL_TEMPT_ITEMS;
    /**
     * Tag representing all possible blocks for axolotls to spawn on
     */
    @NotNull
    public static final XTag<XMaterial> AXOLOTLS_SPAWNABLE_ON;
    /**
     * Tag representing all possible blocks for azalea to grow on
     */
    @NotNull
    public static final XTag<XMaterial> AZALEA_GROWS_ON;
    /**
     * Tag representing all possible blocks that can be replaced by azalea
     */
    @NotNull
    public static final XTag<XMaterial> AZALEA_ROOT_REPLACEABLE;
    /**
     * Tag representing all bamboo wood variants
     */
    @NotNull
    public static final XTag<XMaterial> BAMBOO_LOGS;
    /**
     * Tag representing all possible blocks bamboo may be planted on
     */
    @NotNull
    public static final XTag<XMaterial> BAMBOO_PLANTABLE_ON;
    /**
     * Tag representing all banner blocks
     */
    @NotNull
    public static final XTag<XMaterial> BANNERS;
    /**
     * Tag representing the nether base materials
     */
    @NotNull
    public static final XTag<XMaterial> BASE_STONE_NETHER;
    /**
     * Tag representing the overworld base materials
     */
    @NotNull
    public static final XTag<XMaterial> BASE_STONE_OVERWORLD;
    /**
     * Tag representing all possible blocks that can be used as beacon base
     */
    @NotNull
    public static final XTag<XMaterial> BEACON_BASE_BLOCKS;
    /**
     * Tag representing all possible variants of bed
     */
    @NotNull
    public static final XTag<XMaterial> BEDS;
    /**
     * Tag representing all possible blocks/crops that be grown by bees
     */
    @NotNull
    public static final XTag<XMaterial> BEE_GROWABLES;
    /**
     * Tag representing all possible blocks big dripleaf may be planted on
     */
    @NotNull
    public static final XTag<XMaterial> BIG_DRIPLEAF_PLACEABLE;
    /**
     * Tag representing all birch log and bark variants
     */
    @NotNull
    public static final XTag<XMaterial> BIRCH_LOGS;
    /**
     * Tag representing all possible variants of buttons
     */
    @NotNull
    public static final XTag<XMaterial> BUTTONS;
    /**
     * Tag representing all possible variants of campfires
     */
    @NotNull
    public static final XTag<XMaterial> CAMPFIRES;
    /**
     * Tag representing all possible variants of candle cakes
     */
    @NotNull
    public static final XTag<XMaterial> CANDLE_CAKES;
    /**
     * Tag representing all possible variants of candles
     */
    @NotNull
    public static final XTag<XMaterial> CANDLES;
    /**
     * Tag representing all possible variants of carpets
     */
    @NotNull
    public static final XTag<XMaterial> CARPETS;
    /**
     * Tag representing all possible variants of cauldrons
     */
    @NotNull
    public static final XTag<XMaterial> CAULDRONS;
    /**
     * Tag representing all possible variants of cave vines
     */
    @NotNull
    public static final XTag<XMaterial> CAVE_VINES;
    /**
     * Tag representing all cherry log and bark variants
     */
    @NotNull
    public static final XTag<XMaterial> CHERRY_LOGS;
    /**
     * Tag representing all climbable blocks
     */
    @NotNull
    public static final XTag<XMaterial> CLIMBABLE;
    /**
     * Tag representing all preferred items for harvesting clusters{unused as of 1.18}
     */
    @NotNull
    public static final XTag<XMaterial> CLUSTER_MAX_HARVESTABLES;
    /**
     * Tag representing all possible variants of coal ores
     */
    @NotNull
    public static final XTag<XMaterial> COAL_ORES;
    /**
     * Tag representing all possible variants of concrete
     */
    @NotNull
    public static final XTag<XMaterial> CONCRETE;
    /**
     * Tag representing all possible variants of concrete_powder
     */
    @NotNull
    public static final XTag<XMaterial> CONCRETE_POWDER;
    /**
     * Tag representing all possible variants of copper ores
     */
    @NotNull
    public static final XTag<XMaterial> COPPER_ORES;
    /**
     * Tag representing all corals
     */
    @NotNull
    public static final XTag<XMaterial> CORALS;
    /**
     * Tag representing all crimson log and bark variants
     */
    @NotNull
    public static final XTag<XMaterial> CRIMSON_STEMS;
    /**
     * Tag representing all crops
     */
    @NotNull
    public static final XTag<XMaterial> CROPS;
    /**
     * Tag representing all possible blocks that can make crystal sounds
     */
    @NotNull
    public static final XTag<XMaterial> CRYSTAL_SOUND_BLOCKS;
    /**
     * Tag representing all dark oak log and bark variants
     */
    @NotNull
    public static final XTag<XMaterial> DARK_OAK_LOGS;
    /**
     *
     */
    @NotNull
    public static final XTag<XMaterial> DEAD_CORAL_BLOCKS;
    /**
     *
     */
    @NotNull
    public static final XTag<XMaterial> DEAD_CORAL_FANS;
    /**
     *
     */
    @NotNull
    public static final XTag<XMaterial> DEAD_CORAL_PLANTS;
    /**
     *
     */
    @NotNull
    public static final XTag<XMaterial> DEAD_CORAL_WALL_FANS;
    /**
     * Tag representing all possible blocks that may be replaced by deepslate ores
     */
    @NotNull
    public static final XTag<XMaterial> DEEPSLATE_ORE_REPLACEABLES;
    /**
     * Tag representing all possible variants of diamond ores
     */
    @NotNull
    public static final XTag<XMaterial> DIAMOND_ORES;
    /**
     * Tag representing all dirt
     */
    @NotNull
    public static final XTag<XMaterial> DIRT;
    /**
     * Tag representing all possible types of doors
     */
    @NotNull
    public static final XTag<XMaterial> DOORS;
    /**
     * Tag representing all blocks that can't be destroyed by dragons
     */
    @NotNull
    public static final XTag<XMaterial> DRAGON_IMMUNE;
    /**
     * Tag representing all possible blocks that can be replaced by dripstone
     */
    @NotNull
    public static final XTag<XMaterial> DRIPSTONE_REPLACEABLE;
    @NotNull
    public static final XTag<XMaterial> WALL_HEADS;
    /**
     * Tag representing all variants of emerald ores
     */
    @NotNull
    public static final XTag<XMaterial> EMERALD_ORES;
    /**
     * Tag representing all possible blocks that can be picked up by endermen
     */
    @NotNull
    public static final XTag<XMaterial> ENDERMAN_HOLDABLE;
    /**
     * Tag representing all blocks that cant be replaced by world generation features
     */
    @NotNull
    public static final XTag<XMaterial> FEATURES_CANNOT_REPLACE;
    /**
     * Tag representing all possible variants of fence gates
     */
    @NotNull
    public static final XTag<XMaterial> FENCE_GATES;
    /**
     * Tag representing all possible variants of fences
     */
    @NotNull
    public static final XTag<XMaterial> FENCES;
    /**
     * Tag representing all possible variants of filled cauldron
     */
    @NotNull
    public static final XTag<XMaterial> FILLED_CAULDRONS;
    /**
     * Tag representing all possible variants fire
     */
    @NotNull
    public static final XTag<XMaterial> FIRE = TagBuilder.simple(XMaterial.FIRE, XMaterial.SOUL_FIRE);
    /**
     * Tag representing all possible variants of flower pots
     */
    @NotNull
    public static final XTag<XMaterial> FLOWER_POTS;
    /**
     * Tag representing all possible types of flowers
     */
    @NotNull
    public static final XTag<XMaterial> FLOWERS;
    /**
     * Tag representing all items can be used as food for fox
     */
    @NotNull
    public static final XTag<XMaterial> FOX_FOOD;
    /**
     * Tag representing all possible blocks foxes may spawn on
     */
    @NotNull
    public static final XTag<XMaterial> FOXES_SPAWNABLE_ON;
    /**
     * Tag representing all possible items can be used to avoid freezing
     */
    @NotNull
    public static final XTag<XMaterial> FREEZE_IMMUNE_WEARABLES;
    /**
     * Tag representing all blocks that geodes will not spawn in
     */
    @NotNull
    public static final XTag<XMaterial> GEODE_INVALID_BLOCKS;
    /**
     * Tag representing all variants of glass
     */
    @NotNull
    public static final XTag<XMaterial> GLASS;
    /**
     * Tag representing all possible variants of glazed terracotta
     */
    @NotNull
    public static final XTag<XMaterial> GLAZED_TERRACOTTA;
    /**
     * Tag representing all possible blocks goats may spawn on
     */
    @NotNull
    public static final XTag<XMaterial> GOATS_SPAWNABLE_ON;
    /**
     * Tag representing all possible variants of gold ores
     */
    @NotNull
    public static final XTag<XMaterial> GOLD_ORES;
    /**
     * Tag representing all block types that are guarded by piglins
     */
    @NotNull
    public static final XTag<XMaterial> GUARDED_BY_PIGLINS;
    /**
     * Tag representing all possible types of hanging signs
     */
    @NotNull
    public static final XTag<XMaterial> HANGING_SIGNS;
    /**
     * Tag representing all block types that repel hoglins
     */
    @NotNull
    public static final XTag<XMaterial> HOGLIN_REPELLENTS;
    /**
     * Tag representing all possible variants of ice
     */
    @NotNull
    public static final XTag<XMaterial> ICE;
    /**
     * Tag representing all items ignored by baby piglins
     */
    @NotNull
    public static final XTag<XMaterial> IGNORED_BY_PIGLIN_BABIES;
    /**
     * Tag representing all possible block types that do not drip water/lava
     */
    @NotNull
    public static final XTag<XMaterial> IMPERMEABLE;
    /**
     * Tag representing all block types that can burn for infinitely long in the end
     */
    @NotNull
    public static final XTag<XMaterial> INFINIBURN_END;
    /**
     * Tag representing all block types that can burn for infinitely long in the nether
     */
    @NotNull
    public static final XTag<XMaterial> INFINIBURN_NETHER;
    /**
     * Tag representing all block types that can burn for infinitely long in the overworld
     */
    @NotNull
    public static final XTag<XMaterial> INFINIBURN_OVERWORLD;
    /**
     * Tag representing all block types that play muffled step sounds
     */
    @NotNull
    public static final XTag<XMaterial> INSIDE_STEP_SOUND_BLOCKS;
    /**
     * Tag representing all possible variants of iron ores
     */
    @NotNull
    public static final XTag<XMaterial> IRON_ORES;
    /**
     * Tag representing all possible variants of arrows
     */
    @NotNull
    public static final XTag<XMaterial> ITEMS_ARROWS;
    /**
     * Tag representing all items that can be used as banners
     */
    @NotNull
    public static final XTag<XMaterial> ITEMS_BANNERS;
    /**
     * Tag representing all items that can be used to fuel beacon
     */
    @NotNull
    public static final XTag<XMaterial> ITEMS_BEACON_PAYMENT_ITEMS;
    /**
     * Tag representing all possible variants of boats
     */
    @NotNull
    public static final XTag<XMaterial> ITEMS_BOATS;
    /**
     * Tag representing all possible variants of coal
     */
    @NotNull
    public static final XTag<XMaterial> ITEMS_COALS;
    /**
     * Tag representing all possible music discs that can be dropped by creeper
     */
    @NotNull
    public static final XTag<XMaterial> ITEMS_CREEPER_DROP_MUSIC_DISCS;
    /**
     * Tag representing all possible types of fish
     */
    @NotNull
    public static final XTag<XMaterial> ITEMS_FISHES;
    /**
     * Tag representing all furnace materials {empty in spigot as of 1.18}
     */
    @NotNull
    public static final XTag<XMaterial> ITEMS_FURNACE_MATERIALS;
    /**
     * Tag representing all possible book types that can be placed on lecterns
     */
    @NotNull
    public static final XTag<XMaterial> ITEMS_LECTERN_BOOKS;
    /**
     * Tag representing all types of music discs
     */
    @NotNull
    public static final XTag<XMaterial> ITEMS_MUSIC_DISCS;
    /**
     * Tag representing all items loved by piglins
     */
    @NotNull
    public static final XTag<XMaterial> ITEMS_PIGLIN_LOVED;
    /**
     * Tag representing all stone tool materials
     */
    @NotNull
    public static final XTag<XMaterial> ITEMS_STONE_TOOL_MATERIALS;
    /**
     * Tag representing all possible types of wall banners
     */
    @NotNull
    public static final XTag<XMaterial> WALL_BANNERS;
    /**
     * Tag representing all jungle log and bark variants
     */
    @NotNull
    public static final XTag<XMaterial> JUNGLE_LOGS;
    /**
     * Tag representing all possible variants of lapis ores
     */
    @NotNull
    public static final XTag<XMaterial> LAPIS_ORES;
    /**
     * Tag representing all blocks that can't be replaced by lava pools
     */
    @NotNull
    public static final XTag<XMaterial> LAVA_POOL_STONE_CANNOT_REPLACE;
    /**
     * Tag representing all types of leaves
     */
    @NotNull
    public static final XTag<XMaterial> LEAVES;
    /**
     * Tag representing all wood and bark variants
     */
    @NotNull
    public static final XTag<XMaterial> LOGS;
    /**
     * Tag representing all wood and bark variants that can catch fire
     */
    @NotNull
    public static final XTag<XMaterial> LOGS_THAT_BURN;
    /**
     * Tag representing all possible blocks that can be replaced by lush ground
     */
    @NotNull
    public static final XTag<XMaterial> LUSH_GROUND_REPLACEABLE;
    /**
     * Tag representing all mangrove log and bark variants
     */
    @NotNull
    public static final XTag<XMaterial> MANGROVE_LOGS;
    /**
     * Tag representing all block types mineable with axe
     */
    @NotNull
    public static final XTag<XMaterial> MINEABLE_AXE;
    /**
     * Tag representing all block types mineable with hoe
     */
    @NotNull
    public static final XTag<XMaterial> MINEABLE_HOE;
    /**
     * Tag representing all block types mineable with pickaxe
     */
    @NotNull
    public static final XTag<XMaterial> MINEABLE_PICKAXE;
    /**
     * Tag representing all block types mineable with shovel
     */
    @NotNull
    public static final XTag<XMaterial> MINEABLE_SHOVEL;
    /**
     * Tag representing all possible block types mooshrooms can spawn on
     */
    @NotNull
    public static final XTag<XMaterial> MOOSHROOMS_SPAWNABLE_ON;
    /**
     * Tag representing all block types that can be replaced by moss
     */
    @NotNull
    public static final XTag<XMaterial> MOSS_REPLACEABLE;
    @NotNull
    public static final XTag<XMaterial> MUSHROOM_GROW_BLOCK;
    /**
     * Tag representing all block types that need minimum of diamond tool to drop items
     */
    @NotNull
    public static final XTag<XMaterial> NEEDS_DIAMOND_TOOL;
    /**
     * Tag representing all block types that need minimum of iron tool to drop items
     */
    @NotNull
    public static final XTag<XMaterial> NEEDS_IRON_TOOL;
    /**
     * Tag representing all block types that need minimum of stone tool to drop items
     */
    @NotNull
    public static final XTag<XMaterial> NEEDS_STONE_TOOL;
    /**
     * Tag representing all non-flammable wood and bark variants
     */
    @NotNull
    public static final XTag<XMaterial> NON_FLAMMABLE_WOOD;
    /**
     * Tag representing all non-wooden stairs
     */
    @NotNull
    public static final XTag<XMaterial> NON_WOODEN_STAIRS;
    /**
     * Tag representing all non-wooden slabs
     */
    @NotNull
    public static final XTag<XMaterial> NON_WOODEN_SLABS;
    /**
     * Tag representing all nylium blocks
     */
    @NotNull
    public static final XTag<XMaterial> NYLIUM;
    /**
     * Tag representing all oak wood and bark variants
     */
    @NotNull
    public static final XTag<XMaterial> OAK_LOGS;
    /**
     * Tag representing all possible blocks that can block vibration signals
     */
    @NotNull
    public static final XTag<XMaterial> OCCLUDES_VIBRATION_SIGNALS;
    /**
     * Tag representing all ores
     */
    @NotNull
    public static final XTag<XMaterial> ORES;
    /**
     * Tag representing all possible block types parrots may spawn on
     */
    @NotNull
    public static final XTag<XMaterial> PARROTS_SPAWNABLE_ON;
    /**
     * Tag representing all items that can be used as piglin food
     */
    @NotNull
    public static final XTag<XMaterial> PIGLIN_FOOD;
    /**
     * Tag representing all block types that repel piglins
     */
    @NotNull
    public static final XTag<XMaterial> PIGLIN_REPELLENTS;
    /**
     * Tag representing all types of planks
     */
    @NotNull
    public static final XTag<XMaterial> PLANKS;
    /**
     * Tag representing all possible blocks polar bears may spawn on
     */
    @NotNull
    public static final XTag<XMaterial> POLAR_BEARS_SPAWNABLE_ON_IN_FROZEN_OCEAN;
    /**
     * Tag representing all possible block types that be used as portals
     */
    @NotNull
    public static final XTag<XMaterial> PORTALS = TagBuilder.simple(XMaterial.END_GATEWAY, XMaterial.END_PORTAL, XMaterial.NETHER_PORTAL);
    /**
     * Tag representing all possible pottery sherds
     */
    @NotNull
    public static final XTag<XMaterial> POTTERY_SHERDS;
    /**
     * Tag representing all possible variants of pressure plates
     */
    @NotNull
    public static final XTag<XMaterial> PRESSURE_PLATES;
    /**
     * Tag representing all block types that prevent inside mob spawning
     */
    @NotNull
    public static final XTag<XMaterial> PREVENT_MOB_SPAWNING_INSIDE;
    /**
     * Tag representing all possible block types that rabbits may spawn on
     */
    @NotNull
    public static final XTag<XMaterial> RABBITS_SPAWNABLE_ON;
    /**
     * Tag representing all possible types of rails
     */
    @NotNull
    public static final XTag<XMaterial> RAILS;
    /**
     * Tag representing all possible variants of redstone ores
     */
    @NotNull
    public static final XTag<XMaterial> REDSTONE_ORES;
    /**
     * Tag representing all plant blocks that may be replaced
     */
    @NotNull
    public static final XTag<XMaterial> REPLACEABLE_PLANTS;
    /**
     * Tag representing all possible types of sand
     */
    @NotNull
    public static final XTag<XMaterial> SAND;
    /**
     * Tag representing all possible types of saplings
     */
    @NotNull
    public static final XTag<XMaterial> SAPLINGS;
    /**
     * Tag representing all possible variants of shulker boxes
     */
    @NotNull
    public static final XTag<XMaterial> SHULKER_BOXES;
    /**
     * Tag representing all possible variants of signs
     */
    @NotNull
    public static final XTag<XMaterial> SIGNS;
    /**
     * Tag representing all possible block types small dripleaf may be placed upon
     */
    @NotNull
    public static final XTag<XMaterial> SMALL_DRIPLEAF_PLACEABLE;
    /**
     * Tag representing all flowers small in size {1 block tall}
     */
    @NotNull
    public static final XTag<XMaterial> SMALL_FLOWERS;
    /**
     * Tag representing all possible smithing templates
     */
    @NotNull
    public static final XTag<XMaterial> SMITHING_TEMPLATES;
    /**
     * Tag representing all possible variants of snow
     */
    @NotNull
    public static final XTag<XMaterial> SNOW;
    /**
     * Tag representing all possible blocks that can be lit up with sould fire
     */
    @NotNull
    public static final XTag<XMaterial> SOUL_FIRE_BASE_BLOCKS;
    /**
     * Tag representing all possible blocks that activate soul speed enchantment
     */
    @NotNull
    public static final XTag<XMaterial> SOUL_SPEED_BLOCKS;
    /**
     * Tag representing all spruce wood and log variants
     */
    @NotNull
    public static final XTag<XMaterial> SPRUCE_LOGS;
    /**
     * Tag representing all possible types of stairs
     */
    @NotNull
    public static final XTag<XMaterial> STAIRS;
    /**
     * Tag representing all possible types of standing signs
     */
    @NotNull
    public static final XTag<XMaterial> STANDING_SIGNS;
    /**
     * Tag representing all possible variants of stone bricks
     */
    @NotNull
    public static final XTag<XMaterial> STONE_BRICKS;
    /**
     * Tag representing all possible blocks that can be replaced by regular stone ores
     */
    @NotNull
    public static final XTag<XMaterial> STONE_ORE_REPLACEABLES;
    /**
     * Tag representing all pressure plates made of some type of stone
     */
    @NotNull
    public static final XTag<XMaterial> STONE_PRESSURE_PLATES;
    /**
     * Tag representing all block types that make strider warm
     */
    @NotNull
    public static final XTag<XMaterial> STRIDER_WARM_BLOCKS;
    /**
     * Tag representing all flowers that are tall {2 blocks}
     */
    @NotNull
    public static final XTag<XMaterial> TALL_FLOWERS;
    /**
     * Tag representing all possible variants of non-glazed terracotta
     */
    @NotNull
    public static final XTag<XMaterial> TERRACOTTA;
    /**
     * Tag representing all possible types of trapdoors
     */
    @NotNull
    public static final XTag<XMaterial> TRAPDOORS;
    /**
     * Tag representing all block types that can be bonemealed underwater
     */
    @NotNull
    public static final XTag<XMaterial> UNDERWATER_BONEMEALS;
    /**
     * Tag representing all blocks that have unstable bottom when placed in centre of 2 blocks
     */
    @NotNull
    public static final XTag<XMaterial> UNSTABLE_BOTTOM_CENTER;
    /**
     * Tag representing all valid mob spawn positions
     */
    @NotNull
    public static final XTag<XMaterial> VALID_SPAWN;
    /**
     * Tag representing all possible variants of wall hanging sign
     */
    @NotNull
    public static final XTag<XMaterial> WALL_HANGING_SIGNS;
    /**
     * Tag representing all possible block types that can override a wall post creation
     */
    @NotNull
    public static final XTag<XMaterial> WALL_POST_OVERRIDE;
    /**
     * Tag representing all wall signs
     */
    @NotNull
    public static final XTag<XMaterial> WALL_SIGNS;
    /**
     * Tag representing all possible types of wall torches
     */
    @NotNull
    public static final XTag<XMaterial> WALL_TORCHES;
    /**
     * Tag representing all different types of walls
     */
    @NotNull
    public static final XTag<XMaterial> WALLS;
    /**
     * Tag representing all warped stems
     */
    @NotNull
    public static final XTag<XMaterial> WARPED_STEMS;
    /**
     * Tag representing all block types that can't be destroyed by withers
     */
    @NotNull
    public static final XTag<XMaterial> WITHER_IMMUNE;
    /**
     * Tag representing all possible block types that may be used as wither summon base
     */
    @NotNull
    public static final XTag<XMaterial> WITHER_SUMMON_BASE_BLOCKS;
    /**
     * Tag representing all possible block types that wolves may spawn on
     */
    @NotNull
    public static final XTag<XMaterial> WOLVES_SPAWNABLE_ON;
    /**
     * Tag representing all possible types of wooden buttons
     */
    @NotNull
    public static final XTag<XMaterial> WOODEN_BUTTONS;
    /**
     * Tag representing all possible types of wooden doors
     */
    @NotNull
    public static final XTag<XMaterial> WOODEN_DOORS;
    /**
     * Tag representing all possible types of wooden fence gates
     */
    @NotNull
    public static final XTag<XMaterial> WOODEN_FENCE_GATES;
    /**
     * Tag representing all possible types of wooden fences
     */
    @NotNull
    public static final XTag<XMaterial> WOODEN_FENCES;
    /**
     * Tag representing all possible types of wooden pressure plates
     */
    @NotNull
    public static final XTag<XMaterial> WOODEN_PRESSURE_PLATES;
    /**
     * Tag representing all possible types of wooden slabs
     */
    @NotNull
    public static final XTag<XMaterial> WOODEN_SLABS;
    /**
     * Tag representing all possible types of wooden stairs
     */
    @NotNull
    public static final XTag<XMaterial> WOODEN_STAIRS;
    /**
     * Tag representing all possible types of wooden trapdoors
     */
    @NotNull
    public static final XTag<XMaterial> WOODEN_TRAPDOORS;
    /**
     * Tag representing all possible types of wool
     */
    @NotNull
    public static final XTag<XMaterial> WOOL;


    /**
     * Tag representing all armor pieces made of leather
     */
    @NotNull
    public static final XTag<XMaterial> LEATHER_ARMOR_PIECES;
    /**
     * Tag representing all armor pieces made of iron
     */
    @NotNull
    public static final XTag<XMaterial> IRON_ARMOR_PIECES;
    /**
     * Tag representing all armor pieces made of chains
     */
    @NotNull
    public static final XTag<XMaterial> CHAINMAIL_ARMOR_PIECES;
    /**
     * Tag representing all armor pieces made of gold
     */
    @NotNull
    public static final XTag<XMaterial> GOLDEN_ARMOR_PIECES;
    /**
     * Tag representing all armor pieces made of diamond
     */
    @NotNull
    public static final XTag<XMaterial> DIAMOND_ARMOR_PIECES;
    /**
     * Tag representing all armor pieces made of netherite
     */
    @NotNull
    public static final XTag<XMaterial> NETHERITE_ARMOR_PIECES;
    /**
     * Tag representing all armor pieces that add armor bars upon wearing
     */
    @NotNull
    public static final XTag<XMaterial> ARMOR_PIECES;
    /**
     * Tag representing all wooden tools and swords
     */
    @NotNull
    public static final XTag<XMaterial> WOODEN_TOOLS;
    /**
     * This doesn't refer to all materials that contain fluids, but pure fluids.
     */
    @NotNull
    public static final XTag<XMaterial> FLUID = TagBuilder.simple(XMaterial.LAVA, XMaterial.WATER);
    /**
     * Tag representing all stone tools and swords
     */
    @NotNull
    public static final XTag<XMaterial> STONE_TOOLS;
    /**
     * Tag representing all iron tools and swords
     */
    @NotNull
    public static final XTag<XMaterial> IRON_TOOLS;
    /**
     * Tag representing all diamond tools and swords
     */
    @NotNull
    public static final XTag<XMaterial> DIAMOND_TOOLS;
    /**
     * Tag representing all netherite tools and swords
     */
    @NotNull
    public static final XTag<XMaterial> NETHERITE_TOOLS;
    /**
     * Tag representing all blocks that could harm a player.
     */
    @NotNull
    public static final XTag<XMaterial> DANGEROUS_BLOCKS = TagBuilder.simple(
            XMaterial.MAGMA_BLOCK, XMaterial.LAVA, XMaterial.CAMPFIRE, XMaterial.FIRE, XMaterial.SOUL_FIRE
    );

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////>                                                                  <//////////////////////
    //////////////////////>                           XEnchantment                           <//////////////////////
    //////////////////////>                                                                  <//////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Tag representing all possible enchants that can be applied to all armor pieces (excluding elytras)
     */
    @NotNull
    public static final XTag<XEnchantment> ARMOR_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to helmets/turtle shells
     */
    @NotNull
    public static final XTag<XEnchantment> HELEMT_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to chestplates
     */
    @NotNull
    public static final XTag<XEnchantment> CHESTPLATE_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to leggings
     */
    @NotNull
    public static final XTag<XEnchantment> LEGGINGS_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to boots
     */
    @NotNull
    public static final XTag<XEnchantment> BOOTS_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to elytras
     */
    @NotNull
    public static final XTag<XEnchantment> ELYTRA_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to swords
     */
    @NotNull
    public static final XTag<XEnchantment> SWORD_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to axes
     */
    @NotNull
    public static final XTag<XEnchantment> AXE_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to hoes
     */
    @NotNull
    public static final XTag<XEnchantment> HOE_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to pickaxes
     */
    @NotNull
    public static final XTag<XEnchantment> PICKAXE_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to shovels
     */
    @NotNull
    public static final XTag<XEnchantment> SHOVEL_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to shears
     */
    @NotNull
    public static final XTag<XEnchantment> SHEARS_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to bows
     */
    @NotNull
    public static final XTag<XEnchantment> BOW_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to crossbows
     */
    @NotNull
    public static final XTag<XEnchantment> CROSSBOW_ENCHANTS;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////>                                                                  <//////////////////////
    //////////////////////>                           XEntityType                            <//////////////////////
    //////////////////////>                                                                  <//////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Entity types that {@link XEnchantment#SMITE} enchantment is effective against.
     * This set is unmodifiable.
     *
     * @since 1.2.0
     */
    @NotNull
    public static final XTag<XEntityType> EFFECTIVE_SMITE_ENTITIES = TagBuilder.simple(
            XEntityType.ZOMBIE, XEntityType.SKELETON, XEntityType.WITHER,
            XEntityType.BEE, XEntityType.PHANTOM, XEntityType.DROWNED, XEntityType.WITHER_SKELETON,
            XEntityType.SKELETON_HORSE, XEntityType.STRAY, XEntityType.HUSK
    );

    /**
     * Entity types that {@link XEnchantment#BANE_OF_ARTHROPODS} enchantment is effective against.
     * This set is unmodifiable.
     *
     * @since 1.2.0
     */
    @NotNull
    public static final XTag<XEntityType> EFFECTIVE_BANE_OF_ARTHROPODS_ENTITIES = TagBuilder.simple(
            XEntityType.SPIDER, XEntityType.CAVE_SPIDER, XEntityType.SILVERFISH, XEntityType.ENDERMITE
    );

    /**
     * An unmodifiable set of "bad" potion effects.
     *
     * @since 1.1.0
     */
    @NotNull
    public static final XTag<XPotion> DEBUFFS = TagBuilder.simple(
            XPotion.BAD_OMEN, XPotion.BLINDNESS, XPotion.NAUSEA, XPotion.INSTANT_DAMAGE, XPotion.HUNGER,
            XPotion.LEVITATION, XPotion.POISON, XPotion.SLOWNESS, XPotion.MINING_FATIGUE, XPotion.UNLUCK,
            XPotion.WEAKNESS, XPotion.WITHER
    );

    static { // logs
        ACACIA_LOGS = TagBuilder.simple(XMaterial.STRIPPED_ACACIA_LOG,
                XMaterial.ACACIA_LOG,
                XMaterial.ACACIA_WOOD,
                XMaterial.STRIPPED_ACACIA_WOOD);
        BIRCH_LOGS = TagBuilder.simple(XMaterial.STRIPPED_BIRCH_LOG,
                XMaterial.BIRCH_LOG,
                XMaterial.BIRCH_WOOD,
                XMaterial.STRIPPED_BIRCH_WOOD);
        DARK_OAK_LOGS = TagBuilder.simple(XMaterial.STRIPPED_DARK_OAK_LOG,
                XMaterial.DARK_OAK_LOG,
                XMaterial.DARK_OAK_WOOD,
                XMaterial.STRIPPED_DARK_OAK_WOOD);
        JUNGLE_LOGS = TagBuilder.simple(XMaterial.STRIPPED_JUNGLE_LOG,
                XMaterial.JUNGLE_LOG,
                XMaterial.JUNGLE_WOOD,
                XMaterial.STRIPPED_JUNGLE_WOOD);
        MANGROVE_LOGS = TagBuilder.simple(XMaterial.STRIPPED_MANGROVE_LOG,
                XMaterial.MANGROVE_LOG,
                XMaterial.MANGROVE_WOOD,
                XMaterial.STRIPPED_MANGROVE_WOOD);
        OAK_LOGS = TagBuilder.simple(XMaterial.STRIPPED_OAK_LOG,
                XMaterial.OAK_LOG,
                XMaterial.OAK_WOOD,
                XMaterial.STRIPPED_OAK_WOOD);
        SPRUCE_LOGS = TagBuilder.simple(XMaterial.STRIPPED_SPRUCE_LOG,
                XMaterial.SPRUCE_LOG,
                XMaterial.SPRUCE_WOOD,
                XMaterial.STRIPPED_SPRUCE_WOOD);
        CHERRY_LOGS = TagBuilder.simple(XMaterial.STRIPPED_CHERRY_LOG,
                XMaterial.CHERRY_LOG,
                XMaterial.CHERRY_WOOD,
                XMaterial.STRIPPED_CHERRY_WOOD);
        BAMBOO_LOGS = TagBuilder.simple(XMaterial.STRIPPED_BAMBOO_BLOCK,
                XMaterial.BAMBOO_BLOCK,
                XMaterial.BAMBOO_MOSAIC,
                XMaterial.BAMBOO_PLANKS);
    }

    static { // colorable
        CANDLE_CAKES = TagBuilder.simple(findAllColors("CANDLE_CAKE"));
        CANDLES = TagBuilder.simple(findAllColors("CANDLE"));
        TERRACOTTA = TagBuilder.simple(findAllColors("TERRACOTTA"));
        GLAZED_TERRACOTTA = TagBuilder.simple(findAllColors("GLAZED_TERRACOTTA"));
        SHULKER_BOXES = TagBuilder.simple(findAllColors("SHULKER_BOX"));
        CARPETS = TagBuilder.simple(findAllColors("CARPET"));
        WOOL = TagBuilder.simple(findAllColors("WOOL"));
        GLASS = TagBuilder.of(findAllColors("GLASS"))
                .inheritFrom(TagBuilder.simple(XMaterial.TINTED_GLASS))
                .build();
        ITEMS_BANNERS = TagBuilder.simple(findAllColors("BANNER"));
        WALL_BANNERS = TagBuilder.simple(findAllColors("WALL_BANNER"));
        BANNERS = TagBuilder.simple(ITEMS_BANNERS, WALL_BANNERS);
        BEDS = TagBuilder.simple(findAllColors("BED"));
        CONCRETE = TagBuilder.simple(findAllColors("CONCRETE"));
        CONCRETE_POWDER = TagBuilder.simple(findAllColors("CONCRETE_POWDER"));
    }

    static { // wooded material
        STANDING_SIGNS = TagBuilder.simple(findAllWoodTypes("SIGN"));
        WALL_SIGNS = TagBuilder.simple(findAllWoodTypes("WALL_SIGN"));
        WALL_HANGING_SIGNS = TagBuilder.simple(findAllWoodTypes("WALL_HANGING_SIGN"));
        HANGING_SIGNS = TagBuilder.simple(findAllWoodTypes("HANGING_SIGN"));
        WOODEN_PRESSURE_PLATES = TagBuilder.simple(findAllWoodTypes("PRESSURE_PLATE"));
        WOODEN_DOORS = TagBuilder.simple(findAllWoodTypes("DOOR"));
        WOODEN_FENCE_GATES = TagBuilder.simple(findAllWoodTypes("FENCE_GATE"));
        WOODEN_FENCES = TagBuilder.simple(findAllWoodTypes("FENCE"));
        WOODEN_SLABS = TagBuilder.simple(findAllWoodTypes("SLAB"));
        WOODEN_STAIRS = TagBuilder.simple(findAllWoodTypes("STAIRS"));
        WOODEN_TRAPDOORS = TagBuilder.simple(findAllWoodTypes("TRAPDOOR"));
        PLANKS = TagBuilder.simple(findAllWoodTypes("PLANKS"));
        WOODEN_BUTTONS = TagBuilder.simple(findAllWoodTypes("BUTTON"));
    }

    static { // ores
        COAL_ORES = TagBuilder.simple(XMaterial.COAL_ORE, XMaterial.DEEPSLATE_COAL_ORE);
        IRON_ORES = TagBuilder.simple(XMaterial.IRON_ORE, XMaterial.DEEPSLATE_IRON_ORE);
        COPPER_ORES = TagBuilder.simple(XMaterial.COPPER_ORE, XMaterial.DEEPSLATE_COPPER_ORE);
        REDSTONE_ORES = TagBuilder.simple(XMaterial.REDSTONE_ORE, XMaterial.DEEPSLATE_REDSTONE_ORE);
        LAPIS_ORES = TagBuilder.simple(XMaterial.LAPIS_ORE, XMaterial.DEEPSLATE_LAPIS_ORE);
        GOLD_ORES = TagBuilder.simple(XMaterial.GOLD_ORE, XMaterial.DEEPSLATE_GOLD_ORE, XMaterial.NETHER_GOLD_ORE);
        DIAMOND_ORES = TagBuilder.simple(XMaterial.DIAMOND_ORE, XMaterial.DEEPSLATE_DIAMOND_ORE);
        EMERALD_ORES = TagBuilder.simple(XMaterial.EMERALD_ORE, XMaterial.DEEPSLATE_EMERALD_ORE);

        ORES = TagBuilder
                .of(XMaterial.ANCIENT_DEBRIS, XMaterial.NETHER_QUARTZ_ORE)
                .inheritFrom(COAL_ORES, IRON_ORES, COPPER_ORES, REDSTONE_ORES, LAPIS_ORES, GOLD_ORES, DIAMOND_ORES, EMERALD_ORES)
                .build();
    }

    static { // corals
        ALIVE_CORAL_WALL_FANS = TagBuilder.simple(findAllCorals(true, false, true, true));
        ALIVE_CORAL_FANS = TagBuilder.simple(findAllCorals(true, false, true, false));
        ALIVE_CORAL_BLOCKS = TagBuilder.simple(findAllCorals(true, true, false, false));
        ALIVE_CORAL_PLANTS = TagBuilder.simple(findAllCorals(true, false, false, false));
        DEAD_CORAL_WALL_FANS = TagBuilder.simple(findAllCorals(false, false, true, true));
        DEAD_CORAL_FANS = TagBuilder.simple(findAllCorals(false, false, true, false));
        DEAD_CORAL_BLOCKS = TagBuilder.simple(findAllCorals(false, true, false, false));
        DEAD_CORAL_PLANTS = TagBuilder.simple(findAllCorals(false, false, false, false));
        CORAL_FANS = TagBuilder.simple(ALIVE_CORAL_FANS, ALIVE_CORAL_WALL_FANS, DEAD_CORAL_WALL_FANS, DEAD_CORAL_FANS);

        CORALS = TagBuilder.simple(
                ALIVE_CORAL_WALL_FANS,
                ALIVE_CORAL_FANS,
                ALIVE_CORAL_BLOCKS,
                ALIVE_CORAL_PLANTS,
                DEAD_CORAL_WALL_FANS,
                DEAD_CORAL_FANS,
                DEAD_CORAL_BLOCKS,
                DEAD_CORAL_PLANTS
        );

        /*BRAIN_CORALS = filterCorals("BRAIN");
        BUBBLE_CORALS = filterCorals("BUBBLE");
        FIRE_CORALS = filterCorals("FIRE");
        HORN_CORALS = filterCorals("HORN");
        TUBE_CORALS = filterCorals("TUBE");*/
    }

    static {
        WALL_HEADS = TagBuilder.simple(
                TagBuilder.simple(findMaterialsEndingWith("WALL_HEAD")),
                TagBuilder.simple(XMaterial.WITHER_SKELETON_WALL_SKULL, XMaterial.SKELETON_WALL_SKULL)
        );

        WALL_TORCHES = TagBuilder.simple(XMaterial.WALL_TORCH,
                XMaterial.SOUL_WALL_TORCH,
                XMaterial.REDSTONE_WALL_TORCH);
        WALLS = TagBuilder.simple(XMaterial.POLISHED_DEEPSLATE_WALL,
                XMaterial.NETHER_BRICK_WALL,
                XMaterial.POLISHED_BLACKSTONE_WALL,
                XMaterial.DEEPSLATE_BRICK_WALL,
                XMaterial.RED_SANDSTONE_WALL,
                XMaterial.BRICK_WALL,
                XMaterial.COBBLESTONE_WALL,
                XMaterial.POLISHED_BLACKSTONE_BRICK_WALL,
                XMaterial.PRISMARINE_WALL,
                XMaterial.SANDSTONE_WALL,
                XMaterial.GRANITE_WALL,
                XMaterial.DEEPSLATE_TILE_WALL,
                XMaterial.BLACKSTONE_WALL,
                XMaterial.STONE_BRICK_WALL,
                XMaterial.RED_NETHER_BRICK_WALL,
                XMaterial.DIORITE_WALL,
                XMaterial.MOSSY_COBBLESTONE_WALL,
                XMaterial.ANDESITE_WALL,
                XMaterial.MOSSY_STONE_BRICK_WALL,
                XMaterial.END_STONE_BRICK_WALL,
                XMaterial.COBBLED_DEEPSLATE_WALL);
        STONE_PRESSURE_PLATES = TagBuilder.simple(XMaterial.STONE_PRESSURE_PLATE,
                XMaterial.POLISHED_BLACKSTONE_PRESSURE_PLATE);
        RAILS = TagBuilder.simple(XMaterial.RAIL,
                XMaterial.ACTIVATOR_RAIL,
                XMaterial.DETECTOR_RAIL,
                XMaterial.POWERED_RAIL);
        ANIMALS_SPAWNABLE_ON = TagBuilder.simple(XMaterial.GRASS_BLOCK);
        ANVIL = TagBuilder.simple(XMaterial.ANVIL,
                XMaterial.CHIPPED_ANVIL,
                XMaterial.DAMAGED_ANVIL);
        AXOLOTL_TEMPT_ITEMS = TagBuilder.simple(XMaterial.TROPICAL_FISH_BUCKET);
        AXOLOTLS_SPAWNABLE_ON = TagBuilder.simple(XMaterial.CLAY);

        SNOW = TagBuilder.simple(XMaterial.SNOW_BLOCK,
                XMaterial.SNOW,
                XMaterial.POWDER_SNOW);
        SAND = TagBuilder.simple(XMaterial.SAND,
                XMaterial.RED_SAND);
        DIRT = TagBuilder.simple(XMaterial.MOSS_BLOCK,
                XMaterial.COARSE_DIRT,
                XMaterial.PODZOL,
                XMaterial.DIRT,
                XMaterial.ROOTED_DIRT,
                XMaterial.MYCELIUM,
                XMaterial.GRASS_BLOCK);
        CAVE_VINES = TagBuilder.simple(XMaterial.CAVE_VINES,
                XMaterial.CAVE_VINES_PLANT);
        BASE_STONE_NETHER = TagBuilder.simple(XMaterial.NETHERRACK,
                XMaterial.BASALT,
                XMaterial.BLACKSTONE);
        BASE_STONE_OVERWORLD = TagBuilder.simple(XMaterial.TUFF,
                XMaterial.DIORITE,
                XMaterial.DEEPSLATE,
                XMaterial.ANDESITE,
                XMaterial.GRANITE,
                XMaterial.STONE);
        BEACON_BASE_BLOCKS = TagBuilder.simple(XMaterial.NETHERITE_BLOCK,
                XMaterial.GOLD_BLOCK,
                XMaterial.IRON_BLOCK,
                XMaterial.EMERALD_BLOCK,
                XMaterial.DIAMOND_BLOCK);
        CROPS = TagBuilder.simple(
                XMaterial.CARROT, XMaterial.CARROTS, XMaterial.POTATO, XMaterial.POTATOES,
                XMaterial.NETHER_WART, XMaterial.PUMPKIN_SEEDS, XMaterial.WHEAT_SEEDS, XMaterial.WHEAT,
                XMaterial.MELON_SEEDS, XMaterial.BEETROOT_SEEDS, XMaterial.BEETROOTS, XMaterial.SUGAR_CANE,
                XMaterial.BAMBOO_SAPLING, XMaterial.BAMBOO, XMaterial.CHORUS_PLANT,
                XMaterial.KELP, XMaterial.KELP_PLANT, XMaterial.SEA_PICKLE, XMaterial.BROWN_MUSHROOM, XMaterial.RED_MUSHROOM,
                XMaterial.MELON_STEM, XMaterial.PUMPKIN_STEM, XMaterial.COCOA, XMaterial.COCOA_BEANS);
        CAMPFIRES = TagBuilder.simple(XMaterial.CAMPFIRE,
                XMaterial.SOUL_CAMPFIRE);
        FILLED_CAULDRONS = TagBuilder.simple(XMaterial.LAVA_CAULDRON,
                XMaterial.POWDER_SNOW_CAULDRON,
                XMaterial.WATER_CAULDRON);
        CAULDRONS = TagBuilder.simple(XMaterial.CAULDRON,
                XMaterial.LAVA_CAULDRON,
                XMaterial.POWDER_SNOW_CAULDRON,
                XMaterial.WATER_CAULDRON);
        CLIMBABLE = TagBuilder.of(
                XMaterial.SCAFFOLDING,
                XMaterial.WEEPING_VINES_PLANT,
                XMaterial.WEEPING_VINES,
                XMaterial.TWISTING_VINES,
                XMaterial.TWISTING_VINES_PLANT,
                XMaterial.VINE,
                XMaterial.LADDER
        ).inheritFrom(CAVE_VINES).build();
        CLUSTER_MAX_HARVESTABLES = TagBuilder.simple(XMaterial.DIAMOND_PICKAXE,
                XMaterial.GOLDEN_PICKAXE,
                XMaterial.STONE_PICKAXE,
                XMaterial.NETHERITE_PICKAXE,
                XMaterial.WOODEN_PICKAXE,
                XMaterial.IRON_PICKAXE);
        CRIMSON_STEMS = TagBuilder.simple(XMaterial.CRIMSON_HYPHAE,
                XMaterial.STRIPPED_CRIMSON_STEM,
                XMaterial.CRIMSON_STEM,
                XMaterial.STRIPPED_CRIMSON_HYPHAE);
        WARPED_STEMS = TagBuilder.simple(XMaterial.WARPED_HYPHAE,
                XMaterial.STRIPPED_WARPED_STEM,
                XMaterial.WARPED_STEM,
                XMaterial.STRIPPED_WARPED_HYPHAE);
        CRYSTAL_SOUND_BLOCKS = TagBuilder.simple(XMaterial.AMETHYST_BLOCK,
                XMaterial.BUDDING_AMETHYST);
        DEEPSLATE_ORE_REPLACEABLES = TagBuilder.simple(XMaterial.TUFF,
                XMaterial.DEEPSLATE);
        DOORS = TagBuilder.of(XMaterial.IRON_DOOR).inheritFrom(WOODEN_DOORS).build();
        WITHER_IMMUNE = TagBuilder.simple(XMaterial.STRUCTURE_BLOCK,
                XMaterial.END_GATEWAY,
                XMaterial.BEDROCK,
                XMaterial.END_PORTAL,
                XMaterial.COMMAND_BLOCK,
                XMaterial.REPEATING_COMMAND_BLOCK,
                XMaterial.MOVING_PISTON,
                XMaterial.CHAIN_COMMAND_BLOCK,
                XMaterial.BARRIER,
                XMaterial.END_PORTAL_FRAME,
                XMaterial.JIGSAW
        );
        WITHER_SUMMON_BASE_BLOCKS = TagBuilder.simple(XMaterial.SOUL_SOIL, XMaterial.SOUL_SAND);
        NYLIUM = TagBuilder.simple(XMaterial.CRIMSON_NYLIUM, XMaterial.WARPED_NYLIUM);
        SMALL_FLOWERS = TagBuilder.simple(
                XMaterial.RED_TULIP,
                XMaterial.AZURE_BLUET,
                XMaterial.OXEYE_DAISY,
                XMaterial.BLUE_ORCHID,
                XMaterial.PINK_TULIP,
                XMaterial.POPPY,
                XMaterial.WHITE_TULIP,
                XMaterial.DANDELION,
                XMaterial.ALLIUM,
                XMaterial.CORNFLOWER,
                XMaterial.ORANGE_TULIP,
                XMaterial.LILY_OF_THE_VALLEY,
                XMaterial.WITHER_ROSE
        );
        TALL_FLOWERS = TagBuilder.simple(
                XMaterial.PEONY,
                XMaterial.SUNFLOWER,
                XMaterial.LILAC,
                XMaterial.ROSE_BUSH
        );
        FEATURES_CANNOT_REPLACE = TagBuilder.simple(
                XMaterial.SPAWNER,
                XMaterial.END_PORTAL_FRAME,
                XMaterial.BEDROCK,
                XMaterial.CHEST
        );
        FENCE_GATES = TagBuilder.simple(WOODEN_FENCE_GATES);
        FENCES = TagBuilder.of(XMaterial.NETHER_BRICK_FENCE).inheritFrom(WOODEN_FENCES).build();
        FLOWER_POTS = TagBuilder.simple(
                XMaterial.POTTED_OAK_SAPLING,
                XMaterial.POTTED_WITHER_ROSE,
                XMaterial.POTTED_ACACIA_SAPLING,
                XMaterial.POTTED_LILY_OF_THE_VALLEY,
                XMaterial.POTTED_WARPED_FUNGUS,
                XMaterial.POTTED_WARPED_ROOTS,
                XMaterial.POTTED_ALLIUM,
                XMaterial.POTTED_BROWN_MUSHROOM,
                XMaterial.POTTED_WHITE_TULIP,
                XMaterial.POTTED_ORANGE_TULIP,
                XMaterial.POTTED_DANDELION,
                XMaterial.POTTED_AZURE_BLUET,
                XMaterial.POTTED_FLOWERING_AZALEA_BUSH,
                XMaterial.POTTED_PINK_TULIP,
                XMaterial.POTTED_CORNFLOWER,
                XMaterial.POTTED_CRIMSON_FUNGUS,
                XMaterial.POTTED_RED_MUSHROOM,
                XMaterial.POTTED_BLUE_ORCHID,
                XMaterial.POTTED_FERN,
                XMaterial.POTTED_POPPY,
                XMaterial.POTTED_CRIMSON_ROOTS,
                XMaterial.POTTED_RED_TULIP,
                XMaterial.POTTED_OXEYE_DAISY,
                XMaterial.POTTED_AZALEA_BUSH,
                XMaterial.POTTED_BAMBOO,
                XMaterial.POTTED_CACTUS,
                XMaterial.FLOWER_POT,
                XMaterial.POTTED_DEAD_BUSH,
                XMaterial.POTTED_DARK_OAK_SAPLING,
                XMaterial.POTTED_SPRUCE_SAPLING,
                XMaterial.POTTED_JUNGLE_SAPLING,
                XMaterial.POTTED_BIRCH_SAPLING,
                XMaterial.POTTED_MANGROVE_PROPAGULE,
                XMaterial.POTTED_CHERRY_SAPLING,
                XMaterial.POTTED_TORCHFLOWER
        );
        FOX_FOOD = TagBuilder.simple(XMaterial.GLOW_BERRIES, XMaterial.SWEET_BERRIES);
        FOXES_SPAWNABLE_ON = TagBuilder.simple(
                XMaterial.SNOW,
                XMaterial.SNOW_BLOCK,
                XMaterial.PODZOL,
                XMaterial.GRASS_BLOCK,
                XMaterial.COARSE_DIRT
        );
        FREEZE_IMMUNE_WEARABLES = TagBuilder.simple(
                XMaterial.LEATHER_BOOTS,
                XMaterial.LEATHER_CHESTPLATE,
                XMaterial.LEATHER_HELMET,
                XMaterial.LEATHER_LEGGINGS,
                XMaterial.LEATHER_HORSE_ARMOR
        );
        ICE = TagBuilder.simple(
                XMaterial.ICE,
                XMaterial.PACKED_ICE,
                XMaterial.BLUE_ICE,
                XMaterial.FROSTED_ICE
        );
        GEODE_INVALID_BLOCKS = TagBuilder.simple(
                XMaterial.BEDROCK,
                XMaterial.WATER,
                XMaterial.LAVA,
                XMaterial.ICE,
                XMaterial.PACKED_ICE,
                XMaterial.BLUE_ICE
        );
        HOGLIN_REPELLENTS = TagBuilder.simple(XMaterial.WARPED_FUNGUS,
                XMaterial.NETHER_PORTAL,
                XMaterial.POTTED_WARPED_FUNGUS,
                XMaterial.RESPAWN_ANCHOR);
        IGNORED_BY_PIGLIN_BABIES = TagBuilder.simple(XMaterial.LEATHER);
        IMPERMEABLE = TagBuilder.simple(GLASS);
        INFINIBURN_END = TagBuilder.simple(
                XMaterial.BEDROCK,
                XMaterial.NETHERRACK,
                XMaterial.MAGMA_BLOCK
        );
        INFINIBURN_NETHER = TagBuilder.simple(XMaterial.NETHERRACK, XMaterial.MAGMA_BLOCK);
        INFINIBURN_OVERWORLD = TagBuilder.simple(XMaterial.NETHERRACK, XMaterial.MAGMA_BLOCK);
        INSIDE_STEP_SOUND_BLOCKS = TagBuilder.simple(XMaterial.SNOW, XMaterial.POWDER_SNOW);
        ITEMS_ARROWS = TagBuilder.simple(XMaterial.ARROW, XMaterial.SPECTRAL_ARROW, XMaterial.TIPPED_ARROW);
        ITEMS_BEACON_PAYMENT_ITEMS = TagBuilder.simple
                (XMaterial.EMERALD,
                        XMaterial.DIAMOND,
                        XMaterial.NETHERITE_INGOT,
                        XMaterial.IRON_INGOT,
                        XMaterial.GOLD_INGOT
                );
        ITEMS_BOATS = TagBuilder.simple(
                XMaterial.OAK_BOAT,
                XMaterial.ACACIA_BOAT,
                XMaterial.DARK_OAK_BOAT,
                XMaterial.BIRCH_BOAT,
                XMaterial.SPRUCE_BOAT,
                XMaterial.JUNGLE_BOAT,
                XMaterial.MANGROVE_BOAT,
                XMaterial.CHERRY_BOAT,
                XMaterial.BAMBOO_RAFT
        );
        ITEMS_COALS = TagBuilder.simple(XMaterial.COAL, XMaterial.CHARCOAL);
        ITEMS_CREEPER_DROP_MUSIC_DISCS = TagBuilder.simple(
                XMaterial.MUSIC_DISC_BLOCKS,
                XMaterial.MUSIC_DISC_11,
                XMaterial.MUSIC_DISC_WAIT,
                XMaterial.MUSIC_DISC_MELLOHI,
                XMaterial.MUSIC_DISC_STAL,
                XMaterial.MUSIC_DISC_WARD,
                XMaterial.MUSIC_DISC_13,
                XMaterial.MUSIC_DISC_CAT,
                XMaterial.MUSIC_DISC_CHIRP,
                XMaterial.MUSIC_DISC_MALL,
                XMaterial.MUSIC_DISC_FAR,
                XMaterial.MUSIC_DISC_STRAD
        );
        ITEMS_FISHES = TagBuilder.simple(
                XMaterial.TROPICAL_FISH,
                XMaterial.SALMON,
                XMaterial.PUFFERFISH,
                XMaterial.COOKED_COD,
                XMaterial.COD,
                XMaterial.COOKED_SALMON
        );
        ITEMS_LECTERN_BOOKS = TagBuilder.simple(XMaterial.WRITABLE_BOOK, XMaterial.WRITTEN_BOOK);
        ITEMS_STONE_TOOL_MATERIALS = TagBuilder.simple(XMaterial.COBBLED_DEEPSLATE,
                XMaterial.BLACKSTONE,
                XMaterial.COBBLESTONE);
        LEAVES = TagBuilder.simple(XMaterial.SPRUCE_LEAVES,
                XMaterial.ACACIA_LEAVES,
                XMaterial.DARK_OAK_LEAVES,
                XMaterial.AZALEA_LEAVES,
                XMaterial.JUNGLE_LEAVES,
                XMaterial.FLOWERING_AZALEA_LEAVES,
                XMaterial.BIRCH_LEAVES,
                XMaterial.OAK_LEAVES,
                XMaterial.MANGROVE_LEAVES,
                XMaterial.CHERRY_LEAVES);
        NON_WOODEN_STAIRS = TagBuilder.simple(XMaterial.STONE_BRICK_STAIRS,
                XMaterial.STONE_STAIRS,
                XMaterial.POLISHED_BLACKSTONE_BRICK_STAIRS,
                XMaterial.RED_SANDSTONE_STAIRS,
                XMaterial.PRISMARINE_STAIRS,
                XMaterial.GRANITE_STAIRS,
                XMaterial.WAXED_WEATHERED_CUT_COPPER_STAIRS,
                XMaterial.POLISHED_DIORITE_STAIRS,
                XMaterial.WEATHERED_CUT_COPPER_STAIRS,
                XMaterial.NETHER_BRICK_STAIRS,
                XMaterial.RED_NETHER_BRICK_STAIRS,
                XMaterial.PRISMARINE_BRICK_STAIRS,
                XMaterial.WAXED_CUT_COPPER_STAIRS,
                XMaterial.DEEPSLATE_TILE_STAIRS,
                XMaterial.POLISHED_ANDESITE_STAIRS,
                XMaterial.SMOOTH_RED_SANDSTONE_STAIRS,
                XMaterial.PURPUR_STAIRS,
                XMaterial.POLISHED_DEEPSLATE_STAIRS,
                XMaterial.QUARTZ_STAIRS,
                XMaterial.MOSSY_COBBLESTONE_STAIRS,
                XMaterial.BRICK_STAIRS,
                XMaterial.CUT_COPPER_STAIRS,
                XMaterial.SANDSTONE_STAIRS,
                XMaterial.ANDESITE_STAIRS,
                XMaterial.WAXED_EXPOSED_CUT_COPPER_STAIRS,
                XMaterial.COBBLED_DEEPSLATE_STAIRS,
                XMaterial.COBBLESTONE_STAIRS,
                XMaterial.DEEPSLATE_BRICK_STAIRS,
                XMaterial.DIORITE_STAIRS,
                XMaterial.SMOOTH_QUARTZ_STAIRS,
                XMaterial.EXPOSED_CUT_COPPER_STAIRS,
                XMaterial.DARK_PRISMARINE_STAIRS,
                XMaterial.OXIDIZED_CUT_COPPER_STAIRS,
                XMaterial.POLISHED_BLACKSTONE_STAIRS,
                XMaterial.POLISHED_GRANITE_STAIRS,
                XMaterial.MOSSY_STONE_BRICK_STAIRS,
                XMaterial.END_STONE_BRICK_STAIRS,
                XMaterial.WAXED_OXIDIZED_CUT_COPPER_STAIRS,
                XMaterial.SMOOTH_SANDSTONE_STAIRS,
                XMaterial.BLACKSTONE_STAIRS);
        STAIRS = TagBuilder.simple(NON_WOODEN_STAIRS, WOODEN_STAIRS);
        NON_WOODEN_SLABS = TagBuilder.simple(XMaterial.MOSSY_COBBLESTONE_SLAB,
                XMaterial.EXPOSED_CUT_COPPER_SLAB,
                XMaterial.SMOOTH_QUARTZ_SLAB,
                XMaterial.COBBLESTONE_SLAB,
                XMaterial.POLISHED_BLACKSTONE_SLAB,
                XMaterial.OXIDIZED_CUT_COPPER_SLAB,
                XMaterial.POLISHED_ANDESITE_SLAB,
                XMaterial.RED_SANDSTONE_SLAB,
                XMaterial.BLACKSTONE_SLAB,
                XMaterial.STONE_SLAB,
                XMaterial.SMOOTH_SANDSTONE_SLAB,
                XMaterial.COBBLED_DEEPSLATE_SLAB,
                XMaterial.SMOOTH_RED_SANDSTONE_SLAB,
                XMaterial.POLISHED_DIORITE_SLAB,
                XMaterial.PRISMARINE_BRICK_SLAB,
                XMaterial.QUARTZ_SLAB,
                XMaterial.DIORITE_SLAB,
                XMaterial.NETHER_BRICK_SLAB,
                XMaterial.PRISMARINE_SLAB,
                XMaterial.WAXED_EXPOSED_CUT_COPPER_SLAB,
                XMaterial.RED_NETHER_BRICK_SLAB,
                XMaterial.POLISHED_BLACKSTONE_BRICK_SLAB,
                XMaterial.MOSSY_STONE_BRICK_SLAB,
                XMaterial.SMOOTH_STONE_SLAB,
                XMaterial.SANDSTONE_SLAB,
                XMaterial.WEATHERED_CUT_COPPER_SLAB,
                XMaterial.DEEPSLATE_BRICK_SLAB,
                XMaterial.POLISHED_DEEPSLATE_SLAB,
                XMaterial.GRANITE_SLAB,
                XMaterial.ANDESITE_SLAB,
                XMaterial.CUT_COPPER_SLAB,
                XMaterial.CUT_SANDSTONE_SLAB,
                XMaterial.END_STONE_BRICK_SLAB,
                XMaterial.WAXED_OXIDIZED_CUT_COPPER_SLAB,
                XMaterial.CUT_RED_SANDSTONE_SLAB,
                XMaterial.PURPUR_SLAB,
                XMaterial.STONE_BRICK_SLAB,
                XMaterial.WAXED_CUT_COPPER_SLAB,
                XMaterial.DEEPSLATE_TILE_SLAB,
                XMaterial.DARK_PRISMARINE_SLAB,
                XMaterial.PETRIFIED_OAK_SLAB,
                XMaterial.WAXED_WEATHERED_CUT_COPPER_SLAB,
                XMaterial.BRICK_SLAB,
                XMaterial.POLISHED_GRANITE_SLAB);
        POTTERY_SHERDS = TagBuilder.simple(XMaterial.ANGLER_POTTERY_SHERD,
                XMaterial.ARCHER_POTTERY_SHERD,
                XMaterial.ARMS_UP_POTTERY_SHERD,
                XMaterial.BLADE_POTTERY_SHERD,
                XMaterial.BREWER_POTTERY_SHERD,
                XMaterial.BURN_POTTERY_SHERD,
                XMaterial.DANGER_POTTERY_SHERD,
                XMaterial.EXPLORER_POTTERY_SHERD,
                XMaterial.FRIEND_POTTERY_SHERD,
                XMaterial.HEART_POTTERY_SHERD,
                XMaterial.HEARTBREAK_POTTERY_SHERD,
                XMaterial.HOWL_POTTERY_SHERD,
                XMaterial.MINER_POTTERY_SHERD,
                XMaterial.MOURNER_POTTERY_SHERD,
                XMaterial.PLENTY_POTTERY_SHERD,
                XMaterial.PRIZE_POTTERY_SHERD,
                XMaterial.SHEAF_POTTERY_SHERD,
                XMaterial.SHELTER_POTTERY_SHERD,
                XMaterial.SKULL_POTTERY_SHERD,
                XMaterial.SNORT_POTTERY_SHERD);
        SOUL_FIRE_BASE_BLOCKS = TagBuilder.simple(XMaterial.SOUL_SOIL,
                XMaterial.SOUL_SAND);
        SOUL_SPEED_BLOCKS = TagBuilder.simple(XMaterial.SOUL_SOIL,
                XMaterial.SOUL_SAND);
        STONE_ORE_REPLACEABLES = TagBuilder.simple(XMaterial.STONE,
                XMaterial.DIORITE,
                XMaterial.ANDESITE,
                XMaterial.GRANITE);
        STRIDER_WARM_BLOCKS = TagBuilder.simple(XMaterial.LAVA);
        VALID_SPAWN = TagBuilder.simple(XMaterial.PODZOL,
                XMaterial.GRASS_BLOCK);
        STONE_BRICKS = TagBuilder.simple(XMaterial.CHISELED_STONE_BRICKS,
                XMaterial.CRACKED_STONE_BRICKS,
                XMaterial.MOSSY_STONE_BRICKS,
                XMaterial.STONE_BRICKS);
        SAPLINGS = TagBuilder.simple(XMaterial.ACACIA_SAPLING,
                XMaterial.JUNGLE_SAPLING,
                XMaterial.SPRUCE_SAPLING,
                XMaterial.DARK_OAK_SAPLING,
                XMaterial.AZALEA,
                XMaterial.OAK_SAPLING,
                XMaterial.FLOWERING_AZALEA,
                XMaterial.BIRCH_SAPLING,
                XMaterial.MANGROVE_PROPAGULE,
                XMaterial.CHERRY_SAPLING);
        WOLVES_SPAWNABLE_ON = TagBuilder.simple(XMaterial.GRASS_BLOCK,
                XMaterial.SNOW,
                XMaterial.SNOW_BLOCK);
        POLAR_BEARS_SPAWNABLE_ON_IN_FROZEN_OCEAN = TagBuilder.simple(XMaterial.ICE);
        RABBITS_SPAWNABLE_ON = TagBuilder.simple(XMaterial.GRASS_BLOCK,
                XMaterial.SNOW,
                XMaterial.SNOW_BLOCK,
                XMaterial.SAND);
        PIGLIN_FOOD = TagBuilder.simple(XMaterial.COOKED_PORKCHOP,
                XMaterial.PORKCHOP);
        PIGLIN_REPELLENTS = TagBuilder.simple(XMaterial.SOUL_WALL_TORCH,
                XMaterial.SOUL_TORCH,
                XMaterial.SOUL_CAMPFIRE,
                XMaterial.SOUL_LANTERN,
                XMaterial.SOUL_FIRE);
        REPLACEABLE_PLANTS = TagBuilder.simple(XMaterial.FERN,
                XMaterial.GLOW_LICHEN,
                XMaterial.DEAD_BUSH,
                XMaterial.PEONY,
                XMaterial.TALL_GRASS,
                XMaterial.HANGING_ROOTS,
                XMaterial.VINE,
                XMaterial.SUNFLOWER,
                XMaterial.LARGE_FERN,
                XMaterial.LILAC,
                XMaterial.ROSE_BUSH,
                XMaterial.SHORT_GRASS);
        SMALL_DRIPLEAF_PLACEABLE = TagBuilder.simple(XMaterial.CLAY,
                XMaterial.MOSS_BLOCK);
        NON_FLAMMABLE_WOOD = TagBuilder.simple(XMaterial.CRIMSON_PLANKS,
                XMaterial.WARPED_WALL_SIGN,
                XMaterial.CRIMSON_FENCE_GATE,
                XMaterial.WARPED_HYPHAE,
                XMaterial.CRIMSON_HYPHAE,
                XMaterial.WARPED_STEM,
                XMaterial.WARPED_TRAPDOOR,
                XMaterial.STRIPPED_CRIMSON_HYPHAE,
                XMaterial.CRIMSON_PRESSURE_PLATE,
                XMaterial.WARPED_STAIRS,
                XMaterial.CRIMSON_SIGN,
                XMaterial.CRIMSON_STAIRS,
                XMaterial.STRIPPED_WARPED_STEM,
                XMaterial.CRIMSON_FENCE,
                XMaterial.WARPED_FENCE,
                XMaterial.CRIMSON_TRAPDOOR,
                XMaterial.STRIPPED_WARPED_HYPHAE,
                XMaterial.WARPED_DOOR,
                XMaterial.WARPED_PRESSURE_PLATE,
                XMaterial.WARPED_PLANKS,
                XMaterial.STRIPPED_CRIMSON_STEM,
                XMaterial.CRIMSON_STEM,
                XMaterial.CRIMSON_SLAB,
                XMaterial.CRIMSON_WALL_SIGN,
                XMaterial.WARPED_FENCE_GATE,
                XMaterial.WARPED_BUTTON,
                XMaterial.WARPED_SLAB,
                XMaterial.CRIMSON_DOOR,
                XMaterial.CRIMSON_BUTTON,
                XMaterial.WARPED_SIGN);
        MOOSHROOMS_SPAWNABLE_ON = TagBuilder.simple(XMaterial.MYCELIUM);

        NEEDS_STONE_TOOL = TagBuilder.simple(XMaterial.OXIDIZED_CUT_COPPER,
                XMaterial.DEEPSLATE_COPPER_ORE,
                XMaterial.EXPOSED_CUT_COPPER_SLAB,
                XMaterial.WAXED_OXIDIZED_CUT_COPPER_SLAB,
                XMaterial.WAXED_OXIDIZED_CUT_COPPER,
                XMaterial.OXIDIZED_CUT_COPPER_SLAB,
                XMaterial.WAXED_WEATHERED_CUT_COPPER,
                XMaterial.WAXED_WEATHERED_CUT_COPPER_STAIRS,
                XMaterial.WEATHERED_COPPER,
                XMaterial.WEATHERED_CUT_COPPER_STAIRS,
                XMaterial.EXPOSED_CUT_COPPER,
                XMaterial.DEEPSLATE_LAPIS_ORE,
                XMaterial.COPPER_ORE,
                XMaterial.WEATHERED_CUT_COPPER,
                XMaterial.WAXED_CUT_COPPER_STAIRS,
                XMaterial.WAXED_EXPOSED_CUT_COPPER,
                XMaterial.OXIDIZED_COPPER,
                XMaterial.WAXED_COPPER_BLOCK,
                XMaterial.RAW_IRON_BLOCK,
                XMaterial.LAPIS_BLOCK,
                XMaterial.DEEPSLATE_IRON_ORE,
                XMaterial.CUT_COPPER_STAIRS,
                XMaterial.COPPER_BLOCK,
                XMaterial.WAXED_WEATHERED_CUT_COPPER_SLAB,
                XMaterial.IRON_BLOCK,
                XMaterial.WAXED_EXPOSED_CUT_COPPER_STAIRS,
                XMaterial.RAW_COPPER_BLOCK,
                XMaterial.LAPIS_ORE,
                XMaterial.WEATHERED_CUT_COPPER_SLAB,
                XMaterial.CUT_COPPER_SLAB,
                XMaterial.IRON_ORE,
                XMaterial.EXPOSED_COPPER,
                XMaterial.WAXED_EXPOSED_COPPER,
                XMaterial.EXPOSED_CUT_COPPER_STAIRS,
                XMaterial.WAXED_CUT_COPPER_SLAB,
                XMaterial.WAXED_EXPOSED_CUT_COPPER_SLAB,
                XMaterial.OXIDIZED_CUT_COPPER_STAIRS,
                XMaterial.WAXED_OXIDIZED_COPPER,
                XMaterial.WAXED_CUT_COPPER,
                XMaterial.WAXED_WEATHERED_COPPER,
                XMaterial.LIGHTNING_ROD,
                XMaterial.WAXED_OXIDIZED_CUT_COPPER_STAIRS,
                XMaterial.CUT_COPPER);
        NEEDS_IRON_TOOL = TagBuilder.simple(XMaterial.GOLD_ORE,
                XMaterial.GOLD_BLOCK,
                XMaterial.REDSTONE_ORE,
                XMaterial.RAW_GOLD_BLOCK,
                XMaterial.EMERALD_BLOCK,
                XMaterial.DIAMOND_BLOCK,
                XMaterial.DIAMOND_ORE,
                XMaterial.DEEPSLATE_EMERALD_ORE,
                XMaterial.DEEPSLATE_GOLD_ORE,
                XMaterial.EMERALD_ORE,
                XMaterial.DEEPSLATE_REDSTONE_ORE,
                XMaterial.DEEPSLATE_DIAMOND_ORE);
        NEEDS_DIAMOND_TOOL = TagBuilder.simple(XMaterial.OBSIDIAN,
                XMaterial.NETHERITE_BLOCK,
                XMaterial.ANCIENT_DEBRIS,
                XMaterial.RESPAWN_ANCHOR,
                XMaterial.CRYING_OBSIDIAN);

        MINEABLE_PICKAXE = TagBuilder.of(
                XMaterial.OXIDIZED_CUT_COPPER,
                XMaterial.GOLD_BLOCK,
                XMaterial.SMOOTH_SANDSTONE,
                XMaterial.IRON_DOOR,
                XMaterial.COBBLESTONE,
                XMaterial.DRIPSTONE_BLOCK,
                XMaterial.CHISELED_SANDSTONE,
                XMaterial.INFESTED_STONE_BRICKS,
                XMaterial.QUARTZ_BLOCK,
                XMaterial.COPPER_BLOCK,
                XMaterial.STONE_BRICKS,
                XMaterial.CHISELED_POLISHED_BLACKSTONE,
                XMaterial.DISPENSER,
                XMaterial.DEEPSLATE_BRICKS,
                XMaterial.HEAVY_WEIGHTED_PRESSURE_PLATE,
                XMaterial.OBSIDIAN,
                XMaterial.EXPOSED_CUT_COPPER,
                XMaterial.SMOOTH_QUARTZ,
                XMaterial.SMOOTH_RED_SANDSTONE,
                XMaterial.STONE,
                XMaterial.INFESTED_COBBLESTONE,
                XMaterial.WAXED_CUT_COPPER,
                XMaterial.PRISMARINE,
                XMaterial.PISTON,
                XMaterial.CUT_COPPER,
                XMaterial.CHISELED_QUARTZ_BLOCK,
                XMaterial.MOSSY_STONE_BRICKS,
                XMaterial.EMERALD_BLOCK,
                XMaterial.BELL,
                XMaterial.AMETHYST_BLOCK,
                XMaterial.GILDED_BLACKSTONE,
                XMaterial.CHISELED_NETHER_BRICKS,
                XMaterial.WAXED_COPPER_BLOCK,
                XMaterial.IRON_BLOCK,
                XMaterial.BUDDING_AMETHYST,
                XMaterial.POLISHED_DEEPSLATE,
                XMaterial.HOPPER,
                XMaterial.CUT_RED_SANDSTONE,
                XMaterial.QUARTZ_BRICKS,
                XMaterial.CHISELED_STONE_BRICKS,
                XMaterial.ENDER_CHEST,
                XMaterial.END_STONE_BRICKS,
                XMaterial.NETHERRACK,
                XMaterial.REDSTONE_BLOCK,
                XMaterial.WAXED_OXIDIZED_CUT_COPPER,
                XMaterial.LIGHT_WEIGHTED_PRESSURE_PLATE,
                XMaterial.WAXED_WEATHERED_CUT_COPPER,
                XMaterial.CHAIN,
                XMaterial.MAGMA_BLOCK,
                XMaterial.STONE_PRESSURE_PLATE,
                XMaterial.DARK_PRISMARINE,
                XMaterial.MEDIUM_AMETHYST_BUD,
                XMaterial.LANTERN,
                XMaterial.ICE,
                XMaterial.DIORITE,
                XMaterial.DROPPER,
                XMaterial.CRACKED_NETHER_BRICKS,
                XMaterial.BREWING_STAND,
                XMaterial.CHISELED_RED_SANDSTONE,
                XMaterial.CALCITE,
                XMaterial.CUT_SANDSTONE,
                XMaterial.POLISHED_BASALT,
                XMaterial.DEEPSLATE_TILES,
                XMaterial.QUARTZ_PILLAR,
                XMaterial.LODESTONE,
                XMaterial.POLISHED_GRANITE,
                XMaterial.POLISHED_ANDESITE,
                XMaterial.OBSERVER,
                XMaterial.CHISELED_DEEPSLATE,
                XMaterial.RAW_GOLD_BLOCK,
                XMaterial.CRACKED_POLISHED_BLACKSTONE_BRICKS,
                XMaterial.WAXED_EXPOSED_CUT_COPPER,
                XMaterial.SMALL_AMETHYST_BUD,
                XMaterial.OXIDIZED_COPPER,
                XMaterial.POLISHED_BLACKSTONE,
                XMaterial.RAW_IRON_BLOCK,
                XMaterial.POLISHED_BLACKSTONE_BRICKS,
                XMaterial.INFESTED_DEEPSLATE,
                XMaterial.RAW_COPPER_BLOCK,
                XMaterial.BLACKSTONE,
                XMaterial.AMETHYST_CLUSTER,
                XMaterial.GRINDSTONE,
                XMaterial.WAXED_EXPOSED_COPPER,
                XMaterial.RED_SANDSTONE,
                XMaterial.LIGHTNING_ROD,
                XMaterial.SOUL_LANTERN,
                XMaterial.POLISHED_BLACKSTONE_PRESSURE_PLATE,
                XMaterial.IRON_BARS,
                XMaterial.PURPUR_BLOCK,
                XMaterial.FURNACE,
                XMaterial.CONDUIT,
                XMaterial.SPAWNER,
                XMaterial.COAL_BLOCK,
                XMaterial.BONE_BLOCK,
                XMaterial.WARPED_NYLIUM,
                XMaterial.WEATHERED_COPPER,
                XMaterial.WEATHERED_CUT_COPPER,
                XMaterial.MOSSY_COBBLESTONE,
                XMaterial.SMOKER,
                XMaterial.COBBLED_DEEPSLATE,
                XMaterial.SMOOTH_BASALT,
                XMaterial.STONE_BUTTON,
                XMaterial.NETHER_BRICKS,
                XMaterial.BRICKS,
                XMaterial.RED_NETHER_BRICKS,
                XMaterial.SMOOTH_STONE,
                XMaterial.ANDESITE,
                XMaterial.BASALT,
                XMaterial.TUFF,
                XMaterial.END_STONE,
                XMaterial.WAXED_OXIDIZED_COPPER,
                XMaterial.INFESTED_CHISELED_STONE_BRICKS,
                XMaterial.PRISMARINE_BRICKS,
                XMaterial.CRYING_OBSIDIAN,
                XMaterial.CRACKED_DEEPSLATE_TILES,
                XMaterial.INFESTED_STONE,
                XMaterial.IRON_TRAPDOOR,
                XMaterial.INFESTED_MOSSY_STONE_BRICKS,
                XMaterial.RESPAWN_ANCHOR,
                XMaterial.BLUE_ICE,
                XMaterial.POLISHED_DIORITE,
                XMaterial.NETHER_BRICK_FENCE,
                XMaterial.INFESTED_CRACKED_STONE_BRICKS,
                XMaterial.SANDSTONE,
                XMaterial.EXPOSED_COPPER,
                XMaterial.WAXED_WEATHERED_COPPER,
                XMaterial.CRACKED_DEEPSLATE_BRICKS,
                XMaterial.LARGE_AMETHYST_BUD,
                XMaterial.PISTON_HEAD,
                XMaterial.NETHERITE_BLOCK,
                XMaterial.PURPUR_PILLAR,
                XMaterial.GRANITE,
                XMaterial.STONECUTTER,
                XMaterial.BLAST_FURNACE,
                XMaterial.ENCHANTING_TABLE,
                XMaterial.LAPIS_BLOCK,
                XMaterial.PACKED_ICE,
                XMaterial.CRACKED_STONE_BRICKS,
                XMaterial.DEEPSLATE,
                XMaterial.CRIMSON_NYLIUM,
                XMaterial.STICKY_PISTON,
                XMaterial.DIAMOND_BLOCK,
                XMaterial.POINTED_DRIPSTONE
        ).inheritFrom(TERRACOTTA,
                GLAZED_TERRACOTTA,
                WALLS,
                CORALS,
                SHULKER_BOXES,
                RAILS,
                DIAMOND_ORES,
                GOLD_ORES,
                IRON_ORES,
                EMERALD_ORES,
                COPPER_ORES,
                ANVIL,
                CONCRETE,
                NON_WOODEN_STAIRS,
                NON_WOODEN_SLABS,
                CAULDRONS
        ).build();

        MINEABLE_SHOVEL = TagBuilder.of(
                XMaterial.FARMLAND,
                XMaterial.DIRT_PATH,
                XMaterial.SNOW,
                XMaterial.SNOW_BLOCK,
                XMaterial.RED_SAND,
                XMaterial.COARSE_DIRT,
                XMaterial.SOUL_SAND,
                XMaterial.GRAVEL,
                XMaterial.SAND,
                XMaterial.PODZOL,
                XMaterial.DIRT,
                XMaterial.CLAY,
                XMaterial.ROOTED_DIRT,
                XMaterial.MYCELIUM,
                XMaterial.SOUL_SOIL,
                XMaterial.GRASS_BLOCK
        ).inheritFrom(CONCRETE_POWDER).build();

        MINEABLE_HOE = TagBuilder.simple(XMaterial.FLOWERING_AZALEA_LEAVES,
                XMaterial.DARK_OAK_LEAVES,
                XMaterial.SHROOMLIGHT,
                XMaterial.BIRCH_LEAVES,
                XMaterial.DRIED_KELP_BLOCK,
                XMaterial.JUNGLE_LEAVES,
                XMaterial.OAK_LEAVES,
                XMaterial.MOSS_CARPET,
                XMaterial.WET_SPONGE,
                XMaterial.AZALEA_LEAVES,
                XMaterial.NETHER_WART_BLOCK,
                XMaterial.WARPED_WART_BLOCK,
                XMaterial.SPONGE,
                XMaterial.SPRUCE_LEAVES,
                XMaterial.SCULK_SENSOR,
                XMaterial.HAY_BLOCK,
                XMaterial.TARGET,
                XMaterial.ACACIA_LEAVES,
                XMaterial.MANGROVE_LEAVES,
                XMaterial.CHERRY_LEAVES,
                XMaterial.MOSS_BLOCK);

        LAVA_POOL_STONE_CANNOT_REPLACE = TagBuilder.simple(XMaterial.DARK_OAK_LEAVES,
                XMaterial.STRIPPED_DARK_OAK_WOOD,
                XMaterial.OAK_WOOD,
                XMaterial.CRIMSON_HYPHAE,
                XMaterial.JUNGLE_LEAVES,
                XMaterial.MANGROVE_LEAVES,
                XMaterial.CHERRY_LEAVES,
                XMaterial.DARK_OAK_WOOD,
                XMaterial.STRIPPED_ACACIA_LOG,
                XMaterial.DARK_OAK_LOG,
                XMaterial.STRIPPED_DARK_OAK_LOG,
                XMaterial.AZALEA_LEAVES,
                XMaterial.SPAWNER,
                XMaterial.JUNGLE_LOG,
                XMaterial.SPRUCE_LOG,
                XMaterial.MANGROVE_LOG,
                XMaterial.CHERRY_LOG,
                XMaterial.STRIPPED_CRIMSON_HYPHAE,
                XMaterial.SPRUCE_LEAVES,
                XMaterial.STRIPPED_BIRCH_LOG,
                XMaterial.ACACIA_LOG,
                XMaterial.STRIPPED_ACACIA_WOOD,
                XMaterial.CRIMSON_STEM,
                XMaterial.BIRCH_WOOD,
                XMaterial.STRIPPED_JUNGLE_WOOD,
                XMaterial.STRIPPED_MANGROVE_LOG,
                XMaterial.STRIPPED_CHERRY_LOG,
                XMaterial.WARPED_HYPHAE,
                XMaterial.CHEST,
                XMaterial.FLOWERING_AZALEA_LEAVES,
                XMaterial.STRIPPED_OAK_LOG,
                XMaterial.ACACIA_WOOD,
                XMaterial.BEDROCK,
                XMaterial.BIRCH_LEAVES,
                XMaterial.STRIPPED_CRIMSON_STEM,
                XMaterial.OAK_LEAVES,
                XMaterial.STRIPPED_BIRCH_WOOD,
                XMaterial.STRIPPED_MANGROVE_WOOD,
                XMaterial.STRIPPED_CHERRY_WOOD,
                XMaterial.STRIPPED_JUNGLE_LOG,
                XMaterial.WARPED_STEM,
                XMaterial.END_PORTAL_FRAME,
                XMaterial.SPRUCE_WOOD,
                XMaterial.STRIPPED_SPRUCE_LOG,
                XMaterial.STRIPPED_SPRUCE_WOOD,
                XMaterial.JUNGLE_WOOD,
                XMaterial.MANGROVE_WOOD,
                XMaterial.CHERRY_WOOD,
                XMaterial.STRIPPED_OAK_WOOD,
                XMaterial.STRIPPED_WARPED_STEM,
                XMaterial.OAK_LOG,
                XMaterial.ACACIA_LEAVES,
                XMaterial.STRIPPED_WARPED_HYPHAE,
                XMaterial.BIRCH_LOG);
        LEATHER_ARMOR_PIECES = TagBuilder.simple(XMaterial.LEATHER_HELMET,
                XMaterial.LEATHER_CHESTPLATE,
                XMaterial.LEATHER_LEGGINGS,
                XMaterial.LEATHER_BOOTS);
        IRON_ARMOR_PIECES = TagBuilder.simple(XMaterial.IRON_HELMET,
                XMaterial.IRON_CHESTPLATE,
                XMaterial.IRON_LEGGINGS,
                XMaterial.IRON_BOOTS);
        CHAINMAIL_ARMOR_PIECES = TagBuilder.simple(XMaterial.CHAINMAIL_HELMET,
                XMaterial.CHAINMAIL_CHESTPLATE,
                XMaterial.CHAINMAIL_LEGGINGS,
                XMaterial.CHAINMAIL_BOOTS);
        GOLDEN_ARMOR_PIECES = TagBuilder.simple(XMaterial.GOLDEN_HELMET,
                XMaterial.GOLDEN_CHESTPLATE,
                XMaterial.GOLDEN_LEGGINGS,
                XMaterial.GOLDEN_BOOTS);
        DIAMOND_ARMOR_PIECES = TagBuilder.simple(XMaterial.DIAMOND_HELMET,
                XMaterial.DIAMOND_CHESTPLATE,
                XMaterial.DIAMOND_LEGGINGS,
                XMaterial.DIAMOND_BOOTS);
        NETHERITE_ARMOR_PIECES = TagBuilder.simple(XMaterial.NETHERITE_HELMET,
                XMaterial.NETHERITE_CHESTPLATE,
                XMaterial.NETHERITE_LEGGINGS,
                XMaterial.NETHERITE_BOOTS);
        WOODEN_TOOLS = TagBuilder.simple(XMaterial.WOODEN_PICKAXE,
                XMaterial.WOODEN_AXE,
                XMaterial.WOODEN_HOE,
                XMaterial.WOODEN_SHOVEL,
                XMaterial.WOODEN_SWORD);
        STONE_TOOLS = TagBuilder.simple(XMaterial.STONE_PICKAXE,
                XMaterial.STONE_AXE,
                XMaterial.STONE_HOE,
                XMaterial.STONE_SHOVEL,
                XMaterial.STONE_SWORD);
        IRON_TOOLS = TagBuilder.simple(XMaterial.IRON_PICKAXE,
                XMaterial.IRON_AXE,
                XMaterial.IRON_HOE,
                XMaterial.IRON_SHOVEL,
                XMaterial.IRON_SWORD);
        DIAMOND_TOOLS = TagBuilder.simple(XMaterial.DIAMOND_PICKAXE,
                XMaterial.DIAMOND_AXE,
                XMaterial.DIAMOND_HOE,
                XMaterial.DIAMOND_SHOVEL,
                XMaterial.DIAMOND_SHOVEL);
        NETHERITE_TOOLS = TagBuilder.simple(XMaterial.NETHERITE_PICKAXE,
                XMaterial.NETHERITE_AXE,
                XMaterial.NETHERITE_HOE,
                XMaterial.NETHERITE_SHOVEL,
                XMaterial.NETHERITE_SHOVEL);
        ARMOR_PIECES = TagBuilder
                .of(XMaterial.TURTLE_HELMET)
                .inheritFrom(
                        LEATHER_ARMOR_PIECES,
                        CHAINMAIL_ARMOR_PIECES,
                        IRON_ARMOR_PIECES,
                        GOLDEN_ARMOR_PIECES,
                        DIAMOND_ARMOR_PIECES,
                        NETHERITE_ARMOR_PIECES
                ).build();
        SMITHING_TEMPLATES = TagBuilder.simple(
                XMaterial.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
                XMaterial.COAST_ARMOR_TRIM_SMITHING_TEMPLATE,
                XMaterial.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE,
                XMaterial.EYE_ARMOR_TRIM_SMITHING_TEMPLATE,
                XMaterial.HOST_ARMOR_TRIM_SMITHING_TEMPLATE,
                XMaterial.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE,
                XMaterial.RIB_ARMOR_TRIM_SMITHING_TEMPLATE,
                XMaterial.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE,
                XMaterial.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE,
                XMaterial.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE,
                XMaterial.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE,
                XMaterial.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE,
                XMaterial.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE,
                XMaterial.VEX_ARMOR_TRIM_SMITHING_TEMPLATE,
                XMaterial.WARD_ARMOR_TRIM_SMITHING_TEMPLATE,
                XMaterial.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE,
                XMaterial.WILD_ARMOR_TRIM_SMITHING_TEMPLATE
        );

        AZALEA_GROWS_ON = TagBuilder
                .of(XMaterial.SNOW_BLOCK, XMaterial.POWDER_SNOW)
                .inheritFrom(TERRACOTTA, SAND, DIRT)
                .build();
        AZALEA_ROOT_REPLACEABLE = TagBuilder
                .of(XMaterial.CLAY, XMaterial.GRAVEL)
                .inheritFrom(AZALEA_GROWS_ON, CAVE_VINES, BASE_STONE_OVERWORLD)
                .build();
        BAMBOO_PLANTABLE_ON = TagBuilder
                .of(XMaterial.GRAVEL, XMaterial.BAMBOO_SAPLING, XMaterial.BAMBOO)
                .inheritFrom(DIRT, SAND)
                .build();
        BEE_GROWABLES = TagBuilder
                .of(XMaterial.SWEET_BERRY_BUSH)
                .inheritFrom(CROPS, CAVE_VINES)
                .build();
        BIG_DRIPLEAF_PLACEABLE = TagBuilder
                .of(XMaterial.CLAY, XMaterial.FARMLAND)
                .inheritFrom(DIRT)
                .build();
        BUTTONS = TagBuilder
                .of(XMaterial.STONE_BUTTON, XMaterial.POLISHED_BLACKSTONE_BUTTON)
                .inheritFrom(WOODEN_BUTTONS)
                .build();
        DRIPSTONE_REPLACEABLE = TagBuilder
                .of(XMaterial.DIRT)
                .inheritFrom(BASE_STONE_OVERWORLD)
                .build();
        ENDERMAN_HOLDABLE = TagBuilder.of(
                        XMaterial.TNT,
                        XMaterial.PUMPKIN,
                        XMaterial.CARVED_PUMPKIN,
                        XMaterial.MELON,
                        XMaterial.CRIMSON_FUNGUS,
                        XMaterial.WARPED_FUNGUS,
                        XMaterial.WARPED_ROOTS,
                        XMaterial.CRIMSON_ROOTS,
                        XMaterial.RED_MUSHROOM,
                        XMaterial.BROWN_MUSHROOM,
                        XMaterial.CACTUS,
                        XMaterial.GRAVEL,
                        XMaterial.CLAY
                ).inheritFrom(DIRT, NYLIUM, SAND, SMALL_FLOWERS)
                .build();
        FLOWERS = TagBuilder
                .of(XMaterial.FLOWERING_AZALEA, XMaterial.FLOWERING_AZALEA_LEAVES)
                .inheritFrom(SMALL_FLOWERS, TALL_FLOWERS)
                .build();
        GOATS_SPAWNABLE_ON = TagBuilder.of(
                        XMaterial.GRAVEL,
                        XMaterial.STONE,
                        XMaterial.PACKED_ICE
                ).inheritFrom(SNOW)
                .build();
        GUARDED_BY_PIGLINS = TagBuilder.of(XMaterial.GOLD_BLOCK,
                        XMaterial.ENDER_CHEST,
                        XMaterial.RAW_GOLD_BLOCK,
                        XMaterial.GILDED_BLACKSTONE,
                        XMaterial.CHEST,
                        XMaterial.BARREL,
                        XMaterial.TRAPPED_CHEST
                ).inheritFrom(SHULKER_BOXES, GOLD_ORES)
                .build();
        ITEMS_MUSIC_DISCS = TagBuilder.of(XMaterial.MUSIC_DISC_OTHERSIDE, XMaterial.MUSIC_DISC_PIGSTEP)
                .inheritFrom(ITEMS_CREEPER_DROP_MUSIC_DISCS)
                .build();
        ITEMS_PIGLIN_LOVED = TagBuilder.of(XMaterial.GOLD_BLOCK,
                        XMaterial.RAW_GOLD,
                        XMaterial.GLISTERING_MELON_SLICE,
                        XMaterial.GOLDEN_HORSE_ARMOR,
                        XMaterial.LIGHT_WEIGHTED_PRESSURE_PLATE,
                        XMaterial.GOLDEN_SWORD,
                        XMaterial.GOLDEN_AXE,
                        XMaterial.BELL,
                        XMaterial.ENCHANTED_GOLDEN_APPLE,
                        XMaterial.RAW_GOLD_BLOCK,
                        XMaterial.GILDED_BLACKSTONE,
                        XMaterial.CLOCK,
                        XMaterial.GOLDEN_CARROT,
                        XMaterial.GOLDEN_APPLE,
                        XMaterial.GOLDEN_SHOVEL,
                        XMaterial.GOLDEN_PICKAXE,
                        XMaterial.GOLDEN_HOE,
                        XMaterial.GOLD_INGOT
                ).inheritFrom(GOLD_ORES, GOLDEN_ARMOR_PIECES)
                .build();
        SIGNS = TagBuilder.simple(WALL_SIGNS, STANDING_SIGNS);
        PRESSURE_PLATES = TagBuilder.of(
                        XMaterial.LIGHT_WEIGHTED_PRESSURE_PLATE,
                        XMaterial.HEAVY_WEIGHTED_PRESSURE_PLATE
                ).inheritFrom(STONE_PRESSURE_PLATES, WOODEN_PRESSURE_PLATES)
                .build();
        DRAGON_IMMUNE = TagBuilder.of(XMaterial.IRON_BARS,
                        XMaterial.OBSIDIAN,
                        XMaterial.RESPAWN_ANCHOR,
                        XMaterial.END_STONE,
                        XMaterial.CRYING_OBSIDIAN
                ).inheritFrom(WITHER_IMMUNE)
                .build();
        WALL_POST_OVERRIDE = TagBuilder.of(
                        XMaterial.TORCH,
                        XMaterial.TRIPWIRE,
                        XMaterial.REDSTONE_TORCH,
                        XMaterial.SOUL_TORCH
                ).inheritFrom(SIGNS, BANNERS, PRESSURE_PLATES)
                .build();
        UNDERWATER_BONEMEALS = TagBuilder.of(XMaterial.SEAGRASS)
                .inheritFrom(CORALS, ALIVE_CORAL_WALL_FANS)
                .build();
        UNSTABLE_BOTTOM_CENTER = TagBuilder.simple(FENCE_GATES);
        PREVENT_MOB_SPAWNING_INSIDE = TagBuilder.simple(RAILS);
        OCCLUDES_VIBRATION_SIGNALS = TagBuilder.simple(WOOL);
        LOGS_THAT_BURN = TagBuilder.simple(
                ACACIA_LOGS,
                OAK_LOGS,
                DARK_OAK_LOGS,
                SPRUCE_LOGS,
                JUNGLE_LOGS,
                BIRCH_LOGS,
                MANGROVE_LOGS,
                CHERRY_LOGS
        );
        LOGS = TagBuilder.simple(
                LOGS_THAT_BURN,
                CRIMSON_STEMS,
                WARPED_STEMS
        );

        ITEMS_FURNACE_MATERIALS = TagBuilder.of(XMaterial.COAL, XMaterial.CHARCOAL, XMaterial.COAL_BLOCK)
                .inheritFrom(LOGS, PLANKS).build();

        PARROTS_SPAWNABLE_ON = TagBuilder.of(XMaterial.GRASS_BLOCK).inheritFrom(AIR, LEAVES, LOGS).build();
        LUSH_GROUND_REPLACEABLE = TagBuilder.of(
                XMaterial.GRAVEL,
                XMaterial.SAND,
                XMaterial.CLAY
        ).inheritFrom(CAVE_VINES,
                DIRT,
                BASE_STONE_OVERWORLD
        ).build();
        TRAPDOORS = TagBuilder.of(XMaterial.IRON_TRAPDOOR)
                .inheritFrom(WOODEN_TRAPDOORS)
                .build();
        MUSHROOM_GROW_BLOCK = TagBuilder.of(XMaterial.PODZOL, XMaterial.MYCELIUM)
                .inheritFrom(NYLIUM)
                .build();
        MOSS_REPLACEABLE = TagBuilder.simple(
                CAVE_VINES,
                DIRT,
                BASE_STONE_OVERWORLD
        );

        ARMOR_ENCHANTS = TagBuilder.simple(
                XEnchantment.BLAST_PROTECTION,
                XEnchantment.BINDING_CURSE,
                XEnchantment.VANISHING_CURSE,
                XEnchantment.FIRE_PROTECTION,
                XEnchantment.MENDING,
                XEnchantment.PROJECTILE_PROTECTION,
                XEnchantment.PROTECTION,
                XEnchantment.THORNS,
                XEnchantment.UNBREAKING
        );

        HELEMT_ENCHANTS = TagBuilder
                .of(XEnchantment.AQUA_AFFINITY, XEnchantment.RESPIRATION)
                .inheritFrom(ARMOR_ENCHANTS)
                .build();

        CHESTPLATE_ENCHANTS = TagBuilder.simple(ARMOR_ENCHANTS);
        LEGGINGS_ENCHANTS = TagBuilder.simple(ARMOR_ENCHANTS);
        BOOTS_ENCHANTS = TagBuilder.of(
                        XEnchantment.DEPTH_STRIDER,
                        XEnchantment.FEATHER_FALLING,
                        XEnchantment.FROST_WALKER
                ).inheritFrom(ARMOR_ENCHANTS)
                .build();

        ELYTRA_ENCHANTS = TagBuilder.simple(
                XEnchantment.BINDING_CURSE,
                XEnchantment.VANISHING_CURSE,
                XEnchantment.MENDING,
                XEnchantment.UNBREAKING
        );

        SWORD_ENCHANTS = TagBuilder.simple(
                XEnchantment.BANE_OF_ARTHROPODS,
                XEnchantment.VANISHING_CURSE,
                XEnchantment.FIRE_ASPECT,
                XEnchantment.KNOCKBACK,
                XEnchantment.LOOTING,
                XEnchantment.MENDING,
                XEnchantment.SHARPNESS,
                XEnchantment.SMITE,
                XEnchantment.SWEEPING_EDGE,
                XEnchantment.UNBREAKING
        );

        AXE_ENCHANTS = TagBuilder.simple(
                XEnchantment.BANE_OF_ARTHROPODS,
                XEnchantment.VANISHING_CURSE,
                XEnchantment.EFFICIENCY,
                XEnchantment.FORTUNE,
                XEnchantment.MENDING,
                XEnchantment.SHARPNESS,
                XEnchantment.SILK_TOUCH,
                XEnchantment.SMITE,
                XEnchantment.UNBREAKING
        );

        HOE_ENCHANTS = TagBuilder.simple(XEnchantment.VANISHING_CURSE,
                XEnchantment.EFFICIENCY,
                XEnchantment.FORTUNE,
                XEnchantment.MENDING,
                XEnchantment.SILK_TOUCH,
                XEnchantment.UNBREAKING);

        PICKAXE_ENCHANTS = TagBuilder.simple(XEnchantment.VANISHING_CURSE,
                XEnchantment.EFFICIENCY,
                XEnchantment.FORTUNE,
                XEnchantment.MENDING,
                XEnchantment.SILK_TOUCH,
                XEnchantment.UNBREAKING);

        SHOVEL_ENCHANTS = TagBuilder.simple(XEnchantment.VANISHING_CURSE,
                XEnchantment.EFFICIENCY,
                XEnchantment.FORTUNE,
                XEnchantment.MENDING,
                XEnchantment.SILK_TOUCH,
                XEnchantment.UNBREAKING);

        SHEARS_ENCHANTS = TagBuilder.of(
                XEnchantment.VANISHING_CURSE,
                XEnchantment.EFFICIENCY,
                XEnchantment.MENDING,
                XEnchantment.UNBREAKING).build();

        BOW_ENCHANTS = TagBuilder.simple(XEnchantment.VANISHING_CURSE,
                XEnchantment.FLAME,
                XEnchantment.INFINITY,
                XEnchantment.MENDING,
                XEnchantment.PUNCH,
                XEnchantment.UNBREAKING);

        CROSSBOW_ENCHANTS = TagBuilder.simple(XEnchantment.VANISHING_CURSE,
                XEnchantment.MENDING,
                XEnchantment.MULTISHOT,
                XEnchantment.PIERCING,
                XEnchantment.QUICK_CHARGE,
                XEnchantment.UNBREAKING);


        MINEABLE_AXE = TagBuilder.of(XMaterial.COMPOSTER,
                XMaterial.COCOA,
                XMaterial.RED_MUSHROOM_BLOCK,
                XMaterial.CRAFTING_TABLE,
                XMaterial.TALL_GRASS,
                XMaterial.BIG_DRIPLEAF_STEM,
                XMaterial.RED_MUSHROOM,
                XMaterial.JUKEBOX,
                XMaterial.WARPED_FUNGUS,
                XMaterial.DEAD_BUSH,
                XMaterial.NOTE_BLOCK,
                XMaterial.CRIMSON_FUNGUS,
                XMaterial.MUSHROOM_STEM,
                XMaterial.CHORUS_PLANT,
                XMaterial.BEE_NEST,
                XMaterial.BROWN_MUSHROOM_BLOCK,
                XMaterial.JACK_O_LANTERN,
                XMaterial.FERN,
                XMaterial.NETHER_WART,
                XMaterial.CARTOGRAPHY_TABLE,
                XMaterial.CHEST,
                XMaterial.SWEET_BERRY_BUSH,
                XMaterial.BROWN_MUSHROOM,
                XMaterial.CARVED_PUMPKIN,
                XMaterial.SMITHING_TABLE,
                XMaterial.GLOW_LICHEN,
                XMaterial.SMALL_DRIPLEAF,
                XMaterial.LOOM,
                XMaterial.BEEHIVE,
                XMaterial.SHORT_GRASS,
                XMaterial.HANGING_ROOTS,
                XMaterial.CHORUS_FLOWER,
                XMaterial.ATTACHED_PUMPKIN_STEM,
                XMaterial.BIG_DRIPLEAF,
                XMaterial.DAYLIGHT_DETECTOR,
                XMaterial.SPORE_BLOSSOM,
                XMaterial.LILY_PAD,
                XMaterial.TRAPPED_CHEST,
                XMaterial.BARREL,
                XMaterial.LARGE_FERN,
                XMaterial.LECTERN,
                XMaterial.SUGAR_CANE,
                XMaterial.MELON,
                XMaterial.ATTACHED_MELON_STEM,
                XMaterial.PUMPKIN,
                XMaterial.BAMBOO,
                XMaterial.FLETCHING_TABLE,
                XMaterial.BOOKSHELF
        ).inheritFrom(
                BANNERS,
                SIGNS,
                CAVE_VINES,
                CROPS,
                LOGS,
                WOODEN_STAIRS,
                WOODEN_SLABS,
                WOODEN_PRESSURE_PLATES,
                WOODEN_FENCES,
                WOODEN_FENCE_GATES,
                WOODEN_TRAPDOORS,
                WOODEN_DOORS,
                WOODEN_BUTTONS,
                PLANKS,
                SAPLINGS,
                CLIMBABLE,
                CAMPFIRES
        ).build();
    }

    static {
        INVENTORY_NOT_DISPLAYABLE = TagBuilder
                .of(
                        XMaterial.BIG_DRIPLEAF_STEM, XMaterial.SWEET_BERRY_BUSH, XMaterial.KELP_PLANT,
                        XMaterial.FROSTED_ICE, XMaterial.ATTACHED_MELON_STEM, XMaterial.ATTACHED_PUMPKIN_STEM,
                        XMaterial.COCOA, XMaterial.MOVING_PISTON, XMaterial.PISTON_HEAD, XMaterial.PITCHER_CROP,
                        XMaterial.POWDER_SNOW, XMaterial.REDSTONE_WIRE, XMaterial.TALL_SEAGRASS, XMaterial.TRIPWIRE,
                        XMaterial.TORCHFLOWER_CROP, XMaterial.BUBBLE_COLUMN, XMaterial.TWISTING_VINES_PLANT,
                        XMaterial.WEEPING_VINES_PLANT, XMaterial.BAMBOO_SAPLING
                )
                .inheritFrom(
                        AIR, CAVE_VINES, FILLED_CAULDRONS, FIRE, FLUID, PORTALS,
                        WALL_SIGNS, WALL_HANGING_SIGNS, WALL_TORCHES, ALIVE_CORAL_WALL_FANS,
                        DEAD_CORAL_WALL_FANS, WALL_HEADS, CANDLE_CAKES, WALL_BANNERS,
                        FLOWER_POTS.without(XMaterial.FLOWER_POT),
                        CROPS.without(XMaterial.WHEAT_SEEDS, XMaterial.WHEAT)
                ).build();
    }

    @NotNull
    private final Set<T> values;

    private XTag(@NotNull Set<T> values) {
        this.values = values;
    }

    public static <E> List<Matcher<E>> stringMatcher(@Nullable Collection<String> elements) {
        return stringMatcher(elements, null);
    }

    /**
     * Compiles a list of string checkers for various classes like {@link XMaterial}, {@link XSound}, etc.
     * Mostly used for configs.
     * <p>
     * Supports {@link String#contains} {@code CONTAINS:NAME} and Regular Expression {@code REGEX:PATTERN} formats.
     * <p>
     * <b>Example:</b>
     * <blockquote><pre>
     *     XMaterial material = {@link XMaterial#matchXMaterial(ItemStack)};
     *     if (XTag.anyMatch(XTag.stringMatcher(plugin.getConfig().getStringList("disabled-items"), null)) return;
     * </pre></blockquote>
     * <br>
     * <b>{@code CONTAINS} Examples:</b>
     * <pre>
     *     {@code "CONTAINS:CHEST" -> CHEST, ENDERCHEST, TRAPPED_CHEST -> true}
     *     {@code "cOnTaINS:dYe" -> GREEN_DYE, YELLOW_DYE, BLUE_DYE, INK_SACK -> true}
     * </pre>
     * <p>
     * <b>{@code REGEX} Examples</b>
     * <pre>
     *     {@code "REGEX:^.+_.+_.+$" -> Every Material with 3 underlines or more: SHULKER_SPAWN_EGG, SILVERFISH_SPAWN_EGG, SKELETON_HORSE_SPAWN_EGG}
     *     {@code "REGEX:^.{1,3}$" -> Material names that have 3 letters only: BED, MAP, AIR}
     * </pre>
     * <p>
     * The reason that there are tags for {@code CONTAINS} and {@code REGEX} is for the performance.
     * Server owners should be advised to avoid using {@code REGEX} tag if they can use the {@code CONTAINS} tag instead.
     * <p>
     * Want to learn RegEx? You can mess around in <a href="https://regexr.com/">RegExr</a> website.
     *
     * @param elements the material names to check base material on.
     * @return a compiled list of enum matchers.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E> List<Matcher<E>> stringMatcher(@Nullable Collection<String> elements,
                                                     @Nullable Collection<Matcher.Error> errors) {
        if (elements == null || elements.isEmpty()) return new ArrayList<>();
        List<Matcher<E>> matchers = new ArrayList<>(elements.size());

        for (String comp : elements) {
            String checker = comp.toUpperCase(Locale.ENGLISH);
            if (checker.startsWith("CONTAINS:")) {
                comp = XMaterial.format(checker.substring(9));
                matchers.add(new Matcher.TextMatcher<>(comp, true));
                continue;
            }
            if (checker.startsWith("REGEX:")) {
                comp = comp.substring(6);
                try {
                    matchers.add(new Matcher.RegexMatcher<>(Pattern.compile(comp)));
                } catch (Throwable e) {
                    if (errors != null) errors.add(new Matcher.Error(comp, "REGEX", e));
                }
                continue;
            }
            if (checker.startsWith("TAG:")) {
                comp = XMaterial.format(comp.substring(4));
                Optional<XTag<?>> tag = getTag(comp);
                if (tag.isPresent()) matchers.add(new Matcher.XTagMatcher(tag.get()));
                else errors.add(new Matcher.Error("Cannot find tag: " + comp, "TAG"));
            }

            matchers.add(new Matcher.TextMatcher<>(comp, false));
        }

        return matchers;
    }

    public static <T> boolean anyMatchString(T target, Collection<String> matchers) {
        return anyMatch(target, stringMatcher(matchers));
    }

    public static <T> boolean anyMatch(T target, Collection<Matcher<T>> matchers) {
        return matchers.stream().anyMatch(x -> x.matches(target));
    }

    public abstract static class Matcher<T> {
        public static final class Error extends RuntimeException {
            public final String matcher;

            public Error(String matcher, String message) {
                super(message);
                this.matcher = matcher;
            }

            public Error(String matcher, String message, Throwable cause) {
                super(message, cause);
                this.matcher = matcher;
            }
        }

        public abstract boolean matches(T object);

        public static final class TextMatcher<T> extends Matcher<T> {
            public final String text;
            public final boolean contains;

            public TextMatcher(String text, boolean contains) {
                this.text = text;
                this.contains = contains;
            }

            @Override
            public boolean matches(T object) {
                String name = object instanceof Enum ? ((Enum<?>) object).name() : object.toString();
                return contains ? name.contains(this.text) : name.equals(this.text);
            }
        }

        public static final class RegexMatcher<T> extends Matcher<T> {
            public final Pattern regex;

            public RegexMatcher(Pattern regex) {
                this.regex = regex;
            }

            @Override
            public boolean matches(T object) {
                String name = object instanceof Enum ? ((Enum<?>) object).name() : object.toString();
                return regex.matcher(name).matches();
            }
        }

        public static final class XTagMatcher<T extends XBase<?, ?>> extends Matcher<T> {
            public final XTag<T> matcher;

            public XTagMatcher(XTag<T> matcher) {
                this.matcher = matcher;
            }

            @Override
            public boolean matches(T object) {
                return matcher.isTagged(object);
            }
        }
    }

    private static XMaterial[] findAllColors(String material) {
        String[] colorPrefixes = {"ORANGE", "LIGHT_BLUE", "GRAY", "BLACK", "MAGENTA", "PINK", "BLUE",
                "GREEN", "CYAN", "PURPLE", "YELLOW", "LIME", "LIGHT_GRAY", "WHITE", "BROWN", "RED"};
        List<XMaterial> list = new ArrayList<>();
        XMaterial.matchXMaterial(material).ifPresent(list::add);
        for (String color : colorPrefixes) {
            XMaterial.matchXMaterial(color + '_' + material).ifPresent(list::add);
        }
        return list.toArray(new XMaterial[0]);
    }

    private static XMaterial[] findAllWoodTypes(String material) {
        String[] woodPrefixes = {"ACACIA", "DARK_OAK", "JUNGLE", "BIRCH", "WARPED", "OAK", "SPRUCE", "CRIMSON",
                "MANGROVE", "CHERRY", "BAMBOO"};
        List<XMaterial> list = new ArrayList<>();
        for (String wood : woodPrefixes) {
            XMaterial.matchXMaterial(wood + '_' + material).ifPresent(list::add);
        }
        return list.toArray(new XMaterial[0]);
    }

    private static XMaterial[] findMaterialsEndingWith(String material) {
        return Arrays.stream(XMaterial.VALUES).filter(x -> x.name().endsWith(material)).toArray(XMaterial[]::new);
    }

    @SuppressWarnings("unused")
    private static XMaterial[] findMaterialsStartingWith(String material) {
        return Arrays.stream(XMaterial.VALUES).filter(x -> x.name().startsWith(material)).toArray(XMaterial[]::new);
    }

    private static XMaterial[] findAllCorals(boolean alive, boolean block, boolean fan, boolean wall) {
        String[] materials = {"FIRE", "TUBE", "BRAIN", "HORN", "BUBBLE"};
        List<XMaterial> list = new ArrayList<>();
        for (String material : materials) {
            StringBuilder builder = new StringBuilder();
            if (!alive) builder.append("DEAD_");
            builder.append(material).append("_CORAL");
            if (block) builder.append("_BLOCK");
            if (fan) {
                if (wall) builder.append("_WALL");
                builder.append("_FAN");
            }

            XMaterial.matchXMaterial(builder.toString()).ifPresent(list::add);
        }
        return list.toArray(new XMaterial[0]);
    }

    /**
     * Checks if this Material is an obtainable item. "Obtainable items" are simply materials that can be displayed in your GUI.
     * This method is mainly designed to support pre-1.13, servers using 1.13 and above will directly have their materials checked with {@link Material#isItem()}
     *
     * @return true if this material is an item, otherwise false if it's not an item or the item is not supported.
     * @since 1.13
     */
    public static boolean isItem(XMaterial material) {
        if (XMaterial.supports(13)) {
            Material mat = material.get();
            return mat != null && mat.isItem();
        }

        switch (material) { // All the materials that are NOT an item (only 1.12 materials)
            case ATTACHED_MELON_STEM:
            case ATTACHED_PUMPKIN_STEM:
            case BEETROOTS:
            case BLACK_WALL_BANNER:
            case BLUE_WALL_BANNER:
            case BROWN_WALL_BANNER:
            case CARROTS:
            case COCOA:
            case CREEPER_WALL_HEAD:
            case CYAN_WALL_BANNER:
            case DRAGON_WALL_HEAD:
            case END_GATEWAY:
            case END_PORTAL:
            case FIRE:
            case FIRE_CORAL_WALL_FAN:
            case FROSTED_ICE:
            case GRAY_WALL_BANNER:
            case GREEN_WALL_BANNER:
            case HORN_CORAL_WALL_FAN:
            case LAVA:
            case LIGHT_BLUE_WALL_BANNER:
            case LIGHT_GRAY_WALL_BANNER:
            case LIME_WALL_BANNER:
            case MAGENTA_WALL_BANNER:
            case MELON_STEM:
            case MOVING_PISTON:
            case NETHER_PORTAL:
            case ORANGE_WALL_BANNER:
            case PINK_WALL_BANNER:
            case PISTON_HEAD:
            case PLAYER_WALL_HEAD:
            case POTATOES:
            case POTTED_ACACIA_SAPLING:
            case POTTED_ALLIUM:
            case POTTED_AZURE_BLUET:
            case POTTED_BIRCH_SAPLING:
            case POTTED_BLUE_ORCHID:
            case POTTED_BROWN_MUSHROOM:
            case POTTED_CACTUS:
            case POTTED_DANDELION:
            case POTTED_DARK_OAK_SAPLING:
            case POTTED_DEAD_BUSH:
            case POTTED_FERN:
            case POTTED_JUNGLE_SAPLING:
            case POTTED_OAK_SAPLING:
            case POTTED_ORANGE_TULIP:
            case POTTED_OXEYE_DAISY:
            case POTTED_PINK_TULIP:
            case POTTED_POPPY:
            case POTTED_RED_MUSHROOM:
            case POTTED_RED_TULIP:
            case POTTED_SPRUCE_SAPLING:
            case POTTED_WHITE_TULIP:
            case PUMPKIN_STEM:
            case PURPLE_WALL_BANNER:
            case REDSTONE_WALL_TORCH:
            case REDSTONE_WIRE:
            case RED_WALL_BANNER:
            case SKELETON_WALL_SKULL:
            case TRIPWIRE:
            case ACACIA_WALL_SIGN:
            case OAK_WALL_SIGN:
            case BIRCH_WALL_SIGN:
            case JUNGLE_WALL_SIGN:
            case SPRUCE_WALL_SIGN:
            case DARK_OAK_WALL_SIGN:
            case WALL_TORCH:
            case WATER:
            case WHITE_WALL_BANNER:
            case WITHER_SKELETON_WALL_SKULL:
            case YELLOW_WALL_BANNER:
            case ZOMBIE_WALL_HEAD:
                return false;
            default:
                return true;
        }
    }

    /**
     * Checks if this Material can be interacted with.
     * <p>
     * Interactable materials include those with functionality when they are
     * interacted with by a player such as chests, furnaces, etc.
     * <p>
     * Some blocks such as piston heads and stairs are considered interactable
     * though may not perform any additional functionality.
     * <p>
     * Note that the interactability of some materials may be dependent on their
     * state as well. This method will return true if there is at least one
     * state in which additional interact handling is performed for the
     * material.
     *
     * @return true if this material can be interacted with.
     * @since 1.13
     */
    public static boolean isInteractable(XMaterial material) {
        if (XMaterial.supports(13)) return material.get().isInteractable();
        switch (material) { // 1.12 materials only
            case ACACIA_BUTTON:
            case ACACIA_DOOR:
            case ACACIA_FENCE:
            case ACACIA_FENCE_GATE:
            case ACACIA_STAIRS:
            case ACACIA_TRAPDOOR:
            case ANVIL:
            case BEACON:
            case BIRCH_BUTTON:
            case BIRCH_DOOR:
            case BIRCH_FENCE:
            case BIRCH_FENCE_GATE:
            case BIRCH_STAIRS:
            case BIRCH_TRAPDOOR:
            case BLACK_BED:
            case BLACK_SHULKER_BOX:
            case BLUE_BED:
            case BLUE_SHULKER_BOX:
            case BREWING_STAND:
            case BRICK_STAIRS:
            case BROWN_BED:
            case BROWN_SHULKER_BOX:
            case CAKE:
            case CAULDRON:
            case CHAIN_COMMAND_BLOCK:
            case CHEST:
            case CHIPPED_ANVIL:
            case COBBLESTONE_STAIRS:
            case COMMAND_BLOCK:
            case COMPARATOR:
            case CRAFTING_TABLE:
            case CYAN_BED:
            case CYAN_SHULKER_BOX:
            case DAMAGED_ANVIL:
            case DARK_OAK_BUTTON:
            case DARK_OAK_DOOR:
            case DARK_OAK_FENCE:
            case DARK_OAK_FENCE_GATE:
            case DARK_OAK_STAIRS:
            case DARK_OAK_TRAPDOOR:
            case DARK_PRISMARINE_STAIRS:
            case DAYLIGHT_DETECTOR:
            case DISPENSER:
            case DRAGON_EGG:
            case DROPPER:
            case ENCHANTING_TABLE:
            case ENDER_CHEST:
            case FLOWER_POT:
            case FURNACE:
            case GRAY_BED:
            case GRAY_SHULKER_BOX:
            case GREEN_BED:
            case GREEN_SHULKER_BOX:
            case HOPPER:
            case IRON_DOOR:
            case IRON_TRAPDOOR:
            case JUKEBOX:
            case JUNGLE_BUTTON:
            case JUNGLE_DOOR:
            case JUNGLE_FENCE:
            case JUNGLE_FENCE_GATE:
            case JUNGLE_STAIRS:
            case JUNGLE_TRAPDOOR:
            case LEVER:
            case LIGHT_BLUE_BED:
            case LIGHT_BLUE_SHULKER_BOX:
            case LIGHT_GRAY_BED:
            case LIGHT_GRAY_SHULKER_BOX:
            case LIME_BED:
            case LIME_SHULKER_BOX:
            case MAGENTA_BED:
            case MAGENTA_SHULKER_BOX:
            case MOVING_PISTON:
            case NETHER_BRICK_FENCE:
            case NETHER_BRICK_STAIRS:
            case NOTE_BLOCK:
            case OAK_BUTTON:
            case OAK_DOOR:
            case OAK_FENCE:
            case OAK_FENCE_GATE:
            case OAK_STAIRS:
            case OAK_TRAPDOOR:
            case ORANGE_BED:
            case ORANGE_SHULKER_BOX:
            case PINK_BED:
            case PINK_SHULKER_BOX:
            case POTTED_ACACIA_SAPLING:
            case POTTED_ALLIUM:
            case POTTED_AZURE_BLUET:
            case POTTED_BIRCH_SAPLING:
            case POTTED_BLUE_ORCHID:
            case POTTED_BROWN_MUSHROOM:
            case POTTED_CACTUS:
            case POTTED_DANDELION:
            case POTTED_DARK_OAK_SAPLING:
            case POTTED_DEAD_BUSH:
            case POTTED_FERN:
            case POTTED_JUNGLE_SAPLING:
            case POTTED_OAK_SAPLING:
            case POTTED_ORANGE_TULIP:
            case POTTED_OXEYE_DAISY:
            case POTTED_PINK_TULIP:
            case POTTED_POPPY:
            case POTTED_RED_MUSHROOM:
            case POTTED_RED_TULIP:
            case POTTED_SPRUCE_SAPLING:
            case POTTED_WHITE_TULIP:
            case PRISMARINE_BRICK_STAIRS:
            case PRISMARINE_STAIRS:
            case PUMPKIN:
            case PURPLE_BED:
            case PURPLE_SHULKER_BOX:
            case PURPUR_STAIRS:
            case QUARTZ_STAIRS:
            case REDSTONE_ORE:
            case RED_BED:
            case RED_SANDSTONE_STAIRS:
            case RED_SHULKER_BOX:
            case REPEATER:
            case REPEATING_COMMAND_BLOCK:
            case SANDSTONE_STAIRS:
            case SHULKER_BOX:
            case ACACIA_SIGN:
            case BIRCH_SIGN:
            case DARK_OAK_SIGN:
            case JUNGLE_SIGN:
            case OAK_SIGN:
            case SPRUCE_SIGN:
            case SPRUCE_BUTTON:
            case SPRUCE_DOOR:
            case SPRUCE_FENCE:
            case SPRUCE_FENCE_GATE:
            case SPRUCE_STAIRS:
            case SPRUCE_TRAPDOOR:
            case STONE_BRICK_STAIRS:
            case STONE_BUTTON:
            case STRUCTURE_BLOCK:
            case TNT:
            case TRAPPED_CHEST:
            case ACACIA_WALL_SIGN:
            case OAK_WALL_SIGN:
            case BIRCH_WALL_SIGN:
            case JUNGLE_WALL_SIGN:
            case SPRUCE_WALL_SIGN:
            case DARK_OAK_WALL_SIGN:
            case WHITE_BED:
            case WHITE_SHULKER_BOX:
            case YELLOW_BED:
            case YELLOW_SHULKER_BOX:
                return true;
            default:
                return false;
        }
    }

    /**
     * @return {@link Set} of all the values represented by the tag
     */
    @NotNull
    public Set<T> getValues() {
        return this.values;
    }

    public boolean isTagged(@Nullable T value) {
        return value != null && this.values.contains(value);
    }

    private static final class TagBuilder<T extends XBase<?, ?>> {
        private final Set<T> values;

        private TagBuilder(Collection<T> values) {
            if (values.isEmpty()) {
                this.values = Collections.newSetFromMap(new IdentityHashMap<>());
            } else if (values.iterator().next() instanceof Enum) {
                // this.values = EnumSet.copyOf((Collection<Enum>) values);

                Iterator<T> iter = values.iterator();
                T first = iter.next();

                // noinspection unchecked,rawtypes
                this.values = EnumSet.of((Enum) first);
                while (iter.hasNext())
                    this.values.add(iter.next());
            } else {
                this.values = Collections.newSetFromMap(new IdentityHashMap<>(values.size()));
                this.values.addAll(values);
            }
        }

        @SafeVarargs
        private static <T extends XBase<?, ?>> XTag<T> simple(T... values) {
            return of(values).build();
        }

        @SafeVarargs
        private static <T extends XBase<?, ?>> XTag<T> simple(XTag<T>... values) {
            return new TagBuilder<>(Collections.singletonList(values[0].values.iterator().next()))
                    .inheritFrom(values)
                    .build();
        }

        @SafeVarargs
        private static <T extends XBase<?, ?>> TagBuilder<T> of(T... values) {
            return new TagBuilder<>(Arrays.asList(values));
        }

        @SafeVarargs
        private final TagBuilder<T> inheritFrom(@NotNull XTag<T>... values) {
            for (XTag<T> value : values) {
                this.values.addAll(value.values);
            }
            return this;
        }

        private XTag<T> build() {
            return new XTag<>(Collections.unmodifiableSet(values));
        }
    }

    @SafeVarargs
    private final XTag<T> without(T... without) {
        Set<T> newSet = new HashSet<>(this.values);
        for (T value : without) newSet.remove(value);
        return new XTag<>(newSet);
    }

    private static final Map<String, XTag<?>> TAGS = new HashMap<>(30);

    public static Optional<XTag<?>> getTag(String name) {
        return Optional.ofNullable(TAGS.get(name));
    }

    static {
        for (Field field : XTag.class.getDeclaredFields()) {
            try {
                if (field.getType() == XTag.class) {
                    TAGS.put(field.getName(), (XTag<?>) field.get(null));
                }
            } catch (IllegalAccessException ex) {
                new IllegalStateException("Failed to get XTag " + field, ex).printStackTrace();
            }
        }
    }
}