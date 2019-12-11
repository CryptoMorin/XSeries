/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Crypto Morin
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
import com.google.common.collect.ImmutableSet;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Powerable;
import org.bukkit.material.*;

import java.util.List;

/*
 * References
 *
 * * * GitHub: https://github.com/CryptoMorin/XSeries/blob/master/XBlock.java
 * * XSeries: https://www.spigotmc.org/threads/378136/
 * BlockState: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/BlockState.html
 * BlockData (New): https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/data/BlockData.html
 * MaterialData (Old): https://hub.spigotmc.org/javadocs/spigot/org/bukkit/material/MaterialData.html
 */

/**
 * <b>XBlock BETA</b> - MaterialData/BlockData Support<br>
 * Supports 1.8+ - Requires XMaterial
 * JavaDocs will be added soon.
 *
 * @author Crypto Morin
 * @version 0.0.1
 * @see Block
 * @see BlockData
 * @see BlockState
 * @see MaterialData
 * @see XMaterial
 */
@SuppressWarnings("deprecation")
public final class XBlock {
    public static final ImmutableSet<XMaterial> CROPS = ImmutableSet.of(
            XMaterial.CARROT, XMaterial.POTATO, XMaterial.NETHER_WART, XMaterial.WHEAT_SEEDS, XMaterial.PUMPKIN_SEEDS,
            XMaterial.MELON_SEEDS, XMaterial.BEETROOT_SEEDS, XMaterial.SUGAR_CANE, XMaterial.BAMBOO_SAPLING, XMaterial.CHORUS_PLANT,
            XMaterial.KELP, XMaterial.SEA_PICKLE, XMaterial.BROWN_MUSHROOM, XMaterial.RED_MUSHROOM
    );
    public static final ImmutableSet<XMaterial> DANGEROUS = ImmutableSet.of(
            XMaterial.MAGMA_BLOCK, XMaterial.LAVA, XMaterial.CAMPFIRE, XMaterial.FIRE
    );
    public static final int CAKE_SLICES = 6;
    private static final boolean ISFLAT = XMaterial.isNewVersion();

    public static boolean isLit(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof Lightable)) return false;
            Lightable lightable = (Lightable) block.getBlockData();
            return lightable.isLit();
        }

        return isMaterial(block, "REDSTONE_LAMP_ON", "REDSTONE_TORCH_ON", "BURNING_FURNACE");
    }

    public static void setLit(Block block, boolean lit) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof Lightable)) return;
            Lightable lightable = (Lightable) block.getBlockData();
            lightable.setLit(lit);
            return;
        }

        String name = block.getType().name();
        if (name.endsWith("FURNACE")) block.setType(Material.valueOf("BURNING_FURNACE"));
        else if (name.startsWith("REDSTONE_LAMP")) block.setType(Material.valueOf("REDSTONE_LAMP_ON"));
        else block.setType(Material.valueOf("REDSTONE_TORCH_ON"));
    }

    public static boolean isCrops(Material material) {
        return CROPS.contains(XMaterial.matchXMaterial(material));
    }

    public static boolean isDangerous(Block block) {
        return DANGEROUS.contains(XMaterial.matchXMaterial(block.getType()));
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

    public static boolean isCake(Material material) {
        return ISFLAT ? material == Material.CAKE : material == Material.matchMaterial("CAKE_BLOCK");
    }

    public static boolean isCake(Block block) {
        return isCake(block.getType());
    }

    public static BlockFace getDirection(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof Directional)) return BlockFace.SELF;
            Directional direction = (Directional) block.getBlockData();
            return direction.getFacing();
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (data instanceof org.bukkit.material.Directional) {
            return ((org.bukkit.material.Directional) data).getFacing();
        }
        return null;
    }

    public static boolean setDirection(Block block, BlockFace facing) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof Directional)) return false;
            Directional direction = (Directional) block.getBlockData();
            direction.setFacing(facing);
            return true;
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (data instanceof org.bukkit.material.Directional) {
            ((org.bukkit.material.Directional) data).setFacingDirection(facing);
            return true;
        }
        return false;
    }

    public static boolean setColor(Block block, DyeColor color) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof Colorable)) return false;
            Colorable colorable = (Colorable) block.getBlockData();
            colorable.setColor(color);
            return true;
        }

        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (data instanceof Wool) {
            Wool wool = (Wool) data;
            wool.setColor(DyeColor.BLUE);
            state.update(true);
            return true;
        }
        return false;
    }

    public static boolean isOneOf(Block block, List<String> blocks) {
        return XMaterial.matchXMaterial(block.getType()).isOneOf(blocks);
    }

    public static void setCakeSlices(Block block, int amount) {
        if (!isCake(block)) throw new IllegalArgumentException("Block is not a cake: " + block.getType());
        if (ISFLAT) {
            BlockData bd = block.getBlockData();
            if (bd instanceof org.bukkit.block.data.type.Cake) {
                org.bukkit.block.data.type.Cake cake = (org.bukkit.block.data.type.Cake) bd;

                if (amount <= cake.getMaximumBites()) {
                    cake.setBites(cake.getBites() + 1);
                } else {
                    block.breakNaturally();
                    return;
                }

                block.setBlockData(bd);
            }
            return;
        }

        BlockState state = block.getState();
        if (state instanceof Cake) {
            Cake cake = (Cake) state.getData();

            if (amount <= 1) {
                cake.setSlicesRemaining(amount);
            } else {
                block.breakNaturally();
                return;
            }

            state.update();
        }
    }

    public static int addCakeSlices(Block block, int slices) {
        if (!isCake(block)) throw new IllegalArgumentException("Block is not a cake: " + block.getType());
        if (ISFLAT) {
            BlockData bd = block.getBlockData();
            org.bukkit.block.data.type.Cake cake = (org.bukkit.block.data.type.Cake) bd;

            if (cake.getBites() + slices <= cake.getMaximumBites()) {
                cake.setBites(cake.getBites() + slices);
            } else {
                block.breakNaturally();
                return cake.getMaximumBites() - cake.getBites();
            }

            block.setBlockData(bd);
            return cake.getMaximumBites() - cake.getBites();
        }

        BlockState state = block.getState();
        Cake cake = (Cake) state.getData();

        if (cake.getSlicesEaten() + slices < CAKE_SLICES) {
            cake.setSlicesEaten(cake.getSlicesEaten() + slices);
        } else {
            block.breakNaturally();
            return cake.getSlicesRemaining();
        }
        state.update();
        return cake.getSlicesRemaining();
    }

    public static boolean setWooden(Block block, XMaterial species) {
        block.setType(species.parseMaterial());
        if (ISFLAT) return true;

        TreeSpecies type = species == XMaterial.SPRUCE_LOG ? TreeSpecies.REDWOOD :
                TreeSpecies.valueOf(species.name().substring(0, species.name().indexOf("_")));
        BlockState state = block.getState();
        MaterialData data = state.getData();
        ((Wood) data).setSpecies(type);
        state.update(true);
        return true;
    }

    /**
     * <b>Universal Method</b>
     *
     * @param block    the block to set its type.
     * @param material the material to set the block's type to.
     */
    public static void setType(Block block, XMaterial material) {
        // TODO...
    }

    public static boolean isPowered(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof Powerable)) return false;
            Powerable powerable = (Powerable) block.getBlockData();
            return powerable.isPowered();
        }

        String name = block.getType().name();
        if (name.startsWith("REDSTONE_COMPARATOR"))
            return isMaterial(block, "REDSTONE_COMPARATOR_ON");

        return false;
    }

    public static void setPowered(Block block, boolean powered) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof Powerable)) return;
            Powerable powerable = (Powerable) block.getBlockData();
            powerable.setPowered(powered);
            return;
        }

        String name = block.getType().name();
        if (name.startsWith("REDSTONE_COMPARATOR")) block.setType(Material.valueOf("REDSTONE_COMPARATOR_ON"));
    }

    public static boolean isOpen(Block block) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof Openable)) return false;
            Openable openable = (Openable) block.getBlockData();
            return openable.isOpen();
        }

        BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.data.Openable)) return false;
        org.bukkit.block.data.Openable openable = (org.bukkit.block.data.Openable) state.getData();
        return openable.isOpen();
    }

    public static void setOpened(Block block, boolean opened) {
        if (ISFLAT) {
            if (!(block.getBlockData() instanceof Openable)) return;
            Openable openable = (Openable) block.getBlockData();
            openable.setOpen(opened);
            return;
        }

        BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.data.Openable)) return;
        org.bukkit.block.data.Openable openable = (org.bukkit.block.data.Openable) state.getData();
        openable.setOpen(opened);
        state.setData((MaterialData) openable);
        state.update();
    }

    public static boolean isMaterial(Block block, String... materials) {
        for (String material : materials) {
            Material mat = Material.matchMaterial(material);
            if (mat != null && block.getType() == mat) return true;
        }
        return false;
    }
}
