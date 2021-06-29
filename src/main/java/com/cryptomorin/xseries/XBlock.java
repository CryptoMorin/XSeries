/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Crypto Morin
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

import org.apache.commons.lang.Validate;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.*;

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
 * @version 2.1.0.1
 * @see Block
 * @see BlockState
 * @see MaterialData
 * @see XMaterial
 */
@SuppressWarnings("deprecation")
public final class XBlock {
    public static final Set<XMaterial> CROPS = Collections.unmodifiableSet(EnumSet.of(
            XMaterial.CARROT, XMaterial.POTATO, XMaterial.NETHER_WART, XMaterial.WHEAT_SEEDS, XMaterial.PUMPKIN_SEEDS,
            XMaterial.MELON_SEEDS, XMaterial.BEETROOT_SEEDS, XMaterial.SUGAR_CANE, XMaterial.BAMBOO_SAPLING, XMaterial.CHORUS_PLANT,
            XMaterial.KELP, XMaterial.SEA_PICKLE, XMaterial.BROWN_MUSHROOM, XMaterial.RED_MUSHROOM
    ));
    public static final Set<XMaterial> DANGEROUS = Collections.unmodifiableSet(EnumSet.of(
            XMaterial.MAGMA_BLOCK, XMaterial.LAVA, XMaterial.CAMPFIRE, XMaterial.FIRE, XMaterial.SOUL_FIRE
    ));
    public static final byte CAKE_SLICES = 6;
    private static final boolean ISFLAT = XMaterial.isNewVersion();

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
     *
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
            org.bukkit.block.data.Lightable lightable = (org.bukkit.block.data.Lightable) block.getBlockData();
            lightable.setLit(lit);
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
     *
     * @return true if this material is a crop, otherwise false.
     */
    public static boolean isCrop(XMaterial material) {
        return CROPS.contains(material);
    }

    /**
     * Any material that can damage players, usually by interacting with the block.
     *
     * @param material the material to check.
     *
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
        if (data instanceof org.bukkit.material.Directional) return ((org.bukkit.material.Directional) data).getFacing();
        return BlockFace.SELF;
    }

    public static boolean setDirection(Block block, BlockFace facing) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Directional)) return false;
            org.bukkit.block.data.Directional direction = (org.bukkit.block.data.Directional) block.getBlockData();
            direction.setFacing(facing);
            return true;
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (data instanceof Directional) {
            ((Directional) data).setFacingDirection(facing);
            state.update(true);
            return true;
        }
        return false;
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
            org.bukkit.block.data.Ageable ageable = (org.bukkit.block.data.Ageable) block.getBlockData();
            ageable.setAge(age);
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
     *
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
     *
     * @return true if this block can have a fluid level, otherwise false.
     */
    public static boolean setFluidLevel(Block block, int level) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Levelled)) return false;
            org.bukkit.block.data.Levelled levelled = (org.bukkit.block.data.Levelled) block.getBlockData();
            levelled.setLevel(level);
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
            if (xMat.isPresent()) {
                if (matched == xMat.get() || isType(block, xMat.get())) return true;
            }
        }
        return false;
    }

    public static void setCakeSlices(Block block, int amount) {
        Validate.isTrue(isCake(block.getType()), "Block is not a cake: " + block.getType());
        if (ISFLAT) {
            org.bukkit.block.data.BlockData bd = block.getBlockData();
            org.bukkit.block.data.type.Cake cake = (org.bukkit.block.data.type.Cake) bd;
            int remaining = cake.getMaximumBites() - (cake.getBites() + amount);
            if (remaining > 0) {
                cake.setBites(remaining);
                block.setBlockData(bd);
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
        Validate.isTrue(isCake(block.getType()), "Block is not a cake: " + block.getType());
        if (ISFLAT) {
            org.bukkit.block.data.BlockData bd = block.getBlockData();
            org.bukkit.block.data.type.Cake cake = (org.bukkit.block.data.type.Cake) bd;
            int bites = cake.getBites() - slices;
            int remaining = cake.getMaximumBites() - bites;

            if (remaining > 0) {
                cake.setBites(bites);
                block.setBlockData(bd);
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

    public static boolean setWooden(Block block, XMaterial species) {
        block.setType(species.parseMaterial());
        if (ISFLAT) return true;

        TreeSpecies type = species == XMaterial.SPRUCE_LOG ? TreeSpecies.REDWOOD :
                TreeSpecies.valueOf(species.name().substring(0, species.name().indexOf('_')));

        BlockState state = block.getState();
        MaterialData data = state.getData();
        ((Wood) data).setSpecies(type);
        state.update(true);
        return true;
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
     * @param block the block to get its XMaterial type.
     *
     * @return the XMaterial of the block.
     * @deprecated Not stable, use {@link #isType(Block, XMaterial)} or {@link #isSimilar(Block, XMaterial)} instead.
     * If you want to save a block material somewhere, you need to use {@link XMaterial#matchXMaterial(Material)}
     */
    @Deprecated
    public static XMaterial getType(Block block) {
        if (ISFLAT) return XMaterial.matchXMaterial(block.getType());
        String type = block.getType().name();
        BlockState state = block.getState();
        MaterialData data = state.getData();
        byte dataValue;

        if (data instanceof Wood) {
            TreeSpecies species = ((Wood) data).getSpecies();
            dataValue = species.getData();
        } else if (data instanceof Colorable) {
            DyeColor color = ((Colorable) data).getColor();
            dataValue = color.getDyeData();
        } else {
            dataValue = data.getData();
        }

        return XMaterial.matchDefinedXMaterial(type, dataValue)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported material for block " + dataValue + ": " + block.getType().name()));
    }

    /**
     * Same as {@link #isType(Block, XMaterial)} except it also does a simple {@link XMaterial#matchXMaterial(Material)}
     * comparison with the given block and material.
     *
     * @param block    the block to compare.
     * @param material the material to compare with.
     *
     * @return true if block type is similar to the given material.
     * @see #isType(Block, XMaterial)
     * @since 1.3.0
     */
    public static boolean isSimilar(Block block, XMaterial material) {
        return material == XMaterial.matchXMaterial(block.getType()) || isType(block, material);
    }

    /**
     * <b>Universal Method</b>
     * <p>
     * Check if the block type matches the specified XMaterial.
     * Note that this method assumes that you've already tried doing {@link XMaterial#matchXMaterial(Material)} using
     * {@link Block#getType()} and compared it with the other XMaterial. If not, use {@link #isSimilar(Block, XMaterial)}
     *
     * @param block    the block to check.
     * @param material the XMaterial similar to this block type.
     *
     * @return true if the raw block type matches with the material.
     * @see #isSimilar(Block, XMaterial)
     */
    public static boolean isType(Block block, XMaterial material) {
        Material mat = block.getType();
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
        if (name.startsWith("REDSTONE_COMPARATOR")) return block.getType() == BlockMaterial.REDSTONE_COMPARATOR_ON.material;
        return false;
    }

    public static void setPowered(Block block, boolean powered) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Powerable)) return;
            org.bukkit.block.data.Powerable powerable = (org.bukkit.block.data.Powerable) block.getBlockData();
            powerable.setPowered(powered);
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
            org.bukkit.block.data.Openable openable = (org.bukkit.block.data.Openable) block.getBlockData();
            openable.setOpen(opened);
            return;
        }

        BlockState state = block.getState();
        if (!(state instanceof Openable)) return;
        Openable openable = (Openable) state.getData();
        openable.setOpen(opened);
        state.setData((MaterialData) openable);
        state.update();
    }

    public static BlockFace getRotation(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Rotatable)) return null;
            org.bukkit.block.data.Rotatable rotatable = (org.bukkit.block.data.Rotatable) block.getBlockData();
            return rotatable.getRotation();
        }

        return null;
    }

    public static void setRotation(Block block, BlockFace facing) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.Rotatable)) return;
            org.bukkit.block.data.Rotatable rotatable = (org.bukkit.block.data.Rotatable) block.getBlockData();
            rotatable.setRotation(facing);
        }
    }

    private static boolean isMaterial(Block block, BlockMaterial... materials) {
        Material type = block.getType();
        for (BlockMaterial material : materials) {
            if (type == material.material) return true;
        }
        return false;
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
