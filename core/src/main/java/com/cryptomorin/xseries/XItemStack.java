/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Crypto Morin
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
package com.cryptomorin.xseries;

import com.cryptomorin.xseries.paper.AdventureAPIFactory;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import com.cryptomorin.xseries.reflection.XReflection;
import com.google.common.base.Enums;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.map.MapView;
import org.bukkit.material.MaterialData;
import org.bukkit.material.SpawnEgg;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.cryptomorin.xseries.XMaterial.supports;

/**
 * <b>XItemStack</b> - YAML <a href="https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/ItemStack.html">ItemStack</a> Serializer<br>
 * Using ConfigurationSection Example:
 * <pre>{@code
 *     ConfigurationSection section = plugin.getConfig().getConfigurationSection("staffs.dragon-staff");
 *     ItemStack item = XItemStack.deserializer().fromConfig(section).deserialize();
 * }</pre>
 * Serializing back:
 * <pre>{@code
 *     ItemStack item = ...;
 *     ConfigurationSection section = plugin.getConfig().getConfigurationSection("staffs.dragon-staff");
 *     XItemStack.serializer().fromItem(item).toConfig(section).serialize();
 * }</pre>
 * <p>
 * What's the point of this class when {@link org.bukkit.configuration.MemorySection#getItemStack(String)} exists?
 * That method works based on YAML tags which makes the config hideous and doesn't have syntax sugars for certain
 * configurations to make the config cleaner and concise. Also, certain values will have unreadable formats.
 *
 * @author Crypto Morin
 * @version 9.0.0
 * @see XMaterial
 * @see XPotion
 * @see XSkull
 * @see XEnchantment
 * @see ItemStack
 * @see XPatternType
 */
public final class XItemStack {
    /**
     * Because {@link ItemMeta} cannot be applied to {@link Material#AIR}.
     * @deprecated You should now use {@link Deserializer#deserialize()} which handles {@link UnsetMaterialCondition}.
     */
    @Deprecated
    private static final XMaterial DEFAULT_MATERIAL = XMaterial.BARRIER;
    private static final boolean
            SUPPORTS_UNBREAKABLE,
            SUPPORTS_POTION_COLOR,
            SUPPORTS_Inventory_getStorageContents,
            SUPPORTS_CUSTOM_MODEL_DATA,
            SUPPORTS_ADVANCED_CUSTOM_MODEL_DATA,
            SUPPORTS_ITEM_MODEL,
	        SUPPORTS_ITEM_NAME;

    static {
        boolean supportsPotionColor = false,
                supportsUnbreakable = false,
                supportsGetStorageContents = false,
                supportSCustomModelData = false,
                supportsAdvancedCustomModelData = false,
                supportsItemModel = false,
			    supportsItemName = false;


        try {
            ItemMeta.class.getDeclaredMethod("setUnbreakable", boolean.class);
            supportsUnbreakable = true;
        } catch (NoSuchMethodException ignored) {
        }

        try {
            ItemMeta.class.getDeclaredMethod("hasCustomModelData");
            supportSCustomModelData = true;
        } catch (NoSuchMethodException ignored) {
        }

        try {
            ItemMeta.class.getDeclaredMethod("getCustomModelDataComponent");
            supportsAdvancedCustomModelData = true;
        } catch (NoSuchMethodException ignored) {
        }

        try {
            ItemMeta.class.getDeclaredMethod("getItemModel");
            supportsItemModel = true;
        } catch (NoSuchMethodException ignored) {
        }
		
		try {
			ItemMeta.class.getDeclaredMethod("setItemName", String.class);
			supportsItemName = true;
		} catch (NoSuchMethodException ignored) {
		}
		
        try {
            Class.forName("org.bukkit.inventory.meta.PotionMeta").getMethod("setColor", Color.class);
            supportsPotionColor = true;
        } catch (Throwable ignored) {
        }

        try {
            Inventory.class.getDeclaredMethod("getStorageContents");
            supportsGetStorageContents = true;
        } catch (NoSuchMethodException ignored) {
        }

        SUPPORTS_POTION_COLOR = supportsPotionColor;
        SUPPORTS_UNBREAKABLE = supportsUnbreakable;
        SUPPORTS_Inventory_getStorageContents = supportsGetStorageContents;
        SUPPORTS_CUSTOM_MODEL_DATA = supportSCustomModelData;
        SUPPORTS_ADVANCED_CUSTOM_MODEL_DATA = supportsAdvancedCustomModelData;
        SUPPORTS_ITEM_MODEL = supportsItemModel;
		SUPPORTS_ITEM_NAME = supportsItemName;
    }

    private interface MetaHandler<M extends ItemMeta> {
        void handle(M meta);
    }

    private static final Map<Class<? extends ItemMeta>, Optional<Function<Deserializer, MetaHandler<ItemMeta>>>> DESERIALIZE_META_HANDLERS = new IdentityHashMap<>();
    private static final Map<Class<? extends ItemMeta>, Optional<Function<Serializer, MetaHandler<ItemMeta>>>> SERIALIZE_META_HANDLERS = new IdentityHashMap<>();

    private static <M extends ItemMeta> void meta(Class<? extends M> clazz,
                                                  Function<Deserializer, MetaHandler<M>> deserialize,
                                                  Function<Serializer, MetaHandler<M>> serialize) {
        DESERIALIZE_META_HANDLERS.put(
                clazz,
                Optional.of(cast(deserialize))
        );
        SERIALIZE_META_HANDLERS.put(
                clazz,
                Optional.of(cast(serialize))
        );
    }

    private static void onlyIf(String className, Runnable runnable) {
        try {
            Class.forName("org.bukkit.inventory.meta." + className);
            runnable.run();
        } catch (ClassNotFoundException ignored) {
        }
    }

    static {
        // @formatter:off
        meta(SkullMeta       .class, x -> x::handleSkullMeta,        x -> x::handleSkullMeta);
        meta(LeatherArmorMeta.class, x -> x::handleLeatherArmorMeta, x -> x::handleLeatherArmorMeta);
        meta(PotionMeta      .class, x -> x::handlePotionMeta,       x -> x::handlePotionMeta);
        meta(BlockStateMeta  .class, x -> x::handleBlockStateMeta,   x -> x::handleBlockStateMeta);
        meta(FireworkMeta    .class, x -> x::handleFireworkMeta,     x -> x::handleFireworkMeta);
        meta(BookMeta        .class, x -> x::handleBookMeta,         x -> x::handleBookMeta);
        meta(BannerMeta      .class, x -> x::handleBannerMeta,       x -> x::handleBannerMeta);
        meta(MapMeta         .class, x -> x::handleMapMeta,          x -> x::handleMapMeta);
        meta(EnchantmentStorageMeta.class, x -> x::handleEnchantmentStorageMeta,     x -> x::handleEnchantmentStorageMeta);

        onlyIf("SpawnEggMeta",           () -> meta(SpawnEggMeta          .class, x -> x::handleSpawnEggMeta,           x -> x::handleSpawnEggMeta));
        onlyIf("ArmorMeta",              () -> meta(ArmorMeta             .class, x -> x::handleArmorMeta,              x -> x::handleArmorMeta));
        onlyIf("AxolotlBucketMeta",      () -> meta(AxolotlBucketMeta     .class, x -> x::handleAxolotlBucketMeta,      x -> x::handleAxolotlBucketMeta));
        onlyIf("CompassMeta",            () -> meta(CompassMeta           .class, x -> x::handleCompassMeta,            x -> x::handleCompassMeta));
        onlyIf("SuspiciousStewMeta",     () -> meta(SuspiciousStewMeta    .class, x -> x::handleSuspiciousStewMeta,     x -> x::handleSuspiciousStewMeta)); // Apparently Suspicious Stew was never added in 1.14
        onlyIf("CrossbowMeta",           () -> meta(CrossbowMeta          .class, x -> x::handleCrossbowMeta,           x -> x::handleCrossbowMeta));
        onlyIf("TropicalFishBucketMeta", () -> meta(TropicalFishBucketMeta.class, x -> x::handleTropicalFishBucketMeta, x -> x::handleTropicalFishBucketMeta));
        // @formatter:on
    }

    @SuppressWarnings({"OptionalIsPresent", "unchecked"})
    private static <T extends SerialObject> void recursiveMetaHandle(T serialObject, Class<?> metaClass, ItemMeta meta,
                                                                     Map<Class<? extends ItemMeta>, Optional<Function<T, MetaHandler<ItemMeta>>>> map,
                                                                     List<Function<T, MetaHandler<ItemMeta>>> collectedHandlers) {
        Optional<Function<T, MetaHandler<ItemMeta>>> handler = map.get(metaClass);
        if (handler != null) {
            if (handler.isPresent()) {
                if (collectedHandlers != null) collectedHandlers.add(handler.get());
                handler.get().apply(serialObject).handle(meta);
            }

            return;
        }

        // This rarely happens for the interface classes themselves For example:
        // ColorableArmorMeta extends ArmorMeta, LeatherArmorMeta
        // But practically, this will always happen for every metadata, since this
        // will be the Craft class that implements the metadata.

        List<Function<T, MetaHandler<ItemMeta>>> subCollectedHandlers = new ArrayList<>();
        Class<?> superclass = metaClass.getSuperclass();
        if (superclass != null) recursiveMetaHandle(serialObject, superclass, meta, map, subCollectedHandlers);
        for (Class<?> anInterface : metaClass.getInterfaces()) {
            recursiveMetaHandle(serialObject, anInterface, meta, map, subCollectedHandlers);
        }

        if (subCollectedHandlers.isEmpty()) {
            map.put((Class<? extends ItemMeta>) metaClass, Optional.empty());
        } else {
            map.put((Class<? extends ItemMeta>) metaClass, Optional.of(inst -> subMeta -> { // Cool syntax!
                T castedInst = cast(inst);
                for (Function<T, MetaHandler<ItemMeta>> subCollectedHandler : subCollectedHandlers) {
                    subCollectedHandler.apply(castedInst).handle(subMeta);
                }
            }));

            if (collectedHandlers != null)
                collectedHandlers.addAll(subCollectedHandlers);
        }
    }

    private abstract static class SerialObject {
        @Nullable protected ItemStack item;
        @Nullable protected ConfigurationSection config;
        @Nullable protected Function<String, String> translator = Function.identity();
        protected ItemMeta meta;

        /**
         * Copies all properties of this handler, including the item.
         */
        public abstract SerialObject copy();

        /**
         * Copies all properties of this handler without the item.
         */
        public abstract SerialObject copySettings();

        public SerialObject withTranslator(Function<String, String> translator) {
            this.translator = translator;
            return this;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object something) {
        return (T) something;
    }

    private XItemStack() {}

    private static BlockState safeBlockState(BlockStateMeta meta) {
        try {
            return meta.getBlockState();
        } catch (IllegalStateException ex) {
            // Due to a bug in the latest paper v1.9-1.10 (and some older v1.11) versions.
            // java.lang.IllegalStateException: Missing blockState for BREWING_STAND_ITEM
            // BREWING_STAND_ITEM, ENCHANTMENT_TABLE, REDSTONE_COMPARATOR
            // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/diff/src/main/java/org/bukkit/craftbukkit/inventory/CraftMetaBlockState.java?until=b6ad714e853042def52620befe9bc85d0137cd71
            if (ex.getMessage().toLowerCase(Locale.ENGLISH).contains("missing blockstate")) {
                return null;
            } else {
                throw ex;
            }
        } catch (ClassCastException ex) {
            // java.lang.ClassCastException: net.minecraft.server.v1_9_R2.TileEntityDispenser cannot be cast to net.minecraft.server.v1_9_R2.TileEntityDropper
            return null;
        }
    }

    /**
     * @see #serialize(ItemStack, ConfigurationSection, Function)
     * @since 1.0.0
     * @deprecated Use {@link Serializer} instead.
     */
    @Deprecated
    public static void serialize(@NotNull ItemStack item, @NotNull ConfigurationSection config) {
        serialize(item, config, Function.identity());
    }

    /**
     * Writes an ItemStack object into a config.
     * The config file will not save after the object is written.
     *
     * @param item       the ItemStack to serialize.
     * @param config     the config section to write this item to.
     * @param translator the function applied to item name and each lore lines.
     * @since 7.4.0
     * @deprecated Use {@link Serializer} instead.
     */
    @Deprecated
    public static void serialize(@NotNull ItemStack item,
                                 @NotNull ConfigurationSection config,
                                 @NotNull Function<String, String> translator) {
        new Serializer()
                .fromItem(item)
                .toConfig(config)
                .withTranslator(translator)
                .serialize();
    }

    /**
     * Writes an ItemStack properties into a {@code Map}.
     *
     * @param item the ItemStack to serialize.
     * @return a Map containing the serialized ItemStack properties.
     * @deprecated Use {@link Serializer} instead.
     */
    @Deprecated
    public static Map<String, Object> serialize(@NotNull ItemStack item) {
        Objects.requireNonNull(item, "Cannot serialize a null item");
        ConfigurationSection config = new MemoryConfiguration();
        serialize(item, config);
        return configSectionToMap(config);
    }

    /**
     * Deserialize an ItemStack from the config.
     *
     * @param config the config section to deserialize the ItemStack object from.
     * @return a deserialized ItemStack.
     * @since 1.0.0
     * @deprecated Use {@link Deserializer} instead.
     */
    @NotNull
    @Deprecated
    public static ItemStack deserialize(@NotNull ConfigurationSection config) {
        return edit(DEFAULT_MATERIAL.parseItem(), config, Function.identity(), null);
    }

    /**
     * Deserialize an ItemStack from a {@code Map}.
     *
     * @param serializedItem the map holding the item configurations to deserialize
     *                       the ItemStack object from.
     * @return a deserialized ItemStack.
     * @deprecated Use {@link Deserializer} instead.
     */
    @NotNull
    @Deprecated
    public static ItemStack deserialize(@NotNull Map<String, Object> serializedItem) {
        Objects.requireNonNull(serializedItem, "serializedItem cannot be null.");
        return deserialize(mapToConfigSection(serializedItem));
    }

    /**
     * @deprecated Use {@link Deserializer} instead.
     */
    @NotNull
    @Deprecated
    public static ItemStack deserialize(@NotNull ConfigurationSection config,
                                        @NotNull Function<String, String> translator) {
        return deserialize(config, translator, null);
    }

    /**
     * Deserialize an ItemStack from the config.
     *
     * @param config the config section to deserialize the ItemStack object from.
     * @return an edited ItemStack.
     * @since 7.2.0
     * @deprecated Use {@link Deserializer} instead.
     */
    @NotNull
    @Deprecated
    public static ItemStack deserialize(@NotNull ConfigurationSection config,
                                        @NotNull Function<String, String> translator,
                                        @Nullable Consumer<Exception> restart) {
        return edit(DEFAULT_MATERIAL.parseItem(), config, translator, restart);
    }


    /**
     * Deserialize an ItemStack from a {@code Map}.
     *
     * @param serializedItem the map holding the item configurations to deserialize
     *                       the ItemStack object from.
     * @param translator     the translator to use for translating the item's name.
     * @return a deserialized ItemStack.
     * @deprecated Use {@link Deserializer} instead.
     */
    @NotNull
    @Deprecated
    public static ItemStack deserialize(@NotNull Map<String, Object> serializedItem, @NotNull Function<String, String> translator) {
        Objects.requireNonNull(serializedItem, "serializedItem cannot be null.");
        Objects.requireNonNull(translator, "translator cannot be null.");
        return deserialize(mapToConfigSection(serializedItem), translator);
    }

    private static int toInt(String str, @SuppressWarnings("SameParameterValue") int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    private static List<String> split(@NotNull String str, @SuppressWarnings("SameParameterValue") char separatorChar) {
        List<String> list = new ArrayList<>(5);
        boolean match = false, lastMatch = false;
        int len = str.length();
        int start = 0;

        for (int i = 0; i < len; i++) {
            if (str.charAt(i) == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }

                // This is important, it should not be i++
                start = i + 1;
                continue;
            }

            lastMatch = false;
            match = true;
        }

        if (match || lastMatch) {
            list.add(str.substring(start, len));
        }
        return list;
    }

    private static List<String> splitNewLine(String str) {
        int len = str.length();
        List<String> list = new ArrayList<>();
        int i = 0, start = 0;
        boolean match = false, lastMatch = false;

        while (i < len) {
            if (str.charAt(i) == '\n') {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }
                start = ++i;
                continue;
            }
            lastMatch = false;
            match = true;
            i++;
        }

        if (match || lastMatch) {
            list.add(str.substring(start, i));
        }

        return list;
    }

    /**
     * Get a new instance of {@link Serializer} used for serializing items
     * into a configuration.
     */
    public static Serializer serializer() {
        return new Serializer();
    }

    /**
     * Use {@link XItemStack#serializer()} to use this class.
     */
    public static final class Serializer extends SerialObject {
        private Function<List<? extends Component>, List<String>> miniMessageHandler;

        /**
         * Use {@link XItemStack#serializer()} instead.
         */
        private Serializer() {}

        public Serializer fromItem(ItemStack item) {
            this.item = Objects.requireNonNull(item, "Cannot serialize null item");
            return this;
        }

        public Serializer toConfig(ConfigurationSection config) {
            this.config = config;
            return this;
        }

        public Serializer withTranslator(Function<String, String> translator) {
            super.withTranslator(translator);
            return this;
        }

        @ApiStatus.Experimental
        public Serializer withMiniMessage(Function<List<? extends Component>, List<String>> miniMessageHandler) {
            this.miniMessageHandler = miniMessageHandler;
            return this;
        }

        @NotNull
        public Map<String, Object> writeToMap() {
            if (config == null) config = new MemoryConfiguration();
            serialize();
            return configSectionToMap(config);
        }

        @Override
        public Serializer copy() {
            return copySettings().fromItem(item == null ? null : item.clone());
        }

        @Override
        public Serializer copySettings() {
            return new Serializer()
                    .toConfig(config)
                    .withTranslator(translator)
                    .withMiniMessage(miniMessageHandler);
        }

        public void serialize() {
            Objects.requireNonNull(item, "Item not set");
            Objects.requireNonNull(config, "Config not set");

            config.set("material", XMaterial.matchXMaterial(item).name());
            if (item.getAmount() > 1) config.set("amount", item.getAmount());

            if (!item.hasItemMeta()) return;
            meta = item.getItemMeta();
            if (meta == null) return;

            // Durability - Damage
            handleDurability(meta);

            // Display Name & Lore
            if (meta.hasDisplayName()) {
                if (miniMessageHandler != null) {
                    config.set("name", AdventureAPIFactory.displayName(meta, miniMessageHandler));
                } else {
                    config.set("name", translator.apply(meta.getDisplayName()));
                }
            }
            if (meta.hasLore()) {
                if (miniMessageHandler != null) {
                    config.set("lore", AdventureAPIFactory.lore(meta, miniMessageHandler));
                } else {
                    config.set("lore", meta.getLore().stream().map(translator).collect(Collectors.toList()));
                }
            }
			
			if (SUPPORTS_ITEM_NAME && meta.hasItemName()) {
				String itemName = meta.getItemName();
				config.set("item-name", translator.apply(itemName));
			}

            customModelData();
            if (SUPPORTS_UNBREAKABLE) {
                if (meta.isUnbreakable()) config.set("unbreakable", true);
            }

            handleEnchants();
            handleItemFlags(meta);
            handleAttributes(meta);
            legacySpawnEgg();

            recursiveMetaHandle(this, meta.getClass(), meta, SERIALIZE_META_HANDLERS, null);
        }

        @SuppressWarnings({"UnstableApiUsage", "deprecation"})
        private void customModelData() {
            if (SUPPORTS_ITEM_MODEL) {
                if (meta.hasItemModel()) {
                    config.set("item-model", meta.getItemModel().toString());
                }
            }

            if (SUPPORTS_CUSTOM_MODEL_DATA) {
                if (SUPPORTS_ADVANCED_CUSTOM_MODEL_DATA) {
                    CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
                    List<String> strings = customModelData.getStrings();
                    List<Float> floats = customModelData.getFloats();
                    List<Boolean> flags = customModelData.getFlags();
                    List<Color> colors = customModelData.getColors();

                    int idCount = (int) Stream.of(strings, floats, flags, colors).filter(x -> !x.isEmpty()).count();
                    if (idCount == 0) return;
                    if (idCount == 1) {
                        if (!strings.isEmpty()) config.set("custom-model-data", singleOrList(strings));
                        if (!floats.isEmpty()) config.set("custom-model-data", singleOrList(floats));
                        if (!flags.isEmpty()) config.set("custom-model-data", singleOrList(flags));
                        if (!colors.isEmpty())
                            config.set("custom-model-data", singleOrList(colors.stream().map(Serializer::colorString).collect(Collectors.toList())));
                    } else {
                        ConfigurationSection cfgCustomModelData = config.createSection("custom-model-data");
                        if (!strings.isEmpty()) cfgCustomModelData.set("strings", strings);
                        if (!floats.isEmpty()) cfgCustomModelData.set("floats", floats);
                        if (!flags.isEmpty()) cfgCustomModelData.set("flags", flags);
                        if (!colors.isEmpty())
                            cfgCustomModelData.set("colors", colors.stream().map(Serializer::colorString).collect(Collectors.toList()));
                    }
                } else if (meta.hasCustomModelData()) {
                    config.set("custom-model-data", meta.getCustomModelData());
                }
            }
        }

        private static <T> Object singleOrList(List<T> list) {
            return list.size() == 1 ? list.get(0) : list;
        }

        @SuppressWarnings("deprecation")
        private void legacySpawnEgg() {
            if (!supports(11)) {
                MaterialData data = item.getData();
                if (data instanceof SpawnEgg) {
                    SpawnEgg spawnEgg = (SpawnEgg) data;
                    config.set("creature", spawnEgg.getSpawnedType().getName());
                }
            }
        }

        @SuppressWarnings("deprecation")
        private void handleSpawnEggMeta(SpawnEggMeta spawnEgg) {
            config.set("creature", spawnEgg.getSpawnedType().getName());
        }

        private void handleSuspiciousStewMeta(SuspiciousStewMeta stew) {
            List<PotionEffect> customEffects = stew.getCustomEffects();
            List<String> effects = new ArrayList<>(customEffects.size());

            for (PotionEffect effect : customEffects) {
                effects.add(XPotion.of(effect.getType()).name() + ", " + effect.getDuration() + ", " + effect.getAmplifier());
            }

            config.set("effects", effects);
        }

        private void handleTropicalFishBucketMeta(TropicalFishBucketMeta tropical) {
            config.set("pattern", tropical.getPattern().name());
            config.set("color", tropical.getBodyColor().name());
            config.set("pattern-color", tropical.getPatternColor().name());
        }

        private void handleCrossbowMeta(CrossbowMeta crossbow) {
            int i = 0;
            for (ItemStack projectiles : crossbow.getChargedProjectiles()) {
                this.copySettings()
                        .fromItem(projectiles)
                        .toConfig(config.getConfigurationSection("projectiles." + i))
                        .serialize();
                i++;
            }
        }

        private void handleCompassMeta(CompassMeta compass) {
            ConfigurationSection subSection = config.createSection("lodestone");
            subSection.set("tracked", compass.isLodestoneTracked());
            if (compass.hasLodestone()) {
                Location location = compass.getLodestone();
                subSection.set("location.world", location.getWorld().getName());
                subSection.set("location.x", location.getX());
                subSection.set("location.y", location.getY());
                subSection.set("location.z", location.getZ());
            }
        }

        private void handleAxolotlBucketMeta(AxolotlBucketMeta bucket) {
            if (bucket.hasVariant()) config.set("color", bucket.getVariant().toString());
        }

        @SuppressWarnings("deprecation")
        private void handleArmorMeta(ArmorMeta armorMeta) {
            if (armorMeta.hasTrim()) {
                ArmorTrim trim = armorMeta.getTrim();
                ConfigurationSection trimConfig = config.createSection("trim");
                trimConfig.set("material", trim.getMaterial().getKey().getNamespace() + ':' + trim.getMaterial().getKey().getKey());
                trimConfig.set("pattern", trim.getPattern().getKey().getNamespace() + ':' + trim.getPattern().getKey().getKey());
            }
        }

        @SuppressWarnings("deprecation")
        private void handleMapMeta(MapMeta map) {
            ConfigurationSection mapSection = config.createSection("map");

            mapSection.set("scaling", map.isScaling());
            if (supports(11)) {
                if (map.hasLocationName()) mapSection.set("location", map.getLocationName());
                if (map.hasColor()) {
                    Color color = map.getColor();
                    mapSection.set("color", colorString(color));
                }
            }

            if (supports(14)) {
                if (map.hasMapView()) {
                    MapView mapView = map.getMapView();
                    ConfigurationSection view = mapSection.createSection("view");
                    view.set("scale", mapView.getScale().toString());
                    view.set("world", mapView.getWorld().getName());
                    ConfigurationSection centerSection = view.createSection("center");
                    centerSection.set("x", mapView.getCenterX());
                    centerSection.set("z", mapView.getCenterZ());
                    view.set("locked", mapView.isLocked());
                    view.set("tracking-position", mapView.isTrackingPosition());
                    view.set("unlimited-tracking", mapView.isUnlimitedTracking());
                }
            }
        }

        private void handleBookMeta(BookMeta book) {
            if (book.getTitle() != null || book.getAuthor() != null || book.getGeneration() != null || !book.getPages().isEmpty()) {
                ConfigurationSection bookInfo = config.createSection("book");

                if (book.getTitle() != null) bookInfo.set("title", book.getTitle());
                if (book.getAuthor() != null) bookInfo.set("author", book.getAuthor());
                if (supports(9)) {
                    BookMeta.Generation generation = book.getGeneration();
                    if (generation != null) {
                        bookInfo.set("generation", book.getGeneration().toString());
                    }
                }

                if (!book.getPages().isEmpty()) bookInfo.set("pages", book.getPages());
            }
        }

        private void handleFireworkMeta(FireworkMeta firework) {
            config.set("power", firework.getPower());
            int i = 0;

            for (FireworkEffect fw : firework.getEffects()) {
                config.set("firework." + i + ".type", fw.getType().name());
                ConfigurationSection fwc = config.getConfigurationSection("firework." + i);
                fwc.set("flicker", fw.hasFlicker());
                fwc.set("trail", fw.hasTrail());

                List<Color> fwBaseColors = fw.getColors();
                List<Color> fwFadeColors = fw.getFadeColors();

                List<String> baseColors = new ArrayList<>(fwBaseColors.size());
                List<String> fadeColors = new ArrayList<>(fwFadeColors.size());

                ConfigurationSection colors = fwc.createSection("colors");
                for (Color color : fwBaseColors)
                    baseColors.add(colorString(color));
                colors.set("base", baseColors);

                for (Color color : fwFadeColors)
                    fadeColors.add(colorString(color));
                colors.set("fade", fadeColors);
                i++;
            }
        }

        private static @NotNull String colorString(Color color) {
            return color.getRed() + ", " + color.getGreen() + ", " + color.getBlue();
        }

        private static final boolean SUPPORTS_PotionMeta_getBasePotionType =
                XReflection.of(PotionMeta.class)
                        .method("public org.bukkit.potion.PotionType getBasePotionType()")
                        .exists();
        private static final boolean SUPPORTS_PotionEffectType_getKey =
                XReflection.of(PotionEffectType.class)
                        .method("public org.bukkit.NamespacedKey getKey()")
                        .exists();

        @SuppressWarnings({"deprecation", "StatementWithEmptyBody"})
        private void handlePotionMeta(PotionMeta meta) {
            if (supports(9)) {
                if (SUPPORTS_PotionMeta_getBasePotionType) {
                    PotionType basePotionType = meta.getBasePotionType();
					if (basePotionType != null)
                        config.set("base-type", basePotionType.name());
                } else {
                    @SuppressWarnings("removal")
                    PotionData potionData = meta.getBasePotionData();
                    // noinspection removal
                    config.set("base-effect", potionData.getType().name() + ", " + potionData.isExtended() + ", " + potionData.isUpgraded());
                }

                List<PotionEffect> customEffects = meta.getCustomEffects();
                if (!customEffects.isEmpty()) {
                    config.set("effects", customEffects.stream().map(x -> {
                        String typeStr;
                        if (SUPPORTS_PotionEffectType_getKey) {
                            NamespacedKey type = x.getType().getKey();
                            typeStr = type.getNamespace() + ':' + type.getKey();
                        } else {
                            typeStr = x.getType().getName();
                        }

						// we change this to match what the deserializer expects
						int seconds = x.getDuration() / 20;
						int level = x.getAmplifier() + 1;
                        return typeStr + ", " + seconds + ", " + level;
                    }).collect(Collectors.toList()));
                }

                if (SUPPORTS_POTION_COLOR && meta.hasColor()) config.set("color", meta.getColor().asRGB());
            } else {
                // Check for water bottles in 1.8
                // Potion class is now removed...
                // if (item.getDurability() != 0) {
                //     Potion potion = Potion.fromItemStack(item);
                //     config.set("level", potion.getLevel());
                //     config.set("base-effect", potion.getType().name() + ", " + potion.hasExtendedDuration() + ", " + potion.isSplash());
                // }
            }
        }

        private void handleLeatherArmorMeta(LeatherArmorMeta meta) {
            Color color = meta.getColor();
            config.set("color", colorString(color));
        }

        private void handleBannerMeta(BannerMeta meta) {
            ConfigurationSection patterns = config.createSection("patterns");
            for (Pattern pattern : meta.getPatterns()) {
                patterns.set(XPatternType.of(pattern.getPattern()).name(), pattern.getColor().name());
            }
        }

        private void handleSkullMeta(ItemMeta meta) {
            String skull = XSkull.of(meta).getProfileValue();
            if (skull != null) config.set("skull", skull);
        }

        private void handleEnchantmentStorageMeta(EnchantmentStorageMeta meta) {
            for (Map.Entry<Enchantment, Integer> enchant : meta.getStoredEnchants().entrySet()) {
                String entry = "stored-enchants." + XEnchantment.of(enchant.getKey()).name();
                config.set(entry, enchant.getValue());
            }
        }

        private void handleBlockStateMeta(BlockStateMeta meta) {
            BlockState state = safeBlockState(meta);

            if (supports(11) && state instanceof ShulkerBox) {
                ShulkerBox box = (ShulkerBox) state;
                ConfigurationSection shulker = config.createSection("contents");
                int i = 0;
                for (ItemStack itemInBox : box.getInventory().getContents()) {
                    if (itemInBox != null) {
                        this.copySettings()
                                .fromItem(itemInBox)
                                .toConfig(shulker.createSection(Integer.toString(i)))
                                .serialize();
                    }
                    i++;
                }
            } else if (state instanceof CreatureSpawner) {
                CreatureSpawner cs = (CreatureSpawner) state;
                if (cs.getSpawnedType() != null) config.set("spawner", cs.getSpawnedType().name());
            }
        }

        @SuppressWarnings("deprecation")
        private void handleAttributes(ItemMeta meta) {
            if (supports(13)) {
                Multimap<Attribute, AttributeModifier> attributes = meta.getAttributeModifiers();
                if (attributes != null) {
                    for (Map.Entry<Attribute, AttributeModifier> attribute : attributes.entries()) {
                        String path = "attributes." + XAttribute.of(attribute.getKey()).name() + '.';
                        AttributeModifier modifier = attribute.getValue();

                        // config.set(path + "id", modifier.getUniqueId().toString());
                        config.set(path + "name", modifier.getName());
                        config.set(path + "amount", modifier.getAmount());
                        config.set(path + "operation", modifier.getOperation().name());
                        if (modifier.getSlot() != null) config.set(path + "slot", modifier.getSlot().name());
                    }
                }
            }
        }

        private void handleItemFlags(ItemMeta meta) {
            if (!meta.getItemFlags().isEmpty()) {
                Set<ItemFlag> flags = meta.getItemFlags();
                List<String> flagNames = new ArrayList<>(flags.size());
                for (ItemFlag flag : flags) flagNames.add(flag.name());
                config.set("flags", flagNames);
            }
        }

        private void handleEnchants() {
            for (Map.Entry<Enchantment, Integer> enchant : meta.getEnchants().entrySet()) {
                String entry = "enchants." + XEnchantment.of(enchant.getKey()).name();
                config.set(entry, enchant.getValue());
            }
        }

        @SuppressWarnings("deprecation")
        private void handleDurability(ItemMeta meta) {
            if (supports(13)) {
                if (meta instanceof Damageable) {
                    Damageable damageable = (Damageable) meta;
                    if (damageable.hasDamage()) config.set("damage", damageable.getDamage());
                }
            } else {
                config.set("damage", item.getDurability());
            }
        }
    }

    /**
     * Get a new instance of {@link Deserializer} used for deserializing items
     * from a configuration.
     */
    public static Deserializer deserializer() {
        return new Deserializer();
    }

    /**
     * The deserializer only requires a config to be set. All other properties are optional.
     * To use this class, you have to use {@link XItemStack#deserializer()}.
     * This class is not thread-safe.
     */
    public static final class Deserializer extends SerialObject {
        private Consumer<Exception> restart;
        private Function<List<String>, List<? extends Component>> miniMessageHandler;

        /**
         * Use {@link XItemStack#deserializer()} instead.
         */
        private Deserializer() {}

        /**
         * If an item is set, it'll modify this item instead of making a new one from the config.
         */
        public Deserializer modifyItem(ItemStack item) {
            this.item = item;
            return this;
        }

        /**
         * The config which this item is going to be deserialized from.
         * @see #fromConfig(Map)
         */
        public Deserializer fromConfig(ConfigurationSection config) {
            this.config = config;
            return this;
        }

        /**
         * If you use any other configuration system, you can provide a map instead,
         * which automatically gets converted into a {@link ConfigurationSection}.
         * @see #fromConfig(ConfigurationSection)
         */
        @NotNull
        public Deserializer fromConfig(Map<String, Object> serializedItem) {
            Objects.requireNonNull(serializedItem, "serializedItem cannot be null.");
            return fromConfig(mapToConfigSection(serializedItem));
        }

        /**
         * Used for {@link ItemMeta#getDisplayName()} and {@link ItemMeta#getLore()} if your plugin handles
         * them in a specific way. This utility class does not colorize any strings by default.
         * You can also set a {@link #withMiniMessage(Function)} for Paper servers.
         */
        public Deserializer withTranslator(Function<String, String> translator) {
            super.withTranslator(translator);
            return this;
        }

        /**
         * Restarts are a way to handle {@link Exception} without terminating the process
         * in general by providing a solution on the fly. If you don't set a restart,
         * the deserialization will fail entirely with the exception.
         * <p>
         * Currently the only properly handled exceptions that support restarts are {@link MaterialCondition}
         * any other exception will not trigger a restart.
         */
        public Deserializer withRestart(Consumer<Exception> restart) {
            this.restart = restart;
            return this;
        }

        /**
         * If this server implementation supports adventure API, {@link ItemMeta#getDisplayName()} and
         * {@link ItemMeta#getLore()} will be set using their respective Paper adventure API.
         * If the adventure API implementation is not available, {@link #withTranslator(Function)} is used instead.
         */
        @ApiStatus.Experimental
        public Deserializer withMiniMessage(Function<List<String>, List<? extends Component>> miniMessageHandler) {
            this.miniMessageHandler = miniMessageHandler;
            return this;
        }

        @Override
        public Deserializer copy() {
            return copySettings().modifyItem(item == null ? null : item.clone());
        }

        @Override
        public Deserializer copySettings() {
            return new Deserializer()
                    .fromConfig(config)
                    .withTranslator(translator)
                    .withRestart(restart)
                    .withMiniMessage(miniMessageHandler);
        }

        public ItemStack deserialize() {
            Objects.requireNonNull(config, "Config not set");

            handleMaterial();
            handleDamage();
            getOrCreateMeta();
            handleDurability();
            displayName();
			itemName();
            unbreakable();
            customModelData();
            lore();
            enchants();
            itemFlags();
            attributes();
            legacySpawnEgg();

            recursiveMetaHandle(this, meta.getClass(), meta, DESERIALIZE_META_HANDLERS, null);

            item.setItemMeta(meta);
            return item;
        }

        private void attributes() {
            // Atrributes - https://minecraft.wiki/w/Attribute
            if (!supports(13)) return;

            ConfigurationSection attributes = config.getConfigurationSection("attributes");
            if (attributes != null) {
                for (String attribute : attributes.getKeys(false)) {
                    Optional<XAttribute> attributeInst = XAttribute.of(attribute);
                    if (!attributeInst.isPresent() || !attributeInst.get().isSupported()) continue;

                    ConfigurationSection section = attributes.getConfigurationSection(attribute);
                    if (section == null) continue;

                    // String attribId = section.getString("id");
                    // UUID id = attribId != null ? UUID.fromString(attribId) : UUID.randomUUID();
                    EquipmentSlot slot = section.getString("slot") != null ? Enums.getIfPresent(EquipmentSlot.class, section.getString("slot")).or(EquipmentSlot.HAND) : null;

                    String attrName = section.getString("name");
                    if (attrName == null) {
                        attrName = UUID.randomUUID().toString().toLowerCase(Locale.ENGLISH);
                    }

                    AttributeModifier modifier = XAttribute.createModifier(
                            attrName,
                            section.getDouble("amount"),
                            Enums.getIfPresent(AttributeModifier.Operation.class, section.getString("operation"))
                                    .or(AttributeModifier.Operation.ADD_NUMBER),
                            slot
                    );
                    meta.addAttributeModifier(attributeInst.get().get(), modifier);
                }
            }

            if (!meta.getItemFlags().isEmpty() && XReflection.supports(1, 20, 6)) {
                // Item flags will not work without an attribute modifier being present.
                if (!meta.hasAttributeModifiers()) {
                    meta.addAttributeModifier(
                            XAttribute.ATTACK_DAMAGE.get(),
                            XAttribute.createModifier(
                                    "xseries:itemflagdummy",
                                    0.0,
                                    AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                                    null
                            )
                    );
                }
            }
        }

        @SuppressWarnings("deprecation")
        private void legacySpawnEgg() {
            if (!supports(11)) {
                MaterialData data = item.getData();
                if (data instanceof SpawnEgg) {
                    String creatureName = config.getString("creature");
                    if (!Strings.isNullOrEmpty(creatureName)) {
                        SpawnEgg spawnEgg = (SpawnEgg) data;
                        com.google.common.base.Optional<EntityType> creature = Enums.getIfPresent(EntityType.class, creatureName.toUpperCase(Locale.ENGLISH));
                        if (creature.isPresent()) spawnEgg.setSpawnedType(creature.get());
                        item.setData(data);
                    }
                }
            }
        }

        private void unbreakable() {
            if (SUPPORTS_UNBREAKABLE && config.isSet("unbreakable"))
                meta.setUnbreakable(config.getBoolean("unbreakable"));
        }

        @SuppressWarnings({"UnstableApiUsage", "deprecation"})
        private void customModelData() {
            if (SUPPORTS_ITEM_MODEL) {
                String itemModel = config.getString("item-model");
                if (itemModel != null && !itemModel.isEmpty()) {
                    meta.setItemModel(NamespacedKey.fromString(itemModel));
                }
            }

            if (SUPPORTS_ADVANCED_CUSTOM_MODEL_DATA) {
                CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();

                ConfigurationSection detailed = config.getConfigurationSection("custom-model-data");
                if (detailed != null) {
                    customModelData.setStrings(parseRawOrList("string", "strings", detailed, x -> x));
                    customModelData.setFlags(parseRawOrList("flag", "flags", detailed, Boolean::parseBoolean));
                    customModelData.setFloats(parseRawOrList("float", "floats", detailed, Float::parseFloat));
                    customModelData.setColors(parseRawOrList("color", "colors", detailed, x ->
                            XItemStack.parseColor(x).orElseThrow(() -> new IllegalArgumentException("Unknown color for custom model data: " + x))));
                } else {
                    List<String> listed = config.getStringList("custom-model-data");
                    if (!listed.isEmpty()) {
                        String modelData = listed.get(0);
                        if (modelData != null && !modelData.isEmpty()) {
                            if (tryNumber(modelData, Float::parseFloat) != null) {
                                customModelData.setFloats(listed.stream().map(Float::parseFloat).collect(Collectors.toList()));
                            } else {
                                Optional<Color> color = parseColor(modelData);
                                if (color.isPresent()) {
                                    customModelData.setColors(listed.stream()
                                            .map(XItemStack::parseColor)
                                            .map(x -> x.orElseThrow(() -> new IllegalArgumentException("Unknown color for custom model data: " + x)))
                                            .collect(Collectors.toList())
                                    );
                                } else {
                                    customModelData.setStrings(listed);
                                }
                            }
                        }
                    } else {
                        String modelData = config.getString("custom-model-data");
                        if (modelData != null && !modelData.isEmpty()) {
                            // Not different from setCustomModelData(int)
                            Float floatId = tryNumber(modelData, Float::parseFloat);
                            if (floatId != null) {
                                customModelData.setFloats(Collections.singletonList(floatId));
                            } else {
                                Optional<Color> color = parseColor(modelData);
                                if (color.isPresent()) {
                                    customModelData.setColors(Collections.singletonList(color.get()));
                                } else {
                                    customModelData.setStrings(Collections.singletonList(modelData));
                                }
                            }
                        }
                    }
                }

                // Setting an empty component will save it and change the internal meta which affects isSimilar()
                if (!customModelData.getColors().isEmpty() || !customModelData.getStrings().isEmpty() ||
                        !customModelData.getFlags().isEmpty() || !customModelData.getFloats().isEmpty()) {
                    meta.setCustomModelDataComponent(customModelData);
                }
            } else if (SUPPORTS_CUSTOM_MODEL_DATA) {
                String modelData = config.getString("custom-model-data");
                if (modelData != null && !modelData.isEmpty()) {
                    Integer intId = tryNumber(modelData, Integer::parseInt);
                    if (intId != null) meta.setCustomModelData(intId);
                }
            }
        }

        @SuppressWarnings("ConstantValue")
        private void displayName() {
            String name = config.getString("name");

            if (!Strings.isNullOrEmpty(name)) {
                if (miniMessageHandler != null) {
                    AdventureAPIFactory.setDisplayName(meta, miniMessageHandler.apply(Collections.singletonList(name)).get(0));
                    return;
                }

                String translated = translator.apply(name);
                meta.setDisplayName(translated);
            } else if (name != null && name.isEmpty()) {
                meta.setDisplayName(" "); // For GUI easy access configuration purposes
            }
        }

		@SuppressWarnings("ConstantValue")
        private void itemName() {
			if (!SUPPORTS_ITEM_NAME)
				return;

			String itemName = config.getString("item-name");
			if (!Strings.isNullOrEmpty(itemName)) {
				String translated = translator.apply(itemName);
				meta.setItemName(translated);
			} else if (itemName != null && itemName.isEmpty()) {
				meta.setItemName(" ");
			}
		}
		
        private void itemFlags() {
            List<String> flags = config.getStringList("flags");
            if (!flags.isEmpty()) {
                for (String flag : flags) {
                    flag = flag.toUpperCase(Locale.ENGLISH);
                    if (flag.equals("ALL")) {
                        XItemFlag.decorationOnly(meta);
                        break;
                    }

                    XItemFlag.of(flag).ifPresent(itemFlag -> itemFlag.set(meta));
                }
            } else {
                String allFlags = config.getString("flags");
                if (!Strings.isNullOrEmpty(allFlags) && allFlags.equalsIgnoreCase("ALL"))
                    XItemFlag.decorationOnly(meta);
            }
        }

        private void handleEnchantmentStorageMeta(EnchantmentStorageMeta meta) {
            ConfigurationSection enchantment = config.getConfigurationSection("stored-enchants");
            if (enchantment != null) {
                for (String ench : enchantment.getKeys(false)) {
                    Optional<XEnchantment> enchant = XEnchantment.of(ench);
                    enchant.ifPresent(xEnchantment -> meta.addStoredEnchant(xEnchantment.get(), enchantment.getInt(ench), true));
                }
            }
        }

        private void enchants() {
            ConfigurationSection enchants = config.getConfigurationSection("enchants");
            if (enchants != null) {
                for (String ench : enchants.getKeys(false)) {
                    Optional<XEnchantment> enchant = XEnchantment.of(ench);
                    enchant.ifPresent(xEnchantment -> meta.addEnchant(xEnchantment.get(), enchants.getInt(ench), true));
                }
            } else if (config.getBoolean("glow")) {
                meta.addEnchant(XEnchantment.UNBREAKING.get(), 1, false);
                XItemFlag.HIDE_ENCHANTS.set(meta);
            }
        }

        /**
         * In some versions, an empty string for a lore line is completely
         * ignored, so at least a space " " is needed to get empty lore lines.
         * <p>
         * This seems to be inconsistent between versions, so it's always enabled.
         */
        private static final boolean SPACE_EMPTY_LORE_LINES = true;

        private void lore() {
            if (!config.isSet("lore")) return;

            List<String> translatedLore;
            List<String> lores = config.getStringList("lore");
            if (!lores.isEmpty()) {
                if (miniMessageHandler != null) {
                    AdventureAPIFactory.setLore(meta, miniMessageHandler.apply(lores));
                    return;
                }

                translatedLore = new ArrayList<>(lores.size());

                for (String lore : lores) {
                    if (SPACE_EMPTY_LORE_LINES && lore.isEmpty()) {
                        translatedLore.add(" ");
                        continue;
                    }

                    for (String singleLore : splitNewLine(lore)) {
                        if (SPACE_EMPTY_LORE_LINES && singleLore.isEmpty()) {
                            translatedLore.add(" ");
                            continue;
                        }
                        translatedLore.add(translator.apply(singleLore));
                    }
                }
            } else {
                String lore = config.getString("lore");
                translatedLore = new ArrayList<>(10);

                if (!Strings.isNullOrEmpty(lore)) {
                    if (miniMessageHandler != null) {
                        AdventureAPIFactory.setLore(meta, miniMessageHandler.apply(Collections.singletonList(lore)));
                        return;
                    }

                    for (String singleLore : splitNewLine(lore)) {
                        if (SPACE_EMPTY_LORE_LINES && singleLore.isEmpty()) {
                            translatedLore.add(" ");
                            continue;
                        }
                        translatedLore.add(translator.apply(singleLore));
                    }
                }
            }

            meta.setLore(translatedLore);
        }

        @SuppressWarnings("deprecation")
        private void handleSpawnEggMeta(SpawnEggMeta spawnEgg) {
            String creatureName = config.getString("creature");
            if (!Strings.isNullOrEmpty(creatureName)) {
                com.google.common.base.Optional<EntityType> creature = Enums.getIfPresent(EntityType.class, creatureName.toUpperCase(Locale.ENGLISH));
                if (creature.isPresent()) spawnEgg.setSpawnedType(creature.get());
            }
        }

        private void handleTropicalFishBucketMeta(TropicalFishBucketMeta tropical) {
            DyeColor color = Enums.getIfPresent(DyeColor.class, config.getString("color")).or(DyeColor.WHITE);
            DyeColor patternColor = Enums.getIfPresent(DyeColor.class, config.getString("pattern-color")).or(DyeColor.WHITE);
            TropicalFish.Pattern pattern = Enums.getIfPresent(TropicalFish.Pattern.class, config.getString("pattern")).or(TropicalFish.Pattern.BETTY);

            tropical.setBodyColor(color);
            tropical.setPatternColor(patternColor);
            tropical.setPattern(pattern);
        }

        private void handleCrossbowMeta(CrossbowMeta crossbow) {
            ConfigurationSection projectiles = config.getConfigurationSection("projectiles");
            if (projectiles != null) {
                for (String projectile : projectiles.getKeys(false)) {
                    ItemStack projectileItem = this.copySettings()
                            .fromConfig(config.getConfigurationSection("projectiles." + projectile))
                            .deserialize();
                    crossbow.addChargedProjectile(projectileItem);
                }
            }
        }

        private void handleSuspiciousStewMeta(SuspiciousStewMeta stew) {
            for (String effects : config.getStringList("effects")) {
                XPotion.Effect effect = XPotion.parseEffect(effects);
                if (effect.hasChance()) stew.addCustomEffect(effect.getEffect(), true);
            }
        }

        private void handleCompassMeta(CompassMeta compass) {
            compass.setLodestoneTracked(config.getBoolean("tracked"));

            ConfigurationSection lodestone = config.getConfigurationSection("lodestone");
            if (lodestone != null) {
                World world = Bukkit.getWorld(lodestone.getString("world"));
                double x = lodestone.getDouble("x");
                double y = lodestone.getDouble("y");
                double z = lodestone.getDouble("z");
                compass.setLodestone(new Location(world, x, y, z));
            }
        }

        private void handleAxolotlBucketMeta(AxolotlBucketMeta bucket) {
            String variantStr = config.getString("color");
            if (variantStr != null) {
                Axolotl.Variant variant = Enums.getIfPresent(Axolotl.Variant.class, variantStr.toUpperCase(Locale.ENGLISH)).or(Axolotl.Variant.BLUE);
                bucket.setVariant(variant);
            }
        }

        @SuppressWarnings("UnstableApiUsage")
        private void handleArmorMeta(ArmorMeta armor) {
            ConfigurationSection trim = config.getConfigurationSection("trim");
            if (trim != null) {
                TrimMaterial trimMaterial = Registry.TRIM_MATERIAL.get(NamespacedKey.fromString(trim.getString("material")));
                TrimPattern trimPattern = Registry.TRIM_PATTERN.get(NamespacedKey.fromString(trim.getString("pattern")));
                armor.setTrim(new ArmorTrim(trimMaterial, trimPattern));
            }
        }

        @SuppressWarnings("deprecation")
        private void handleMapMeta(MapMeta map) {
            ConfigurationSection mapSection = config.getConfigurationSection("map");
            if (mapSection == null) return;

            map.setScaling(mapSection.getBoolean("scaling"));
            if (supports(11)) {
                if (mapSection.isSet("location")) map.setLocationName(mapSection.getString("location"));
                if (mapSection.isSet("color")) {
                    parseColor(mapSection.getString("color")).ifPresent(map::setColor);
                }
            }

            if (supports(14)) {
                ConfigurationSection view = mapSection.getConfigurationSection("view");
                if (view != null) {
                    World world = Bukkit.getWorld(view.getString("world"));
                    if (world != null) {
                        MapView mapView = Bukkit.createMap(world);
                        mapView.setWorld(world);
                        mapView.setScale(Enums.getIfPresent(MapView.Scale.class, view.getString("scale")).or(MapView.Scale.NORMAL));
                        mapView.setLocked(view.getBoolean("locked"));
                        mapView.setTrackingPosition(view.getBoolean("tracking-position"));
                        mapView.setUnlimitedTracking(view.getBoolean("unlimited-tracking"));

                        ConfigurationSection centerSection = view.getConfigurationSection("center");
                        if (centerSection != null) {
                            mapView.setCenterX(centerSection.getInt("x"));
                            mapView.setCenterZ(centerSection.getInt("z"));
                        }

                        map.setMapView(mapView);
                    }
                }
            }
        }

        private void handleBookMeta(BookMeta book) {
            ConfigurationSection bookInfo = config.getConfigurationSection("book");
            if (bookInfo == null) return;

            book.setTitle(bookInfo.getString("title"));
            book.setAuthor(bookInfo.getString("author"));
            book.setPages(bookInfo.getStringList("pages"));

            if (supports(9)) {
                String generationValue = bookInfo.getString("generation");
                if (generationValue != null) {
                    BookMeta.Generation generation = Enums.getIfPresent(BookMeta.Generation.class, generationValue).orNull();
                    book.setGeneration(generation);
                }
            }
        }

        private void handleFireworkMeta(FireworkMeta firework) {
            firework.setPower(config.getInt("power"));

            ConfigurationSection fireworkSection = config.getConfigurationSection("firework");
            if (fireworkSection == null) return;

            FireworkEffect.Builder builder = FireworkEffect.builder();
            for (String fws : fireworkSection.getKeys(false)) {
                ConfigurationSection fw = config.getConfigurationSection("firework." + fws);

                builder.flicker(fw.getBoolean("flicker"));
                builder.trail(fw.getBoolean("trail"));
                builder.with(Enums.getIfPresent(FireworkEffect.Type.class, fw.getString("type")
                                .toUpperCase(Locale.ENGLISH))
                        .or(FireworkEffect.Type.STAR));

                ConfigurationSection colorsSection = fw.getConfigurationSection("colors");
                if (colorsSection != null) {
                    List<String> fwColors = colorsSection.getStringList("base");
                    List<Color> colors = new ArrayList<>(fwColors.size());
                    for (String colorStr : fwColors) {
                        Optional<Color> color = parseColor(colorStr);
                        if (color.isPresent()) colors.add(color.get());
                    }
                    builder.withColor(colors);

                    fwColors = colorsSection.getStringList("fade");
                    colors = new ArrayList<>(fwColors.size());
                    for (String colorStr : fwColors) {
                        Optional<Color> color = parseColor(colorStr);
                        if (color.isPresent()) colors.add(color.get());
                    }
                    builder.withFade(colors);
                }

                firework.addEffect(builder.build());
            }
        }

        private void handleBlockStateMeta(BlockStateMeta bsm) {
            BlockState state = safeBlockState(bsm);

            if (state instanceof CreatureSpawner) {
                // Do we still need this? XMaterial handles it, doesn't it?
                CreatureSpawner spawner = (CreatureSpawner) state;
                String spawnerStr = config.getString("spawner");
                if (!Strings.isNullOrEmpty(spawnerStr)) {
                    spawner.setSpawnedType(Enums.getIfPresent(EntityType.class, spawnerStr.toUpperCase(Locale.ENGLISH)).orNull());
                    spawner.update(true);
                    bsm.setBlockState(spawner);
                }
            } else if (supports(11) && state instanceof ShulkerBox) {
                ConfigurationSection shulkerSection = config.getConfigurationSection("contents");
                if (shulkerSection != null) {
                    ShulkerBox box = (ShulkerBox) state;
                    for (String key : shulkerSection.getKeys(false)) {
                        ItemStack boxItem = this.copySettings()
                                .fromConfig(shulkerSection.getConfigurationSection(key))
                                .deserialize();
                        int slot = toInt(key, 0);
                        box.getInventory().setItem(slot, boxItem);
                    }
                    box.update(true);
                    bsm.setBlockState(box);
                }
            } else if (state instanceof Banner) {
                Banner banner = (Banner) state;
                ConfigurationSection patterns = config.getConfigurationSection("patterns");
                if (!supports(14)) {
                    // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/diff/src/main/java/org/bukkit/craftbukkit/block/CraftBanner.java?until=b3dc236663a55450c69356e660c0c84f0abbb3aa
                    banner.setBaseColor(DyeColor.WHITE);
                }

                if (patterns != null) {
                    for (String pattern : patterns.getKeys(false)) {
                        Optional<XPatternType> patternType = XPatternType.of(pattern);
                        if (patternType.isPresent() && patternType.get().isSupported()) {
                            DyeColor color = Enums.getIfPresent(DyeColor.class, patterns.getString(pattern).toUpperCase(Locale.ENGLISH)).or(DyeColor.WHITE);
                            banner.addPattern(new Pattern(color, patternType.get().get()));
                        }
                    }

                    banner.update(true);
                    bsm.setBlockState(banner);
                }
            }
        }

        private static final boolean SUPPORTS_PotionMeta_setBasePotionType =
                XReflection.of(PotionMeta.class)
                        .method("public void setBasePotionType(org.bukkit.potion.PotionType potionType)")
                        .exists();
        private static final boolean SUPPORTS_PotionEffectType_getKey =
                XReflection.of(PotionEffectType.class)
                        .method("public org.bukkit.NamespacedKey getKey()")
                        .exists();

        @SuppressWarnings("StatementWithEmptyBody")
        private void handlePotionMeta(ItemMeta meta) {
            if (supports(9)) {
                PotionMeta potion = (PotionMeta) meta;

                for (String effects : config.getStringList("effects")) {
                    XPotion.Effect effect = XPotion.parseEffect(effects);
                    if (effect.hasChance()) potion.addCustomEffect(effect.getEffect(), true);
                }

                String baseType = config.getString("base-type");
                if (!Strings.isNullOrEmpty(baseType)) {
                    boolean isNewType = !baseType.contains(",");

                    if (SUPPORTS_PotionMeta_setBasePotionType) {
                        if (isNewType) {
                            PotionType potionType;
                            try {
                                potionType = PotionType.valueOf(baseType);
                            } catch (IllegalArgumentException ex) {
                                potionType = PotionType.AWKWARD;
                            }

                            potion.setBasePotionType(potionType);
                        } else {
                            // Format: Type, Extended, Upgraded
                            String[] components = baseType.split(",");

                            XPotion effect = XPotion.of(components[0]).orElse(XPotion.HEALTH_BOOST);
                            boolean extended = Boolean.parseBoolean(components[1]);
                            boolean upgraded = Boolean.parseBoolean(components[2]);

                            String potionTypeStr = effect.getPotionType().name();
                            if (extended) potionTypeStr = "STRONG_" + potionTypeStr;
                            else if (upgraded) potionTypeStr = "LONG_" + potionTypeStr;

                            PotionType potionType;
                            try {
                                potionType = PotionType.valueOf(potionTypeStr);
                            } catch (IllegalArgumentException ex) {
                                potionType = PotionType.AWKWARD;
                            }

                            potion.setBasePotionType(potionType);
                        }
                    } else {
                        boolean extended = false;
                        boolean upgraded = false;
                        PotionType effect;

                        if (isNewType) {
                            if (baseType.startsWith("STRONG_")) {
                                extended = true;
                                baseType = baseType.substring(7);
                            } else if (baseType.startsWith("LONG_")) {
                                upgraded = true;
                                baseType = baseType.substring(5);
                            }

                            effect = XPotion.of(baseType).orElse(XPotion.HEALTH_BOOST).getPotionType();
                        } else {
                            // Format: Type, Extended, Upgraded
                            String[] components = baseType.split(",");

                            effect = XPotion.of(components[0]).orElse(XPotion.HEALTH_BOOST).getPotionType();
                            extended = Boolean.parseBoolean(components[1]);
                            upgraded = Boolean.parseBoolean(components[2]);
                        }

                        // noinspection removal,deprecation
                        potion.setBasePotionData(new PotionData(effect, extended, upgraded));
                    }
                }

                if (SUPPORTS_POTION_COLOR && config.contains("color")) {
                    potion.setColor(Color.fromRGB(config.getInt("color")));
                }
            } else {
                // What do we do for 1.8?
                // if (config.contains("level")) {
                //     int level = config.getInt("level");
                //     String baseEffect = config.getString("base-effect");
                //     if (!Strings.isNullOrEmpty(baseEffect)) {
                //         List<String> split = split(baseEffect, ',');
                //         PotionType type = Enums.getIfPresent(PotionType.class, split.get(0).trim().toUpperCase(Locale.ENGLISH)).or(PotionType.SLOWNESS);
                //         boolean extended = split.size() != 1 && Boolean.parseBoolean(split.get(1).trim());
                //         boolean splash = split.size() > 2 && Boolean.parseBoolean(split.get(2).trim());
                //
                //         item = (splash ? XMaterial.SPLASH_POTION : XMaterial.POTION).parseItem();
                //         PotionMeta potion = (PotionMeta) item.getItemMeta();
                //         // potion.addCustomEffect(XPotion.matchXPotion(type).buildPotionEffect(extended ? 3 : 1, level), true);
                //         item.setItemMeta(potion);
                //         item = (new Potion(type, level, splash, extended)).toItemStack(1);
                //     }
                // }
            }
        }

        private void handleLeatherArmorMeta(LeatherArmorMeta leather) {
            String colorStr = config.getString("color");
            if (colorStr != null) {
                parseColor(colorStr).ifPresent(leather::setColor);
            }
        }

        private void handleBannerMeta(BannerMeta banner) {
            ConfigurationSection patterns = config.getConfigurationSection("patterns");

            if (patterns != null) {
                for (String pattern : patterns.getKeys(false)) {
                    Optional<XPatternType> patternType = XPatternType.of(pattern);
                    if (patternType.isPresent() && patternType.get().isSupported()) {
                        DyeColor color = Enums.getIfPresent(DyeColor.class, patterns.getString(pattern).toUpperCase(Locale.ENGLISH)).or(DyeColor.WHITE);
                        banner.addPattern(new Pattern(color, patternType.get().get()));
                    }
                }
            }
        }

        @SuppressWarnings("WriteOnlyObject") // Is IntelliJ confused?
        private void handleSkullMeta(SkullMeta meta) {
            // Make it lenient to support placeholders.
            String skull = config.getString("skull");
            if (skull != null) {
                // Since this is also an editing method, allow empty strings to
                // represent the instruction to completely remove an existing profile.
                if (skull.isEmpty()) XSkull.of(meta).profile(Profileable.detect(skull)).removeProfile();
                else XSkull.of(meta).profile(Profileable.detect(skull)).lenient().apply();
            }
        }

        @SuppressWarnings("deprecation")
        private void handleDurability() {
            if (supports(13)) {
                if (meta instanceof Damageable) {
                    int damage = config.getInt("damage");
                    if (damage > 0) ((Damageable) meta).setDamage(damage);
                }
            } else {
                int damage = config.getInt("damage");
                if (damage > 0) item.setDurability((short) damage);
            }
        }

        private void handleDamage() {
            int amount = config.getInt("amount");
            if (amount > 1) item.setAmount(amount);
        }

        private void getOrCreateMeta() {
            meta = item.getItemMeta();
            if (meta == null) {
                // When AIR is null. Useful for when you just want to use the meta to save data and
                // set the type later. A simple CraftMetaItem.
                meta = Bukkit.getItemFactory().getItemMeta(XMaterial.STONE.get());
            }
        }

        @SuppressWarnings("OptionalIsPresent")
        private void handleMaterial() {
            String materialName = config.getString("material");
            XMaterial material = null;

            if (!Strings.isNullOrEmpty(materialName)) {
                Optional<XMaterial> materialOpt = XMaterial.matchXMaterial(materialName);
                if (materialOpt.isPresent()) material = materialOpt.get();
                else {
                    material = solutionOrThrow(new UnknownMaterialCondition(materialName));
                }

                if (!material.isSupported()) {
                    material = solutionOrThrow(new UnAcceptableMaterialCondition(material,
                            UnAcceptableMaterialCondition.Reason.UNSUPPORTED));
                }
                if (XTag.INVENTORY_NOT_DISPLAYABLE.isTagged(material)) {
                    material = solutionOrThrow(new UnAcceptableMaterialCondition(material,
                            UnAcceptableMaterialCondition.Reason.NOT_DISPLAYABLE));
                }
            } else {
                // Shortcut for more compact configs.
                String skull = config.getString("skull");
                if (skull != null) material = XMaterial.PLAYER_HEAD;
            }

            if (material == null) {
                material = solutionOrThrow(new UnsetMaterialCondition());
            }

            if (item == null) item = material.parseItem();
            else material.setType(item);
        }

        private XMaterial solutionOrThrow(MaterialCondition condition) {
            if (restart == null) throw condition;
            restart.accept(condition);

            if (condition.hasSolution()) return condition.solution;
            else throw condition;
        }
    }

    /**
     * Deserialize an ItemStack from the config.
     *
     * @param config     the config section to deserialize the ItemStack object from.
     * @param translator the function applied to item name and each lore line.
     * @param restart    the function called when an error occurs while deserializing one of the properties.
     * @return an edited ItemStack.
     * @since 1.0.0
     * @deprecated Use {@link Deserializer} instead.
     */
    @NotNull
    @Deprecated
    public static ItemStack edit(@NotNull ItemStack item,
                                 @NotNull final ConfigurationSection config,
                                 @NotNull final Function<String, String> translator,
                                 @Nullable final Consumer<Exception> restart) {
        return new Deserializer()
                .modifyItem(item)
                .fromConfig(config)
                .withTranslator(translator)
                .withRestart(restart)
                .deserialize();
    }

    /**
     * Converts a {@code Map<?, ?>} into a {@code ConfigurationSection}.
     *
     * @param map the map to convert.
     * @return a {@code ConfigurationSection} containing the map values.
     */
    @NotNull
    private static ConfigurationSection mapToConfigSection(@NotNull Map<?, ?> map) {
        ConfigurationSection config = new MemoryConfiguration();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            if (value == null) continue;
            if (value instanceof Map<?, ?>) {
                value = mapToConfigSection((Map<?, ?>) value);
            }

            config.set(key, value);
        }

        return config;
    }

    /**
     * Converts a {@code ConfigurationSection} into a {@code Map<String, Object>}.
     *
     * @param config the configuration section to convert.
     * @return a {@code Map<String, Object>} containing the configuration section values.
     */
    @NotNull
    private static Map<String, Object> configSectionToMap(@NotNull ConfigurationSection config) {
        Map<String, Object> map = new LinkedHashMap<>();

        for (String key : config.getKeys(false)) {
            Object value = config.get(key);

            if (value == null) continue;
            if (value instanceof ConfigurationSection) {
                value = configSectionToMap((ConfigurationSection) value);
            }

            map.put(key, value);
        }

        return map;
    }

    /**
     * Parses RGB color codes from a string.
     * This only works for 1.13 and above.
     * Accepts the following formats:
     * "r, g, b"
     * "#RRGGBB"
     * decimal number representing {@code "r << 16 | g << 8 | b"}
     * (format "0xRRGGBB" is converted to decimal by SnakeYAML and handled as such)
     *
     * @param str the RGB string.
     * @return a color based on the RGB.
     * @since 1.1.0
     */
    @NotNull
    @ApiStatus.Internal
    public static Optional<Color> parseColor(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) return Optional.empty();
        List<String> rgb = split(str.replace(" ", ""), ',');
        if (rgb.size() == 3) {
            return Optional.of(Color.fromRGB(toInt(rgb.get(0), 0), toInt(rgb.get(1), 0), toInt(rgb.get(2), 0)));
        }
        // If we read a number that starts with 0x, SnakeYAML has already converted it to base-10
        try {
            return Optional.of(Color.fromRGB(Integer.parseInt(str)));
        } catch (NumberFormatException ignored) {
        }
        // Trim any prefix, parseInt only accepts digits
        if (str.startsWith("#")) {
            str = str.substring(1);
        }
        try {
            return Optional.of(Color.fromRGB(Integer.parseInt(str, 16)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static <T> List<T> parseRawOrList(String singular, String plural, ConfigurationSection section, Function<String, T> convert) {
        List<String> list = section.getStringList(plural);
        if (!list.isEmpty()) {
            return list.stream().map(convert).collect(Collectors.toList());
        }
        list = section.getStringList(singular);
        if (!list.isEmpty()) {
            return list.stream().map(convert).collect(Collectors.toList());
        }

        String single = section.getString(singular);
        if (single != null && !single.isEmpty()) {
            return Collections.singletonList(convert.apply(single));
        }
        single = section.getString(plural);
        if (single != null && !single.isEmpty()) {
            return Collections.singletonList(convert.apply(single));
        }

        return Collections.emptyList();
    }

    private static <T> T tryNumber(String str, Function<String, T> convert) {
        try {
            return convert.apply(str);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Adds a list of items to the player's inventory and drop the items that did not fit.
     *
     * @param player the player to give the items to.
     * @param items  the items to give.
     * @return the items that did not fit and were dropped.
     * @since 2.0.1
     */
    @NotNull
    @Contract(mutates = "param1")
    public static List<ItemStack> giveOrDrop(@NotNull Player player, @Nullable ItemStack... items) {
        return giveOrDrop(player, true, items);
    }

    /**
     * Adds a list of items to the player's inventory and drop the items that did not fit.
     *
     * @param player the player to give the items to.
     * @param items  the items to give.
     * @param split  same as {@link #addItems(Inventory, boolean, ItemStack...)}
     * @return the items that did not fit and were dropped.
     * @since 2.0.1
     */
    @NotNull
    @Contract(mutates = "param1")
    public static List<ItemStack> giveOrDrop(@NotNull Player player, boolean split, @Nullable ItemStack... items) {
        if (items == null || items.length == 0) return new ArrayList<>();
        List<ItemStack> leftOvers = addItems(player.getInventory(), split, items);
        World world = player.getWorld();
        Location location = player.getLocation();

        for (ItemStack drop : leftOvers) world.dropItemNaturally(location, drop);
        return leftOvers;
    }

    @Contract(mutates = "param1")
    public static List<ItemStack> addItems(@NotNull Inventory inventory, boolean split, @NotNull ItemStack... items) {
        return addItems(inventory, split, null, items);
    }

    /**
     * Optimized version of {@link Inventory#addItem(ItemStack...)}
     * <a href="https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/inventory/CraftInventory.java">CraftInventory</a>
     *
     * @param inventory       the inventory to add the items to.
     * @param split           false if it should check for the inventory stack size {@link Inventory#getMaxStackSize()} or
     *                        true for item's max stack size {@link ItemStack#getMaxStackSize()} when putting items. This is useful when
     *                        you're adding stacked tools such as swords that you'd like to split them to other slots.
     * @param modifiableSlots the slots that are allowed to be used for adding the items, otherwise null to allow all slots.
     * @param items           the items to add.
     * @return items that didn't fit in the inventory.
     * @since 4.0.0
     */

    @NotNull
    @Contract(mutates = "param1")
    public static List<ItemStack> addItems(@NotNull Inventory inventory, boolean split,
                                           @Nullable Predicate<Integer> modifiableSlots, @NotNull ItemStack... items) {
        Objects.requireNonNull(inventory, "Cannot add items to null inventory");
        Objects.requireNonNull(items, "Cannot add null items to inventory");

        List<ItemStack> leftOvers = new ArrayList<>(items.length);

        // No other optimized way to access this using Bukkit API...
        // We could pass the length to individual methods, so they could also use getItem() which
        // skips parsing all the items in the inventory if not needed, but that's just too much.
        // Note: This is not the same as Inventory#getSize()
        int invSize = getStorageContents(inventory).length;
        int lastEmpty = 0;

        for (ItemStack item : items) {
            int lastPartial = 0;
            int maxAmount = split ? item.getMaxStackSize() : inventory.getMaxStackSize();

            while (true) {
                // Check if there is a similar item that can be stacked before using free slots.
                int firstPartial = lastPartial >= invSize ? -1 : firstPartial(inventory, item, lastPartial, modifiableSlots);
                if (firstPartial == -1) { // No partial items found
                    // Start adding items to leftovers if there are no partial and empty slots
                    // -1 means that there are no empty slots left.
                    if (lastEmpty != -1) lastEmpty = firstEmpty(inventory, lastEmpty, modifiableSlots);
                    if (lastEmpty == -1) {
                        leftOvers.add(item);
                        break;
                    }

                    // Avoid firstPartial() for checking again for no reason, since if we're already checking
                    // for free slots, that means there are no partials even left.
                    lastPartial = Integer.MAX_VALUE;

                    int amount = item.getAmount();
                    if (amount <= maxAmount) {
                        inventory.setItem(lastEmpty, item);
                        break;
                    } else {
                        ItemStack copy = item.clone();
                        copy.setAmount(maxAmount);
                        inventory.setItem(lastEmpty, copy);
                        item.setAmount(amount - maxAmount);
                    }
                    if (++lastEmpty == invSize) lastEmpty = -1;
                } else {
                    ItemStack partialItem = inventory.getItem(firstPartial);
                    int sum = item.getAmount() + partialItem.getAmount();

                    if (sum <= maxAmount) {
                        partialItem.setAmount(sum);
                        inventory.setItem(firstPartial, partialItem);
                        break;
                    } else {
                        partialItem.setAmount(maxAmount);
                        inventory.setItem(firstPartial, partialItem);
                        item.setAmount(sum - maxAmount);
                    }
                    lastPartial = firstPartial + 1;
                }
            }
        }

        return leftOvers;
    }

    @NotNull
    @Contract(pure = true)
    @Range(from = -1, to = Integer.MAX_VALUE)
    public static int firstPartial(@NotNull Inventory inventory, @Nullable ItemStack item, int beginIndex) {
        return firstPartial(inventory, item, beginIndex, null);
    }

    /**
     * Gets the item slot in the inventory that matches the given item argument.
     * The matched item must be {@link ItemStack#isSimilar(ItemStack)} and has not
     * reached its {@link ItemStack#getMaxStackSize()} for the inventory.
     *
     * @param inventory       the inventory to match the item from.
     * @param item            the item to match.
     * @param beginIndex      the index which to start the search from in the inventory.
     * @param modifiableSlots the slots that can be used to share items.
     * @return the first matched item slot, otherwise -1
     * @throws IndexOutOfBoundsException if the beginning index is less than 0 or greater than the inventory storage size.
     * @since 4.0.0
     */
    @NotNull
    @Contract(pure = true)
    @Range(from = -1, to = Integer.MAX_VALUE)
    public static int firstPartial(@NotNull Inventory inventory, @Nullable ItemStack item, int beginIndex, @Nullable Predicate<Integer> modifiableSlots) {
        if (item != null) {
            ItemStack[] items = getStorageContents(inventory);
            int invSize = items.length;
            if (beginIndex < 0 || beginIndex >= invSize)
                throw new IndexOutOfBoundsException("Begin Index: " + beginIndex + ", Inventory storage content size: " + invSize);

            for (; beginIndex < invSize; beginIndex++) {
                if (modifiableSlots != null && !modifiableSlots.test(beginIndex)) continue;
                ItemStack cItem = items[beginIndex];
                if (cItem != null && cItem.getAmount() < cItem.getMaxStackSize() && cItem.isSimilar(item))
                    return beginIndex;
            }
        }
        return -1;
    }

    @NotNull
    @Contract(pure = true)
    public static List<ItemStack> stack(@NotNull Collection<ItemStack> items) {
        return stack(items, ItemStack::isSimilar);
    }

    /**
     * Stacks up the items in the given item collection that are pass the similarity check.
     * This means that if you have a collection that consists of separate items with the same material, you can reduce them using the following:
     * <pre>{@code
     *   List<ItemStack> items = Arrays.asList(XMaterial.STONE.parseItem(), XMaterial.STONE.parseItem(), XMaterial.AIR.parseItem());
     *   items = XItemStack.stack(items, (first, second) -> first.getType == second.getType());
     *   // items -> [STONE x2, AIR x1]
     * }</pre>
     *
     * @param items the items to stack.
     * @return stacked up items.
     * @since 4.0.0
     */
    @NotNull
    @Contract(pure = true)
    public static List<ItemStack> stack(@NotNull Collection<ItemStack> items, @NotNull BiPredicate<ItemStack, ItemStack> similarity) {
        Objects.requireNonNull(items, "Cannot stack null items");
        Objects.requireNonNull(similarity, "Similarity check cannot be null");
        List<ItemStack> stacked = new ArrayList<>(items.size());

        for (ItemStack item : items) {
            if (item == null) continue;

            // AssertionError: TRAP ItemStack.setCount -> ItemStack.updateEmptyCacheFlag
            if (item.getType() == Material.AIR) continue;

            boolean add = true;
            for (ItemStack stack : stacked) {
                if (similarity.test(item, stack)) {
                    stack.setAmount(stack.getAmount() + item.getAmount());
                    add = false;
                    break;
                }
            }

            if (add) stacked.add(item.clone());
        }
        return stacked;
    }

    @Contract(pure = true)
    @Range(from = -1, to = Integer.MAX_VALUE)
    public static int firstEmpty(@NotNull Inventory inventory, int beginIndex) {
        return firstEmpty(inventory, beginIndex, null);
    }

    /**
     * Gets the first item slot in the inventory that is empty or matches the given item argument.
     * The matched item must be {@link ItemStack#isSimilar(ItemStack)} and has not
     * reached its {@link ItemStack#getMaxStackSize()} for the inventory.
     *
     * @param inventory       the inventory to search from.
     * @param beginIndex      the item slot to start the search from in the inventory.
     * @param modifiableSlots the slots that can be used.
     * @return first empty item slot, otherwise -1
     * @throws IndexOutOfBoundsException if the beginning index is less than 0 or greater than the inventory storage size.
     * @since 4.0.0
     */
    @Contract(pure = true)
    @Range(from = -1, to = Integer.MAX_VALUE)
    public static int firstEmpty(@NotNull Inventory inventory, int beginIndex, @Nullable Predicate<Integer> modifiableSlots) {
        ItemStack[] items = getStorageContents(inventory);
        int invSize = items.length;
        if (beginIndex < 0 || beginIndex >= invSize)
            throw new IndexOutOfBoundsException("Begin Index: " + beginIndex + ", Inventory storage content size: " + invSize);

        for (; beginIndex < invSize; beginIndex++) {
            if (modifiableSlots != null && !modifiableSlots.test(beginIndex)) continue;
            if (items[beginIndex] == null) return beginIndex;
        }
        return -1;
    }

    /**
     * Gets the first empty slot or partial item in the inventory from an index.
     *
     * @param inventory  the inventory to search from.
     * @param beginIndex the item slot to start the search from in the inventory.
     * @return first empty or partial item slot, otherwise -1
     * @throws IndexOutOfBoundsException if the beginning index is less than 0 or greater than the inventory storage size.
     * @see #firstEmpty(Inventory, int)
     * @see #firstPartial(Inventory, ItemStack, int)
     * @since 4.2.0
     */
    @Contract(pure = true)
    @Range(from = -1, to = Integer.MAX_VALUE)
    public static int firstPartialOrEmpty(@NotNull Inventory inventory, @Nullable ItemStack item, int beginIndex) {
        if (item != null) {
            ItemStack[] items = getStorageContents(inventory);
            int len = items.length;
            if (beginIndex < 0 || beginIndex >= len)
                throw new IndexOutOfBoundsException("Begin Index: " + beginIndex + ", Size: " + len);

            for (; beginIndex < len; beginIndex++) {
                ItemStack cItem = items[beginIndex];
                if (cItem == null || (cItem.getAmount() < cItem.getMaxStackSize() && cItem.isSimilar(item)))
                    return beginIndex;
            }
        }
        return -1;
    }

    /**
     * Cross-version compatible version of {@link Inventory#getStorageContents()}.
     */
    @Contract(pure = true)
    public static ItemStack[] getStorageContents(Inventory inventory) {
        // Mojang divides player inventory like this:
        //     public final ItemStack[] items = new ItemStack[36];
        //     public final ItemStack[] armor = new ItemStack[4];
        //     public final ItemStack[] extraSlots = new ItemStack[1];
        if (SUPPORTS_Inventory_getStorageContents) {
            // v1.9 extends inventory API
            return inventory.getStorageContents();
        } else {
            // Arrays.copyOfRange(this.getContents(), 0, this.getInventory().items.length);
            // 36 -> boots, 37 -> leggings, 38 -> chestplate, 39 - helmet, 40 -> offhand
            return Arrays.copyOfRange(inventory.getContents(), 0, 36);
        }
    }

    /**
     * @see #isEmpty(ItemStack)
     * @since 7.5.2
     */
    @Contract(pure = true)
    public static boolean notEmpty(@Nullable ItemStack item) {
        return !isEmpty(item);
    }

    /**
     * Checks if this item is {@code null} or {@link Material#AIR}.
     * The latter can only happen in the following situations:
     * <ul>
     *     <li>{@link PlayerInventory#getItemInMainHand()}</li>
     *     <li>{@link PlayerInventory#getItemInOffHand()}</li>
     * </ul>
     *
     * @see #notEmpty(ItemStack)
     * @since 7.5.2
     */
    @Contract(pure = true)
    public static boolean isEmpty(@Nullable ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    public static class MaterialCondition extends RuntimeException {
        protected XMaterial solution;

        public MaterialCondition(String message) {
            super(message);
        }

        public void setSolution(XMaterial solution) {
            this.solution = solution;
        }

        public boolean hasSolution() {
            return this.solution != null;
        }
    }

    public static final class UnknownMaterialCondition extends MaterialCondition {
        private final String material;

        public UnknownMaterialCondition(String material) {
            super("Unknown material: " + material);
            this.material = material;
        }

        public String getMaterial() {
            return material;
        }
    }

    public static final class UnAcceptableMaterialCondition extends MaterialCondition {
        private final XMaterial material;
        private final Reason reason;

        public UnAcceptableMaterialCondition(XMaterial material, Reason reason) {
            super("Unacceptable material: " + material.name() + " (" + reason.name() + ')');
            this.material = material;
            this.reason = reason;
        }

        public Reason getReason() {
            return reason;
        }

        public XMaterial getMaterial() {
            return material;
        }

        public enum Reason {UNSUPPORTED, NOT_DISPLAYABLE}
    }

    public static final class UnsetMaterialCondition extends MaterialCondition {
        public UnsetMaterialCondition() {
            super("No material is not set or could be derived");
        }
    }
}
