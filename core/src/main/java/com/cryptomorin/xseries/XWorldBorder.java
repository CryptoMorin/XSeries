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
package com.cryptomorin.xseries;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftClassHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftConnection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftMapping;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;

import static com.cryptomorin.xseries.reflection.XReflection.*;

/**
 * Send different <a href="https://minecraft.fandom.com/wiki/World_border">World Border</a> data to each player.
 *
 * @version 1.0.1
 */
public abstract class XWorldBorder {
    public static final int ABSOLUTE_MAX_SIZE = 29999984;
    public static final double MAX_SIZE = 5.9999968E7D;
    public static final double MAX_CENTER_COORDINATE = 2.9999984E7D;

    private static final boolean SUPPORTS_NATIVE_WORLDBORDERS = XReflection.of(Player.class)
            .method().named("setWorldBorder").returns(void.class).parameters(WorldBorder.class)
            .exists();

    protected BorderBounds borderBounds;

    // @formatter:off
    public abstract double getDamageBuffer();
    public abstract double getSizeLerpTarget();
    public abstract double getSize();
    public abstract boolean isWithinBorder(Location location);
    public abstract int getWarningDistance();
    public abstract Duration getWarningTime();
    public abstract Location getCenter();
    public abstract void setFor(Player player, boolean forceInit);
    public final BorderBounds getBorderBounds() {return borderBounds;}

    public abstract XWorldBorder copy();
    public abstract XWorldBorder setDamageBuffer(double blocks);
    public abstract XWorldBorder setWarningTime(Duration time);
    public abstract XWorldBorder setWarningDistance(int blocks);
    public abstract XWorldBorder setSize(double newSize, @NotNull Duration duration);
    public abstract XWorldBorder setCenter(double x, double z);
    public abstract XWorldBorder setSizeLerpTarget(double sizeLerpTarget);
    // @formatter:on

    public final double getDistanceToBorder(Location location) {
        if (this.borderBounds == null) {
            return getCenter().distanceSquared(location);
        }

        double x = location.getX();
        double z = location.getZ();

        double d2 = z - borderBounds.minZ;
        double d3 = borderBounds.maxZ - z;
        double d4 = x - borderBounds.minX;
        double d5 = borderBounds.maxX - x;
        double d6 = Math.min(d4, d5);

        d6 = Math.min(d6, d2);
        return Math.min(d6, d3);
    }

    protected void updateBorderBounds() {
        Location center = getCenter();
        this.borderBounds = new BorderBounds(center.getWorld(), center.getX(), center.getZ(), getSize());
    }

    public static final class BorderBounds {
        protected final World lastCenterWorld;
        protected final double lastCenterX, lastCenterZ;
        public final double minX, minZ, maxX, maxZ;

        private static double clamp(double var0, double var2, double var4) {
            return var0 < var2 ? var2 : Math.min(var0, var4);
        }

        public boolean isCenterSame(World world, double centerX, double centerZ) {
            return this.lastCenterWorld == world && this.lastCenterX == centerX && this.lastCenterZ == centerZ;
        }

        public BorderBounds(World centerWorld, double centerX, double centerZ, double size) {
            this.lastCenterWorld = centerWorld;
            this.lastCenterX = centerX;
            this.lastCenterZ = centerZ;

            this.minX = clamp(centerX - size / 2.0D, -ABSOLUTE_MAX_SIZE, ABSOLUTE_MAX_SIZE);
            this.minZ = clamp(centerZ - size / 2.0D, -ABSOLUTE_MAX_SIZE, ABSOLUTE_MAX_SIZE);
            this.maxX = clamp(centerX + size / 2.0D, -ABSOLUTE_MAX_SIZE, ABSOLUTE_MAX_SIZE);
            this.maxZ = clamp(centerZ + size / 2.0D, -ABSOLUTE_MAX_SIZE, ABSOLUTE_MAX_SIZE);
        }
    }

    private static final class NMSWorldBorder extends XWorldBorder {
        private static final MethodHandle WORLD_HANDLE, WORLDBORDER, WORLDBORDER_WORLD, CENTER, WARNING_DISTANCE, WARNING_TIME, SIZE, TRANSITION;
        private static final MethodHandle PACKET_WARNING_DISTANCE, PACKET_WARNING_DELAY, PACKET_LERP_SIZE, PACKET_INIT, PACKET_CENTER, PACKET_SIZE;
        private static final Object INITIALIZE;
        private static final boolean SUPPORTS_SEPARATE_PACKETS;
        private static final Map<UUID, XWorldBorder> WORLD_BORDERS = new HashMap<>();

        private Object handle;
        private double damagePerBlock = 0.2D;
        private double damageSafeZone = 5.0D;
        private double size = 100;
        private double sizeLerpTarget = 0;
        private Duration warningTime = Duration.ofSeconds(15);
        private Duration sizeLerpTime = Duration.ZERO;
        private int warningBlocks = 5;
        private World world;
        private double centerX, centerZ;
        private final Set<Component> updateRequired = EnumSet.noneOf(Component.class);
        private boolean init = true;

        @Override
        public NMSWorldBorder copy() {
            NMSWorldBorder wb = new NMSWorldBorder();
            wb.world = world;
            wb.centerX = centerX;
            wb.centerZ = centerZ;
            wb.size = size;
            wb.sizeLerpTime = sizeLerpTime;
            wb.damagePerBlock = damagePerBlock;
            wb.damageSafeZone = damageSafeZone;
            wb.warningTime = warningTime;
            wb.warningBlocks = warningBlocks;
            wb.handle = wb.createHandle();
            return wb;
        }

        public XWorldBorder setDamageAmount(double damage) {
            damagePerBlock = damage;
            return this;
        }

        public double getSize() {
            return size;
        }

        public double getDamageAmount() {
            return damagePerBlock;
        }

        public XWorldBorder setDamageBuffer(double blocks) {
            damageSafeZone = blocks;
            return this;
        }

        public double getDamageBuffer() {
            return damageSafeZone;
        }

        public XWorldBorder setWarningTime(Duration time) {
            if (this.warningTime == time) return this;
            warningTime = time;
            update(Component.WARNING_DELAY);
            return this;
        }

        public Duration getWarningTime() {
            return warningTime;
        }

        public XWorldBorder setWarningDistance(int blocks) {
            if (warningBlocks == blocks) return this;
            warningBlocks = blocks;
            update(Component.WARNING_DISTANCE);
            return this;
        }

        public double getSizeLerpTarget() {
            return sizeLerpTarget;
        }

        public XWorldBorder setSizeLerpTarget(double sizeLerpTarget) {
            if (this.sizeLerpTarget == sizeLerpTarget) return this;
            this.sizeLerpTarget = sizeLerpTarget;
            update(Component.SIZE_LERP);
            return this;
        }

        public int getWarningDistance() {
            return warningBlocks;
        }

        public XWorldBorder setCenter(double x, double z) {
            if (centerX == x && centerZ == z) return this;
            centerX = x;
            centerZ = z;

            updateBorderBounds();
            update(Component.CENTER);

            return this;
        }

        public Location getCenter() {
            return new Location(world, centerX, 0, centerZ);
        }

        public XWorldBorder setSize(double newSize, @NotNull Duration duration) {
            if (this.size == newSize && sizeLerpTime.equals(duration)) return this;
            size = newSize;
            sizeLerpTime = duration;

            updateBorderBounds();
            update(Component.SIZE);
            if (!duration.isZero()) update(Component.SIZE_LERP);

            return this;
        }

        private void update(Component comp) {
            if (SUPPORTS_SEPARATE_PACKETS) updateRequired.add(comp);
        }

        public boolean isWithinBorder(Location location) {
            if (this.borderBounds == null) return false;
            if (this.world != location.getWorld()) return false;

            return (location.getX() + 1) > borderBounds.minX &&
                    location.getX() < borderBounds.maxX &&
                    (location.getZ() + 1) > borderBounds.minZ &&
                    location.getZ() < borderBounds.maxZ;
        }

        static {
            if (!SUPPORTS_NATIVE_WORLDBORDERS) {
                Object initialize = null;

                MethodHandle packetInit = null, packetWarnDist = null, packetWarnDelay = null,
                        packetLerpSize = null, packetCenter = null, packetSize = null;

                boolean supportsSeperatePackets;

                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MinecraftClassHandle wb = ofMinecraft()
                        .inPackage(MinecraftPackage.NMS, "world.level.border")
                        .named("WorldBorder"); // Same mapping
                MinecraftClassHandle worldServer = ofMinecraft()
                        .inPackage(MinecraftPackage.NMS, "server.level")
                        .map(MinecraftMapping.MOJANG, "ServerLevel")
                        .map(MinecraftMapping.SPIGOT, "WorldServer");
                MinecraftClassHandle craftWorld = ofMinecraft().inPackage(MinecraftPackage.CB).named("CraftWorld");

                try {
                    if (!supports(17)) {
                        Class<?> wbType;
                        try {
                            wbType = Class.forName("EnumWorldBorderAction");
                        } catch (ClassNotFoundException e) {
                            wbType = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS)
                                    .named("PacketPlayOutWorldBorder$EnumWorldBorderAction").unreflect();
                        }

                        packetInit = lookup.findConstructor(XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS)
                                        .named("PacketPlayOutWorldBorder").unreflect(),
                                MethodType.methodType(void.class, wb.reflect(), wbType));

                        for (Object type : wbType.getEnumConstants()) {
                            if (type.toString().equals("INITIALIZE")) {
                                initialize = type;
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                try {
                    // Individual packets were added in 1.17
                    Function<String, MethodHandle> getPacket = (packet) -> ofMinecraft()
                            .inPackage(MinecraftPackage.NMS, "network.protocol.game")
                            .named(packet)
                            .constructor(wb)
                            .unreflect();

                    packetWarnDist = getPacket.apply("ClientboundSetBorderWarningDistancePacket");
                    packetWarnDelay = getPacket.apply("ClientboundSetBorderWarningDelayPacket");
                    packetLerpSize = getPacket.apply("ClientboundSetBorderLerpSizePacket");
                    packetInit = getPacket.apply("ClientboundInitializeBorderPacket");
                    packetCenter = getPacket.apply("ClientboundSetBorderCenterPacket");
                    packetSize = getPacket.apply("ClientboundSetBorderSizePacket");
                    supportsSeperatePackets = true;
                } catch (Throwable ignored) {
                    supportsSeperatePackets = false;
                }

                PACKET_INIT = packetInit;
                PACKET_SIZE = packetSize;
                PACKET_CENTER = packetCenter;
                PACKET_LERP_SIZE = packetLerpSize;
                PACKET_WARNING_DELAY = packetWarnDelay;
                PACKET_WARNING_DISTANCE = packetWarnDist;

                SUPPORTS_SEPARATE_PACKETS = supportsSeperatePackets;

                WORLD_HANDLE = craftWorld.method().named("getHandle").returns(worldServer).unreflect();
                INITIALIZE = initialize;
                WORLDBORDER = wb.constructor().unreflect();
                WORLDBORDER_WORLD = wb.field().setter().named("world").returns(worldServer).unreflect(); // name not obfuscated since it's added by craftbukkit

                CENTER = wb.method()
                        .named(v(18, "c").orElse("setCenter"))
                        .returns(void.class).parameters(double.class, double.class)
                        .unreflect();
                SIZE = wb.method()
                        .named(v(18, "a").orElse("setSize"))
                        .returns(void.class).parameters(double.class)
                        .unreflect();
                WARNING_TIME = wb.method()
                        .named(v(18, "b").orElse("setWarningTime"))
                        .returns(void.class).parameters(int.class)
                        .unreflect();
                WARNING_DISTANCE = wb.method() // or setWarningBlocks
                        .named(v(20, "c").v(18, "b").orElse("setWarningDistance"))
                        .returns(void.class).parameters(int.class)
                        .unreflect();
                // Renamed to lerpSizeBetween(double d0, double d1, long i)
                TRANSITION = wb.method()
                        .named(v(18, "a").orElse("transitionSizeBetween"))
                        .returns(void.class).parameters(double.class, double.class, long.class)
                        .unreflect();
            } else {
                WORLD_HANDLE = WORLDBORDER = WORLDBORDER_WORLD = CENTER = WARNING_DISTANCE = WARNING_TIME =
                        SIZE = TRANSITION = PACKET_WARNING_DISTANCE = PACKET_WARNING_DELAY = PACKET_LERP_SIZE =
                                PACKET_INIT = PACKET_CENTER = PACKET_SIZE = null;
                INITIALIZE = null;
                SUPPORTS_SEPARATE_PACKETS = true;
            }
        }

        public void setFor(Player player, boolean forceInit) {
            boolean init = forceInit || this.init;
            this.init = false;

            try {
                if (SUPPORTS_SEPARATE_PACKETS && !init) {
                    Object[] packets = new Object[updateRequired.size()];
                    int i = 0;
                    for (Component component : updateRequired) {
                        component.setHandle(this);
                        packets[i++] = component.createPacket(this);
                    }
                    MinecraftConnection.sendPacket(player, packets);
                } else {
                    for (Component component : updateRequired) {
                        component.setHandle(this);
                    }
                    Object packet = supports(17) ?
                            PACKET_INIT.invoke(handle) :
                            PACKET_INIT.invoke(handle, INITIALIZE);
                    MinecraftConnection.sendPacket(player, packet);
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            } finally {
                updateRequired.clear();
            }
        }

        /**
         * Create a new world border object, set its world and center location to the player.
         */
        private Object createHandle() {
            Objects.requireNonNull(world, "No world specified");
            try {
                Object worldBorder = WORLDBORDER.invoke();
                Object world = WORLD_HANDLE.invoke(this.world);
                WORLDBORDER_WORLD.invoke(worldBorder, world);
                return worldBorder;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }

        private enum Component {
            SIZE {
                @Override
                protected void setHandle(NMSWorldBorder wb) throws Throwable {
                    NMSWorldBorder.SIZE.invoke(wb.handle, wb.size);
                }

                @Override
                protected Object createPacket(NMSWorldBorder wb) throws Throwable {
                    return PACKET_SIZE.invoke(wb.handle);
                }
            }, SIZE_LERP {
                @Override
                protected void setHandle(NMSWorldBorder wb) throws Throwable {
                    NMSWorldBorder.TRANSITION.invoke(wb.handle, wb.sizeLerpTarget, wb.size, wb.sizeLerpTime.toMillis());
                }

                @Override
                protected Object createPacket(NMSWorldBorder wb) throws Throwable {
                    return PACKET_LERP_SIZE.invoke(wb.handle);
                }
            }, WARNING_DISTANCE {
                @Override
                protected void setHandle(NMSWorldBorder wb) throws Throwable {
                    NMSWorldBorder.WARNING_DISTANCE.invoke(wb.handle, wb.warningBlocks);
                }

                @Override
                protected Object createPacket(NMSWorldBorder wb) throws Throwable {
                    return PACKET_WARNING_DISTANCE.invoke(wb.handle);
                }
            }, WARNING_DELAY {
                @Override
                protected void setHandle(NMSWorldBorder wb) throws Throwable {
                    NMSWorldBorder.WARNING_TIME.invoke(wb.handle, wb.warningBlocks);
                }

                @Override
                protected Object createPacket(NMSWorldBorder wb) throws Throwable {
                    return PACKET_WARNING_DELAY.invoke(wb.handle);
                }
            }, CENTER {
                @Override
                protected void setHandle(NMSWorldBorder wb) throws Throwable {
                    NMSWorldBorder.CENTER.invoke(wb.handle, wb.centerX, wb.centerZ);
                }

                @Override
                protected Object createPacket(NMSWorldBorder wb) throws Throwable {
                    return PACKET_CENTER.invoke(wb.handle);
                }
            };

            protected abstract void setHandle(NMSWorldBorder wb) throws Throwable;

            protected abstract Object createPacket(NMSWorldBorder wb) throws Throwable;
        }
    }

    private static final class BukkitWorldBorder extends XWorldBorder {
        private final WorldBorder worldBorder;

        private BukkitWorldBorder(WorldBorder worldBorder) {this.worldBorder = worldBorder;}

        // @formatter:off
        public double getDamageBuffer() {return worldBorder.getDamageBuffer();}
        public double getSizeLerpTarget() {return 0; }
        public double getSize() {return worldBorder.getSize();}
        public int getWarningDistance() {return worldBorder.getWarningDistance();}
        public Duration getWarningTime() {return Duration.ofSeconds(worldBorder.getWarningTime());}
        public void setFor(Player player, boolean forceInit) {player.setWorldBorder(this.worldBorder);}


        public XWorldBorder setDamageBuffer(double blocks) { worldBorder.setDamageBuffer(blocks); return this; }
        public XWorldBorder setWarningDistance(int blocks) {worldBorder.setWarningDistance(blocks); return this;}
        public XWorldBorder setWarningTime(Duration time) {worldBorder.setWarningTime((int) time.getSeconds()); return this;}
        public XWorldBorder setSize(double newSize, @NotNull Duration duration) {worldBorder.setSize(newSize); return this;}
        public XWorldBorder setCenter(double x, double z) {worldBorder.setCenter(x, z); return this;}
        public XWorldBorder setSizeLerpTarget(double sizeLerpTarget) {return this;}
        // @formatter:on


        @Override
        public Location getCenter() {
            Location center = worldBorder.getCenter();
            if (borderBounds == null || borderBounds.isCenterSame(center.getWorld(), center.getX(), center.getZ())) {
                updateBorderBounds();
            }
            return center;
        }

        @Override
        public boolean isWithinBorder(Location location) {
            return worldBorder.isInside(location);
        }

        public XWorldBorder copy() {
            WorldBorder border = Bukkit.createWorldBorder();
            border.setCenter(worldBorder.getCenter());
            border.setSize(worldBorder.getSize());
            border.setDamageBuffer(worldBorder.getDamageBuffer());
            border.setDamageAmount(worldBorder.getDamageAmount());
            border.setWarningDistance(worldBorder.getWarningDistance());
            border.setWarningTime(worldBorder.getWarningTime());
            return new BukkitWorldBorder(border);
        }
    }

    @SuppressWarnings("unused")
    public static final class Events implements Listener {
        @EventHandler
        public void onJoin(PlayerMoveEvent event) {
            XWorldBorder wb = get(event.getPlayer());
            if (wb == null) return;
            Player p = event.getPlayer();
            Location loc = p.getLocation();
            if (wb.isWithinBorder(loc)) return;

            double distance = wb.getDistanceToBorder(loc);
            if (distance < wb.getDamageBuffer()) return;
            p.damage(wb.getDamageBuffer() * distance); // Should be per second.
        }

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            XWorldBorder wb = get(player);
            if (wb == null) return;
            wb.setFor(player, true);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onWorldChange(PlayerChangedWorldEvent event) {
            Player player = event.getPlayer();
            XWorldBorder wb = get(player);
            if (wb == null) return;
            wb.setFor(player, true);
        }
    }

    public static XWorldBorder create() {
        if (SUPPORTS_NATIVE_WORLDBORDERS) return new BukkitWorldBorder(Bukkit.createWorldBorder());
        else return new NMSWorldBorder();
    }

    public static XWorldBorder getOrCreate(Player player) {
        XWorldBorder wb = get(player);
        if (wb != null) return wb;

        wb = create();
        wb.setFor(player, true);

        if (!SUPPORTS_NATIVE_WORLDBORDERS) {
            NMSWorldBorder.WORLD_BORDERS.put(player.getUniqueId(), wb);
        }

        return wb;
    }

    @Nullable
    public static XWorldBorder get(Player player) {
        if (SUPPORTS_NATIVE_WORLDBORDERS) {
            WorldBorder worldBorder = player.getWorldBorder();
            return worldBorder == null ? null : new BukkitWorldBorder(worldBorder);
        } else {
            return NMSWorldBorder.WORLD_BORDERS.get(player.getUniqueId());
        }
    }

    public static XWorldBorder from(WorldBorder bukkitWb) {
        if (SUPPORTS_NATIVE_WORLDBORDERS) {
            return new BukkitWorldBorder(bukkitWb).copy();
        } else {
            NMSWorldBorder wb = new NMSWorldBorder();
            wb.world = bukkitWb.getCenter().getWorld(); // Don't use WorldBorder#getWorld() not supported in pre-1.17
            wb.centerX = bukkitWb.getCenter().getX();
            wb.centerZ = bukkitWb.getCenter().getZ();
            wb.size = bukkitWb.getSize();
            wb.sizeLerpTime = Duration.ZERO;
            wb.damagePerBlock = bukkitWb.getDamageAmount();
            wb.damageSafeZone = bukkitWb.getDamageBuffer();
            wb.warningTime = Duration.ofSeconds(bukkitWb.getWarningTime());
            wb.warningBlocks = bukkitWb.getWarningDistance();
            wb.handle = wb.createHandle();
            return wb;
        }
    }
}