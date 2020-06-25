package com.cryptomorin.xseries;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Examples {
    private static void print(String str) {
        Bukkit.getLogger().info(str);
    }

    public static void testMaterials() {
        XMaterial[] subjects = {XMaterial.MELON, XMaterial.MELON_SLICE, XMaterial.CARROT, XMaterial.CARROTS,
                XMaterial.MAP, XMaterial.FILLED_MAP, XMaterial.BLACK_GLAZED_TERRACOTTA, XMaterial.COD_BUCKET, XMaterial.WHITE_DYE};

        for (XMaterial subject : subjects) {
            Material parsed = subject.parseMaterial();
            Material suggestion = subject.parseMaterial(true);

            print("Matched(" + subject.name() + ") -> " + XMaterial.matchXMaterial(subject.name()) +
                    ", parsed: " + parsed + ", suggestion: " + suggestion);
        }
    }

    public static void convertYAMLMaterial(File file) {
        StringBuilder sb = new StringBuilder();

        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().startsWith("type:")) sb.append(line);
                    else {
                        int index = line.indexOf(':');
                        String material = line.substring(index + 1);
                        XMaterial mat = XMaterial.matchXMaterial(material).orElse(null);
                        if (mat == null || mat.name().contains(mat.parseMaterial().name()) || mat.parseMaterial().name().contains(mat.name())) {
                            sb.append(line).append(System.lineSeparator());
                            continue;
                        }
                        sb.append(line, 0, index).append(": ").append(mat.parseMaterial().name());
                        if (!XMaterial.isNewVersion() && mat.getData() != 0) {
                            sb.append(System.lineSeparator());
                            sb.append(line, 0, index - 4).append("damage: ").append(mat.getData());
                        }
                    }
                    sb.append(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(sb.toString());
                writer.flush();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Writes the material and sound differences for updating purposes.
     *
     * @param path the path folder to save the files to.
     * @since 6.0.0
     */
    public static void versionDifference(Path path) {
        Path materials = path.resolve("XMaterial.txt");
        Path sounds = path.resolve("XSound.txt");

        writeDifference(materials, Material.class, XMaterial.class);
        writeDifference(sounds, Sound.class, XSound.class);
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
     * @since 1.0.0
     */
    public static <S extends Enum<S>, E extends Enum<E>> void writeDifference(@Nonnull Path path, @Nonnull Class<S> system, @Nonnull Class<E> custom) {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            for (Enum<S> systemConst : system.getEnumConstants()) {
                boolean exists = true;
                for (Enum<E> customConst : custom.getEnumConstants()) {
                    if (systemConst.name().equals(customConst.name())) {
                        exists = false;
                        break;
                    }
                }
                if (exists) {
                    writer.write(systemConst.name());
                    writer.newLine();
                }
            }
            writer.write("--------------------------------");
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
