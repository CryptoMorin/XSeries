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
package com.cryptomorin.xseries;

import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import com.google.common.base.Enums;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.map.MapView;
import org.bukkit.material.MaterialData;
import org.bukkit.material.SpawnEgg;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.cryptomorin.xseries.XMaterial.supports;

/**
 * <b>XItemStack</b> - YAML Item Serializer<br>
 * Using ConfigurationSection Example:
 * <pre>
 *     ConfigurationSection section = plugin.getConfig().getConfigurationSection("staffs.dragon-staff");
 *     ItemStack item = XItemStack.deserialize(section);
 * </pre>
 * <a href="https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/ItemStack.html">ItemStack</a>
 *
 * @author Crypto Morin
 * @version 7.5.1
 * @see XMaterial
 * @see XPotion
 * @see XSkull
 * @see XEnchantment
 * @see ItemStack
 */
public final class XItemStack {
    public static final ItemFlag[] ITEM_FLAGS = ItemFlag.values();
    public static final boolean SUPPORTS_CUSTOM_MODEL_DATA;

    /**
     * Because {@link ItemMeta} cannot be applied to {@link Material#AIR}.
     */
    private static final XMaterial DEFAULT_MATERIAL = XMaterial.BARRIER;
    private static final boolean SUPPORTS_POTION_COLOR;

    static {
        boolean supportsPotionColor = false;
        try {
            Class.forName("org.bukkit.inventory.meta.PotionMeta").getMethod("setColor", Color.class);
            supportsPotionColor = true;
        } catch (Throwable ignored) {
        }

        SUPPORTS_POTION_COLOR = supportsPotionColor;
    }

    static {
        boolean supportsCustomModelData = false;
        try {
            ItemMeta.class.getMethod("hasCustomModelData");
            supportsCustomModelData = true;
        } catch (Throwable ignored) {
        }

        SUPPORTS_CUSTOM_MODEL_DATA = supportsCustomModelData;
    }

    private XItemStack() {
    }

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
     */
    public static void serialize(@Nonnull ItemStack item, @Nonnull ConfigurationSection config) {
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
     */
    @SuppressWarnings("deprecation")
    public static void serialize(@Nonnull ItemStack item, @Nonnull ConfigurationSection config,
                                 @Nonnull Function<String, String> translator) {
        Objects.requireNonNull(item, "Cannot serialize a null item");
        Objects.requireNonNull(config, "Cannot serialize item from a null configuration section.");

        // Material
        config.set("material", XMaterial.matchXMaterial(item).name());

        // Amount
        if (item.getAmount() > 1) config.set("amount", item.getAmount());

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Durability - Damage
        if (supports(13)) {
            if (meta instanceof Damageable) {
                Damageable damageable = (Damageable) meta;
                if (damageable.hasDamage()) config.set("damage", damageable.getDamage());
            }
        } else {
            config.set("damage", item.getDurability());
        }

        // Display Name & Lore
        if (meta.hasDisplayName()) config.set("name", translator.apply(meta.getDisplayName()));
        if (meta.hasLore()) config.set("lore", meta.getLore().stream().map(translator).collect(Collectors.toList()));

        if (supports(14)) {
            if (meta.hasCustomModelData()) config.set("custom-model-data", meta.getCustomModelData());
        }
        if (supports(11)) {
            if (meta.isUnbreakable()) config.set("unbreakable", true);
        }

        // Enchantments
        for (Map.Entry<Enchantment, Integer> enchant : meta.getEnchants().entrySet()) {
            String entry = "enchants." + XEnchantment.matchXEnchantment(enchant.getKey()).name();
            config.set(entry, enchant.getValue());
        }

        // Flags
        if (!meta.getItemFlags().isEmpty()) {
            Set<ItemFlag> flags = meta.getItemFlags();
            List<String> flagNames = new ArrayList<>(flags.size());
            for (ItemFlag flag : flags) flagNames.add(flag.name());
            config.set("flags", flagNames);
        }

        // Attributes - https://minecraft.wiki/w/Attribute
        if (supports(13)) {
            Multimap<Attribute, AttributeModifier> attributes = meta.getAttributeModifiers();
            if (attributes != null) {
                for (Map.Entry<Attribute, AttributeModifier> attribute : attributes.entries()) {
                    String path = "attributes." + attribute.getKey().name() + '.';
                    AttributeModifier modifier = attribute.getValue();

                    config.set(path + "id", modifier.getUniqueId().toString());
                    config.set(path + "name", modifier.getName());
                    config.set(path + "amount", modifier.getAmount());
                    config.set(path + "operation", modifier.getOperation().name());
                    if (modifier.getSlot() != null) config.set(path + "slot", modifier.getSlot().name());
                }
            }
        }

        if (meta instanceof BlockStateMeta) {
            BlockState state = safeBlockState((BlockStateMeta) meta);

            if (supports(11) && state instanceof ShulkerBox) {
                ShulkerBox box = (ShulkerBox) state;
                ConfigurationSection shulker = config.createSection("contents");
                int i = 0;
                for (ItemStack itemInBox : box.getInventory().getContents()) {
                    if (itemInBox != null) serialize(itemInBox, shulker.createSection(Integer.toString(i)), translator);
                    i++;
                }
            } else if (state instanceof CreatureSpawner) {
                CreatureSpawner cs = (CreatureSpawner) state;
                if (cs.getSpawnedType() != null) config.set("spawner", cs.getSpawnedType().name());
            }
        } else if (meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta book = (EnchantmentStorageMeta) meta;
            for (Map.Entry<Enchantment, Integer> enchant : book.getStoredEnchants().entrySet()) {
                String entry = "stored-enchants." + XEnchantment.matchXEnchantment(enchant.getKey()).name();
                config.set(entry, enchant.getValue());
            }
        } else if (meta instanceof SkullMeta) {
            String skull = XSkull.of(meta).getProfileString();
            if (skull != null) config.set("skull", skull);
        } else if (meta instanceof BannerMeta) {
            BannerMeta banner = (BannerMeta) meta;
            ConfigurationSection patterns = config.createSection("patterns");
            for (Pattern pattern : banner.getPatterns()) {
                patterns.set(pattern.getPattern().name(), pattern.getColor().name());
            }
        } else if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leather = (LeatherArmorMeta) meta;
            Color color = leather.getColor();
            config.set("color", color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
        } else if (meta instanceof PotionMeta) {
            if (supports(9)) {
                PotionMeta potion = (PotionMeta) meta;
                List<PotionEffect> customEffects = potion.getCustomEffects();
                List<String> effects = new ArrayList<>(customEffects.size());
                for (PotionEffect effect : customEffects) {
                    effects.add(effect.getType().getName() + ", " + effect.getDuration() + ", " + effect.getAmplifier());
                }

                if (!effects.isEmpty()) config.set("effects", effects);
                PotionType basePotionType = potion.getBasePotionType();
                // PotionData potionData = potion.getBasePotionData();
                // config.set("base-effect", potionData.getType().name() + ", " + potionData.isExtended() + ", " + potionData.isUpgraded());

                config.set("base-type", basePotionType.name());

                config.set("effects", potion.getCustomEffects().stream().map(x -> {
                    NamespacedKey type = x.getType().getKey();
                    String typeStr = type.getNamespace() + ':' + type.getKey();
                    return typeStr + ", " + x.getDuration() + ", " + x.getAmplifier();
                }));

                if (SUPPORTS_POTION_COLOR && potion.hasColor()) config.set("color", potion.getColor().asRGB());
            } else {
                // Check for water bottles in 1.8
                // Potion class is now removed...
                // if (item.getDurability() != 0) {
                //     Potion potion = Potion.fromItemStack(item);
                //     config.set("level", potion.getLevel());
                //     config.set("base-effect", potion.getType().name() + ", " + potion.hasExtendedDuration() + ", " + potion.isSplash());
                // }
            }
        } else if (meta instanceof FireworkMeta) {
            FireworkMeta firework = (FireworkMeta) meta;
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
                    baseColors.add(color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
                colors.set("base", baseColors);

                for (Color color : fwFadeColors)
                    fadeColors.add(color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
                colors.set("fade", fadeColors);
                i++;
            }
        } else if (meta instanceof BookMeta) {
            BookMeta book = (BookMeta) meta;

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
        } else if (meta instanceof MapMeta) {
            MapMeta map = (MapMeta) meta;
            ConfigurationSection mapSection = config.createSection("map");

            mapSection.set("scaling", map.isScaling());
            if (supports(11)) {
                if (map.hasLocationName()) mapSection.set("location", map.getLocationName());
                if (map.hasColor()) {
                    Color color = map.getColor();
                    mapSection.set("color", color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
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
        } else {
            if (supports(20)) {
                if (meta instanceof ArmorMeta) {
                    ArmorMeta armorMeta = (ArmorMeta) meta;
                    if (armorMeta.hasTrim()) {
                        ArmorTrim trim = armorMeta.getTrim();
                        ConfigurationSection trimConfig = config.createSection("trim");
                        trimConfig.set("material", trim.getMaterial().getKey().getNamespace() + ':' + trim.getMaterial().getKey().getKey());
                        trimConfig.set("pattern", trim.getPattern().getKey().getNamespace() + ':' + trim.getPattern().getKey().getKey());
                    }
                }
            }

            if (supports(17)) {
                if (meta instanceof AxolotlBucketMeta) {
                    AxolotlBucketMeta bucket = (AxolotlBucketMeta) meta;
                    if (bucket.hasVariant()) config.set("color", bucket.getVariant().toString());
                }
            }

            if (supports(16)) {
                if (meta instanceof CompassMeta) {
                    CompassMeta compass = (CompassMeta) meta;
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
            }

            if (supports(14)) {
                if (meta instanceof CrossbowMeta) {
                    CrossbowMeta crossbow = (CrossbowMeta) meta;
                    int i = 0;
                    for (ItemStack projectiles : crossbow.getChargedProjectiles()) {
                        serialize(projectiles, config.getConfigurationSection("projectiles." + i), translator);
                        i++;
                    }
                } else if (meta instanceof TropicalFishBucketMeta) {
                    TropicalFishBucketMeta tropical = (TropicalFishBucketMeta) meta;
                    config.set("pattern", tropical.getPattern().name());
                    config.set("color", tropical.getBodyColor().name());
                    config.set("pattern-color", tropical.getPatternColor().name());
                } else if (meta instanceof SuspiciousStewMeta) {
                    SuspiciousStewMeta stew = (SuspiciousStewMeta) meta;
                    List<PotionEffect> customEffects = stew.getCustomEffects();
                    List<String> effects = new ArrayList<>(customEffects.size());

                    for (PotionEffect effect : customEffects) {
                        effects.add(effect.getType().getName() + ", " + effect.getDuration() + ", " + effect.getAmplifier());
                    }

                    config.set("effects", effects);
                }
            }

            if (!supports(13)) {
                // Spawn Eggs
                if (supports(11)) {
                    if (meta instanceof SpawnEggMeta) {
                        SpawnEggMeta spawnEgg = (SpawnEggMeta) meta;
                        config.set("creature", spawnEgg.getSpawnedType().getName());
                    }
                } else {
                    MaterialData data = item.getData();
                    if (data instanceof SpawnEgg) {
                        SpawnEgg spawnEgg = (SpawnEgg) data;
                        config.set("creature", spawnEgg.getSpawnedType().getName());
                    }
                }
            }
        }
    }

    /**
     * Writes an ItemStack properties into a {@code Map}.
     *
     * @param item the ItemStack to serialize.
     * @return a Map containing the serialized ItemStack properties.
     */
    public static Map<String, Object> serialize(@Nonnull ItemStack item) {
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
     */
    @Nonnull
    public static ItemStack deserialize(@Nonnull ConfigurationSection config) {
        return edit(DEFAULT_MATERIAL.parseItem(), config, Function.identity(), null);
    }

    /**
     * Deserialize an ItemStack from a {@code Map}.
     *
     * @param serializedItem the map holding the item configurations to deserialize
     *                       the ItemStack object from.
     * @return a deserialized ItemStack.
     */
    @Nonnull
    public static ItemStack deserialize(@Nonnull Map<String, Object> serializedItem) {
        Objects.requireNonNull(serializedItem, "serializedItem cannot be null.");
        return deserialize(mapToConfigSection(serializedItem));
    }

    @Nonnull
    public static ItemStack deserialize(@Nonnull ConfigurationSection config,
                                        @Nonnull Function<String, String> translator) {
        return deserialize(config, translator, null);
    }

    /**
     * Deserialize an ItemStack from the config.
     *
     * @param config the config section to deserialize the ItemStack object from.
     * @return an edited ItemStack.
     * @since 7.2.0
     */
    @Nonnull
    public static ItemStack deserialize(@Nonnull ConfigurationSection config,
                                        @Nonnull Function<String, String> translator,
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
     */
    @Nonnull
    public static ItemStack deserialize(@Nonnull Map<String, Object> serializedItem, @Nonnull Function<String, String> translator) {
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

    private static List<String> split(@Nonnull String str, @SuppressWarnings("SameParameterValue") char separatorChar) {
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
     * Deserialize an ItemStack from the config.
     *
     * @param config     the config section to deserialize the ItemStack object from.
     * @param translator the function applied to item name and each lore line.
     * @param restart    the function called when an error occurs while deserializing one of the properties.
     * @return an edited ItemStack.
     * @since 1.0.0
     */
    @SuppressWarnings("deprecation")
    @Nonnull
    public static ItemStack edit(@Nonnull ItemStack item,
                                 @Nonnull final ConfigurationSection config,
                                 @Nonnull final Function<String, String> translator,
                                 @Nullable final Consumer<Exception> restart) {
        Objects.requireNonNull(item, "Cannot operate on null ItemStack, considering using an AIR ItemStack instead");
        Objects.requireNonNull(config, "Cannot deserialize item to a null configuration section.");
        Objects.requireNonNull(translator, "Translator function cannot be null");

        // Material
        String materialName = config.getString("material");
        if (!Strings.isNullOrEmpty(materialName)) {
            Optional<XMaterial> materialOpt = XMaterial.matchXMaterial(materialName);
            XMaterial material;
            if (materialOpt.isPresent()) material = materialOpt.get();
            else {
                UnknownMaterialCondition unknownMaterialCondition = new UnknownMaterialCondition(materialName);
                if (restart == null) throw unknownMaterialCondition;
                restart.accept(unknownMaterialCondition);

                if (unknownMaterialCondition.hasSolution()) material = unknownMaterialCondition.solution;
                else throw unknownMaterialCondition;
            }

            if (!material.isSupported()) {
                UnAcceptableMaterialCondition unsupportedMaterialCondition = new UnAcceptableMaterialCondition(material, UnAcceptableMaterialCondition.Reason.UNSUPPORTED);
                if (restart == null) throw unsupportedMaterialCondition;
                restart.accept(unsupportedMaterialCondition);

                if (unsupportedMaterialCondition.hasSolution()) material = unsupportedMaterialCondition.solution;
                else throw unsupportedMaterialCondition;
            }
            if (XTag.INVENTORY_NOT_DISPLAYABLE.isTagged(material)) {
                UnAcceptableMaterialCondition unsupportedMaterialCondition = new UnAcceptableMaterialCondition(material, UnAcceptableMaterialCondition.Reason.NOT_DISPLAYABLE);
                if (restart == null) throw unsupportedMaterialCondition;
                restart.accept(unsupportedMaterialCondition);

                if (unsupportedMaterialCondition.hasSolution()) material = unsupportedMaterialCondition.solution;
                else throw unsupportedMaterialCondition;
            }

            material.setType(item);
        }

        // Amount
        int amount = config.getInt("amount");
        if (amount > 1) item.setAmount(amount);

        ItemMeta meta;
        { // For Java's stupid closure capture system.
            ItemMeta tempMeta = item.getItemMeta();
            if (tempMeta == null) {
                // When AIR is null. Useful for when you just want to use the meta to save data and
                // set the type later. A simple CraftMetaItem.
                meta = Bukkit.getItemFactory().getItemMeta(XMaterial.STONE.parseMaterial());
            } else {
                meta = tempMeta;
            }
        }


        // Durability - Damage
        if (supports(13)) {
            if (meta instanceof Damageable) {
                int damage = config.getInt("damage");
                if (damage > 0) ((Damageable) meta).setDamage(damage);
            }
        } else {
            int damage = config.getInt("damage");
            if (damage > 0) item.setDurability((short) damage);
        }

        // Special Items
        if (meta instanceof SkullMeta) {
            // Make it lenient to support placeholders.
            String skull = config.getString("skull");
            if (skull != null) {
                // Since this is also an editing method, allow empty strings to
                // represent the instruction to completely remove an existing profile.
                if (skull.isEmpty()) XSkull.of(meta).profile(Profileable.detect(skull)).removeProfile();
                else XSkull.of(meta).profile(Profileable.detect(skull)).lenient().apply();
            }
        } else if (meta instanceof BannerMeta) {
            BannerMeta banner = (BannerMeta) meta;
            ConfigurationSection patterns = config.getConfigurationSection("patterns");

            if (patterns != null) {
                for (String pattern : patterns.getKeys(false)) {
                    PatternType type = Enums.getIfPresent(PatternType.class, pattern).orNull();
                    if (type == null)
                        type = Enums.getIfPresent(PatternType.class, pattern.toUpperCase(Locale.ENGLISH)).or(PatternType.BASE);
                    DyeColor color = Enums.getIfPresent(DyeColor.class, patterns.getString(pattern).toUpperCase(Locale.ENGLISH)).or(DyeColor.WHITE);

                    banner.addPattern(new Pattern(color, type));
                }
            }
        } else if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leather = (LeatherArmorMeta) meta;
            String colorStr = config.getString("color");
            if (colorStr != null) {
                leather.setColor(parseColor(colorStr));
            }
        } else if (meta instanceof PotionMeta) {
            if (supports(9)) {
                PotionMeta potion = (PotionMeta) meta;

                for (String effects : config.getStringList("effects")) {
                    XPotion.Effect effect = XPotion.parseEffect(effects);
                    if (effect.hasChance()) potion.addCustomEffect(effect.getEffect(), true);
                }

                String baseType = config.getString("base-type");
                if (!Strings.isNullOrEmpty(baseType)) {
                    XPotion.matchXPotion(baseType).ifPresent(x -> potion.setBasePotionType(x.getPotionType()));
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
        } else if (meta instanceof BlockStateMeta) {
            BlockStateMeta bsm = (BlockStateMeta) meta;
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
                        ItemStack boxItem = deserialize(shulkerSection.getConfigurationSection(key));
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
                        PatternType type = Enums.getIfPresent(PatternType.class, pattern).orNull();
                        if (type == null)
                            type = Enums.getIfPresent(PatternType.class, pattern.toUpperCase(Locale.ENGLISH)).or(PatternType.BASE);
                        DyeColor color = Enums.getIfPresent(DyeColor.class, patterns.getString(pattern).toUpperCase(Locale.ENGLISH)).or(DyeColor.WHITE);

                        banner.addPattern(new Pattern(color, type));
                    }

                    banner.update(true);
                    bsm.setBlockState(banner);
                }
            }
        } else if (meta instanceof FireworkMeta) {
            FireworkMeta firework = (FireworkMeta) meta;
            firework.setPower(config.getInt("power"));

            ConfigurationSection fireworkSection = config.getConfigurationSection("firework");
            if (fireworkSection != null) {
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
                        for (String colorStr : fwColors) colors.add(parseColor(colorStr));
                        builder.withColor(colors);

                        fwColors = colorsSection.getStringList("fade");
                        colors = new ArrayList<>(fwColors.size());
                        for (String colorStr : fwColors) colors.add(parseColor(colorStr));
                        builder.withFade(colors);
                    }

                    firework.addEffect(builder.build());
                }
            }
        } else if (meta instanceof BookMeta) {
            BookMeta book = (BookMeta) meta;
            ConfigurationSection bookInfo = config.getConfigurationSection("book");

            if (bookInfo != null) {
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
        } else if (meta instanceof MapMeta) {
            MapMeta map = (MapMeta) meta;
            ConfigurationSection mapSection = config.getConfigurationSection("map");

            if (mapSection != null) {
                map.setScaling(mapSection.getBoolean("scaling"));
                if (supports(11)) {
                    if (mapSection.isSet("location")) map.setLocationName(mapSection.getString("location"));
                    if (mapSection.isSet("color")) {
                        Color color = parseColor(mapSection.getString("color"));
                        map.setColor(color);
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
        } else {
            if (supports(20)) {
                if (meta instanceof ArmorMeta) {
                    ArmorMeta armorMeta = (ArmorMeta) meta;
                    if (config.isSet("trim")) {
                        ConfigurationSection trim = config.getConfigurationSection("trim");
                        TrimMaterial trimMaterial = Registry.TRIM_MATERIAL.get(NamespacedKey.fromString(trim.getString("material")));
                        TrimPattern trimPattern = Registry.TRIM_PATTERN.get(NamespacedKey.fromString(trim.getString("pattern")));
                        armorMeta.setTrim(new ArmorTrim(trimMaterial, trimPattern));
                    }
                }
            }

            if (supports(17)) {
                if (meta instanceof AxolotlBucketMeta) {
                    AxolotlBucketMeta bucket = (AxolotlBucketMeta) meta;
                    String variantStr = config.getString("color");
                    if (variantStr != null) {
                        Axolotl.Variant variant = Enums.getIfPresent(Axolotl.Variant.class, variantStr.toUpperCase(Locale.ENGLISH)).or(Axolotl.Variant.BLUE);
                        bucket.setVariant(variant);
                    }
                }
            }

            if (supports(16)) {
                if (meta instanceof CompassMeta) {
                    CompassMeta compass = (CompassMeta) meta;
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
            }

            if (supports(15)) {
                if (meta instanceof SuspiciousStewMeta) {
                    SuspiciousStewMeta stew = (SuspiciousStewMeta) meta;
                    for (String effects : config.getStringList("effects")) {
                        XPotion.Effect effect = XPotion.parseEffect(effects);
                        if (effect.hasChance()) stew.addCustomEffect(effect.getEffect(), true);
                    }
                }
            }

            if (supports(14)) {
                if (meta instanceof CrossbowMeta) {
                    CrossbowMeta crossbow = (CrossbowMeta) meta;
                    ConfigurationSection projectiles = config.getConfigurationSection("projectiles");
                    if (projectiles != null) {
                        for (String projectile : projectiles.getKeys(false)) {
                            ItemStack projectileItem = deserialize(config.getConfigurationSection("projectiles." + projectile));
                            crossbow.addChargedProjectile(projectileItem);
                        }
                    }
                } else if (meta instanceof TropicalFishBucketMeta) {
                    TropicalFishBucketMeta tropical = (TropicalFishBucketMeta) meta;
                    DyeColor color = Enums.getIfPresent(DyeColor.class, config.getString("color")).or(DyeColor.WHITE);
                    DyeColor patternColor = Enums.getIfPresent(DyeColor.class, config.getString("pattern-color")).or(DyeColor.WHITE);
                    TropicalFish.Pattern pattern = Enums.getIfPresent(TropicalFish.Pattern.class, config.getString("pattern")).or(TropicalFish.Pattern.BETTY);

                    tropical.setBodyColor(color);
                    tropical.setPatternColor(patternColor);
                    tropical.setPattern(pattern);
                }
            }

            // Apparently Suspicious Stew was never added in 1.14
            if (!supports(13)) {
                // Spawn Eggs
                if (supports(11)) {
                    if (meta instanceof SpawnEggMeta) {
                        String creatureName = config.getString("creature");
                        if (!Strings.isNullOrEmpty(creatureName)) {
                            SpawnEggMeta spawnEgg = (SpawnEggMeta) meta;
                            com.google.common.base.Optional<EntityType> creature = Enums.getIfPresent(EntityType.class, creatureName.toUpperCase(Locale.ENGLISH));
                            if (creature.isPresent()) spawnEgg.setSpawnedType(creature.get());
                        }
                    }
                } else {
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
        }

        // Display Name
        String name = config.getString("name");
        if (!Strings.isNullOrEmpty(name)) {
            String translated = translator.apply(name);
            meta.setDisplayName(translated);
        } else if (name != null && name.isEmpty())
            meta.setDisplayName(" "); // For GUI easy access configuration purposes

        // Unbreakable
        if (supports(11) && config.isSet("unbreakable")) meta.setUnbreakable(config.getBoolean("unbreakable"));

        // Custom Model Data
        if (supports(14)) {
            int modelData = config.getInt("custom-model-data");
            if (modelData != 0) meta.setCustomModelData(modelData);
        }

        // Lore
        if (config.isSet("lore")) {
            List<String> translatedLore;
            List<String> lores = config.getStringList("lore");
            if (!lores.isEmpty()) {
                translatedLore = new ArrayList<>(lores.size());

                for (String lore : lores) {
                    if (lore.isEmpty()) {
                        translatedLore.add(" ");
                        continue;
                    }

                    for (String singleLore : splitNewLine(lore)) {
                        if (singleLore.isEmpty()) {
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
                    for (String singleLore : splitNewLine(lore)) {
                        if (singleLore.isEmpty()) {
                            translatedLore.add(" ");
                            continue;
                        }
                        translatedLore.add(translator.apply(singleLore));
                    }
                }
            }

            meta.setLore(translatedLore);
        }

        // Enchantments
        ConfigurationSection enchants = config.getConfigurationSection("enchants");
        if (enchants != null) {
            for (String ench : enchants.getKeys(false)) {
                Optional<XEnchantment> enchant = XEnchantment.matchXEnchantment(ench);
                enchant.ifPresent(xEnchantment -> meta.addEnchant(xEnchantment.getEnchant(), enchants.getInt(ench), true));
            }
        } else if (config.getBoolean("glow")) {
            meta.addEnchant(XEnchantment.UNBREAKING.getEnchant(), 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS); // HIDE_UNBREAKABLE is not for UNBREAKING enchant.
        }

        // Enchanted Books
        ConfigurationSection enchantment = config.getConfigurationSection("stored-enchants");
        if (enchantment != null) {
            for (String ench : enchantment.getKeys(false)) {
                Optional<XEnchantment> enchant = XEnchantment.matchXEnchantment(ench);
                EnchantmentStorageMeta book = (EnchantmentStorageMeta) meta;
                enchant.ifPresent(xEnchantment -> book.addStoredEnchant(xEnchantment.getEnchant(), enchantment.getInt(ench), true));
            }
        }

        // Flags
        List<String> flags = config.getStringList("flags");
        if (!flags.isEmpty()) {
            for (String flag : flags) {
                flag = flag.toUpperCase(Locale.ENGLISH);
                if (flag.equals("ALL")) {
                    meta.addItemFlags(ITEM_FLAGS);
                    break;
                }

                ItemFlag itemFlag = Enums.getIfPresent(ItemFlag.class, flag).orNull();
                if (itemFlag != null) meta.addItemFlags(itemFlag);
            }
        } else {
            String allFlags = config.getString("flags");
            if (!Strings.isNullOrEmpty(allFlags) && allFlags.equalsIgnoreCase("ALL"))
                meta.addItemFlags(ITEM_FLAGS);
        }

        // Atrributes - https://minecraft.wiki/w/Attribute
        if (supports(13)) {
            ConfigurationSection attributes = config.getConfigurationSection("attributes");
            if (attributes != null) {
                for (String attribute : attributes.getKeys(false)) {
                    Attribute attributeInst = Enums.getIfPresent(Attribute.class, attribute.toUpperCase(Locale.ENGLISH)).orNull();
                    if (attributeInst == null) continue;
                    ConfigurationSection section = attributes.getConfigurationSection(attribute);
                    if (section == null) continue;

                    String attribId = section.getString("id");
                    UUID id = attribId != null ? UUID.fromString(attribId) : UUID.randomUUID();
                    EquipmentSlot slot = section.getString("slot") != null ? Enums.getIfPresent(EquipmentSlot.class, section.getString("slot")).or(EquipmentSlot.HAND) : null;

                    AttributeModifier modifier = new AttributeModifier(
                            id,
                            section.getString("name"),
                            section.getDouble("amount"),
                            Enums.getIfPresent(AttributeModifier.Operation.class, section.getString("operation"))
                                    .or(AttributeModifier.Operation.ADD_NUMBER),
                            slot);

                    meta.addAttributeModifier(attributeInst, modifier);
                }
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Converts a {@code Map<?, ?>} into a {@code ConfigurationSection}.
     *
     * @param map the map to convert.
     * @return a {@code ConfigurationSection} containing the map values.
     */
    @Nonnull
    private static ConfigurationSection mapToConfigSection(@Nonnull Map<?, ?> map) {
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
    @Nonnull
    private static Map<String, Object> configSectionToMap(@Nonnull ConfigurationSection config) {
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
     *
     * @param str the RGB string.
     * @return a color based on the RGB.
     * @since 1.1.0
     */
    @Nonnull
    public static Color parseColor(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) return Color.BLACK;
        List<String> rgb = split(str.replace(" ", ""), ',');
        if (rgb.size() < 3) return Color.WHITE;
        return Color.fromRGB(toInt(rgb.get(0), 0), toInt(rgb.get(1), 0), toInt(rgb.get(2), 0));
    }

    /**
     * Adds a list of items to the player's inventory and drop the items that did not fit.
     *
     * @param player the player to give the items to.
     * @param items  the items to give.
     * @return the items that did not fit and were dropped.
     * @since 2.0.1
     */
    @Nonnull
    public static List<ItemStack> giveOrDrop(@Nonnull Player player, @Nullable ItemStack... items) {
        return giveOrDrop(player, false, items);
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
    @Nonnull
    public static List<ItemStack> giveOrDrop(@Nonnull Player player, boolean split, @Nullable ItemStack... items) {
        if (items == null || items.length == 0) return new ArrayList<>();
        List<ItemStack> leftOvers = addItems(player.getInventory(), split, items);
        World world = player.getWorld();
        Location location = player.getLocation();

        for (ItemStack drop : leftOvers) world.dropItemNaturally(location, drop);
        return leftOvers;
    }

    public static List<ItemStack> addItems(@Nonnull Inventory inventory, boolean split, @Nonnull ItemStack... items) {
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
    @Nonnull
    public static List<ItemStack> addItems(@Nonnull Inventory inventory, boolean split,
                                           @Nullable Predicate<Integer> modifiableSlots, @Nonnull ItemStack... items) {
        Objects.requireNonNull(inventory, "Cannot add items to null inventory");
        Objects.requireNonNull(items, "Cannot add null items to inventory");

        List<ItemStack> leftOvers = new ArrayList<>(items.length);

        // No other optimized way to access this using Bukkit API...
        // We could pass the length to individual methods, so they could also use getItem() which
        // skips parsing all the items in the inventory if not needed, but that's just too much.
        // Note: This is not the same as Inventory#getSize()
        int invSize = inventory.getStorageContents().length;
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

    public static int firstPartial(@Nonnull Inventory inventory, @Nullable ItemStack item, int beginIndex) {
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
    public static int firstPartial(@Nonnull Inventory inventory, @Nullable ItemStack item, int beginIndex, @Nullable Predicate<Integer> modifiableSlots) {
        if (item != null) {
            ItemStack[] items = inventory.getStorageContents();
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

    public static List<ItemStack> stack(@Nonnull Collection<ItemStack> items) {
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
    @Nonnull
    public static List<ItemStack> stack(@Nonnull Collection<ItemStack> items, @Nonnull BiPredicate<ItemStack, ItemStack> similarity) {
        Objects.requireNonNull(items, "Cannot stack null items");
        Objects.requireNonNull(similarity, "Similarity check cannot be null");
        List<ItemStack> stacked = new ArrayList<>(items.size());

        for (ItemStack item : items) {
            if (item == null) continue;

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

    public static int firstEmpty(@Nonnull Inventory inventory, int beginIndex) {
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
    public static int firstEmpty(@Nonnull Inventory inventory, int beginIndex, @Nullable Predicate<Integer> modifiableSlots) {
        ItemStack[] items = inventory.getStorageContents();
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
    public static int firstPartialOrEmpty(@Nonnull Inventory inventory, @Nullable ItemStack item, int beginIndex) {
        if (item != null) {
            ItemStack[] items = inventory.getStorageContents();
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
}
