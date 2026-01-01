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

package com.cryptomorin.xseries.test.writer;

import com.cryptomorin.xseries.*;
import com.cryptomorin.xseries.base.XModule;
import com.cryptomorin.xseries.base.XRegistry;
import com.cryptomorin.xseries.particles.XParticle;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import com.cryptomorin.xseries.test.Constants;
import com.cryptomorin.xseries.test.util.XLogger;
import org.bukkit.GameRule;
import org.bukkit.Keyed;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
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
        Path serverFolder = Constants.getTestPath();
        XLogger.log("Server container: " + serverFolder.toAbsolutePath());

        Path materials = serverFolder.resolve("XMaterial.txt"),
                gameRules = serverFolder.resolve("XGameRule.txt"),
                sounds = serverFolder.resolve("XSound.txt"),
                potion = serverFolder.resolve("XPotion.txt"),
                enchantment = serverFolder.resolve("XEnchantment.txt"),
                patternType = serverFolder.resolve("XPatternType.txt"),
                particle = serverFolder.resolve("XParticle.txt"),
                entityType = serverFolder.resolve("XEntityType.txt"),
                itemFlag = serverFolder.resolve("XItemFlag.txt"),
                attributes = serverFolder.resolve("XAttribute.txt"),
                biomes = serverFolder.resolve("XBiome.txt");

        // TODO - Right now the difference writer doesn't properly consider the version that is being tested
        new DiffWriter(materials).ignore(mat -> mat.startsWith("LEGACY_")).writeDifference(org.bukkit.Material.class, XMaterial.class);
        new DiffWriter(gameRules).writeDifference(getEnumLikeFields(GameRule.class), getEnumLikeFields(XGameRule.class));
        new DiffWriter(sounds).writeDifference(Sound.class, XSound.REGISTRY);
        new DiffWriter(biomes).writeDifference(Biome.class, XBiome.REGISTRY);
        new DiffWriter(entityType).writeDifference(getEnumLikeFields(EntityType.class), XEntityType.class);
        new DiffWriter(itemFlag).writeDifference(getEnumLikeFields(ItemFlag.class), XItemFlag.class);
        new DiffWriter(potion).writeDifference(getEnumLikeFields(PotionEffectType.class), XPotion.class);
        new DiffWriter(enchantment).writeDifference(Enchantment.class, XEnchantment.REGISTRY);
        new DiffWriter(attributes).writeDifference(Attribute.class, XAttribute.REGISTRY);

        if (XReflection.supports(9))
            new DiffWriter(particle).writeDifference(getEnumLikeFields(Particle.class), XParticle.class);

        if (XReflection.supports(13))
            new DiffWriter(patternType).writeDifference(PatternType.class, XPatternType.REGISTRY);

        // printRegistryNames(Sound.class);
    }

    public static void printRegistryNames(Class<?> enume) {
        if (!KEYED_EXISTS) return;
        if (!enume.isEnum()) return;
        if (!Keyed.class.isAssignableFrom(enume)) return;

        for (Object enumConstant : enume.getEnumConstants()) {
            XLogger.log(((Enum<?>) enumConstant).name() + " -> " + ((Keyed) enumConstant).getKey());
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
                        throw new IllegalStateException("Failed to get enum value of " + x.getName(), e);
                    }
                })
                .collect(Collectors.toList());
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
     */
    private static final class DiffWriter {
        private final Path path;
        @SuppressWarnings("StringBufferField")
        private final StringBuilder writer = new StringBuilder(100);
        private java.util.function.Predicate<String> ignore;

        private DiffWriter(Path path) {this.path = path;}

        /**
         * Used soley for legacy materials.
         */
        public DiffWriter ignore(java.util.function.Predicate<String> ignore) {
            this.ignore = ignore;
            return this;
        }

        private void newLine() {
            writer.append('\n');
        }

        private void writeAdded() {
            writer.append("----------------------------------------------------------------------------");
            newLine();
            writer.append("---------------------------------- Added -----------------------------------");
            newLine();
            writer.append("----------------------------------------------------------------------------");
            newLine();
            newLine();
        }

        private void writeRemoved() {
            newLine();
            newLine();
            writer.append("----------------------------------------------------------------------------");
            newLine();
            writer.append("---------------------------------- Removed ---------------------------------");
            newLine();
            writer.append("----------------------------------------------------------------------------");
            newLine();
            newLine();
        }

        private void writeToFile() {
            if (writer.length() == 0) return;
            try {
                List<String> lines = Arrays.stream(writer.toString().split("\n")).collect(Collectors.toList());
                OpenOption[] openOptions = {StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};
                Files.write(path, lines, StandardCharsets.UTF_8, openOptions);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public <S extends Enum<S>, E extends Enum<E>>
        void writeDifference(Class<S> system, Class<E> custom) {
            List<String> enumNames = Arrays.stream(system.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
            writeDifference(enumNames, custom);
        }

        public <X extends XModule<X, Bukkit>, Bukkit> void writeDifference(
                Class<Bukkit> systemRegistryClass, XRegistry<X, Bukkit> xRegistry
        ) {
            List<EnumLike<Bukkit>> systemRegistry = getEnumLikePairFields(systemRegistryClass);
            boolean hasEntries = false;

            for (EnumLike<Bukkit> systemConst : systemRegistry) {
                boolean changed = false;
                List<String> altNames = new ArrayList<>(5);

                // For enums these two are the same.
                String ns = systemConst.value == null ? null : XRegistry.getBukkitName(systemConst.value);
                Optional<X> byNsName = ns == null ? Optional.empty() : xRegistry.getByName(ns);
                Optional<X> byFieldName = xRegistry.getByName(systemConst.name);

                if (ns != null && !byNsName.isPresent()) {
                    changed = true;
                    if (ns.startsWith("minecraft:")) ns = ns.substring("minecraft:".length());
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
                    if (!hasEntries) {
                        hasEntries = true;
                        writeAdded();
                        writeInfoAnnotation();
                        writer.append("public static final ").append(xRegistry.getXFormClass().getSimpleName());
                        newLine();
                    }


                    String otherNames = altNames.stream().map(j -> '"' + j + '"').collect(Collectors.joining(", "));
                    writer.append("    ").append(systemConst.name).append(" = std(").append(otherNames).append("),");
                    newLine();
                }
            }

            hasEntries = false;
            for (X customConst : xRegistry) {
                if (customConst.getMetadata().wasRemoved()) continue;

                boolean exists = customConst.isSupported();
                if (!exists) {
                    if (!hasEntries) {
                        hasEntries = true;
                        writeRemoved();
                    }
                    writer.append(customConst.name());
                    newLine();
                }
            }

            writeToFile();
        }

        private void writeInfoAnnotation() {
            writer.append("@XInfo(since = \"")
                    .append(XReflection.MAJOR_NUMBER)
                    .append('.').append(XReflection.MINOR_NUMBER)
                    .append('.').append(XReflection.PATCH_NUMBER).append("\")");
            // newLine(); It looks cleaner without the new line
            writer.append(' ');
        }

        public <E extends Enum<E>> void writeDifference(List<String> system, List<String> xForm) {
            writeDifference(system, xForm, null);
        }

        public <E extends Enum<E>> void writeDifference(List<String> system, Class<E> xForm) {
            writeDifference(system, Arrays.stream(xForm.getEnumConstants()).map(Enum::name).collect(Collectors.toList()), xForm);
        }

        public <E extends Enum<E>> void writeDifference(List<String> system, List<String> xForm, @Nullable Class<E> xFormClass) {
            boolean hasEntries = false;

            for (String systemConst : system) {
                if (ignore != null && ignore.test(systemConst)) continue;

                boolean exists = true;
                for (String customConst : xForm) {
                    if (systemConst.equals(customConst)) {
                        exists = false;
                        break;
                    }
                }
                if (exists) {
                    if (!hasEntries) {
                        hasEntries = true;
                        writeAdded();
                    }
                    writeInfoAnnotation();
                    writer.append(systemConst).append(',');
                    newLine();
                }
            }

            hasEntries = false;
            for (String customConst : xForm) {
                try {
                    if (xFormClass != null && xFormClass.getDeclaredField(customConst).isAnnotationPresent(Deprecated.class)) continue;
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }

                boolean exists = false;
                for (String systemConst : system) {
                    if (systemConst.equals(customConst)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    if (!hasEntries) {
                        hasEntries = true;
                        writeRemoved();
                    }
                    writer.append(customConst);
                    newLine();
                }
            }

            writeToFile();
        }
    }
}
