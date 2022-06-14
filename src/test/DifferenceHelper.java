import com.cryptomorin.xseries.XBiome;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class DifferenceHelper {
    /**
     * Writes the material and sound differences to files in the server's root folder for updating purposes.
     */
    public static void versionDifference() {
        Path serverFolder = Bukkit.getWorldContainer().toPath();
        Path materials = serverFolder.resolve("XMaterial.txt"),
                sounds = serverFolder.resolve("XSound.txt"),
                biomes = serverFolder.resolve("XBiome.txt");

        writeDifference(materials, org.bukkit.Material.class, XMaterial.class, mat -> mat.startsWith("LEGACY_"));
        writeDifference(sounds, org.bukkit.Sound.class, XSound.class, null);
        writeDifference(biomes, org.bukkit.block.Biome.class, XBiome.class, null);
    }

    /**
     * Writes the difference between two enums.
     * For other differences check:
     * <pre>
     *     https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/Material.java
     *     https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/Sound.java
     *     https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/potion/PotionEffectType.java
     *     https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/enchantments/Enchantment.java
     *     https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/Particle.java
     * </pre>
     *
     * @param path   the file path to write the difference to.
     * @param system the original enum.
     * @param custom the custom enum that is most likely a version behind the original enum.
     * @param ignore Used soley for legacy materials.
     */
    public static <S extends Enum<S>, E extends Enum<E>>
    void writeDifference(Path path, Class<S> system, Class<E> custom, java.util.function.Predicate<String> ignore) {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            writer.write("------------------- Added -------------------");
            writer.newLine();

            for (Enum<S> systemConst : system.getEnumConstants()) {
                if (ignore != null && ignore.test(systemConst.name())) continue;

                boolean exists = true;
                for (Enum<E> customConst : custom.getEnumConstants()) {
                    if (systemConst.name().equals(customConst.name())) {
                        exists = false;
                        break;
                    }
                }
                if (exists) {
                    writer.write(systemConst.name() + ',');
                    writer.newLine();
                }
            }

            writer.newLine();
            writer.write("------------------ Removed ------------------");
            writer.newLine();

            for (Enum<E> customConst : custom.getEnumConstants()) {
                boolean exists = true;
                for (Enum<S> systemConst : system.getEnumConstants()) {
                    if (systemConst.name().equals(customConst.name())) {
                        exists = false;
                        break;
                    }
                }
                if (exists) {
                    writer.write(customConst.name());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
