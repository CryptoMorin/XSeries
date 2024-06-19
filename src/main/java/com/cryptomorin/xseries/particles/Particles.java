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

import org.bukkit.Color;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.NumberConversions;
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
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * <b>XParticle</b> - The most unique particle animation, text and image renderer.<br>
 * This utility uses {@link ParticleDisplay} for cleaner code. This class adds the ability
 * to define the optional values for spawning particles.
 * <p>
 * While this class provides many methods with options to spawn unique shapes,
 * it's recommended to make your own shapes by copying the code from these methods.<br>
 * There are some shapes such as the magic circles, illuminati and the explosion method
 * that mainly focus on using the other methods to create a new shape.
 * <p>
 * Note that some of the values for some methods are extremely sensitive and can change
 * the shape significantly by adding small numbers such as 0.5 Yes, Chaos theory.<br>
 * Most of the method parameters have a recommended value set to start with.
 * Note that these values are there to show how the intended normal shape
 * looks like before you start changing the values.<br>
 * All the parameters and return types are not null.
 * <p>
 * It's recommended to use low particle counts.
 * In most cases, increasing the rate is better than increasing the particle count.
 * Most of the methods provide an option called "rate" that you can get more particles
 * by decreasing the distance between each point the particle spawns.
 * Rates for methods act in two ways. They're either for straight lines like the polygon
 * method which lower rate means more points (usually 0.1 is used) and shapes that are curved such as
 * the circle method, which higher rate means more points (these types of rates usually start from 30).<br>
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
 * the clients, unless they have a powerful PC.
 * <p>
 * You can test your 2D shapes at <a href="https://www.desmos.com/calculator">Desmos</a><br>
 * Stuff you can do with with
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Math.html">Java {@link Math}</a><br>
 * Getting started with <a href="https://www.spigotmc.org/wiki/vector-programming-for-beginners/">Vectors</a><br>
 * Extra stuff if you want to read more: https://www.spigotmc.org/threads/418399/<br>
 * Particles: https://minecraft.wiki/w/Particles<br>
 * <p>
 * This class also uses {@link BooleanSupplier} and {@link Runnable} for repeating/delayed tasks
 * in order to be compatible with other server softwares such as <a href="https://papermc.io/software/folia">Folia</a>.
 *
 * @author Crypto Morin
 * @version 7.2.0
 * @see ParticleDisplay
 * @see Particle
 * @see Location
 * @see Vector
 */
@SuppressWarnings("JavadocLinkAsPlainText")
public final class Particles {
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
     *     PI = 180 degrees
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
    public static final double
            PII = 2 * Math.PI,
            R270 = Math.toRadians(270),
            R90 = Math.PI / 2;

    private Particles() {
    }

    /**
     * Get a random particle from a list of particle names.
     *
     * @param particles the particles name.
     * @return a random particle from the list.
     * @since 1.0.0
     */
    public static Optional<XParticle> randomParticle(String... particles) {
        int rand = randInt(0, particles.length - 1);
        return XParticle.of(particles[rand]);
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
        float size = randInt(5, 10) / 10f;
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
     *
     * @param radius the circle radius.
     * @param rate   the rate of cirlce points/particles.
     * @see #sphere(double, double, ParticleDisplay)
     * @see #circle(double, double, double, double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void circle(double radius, double rate, ParticleDisplay display) {
        circle(radius, radius, 1, rate, 0, display);
    }

    /**
     * Spawns a circle.
     * Most common shapes that can be built:
     * <pre>
     *     The simplest shape, a circle
     *     circle(3, 3, 1, 30, 0, display);
     *
     *     An ellipse only has a different radius for one of its waves.
     *     circle(3, 4, 1, 30, 0, display);
     * </pre>
     * <p>
     * Tutorial: https://www.spigotmc.org/threads/111238/
     * Uses its own unique directional pattern.
     *
     * @param radius    the first radius of the circle.
     * @param radius2   the second radius of the circle.
     * @param extension the extension of the circle waves.
     * @param rate      the rate of the circle points.
     * @param limit     the limit of the circle. Usually from 0 to PII.
     *                  If you choose 0, it'll be a full circle {@link #PII}
     *                  If you choose -1, it'll do a full loop based on the extension.
     * @see #illuminati(double, double, ParticleDisplay)
     * @see #eye(double, double, double, double, ParticleDisplay)
     */
    public static void circle(double radius, double radius2, double extension, double rate, double limit, ParticleDisplay display) {
        // 180 degrees = PI
        // We need a full circle, 360 so we need two pies!
        // https://www.spigotmc.org/threads/176792/
        // cos and sin methods only accept radians.
        // Converting degrees to radians is not resource intensive. It's a really simple operation.
        // However we can skip the conversion by using radians in the first place.
        double rateDiv = Math.PI / Math.abs(rate);

        // If no limit is specified do a full loop.
        if (limit == 0) limit = PII;
        else if (limit == -1) limit = PII / Math.abs(extension);
        // If the extension changes (isn't 1), the wave might not do a full
        // loop anymore. So by simply dividing PI from the extension you can get the limit for a full loop.
        // By full loop it means: sin(bx) {0 < x < PI} if b (the extension) is equal to 1
        // Using period => T = 2PI/|b|

        for (double theta = 0; theta <= limit; theta += rateDiv) {
            // In order to curve our straight line in the loop, we need to
            // use cos and sin. It doesn't matter, you can get x as sin and z as cos.
            // But you'll get weird results if you use si+n or cos for both or using tan or cot.
            double x = radius * Math.cos(extension * theta);
            double z = radius2 * Math.sin(extension * theta);

            if (display.isDirectional()) {
                // We're going to get the angle in these two coordinates.
                // Then we can spread each particle in the right angle.
                double phi = Math.atan2(z, x);
                double directionX = Math.cos(extension * phi);
                double directionZ = Math.sin(extension * phi);

                display.particleDirection(directionX, display.getOffset().getY(), directionZ);
            }

            display.spawn(x, 0, z);
        }
    }

    /**
     * Spawns a diamond-shaped rhombus.
     *
     * @param radiusRate the radius of the diamond. Lower means longer radius.
     * @param rate       the rate of the diamond points.
     * @param height     the height of the diamond.
     * @since 4.0.0
     */
    public static void diamond(double radiusRate, double rate, double height, ParticleDisplay display) {
        double count = 0;
        for (double y = 0; y < height * 2; y += rate) {
            // We're going to increase our x particles as we get closer to the center
            // and decrease as we move away. If the radius is equal to rate it'll form a rotated square.
            if (y < height) count += radiusRate;
            else count -= radiusRate;

            // Now we can make an arrow or a right triangle if let x be equal to 0
            // But we want both sides to have particle.
            for (double x = -count; x < count; x += rate) display.spawn(x, y, 0);
        }
    }

    /**
     * Spawns connected 3D ellipses.
     *
     * @param maxRadius  the maximum radius for the ellipses.
     * @param rate       the rate of the 3D ellipses circle points.
     * @param radiusRate the rate of the circle radius change.
     * @param extend     the extension for each ellipse.
     * @return the animation runnable.
     * @see #magicCircles(double, double, double, double, ParticleDisplay)
     * @since 3.0.0
     */
    public static Runnable circularBeam(double maxRadius, double rate, double radiusRate, double extend, ParticleDisplay display) {
        return new Runnable() {
            final double rateDiv = Math.PI / rate;
            final double radiusDiv = Math.PI / radiusRate;
            final Vector dir = display.getLocation().getDirection().normalize().multiply(extend);
            double dynamicRadius = 0;

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
                display.getLocation().add(dir);
            }
        };
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
     * @see #magicCircles(Plugin, double, double, double, double, ParticleDisplay)
     * @since 3.0.0
     */
    public static BukkitTask circularBeam(Plugin plugin, double maxRadius, double rate, double radiusRate, double extend, ParticleDisplay display) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, circularBeam(maxRadius, rate, radiusRate, extend, display), 0, 1);
    }

    /**
     * Spawns the given shape(s) in the runnable in a circular form.
     * The distance between the shapes are evenly separated.
     *
     * @param count    the count of the shapes.
     * @param radius   the radius of the circular form.
     * @param runnable the shape(s) to display.
     * @since 4.0.0
     */
    public static void flower(int count, double radius, ParticleDisplay display, Runnable runnable) {
        for (double theta = 0; theta < PII; theta += PII / count) {
            double x = radius * Math.cos(theta);
            double z = radius * Math.sin(theta);

            display.getLocation().add(x, 0, z);
            runnable.run();
            display.getLocation().subtract(x, 0, z);
        }
    }

    /**
     * Spawns a filled circle using circles.
     *
     * @param radius     the radius of the circle.
     * @param rate       the rate of the circle points.
     * @param radiusRate the radius change of the circle to fill it.
     * @see #circle(double, double, ParticleDisplay)
     * @since 4.0.0
     */
    public static void filledCircle(double radius, double rate, double radiusRate, ParticleDisplay display) {
        double dynamicRate = 0;
        for (double i = 0.1; i < radius; i += radiusRate) {
            // noinspection ConstantValue
            if (i > radius) i = radius;
            dynamicRate += rate / (radius / radiusRate);
            circle(i, dynamicRate, display);
        }
    }

    /**
     * Spawns a double pendulum with chaotic movement.
     * Note that if this runs for too long it'll stop working due to
     * the limit of doubles resulting in a {@link Double#NaN}
     * <p>
     * <a href="https://en.wikipedia.org/wiki/Double_pendulum">Double pendulum</a>
     * is a way to show <a href="https://en.wikipedia.org/wiki/Chaos_theory">Chaos motion</a>.
     * The particles display are showing the path where the second
     * pendulum is going from.
     * <p>
     * Changing the mass or length to a lower value can make the
     * shape stop producing new paths since it reaches the doubles limit.
     * Source: <a href="https://www.myphysicslab.com/pendulum/double-pendulum-en.html">myphysicslab</a>
     *
     * @param radius     the radius of the pendulum. Yes this doesn't depend on length since the length needs to be a really
     *                   high value and this won't work with Minecraft's xyz.
     * @param gravity    the gravity of the enviroment. Recommended is -1 positive numbers will mean gravity towards space.
     * @param length     the length of the first pendulum. Recommended is 200
     * @param length2    the length of the second pendulum. Recommended is 200
     * @param mass1      the mass of the first pendulum. Recommended is 50
     * @param mass2      the mass of the second pendulum. Recommended is 50
     * @param dimension3 if it should enter 3D mode.
     * @return the animation runnable.
     * @since 4.0.0
     */
    public static Runnable chaoticDoublePendulum(double radius, double gravity, double length, double length2,
                                                 double mass1, double mass2,
                                                 boolean dimension3, int speed, ParticleDisplay display) {
        // If you want the particles to stay. But it's gonna lag a lot.
        // Map<Vector, Vector> locs = new HashMap<>();

        return new Runnable() {
            double theta = Math.PI / 2;
            double theta2 = Math.PI / 2;
            double thetaPrime = 0;
            double thetaPrime2 = 0;

            @Override
            public void run() {
                int repeat = speed;
                while (repeat-- != 0) {
                    if (dimension3) display.rotate(Math.PI / 33, Math.PI / 44, Math.PI / 55);
                    double totalMass = mass1 + mass2;
                    double totalMassDouble = 2 * totalMass;
                    double deltaTheta = theta - theta2;

                    double lenLunar = (totalMassDouble - mass2 * Math.cos(2 * theta - 2 * theta2));
                    double deltaCosTheta = Math.cos(deltaTheta);
                    double deltaSinTheta = Math.sin(deltaTheta);
                    double phi = thetaPrime * thetaPrime * length;
                    double phi2 = thetaPrime2 * thetaPrime2 * length2;

                    // Don't expect me to explain these... Read the website.
                    double num1 = -gravity * totalMassDouble * Math.sin(theta);
                    double num2 = -mass2 * gravity * Math.sin(theta - 2 * theta2);
                    double num3 = -2 * deltaSinTheta * mass2;
                    double num4 = phi2 + phi * deltaCosTheta;
                    double len = length * lenLunar;
                    double thetaDoublePrime = (num1 + num2 + num3 * num4) / len;

                    num1 = 2 * deltaSinTheta;
                    num2 = phi * totalMass;
                    num3 = gravity * totalMass * Math.cos(theta);
                    num4 = phi2 * mass2 * deltaCosTheta;
                    len = length2 * lenLunar;
                    double thetaDoublePrime2 = (num1 * (num2 + num3 + num4)) / len;

                    thetaPrime += thetaDoublePrime;
                    thetaPrime2 += thetaDoublePrime2;
                    theta += thetaPrime;
                    theta2 += thetaPrime2;

                    double x = radius * Math.sin(theta);
                    double y = radius * Math.cos(theta);
                    double x2 = x + radius * Math.sin(theta2);
                    double y2 = y + radius * Math.cos(theta2);

                    display.spawn(x2, y2, 0);

//                locs.forEach((v, v2) -> {
//                    ParticleDisplay dis = display.clone();
//                    dis.rotation = v2;
//                    dis.spawn(v.getX(), v.getY(), v.getZ());
//                });
//                locs.put(new Vector(x2, y2, 0), display.rotation.clone());
                }
            }
        };
    }

    /**
     * Spawns a double pendulum with chaotic movement.
     * Note that if this runs for too long it'll stop working due to
     * the limit of doubles resulting in a {@link Double#NaN}
     * <p>
     * <a href="https://en.wikipedia.org/wiki/Double_pendulum">Double pendulum</a>
     * is a way to show <a href="https://en.wikipedia.org/wiki/Chaos_theory">Chaos motion</a>.
     * The particles display are showing the path where the second
     * pendulum is going from.
     * <p>
     * Changing the mass or length to a lower value can make the
     * shape stop producing new paths since it reaches the doubles limit.
     * <a href="https://www.myphysicslab.com/pendulum/double-pendulum-en.html">Source</a>
     *
     * @param plugin     the timer handler.
     * @param radius     the radius of the pendulum. Yes this doesn't depend on length since the length needs to be a really
     *                   high value and this won't work with Minecraft's xyz.
     * @param gravity    the gravity of the enviroment. Recommended is -1 positive numbers will mean gravity towards space.
     * @param length     the length of the first pendulum. Recommended is 200
     * @param length2    the length of the second pendulum. Recommended is 200
     * @param mass1      the mass of the first pendulum. Recommended is 50
     * @param mass2      the mass of the second pendulum. Recommended is 50
     * @param dimension3 if it should enter 3D mode.
     * @param speed      the speed of the animation.
     * @return the animation handler.
     * @since 4.0.0
     */
    public static BukkitTask chaoticDoublePendulum(Plugin plugin, double radius, double gravity, double length, double length2,
                                                   double mass1, double mass2,
                                                   boolean dimension3, int speed, ParticleDisplay display) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, chaoticDoublePendulum(radius, gravity, length, length2, mass1, mass2, dimension3, speed, display), 0, 1);
    }

    /**
     * Spawns circles increasing their radius.
     *
     * @param radius     the radius for the first circle.
     * @param rate       the rate of circle points.
     * @param radiusRate the circle radius change rate.
     * @param distance   the distance between each circle.
     * @return the animation handler.
     * @see #circularBeam(Plugin, double, double, double, double, ParticleDisplay)
     * @since 3.0.0
     */
    public static Runnable magicCircles(double radius, double rate, double radiusRate, double distance, ParticleDisplay display) {
        return new Runnable() {
            final double radiusDiv = Math.PI / radiusRate;
            final Vector dir = display.getLocation().getDirection().normalize().multiply(distance);
            double dynamicRadius = radius;

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
                display.getLocation().add(dir);
            }
        };
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
     * @see #circularBeam(Plugin, double, double, double, double, ParticleDisplay)
     * @since 3.0.0
     */
    public static BukkitTask magicCircles(Plugin plugin, double radius, double rate, double radiusRate, double distance, ParticleDisplay display) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, magicCircles(radius, rate, radiusRate, distance, display), 0, 1);
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
     * An example of a shash particle.
     *
     * @param size        1 would be approx the size of the player.
     * @param useWideSide Whether to use the wide or narrow slash.
     * @since 7.0.0
     */
    public static void slash(double size, boolean useWideSide, ParticleDisplay display) {
        double start = useWideSide ? R90 : 0;
        double end = useWideSide ? R270 : Math.PI;
        Particles.ellipse(
                start, end,
                Math.PI / 30,
                size, size + 2,
                display
        );
    }

    public static void slash(Plugin plugin, double distance, boolean useWideSide,
                             Supplier<Double> size, Supplier<Double> speed, ParticleDisplay display) {
        new BukkitRunnable() {
            double distanceTraveled = 0;

            @Override
            public void run() {
                slash(size.get(), useWideSide, display);
                double speedConst = speed.get();
                distanceTraveled += speedConst;

                if (distanceTraveled >= distance) cancel();
                else display.advanceInDirection(speedConst);
            }
        }.runTaskTimerAsynchronously(plugin, 1L, 1L);
    }

    /**
     * Spawn an ellipse.
     *
     * @see #circle(double, double, ParticleDisplay)
     * @since 2.0.0
     */
    public static void ellipse(double start, double end, double rate, double radius, double otherRadius, ParticleDisplay display) {
        // The only difference between circles and ellipses are that
        // ellipses use a different radius for one of their axis.
        for (double theta = start; theta <= end; theta += rate) {
            double x = radius * Math.cos(theta);
            double z = otherRadius * Math.sin(theta);
            display.spawn(x, 0, z);
        }
    }

    /**
     * Spawn a blackhole.
     *
     * @param points the points of the blackhole pulls.
     * @param radius the radius of the blackhole circle.
     * @param rate   the rate of the blackhole circle points.
     * @param mode   blackhole mode. There are 5 modes.
     * @param time   the amount of ticks to keep the blackhole.
     * @return the blackhole runnable. It will return false when the blackhole is done.
     * @since 3.0.0
     */
    public static BooleanSupplier blackhole(int points, double radius, double rate, int mode, int time, ParticleDisplay display) {
        display.extra = 0.1;

        return new BooleanSupplier() {
            final double rateDiv = Math.PI / rate;
            int timer = time;
            double theta = 0;
            boolean done = false;

            @Override
            public boolean getAsBoolean() {
                if (done) return false;

                for (int i = 0; i < points; i++) {
                    // Spawn a circle.
                    double angle = PII * ((double) i / points);
                    double x = radius * Math.cos(theta + angle);
                    double z = radius * Math.sin(theta + angle);

                    // Set the angle of the circle point as its degree.
                    double phi = Math.atan2(z, x);
                    double xDirection = -Math.cos(phi);
                    double zDirection = -Math.sin(phi);

                    display.particleDirection(xDirection, 0, zDirection);
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

                        display.particleDirection(xDirection, 0, zDirection);
                        display.spawn(x, 0, z);
                    }
                }

                theta += rateDiv;

                if (--timer <= 0) {
                    done = true;
                    return false;
                }
                return true;
            }
        };
    }

    /**
     * Spawn a blackhole.
     *
     * @param plugin the timer handler.
     * @param points the points of the blackhole pulls.
     * @param radius the radius of the blackhole circle.
     * @param rate   the rate of the blackhole circle points.
     * @param mode   blackhole mode. There are 5 modes.
     * @param time   the amount of ticks to keep the blackhole.
     * @since 3.0.0
     */
    public static BukkitTask blackhole(Plugin plugin, int points, double radius, double rate, int mode, int time, ParticleDisplay display) {
        BooleanSupplier blackhole = blackhole(points, radius, rate, mode, time, display);
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (!blackhole.getAsBoolean()) cancel();
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
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
        int[][] rainbow = {
                {128, 0, 128}, // Violet
                {75, 0, 130}, // Indigo
                {0, 0, 255}, // Blue
                {0, 255, 0}, // Green
                {255, 255, 0}, // Yellow
                {255, 140, 0}, // Orange
                {255, 0, 0} // Red
        };
        double secondRadius = radius * curve;

        // Rainbows have 7 colors.
        // Refer to RAINBOW constant for the color order.
        for (int i = 0; i < 7; i++) {
            // Get the rainbow color in order.
            int[] rgb = rainbow[i];
            display = ParticleDisplay.colored(display.getLocation(), rgb[0], rgb[1], rgb[2], 1);

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
        double end = Math.toRadians(325);

        // Crescents are two circles, one with a smaller radius and slightly shifted to the open part of the bigger circle.
        // To align the opening of the bigger circle with the +X axis we'll have to adjust our start and end  radians.
        for (double theta = Math.toRadians(45); theta <= end; theta += rateDiv) {
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
     * @param points the points of the vortex.
     * @param rate   the speed of the vortex.
     * @return the task handling the animation.
     * @since 2.0.0
     */
    public static Runnable vortex(int points, double rate, ParticleDisplay display) {
        double rateDiv = Math.PI / rate;

        return new Runnable() {
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

                    display.particleDirection(xDirection, 0, zDirection);
                    display.spawn(x, 0, z);
                }
            }
        };
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
    public static BukkitTask vortex(Plugin plugin, int points, double rate, ParticleDisplay display) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, vortex(points, rate, display), 0, 1);
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
        filledCircle(radius, rate, 3, display);
        filledCircle(radius, rate, 3, display.cloneWithLocation(0, height, 0));
        for (double y = 0; y < height; y += 0.1) {
            circle(radius, rate, display.cloneWithLocation(0, y, 0));
        }
    }

    /**
     * This will move the shape around in an area randomly while rotating them.
     * The position of the shape will be randomized positively and negatively by the offset parameters on each axis.
     *
     * @param rate     the distance between each location. Recommended value is 5.
     * @param runnable the particles to spawn.
     * @param displays the display references used to spawn particles in the runnable.
     * @return the async task handling the movement.
     * @see #rotateAround(Plugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @see #guard(Plugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @since 1.0.0
     */
    public static Runnable moveRotatingAround(double rate, double offsetx, double offsety, double offsetz,
                                              Runnable runnable, ParticleDisplay... displays) {
        return new Runnable() {
            double rotation = 180;

            @Override
            public void run() {
                rotation += rate;
                // Generate random radians.
                double x = Math.toRadians(90 + rotation);
                double y = Math.toRadians(60 + rotation);
                double z = Math.toRadians(30 + rotation);

                Vector vector = new Vector(offsetx * Math.PI, offsety * Math.PI, offsetz * Math.PI);
                if (offsetx != 0) ParticleDisplay.rotateAround(vector, ParticleDisplay.Axis.X, x);
                if (offsety != 0) ParticleDisplay.rotateAround(vector, ParticleDisplay.Axis.Y, y);
                if (offsetz != 0) ParticleDisplay.rotateAround(vector, ParticleDisplay.Axis.Z, z);

                for (ParticleDisplay display : displays) display.getLocation().add(vector);
                runnable.run();
                for (ParticleDisplay display : displays) display.getLocation().subtract(vector);
            }
        };
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
     * @see #rotateAround(Plugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @see #guard(Plugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @since 1.0.0
     */
    public static BukkitTask moveRotatingAround(Plugin plugin, long update, double rate, double offsetx, double offsety, double offsetz,
                                                Runnable runnable, ParticleDisplay... displays) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, moveRotatingAround(rate, offsetx, offsety, offsetz, runnable, displays), 0, update);
    }

    /**
     * This will move the particle around in an area randomly.
     * The position of the shape will be randomized positively and negatively by the offset parameters on each axis.
     *
     * @param rate     the distance between each location. Recommended value is 5.
     * @param runnable the particles to spawn.
     * @param displays the display references used to spawn particles in the runnable.
     * @return the runnable handling the movement.
     * @see #rotateAround(double, double, double, double, Runnable, ParticleDisplay...)
     * @see #guard(double, double, double, double, Runnable, ParticleDisplay...)
     * @since 1.0.0
     */
    public static Runnable moveAround(double rate, double endRate, double offsetx, double offsety, double offsetz,
                                      Runnable runnable, ParticleDisplay... displays) {
        return new Runnable() {
            double multiplier = 0;
            boolean opposite = false;

            @Override
            public void run() {
                if (opposite) multiplier -= rate;
                else multiplier += rate;

                double x = multiplier * offsetx;
                double y = multiplier * offsety;
                double z = multiplier * offsetz;

                for (ParticleDisplay display : displays) display.getLocation().add(x, y, z);
                runnable.run();
                for (ParticleDisplay display : displays) display.getLocation().subtract(x, y, z);

                if (opposite) {
                    if (multiplier <= 0) opposite = false;
                } else {
                    if (multiplier >= endRate) opposite = true;
                }
            }
        };
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
     * @see #rotateAround(Plugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @see #guard(Plugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @since 1.0.0
     */
    public static BukkitTask moveAround(Plugin plugin, long update, double rate, double endRate, double offsetx, double offsety, double offsetz,
                                        Runnable runnable, ParticleDisplay... displays) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, moveAround(rate, endRate, offsetx, offsety, offsetz, runnable, displays), 0, update);
    }

    /**
     * A simple test method to spawn a shape repeatedly for diagnosis.
     *
     * @param plugin   the timer handler.
     * @param runnable the shape(s) to display.
     * @return the timer task handling the displays.
     * @since 1.0.0
     */
    public static BukkitTask testDisplay(Plugin plugin, Runnable runnable) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, 0L, 1L);
    }

    /**
     * This will rotate the shape around in an area randomly.
     * The position of the shape will be randomized positively and negatively by the offset parameters on each axis.
     *
     * @param rate     the distance between each location. Recommended value is 5.
     * @param runnable the particles to spawn.
     * @param displays the displays references used to spawn particles in the runnable.
     * @return the runnable handling the movement.
     * @see #moveRotatingAround(double, double, double, double, Runnable, ParticleDisplay...)
     * @see #guard(double, double, double, double, Runnable, ParticleDisplay...)
     * @since 1.0.0
     */
    public static Runnable rotateAround(double rate, double offsetx, double offsety, double offsetz,
                                        Runnable runnable, ParticleDisplay... displays) {
        return new Runnable() {
            double rotation = 180;

            @Override
            public void run() {
                rotation += rate;
                double x = Math.toRadians((90 + rotation) * offsetx);
                double y = Math.toRadians((60 + rotation) * offsety);
                double z = Math.toRadians((30 + rotation) * offsetz);

                for (ParticleDisplay display : displays) display.rotate(x, y, z);
                runnable.run();
            }
        };
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
     * @see #moveRotatingAround(Plugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @see #guard(Plugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @since 1.0.0
     */
    public static BukkitTask rotateAround(Plugin plugin, long update, double rate, double offsetx, double offsety, double offsetz,
                                          Runnable runnable, ParticleDisplay... displays) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, rotateAround(rate, offsetx, offsety, offsetz, runnable, displays), 0, update);
    }

    /**
     * This will move the particle around in an area randomly.
     * The position of the shape will be randomized positively and negatively by the offset parameters on each axis.
     * Note that the ParticleDisplays used in runnable and displays options must be from the same reference.
     * <p>
     * <b>Example</b>
     * <pre>
     *     ParticleDisplays display = new ParticleDisplay(...);
     *     {@code WRONG: moveAround(5, 1.5, 1.5, 1.5, () -> circle(1, 10, new ParticleDisplay(...)), display);}
     *     {@code CORRECT: moveAround(5, 1.5, 1.5, 1.5, () -> circle(1, 10, display), display);}
     * </pre>
     *
     * @param rate     the distance between each location. Recommended value is 5.
     * @param runnable the particles to spawn.
     * @param displays the displays references used to spawn particles in the runnable.
     * @return the async task handling the movement.
     * @see #rotateAround(double, double, double, double, Runnable, ParticleDisplay...)
     * @see #moveRotatingAround(double, double, double, double, Runnable, ParticleDisplay...)
     * @since 1.0.0
     */
    public static Runnable guard(double rate, double offsetx, double offsety, double offsetz,
                                 Runnable runnable, ParticleDisplay... displays) {
        return new Runnable() {
            double rotation = 180;

            @Override
            public void run() {
                rotation += rate;
                double x = Math.toRadians((90 + rotation) * offsetx);
                double y = Math.toRadians((60 + rotation) * offsety);
                double z = Math.toRadians((30 + rotation) * offsetz);

                Vector vector = new Vector(offsetx * Math.PI, offsety * Math.PI, offsetz * Math.PI);
                ParticleDisplay.rotateAround(vector, x, y, z);

                for (ParticleDisplay display : displays) {
                    display.rotate(x, y, z);
                    display.getLocation().add(vector);
                }
                runnable.run();
                for (ParticleDisplay display : displays) display.getLocation().subtract(vector);
            }
        };
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
     * @see #rotateAround(Plugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @see #moveRotatingAround(Plugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @since 1.0.0
     */
    public static BukkitTask guard(Plugin plugin, long update, double rate, double offsetx, double offsety, double offsetz,
                                   Runnable runnable, ParticleDisplay... displays) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, guard(rate, offsetx, offsety, offsetz, runnable, displays), 0, update);
    }

    /**
     * Spawn a sphere.
     * <a href="https://www.spigotmc.org/threads/146">Spigot Thread Tutorial</a>
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

                    display.particleDirection(directionX, directionY, directionZ);
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
                    Vector endVect = start.clone().subtract(display.getLocation()).toVector().multiply(random(minRandomDistance, maxRandomDistance));
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
     * @see #circle(double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void ring(double rate, double radius, double tubeRadius, ParticleDisplay display) {
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
     * @param amount    the amount of spikes to spawn.
     * @param rate      rate of spike line points.
     * @param start     start location of spikes.
     * @param originEnd end location of spikes.
     * @return the runnable. It will return false when the amount of spikes has been reached.
     * @since 1.0.0
     */
    public static BooleanSupplier spread(int amount, int rate, Location start, Location originEnd,
                                         double offsetx, double offsety, double offsetz, ParticleDisplay display) {
        return new BooleanSupplier() {
            int count = amount;
            boolean done = false;

            @Override
            public boolean getAsBoolean() {
                if (done) return false;

                int frame = rate;

                while (frame-- != 0) {
                    double x = random(-offsetx, offsetx);
                    double y = random(-offsety, offsety);
                    double z = random(-offsetz, offsetz);

                    Location end = originEnd.clone().add(x, y, z);
                    line(start, end, 0.1, display);
                }

                if (count-- <= 0) {
                    done = true;
                    return false;
                }
                return true;
            }
        };
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
    public static BukkitTask spread(Plugin plugin, int amount, int rate, Location start, Location originEnd,
                                    double offsetx, double offsety, double offsetz, ParticleDisplay display) {
        BooleanSupplier spread = spread(amount, rate, start, originEnd, offsetx, offsety, offsetz, display);
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (!spread.getAsBoolean()) {
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
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
     * @param orbits the orbits of the atom.
     * @param radius the radius of the atom orbits.
     * @param rate   the rate of orbit points.
     * @see #atom(int, double, double, ParticleDisplay, ParticleDisplay)
     * @since 1.0.0
     */
    public static Runnable atomic(int orbits, double radius, double rate, ParticleDisplay orbit) {
        return new Runnable() {
            final double rateDiv = Math.PI / rate;
            final double dist = Math.PI / orbits;
            double theta = 0;

            @Override
            public void run() {
                int orbital = orbits;
                theta += rateDiv;

                double x = radius * Math.cos(theta);
                double z = radius * Math.sin(theta);

                for (double angle = 0; orbital > 0; angle += dist) {
                    orbit.rotate(ParticleDisplay.Rotation.of(angle, ParticleDisplay.Axis.Z));
                    orbit.spawn(x, 0, z);
                    orbital--;
                }
            }
        };
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
    public static BukkitTask atomic(Plugin plugin, int orbits, double radius, double rate, ParticleDisplay orbit) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, atomic(orbits, radius, rate, orbit), 0, 1);
    }

    /**
     * Spawns animated helix shapes.
     *
     * @param strings      the amount of helix strings. The rotation angle will split equally for each.
     * @param radius       the radius of the helix.
     * @param rate         the rate of helix points.
     * @param extension    the helix circle extension.
     * @param length       the length of the helix.
     * @param speed        the amount of blocks the particles advances in one tick. Recommended is 0.5
     * @param rotationRate The amount particles rotate around the circular reference, should be set depending on the amount of strings.
     *                     5 is usually a good value.
     * @param fadeUp       helix radius will decrease to zero as it gets closer to the top.
     * @param fadeDown     helix radius will increase to the original radius as it gets closer to the center.
     * @return the animation runnable. It will return false when the animation is finished.
     * @see #dnaReplication(double, double, int, double, int, int, ParticleDisplay)
     * @since 3.0.0
     */
    public static BooleanSupplier helix(int strings, double radius, double rate, double extension,
                                        double length, double speed, double rotationRate,
                                        boolean fadeUp, boolean fadeDown,
                                        ParticleDisplay display) {
        return new BooleanSupplier() {
            // If we look at a helix string from above, we'll see a circle tunnel.
            // To make this tunnel we're going to generate circles while moving
            // upwards to get a curvy tunnel.
            // Since we're generating this string infinitely we don't need
            // to use radians or degrees.
            final double distanceBetweenEachCirclePoints = Particles.PII / strings;
            final double radiusDiv = radius / (length / rate);
            final double radiusDiv2 = fadeUp && fadeDown ? radiusDiv * 2 : radiusDiv;
            double dynamicRadius = fadeDown ? 0 : radius;
            boolean center = !fadeDown;
            final double calculatedRotRate = distanceBetweenEachCirclePoints / rotationRate;
            double rotation = 0;
            double currentDistance = 0;

            @Override
            public boolean getAsBoolean() {
                if (currentDistance >= length) return false;

                if (!center) {
                    dynamicRadius += radiusDiv2;
                    if (dynamicRadius >= radius) center = true;
                } else if (fadeUp) dynamicRadius -= radiusDiv2;

                // Now we're going to copy our points and rotate them.
                for (double i = 0; i < strings; i++) {
                    // 2D cirlce points.
                    double angle = i * distanceBetweenEachCirclePoints * extension + rotation;
                    double x = dynamicRadius * Math.cos(angle);
                    double z = dynamicRadius * Math.sin(angle);
                    display.spawn(x, 0, z);
                }

                currentDistance += speed;
                if (currentDistance < length) display.advanceInDirection(speed);
                else display.advanceInDirection(speed - (currentDistance - length));
                rotation += calculatedRotRate;

                return true;
            }
        };
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
     * @see #dnaReplication(Plugin, double, double, int, double, int, int, ParticleDisplay)
     * @since 3.0.0
     */
    public static BukkitTask helix(Plugin plugin, int strings, double radius, double rate,
                                   double extension, double height, double speed, double rotationRate,
                                   boolean fadeUp, boolean fadeDown, ParticleDisplay display) {
        BooleanSupplier helix = helix(strings, radius, rate, extension, height, speed, rotationRate, fadeUp, fadeDown, display);
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (!helix.getAsBoolean()) cancel();
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
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
     * @see #helix(int, double, double, double, double, double, double, boolean, boolean, ParticleDisplay)
     * @see #dnaReplication(Plugin, double, double, int, double, int, int, ParticleDisplay)
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
            Location nucleotide1 = display.getLocation().clone().add(x, y, z);
            display.spawn(x, y, z);
            Location nucleotide2 = display.getLocation().clone().subtract(x, -y, z);
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
     * @param radius           the radius of DNA helix circle.
     * @param rate             the rate of DNA points.
     * @param speed            the number of points to build in a single tick. Recommended is 5.
     * @param extension        the extension of the DNA helix sin/cos waves.
     * @param height           the height of the DNA strings.
     * @param hydrogenBondDist the distance between two DNA string helix points in a single string for each hydrogen bond to be formed.
     * @return the runnable handling the animation. It will return false when the animation is finished.
     * @see #dna(double, double, double, int, int, ParticleDisplay, ParticleDisplay)
     * @since 3.0.0
     */
    public static BooleanSupplier dnaReplication(double radius, double rate, int speed, double extension,
                                                 int height, int hydrogenBondDist, ParticleDisplay display) {
        // We'll use the common nucleotide colors.
        ParticleDisplay adenine = ParticleDisplay.colored(null, java.awt.Color.BLUE, 1); // Blue
        ParticleDisplay thymine = ParticleDisplay.colored(null, java.awt.Color.YELLOW, 1); // Yellow
        ParticleDisplay guanine = ParticleDisplay.colored(null, java.awt.Color.GREEN, 1); // Green
        ParticleDisplay cytosine = ParticleDisplay.colored(null, java.awt.Color.RED, 1); // Red

        return new BooleanSupplier() {
            double y = 0;
            int nucleotideDist = 0;
            boolean done = false;

            @Override
            public boolean getAsBoolean() {
                if (done) return false;

                int repeat = speed;
                while (repeat-- != 0) {
                    y += rate;
                    nucleotideDist++;

                    double x = radius * Math.cos(extension * y);
                    double z = radius * Math.sin(extension * y);
                    Location nucleotide1 = display.getLocation().clone().add(x, y, z);
                    // display.spawn(x, y, z);
                    circle(0.1, 10, display.cloneWithLocation(x, y, z));
                    Location nucleotide2 = display.getLocation().clone().subtract(x, -y, z);
                    circle(0.1, 10, display.cloneWithLocation(-x, y, -z));
                    // display.spawn(-x, y, -z);

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

                    if (y > height) {
                        done = true;
                        return false;
                    }
                }

                return true;
            }
        };
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
    public static BukkitTask dnaReplication(Plugin plugin, double radius, double rate, int speed, double extension,
                                            int height, int hydrogenBondDist, ParticleDisplay display) {
        BooleanSupplier dnaReplication = dnaReplication(radius, rate, speed, extension, height, hydrogenBondDist, display);
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (!dnaReplication.getAsBoolean()) {
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
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
     * @param cloud recommended particle is {@link XParticle#CLOUD} or {@link XParticle#LARGE_SMOKE} and the offset xyz should be higher than 2
     * @param rain  recommended particle is {@link XParticle#FALLING_WATER} or {@link XParticle#FALLING_LAVA} and the offset xyz should be the same as cloud.
     * @return the runnable handling the animation.
     * @since 1.0.0
     */
    public static Runnable cloud(ParticleDisplay cloud, ParticleDisplay rain) {
        return () -> {
            cloud.spawn();
            rain.spawn();
        };
    }

    /**
     * A simple method to spawn animated clouds effect.
     *
     * @param plugin the timer handler.
     * @param cloud  recommended particle is {@link XParticle#CLOUD} or {@link XParticle#LARGE_SMOKE} and the offset xyz should be higher than 2
     * @param rain   recommended particle is {@link XParticle#FALLING_WATER} or {@link XParticle#FALLING_LAVA} and the offset xyz should be the same as cloud.
     * @return the timer task handling the animation.
     * @since 1.0.0
     */
    public static BukkitTask cloud(Plugin plugin, ParticleDisplay cloud, ParticleDisplay rain) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, cloud(cloud, rain), 0, 1);
    }

    /**
     * Spawns a line from a location to another.
     * <a href="https://www.spigotmc.org/threads/176695/">Spigot Thread Tutorial</a>
     * This method is a modified version to get the best performance.
     *
     * @param start the starting point of the line.
     * @param end   the ending point of the line.
     * @param rate  the rate of points of the line.
     * @see #drawLine(Player, double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void line(Location start, Location end, double rate, ParticleDisplay display) {
        rate = Math.abs(rate);
        double x = end.getX() - start.getX();
        double y = end.getY() - start.getY();
        double z = end.getZ() - start.getZ();
        double length = Math.sqrt(NumberConversions.square(x) + NumberConversions.square(y) + NumberConversions.square(z));

        x /= length;
        y /= length;
        z /= length;

        ParticleDisplay clone = display.clone();
        clone.withLocation(start);
        for (double i = 0; i < length; i += rate) {
            // Since the rate can be any number it's possible to get a higher number than
            // the length in the last loop.
            // noinspection ConstantValue
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
        display.withLocation(start);
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
        display.withLocation(start);
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
        display.withLocation(start);
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
        display.withLocation(start);
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
     * <a href="https://en.wikipedia.org/wiki/Hypercube">Hypercube</a>
     * <p>
     * I'm still looking for a way to make this animated
     * but it's damn confusing: <a href="https://www.youtube.com/watch?v=iGO12Z5Lw8s">YouTube</a>
     *
     * @param startOrigin the starting point for the original cube.
     * @param endOrigin   the endnig point for the original cube.
     * @param rate        the rate of cube points.
     * @param sizeRate    the size
     * @param cubes       the dimension of the hypercube starting from 3D. E.g. {@code dimension 1 -> 4D tersseract}
     * @see #structuredCube(Location, Location, double, ParticleDisplay)
     * @see #tesseract(Plugin, double, double, double, long, ParticleDisplay)
     * @since 1.0.0
     */
    public static void hypercube(Location startOrigin, Location endOrigin, double rate, double sizeRate, int cubes, ParticleDisplay display) {
        List<Location> previousPoints = null;
        for (int i = 0; i < cubes + 1; i++) {
            List<Location> points = new ArrayList<>(8);
            Location start = startOrigin.clone().subtract(i * sizeRate, i * sizeRate, i * sizeRate);
            Location end = endOrigin.clone().add(i * sizeRate, i * sizeRate, i * sizeRate);

            display.withLocation(start);
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
     * Animated 4D tesseract using matrix motion.
     * Since this is a 4D shape the usage should be highly limited.
     * A failed prototype: https://imgur.com/eziNk7x
     * Final Version: https://imgur.com/Vb2HDQN
     * <p>
     * https://en.wikipedia.org/wiki/Tesseract
     * https://en.wikipedia.org/wiki/Rotation_matrix
     *
     * @param size  the size of the tesseract. Recommended is 4
     * @param rate  the rate of the tesseract points. Recommended is 0.3
     * @param speed the speed of the tesseract matrix motion. Recommended is 0.01
     * @param ticks the amount of ticks to keep the animation.
     * @return the animation runnable. It will return false when the animation is over.
     * @see #hypercube(Location, Location, double, double, int, ParticleDisplay)
     * @since 4.0.0
     */
    public static BooleanSupplier tesseract(double size, double rate, double speed, long ticks, ParticleDisplay display) {
        // We can multiply these later to change the size.
        // This array doesn't really need to be a constant as it's initialized once.
        double[][] positions = {
                {-1, -1, -1, 1}, {1, -1, -1, 1},
                {1, 1, -1, 1}, {-1, 1, -1, 1},
                {-1, -1, 1, 1}, {1, -1, 1, 1},
                {1, 1, 1, 1}, {-1, 1, 1, 1},

                {-1, -1, -1, -1}, {1, -1, -1, -1},
                {1, 1, -1, -1}, {-1, 1, -1, -1},
                {-1, -1, 1, -1}, {1, -1, 1, -1},
                {1, 1, 1, -1}, {-1, 1, 1, -1},
        };

//        BiFunction<Double, Double, Double> reverseMatrix = (a, b) -> {
//            if (a < 0) a -= b;
//            else a += b;
//            return -a;
//        };
//
//        List<double[]> original = new ArrayList<>(Arrays.asList(positions));
//        List<double[]> points = new ArrayList<>(original);
//        List<double[]> rev = new ArrayList<>(original);
        List<int[]> connections = new ArrayList<>();
//
//        double dist = 0;
//        Collections.reverse(rev);
//        List<double[]> reversed = new ArrayList<>();
//        for (int i = 0; i < 4; i += 2) {
//            reversed.add(rev.get(i + 1));
//            reversed.add(rev.get(i));
//        }
//        reversed.forEach(x -> points.add(new double[]{
//                reverseMatrix.apply(x[0], dist), reverseMatrix.apply(x[1], dist),
//                reverseMatrix.apply(x[2], dist), reverseMatrix.apply(x[3], dist)}));

        // Connect the generated 4D points together.
        // This can later be modified to support multi-dimension hypercubes.
        int level = 1;
        for (int h = 0; h <= level; h++) {
            int start = 8 * h;
            for (int i = start; i < start + 4; i++) {
                connections.add(new int[]{i, ((i + 1) % 4) + start});
                connections.add(new int[]{i + 4, (((i + 1) % 4) + 4) + start});
                connections.add(new int[]{i, i + 4});
            }
        }
        for (int i = 0; i < (level + 1) * 4; i++) connections.add(new int[]{i, i + 8});

        return new BooleanSupplier() {
            double angle = 0;
            long repeat = 0;
            boolean done = false;

            @Override
            public boolean getAsBoolean() {
                if (done) return false;

                double cos = Math.cos(angle);
                double sin = Math.sin(angle);

                // https://en.wikipedia.org/wiki/Rotation_matrix
                double[][] rotationXY = {
                        {cos, -sin, 0, 0},
                        {sin, cos, 0, 0},
                        {0, 0, 1, 0},
                        {0, 0, 0, 1}
                };

                // What does it mean to rotate a shape in the w (4th) axis?
                double[][] rotationZW = {
                        {1, 0, 0, 0},
                        {0, 1, 0, 0},
                        {0, 0, cos, -sin},
                        {0, 0, sin, cos}
                };

                double[][] projected3D = new double[positions.length][4];
                for (int i = 0; i < positions.length; i++) {
                    // To get the prototype version simply rotate the
                    // cube by using the display.rotate method in one of the axis.
                    double[] point = positions[i];
                    double[] rotated = matrix(rotationXY, point);
                    rotated = matrix(rotationZW, rotated);

                    int distance = 2;
                    double w = 1 / (distance - rotated[3]);
                    double[][] projection = {
                            {w, 0, 0, 0},
                            {0, w, 0, 0},
                            {0, 0, w, 0}
                    };

                    double[] projected = matrix(projection, rotated);
                    for (int proj = 0; proj < projected.length; proj++) projected[proj] *= size;
                    projected3D[i] = projected;

                    display.spawn(projected[0], projected[1], projected[2]);
                }

                for (int[] connection : connections) {
                    // Get the points of our tesseract and connect the two points using our line method.
                    double[] pointA = projected3D[connection[0]];
                    double[] pointB = projected3D[connection[1]];
                    Location start = display.cloneLocation(pointA[0], pointA[1], pointA[2]);
                    Location end = display.cloneLocation(pointB[0], pointB[1], pointB[2]);
                    line(start, end, rate, display);
                }

                if (++repeat > ticks) {
                    done = true;
                    return false;
                } else {
                    angle += speed;
                    return true;
                }
            }
        };
    }

    /**
     * Animated 4D tesseract using matrix motion.
     * Since this is a 4D shape the usage should be highly limited.
     * A failed prototype: https://imgur.com/eziNk7x
     * Final Version: https://imgur.com/Vb2HDQN
     * <p>
     * https://en.wikipedia.org/wiki/Tesseract
     * https://en.wikipedia.org/wiki/Rotation_matrix
     *
     * @param plugin the timer handler.
     * @param size   the size of the tesseract. Recommended is 4
     * @param rate   the rate of the tesseract points. Recommended is 0.3
     * @param speed  the speed of the tesseract matrix motion. Recommended is 0.01
     * @param ticks  the amount of ticks to keep the animation.
     * @see #hypercube(Location, Location, double, double, int, ParticleDisplay)
     * @since 4.0.0
     */
    public static BukkitTask tesseract(Plugin plugin, double size, double rate, double speed, long ticks, ParticleDisplay display) {
        BooleanSupplier tesseract = tesseract(size, rate, speed, ticks, display);
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (!tesseract.getAsBoolean()) cancel();
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
    }

    /**
     * A method to translate matrix motion for 4D.
     * https://en.wikipedia.org/wiki/Rotation_matrix
     *
     * @since 4.0.0
     */
    private static double[] matrix(double[][] a, double[] m) {
        double[][] b = new double[4][1];
        b[0][0] = m[0];
        b[1][0] = m[1];
        b[2][0] = m[2];
        b[3][0] = m[3];

        int colsA = a[0].length;
        int rowsA = a.length;
        int colsB = b[0].length;
        int rowsB = b.length;

        double[][] result = new double[rowsA][rowsB];
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                float sum = 0;
                for (int k = 0; k < colsA; k++) {
                    sum += a[i][k] * b[k][j];
                }
                result[i][j] = sum;
            }
        }

        double[] v = new double[4];
        v[0] = result[0][0];
        v[1] = result[1][0];
        v[2] = result[2][0];
        if (result.length > 3) v[3] = result[3][0];
        return v;
    }

    /**
     * Spawns a mandelbrot set.
     * https://en.wikipedia.org/wiki/Mandelbrot_set
     *
     * @param size  the size of the mandelbrot. Recommended is 5
     * @param zoom  the zooming length of the mandelbrot (Does not show the julia set.) Recommended is 1
     * @param rate  the rate of the shape points. Recommended is 0.1
     * @param x0    the amount of x to move the shape. Recommended is 3
     * @param y0    the amount of y to move the shape. Recommended is 0
     * @param color the color set of the mandelbrot. This can change the shape. Recommended is 1000
     * @since 4.0.0
     */
    public static void mandelbrot(double size, double zoom, double rate, double x0, double y0, int color, ParticleDisplay display) {
        for (double y = -size; y < size; y += rate) {
            for (double x = -size; x < size; x += rate) {
                double zy = 0;
                double zx = 0;
                double cX = (x - x0) / zoom;
                double cY = (y - y0) / zoom;

                int iteration = color; // Max iterations
                while (zx * zx + zy * zy <= 4 && iteration > 0) {
                    double xtemp = zx * zx - zy * zy + cX;
                    zy = 2 * zx * zy + cY; // Changing 2 to 1 or -1 can give interesting results.
                    zx = xtemp;
                    iteration--;
                }

                if (iteration != 0) continue;
                // Color color = new Color(iteration | (iteration << 8));
                display.spawn(x, y, 0);
            }
        }
    }

    /**
     * Spawns a julia set.
     * https://en.wikipedia.org/wiki/Julia_set
     *
     * @param size        the size of the image.
     * @param zoom        the zoom ratio to the set.
     * @param colorScheme the color scheme for the julia set.
     * @param moveX       the amount to move in the x axis.
     * @param moveY       the amount to move in the y axis.
     * @param display     The particle should be {@link XParticle#DUST}
     * @see #mandelbrot(double, double, double, double, double, int, ParticleDisplay)
     * @since 4.0.0
     */
    public static void julia(double size, double zoom, int colorScheme, double moveX, double moveY, ParticleDisplay display) {
        double cx = -0.7;
        double cy = 0.27015;

        for (double x = -size; x < size; x += 0.1) {
            for (double y = -size; y < size; y += 0.1) {
                double zx = 1.5 * (size - size / 2) / (0.5 * zoom * size) + moveX;
                double zy = (y - size / 2) / (0.5 * zoom * size) + moveY;

                int i = colorScheme;
                while (zx * zx + zy * zy < 4 && i > 0) {
                    double xtemp = zx * zx - zy * zy + cx;// Math.pow((zx * zx + zy * zy), (n / 2)) * (Math.cos(n * Math.atan2(zy, zx))) + cx;
                    zy = 2 * zx * zy + cy; // Math.pow((zx * zx + zy * zy), (n / 2)) * Math.sin(n * Math.atan2(zy, zx)) + cy;
                    zx = xtemp;
                    i--;
                }
                java.awt.Color color = new java.awt.Color((i << 21) + (i << 10) + i * 8);

                display.withColor(color, 0.8f)
                        .spawn(x, y, 0);
            }
        }
    }

    /**
     * Spawn 3D spiked circles.
     * Note that the animation is intended to be used with prototype mode enabled.
     * Animations without prototype doesn't really look good. You might want to increase the speed.
     *
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
     * @return a list of runnables. They will return false when the animation is done.
     * @see #spikeSphere(double, double, int, double, double, ParticleDisplay)
     * @since 3.0.0
     */
    public static List<BooleanSupplier> star(int points, int spikes, double rate, double spikeLength, double coreRadius,
                                             double neuron, boolean prototype, int speed, ParticleDisplay display) {
        double pointsRate = PII / points;
        double rateDiv = Math.PI / rate;
        ThreadLocalRandom random = prototype ? null : ThreadLocalRandom.current();
        List<BooleanSupplier> tasks = new ArrayList<>();

        for (int i = 0; i < spikes * 2; i++) {
            double spikeAngle = i * Math.PI / spikes;

            tasks.add(new BooleanSupplier() {
                double vein = 0;
                double theta = 0;
                boolean done = false;

                @Override
                public boolean getAsBoolean() {
                    if (done) return false;

                    int repeat = speed;
                    while (repeat-- != 0) {
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
                        ParticleDisplay.rotateAround(vector, ParticleDisplay.Axis.X, spikeAngle);
                        for (int j = 0; j < points; j++) {
                            // Rotate the spikes to copy them with equal angles.
                            ParticleDisplay.rotateAround(vector, ParticleDisplay.Axis.Y, pointsRate);
                            display.spawn(vector);
                        }
                    }

                    if (theta >= PII) {
                        done = true;
                        return false;
                    }
                    return true;
                }
            });
        }
        return tasks;
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
    public static List<BukkitTask> star(Plugin plugin, int points, int spikes, double rate, double spikeLength, double coreRadius,
                                        double neuron, boolean prototype, int speed, ParticleDisplay display) {
        List<BukkitTask> tasks = new ArrayList<>();
        for (BooleanSupplier task : star(points, spikes, rate, spikeLength, coreRadius, neuron, prototype, speed, display)) {
            tasks.add(new BukkitRunnable() {
                @Override
                public void run() {
                    if (!task.getAsBoolean()) cancel();
                }
            }.runTaskTimerAsynchronously(plugin, 0, 1));
        }
        return tasks;
    }

    /**
     * Spawns an eye-shaped circle.
     *
     * @param radius    the radius of the eye.
     * @param radius2   the other radius of the eye. Usually the same as the first radius.
     * @param rate      the rate of the eye points.
     * @param extension the extension of the eye. Recommended is 0.2
     * @since 4.0.0
     */
    public static void eye(double radius, double radius2, double rate, double extension, ParticleDisplay display) {
        double rateDiv = Math.PI / rate;
        double limit = Math.PI / extension;
        double x = 0;

        for (double i = 0; i < limit; i += rateDiv) {
            double y = radius * Math.sin(extension * i);
            double y2 = radius2 * Math.sin(extension * -i);
            display.spawn(x, y, 0);
            display.spawn(x, y2, 0);
            x += 0.1;
        }
    }

    /**
     * Spawns an illuminati shape.
     *
     * @param size      the size of the illuminati shape.
     * @param extension the extension of the illuminati eye.
     * @since 4.0.0
     */
    public static void illuminati(double size, double extension, ParticleDisplay display) {
        polygon(3, 1, size, 1 / (size * 30), 0, display);
        // It'd be really hard to automatically adjust the extension based on the size.
        eye(size / 4, size / 4, 30, extension, display.cloneWithLocation(0.3, 0, size / 1.8).rotate(Math.PI / 2, Math.PI / 2, 0));
        circle(size / 5, size * 5, display.cloneWithLocation(0.3, 0, 0));
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
                double x1 = x + (deltaX * pos);
                double z1 = z + (deltaZ * pos);
                display.spawn(x1, 0, z1);
            }
        }
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
     * @see #atomic(Plugin, int, double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void atom(int orbits, double radius, double rate, ParticleDisplay orbit, ParticleDisplay nucleus) {
        double dist = Math.PI / orbits;
        for (double angle = 0; orbits > 0; angle += dist) {
            orbit.rotate(ParticleDisplay.Rotation.of(angle, ParticleDisplay.Axis.Z));
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
    public static BooleanSupplier meguminExplosion(double size, ParticleDisplay display) {
        BooleanSupplier spread = spread(30, 2, display.getLocation(), display.getLocation().clone().add(0, 10, 0), 5, 5, 5, display);
        return new BooleanSupplier() {
            boolean first = true;

            @Override
            public boolean getAsBoolean() {
                if (first) {
                    first = false;
                    polygon(10, 4, size, 0.02, 0.3, display);
                    polygon(10, 3, size / (size - 1), 0.5, 0, display);
                    circle(size, 40, display);
                }
                return spread.getAsBoolean();
            }
        };
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
    public static BukkitTask meguminExplosion(Plugin plugin, double size, ParticleDisplay display) {
        BooleanSupplier explosion = meguminExplosion(size, display);
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (!explosion.getAsBoolean()) {
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
    }

    /**
     * A sin/cos based smoothly animated explosion wave.
     * Source: https://www.youtube.com/watch?v=n8W7RxW5KB4
     *
     * @param rate the distance between each cos/sin lines.
     * @return the animation runnable. It will return false when it's done.
     * @since 1.0.0
     */
    public static BooleanSupplier explosionWave(double rate, ParticleDisplay display, ParticleDisplay secDisplay) {
        return new BooleanSupplier() {
            final double addition = Math.PI * 0.1;
            final double rateDiv = Math.PI / rate;
            double times = Math.PI / 4;
            boolean done = false;

            @Override
            public boolean getAsBoolean() {
                if (done) return false;

                times += addition;
                for (double theta = 0; theta <= PII; theta += rateDiv) {
                    double x = times * Math.cos(theta);
                    double y = 2 * Math.exp(-0.1 * times) * Math.sin(times) + 1.5;
                    double z = times * Math.sin(theta);
                    display.spawn(x, y, z);

                    theta = theta + Math.PI / 64;
                    x = times * Math.cos(theta);
                    // y = 2 * Math.exp(-0.1 * times) * Math.sin(times) + 1.5;
                    z = times * Math.sin(theta);
                    secDisplay.spawn(x, y, z);
                }
                if (times > 20) {
                    done = true;
                    return false;
                }
                return true;
            }
        };
    }

    /**
     * A sin/cos based smoothly animated explosion wave.
     * Source: https://www.youtube.com/watch?v=n8W7RxW5KB4
     *
     * @param rate the distance between each cos/sin lines.
     * @since 1.0.0
     */
    public static BukkitTask explosionWave(Plugin plugin, double rate, ParticleDisplay display, ParticleDisplay secDisplay) {
        BooleanSupplier explosionWave = explosionWave(rate, display, secDisplay);
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (!explosionWave.getAsBoolean()) cancel();
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
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
    public static CompletableFuture<Map<double[], Color>> renderImage(Path path, int resizedWidth, int resizedHeight, double compact) {
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
    @SuppressWarnings("unused")
    public static CompletableFuture<Map<double[], Color>> renderImage(BufferedImage image, int resizedWidth, int resizedHeight, double compact) {
        return CompletableFuture.supplyAsync(() -> {
            if (image == null) return null;

            int width = image.getWidth();
            int height = image.getHeight();
            double centerX = width / 2D;
            double centerY = height / 2D;

            Map<double[], Color> rendered = new HashMap<>();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getRGB(x, y);

                    // Transparency
                    if ((pixel >> 24) == 0x0) continue;
                    // 0 - 255
                    // if ((pixel & 0xff000000) >>> 24 == 0) continue;
                    // 0.0 - 1.0
                    // if (pixel == java.awt.Color.TRANSLUCENT) continue;

                    java.awt.Color color = new java.awt.Color(pixel);
                    int r = color.getRed();
                    int g = color.getGreen();
                    int b = color.getBlue();

                    double[] coords = {(x - centerX) * compact, (y - centerY) * compact};
                    Color bukkitColor = Color.fromRGB(r, g, b);
                    rendered.put(coords, bukkitColor);
                }
            }
            return rendered;
        });
    }

    /**
     * Display a rendered image repeatedly.
     *
     * @param render   the rendered image map.
     * @param location the dynamic location to display the image at.
     * @param repeat   amount of times to repeat displaying the image.
     * @param quality  the quality of the image is exactly the number of particles display for each pixel. Recommended value is 1
     * @param speed    the speed is exactly the same value as the speed of particles. Recommended amount is 0
     * @param size     the size of the particle. Recommended amount is 0.8
     * @return the async bukkit task displaying the image.
     * @since 1.0.0
     */
    public static BooleanSupplier displayRenderedImage(Map<double[], Color> render, Callable<Location> location,
                                                       int repeat, int quality, int speed, float size) {
        return new BooleanSupplier() {
            int times = repeat;
            boolean done = false;

            @Override
            public boolean getAsBoolean() {
                if (done) return false;

                try {
                    displayRenderedImage(render, location.call(), quality, speed, size);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (times-- <= 0) {
                    done = true;
                    return false;
                }
                return true;
            }
        };
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
    public static BukkitTask displayRenderedImage(Plugin plugin, Map<double[], Color> render, Callable<Location> location,
                                                  int repeat, long period, int quality, int speed, float size) {
        BooleanSupplier displayRenderedImage = displayRenderedImage(render, location, repeat, quality, speed, size);
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (!displayRenderedImage.getAsBoolean()) cancel();
            }
        }.runTaskTimerAsynchronously(plugin, 0, period);
    }

    /**
     * Display a rendered image repeatedly.
     *
     * @param render   the rendered image map.
     * @param location the dynamic location to display the image at. The {@link Location#getYaw()} determines the image's rotation.
     * @param quality  the quality of the image is exactly the number of particles display for each pixel. Recommended value is 1
     * @param speed    the speed is exactly the same value as the speed of particles. Recommended amount is 0
     * @param size     the size of the particle. Recommended amount is 0.8
     * @since 1.0.0
     */
    @SuppressWarnings("ConstantConditions")
    public static void displayRenderedImage(Map<double[], Color> render, Location location, int quality, int speed, float size) {
        World world = location.getWorld();

        BlockFace facing;
        double rotation = location.getYaw(); // The rotation axis.
        if (rotation >= 135 || rotation < -135) facing = BlockFace.NORTH;
        else if (rotation >= -135 && rotation < -45) facing = BlockFace.EAST;
        else if (rotation >= -45 && rotation < 45) facing = BlockFace.SOUTH;
        else if (rotation >= 45 && rotation < 135) facing = BlockFace.WEST;
        else throw new IllegalArgumentException("Unknown rotation yaw: " + rotation);

        for (Map.Entry<double[], Color> pixel : render.entrySet()) {
            Particle.DustOptions data = new Particle.DustOptions(pixel.getValue(), size);
            double[] pixelLoc = pixel.getKey();
            double x, y, z;

            switch (facing) {
                case NORTH:
                    x = location.getX() - pixelLoc[0];
                    y = location.getY() - pixelLoc[1];
                    z = location.getZ();
                    break;
                case EAST:
                    // East
                    x = location.getX();
                    y = location.getY() - pixelLoc[0];
                    z = location.getZ() - pixelLoc[1];
                    break;
                case SOUTH:
                    x = location.getX() - pixelLoc[1];
                    y = location.getY() - pixelLoc[0];
                    z = location.getZ();
                    break;
                case WEST:
                    x = location.getX();
                    y = location.getY() - pixelLoc[1];
                    z = location.getZ() - pixelLoc[0];
                    break;
                default:
                    throw new AssertionError("Invalid facing: " + facing);
            }

            Location loc = new Location(world, x, y, z);
            world.spawnParticle(XParticle.DUST.get(), loc, quality, 0, 0, 0, speed, data);
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
