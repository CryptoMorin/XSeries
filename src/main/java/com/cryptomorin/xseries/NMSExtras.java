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

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * A class that provides various different essential features that the API
 * didn't/doesn't support.
 * <p>
 * All the parameters are non-null.
 *
 * @author Crypto Morin
 * @version 4.0.0
 */
public final class NMSExtras {
    private static final MethodHandle EXP_PACKET;
    private static final MethodHandle ENTITY_PACKET;
    private static final MethodHandle WORLD_HANDLE;
    private static final MethodHandle LIGHTNING_ENTITY;
    private static final MethodHandle VEC3D;

    private static final MethodHandle ANIMATION_PACKET, ANIMATION_TYPE, ANIMATION_ENTITY_ID;

    private static final MethodHandle PLAY_OUT_MULTI_BLOCK_CHANGE_PACKET, MULTI_BLOCK_CHANGE_INFO, CHUNK_WRAPPER_SET, CHUNK_WRAPPER, SHORTS_OR_INFO, SET_BLOCK_DATA;

    private static final MethodHandle BLOCK_POSITION;
    private static final MethodHandle PLAY_BLOCK_ACTION;
    private static final MethodHandle GET_BLOCK_TYPE;
    private static final MethodHandle GET_BLOCK;

    private static final Class<?>
            MULTI_BLOCK_CHANGE_INFO_CLASS = null, // ReflectionUtils.getNMSClass("PacketPlayOutMultiBlockChange$MultiBlockChangeInfo")
            BLOCK_DATA = ReflectionUtils.getNMSClass("IBlockData");

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle expPacket = null;
        MethodHandle entityPacket = null;
        MethodHandle worldHandle = null;
        MethodHandle lightning = null;
        MethodHandle vec3D = null;

        MethodHandle animationPacket = null;
        MethodHandle animationType = null;
        MethodHandle animationEntityId = null;

        MethodHandle blockPosition = null;
        MethodHandle playBlockAction = null;
        MethodHandle getBlockType = null;
        MethodHandle getBlock = null;

        MethodHandle playOutMultiBlockChange = null, multiBlockChangeInfo = null, chunkWrapper = null, chunkWrapperSet = null,
                shortsOrInfo = null, setBlockData = null;

        try {
            Class<?> nmsEntityType = ReflectionUtils.getNMSClass("EntityTypes");
            Class<?> nmsEntity = ReflectionUtils.getNMSClass("Entity");
            Class<?> nmsVec3D = ReflectionUtils.getNMSClass("Vec3D");
            Class<?> world = ReflectionUtils.getNMSClass("World");

            // https://wiki.vg/Protocol#Set_Experience
            // exp - lvl - total exp
            expPacket = lookup.findConstructor(ReflectionUtils.getNMSClass("PacketPlayOutExperience"), MethodType.methodType(void.class, float.class,
                    int.class, int.class));
            // Lightning
            if (!XMaterial.supports(16)) {
                entityPacket = lookup.findConstructor(ReflectionUtils.getNMSClass("PacketPlayOutSpawnEntityWeather"), MethodType.methodType(void.class,
                        nmsEntity));
            } else {
                vec3D = lookup.findConstructor(nmsVec3D, MethodType.methodType(void.class,
                        double.class, double.class, double.class));

                entityPacket = lookup.findConstructor(ReflectionUtils.getNMSClass("PacketPlayOutSpawnEntity"), MethodType.methodType(void.class,
                        int.class, UUID.class, double.class, double.class, double.class, float.class, float.class, nmsEntityType, int.class, nmsVec3D));
            }

            worldHandle = lookup.findVirtual(ReflectionUtils.getCraftClass("CraftWorld"), "getHandle", MethodType.methodType(
                    ReflectionUtils.getNMSClass("WorldServer")));

            if (!XMaterial.supports(16)) {
                lightning = lookup.findConstructor(ReflectionUtils.getNMSClass("EntityLightning"), MethodType.methodType(void.class,
                        // world, x, y, z, isEffect, isSilent
                        world, double.class, double.class, double.class, boolean.class, boolean.class));
            } else {
                lightning = lookup.findConstructor(ReflectionUtils.getNMSClass("EntityLightning"), MethodType.methodType(void.class,
                        // entitytype, world
                        nmsEntityType, world));
            }

            // Multi Block Change
            Class<?> playOutMultiBlockChangeClass = ReflectionUtils.getNMSClass("PacketPlayOutMultiBlockChange");
            Class<?> chunkCoordIntPairClass = ReflectionUtils.getNMSClass("ChunkCoordIntPair");
            try {
                playOutMultiBlockChange = lookup.findConstructor(playOutMultiBlockChangeClass, MethodType.methodType(void.class));
//                multiBlockChangeInfo = lookup.findConstructor(MULTI_BLOCK_CHANGE_INFO_CLASS, MethodType.methodType(void.class, short.class, BLOCK_DATA));

                // a - chunk
//                Field sectionPositionField = playOutMultiBlockChangeClass.getDeclaredField("a");
//                sectionPositionField.setAccessible(true);
//                chunkWrapperSet = lookup.unreflectSetter(sectionPositionField);

                // b - shorts
//                Field shortsField = playOutMultiBlockChangeClass.getDeclaredField("b");
//                shortsField.setAccessible(true);
//                shortsOrInfo = lookup.unreflectSetter(shortsField);

                // c - block data
//                Field blockDataField = playOutMultiBlockChangeClass.getDeclaredField("c");
//                blockDataField.setAccessible(true);
//                setBlockData = lookup.unreflectSetter(blockDataField);

                if (XMaterial.supports(16)) {
//                    Class<?> sectionPosClass = ReflectionUtils.getNMSClass("SectionPosition");
//                    chunkWrapper = lookup.findConstructor(sectionPosClass, MethodType.methodType(int.class, int.class, int.class));
                } else chunkWrapper = lookup.findConstructor(chunkCoordIntPairClass, MethodType.methodType(void.class, int.class, int.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            Class<?> animation = ReflectionUtils.getNMSClass("PacketPlayOutAnimation");
            animationPacket = lookup.findConstructor(animation, MethodType.methodType(void.class));
            Field field = animation.getDeclaredField("a");
            field.setAccessible(true);
            animationEntityId = lookup.unreflectSetter(field);
            field = animation.getDeclaredField("b");
            field.setAccessible(true);
            animationType = lookup.unreflectSetter(field);


            Class<?> blockPos = ReflectionUtils.getNMSClass("BlockPosition");
            Class<?> blockData = ReflectionUtils.getNMSClass("IBlockData");
            Class<?> block = ReflectionUtils.getNMSClass("Block");
            blockPosition = lookup.findConstructor(blockPos, MethodType.methodType(void.class, double.class, double.class, double.class));
            getBlockType = lookup.findVirtual(world, "getType", MethodType.methodType(blockData, blockPos));
            getBlock = lookup.findVirtual(blockData, "getBlock", MethodType.methodType(block));
            playBlockAction = lookup.findVirtual(world, "playBlockAction", MethodType.methodType(void.class, blockPos, ReflectionUtils.getNMSClass("Block"), int.class, int.class));
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }

        EXP_PACKET = expPacket;
        ENTITY_PACKET = entityPacket;
        WORLD_HANDLE = worldHandle;
        LIGHTNING_ENTITY = lightning;
        VEC3D = vec3D;

        ANIMATION_PACKET = animationPacket;
        ANIMATION_TYPE = animationType;
        ANIMATION_ENTITY_ID = animationEntityId;

        BLOCK_POSITION = blockPosition;
        PLAY_BLOCK_ACTION = playBlockAction;
        GET_BLOCK_TYPE = getBlockType;
        GET_BLOCK = getBlock;

        PLAY_OUT_MULTI_BLOCK_CHANGE_PACKET = playOutMultiBlockChange;
        MULTI_BLOCK_CHANGE_INFO = multiBlockChangeInfo;
        CHUNK_WRAPPER = chunkWrapper;
        CHUNK_WRAPPER_SET = chunkWrapperSet;
        SHORTS_OR_INFO = shortsOrInfo;
        SET_BLOCK_DATA = setBlockData;
    }

    private NMSExtras() {
    }

    public static void setExp(Player player, float bar, int lvl, int exp) {
        try {
            Object packet = EXP_PACKET.invoke(bar, lvl, exp);
            ReflectionUtils.sendPacket(player, packet);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static void lightning(Player player, Location location, boolean sound) {
        lightning(Collections.singletonList(player), location, sound);
    }

    /**
     * https://minecraft.gamepedia.com/Damage#Lightning_damage
     * Lightnings deal 5 damage.
     *
     * @param players  the players to send the packet to.
     * @param location the location to spawn the lightning.
     * @param sound    if the lightning should have a sound or be silent.
     */
    public static void lightning(Collection<Player> players, Location location, boolean sound) {
        try {
            Object world = WORLD_HANDLE.invoke(location.getWorld());

            if (!XMaterial.supports(16)) {
                // I don't know what the isEffect and isSilent params are used for.
                // It doesn't seem to visually change the lightning.
                Object lightningBolt = LIGHTNING_ENTITY.invoke(world, location.getX(), location.getY(), location.getZ(), false, false);
                Object packet = ENTITY_PACKET.invoke(lightningBolt);

                for (Player player : players) {
                    if (sound) XSound.ENTITY_LIGHTNING_BOLT_THUNDER.play(player);
                    ReflectionUtils.sendPacket(player, packet);
                }
            } else {
                Class<?> nmsEntityType = ReflectionUtils.getNMSClass("EntityTypes");

                Object lightningType = nmsEntityType.getClass().getField("LIGHTNING_BOLT").get(nmsEntityType);
                Object lightningBolt = LIGHTNING_ENTITY.invoke(lightningType, world);
                Object lightningBoltID = lightningBolt.getClass().getMethod("getId").invoke(lightningBolt);
                Object lightningBoltUUID = lightningBolt.getClass().getMethod("getUniqueID").invoke(lightningBolt);
                Object vec3D = VEC3D.invoke(0D, 0D, 0D);
                Object packet = ENTITY_PACKET.invoke(lightningBoltID, lightningBoltUUID, location.getX(), location.getY(), location.getZ(), 0F, 0F, lightningType, 0, vec3D);

                for (Player player : players) {
                    if (sound) XSound.ENTITY_LIGHTNING_BOLT_THUNDER.play(player);
                    ReflectionUtils.sendPacket(player, packet);
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void animation(Collection<? extends Player> players, LivingEntity entity, Animation animation) {
        try {
            // https://wiki.vg/Protocol#Entity_Animation_.28clientbound.29
            Object packet = ANIMATION_PACKET.invoke();
            ANIMATION_TYPE.invoke(packet, animation.ordinal());
            ANIMATION_ENTITY_ID.invoke(packet, entity.getEntityId());

            for (Player player : players) ReflectionUtils.sendPacket(player, packet);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void chest(Block chest, boolean open) {
        Location location = chest.getLocation();
        try {
            Object world = WORLD_HANDLE.invoke(location.getWorld());
            Object position = BLOCK_POSITION.invoke(location.getX(), location.getY(), location.getZ());
            Object block = GET_BLOCK.invoke(GET_BLOCK_TYPE.invoke(world, position));
            PLAY_BLOCK_ACTION.invoke(world, position, block, 1, open ? 1 : 0);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Not completed yet. I have no idea.
     */
    @Deprecated
    protected static void sendBlockChange(Player player, Chunk chunk, Map<WorldlessBlockWrapper, Object> blocks) {
        try {
            Object packet = PLAY_OUT_MULTI_BLOCK_CHANGE_PACKET.invoke();

            if (XMaterial.supports(16)) {
                Object wrapper = CHUNK_WRAPPER.invoke(chunk.getX(), chunk.getZ());
                CHUNK_WRAPPER_SET.invoke(wrapper);

                Object dataArray = Array.newInstance(BLOCK_DATA, blocks.size());
                Object shortArray = Array.newInstance(short.class, blocks.size());

                int i = 0;
                for (Map.Entry<WorldlessBlockWrapper, Object> entry : blocks.entrySet()) {
                    Block loc = entry.getKey().block;
                    int x = loc.getX() & 15;
                    int y = loc.getY() & 15;
                    int z = loc.getZ() & 15;
                    i++;
                }

                SHORTS_OR_INFO.invoke(packet, shortArray);
                SET_BLOCK_DATA.invoke(packet, dataArray);
            } else {
                Object wrapper = CHUNK_WRAPPER.invoke(chunk.getX(), chunk.getZ());
                CHUNK_WRAPPER_SET.invoke(wrapper);

                Object array = Array.newInstance(MULTI_BLOCK_CHANGE_INFO_CLASS, blocks.size());
                int i = 0;
                for (Map.Entry<WorldlessBlockWrapper, Object> entry : blocks.entrySet()) {
                    Block loc = entry.getKey().block;
                    int x = loc.getX() & 15;
                    int z = loc.getZ() & 15;
                    i++;
                }

                SHORTS_OR_INFO.invoke(packet, array);
            }

            ReflectionUtils.sendPacketSync(player, packet);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Order of this enum should not be changed.
     */
    public enum Animation {
        SWING_MAIN_ARM, HURT, LEAVE_BED, SWING_OFF_HAND, CRITICAL_EFFECT, MAGIC_CRITICAL_EFFECT;
    }

    public static class WorldlessBlockWrapper {
        public final Block block;

        public WorldlessBlockWrapper(Block block) {this.block = block;}

        @Override
        public int hashCode() {
            return (block.getY() + block.getZ() * 31) * 31 + block.getX();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Block)) return false;

            Block other = (Block) obj;
            return block.getX() == other.getX()
                    && block.getY() == other.getY()
                    && block.getZ() == other.getZ();
        }
    }
}
