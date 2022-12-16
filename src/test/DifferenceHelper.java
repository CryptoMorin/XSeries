import com.cryptomorin.xseries.*;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DifferenceHelper {
    /**
     * Writes the material and sound differences to files in the server's root folder for updating purposes.
     */
    public static void versionDifference() {
        Path serverFolder = Bukkit.getWorldContainer().toPath();
        Path materials = serverFolder.resolve("XMaterial.txt"),
                sounds = serverFolder.resolve("XSound.txt"),
                xPotion = serverFolder.resolve("XPotion.txt"),
                xEnchant = serverFolder.resolve("XEnchantment.txt"),
                biomes = serverFolder.resolve("XBiome.txt");

        writeDifference(materials, org.bukkit.Material.class, XMaterial.class, mat -> mat.startsWith("LEGACY_"));
        writeDifference(sounds, org.bukkit.Sound.class, XSound.class, null);
        writeDifference(biomes, org.bukkit.block.Biome.class, XBiome.class, null);
        writeDifference(xPotion, getEnumLikeFields(PotionEffectType.class), XPotion.class, null);
        writeDifference(xEnchant, getEnumLikeFields(Enchantment.class), XEnchantment.class, null);
    }

    public static List<String> getEnumLikeFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(x -> x.getType() == clazz)
                .filter(x -> Modifier.isStatic(x.getModifiers()))
                .map(Field::getName)
                .collect(Collectors.toList());
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
        List<String> enumNames = Arrays.stream(system.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
        writeDifference(path, enumNames, custom, ignore);
    }

    public static <E extends Enum<E>>
    void writeDifference(Path path, List<String> system, Class<E> custom, java.util.function.Predicate<String> ignore) {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            writer.write("------------------- Added -------------------");
            writer.newLine();

            for (String systemConst : system) {
                if (ignore != null && ignore.test(systemConst)) continue;

                boolean exists = true;
                for (Enum<E> customConst : custom.getEnumConstants()) {
                    if (systemConst.equals(customConst.name())) {
                        exists = false;
                        break;
                    }
                }
                if (exists) {
                    writer.write(systemConst + ',');
                    writer.newLine();
                }
            }

            writer.newLine();
            writer.write("------------------ Removed ------------------");
            writer.newLine();

            for (Enum<E> customConst : custom.getEnumConstants()) {
                boolean exists = true;
                for (String systemConst : system) {
                    if (systemConst.equals(customConst.name())) {
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
