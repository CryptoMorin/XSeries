/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Crypto Morin
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * By default the particle xyz offsets and speed aren't 0, but
 * everything will be 0 by default in this class.
 * Particles are spawned to a location. So all the nearby players can see it.
 * <p>
 * The fields of this class are publicly accessible for ease of use.
 * All the fields can be null but they can cause trouble when
 * spawning them.
 *
 * @author Crypto Morin
 * @version 2.1.0
 * @see XParticle
 */
public class ParticleDisplay {
    private static boolean ISFLAT = XParticle.getParticle("FOOTSTEP") == null;
    public Particle particle;
    public Location location;
    public int count;
    public double offsetx, offsety, offsetz;
    public double extra;
    public Vector rotation;
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
    public ParticleDisplay(Particle particle, @Nullable Location location, int count, double offsetx, double offsety, double offsetz, double extra) {
        this.particle = particle;
        this.location = location;
        this.count = count;
        this.offsetx = offsetx;
        this.offsety = offsety;
        this.offsetz = offsetz;
        this.extra = extra;
    }

    public ParticleDisplay(Particle particle, Location location, int count, double offsetx, double offsety, double offsetz) {
        this(particle, location, count, offsetx, offsety, offsetz, 0);
    }

    public ParticleDisplay(Particle particle, Location location, int count) {
        this(particle, location, count, 0, 0, 0);
    }

    public ParticleDisplay(Particle particle, Location location) {
        this(particle, location, 0);
    }

    /**
     * Builds a simple ParticleDisplay object with cross-version
     * compatible {@link org.bukkit.Particle.DustOptions} properties.
     *
     * @param location the location of the display.
     * @param r        the red color RGB.
     * @param g        the green color RGB.
     * @param b        the blue color RGB.
     * @param size     the size of the dust.
     * @return a redstone colored dust.
     * @see #simple(Location, Particle)
     * @since 1.0.0
     */
    @Nonnull
    public static ParticleDisplay paintDust(@Nullable Location location, int r, int g, int b, float size) {
        ParticleDisplay dust = new ParticleDisplay(Particle.REDSTONE, location, 1, 0, 0, 0, 0);
        dust.data = new float[]{r, g, b, size};
        return dust;
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
     * @return a simple ParticleDisplay.
     * @since 1.0.0
     */
    @Nonnull
    public static ParticleDisplay simple(@Nullable Location location, @Nonnull Particle particle) {
        Objects.requireNonNull(particle, "Cannot build ParticleDisplay with null particle");
        return new ParticleDisplay(particle, location, 1, 0, 0, 0, 0);
    }

    /**
     * Builds particle settings from a configuration section.
     *
     * @param location the location for this particle settings.
     * @param config   the config section for the settings.
     * @return a parsed ParticleDisplay.
     * @since 1.0.0
     */
    @Nonnull
    public static ParticleDisplay fromConfig(@Nullable Location location, @Nonnull ConfigurationSection config) {
        Objects.requireNonNull(config, "Cannot parse ParticleDisplay from a null config section");
        Particle particle = XParticle.getParticle(config.getString("particle"));
        if (particle == null) particle = Particle.FLAME;
        int count = config.getInt("count");
        double extra = config.getDouble("extra");
        double offsetx = 0, offsety = 0, offsetz = 0;

        String offset = config.getString("offset");
        if (offset != null) {
            String[] offsets = StringUtils.split(offset, ',');
            if (offsets.length > 0) {
                offsetx = NumberUtils.toDouble(offsets[0]);
                if (offsets.length > 1) {
                    offsety = NumberUtils.toDouble(offsets[1]);
                    if (offsets.length > 2) {
                        offsetz = NumberUtils.toDouble(offsets[2]);
                    }
                }
            }
        }

        double x = 0, y = 0, z = 0;
        String rotation = config.getString("offset");
        if (offset != null) {
            String[] rotations = StringUtils.split(rotation, ',');
            if (rotations.length > 0) {
                x = NumberUtils.toDouble(rotations[0]);
                if (rotations.length > 1) {
                    y = NumberUtils.toDouble(rotations[1]);
                    if (rotations.length > 2) {
                        z = NumberUtils.toDouble(rotations[2]);
                    }
                }
            }
        }

        Vector rotate = new Vector(x, y, z);
        ParticleDisplay display = new ParticleDisplay(particle, location, count, offsetx, offsety, offsetz, extra);
        display.rotation = rotate;
        return display;
    }

    /**
     * Adjusts the rotation settings to face the entitys direction.
     * Only some of the shapes support this method.
     *
     * @param entity the entity to face.
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
     * @return the cloned location.
     * @see #clone()
     * @since 1.0.0
     */
    @Nullable
    public Location cloneLocation(double x, double y, double z) {
        if (location == null) return null;
        return location.clone().add(x, y, z);
    }

    /**
     * Clones this particle settings and adds xyz to its location.
     *
     * @param x the x to add.
     * @param y the y to add.
     * @param z the z to add.
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
    public ParticleDisplay clone() {
        ParticleDisplay display = new ParticleDisplay(particle, (location == null ? null : location.clone()), count, offsetx, offsety, offsetz, extra);
        if (rotation != null) display.rotation = rotation.clone();
        display.data = data;
        return display;
    }

    /**
     * Rotates the particle position based on this vector.
     *
     * @param vector the vector to rotate from. The xyz values of this vectors must be radians.
     * @see #rotate(double, double, double)
     * @since 1.0.0
     */
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
    public ParticleDisplay rotate(double x, double y, double z) {
        rotate(new Vector(x, y, z));
        return this;
    }

    /**
     * Adds xyz of the given vector to the cloned location before
     * spawning particles.
     *
     * @param vector the vector to add.
     * @since 1.0.0
     */
    public void spawn(@Nonnull Vector vector) {
        Objects.requireNonNull(vector, "Cannot add xyz of null vector to ParticleDisplay");
        spawn(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * Set the xyz offset of the particle settings.
     *
     * @since 1.1.0
     */
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
     * @see #isDirectional()
     * @since 1.1.0
     */
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
     * @see #spawn(Location)
     * @since 2.0.1
     */
    public void spawn() {
        spawn(this.location);
    }

    /**
     * Adds xyz to the cloned loaction before spawning particle.
     *
     * @since 1.0.0
     */
    public Location spawn(double x, double y, double z) {
        Location loc;
        if (rotation != null) {
            Vector rotate = new Vector(x, y, z);
            if (rotation.getX() != 0) XParticle.rotateAroundX(rotate, rotation.getX());
            if (rotation.getY() != 0) XParticle.rotateAroundY(rotate, rotation.getY());
            if (rotation.getZ() != 0) XParticle.rotateAroundZ(rotate, rotation.getZ());
            loc = location.clone().add(rotate);
        } else loc = location.clone().add(x, y, z);
        spawn(loc);
        return loc;
    }

    /**
     * Displays the particle in the specified location.
     * This method does not support rotations if used directly.
     *
     * @param loc the location to display the particle at.
     * @see #spawn(double, double, double)
     * @since 3.0.0
     */
    public void spawn(Location loc) {
        if (data != null) {
            if (data instanceof float[]) {
                float[] datas = (float[]) data;
                if (ISFLAT) {
                    Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB((int) datas[0], (int) datas[1], (int) datas[2]), datas[3]);
                    loc.getWorld().spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, dust);
                } else {
                    loc.getWorld().spawnParticle(particle, loc, count, (int) datas[0], (int) datas[1], (int) datas[2], datas[3]);
                }
            }
        } else {
            loc.getWorld().spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra);
        }
    }
}