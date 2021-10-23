package com.cryptomorin.xseries;

import org.bukkit.block.Block;

public class XCoral {

    public static boolean isCoralType(Block block){
        return (isCoral(block) || isCoralBlock(block) || isCoralFan(block));
    }

    public static boolean isCoral(Block block){
        return (isAliveCoral(block) || isDeadCoral(block));
    }

    public static boolean isAliveCoral(Block block){
        return (XBlock.isSimilar(block, XMaterial.TUBE_CORAL) ||
                XBlock.isSimilar(block, XMaterial.HORN_CORAL) ||
                XBlock.isSimilar(block, XMaterial.FIRE_CORAL) ||
                XBlock.isSimilar(block, XMaterial.BUBBLE_CORAL));
    }

    public static boolean isDeadCoral(Block block){
        return (XBlock.isSimilar(block, XMaterial.DEAD_TUBE_CORAL) ||
                XBlock.isSimilar(block, XMaterial.DEAD_HORN_CORAL) ||
                XBlock.isSimilar(block, XMaterial.DEAD_FIRE_CORAL) ||
                XBlock.isSimilar(block, XMaterial.DEAD_BUBBLE_CORAL));
    }

    public static boolean isCoralBlock(Block block){
        return (isAliveCoralBlock(block) || isDeadCoralBlock(block));
    }

    public static boolean isAliveCoralBlock(Block block){
        return (XBlock.isSimilar(block, XMaterial.TUBE_CORAL_BLOCK) ||
                XBlock.isSimilar(block, XMaterial.HORN_CORAL_BLOCK) ||
                XBlock.isSimilar(block, XMaterial.FIRE_CORAL_BLOCK) ||
                XBlock.isSimilar(block, XMaterial.BUBBLE_CORAL_BLOCK));
    }

    public static boolean isDeadCoralBlock(Block block){
        return (XBlock.isSimilar(block, XMaterial.DEAD_TUBE_CORAL_BLOCK) ||
                XBlock.isSimilar(block, XMaterial.DEAD_HORN_CORAL_BLOCK) ||
                XBlock.isSimilar(block, XMaterial.DEAD_FIRE_CORAL_BLOCK) ||
                XBlock.isSimilar(block, XMaterial.DEAD_BUBBLE_CORAL_BLOCK));
    }

    public static boolean isCoralFan(Block block){
        return (isAliveCoralFan(block) || isDeadCoralFan(block));
    }

    public static boolean isAliveCoralFan(Block block){
        return (isAliveGroundCoralFan(block) || isAliveWallCoralFan(block));
    }

    public static boolean isDeadCoralFan(Block block){
        return (isDeadGroundCoralFan(block) || isAliveGroundCoralFan(block));
    }

    public static boolean isGroundCoralFan(Block block){
        return (isAliveGroundCoralFan(block) || isDeadGroundCoralFan(block));
    }

    public static boolean isAliveGroundCoralFan(Block block){
        return (XBlock.isSimilar(block, XMaterial.TUBE_CORAL_FAN) ||
                XBlock.isSimilar(block, XMaterial.HORN_CORAL_FAN) ||
                XBlock.isSimilar(block, XMaterial.FIRE_CORAL_FAN) ||
                XBlock.isSimilar(block, XMaterial.BUBBLE_CORAL_FAN));
    }

    public static boolean isDeadGroundCoralFan(Block block){
        return (XBlock.isSimilar(block, XMaterial.DEAD_TUBE_CORAL_FAN) ||
                XBlock.isSimilar(block, XMaterial.DEAD_HORN_CORAL_FAN) ||
                XBlock.isSimilar(block, XMaterial.DEAD_FIRE_CORAL_FAN) ||
                XBlock.isSimilar(block, XMaterial.DEAD_BUBBLE_CORAL_FAN));
    }

    public static boolean isCoralWallFan(Block block){
        return (isAliveWallCoralFan(block) || isDeadWallCoralFan(block));
    }

    public static boolean isAliveWallCoralFan(Block block){
        return (XBlock.isSimilar(block, XMaterial.TUBE_CORAL_WALL_FAN) ||
                XBlock.isSimilar(block, XMaterial.HORN_CORAL_WALL_FAN) ||
                XBlock.isSimilar(block, XMaterial.FIRE_CORAL_WALL_FAN) ||
                XBlock.isSimilar(block, XMaterial.BUBBLE_CORAL_WALL_FAN));
    }

    public static boolean isDeadWallCoralFan(Block block){
        return (XBlock.isSimilar(block, XMaterial.DEAD_TUBE_CORAL_WALL_FAN) ||
                XBlock.isSimilar(block, XMaterial.DEAD_HORN_CORAL_WALL_FAN) ||
                XBlock.isSimilar(block, XMaterial.DEAD_FIRE_CORAL_WALL_FAN) ||
                XBlock.isSimilar(block, XMaterial.DEAD_BUBBLE_CORAL_WALL_FAN));
    }

}
