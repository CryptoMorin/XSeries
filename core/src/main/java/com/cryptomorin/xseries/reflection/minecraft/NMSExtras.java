/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Crypto Morin
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
package com.cryptomorin.xseries.reflection.minecraft;

import com.cryptomorin.xseries.reflection.XReflection;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

import static com.cryptomorin.xseries.reflection.XReflection.*;

/**
 * Note: This class is currently really unorganized and unstable. It needs a proper recode.
 * <p>
 * A class that provides various different essential features that the API
 * didn't/doesn't support.
 * <p>
 * All the parameters are non-null.
 *
 * @author Crypto Morin
 * @version 5.5.0
 */
@SuppressWarnings("unused")
public final class NMSExtras {
    public static final Class<?> EntityLiving = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.entity")
            .map(MinecraftMapping.MOJANG, "LivingEntity")
            .map(MinecraftMapping.SPIGOT, "EntityLiving")
            .unreflect();
    private static final MethodHandle GET_ENTITY_HANDLE;
    public static final MethodHandle EXP_PACKET;
    public static final MethodHandle ENTITY_PACKET;
    public static final MethodHandle WORLD_HANDLE, ENTITY_HANDLE;
    public static final MethodHandle LIGHTNING_ENTITY;
    public static final MethodHandle VEC3D;
    public static final MethodHandle GET_DATA_WATCHER, DATA_WATCHER_GET_ITEM, DATA_WATCHER_SET_ITEM;
    public static final MethodHandle PACKET_PLAY_OUT_OPEN_SIGN_EDITOR, PACKET_PLAY_OUT_BLOCK_CHANGE;

    public static final MethodHandle ANIMATION_PACKET, ANIMATION_TYPE, ANIMATION_ENTITY_ID;

    public static final MethodHandle PLAY_OUT_MULTI_BLOCK_CHANGE_PACKET, MULTI_BLOCK_CHANGE_INFO, CHUNK_WRAPPER_SET, CHUNK_WRAPPER, SHORTS_OR_INFO, SET_BlockState;

    public static final MethodHandle BLOCK_POSITION;
    public static final MethodHandle PLAY_BLOCK_ACTION;
    public static final MethodHandle GET_BUKKIT_ENTITY;
    public static final MethodHandle GET_BLOCK_TYPE;
    public static final MethodHandle GET_BLOCK, GET_IBlockState, SANITIZE_LINES, TILE_ENTITY_SIGN,
            TILE_ENTITY_SIGN__GET_UPDATE_PACKET, TILE_ENTITY_SIGN__SET_LINE, SIGN_TEXT;

    public static final Class<?> BlockState = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.level.block.state")
            .map(MinecraftMapping.MOJANG, "BlockState")
            .map(MinecraftMapping.SPIGOT, "IBlockData")
            .unreflect();
    public static final Class<?> MULTI_BLOCK_CHANGE_INFO_CLASS = null; // getNMSClass("PacketPlayOutMultiBlockChange$MultiBlockChangeInfo")

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        MethodHandle expPacket = null;
        MethodHandle entityPacket = null;
        MethodHandle worldHandle = null, entityHandle = null;
        MethodHandle lightning = null;
        MethodHandle vec3D = null;
        MethodHandle signEditorPacket = null, packetPlayOutBlockChange = null;

        MethodHandle animationPacket = null;
        MethodHandle animationType = null;
        MethodHandle animationEntityId = null;

        MethodHandle getBukkitEntity = null;
        MethodHandle blockPosition = null;
        MethodHandle playBlockAction = null;
        MethodHandle getBlockType = null;
        MethodHandle getBlock = null;
        MethodHandle getIBlockData = null;
        MethodHandle sanitizeLines = null;
        MethodHandle tileEntitySign = null, tileEntitySign_getUpdatePacket = null, tileEntitySign_setLine = null, signText = null;

        MethodHandle playOutMultiBlockChange = null, multiBlockChangeInfo = null, chunkWrapper = null, chunkWrapperSet = null,
                shortsOrInfo = null, setBlockData = null, getDataWatcher = null, dataWatcherGetItem = null,
                dataWatcherSetItem = null, getHandle = null;

        try {
            Class<?> CraftEntityClass = ofMinecraft().inPackage(MinecraftPackage.CB, "entity")
                    .named("CraftEntity").unreflect();
            Class<?> nmsEntityType = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.entity")
                    .map(MinecraftMapping.MOJANG, "EntityType")
                    .map(MinecraftMapping.SPIGOT, "EntityTypes").unreflect();
            Class<?> nmsEntity = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.entity")
                    .named("Entity").unreflect();
            Class<?> craftEntity = ofMinecraft().inPackage(MinecraftPackage.CB, "entity")
                    .named("CraftEntity").unreflect();
            Class<?> nmsVec3D = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.phys")
                    .map(MinecraftMapping.MOJANG, "Vec3")
                    .map(MinecraftMapping.SPIGOT, "Vec3D").unreflect();
            Class<?> world = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.level")
                    .map(MinecraftMapping.MOJANG, "Level")
                    .map(MinecraftMapping.SPIGOT, "World").unreflect();
            Class<?> signOpenPacket = ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
                    .map(MinecraftMapping.MOJANG, "ClientboundOpenSignEditorPacket")
                    .map(MinecraftMapping.SPIGOT, "PacketPlayOutOpenSignEditor").unreflect();
            Class<?> packetPlayOutBlockChangeClass = ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
                    .map(MinecraftMapping.MOJANG, "ClientboundBlockUpdatePacket")
                    .map(MinecraftMapping.SPIGOT, "PacketPlayOutBlockChange").unreflect();
            Class<?> CraftMagicNumbers = ofMinecraft().inPackage(MinecraftPackage.CB, "util")
                    .named("CraftMagicNumbers").unreflect();
            Class<?> CraftSign = ofMinecraft().inPackage(MinecraftPackage.CB, "block")
                    .named("CraftSign").unreflect();
            Class<?> IChatBaseComponent = ofMinecraft().inPackage(MinecraftPackage.NMS, "network.chat")
                    .map(MinecraftMapping.MOJANG, "Component")
                    .map(MinecraftMapping.SPIGOT, "IChatBaseComponent").unreflect();
            Class<?> TileEntitySign = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.level.block.entity")
                    .map(MinecraftMapping.MOJANG, "SignBlockEntity")
                    .map(MinecraftMapping.SPIGOT, "TileEntitySign").unreflect();
            Class<?> PacketPlayOutTileEntityData = ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
                    .map(MinecraftMapping.MOJANG, "ClientboundBlockEntityDataPacket")
                    .map(MinecraftMapping.SPIGOT, "PacketPlayOutTileEntityData").unreflect();
            Class<?> DataWatcherClass = ofMinecraft().inPackage(MinecraftPackage.NMS, "network.syncher")
                    .map(MinecraftMapping.MOJANG, "SynchedEntityData")
                    .map(MinecraftMapping.SPIGOT, "DataWatcher").unreflect();
            Class<?> DataWatcherItemClass =
                    ofMinecraft().inPackage(MinecraftPackage.NMS, "network.syncher")
                            .map(MinecraftMapping.MOJANG, "SynchedEntityData$DataItem")
                            .map(MinecraftMapping.SPIGOT, "DataWatcher$Item").unreflect();

            Class<?> DataWatcherObject = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "network.syncher")
                    .map(MinecraftMapping.MOJANG, "EntityDataAccessor")
                    .map(MinecraftMapping.SPIGOT, "DataWatcherObject")
                    .unreflect();

            getHandle = lookup.findVirtual(CraftEntityClass, "getHandle", MethodType.methodType(nmsEntity));
            getDataWatcher = XReflection.of(nmsEntity)
                    .method().returns(DataWatcherClass)
                    .map(MinecraftMapping.MOJANG, "getEntityData")
                    .map(MinecraftMapping.OBFUSCATED, v(21, 5, "ar")
                            .v(21, 3, "au")
                            .v(21, "ar")
                            .v(20, 5, "ap")
                            .v(20, 4, "an")
                            .v(20, 2, "al")
                            .v(19, "aj")
                            .v(18, "ai")
                            .orElse("getDataWatcher")
                    ).unreflect();


            // public <T> T get(DataWatcherObject<T> datawatcherobject) {
            //     return this.b(datawatcherobject).b();
            // }
            dataWatcherGetItem = XReflection.of(DataWatcherClass).method()
                    .returns(Object.class).parameters(DataWatcherObject)
                    .map(MinecraftMapping.MOJANG, "get")
                    .map(MinecraftMapping.SPIGOT, v(20, 5, "a").v(20, "b").v(18, "a").orElse("get"))
                    .unreflect();

            /*
                public <T> void b(DataWatcherObject<T> datawatcherobject, T t0) {
                    this.a(datawatcherobject, t0, false);
                }
             */
            dataWatcherSetItem = XReflection.of(DataWatcherClass).method()
                    .returns(void.class).parameters(DataWatcherObject, Object.class)
                    .map(MinecraftMapping.MOJANG, "set")
                    .map(MinecraftMapping.SPIGOT, v(20, 5, "a").v(18, "b").orElse("set"))
                    .unreflect();

            getBukkitEntity = lookup.findVirtual(nmsEntity, "getBukkitEntity", MethodType.methodType(craftEntity));
            entityHandle = lookup.findVirtual(craftEntity, "getHandle", MethodType.methodType(nmsEntity));

            // https://wiki.vg/Protocol#Set_Experience
            // exp - lvl - total exp
            expPacket = lookup.findConstructor(ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
                    .map(MinecraftMapping.MOJANG, "ClientboundSetExperiencePacket")
                    .map(MinecraftMapping.SPIGOT, "PacketPlayOutExperience")
                    .unreflect(), MethodType.methodType(
                    void.class, float.class, int.class, int.class));
            // Lightning
            if (!supports(16)) {
                entityPacket = ofMinecraft().inPackage(MinecraftPackage.NMS)
                        .named("PacketPlayOutSpawnEntityWeather")
                        .constructor().parameters(nmsEntity).unreflect();
            } else {
                vec3D = lookup.findConstructor(nmsVec3D, MethodType.methodType(void.class,
                        double.class, double.class, double.class));

                List<Class<?>> spawnTypes = new ArrayList<>(Arrays.asList(
                        int.class, UUID.class,
                        double.class, double.class, double.class, float.class, float.class,
                        nmsEntityType, int.class, nmsVec3D)
                );
                if (XReflection.supports(19)) spawnTypes.add(double.class);
                entityPacket = lookup.findConstructor(ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
                                .map(MinecraftMapping.MOJANG, "ClientboundAddEntityPacket")
                                .map(MinecraftMapping.SPIGOT, "PacketPlayOutSpawnEntity")
                                .unreflect(),
                        MethodType.methodType(void.class, spawnTypes));
            }

            worldHandle = lookup.findVirtual(ofMinecraft().inPackage(MinecraftPackage.CB).named("CraftWorld").unreflect(), "getHandle", MethodType.methodType(
                    ofMinecraft().inPackage(MinecraftPackage.NMS, "server.level")
                            .map(MinecraftMapping.MOJANG, "ServerLevel")
                            .map(MinecraftMapping.SPIGOT, "WorldServer").unreflect()));

            MinecraftClassHandle entityLightning = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.entity")
                    .map(MinecraftMapping.MOJANG, "LightningBolt")
                    .map(MinecraftMapping.SPIGOT, "EntityLightning");
            if (!supports(16)) {
                lightning = lookup.findConstructor(entityLightning.unreflect(), MethodType.methodType(void.class,
                        // world, x, y, z, isEffect, isSilent
                        world, double.class, double.class, double.class, boolean.class, boolean.class));
            } else {
                lightning = lookup.findConstructor(entityLightning.unreflect(), MethodType.methodType(void.class,
                        // entitytype, world
                        nmsEntityType, world));
            }

            // Multi Block Change
            Class<?> playOutMultiBlockChangeClass = ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
                    .map(MinecraftMapping.MOJANG, "ClientboundSectionBlocksUpdatePacket")
                    .map(MinecraftMapping.SPIGOT, "PacketPlayOutMultiBlockChange")
                    .unreflect();
            Class<?> chunkCoordIntPairClass = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.level")
                    .map(MinecraftMapping.MOJANG, "ChunkPos")
                    .map(MinecraftMapping.SPIGOT, "ChunkCoordIntPair")
                    .unreflect();
            try {
//                playOutMultiBlockChange = lookup.findConstructor(playOutMultiBlockChangeClass, MethodType.methodType(void.class));
//                multiBlockChangeInfo = lookup.findConstructor(MULTI_BLOCK_CHANGE_INFO_CLASS, MethodType.methodType(void.class, short.class, BlockState));

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

                // noinspection StatementWithEmptyBody
                if (supports(16)) {
//                    Class<?> sectionPosClass = getNMSClass("SectionPosition");
//                    chunkWrapper = lookup.findConstructor(sectionPosClass, MethodType.methodType(int.class, int.class, int.class));
                } else
                    chunkWrapper = lookup.findConstructor(chunkCoordIntPairClass, MethodType.methodType(void.class, int.class, int.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }

            Class<?> animation = ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
                    .map(MinecraftMapping.MOJANG, "ClientboundAnimatePacket")
                    .map(MinecraftMapping.SPIGOT, "PacketPlayOutAnimation")
                    .unreflect();
            animationPacket = lookup.findConstructor(animation,
                    supports(17) ? MethodType.methodType(void.class, nmsEntity, int.class) : MethodType.methodType(void.class));

            if (!supports(17)) {
                Field field = animation.getDeclaredField("a");
                field.setAccessible(true);
                animationEntityId = lookup.unreflectSetter(field);
                field = animation.getDeclaredField("b");
                field.setAccessible(true);
                animationType = lookup.unreflectSetter(field);
            }

            Class<?> blockPos = ofMinecraft().inPackage(MinecraftPackage.NMS, "core")
                    .map(MinecraftMapping.MOJANG, "BlockPos")
                    .map(MinecraftMapping.SPIGOT, "BlockPosition")
                    .unreflect();
            Class<?> block = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.level.block")
                    .named("Block")
                    .unreflect();

            blockPosition = lookup.findConstructor(blockPos,
                    v(19, MethodType.methodType(void.class, int.class, int.class, int.class)).orElse(
                            MethodType.methodType(void.class, double.class, double.class, double.class)));

            // public IBlockData getBlockState(BlockPosition blockposition)
            getBlockType = XReflection.of(world).method().returns(BlockState).parameters(blockPos)
                    .map(MinecraftMapping.MOJANG, "getBlockState")
                    .map(MinecraftMapping.SPIGOT, v(18, "a_").orElse("getType"))
                    .unreflect();
            if (supports(21)) {
                getBlock = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "world.level.block.state")
                        .map(MinecraftMapping.MOJANG, "BlockBehaviour")
                        .map(MinecraftMapping.SPIGOT, "BlockBase")
                        .inner(XReflection.ofMinecraft()
                                .map(MinecraftMapping.MOJANG, "BlockStateBase")
                                .map(MinecraftMapping.SPIGOT, "BlockData"))
                        .method().returns(block)
                        .map(MinecraftMapping.MOJANG, "getBlock")
                        .map(MinecraftMapping.SPIGOT, "b")
                        .unreflect();
            } else {
                getBlock = XReflection.of(BlockState).method().returns(block)
                        .map(MinecraftMapping.MOJANG, "getBlock")
                        .map(MinecraftMapping.SPIGOT, v(18, "b").orElse("getBlock"))
                        .unreflect();
            }
            playBlockAction = XReflection.of(world).method().returns(void.class).parameters(blockPos, block, int.class, int.class)
                    .map(MinecraftMapping.MOJANG, "blockEvent")
                    .map(MinecraftMapping.SPIGOT, v(18, "a").orElse("playBlockAction"))
                    .unreflect();

            signEditorPacket = lookup.findConstructor(signOpenPacket,
                    v(20, MethodType.methodType(void.class, blockPos, boolean.class))
                            .orElse(MethodType.methodType(void.class, blockPos)));
            if (supports(17)) {
                packetPlayOutBlockChange = lookup.findConstructor(packetPlayOutBlockChangeClass, MethodType.methodType(void.class, blockPos, BlockState));
                getIBlockData = lookup.findStatic(CraftMagicNumbers, "getBlock", MethodType.methodType(BlockState, Material.class, byte.class));
                sanitizeLines = lookup.findStatic(CraftSign, v(17, "sanitizeLines").orElse("SANITIZE_LINES"),
                        MethodType.methodType(toArrayClass(IChatBaseComponent), String[].class));

                tileEntitySign = lookup.findConstructor(TileEntitySign, MethodType.methodType(void.class, blockPos, BlockState));
                tileEntitySign_getUpdatePacket = XReflection.of(TileEntitySign).method().returns(PacketPlayOutTileEntityData)
                        .map(MinecraftMapping.MOJANG, "getUpdatePacket")
                        .map(MinecraftMapping.SPIGOT, v(21, 4, "s").v(21, 3, "t").v(20, 5, "l").v(20, 4, "m").v(20, "j").v(19, "f").v(18, "c").orElse("getUpdatePacket"))
                        .unreflect();

                if (supports(20)) {
                    Class<?> SignText = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.level.block.entity")
                            .named("SignText").unreflect();
                    // public boolean a(SignText signtext, boolean flag) {
                    //        return flag ? this.c(signtext) : this.b(signtext);
                    // }
                    if (!supports(20, 6)) { // It completely changed, needs a lot of work
                        tileEntitySign_setLine = lookup.findVirtual(TileEntitySign, "a",
                                MethodType.methodType(boolean.class, SignText, boolean.class));
                    }

                    Class<?> IChatBaseComponentArray = XReflection.of(IChatBaseComponent).asArray().unreflect();

                    // public SignText(net.minecraft.network.chat.IChatBaseComponent[] var0, IChatBaseComponent[] var1,
                    // EnumColor var2, boolean var3) {
                    Class<?> EnumColor = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.item")
                            .map(MinecraftMapping.MOJANG, "DyeColor")
                            .map(MinecraftMapping.SPIGOT, "EnumColor").unreflect();
                    signText = lookup.findConstructor(SignText, MethodType.methodType(void.class,
                            IChatBaseComponentArray, IChatBaseComponentArray, EnumColor, boolean.class));
                } else {
                    tileEntitySign_setLine = lookup.findVirtual(TileEntitySign, "a", MethodType.methodType(void.class, int.class, IChatBaseComponent, IChatBaseComponent));
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }

        GET_ENTITY_HANDLE = getHandle;
        GET_DATA_WATCHER = getDataWatcher;
        DATA_WATCHER_GET_ITEM = dataWatcherGetItem;
        DATA_WATCHER_SET_ITEM = dataWatcherSetItem;
        EXP_PACKET = expPacket;
        ENTITY_PACKET = entityPacket;
        WORLD_HANDLE = worldHandle;
        ENTITY_HANDLE = entityHandle;
        LIGHTNING_ENTITY = lightning;
        VEC3D = vec3D;
        PACKET_PLAY_OUT_OPEN_SIGN_EDITOR = signEditorPacket;
        PACKET_PLAY_OUT_BLOCK_CHANGE = packetPlayOutBlockChange;

        ANIMATION_PACKET = animationPacket;
        ANIMATION_TYPE = animationType;
        ANIMATION_ENTITY_ID = animationEntityId;

        BLOCK_POSITION = blockPosition;
        PLAY_BLOCK_ACTION = playBlockAction;
        GET_BLOCK_TYPE = getBlockType;
        GET_BLOCK = getBlock;
        GET_IBlockState = getIBlockData;
        SANITIZE_LINES = sanitizeLines;
        TILE_ENTITY_SIGN = tileEntitySign;
        TILE_ENTITY_SIGN__GET_UPDATE_PACKET = tileEntitySign_getUpdatePacket;
        TILE_ENTITY_SIGN__SET_LINE = tileEntitySign_setLine;

        GET_BUKKIT_ENTITY = getBukkitEntity;
        PLAY_OUT_MULTI_BLOCK_CHANGE_PACKET = playOutMultiBlockChange;
        MULTI_BLOCK_CHANGE_INFO = multiBlockChangeInfo;
        CHUNK_WRAPPER = chunkWrapper;
        CHUNK_WRAPPER_SET = chunkWrapperSet;
        SHORTS_OR_INFO = shortsOrInfo;
        SET_BlockState = setBlockData;
        SIGN_TEXT = signText;
    }

    private NMSExtras() {
    }

    public static void setExp(Player player, float bar, int lvl, int exp) {
        try {
            Object packet = EXP_PACKET.invoke(bar, lvl, exp);
            MinecraftConnection.sendPacket(player, packet);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static void lightning(Player player, Location location, boolean sound) {
        lightning(Collections.singletonList(player), location, sound);
    }

    /**
     * https://minecraft.wiki/w/Damage#Lightning_damage
     * Lightnings deal 5 damage.
     *
     * @param players  the players to send the packet to.
     * @param location the location to spawn the lightning.
     * @param sound    if the lightning should have a sound or be silent.
     */
    public static void lightning(Collection<Player> players, Location location, boolean sound) {
        try {
            Object world = WORLD_HANDLE.invoke(location.getWorld());

            if (!supports(16)) {
                // I don't know what the isEffect and isSilent params are used for.
                // It doesn't seem to visually change the lightning.
                Object lightningBolt = LIGHTNING_ENTITY.invoke(world, location.getX(), location.getY(), location.getZ(), false, false);
                Object packet = ENTITY_PACKET.invoke(lightningBolt);

                for (Player player : players) {
                    // if (sound) XSound.ENTITY_LIGHTNING_BOLT_THUNDER.record().soundPlayer().forPlayers(player).play();
                    MinecraftConnection.sendPacket(player, packet);
                }
            } else {
                Class<?> nmsEntityType = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.entity")
                        .map(MinecraftMapping.MOJANG, "EntityType")
                        .map(MinecraftMapping.SPIGOT, "EntityTypes").unreflect();

                Object lightningType = nmsEntityType.getField(supports(17) ? "U" : "LIGHTNING_BOLT").get(nmsEntityType);
                Object lightningBolt = LIGHTNING_ENTITY.invoke(lightningType, world);
                Object lightningBoltID = lightningBolt.getClass().getMethod("getId").invoke(lightningBolt);
                Object lightningBoltUUID = lightningBolt.getClass().getMethod("getUniqueID").invoke(lightningBolt);
                Object vec3D = VEC3D.invoke(0D, 0D, 0D);
                Object packet = ENTITY_PACKET.invoke(lightningBoltID, lightningBoltUUID, location.getX(), location.getY(), location.getZ(), 0F, 0F, lightningType, 0, vec3D);

                for (Player player : players) {
                    // if (sound) XSound.ENTITY_LIGHTNING_BOLT_THUNDER.record().soundPlayer().forPlayers(player).play();
                    MinecraftConnection.sendPacket(player, packet);
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static Object getData(Object dataWatcher, Object dataWatcherObject) {
        try {
            return DATA_WATCHER_GET_ITEM.invoke(dataWatcher, dataWatcherObject);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Failed to create data watcher", e);
        }
    }

    @Nullable
    public static Object getEntityHandle(Entity entity) {
        Objects.requireNonNull(entity, "Cannot get handle of null entity");
        try {
            return GET_ENTITY_HANDLE.invoke(entity);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    public static Object getDataWatcher(Object handle) {
        try {
            return GET_DATA_WATCHER.invoke(handle);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Failed to get data watcher", e);
        }
    }

    public static Object setData(Object dataWatcher, Object dataWatcherObject, Object value) {
        try {
            return DATA_WATCHER_SET_ITEM.invoke(dataWatcher, dataWatcherObject, value);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Failed to set data watcher item", e);
        }
    }

    public static Object getStaticFieldIgnored(Class<?> clazz, String name) {
        return getStaticField(clazz, name, true);
    }

    public static Object getStaticField(Class<?> clazz, String name, boolean silent) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(null);
        } catch (Throwable e) {
            if (!silent)
                throw new IllegalArgumentException("Failed to get static field of " + clazz + " named " + name, e);
            else return null;
        }
    }

    public enum EntityPose {
        STANDING("a"),
        FALL_FLYING("b"),
        SLEEPING("c"),
        SWIMMING("d"),
        SPIN_ATTACK("e"),
        CROUCHING("f"),
        LONG_JUMPING("g"),
        DYING("h"),
        CROAKING("i"),
        USING_TONGUE("j"),
        SITTING("k"),
        ROARING("l"),
        SNIFFING("m"),
        EMERGING("n"),
        DIGGING("o"),
        ;

        public final Object enumValue;
        private final boolean supported;

        EntityPose(String fieldName) {
            boolean supported = true;
            Object enumValue = null;

            try {
                Class<?> EntityPose = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.entity")
                        .map(MinecraftMapping.MOJANG, "Pose")
                        .map(MinecraftMapping.SPIGOT, "EntityPose")
                        .reflect();
                enumValue = EntityPose.getDeclaredField(v(17, fieldName).orElse(name())).get(null);
            } catch (Throwable e) {
                supported = false;
            }

            this.supported = supported;
            this.enumValue = enumValue;
        }

        public boolean isSupported() {
            return supported;
        }

        public Object getEnumValue() {
            return enumValue;
        }
    }

    public enum DataWatcherItemType {
        // protected static final DataWatcherObject<Byte> DATA_LIVING_ENTITY_FLAGS = DataWatcher.defineId(EntityLiving.class, DataWatcherRegistry.BYTE);
        DATA_LIVING_ENTITY_FLAGS(getStaticFieldIgnored(EntityLiving, "t"));

        private final Object id;

        private final boolean supported;

        DataWatcherItemType(Object DataWatcherObject) {
            boolean supported = true;
            Object id = null;

            try {
                // public int a() { return this.a; }
                // Method idMethod = DataWatcherObject.getClass().getMethod("a");
                id = DataWatcherObject;
            } catch (Throwable e) {
                supported = false;
            }

            this.supported = supported;
            this.id = id;
        }

        public boolean isSupported() {
            return supported;
        }

        public Object getId() {
            return id;
        }
    }

    public enum LivingEntityFlags {
        SPIN_ATTACK(0x04);

        private final byte bit;

        LivingEntityFlags(int bit) {
            this.bit = (byte) bit;
        }

        public byte getBit() {
            return bit;
        }
    }

    public static void spinEntity(LivingEntity entity, boolean enabled) {
        if (!EntityPose.SPIN_ATTACK.isSupported()) {
            throw new UnsupportedOperationException("Spin attacks are not supported in " + getVersionInformation());
        }

        // https://www.spigotmc.org/threads/trident-spinning-animation-riptide.426086/
        // https://www.spigotmc.org/threads/using-the-riptide-animation.469207/
        // https://wiki.vg/Entity_metadata#Living_Entity
        // Referenced as "Riptiding" or "AutoSpinAttack" modes in code.
        // EntityLiving.r(int ticks) doesn't exist in newer versions.

        // EntityLiving entityLiv = ((CraftPlayer) entity).getHandle();
        // DataWatcher dataWatcher = entityLiv.al();
        // dataWatcher.b((DataWatcherObject<Byte>) DataWatcherItemType.DATA_LIVING_ENTITY_FLAGS.getId(), (byte) 0x04);

        setLivingEntityFlag(entity, LivingEntityFlags.SPIN_ATTACK.getBit(), enabled);
    }

    public static void setLivingEntityFlag(Entity entity, int index, boolean flag) {
        Object handle = getEntityHandle(entity);
        Object dataWatcher = getDataWatcher(handle);

        Object flagItem = DataWatcherItemType.DATA_LIVING_ENTITY_FLAGS.getId();
        byte currentFlags = (byte) getData(dataWatcher, flagItem);
        int newFlags;

        if (flag) {
            newFlags = currentFlags | index;
        } else {
            newFlags = currentFlags & ~index;
        }

        setData(dataWatcher, flagItem, (byte) newFlags);
    }

    public static boolean hasLivingEntityFlag(Entity entity, int index) {
        Object handle = getEntityHandle(entity);
        Object dataWatcher = getDataWatcher(handle);
        byte flags = (byte) getData(dataWatcher, DataWatcherItemType.DATA_LIVING_ENTITY_FLAGS.getId());
        return (flags & index) != 0;
    }

    public boolean isAutoSpinAttack(LivingEntity entity) {
        return hasLivingEntityFlag(entity, LivingEntityFlags.SPIN_ATTACK.getBit());
    }

    /**
     * For the trident riptide animation use {@link #spinEntity(LivingEntity, boolean)} instead.
     */
    public static void animation(Collection<? extends Player> players, LivingEntity entity, Animation animation) {
        try {
            // https://wiki.vg/Protocol#Entity_Animation_.28clientbound.29
            Object packet;
            if (supports(17)) packet = ANIMATION_PACKET.invoke(ENTITY_HANDLE.invoke(entity), animation.ordinal());
            else {
                packet = ANIMATION_PACKET.invoke();
                ANIMATION_TYPE.invoke(packet, animation.ordinal());
                ANIMATION_ENTITY_ID.invoke(packet, entity.getEntityId());
            }

            for (Player player : players) MinecraftConnection.sendPacket(player, packet);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void chest(Block chest, boolean open) {
        Location location = chest.getLocation();
        try {
            Object world = WORLD_HANDLE.invoke(location.getWorld());
            Object position = v(19,
                    () ->
                    {
                        try {
                            return BLOCK_POSITION.invoke(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                        } catch (Throwable e) {
                            throw new IllegalArgumentException("Failed to set block position", e);
                        }
                    }).orElse(
                    () ->
                    {
                        try {
                            return BLOCK_POSITION.invoke(location.getX(), location.getY(), location.getZ());
                        } catch (Throwable e) {
                            throw new IllegalArgumentException("Failed to set block position", e);
                        }
                    });
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

            if (supports(16)) {
                Object wrapper = CHUNK_WRAPPER.invoke(chunk.getX(), chunk.getZ());
                CHUNK_WRAPPER_SET.invoke(wrapper);

                Object dataArray = Array.newInstance(BlockState, blocks.size());
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
                SET_BlockState.invoke(packet, dataArray);
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

            MinecraftConnection.sendPacket(player, packet);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Currently only supports 1.17
     */
    public static void openSign(Player player, DyeColor textColor, String[] lines, boolean frontSide) {
        try {
            Location loc = player.getLocation();
            Object position = BLOCK_POSITION.invoke(loc.getBlockX(), 1, loc.getBlockY());
            Object signBlockData = GET_IBlockState.invoke(Material.OAK_SIGN, (byte) 0);
            Object blockChangePacket = PACKET_PLAY_OUT_BLOCK_CHANGE.invoke(position, signBlockData);

            Object components = SANITIZE_LINES.invoke((Object[]) lines);
            Object tileSign = TILE_ENTITY_SIGN.invoke(position, signBlockData);
            if (supports(20)) {
                // When can we use this without blocks... player.openSign();
                Class<?> EnumColor = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.item")
                        .map(MinecraftMapping.MOJANG, "DyeColor")
                        .map(MinecraftMapping.SPIGOT, "EnumColor").unreflect();
                Object enumColor = null;
                for (Field field : EnumColor.getFields()) {
                    Object color = field.get(null);
                    String colorName = (String) EnumColor.getDeclaredMethod("b").invoke(color); // gets its name
                    if (textColor.name().equalsIgnoreCase(colorName)) {
                        enumColor = color;
                        break;
                    }
                }

                Object signText = SIGN_TEXT.invoke(components, components, enumColor, frontSide);
                TILE_ENTITY_SIGN__SET_LINE.invoke(signText, true);
            } else {
                for (int i = 0; i < lines.length; i++) {
                    Object component = java.lang.reflect.Array.get(components, i);
                    TILE_ENTITY_SIGN__SET_LINE.invoke(tileSign, i, component, component);
                }
            }
            Object signLinesUpdatePacket = TILE_ENTITY_SIGN__GET_UPDATE_PACKET.invoke(tileSign);

            Object signPacket =
                    v(20, PACKET_PLAY_OUT_OPEN_SIGN_EDITOR.invoke(position, true))
                            .orElse(PACKET_PLAY_OUT_OPEN_SIGN_EDITOR.invoke(position));

            MinecraftConnection.sendPacket(player, blockChangePacket, signLinesUpdatePacket, signPacket);
        } catch (Throwable x) {
            x.printStackTrace();
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

        public WorldlessBlockWrapper(Block block) {
            this.block = block;
        }

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
