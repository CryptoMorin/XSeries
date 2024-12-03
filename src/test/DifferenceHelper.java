import com.cryptomorin.xseries.*;
import com.cryptomorin.xseries.base.XModule;
import com.cryptomorin.xseries.base.XRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.intellij.lang.annotations.Language;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        writeDifference(sounds, Sound.class, XSound.REGISTRY, null);
        writeDifference(biomes, Biome.class, XBiome.REGISTRY, null);
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
            // this.value = Objects.requireNonNull(value, () -> "Field " + name + " was not initialized");
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

    @SuppressWarnings({"unused", "RegExpRedundantEscape", "RegExpRepeatedSpace"})
    private static final Pattern ENUM_REPLACER = Pattern.compile(
            "(?<comments>    \\/\\*\\*\\s+(?:\\*[\\w\\. ]+\\s+)+\\*\\/\\n)?    (?<name>[A-Z_0-9]+)(?:\\((?<params>.+)\\))?[,;]");
    private static final Pattern PARAM_ENUMERATOR = Pattern.compile(
            "(?<before>[^\"]+)?(?<after>\".+)?");

    public static String replaceEnums(String txt, String xForm) {
        Matcher matcher = ENUM_REPLACER.matcher(txt);

        StringBuilder builder = new StringBuilder(1000);
        StringJoiner declr = null;

        while (matcher.find()) {
            String comments = matcher.group("comments");
            String name = matcher.group("name");
            String params = matcher.group("params");
            String indent = "    ";

            if (declr == null || comments != null) {
                if (declr != null) builder.append(declr);
                declr = new StringJoiner(",\n", indent + "public static final " + xForm + '\n', ";\n\n");
            }

            if (params == null || !params.toLowerCase(Locale.ENGLISH).contains('"' + name.toLowerCase(Locale.ENGLISH) + '"')) {
                if (params == null) params = '"' + name + '"';
                else {
                    // Make the field name the first name that is checked.
                    // $1 = everything before the name list.
                    // $2 = the name list.
                    Matcher mathcedParam = PARAM_ENUMERATOR.matcher(params);
                    if (mathcedParam.find()) {
                        String before = mathcedParam.group("before");
                        String after = mathcedParam.group("after");
                        params
                                = (before == null ? "" : before)
                                + (after == null ? ", " : "") // If there is more, then they put the comma for us
                                + ('"' + name + '"')
                                + (after == null ? "" : ", " + after);
                    }
                }
            }
            declr.add(indent + indent + name + " = std(" + params + ')');
            if (comments != null) {
                builder.append(comments);
                builder.append(declr);
                declr = null;
            }
        }
        if (declr != null) builder.append(declr);

        return builder.toString();
    }

    @SuppressWarnings("ALL")
    public static <T extends Enum<T>> void enumToRegistry(Path enumClass, Path writeTo) {
        // if (!enumClass.isEnum()) throw new IllegalArgumentException("Provided class is not an enum: " + enumClass);

        String xForm = enumClass.getFileName().toString().replaceFirst("[.][^.]+$", "");
        ;
        String bukkitForm = xForm.substring(1);

        try (BufferedWriter writer = Files.newBufferedWriter(
                writeTo.resolve(xForm + ".java"),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writeTo(writer, "public final class XForm extends XModule<XForm, BForm> {}", xForm, bukkitForm, 1);
            writer.newLine();
            writeTo(writer, "    private static final XRegistry<XForm, BForm> REGISTRY = \n" +
                    "       new XRegistry<>(BForm.class, XForm.class, () -> Registry.UBForm, XForm::new, XForm[]::new);", xForm, bukkitForm);

            writer.newLine();
            writer.newLine();
            writer.write(replaceEnums(Files.readAllLines(enumClass).stream().collect(Collectors.joining("\n")), xForm));
            writer.newLine();
            writer.newLine();

            writeTo(writer, "    private XForm(BForm LBForm, String[] names) {\n" +
                    "        super(LBForm, names);\n" +
                    "    }", xForm, bukkitForm);
            writer.newLine();
            writer.newLine();

            writeTo(writer, "    @NotNull\n    public static XForm of(@NotNull BForm LBForm) {\n" +
                    "        return REGISTRY.getByBukkitForm(LBForm);\n" +
                    "    }\n" +
                    '\n' +
                    "    public static Optional<XForm> of(@NotNull String LBForm) {\n" +
                    "        return REGISTRY.getByName(LBForm);\n" +
                    "    }\n" +
                    '\n' +
                    "    @NotNull\n    public static XForm[] values() {\n" +
                    "        return REGISTRY.values();\n" +
                    "    }\n" +
                    '\n' +
                    "    @NotNull\n    private static XForm std(@NotNull String... names) {\n" +
                    "        return REGISTRY.std(names);\n" +
                    "    }", xForm, bukkitForm);

            writer.newLine();
            writer.write('}');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeTo(Writer writer, @Language("Java") String txt, String xForm, String bukkitForm) throws IOException {
        writeTo(writer, txt, xForm, bukkitForm, 0);
    }

    private static void writeTo(Writer writer, @Language("Java") String txt, String xForm, String bukkitForm, int deleteLast) throws IOException {
        writer.write(txt
                .substring(0, txt.length() - deleteLast)
                .replace("XForm", xForm)
                .replace("UBForm", bukkitForm.toUpperCase(Locale.ENGLISH))
                .replace("LBForm", bukkitForm.toLowerCase(Locale.ENGLISH))
                .replace("BForm", bukkitForm));
    }
}
