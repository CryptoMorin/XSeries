package com.cryptomorin.xseries;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class XTag<T> {

    static XTag<XMaterial> ACACIA_LOGS;
    static XTag<XMaterial> ANIMALS_SPAWNABLE_ON;
    static XTag<XMaterial> ANVIL;
    static XTag<XMaterial> AXOLOTL_TEMPT_ITEMS;
    static XTag<XMaterial> AXOLOTLS_SPAWNABLE_ON;
    static XTag<XMaterial> AZALEA_GROWS_ON;
    static XTag<XMaterial> AZALEA_ROOT_REPLACEABLE;
    static XTag<XMaterial> BAMBOO_PLANTABLE_ON;
    static XTag<XMaterial> BANNERS;
    static XTag<XMaterial> BASE_STONE_NETHER;
    static XTag<XMaterial> BASE_STONE_OVERWORLD;
    static XTag<XMaterial> BEACON_BASE_BLOCKS;
    static XTag<XMaterial> BEDS;
    static XTag<XMaterial> BEE_GROWABLES;
    static XTag<XMaterial> BIG_DRIPLEAF_PLACEABLE;
    static XTag<XMaterial> BIRCH_LOGS;
    static XTag<XMaterial> BUTTONS;
    static XTag<XMaterial> CAMPFIRES;
    static XTag<XMaterial> CANDLE_CAKES;
    static XTag<XMaterial> CANDLES;
    static XTag<XMaterial> CARPETS;
    static XTag<XMaterial> CAULDRONS;
    static XTag<XMaterial> CAVE_VINES;
    static XTag<XMaterial> CLIMBABLE;
    static XTag<XMaterial> CLUSTER_MAX_HARVESTABLES;
    static XTag<XMaterial> COAL_ORES;
    static XTag<XMaterial> COPPER_ORES;
    static XTag<XMaterial> CORAL_PLANTS;
    static XTag<XMaterial> CORALS;
    static XTag<XMaterial> CRIMSON_STEMS;
    static XTag<XMaterial> CROPS;
    static XTag<XMaterial> CRYSTAL_SOUND_BLOCKS;
    static XTag<XMaterial> DARK_OAK_LOGS;
    static XTag<XMaterial> DEEPSLATE_ORE_REPLACEABLES;
    static XTag<XMaterial> DIAMOND_ORES;
    static XTag<XMaterial> DIRT;
    static XTag<XMaterial> DOORS;
    static XTag<XMaterial> DRAGON_IMMUNE;
    static XTag<XMaterial> DRIPSTONE_REPLACEABLE;
    static XTag<XMaterial> EMERALD_ORES;
    static XTag<XMaterial> ENDERMAN_HOLDABLE;
    static XTag<XMaterial> FEATURES_CANNOT_REPLACE;
    static XTag<XMaterial> FENCE_GATES;
    static XTag<XMaterial> FENCES;
    static XTag<XMaterial> FIRE;
    static XTag<XMaterial> FLOWER_POTS;
    static XTag<XMaterial> FLOWERS;
    static XTag<XMaterial> FOX_FOOD;
    static XTag<XMaterial> FOXES_SPAWNABLE_ON;
    static XTag<XMaterial> FREEZE_IMMUNE_WEARABLES;
    static XTag<XMaterial> GEODE_INVALID_BLOCKS;
    static XTag<XMaterial> GOATS_SPAWNABLE_ON;
    static XTag<XMaterial> GOLD_ORES;
    static XTag<XMaterial> GUARDED_BY_PIGLINS;
    static XTag<XMaterial> HOGLIN_REPELLENTS;
    static XTag<XMaterial> ICE;
    static XTag<XMaterial> IGNORED_BY_PIGLIN_BABIES;
    static XTag<XMaterial> IMPERMEABLE;
    static XTag<XMaterial> INFINIBURN_END;
    static XTag<XMaterial> INFINIBURN_NETHER;
    static XTag<XMaterial> INFINIBURN_OVERWORLD;
    static XTag<XMaterial> INSIDE_STEP_SOUND_BLOCKS;
    static XTag<XMaterial> IRON_ORES;
    static XTag<XMaterial> ITEMS_ARROWS;
    static XTag<XMaterial> ITEMS_BANNERS;
    static XTag<XMaterial> ITEMS_BEACON_PAYMENT_ITEMS;
    static XTag<XMaterial> ITEMS_BOATS;
    static XTag<XMaterial> ITEMS_COALS;
    static XTag<XMaterial> ITEMS_CREEPER_DROP_MUSIC_DISCS;
    static XTag<XMaterial> ITEMS_FISHES;
    static XTag<XMaterial> ITEMS_FURNACE_MATERIALS;
    static XTag<XMaterial> ITEMS_LECTERN_BOOKS;
    static XTag<XMaterial> ITEMS_MUSIC_DISCS;
    static XTag<XMaterial> ITEMS_PIGLIN_LOVED;
    static XTag<XMaterial> ITEMS_STONE_TOOL_MATERIALS;
    static XTag<XMaterial> WALL_BANNERS;
    static XTag<XMaterial> JUNGLE_LOGS;
    static XTag<XMaterial> LAPIS_ORES;
    static XTag<XMaterial> LAVA_POOL_STONE_CANNOT_REPLACE;
    static XTag<XMaterial> LEAVES;
    static XTag<XMaterial> LOGS;
    static XTag<XMaterial> LOGS_THAT_BURN;
    static XTag<XMaterial> LUSH_GROUND_REPLACEABLE;
    static XTag<XMaterial> MINEABLE_AXE;
    static XTag<XMaterial> MINEABLE_HOE;
    static XTag<XMaterial> MINEABLE_PICKAXE;
    static XTag<XMaterial> MINEABLE_SHOVEL;
    static XTag<XMaterial> MUSHROOMS_SPAWNABLE_ON;
    static XTag<XMaterial> MOSS_REPLACEABLE;
    static XTag<XMaterial> MUSHROOM_GROW_BLOCK;
    static XTag<XMaterial> NEEDS_DIAMOND_TOOL;
    static XTag<XMaterial> NEEDS_IRON_TOOL;
    static XTag<XMaterial> NEEDS_STONE_TOOL;
    static XTag<XMaterial> NON_FLAMMABLE_WOOD;
    static XTag<XMaterial> NYLIUM;
    static XTag<XMaterial> OAK_LOGS;
    static XTag<XMaterial> OCCLUDES_VIBRATION_SIGNALS;
    static XTag<XMaterial> PARROTS_SPAWNABLE_ON;
    static XTag<XMaterial> PIGLIN_FOOD;
    static XTag<XMaterial> PIGLIN_REPELLENTS;
    static XTag<XMaterial> PLANKS;
    static XTag<XMaterial> POLAR_BEARS_SPAWNABLE_ON_IN_FROZEN_OCEAN;
    static XTag<XMaterial> PORTALS;
    static XTag<XMaterial> PRESSURE_PLATES;
    static XTag<XMaterial> PREVENT_MOB_SPAWNING_INSIDE;
    static XTag<XMaterial> RABBITS_SPAWNABLE_ON;
    static XTag<XMaterial> RAILS;
    static XTag<XMaterial> REDSTONE_ORES;
    static XTag<XMaterial> REPLACEABLE_PLANTS;
    static XTag<XMaterial> SAND;
    static XTag<XMaterial> SAPLINGS;
    static XTag<XMaterial> SHULKER_BOXES;
    static XTag<XMaterial> SIGNS;
    static XTag<XMaterial> SMALL_DRIPLEAF_PLACEABLE;
    static XTag<XMaterial> SMALL_FLOWERS;
    static XTag<XMaterial> SNOW;
    static XTag<XMaterial> SOUL_FIRE_BASE_BLOCKS;
    static XTag<XMaterial> SOUL_SPEED_BLOCKS;
    static XTag<XMaterial> SPRUCE_LOGS;
    static XTag<XMaterial> STAIRS;
    static XTag<XMaterial> STANDING_SIGNS;
    static XTag<XMaterial> STONE_BRICKS;
    static XTag<XMaterial> STONE_ORE_REPLACEABLES;
    static XTag<XMaterial> STONE_PRESSURE_PLATES;
    static XTag<XMaterial> STRIDER_WARM_BLOCKS;
    static XTag<XMaterial> TALL_FLOWERS;
    static XTag<XMaterial> TERRACOTTA;
    static XTag<XMaterial> TRAPDOORS;
    static XTag<XMaterial> UNDERWATER_BONEMEALS;
    static XTag<XMaterial> UNSTABLE_BOTTOM_CENTER;
    static XTag<XMaterial> VALID_SPAWN;
    static XTag<XMaterial> WALL_CORALS;
    static XTag<XMaterial> WALL_POST_OVERRIDE;
    static XTag<XMaterial> WALL_SIGNS;
    static XTag<XMaterial> WALLS;
    static XTag<XMaterial> WARPED_STEMS;
    static XTag<XMaterial> WITHER_IMMUNE;
    static XTag<XMaterial> WITHER_SUMMON_BASE_BLOCKS;
    static XTag<XMaterial> WOLVES_SPAWNABLE_ON;
    static XTag<XMaterial> WOODEN_BUTTONS;
    static XTag<XMaterial> WOODEN_DOORS;
    static XTag<XMaterial> WOODEN_FENCES;
    static XTag<XMaterial> WOODEN_PRESSURE_PLATES;
    static XTag<XMaterial> WOODEN_SLABS;
    static XTag<XMaterial> WOODEN_TRAPDOORS;
    static XTag<XMaterial> WOOL;


    static {
        ACACIA_LOGS = new XTag<>(XMaterial.STRIPPED_ACACIA_LOG,
                XMaterial.ACACIA_LOG,
                XMaterial.ACACIA_WOOD,
                XMaterial.STRIPPED_ACACIA_WOOD);
        BIRCH_LOGS = new XTag<>(XMaterial.STRIPPED_BIRCH_LOG,
                XMaterial.BIRCH_LOG,
                XMaterial.BIRCH_WOOD,
                XMaterial.STRIPPED_BIRCH_WOOD);
        DARK_OAK_LOGS = new XTag<>(XMaterial.STRIPPED_DARK_OAK_LOG,
                XMaterial.DARK_OAK_LOG,
                XMaterial.DARK_OAK_WOOD,
                XMaterial.STRIPPED_DARK_OAK_WOOD);
        JUNGLE_LOGS = new XTag<>(XMaterial.STRIPPED_JUNGLE_LOG,
                XMaterial.JUNGLE_LOG,
                XMaterial.JUNGLE_WOOD,
                XMaterial.STRIPPED_JUNGLE_WOOD);
        ANIMALS_SPAWNABLE_ON = new XTag<>(XMaterial.GRASS_BLOCK);
        ANVIL = new XTag<>(XMaterial.ANVIL,
                XMaterial.CHIPPED_ANVIL,
                XMaterial.DAMAGED_ANVIL);
        AXOLOTL_TEMPT_ITEMS = new XTag<>(XMaterial.TROPICAL_FISH_BUCKET);
        AXOLOTLS_SPAWNABLE_ON = new XTag<>(XMaterial.CLAY);
        TERRACOTTA = new XTag<>(XMaterial.ORANGE_TERRACOTTA,
                XMaterial.LIGHT_GRAY_TERRACOTTA,
                XMaterial.LIME_TERRACOTTA,
                XMaterial.YELLOW_TERRACOTTA,
                XMaterial.CYAN_TERRACOTTA,
                XMaterial.GREEN_TERRACOTTA,
                XMaterial.PURPLE_TERRACOTTA,
                XMaterial.PINK_TERRACOTTA,
                XMaterial.BROWN_TERRACOTTA,
                XMaterial.GRAY_TERRACOTTA,
                XMaterial.TERRACOTTA,
                XMaterial.WHITE_TERRACOTTA,
                XMaterial.MAGENTA_TERRACOTTA,
                XMaterial.LIGHT_BLUE_TERRACOTTA,
                XMaterial.RED_TERRACOTTA,
                XMaterial.BLACK_TERRACOTTA,
                XMaterial.BLUE_TERRACOTTA);
        SNOW = new XTag<>(XMaterial.SNOW_BLOCK,
                XMaterial.SNOW,
                XMaterial.POWDER_SNOW);
        SAND = new XTag<>(XMaterial.SAND,
                XMaterial.RED_SAND);
        DIRT.addValues(XMaterial.MOSS_BLOCK,
                XMaterial.COARSE_DIRT,
                XMaterial.PODZOL,
                XMaterial.DIRT,
                XMaterial.ROOTED_DIRT,
                XMaterial.MYCELIUM,
                XMaterial.GRASS_BLOCK);
        CAVE_VINES = new XTag<>(XMaterial.CAVE_VINES,
                XMaterial.CAVE_VINES_PLANT);
        BASE_STONE_NETHER = new XTag<>(XMaterial.NETHERRACK,
                XMaterial.BASALT,
                XMaterial.BLACKSTONE);
        BASE_STONE_OVERWORLD = new XTag<>(XMaterial.TUFF,
                XMaterial.DIORITE,
                XMaterial.DEEPSLATE,
                XMaterial.ANDESITE,
                XMaterial.GRANITE,
                XMaterial.STONE);
        ITEMS_BANNERS = new XTag<>(XMaterial.ORANGE_BANNER,
                XMaterial.PINK_BANNER,
                XMaterial.PURPLE_BANNER,
                XMaterial.GRAY_BANNER,
                XMaterial.BROWN_BANNER,
                XMaterial.BLUE_BANNER,
                XMaterial.BLACK_BANNER,
                XMaterial.YELLOW_BANNER,
                XMaterial.WHITE_BANNER,
                XMaterial.LIME_BANNER,
                XMaterial.LIGHT_GRAY_BANNER,
                XMaterial.RED_BANNER,
                XMaterial.CYAN_BANNER,
                XMaterial.MAGENTA_BANNER,
                XMaterial.LIGHT_BLUE_BANNER,
                XMaterial.GREEN_BANNER);
        WALL_BANNERS = new XTag<>(XMaterial.ORANGE_WALL_BANNER,
                XMaterial.PINK_WALL_BANNER,
                XMaterial.PURPLE_WALL_BANNER,
                XMaterial.GRAY_WALL_BANNER,
                XMaterial.BROWN_WALL_BANNER,
                XMaterial.BLUE_WALL_BANNER,
                XMaterial.BLACK_WALL_BANNER,
                XMaterial.YELLOW_WALL_BANNER,
                XMaterial.WHITE_WALL_BANNER,
                XMaterial.LIME_WALL_BANNER,
                XMaterial.LIGHT_GRAY_WALL_BANNER,
                XMaterial.RED_WALL_BANNER,
                XMaterial.CYAN_WALL_BANNER,
                XMaterial.MAGENTA_WALL_BANNER,
                XMaterial.LIGHT_BLUE_WALL_BANNER,
                XMaterial.GREEN_WALL_BANNER);
        BEACON_BASE_BLOCKS = new XTag<>(XMaterial.NETHERITE_BLOCK,
                XMaterial.GOLD_BLOCK,
                XMaterial.IRON_BLOCK,
                XMaterial.EMERALD_BLOCK,
                XMaterial.DIAMOND_BLOCK);
        BEDS = new XTag<>(XMaterial.LIGHT_GRAY_BED,
                XMaterial.CYAN_BED,
                XMaterial.GRAY_BED,
                XMaterial.WHITE_BED,
                XMaterial.PURPLE_BED,
                XMaterial.MAGENTA_BED,
                XMaterial.LIME_BED,
                XMaterial.BLUE_BED,
                XMaterial.GREEN_BED,
                XMaterial.BLACK_BED,
                XMaterial.PINK_BED,
                XMaterial.YELLOW_BED,
                XMaterial.LIGHT_BLUE_BED,
                XMaterial.ORANGE_BED,
                XMaterial.BROWN_BED,
                XMaterial.RED_BED);
        CROPS = new XTag<>(XMaterial.CARROTS,
                XMaterial.POTATOES,
                XMaterial.WHEAT,
                XMaterial.MELON_STEM,
                XMaterial.BEETROOTS,
                XMaterial.PUMPKIN_STEM);
        WOODEN_BUTTONS = new XTag<>(XMaterial.BIRCH_BUTTON,
                XMaterial.OAK_BUTTON,
                XMaterial.ACACIA_BUTTON,
                XMaterial.CRIMSON_BUTTON,
                XMaterial.WARPED_BUTTON,
                XMaterial.SPRUCE_BUTTON,
                XMaterial.JUNGLE_BUTTON,
                XMaterial.DARK_OAK_BUTTON);
        CAMPFIRES = new XTag<>(XMaterial.CAMPFIRE,
                XMaterial.SOUL_CAMPFIRE);
        CANDLE_CAKES = new XTag<>(XMaterial.CANDLE_CAKE,
                XMaterial.ORANGE_CANDLE_CAKE,
                XMaterial.LIGHT_BLUE_CANDLE_CAKE,
                XMaterial.GRAY_CANDLE_CAKE,
                XMaterial.BLACK_CANDLE_CAKE,
                XMaterial.MAGENTA_CANDLE_CAKE,
                XMaterial.PINK_CANDLE_CAKE,
                XMaterial.BLUE_CANDLE_CAKE,
                XMaterial.GREEN_CANDLE_CAKE,
                XMaterial.CYAN_CANDLE_CAKE,
                XMaterial.PURPLE_CANDLE_CAKE,
                XMaterial.YELLOW_CANDLE,
                XMaterial.LIME_CANDLE_CAKE,
                XMaterial.LIGHT_GRAY_CANDLE_CAKE,
                XMaterial.WHITE_CANDLE_CAKE,
                XMaterial.BROWN_CANDLE_CAKE,
                XMaterial.RED_CANDLE_CAKE);
        CANDLES = new XTag<>(XMaterial.CANDLE,
                XMaterial.ORANGE_CANDLE,
                XMaterial.LIGHT_BLUE_CANDLE,
                XMaterial.GRAY_CANDLE,
                XMaterial.BLACK_CANDLE,
                XMaterial.MAGENTA_CANDLE,
                XMaterial.PINK_CANDLE,
                XMaterial.BLUE_CANDLE,
                XMaterial.GREEN_CANDLE,
                XMaterial.CYAN_CANDLE,
                XMaterial.PURPLE_CANDLE,
                XMaterial.YELLOW_CANDLE,
                XMaterial.LIME_CANDLE,
                XMaterial.LIGHT_GRAY_CANDLE,
                XMaterial.WHITE_CANDLE,
                XMaterial.BROWN_CANDLE,
                XMaterial.RED_CANDLE);
        CARPETS = new XTag<>(XMaterial.ORANGE_CARPET,
                XMaterial.LIGHT_BLUE_CARPET,
                XMaterial.GRAY_CARPET,
                XMaterial.BLACK_CARPET,
                XMaterial.MAGENTA_CARPET,
                XMaterial.PINK_CARPET,
                XMaterial.BLUE_CARPET,
                XMaterial.GREEN_CARPET,
                XMaterial.CYAN_CARPET,
                XMaterial.PURPLE_CARPET,
                XMaterial.YELLOW_CARPET,
                XMaterial.LIME_CARPET,
                XMaterial.LIGHT_GRAY_CARPET,
                XMaterial.WHITE_CARPET,
                XMaterial.BROWN_CARPET,
                XMaterial.RED_CARPET);
        CAULDRONS = new XTag<>(XMaterial.CAULDRON,
                XMaterial.LAVA_CAULDRON,
                XMaterial.POWDER_SNOW_CAULDRON,
                XMaterial.WATER_CAULDRON);
        CLIMBABLE = new XTag<>(XMaterial.SCAFFOLDING,
                XMaterial.WEEPING_VINES_PLANT,
                XMaterial.WEEPING_VINES,
                XMaterial.TWISTING_VINES,
                XMaterial.TWISTING_VINES_PLANT,
                XMaterial.VINE,
                XMaterial.LADDER);
        CLIMBABLE.inheritFrom(CAVE_VINES);
        CLUSTER_MAX_HARVESTABLES = new XTag<>(XMaterial.DIAMOND_PICKAXE,
                XMaterial.GOLDEN_PICKAXE,
                XMaterial.STONE_PICKAXE,
                XMaterial.NETHERITE_PICKAXE,
                XMaterial.WOODEN_PICKAXE,
                XMaterial.IRON_PICKAXE);
        COAL_ORES = new XTag<>(XMaterial.COAL_ORE,
                XMaterial.DEEPSLATE_COAL_ORE);
        COPPER_ORES = new XTag<>(XMaterial.COPPER_ORE,
                XMaterial.DEEPSLATE_COPPER_ORE);
        CORAL_PLANTS = new XTag<>(XMaterial.FIRE_CORAL,
                XMaterial.TUBE_CORAL,
                XMaterial.BRAIN_CORAL,
                XMaterial.HORN_CORAL,
                XMaterial.BUBBLE_CORAL);
        CRIMSON_STEMS = new XTag<>(XMaterial.CRIMSON_HYPHAE,
                XMaterial.STRIPPED_CRIMSON_STEM,
                XMaterial.CRIMSON_STEM);
        CRYSTAL_SOUND_BLOCKS = new XTag<>(XMaterial.AMETHYST_BLOCK,
                XMaterial.BUDDING_AMETHYST);
        DEEPSLATE_ORE_REPLACEABLES = new XTag<>(XMaterial.TUFF,
                XMaterial.DEEPSLATE);
        DIAMOND_ORES = new XTag<>(XMaterial.DIAMOND_ORE,
                XMaterial.DEEPSLATE_DIAMOND_ORE);
        DOORS = new XTag<>(XMaterial.OAK_DOOR,
                XMaterial.BIRCH_DOOR,
                XMaterial.SPRUCE_DOOR,
                XMaterial.JUNGLE_DOOR,
                XMaterial.ACACIA_DOOR,
                XMaterial.WARPED_DOOR,
                XMaterial.CRIMSON_DOOR,
                XMaterial.DARK_OAK_DOOR,
                XMaterial.IRON_DOOR);
        DRAGON_IMMUNE = new XTag<>(XMaterial.END_GATEWAY,
                XMaterial.BEDROCK,
                XMaterial.IRON_BARS,
                XMaterial.CHAIN_COMMAND_BLOCK,
                XMaterial.BARRIER,
                XMaterial.END_PORTAL_FRAME,
                XMaterial.OBSIDIAN,
                XMaterial.JIGSAW,
                XMaterial.RESPAWN_ANCHOR,
                XMaterial.STRUCTURE_BLOCK,
                XMaterial.END_PORTAL,
                XMaterial.COMMAND_BLOCK,
                XMaterial.REPEATING_COMMAND_BLOCK,
                XMaterial.MOVING_PISTON,
                XMaterial.END_STONE,
                XMaterial.CRYING_OBSIDIAN);
        EMERALD_ORES = new XTag<>(XMaterial.EMERALD_ORE,
                XMaterial.DEEPSLATE_EMERALD_ORE);
        NYLIUM = new XTag<>(XMaterial.CRIMSON_NYLIUM,
                XMaterial.WARPED_NYLIUM);
        SMALL_FLOWERS = new XTag<>(XMaterial.RED_TULIP,
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
                XMaterial.WITHER_ROSE);
        TALL_FLOWERS = new XTag<>(XMaterial.PEONY,
                XMaterial.SUNFLOWER,
                XMaterial.LILAC,
                XMaterial.ROSE_BUSH);
        FEATURES_CANNOT_REPLACE = new XTag<>(XMaterial.SPAWNER,
                XMaterial.END_PORTAL_FRAME,
                XMaterial.BEDROCK,
                XMaterial.CHEST);
        FENCE_GATES = new XTag<>(XMaterial.ACACIA_FENCE_GATE,
                XMaterial.WARPED_FENCE_GATE,
                XMaterial.SPRUCE_FENCE_GATE,
                XMaterial.CRIMSON_FENCE_GATE,
                XMaterial.BIRCH_FENCE_GATE,
                XMaterial.DARK_OAK_FENCE_GATE,
                XMaterial.JUNGLE_FENCE_GATE,
                XMaterial.OAK_FENCE_GATE);
        FENCES = new XTag<>(XMaterial.ACACIA_FENCE,
                XMaterial.WARPED_FENCE,
                XMaterial.SPRUCE_FENCE,
                XMaterial.CRIMSON_FENCE,
                XMaterial.BIRCH_FENCE,
                XMaterial.DARK_OAK_FENCE,
                XMaterial.JUNGLE_FENCE,
                XMaterial.OAK_FENCE,
                XMaterial.NETHER_BRICK_FENCE);
        FIRE = new XTag<>(XMaterial.FIRE,
                XMaterial.SOUL_FIRE);
        FLOWER_POTS = new XTag<>(XMaterial.POTTED_OAK_SAPLING,
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
                XMaterial.POTTED_BIRCH_SAPLING);
        FOX_FOOD = new XTag<>(XMaterial.GLOW_BERRIES,
                XMaterial.SWEET_BERRIES);
        FOXES_SPAWNABLE_ON = new XTag<>(XMaterial.SNOW,
                XMaterial.SNOW_BLOCK,
                XMaterial.PODZOL,
                XMaterial.GRASS_BLOCK,
                XMaterial.COARSE_DIRT);
        FREEZE_IMMUNE_WEARABLES = new XTag<>(XMaterial.LEATHER_BOOTS,
                XMaterial.LEATHER_CHESTPLATE,
                XMaterial.LEATHER_HELMET,
                XMaterial.LEATHER_LEGGINGS,
                XMaterial.LEATHER_HORSE_ARMOR);
        ICE = new XTag<>(XMaterial.ICE,
                XMaterial.PACKED_ICE,
                XMaterial.BLUE_ICE,
                XMaterial.FROSTED_ICE);
        GEODE_INVALID_BLOCKS = new XTag<>(XMaterial.BEDROCK,
                XMaterial.WATER,
                XMaterial.LAVA,
                XMaterial.ICE,
                XMaterial.PACKED_ICE,
                XMaterial.BLUE_ICE);
        GOLD_ORES = new XTag<>(XMaterial.GOLD_ORE,
                XMaterial.DEEPSLATE_GOLD_ORE,
                XMaterial.NETHER_GOLD_ORE);
        SHULKER_BOXES = new XTag<>(XMaterial.LIGHT_BLUE_SHULKER_BOX,
                XMaterial.BROWN_SHULKER_BOX,
                XMaterial.BLUE_SHULKER_BOX,
                XMaterial.PINK_SHULKER_BOX,
                XMaterial.SHULKER_BOX,
                XMaterial.GRAY_SHULKER_BOX,
                XMaterial.ORANGE_SHULKER_BOX,
                XMaterial.RED_SHULKER_BOX,
                XMaterial.GREEN_SHULKER_BOX,
                XMaterial.CYAN_SHULKER_BOX,
                XMaterial.YELLOW_SHULKER_BOX,
                XMaterial.PURPLE_SHULKER_BOX,
                XMaterial.LIGHT_GRAY_SHULKER_BOX,
                XMaterial.MAGENTA_SHULKER_BOX,
                XMaterial.BLACK_SHULKER_BOX,
                XMaterial.WHITE_SHULKER_BOX,
                XMaterial.LIME_SHULKER_BOX);
        HOGLIN_REPELLENTS = new XTag<>(XMaterial.WARPED_FUNGUS,
                XMaterial.NETHER_PORTAL,
                XMaterial.POTTED_WARPED_FUNGUS,
                XMaterial.RESPAWN_ANCHOR);
        IGNORED_BY_PIGLIN_BABIES = new XTag<>(XMaterial.LEATHER);
        IMPERMEABLE = new XTag<>(XMaterial.BLUE_STAINED_GLASS,
                XMaterial.BROWN_STAINED_GLASS,
                XMaterial.GREEN_STAINED_GLASS,
                XMaterial.YELLOW_STAINED_GLASS,
                XMaterial.RED_STAINED_GLASS,
                XMaterial.BLACK_STAINED_GLASS,
                XMaterial.TINTED_GLASS,
                XMaterial.CYAN_STAINED_GLASS,
                XMaterial.MAGENTA_STAINED_GLASS,
                XMaterial.GRAY_STAINED_GLASS,
                XMaterial.PURPLE_STAINED_GLASS,
                XMaterial.GLASS,
                XMaterial.WHITE_STAINED_GLASS,
                XMaterial.LIGHT_BLUE_STAINED_GLASS,
                XMaterial.LIGHT_GRAY_STAINED_GLASS,
                XMaterial.PINK_STAINED_GLASS,
                XMaterial.ORANGE_STAINED_GLASS,
                XMaterial.LIME_STAINED_GLASS);
        INFINIBURN_END = new XTag<>(XMaterial.BEDROCK,
                XMaterial.NETHERRACK,
                XMaterial.MAGMA_BLOCK);
        INFINIBURN_NETHER = new XTag<>(XMaterial.NETHERRACK,
                XMaterial.MAGMA_BLOCK);
        INFINIBURN_OVERWORLD = new XTag<>(XMaterial.NETHERRACK,
                XMaterial.MAGMA_BLOCK);
        INSIDE_STEP_SOUND_BLOCKS = new XTag<>(XMaterial.SNOW, XMaterial.POWDER_SNOW);
        IRON_ORES = new XTag<>(XMaterial.IRON_ORE,
                XMaterial.DEEPSLATE_IRON_ORE);
        ITEMS_ARROWS = new XTag<>(XMaterial.ARROW,
                XMaterial.SPECTRAL_ARROW,
                XMaterial.TIPPED_ARROW);
        ITEMS_BEACON_PAYMENT_ITEMS = new XTag<>(XMaterial.EMERALD,
                XMaterial.DIAMOND,
                XMaterial.NETHERITE_INGOT,
                XMaterial.IRON_INGOT,
                XMaterial.GOLD_INGOT);
        ITEMS_BOATS = new XTag<>(XMaterial.OAK_BOAT,
                XMaterial.ACACIA_BOAT,
                XMaterial.DARK_OAK_BOAT,
                XMaterial.BIRCH_BOAT,
                XMaterial.SPRUCE_BOAT,
                XMaterial.JUNGLE_BOAT);
        ITEMS_COALS = new XTag<>(XMaterial.COAL,
                XMaterial.CHARCOAL);
        ITEMS_CREEPER_DROP_MUSIC_DISCS = new XTag<>(XMaterial.MUSIC_DISC_BLOCKS,
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
                XMaterial.MUSIC_DISC_STRAD);
        ITEMS_FISHES = new XTag<>(XMaterial.TROPICAL_FISH,
                XMaterial.SALMON,
                XMaterial.PUFFERFISH,
                XMaterial.COOKED_COD,
                XMaterial.COD,
                XMaterial.COOKED_SALMON);
        ITEMS_FURNACE_MATERIALS = new XTag<>();
        ITEMS_LECTERN_BOOKS = new XTag<>(XMaterial.WRITABLE_BOOK,
                XMaterial.WRITTEN_BOOK);
        ITEMS_STONE_TOOL_MATERIALS = new XTag<>(XMaterial.COBBLED_DEEPSLATE,
                XMaterial.BLACKSTONE,
                XMaterial.COBBLESTONE);
        LAPIS_ORES = new XTag<>(XMaterial.LAPIS_ORE,
                XMaterial.DEEPSLATE_LAPIS_ORE);
        LEAVES = new XTag<>(XMaterial.SPRUCE_LEAVES,
                XMaterial.ACACIA_LEAVES,
                XMaterial.DARK_OAK_LEAVES,
                XMaterial.AZALEA_LEAVES,
                XMaterial.JUNGLE_LEAVES,
                XMaterial.FLOWERING_AZALEA_LEAVES,
                XMaterial.BIRCH_LEAVES,
                XMaterial.OAK_LEAVES);





        AZALEA_GROWS_ON = new XTag<>(XMaterial.SNOW_BLOCK, XMaterial.POWDER_SNOW);
        AZALEA_GROWS_ON.inheritFrom(TERRACOTTA, SAND, DIRT);
        AZALEA_ROOT_REPLACEABLE = new XTag<>(XMaterial.CLAY, XMaterial.GRAVEL);
        AZALEA_ROOT_REPLACEABLE.inheritFrom(AZALEA_GROWS_ON, CAVE_VINES, BASE_STONE_OVERWORLD);
        BAMBOO_PLANTABLE_ON = new XTag<>(XMaterial.GRAVEL, XMaterial.BAMBOO_SAPLING, XMaterial.BAMBOO);
        BAMBOO_PLANTABLE_ON.inheritFrom(DIRT, SAND);
        BANNERS = new XTag<>();
        BANNERS.inheritFrom(ITEMS_BANNERS, WALL_BANNERS);
        BEE_GROWABLES = new XTag<>(XMaterial.SWEET_BERRY_BUSH);
        BEE_GROWABLES.inheritFrom(CROPS, CAVE_VINES);
        BIG_DRIPLEAF_PLACEABLE = new XTag<>(XMaterial.CLAY, XMaterial.FARMLAND);
        BIG_DRIPLEAF_PLACEABLE.inheritFrom(DIRT);
        BUTTONS = new XTag<>(XMaterial.STONE_BUTTON,
                XMaterial.POLISHED_BLACKSTONE_BUTTON);
        BUTTONS.inheritFrom(WOODEN_BUTTONS);
        CORALS = new XTag<>(XMaterial.FIRE_CORAL_FAN,
                XMaterial.TUBE_CORAL_FAN,
                XMaterial.BRAIN_CORAL_FAN,
                XMaterial.HORN_CORAL_FAN,
                XMaterial.BUBBLE_CORAL_FAN);
        CORALS.inheritFrom(CORAL_PLANTS);
        DRIPSTONE_REPLACEABLE = new XTag<>(XMaterial.DIRT);
        DRIPSTONE_REPLACEABLE.inheritFrom(BASE_STONE_OVERWORLD);
        ENDERMAN_HOLDABLE = new XTag<>(XMaterial.TNT,
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
                XMaterial.CLAY);
        ENDERMAN_HOLDABLE.inheritFrom(DIRT, NYLIUM, SAND, SMALL_FLOWERS);
        FLOWERS = new XTag<>(XMaterial.FLOWERING_AZALEA,
                XMaterial.FLOWERING_AZALEA_LEAVES);
        FLOWERS.inheritFrom(SMALL_FLOWERS, TALL_FLOWERS);
        GOATS_SPAWNABLE_ON = new XTag<>(XMaterial.GRAVEL,
                XMaterial.STONE,
                XMaterial.PACKED_ICE);
        GOATS_SPAWNABLE_ON.inheritFrom(SNOW);
        GUARDED_BY_PIGLINS = new XTag<>(XMaterial.GOLD_BLOCK,
                XMaterial.ENDER_CHEST,
                XMaterial.RAW_GOLD_BLOCK,
                XMaterial.GILDED_BLACKSTONE,
                XMaterial.CHEST,
                XMaterial.BARREL,
                XMaterial.TRAPPED_CHEST);
        GUARDED_BY_PIGLINS.inheritFrom(SHULKER_BOXES, GOLD_ORES);
        ITEMS_MUSIC_DISCS = new XTag<>(XMaterial.MUSIC_DISC_OTHERSIDE,
                XMaterial.MUSIC_DISC_PIGSTEP);
        ITEMS_MUSIC_DISCS.inheritFrom(ITEMS_CREEPER_DROP_MUSIC_DISCS);
        ITEMS_PIGLIN_LOVED = new XTag<>(XMaterial.GOLD_BLOCK,
                XMaterial.RAW_GOLD,
                XMaterial.GLISTERING_MELON_SLICE,
                XMaterial.GOLDEN_HORSE_ARMOR,
                XMaterial.GOLDEN_LEGGINGS,
                XMaterial.LIGHT_WEIGHTED_PRESSURE_PLATE,
                XMaterial.GOLDEN_SWORD,
                XMaterial.GOLDEN_AXE,
                XMaterial.BELL,
                XMaterial.ENCHANTED_GOLDEN_APPLE,
                XMaterial.RAW_GOLD_BLOCK,
                XMaterial.GILDED_BLACKSTONE,
                XMaterial.CLOCK,
                XMaterial.GOLDEN_HELMET,
                XMaterial.GOLDEN_CARROT,
                XMaterial.GOLDEN_APPLE,
                XMaterial.GOLDEN_CHESTPLATE,
                XMaterial.GOLDEN_BOOTS,
                XMaterial.GOLDEN_SHOVEL,
                XMaterial.GOLDEN_HOE,
                XMaterial.GOLD_INGOT);
        ITEMS_PIGLIN_LOVED.inheritFrom(GOLD_ORES);


    }

    private XTag(T... values) {
        this.values = new HashSet<>();
        this.addValues(values);
    }

    private final Set<T> values;

    public Set<T> getValues() {
        return Collections.unmodifiableSet(values);
    }

    private void addValues(T... values) {
        this.values.addAll(Arrays.asList(values));
    }

    private void inheritFrom(XTag<T>... values) {
        for (XTag<T> value : values) {
            for (T valueValue : value.getValues()) {
                addValues(valueValue);
            }
        }
    }

}
