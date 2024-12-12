import com.cryptomorin.xseries.*;
import com.cryptomorin.xseries.base.XModule;
import com.cryptomorin.xseries.base.XRegistry;
import com.cryptomorin.xseries.particles.XParticle;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
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
    private static final boolean KEYED_EXISTS = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.BUKKIT).named("Keyed").exists();

    /**
     * Writes the material and sound differences to files in the server's root folder for updating purposes.
     */
    public static void versionDifference() {
        Path serverFolder = Bukkit.getWorldContainer().toPath();
        System.out.println("Server container: " + serverFolder.toAbsolutePath());

        Path materials = serverFolder.resolve("XMaterial.txt"),
                sounds = serverFolder.resolve("XSound.txt"),
                potion = serverFolder.resolve("XPotion.txt"),
                enchantment = serverFolder.resolve("XEnchantment.txt"),
                patternType = serverFolder.resolve("XPatternType.txt"),
                particle = serverFolder.resolve("XParticle.txt"),
                entityType = serverFolder.resolve("XEntityType.txt"),
                itemFlag = serverFolder.resolve("XItemFlag.txt"),
                biomes = serverFolder.resolve("XBiome.txt");

        // TODO - Right now the difference writer doesn't properly consider the version that is being tested
        //        and it also doesn't stop reporting about entries that are known to be removed but are only
        //        kept for cross-compatibility purposes.
        writeDifference(materials, org.bukkit.Material.class, XMaterial.class, mat -> mat.startsWith("LEGACY_"));
        writeDifference(sounds, Sound.class, XSound.REGISTRY, null);
        writeDifference(biomes, Biome.class, XBiome.REGISTRY, null);
        writeDifference(entityType, getEnumLikeFields(EntityType.class), XEntityType.class, null);
        writeDifference(itemFlag, getEnumLikeFields(ItemFlag.class), XItemFlag.class, null);
        writeDifference(potion, getEnumLikeFields(PotionEffectType.class), XPotion.class, null);
        writeDifference(enchantment, getEnumLikeFields(Enchantment.class), XEnchantment.class, null);

        if (XReflection.supports(9))
            writeDifference(particle, getEnumLikeFields(Particle.class), XParticle.class, null);

        if (XReflection.supports(13))
            writeDifference(patternType, PatternType.class, XPatternType.REGISTRY, null);

        // printRegistryNames(Sound.class);
    }

    public static void printRegistryNames(Class<?> enume) {
        if (!KEYED_EXISTS) return;
        if (!enume.isEnum()) return;
        if (!Keyed.class.isAssignableFrom(enume)) return;

        for (Object enumConstant : enume.getEnumConstants()) {
            System.out.println(((Enum<?>) enumConstant).name() + " -> " + ((Keyed) enumConstant).getKey());
        }
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
            // this.value = Objects.requireNonNull(value, () -> "Field " + name + " was not initialized");
        }
    }

    public static <X extends XModule<X, Bukkit>, Bukkit>
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
                String ns = systemConst.value == null ? null : XRegistry.getName(systemConst.value);
                Optional<X> byNsName = ns == null ? Optional.empty() : xRegistry.getByName(ns);
                Optional<X> byFieldName = xRegistry.getByName(systemConst.name);

                if (ns != null && !byNsName.isPresent()) {
                    changed = true;
                    altNames.add(ns);
                }
                if (!byFieldName.isPresent()) {
                    changed = true;
                }

                // Checking isSupported() here wouldn't always work because of experimental fields
                // added to the registry-like classes initialize to null.
                X x = byNsName.orElseGet(() -> byFieldName.orElse(null));
                if (x != null && changed) altNames.addAll(Arrays.asList(x.getNames()));

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
    void writeDifference(Path path, List<String> system, Class<E> xForm, java.util.function.Predicate<String> ignore) {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("------------------- Added -------------------");
            writer.newLine();

            for (String systemConst : system) {
                if (ignore != null && ignore.test(systemConst)) continue;

                boolean exists = true;
                for (Enum<E> customConst : xForm.getEnumConstants()) {
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

            for (Enum<E> customConst : xForm.getEnumConstants()) {
                boolean exists = false;
                for (String systemConst : system) {
                    if (systemConst.equals(customConst.name())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    writer.write(customConst.name());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
