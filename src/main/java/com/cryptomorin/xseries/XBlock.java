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

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Banner;
import org.bukkit.block.Skull;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * <b>XBlock</b> - MaterialData/BlockData Support<br>
 * BlockState (Old): https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/BlockState.html
 * BlockData (New): https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/data/BlockData.html
 * MaterialData (Old): https://hub.spigotmc.org/javadocs/spigot/org/bukkit/material/MaterialData.html
 * <p>
 * All the parameters are non-null except the ones marked as nullable.
 * This class doesn't and shouldn't support materials that are {@link Material#isLegacy()}.
 *
 * @author Crypto Morin
 * @version 3.1.0
 * @see Block
 * @see BlockState
 * @see MaterialData
 * @see XMaterial
 */
@SuppressWarnings("deprecation")
public final class XBlock {
    /**
     * This list contains both block and item version of the same material.
     */
    public static final Set<XMaterial> CROPS = Collections.unmodifiableSet(EnumSet.of(
            XMaterial.CARROT, XMaterial.CARROTS, XMaterial.POTATO, XMaterial.POTATOES,
            XMaterial.NETHER_WART, XMaterial.PUMPKIN_SEEDS, XMaterial.WHEAT_SEEDS, XMaterial.WHEAT,
            XMaterial.MELON_SEEDS, XMaterial.BEETROOT_SEEDS, XMaterial.BEETROOTS, XMaterial.SUGAR_CANE,
            XMaterial.BAMBOO_SAPLING, XMaterial.BAMBOO, XMaterial.CHORUS_PLANT,
            XMaterial.KELP, XMaterial.KELP_PLANT, XMaterial.SEA_PICKLE, XMaterial.BROWN_MUSHROOM, XMaterial.RED_MUSHROOM,
            XMaterial.MELON_STEM, XMaterial.PUMPKIN_STEM, XMaterial.COCOA, XMaterial.COCOA_BEANS

    ));
    public static final Set<XMaterial> DANGEROUS = Collections.unmodifiableSet(EnumSet.of(
            XMaterial.MAGMA_BLOCK, XMaterial.LAVA, XMaterial.CAMPFIRE, XMaterial.FIRE, XMaterial.SOUL_FIRE
    ));
    public static final byte CAKE_SLICES = 6;
    private static final boolean ISFLAT = XMaterial.supports(13);
    private static final Map<XMaterial, XMaterial> ITEM_TO_BLOCK = new EnumMap<>(XMaterial.class);

    static {
        ITEM_TO_BLOCK.put(XMaterial.MELON_SLICE, XMaterial.MELON_STEM);
        ITEM_TO_BLOCK.put(XMaterial.MELON_SEEDS, XMaterial.MELON_STEM);

        ITEM_TO_BLOCK.put(XMaterial.CARROT_ON_A_STICK, XMaterial.CARROTS);
        ITEM_TO_BLOCK.put(XMaterial.GOLDEN_CARROT, XMaterial.CARROTS);
        ITEM_TO_BLOCK.put(XMaterial.CARROT, XMaterial.CARROTS);

        ITEM_TO_BLOCK.put(XMaterial.POTATO, XMaterial.POTATOES);
        ITEM_TO_BLOCK.put(XMaterial.BAKED_POTATO, XMaterial.POTATOES);
        ITEM_TO_BLOCK.put(XMaterial.POISONOUS_POTATO, XMaterial.POTATOES);

        ITEM_TO_BLOCK.put(XMaterial.PUMPKIN_SEEDS, XMaterial.PUMPKIN_STEM);
        ITEM_TO_BLOCK.put(XMaterial.PUMPKIN_PIE, XMaterial.PUMPKIN);
    }

    private XBlock() {
    }

    public static boolean isLit(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Lightable)) return false;
            org.bukkit.block.data.Lightable lightable = (org.bukkit.block.data.Lightable) block.getBlockData();
            return lightable.isLit();
        }

        return isMaterial(block, BlockMaterial.REDSTONE_LAMP_ON, BlockMaterial.REDSTONE_TORCH_ON, BlockMaterial.BURNING_FURNACE);
    }

    /**
     * Checks if the block is a container.
     * Containers are chests, hoppers, enderchests and everything that
     * has an inventory.
     *
     * @param block the block to check.
     * @return true if the block is a container, otherwise false.
     */
    public static boolean isContainer(@Nullable Block block) {
        return block != null && block.getState() instanceof InventoryHolder;
    }

    /**
     * Can be furnaces or redstone lamps.
     *
     * @param block the block to change.
     * @param lit   if it should be lit or not.
     */
    public static void setLit(Block block, boolean lit) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Lightable)) return;
            BlockData data = block.getBlockData();
            org.bukkit.block.data.Lightable lightable = (org.bukkit.block.data.Lightable) data;
            lightable.setLit(lit);
            block.setBlockData(data, false);
            return;
        }

        String name = block.getType().name();
        if (name.endsWith("FURNACE")) block.setType(BlockMaterial.BURNING_FURNACE.material);
        else if (name.startsWith("REDSTONE_LAMP")) block.setType(BlockMaterial.REDSTONE_LAMP_ON.material);
        else block.setType(BlockMaterial.REDSTONE_TORCH_ON.material);
    }

    /**
     * Any material that can be planted which is from {@link #CROPS}
     *
     * @param material the material to check.
     * @return true if this material is a crop, otherwise false.
     */
    public static boolean isCrop(XMaterial material) {
        return CROPS.contains(material);
    }

    /**
     * Any material that can damage players, usually by interacting with the block.
     *
     * @param material the material to check.
     * @return true if this material is dangerous, otherwise false.
     */
    public static boolean isDangerous(XMaterial material) {
        return DANGEROUS.contains(material);
    }

    /**
     * Wool and Dye. But Dye is not a block itself.
     */
    public static DyeColor getColor(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof Colorable)) return null;
            Colorable colorable = (Colorable) block.getBlockData();
            return colorable.getColor();
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (data instanceof Wool) {
            Wool wool = (Wool) data;
            return wool.getColor();
        }
        return null;
    }

    public static boolean isCake(@Nullable Material material) {
        return material == Material.CAKE || material == BlockMaterial.CAKE_BLOCK.material;
    }

    public static boolean isWheat(@Nullable Material material) {
        return material == Material.WHEAT || material == BlockMaterial.CROPS.material;
    }

    public static boolean isSugarCane(@Nullable Material material) {
        return material == Material.SUGAR_CANE || material == BlockMaterial.SUGAR_CANE_BLOCK.material;
    }

    public static boolean isBeetroot(@Nullable Material material) {
        return material == Material.BEETROOT || material == Material.BEETROOTS || material == BlockMaterial.BEETROOT_BLOCK.material;
    }

    public static boolean isNetherWart(@Nullable Material material) {
        return material == Material.NETHER_WART || material == BlockMaterial.NETHER_WARTS.material;
    }

    public static boolean isCarrot(@Nullable Material material) {
        return material == Material.CARROT || material == Material.CARROTS;
    }

    public static boolean isMelon(@Nullable Material material) {
        return material == Material.MELON || material == Material.MELON_SLICE || material == BlockMaterial.MELON_BLOCK.material;
    }

    public static boolean isPotato(@Nullable Material material) {
        return material == Material.POTATO || material == Material.POTATOES;
    }

    public static BlockFace getDirection(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Directional)) return BlockFace.SELF;
            org.bukkit.block.data.Directional direction = (org.bukkit.block.data.Directional) block.getBlockData();
            return direction.getFacing();
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (data instanceof org.bukkit.material.Directional)
            return ((org.bukkit.material.Directional) data).getFacing();
        return BlockFace.SELF;
    }

    public static boolean setDirection(Block block, BlockFace facing) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Directional)) return false;
            BlockData data = block.getBlockData();
            org.bukkit.block.data.Directional direction = (org.bukkit.block.data.Directional) data;
            direction.setFacing(facing);
            block.setBlockData(data, false);
            return true;
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (data instanceof Directional) {
            if (XMaterial.matchXMaterial(block.getType()) == XMaterial.LADDER) facing = facing.getOppositeFace();
            ((Directional) data).setFacingDirection(facing);
            state.update(true);
            return true;
        }
        return false;
    }

    public static boolean setType(@Nonnull Block block, @Nullable XMaterial material, boolean applyPhysics) {
        Objects.requireNonNull(block, "Cannot set type of null block");
        if (material == null) material = XMaterial.AIR;
        XMaterial smartConversion = ITEM_TO_BLOCK.get(material);
        if (smartConversion != null) material = smartConversion;

        Material parsedMat = material.parseMaterial();
        if (parsedMat == null) return false;

        String parsedName = parsedMat.name();

        // SKULL_ITEM is for items and SKULL is for blocks.
        SkullType skullType = getSkullType(material);
        if (!ISFLAT && (parsedName.equals("SKULL_ITEM") || skullType != null)) parsedMat = Material.valueOf("SKULL");

        block.setType(parsedMat, applyPhysics);
        if (ISFLAT) return false;

        if (parsedName.endsWith("_ITEM")) {
            String blockName = parsedName.substring(0, parsedName.length() - "_ITEM".length());
            Material blockMaterial = Objects.requireNonNull(Material.getMaterial(blockName),
                    () -> "Could not find block material for item '" + parsedName + "' as '" + blockName + '\'');
            block.setType(blockMaterial, applyPhysics);
        } else if (parsedName.contains("CAKE")) {
            Material blockMaterial = Material.getMaterial("CAKE_BLOCK");
            block.setType(blockMaterial, applyPhysics);
        }

        LegacyMaterial legacyMaterial = LegacyMaterial.getMaterial(parsedName);
        if (legacyMaterial == LegacyMaterial.BANNER)
            block.setType(LegacyMaterial.STANDING_BANNER.material, applyPhysics);
        LegacyMaterial.Handling handling = legacyMaterial == null ? null : legacyMaterial.handling;

        BlockState state = block.getState();
        boolean update = false;

        if (handling == LegacyMaterial.Handling.COLORABLE) {
            if (state instanceof Banner) {
                Banner banner = (Banner) state;
                String xName = material.name();
                int colorIndex = xName.indexOf('_');
                String color = xName.substring(0, colorIndex);
                if (color.equals("LIGHT")) color = xName.substring(0, "LIGHT_".length() + 4);

                banner.setBaseColor(DyeColor.valueOf(color));
            } else state.setRawData(material.getData());
            update = true;
        } else if (handling == LegacyMaterial.Handling.WOOD_SPECIES) {
            // Wood doesn't exist in 1.8
            // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/material/Wood.java?until=7d83cba0f2575112577ed7a091ed8a193bfc261a&untilPath=src%2Fmain%2Fjava%2Forg%2Fbukkit%2Fmaterial%2FWood.java
            // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/TreeSpecies.java

            String name = material.name();
            int firstIndicator = name.indexOf('_');
            if (firstIndicator < 0) return false;
            String woodType = name.substring(0, firstIndicator);

            TreeSpecies species;
            switch (woodType) {
                case "OAK":
                    species = TreeSpecies.GENERIC;
                    break;
                case "DARK":
                    species = TreeSpecies.DARK_OAK;
                    break;
                case "SPRUCE":
                    species = TreeSpecies.REDWOOD;
                    break;
                default: {
                    try {
                        species = TreeSpecies.valueOf(woodType);
                    } catch (IllegalArgumentException ex) {
                        throw new AssertionError("Unknown material " + legacyMaterial + " for wood species");
                    }
                }
            }

            // Doesn't handle stairs, slabs, fence and fence gates as they had their own separate materials.
            boolean firstType = false;
            switch (legacyMaterial) {
                case WOOD:
                case WOOD_DOUBLE_STEP:
                    state.setRawData(species.getData());
                    update = true;
                    break;
                case LOG:
                case LEAVES:
                    firstType = true;
                    // fall through to next switch statement below
                case LOG_2:
                case LEAVES_2:
                    switch (species) {
                        case GENERIC:
                        case REDWOOD:
                        case BIRCH:
                        case JUNGLE:
                            if (!firstType)
                                throw new AssertionError("Invalid tree species " + species + " for block type" + legacyMaterial + ", use block type 2 instead");
                            break;
                        case ACACIA:
                        case DARK_OAK:
                            if (firstType)
                                throw new AssertionError("Invalid tree species " + species + " for block type 2 " + legacyMaterial + ", use block type instead");
                            break;
                    }
                    state.setRawData((byte) ((state.getRawData() & 0xC) | (species.getData() & 0x3)));
                    update = true;
                    break;
                case SAPLING:
                case WOOD_STEP:
                    state.setRawData((byte) ((state.getRawData() & 0x8) | species.getData()));
                    update = true;
                    break;
                default:
                    throw new AssertionError("Unknown block type " + legacyMaterial + " for tree species: " + species);
            }
        } else if (material.getData() != 0) {
            if (skullType != null) {
                boolean isWallSkull = material.name().contains("WALL");
                state.setRawData((byte) (isWallSkull ? 0 : 1));
            } else {
                state.setRawData(material.getData());
            }
            update = true;
        }

        if (skullType != null) {
            Skull skull = (Skull) state;
            skull.setSkullType(skullType);
            update = true;
        }

        if (update) state.update(true, applyPhysics);
        return update;
    }

    public static SkullType getSkullType(XMaterial material) {
        switch (material) {
            case PLAYER_HEAD:
            case PLAYER_WALL_HEAD:
                return SkullType.PLAYER;
            case DRAGON_HEAD:
            case DRAGON_WALL_HEAD:
                return SkullType.DRAGON;
            case ZOMBIE_HEAD:
            case ZOMBIE_WALL_HEAD:
                return SkullType.ZOMBIE;
            case CREEPER_HEAD:
            case CREEPER_WALL_HEAD:
                return SkullType.CREEPER;
            case SKELETON_SKULL:
            case SKELETON_WALL_SKULL:
                return SkullType.SKELETON;
            case WITHER_SKELETON_SKULL:
            case WITHER_SKELETON_WALL_SKULL:
                return SkullType.WITHER;
            case PIGLIN_HEAD:
            case PIGLIN_WALL_HEAD:
                return SkullType.PIGLIN;
            default:
                return null;
        }
    }

    public static boolean setType(@Nonnull Block block, @Nullable XMaterial material) {
        return setType(block, material, true);
    }

    public static int getAge(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Ageable)) return 0;
            org.bukkit.block.data.Ageable ageable = (org.bukkit.block.data.Ageable) block.getBlockData();
            return ageable.getAge();
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        return data.getData();
    }

    public static void setAge(Block block, int age) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Ageable)) return;
            BlockData data = block.getBlockData();
            org.bukkit.block.data.Ageable ageable = (org.bukkit.block.data.Ageable) data;
            ageable.setAge(age);
            block.setBlockData(data, false);
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        data.setData((byte) age);
        state.update(true);
    }

    /**
     * Sets the type of any block that can be colored.
     *
     * @param block the block to color.
     * @param color the color to use.
     * @return true if the block can be colored, otherwise false.
     */
    public static boolean setColor(Block block, DyeColor color) {
        if (ISFLAT) {
            String type = block.getType().name();
            int index = type.indexOf('_');
            if (index == -1) return false;

            String realType = type.substring(index);
            Material material = Material.getMaterial(color.name() + '_' + realType);
            if (material == null) return false;
            block.setType(material);
            return true;
        }

        BlockState state = block.getState();
        state.setRawData(color.getWoolData());
        state.update(true);
        return false;
    }

    /**
     * Can be used on cauldrons as well.
     *
     * @param block the block to set the fluid level of.
     * @param level the level of fluid.
     * @return true if this block can have a fluid level, otherwise false.
     */
    public static boolean setFluidLevel(Block block, int level) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Levelled)) return false;
            BlockData data = block.getBlockData();
            org.bukkit.block.data.Levelled levelled = (org.bukkit.block.data.Levelled) data;
            levelled.setLevel(level);
            block.setBlockData(data, false);
            return true;
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        data.setData((byte) level);
        state.update(true);
        return false;
    }

    public static int getFluidLevel(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Levelled)) return -1;
            org.bukkit.block.data.Levelled levelled = (org.bukkit.block.data.Levelled) block.getBlockData();
            return levelled.getLevel();
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        return data.getData();
    }

    public static boolean isWaterStationary(Block block) {
        return ISFLAT ? getFluidLevel(block) < 7 : block.getType() == BlockMaterial.STATIONARY_WATER.material;
    }

    public static boolean isWater(Material material) {
        return material == Material.WATER || material == BlockMaterial.STATIONARY_WATER.material;
    }

    public static boolean isLava(Material material) {
        return material == Material.LAVA || material == BlockMaterial.STATIONARY_LAVA.material;
    }

    /**
     * @deprecated use {@link XTag#anyMatch(Object, Collection)} instead.
     */
    @Deprecated
    public static boolean isOneOf(Block block, Collection<String> blocks) {
        if (blocks == null || blocks.isEmpty()) return false;
        String name = block.getType().name();
        XMaterial matched = XMaterial.matchXMaterial(block.getType());

        for (String comp : blocks) {
            String checker = comp.toUpperCase(Locale.ENGLISH);
            if (checker.startsWith("CONTAINS:")) {
                comp = XMaterial.format(checker.substring(9));
                if (name.contains(comp)) return true;
                continue;
            }
            if (checker.startsWith("REGEX:")) {
                comp = comp.substring(6);
                if (name.matches(comp)) return true;
                continue;
            }

            // Direct Object Equals
            Optional<XMaterial> xMat = XMaterial.matchXMaterial(comp);
            if (xMat.isPresent() && isSimilar(block, xMat.get())) return true;
        }
        return false;
    }

    public static void setCakeSlices(Block block, int amount) {
        if (!isCake(block.getType())) throw new IllegalArgumentException("Block is not a cake: " + block.getType());
        if (ISFLAT) {
            BlockData data = block.getBlockData();
            org.bukkit.block.data.type.Cake cake = (org.bukkit.block.data.type.Cake) data;
            int remaining = cake.getMaximumBites() - (cake.getBites() + amount);
            if (remaining > 0) {
                cake.setBites(remaining);
                block.setBlockData(data);
            } else {
                block.breakNaturally();
            }

            return;
        }

        BlockState state = block.getState();
        Cake cake = (Cake) state.getData();
        if (amount > 0) {
            cake.setSlicesRemaining(amount);
            state.update(true);
        } else {
            block.breakNaturally();
        }
    }

    public static int addCakeSlices(Block block, int slices) {
        if (!isCake(block.getType())) throw new IllegalArgumentException("Block is not a cake: " + block.getType());
        if (ISFLAT) {
            BlockData data = block.getBlockData();
            org.bukkit.block.data.type.Cake cake = (org.bukkit.block.data.type.Cake) data;
            int bites = cake.getBites() - slices;
            int remaining = cake.getMaximumBites() - bites;

            if (remaining > 0) {
                cake.setBites(bites);
                block.setBlockData(data);
                return remaining;
            } else {
                block.breakNaturally();
                return 0;
            }
        }

        BlockState state = block.getState();
        Cake cake = (Cake) state.getData();
        int remaining = cake.getSlicesRemaining() + slices;

        if (remaining > 0) {
            cake.setSlicesRemaining(remaining);
            state.update(true);
            return remaining;
        } else {
            block.breakNaturally();
            return 0;
        }
    }

    public static void setEnderPearlOnFrame(Block endPortalFrame, boolean eye) {
        BlockState state = endPortalFrame.getState();
        if (ISFLAT) {
            org.bukkit.block.data.BlockData data = state.getBlockData();
            org.bukkit.block.data.type.EndPortalFrame frame = (org.bukkit.block.data.type.EndPortalFrame) data;
            frame.setEye(eye);
            state.setBlockData(data);
        } else {
            state.setRawData((byte) (eye ? 4 : 0));
        }
        state.update(true);
    }

    /**
     * <b>Universal Method</b>
     *
     * @param block    the block to compare.
     * @param material the material to compare with.
     * @return true if block type is similar to the given material.
     * @since 1.3.0
     */
    public static boolean isSimilar(Block block, XMaterial material) {
        Material mat = block.getType();
        if (material == XMaterial.matchXMaterial(mat)) return true;
        switch (material) {
            case CAKE:
                return isCake(mat);
            case NETHER_WART:
                return isNetherWart(mat);
            case MELON:
            case MELON_SLICE:
                return isMelon(mat);
            case CARROT:
            case CARROTS:
                return isCarrot(mat);
            case POTATO:
            case POTATOES:
                return isPotato(mat);
            case WHEAT:
            case WHEAT_SEEDS:
                return isWheat(mat);
            case BEETROOT:
            case BEETROOT_SEEDS:
            case BEETROOTS:
                return isBeetroot(mat);
            case SUGAR_CANE:
                return isSugarCane(mat);
            case WATER:
                return isWater(mat);
            case LAVA:
                return isLava(mat);
            case AIR:
            case CAVE_AIR:
            case VOID_AIR:
                return isAir(mat);
        }
        return false;
    }

    public static boolean isAir(@Nullable Material material) {
        if (ISFLAT) {
            // material.isAir() doesn't exist for 1.13
            switch (material) {
                case AIR:
                case CAVE_AIR:
                case VOID_AIR:
                    return true;
                default:
                    return false;
            }
        }
        return material == Material.AIR;
    }

    public static boolean isPowered(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Powerable)) return false;
            org.bukkit.block.data.Powerable powerable = (org.bukkit.block.data.Powerable) block.getBlockData();
            return powerable.isPowered();
        }

        String name = block.getType().name();
        if (name.startsWith("REDSTONE_COMPARATOR"))
            return block.getType() == BlockMaterial.REDSTONE_COMPARATOR_ON.material;
        return false;
    }

    public static void setPowered(Block block, boolean powered) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Powerable)) return;
            BlockData data = block.getBlockData();
            org.bukkit.block.data.Powerable powerable = (org.bukkit.block.data.Powerable) data;
            powerable.setPowered(powered);
            block.setBlockData(data, false);
            return;
        }

        String name = block.getType().name();
        if (name.startsWith("REDSTONE_COMPARATOR")) block.setType(BlockMaterial.REDSTONE_COMPARATOR_ON.material);
    }

    public static boolean isOpen(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Openable)) return false;
            org.bukkit.block.data.Openable openable = (org.bukkit.block.data.Openable) block.getBlockData();
            return openable.isOpen();
        }

        BlockState state = block.getState();
        if (!(state instanceof Openable)) return false;
        Openable openable = (Openable) state.getData();
        return openable.isOpen();
    }

    public static void setOpened(Block block, boolean opened) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Openable)) return;
            // These useless "data" variables are used because JVM doesn't like upcasts/downcasts for
            // non-existing classes even if unused.
            BlockData data = block.getBlockData();
            org.bukkit.block.data.Openable openable = (org.bukkit.block.data.Openable) data;
            openable.setOpen(opened);
            block.setBlockData(data, false);
            return;
        }

        BlockState state = block.getState();
        if (!(state instanceof Openable)) return;
        Openable openable = (Openable) state.getData();
        openable.setOpen(opened);
        state.setData((MaterialData) openable);
        state.update(true, true);
    }

    public static BlockFace getRotation(Block block) {
        if (ISFLAT) {
            BlockData blockData = block.getBlockData();
            if (blockData instanceof org.bukkit.block.data.Rotatable) {
                return ((org.bukkit.block.data.Rotatable) blockData).getRotation();
            } else if (blockData instanceof org.bukkit.block.data.Directional) {
                return ((org.bukkit.block.data.Directional) blockData).getFacing();
            }
        } else {
            BlockState state = block.getState();
            if (state instanceof Skull) {
                return ((Skull) state).getRotation();
            } else {
                MaterialData data = state.getData();
                if (data instanceof Directional) {
                    return ((Directional) data).getFacing();
                }
            }
        }

        return null;
    }

    public static void setRotation(Block block, BlockFace facing) {
        if (ISFLAT) {
            BlockData blockData = block.getBlockData();
            if (blockData instanceof org.bukkit.block.data.Rotatable) {
                ((org.bukkit.block.data.Rotatable) blockData).setRotation(facing);
            } else if (blockData instanceof org.bukkit.block.data.Directional) {
                ((org.bukkit.block.data.Directional) blockData).setFacing(facing);
            }
            block.setBlockData(blockData, false);
        } else {
            BlockState state = block.getState();
            if (state instanceof Skull) {
                // Special case because the raw data is used for both rotation and
                // whether the block should be considered a wall skull or a normal skull
                // on the ground. Some of the raw data values represent wall skulls with
                // various rotations, but only values that represent normal skull values
                // on the ground only has one rotation.
                // https://www.spigotmc.org/threads/getting-player-skull-to-spawn-on-top-of-fence.385083/
                // https://www.spigotmc.org/threads/placing-skull-in-world.212900/
                // https://www.spigotmc.org/threads/skull-position-above-block-1-13.343247/
                // https://www.spigotmc.org/threads/solved-update-skull-rotation.47795/
                ((Skull) state).setRotation(facing);
            } else {
                MaterialData data = state.getData();
                if (!(data instanceof Directional)) return;
                Directional directional = (Directional) data;
                directional.setFacingDirection(facing);
            }
            state.update(true, true);
        }
    }

    private static boolean isMaterial(Block block, BlockMaterial... materials) {
        Material type = block.getType();
        for (BlockMaterial material : materials) {
            if (type == material.material) return true;
        }
        return false;
    }

    private enum LegacyMaterial {
        // Colorable
        STANDING_BANNER(Handling.COLORABLE), WALL_BANNER(Handling.COLORABLE), BANNER(Handling.COLORABLE),
        CARPET(Handling.COLORABLE), WOOL(Handling.COLORABLE), STAINED_CLAY(Handling.COLORABLE),
        STAINED_GLASS(Handling.COLORABLE), STAINED_GLASS_PANE(Handling.COLORABLE), THIN_GLASS(Handling.COLORABLE),

        // Wood Species
        WOOD(Handling.WOOD_SPECIES), WOOD_STEP(Handling.WOOD_SPECIES), WOOD_DOUBLE_STEP(Handling.WOOD_SPECIES),
        LEAVES(Handling.WOOD_SPECIES), LEAVES_2(Handling.WOOD_SPECIES),
        LOG(Handling.WOOD_SPECIES), LOG_2(Handling.WOOD_SPECIES),
        SAPLING(Handling.WOOD_SPECIES);

        private static final Map<String, LegacyMaterial> LOOKUP = new HashMap<>();

        static {
            for (LegacyMaterial legacyMaterial : values()) {
                LOOKUP.put(legacyMaterial.name(), legacyMaterial);
            }
        }

        private final Material material = Material.getMaterial(name());
        private final Handling handling;

        LegacyMaterial(Handling handling) {
            this.handling = handling;
        }

        private static LegacyMaterial getMaterial(String name) {
            return LOOKUP.get(name);
        }

        private enum Handling {COLORABLE, WOOD_SPECIES;}
    }

    /**
     * An enum with cached legacy materials which can be used when comparing blocks with blocks and blocks with items.
     *
     * @since 2.0.0
     */
    public enum BlockMaterial {
        // Blocks
        CAKE_BLOCK, CROPS, SUGAR_CANE_BLOCK, BEETROOT_BLOCK, NETHER_WARTS, MELON_BLOCK,

        // Others
        BURNING_FURNACE, STATIONARY_WATER, STATIONARY_LAVA,

        // Toggleable
        REDSTONE_LAMP_ON, REDSTONE_LAMP_OFF,
        REDSTONE_TORCH_ON, REDSTONE_TORCH_OFF,
        REDSTONE_COMPARATOR_ON, REDSTONE_COMPARATOR_OFF;

        @Nullable
        private final Material material;

        BlockMaterial() {
            this.material = Material.getMaterial(this.name());
        }
    }
}
