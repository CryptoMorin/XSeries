/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Crypto Morin
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
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

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
 * @version 7.1.0
 * @see XParticle
 */
public class ParticleDisplay implements Cloneable {
    /**
     * Checks if spawn methods should use particle data classes such as {@link org.bukkit.Particle.DustOptions}
     * which is only available from 1.13+ (FOOTSTEP was removed in 1.13)
     *
     * @since 1.0.0
     */
    private static final boolean ISFLAT = XParticle.getParticle("FOOTSTEP") == null;
    /**
     * Checks if spawn methods should use particle data classes such as {@link org.bukkit.Particle.DustTransition}
     * which is only available from 1.17+ (VIBRATION was released in 1.17)
     *
     * @since 8.6.0.0.1
     */
    private static final boolean ISFLAT2 = XParticle.getParticle("VIBRATION") != null;
    private static final Axis[] DEFAULT_ROTATION_ORDER = {Axis.X, Axis.Y, Axis.Z};
    private static final Particle DEFAULT_PARTICLE = Particle.CLOUD;

    public int count;
    public double extra;
    public boolean force;
    @Nonnull private Particle particle = DEFAULT_PARTICLE;
    @Nullable private Location location;
    @Nullable private Callable<Location> locationCaller;
    @Nullable private Vector rotation, offset = new Vector();
    @Nonnull private Axis[] rotationOrder = DEFAULT_ROTATION_ORDER;
    @Nullable private Object data;
    @Nullable private Predicate<Location> onSpawn;

    /**
     * Builds a simple ParticleDisplay object with cross-version
     * compatible {@link org.bukkit.Particle.DustOptions} properties.
     * Only REDSTONE particle type can be colored like this.
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
        return ParticleDisplay.simple(location, Particle.REDSTONE).withColor(r, g, b, size);
    }

    /**
     * Builds a simple ParticleDisplay object with cross-version
     * compatible {@link org.bukkit.Particle.DustOptions} properties.
     * Only REDSTONE particle type can be colored like this.
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
     * @return a simple ParticleDisplay with count 1 and no offset, rotation etc.
     * @since 1.0.0
     */
    @Nonnull
    public static ParticleDisplay simple(@Nullable Location location, @Nonnull Particle particle) {
        Objects.requireNonNull(particle, "Cannot build ParticleDisplay with null particle");
        ParticleDisplay display = new ParticleDisplay();
        display.particle = particle;
        display.location = location;
        return display;
    }

    /**
     * @since 6.0.0.1
     */
    @Nonnull
    public static ParticleDisplay of(@Nonnull Particle particle) {
        return simple(null, particle);
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
     * @return a simple ParticleDisplay with count 1 and no offset, rotation etc.
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
     * @param config the config section for the settings.
     *
     * @return a parsed ParticleDisplay from the config.
     * @since 1.0.0
     */
    public static ParticleDisplay fromConfig(@Nonnull ConfigurationSection config) {
        return edit(new ParticleDisplay(), config);
    }

    /**
     * Builds particle settings from a configuration section. Keys in config can be :
     * <ul>
     * <li>particle : the particle type.
     * <li>count : the count as integer, at least 0.
     * <li>extra : the particle speed, most of the time.
     * <li>force : true or false, if the particle has force or not.
     * <li>offset : the offset where values are separated by commas "dx, dy, dz".
     * <li>rotation : the rotation of the particles in degrees.
     * <li>color : the data representing color "R, G, B, size" where RGB values are integers
     *             between 0 and 255 and size is a positive (or null) float.
     * <li>blockdata : the data representing block data. Given by a material name that's a block.
     * <li>materialdata : same than blockdata, but with legacy data before 1.12.
     *                    <strong>Do not use this in 1.13 and above.</strong>
     * <li>itemstack : the data representing item. Given by a material name that's an item.
     * </ul>
     *
     * @param display the particle display settings to update.
     * @param config  the config section for the settings.
     *
     * @return the same ParticleDisplay, but edited.
     * @since 5.0.0
     */
    @Nonnull
    public static ParticleDisplay edit(@Nonnull ParticleDisplay display, @Nonnull ConfigurationSection config) {
        Objects.requireNonNull(display, "Cannot edit a null particle display");
        Objects.requireNonNull(config, "Cannot parse ParticleDisplay from a null config section");

        String particleName = config.getString("particle");
        Particle particle = particleName == null ? null : XParticle.getParticle(particleName);

        if (particle != null) display.particle = particle;
        if (config.isSet("count")) display.withCount(config.getInt("count"));
        if (config.isSet("extra")) display.withExtra(config.getDouble("extra"));
        if (config.isSet("force")) display.forceSpawn(config.getBoolean("force"));

        String offset = config.getString("offset");
        if (offset != null) {
            String[] offsets = StringUtils.split(StringUtils.deleteWhitespace(offset), ',');
            if (offsets.length >= 3) {
                double offsetx = NumberUtils.toDouble(offsets[0]);
                double offsety = NumberUtils.toDouble(offsets[1]);
                double offsetz = NumberUtils.toDouble(offsets[2]);
                display.offset(offsetx, offsety, offsetz);
            } else {
                double masterOffset = NumberUtils.toDouble(offsets[0]);
                display.offset(masterOffset);
            }
        }

        String rotation = config.getString("rotation");
        if (rotation != null) {
            String[] rotations = StringUtils.split(StringUtils.deleteWhitespace(rotation), ',');
            if (rotations.length >= 3) {
                double x = Math.toRadians(NumberUtils.toDouble(rotations[0]));
                double y = Math.toRadians(NumberUtils.toDouble(rotations[1]));
                double z = Math.toRadians(NumberUtils.toDouble(rotations[2]));
                display.rotation = new Vector(x, y, z);
            }
        }

        String rotationOrder = config.getString("rotation-order");
        if (rotationOrder != null) {
            rotationOrder = StringUtils.deleteWhitespace(rotationOrder).toUpperCase(Locale.ENGLISH);
            display.rotationOrder(
                    Axis.valueOf(String.valueOf(rotationOrder.charAt(0))),
                    Axis.valueOf(String.valueOf(rotationOrder.charAt(1))),
                    Axis.valueOf(String.valueOf(rotationOrder.charAt(2)))
            );
        }

        String color = config.getString("color"); // array-like "R, G, B"
        String blockdata = config.getString("blockdata");       // material name
        String item = config.getString("itemstack");            // material name
        String materialdata = config.getString("materialdata"); // material name

        float size;
        if (config.isSet("size")) {
            size = (float) config.getDouble("size");
            if (display.data instanceof float[]) {
                float[] datas = (float[]) display.data;
                if (datas.length > 3) {
                    datas[3] = size;
                }
            }
        } else {
            size = 1f;
        }

        if (color != null) {
            String[] colors = StringUtils.split(StringUtils.deleteWhitespace(color), ',');
            if (colors.length <= 3 || colors.length == 6) { // 1 or 3 : single color, 2 or 6 : two colors for DUST_TRANSITION
                Color parsedColor1 = Color.white;
                Color parsedColor2 = null;
                if (colors.length <= 2) {
                    try {
                        parsedColor1 = Color.decode(colors[0]);
                        if (colors.length == 2)
                            parsedColor2 = Color.decode(colors[1]);
                    } catch (NumberFormatException ex) {
                        /* I don't think it's worth it.
                        try {
                            parsedColor = (Color) Color.class.getField(colors[0].toUpperCase(Locale.ENGLISH)).get(null);
                        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ignored) { }
                         */
                    }
                } else {
                    parsedColor1 = new Color(NumberUtils.toInt(colors[0]), NumberUtils.toInt(colors[1]), NumberUtils.toInt(colors[2]));
                    if (colors.length == 6)
                        parsedColor2 = new Color(NumberUtils.toInt(colors[3]), NumberUtils.toInt(colors[4]), NumberUtils.toInt(colors[5]));
                }

                if (parsedColor2 != null) {
                    display.data = new float[]{
                            parsedColor1.getRed(), parsedColor1.getGreen(), parsedColor1.getBlue(),
                            size,
                            parsedColor2.getRed(), parsedColor2.getGreen(), parsedColor2.getBlue()
                    };
                } else {
                    display.data = new float[]{
                            parsedColor1.getRed(), parsedColor1.getGreen(), parsedColor1.getBlue(),
                            size
                    };
                }
            }
        } else if (blockdata != null) {
            Material material = Material.getMaterial(blockdata);
            if (material != null && material.isBlock()) {
                display.data = material.createBlockData();
            }
        } else if (item != null) {
            Material material = Material.getMaterial(item);
            if (material != null && material.isItem()) {
                display.data = new ItemStack(material, 1);
            }
        } else if (materialdata != null) {
            Material material = Material.getMaterial(materialdata);
            if (material != null && material.isBlock()) {
                display.data = material.getData();
            }
        }

        return display;
    }

    /**
     * We don't want to use {@link Location#clone()} since it doesn't copy to constructor and Java's clone method
     * is known to be inefficient and broken.
     *
     * @since 3.0.3
     */
    @Nonnull
    private static Location cloneLocation(@Nonnull Location location) {
        return new Location(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    /**
     * Rotates the given location vector around a certain axis.
     *
     * @param location the location to rotate.
     * @param axis     the axis to rotate the location around.
     * @param rotation the rotation vector that contains the degrees of the rotation. The number is taken from this vector according to the given axis.
     *
     * @since 7.0.0
     */
    public static Vector rotateAround(@Nonnull Vector location, @Nonnull Axis axis, @Nonnull Vector rotation) {
        Objects.requireNonNull(axis, "Cannot rotate around null axis");
        Objects.requireNonNull(rotation, "Rotation vector cannot be null");

        switch (axis) {
            case X:
                return rotateAround(location, axis, rotation.getX());
            case Y:
                return rotateAround(location, axis, rotation.getY());
            case Z:
                return rotateAround(location, axis, rotation.getZ());
            default:
                throw new AssertionError("Unknown rotation axis: " + axis);
        }
    }

    /**
     * Rotates the given location vector around a certain axis.
     *
     * @param location the location to rotate.
     *
     * @since 7.0.0
     */
    public static Vector rotateAround(@Nonnull Vector location, double x, double y, double z) {
        rotateAround(location, Axis.X, x);
        rotateAround(location, Axis.Y, y);
        rotateAround(location, Axis.Z, z);
        return location;
    }

    /**
     * Rotates the given location vector around a certain axis.
     *
     * @param location the location to rotate.
     * @param axis     the axis to rotate the location around.
     *
     * @since 7.0.0
     */
    public static Vector rotateAround(@Nonnull Vector location, @Nonnull Axis axis, double angle) {
        Objects.requireNonNull(location, "Cannot rotate a null location");
        Objects.requireNonNull(axis, "Cannot rotate around null axis");
        if (angle == 0) return location;

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        switch (axis) {
            case X: {
                double y = location.getY() * cos - location.getZ() * sin;
                double z = location.getY() * sin + location.getZ() * cos;
                return location.setY(y).setZ(z);
            }
            case Y: {
                double x = location.getX() * cos + location.getZ() * sin;
                double z = location.getX() * -sin + location.getZ() * cos;
                return location.setX(x).setZ(z);
            }
            case Z: {
                double x = location.getX() * cos - location.getY() * sin;
                double y = location.getX() * sin + location.getY() * cos;
                return location.setX(x).setY(y);
            }
            default:
                throw new AssertionError("Unknown rotation axis: " + axis);
        }
    }

    /**
     * A simple event that is called after the final calculations of each particle location are applied.
     * You can modify the given location. It's NOT a copy.
     *
     * @param onSpawn a predicate that if returns false, it'll not spawn that particle.
     *
     * @return the same particle display.
     * @since 7.0.0
     */
    public ParticleDisplay onSpawn(@Nullable Predicate<Location> onSpawn) {
        this.onSpawn = onSpawn;
        return this;
    }

    /**
     * @since 7.0.0
     */
    public void withParticle(@Nonnull Particle particle) {
        this.particle = Objects.requireNonNull(particle, "Particle cannot be null");
    }

    /**
     * Rotates the given xyz with the given rotation radians and
     * adds the to the specified location.
     *
     * @param location the location to add the rotated axis.
     *
     * @return a cloned rotated location.
     * @since 3.0.0
     */
    @Nonnull
    public Location rotate(@Nonnull Location location, double x, double y, double z) {
        if (location == null) throw new IllegalStateException("Attempting to spawn particle when no location is set");
        if (rotation == null) return cloneLocation(location).add(x, y, z);

        Vector rotate = new Vector(x, y, z);
        rotateAround(rotate, rotationOrder[0], rotation);
        rotateAround(rotate, rotationOrder[1], rotation);
        rotateAround(rotate, rotationOrder[2], rotation);

        return cloneLocation(location).add(rotate);
    }

    /**
     * Get the data object. Currently, it can be instance of float[] with [R, G, B, size],
     * or instance of {@link BlockData}, {@link MaterialData} for legacy usage or {@link ItemStack}
     *
     * @return the data object.
     * @since 5.1.0
     */
    @SuppressWarnings("deprecation")
    @Nullable
    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        Location location = getLocation();
        return "ParticleDisplay:[" +
                "Particle=" + particle + ", " +
                "Count=" + count + ", " +
                "Offset:{" + offset.getX() + ", " + offset.getY() + ", " + offset.getZ() + "}, " +

                (location != null ? (
                        "Location:{" + location.getWorld().getName() + location.getX() + ", " + location.getY() + ", " + location.getZ() + "} " +
                                '(' + (locationCaller == null ? "Static" : "Dynamic") + "), "
                ) : "") +

                (rotation != null ? (
                        "Rotation:{" + Math.toDegrees(rotation.getX()) + ", " + Math.toRadians(rotation.getY()) + ", " + Math.toDegrees(rotation.getZ()) + "}, "
                ) : "") +

                (rotationOrder != DEFAULT_ROTATION_ORDER ? ("RotationOrder:" + Arrays.toString(rotationOrder) + ", ") : "") +

                "Extra=" + extra + ", " +
                "Force=" + force + ", " +
                "Data=" + (data == null ? "null" : data instanceof float[] ? Arrays.toString((float[]) data) : data);
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
     * A displayed particle with force can be seen further
     * away for all player regardless of their particle
     * settings. Force has no effect if specific players
     * are added with {@link #spawn(Location, Player...)}.
     *
     * @param force the force argument.
     *
     * @return the same particle display, but modified.
     * @since 5.0.1
     */
    @Nonnull
    public ParticleDisplay forceSpawn(boolean force) {
        this.force = force;
        return this;
    }

    /**
     * Adds color properties to the particle settings.
     * The particle must be {@link Particle#REDSTONE}
     * to get custom colors.
     *
     * @param color the RGB color of the particle.
     * @param size  the size of the particle.
     *
     * @return the same particle display, but modified.
     * @see #colored(Location, Color, float)
     * @since 3.0.0
     */
    @Nonnull
    public ParticleDisplay withColor(@Nonnull Color color, float size) {
        return withColor(color.getRed(), color.getGreen(), color.getBlue(), size);
    }

    /**
     * Adds color properties to the particle settings.
     * The particle must be {@link Particle#DUST_COLOR_TRANSITION}
     * to get custom colors.
     *
     * @param color1 the RGB color of the particle on spawn.
     * @param size  the size of the particle.
     * @param color2 the RGB color of the particle at the end.
     *
     * @return the same particle display, but modified.
     * @see #colored(Location, Color, float)
     * @since 8.6.0.0.1
     */
    @Nonnull
    public ParticleDisplay withTransitionColor(@Nonnull Color color1, float size, @Nonnull Color color2) {
        return withTransitionColor(color1.getRed(), color1.getGreen(), color1.getBlue(), size, color2.getRed(), color2.getGreen(), color2.getBlue());
    }

    /**
     * @since 7.1.0
     */
    @Nonnull
    public ParticleDisplay withColor(float red, float green, float blue, float size) {
        this.data = new float[]{red, green, blue, size};
        return this;
    }

    /**
     * @since 8.6.0.0.1
     */
    @Nonnull
    public ParticleDisplay withTransitionColor(float red1, float green1, float blue1, float size, float red2, float green2, float blue2) {
        this.data = new float[]{red1, green1, blue1, size, red2, green2, blue2};
        return this;
    }

    /**
     * Adds data for {@link Particle#BLOCK_CRACK}, {@link Particle#BLOCK_DUST},
     * {@link Particle#FALLING_DUST} and {@link Particle#BLOCK_MARKER} particles.
     * The displayed particle will depend on the given block data for its color.
     * <p>
     * Only works on minecraft version 1.13 and more, because
     * {@link BlockData} didn't exist before.
     *
     * @param blockData the block data that will change the particle data.
     *
     * @return the same particle display, but modified.
     * @since 5.1.0
     */
    @Nonnull
    public ParticleDisplay withBlock(@Nonnull BlockData blockData) {
        this.data = blockData;
        return this;
    }

    /**
     * Adds data for {@link Particle#LEGACY_BLOCK_CRACK}, {@link Particle#LEGACY_BLOCK_DUST}
     * and {@link Particle#LEGACY_FALLING_DUST} particles if the minecraft version is 1.13 or more.
     * <p>
     * If version is at most 1.12, old particles {@link Particle#BLOCK_CRACK},
     * {@link Particle#BLOCK_DUST} and {@link Particle#FALLING_DUST} will support this data.
     *
     * @param materialData the material data that will change the particle data.
     *
     * @return the same particle display, but modified.
     * @see #withBlock(BlockData)
     * @since 5.1.0
     */
    @SuppressWarnings("deprecation")
    @Nonnull
    public ParticleDisplay withBlock(@Nonnull MaterialData materialData) {
        this.data = materialData;
        return this;
    }

    /**
     * Adds extra data for {@link Particle#ITEM_CRACK}
     * particle, depending on the given item stack.
     *
     * @param item the item stack that will change the particle data.
     *
     * @return the same particle display, but modified.
     * @since 5.1.0
     */
    @Nonnull
    public ParticleDisplay withItem(@Nonnull ItemStack item) {
        this.data = item;
        return this;
    }

    @Nullable
    public Vector getOffset() {
        return offset;
    }

    /**
     * Saves an instance of an entity to track the location from.
     *
     * @param entity the entity to track the location from.
     *
     * @return the same particle settings with the caller added.
     * @since 3.1.0
     */
    @Nonnull
    public ParticleDisplay withEntity(@Nonnull Entity entity) {
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
     * Sets the rotation order that the particles should be rotated.
     * Yes,it matters which axis you rotate first as it'll have an impact on the
     * other rotations.
     *
     * @since 7.0.0
     */
    public ParticleDisplay rotationOrder(@Nonnull Axis first, @Nonnull Axis second, @Nonnull Axis third) {
        Objects.requireNonNull(first, "First rotation order axis is null");
        Objects.requireNonNull(second, "Second rotation order axis is null");
        Objects.requireNonNull(third, "Third rotation order axis is null");

        this.rotationOrder = new Axis[]{first, second, third};
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
     * Sets the location that this particle should spawn.
     *
     * @param location the new location.
     *
     * @since 7.0.0
     */
    public ParticleDisplay withLocation(@Nullable Location location) {
        this.location = location;
        return this;
    }

    /**
     * Adjusts the rotation settings to face the entity's direction.
     * Only some of the shapes support this method.
     *
     * @param entity the entity to face.
     *
     * @return the same particle display.
     * @see #rotate(Vector)
     * @since 3.0.0
     */
    @Nonnull
    public ParticleDisplay face(@Nonnull Entity entity) {
        return face(Objects.requireNonNull(entity, "Cannot face null entity").getLocation());
    }

    /**
     * Adjusts the rotation settings to face the locations pitch and yaw.
     * Only some of the shapes support this method.
     *
     * @param location the location to face.
     *
     * @return the same particle display.
     * @see #rotate(Vector)
     * @since 6.1.0
     */
    @Nonnull
    public ParticleDisplay face(@Nonnull Location location) {
        Objects.requireNonNull(location, "Cannot face null location");
        // We add 90 degrees to compensate for the non-standard use of pitch degrees in Minecraft.
        this.rotation = new Vector(Math.toRadians(location.getPitch() + 90), Math.toRadians(-location.getYaw()), 0);
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
        ParticleDisplay display = ParticleDisplay.of(particle)
                .withLocationCaller(locationCaller)
                .withCount(count).offset(offset.clone())
                .forceSpawn(force).onSpawn(onSpawn);

        if (location != null) display.location = cloneLocation(location);
        if (rotation != null) display.rotation = this.rotation.clone();
        display.rotationOrder = this.rotationOrder;
        display.data = data;
        return display;
    }

    /**
     * Rotates the particle position based on this vector.
     *
     * @param vector the vector to rotate from. The xyz values of this vector must be radians.
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
        return offset(new Vector(x, y, z));
    }

    /**
     * Set the xyz offset of the particle settings.
     *
     * @since 7.0.0
     */
    @Nonnull
    public ParticleDisplay offset(@Nonnull Vector offset) {
        this.offset = Objects.requireNonNull(offset, "Particle offset cannot be null");
        return this;
    }

    /**
     * Gets the rotation vector of this particle once spawned.
     *
     * @return a rotation that will be applied.
     * @since 6.1.0
     */
    @Nullable
    public Vector getRotation() {
        return rotation;
    }

    /**
     * Sets a new rotation vector ignoring previous ones.
     *
     * @param rotation the new rotation.
     *
     * @since 7.0.0
     */
    public void setRotation(@Nullable Vector rotation) {
        this.rotation = rotation;
    }

    /**
     * Set the xyz offset of the particle settings to a single number.
     *
     * @since 6.0.0.1
     */
    @Nonnull
    public ParticleDisplay offset(double offset) {
        return offset(offset, offset, offset);
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
     * Adds xyz to the cloned location before spawning particle.
     *
     * @since 1.0.0
     */
    @Nonnull
    public Location spawn(double x, double y, double z) {
        return spawn(rotate(getLocation(), x, y, z));
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
     * @param players if this particle should only be sent to specific players. Shouldn't be empty.
     *
     * @see #spawn(double, double, double)
     * @since 5.0.0
     */
    @Nonnull
    public Location spawn(Location loc, @Nullable Player... players) {
        if (loc == null) throw new IllegalStateException("Attempting to spawn particle when no location is set");
        if (onSpawn != null) {
            if (!onSpawn.test(loc)) return loc;
        }

        World world = loc.getWorld();
        double offsetx = offset.getX();
        double offsety = offset.getY();
        double offsetz = offset.getZ();

        if (data != null && data instanceof float[]) {
            float[] datas = (float[]) data;
            if (ISFLAT && particle.getDataType() == Particle.DustOptions.class) {
                Particle.DustOptions dust = new Particle.DustOptions(org.bukkit.Color
                        .fromRGB((int) datas[0], (int) datas[1], (int) datas[2]), datas[3]);
                if (players == null) world.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, dust, force);
                else for (Player player : players) player.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, dust);

            } else if (ISFLAT2 && particle.getDataType() == Particle.DustTransition.class) {
                Particle.DustOptions dust = new Particle.DustTransition(
                        org.bukkit.Color.fromRGB((int) datas[0], (int) datas[1], (int) datas[2]),
                        org.bukkit.Color.fromRGB((int) datas[4], (int) datas[5], (int) datas[6]),
                        datas[3]);
                if (players == null) world.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, dust, force);
                else for (Player player : players) player.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, dust);

            } else if (isDirectional()) {
                // With count=0, color on offset e.g. for MOB_SPELL or 1.12 REDSTONE
                float[] rgb = {datas[0] / 255f, datas[1] / 255f, datas[2] / 255f};
                if (players == null) {
                    if (ISFLAT) world.spawnParticle(particle, loc, count, rgb[0], rgb[1], rgb[2], datas[3], null, force);
                    else world.spawnParticle(particle, loc, count, rgb[0], rgb[1], rgb[2], datas[3], null);
                } else for (Player player : players) player.spawnParticle(particle, loc, count, rgb[0], rgb[1], rgb[2], datas[3]);

            } else {
                // Else color can't have any effect, keep default param
                if (players == null) {
                    if (ISFLAT) world.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, null, force);
                    else world.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, null);
                } else for (Player player : players) player.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra);
            }
        } else {
            // Checks without data or block crack, block dust, falling dust, item crack or if data isn't right type
            Object datas = particle.getDataType().isInstance(data) ? data : null;
            if (players == null) {
                if (ISFLAT) world.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, datas, force);
                else world.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, datas);
            } else for (Player player : players) player.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, datas);
        }

        return loc;
    }

    /**
     * As an alternative to {@link org.bukkit.Axis} because it doesn't exist in 1.12
     *
     * @since 7.0.0
     */
    public enum Axis {X, Y, Z}
}