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

import org.bukkit.*;
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
import java.awt.Color;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents how particles should be spawned. The simplest use case would be the following code
 * which spawns a single particle in front of the player:
 * <pre>{@code
 * ParticleDisplay.of(Particle.FLAME).spawn(player.getEyeLocation());
 * }</pre>
 * This class is disposable by {@link Particles} methods.
 * It should not be used across multiple methods. I.e. it should not be
 * used even to spawn a simple particle after it was used by one of {@link Particles} methods.
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
 * @author Crypto Morin, cricri211, datatags
 * @version 12.0.0
 * @see Particles
 */
@SuppressWarnings("CallToSimpleGetterFromWithinClass")
public class ParticleDisplay implements Cloneable {
    /**
     * Checks if spawn methods should use particle data classes such as {@link org.bukkit.Particle.DustOptions}
     * which is only available from 1.13+ (FOOTSTEP was removed in 1.13)
     *
     * @since 1.0.0
     */
    private static final boolean ISFLAT;

    /**
     * Checks if org.bukkit.Color supports colors with an alpha value.
     * This was added in 1.19.4
     *
     * @since 11.0.0
     */
    private static final boolean SUPPORTS_ALPHA_COLORS;

    static {
        boolean isFlat;
        try {
            World.class.getDeclaredMethod("spawnParticle", Particle.class, Location.class, int.class,
                    double.class, double.class, double.class,
                    double.class, Object.class, boolean.class
            );
            isFlat = true;
        } catch (NoSuchMethodException e) {
            isFlat = false;
        }
        ISFLAT = isFlat;

        boolean supportsAlphaColors;
        try {
            org.bukkit.Color.fromARGB(0);
            supportsAlphaColors = true;
        } catch (NoSuchMethodError e) {
            supportsAlphaColors = false;
        }
        SUPPORTS_ALPHA_COLORS = supportsAlphaColors;
    }

    /**
     * The possible colors for note particles.
     * See: <a href="https://minecraft.wiki/w/Note_Block#Notes">Minecraft wiki</a>
     */
    public static final Color[] NOTE_COLORS = {
            new Color(0x77D700),
            new Color(0x95C000),
            new Color(0xB2A500),
            new Color(0xCC8600),
            new Color(0xE26500),
            new Color(0xF34100),
            new Color(0xFC1E00),
            new Color(0xFE000F),
            new Color(0xF70033),
            new Color(0xE8005A),
            new Color(0xCF0083),
            new Color(0xAE00A9),
            new Color(0x8600CC),
            new Color(0x5B00E7),
            new Color(0x2D00F9),
            new Color(0x020AFE),
            new Color(0x0037F6),
            new Color(0x0068E0),
            new Color(0x009ABC),
            new Color(0x00C68D),
            new Color(0x00E958),
            new Color(0x00FC21),
            new Color(0x1FFC00),
            new Color(0x59E800),
            new Color(0x94C100),
    };

    /**
     * Flames seem to be the simplest particles that allows you to get a good visual
     * on how precise shapes that depend on complex algorithms play out.
     */
    @Nonnull
    private static final XParticle DEFAULT_PARTICLE = XParticle.FLAME;

    public int count = 1;
    /**
     * "extra" is usually the particle speed, but it
     * represents the size when used for dust particles.
     */
    public double extra;
    public boolean force;
    @Nonnull
    private XParticle particle = DEFAULT_PARTICLE;
    @Nullable
    private Location location, lastLocation;
    @Nonnull
    private Vector offset = new Vector();
    @Nullable
    private Vector particleDirection;
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
     * But here for the standard {@link Particles} methods, we're going to be using Tait–Bryan angles.
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
    private ParticleData data;
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
     * @deprecated use {@link #withColor(Color)}
     */
    @Nonnull
    @Deprecated
    public static ParticleDisplay colored(@Nullable Location location, int r, int g, int b, float size) {
        return ParticleDisplay.of(XParticle.DUST).withLocation(location).withColor(r, g, b, size);
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
        return ParticleDisplay.of(XParticle.DUST).withLocation(location).withColor(color, size);
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
     * @deprecated use {@link #of(XParticle)} and {@link #withLocation(Location)}
     */
    @Nonnull
    @Deprecated
    public static ParticleDisplay simple(@Nullable Location location, @Nonnull Particle particle) {
        Objects.requireNonNull(particle, "Cannot build ParticleDisplay with null particle");
        ParticleDisplay display = new ParticleDisplay();
        display.particle = XParticle.of(particle);
        display.location = location;
        return display;
    }

    /**
     * @deprecated use {@link #of(XParticle)} instead.
     */
    @Nonnull
    @Deprecated
    public static ParticleDisplay of(@Nonnull Particle particle) {
        return of(XParticle.of(particle));
    }

    /**
     * @since 6.0.0.1
     */
    @Nonnull
    public static ParticleDisplay of(@Nonnull XParticle particle) {
        ParticleDisplay display = new ParticleDisplay();
        display.particle = particle;
        return display;
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
    @Nonnull
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
        Optional<XParticle> particle = particleName == null ? Optional.empty() : XParticle.of(particleName);

        particle.ifPresent(xParticle -> display.particle = xParticle);
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

        String particleDirection = config.getString("direction");
        if (particleDirection != null) {
            List<String> directions = split(particleDirection.replace(" ", ""), ',');
            if (directions.size() >= 3) {
                double directionx = toDouble(directions.get(0));
                double directiony = toDouble(directions.get(1));
                double directionz = toDouble(directions.get(2));
                display.particleDirection(directionx, directiony, directionz);
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

        double size;
        if (config.isSet("size")) {
            size = config.getDouble("size");
            display.extra = size;
        } else {
            size = 1;
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
                    display.data = new DustTransitionParticleColor(parsedColor1, parsedColor2, size);
                } else {
                    display.data = new RGBParticleColor(parsedColor1);
                }
            }
        } else if (blockdata != null) {
            Material material = Material.getMaterial(blockdata);
            if (material != null && material.isBlock()) {
                display.data = new ParticleBlockData(material.createBlockData());
            }
        } else if (item != null) {
            Material material = Material.getMaterial(item);
            if (material != null && material.isItem()) {
                display.data = new ParticleItemData(new ItemStack(material, 1));
            }
        } else if (materialdata != null) {
            Material material = Material.getMaterial(materialdata);
            if (material != null && material.isBlock()) {
                // noinspection deprecation
                display.data = new ParticleMaterialData(material.getNewData((byte) 0));
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

        if (!isZero(display.offset)) {
            Vector offset = display.offset;
            section.set("offset", offset.getX() + ", " + offset.getY() + ", " + offset.getZ());
        }

        if (display.particleDirection != null) {
            Vector direction = display.particleDirection;
            section.set("direction", direction.getX() + ", " + direction.getY() + ", " + direction.getZ());
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

        if (display.data != null) {
            display.data.serialize(section);
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

    public ParticleDisplay withParticle(@Nonnull Particle particle) {
        return withParticle(XParticle.of(Objects.requireNonNull(particle, "Particle cannot be null")));
    }

    /**
     * @since 7.0.0
     */
    public ParticleDisplay withParticle(@Nonnull XParticle particle) {
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
    public XParticle getParticle() {
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
     * Get the data object.
     *
     * @return the data object.
     * @since 5.1.0
     */
    @Nullable
    public ParticleData getData() {
        return data;
    }

    /**
     * Sets the data object.
     */
    public ParticleDisplay withData(ParticleData data) {
        this.data = data;
        return this;
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
                "Data=" + data;
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
     * The particle must be {@link Particle#DUST}
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

    @Nonnull
    public ParticleDisplay withColor(@Nonnull Color color) {
        // TODO separate withColor() and withSize()
        return withColor(color, 1f);
    }

    /**
     * Adds note color properties to the particle settings.
     * The particle must be {@link Particle#NOTE}
     * for colors to work as expected.
     *
     * @param color the note number for the color (0-24, inclusive)
     * @return the same particle display, but modified.
     * @since 11.0.0
     */
    @Nonnull
    public ParticleDisplay withNoteColor(int color) {
        this.data = new NoteParticleColor(color);
        return this;
    }

    /**
     * Adds note color properties to the particle settings.
     * @param note the note color.
     * @return the same particle display, but modified.
     * @since 11.0.0
     */
    @SuppressWarnings("deprecation")
    @Nonnull
    public ParticleDisplay withNoteColor(Note note) {
        return withNoteColor(note.getId());
    }

    // public ParticleDisplay withSize(float size) {
    //     if (data == null) {
    //         this.data = new float[]{red, green, blue, size};
    //     }
    //     return this;
    // }

    /**
     * @since 7.1.0
     * @deprecated use {@link #withColor(Color, float)}
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Nonnull
    @Deprecated
    public ParticleDisplay withColor(float red, float green, float blue, float size) {
        this.data = new RGBParticleColor((int) red, (int) green, (int) blue);
        this.extra = size;
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
        this.data = new DustTransitionParticleColor(fromColor, toColor, size);
        this.extra = size;
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
        return withTransitionColor(new Color((int) red1, (int) green1, (int) blue1), size,
                new Color((int) red2, (int) green2, (int) blue2));
    }

    /**
     * Adds data for {@code BLOCK_CRACK}, {@code BLOCK_DUST},
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
        this.data = new ParticleBlockData(blockData);
        return this;
    }

    /**
     * Adds data for {@code LEGACY_BLOCK_CRACK}, {@code LEGACY_BLOCK_DUST}
     * and {@code LEGACY_FALLING_DUST} particles if the minecraft version is 1.13 or more.
     * <p>
     * If version is at most 1.12, old particles {@code BLOCK_CRACK},
     * {@code BLOCK_DUST} and {@code FALLING_DUST} will support this data.
     *
     * @param materialData the material data that will change the particle data.
     * @return the same particle display, but modified.
     * @see #withBlock(BlockData)
     * @since 5.1.0
     */
    @SuppressWarnings("deprecation")
    @Nonnull
    public ParticleDisplay withBlock(@Nonnull MaterialData materialData) {
        this.data = new ParticleMaterialData(materialData);
        return this;
    }

    /**
     * Adds extra data for {@code ITEM_CRACK}
     * particle, depending on the given item stack.
     *
     * @param item the item stack that will change the particle data.
     * @return the same particle display, but modified.
     * @since 5.1.0
     */
    @Nonnull
    public ParticleDisplay withItem(@Nonnull ItemStack item) {
        this.data = new ParticleItemData(item);
        return this;
    }

    @Nonnull
    public Vector getOffset() {
        return offset;
    }

    @Nonnull
    public Vector getParticleDirection() {
        return direction;
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
        this.preCalculation = (context) -> {
            try {
                context.location = locationCaller.call();
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
     * The method {@link Vector#isZero()} was not added until 1.19.3.
     */
    private static boolean isZero(@Nonnull Vector vector) {
        return vector.getX() == 0 && vector.getY() == 0 && vector.getZ() == 0;
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

        Location location = preContext.location;
        if (location == null) throw new IllegalStateException("Attempting to spawn particle when no location is set");
        // Exception check after preCalculation to account for dynamic location callers from withEntity()

        local = preContext.local;
        if (local != null && !rotations.isEmpty()) {
            List<Quaternion> rotations = getRotation(false);
            for (Quaternion grouped : rotations) {
                local = Quaternion.rotate(local, grouped);
            }
        }

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
     * Set the xyz direction of a particle.
     *
     * @since 11.0.0
     */
    @Nonnull
    public ParticleDisplay particleDirection(double x, double y, double z) {
        return particleDirection(new Vector(x, y, z));
    }

    /**
     * Set the xyz direction of a particle.
     *
     * @since 11.0.0
     */
    @Nonnull
    public ParticleDisplay particleDirection(@Nullable Vector particleDirection) {
        this.particleDirection = particleDirection;
        // Particle directions require a nonzero speed or the direction won't do anything.
        if (particleDirection != null && extra == 0) extra = 1;
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
        particleDirection = new Vector();
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
        return particleDirection != null;
    }

    /**
     * Spawns the particle at the current location.
     *
     * @return the location the particle was spawned at.
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
     * @return the location the particle was spawned at.
     * @since 1.0.0
     */
    @Nullable
    public Location spawn(@Nullable Vector local) {
        return spawn(finalizeLocation(local));
    }

    /**
     * Adds xyz to the cloned location before spawning particle.
     *
     * @return the location the particle was spawned at.
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
     * @return the same location that was passed.
     * @see #spawn(double, double, double)
     * @since 5.0.0
     */
    @Nullable
    public Location spawn(Location loc) {
        if (loc == null) return null;
        lastLocation = loc;

        Particle particle = this.particle.get();
        Objects.requireNonNull(particle, () -> "Cannot spawn unsupported particle: " + particle);

        // Compatibility for previous versions of ParticleDisplay where
        // count = 0 was required for certain particle data, e.g. directional particles.
        if (count == 0) count = 1;

        Object data = null;
        if (this.data != null) {
            this.data = this.data.transform(this);
            Vector offsetData = this.data.offsetValues(this);
            if (offsetData != null) {
                spawnWithDataInOffset(particle, loc, offsetData, null);
                return loc;
            }
            data = this.data.data(this);
            // Checks without data or block crack, block dust, falling dust, item crack or if data isn't right type
            if (!particle.getDataType().isInstance(data)) data = null;
        }

        if (particleDirection != null) {
            spawnWithDataInOffset(particle, loc, particleDirection, data);
            return loc;
        }

        // Nothing weird, just spawn the particles normally.
        spawnRaw(particle, loc, count, offset, data);
        return loc;
    }

    /**
     * Spawns the particles with specific data in the offset fields.
     * If required, this method will manually calculate an offset for
     * each particle similarly to the standard behavior, and spawn the particles at those.
     *
     * @param offsetData the data that needs to go in the offset fields.
     */
    private void spawnWithDataInOffset(Particle particle, Location loc, Vector offsetData, Object data) {
        // If there is no offset and we only want a single particle, we don't actually need to do anything special.
        // Otherwise, we'll at least need to use a loop.
        if (isZero(offset) && count < 2) {
            spawnRaw(particle, loc, 0, offsetData, data);
            return;
        }
        // Particles with a specific direction must be flagged with count = 0,
        // so we have to spawn each particle manually.
        double offsetx = offset.getX();
        double offsety = offset.getY();
        double offsetz = offset.getZ();
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < count; i++) {
            // When specifying an offset normally, bound of 1 gets you an 8 block range,
            // being +/- 4 blocks in each direction from the origin. Uses a Gaussian distribution.
            // Gaussian distribution uses a sqrt, so skip that if we can.
            double dx = offsetx == 0 ? 0 : r.nextGaussian() * 4 * offsetx;
            double dy = offsety == 0 ? 0 : r.nextGaussian() * 4 * offsety;
            double dz = offsetz == 0 ? 0 : r.nextGaussian() * 4 * offsetz;
            Location offsetLoc = cloneLocation(loc).add(dx, dy, dz);
            spawnRaw(particle, offsetLoc, 0, offsetData, data);
        }
    }

    /**
     * Calls the appropriate spawnParticle method with the parameters given.
     */
    private void spawnRaw(Particle particle, Location loc, int count, Vector offset, Object data) {
        double dx = offset.getX();
        double dy = offset.getY();
        double dz = offset.getZ();
        // The "extra" field has no effect on dust particles in some versions,
        // but in others it causes the colors to not display when set to 0.
        double extra = (this.particle == XParticle.DUST) ? 1 : this.extra;
        if (players == null)
            if (ISFLAT)
                loc.getWorld().spawnParticle(particle, loc, count, dx, dy, dz, extra, data, force);
            else loc.getWorld().spawnParticle(particle, loc, count, dx, dy, dz, extra, data);
        else for (Player player : players)
            player.spawnParticle(particle, loc, count, dx, dy, dz, extra, data);
    }

    /**
     * Returns the nearest note color to the given RGB values.
     * The nearest color is returned as an index in the {@link #NOTE_COLORS} array.
     * @param color the color to find the nearest note color for.
     * @return the index of the nearest note color (see {@link #NOTE_COLORS}).
     */
    public static int findNearestNoteColor(Color color) {
        double best = colorDistanceSquared(color, NOTE_COLORS[0]);
        int bestIndex = 0;
        for (int i = 1; i < NOTE_COLORS.length; i++) {
            double distance = colorDistanceSquared(color, NOTE_COLORS[i]);
            if (distance < best) {
                best = distance;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    /**
     * Computes the distance between two colors,
     * based on <a href="https://stackoverflow.com/a/6334454">this</a> SO answer
     * and <a href="https://www.compuphase.com/cmetric.htm">this</a> paper.
     * @param c1 the first color to compare
     * @param c2 the second color to compare
     * @return the square of the distance between the two colors
     */
    public static double colorDistanceSquared(Color c1, Color c2) {
        int red1 = c1.getRed();
        int red2 = c2.getRed();
        int rmean = (red1 + red2) >> 1;
        int r = red1 - red2;
        int g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        return (((512 + rmean) * r * r) >> 8) + 4 * g * g + (((767 - rmean) * b * b) >> 8);
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

    public interface ParticleData {

        default Vector offsetValues(ParticleDisplay display) {
            return null;
        }

        Object data(ParticleDisplay display);

        void serialize(ConfigurationSection section);

        /**
         * If this data doesn't support the given particle type but can be
         * converted to a type that does, this method should return the appropriate
         * ParticleData for that particle type.
         * Used for converting RGB particle data to note block particle data.
         */
        default ParticleData transform(ParticleDisplay display) {
            return this;
        }
    }

    public static class RGBParticleColor implements ParticleData {
        private final Color color;

        public RGBParticleColor(Color color) {
            this.color = color;
        }

        public RGBParticleColor(int r, int g, int b) {
            this(new Color(r, g, b));
        }

        @Override
        public Vector offsetValues(ParticleDisplay display) {
            // All particles that supported color used offset fields for them before the flattening.
            // ENTITY_EFFECT particle uses the offset fields for color on 1.20.4 and below.
            if (!ISFLAT || (display.particle == XParticle.ENTITY_EFFECT && display.particle.isSupported()
                    && display.particle.get().getDataType() == Void.class)) {
                // Dust particles on older versions would ignore the red channel if it's set to 0.
                double red = (color.getRed() == 0) ? Float.MIN_VALUE : color.getRed() / 255d;
                return new Vector(red, color.getGreen() / 255d, color.getBlue() / 255d);
            }
            return null;
        }

        public Object data(ParticleDisplay display) {
            if (display.particle == XParticle.DUST) {
                return new Particle.DustOptions(org.bukkit.Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue()), (float) display.extra);
            } else if (display.particle == XParticle.DUST_COLOR_TRANSITION) {
                org.bukkit.Color color = org.bukkit.Color.fromRGB(this.color.getRed(), this.color.getGreen(), this.color.getBlue());
                return new Particle.DustTransition(color, color, (float) display.extra);
            }
            if (SUPPORTS_ALPHA_COLORS) {
                return org.bukkit.Color.fromARGB(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
            } else {
                return org.bukkit.Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
            }
        }

        @Override
        public void serialize(ConfigurationSection section) {
            StringJoiner colorJoiner = new StringJoiner(", ");
            colorJoiner.add(Integer.toString(color.getRed()));
            colorJoiner.add(Integer.toString(color.getGreen()));
            colorJoiner.add(Integer.toString(color.getBlue()));
            section.set("color", colorJoiner.toString());
        }

        @Override
        public ParticleData transform(ParticleDisplay display) {
            if (display.particle == XParticle.NOTE) {
                return new NoteParticleColor(findNearestNoteColor(color));
            }
            return this;
        }
    }

    public static class DustTransitionParticleColor implements ParticleData {
        private final Particle.DustTransition dustTransition;

        public DustTransitionParticleColor(Color fromColor, Color toColor, double size) {
            this.dustTransition = new Particle.DustTransition(
                    org.bukkit.Color.fromRGB(fromColor.getRed(), fromColor.getGreen(), fromColor.getBlue()),
                    org.bukkit.Color.fromRGB(toColor.getRed(), toColor.getGreen(), toColor.getBlue()),
                    (float) size
            );
        }

        @Override
        public Object data(ParticleDisplay display) {
            return dustTransition;
        }

        @Override
        public void serialize(ConfigurationSection section) {
            StringJoiner colorJoiner = new StringJoiner(", ");
            org.bukkit.Color fromColor = dustTransition.getColor();
            org.bukkit.Color toColor = dustTransition.getToColor();
            colorJoiner.add(Integer.toString(fromColor.getRed()));
            colorJoiner.add(Integer.toString(fromColor.getGreen()));
            colorJoiner.add(Integer.toString(fromColor.getBlue()));
            colorJoiner.add(Integer.toString(toColor.getRed()));
            colorJoiner.add(Integer.toString(toColor.getGreen()));
            colorJoiner.add(Integer.toString(toColor.getBlue()));
            section.set("color", colorJoiner.toString());
        }
    }

    /**
     * Represents a color that a note particle can be.
     */
    public static class NoteParticleColor implements ParticleData {
        private final int note;

        public NoteParticleColor(int note) {
            this.note = note;
        }

        @SuppressWarnings("deprecation")
        public NoteParticleColor(Note note) {
            this(note.getId());
        }

        @Override
        public Vector offsetValues(ParticleDisplay display) {
            return new Vector(note / 24d, 0, 0);
        }

        @Override
        public Object data(ParticleDisplay display) {
            return null;
        }

        @Override
        public void serialize(ConfigurationSection section) {
            section.set("color", note);
        }

        @Override
        public ParticleData transform(ParticleDisplay display) {
            if (display.particle == XParticle.NOTE) {
                return this;
            }
            return new RGBParticleColor(NOTE_COLORS[note]);
        }
    }

    public static class ParticleBlockData implements ParticleData {
        private final BlockData blockData;

        public ParticleBlockData(BlockData blockData) {
            this.blockData = blockData;
        }

        @Override
        public Object data(ParticleDisplay display) {
            return blockData;
        }

        @Override
        public void serialize(ConfigurationSection section) {
            section.set("blockdata", blockData.getMaterial().name());
        }
    }

    @SuppressWarnings("deprecation")
    public static class ParticleMaterialData implements ParticleData {
        private final MaterialData materialData;

        public ParticleMaterialData(MaterialData materialData) {
            this.materialData = materialData;
        }

        @Override
        public Object data(ParticleDisplay display) {
            return materialData;
        }

        @Override
        public void serialize(ConfigurationSection section) {
            section.set("materialdata", materialData.getItemType().name());
        }
    }

    public static class ParticleItemData implements ParticleData {
        private final ItemStack item;

        public ParticleItemData(ItemStack item) {
            this.item = item;
        }

        @Override
        public Object data(ParticleDisplay display) {
            return item;
        }

        @Override
        public void serialize(ConfigurationSection section) {
            section.set("itemstack", item.getType());
        }
    }
}