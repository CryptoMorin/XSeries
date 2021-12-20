package com.cryptomorin.xseries;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

@SuppressWarnings("unchecked")
public final class XTag<@NonNull T extends Enum<T>> {

    /**
     * Tag representing all acacia log and bark variants
     */
    public static @NonNull XTag<XMaterial> ACACIA_LOGS;
    /**
     * Tag representing all possible blocks available for animals to spawn on
     */
    public final static @NonNull XTag<XMaterial> ANIMALS_SPAWNABLE_ON;
    /**
     * Tag representing all variants of anvil
     */
    public final static @NonNull XTag<XMaterial> ANVIL;
    /**
     * Tag representing all items that can tempt axolotl
     */
    public final static @NonNull XTag<XMaterial> AXOLOTL_TEMPT_ITEMS;
    /**
     * Tag representing all possible blocks for axolotls to spawn on
     */
    public final static @NonNull XTag<XMaterial> AXOLOTLS_SPAWNABLE_ON;
    /**
     * Tag representing all possible blocks for azalea to grow on
     */
    public final static @NonNull XTag<XMaterial> AZALEA_GROWS_ON;
    /**
     * Tag representing all possible blocks that can be replaced by azalea
     */
    public final static @NonNull XTag<XMaterial> AZALEA_ROOT_REPLACEABLE;
    /**
     * Tag representing all possible blocks bamboo may be planted on
     */
    public final static @NonNull XTag<XMaterial> BAMBOO_PLANTABLE_ON;
    /**
     * Tag representing all banner blocks
     */
    public static @NonNull XTag<XMaterial> BANNERS;
    /**
     * Tag representing the nether base materials
     */
    public final static @NonNull XTag<XMaterial> BASE_STONE_NETHER;
    /**
     * Tag representing the overworld base materials
     */
    public final static @NonNull XTag<XMaterial> BASE_STONE_OVERWORLD;
    /**
     * Tag representing all possible blocks that can be used as beacon base
     */
    public final static @NonNull XTag<XMaterial> BEACON_BASE_BLOCKS;
    /**
     * Tag representing all possible variants of bed
     */
    public static @NonNull XTag<XMaterial> BEDS;
    /**
     * Tag representing all possible blocks/crops that be grown by bees
     */
    public final static @NonNull XTag<XMaterial> BEE_GROWABLES;
    /**
     * Tag representing all possible blocks big dripleaf may be planted on
     */
    public final static @NonNull XTag<XMaterial> BIG_DRIPLEAF_PLACEABLE;
    /**
     * Tag representing all birch log and bark variants
     */
    public static @NonNull XTag<XMaterial> BIRCH_LOGS;
    /**
     * Tag representing all possible variants of buttons
     */
    public final static @NonNull XTag<XMaterial> BUTTONS;
    /**
     * Tag representing all possible variants of campfires
     */
    public final static @NonNull XTag<XMaterial> CAMPFIRES;
    /**
     * Tag representing all possible variants of candle cakes
     */
    public static @NonNull XTag<XMaterial> CANDLE_CAKES;
    /**
     * Tag representing all possible variants of candles
     */
    public static @NonNull XTag<XMaterial> CANDLES;
    /**
     * Tag representing all possible variants of carpets
     */
    public static @NonNull XTag<XMaterial> CARPETS;
    /**
     * Tag representing all possible variants of cauldrons
     */
    public final static @NonNull XTag<XMaterial> CAULDRONS;
    /**
     * Tag representing all possible variants of cave vines
     */
    public final static @NonNull XTag<XMaterial> CAVE_VINES;
    /**
     * Tag representing all climbable blocks
     */
    public final static @NonNull XTag<XMaterial> CLIMBABLE;
    /**
     * Tag representing all preferred items for harvesting clusters{unused as of 1.18}
     */
    public final static @NonNull XTag<XMaterial> CLUSTER_MAX_HARVESTABLES;
    /**
     * Tag representing all possible variants of coal ores
     */
    public final static @NonNull XTag<XMaterial> COAL_ORES;
    /**
     * Tag representing all possible variants of concrete
     */
    public static @NonNull XTag<XMaterial> CONCRETE;
    /**
     *
     */
    public static @NonNull XTag<XMaterial> CONCRETE_POWDER;
    /**
     * Tag representing all possible variants of copper ores
     */
    public final static @NonNull XTag<XMaterial> COPPER_ORES;
    /**
     * Tag representing all non-walled coral fans
     */
    public final static @NonNull XTag<XMaterial> CORAL_FANS;
    /**
     * Tag representing all coral plants
     */
    public final static @NonNull XTag<XMaterial> CORAL_PLANTS;
    /**
     * Tag representing all corals
     */
    public final static @NonNull XTag<XMaterial> CORALS;
    /**
     * Tag representing all crimson log and bark variants
     */
    public final static @NonNull XTag<XMaterial> CRIMSON_STEMS;
    /**
     * Tag representing all crops
     */
    public final static @NonNull XTag<XMaterial> CROPS;
    /**
     * Tag representing all possible blocks that can make crystal sounds
     */
    public final static @NonNull XTag<XMaterial> CRYSTAL_SOUND_BLOCKS;
    /**
     * Tag representing all dark oak log and bark variants
     */
    public static @NonNull XTag<XMaterial> DARK_OAK_LOGS;
    /**
     * Tag representing all possible blocks that may be replaced by deepslate ores
     */
    public final static @NonNull XTag<XMaterial> DEEPSLATE_ORE_REPLACEABLES;
    /**
     * Tag representing all possible variants of diamond ores
     */
    public final static @NonNull XTag<XMaterial> DIAMOND_ORES;
    /**
     * Tag representing all dirt
     */
    public final static @NonNull XTag<XMaterial> DIRT;
    /**
     * Tag representing all possible types of doors
     */
    public final static @NonNull XTag<XMaterial> DOORS;
    /**
     * Tag representing all blocks that can't be destroyed by dragons
     */
    public final static @NonNull XTag<XMaterial> DRAGON_IMMUNE;
    /**
     * Tag representing all possible blocks that can be replaced by dripstone
     */
    public final static @NonNull XTag<XMaterial> DRIPSTONE_REPLACEABLE;
    /**
     * Tag representing all variants of emerald ores
     */
    public final static @NonNull XTag<XMaterial> EMERALD_ORES;
    /**
     * Tag representing all possible blocks that can be picked up by endermen
     */
    public final static @NonNull XTag<XMaterial> ENDERMAN_HOLDABLE;
    /**
     * Tag representing all blocks that cant be replaced by world generation features
     */
    public final static @NonNull XTag<XMaterial> FEATURES_CANNOT_REPLACE;
    /**
     * Tag representing all possible variants of fence gates
     */
    public final static @NonNull XTag<XMaterial> FENCE_GATES;
    /**
     * Tag representing all possible variants of fences
     */
    public final static @NonNull XTag<XMaterial> FENCES;
    /**
     * Tag representing all possible variants fire
     */
    public final static @NonNull XTag<XMaterial> FIRE;
    /**
     * Tag representing all possible variants of flower pots
     */
    public final static @NonNull XTag<XMaterial> FLOWER_POTS;
    /**
     * Tag representing all possible types of flowers
     */
    public final static @NonNull XTag<XMaterial> FLOWERS;
    /**
     * Tag representing all items can be used as food for fox
     */
    public final static @NonNull XTag<XMaterial> FOX_FOOD;
    /**
     * Tag representing all possible blocks foxes may spawn on
     */
    public final static @NonNull XTag<XMaterial> FOXES_SPAWNABLE_ON;
    /**
     * Tag representing all possible items can be used to avoid freezing
     */
    public final static @NonNull XTag<XMaterial> FREEZE_IMMUNE_WEARABLES;
    /**
     * Tag representing all blocks that geodes will not spawn in
     */
    public final static @NonNull XTag<XMaterial> GEODE_INVALID_BLOCKS;
    /**
     * Tag representing all variants of glass
     */
    public static @NonNull XTag<XMaterial> GLASS;
    /**
     * Tag representing all possible variants of glazed terracotta
     */
    public static @NonNull XTag<XMaterial> GLAZED_TERRACOTTA;
    /**
     * Tag representing all possible blocks goats may spawn on
     */
    public final static @NonNull XTag<XMaterial> GOATS_SPAWNABLE_ON;
    /**
     * Tag representing all possible variants of gold ores
     */
    public final static @NonNull XTag<XMaterial> GOLD_ORES;
    /**
     * Tag representing all block types that are guarded by piglins
     */
    public final static @NonNull XTag<XMaterial> GUARDED_BY_PIGLINS;
    /**
     * Tag representing all block types that repel hoglins
     */
    public final static @NonNull XTag<XMaterial> HOGLIN_REPELLENTS;
    /**
     * Tag representing all possible variants of ice
     */
    public final static @NonNull XTag<XMaterial> ICE;
    /**
     * Tag representing all items ignored by baby piglins
     */
    public final static @NonNull XTag<XMaterial> IGNORED_BY_PIGLIN_BABIES;
    /**
     * Tag representing all possible block types that do not drip water/lava
     */
    public final static @NonNull XTag<XMaterial> IMPERMEABLE;
    /**
     * Tag representing all block types that can burn for infinitely long in the end
     */
    public final static @NonNull XTag<XMaterial> INFINIBURN_END;
    /**
     * Tag representing all block types that can burn for infinitely long in the nether
     */
    public final static @NonNull XTag<XMaterial> INFINIBURN_NETHER;
    /**
     * Tag representing all block types that can burn for infinitely long in the overworld
     */
    public final static @NonNull XTag<XMaterial> INFINIBURN_OVERWORLD;
    /**
     * Tag representing all block types that play muffled step sounds
     */
    public final static @NonNull XTag<XMaterial> INSIDE_STEP_SOUND_BLOCKS;
    /**
     * Tag representing all possible variants of iron ores
     */
    public final static @NonNull XTag<XMaterial> IRON_ORES;
    /**
     * Tag representing all possible variants of arrows
     */
    public final static @NonNull XTag<XMaterial> ITEMS_ARROWS;
    /**
     * Tag representing all items that can be used as banners
     */
    public static @NonNull XTag<XMaterial> ITEMS_BANNERS;
    /**
     * Tag representing all items that can be used to fuel beacon
     */
    public final static @NonNull XTag<XMaterial> ITEMS_BEACON_PAYMENT_ITEMS;
    /**
     * Tag representing all possible variants of boats
     */
    public final static @NonNull XTag<XMaterial> ITEMS_BOATS;
    /**
     * Tag representing all possible variants of coal
     */
    public final static @NonNull XTag<XMaterial> ITEMS_COALS;
    /**
     * Tag representing all possible music discs that can be dropped by creeper
     */
    public final static @NonNull XTag<XMaterial> ITEMS_CREEPER_DROP_MUSIC_DISCS;
    /**
     * Tag representing all possible types of fish
     */
    public final static @NonNull XTag<XMaterial> ITEMS_FISHES;
    /**
     * Tag representing all furnace materials {empty in spigot as of 1.18}
     */
    public final static @NonNull XTag<XMaterial> ITEMS_FURNACE_MATERIALS;
    /**
     * Tag representing all possible book types that can be placed on lecterns
     */
    public final static @NonNull XTag<XMaterial> ITEMS_LECTERN_BOOKS;
    /**
     * Tag representing all types of music discs
     */
    public final static @NonNull XTag<XMaterial> ITEMS_MUSIC_DISCS;
    /**
     * Tag representing all items loved by piglins
     */
    public final static @NonNull XTag<XMaterial> ITEMS_PIGLIN_LOVED;
    /**
     * Tag representing all stone tool materials
     */
    public final static @NonNull XTag<XMaterial> ITEMS_STONE_TOOL_MATERIALS;
    /**
     * Tag representing all possible types of wall banners
     */
    public static @NonNull XTag<XMaterial> WALL_BANNERS;
    /**
     * Tag representing all jungle log and bark variants
     */
    public static @NonNull XTag<XMaterial> JUNGLE_LOGS;
    /**
     * Tag representing all possible variants of lapis ores
     */
    public final static @NonNull XTag<XMaterial> LAPIS_ORES;
    /**
     * Tag representing all blocks that can't be replaced by lava pools
     */
    public final static @NonNull XTag<XMaterial> LAVA_POOL_STONE_CANNOT_REPLACE;
    /**
     * Tag representing all types of leaves
     */
    public final static @NonNull XTag<XMaterial> LEAVES;
    /**
     * Tag representing all wood and bark variants
     */
    public final static @NonNull XTag<XMaterial> LOGS;
    /**
     * Tag representing all wood and bark variants that can catch fire
     */
    public final static @NonNull XTag<XMaterial> LOGS_THAT_BURN;
    /**
     * Tag representing all possible blocks that can be replaced by lush ground
     */
    public final static @NonNull XTag<XMaterial> LUSH_GROUND_REPLACEABLE;
    /**
     * Tag representing all block types mineable with axe
     */
    public final static @NonNull XTag<XMaterial> MINEABLE_AXE;
    /**
     * Tag representing all block types mineable with hoe
     */
    public final static @NonNull XTag<XMaterial> MINEABLE_HOE;
    /**
     * Tag representing all block types mineable with pickaxe
     */
    public final static @NonNull XTag<XMaterial> MINEABLE_PICKAXE;
    /**
     * Tag representing all block types mineable with shovel
     */
    public final static @NonNull XTag<XMaterial> MINEABLE_SHOVEL;
    /**
     * Tag representing all possible block types mooshrooms can spawn on
     */
    public final static @NonNull XTag<XMaterial> MOOSHROOMS_SPAWNABLE_ON;
    /**
     * Tag representing all block types that can be replaced by moss
     */
    public final static @NonNull XTag<XMaterial> MOSS_REPLACEABLE;
    public final static @NonNull XTag<XMaterial> MUSHROOM_GROW_BLOCK;
    /**
     * Tag representing all block types that need minimum of diamond tool to drop items
     */
    public final static @NonNull XTag<XMaterial> NEEDS_DIAMOND_TOOL;
    /**
     * Tag representing all block types that need minimum of iron tool to drop items
     */
    public final static @NonNull XTag<XMaterial> NEEDS_IRON_TOOL;
    /**
     * Tag representing all block types that need minimum of stone tool to drop items
     */
    public final static @NonNull XTag<XMaterial> NEEDS_STONE_TOOL;
    /**
     * Tag representing all non-flammable wood and bark variants
     */
    public final static @NonNull XTag<XMaterial> NON_FLAMMABLE_WOOD;
    /**
     * Tag representing all nylium blocks
     */
    public final static @NonNull XTag<XMaterial> NYLIUM;
    /**
     * Tag representing all oak wood and bark variants
     */
    public static @NonNull XTag<XMaterial> OAK_LOGS;
    /**
     * Tag representing all possible blocks that can block vibration signals
     */
    public final static @NonNull XTag<XMaterial> OCCLUDES_VIBRATION_SIGNALS;
    /**
     * Tag representing all possible block types parrots may spawn on
     */
    public final static @NonNull XTag<XMaterial> PARROTS_SPAWNABLE_ON;
    /**
     * Tag representing all items that can be used as piglin food
     */
    public final static @NonNull XTag<XMaterial> PIGLIN_FOOD;
    /**
     * Tag representing all block types that repel piglins
     */
    public final static @NonNull XTag<XMaterial> PIGLIN_REPELLENTS;
    /**
     * Tag representing all types of planks
     */
    public static @NonNull XTag<XMaterial> PLANKS;
    /**
     * Tag representing all possible blocks polar bears may spawn on
     */
    public final static @NonNull XTag<XMaterial> POLAR_BEARS_SPAWNABLE_ON_IN_FROZEN_OCEAN;
    /**
     * Tag representing all possible block types that be used as portals
     */
    public final static @NonNull XTag<XMaterial> PORTALS;
    /**
     * Tag representing all possible variants of pressure plates
     */
    public final static @NonNull XTag<XMaterial> PRESSURE_PLATES;
    /**
     * Tag representing all block types that prevent inside mob spawning
     */
    public final static @NonNull XTag<XMaterial> PREVENT_MOB_SPAWNING_INSIDE;
    /**
     * Tag representing all possible block types that rabbits may spawn on
     */
    public final static @NonNull XTag<XMaterial> RABBITS_SPAWNABLE_ON;
    /**
     * Tag representing all possible types of rails
     */
    public final static @NonNull XTag<XMaterial> RAILS;
    /**
     * Tag representing all possible variants of redstone ores
     */
    public final static @NonNull XTag<XMaterial> REDSTONE_ORES;
    /**
     * Tag representing all plant blocks that may be replaced
     */
    public final static @NonNull XTag<XMaterial> REPLACEABLE_PLANTS;
    /**
     * Tag representing all possible types of sand
     */
    public final static @NonNull XTag<XMaterial> SAND;
    /**
     * Tag representing all possible types of saplings
     */
    public final static @NonNull XTag<XMaterial> SAPLINGS;
    /**
     * Tag representing all possible variants of shulker boxes
     */
    public static @NonNull XTag<XMaterial> SHULKER_BOXES;
    /**
     * Tag representing all possible variants of signs
     */
    public final static @NonNull XTag<XMaterial> SIGNS;
    /**
     * Tag representing all possible block types small dripleaf may be placed upon
     */
    public final static @NonNull XTag<XMaterial> SMALL_DRIPLEAF_PLACEABLE;
    /**
     * Tag representing all flowers small in size {1 block tall}
     */
    public final static @NonNull XTag<XMaterial> SMALL_FLOWERS;
    /**
     * Tag representing all possible variants of snow
     */
    public final static @NonNull XTag<XMaterial> SNOW;
    /**
     * Tag representing all possible blocks that can be lit up with sould fire
     */
    public final static @NonNull XTag<XMaterial> SOUL_FIRE_BASE_BLOCKS;
    /**
     * Tag representing all possible blocks that activate soul speed enchantment
     */
    public final static @NonNull XTag<XMaterial> SOUL_SPEED_BLOCKS;
    /**
     * Tag representing all spruce wood and log variants
     */
    public static @NonNull XTag<XMaterial> SPRUCE_LOGS;
    /**
     * Tag representing all possible types of stairs
     */
    public final static @NonNull XTag<XMaterial> STAIRS;
    /**
     * Tag representing all possible types of standing signs
     */
    public static @NonNull XTag<XMaterial> STANDING_SIGNS;
    /**
     * Tag representing all possible variants of stone bricks
     */
    public final static @NonNull XTag<XMaterial> STONE_BRICKS;
    /**
     * Tag representing all possible blocks that can be replaced by regular stone ores
     */
    public final static @NonNull XTag<XMaterial> STONE_ORE_REPLACEABLES;
    /**
     * Tag representing all pressure plates made of some type of stone
     */
    public final static @NonNull XTag<XMaterial> STONE_PRESSURE_PLATES;
    /**
     * Tag representing all block types that make strider warm
     */
    public final static @NonNull XTag<XMaterial> STRIDER_WARM_BLOCKS;
    /**
     * Tag representing all flowers that are tall {2 blocks}
     */
    public final static @NonNull XTag<XMaterial> TALL_FLOWERS;
    /**
     * Tag representing all possible variants of non-glazed terracotta
     */
    public static @NonNull XTag<XMaterial> TERRACOTTA;
    /**
     * Tag representing all possible types of trapdoors
     */
    public final static @NonNull XTag<XMaterial> TRAPDOORS;
    /**
     * Tag representing all block types that can be bonemealed underwater
     */
    public final static @NonNull XTag<XMaterial> UNDERWATER_BONEMEALS;
    /**
     * Tag representing all blocks that have unstable bottom when placed in centre of 2 blocks
     */
    public final static @NonNull XTag<XMaterial> UNSTABLE_BOTTOM_CENTER;
    /**
     * Tag representing all valid mob spawn positions
     */
    public final static @NonNull XTag<XMaterial> VALID_SPAWN;
    /**
     * Tag representing all wall corals
     */
    public final static @NonNull XTag<XMaterial> WALL_CORALS;
    /**
     * Tag representing all possible block types that can override a wall post creation
     */
    public final static @NonNull XTag<XMaterial> WALL_POST_OVERRIDE;
    /**
     * Tag representing all wall signs
     */
    public static @NonNull XTag<XMaterial> WALL_SIGNS;
    /**
     * Tag representing all different types of walls
     */
    public final static @NonNull XTag<XMaterial> WALLS;
    /**
     * Tag representing all warped stems
     */
    public final static @NonNull XTag<XMaterial> WARPED_STEMS;
    /**
     * Tag representing all block types that can't be destroyed by withers
     */
    public final static @NonNull XTag<XMaterial> WITHER_IMMUNE;
    /**
     * Tag representing all possible block types that may be used as wither summon base
     */
    public final static @NonNull XTag<XMaterial> WITHER_SUMMON_BASE_BLOCKS;
    /**
     * Tag representing all possible block types that wolves may spawn on
     */
    public final static @NonNull XTag<XMaterial> WOLVES_SPAWNABLE_ON;
    /**
     * Tag representing all possible types of wooden buttons
     */
    public static @NonNull XTag<XMaterial> WOODEN_BUTTONS;
    /**
     * Tag representing all possible types of wooden doors
     */
    public static @NonNull XTag<XMaterial> WOODEN_DOORS;
    /**
     * Tag representing all possible types of wooden fence gates
     */
    public static @NonNull XTag<XMaterial> WOODEN_FENCE_GATES;
    /**
     * Tag representing all possible types of wooden fences
     */
    public static @NonNull XTag<XMaterial> WOODEN_FENCES;
    /**
     * Tag representing all possible types of wooden pressure plates
     */
    public static @NonNull XTag<XMaterial> WOODEN_PRESSURE_PLATES;
    /**
     * Tag representing all possible types of wooden slabs
     */
    public static @NonNull XTag<XMaterial> WOODEN_SLABS;
    /**
     * Tag representing all possible types of wooden stairs
     */
    public static @NonNull XTag<XMaterial> WOODEN_STAIRS;
    /**
     * Tag representing all possible types of wooden trapdoors
     */
    public static @NonNull XTag<XMaterial> WOODEN_TRAPDOORS;
    /**
     * Tag representing all possible types of wool
     */
    public static @NonNull XTag<XMaterial> WOOL;




    /**
     * Tag representing all armor pieces made of leather
     */
    public final static @NonNull XTag<XMaterial> LEATHER_ARMOR_PIECES;
    /**
     * Tag representing all armor pieces made of iron
     */
    public final static @NonNull XTag<XMaterial> IRON_ARMOR_PIECES;
    /**
     * Tag representing all armor pieces made of chains
     */
    public final static @NonNull XTag<XMaterial> CHAINMAIL_ARMOR_PIECES;
    /**
     * Tag representing all armor pieces made of gold
     */
    public final static @NonNull XTag<XMaterial> GOLDEN_ARMOR_PIECES;
    /**
     * Tag representing all armor pieces made of diamond
     */
    public final static @NonNull XTag<XMaterial> DIAMOND_ARMOR_PIECES;
    /**
     * Tag representing all armor pieces made of netherite
     */
    public final static @NonNull XTag<XMaterial> NETHERITE_ARMOR_PIECES;
    /**
     * Tag representing all armor pieces that add armor bars upon wearing
     */
    public final static @NonNull XTag<XMaterial> ARMOR_PIECES;
    /**
     * Tag representing all wooden tools and swords
     */
    public final static @NonNull XTag<XMaterial> WOODEN_TOOLS;
    /**
     * Tag representing all stone tools and swords
     */
    public final static @NonNull XTag<XMaterial> STONE_TOOLS;
    /**
     * Tag representing all iron tools and swords
     */
    public final static @NonNull XTag<XMaterial> IRON_TOOLS;
    /**
     * Tag representing all diamond tools and swords
     */
    public final static @NonNull XTag<XMaterial> DIAMOND_TOOLS;
    /**
     * Tag representing all netherite tools and swords
     */
    public final static @NonNull XTag<XMaterial> NETHERITE_TOOLS;

    /**
     * Tag representing all possible enchants that can be applied to all armor pieces (excluding elytras)
     */
    public final static @NonNull XTag<XEnchantment> ARMOR_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to helmets/turtle shells
     */
    public final static @NonNull XTag<XEnchantment> HELEMT_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to chestplates
     */
    public final static @NonNull XTag<XEnchantment> CHESTPLATE_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to leggings
     */
    public final static @NonNull XTag<XEnchantment> LEGGINGS_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to boots
     */
    public final static @NonNull XTag<XEnchantment> BOOTS_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to elytras
     */
    public final static @NonNull XTag<XEnchantment> ELYTRA_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to swords
     */
    public final static @NonNull XTag<XEnchantment> SWORD_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to axes
     */
    public final static @NonNull XTag<XEnchantment> AXE_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to hoes
     */
    public final static @NonNull XTag<XEnchantment> HOE_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to pickaxes
     */
    public final static @NonNull XTag<XEnchantment> PICKAXE_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to shovels
     */
    public final static @NonNull XTag<XEnchantment> SHOVEL_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to shears
     */
    public final static @NonNull XTag<XEnchantment> SHEARS_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to bows
     */
    public final static @NonNull XTag<XEnchantment> BOW_ENCHANTS;
    /**
     * Tag representing all possible enchants that can be applied to crossbows
     */
    public final static @NonNull XTag<XEnchantment> CROSSBOW_ENCHANTS;


    static {
        initLogs();
        initColorables();
        PORTALS = new XTag<>(XMaterial.END_GATEWAY,
                XMaterial.END_PORTAL,
                XMaterial.NETHER_PORTAL);
        WALLS = new XTag<>(XMaterial.POLISHED_DEEPSLATE_WALL,
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
        STONE_PRESSURE_PLATES = new XTag<>(XMaterial.STONE_PRESSURE_PLATE,
                XMaterial.POLISHED_BLACKSTONE_PRESSURE_PLATE);
        RAILS = new XTag<>(XMaterial.RAIL,
                XMaterial.ACTIVATOR_RAIL,
                XMaterial.DETECTOR_RAIL,
                XMaterial.POWERED_RAIL);
        REDSTONE_ORES = new XTag<>(XMaterial.REDSTONE_ORE,
                XMaterial.DEEPSLATE_REDSTONE_ORE);
        ANIMALS_SPAWNABLE_ON = new XTag<>(XMaterial.GRASS_BLOCK);
        ANVIL = new XTag<>(XMaterial.ANVIL,
                XMaterial.CHIPPED_ANVIL,
                XMaterial.DAMAGED_ANVIL);
        AXOLOTL_TEMPT_ITEMS = new XTag<>(XMaterial.TROPICAL_FISH_BUCKET);
        AXOLOTLS_SPAWNABLE_ON = new XTag<>(XMaterial.CLAY);

        SNOW = new XTag<>(XMaterial.SNOW_BLOCK,
                XMaterial.SNOW,
                XMaterial.POWDER_SNOW);
        SAND = new XTag<>(XMaterial.SAND,
                XMaterial.RED_SAND);
        DIRT = new XTag<>(XMaterial.MOSS_BLOCK,
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
        BEACON_BASE_BLOCKS = new XTag<>(XMaterial.NETHERITE_BLOCK,
                XMaterial.GOLD_BLOCK,
                XMaterial.IRON_BLOCK,
                XMaterial.EMERALD_BLOCK,
                XMaterial.DIAMOND_BLOCK);
        CROPS = new XTag<>(XMaterial.CARROTS,
                XMaterial.POTATOES,
                XMaterial.WHEAT,
                XMaterial.MELON_STEM,
                XMaterial.BEETROOTS,
                XMaterial.PUMPKIN_STEM);
        CAMPFIRES = new XTag<>(XMaterial.CAMPFIRE,
                XMaterial.SOUL_CAMPFIRE);
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
        WALL_CORALS = new XTag<>(XMaterial.FIRE_CORAL_WALL_FAN,
                XMaterial.TUBE_CORAL_WALL_FAN,
                XMaterial.BRAIN_CORAL_WALL_FAN,
                XMaterial.HORN_CORAL_WALL_FAN,
                XMaterial.BUBBLE_CORAL_WALL_FAN);
        CRIMSON_STEMS = new XTag<>(XMaterial.CRIMSON_HYPHAE,
                XMaterial.STRIPPED_CRIMSON_STEM,
                XMaterial.CRIMSON_STEM,
                XMaterial.STRIPPED_CRIMSON_HYPHAE);
        WARPED_STEMS = new XTag<>(XMaterial.WARPED_HYPHAE,
                XMaterial.STRIPPED_WARPED_STEM,
                XMaterial.WARPED_STEM,
                XMaterial.STRIPPED_WARPED_HYPHAE);
        CRYSTAL_SOUND_BLOCKS = new XTag<>(XMaterial.AMETHYST_BLOCK,
                XMaterial.BUDDING_AMETHYST);
        DEEPSLATE_ORE_REPLACEABLES = new XTag<>(XMaterial.TUFF,
                XMaterial.DEEPSLATE);
        DIAMOND_ORES = new XTag<>(XMaterial.DIAMOND_ORE,
                XMaterial.DEEPSLATE_DIAMOND_ORE);
        DOORS = new XTag<>(XMaterial.IRON_DOOR);
        DOORS.inheritFrom(WOODEN_DOORS);
        WITHER_IMMUNE = new XTag<>(XMaterial.STRUCTURE_BLOCK,
                XMaterial.END_GATEWAY,
                XMaterial.BEDROCK,
                XMaterial.END_PORTAL,
                XMaterial.COMMAND_BLOCK,
                XMaterial.REPEATING_COMMAND_BLOCK,
                XMaterial.MOVING_PISTON,
                XMaterial.CHAIN_COMMAND_BLOCK,
                XMaterial.BARRIER,
                XMaterial.END_PORTAL_FRAME,
                XMaterial.JIGSAW);
        WITHER_SUMMON_BASE_BLOCKS = new XTag<>(XMaterial.SOUL_SOIL,
                XMaterial.SOUL_SAND);
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
        FENCE_GATES = new XTag<>(XMaterial.class, WOODEN_FENCE_GATES);
        FENCES = new XTag<>(XMaterial.NETHER_BRICK_FENCE);
        FENCES.inheritFrom(WOODEN_FENCES);
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
        HOGLIN_REPELLENTS = new XTag<>(XMaterial.WARPED_FUNGUS,
                XMaterial.NETHER_PORTAL,
                XMaterial.POTTED_WARPED_FUNGUS,
                XMaterial.RESPAWN_ANCHOR);
        IGNORED_BY_PIGLIN_BABIES = new XTag<>(XMaterial.LEATHER);
        IMPERMEABLE = new XTag<>(XMaterial.class, GLASS);
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
        ITEMS_FURNACE_MATERIALS = new XTag<>(XMaterial.class);
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
        STAIRS = new XTag<>(XMaterial.STONE_BRICK_STAIRS,
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
                XMaterial.JUNGLE_STAIRS,
                XMaterial.ACACIA_STAIRS,
                XMaterial.WAXED_CUT_COPPER_STAIRS,
                XMaterial.DEEPSLATE_TILE_STAIRS,
                XMaterial.CRIMSON_STAIRS,
                XMaterial.POLISHED_ANDESITE_STAIRS,
                XMaterial.SMOOTH_RED_SANDSTONE_STAIRS,
                XMaterial.PURPUR_STAIRS,
                XMaterial.POLISHED_DEEPSLATE_STAIRS,
                XMaterial.SPRUCE_STAIRS,
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
                XMaterial.OAK_STAIRS,
                XMaterial.EXPOSED_CUT_COPPER_STAIRS,
                XMaterial.WARPED_STAIRS,
                XMaterial.DARK_PRISMARINE_STAIRS,
                XMaterial.OXIDIZED_CUT_COPPER_STAIRS,
                XMaterial.POLISHED_BLACKSTONE_STAIRS,
                XMaterial.BIRCH_STAIRS,
                XMaterial.POLISHED_GRANITE_STAIRS,
                XMaterial.MOSSY_STONE_BRICK_STAIRS,
                XMaterial.DARK_OAK_STAIRS,
                XMaterial.END_STONE_BRICK_STAIRS,
                XMaterial.WAXED_OXIDIZED_CUT_COPPER_STAIRS,
                XMaterial.SMOOTH_SANDSTONE_STAIRS,
                XMaterial.BLACKSTONE_STAIRS);
        SOUL_FIRE_BASE_BLOCKS = new XTag<>(XMaterial.SOUL_SOIL,
                XMaterial.SOUL_SAND);
        SOUL_SPEED_BLOCKS = new XTag<>(XMaterial.SOUL_SOIL,
                XMaterial.SOUL_SAND);
        STONE_ORE_REPLACEABLES = new XTag<>(XMaterial.STONE,
                XMaterial.DIORITE,
                XMaterial.ANDESITE,
                XMaterial.GRANITE);
        STRIDER_WARM_BLOCKS = new XTag<>(XMaterial.LAVA);
        VALID_SPAWN = new XTag<>(XMaterial.PODZOL,
                XMaterial.GRASS_BLOCK);
        STONE_BRICKS = new XTag<>(XMaterial.CHISELED_STONE_BRICKS,
                XMaterial.CRACKED_STONE_BRICKS,
                XMaterial.MOSSY_STONE_BRICKS,
                XMaterial.STONE_BRICKS);
        SAPLINGS = new XTag<>(XMaterial.ACACIA_SAPLING,
                XMaterial.JUNGLE_SAPLING,
                XMaterial.SPRUCE_SAPLING,
                XMaterial.DARK_OAK_SAPLING,
                XMaterial.AZALEA,
                XMaterial.OAK_SAPLING,
                XMaterial.FLOWERING_AZALEA,
                XMaterial.BIRCH_SAPLING);
        WOLVES_SPAWNABLE_ON = new XTag<>(XMaterial.GRASS_BLOCK,
                XMaterial.SNOW,
                XMaterial.SNOW_BLOCK);
        POLAR_BEARS_SPAWNABLE_ON_IN_FROZEN_OCEAN = new XTag<>(XMaterial.ICE);
        RABBITS_SPAWNABLE_ON = new XTag<>(XMaterial.GRASS_BLOCK,
                XMaterial.SNOW,
                XMaterial.SNOW_BLOCK,
                XMaterial.SAND);
        PIGLIN_FOOD = new XTag<>(XMaterial.COOKED_PORKCHOP,
                XMaterial.PORKCHOP);
        PIGLIN_REPELLENTS = new XTag<>(XMaterial.SOUL_WALL_TORCH,
                XMaterial.SOUL_TORCH,
                XMaterial.SOUL_CAMPFIRE,
                XMaterial.SOUL_LANTERN,
                XMaterial.SOUL_FIRE);
        REPLACEABLE_PLANTS = new XTag<>(XMaterial.FERN,
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
                XMaterial.GRASS);
        SMALL_DRIPLEAF_PLACEABLE = new XTag<>(XMaterial.CLAY,
                XMaterial.MOSS_BLOCK);
        NON_FLAMMABLE_WOOD = new XTag<>(XMaterial.CRIMSON_PLANKS,
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
        MOOSHROOMS_SPAWNABLE_ON = new XTag<>(XMaterial.MYCELIUM);

        NEEDS_STONE_TOOL = new XTag<>(XMaterial.OXIDIZED_CUT_COPPER,
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
        NEEDS_IRON_TOOL = new XTag<>(XMaterial.GOLD_ORE,
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
        NEEDS_DIAMOND_TOOL = new XTag<>(XMaterial.OBSIDIAN,
                XMaterial.NETHERITE_BLOCK,
                XMaterial.ANCIENT_DEBRIS,
                XMaterial.RESPAWN_ANCHOR,
                XMaterial.CRYING_OBSIDIAN);

        MINEABLE_PICKAXE = new XTag<>(XMaterial.ORANGE_TERRACOTTA,
                XMaterial.OXIDIZED_CUT_COPPER,
                XMaterial.GOLD_BLOCK,
                XMaterial.SMOOTH_SANDSTONE,
                XMaterial.MOSSY_COBBLESTONE_SLAB,
                XMaterial.BROWN_SHULKER_BOX,
                XMaterial.EXPOSED_CUT_COPPER_SLAB,
                XMaterial.DEAD_HORN_CORAL,
                XMaterial.ANDESITE_WALL,
                XMaterial.IRON_DOOR,
                XMaterial.WHITE_CONCRETE,
                XMaterial.POWERED_RAIL,
                XMaterial.COBBLESTONE,
                XMaterial.DEAD_FIRE_CORAL_FAN,
                XMaterial.SMOOTH_QUARTZ_SLAB,
                XMaterial.DRIPSTONE_BLOCK,
                XMaterial.DEAD_FIRE_CORAL_WALL_FAN,
                XMaterial.RED_TERRACOTTA,
                XMaterial.CHISELED_SANDSTONE,
                XMaterial.INFESTED_STONE_BRICKS,
                XMaterial.NETHER_BRICK_WALL,
                XMaterial.MOSSY_COBBLESTONE_STAIRS,
                XMaterial.BLACK_SHULKER_BOX,
                XMaterial.QUARTZ_BLOCK,
                XMaterial.BLUE_TERRACOTTA,
                XMaterial.DEAD_BUBBLE_CORAL_WALL_FAN,
                XMaterial.COBBLESTONE_SLAB,
                XMaterial.GRAY_GLAZED_TERRACOTTA,
                XMaterial.CUT_COPPER_STAIRS,
                XMaterial.SANDSTONE_WALL,
                XMaterial.DEEPSLATE_BRICK_WALL,
                XMaterial.COPPER_BLOCK,
                XMaterial.LIME_TERRACOTTA,
                XMaterial.ORANGE_GLAZED_TERRACOTTA,
                XMaterial.STONE_BRICKS,
                XMaterial.DETECTOR_RAIL,
                XMaterial.POLISHED_BLACKSTONE_SLAB,
                XMaterial.BLACKSTONE_WALL,
                XMaterial.TERRACOTTA,
                XMaterial.DIAMOND_ORE,
                XMaterial.DEAD_FIRE_CORAL,
                XMaterial.CHISELED_POLISHED_BLACKSTONE,
                XMaterial.YELLOW_GLAZED_TERRACOTTA,
                XMaterial.DISPENSER,
                XMaterial.MOSSY_STONE_BRICK_STAIRS,
                XMaterial.EMERALD_ORE,
                XMaterial.MAGENTA_CONCRETE,
                XMaterial.DEEPSLATE_BRICKS,
                XMaterial.ANCIENT_DEBRIS,
                XMaterial.CAULDRON,
                XMaterial.DEEPSLATE_EMERALD_ORE,
                XMaterial.HEAVY_WEIGHTED_PRESSURE_PLATE,
                XMaterial.ANVIL,
                XMaterial.OXIDIZED_CUT_COPPER_SLAB,
                XMaterial.OBSIDIAN,
                XMaterial.DEAD_BUBBLE_CORAL_FAN,
                XMaterial.RAIL,
                XMaterial.DEEPSLATE_DIAMOND_ORE,
                XMaterial.POLISHED_ANDESITE_SLAB,
                XMaterial.NETHER_GOLD_ORE,
                XMaterial.DEAD_FIRE_CORAL_BLOCK,
                XMaterial.EXPOSED_CUT_COPPER,
                XMaterial.TUBE_CORAL_BLOCK,
                XMaterial.RED_SANDSTONE_SLAB,
                XMaterial.DEEPSLATE_TILE_WALL,
                XMaterial.RED_GLAZED_TERRACOTTA,
                XMaterial.SMOOTH_QUARTZ,
                XMaterial.RED_SANDSTONE_WALL,
                XMaterial.SMOOTH_RED_SANDSTONE,
                XMaterial.WHITE_GLAZED_TERRACOTTA,
                XMaterial.STONE_BRICK_WALL,
                XMaterial.GRAY_CONCRETE,
                XMaterial.LAVA_CAULDRON,
                XMaterial.PINK_GLAZED_TERRACOTTA,
                XMaterial.CYAN_TERRACOTTA,
                XMaterial.COBBLED_DEEPSLATE_STAIRS,
                XMaterial.LAPIS_ORE,
                XMaterial.STONE,
                XMaterial.DEEPSLATE_COAL_ORE,
                XMaterial.INFESTED_COBBLESTONE,
                XMaterial.DIORITE_WALL,
                XMaterial.ACTIVATOR_RAIL,
                XMaterial.DARK_PRISMARINE_STAIRS,
                XMaterial.WAXED_CUT_COPPER,
                XMaterial.PRISMARINE,
                XMaterial.PISTON,
                XMaterial.LIME_CONCRETE,
                XMaterial.BLACKSTONE_SLAB,
                XMaterial.STONE_SLAB,
                XMaterial.SMOOTH_SANDSTONE_SLAB,
                XMaterial.CUT_COPPER,
                XMaterial.COBBLED_DEEPSLATE_SLAB,
                XMaterial.CHISELED_QUARTZ_BLOCK,
                XMaterial.REDSTONE_ORE,
                XMaterial.MOSSY_STONE_BRICKS,
                XMaterial.GREEN_CONCRETE,
                XMaterial.POLISHED_BLACKSTONE_BRICK_STAIRS,
                XMaterial.EMERALD_BLOCK,
                XMaterial.FIRE_CORAL_BLOCK,
                XMaterial.SMOOTH_RED_SANDSTONE_SLAB,
                XMaterial.BELL,
                XMaterial.NETHER_BRICK_STAIRS,
                XMaterial.AMETHYST_BLOCK,
                XMaterial.POLISHED_DIORITE_SLAB,
                XMaterial.GILDED_BLACKSTONE,
                XMaterial.PRISMARINE_BRICK_SLAB,
                XMaterial.WAXED_CUT_COPPER_STAIRS,
                XMaterial.CHISELED_NETHER_BRICKS,
                XMaterial.WAXED_COPPER_BLOCK,
                XMaterial.DEEPSLATE_IRON_ORE,
                XMaterial.PINK_SHULKER_BOX,
                XMaterial.IRON_BLOCK,
                XMaterial.BUDDING_AMETHYST,
                XMaterial.POLISHED_DEEPSLATE,
                XMaterial.HOPPER,
                XMaterial.CUT_RED_SANDSTONE,
                XMaterial.DEAD_HORN_CORAL_FAN,
                XMaterial.QUARTZ_BRICKS,
                XMaterial.IRON_ORE,
                XMaterial.CHISELED_STONE_BRICKS,
                XMaterial.GREEN_TERRACOTTA,
                XMaterial.LIGHT_BLUE_CONCRETE,
                XMaterial.BLACKSTONE_STAIRS,
                XMaterial.ENDER_CHEST,
                XMaterial.END_STONE_BRICKS,
                XMaterial.NETHERRACK,
                XMaterial.REDSTONE_BLOCK,
                XMaterial.WAXED_OXIDIZED_CUT_COPPER,
                XMaterial.LIGHT_WEIGHTED_PRESSURE_PLATE,
                XMaterial.GRAY_SHULKER_BOX,
                XMaterial.ORANGE_SHULKER_BOX,
                XMaterial.WAXED_WEATHERED_CUT_COPPER,
                XMaterial.YELLOW_SHULKER_BOX,
                XMaterial.CHAIN,
                XMaterial.MAGMA_BLOCK,
                XMaterial.QUARTZ_SLAB,
                XMaterial.RED_NETHER_BRICK_STAIRS,
                XMaterial.BLUE_CONCRETE,
                XMaterial.GRAY_TERRACOTTA,
                XMaterial.STONE_PRESSURE_PLATE,
                XMaterial.DARK_PRISMARINE,
                XMaterial.LIME_GLAZED_TERRACOTTA,
                XMaterial.DEAD_BUBBLE_CORAL_BLOCK,
                XMaterial.POLISHED_DEEPSLATE_STAIRS,
                XMaterial.DIORITE_SLAB,
                XMaterial.MEDIUM_AMETHYST_BUD,
                XMaterial.LIGHT_GRAY_TERRACOTTA,
                XMaterial.PURPLE_GLAZED_TERRACOTTA,
                XMaterial.SANDSTONE_STAIRS,
                XMaterial.ORANGE_CONCRETE,
                XMaterial.LIGHT_BLUE_GLAZED_TERRACOTTA,
                XMaterial.BROWN_GLAZED_TERRACOTTA,
                XMaterial.LANTERN,
                XMaterial.NETHER_BRICK_SLAB,
                XMaterial.WAXED_EXPOSED_CUT_COPPER_STAIRS,
                XMaterial.PRISMARINE_SLAB,
                XMaterial.BRICK_WALL,
                XMaterial.DEAD_BRAIN_CORAL_FAN,
                XMaterial.ICE,
                XMaterial.DIORITE,
                XMaterial.RED_CONCRETE,
                XMaterial.EXPOSED_CUT_COPPER_STAIRS,
                XMaterial.DROPPER,
                XMaterial.CRACKED_NETHER_BRICKS,
                XMaterial.WAXED_EXPOSED_CUT_COPPER_SLAB,
                XMaterial.BREWING_STAND,
                XMaterial.CHISELED_RED_SANDSTONE,
                XMaterial.RED_NETHER_BRICK_SLAB,
                XMaterial.CALCITE,
                XMaterial.CUT_SANDSTONE,
                XMaterial.POLISHED_BASALT,
                XMaterial.DEAD_BRAIN_CORAL_WALL_FAN,
                XMaterial.LIME_SHULKER_BOX,
                XMaterial.POLISHED_BLACKSTONE_BRICK_SLAB,
                XMaterial.DEEPSLATE_TILES,
                XMaterial.BUBBLE_CORAL_BLOCK,
                XMaterial.MOSSY_STONE_BRICK_SLAB,
                XMaterial.SHULKER_BOX,
                XMaterial.QUARTZ_PILLAR,
                XMaterial.LODESTONE,
                XMaterial.GRANITE_STAIRS,
                XMaterial.COBBLESTONE_WALL,
                XMaterial.POLISHED_GRANITE,
                XMaterial.POLISHED_ANDESITE,
                XMaterial.OBSERVER,
                XMaterial.CHISELED_DEEPSLATE,
                XMaterial.HORN_CORAL_BLOCK,
                XMaterial.COPPER_ORE,
                XMaterial.RAW_GOLD_BLOCK,
                XMaterial.SMOOTH_STONE_SLAB,
                XMaterial.PINK_CONCRETE,
                XMaterial.CRACKED_POLISHED_BLACKSTONE_BRICKS,
                XMaterial.DEEPSLATE_TILE_STAIRS,
                XMaterial.WAXED_EXPOSED_CUT_COPPER,
                XMaterial.SMALL_AMETHYST_BUD,
                XMaterial.OXIDIZED_COPPER,
                XMaterial.YELLOW_CONCRETE,
                XMaterial.POLISHED_BLACKSTONE,
                XMaterial.QUARTZ_STAIRS,
                XMaterial.RAW_IRON_BLOCK,
                XMaterial.POLISHED_BLACKSTONE_BRICKS,
                XMaterial.WATER_CAULDRON,
                XMaterial.BROWN_CONCRETE,
                XMaterial.DEAD_HORN_CORAL_WALL_FAN,
                XMaterial.POLISHED_BLACKSTONE_BRICK_WALL,
                XMaterial.BLUE_SHULKER_BOX,
                XMaterial.POWDER_SNOW_CAULDRON,
                XMaterial.INFESTED_DEEPSLATE,
                XMaterial.SANDSTONE_SLAB,
                XMaterial.LIGHT_GRAY_GLAZED_TERRACOTTA,
                XMaterial.RAW_COPPER_BLOCK,
                XMaterial.CYAN_SHULKER_BOX,
                XMaterial.BLACKSTONE,
                XMaterial.WEATHERED_CUT_COPPER_SLAB,
                XMaterial.DEEPSLATE_BRICK_STAIRS,
                XMaterial.AMETHYST_CLUSTER,
                XMaterial.CHIPPED_ANVIL,
                XMaterial.CYAN_GLAZED_TERRACOTTA,
                XMaterial.PURPLE_TERRACOTTA,
                XMaterial.GRINDSTONE,
                XMaterial.DEAD_BUBBLE_CORAL,
                XMaterial.WAXED_EXPOSED_COPPER,
                XMaterial.PINK_TERRACOTTA,
                XMaterial.BROWN_TERRACOTTA,
                XMaterial.DEEPSLATE_BRICK_SLAB,
                XMaterial.OXIDIZED_CUT_COPPER_STAIRS,
                XMaterial.POLISHED_BLACKSTONE_STAIRS,
                XMaterial.RED_SANDSTONE,
                XMaterial.DEAD_TUBE_CORAL,
                XMaterial.LIGHTNING_ROD,
                XMaterial.PURPLE_CONCRETE,
                XMaterial.SOUL_LANTERN,
                XMaterial.POLISHED_BLACKSTONE_PRESSURE_PLATE,
                XMaterial.SMOOTH_SANDSTONE_STAIRS,
                XMaterial.GREEN_GLAZED_TERRACOTTA,
                XMaterial.IRON_BARS,
                XMaterial.PURPUR_BLOCK,
                XMaterial.LIGHT_GRAY_CONCRETE,
                XMaterial.FURNACE,
                XMaterial.POLISHED_DEEPSLATE_SLAB,
                XMaterial.STONE_STAIRS,
                XMaterial.DEAD_BRAIN_CORAL,
                XMaterial.CONDUIT,
                XMaterial.BLACK_CONCRETE,
                XMaterial.SPAWNER,
                XMaterial.COAL_BLOCK,
                XMaterial.BONE_BLOCK,
                XMaterial.WARPED_NYLIUM,
                XMaterial.POLISHED_DIORITE_STAIRS,
                XMaterial.WEATHERED_COPPER,
                XMaterial.WEATHERED_CUT_COPPER_STAIRS,
                XMaterial.WEATHERED_CUT_COPPER,
                XMaterial.MOSSY_COBBLESTONE_WALL,
                XMaterial.MOSSY_COBBLESTONE,
                XMaterial.DEAD_TUBE_CORAL_FAN,
                XMaterial.POLISHED_ANDESITE_STAIRS,
                XMaterial.GRANITE_SLAB,
                XMaterial.SMOKER,
                XMaterial.COBBLED_DEEPSLATE,
                XMaterial.SMOOTH_BASALT,
                XMaterial.STONE_BUTTON,
                XMaterial.MAGENTA_GLAZED_TERRACOTTA,
                XMaterial.SMOOTH_RED_SANDSTONE_STAIRS,
                XMaterial.PURPUR_STAIRS,
                XMaterial.MAGENTA_SHULKER_BOX,
                XMaterial.NETHER_BRICKS,
                XMaterial.BRICKS,
                XMaterial.CYAN_CONCRETE,
                XMaterial.END_STONE_BRICK_WALL,
                XMaterial.RED_NETHER_BRICKS,
                XMaterial.SMOOTH_STONE,
                XMaterial.BRAIN_CORAL_BLOCK,
                XMaterial.ANDESITE,
                XMaterial.BASALT,
                XMaterial.ANDESITE_SLAB,
                XMaterial.CUT_COPPER_SLAB,
                XMaterial.TUFF,
                XMaterial.DIORITE_STAIRS,
                XMaterial.CUT_SANDSTONE_SLAB,
                XMaterial.DEAD_HORN_CORAL_BLOCK,
                XMaterial.END_STONE,
                XMaterial.WAXED_OXIDIZED_COPPER,
                XMaterial.INFESTED_CHISELED_STONE_BRICKS,
                XMaterial.END_STONE_BRICK_SLAB,
                XMaterial.NETHER_QUARTZ_ORE,
                XMaterial.LIGHT_GRAY_SHULKER_BOX,
                XMaterial.PRISMARINE_BRICKS,
                XMaterial.CRYING_OBSIDIAN,
                XMaterial.CRACKED_DEEPSLATE_TILES,
                XMaterial.WHITE_SHULKER_BOX,
                XMaterial.INFESTED_STONE,
                XMaterial.DEEPSLATE_COPPER_ORE,
                XMaterial.IRON_TRAPDOOR,
                XMaterial.STONE_BRICK_STAIRS,
                XMaterial.WAXED_OXIDIZED_CUT_COPPER_SLAB,
                XMaterial.GRANITE_WALL,
                XMaterial.INFESTED_MOSSY_STONE_BRICKS,
                XMaterial.RED_SHULKER_BOX,
                XMaterial.YELLOW_TERRACOTTA,
                XMaterial.CUT_RED_SANDSTONE_SLAB,
                XMaterial.RESPAWN_ANCHOR,
                XMaterial.WAXED_WEATHERED_CUT_COPPER_STAIRS,
                XMaterial.POLISHED_DEEPSLATE_WALL,
                XMaterial.BLUE_ICE,
                XMaterial.COBBLED_DEEPSLATE_WALL,
                XMaterial.POLISHED_DIORITE,
                XMaterial.NETHER_BRICK_FENCE,
                XMaterial.PURPUR_SLAB,
                XMaterial.INFESTED_CRACKED_STONE_BRICKS,
                XMaterial.LIGHT_BLUE_SHULKER_BOX,
                XMaterial.SANDSTONE,
                XMaterial.RED_NETHER_BRICK_WALL,
                XMaterial.GREEN_SHULKER_BOX,
                XMaterial.STONE_BRICK_SLAB,
                XMaterial.DEAD_BRAIN_CORAL_BLOCK,
                XMaterial.PURPLE_SHULKER_BOX,
                XMaterial.EXPOSED_COPPER,
                XMaterial.WAXED_CUT_COPPER_SLAB,
                XMaterial.WHITE_TERRACOTTA,
                XMaterial.MAGENTA_TERRACOTTA,
                XMaterial.POLISHED_BLACKSTONE_WALL,
                XMaterial.DAMAGED_ANVIL,
                XMaterial.WAXED_WEATHERED_COPPER,
                XMaterial.CRACKED_DEEPSLATE_BRICKS,
                XMaterial.LARGE_AMETHYST_BUD,
                XMaterial.DEEPSLATE_GOLD_ORE,
                XMaterial.PISTON_HEAD,
                XMaterial.WAXED_OXIDIZED_CUT_COPPER_STAIRS,
                XMaterial.DEEPSLATE_TILE_SLAB,
                XMaterial.NETHERITE_BLOCK,
                XMaterial.MOSSY_STONE_BRICK_WALL,
                XMaterial.DARK_PRISMARINE_SLAB,
                XMaterial.RED_SANDSTONE_STAIRS,
                XMaterial.PURPUR_PILLAR,
                XMaterial.BLUE_GLAZED_TERRACOTTA,
                XMaterial.PRISMARINE_STAIRS,
                XMaterial.GRANITE,
                XMaterial.STONECUTTER,
                XMaterial.GOLD_ORE,
                XMaterial.DEEPSLATE_LAPIS_ORE,
                XMaterial.BLAST_FURNACE,
                XMaterial.PRISMARINE_BRICK_STAIRS,
                XMaterial.ENCHANTING_TABLE,
                XMaterial.LIGHT_BLUE_TERRACOTTA,
                XMaterial.COAL_ORE,
                XMaterial.LAPIS_BLOCK,
                XMaterial.BLACK_TERRACOTTA,
                XMaterial.PETRIFIED_OAK_SLAB,
                XMaterial.PACKED_ICE,
                XMaterial.BRICK_STAIRS,
                XMaterial.CRACKED_STONE_BRICKS,
                XMaterial.ANDESITE_STAIRS,
                XMaterial.WAXED_WEATHERED_CUT_COPPER_SLAB,
                XMaterial.DEEPSLATE,
                XMaterial.BLACK_GLAZED_TERRACOTTA,
                XMaterial.CRIMSON_NYLIUM,
                XMaterial.STICKY_PISTON,
                XMaterial.PRISMARINE_WALL,
                XMaterial.COBBLESTONE_STAIRS,
                XMaterial.BRICK_SLAB,
                XMaterial.SMOOTH_QUARTZ_STAIRS,
                XMaterial.DEAD_TUBE_CORAL_BLOCK,
                XMaterial.DIAMOND_BLOCK,
                XMaterial.POLISHED_GRANITE_STAIRS,
                XMaterial.POINTED_DRIPSTONE,
                XMaterial.DEEPSLATE_REDSTONE_ORE,
                XMaterial.END_STONE_BRICK_STAIRS,
                XMaterial.DEAD_TUBE_CORAL_WALL_FAN,
                XMaterial.POLISHED_GRANITE_SLAB);
        MINEABLE_PICKAXE.inheritFrom(TERRACOTTA,
                GLAZED_TERRACOTTA,
                WALLS,
                WALL_CORALS,
                SHULKER_BOXES,
                RAILS,
                DIAMOND_ORES,
                GOLD_ORES,
                IRON_ORES,
                EMERALD_ORES,
                COPPER_ORES,
                ANVIL,
                );

        MINEABLE_SHOVEL = new XTag<>(XMaterial.BROWN_CONCRETE_POWDER,
                XMaterial.RED_CONCRETE_POWDER,
                XMaterial.PINK_CONCRETE_POWDER,
                XMaterial.YELLOW_CONCRETE_POWDER,
                XMaterial.GREEN_CONCRETE_POWDER,
                XMaterial.WHITE_CONCRETE_POWDER,
                XMaterial.MAGENTA_CONCRETE_POWDER,
                XMaterial.FARMLAND,
                XMaterial.DIRT_PATH,
                XMaterial.SNOW,
                XMaterial.SNOW_BLOCK,
                XMaterial.BLUE_CONCRETE_POWDER,
                XMaterial.LIGHT_BLUE_CONCRETE_POWDER,
                XMaterial.RED_SAND,
                XMaterial.LIGHT_GRAY_CONCRETE_POWDER,
                XMaterial.COARSE_DIRT,
                XMaterial.SOUL_SAND,
                XMaterial.GRAVEL,
                XMaterial.GRAY_CONCRETE_POWDER,
                XMaterial.PURPLE_CONCRETE_POWDER,
                XMaterial.CYAN_CONCRETE_POWDER,
                XMaterial.SAND,
                XMaterial.ORANGE_CONCRETE_POWDER,
                XMaterial.BLACK_CONCRETE_POWDER,
                XMaterial.PODZOL,
                XMaterial.LIME_CONCRETE_POWDER,
                XMaterial.DIRT,
                XMaterial.CLAY,
                XMaterial.ROOTED_DIRT,
                XMaterial.MYCELIUM,
                XMaterial.SOUL_SOIL,
                XMaterial.GRASS_BLOCK);

        MINEABLE_HOE = new XTag<>(XMaterial.FLOWERING_AZALEA_LEAVES,
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
                XMaterial.MOSS_BLOCK);

        LAVA_POOL_STONE_CANNOT_REPLACE = new XTag<>(XMaterial.DARK_OAK_LEAVES,
                XMaterial.STRIPPED_DARK_OAK_WOOD,
                XMaterial.OAK_WOOD,
                XMaterial.CRIMSON_HYPHAE,
                XMaterial.JUNGLE_LEAVES,
                XMaterial.DARK_OAK_WOOD,
                XMaterial.STRIPPED_ACACIA_LOG,
                XMaterial.DARK_OAK_LOG,
                XMaterial.STRIPPED_DARK_OAK_LOG,
                XMaterial.AZALEA_LEAVES,
                XMaterial.SPAWNER,
                XMaterial.JUNGLE_LOG,
                XMaterial.SPRUCE_LOG,
                XMaterial.STRIPPED_CRIMSON_HYPHAE,
                XMaterial.SPRUCE_LEAVES,
                XMaterial.STRIPPED_BIRCH_LOG,
                XMaterial.ACACIA_LOG,
                XMaterial.STRIPPED_ACACIA_WOOD,
                XMaterial.CRIMSON_STEM,
                XMaterial.BIRCH_WOOD,
                XMaterial.STRIPPED_JUNGLE_WOOD,
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
                XMaterial.STRIPPED_JUNGLE_LOG,
                XMaterial.WARPED_STEM,
                XMaterial.END_PORTAL_FRAME,
                XMaterial.SPRUCE_WOOD,
                XMaterial.STRIPPED_SPRUCE_LOG,
                XMaterial.STRIPPED_SPRUCE_WOOD,
                XMaterial.JUNGLE_WOOD,
                XMaterial.STRIPPED_OAK_WOOD,
                XMaterial.STRIPPED_WARPED_STEM,
                XMaterial.OAK_LOG,
                XMaterial.ACACIA_LEAVES,
                XMaterial.STRIPPED_WARPED_HYPHAE,
                XMaterial.BIRCH_LOG);
        LEATHER_ARMOR_PIECES = new XTag<>(XMaterial.LEATHER_HELMET,
                XMaterial.LEATHER_CHESTPLATE,
                XMaterial.LEATHER_LEGGINGS,
                XMaterial.LEATHER_BOOTS);
        IRON_ARMOR_PIECES = new XTag<>(XMaterial.IRON_HELMET,
                XMaterial.IRON_CHESTPLATE,
                XMaterial.IRON_LEGGINGS,
                XMaterial.IRON_BOOTS);
        CHAINMAIL_ARMOR_PIECES = new XTag<>(XMaterial.CHAINMAIL_HELMET,
                XMaterial.CHAINMAIL_CHESTPLATE,
                XMaterial.CHAINMAIL_LEGGINGS,
                XMaterial.CHAINMAIL_BOOTS);
        GOLDEN_ARMOR_PIECES = new XTag<>(XMaterial.GOLDEN_HELMET,
                XMaterial.GOLDEN_CHESTPLATE,
                XMaterial.GOLDEN_LEGGINGS,
                XMaterial.GOLDEN_BOOTS);
        DIAMOND_ARMOR_PIECES = new XTag<>(XMaterial.DIAMOND_HELMET,
                XMaterial.DIAMOND_CHESTPLATE,
                XMaterial.DIAMOND_LEGGINGS,
                XMaterial.DIAMOND_BOOTS);
        NETHERITE_ARMOR_PIECES = new XTag<>(XMaterial.NETHERITE_HELMET,
                XMaterial.NETHERITE_CHESTPLATE,
                XMaterial.NETHERITE_LEGGINGS,
                XMaterial.NETHERITE_BOOTS);
        WOODEN_TOOLS = new XTag<>(XMaterial.WOODEN_PICKAXE,
                XMaterial.WOODEN_AXE,
                XMaterial.WOODEN_HOE,
                XMaterial.WOODEN_SHOVEL,
                XMaterial.WOODEN_SWORD);
        STONE_TOOLS = new XTag<>(XMaterial.STONE_PICKAXE,
                XMaterial.STONE_AXE,
                XMaterial.STONE_HOE,
                XMaterial.STONE_SHOVEL,
                XMaterial.STONE_SWORD);
        IRON_TOOLS = new XTag<>(XMaterial.IRON_PICKAXE,
                XMaterial.IRON_AXE,
                XMaterial.IRON_HOE,
                XMaterial.IRON_SHOVEL,
                XMaterial.IRON_SWORD);
        DIAMOND_TOOLS = new XTag<>(XMaterial.DIAMOND_PICKAXE,
                XMaterial.DIAMOND_AXE,
                XMaterial.DIAMOND_HOE,
                XMaterial.DIAMOND_SHOVEL,
                XMaterial.DIAMOND_SHOVEL);
        NETHERITE_TOOLS = new XTag<>(XMaterial.NETHERITE_PICKAXE,
                XMaterial.NETHERITE_AXE,
                XMaterial.NETHERITE_HOE,
                XMaterial.NETHERITE_SHOVEL,
                XMaterial.NETHERITE_SHOVEL);
        ARMOR_PIECES = new XTag<>(XMaterial.TURTLE_HELMET);
        ARMOR_PIECES.inheritFrom(LEATHER_ARMOR_PIECES,
                CHAINMAIL_ARMOR_PIECES,
                IRON_ARMOR_PIECES,
                GOLDEN_ARMOR_PIECES,
                DIAMOND_ARMOR_PIECES,
                NETHERITE_ARMOR_PIECES);

        AZALEA_GROWS_ON = new XTag<>(XMaterial.SNOW_BLOCK, XMaterial.POWDER_SNOW);
        AZALEA_GROWS_ON.inheritFrom(TERRACOTTA, SAND, DIRT);
        AZALEA_ROOT_REPLACEABLE = new XTag<>(XMaterial.CLAY, XMaterial.GRAVEL);
        AZALEA_ROOT_REPLACEABLE.inheritFrom(AZALEA_GROWS_ON, CAVE_VINES, BASE_STONE_OVERWORLD);
        BAMBOO_PLANTABLE_ON = new XTag<>(XMaterial.GRAVEL, XMaterial.BAMBOO_SAPLING, XMaterial.BAMBOO);
        BAMBOO_PLANTABLE_ON.inheritFrom(DIRT, SAND);
        BEE_GROWABLES = new XTag<>(XMaterial.SWEET_BERRY_BUSH);
        BEE_GROWABLES.inheritFrom(CROPS, CAVE_VINES);
        BIG_DRIPLEAF_PLACEABLE = new XTag<>(XMaterial.CLAY, XMaterial.FARMLAND);
        BIG_DRIPLEAF_PLACEABLE.inheritFrom(DIRT);
        BUTTONS = new XTag<>(XMaterial.STONE_BUTTON,
                XMaterial.POLISHED_BLACKSTONE_BUTTON);
        BUTTONS.inheritFrom(WOODEN_BUTTONS);
        CORAL_FANS = new XTag<>(XMaterial.FIRE_CORAL_FAN,
                XMaterial.TUBE_CORAL_FAN,
                XMaterial.BRAIN_CORAL_FAN,
                XMaterial.HORN_CORAL_FAN,
                XMaterial.BUBBLE_CORAL_FAN);
        CORALS = new XTag<>(XMaterial.class, CORAL_FANS, CORAL_PLANTS, WALL_CORALS);
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
                XMaterial.GOLD_INGOT);
        ITEMS_PIGLIN_LOVED.inheritFrom(GOLD_ORES, GOLDEN_ARMOR_PIECES);
        SIGNS = new XTag<>(XMaterial.class,
                WALL_SIGNS,
                STANDING_SIGNS);
        PRESSURE_PLATES = new XTag<>(XMaterial.LIGHT_WEIGHTED_PRESSURE_PLATE,
                XMaterial.HEAVY_WEIGHTED_PRESSURE_PLATE);
        PRESSURE_PLATES.inheritFrom(STONE_PRESSURE_PLATES, WOODEN_PRESSURE_PLATES);
        DRAGON_IMMUNE = new XTag<>(XMaterial.IRON_BARS,
                XMaterial.OBSIDIAN,
                XMaterial.RESPAWN_ANCHOR,
                XMaterial.END_STONE,
                XMaterial.CRYING_OBSIDIAN);
        DRAGON_IMMUNE.inheritFrom(WITHER_IMMUNE);
        WALL_POST_OVERRIDE = new XTag<>(XMaterial.TORCH,
                XMaterial.TRIPWIRE,
                XMaterial.REDSTONE_TORCH,
                XMaterial.SOUL_TORCH);
        WALL_POST_OVERRIDE.inheritFrom(SIGNS, BANNERS, PRESSURE_PLATES);
        UNDERWATER_BONEMEALS = new XTag<>(XMaterial.SEAGRASS);
        UNDERWATER_BONEMEALS.inheritFrom(CORALS,WALL_CORALS);
        UNSTABLE_BOTTOM_CENTER = new XTag<>(XMaterial.class,
                FENCE_GATES);
        PREVENT_MOB_SPAWNING_INSIDE = new XTag<>(XMaterial.class,
                RAILS);
        PARROTS_SPAWNABLE_ON = new XTag<>(XMaterial.AIR, XMaterial.GRASS_BLOCK);
        OCCLUDES_VIBRATION_SIGNALS = new XTag<>(XMaterial.class, WOOL);
        LOGS_THAT_BURN = new XTag<>(XMaterial.class, ACACIA_LOGS,
                OAK_LOGS,
                DARK_OAK_LOGS,
                SPRUCE_LOGS,
                JUNGLE_LOGS,
                BIRCH_LOGS);
        LOGS = new XTag<>(XMaterial.class,
                LOGS_THAT_BURN,
                CRIMSON_STEMS,
                WARPED_STEMS);
        PARROTS_SPAWNABLE_ON.inheritFrom(LEAVES, LOGS);
        LUSH_GROUND_REPLACEABLE = new XTag<>(XMaterial.GRAVEL,
                XMaterial.SAND,
                XMaterial.CLAY);
        LUSH_GROUND_REPLACEABLE.inheritFrom(CAVE_VINES,
                DIRT,
                BASE_STONE_OVERWORLD);
        TRAPDOORS = new XTag<>(XMaterial.IRON_TRAPDOOR);
        TRAPDOORS.inheritFrom(WOODEN_TRAPDOORS);
        MUSHROOM_GROW_BLOCK = new XTag<>(XMaterial.PODZOL, XMaterial.MYCELIUM);
        MUSHROOM_GROW_BLOCK.inheritFrom(NYLIUM);
        MOSS_REPLACEABLE = new XTag<>(XMaterial.class,
                CAVE_VINES,
                DIRT,
                BASE_STONE_OVERWORLD);

        ARMOR_ENCHANTS = new XTag<>(XEnchantment.PROTECTION_EXPLOSIONS,
                XEnchantment.BINDING_CURSE,
                XEnchantment.VANISHING_CURSE,
                XEnchantment.PROTECTION_FIRE,
                XEnchantment.MENDING,
                XEnchantment.PROTECTION_PROJECTILE,
                XEnchantment.PROTECTION_ENVIRONMENTAL,
                XEnchantment.THORNS,
                XEnchantment.DURABILITY);

        HELEMT_ENCHANTS = new XTag<>(XEnchantment.WATER_WORKER,
                XEnchantment.OXYGEN);
        HELEMT_ENCHANTS.inheritFrom(ARMOR_ENCHANTS);

        CHESTPLATE_ENCHANTS = new XTag<>(XEnchantment.class, ARMOR_ENCHANTS);

        LEGGINGS_ENCHANTS = new XTag<>(XEnchantment.class, ARMOR_ENCHANTS);

        BOOTS_ENCHANTS = new XTag<>(XEnchantment.DEPTH_STRIDER,
                XEnchantment.PROTECTION_FALL,
                XEnchantment.FROST_WALKER);
        BOOTS_ENCHANTS.inheritFrom(ARMOR_ENCHANTS);

        ELYTRA_ENCHANTS = new XTag<>(XEnchantment.BINDING_CURSE,
                XEnchantment.VANISHING_CURSE,
                XEnchantment.MENDING,
                XEnchantment.DURABILITY);

        SWORD_ENCHANTS = new XTag<>(XEnchantment.DAMAGE_ARTHROPODS,
                XEnchantment.VANISHING_CURSE,
                XEnchantment.FIRE_ASPECT,
                XEnchantment.KNOCKBACK,
                XEnchantment.LOOT_BONUS_MOBS,
                XEnchantment.MENDING,
                XEnchantment.DAMAGE_ALL,
                XEnchantment.DAMAGE_UNDEAD,
                XEnchantment.SWEEPING_EDGE,
                XEnchantment.DURABILITY);

        AXE_ENCHANTS = new XTag<>(XEnchantment.DAMAGE_ARTHROPODS,
                XEnchantment.VANISHING_CURSE,
                XEnchantment.DIG_SPEED,
                XEnchantment.LOOT_BONUS_BLOCKS,
                XEnchantment.MENDING,
                XEnchantment.DAMAGE_ALL,
                XEnchantment.SILK_TOUCH,
                XEnchantment.DAMAGE_UNDEAD,
                XEnchantment.DURABILITY);

        HOE_ENCHANTS = new XTag<>(XEnchantment.VANISHING_CURSE,
                XEnchantment.DIG_SPEED,
                XEnchantment.LOOT_BONUS_BLOCKS,
                XEnchantment.MENDING,
                XEnchantment.SILK_TOUCH,
                XEnchantment.DURABILITY);

        PICKAXE_ENCHANTS = new XTag<>(XEnchantment.VANISHING_CURSE,
                XEnchantment.DIG_SPEED,
                XEnchantment.LOOT_BONUS_BLOCKS,
                XEnchantment.MENDING,
                XEnchantment.SILK_TOUCH,
                XEnchantment.DURABILITY);

        SHOVEL_ENCHANTS = new XTag<>(XEnchantment.VANISHING_CURSE,
                XEnchantment.DIG_SPEED,
                XEnchantment.LOOT_BONUS_BLOCKS,
                XEnchantment.MENDING,
                XEnchantment.SILK_TOUCH,
                XEnchantment.DURABILITY);

        SHEARS_ENCHANTS = new XTag<>(XEnchantment.VANISHING_CURSE,
                XEnchantment.DIG_SPEED,
                XEnchantment.MENDING,
                XEnchantment.DURABILITY);

        BOW_ENCHANTS = new XTag<>(XEnchantment.VANISHING_CURSE,
                XEnchantment.ARROW_FIRE,
                XEnchantment.ARROW_INFINITE,
                XEnchantment.MENDING,
                XEnchantment.ARROW_KNOCKBACK,
                XEnchantment.DURABILITY);

        CROSSBOW_ENCHANTS = new XTag<>(XEnchantment.VANISHING_CURSE,
                XEnchantment.MENDING,
                XEnchantment.MULTISHOT,
                XEnchantment.PIERCING,
                XEnchantment.QUICK_CHARGE,
                XEnchantment.DURABILITY);


        MINEABLE_AXE = new XTag<>(XMaterial.COMPOSTER,
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
                XMaterial.GRASS,
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
                XMaterial.BOOKSHELF);

        MINEABLE_AXE.inheritFrom(BANNERS,
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
                CAMPFIRES);

    }

    private XTag(@NonNull T... values) {
        this.values = Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(values)));
    }

    private XTag(@NonNull Class<T> clazz ,@NonNull XTag<@NonNull T>... values) {
        this.values = EnumSet.noneOf(clazz);
        this.inheritFrom(values);
    }

    private @NonNull Set<@NonNull T> values;

    /**
     *
     * @return {@link Set} of all the values represented by the tag
     */
    public @NonNull Set<@NonNull T> getValues() {
        return this.values;
    }

    public boolean isTagged(@Nullable T value) {
        // Just encase some plugins pass thru a null value.
        if(value == null){
            return false;
        }
        return this.values.contains(value);
    }

    private void inheritFrom(@NonNull XTag<@NonNull T>... values) {
        Set<@NonNull T> newValues;
        if (this.values.isEmpty()) newValues = EnumSet.copyOf((EnumSet<T>) this.values);
        else newValues = EnumSet.copyOf(this.values);
        for (XTag<T> value : values) {
            newValues.addAll(value.values);
        }
        this.values = Collections.unmodifiableSet(newValues);
    }


    /*
     * ---------------------------------------
     */

    private static void initLogs() {
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
        OAK_LOGS = new XTag<>(XMaterial.STRIPPED_OAK_LOG,
                XMaterial.OAK_LOG,
                XMaterial.OAK_WOOD,
                XMaterial.STRIPPED_OAK_WOOD);
        SPRUCE_LOGS = new XTag<>(XMaterial.STRIPPED_SPRUCE_LOG,
                XMaterial.SPRUCE_LOG,
                XMaterial.SPRUCE_WOOD,
                XMaterial.STRIPPED_SPRUCE_WOOD);
    }

    private static void initColorables() {
        CANDLE_CAKES = new XTag<>(findAllColors("CANDLE_CAKE"));
        CANDLES = new XTag<>(findAllColors("CANDLE"));
        TERRACOTTA = new XTag<>(findAllColors("TERRACOTTA"));
        GLAZED_TERRACOTTA = new XTag<>(findAllColors("GLAZED_TERRACOTTA"));
        SHULKER_BOXES = new XTag<>(findAllColors("SHULKER_BOX"));
        CARPETS = new XTag<>(findAllColors("CARPET"));
        WOOL = new XTag<>(findAllColors("WOOL"));
        GLASS = new XTag<>(findAllColors("GLASS"));
        GLASS.inheritFrom(new XTag<>(XMaterial.TINTED_GLASS));
        ITEMS_BANNERS = new XTag<>(findAllColors("BANNER"));
        WALL_BANNERS = new XTag<>(findAllColors("WALL_BANNER"));
        BANNERS = new XTag<>(XMaterial.class, ITEMS_BANNERS, WALL_BANNERS);
        BEDS = new XTag<>(findAllColors("BED"));
        CONCRETE = new XTag<>(findAllColors("CONCRETE"));
        CONCRETE_POWDER = new XTag<>(findAllColors("CONCRETE_POWDER"));
    }

    private static void initWoodenMaterials() {
        STANDING_SIGNS = new XTag<>(findAllWoodTypes("SIGN"));
        WALL_SIGNS = new XTag<>(findAllWoodTypes("WALL_SIGN"));
        WOODEN_PRESSURE_PLATES = new XTag<>(findAllWoodTypes("PRESSURE_PLATE"));
        WOODEN_DOORS = new XTag<>(findAllWoodTypes("DOOR"));
        WOODEN_FENCE_GATES = new XTag<>(findAllWoodTypes("FENCE_GATE"));
        WOODEN_FENCES = new XTag<>(findAllWoodTypes("FENCE"));
        WOODEN_SLABS = new XTag<>(findAllWoodTypes("SLAB"));
        WOODEN_STAIRS = new XTag<>(findAllWoodTypes("STAIRS"));
        WOODEN_TRAPDOORS = new XTag<>(findAllWoodTypes("TRAPDOOR"));
        PLANKS = new XTag<>(findAllWoodTypes("PLANKS"));
        WOODEN_BUTTONS = new XTag<>(findAllWoodTypes("BUTTON"));
    }

    private static XMaterial[] findAllColors(String material) {
        String[] colorPrefixes = new String[]{"ORANGE", "LIGHT_BLUE", "GRAY", "BLACK", "MAGENTA", "PINK", "BLUE", "GREEN", "CYAN", "PURPLE", "YELLOW", "LIME", "LIGHT_GRAY", "WHITE", "BROWN", "RED"};
        List<XMaterial> list = new ArrayList<>();
        XMaterial.matchXMaterial(material).ifPresent(list::add);
        for (String color : colorPrefixes) {
            XMaterial.matchXMaterial(color + "_" + material).ifPresent(list::add);
        }
        return list.toArray(new XMaterial[0]);
    }

    private static XMaterial[] findAllWoodTypes(String material) {
        String[] woodPrefixes = new String[]{"ACACIA", "DARK_OAK", "JUNGLE", "BIRCH", "WARPED", "OAK", "SPRUCE", "CRIMSON"};
        List<XMaterial> list = new ArrayList<>();
        for (String wood : woodPrefixes) {
            XMaterial.matchXMaterial(wood + "_" + material).ifPresent(list::add);
        }
        return list.toArray(new XMaterial[0]);
    }

}
