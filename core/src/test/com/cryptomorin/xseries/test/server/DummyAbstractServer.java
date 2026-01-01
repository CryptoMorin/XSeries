/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2026 Crypto Morin
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

package com.cryptomorin.xseries.test.server;

import com.cryptomorin.xseries.base.XRegistry;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import com.cryptomorin.xseries.test.Constants;
import com.cryptomorin.xseries.test.XSeriesTests;
import com.cryptomorin.xseries.test.benchmark.BenchmarkMain;
import joptsimple.OptionParser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static com.cryptomorin.xseries.test.util.XLogger.log;

public abstract class DummyAbstractServer {
    private static final OptionParser OPTION_PARSER = new OptionParser() {
        {
            acceptsAll(Arrays.asList("?", "help"), "Show the help");

            acceptsAll(Arrays.asList("c", "config"), "Properties file to use")
                    .withRequiredArg()
                    .ofType(File.class)
                    .defaultsTo(new File("server.properties"))
                    .describedAs("Properties file");

            acceptsAll(Arrays.asList("P", "plugins"), "Plugin directory to use")
                    .withRequiredArg()
                    .ofType(File.class)
                    .defaultsTo(new File("plugins"))
                    .describedAs("Plugin directory");

            acceptsAll(Arrays.asList("h", "host", "server-ip"), "Host to listen on")
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("Hostname or IP");

            acceptsAll(Arrays.asList("W", "world-dir", "universe", "world-container"), "World container")
                    .withRequiredArg()
                    .ofType(File.class)
                    .defaultsTo(new File("."))
                    .describedAs("Directory containing worlds");

            acceptsAll(Arrays.asList("w", "world", "level-name"), "World name")
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("World name");

            acceptsAll(Arrays.asList("p", "port", "server-port"), "Port to listen on")
                    .withRequiredArg()
                    .ofType(Integer.class)
                    .describedAs("Port");

            acceptsAll(Arrays.asList("o", "online-mode"), "Whether to use online authentication")
                    .withRequiredArg()
                    .ofType(Boolean.class)
                    .describedAs("Authentication");

            acceptsAll(Arrays.asList("s", "size", "max-players"), "Maximum amount of players")
                    .withRequiredArg()
                    .ofType(Integer.class)
                    .describedAs("Server size");

            acceptsAll(Arrays.asList("d", "date-format"), "Format of the date to display in the console (for log entries)")
                    .withRequiredArg()
                    .ofType(SimpleDateFormat.class)
                    .describedAs("Log date format");

            acceptsAll(Collections.singletonList("log-pattern"), "Specfies the log filename pattern")
                    .withRequiredArg()
                    .ofType(String.class)
                    .defaultsTo("server.log")
                    .describedAs("Log filename");

            acceptsAll(Collections.singletonList("log-limit"), "Limits the maximum size of the log file (0 = unlimited)")
                    .withRequiredArg()
                    .ofType(Integer.class)
                    .defaultsTo(0)
                    .describedAs("Max log size");

            acceptsAll(Collections.singletonList("log-count"), "Specified how many log files to cycle through")
                    .withRequiredArg()
                    .ofType(Integer.class)
                    .defaultsTo(1)
                    .describedAs("Log count");

            acceptsAll(Collections.singletonList("log-append"), "Whether to append to the log file")
                    .withRequiredArg()
                    .ofType(Boolean.class)
                    .defaultsTo(true)
                    .describedAs("Log append");

            acceptsAll(Collections.singletonList("log-strip-color"), "Strips color codes from log file");

            acceptsAll(Arrays.asList("b", "bukkit-settings"), "File for bukkit settings")
                    .withRequiredArg()
                    .ofType(File.class)
                    .defaultsTo(new File("bukkit.yml"))
                    .describedAs("Yml file");

            acceptsAll(Arrays.asList("C", "commands-settings"), "File for command settings")
                    .withRequiredArg()
                    .ofType(File.class)
                    .defaultsTo(new File("commands.yml"))
                    .describedAs("Yml file");

            acceptsAll(Collections.singletonList("forceUpgrade"), "Whether to force a world upgrade");
            acceptsAll(Collections.singletonList("eraseCache"), "Whether to force cache erase during world upgrade");
            acceptsAll(Collections.singletonList("nogui"), "Disables the graphical console");
            acceptsAll(Collections.singletonList("nojline"), "Disables jline and emulates the vanilla console");
            acceptsAll(Collections.singletonList("noconsole"), "Disables the console");
            acceptsAll(Arrays.asList("v", "version"), "Show the CraftBukkit Version");
            acceptsAll(Collections.singletonList("demo"), "Demo mode");
            acceptsAll(Arrays.asList("S", "spigot-settings"), "File for spigot settings")
                    .withRequiredArg()
                    .ofType(File.class)
                    .defaultsTo(new File("spigot.yml"))
                    .describedAs("Yml file");
        }
    };

    public static final File HERE = new File(System.getProperty("user.dir"));

    protected void runServer() {
        try {
            File before = HERE.getParentFile();
            Path pathBefore = before.toPath();
            Path testClassesPath = pathBefore.resolve("test-classes");
            Path serverProperties = testClassesPath.resolve("server.properties");
            Path bukkitYml = testClassesPath.resolve("bukkit.yml");
            Path spigotYml = testClassesPath.resolve("spigot.yml");

            // Don't delete worlds, it takes ages to regenerate.
            // TODO delete only if versions downgrade. Generate some kind of file?
            // Files.deleteIfExists(path.resolve("world"));
            // Files.deleteIfExists(path.resolve("world_nether"));
            // Files.deleteIfExists(path.resolve("world_the_end"));

            AtomicReference<Throwable> startException = new AtomicReference<>();
            Thread thread = new Thread(() -> {
                System.setProperty("com.mojang.eula.agree", "true");
                System.setProperty("IReallyKnowWhatIAmDoingISwear", "true");
                InvocationHandler implementer = main();

                log("Implementing dummy server...");
                // A proxy because we don't want to pollute this class with a bunch of methods we can't implement.
//                Server instance = (Server) Proxy.newProxyInstance(Server.class.getClassLoader(), new Class[]{Server.class}, implementer);

                log("Starting org.bukkit.craftbukkit.Main...");
                String[] startupArgs = { // https://www.spigotmc.org/wiki/start-up-parameters/
                        "nogui",
                        "noconsole",
                        "--config=" + serverProperties,
                        "--bukkit-settings=" + bukkitYml,
                        "--spigot-settings=" + spigotYml,
                };
                try {
                    // org.bukkit.craftbukkit.Main
                    // Main.main(startupArgs);

                    // This doesn't work, because XReflection depends on Bukkit.getBukkitVersion()
                    // but the server isn't started at this point.
                    // XReflection.ofMinecraft().inPackage(MinecraftPackage.CB).named("Main")
                    //         .method("public static void main(String[] args);")
                    //         .unreflect().invoke(null, (Object) startupArgs);

                    Class.forName("org.bukkit.craftbukkit.Main").getMethod("main", String[].class).invoke(null, (Object) startupArgs);
                    // TinyReflection.CraftBukkit$Main.getMethod("main", String[].class).invoke(null, (Object) startupArgs);
                } catch (Throwable e) {
                    startException.set(e);
                    return;
                }
                // Main.main(startupArgs);

//                log("Initializing server...");
                // Bukkit.setServer(instance);

                log("Done!");
            }, "XSeries Server Starter Thread");

            // Will be useless since CraftBukkit's main thread is not a daemon.
            thread.setDaemon(true);

            // I'm still not sure how to make this part more reliable.
            log("Starting thread " + thread + "...");
            thread.start();
            log("Joining thread " + thread + "...");
            thread.join();
            log("Joined thread " + thread);

            // net.minecraft.server.Main.main() -> MinecraftServer.a() ---[New Thread]--> MinecraftServer.y() ->
            // DedicatedPlayerList.e() -> new DedicatedPlayerList() ->
            // new PlayerList() -> new CraftServer() -> Bukkit.setServer(this)
            // -----------------------------------------------------------------
            // This is needed because setServer() is set inside a [New Thread]
            // We can't simply wait until the server is just fully loaded with the scheduler
            // either, because that also requires the Bukkit object.

            if (startException.get() != null) {
                log("Server startup failed. Not continuing.");
                throw new IllegalStateException("Server startup failed", startException.get());
            }

            waitForServer();

            // We can't use reflection to load the plugin manually because JavaPlugin's constructor
            // will complain when it's not loaded by a PluginClassLoader which is private.
            // Also, we can't package the test classes as a plugin JAR for the server to load either,
            // that's because IDEs in general (both IntelliJ and Eclipse) will mess with the class loaders,
            // causing the same error to occur.
            Plugin plugin = EmbeddedPlugin.createInstance();
            AtomicReference<Throwable> error = new AtomicReference<>();
            Bukkit.getScheduler().runTask(plugin, () -> {
                log("Server fully loaded. Running Tests...");
                synchronized (Constants.LOCK) {
                    log("Synchronize Access Granted");
                    try {
                        try {
                            XReflection.of(XRegistry.class)
                                    .field("private static boolean PERFORM_AUTO_ADD;")
                                    .setter().makeAccessible()
                                    .reflect().invoke(false);
                        } catch (Throwable e) {
                            throw new IllegalStateException("Failed to disable XRegistry's auto-add system", e);
                        }

                        if (Constants.TEST) XSeriesTests.test();
                    } catch (Throwable ex) {
                        error.set(ex);
                    }
                    Constants.LOCK.notifyAll();
                    log("Synchronize Done");
                }
            });
            log("Locking the test thread...");
            synchronized (Constants.LOCK) {
                Constants.LOCK.wait();
            }

            // Note: If this is not properly reached, it'll cause the surefire process to hang in the background
            // in that case, the process will need to be forcefully terminated manually (e.g. by using Task Manager)
            log("Lock has been released.");

            // Executors.newSingleThreadScheduledExecutor(Executors.defaultThreadFactory())
            //         .schedule(() -> {
            //             // It wants to load the world and all that bullshit. Just stop it.
            //             log("Interrupting thread: " + thread + "...");
            //             thread.interrupt();
            //             log("Interrupted thread " + thread);
            //         }, 5, TimeUnit.SECONDS);
            if (error.get() != null) throw XReflection.throwCheckedException(error.get());
        } catch (InterruptedException e) {
            throw new IllegalStateException("Server startup process has been interrupted", e);
        }

        // JMH Tests
        if (Constants.BENCHMARK) benchmark();

        log("All operations are done. " + (Constants.SAFE_SHUTDOWN ?
                "Shutting down the server..." :
                "Forcefully shutting down the server..."));
        if (Constants.SAFE_SHUTDOWN) {
            Bukkit.shutdown();
            log("Server shutdown signal sent.");
        }
    }

    @SuppressWarnings("unused")
    private static void forceShutdown() throws Throwable {
        // The only way to forcefully shut it down would be starting another process...
        // which wouldn't work, or at least we need extra setup to get it working.
        Object dedicatedServer = XReflection.ofMinecraft().inPackage(MinecraftPackage.CB)
                .named("CraftServer")
                .field("protected final DedicatedServer console")
                .returns(XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "server.dedicated").named("DedicatedServer"))
                .getter()
                .unreflect()
                .invoke(Bukkit.getServer());

        Thread serverThread = (Thread) XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "server")
                .named("MinecraftServer")
                //.method("public Thread ay()")
                .field("public final Thread aj")
                .getter()
                .unreflect()
                .invoke(dedicatedServer);

        serverThread.interrupt(); // Won't do anything...
        serverThread.join(); // Same as Bukkit.shutdown()
    }

    private static void benchmark() {
        try {
            log("[JMH] Running benchmarks...");
            // Don't use Bukkit's scheduler... We already have enough external factors affecting our benchmarks...
            BenchmarkMain benchmarkThread = new BenchmarkMain();
            benchmarkThread.start();
            synchronized (Constants.LOCK) {
                log("[JMH] Benchmarks are complete");
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            log("[JMH] JMH Benchmarking are done!");
            synchronized (Constants.LOCK) {
                log("[JMH] Synchronize Access Granted");
                Constants.LOCK.notify();
                log("[JMH] Synchronize Done");
            }
        }


        log("[JMH] Preparing lock for benchmarking...");
        synchronized (Constants.LOCK) {
            try {
                Constants.LOCK.wait();
            } catch (InterruptedException e) {
                throw new IllegalArgumentException("Failed to acquire lock for JMH benchmarks", e);
            }
        }
    }

    @SuppressWarnings({"ConstantValue", "BusyWait"})
    private static void waitForServer() throws InterruptedException {
        long start = System.currentTimeMillis();
        while (Bukkit.getServer() == null) {
            // Since we don't have access to Minecraft's code,
            // our best solution is just polling...
            Thread.sleep(50L);
        }
        long end = System.currentTimeMillis();
        long diff = end - start;

        // Usually takes ~200ms
        log("Took ~" + diff + "ms for Bukkit server instance to load");
    }

    private static void loadembeddedPlugin() throws Exception {
        Class<?> XSeriesPlugin = Class.forName("com.cryptomorin.xseries.test.XSeriesPlugin", true, Bukkit.getServer().getPluginManager().getClass().getClassLoader());
        // Bukkit.getServer().getPluginManager().enablePlugin(new XSeriesPlugin());
    }

// protected OptionSet parseOptions(String[] args) {
//     try {
//         return OPTION_PARSER.parse(args);
//     } catch (Exception e) {
//         throw new RuntimeException(e);
//     }
// }

    protected abstract InvocationHandler main();
}
