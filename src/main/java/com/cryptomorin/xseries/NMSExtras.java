package com.cryptomorin.xseries;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;

public class NMSExtras {
    private static final MethodHandle EXP_PACKET;
    private static final MethodHandle WEATHER_PACKET;
    private static final MethodHandle WORLD_HANDLE;
    private static final MethodHandle LIGHTNING_ENTITY;

    private static final MethodHandle ANIMATION_PACKET;
    private static final MethodHandle ANIMATION_TYPE;
    private static final MethodHandle ANIMATION_ENTITY_ID;

    private static final MethodHandle BLOCK_POSITION;
    private static final MethodHandle PLAY_BLOCK_ACTION;
    private static final MethodHandle GET_BLOCK_TYPE;
    private static final MethodHandle GET_BLOCK;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle expPacket = null;
        MethodHandle weatherPacket = null;
        MethodHandle worldHandle = null;
        MethodHandle lightning = null;

        MethodHandle animationPacket = null;
        MethodHandle animationType = null;
        MethodHandle animationEntityId = null;

        MethodHandle blockPosition = null;
        MethodHandle playBlockAction = null;
        MethodHandle getBlockType = null;
        MethodHandle getBlock = null;

        try {
            Class<?> nmsEntity = ReflectionUtils.getNMSClass("Entity");
            Class<?> world = ReflectionUtils.getNMSClass("World");

            // https://wiki.vg/Protocol#Set_Experience
            // exp - lvl - total exp
            expPacket = lookup.findConstructor(ReflectionUtils.getNMSClass("PacketPlayOutExperience"), MethodType.methodType(void.class, float.class,
                    int.class, int.class));
            // Lightning
            // TODO Not sure what the method changed to in 16...
            if (!XMaterial.supports(16)) weatherPacket = lookup.findConstructor(ReflectionUtils.getNMSClass("PacketPlayOutSpawnEntityWeather"), MethodType.methodType(void.class,
                    nmsEntity));
            worldHandle = lookup.findVirtual(ReflectionUtils.getCraftClass("CraftWorld"), "getHandle", MethodType.methodType(
                    ReflectionUtils.getNMSClass("WorldServer")));
            if (!XMaterial.supports(16)) lightning = lookup.findConstructor(ReflectionUtils.getNMSClass("EntityLightning"), MethodType.methodType(void.class,
                    // world, x, y, z, isEffect, isSilent
                    world, double.class, double.class, double.class, boolean.class, boolean.class));


            Class<?> animation = ReflectionUtils.getNMSClass("PacketPlayOutAnimation");
            animationPacket = lookup.findConstructor(animation, MethodType.methodType(void.class)); // nmsEntity, int.class
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
        WEATHER_PACKET = weatherPacket;
        WORLD_HANDLE = worldHandle;
        LIGHTNING_ENTITY = lightning;

        ANIMATION_PACKET = animationPacket;
        ANIMATION_TYPE = animationType;
        ANIMATION_ENTITY_ID = animationEntityId;

        BLOCK_POSITION = blockPosition;
        PLAY_BLOCK_ACTION = playBlockAction;
        GET_BLOCK_TYPE = getBlockType;
        GET_BLOCK = getBlock;
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
     */
    public static void lightning(Collection<Player> players, Location location, boolean sound) {
        try {
            Object world = WORLD_HANDLE.invoke(location.getWorld());
            // I don't know what the isEffect and isSilent params are used for.
            // It doesn't seem to visually change the lightning.
            Object lightningBolt = LIGHTNING_ENTITY.invoke(world, location.getX(), location.getY(), location.getZ(), false, false);
            Object packet = WEATHER_PACKET.invoke(lightningBolt);

            for (Player player : players) {
                if (sound) XSound.ENTITY_LIGHTNING_BOLT_THUNDER.play(player);
                ReflectionUtils.sendPacket(player, packet);
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
     * Order of this enum should not be changed.
     */
    public enum Animation {
        SWING_MAIN_ARM, HURT, LEAVE_BED, SWING_OFF_HAND, CRITICAL_EFFECT, MAGIC_CRITICAL_EFFECT;
    }
}
