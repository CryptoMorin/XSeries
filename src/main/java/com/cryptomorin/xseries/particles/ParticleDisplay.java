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
package com.cryptomorin.xseries.particles;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * By default the particle xyz offsets and speed aren't 0, but
 * everything will be 0 by default in this class.
 * Particles are spawned to a location. So all the nearby players can see it.
 * <p>
 * The fields of this class are publicly accessible for ease of use.
 * All the fields can be null except the particle type.
 * <p>
 * For cross-version compatibility, instead of Bukkit's {@link org.bukkit.Color}
 * the java awt {@link Color} class is used.
 * <p>
 * the data field is used to store special particle data, such as colored particles.
 * For colored particles a float list is used since the particle size is a float.
 * The format of float list data for a colored particle is:
 * <code>[r, g, b, size]</code>
 *
 * @author Crypto Morin
 * @version 5.0.0
 * @see XParticle
 */
public class ParticleDisplay implements Cloneable {
    private static final boolean ISFLAT = XParticle.getParticle("FOOTSTEP") == null;
    private static final Particle DEFAULT_PARTICLE = Particle.CLOUD;

    @Nonnull
    public Particle particle;
    @Nullable
    public Location location;
    @Nullable
    public Callable<Location> locationCaller;
    public int count;
    public double offsetx, offsety, offsetz;
    public double extra;
    @Nullable
    public Vector rotation;
    @Nullable
    public Object data;

    /**
     * Make a new instance of particle display.
     * The position of each particle will be randomized positively and negatively by the offset parameters on each axis.
     *
     * @param particle the particle to spawn.
     * @param location the location to spawn the particle at.
     * @param count    the count of particles to spawn.
     * @param offsetx  the x offset.
     * @param offsety  the y offset.
     * @param offsetz  the z offset.
     * @param extra    in most cases extra is the speed of the particles.
     */
    public ParticleDisplay(@Nonnull Particle particle, @Nullable Callable<Location> locationCaller, @Nullable Location location, int count,
                           double offsetx, double offsety, double offsetz, double extra) {
        this.particle = particle;
        this.location = location;
        this.locationCaller = locationCaller;
        this.count = count;
        this.offsetx = offsetx;
        this.offsety = offsety;
        this.offsetz = offsetz;
        this.extra = extra;
    }

    public ParticleDisplay(@Nonnull Particle particle, @Nullable Location location, int count, double offsetx, double offsety, double offsetz) {
        this(particle, null, location, count, offsetx, offsety, offsetz, 0);
    }

    public ParticleDisplay(@Nonnull Particle particle, @Nullable Location location, int count) {
        this(particle, location, count, 0, 0, 0);
    }

    public ParticleDisplay(@Nonnull Particle particle, @Nullable Location location) {
        this(particle, location, 0);
    }

    /**
     * Builds a simple ParticleDisplay object with cross-version
     * compatible {@link org.bukkit.Particle.DustOptions} properties.
     *
     * @param location the location of the display.
     * @param size     the size of the dust.
     *
     * @return a redstone colored dust.
     * @see #simple(Location, Particle)
     * @since 1.0.0
     */
    @Nonnull
    public static ParticleDisplay colored(@Nullable Location location, int r, int g, int b, float size) {
        ParticleDisplay dust = new ParticleDisplay(Particle.REDSTONE, null, location, 1, 0, 0, 0, 0);
        dust.data = new float[]{r, g, b, size};
        return dust;
    }

    /**
     * Builds a simple ParticleDisplay object with cross-version
     * compatible {@link org.bukkit.Particle.DustOptions} properties.
     *
     * @param location the location of the display.
     * @param color    the color of the particle.
     * @param size     the size of the dust.
     *
     * @return a redstone colored dust.
     * @see #colored(Location, int, int, int, float)
     * @since 3.0.0
     */
    @Nonnull
    public static ParticleDisplay colored(@Nullable Location location, @Nonnull Color color, float size) {
        return colored(location, color.getRed(), color.getGreen(), color.getBlue(), size);
    }

    /**
     * Builds a simple ParticleDisplay object.
     * An invocation of this method yields exactly the same result as the expression:
     * <p>
     * <blockquote>
     * new ParticleDisplay(particle, location, 1, 0, 0, 0, 0);
     * </blockquote>
     *
     * @param location the location of the display.
     * @param particle the particle of the display.
     *
     * @return a simple ParticleDisplay.
     * @since 1.0.0
     */
    @Nonnull
    public static ParticleDisplay simple(@Nullable Location location, @Nonnull Particle particle) {
        Objects.requireNonNull(particle, "Cannot build ParticleDisplay with null particle");
        return new ParticleDisplay(particle, null, location, 1, 0, 0, 0, 0);
    }

    /**
     * A quick access method to display a simple particle.
     * An invocation of this method yields the same result as the expression:
     * <p>
     * <blockquote>
     * ParticleDisplay.simple(location, particle).spawn();
     * </blockquote>
     *
     * @param location the location of the particle.
     * @param particle the particle to show.
     *
     * @return a simple ParticleDisplay.
     * @since 1.0.0
     */
    @Nonnull
    public static ParticleDisplay display(@Nonnull Location location, @Nonnull Particle particle) {
        Objects.requireNonNull(location, "Cannot display particle in null location");
        ParticleDisplay display = simple(location, particle);
        display.spawn();
        return display;
    }

    /**
     * Builds particle settings from a configuration section.
     *
     * @param location the location to display this particle.
     * @param config   the config section for the settings.
     *
     * @return a parsed ParticleDisplay.
     * @since 1.0.0
     */
    public static ParticleDisplay fromConfig(@Nullable Location location, @Nonnull ConfigurationSection config) {
        ParticleDisplay display = new ParticleDisplay(DEFAULT_PARTICLE, location);
        return edit(display, config);
    }

    /**
     * Builds particle settings from a configuration section.
     *
     * @param display the particle display settings to update.
     * @param config  the config section for the settings.
     *
     * @return an edited ParticleDisplay.
     * @since 5.0.0
     */
    @Nonnull
    public static ParticleDisplay edit(@Nonnull ParticleDisplay display, @Nonnull ConfigurationSection config) {
        Objects.requireNonNull(display, "Cannot edit a null particle display");
        Objects.requireNonNull(config, "Cannot parse ParticleDisplay from a null config section");

        String particleName = config.getString("particle");
        Particle particle = particleName == null ? null : XParticle.getParticle(particleName);
        int count = config.getInt("count");
        double extra = config.getDouble("extra");

        if (particle != null) display.particle = particle;
        if (count != 0) display.withCount(count);
        if (extra != 0) display.extra = extra;

        String offset = config.getString("offset");
        if (offset != null) {
            String[] offsets = StringUtils.split(StringUtils.deleteWhitespace(offset), ',');
            if (offsets.length >= 3) {
                double offsetx = NumberUtils.toDouble(offsets[0]);
                double offsety = NumberUtils.toDouble(offsets[1]);
                double offsetz = NumberUtils.toDouble(offsets[2]);
                display.offset(offsetx, offsety, offsetz);
            }
        }

        String rotation = config.getString("rotation");
        if (rotation != null) {
            String[] rotations = StringUtils.split(StringUtils.deleteWhitespace(rotation), ',');
            if (rotations.length >= 3) {
                double x = NumberUtils.toDouble(rotations[0]);
                double y = NumberUtils.toDouble(rotations[1]);
                double z = NumberUtils.toDouble(rotations[2]);
                display.rotation = new Vector(x, y, z);
            }
        }

        String color = config.getString("color");
        if (color != null) {
            String[] colors = StringUtils.split(StringUtils.deleteWhitespace(color), ',');
            if (colors.length >= 3) {
                display.data = new float[]{
                        // Color
                        NumberUtils.toInt(colors[0]), NumberUtils.toInt(colors[1]), NumberUtils.toInt(colors[2]),
                        // Size
                        (colors.length > 3 ? NumberUtils.toFloat(colors[3]) : 1.0f)};
            }
        }

        return display;
    }

    /**
     * Rotates the given xyz with the given rotation radians and
     * adds the to the specified location.
     *
     * @param location the location to add the rotated axis.
     * @param rotation the xyz rotation radians.
     *
     * @return a cloned rotated location.
     * @since 3.0.0
     */
    @Nonnull
    public static Location rotate(@Nonnull Location location, double x, double y, double z, @Nullable Vector rotation) {
        if (rotation == null) return cloneLocation(location).add(x, y, z);

        Vector rotate = new Vector(x, y, z);
        XParticle.rotateAround(rotate, rotation.getX(), rotation.getY(), rotation.getZ());
        return cloneLocation(location).add(rotate);
    }

    /**
     * We don't want to use {@link Location#clone()} since it doens't copy to constructor and Javas clone method
     * is known to be inefficient and broken.
     *
     * @since 3.0.3
     */
    @Nonnull
    private static Location cloneLocation(@Nonnull Location location) {
        return new Location(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    @Override
    public String toString() {
        return "ParticleDisplay:[Particle=" + particle + ", Count=" + count + ", Offset:{" + offsetx + ", " + offsety + ", " + offsetz + "}, Extra=" + extra
                + ", Data=" + (data == null ? "null" : data instanceof float[] ? Arrays.toString((float[]) data) : data);
    }

    /**
     * Changes the particle count of the particle settings.
     *
     * @param count the particle count.
     *
     * @return the same particle display.
     * @since 3.0.0
     */
    @Nonnull
    public ParticleDisplay withCount(int count) {
        this.count = count;
        return this;
    }

    /**
     * In most cases extra is the speed of the particles.
     *
     * @param extra the extra number.
     *
     * @return the same particle display.
     * @since 3.0.1
     */
    @Nonnull
    public ParticleDisplay withExtra(double extra) {
        this.extra = extra;
        return this;
    }

    /**
     * Adds color properties to the particle settings.
     *
     * @param color the RGB color of the particle.
     * @param size  the size of the particle.
     *
     * @return a colored particle.
     * @see #colored(Location, Color, float)
     * @since 3.0.0
     */
    @Nonnull
    public ParticleDisplay withColor(@Nonnull Color color, float size) {
        this.data = new float[]{color.getRed(), color.getGreen(), color.getBlue(), size};
        return this;
    }

    /**
     * Saves an instance of an entity to track the location from.
     *
     * @param entity the entity to track the location from.
     *
     * @return the location tracker entity.
     * @since 3.1.0
     */
    @Nonnull
    public ParticleDisplay withEntity(@Nullable Entity entity) {
        return withLocationCaller(entity::getLocation);
    }

    /**
     * Sets a caller for location changes.
     *
     * @param locationCaller the caller to call to get the new location.
     *
     * @return the same particle settings with the caller added.
     * @since 3.1.0
     */
    @Nonnull
    public ParticleDisplay withLocationCaller(@Nullable Callable<Location> locationCaller) {
        this.locationCaller = locationCaller;
        return this;
    }

    /**
     * Gets the location of an entity if specified or the constant location.
     *
     * @return the location of the particle.
     * @since 3.1.0
     */
    @Nullable
    public Location getLocation() {
        try {
            return locationCaller == null ? location : locationCaller.call();
        } catch (Exception e) {
            e.printStackTrace();
            return location;
        }
    }

    /**
     * Adjusts the rotation settings to face the entitys direction.
     * Only some of the shapes support this method.
     *
     * @param entity the entity to face.
     *
     * @return the same particle display.
     * @see #rotate(Vector)
     * @since 3.0.0
     */
    @Nonnull
    public ParticleDisplay faceEntity(@Nonnull Entity entity) {
        Objects.requireNonNull(entity, "Cannot face null entity");
        Location loc = entity.getLocation();
        this.rotation = new Vector(Math.toRadians(loc.getPitch() + 90), Math.toRadians(-loc.getYaw()), 0);
        return this;
    }

    /**
     * Clones the location of this particle display and adds xyz.
     *
     * @param x the x to add to the location.
     * @param y the y to add to the location.
     * @param z the z to add to the location.
     *
     * @return the cloned location.
     * @see #clone()
     * @since 1.0.0
     */
    @Nullable
    public Location cloneLocation(double x, double y, double z) {
        return location == null ? null : cloneLocation(location).add(x, y, z);
    }

    /**
     * Clones this particle settings and adds xyz to its location.
     *
     * @param x the x to add.
     * @param y the y to add.
     * @param z the z to add.
     *
     * @return the cloned ParticleDisplay.
     * @see #clone()
     * @since 1.0.0
     */
    @Nonnull
    public ParticleDisplay cloneWithLocation(double x, double y, double z) {
        ParticleDisplay display = clone();
        if (location == null) return display;
        display.location.add(x, y, z);
        return display;
    }

    /**
     * Clones this particle settings.
     *
     * @return the cloned ParticleDisplay.
     * @see #cloneWithLocation(double, double, double)
     * @see #cloneLocation(double, double, double)
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    @Nonnull
    public ParticleDisplay clone() {
        ParticleDisplay display = new ParticleDisplay(particle, locationCaller, (location == null ? null : cloneLocation(location)), count, offsetx, offsety, offsetz, extra);
        if (rotation != null) display.rotation = new Vector(rotation.getX(), rotation.getY(), rotation.getZ());
        display.data = data;
        return display;
    }

    /**
     * Rotates the particle position based on this vector.
     *
     * @param vector the vector to rotate from. The xyz values of this vectors must be radians.
     *
     * @see #rotate(double, double, double)
     * @since 1.0.0
     */
    @Nonnull
    public ParticleDisplay rotate(@Nonnull Vector vector) {
        Objects.requireNonNull(vector, "Cannot rotate ParticleDisplay with null vector");
        if (rotation == null) rotation = vector;
        else rotation.add(vector);
        return this;
    }

    /**
     * Rotates the particle position based on the xyz radians.
     * Rotations are only supported for some shapes in {@link XParticle}.
     * Rotating some of them can result in weird shapes.
     *
     * @see #rotate(Vector)
     * @since 3.0.0
     */
    @Nonnull
    public ParticleDisplay rotate(double x, double y, double z) {
        return rotate(new Vector(x, y, z));
    }

    /**
     * Set the xyz offset of the particle settings.
     *
     * @since 1.1.0
     */
    @Nonnull
    public ParticleDisplay offset(double x, double y, double z) {
        offsetx = x;
        offsety = y;
        offsetz = z;
        return this;
    }

    /**
     * When a particle is set to be directional it'll only
     * spawn one particle and the xyz offset values are used for
     * the direction of the particle.
     * <p>
     * Colored particles in 1.12 and below don't support this.
     *
     * @return the same particle display.
     * @see #isDirectional()
     * @since 1.1.0
     */
    @Nonnull
    public ParticleDisplay directional() {
        count = 0;
        return this;
    }

    /**
     * Check if this particle setting is a directional particle.
     *
     * @return true if the particle is directional, otherwise false.
     * @see #directional()
     * @since 2.1.0
     */
    public boolean isDirectional() {
        return count == 0;
    }

    /**
     * Spawns the particle at the current location.
     *
     * @since 2.0.1
     */
    public void spawn() {
        spawn(getLocation());
    }

    /**
     * Adds xyz of the given vector to the cloned location before
     * spawning particles.
     *
     * @param location the xyz to add.
     *
     * @since 1.0.0
     */
    @Nonnull
    public Location spawn(@Nonnull Vector location) {
        Objects.requireNonNull(location, "Cannot add xyz of null vector to ParticleDisplay");
        return spawn(location.getX(), location.getY(), location.getZ());
    }

    /**
     * Adds xyz to the cloned loaction before spawning particle.
     *
     * @since 1.0.0
     */
    @Nonnull
    public Location spawn(double x, double y, double z) {
        return spawn(rotate(getLocation(), x, y, z, rotation));
    }

    /**
     * Displays the particle in the specified location.
     * This method does not support rotations if used directly.
     *
     * @param loc the location to display the particle at.
     *
     * @see #spawn(double, double, double)
     * @since 2.1.0
     */
    @Nonnull
    public Location spawn(@Nonnull Location loc) {
        return spawn(loc, (Player[]) null);
    }

    /**
     * Displays the particle in the specified location.
     * This method does not support rotations if used directly.
     *
     * @param loc     the location to display the particle at.
     * @param players if this particle should only be sent to specific players.
     *
     * @see #spawn(double, double, double)
     * @since 5.0.0
     */
    @Nonnull
    public Location spawn(@Nonnull Location loc, @Nullable Player... players) {
        if (data != null) {
            if (data instanceof float[]) {
                float[] datas = (float[]) data;
                if (ISFLAT) {
                    Particle.DustOptions dust = new Particle.DustOptions(org.bukkit.Color
                            .fromRGB((int) datas[0], (int) datas[1], (int) datas[2]), datas[3]);
                    if (players == null) loc.getWorld().spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, dust);
                    else for (Player player : players) player.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, dust);
                } else {
                    if (players == null) loc.getWorld().spawnParticle(particle, loc, count, (int) datas[0], (int) datas[1], (int) datas[2], datas[3]);
                    else for (Player player : players) player.spawnParticle(particle, loc, count, (int) datas[0], (int) datas[1], (int) datas[2], datas[3]);
                }
            }
        } else {
            if (players == null) loc.getWorld().spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra);
            else for (Player player : players) player.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra);
        }
        return loc;
    }
}