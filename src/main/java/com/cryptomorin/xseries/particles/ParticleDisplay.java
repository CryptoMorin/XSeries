/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Crypto Morin
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
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents how particles should be spawned. The simplest use case would be the following code
 * which spawns a single particle in front of the player:
 * <pre>{@code
 * ParticleDisplay.of(Particle.FLAME).spawn(player.getEyeLocation());
 * }</pre>
 * This class is disposable by {@link XParticle} methods.
 * It should not be used across multiple methods. I.e. it should not be
 * used even to spawn a simple particle after it was used by one of {@link XParticle} methods.
 * <p>
 * By default, the particle xyz offsets and speed aren't 0, but
 * everything will be 0 by default in this class.
 * Particles are spawned to a location. So all the nearby players can see it.
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
 * @version 10.0.0
 * @see XParticle
 */
@SuppressWarnings("CallToSimpleGetterFromWithinClass")
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
     * which is only available from 1.17+ (DUST_COLOR_TRANSITION was released in 1.17)
     *
     * @since 8.6.0.0.1
     */
    private static final boolean SUPPORTS_DUST_TRANSITION = XParticle.getParticle("DUST_COLOR_TRANSITION") != null;
    // private static final Axis[] DEFAULT_ROTATION_ORDER = {Axis.X, Axis.Y, Axis.Z};
    /**
     * Flames seem to be the simplest particles that allows you to get a good visual
     * on how precise shapes that depend on complex algorithms play out.
     */
    @Nonnull
    private static final Particle DEFAULT_PARTICLE = Particle.FLAME;

    public int count = 1;
    public double extra;
    public boolean force;
    @Nonnull
    private Particle particle = DEFAULT_PARTICLE;
    @Nullable
    private Location location, lastLocation;
    @Nullable
    private Vector offset = new Vector();
    /**
     * The direction is mostly used for APIs to call {@link #advanceInDirection(double)}
     * instead of handling the direction in a specific axis.
     * This makes it easier for them as well and allows easier use of the {@link #rotations} API.
     */
    @Nonnull
    private Vector direction = new Vector(0, 1, 0);
    /**
     * The xyz axis order of how the particle's matrix should be rotated.
     * Yes, it matters which axis you rotate first as it'll have an impact on the
     * other rotations.
     * <p>
     * Check <a href="https://stackoverflow.com/questions/11819644/pitch-yaw-roll-angle-independency">this stackoverflow question.</a>
     * Quaternions are a solution to this problem which is already present in {@link org.bukkit.entity.Display} entities API.
     * See <a href="https://www.youtube.com/watch?v=zjMuIxRvygQ">this 3Blue1Brown YouTube video.</a>
     * <p>
     * You could use an axis two times such as yaw -> roll -> yaw sequence which is the canonical Euler sequence.
     * But here for the standard {@link XParticle} methods, we're going to be using Tait–Bryan angles.
     * Minecraft Euler angles use XYZ order.
     * <a href="https://www.spigotmc.org/threads/euler-angles-strange-behavior.377072/">Source</a>
     * <a href="https://www.youtube.com/watch?v=zc8b2Jo7mno">Gimbal lock</a>.
     * <p>
     * For 2D shapes, it's recommended that your algorithm uses the x and z axis and leave y as 0.
     * <p>
     * Each list within the main list represents the rotations that are going to be applied individually in order.
     * While it's true that in order to combine multiple quaternion rotations you'd have to multiply them,
     * quaternion multiplication is not commutative and some rotations should be done separately.
     */
    @Nonnull
    public List<List<Rotation>> rotations = new ArrayList<>();
    @Nullable
    private List<Quaternion> cachedFinalRotationQuaternions;
    @Nullable
    private Object data;
    @Nullable
    private Consumer<CalculationContext> preCalculation;
    @Nullable
    private Consumer<CalculationContext> postCalculation;
    @Nullable
    private Function<Double, Double> onAdvance;
    @Nullable
    private Set<Player> players;

    /**
     * Builds a simple ParticleDisplay object with cross-version
     * compatible {@link org.bukkit.Particle.DustOptions} properties.
     * Only REDSTONE particle type can be colored like this.
     *
     * @param location the location of the display.
     * @param size     the size of the dust.
     * @return a redstone colored dust.
     * @see #simple(Location, Particle)
     * @since 1.0.0
     * @deprecated use {@link #withColor(float, float, float, float)}
     */
    @Nonnull
    @Deprecated
    public static ParticleDisplay colored(@Nullable Location location, int r, int g, int b, float size) {
        return ParticleDisplay.simple(location, Particle.REDSTONE).withColor(r, g, b, size);
    }

    /**
     * @return the players that this particle will be visible to or null if it's visible to all.
     * @since 9.0.0
     */
    @Nullable
    public Set<Player> getPlayers() {
        return players;
    }

    /**
     * Makes this particle only visible to certain players.
     *
     * @since 9.0.0
     */
    public ParticleDisplay onlyVisibleTo(Collection<Player> players) {
        if (players.isEmpty()) return this;
        if (this.players == null) this.players = Collections.newSetFromMap(new WeakHashMap<>());
        this.players.addAll(players);
        return this;
    }

    /**
     * @see #onlyVisibleTo(Collection)
     * @since 9.0.0
     */
    public ParticleDisplay onlyVisibleTo(Player... players) {
        if (players.length == 0) return this;
        if (this.players == null) this.players = Collections.newSetFromMap(new WeakHashMap<>());
        Collections.addAll(this.players, players);
        return this;
    }

    /**
     * Builds a simple ParticleDisplay object with cross-version
     * compatible {@link org.bukkit.Particle.DustOptions} properties.
     * Only REDSTONE particle type can be colored like this.
     *
     * @param color the color of the particle.
     * @param size  the size of the dust.
     * @return a redstone colored dust.
     * @since 3.0.0
     * @deprecated use {@link #withColor(Color, float)}
     */
    @Nonnull
    @Deprecated
    public static ParticleDisplay colored(Location location, @Nonnull Color color, float size) {
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
     * @return a simple ParticleDisplay with count 1 and no offset, rotation etc.
     * @since 1.0.0
     * @deprecated use {@link #of(Particle)} and {@link #withLocation(Location)}
     */
    @Nonnull
    @Deprecated
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
     * @return a simple ParticleDisplay with count 1 and no offset, rotation etc.
     * @since 1.0.0
     * @deprecated use {@link #of(Particle)} and {@link #withLocation(Location)}
     */
    @Nullable
    @Deprecated
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
     * @return a parsed ParticleDisplay from the config.
     * @since 1.0.0
     */
    public static ParticleDisplay fromConfig(@Nonnull ConfigurationSection config) {
        return edit(new ParticleDisplay(), config);
    }

    private static int toInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    private static double toDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    private static java.util.List<String> split(@Nonnull String str, @SuppressWarnings("SameParameterValue") char separatorChar) {
        List<String> list = new ArrayList<>(5);
        boolean match = false, lastMatch = false;
        int len = str.length();
        int start = 0;

        for (int i = 0; i < len; i++) {
            if (str.charAt(i) == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }

                // This is important, it should not be i++
                start = i + 1;
                continue;
            }

            lastMatch = false;
            match = true;
        }

        if (match || lastMatch) {
            list.add(str.substring(start, len));
        }
        return list;
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
            List<String> offsets = split(offset.replace(" ", ""), ',');
            if (offsets.size() >= 3) {
                double offsetx = toDouble(offsets.get(0));
                double offsety = toDouble(offsets.get(1));
                double offsetz = toDouble(offsets.get(2));
                display.offset(offsetx, offsety, offsetz);
            } else {
                double masterOffset = toDouble(offsets.get(0));
                display.offset(masterOffset);
            }
        }

        ConfigurationSection rotations = config.getConfigurationSection("rotations");
        if (rotations != null) {
            /*
            rotations:
              group-1:
                0:
                  angle: 3.14
                  axis: "Y"
                1:
                  angle: 4
                  axis: "3, 5, 3.4"
              group-2:
                0:
                  angle: 1.6
                  axis: "6, 4, 2"
             */

            for (String rotationGroupName : rotations.getKeys(false)) {
                ConfigurationSection rotationGroup = rotations.getConfigurationSection(rotationGroupName);

                List<Rotation> grouped = new ArrayList<>();
                for (String rotationName : rotationGroup.getKeys(false)) {
                    ConfigurationSection rotation = rotationGroup.getConfigurationSection(rotationName);
                    double angle = rotation.getDouble("angle");
                    Vector axis;

                    String axisStr = rotation.getString("vector").toUpperCase(Locale.ENGLISH).replace(" ", "");
                    if (axisStr.length() == 1) {
                        axis = Axis.valueOf(axisStr).vector;
                    } else {
                        String[] split = axisStr.split(",");
                        axis = new Vector(Math.toRadians(Double.parseDouble(split[0])),
                                Math.toRadians(Double.parseDouble(split[1])),
                                Math.toRadians(Double.parseDouble(split[2])));
                    }

                    grouped.add(Rotation.of(angle, axis));
                }
                display.rotations.add(grouped);
            }
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
            List<String> colors = split(color.replace(" ", ""), ',');
            if (colors.size() <= 3 || colors.size() == 6) { // 1 or 3 : single color, 2 or 6 : two colors for DUST_TRANSITION
                Color parsedColor1 = Color.white;
                Color parsedColor2 = null;
                if (colors.size() <= 2) {
                    try {
                        parsedColor1 = Color.decode(colors.get(0));
                        if (colors.size() == 2)
                            parsedColor2 = Color.decode(colors.get(1));
                    } catch (NumberFormatException ex) {
                        /* I don't think it's worth it.
                        try {
                            parsedColor = (Color) Color.class.getField(colors[0].toUpperCase(Locale.ENGLISH)).get(null);
                        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ignored) { }
                         */
                    }
                } else {
                    parsedColor1 = new Color(toInt(colors.get(0)), toInt(colors.get(1)), toInt(colors.get(2)));
                    if (colors.size() == 6)
                        parsedColor2 = new Color(toInt(colors.get(3)), toInt(colors.get(4)), toInt(colors.get(5)));
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
     * Serialize a ParticleDisplay into a ConfigurationSection
     *
     * @param display The ParticleDisplay to serialize
     * @param section The ConfigurationSection to serialize into
     */
    @SuppressWarnings("deprecation")
    public static void serialize(ParticleDisplay display, ConfigurationSection section) {
        section.set("particle", display.particle.name());

        if (display.count != 1) {
            section.set("count", display.count);
        }

        if (display.extra != 0) {
            section.set("extra", display.extra);
        }

        if (display.force) {
            section.set("force", true);
        }

        if (display.offset != null) {
            Vector offset = display.offset;
            section.set("offset", offset.getX() + ", " + offset.getY() + ", " + offset.getZ());
        }

        if (!display.rotations.isEmpty()) {
            ConfigurationSection rotations = section.createSection("rotations");

            int index = 1;
            for (List<Rotation> rotationGroup : display.rotations) {
                ConfigurationSection rotationGroupSection = rotations.createSection("group-" + index++);

                int groupIndex = 1;
                for (Rotation rotation : rotationGroup) {
                    ConfigurationSection rotationSection = rotationGroupSection.createSection(
                            String.valueOf(groupIndex++));

                    rotationSection.set("angle", rotation.angle);
                    Vector axis = rotation.axis;
                    Optional<Axis> mainAxis = Arrays.stream(Axis.values())
                            .filter(x -> x.vector.equals(axis))
                            .findFirst();

                    if (mainAxis.isPresent()) {
                        rotationSection.set("axis", mainAxis.get().name());
                    } else {
                        rotationSection.set("axis", axis.getX() + ", " + axis.getY() + ", " + axis.getZ());
                    }
                }
            }
        }

        if (display.data instanceof float[]) {
            float size = 1f;
            float[] datas = (float[]) display.data;
            StringJoiner colorJoiner = new StringJoiner(", ");
            if (datas.length >= 3) {
                if (datas.length > 3) {
                    size = datas[3];
                }
                Color color1 = new Color(datas[0], datas[1], datas[2]);
                colorJoiner.add(Integer.toString(color1.getRed()));
                colorJoiner.add(Integer.toString(color1.getGreen()));
                colorJoiner.add(Integer.toString(color1.getBlue()));
            }
            if (datas.length >= 7) {
                Color color2 = new Color(datas[4], datas[5], datas[6]);
                colorJoiner.add(Integer.toString(color2.getRed()));
                colorJoiner.add(Integer.toString(color2.getGreen()));
                colorJoiner.add(Integer.toString(color2.getBlue()));
            }
            section.set("color", colorJoiner.toString());
            section.set("size", size);
        }

        if (ISFLAT) {
            if (display.data instanceof BlockData) {
                section.set("blockdata", ((BlockData) display.data).getMaterial().name());
            }
        }
        if (display.data instanceof ItemStack) {
            section.set("itemstack", ((ItemStack) display.data).getType().name());
        } else if (display.data instanceof MaterialData) {
            section.set("materialdata", ((MaterialData) display.data).getItemType().name());
        }
    }

    /**
     * Rotates the given location vector around a certain axis.
     *
     * @param location the location to rotate.
     * @param axis     the axis to rotate the location around.
     * @param rotation the rotation vector that contains the degrees of the rotation. The number is taken from this vector according to the given axis.
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
     * It simply uses the <a href="https://en.wikipedia.org/wiki/Rotation_matrix">rotation matrix</a>.
     *
     * @param location the location to rotate.
     * @param axis     the axis to rotate the location around.
     * @param angle    the rotation angle in radians.
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
     * Called before rotation is applied to the xyz spawn location. The xyz provided
     * in this method is implementation-specific, but it should be the xyz values that
     * are going to be {@link Location#add(Vector)} to {@link #getLocation()}.
     * <p>
     * The provided xyz local coordinates vector might be null.
     *
     * @return the same particle display.
     * @since 9.0.0
     */
    public ParticleDisplay preCalculation(@Nullable Consumer<CalculationContext> preCalculation) {
        this.preCalculation = preCalculation;
        return this;
    }

    /**
     * Called after rotation is applied to the xyz spawn location. This is the final
     * location that's going to spawn a single particle.
     * <p>
     * The provided xyz local coordinates vector might be null.
     *
     * @return the same particle display.
     * @since 10.0.0
     */
    public ParticleDisplay postCalculation(@Nullable Consumer<CalculationContext> postCalculation) {
        this.postCalculation = postCalculation;
        return this;
    }

    /**
     * Called when {@link #advanceInDirection(double)} is called.
     *
     * @param onAdvance The argument and the return values are the amount of blocks to advance.
     * @return the same particle display.
     * @since 9.0.0
     */
    public ParticleDisplay onAdvance(@Nullable Function<Double, Double> onAdvance) {
        this.onAdvance = onAdvance;
        return this;
    }

    /**
     * @since 7.0.0
     */
    public ParticleDisplay withParticle(@Nonnull Particle particle) {
        this.particle = Objects.requireNonNull(particle, "Particle cannot be null");
        return this;
    }

    /**
     * @see #direction
     * @since 8.0.0
     */
    @Nonnull
    public Vector getDirection() {
        return direction;
    }

    /**
     * Changes the current {@link #location} in {@link #direction} by {@code distance} blocks.
     *
     * @since 8.0.0
     */
    public void advanceInDirection(double distance) {
        Objects.requireNonNull(direction, "Cannot advance with null direction");
        if (distance == 0) return;
        if (this.onAdvance != null) distance = onAdvance.apply(distance);
        this.location.add(this.direction.clone().multiply(distance));
    }

    /**
     * @see #direction
     * @since 8.0.0
     */
    public ParticleDisplay withDirection(@Nullable Vector direction) {
        this.direction = direction.clone().normalize();
        return this;
    }

    /**
     * Get the particle.
     *
     * @return the particle.
     */
    @Nonnull
    public Particle getParticle() {
        return particle;
    }

    /**
     * Get the count of the particle.
     *
     * @return the count of the particle.
     */
    public int getCount() {
        return count;
    }

    /**
     * Get the extra data of the particle.
     *
     * @return the extra data of the particle.
     */
    public double getExtra() {
        return extra;
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
        return "ParticleDisplay:[" +
                "Particle=" + particle + ", " +
                "Count=" + count + ", " +
                "Offset:{" + offset.getX() + ", " + offset.getY() + ", " + offset.getZ() + "}, " +

                (location != null ? (
                        "Location:{" + location.getWorld().getName() + location.getX() + ", " + location.getY() + ", " + location.getZ() + "}, "
                ) : "") +

                "Rotation:" + this.rotations + ", " +

                "Extra=" + extra + ", " +
                "Force=" + force + ", " +
                "Data=" + (data == null ? "null" : data instanceof float[] ? Arrays.toString((float[]) data) : data);
    }

    /**
     * Changes the particle count of the particle settings.
     *
     * @param count the particle count.
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
     * are added with {@link #spawn(Location)}.
     *
     * @param force the force argument.
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
     * @return the same particle display, but modified.
     * @see #colored(Location, Color, float)
     * @since 3.0.0
     */
    @Nonnull
    public ParticleDisplay withColor(@Nonnull Color color, float size) {
        return withColor(color.getRed(), color.getGreen(), color.getBlue(), size);
    }

    /**
     * @since 7.1.0
     * @deprecated use {@link #withColor(Color, float)}
     */
    @Nonnull
    @Deprecated
    public ParticleDisplay withColor(float red, float green, float blue, float size) {
        this.data = new float[]{red, green, blue, size};
        return this;
    }

    /**
     * Adds color properties to the particle settings.
     * The particle must be {@link Particle#DUST_COLOR_TRANSITION}
     * to get custom colors.
     *
     * @param fromColor the RGB color of the particle on spawn.
     * @param size      the size of the particle.
     * @param toColor   the RGB color of the particle at the end.
     * @return the same particle display, but modified.
     * @see #colored(Location, Color, float)
     * @since 8.6.0.0.1
     */
    @Nonnull
    public ParticleDisplay withTransitionColor(@Nonnull Color fromColor, float size, @Nonnull Color toColor) {
        this.data = new float[]{
                fromColor.getRed(), fromColor.getGreen(), fromColor.getBlue(),
                size,
                toColor.getRed(), toColor.getGreen(), toColor.getBlue()
        };
        return this;
    }

    /**
     * @since 8.6.0.0.1
     * @deprecated use {@link #withTransitionColor(Color, float, Color)}
     */
    @Nonnull
    @Deprecated
    public ParticleDisplay withTransitionColor(float red1, float green1, float blue1,
                                               float size,
                                               float red2, float green2, float blue2) {
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
     * @return the same particle settings with the caller added.
     * @since 3.1.0
     */
    @Nonnull
    public ParticleDisplay withLocationCaller(@Nullable Callable<Location> locationCaller) {
        this.preCalculation = (loc) -> {
            try {
                this.location = locationCaller.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        return this;
    }

    /**
     * Gets the location of an entity if specified or the constant location.
     * <p>
     * This method is usually the center of the shape if the algorithm which uses
     * it supports the use of {@link #advanceInDirection(double)}.
     *
     * @return the location of the particle.
     * @since 3.1.0
     */
    @Nullable
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the location that this particle should spawn.
     *
     * @param location the new location.
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
     * @return the same particle display.
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
     * @return the same particle display.
     * @since 6.1.0
     */
    @Nonnull
    public ParticleDisplay face(@Nonnull Location location) {
        Objects.requireNonNull(location, "Cannot face null location");
        rotate(
                Rotation.of(Math.toRadians(location.getYaw()), Axis.Y),
                Rotation.of(Math.toRadians(-location.getPitch()), Axis.X)
        );
        this.direction = location.getDirection().clone().normalize();
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
        return location == null ? null : cloneLocation(location).add(x, y, z);
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
    @Nonnull
    public ParticleDisplay clone() {
        ParticleDisplay display = ParticleDisplay.of(particle)
                .withDirection(direction)
                .withCount(count).offset(offset.clone())
                .forceSpawn(force)
                .preCalculation(this.preCalculation)
                .postCalculation(this.postCalculation);

        if (location != null) display.location = cloneLocation(location);
        if (!rotations.isEmpty()) {
            display.rotations = new ArrayList<>(this.rotations);
        }
        display.data = data;
        return display;
    }

    /**
     * @see #getPrincipalAxesRotation(float, float, float)
     */
    public static Vector getPrincipalAxesRotation(Location location) {
        return getPrincipalAxesRotation(location.getPitch(), location.getYaw(), 0);
    }

    /**
     * Taken from <a href="https://en.wikipedia.org/wiki/Aircraft_principal_axes">Aircraft principal axes.</a>
     *
     * @return The vector representating how a point should be rotated to face these axes.
     * @since 8.1.0
     */
    public static Vector getPrincipalAxesRotation(float pitch, float yaw, float roll) {
        // First the pitch has to be rotated around the x-axis because if we were to rotate the yaw around
        // the y-axis first, the point could be facing either x or z axis and rotating the pitch around the
        // x-axis would no longer be viable. But when we start from zero rotations, the point would be facing
        // towards positive z-axis (why?) and rotating the pitch around the x-axis now works fine. After that,
        // rotating the yaw around the y-axis would always work no matter the pitch of the point.
        // https://danceswithcode.net/engineeringnotes/rotations_in_3d/rotations_in_3d_part2.html
        return new Vector(
                // We add 90 degrees to compensate for the non-standard use of pitch degrees in Minecraft.
                Math.toRadians(pitch + 90),
                Math.toRadians(-yaw),
                roll
        );
    }

    /**
     * Gets yaw and pitch from a given direction.
     *
     * @return the first element is the yaw and the second is the pitch.
     * @since 8.0.0
     */
    public static float[] getYawPitch(Vector vector) {
        /*
         * Sin = Opp / Hyp
         * Cos = Adj / Hyp
         * Tan = Opp / Adj
         *
         * x = -Opp
         * z = Adj
         */
        final double _2PI = 2 * Math.PI;
        final double x = vector.getX();
        final double z = vector.getZ();
        float pitch, yaw;

        if (x == 0 && z == 0) {
            yaw = 0;
            pitch = vector.getY() > 0 ? -90 : 90;
        } else {
            double theta = Math.atan2(-x, z);
            yaw = (float) Math.toDegrees((theta + _2PI) % _2PI);

            double x2 = NumberConversions.square(x);
            double z2 = NumberConversions.square(z);
            double xz = Math.sqrt(x2 + z2);
            pitch = (float) Math.toDegrees(Math.atan(-vector.getY() / xz));
        }

        return new float[]{yaw, pitch};
    }

    /**
     * Gets the final calculated rotation.
     *
     * @param forceUpdate whether to update the cached rotation quaternion.
     *                    Used when a new rotation is added.
     * @since 9.0.0
     */
    @Nonnull
    public List<Quaternion> getRotation(boolean forceUpdate) {
        if (this.rotations.isEmpty()) return new ArrayList<>();
        if (forceUpdate) cachedFinalRotationQuaternions = null;
        if (cachedFinalRotationQuaternions == null) {
            this.cachedFinalRotationQuaternions = new ArrayList<>();

            for (List<Rotation> rotationGroup : this.rotations) {
                Quaternion groupedQuat = null;
                for (Rotation rotation : rotationGroup) {
                    Quaternion q = Quaternion.rotation(rotation.angle, rotation.axis);
                    if (groupedQuat == null) groupedQuat = q;
                    else groupedQuat = groupedQuat.mul(q);
                }
                this.cachedFinalRotationQuaternions.add(groupedQuat);
            }
        }

        return cachedFinalRotationQuaternions;
    }

    /**
     * Rotates the particle position based on this XYZ vector without overriding previous rotations.
     * The xyz values must be <b>radians</b> which represent the angles
     * to rotate the particle around x, y and then z axis in that order.
     *
     * @see #rotate(double, double, double)
     * @since 1.0.0
     */
    @Nonnull
    public ParticleDisplay rotate(double x, double y, double z) {
        return rotate(
                Rotation.of(x, Axis.X),
                Rotation.of(y, Axis.Y),
                Rotation.of(z, Axis.Z)
        );
    }

    /**
     * @since 10.0.0
     */
    public ParticleDisplay rotate(Rotation... rotations) {
        Objects.requireNonNull(rotations, "Null rotations");

        if (rotations.length != 0) {
            List<Rotation> finalRots = Arrays.stream(rotations).filter(x -> x.angle != 0).collect(Collectors.toList());
            if (!finalRots.isEmpty()) {
                this.rotations.add(finalRots);
                if (this.cachedFinalRotationQuaternions != null) this.cachedFinalRotationQuaternions.clear();
            }
        }

        return this;
    }

    /**
     * @since 10.0.0
     */
    public ParticleDisplay rotate(Rotation rotation) {
        Objects.requireNonNull(rotation, "Null rotation");
        if (rotation.angle != 0) {
            this.rotations.add(Collections.singletonList(rotation));
            if (this.cachedFinalRotationQuaternions != null) this.cachedFinalRotationQuaternions.clear();
        }

        return this;
    }

    /**
     * @return the location of the last particle spawned with this object.
     * @since 8.0.0
     */
    @Nullable
    public Location getLastLocation() {
        return lastLocation == null ? getLocation() : lastLocation;
    }

    /**
     * Runs {@link #preCalculation}, rotates the given xyz with the given rotation radians and
     * adds them to the specified location, and then calls {@link #postCalculation}.
     *
     * @return a cloned rotated location.
     * @since 3.0.0
     */
    @Nullable
    public Location finalizeLocation(@Nullable Vector local) {
        CalculationContext preContext = new CalculationContext(location, local);

        if (this.preCalculation != null) this.preCalculation.accept(preContext);
        if (!preContext.shouldSpawn) return null;

        local = preContext.local;
        Location location = preContext.location;

        if (local != null && !rotations.isEmpty()) {
            List<Quaternion> rotations = getRotation(false);
            for (Quaternion grouped : rotations) {
                local = Quaternion.rotate(local, grouped);
            }
        }

        if (location == null) throw new IllegalStateException("Attempting to spawn particle when no location is set");
        // Exception check after onCalculation to account for dynamic location callers from withEntity()


        location = cloneLocation(location);
        if (local != null) location.add(local);

        CalculationContext postContext = new CalculationContext(location, local);
        if (this.postCalculation != null) this.postCalculation.accept(postContext);
        if (!postContext.shouldSpawn) return null;

        return location;
    }

    public final class CalculationContext {
        private Location location;
        private Vector local;
        private boolean shouldSpawn = true;

        public CalculationContext(Location location, Vector local) {
            this.location = location;
            this.local = local;
        }

        @Nullable
        public Location getLocation() {
            return location;
        }

        @Nullable
        public Vector getLocal() {
            return local;
        }

        public void setLocal(Vector local) {
            this.local = local;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public void dontSpawn() {
            this.shouldSpawn = false;
        }

        public ParticleDisplay getDisplay() {
            return ParticleDisplay.this;
        }
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
    @Nullable
    public Location spawn() {
        return spawn(finalizeLocation(null));
    }

    /**
     * Adds xyz of the given vector to the cloned location before
     * spawning particles.
     *
     * @param local the xyz to add.
     * @since 1.0.0
     */
    @Nullable
    public Location spawn(@Nullable Vector local) {
        return spawn(finalizeLocation(local));
    }

    /**
     * Adds xyz to the cloned location before spawning particle.
     *
     * @since 1.0.0
     */
    @Nullable
    public Location spawn(double x, double y, double z) {
        return spawn(finalizeLocation(new Vector(x, y, z)));
    }

    /**
     * Displays the particle in the specified location.
     * This method does not support rotations if used directly.
     *
     * @param loc the location to display the particle at.
     * @see #spawn(double, double, double)
     * @since 5.0.0
     */
    @Nullable
    public Location spawn(Location loc) {
        if (loc == null) return null;

        World world = loc.getWorld();
        double offsetx = offset.getX();
        double offsety = offset.getY();
        double offsetz = offset.getZ();

        if (data != null && data instanceof float[]) {
            float[] datas = (float[]) data;
            if (ISFLAT && particle.getDataType() == Particle.DustOptions.class) {
                Particle.DustOptions dust = new Particle.DustOptions(org.bukkit.Color
                        .fromRGB((int) datas[0], (int) datas[1], (int) datas[2]), datas[3]);
                if (players == null)
                    world.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, dust, force);
                else for (Player player : players)
                    player.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, dust);
            } else if (SUPPORTS_DUST_TRANSITION && particle.getDataType() == Particle.DustTransition.class) {
                // Having the variable type as Particle.DustOptions causes NoClassDefFoundError for DustOptions
                // because of some weird upcasting stuff.
                Particle.DustTransition dust = new Particle.DustTransition(
                        org.bukkit.Color.fromRGB((int) datas[0], (int) datas[1], (int) datas[2]),
                        org.bukkit.Color.fromRGB((int) datas[4], (int) datas[5], (int) datas[6]),
                        datas[3]);
                if (players == null)
                    world.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, dust, force);
                else for (Player player : players)
                    player.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, dust);
            } else if (isDirectional()) {
                // With count=0, color on offset e.g. for MOB_SPELL or 1.12 REDSTONE
                float[] rgb = {datas[0] / 255f, datas[1] / 255f, datas[2] / 255f};
                if (players == null) {
                    if (ISFLAT)
                        world.spawnParticle(particle, loc, count, rgb[0], rgb[1], rgb[2], datas[3], null, force);
                    else world.spawnParticle(particle, loc, count, rgb[0], rgb[1], rgb[2], datas[3], null);
                } else for (Player player : players)
                    player.spawnParticle(particle, loc, count, rgb[0], rgb[1], rgb[2], datas[3]);

            } else {
                // Else color can't have any effect, keep default param
                if (players == null) {
                    if (ISFLAT)
                        world.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, null, force);
                    else world.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, null);
                } else for (Player player : players)
                    player.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra);
            }
        } else {
            // Checks without data or block crack, block dust, falling dust, item crack or if data isn't right type
            Object datas = particle.getDataType().isInstance(data) ? data : null;
            if (players == null) {
                if (ISFLAT) world.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, datas, force);
                else world.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, datas);
            } else for (Player player : players)
                player.spawnParticle(particle, loc, count, offsetx, offsety, offsetz, extra, datas);
        }

        this.lastLocation = loc;
        return loc;
    }

    /**
     * As an alternative to {@link org.bukkit.Axis} because it doesn't exist in 1.12
     *
     * @since 7.0.0
     */
    public enum Axis {
        X(new Vector(1, 0, 0)), Y(new Vector(0, 1, 0)), Z(new Vector(0, 0, 1));

        private final Vector vector;

        Axis(Vector vector) {
            this.vector = vector;
        }

        public Vector getVector() {
            return vector;
        }
    }

    public static class Rotation implements Cloneable {
        public double angle;
        public Vector axis;

        public Rotation(double angle, Vector axis) {
            this.angle = angle;
            this.axis = axis;
        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        public Object clone() {
            return new Rotation(angle, axis.clone());
        }

        public static Rotation of(double angle, Vector axis) {
            return new Rotation(angle, axis);
        }

        public static Rotation of(double angle, Axis axis) {
            return new Rotation(angle, axis.vector);
        }
    }

    public static class Quaternion implements Cloneable {
        /**
         * Only change these values directly if you know what you're doing.
         */
        public final double w, x, y, z;

        public Quaternion(double w, double x, double y, double z) {
            this.w = w;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        public Quaternion clone() {
            return new Quaternion(w, x, y, z);
        }

        // Rotate a vector using a rotation quaternion.
        public static Vector rotate(Vector vector, Quaternion rotation) {
            return rotation.mul(Quaternion.from(vector)).mul(rotation.inverse()).toVector();
        }

        // Rotate a vector theta degrees around an axis.
        public static Vector rotate(Vector vector, Vector axis, double deg) {
            return Quaternion.rotate(vector, Quaternion.rotation(deg, axis));
        }

        // Create quaternion from a vector.
        public static Quaternion from(Vector vector) {
            return new Quaternion(0, vector.getX(), vector.getY(), vector.getZ());
        }

        public static Quaternion rotation(double degrees, Vector vector) {
            vector = vector.normalize();
            degrees = degrees / 2;
            double sin = Math.sin(degrees);
            return new Quaternion(Math.cos(degrees), vector.getX() * sin, vector.getY() * sin, vector.getZ() * sin);
        }

        public String getInverseString() {
            double rads = Math.acos(this.w);
            double deg = Math.toDegrees(rads) * 2;
            double sin = Math.sin(rads);
            Vector axis = new Vector(this.x / sin, this.y / sin, this.z / sin);

            return deg + ", " + axis.getX() + ", " + axis.getY() + ", " + axis.getZ();
        }

        public Vector toVector() {
            return new Vector(x, y, z);
        }

        public Quaternion inverse() {
            double l = w * w + x * x + y * y + z * z;
            return new Quaternion(w / l, -x / l, -y / l, -z / l);
        }

        public Quaternion conjugate() {
            return new Quaternion(w, -x, -y, -z);
        }

        // Multiply this quaternion and another.
        // Returns the Hamilton product of this quaternion and r.
        public Quaternion mul(Quaternion r) {
            double n0 = r.w * w - r.x * x - r.y * y - r.z * z;
            double n1 = r.w * x + r.x * w + r.y * z - r.z * y;
            double n2 = r.w * y - r.x * z + r.y * w + r.z * x;
            double n3 = r.w * z + r.x * y - r.y * x + r.z * w;
            return new Quaternion(n0, n1, n2, n3);
        }

        public Vector mul(Vector point) {
            // https://github.com/Unity-Technologies/UnityCsReference/blob/7c95a72366b5ed9b6d9e804de8b5e869c962f5a9/Runtime/Export/Math/Quaternion.cs#L96-L117
            double x = this.x * 2;
            double y = this.y * 2;
            double z = this.z * 2;
            double xx = this.x * x;
            double yy = this.y * y;
            double zz = this.z * z;
            double xy = this.x * y;
            double xz = this.x * z;
            double yz = this.y * z;
            double wx = this.w * x;
            double wy = this.w * y;
            double wz = this.w * z;

            double vx = (1F - (yy + zz)) * point.getX() + (xy - wz) * point.getY() + (xz + wy) * point.getZ();
            double vy = (xy + wz) * point.getX() + (1F - (xx + zz)) * point.getY() + (yz - wx) * point.getZ();
            double vz = (xz - wy) * point.getX() + (yz + wx) * point.getY() + (1F - (xx + yy)) * point.getZ();

            return new Vector(vx, vy, vz);
        }
    }
}