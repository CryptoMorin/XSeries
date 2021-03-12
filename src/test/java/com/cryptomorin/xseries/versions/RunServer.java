package com.cryptomorin.xseries.versions;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.bukkit.Bukkit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;

public abstract class RunServer {

    private static final OptionParser parser = new OptionParser() {
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

    protected void runServer(final Runnable tests) {
        try {
            final File here = new File(System.getProperty("user.dir"));
            final Path path = here.toPath();
            final File before = here.getParentFile();
            final Path pathBefore = before.toPath();
            final Path testClassesPath = pathBefore.resolve("test-classes");
            final Path serverProperties = testClassesPath.resolve("server.properties");
            final Path bukkitYml = testClassesPath.resolve("bukkit.yml");
            final Path spigotYml = testClassesPath.resolve("spigot.yml");
            Files.deleteIfExists(path.resolve("world"));
            Files.deleteIfExists(path.resolve("world_nether"));
            Files.deleteIfExists(path.resolve("world_the_end"));
            final Thread thread = new Thread(() -> {
                System.setProperty("com.mojang.eula.agree", "true");
                main(new String[]{
                        "nogui",
                        "noconsole",
                        "--config=" + serverProperties.toString(),
                        "--bukkit-settings=" + bukkitYml.toString(),
                        "--spigot-settings=" + spigotYml.toString()
                });
            });
            thread.start();
            while (!checkTpsFilled()) {
                Thread.sleep(5L);
            }
            Thread.sleep(1000L);
            tests.run();
            Thread.sleep(1000L);
            Bukkit.shutdown();
            thread.interrupt();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected OptionSet parseOptions(final String[] args) {
        try {
            return parser.parse(args);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract boolean checkTpsFilled() throws Exception;

    protected abstract void main(final String[] args);
}
