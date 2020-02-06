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

import com.google.common.base.Enums;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <b>XParticle</b> - Different particle animations, text and image renderer.<br>
 * This utility uses {@link ParticleDisplay} for cleaner code. This class adds the ability
 * to define the optional values for spawning particles.
 * <p>
 * While this class provides many methods with options to spawn unique shapes,
 * it's recommended to make your own shapes by copying the code from these methods.
 * Note that some of the values for some methods are extremely sensitive and can change
 * the shape significantly by adding small numbers such as 0.5<br>
 * Most of the method parameters have a recommended value set to start with.
 * Note that these values are there to show how the intended normal shape
 * looks like before you start changing the values.
 * <p>
 * It's recommended to use low particle counts.
 * In most cases, decreasing the rate is better than increasing the particle count.
 * Most of the methods provide an option called "rate" that you can get more particles
 * by decreasing the distance between each point the particle spawns.<br>
 * Most of the {@link ParticleDisplay} used in this class are intended to
 * have 1 particle count and 0 xyz offset and speed.
 * <p>
 * Particles are rendered as front-facing 2D sprites, meaning they always face the player.
 * Minecraft clients will automatically clear previous particles if you reach the limit.
 * Particle range is 32 blocks. Particle count limit is 16,384.
 * Particles are not entities.
 * <p>
 * All the methods and operations used in this class are thread-safe.
 * Most of the methods do not run asynchronous by default.
 * If you're doing a resource intensive operation it's recommended
 * to either use {@link CompletableFuture#runAsync(Runnable)} or
 * {@link BukkitRunnable#runTaskTimerAsynchronously(Plugin, long, long)} for
 * smoothly animated shapes.
 * For huge animations you can use splittable tasks.
 * https://www.spigotmc.org/threads/409003/
 * By "huge", the algorithm used to generate locations is considered. You should not spawn
 * a lot of particles at once. This will cause FPS drops for most of
 * the clients, unless they have a good PC.
 * <p>
 * You can test your 2D shapes at <a href="https://www.desmos.com/calculator">Desmos</a>
 * Stuff you can do with with
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Math.html">Java {@link Math}</a>
 * Getting started with <a href="https://www.spigotmc.org/wiki/vector-programming-for-beginners/">Vectors</a>.
 * Particles: https://minecraft.gamepedia.com/Particles
 *
 * @author Crypto Morin
 * @version 3.1.0
 * @see ParticleDisplay
 * @see Particle
 * @see Location
 * @see Vector
 */
public final class XParticle {
    /**
     * A full circle has two PIs.
     * Don't know what the fuck is a PI? You can
     * watch this <a href="https://www.youtube.com/watch?v=pMpQK7Y8CiM">YouTube video</a>
     * <p>
     * PI is a radian number itself. So you can obtain other radians by simply
     * dividing PI.
     * Some simple ones:
     * <p>
     * <b>Important Radians:</b>
     * <pre>
     *     PI / 2 = 90 degrees
     *     PI / 3 = 60 degrees
     *     PI / 4 = 45 degrees
     *     PI / 6 = 30 degrees
     * </pre>
     * Any degree can be converted simply be using {@code PI/180 * degree}
     *
     * @see Math#toRadians(double)
     * @see Math#toDegrees(double)
     * @since 1.0.0
     */
    public static final double PII = 2 * Math.PI;
    /**
     * RGB list of all the 7 rainbow colors in order.
     *
     * @since 2.0.0
     */
    public static final List<int[]> RAINBOW = new ArrayList<>();

    static {
        RAINBOW.add(new int[]{128, 0, 128}); // Violet
        RAINBOW.add(new int[]{75, 0, 130}); // Indigo
        RAINBOW.add(new int[]{0, 0, 255}); // Blue
        RAINBOW.add(new int[]{0, 255, 0}); // Green
        RAINBOW.add(new int[]{255, 255, 0}); // Yellow
        RAINBOW.add(new int[]{255, 140, 0}); // Orange
        RAINBOW.add(new int[]{255, 0, 0}); // Red
    }

    /**
     * An optimized and stable way of getting particles for cross-version support.
     *
     * @param particle the particle name.
     * @return a particle that matches the specified name.
     * @since 1.0.0
     */
    public static Particle getParticle(String particle) {
        return Enums.getIfPresent(Particle.class, particle).orNull();
    }

    /**
     * Get a random particle from a list of particle names.
     *
     * @param particles the particles name.
     * @return a random particle from the list.
     * @since 1.0.0
     */
    public static Particle randomParticle(String... particles) {
        int rand = randInt(0, particles.length - 1);
        return getParticle(particles[rand]);
    }

    /**
     * A thread safe way to get a random double in a range.
     *
     * @param min the minimum number.
     * @param max the maximum number.
     * @return a random number.
     * @see #randInt(int, int)
     * @since 1.0.0
     */
    public static double random(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    /**
     * A thread safe way to get a random integer in a range.
     *
     * @param min the minimum number.
     * @param max the maximum number.
     * @return a random number.
     * @see #random(double, double)
     * @since 1.0.0
     */
    public static int randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Generate a random RGB color for particles.
     *
     * @return a random color.
     * @since 1.0.0
     */
    public static Color randomColor() {
        ThreadLocalRandom gen = ThreadLocalRandom.current();
        int randR = gen.nextInt(0, 256);
        int randG = gen.nextInt(0, 256);
        int randB = gen.nextInt(0, 256);

        return Color.fromRGB(randR, randG, randB);
    }

    /**
     * Generate a random colorized dust with a random size.
     *
     * @return a REDSTONE colored dust.
     * @since 1.0.0
     */
    public static Particle.DustOptions randomDust() {
        float size = randInt(5, 10) / 0.1f;
        return new Particle.DustOptions(randomColor(), size);
    }

    /**
     * Creates a blacksun-like increasing circles.
     *
     * @param radius     the radius of the biggest circle.
     * @param radiusRate the radius rate change of circles.
     * @param rate       the rate of the biggest cirlce points.
     * @param rateChange the rate change of circle points.
     * @see #circle(double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void blackSun(double radius, double radiusRate, double rate, double rateChange, ParticleDisplay display) {
        double j = 0;
        for (double i = 10; i > 0; i -= radiusRate) {
            j += rateChange;
            circle(radius + i, rate - j, display);
        }
    }

    /**
     * Spawn a circle.
     * Tutorial: https://www.spigotmc.org/threads/111238/
     * Uses its own unique directional pattern.
     *
     * @param radius the circle radius.
     * @param rate   the rate of cirlce points/particles.
     * @see #sphere(double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void circle(double radius, double rate, ParticleDisplay display) {
        // 180 degrees = PI
        // We need a full circle, 360 so we need two pies!
        // https://www.spigotmc.org/threads/176792/
        // cos and sin methods only accept radians.
        // Converting degrees to radians is not resource intensive. It's a really simple operation.
        // However we can skip the conversion by using radians in the first place.
        double rateDiv = Math.PI / rate;
        for (double theta = 0; theta <= PII; theta += rateDiv) {
            // In order to curve our straight line in the loop, we need to
            // use cos and sin. It doesn't matter, you can get x as sin and z as cos.
            // But you'll get weird results if you use si+n or cos for both or using tan or cot.
            double x = radius * Math.cos(theta);
            double z = radius * Math.sin(theta);

            if (display.isDirectional()) {
                // We're going to get the angle in these two coordinates.
                // Then we can spread each particle in the right angle.
                double phi = Math.atan2(z, x);
                double directionX = Math.cos(phi);
                double directionZ = Math.sin(phi);

                display.offset(directionX, display.offsety, directionZ);
            }

            display.spawn(x, 0, z);
        }
    }

    /**
     * Spawns connected 3D ellipses.
     *
     * @param plugin     the timer handler.
     * @param maxRadius  the maximum radius for the ellipses.
     * @param rate       the rate of the 3D ellipses circle points.
     * @param radiusRate the rate of the circle radius change.
     * @param extend     the extension for each ellipse.
     * @return the animation handler.
     * @see #magicCircles(JavaPlugin, double, double, double, double, ParticleDisplay)
     * @since 3.0.0
     */
    public static BukkitTask circularBeam(JavaPlugin plugin, double maxRadius, double rate, double radiusRate, double extend, ParticleDisplay display) {
        return new BukkitRunnable() {
            double rateDiv = Math.PI / rate;
            double radiusDiv = Math.PI / radiusRate;
            double dynamicRadius = 0;
            Vector dir = display.location.getDirection().normalize().multiply(extend);

            @Override
            public void run() {
                // If we wanted to use actual numbers as the radius then the curve for
                // each loop wouldn't be smooth.
                double radius = maxRadius * Math.sin(dynamicRadius);
                // Spawn normal circles.
                for (double theta = 0; theta < PII; theta += rateDiv) {
                    double x = radius * Math.sin(theta);
                    double z = radius * Math.cos(theta);
                    display.spawn(x, 0, z);
                }

                dynamicRadius += radiusDiv;
                if (dynamicRadius > Math.PI) dynamicRadius = 0;
                // Next circle center location.
                display.location.add(dir);
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    /**
     * Spawns circles increasing their radius.
     *
     * @param plugin     the timer handler.
     * @param radius     the radius for the first circle.
     * @param rate       the rate of circle points.
     * @param radiusRate the circle radius change rate.
     * @param distance   the distance between each circle.
     * @return the animation handler.
     * @see #circularBeam(JavaPlugin, double, double, double, double, ParticleDisplay)
     * @since 3.0.0
     */
    public static BukkitTask magicCircles(JavaPlugin plugin, double radius, double rate, double radiusRate, double distance, ParticleDisplay display) {
        return new BukkitRunnable() {
            double radiusDiv = Math.PI / radiusRate;
            double dynamicRadius = radius;
            Vector dir = display.location.getDirection().normalize().multiply(distance);

            @Override
            public void run() {
                double rateDiv = Math.PI / (rate * dynamicRadius);
                for (double theta = 0; theta < PII; theta += rateDiv) {
                    double x = dynamicRadius * Math.sin(theta);
                    double z = dynamicRadius * Math.cos(theta);
                    display.spawn(x, 0, z);
                }

                // We're going to use normal numbers since the circle radius will be always changing
                // in one axis.
                dynamicRadius += radiusDiv;
                display.location.add(dir);
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    /**
     * Spawn a 3D infinity sign.
     *
     * @param radius the radius of the infinity circles.
     * @param rate   the rate of the sign points.
     * @since 3.0.0
     */
    public static void infinity(double radius, double rate, ParticleDisplay display) {
        double rateDiv = Math.PI / rate;
        for (double i = 0; i < PII; i += rateDiv) {
            double x = Math.sin(i);
            double smooth = Math.pow(x, 2) + 1;
            double curve = radius * Math.cos(i);

            double z = curve / smooth;
            double y = (curve * x) / smooth;

            // If you remove x the infinity symbol will be 2D
            circle(1, rate, display.cloneWithLocation(x, y, z));
        }
    }

    /**
     * Spawn a cone.
     *
     * @param height     the height of the cone.
     * @param radius     the radius of the cone circle.
     * @param rate       the rate of the cone circles.
     * @param circleRate the rate of the cone circle points.
     * @since 1.0.0
     */
    public static void cone(double height, double radius, double rate, double circleRate, ParticleDisplay display) {
        // Our biggest radius / amount of loop times = the amount to subtract from the biggest radius so it wouldn't be negative.
        double radiusDiv = radius / (height / rate);
        // We're going spawn circles with different radiuses and rates to make a cone.
        for (double i = 0; i < height; i += rate) {
            radius -= radiusDiv;
            // The remainder of radiusDiv division might be not 0
            // This will happen to the last loop only.
            if (radius < 0) radius = 0;
            circle(radius, circleRate - i, display.cloneWithLocation(0, i, 0));
        }
    }

    /**
     * Spawn an ellipse.
     *
     * @param radius      the radius of the ellipse.
     * @param otherRadius the curve of the ellipse.
     * @param rate        the rate of ellipse points.
     * @see #circle(double, double, ParticleDisplay)
     * @since 2.0.0
     */
    public static void ellipse(double radius, double otherRadius, double rate, ParticleDisplay display) {
        double rateDiv = Math.PI / rate;

        // The only difference between circles and ellipses are that
        // ellipses use a different radius for one of their axis.
        for (double theta = 0; theta <= PII; theta += rateDiv) {
            double x = radius * Math.cos(theta);
            double y = otherRadius * Math.sin(theta);
            display.spawn(x, y, 0);
        }
    }

    /**
     * Spawn a blackhole.
     *
     * @param plugin the timer handler.
     * @param points the points of the blackhole pulls.
     * @param radius the radius of the blackhole circle.
     * @param rate   the rate of the blackhole circle points.
     * @param mode   blackhole mode. There are 5 modes.
     * @since 3.0.0
     */
    public static void blackhole(JavaPlugin plugin, int points, double radius, double rate, int mode, ParticleDisplay display) {
        display.directional();
        display.extra = 0.1;
        double rateDiv = Math.PI / rate;

        new BukkitRunnable() {
            double theta = 0;

            @Override
            public void run() {
                for (int i = 0; i < points; i++) {
                    // Spawn a circle.
                    double angle = PII * ((double) i / points);
                    double x = radius * Math.cos(theta + angle);
                    double z = radius * Math.sin(theta + angle);

                    // Set the angle of the circle point as its degree.
                    double phi = Math.atan2(z, x);
                    double xDirection = -Math.cos(phi);
                    double zDirection = -Math.sin(phi);

                    display.offset(xDirection, 0, zDirection);
                    display.spawn(x, 0, z);

                    // The modes are done by random math methods that are
                    // just randomly tested to give a different shape.
                    if (mode > 1) {
                        x = radius * Math.cos(-theta + angle);
                        z = radius * Math.sin(-theta + angle);

                        // Eye shaped blackhole
                        if (mode == 2) phi = Math.atan2(z, x);
                        else if (mode == 3) phi = Math.atan2(x, z);
                        else if (mode == 4) Math.atan2(Math.log(x), Math.log(z));

                        xDirection = -Math.cos(phi);
                        zDirection = -Math.sin(phi);

                        display.offset(xDirection, 0, zDirection);
                        display.spawn(x, 0, z);
                    }
                }

                theta += rateDiv;
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    /**
     * Spawns a rainbow.
     *
     * @param radius  the radius of the smallest circle.
     * @param rate    the rate of the rainbow points.
     * @param curve   the curve the the rainbow circles.
     * @param layers  the layers of each rainbow color.
     * @param compact the distance between each circles.
     * @since 2.0.0
     */
    public static void rainbow(double radius, double rate, double curve, double layers, double compact, ParticleDisplay display) {
        double secondRadius = radius * curve;

        // Rainbows have 7 colors.
        // Refer to RAINBOW constant for the color order.
        for (int i = 0; i < 7; i++) {
            // Get the rainbow color in order.
            int[] rgb = RAINBOW.get(i);
            display = ParticleDisplay.paintDust(display.location, rgb[0], rgb[1], rgb[2], 1F);

            // Display the same color multiple times.
            for (int layer = 0; layer < layers; layer++) {
                double rateDiv = Math.PI / (rate * (i + 2));

                // We're going to create our rainbow layer from half circles.
                for (double theta = 0; theta <= Math.PI; theta += rateDiv) {
                    double x = radius * Math.cos(theta);
                    double y = secondRadius * Math.sin(theta);
                    display.spawn(x, y, 0);
                }

                radius += compact;
            }
        }
    }

    /**
     * Spawns a crescent.
     *
     * @param radius the radius of crescent's big circle.
     * @param rate   the rate of the crescent's circle points.
     * @see #circle(double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void crescent(double radius, double rate, ParticleDisplay display) {
        double rateDiv = Math.PI / rate;

        // Crescents are two circles, one with a smaller radius and slightly shifted to the open part of the bigger circle.
        // To align the opening of the bigger circle with the +X axis we'll have to adjust our start and end  radians.
        for (double theta = Math.toRadians(45); theta <= Math.toRadians(325); theta += rateDiv) {
            // Our circle at the bottom.
            double x = Math.cos(theta);
            double z = Math.sin(theta);
            display.spawn(radius * x, 0, radius * z);

            // Slightly move the smaller circle to connect the openings.
            double smallerRadius = radius / 1.3;
            display.spawn(smallerRadius * x + 0.8, 0, smallerRadius * z);
        }
    }

    /**
     * Something similar to <a href="https://en.wikipedia.org/wiki/Wave_function">Quantum Wave function</a>
     *
     * @param extend      the particle width extension. Recommended value is 3
     * @param heightRange the height range of randomized waves. Recommended value is 1
     * @param size        the size of the terrain. Normal size is 3
     * @param rate        the rate of waves points. Recommended value is around 30
     * @since 2.0.0
     */
    public static void waveFunction(double extend, double heightRange, double size, double rate, ParticleDisplay display) {
        double height = heightRange / 2;
        boolean increase = true;
        double increaseRandomizer = random(heightRange / 2, heightRange);
        double rateDiv = Math.PI / rate;
        // Each wave is like a circle curving up and down.
        size *= PII;

        // We're going to create randomized circles.
        for (double x = 0; x <= size; x += rateDiv) {
            double xx = extend * x;
            double y1 = Math.sin(x);

            // Maximum value of sin is 1, when our sin is 1 it means
            // one full circle has been created, so we'll regenerate our random height.
            if (y1 == 1) {
                increase = !increase;
                if (increase) increaseRandomizer = random(heightRange / 2, heightRange);
                else increaseRandomizer = random(-heightRange, -heightRange / 2);
            }
            height += increaseRandomizer;

            // We'll generate horizontal cos/sin circles and move forward.
            for (double z = 0; z <= size; z += rateDiv) {
                double y2 = Math.cos(z);
                double yy = height * y1 * y2;
                double zz = extend * z;

                display.spawn(xx, yy, zz);
            }
        }
    }

    /**
     * Spawns a galaxy-like vortex.
     * Note that the speed of the particle is important.
     * Speed 0 will spawn static lines.
     *
     * @param plugin the timer handler.
     * @param points the points of the vortex.
     * @param rate   the speed of the vortex.
     * @return the task handling the animation.
     * @since 2.0.0
     */
    public static BukkitTask vortex(JavaPlugin plugin, int points, double rate, ParticleDisplay display) {
        double rateDiv = Math.PI / rate;
        display.directional();

        return new BukkitRunnable() {
            double theta = 0;

            @Override
            public void run() {
                theta += rateDiv;

                for (int i = 0; i < points; i++) {
                    // Calculate our starting point in a circle radius.
                    double multiplier = (PII * ((double) i / points));
                    double x = Math.cos(theta + multiplier);
                    double z = Math.sin(theta + multiplier);

                    // Calculate our direction of the spreading particles based on their angle.
                    double angle = Math.atan2(z, x);
                    double xDirection = Math.cos(angle);
                    double zDirection = Math.sin(angle);

                    display.offset(xDirection, 0, zDirection);
                    display.spawn(x, 0, z);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    /**
     * Not really a cylinder. It looks more like a cage.
     * For an actual cylidner just use {@link #circle(double, double, ParticleDisplay)}
     * and use one the xyz axis to build multiple circles.
     *
     * @param height the height of the cylinder.
     * @param radius the radius of the cylinder circles.
     * @param rate   the rate of cylinder points.
     * @since 1.0.0
     */
    public static void cylinder(double height, double radius, double rate, ParticleDisplay display) {
        double rateDiv = Math.PI / rate;

        // We want to connect our circle points so we use 180
        for (double theta = 0; theta <= Math.PI; theta += rateDiv) {
            // Our circle at the bottom.
            double x = radius * Math.cos(theta);
            double z = radius * Math.sin(theta);

            // Bottom Circle
            display.spawn(x, 0, z);
            display.spawn(-x, 0, -z);

            // Top Circle
            display.spawn(x, height, z);
            display.spawn(-x, height, -z);

            // Connect the circle points from opposite sides to each other.
            Location point1 = display.cloneLocation(x, 0, z);
            Location point2 = display.cloneLocation(-x, 0, -z);
            line(point1, point2, 0.1, display);

            Location point21 = display.cloneLocation(x, height, z);
            Location point22 = display.cloneLocation(-x, height, -z);
            line(point21, point22, 0.1, display);

            // Connect the two circles points to each other.
            line(point1, point21, 0.1, display);
            line(point2, point22, 0.1, display);
        }
    }

    /**
     * This will move the shape around in an area randomly while rotating them.
     * The position of the shape will be randomized positively and negatively by the offset parameters on each axis.
     *
     * @param plugin   the schedule handler.
     * @param update   the timer period in ticks.
     * @param rate     the distance between each location. Recommended value is 5.
     * @param runnable the particles to spawn.
     * @param displays the display references used to spawn particles in the runnable.
     * @return the async task handling the movement.
     * @see #rotateAround(JavaPlugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @see #guard(JavaPlugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @since 1.0.0
     */
    public static BukkitTask moveRotatingAround(JavaPlugin plugin, long update, double rate, double offsetx, double offsety, double offsetz,
                                                Runnable runnable, ParticleDisplay... displays) {
        return new BukkitRunnable() {
            double rotation = 180;

            @Override
            public void run() {
                rotation += rate;
                // Generate random radians.
                double x = Math.toRadians(90 + rotation);
                double y = Math.toRadians(60 + rotation);
                double z = Math.toRadians(30 + rotation);

                Vector vector = new Vector(offsetx * Math.PI, offsety * Math.PI, offsetz * Math.PI);
                if (offsetx != 0) rotateAroundX(vector, x);
                if (offsety != 0) rotateAroundY(vector, y);
                if (offsetz != 0) rotateAroundZ(vector, z);

                for (ParticleDisplay display : displays) display.location.add(vector);
                runnable.run();
                for (ParticleDisplay display : displays) display.location.subtract(vector);
            }
        }.runTaskTimerAsynchronously(plugin, 0L, update);
    }

    /**
     * This will move the particle around in an area randomly.
     * The position of the shape will be randomized positively and negatively by the offset parameters on each axis.
     *
     * @param plugin   the schedule handler.
     * @param update   the timer period in ticks.
     * @param rate     the distance between each location. Recommended value is 5.
     * @param runnable the particles to spawn.
     * @param displays the display references used to spawn particles in the runnable.
     * @return the async task handling the movement.
     * @see #rotateAround(JavaPlugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @see #guard(JavaPlugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @since 1.0.0
     */
    public static BukkitTask moveAround(JavaPlugin plugin, long update, double rate, double endRate, double offsetx, double offsety, double offsetz,
                                        Runnable runnable, ParticleDisplay... displays) {
        return new BukkitRunnable() {
            double multiplier = 0;
            boolean opposite = false;

            @Override
            public void run() {
                if (opposite) multiplier -= rate;
                else multiplier += rate;

                double x = multiplier * offsetx;
                double y = multiplier * offsety;
                double z = multiplier * offsetz;

                for (ParticleDisplay display : displays) display.location.add(x, y, z);
                runnable.run();
                for (ParticleDisplay display : displays) display.location.subtract(x, y, z);

                if (opposite) {
                    if (multiplier <= 0) opposite = !opposite;
                } else {
                    if (multiplier >= endRate) opposite = !opposite;
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, update);
    }

    /**
     * A simple test method to spawn a shape repeatedly for diagnosis.
     *
     * @param plugin   the timer handler.
     * @param runnable the shape(s) to display.
     * @return the timer task handling the displays.
     * @since 1.0.0
     */
    public static BukkitTask testDisplay(JavaPlugin plugin, Runnable runnable) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, 0L, 1L);
    }

    /**
     * This will rotate the shape around in an area randomly.
     * The position of the shape will be randomized positively and negatively by the offset parameters on each axis.
     *
     * @param plugin   the schedule handler.
     * @param update   the timer period in ticks.
     * @param rate     the distance between each location. Recommended value is 5.
     * @param runnable the particles to spawn.
     * @param displays the displays references used to spawn particles in the runnable.
     * @return the async task handling the movement.
     * @see #moveRotatingAround(JavaPlugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @see #guard(JavaPlugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @since 1.0.0
     */
    public static BukkitTask rotateAround(JavaPlugin plugin, long update, double rate, double offsetx, double offsety, double offsetz,
                                          Runnable runnable, ParticleDisplay... displays) {
        return new BukkitRunnable() {
            double rotation = 180;

            @Override
            public void run() {
                rotation += rate;
                double x = Math.toRadians((90 + rotation) * offsetx);
                double y = Math.toRadians((60 + rotation) * offsety);
                double z = Math.toRadians((30 + rotation) * offsetz);

                Vector vector = new Vector(x, y, z);
                for (ParticleDisplay display : displays) display.rotate(vector);
                runnable.run();
            }
        }.runTaskTimerAsynchronously(plugin, 0L, update);
    }

    /**
     * This will move the particle around in an area randomly.
     * The position of the shape will be randomized positively and negatively by the offset parameters on each axis.
     * Note that the ParticleDisplays used in runnable and displays options must be from the same reference.
     * <p>
     * <b>Example</b>
     * <pre>
     *     ParticleDisplays display = new ParticleDisplay(...);
     *     {@code WRONG: moveAround(plugin, 1, 5, 1.5, 1.5, 1.5, () -> circle(1, 10, new ParticleDisplay(...)), display);}
     *     {@code CORRECT: moveAround(plugin, 1, 5, 1.5, 1.5, 1.5, () -> circle(1, 10, display), display);}
     * </pre>
     *
     * @param plugin   the schedule handler.
     * @param update   the timer period in ticks.
     * @param rate     the distance between each location. Recommended value is 5.
     * @param runnable the particles to spawn.
     * @param displays the displays references used to spawn particles in the runnable.
     * @return the async task handling the movement.
     * @see #rotateAround(JavaPlugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @see #moveRotatingAround(JavaPlugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @since 1.0.0
     */
    public static BukkitTask guard(JavaPlugin plugin, long update, double rate, double offsetx, double offsety, double offsetz,
                                   Runnable runnable, ParticleDisplay... displays) {
        return new BukkitRunnable() {
            double rotation = 180;

            @Override
            public void run() {
                rotation += rate;
                double x = Math.toRadians((90 + rotation) * offsetx);
                double y = Math.toRadians((60 + rotation) * offsety);
                double z = Math.toRadians((30 + rotation) * offsetz);

                Vector vector = new Vector(offsetx * Math.PI, offsety * Math.PI, offsetz * Math.PI);
                rotateAroundX(vector, x);
                rotateAroundY(vector, y);
                rotateAroundZ(vector, z);

                for (ParticleDisplay display : displays) {
                    display.rotation = new Vector(x, y, z);
                    display.location.add(vector);
                }
                runnable.run();
                for (ParticleDisplay display : displays) display.location.subtract(vector);
            }
        }.runTaskTimerAsynchronously(plugin, 0L, update);
    }

    /**
     * Spawn a sphere.
     * Tutorial: https://www.spigotmc.org/threads/146338/
     * Also uses its own unique directional pattern.
     *
     * @param radius the circle radius.
     * @param rate   the rate of cirlce points/particles.
     * @see #circle(double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void sphere(double radius, double rate, ParticleDisplay display) {
        // Cache
        double rateDiv = Math.PI / rate;

        // To make a sphere we're going to generate multiple circles
        // next to each other.
        for (double phi = 0; phi <= Math.PI; phi += rateDiv) {
            // Cache
            double y1 = radius * Math.cos(phi);
            double y2 = radius * Math.sin(phi);

            for (double theta = 0; theta <= PII; theta += rateDiv) {
                double x = Math.cos(theta) * y2;
                double z = Math.sin(theta) * y2;

                if (display.isDirectional()) {
                    // We're going to do the same thing from spreading circle.
                    // Since this is a 3D shape we'll need to get the y value as well.
                    // I'm not sure if this is the right way to do it.
                    double omega = Math.atan2(z, x);
                    double directionX = Math.cos(omega);
                    double directionY = Math.sin(Math.atan2(y2, y1));
                    double directionZ = Math.sin(omega);

                    display.offset(directionX, directionY, directionZ);
                }

                display.spawn(x, y1, z);
            }
        }
    }

    /**
     * Spawns a sphere with spikes coming out from the center.
     * The sphere points will not be visible.
     *
     * @param radius            the radius of the sphere.
     * @param rate              the rate of sphere spike points.
     * @param chance            the chance to grow a spike randomly.
     * @param minRandomDistance he minimum distance of spikes from sphere.
     * @param maxRandomDistance the maximum distance of spikes from sphere.
     * @see #sphere(double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void spikeSphere(double radius, double rate, int chance, double minRandomDistance, double maxRandomDistance, ParticleDisplay display) {
        double rateDiv = Math.PI / rate;

        // Generate normal circle points.
        for (double phi = 0; phi <= Math.PI; phi += rateDiv) {
            double y = radius * Math.cos(phi);
            double sinPhi = radius * Math.sin(phi);

            for (double theta = 0; theta <= PII; theta += rateDiv) {
                double x = Math.cos(theta) * sinPhi;
                double z = Math.sin(theta) * sinPhi;

                if (chance == 0 || randInt(0, chance) == 1) {
                    Location start = display.cloneLocation(x, y, z);
                    // We want to get the direction of our center location and the circle point
                    // so we cant spawn spikes on the opposite direction.
                    Vector endVect = start.clone().subtract(display.location).toVector().multiply(random(minRandomDistance, maxRandomDistance));
                    Location end = start.clone().add(endVect);
                    line(start, end, 0.1, display);
                }
            }
        }
    }

    /**
     * Spawns a donut-shaped ring.
     * When the tube radius is greater than the main radius, the hole radius in the middle of the circle
     * will increase as the circles come closer to the mid-point.
     *
     * @param rate       the number of circles used to form the ring (tunnel circles)
     * @param radius     the radius of the ring.
     * @param tubeRadius the radius of the circles used to form the ring (tunnel circles)
     * @param tubeRate   the rate of circle points.
     * @see #circle(double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void ring(double rate, double tubeRate, double radius, double tubeRadius, ParticleDisplay display) {
        double rateDiv = Math.PI / rate;
        double tubeDiv = Math.PI / tubeRadius;

        // Use circles to build the ring.
        for (double theta = 0; theta <= PII; theta += rateDiv) {
            double cos = Math.cos(theta);
            double sin = Math.sin(theta);

            for (double phi = 0; phi <= PII; phi += tubeDiv) {
                double finalRadius = radius + (tubeRadius * Math.cos(phi));
                double x = finalRadius * cos;
                double y = finalRadius * sin;
                double z = tubeRadius * Math.sin(phi);

                display.spawn(x, y, z);
            }
        }
    }

    /**
     * Spawns animated spikes randomly spreading at the end location.
     *
     * @param plugin    the timer handler.
     * @param amount    the amount of spikes to spawn.
     * @param rate      rate of spike line points.
     * @param start     start location of spikes.
     * @param originEnd end location of spikes.
     * @since 1.0.0
     */
    public static void spread(JavaPlugin plugin, int amount, int rate, Location start, Location originEnd,
                              double offsetx, double offsety, double offsetz, ParticleDisplay display) {
        new BukkitRunnable() {
            int count = amount;

            @Override
            public void run() {
                count--;
                int frame = rate;

                while (frame != 0) {
                    double x = random(-offsetx, offsetx);
                    double y = random(-offsety, offsety);
                    double z = random(-offsetz, offsetz);

                    Location end = originEnd.clone().add(x, y, z);
                    line(start, end, 0.1, display);
                    frame--;
                }

                if (count == 0) cancel();
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    /**
     * Spawns a circle with heart shaped circles sticking out.
     * This method can be used to create many other shapes other than heart.
     *
     * @param cut            defines the count of two oval pairs. For heart use 2
     * @param cutAngle       defines the compression of two oval pairs. For heart use 4
     * @param depth          the depth of heart's inner spike.
     * @param compressHeight compress the heart along the y axis.
     * @param rate           the rate of the heart points. Will be converted to radians.
     * @since 1.0.0
     */
    public static void heart(double cut, double cutAngle, double depth, double compressHeight, double rate, ParticleDisplay display) {
        for (double theta = 0; theta <= PII; theta += Math.PI / rate) {
            double phi = theta / cut;
            double cos = Math.cos(phi);
            double sin = Math.sin(phi);
            double omega = Math.pow(Math.abs(Math.sin(2 * cutAngle * phi)) + depth * Math.abs(Math.sin(cutAngle * phi)), 1 / compressHeight);

            double y = omega * (sin + cos);
            double z = omega * (cos - sin);

            display.spawn(0, y, z);
        }
    }

    /**
     * Spawns multiple animated atomic-like circles rotating around in their orbit.
     *
     * @param plugin the timer handler.
     * @param orbits the orbits of the atom.
     * @param radius the radius of the atom orbits.
     * @param rate   the rate of orbit points.
     * @see #atom(int, double, double, ParticleDisplay, ParticleDisplay)
     * @since 1.0.0
     */
    public static void atomic(JavaPlugin plugin, int orbits, double radius, double rate, ParticleDisplay orbit) {
        new BukkitRunnable() {
            double theta = 0;
            double rateDiv = Math.PI / rate;
            double dist = Math.PI / orbits;

            @Override
            public void run() {
                int orbital = orbits;
                theta += rateDiv;

                double x = radius * Math.cos(theta);
                double z = radius * Math.sin(theta);

                for (double angle = 0; orbital > 0; angle += dist) {
                    orbit.rotation = new Vector(0, 0, angle);
                    orbit.spawn(x, 0, z);
                    orbital--;
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    /**
     * Spawns animated helix shapes.
     *
     * @param plugin    the timer handler.
     * @param strings   the amount of helix strings. The rotation angle will split equally for each.
     * @param radius    the radius of the helix.
     * @param rate      the rate of helix points.
     * @param extension the helix circle extension.
     * @param height    the height of the helix.
     * @param speed     the speed of the rate builder in each animation tick.
     * @param fadeUp    helix radius will decrease to zero as it gets closer to the top.
     * @param fadeDown  helix radius will increase to the original radius as it gets closer to the center.
     * @return the animation task.
     * @see #dnaReplication(JavaPlugin, double, double, int, double, int, int, ParticleDisplay)
     * @since 3.0.0
     */
    public static BukkitTask helix(JavaPlugin plugin, int strings, double radius, double rate, double extension, int height, int speed,
                                   boolean fadeUp, boolean fadeDown, ParticleDisplay display) {
        return new BukkitRunnable() {
            // If we look at a helix string from above, we'll see a circle tunnel.
            // To make this tunnel we're going to generate circles while moving
            // upwards to get a curvy tunnel.
            // Since we're generating this string infinitely we don't need
            // to use radians or degrees.
            double dist = PII / strings;
            double radiusDiv = radius / (height / rate);
            double radiusDiv2 = fadeUp && fadeDown ? radiusDiv * 2 : radiusDiv;
            double dynamicRadius = fadeDown ? 0 : radius;
            boolean center = !fadeDown;
            double y = 0;

            @Override
            public void run() {
                int repeat = speed;
                while (repeat > 0) {
                    repeat--;
                    y += rate;

                    // 2D cirlce points.
                    double x = dynamicRadius * Math.cos(extension * y);
                    double z = dynamicRadius * Math.sin(extension * y);

                    if (!center) {
                        dynamicRadius += radiusDiv2;
                        if (dynamicRadius >= radius) center = true;
                    } else if (fadeUp) dynamicRadius -= radiusDiv2;

                    // Now we're going to copy our points and rotate them.
                    int tempString = strings;
                    for (double angle = 0; tempString > 0; angle += dist) {
                        display.rotate(0, angle, 0);
                        display.spawn(x, y, z);
                        display.rotate(0, -angle, 0);
                        tempString--;
                    }

                    if (y > height) cancel();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    /**
     * Spawns a broken line that creates more and extended branches
     * as it gets closer to the end length.
     * This method doesn't support rotations. Use the direction instead.
     *
     * @param start      the starting point of the new branch. For the first call it's the same location as the displays location.
     * @param direction  the direction of the lightning. A simple direction would be {@code entity.getLocation().getDirection().normalize()}
     *                   For a simple end point direction would be {@code endLocation.toVector().subtract(start.toVector()).normalize()}
     * @param entries    the number of entries for the main lightning branch. Recommended is 20
     * @param branches   the maximum number of branches each entry can have. Recommended is 200
     * @param radius     the radius of the lightning branches. Recommended is 0.5
     * @param offset     the offset of the lightning branches. Recommended is 2
     * @param offsetRate the offset change rate of the lightning branches. Recommended is 1
     * @param length     the length of the lightning branch. Recommended is 1.5
     * @param lengthRate the length change rate of the lightning branch. Recommended is 1
     * @param branch     the chance of creating a new branch. Recommended is 0.1
     * @param branchRate the chance change of creating a new branch. Recommended is 1
     * @since 3.0.0
     */
    public static void lightning(Location start, Vector direction, int entries, int branches, double radius,
                                 double offset, double offsetRate,
                                 double length, double lengthRate,
                                 double branch, double branchRate, ParticleDisplay display) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (entries <= 0) return;
        boolean inRange = true;

        // Check if we can create new branches or the current branch
        // length is already in range.
        while (random.nextDouble() < branch || inRange) {
            // Break our straight line randomly.
            Vector randomizer = new Vector(
                    random.nextDouble(-radius, radius), random.nextDouble(-radius, radius), random.nextDouble(-radius, radius))
                    .normalize().multiply((random.nextDouble(-radius, radius)) * offset);
            Vector endVector = start.clone().toVector().add(direction.clone().multiply(length)).add(randomizer);
            Location end = endVector.toLocation(start.getWorld());

            // Check if the broken line length is in our max length range.
            if (end.distance(start) <= length) {
                inRange = true;
                continue;
            } else inRange = false;

            // Create particle points in our broken straight line.
            int rate = (int) (start.distance(end) / 0.1); // distance * (distance / 10)
            Vector rateDir = endVector.clone().subtract(start.toVector()).normalize().multiply(0.1);
            for (int i = 0; i < rate; i++) {
                Location loc = start.clone().add(rateDir.clone().multiply(i));
                display.spawn(loc);
            }

            // Create new entries if possible.
            lightning(end.clone(), direction, entries - 1, branches - 1, radius, offset * offsetRate, offsetRate,
                    length * lengthRate, lengthRate,
                    branch * branchRate, branchRate, display);
            // Check if the maximum number of branches has already been used for this entry.
            if (branches <= 0) break;
        }
    }


    /**
     * Spawn a DNA double helix string with nucleotides.
     *
     * @param radius              the radius of two DNA string circles.
     * @param rate                the rate of DNA strings and hydrogen bond points.
     * @param height              the height of the DNA strings.
     * @param hydrogenBondDist    the distance between each hydrogen bond (read inside method). This distance is also affected by rate.
     * @param display             display for strings.
     * @param hydrogenBondDisplay display for hydrogen bonds.
     * @see #helix(JavaPlugin, int, double, double, double, int, int, boolean, boolean, ParticleDisplay)
     * @see #dnaReplication(JavaPlugin, double, double, int, double, int, int, ParticleDisplay)
     * @since 1.0.0
     */
    public static void dna(double radius, double rate, double extension, int height, int hydrogenBondDist, ParticleDisplay display, ParticleDisplay hydrogenBondDisplay) {
        // The distance between each hydrogen bond from the previous bond.
        // All the nucleotides in DNA will form a bond but this will indicate the
        // distance between the phosphodiester bonds.
        int nucleotideDist = 0;

        // Move the helix upwards by forming phosphodiester bonds between two nucleotides on the same string.
        for (double y = 0; y <= height; y += rate) {
            nucleotideDist++;

            // The helix string is generated in a circle tunnel.
            double x = radius * Math.cos(extension * y);
            double z = radius * Math.sin(extension * y);

            // The two nucleotides on each DNA string.
            // Should be exactly facing each other with the same Y pos.
            Location nucleotide1 = display.location.clone().add(x, y, z);
            display.spawn(x, y, z);
            Location nucleotide2 = display.location.clone().subtract(x, -y, z);
            display.spawn(-x, y, -z);

            // If it's the appropriate distance for two nucleotides to form a hydrogen bond.
            // We don't care about the type of nucleotide. It's going to be one bond only.
            if (nucleotideDist >= hydrogenBondDist) {
                nucleotideDist = 0;
                line(nucleotide1, nucleotide2, rate * 2, hydrogenBondDisplay);
            }
        }
    }

    /**
     * Spawn an animated DNA replication with colored bonds.
     *
     * @param plugin           the timer handler.
     * @param radius           the radius of DNA helix circle.
     * @param rate             the rate of DNA points.
     * @param speed            the number of points to build in a single tick. Recommended is 5.
     * @param extension        the extension of the DNA helix sin/cos waves.
     * @param height           the height of the DNA strings.
     * @param hydrogenBondDist the distance between two DNA string helix points in a single string for each hydrogen bond to be formed.
     * @return the timer handling the animation.
     * @see #dna(double, double, double, int, int, ParticleDisplay, ParticleDisplay)
     * @since 3.0.0
     */
    public static BukkitTask dnaReplication(JavaPlugin plugin, double radius, double rate, int speed, double extension,
                                            int height, int hydrogenBondDist, ParticleDisplay display) {
        // We'll use the common nucleotide colors.
        ParticleDisplay adenine = ParticleDisplay.paintDust(null, 0, 0, 255, 1); // Blue
        ParticleDisplay thymine = ParticleDisplay.paintDust(null, 0, 255, 255, 1); // Yellow
        ParticleDisplay guanine = ParticleDisplay.paintDust(null, 0, 255, 0, 1); // Green
        ParticleDisplay cytosine = ParticleDisplay.paintDust(null, 255, 0, 0, 1); // Red

        return new BukkitRunnable() {
            double y = 0;
            int nucleotideDist = 0;

            @Override
            public void run() {
                int repeat = speed;
                while (repeat != 0) {
                    y += rate;
                    nucleotideDist++;
                    repeat--;

                    double x = radius * Math.cos(extension * y);
                    double z = radius * Math.sin(extension * y);
                    Location nucleotide1 = display.location.clone().add(x, y, z);
                    //display.spawn(x, y, z);
                    circle(0.1, 10, display.cloneWithLocation(x, y, z));
                    Location nucleotide2 = display.location.clone().subtract(x, -y, z);
                    circle(0.1, 10, display.cloneWithLocation(-x, y, -z));
                    //display.spawn(-x, y, -z);

                    // We're going to find the midpoint of the two nucleotides so we can
                    // form our hydrogen bond.
                    // We'll convert locations to vectors since the midpoint method is only
                    // available for Vectors. Yes, we can still calculate the midpoint from locations too.
                    // Xm = (x1 + x2) / 2, Ym = (y1 + y2) / 2, Zm = (z1 + z2) / 2
                    Location midPointBond = nucleotide1.toVector().midpoint(nucleotide2.toVector()).toLocation(nucleotide1.getWorld());

                    if (nucleotideDist >= hydrogenBondDist) {
                        nucleotideDist = 0;

                        // Adenine - Thymine
                        if (randInt(0, 1) == 1) {
                            line(nucleotide1, midPointBond, rate - 0.1, adenine);
                            line(nucleotide2, midPointBond, rate - 0.1, thymine);
                        }
                        // Guanine - Cytosine
                        else {
                            line(nucleotide1, midPointBond, rate - 0.1, cytosine);
                            line(nucleotide2, midPointBond, rate - 0.1, guanine);
                        }
                    }

                    if (y >= height) cancel();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    /**
     * Draws a line from the player's looking direction.
     *
     * @param player the player to draw the line from.
     * @param length the length of the line.
     * @param rate   the rate of points of the line.
     * @see #line(Location, Location, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void drawLine(Player player, double length, double rate, ParticleDisplay display) {
        Location eye = player.getEyeLocation();
        line(eye, eye.clone().add(eye.getDirection().multiply(length)), rate, display);
    }

    /**
     * A simple method to spawn animated clouds effect.
     *
     * @param plugin the timer handler.
     * @param cloud  recommended particle is {@link Particle#CLOUD} or {@link Particle#SMOKE_LARGE} and the offset xyz should be higher than 2
     * @param rain   recommended particle is {@link Particle#WATER_DROP} or {@link Particle#FALLING_LAVA} and the offset xyz should be the same as cloud.
     * @return the timer task handling the animation.
     * @since 1.0.0
     */
    public static BukkitTask cloud(JavaPlugin plugin, ParticleDisplay cloud, ParticleDisplay rain) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                cloud.spawn();
                rain.spawn();
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    /**
     * Spawns a line from a location to another.
     * Tutorial: https://www.spigotmc.org/threads/176695/
     *
     * @param start the starting point of the line.
     * @param end   the ending point of the line.
     * @param rate  the rate of points of the line.
     * @see #drawLine(Player, double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void line(Location start, Location end, double rate, ParticleDisplay display) {
        Vector distance = end.toVector().subtract(start.toVector());
        double length = distance.length();
        distance.normalize();

        double x = distance.getX();
        double y = distance.getY();
        double z = distance.getZ();

        ParticleDisplay clone = display.clone();
        clone.location = start;
        for (double i = 0; i < length; i += rate) {
            // Since the rate can be any number it's possible to get a higher number than
            // the length in the last loop.
            if (i > length) i = length;
            clone.spawn(x * i, y * i, z * i);
        }
    }

    /**
     * Spawns a rectangle.
     *
     * @param start the starting point of the rectangle which is equals to the display location.
     * @param end   the ending point of the rectangle.
     * @param rate  the rate of the rectangle points.
     * @see #cube(Location, Location, double, ParticleDisplay)
     * @see #cage(Location, Location, double, double, ParticleDisplay)
     * @since 3.0.0
     */
    public static void rectangle(Location start, Location end, double rate, ParticleDisplay display) {
        display.location = start;
        double maxX = Math.max(start.getX(), end.getX());
        double minX = Math.min(start.getX(), end.getX());

        double maxY = Math.max(start.getY(), end.getY());
        double minY = Math.min(start.getY(), end.getY());

        // A simple 2D Shape
        for (double x = minX; x <= maxX; x += rate) {
            for (double y = minY; y <= maxY; y += rate) {
                display.spawn(x - minX, y - minY, 0);
            }
        }
    }

    /**
     * Spawns a cage.
     *
     * @param start   the starting point of the cage.
     * @param end     the ending point of the cage.
     * @param rate    the rate of cage two rectangles and the bar lines.
     * @param barRate the chance of bars for the cage.
     * @see #rectangle(Location, Location, double, ParticleDisplay)
     * @see #cylinder(double, double, double, ParticleDisplay)
     * @since 3.0.0
     */
    public static void cage(Location start, Location end, double rate, double barRate, ParticleDisplay display) {
        double maxX = Math.max(start.getX(), end.getX());
        double minX = Math.min(start.getX(), end.getX());

        double maxZ = Math.max(start.getZ(), end.getZ());
        double minZ = Math.min(start.getZ(), end.getZ());

        // Same thing as a rectangle.
        double barChance = 0;
        for (double x = minX; x <= maxX; x += rate) {
            for (double z = minZ; z <= maxZ; z += rate) {
                Location barStart = display.spawn(x - minX, 0, z - minZ);
                Location barEnd = display.spawn(x - minX, 3, z - minZ);

                if ((x == minX || x + rate > maxX) || (z == minZ || z + rate > maxZ)) {
                    barChance++;
                    if (barChance >= barRate) {
                        barChance = 0;
                        line(barStart, barEnd, rate, display);
                    }
                }
            }
        }
    }

    /**
     * Spawn a cube with all the space filled with particles inside.
     * To spawn a cube with a width, height and depth you can simply add to the original location.
     * <p>
     * <b>Example</b>
     * <pre>
     *     Location start = player.getLocation();
     *     Location end = start.clone().add(width, height, depth);
     *     filledCube(start, end, 0.3, new ParticleDisplay(Particle.FLAME, null, 1));
     * </pre>
     *
     * @param start the starting point of the cube.
     * @param end   the ending point of the cube.
     * @param rate  the rate of cube points.
     * @see #cube(Location, Location, double, ParticleDisplay)
     * @see #structuredCube(Location, Location, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void filledCube(Location start, Location end, double rate, ParticleDisplay display) {
        display.location = start;
        double maxX = Math.max(start.getX(), end.getX());
        double minX = Math.min(start.getX(), end.getX());

        double maxY = Math.max(start.getY(), end.getY());
        double minY = Math.min(start.getY(), end.getY());

        double maxZ = Math.max(start.getZ(), end.getZ());
        double minZ = Math.min(start.getZ(), end.getZ());

        // A simple 3D Shape.
        // This is really easy. You just have to loop
        // thro the z of each y and y of each x.
        // Although spawning 1D (line) and 2D (rectangle) shapes are possible
        // with this method alone, having them as separated methods is more efficient.
        for (double x = minX; x <= maxX; x += rate) {
            for (double y = minY; y <= maxY; y += rate) {
                for (double z = minZ; z <= maxZ; z += rate) {
                    display.spawn(x - minX, y - minY, z - minZ);
                }
            }
        }
    }

    /**
     * Spawns a cube with the inner space empty.
     *
     * @param start the starting point of the cube.
     * @param end   the ending point of the cube.
     * @param rate  the rate of cube points.
     * @see #filledCube(Location, Location, double, ParticleDisplay)
     * @see #structuredCube(Location, Location, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void cube(Location start, Location end, double rate, ParticleDisplay display) {
        display.location = start;
        double maxX = Math.max(start.getX(), end.getX());
        double minX = Math.min(start.getX(), end.getX());

        double maxY = Math.max(start.getY(), end.getY());
        double minY = Math.min(start.getY(), end.getY());

        double maxZ = Math.max(start.getZ(), end.getZ());
        double minZ = Math.min(start.getZ(), end.getZ());

        // A simple 3D Shape.
        for (double x = minX; x <= maxX; x += rate) {
            for (double y = minY; y <= maxY; y += rate) {
                for (double z = minZ; z <= maxZ; z += rate) {
                    // We're going to filter the locations that are on the wall of the cube.
                    // So we don't fill the cube itself.
                    // Another way is to use 6 loops, one 2 axis loop for each side.
                    if ((y == minY || y + rate > maxY) || (x == minX || x + rate > maxX) || (z == minZ || z + rate > maxZ)) {
                        display.spawn(x - minX, y - minY, z - minZ);
                    }
                }
            }
        }
    }

    /**
     * spawn a cube with the inner space and walls empty, leaving only the edges visible.
     *
     * @param start the starting point of the cube.
     * @param end   the ending point of the cube.
     * @param rate  the rate of cube points.
     * @see #filledCube(Location, Location, double, ParticleDisplay)
     * @see #cube(Location, Location, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void structuredCube(Location start, Location end, double rate, ParticleDisplay display) {
        display.location = start;
        double maxX = Math.max(start.getX(), end.getX());
        double minX = Math.min(start.getX(), end.getX());

        double maxY = Math.max(start.getY(), end.getY());
        double minY = Math.min(start.getY(), end.getY());

        double maxZ = Math.max(start.getZ(), end.getZ());
        double minZ = Math.min(start.getZ(), end.getZ());

        // A simple 3D Shape.
        for (double x = minX; x <= maxX; x += rate) {
            for (double y = minY; y <= maxY; y += rate) {
                for (double z = minZ; z <= maxZ; z += rate) {
                    // We only want the edges so we need to get the location
                    // where at least 2 xyz components are either min or max.
                    // Another way is to use 10 loops, one 1 axis loop for each side.
                    int components = 0;
                    if (x == minX || x + rate > maxX) components++;
                    if (y == minY || y + rate > maxY) components++;
                    if (z == minZ || z + rate > maxZ) components++;
                    if (components >= 2) display.spawn(x - minX, y - minY, z - minZ);
                }
            }
        }
    }

    /**
     * Inaccurate representation of hypercubes. Just a bunch of tesseracts.
     * New smaller tesseracts will be created as the dimension increases.
     * https://en.wikipedia.org/wiki/Hypercube
     * <p>
     * I'm still looking for a way to make this animated
     * but it's damn confusing: https://www.youtube.com/watch?v=iGO12Z5Lw8s
     *
     * @param startOrigin the starting point for the original cube.
     * @param endOrigin   the endnig point for the original cube.
     * @param rate        the rate of cube points.
     * @param sizeRate    the size
     * @param cubes       the dimension of the hypercube starting from 3D. E.g. {@code dimension 1 -> 4D tersseract}
     * @see #structuredCube(Location, Location, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void hypercube(Location startOrigin, Location endOrigin, double rate, double sizeRate, int cubes, ParticleDisplay display) {
        List<Location> previousPoints = null;
        for (int i = 0; i < cubes + 1; i++) {
            List<Location> points = new ArrayList<>();
            Location start = startOrigin.clone().subtract(i * sizeRate, i * sizeRate, i * sizeRate);
            Location end = endOrigin.clone().add(i * sizeRate, i * sizeRate, i * sizeRate);

            display.location = start;
            double maxX = Math.max(start.getX(), end.getX());
            double minX = Math.min(start.getX(), end.getX());

            double maxY = Math.max(start.getY(), end.getY());
            double minY = Math.min(start.getY(), end.getY());

            double maxZ = Math.max(start.getZ(), end.getZ());
            double minZ = Math.min(start.getZ(), end.getZ());

            // We're going to hardcode the corner points.
            // M M M
            points.add(new Location(start.getWorld(), maxX, maxY, maxZ));
            // m m m
            points.add(new Location(start.getWorld(), minX, minY, minZ));
            // M m M
            points.add(new Location(start.getWorld(), maxX, minY, maxZ));
            // m M m
            points.add(new Location(start.getWorld(), minX, maxY, minZ));
            // m m M
            points.add(new Location(start.getWorld(), minX, minY, maxZ));
            // M m m
            points.add(new Location(start.getWorld(), maxX, minY, minZ));
            // M M m
            points.add(new Location(start.getWorld(), maxX, maxY, minZ));
            // m M M
            points.add(new Location(start.getWorld(), minX, maxY, maxZ));

            if (previousPoints != null) {
                for (int p = 0; p < 8; p++) {
                    Location current = points.get(p);
                    Location previous = previousPoints.get(p);
                    line(previous, current, rate, display);
                }
            }
            previousPoints = points;

            // Same thing as a structured cube.
            for (double x = minX; x <= maxX; x += rate) {
                for (double y = minY; y <= maxY; y += rate) {
                    for (double z = minZ; z <= maxZ; z += rate) {
                        int components = 0;
                        if (x == minX || x + rate > maxX) components++;
                        if (y == minY || y + rate > maxY) components++;
                        if (z == minZ || z + rate > maxZ) components++;
                        if (components >= 2) display.spawn(x - minX, y - minY, z - minZ);
                    }
                }
            }
        }
    }

    /**
     * Spawn 3D spiked circles.
     * Note that the animation is intended to be used with prototype mode enabled.
     * Animations without prototype doesn't really look good. You might want to increase the speed.
     *
     * @param plugin      the timer handler.
     * @param points      the number of circle sides with spikes.
     * @param spikes      the number of spikes on each side.
     * @param rate        the rate of star points.
     * @param spikeLength the length of each spike.
     * @param coreRadius  the radius of the center circle.
     * @param neuron      the neuron level. Neuron level is affected by the prototype mode.
     *                    Normal value is 1 if prototype mode is disabled. If the value goes higher than 1 it'll form a neuron-like body cell shape.
     *                    The value is used in small ranges for when prototype mode is enabled. Usually between 0.01 and 0.1
     * @param prototype   if the spikes of the star should use helix instead of a random generator.
     * @param speed       the speed of animation. Smoothest/slowest is 1
     * @see #spikeSphere(double, double, int, double, double, ParticleDisplay)
     * @since 3.0.0
     */
    public static void star(JavaPlugin plugin, int points, int spikes, double rate, double spikeLength, double coreRadius,
                            double neuron, boolean prototype, int speed, ParticleDisplay display) {
        double pointsRate = PII / points;
        double rateDiv = Math.PI / rate;
        ThreadLocalRandom random = prototype ? null : ThreadLocalRandom.current();

        for (int i = 0; i < spikes * 2; i++) {
            double spikeAngle = i * Math.PI / spikes;

            new BukkitRunnable() {
                double vein = 0;
                double theta = 0;

                @Override
                public void run() {
                    int repeat = speed;
                    while (repeat != 0) {
                        repeat--;
                        theta += rateDiv;

                        // We're going to spawn little circles to create our spikes.
                        // Spawning them with a random radius.
                        double height = (prototype ? vein : random.nextDouble(0, neuron)) * spikeLength;
                        if (prototype) vein += neuron;
                        Vector vector = new Vector(Math.cos(theta), 0, Math.sin(theta));

                        // We don't want to fill the inside circle.
                        vector.multiply((spikeLength - height) * coreRadius / spikeLength);
                        vector.setY(coreRadius + height);

                        // Rotate the vector for the next spike.
                        rotateAroundX(vector, spikeAngle);
                        for (int j = 0; j < points; j++) {
                            // Rotate the spikes to copy them with equal angles.
                            rotateAroundY(vector, pointsRate);
                            display.spawn(vector);
                        }

                        if (theta >= PII) cancel();
                    }
                }
            }.runTaskTimerAsynchronously(plugin, 0L, 1L);
        }
    }

    /**
     * Spawns a connected 2D polygon.
     * Tutorial: https://www.spigotmc.org/threads/158678/
     *
     * @param points     the number of polygon points.
     * @param connection the connection level of two points.
     * @param size       the size of the shape.
     * @param rate       the rate of connection points.
     * @param extend     extends the shape, connecting unrelated points together.
     * @since 1.0.0
     */
    public static void polygon(int points, int connection, double size, double rate, double extend, ParticleDisplay display) {
        for (int point = 0; point < points; point++) {
            // Generate our points in a circle shaped area.
            double angle = Math.toRadians(360D / points * point);
            // Our next point to connect to the previous one.
            // So if you don't want them to connect you can just skip the rest.
            double nextAngle = Math.toRadians(360D / points * (point + connection));

            // Size is basically the circle's radius.
            // Get our X and Z position based on the angle of the point.
            double x = Math.cos(angle) * size;
            double z = Math.sin(angle) * size;

            double x2 = Math.cos(nextAngle) * size;
            double z2 = Math.sin(nextAngle) * size;

            // The distance between one point to another.
            double deltaX = x2 - x;
            double deltaZ = z2 - z;

            // Connect the points.
            // Extend value is a little complicated Idk how to explain it.
            // Might be related: https://en.wikipedia.org/wiki/Hypercube
            for (double pos = 0; pos < 1 + extend; pos += rate) {
                display.spawn(x + (deltaX * pos), 0, z + (deltaZ * pos));
            }
        }
    }

    /**
     * Rotates vector around the X axis with the specified angle.
     * Cross-version compatibility.
     *
     * @param angle the rotation angle, in radians.
     * @return the rotated vector.
     * @since 1.0.0
     */
    public static Vector rotateAroundX(Vector vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double y = vector.getY() * cos - vector.getZ() * sin;
        double z = vector.getY() * sin + vector.getZ() * cos;
        return vector.setY(y).setZ(z);
    }

    /**
     * Rotates vector around the Y axis with the specified angle.
     * Cross-version compatibility.
     *
     * @param angle the rotation angle, in radians.
     * @return the rotated vector.
     * @since 1.0.0
     */
    public static Vector rotateAroundY(Vector vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double x = vector.getX() * cos + vector.getZ() * sin;
        double z = vector.getX() * -sin + vector.getZ() * cos;
        return vector.setX(x).setZ(z);
    }

    /**
     * Rotates vector around the Z axis with the specified angle.
     * Cross-version compatibility.
     *
     * @param angle the rotation angle, in radians.
     * @return the rotated vector.
     * @since 1.0.0
     */
    public static Vector rotateAroundZ(Vector vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double x = vector.getX() * cos - vector.getY() * sin;
        double y = vector.getX() * sin + vector.getY() * cos;
        return vector.setX(x).setY(y);
    }

    /**
     * https://upload.wikimedia.org/wikipedia/commons/thumb/9/9a/Pentagram_within_circle.svg/800px-Pentagram_within_circle.svg.png
     *
     * @see #polygon(int, int, double, double, double, ParticleDisplay)
     * @see #circle(double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void neopaganPentagram(double size, double rate, double extend, ParticleDisplay star, ParticleDisplay circle) {
        polygon(5, 2, size, rate, extend, star);
        circle(size + 0.5, rate * 1000, circle);
    }

    /**
     * Spawns an atom with orbits and a nucleus.
     *
     * @param orbits the number of atom orbits.
     * @param radius the radius of orbits.
     * @param rate   the rate of orbit and nucleus points.
     * @see #atomic(JavaPlugin, int, double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void atom(int orbits, double radius, double rate, ParticleDisplay orbit, ParticleDisplay nucleus) {
        double dist = Math.PI / orbits;
        for (double angle = 0; orbits > 0; angle += dist) {
            orbit.rotation = new Vector(0, 0, angle);
            circle(radius, rate, orbit);
            orbits--;
        }

        sphere(radius / 3, rate / 2, nucleus);
    }

    /**
     * This is supposed to be something similar to this: https://www.deviantart.com/pwincessstar/art/701840646
     * The numbers on this shape are really sensitive. Changing a single one can result
     * in a totally different shape.
     *
     * @param size the shape of the explosion circle. Recommended value is 6
     * @see #polygon(int, int, double, double, double, ParticleDisplay)
     * @see #circle(double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void meguminExplosion(JavaPlugin plugin, double size, ParticleDisplay display) {
        polygon(10, 4, size, 0.02, 0.3, display);
        polygon(10, 3, size / (size - 1), 0.5, 0, display);
        circle(size, 40, display);
        spread(plugin, 30, 2, display.location, display.location.clone().add(0, 10, 0), 5, 5, 5, display);
    }

    /**
     * A sin/cos based smoothly animated explosion wave.
     * Source: https://www.youtube.com/watch?v=n8W7RxW5KB4
     *
     * @param rate the distance between each cos/sin lines.
     * @since 1.0.0
     */
    public static void explosionWave(JavaPlugin plugin, double rate, ParticleDisplay display, ParticleDisplay secDisplay) {
        new BukkitRunnable() {
            double t = Math.PI / 4;
            double addition = Math.PI * 0.1;

            public void run() {
                t += addition;
                for (double theta = 0; theta <= PII; theta = theta + Math.PI / rate) {
                    double x = t * Math.cos(theta);
                    double y = 2 * Math.exp(-0.1 * t) * Math.sin(t) + 1.5;
                    double z = t * Math.sin(theta);

                    Location loc = display.location.clone().add(x, y, z);
                    loc.getWorld().spawnParticle(display.particle, loc, display.count, display.offsetx, display.offsety, display.offsetz, display.extra);

                    theta = theta + Math.PI / 64;
                    x = t * Math.cos(theta);
                    y = 2 * Math.exp(-0.1 * t) * Math.sin(t) + 1.5;
                    z = t * Math.sin(theta);

                    loc = display.location.clone().add(x, y, z);
                    loc.getWorld().spawnParticle(secDisplay.particle, loc, secDisplay.count,
                            secDisplay.offsetx, secDisplay.offsety, secDisplay.offsetz, secDisplay.extra);
                }

                if (t > 20) cancel();
            }

        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    /**
     * Reads an Image from the given path.
     *
     * @param path the path of the image.
     * @return a buffered image.
     * @since 1.0.0
     */
    private static BufferedImage getImage(Path path) {
        if (!Files.exists(path)) return null;
        try {
            return ImageIO.read(Files.newInputStream(path, StandardOpenOption.READ));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Resizes an image maintaining aspect ratio (kinda).
     *
     * @param path   the path of the image.
     * @param width  the new width.
     * @param height the new height.
     * @return the resized image.
     * @since 1.0.0
     */
    private static CompletableFuture<BufferedImage> getScaledImage(Path path, int width, int height) {
        return CompletableFuture.supplyAsync(() -> {
            BufferedImage image = getImage(path);
            if (image == null) return null;
            int finalHeight = height;
            int finalWidth = width;

            if (image.getWidth() > image.getHeight()) {
                finalHeight = width * image.getHeight() / image.getWidth();
            } else {
                finalWidth = height * image.getWidth() / image.getHeight();
            }

            BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = resizedImg.createGraphics();

            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.drawImage(image, 0, 0, finalWidth, finalHeight, null);
            graphics.dispose();
            return resizedImg;
        });
    }

    /**
     * Renders a resized image.
     *
     * @param path          the path of the image.
     * @param resizedWidth  the resizing width.
     * @param resizedHeight the resizing height.
     * @param compact       the pixel compact of the image.
     * @return the rendered particle locations.
     * @since 1.0.0
     */
    public static CompletableFuture<Map<Location, Color>> renderImage(Path path, int resizedWidth, int resizedHeight, double compact) {
        return getScaledImage(path, resizedWidth, resizedHeight).thenCompose((image) -> renderImage(image, resizedWidth, resizedHeight, compact));
    }

    /**
     * Renders every pixel of the image and saves the location and
     * the particle colors to a map.
     *
     * @param image         the image to render.
     * @param resizedWidth  the new image width.
     * @param resizedHeight the new image height.
     * @param compact       particles compact value. Should be lower than 0.5 and higher than 0.1 The recommended value is 0.2
     * @return a rendered map of an image.
     * @since 1.0.0
     */
    public static CompletableFuture<Map<Location, Color>> renderImage(BufferedImage image, int resizedWidth, int resizedHeight, double compact) {
        return CompletableFuture.supplyAsync(() -> {
            if (image == null) return null;

            double centerX = image.getWidth() / 2D;
            double centerY = image.getHeight() / 2D;

            Map<Location, Color> rendered = new HashMap<>();
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int pixel = image.getRGB(x, y);

                    // Transparency
                    if ((pixel >> 24) == 0x00) continue;
                    // 0 - 255
                    //if ((pixel & 0xff000000) >>> 24 == 0) continue;
                    // 0.0 - 1.0
                    //if (pixel == java.awt.Color.TRANSLUCENT) continue;

                    java.awt.Color color = new java.awt.Color(pixel);
                    int r = color.getRed();
                    int g = color.getGreen();
                    int b = color.getBlue();

                    Color bukkitColor = Color.fromRGB(r, g, b);
                    rendered.put(new Location(null, (x - centerX) * compact, (y - centerY) * compact, 0), bukkitColor);
                }
            }
            return rendered;
        });
    }

    /**
     * Display a rendered image repeatedly.
     *
     * @param plugin   the scheduler handler.
     * @param render   the rendered image map.
     * @param location the dynamic location to display the image at.
     * @param repeat   amount of times to repeat displaying the image.
     * @param period   the perioud between each repeats.
     * @param quality  the quality of the image is exactly the number of particles display for each pixel. Recommended value is 1
     * @param speed    the speed is exactly the same value as the speed of particles. Recommended amount is 0
     * @param size     the size of the particle. Recommended amount is 0.8
     * @return the async bukkit task displaying the image.
     * @since 1.0.0
     */
    public static BukkitTask displayRenderedImage(JavaPlugin plugin, Map<Location, Color> render, Callable<Location> location,
                                                  int repeat, long period, int quality, int speed, float size) {
        return new BukkitRunnable() {
            int times = repeat;

            @Override
            public void run() {
                try {
                    displayRenderedImage(render, location.call(), quality, speed, size);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (times-- < 1) cancel();
            }
        }.runTaskTimerAsynchronously(plugin, 0L, period);
    }

    /**
     * Display a rendered image repeatedly.
     *
     * @param render   the rendered image map.
     * @param location the dynamic location to display the image at.
     * @param quality  the quality of the image is exactly the number of particles display for each pixel. Recommended value is 1
     * @param speed    the speed is exactly the same value as the speed of particles. Recommended amount is 0
     * @param size     the size of the particle. Recommended amount is 0.8
     * @since 1.0.0
     */
    public static void displayRenderedImage(Map<Location, Color> render, Location location, int quality, int speed, float size) {
        for (Map.Entry<Location, Color> pixel : render.entrySet()) {
            Particle.DustOptions data = new Particle.DustOptions(pixel.getValue(), size);
            Location pixelLoc = pixel.getKey();

            Location loc = new Location(location.getWorld(), location.getX() - pixelLoc.getX(),
                    location.getY() - pixelLoc.getY(), location.getZ() - pixelLoc.getZ());
            loc.getWorld().spawnParticle(Particle.REDSTONE, loc, quality, 0, 0, 0, speed, data);
        }
    }

    /**
     * A simple method used to save images. Useful to cache text generated images.
     *
     * @param image the buffered image to save.
     * @param path  the path to save the image to.
     * @see #stringToImage(Font, java.awt.Color, String)
     * @since 1.0.0
     */
    public static void saveImage(BufferedImage image, Path path) {
        try {
            ImageIO.write(image, "png", Files.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts a string to an image which can be used to display as a particle.
     *
     * @param font  the font to generate the text with.
     * @param color the color of text.
     * @param str   the string to generate the image.
     * @return the buffered image.
     * @see #saveImage(BufferedImage, Path)
     * @since 1.0.0
     */
    public static CompletableFuture<BufferedImage> stringToImage(Font font, java.awt.Color color, String str) {
        return CompletableFuture.supplyAsync(() -> {
            BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setFont(font);

            FontRenderContext context = graphics.getFontMetrics().getFontRenderContext();
            Rectangle2D frame = font.getStringBounds(str, context);
            graphics.dispose();

            image = new BufferedImage((int) Math.ceil(frame.getWidth()), (int) Math.ceil(frame.getHeight()), BufferedImage.TYPE_INT_ARGB);
            graphics = image.createGraphics();
            graphics.setColor(color);
            graphics.setFont(font);

            graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            FontMetrics metrics = graphics.getFontMetrics();
            graphics.drawString(str, 0, metrics.getAscent());
            graphics.dispose();

            return image;
        });
    }
}
