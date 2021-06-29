package com.cryptomorin.xseries;

import org.bukkit.Material;

/**
 * This class is excluded from the repository by default. If you wish to add it to your project, you can download it
 * directly from GitHub.
 * <p>
 * All the following methods are taken directly from Spigot with minimal changes.
 * https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/Material.java
 *
 * @author Crypto Morin
 */
public final class XMaterialUtil {
    /**
     * Checks if this Material is an obtainable item. "Obtainable items" are simply materials that can be displayed in your GUI.
     * This method is mainly designed to support pre-1.13, servers using 1.13 and above will directly have their materials checked with {@link Material#isItem()}
     *
     * @return true if this material is an item.
     * @since 1.13
     */
    public boolean isItem(XMaterial material) {
        if (XMaterial.isNewVersion()) return material.parseMaterial().isItem();
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
     * Note that the interactability of some materials may be dependant on their
     * state as well. This method will return true if there is at least one
     * state in which additional interact handling is performed for the
     * material.
     *
     * @return true if this material can be interacted with.
     * @since 1.13
     */
    public boolean isInteractable(XMaterial material) {
        if (XMaterial.isNewVersion()) return material.parseMaterial().isInteractable();
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
     * Check if the material is an air block.
     *
     * @return True if this material is an air block.
     * @since 1.14.4
     */
    public boolean isAir(XMaterial material) {
        switch (material) {
            case AIR:
            case CAVE_AIR:
            case VOID_AIR:
                return true;
            default:
                return false;
        }
    }
}
