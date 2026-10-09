package com.cryptomorin.xseries.reflection.minecraft.capabilities;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftClassHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftConnection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftMapping;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.*;

import static com.cryptomorin.xseries.reflection.XReflection.*;
import static com.cryptomorin.xseries.reflection.minecraft.capabilities.MinecraftCapabilities.World;

public final class MinecraftEntityCapability extends MinecraftCapability {
    private final MethodHandle GET_ENTITY_HANDLE, ENTITY_HANDLE;
    public final MethodHandle ENTITY_PACKET;
    public final MethodHandle GET_DATA_WATCHER, DATA_WATCHER_GET_ITEM, DATA_WATCHER_SET_ITEM;
    public final MethodHandle ANIMATION_PACKET, ANIMATION_TYPE, ANIMATION_ENTITY_ID;
    public final MethodHandle GET_BUKKIT_ENTITY;
    public final MethodHandle EXP_PACKET;

    public static final Class<?> EntityLiving = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.entity")
            .map(MinecraftMapping.MOJANG, "LivingEntity")
            .map(MinecraftMapping.SPIGOT, "EntityLiving")
            .unreflect();

    public static final Class<?> CraftEntityClass = ofMinecraft().inPackage(MinecraftPackage.CB, "entity")
            .named("CraftEntity").unreflect();
    public static final Class<?> nmsEntityType = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.entity")
            .map(MinecraftMapping.MOJANG, "EntityType")
            .map(MinecraftMapping.SPIGOT, "EntityTypes").unreflect();
    public static final Class<?> nmsEntity = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.entity")
            .named("Entity").unreflect();
    public static final Class<?> craftEntity = ofMinecraft().inPackage(MinecraftPackage.CB, "entity")
            .named("CraftEntity").unreflect();

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
    public final MethodHandle LIGHTNING_ENTITY;
    public final MethodHandle VEC3D;

    public static final Class<?> nmsVec3D = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.phys")
            .map(MinecraftMapping.MOJANG, "Vec3")
            .map(MinecraftMapping.SPIGOT, "Vec3D").unreflect();

    MinecraftEntityCapability(MethodHandles.Lookup lookup) throws Throwable {
        // public <T> T get(DataWatcherObject<T> datawatcherobject) {
        //     return this.b(datawatcherobject).b();
        // }
        DATA_WATCHER_GET_ITEM = XReflection.of(DataWatcherClass).method()
                .returns(Object.class).parameters(DataWatcherObject)
                .map(MinecraftMapping.MOJANG, "get")
                .map(MinecraftMapping.SPIGOT, v(1, 20, 5, "a").v(1, 20, "b").v(1, 18, "a").orElse("get"))
                .unreflect();

            /*
                public <T> void b(DataWatcherObject<T> datawatcherobject, T t0) {
                    this.a(datawatcherobject, t0, false);
                }
             */
        DATA_WATCHER_SET_ITEM = XReflection.of(DataWatcherClass).method()
                .returns(void.class).parameters(DataWatcherObject, Object.class)
                .map(MinecraftMapping.MOJANG, "set")
                .map(MinecraftMapping.SPIGOT, v(1, 20, 5, "a").v(1, 18, "b").orElse("set"))
                .unreflect();

        GET_BUKKIT_ENTITY = lookup.findVirtual(nmsEntity, "getBukkitEntity", MethodType.methodType(craftEntity));
        ENTITY_HANDLE = lookup.findVirtual(craftEntity, "getHandle", MethodType.methodType(nmsEntity));

        // https://wiki.vg/Protocol#Set_Experience
        // exp - lvl - total exp
        EXP_PACKET = lookup.findConstructor(ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
                .map(MinecraftMapping.MOJANG, "ClientboundSetExperiencePacket")
                .map(MinecraftMapping.SPIGOT, "PacketPlayOutExperience")
                .unreflect(), MethodType.methodType(
                void.class, float.class, int.class, int.class));
        // Lightning
        if (!supports(1, 16)) {
            ENTITY_PACKET = ofMinecraft().inPackage(MinecraftPackage.NMS)
                    .named("PacketPlayOutSpawnEntityWeather")
                    .constructor().parameters(nmsEntity).unreflect();
            VEC3D = null;
        } else {
            VEC3D = lookup.findConstructor(nmsVec3D, MethodType.methodType(void.class,
                    double.class, double.class, double.class));

            List<Class<?>> spawnTypes = new ArrayList<>(Arrays.asList(
                    int.class, UUID.class,
                    double.class, double.class, double.class, float.class, float.class,
                    nmsEntityType, int.class, nmsVec3D)
            );
            if (XReflection.supports(1, 19)) spawnTypes.add(double.class);
            ENTITY_PACKET = lookup.findConstructor(ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
                            .map(MinecraftMapping.MOJANG, "ClientboundAddEntityPacket")
                            .map(MinecraftMapping.SPIGOT, "PacketPlayOutSpawnEntity")
                            .unreflect(),
                    MethodType.methodType(void.class, spawnTypes));
        }


        MinecraftClassHandle entityLightning = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.entity")
                .map(MinecraftMapping.MOJANG, "LightningBolt")
                .map(MinecraftMapping.SPIGOT, "EntityLightning");
        if (!supports(1, 16)) {
            LIGHTNING_ENTITY = lookup.findConstructor(entityLightning.unreflect(), MethodType.methodType(void.class,
                    // world, x, y, z, isEffect, isSilent
                    World, double.class, double.class, double.class, boolean.class, boolean.class));
        } else {
            LIGHTNING_ENTITY = lookup.findConstructor(entityLightning.unreflect(), MethodType.methodType(void.class,
                    // entitytype, world
                    nmsEntityType, World));
        }

        GET_ENTITY_HANDLE = lookup.findVirtual(CraftEntityClass, "getHandle", MethodType.methodType(nmsEntity));
        GET_DATA_WATCHER = XReflection.of(nmsEntity)
                .method().returns(DataWatcherClass)
                .map(MinecraftMapping.MOJANG, "getEntityData")
                .map(MinecraftMapping.OBFUSCATED, v(1, 21, 11, "aD").v(1, 21, 9, "aC").v(1, 21, 6, "au")
                        .v(1, 21, 5, "ar")
                        .v(1, 21, 3, "au")
                        .v(1, 21, "ar")
                        .v(1, 20, 5, "ap")
                        .v(1, 20, 4, "an")
                        .v(1, 20, 2, "al")
                        .v(1, 19, "aj")
                        .v(1, 18, "ai")
                        .orElse("getDataWatcher")
                ).unreflect();

        Class<?> animation = ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
                .map(MinecraftMapping.MOJANG, "ClientboundAnimatePacket")
                .map(MinecraftMapping.SPIGOT, "PacketPlayOutAnimation")
                .unreflect();
        ANIMATION_PACKET = lookup.findConstructor(animation,
                supports(1, 17) ? MethodType.methodType(void.class, nmsEntity, int.class) : MethodType.methodType(void.class));

        if (!supports(1, 17)) {
            Field field = animation.getDeclaredField("a");
            field.setAccessible(true);
            ANIMATION_ENTITY_ID = lookup.unreflectSetter(field);
            field = animation.getDeclaredField("b");
            field.setAccessible(true);
            ANIMATION_TYPE = lookup.unreflectSetter(field);
        } else {
            ANIMATION_TYPE = null;
            ANIMATION_ENTITY_ID = null;
        }
    }

    public Object getData(Object dataWatcher, Object dataWatcherObject) {
        try {
            return DATA_WATCHER_GET_ITEM.invoke(dataWatcher, dataWatcherObject);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Failed to create data watcher", e);
        }
    }

    @Nullable
    public Object getEntityHandle(Entity entity) {
        Objects.requireNonNull(entity, "Cannot get handle of null entity");
        try {
            return GET_ENTITY_HANDLE.invoke(entity);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    public Object getDataWatcher(Object handle) {
        try {
            return GET_DATA_WATCHER.invoke(handle);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Failed to get data watcher", e);
        }
    }

    public Object setData(Object dataWatcher, Object dataWatcherObject, Object value) {
        try {
            return DATA_WATCHER_SET_ITEM.invoke(dataWatcher, dataWatcherObject, value);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Failed to set data watcher item", e);
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
                enumValue = EntityPose.getDeclaredField(v(1, 17, fieldName).orElse(name())).get(null);
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
        DATA_LIVING_ENTITY_FLAGS(MinecraftCapabilities.getStaticFieldIgnored(EntityLiving, "t"));

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

    public void spinEntity(LivingEntity entity, boolean enabled) {
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

    public void setLivingEntityFlag(Entity entity, int index, boolean flag) {
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

    public boolean hasLivingEntityFlag(Entity entity, int index) {
        Object handle = getEntityHandle(entity);
        Object dataWatcher = getDataWatcher(handle);
        byte flags = (byte) getData(dataWatcher, DataWatcherItemType.DATA_LIVING_ENTITY_FLAGS.getId());
        return (flags & index) != 0;
    }

    public boolean isAutoSpinAttack(LivingEntity entity) {
        return hasLivingEntityFlag(entity, LivingEntityFlags.SPIN_ATTACK.getBit());
    }

    public void setExp(Player player, float bar, int lvl, int exp) {
        try {
            Object packet = EXP_PACKET.invoke(bar, lvl, exp);
            MinecraftConnection.sendPacket(player, packet);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public void lightning(Player player, Location location, boolean sound) {
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
    public void lightning(Collection<Player> players, Location location, boolean sound) {
        try {
            Object world = MinecraftCapabilities.WORLD_HANDLE.invoke(location.getWorld());

            if (!supports(1, 16)) {
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

                Object lightningType = nmsEntityType.getField(supports(1, 17) ? "U" : "LIGHTNING_BOLT").get(nmsEntityType);
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

    /**
     * Order of this enum should not be changed.
     */
    public enum Animation {
        SWING_MAIN_ARM, HURT, LEAVE_BED, SWING_OFF_HAND, CRITICAL_EFFECT, MAGIC_CRITICAL_EFFECT;
    }

    /**
     * For the trident riptide animation use {@link #spinEntity(LivingEntity, boolean)} instead.
     */
    public void animation(Collection<? extends Player> players, LivingEntity entity, Animation animation) {
        try {
            // https://wiki.vg/Protocol#Entity_Animation_.28clientbound.29
            Object packet;
            if (supports(1, 17)) packet = ANIMATION_PACKET.invoke(ENTITY_HANDLE.invoke(entity), animation.ordinal());
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

}
