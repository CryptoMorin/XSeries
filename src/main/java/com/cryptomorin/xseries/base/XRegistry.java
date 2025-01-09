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
package com.cryptomorin.xseries.base;

import com.cryptomorin.xseries.base.annotations.XMerge;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A registry similar to Bukkit's {@link Registry}. It holds values as a form of {@link XBase} and
 * allows cross-version name mappings and also direct bukkit-to-xform mappings as well.
 *
 * @param <XForm>      The type used within this library.
 * @param <BukkitForm> The corresponding Bukkit type of the {@link XForm}.
 * @see XBase
 * @see XModule
 */
@ApiStatus.Internal
public final class XRegistry<XForm extends XBase<XForm, BukkitForm>, BukkitForm> implements Iterable<XForm> {
    /**
     * Lenient system that adds unknown values automatically on the fly.
     * Should be turned off for {@code DifferenceHelper} which is handled
     * by reflection.
     */
    @SuppressWarnings("FieldMayBeFinal")
    private static boolean PERFORM_AUTO_ADD = true;

    private static final boolean KEYED_EXISTS;

    static {
        boolean keyedExists = false;
        try {
            Class.forName("org.bukkit.Keyed");
            keyedExists = true;
        } catch (ClassNotFoundException ignored) {
        }

        KEYED_EXISTS = keyedExists;
    }

    /**
     * All entries are lowercase.
     * Entries that belong to "minecraft" namespace, are added without the namespace.
     */
    private final Map<String, XForm> nameMappings = new HashMap<>(20);
    private final Map<BukkitForm, XForm> bukkitToX = new IdentityHashMap<>(20);

    private final Class<BukkitForm> bukkitFormClass;
    private final Class<XForm> xFormClass;

    private final Supplier<Object> registrySupplier;
    private final BiFunction<BukkitForm, String[], XForm> creator;
    private final Function<Integer, XForm[]> createArray;
    private final String registryName;

    private final boolean supportsRegistry;
    private final ClassType bukkitClassType;
    private boolean pulled = false;

    @ApiStatus.Internal
    public XRegistry(Class<BukkitForm> bukkitFormClass, Class<XForm> xFormClass,
                     Supplier<Object> registrySupplier,
                     BiFunction<BukkitForm, String[], XForm> creator,
                     Function<Integer, XForm[]> createArray) {
        boolean supported;
        try {
            registrySupplier.get();
            supported = true;
        } catch (Throwable ex) {
            supported = false;
        }

        this.bukkitFormClass = Objects.requireNonNull(bukkitFormClass);
        this.xFormClass = Objects.requireNonNull(xFormClass);
        this.registryName = this.bukkitFormClass.getSimpleName();
        this.registrySupplier = registrySupplier;
        this.createArray = Objects.requireNonNull(createArray);
        this.creator = creator;

        // Just because the registry exists, doesn't necessarily mean that
        // the class itself cannot be an enum.
        supportsRegistry = supported;
        if (bukkitFormClass.isEnum()) {
            bukkitClassType = ClassType.ENUM;
        } else if (Modifier.isAbstract(bukkitFormClass.getModifiers())) {
            bukkitClassType = ClassType.ABSTRACTION;
        } else {
            bukkitClassType = null;
        }

        if (!supportsRegistry && bukkitClassType == null) {
            throw new IllegalStateException("Bukkit form is not an enum, abstraction or a registry " + bukkitFormClass);
        }
    }

    private enum ClassType {
        ENUM, ABSTRACTION;
    }

    @ApiStatus.Internal
    public XRegistry(Class<BukkitForm> bukkitFormClass, Class<XForm> xFormClass, Function<Integer, XForm[]> createArray) {
        this(bukkitFormClass, xFormClass, null, null, createArray);
    }

    @ApiStatus.Internal
    @NotNull
    public Map<String, XForm> nameMapping() {
        return nameMappings;
    }

    @ApiStatus.Internal
    @NotNull
    public Map<BukkitForm, XForm> bukkitMapping() {
        return bukkitToX;
    }

    /**
     * Gets the class of the bukkit form.
     */
    public Class<BukkitForm> getBukkitFormClass() {
        return bukkitFormClass;
    }

    /**
     * Gets the class of the xform.
     */
    public Class<XForm> getXFormClass() {
        return xFormClass;
    }

    /**
     * Gets the name of the registry, which is usually the bukkit form's simple class name.
     */
    public String getName() {
        return registryName;
    }

    private void pullValues() {
        if (!pulled) {
            pulled = true;
            if (creator == null) return;
            pullFieldNames();
            if (PERFORM_AUTO_ADD) pullSystemValues();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void processEnumLikeFields(Class<T> clazz, BiConsumer<Field, T> consumer) {
        for (Field field : clazz.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (field.getType() == clazz &&
                    Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                try {
                    consumer.accept(field, (T) field.get(null));
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Cannot process enum-like fields of: " + clazz, e);
                }
            }
        }
    }

    @ApiStatus.Internal
    public void registerName(String name, XForm xForm) {
        nameMappings.put(normalizeName(name), xForm);
    }

    @SuppressWarnings("unused")
    private void pullFieldNames() {
        processEnumLikeFields(xFormClass, (field, x) -> registerMerged(x, field));
        // processEnumLikeFields(xFormClass, (name, xForm) -> {
        //     xForm.setEnumName(normalizeName(name));
        //     registerName(name, xForm);
        // });
    }

    @SuppressWarnings("unchecked")
    private void pullSystemValues() {
        // Enum-life names
        if (bukkitClassType == ClassType.ENUM) {
            for (BukkitForm bukkitForm : bukkitFormClass.getEnumConstants()) {
                std(((Enum<?>) bukkitForm).name(), bukkitForm);
            }
        } else {
            processEnumLikeFields(bukkitFormClass, (field, bukkit) -> {
                if (bukkit == null) return; // Experimental value of declared field.
                std(field.getName(), bukkit);
            });
        }

        // Minecraft namespaces
        if (supportsRegistry) {
            for (Keyed bukkitForm : bukkitRegistry()) {
                std((BukkitForm) bukkitForm);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private BukkitForm valueOf(String name) {
        name = name.toUpperCase(Locale.ENGLISH).replace('.', '_');
        Class<Enum> clazz = (Class<Enum>) bukkitFormClass;

        try {
            return (BukkitForm) Enum.valueOf(clazz, name);
        } catch (IllegalArgumentException ignored) {
            // Thrown even if this is not an enum, which shouldn't happen.
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private BukkitForm fieldOf(String name) {
        try {
            return (BukkitForm) bukkitFormClass.getDeclaredField(name).get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return null;
        }
    }

    @NotNull
    private Registry<?> bukkitRegistry() {
        return ((Registry<?>) registrySupplier.get());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected BukkitForm getBukkit(String[] names) {
        for (String name : names) {
            BukkitForm bukkitForm;

            if (supportsRegistry) {
                name = name.toLowerCase(Locale.ENGLISH);
                // if (!name.equals(name.toLowerCase(Locale.ENGLISH))) {
                //     // Namespaces don't support uppercase names which is for enums.
                //     continue;
                // }

                NamespacedKey key;
                if (name.contains(":")) key = NamespacedKey.fromString(name);
                else key = NamespacedKey.minecraft(name);

                Keyed bukkit = bukkitRegistry().get(key);
                if (bukkit != null) bukkitForm = (BukkitForm) bukkit;
                else bukkitForm = null;
            } else if (bukkitClassType == ClassType.ENUM) {
                bukkitForm = valueOf(name);
            } else if (bukkitClassType == ClassType.ABSTRACTION) {
                // For classes like the Enchantment class for older versions.
                bukkitForm = fieldOf(name);
            } else {
                throw new AssertionError("None of the class strategies worked for " + this);
            }

            if (bukkitForm != null) return bukkitForm;
        }
        return null;
    }

    /**
     * @see #values()
     */
    @Unmodifiable
    @NotNull
    public Collection<XForm> getValues() {
        pullValues();

        // Don't use nameMapping because it will return duplicates.
        return Collections.unmodifiableCollection(bukkitToX.values());
    }

    /**
     * @deprecated Use of {@code values()} should be discouraged by static {@link XModule} implementations,
     * instead {@link #getValues()} should be used.
     */
    @Deprecated
    public XForm[] values() {
        pullValues();

        // Don't use getValues() for extra unmodifiable overhead.
        Collection<XForm> values = bukkitToX.values();
        return values.toArray(createArray.apply(values.size()));
    }

    @NotNull
    @Override
    public Iterator<XForm> iterator() {
        return getValues().iterator();
    }

    @NotNull
    public XForm getByBukkitForm(BukkitForm bukkit) {
        Objects.requireNonNull(bukkit, () -> "Cannot match null " + registryName);
        XForm mapping = bukkitToX.get(bukkit);

        if (mapping == null) {
            if (!PERFORM_AUTO_ADD) // If you ever get this error, it could mean that you're not following Minecraft's new dot separated namespace format.
                throw new UnsupportedOperationException("Unknown standard bukkit form (no auto-add) for " + registryName + ": " + bukkit);
            if (creator == null)
                throw new UnsupportedOperationException("Unsupported value for " + registryName + ": " + bukkit);
            XForm xForm = std(bukkit);
            if (xForm == null) throw new IllegalStateException("Unknown " + registryName + ": " + bukkit);
        }

        return mapping;
    }

    public Optional<XForm> getByName(@NotNull String name) {
        Objects.requireNonNull(name, () -> "Cannot match null " + registryName);
        if (name.isEmpty()) return Optional.empty();

        pullValues(); // Ensure field names are loaded too.
        return Optional.ofNullable(nameMappings.get(normalizeName(name)));
    }

    @SuppressWarnings("deprecation")
    @ApiStatus.Internal
    @NotNull
    public static String getBukkitName(@NotNull Object bukkitForm) {
        Objects.requireNonNull(bukkitForm, "Cannot get name of a null bukkit form");

        if (bukkitForm instanceof Enum) {
            return ((Enum<?>) bukkitForm).name();
        } else if (KEYED_EXISTS && bukkitForm instanceof Keyed) {
            return ((Keyed) bukkitForm).getKey().toString();
        } else if (bukkitForm instanceof PotionEffectType) {
            // For older versions which didn't even have Keyed (as far as v1.16?)
            return ((PotionEffectType) bukkitForm).getName();
        } else if (bukkitForm instanceof Enchantment) {
            return ((Enchantment) bukkitForm).getName();
        } else {
            throw new AssertionError("Unknown xform type: " + bukkitForm + " (" + bukkitForm.getClass() + ')');
        }
    }

    /**
     * This method is here for legacy purposes.
     * <p>
     * Attempts to build the string like an enum name.<br>
     * Removes all the spaces, numbers and extra non-English characters. Also removes some config/in-game based strings.
     * While this method is hard to maintain, it's extremely efficient. It's approximately more than x5 times faster than
     * the normal RegEx + String Methods approach for both formatted and unformatted material names.
     *
     * @param name the sound name to format.
     * @return an enum name.
     * @since 1.0.0
     */
    @SuppressWarnings("unused")
    @NotNull
    private static String format(@NotNull String name) {
        int len = name.length();
        char[] chs = new char[len];
        int count = 0;
        boolean appendUnderline = false;

        for (int i = 0; i < len; i++) {
            char ch = name.charAt(i);

            if (!appendUnderline && count != 0 && (ch == '-' || ch == ' ' || ch == '_') && chs[count] != '_')
                appendUnderline = true;
            else {
                boolean number = false;
                // A few sounds have numbers in them.
                if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (number = (ch >= '0' && ch <= '9'))) {
                    if (appendUnderline) {
                        chs[count++] = '_';
                        appendUnderline = false;
                    }

                    if (number) chs[count++] = ch;
                    else chs[count++] = (char) (ch & 0x5f);
                }
            }
        }

        return new String(chs, 0, count);
    }

    private static String normalizeName(String name) {
        // TODO convert this into the format() method above.
        name = name.toLowerCase(Locale.ENGLISH);
        if (name.startsWith("minecraft:")) name = name.substring("minecraft:".length());
        name = name.replace('.', '_'); // This is very unlikely to cause a conflict.
        return name;
    }

    private XForm std(BukkitForm bukkit) {
        return std(null, bukkit);
    }

    private XForm std(@Nullable String extraFieldName, BukkitForm bukkit) {
        XForm xForm = bukkitToX.get(bukkit);
        if (xForm != null) return xForm;

        String name = getBukkitName(bukkit);

        if (getBukkit(new String[]{name}) == null && extraFieldName == null) {
            // This happens in very rare cases, such as Biome's:
            // Biome CUSTOM = Bukkit.getUnsafe().getCustomBiome();
            // These values are not registered in the registry, but available for use.

            // This could also happen if registry is supported but the Bukkit classes uses an enum
            // in that case enum names are not supported with namespace type (even if we uppercase
            // it, we don't know the placement of dots instead of underscores)
            throw new IllegalArgumentException("Unknown standard bukkit form for " + registryName + ": " + bukkit
                    + (bukkit.toString().equals(name) ? "" : (" (" + name + ')')));
        }

        xForm = creator.apply(bukkit, extraFieldName == null ? new String[]{name} : new String[]{extraFieldName, name});
        if (!PERFORM_AUTO_ADD) return xForm;

        registerName(name, xForm);
        if (extraFieldName != null) registerName(extraFieldName, xForm);
        bukkitToX.put(bukkit, xForm);

        return xForm;
    }

    @ApiStatus.Internal
    public XForm std(String[] names) {
        // Doesn't matter if it's not supported, we should still create it.
        BukkitForm bukkit = getBukkit(names);
        XForm xForm = creator.apply(bukkit, names);
        return std(xForm);
    }

    @ApiStatus.Internal
    public BukkitForm stdEnum(XForm xForm, String[] names) {
        String enumName = xForm.name();

        boolean merged = false;
        BukkitForm bukkit = getBukkit(new String[]{enumName});
        if (bukkit == null) bukkit = getBukkit(names);
        if (bukkit == null) {
            bukkit = registerMerged(xForm);
            merged = true;
        }

        return stdEnum0(xForm, names, bukkit, merged);
    }

    public BukkitForm stdEnum(XForm xForm, String[] names, BukkitForm bukkit) {
        return stdEnum0(xForm, names, bukkit, false);
    }

    @ApiStatus.Internal
    private BukkitForm stdEnum0(XForm xForm, String[] names, BukkitForm bukkit, boolean merged) {
        // Doesn't matter if it's not supported, we should still create it.
        String enumName = xForm.name();

        if (!merged) registerMerged(xForm);

        registerName(enumName, xForm);
        for (String name : names) {
            registerName(name, xForm);
        }
        if (bukkit != null) bukkitToX.put(bukkit, xForm);
        return bukkit;
    }

    private BukkitForm registerMerged(XForm xForm) {
        Field formField;
        try {
            formField = xForm.getClass().getDeclaredField(xForm.name());
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Cannot find field for XForm: " + xForm, e);
        }
        return registerMerged(xForm, formField);
    }

    private BukkitForm registerMerged(XForm xForm, Field formField) {
        XMerge[] merges = formField.getAnnotationsByType(XMerge.class);
        BukkitForm mergedBukkit = null;
        for (XMerge merge : merges) { // Will be an empty array if null.
            mergedBukkit = getBukkit(new String[]{merge.name()});
            registerName(merge.name(), xForm);
            if (mergedBukkit != null) bukkitToX.put(mergedBukkit, xForm);
        }
        return mergedBukkit;
    }

    @ApiStatus.Internal
    public XForm std(Function<BukkitForm, XForm> xForm, String[] names) {
        BukkitForm bukkit = getBukkit(names);
        return std(xForm.apply(bukkit));
    }

    @ApiStatus.Internal
    public XForm std(Function<BukkitForm, XForm> xForm, XForm tryOther, String[] names) {
        BukkitForm bukkit = getBukkit(names);
        if (bukkit == null) bukkit = tryOther.get();
        return std(xForm.apply(bukkit));
    }

    @ApiStatus.Internal
    public XForm std(XForm xForm) {
        for (String name : xForm.getNames()) {
            registerName(name, xForm);
        }
        if (xForm.isSupported()) bukkitToX.put(xForm.get(), xForm);
        return xForm;
    }

    @Override
    public String toString() {
        return "XRegistry<" + registryName + ">(" +
                "nameMappings=" + nameMappings.size() + ", bukkitToX=" + bukkitToX.size() +
                ", bukkitFormClass=" + bukkitFormClass.getName() +
                ", xFormClass=" + xFormClass.getName() +
                ", supportsRegistry=" + supportsRegistry +
                ", bukkitFormClassType=" + bukkitClassType +
                ", pulled=" + pulled +
                ", values=[" + bukkitToX.values().stream().limit(10).map(XBase::name).collect(Collectors.joining(", ")) + ']' +
                ')';
    }
}
