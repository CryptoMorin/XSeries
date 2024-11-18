import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.base.XModule;
import com.cryptomorin.xseries.base.XRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class DifferenceHelper {
    /**
     * Writes the material and sound differences to files in the server's root folder for updating purposes.
     */
    public static void versionDifference() {
        Path serverFolder = Bukkit.getWorldContainer().toPath();
        System.out.println("Server container: " + serverFolder.toAbsolutePath());

        Path materials = serverFolder.resolve("XMaterial.txt"),
                sounds = serverFolder.resolve("XSound.txt"),
                xPotion = serverFolder.resolve("XPotion.txt"),
                xEnchant = serverFolder.resolve("XEnchantment.txt"),
                biomes = serverFolder.resolve("XBiome.txt");

        writeDifference(materials, org.bukkit.Material.class, XMaterial.class, mat -> mat.startsWith("LEGACY_"));
        // writeDifference(sounds, getEnumLikeFields(org.bukkit.Sound.class, XSound.class, null);
        // writeDifference(biomes, org.bukkit.block.Biome.class, XBiome.class, null);
        writeDifference(xPotion, getEnumLikeFields(PotionEffectType.class), XPotion.class, null);
        writeDifference(xEnchant, getEnumLikeFields(Enchantment.class), XEnchantment.class, null);
    }

    public static List<String> getEnumLikeFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(x -> x.getType() == clazz)
                .filter(x -> {
                    int modifiers = x.getModifiers();
                    return Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && Modifier.isPublic(modifiers);
                })
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static <T> List<EnumLike<T>> getEnumLikePairFields(Class<T> clazz) {
        if (clazz.isEnum()) {
            return Arrays.stream(clazz.getEnumConstants())
                    .map(x -> (Enum<?>) x)
                    .map(x -> new EnumLike<>(x.name(), (T) x))
                    .collect(Collectors.toList());
        }

        return Arrays.stream(clazz.getDeclaredFields())
                .filter(x -> x.getType() == clazz)
                .filter(x -> {
                    int modifiers = x.getModifiers();
                    return Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && Modifier.isPublic(modifiers);
                })
                .map(x -> {
                    try {
                        return new EnumLike<>(x.getName(), (T) x.get(null));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
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

    private static final class EnumLike<T> {
        private final String name;
        private final T value;

        private EnumLike(String name, T value) {
            this.name = name;
            this.value = value;
        }
    }

    public static <X extends XModule<X, Bukkit>, Bukkit extends Keyed>
    void writeDifference(Path path, Class<Bukkit> systemRegistryClass, XRegistry<X, Bukkit> xRegistry, java.util.function.Predicate<String> ignore) {
        List<EnumLike<Bukkit>> systemRegistry = getEnumLikePairFields(systemRegistryClass);

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("------------------- Added -------------------");
            writer.newLine();

            for (EnumLike<Bukkit> systemConst : systemRegistry) {
                boolean changed = false;
                List<String> altNames = new ArrayList<>(5);

                // For enums these two are the same.
                String ns = XRegistry.getName(systemConst.value);
                Optional<X> byNsName = xRegistry.getByName(ns);
                Optional<X> byFieldName = xRegistry.getByName(systemConst.name);

                if (!byNsName.isPresent()) {
                    changed = true;
                    altNames.add(ns);
                }
                if (!byFieldName.isPresent()) {
                    changed = true;
                }

                X x = byNsName.orElseGet(() -> byFieldName.orElse(null));
                if (x != null && !x.isSupported()) {
                    changed = true;
                    altNames.addAll(Arrays.asList(x.names));
                }

                if (changed) {
                    String otherNames = altNames.stream().map(j -> '"' + j + '"').collect(Collectors.joining(", "));
                    writer.write(systemConst.name + " = std(" + otherNames + "),");
                    writer.newLine();
                }
            }

            writer.newLine();
            writer.write("------------------ Removed ------------------");
            writer.newLine();

            for (X customConst : xRegistry) {
                boolean exists = customConst.isSupported();
                if (!exists) {
                    writer.write(customConst.name());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <E extends Enum<E>>
    void writeDifference(Path path, List<String> system, Class<E> custom, java.util.function.Predicate<String> ignore) {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
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
