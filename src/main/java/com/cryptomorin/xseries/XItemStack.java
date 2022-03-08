/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Crypto Morin
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

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
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
import org.bukkit.map.MapView;
import org.bukkit.material.MaterialData;
import org.bukkit.material.SpawnEgg;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.cryptomorin.xseries.XMaterial.supports;

/**
 * <b>XItemStack</b> - YAML Item Serializer<br>
 * Using ConfigurationSection Example:
 * <pre>
 *     ConfigurationSection section = plugin.getConfig().getConfigurationSection("staffs.dragon-staff");
 *     ItemStack item = XItemStack.deserialize(section);
 * </pre>
 * ItemStack: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/ItemStack.html
 *
 * @author Crypto Morin
 * @version 7.0.0
 * @see XMaterial
 * @see XPotion
 * @see SkullUtils
 * @see XEnchantment
 * @see ItemStack
 */
public final class XItemStack {
    public static final ItemFlag[] ITEM_FLAGS = ItemFlag.values();

    private XItemStack() {}

    /**
     * Writes an ItemStack object into a config.
     * The config file will not save after the object is written.
     *
     * @param item   the ItemStack to serialize.
     * @param config the config section to write this item to.
     *
     * @since 1.0.0
     */
    @SuppressWarnings("deprecation")
    public static void serialize(@Nonnull ItemStack item, @Nonnull ConfigurationSection config) {
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
        if (meta.hasDisplayName()) config.set("name", meta.getDisplayName());
        if (meta.hasLore()) config.set("lore", meta.getLore()); // TODO Add a method to "untranslate" color codes.

        if (supports(14)) {
            if (meta.hasCustomModelData()) config.set("custom-model", meta.getCustomModelData());
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

        // Attributes - https://minecraft.gamepedia.com/Attribute
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
            BlockState state = ((BlockStateMeta) meta).getBlockState();

            if (supports(11) && state instanceof ShulkerBox) {
                ShulkerBox box = (ShulkerBox) state;
                ConfigurationSection shulker = config.createSection("shulker");
                int i = 0;
                for (ItemStack itemInBox : box.getInventory().getContents()) {
                    if (itemInBox != null) serialize(itemInBox, shulker.createSection(Integer.toString(i)));
                    i++;
                }
            } else if (state instanceof CreatureSpawner) {
                CreatureSpawner cs = (CreatureSpawner) state;
                config.set("spawner", cs.getSpawnedType().name());
            }
        } else if (meta instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta book = (EnchantmentStorageMeta) meta;
            for (Map.Entry<Enchantment, Integer> enchant : book.getStoredEnchants().entrySet()) {
                String entry = "stored-enchants." + XEnchantment.matchXEnchantment(enchant.getKey()).name();
                config.set(entry, enchant.getValue());
            }
        } else if (meta instanceof SkullMeta) {
            config.set("skull", SkullUtils.getSkinValue(meta));
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

                config.set("custom-effects", effects);
                PotionData potionData = potion.getBasePotionData();
                config.set("base-effect", potionData.getType().name() + ", " + potionData.isExtended() + ", " + potionData.isUpgraded());

                if (potion.hasColor()) config.set("color", potion.getColor().asRGB());

            } else {

                //check for water bottles in 1.8
                if (item.getDurability() != 0) {
                    Potion potion = Potion.fromItemStack(item);
                    config.set("level", potion.getLevel());
                    config.set("base-effect", potion.getType().name() + ", " + potion.hasExtendedDuration() + ", " + potion.isSplash());
                }
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
                for (Color color : fwBaseColors) baseColors.add(color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
                colors.set("base", baseColors);

                for (Color color : fwFadeColors) fadeColors.add(color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
                colors.set("fade", fadeColors);
                i++;
            }
        } else if (meta instanceof BookMeta) {
            BookMeta book = (BookMeta) meta;
            ConfigurationSection bookInfo = config.createSection("book");

            bookInfo.set("title", book.getTitle());
            bookInfo.set("author", book.getAuthor());
            if (supports(9)) {
                BookMeta.Generation generation = book.getGeneration();
                if (generation != null) {
                    bookInfo.set("generation", book.getGeneration().toString());
                }
            }

            bookInfo.set("pages", book.getPages());
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
        } else if (supports(17)) {
            if (meta instanceof AxolotlBucketMeta) {
                AxolotlBucketMeta bucket = (AxolotlBucketMeta) meta;
                if (bucket.hasVariant()) config.set("variant", bucket.getVariant().toString());
            }
        } else if (supports(16)) {
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
        } else if (supports(14)) {
            if (meta instanceof CrossbowMeta) {
                CrossbowMeta crossbow = (CrossbowMeta) meta;
                int i = 0;
                for (ItemStack projectiles : crossbow.getChargedProjectiles()) {
                    serialize(projectiles, config.getConfigurationSection("projectiles." + i));
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
        } else if (!supports(13)) {
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

    /**
     * Writes an ItemStack properties into a {@code Map}.
     *
     * @param item the ItemStack to serialize.
     *
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
     *
     * @return a deserialized ItemStack.
     * @since 1.0.0
     */
    @Nonnull
    public static ItemStack deserialize(@Nonnull ConfigurationSection config) {
        return edit(new ItemStack(Material.AIR), config, Function.identity());
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
     * @param config the config section to deserialize the ItemStack object from.
     *
     * @return an edited ItemStack.
     * @since 7.0.0
     */
    @Nonnull
    public static ItemStack deserialize(@Nonnull ConfigurationSection config, @Nonnull Function<String, String> translator) {
        return edit(new ItemStack(Material.AIR), config, translator);
    }

    /**
     * Deserialize an ItemStack from the config.
     *
     * @param config the config section to deserialize the ItemStack object from.
     *
     * @return an edited ItemStack.
     * @since 1.0.0
     */
    @SuppressWarnings("deprecation")
    @Nonnull
    public static ItemStack edit(@Nonnull ItemStack item, @Nonnull ConfigurationSection config, @Nonnull Function<String, String> translator) {
        Objects.requireNonNull(item, "Cannot operate on null ItemStack, considering using an AIR ItemStack instead");
        Objects.requireNonNull(config, "Cannot deserialize item to a null configuration section.");
        Objects.requireNonNull(translator, "Translator function cannot be null");

        // Material
        String materialName = config.getString("material");
        Optional<XMaterial> material = Strings.isNullOrEmpty(materialName) ?
                Optional.empty() : XMaterial.matchXMaterial(materialName);
        if (material.isPresent()) material.get().setType(item);

        // Amount
        int amount = config.getInt("amount");
        if (amount > 1) item.setAmount(amount);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

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
            String skull = config.getString("skull");
            if (skull != null) SkullUtils.applySkin(meta, skull);
        } else if (meta instanceof BannerMeta) {
            BannerMeta banner = (BannerMeta) meta;
            ConfigurationSection patterns = config.getConfigurationSection("patterns");

            if (patterns != null) {
                for (String pattern : patterns.getKeys(false)) {
                    PatternType type = PatternType.getByIdentifier(pattern);
                    if (type == null) type = Enums.getIfPresent(PatternType.class, pattern.toUpperCase(Locale.ENGLISH)).or(PatternType.BASE);
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

                for (String effects : config.getStringList("custom-effects")) {
                    XPotion.Effect effect = XPotion.parseEffect(effects);
                    if (effect.hasChance()) potion.addCustomEffect(effect.getEffect(), true);
                }

                String baseEffect = config.getString("base-effect");
                if (!Strings.isNullOrEmpty(baseEffect)) {
                    String[] split = StringUtils.split(baseEffect, ',');
                    PotionType type = Enums.getIfPresent(PotionType.class, split[0].trim().toUpperCase(Locale.ENGLISH)).or(PotionType.UNCRAFTABLE);
                    boolean extended = split.length != 1 && Boolean.parseBoolean(split[1].trim());
                    boolean upgraded = split.length > 2 && Boolean.parseBoolean(split[2].trim());
                    PotionData potionData = new PotionData(type, extended, upgraded);
                    potion.setBasePotionData(potionData);
                }

                if (config.contains("color")) {
                    potion.setColor(Color.fromRGB(config.getInt("color")));
                }
            } else {

                if (config.contains("level")) {
                    int level = config.getInt("level");
                    String baseEffect = config.getString("base-effect");
                    if (!Strings.isNullOrEmpty(baseEffect)) {
                        String[] split = StringUtils.split(baseEffect, ',');
                        PotionType type = Enums.getIfPresent(PotionType.class, split[0].trim().toUpperCase(Locale.ENGLISH)).or(PotionType.SLOWNESS);
                        boolean extended = split.length != 1 && Boolean.parseBoolean(split[1].trim());
                        boolean splash = split.length > 2 && Boolean.parseBoolean(split[2].trim());

                        item = (new Potion(type, level, splash, extended)).toItemStack(1);
                    }
                }
            }
        } else if (meta instanceof BlockStateMeta) {
            BlockStateMeta bsm = (BlockStateMeta) meta;
            BlockState state = bsm.getBlockState();

            if (state instanceof CreatureSpawner) {
                CreatureSpawner spawner = (CreatureSpawner) state;
                spawner.setSpawnedType(Enums.getIfPresent(EntityType.class, config.getString("spawner").toUpperCase(Locale.ENGLISH)).orNull());
                spawner.update(true);
                bsm.setBlockState(spawner);
            } else if (supports(11) && state instanceof ShulkerBox) {
                ConfigurationSection shulkerSection = config.getConfigurationSection("shulker");
                if (shulkerSection != null) {
                    ShulkerBox box = (ShulkerBox) state;
                    for (String key : shulkerSection.getKeys(false)) {
                        ItemStack boxItem = deserialize(shulkerSection.getConfigurationSection(key));
                        int slot = NumberUtils.toInt(key, 0);
                        box.getInventory().setItem(slot, boxItem);
                    }
                    box.update(true);
                    bsm.setBlockState(box);
                }
            } else if (state instanceof Banner) {
                Banner banner = (Banner) state;
                ConfigurationSection patterns = config.getConfigurationSection("patterns");
                if (!supports(14))
                    banner.setBaseColor(DyeColor.WHITE); // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/diff/src/main/java/org/bukkit/craftbukkit/block
                // /CraftBanner.java?until=b3dc236663a55450c69356e660c0c84f0abbb3aa

                if (patterns != null) {
                    for (String pattern : patterns.getKeys(false)) {
                        PatternType type = PatternType.getByIdentifier(pattern);
                        if (type == null) type = Enums.getIfPresent(PatternType.class, pattern.toUpperCase(Locale.ENGLISH)).or(PatternType.BASE);
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
                    List<String> fwColors = colorsSection.getStringList("base");
                    List<Color> colors = new ArrayList<>(fwColors.size());
                    for (String colorStr : fwColors) colors.add(parseColor(colorStr));
                    builder.withColor(colors);

                    fwColors = colorsSection.getStringList("fade");
                    colors = new ArrayList<>(fwColors.size());
                    for (String colorStr : fwColors) colors.add(parseColor(colorStr));
                    builder.withFade(colors);

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
                            mapView.setCenterX(centerSection.getInt("x"));
                            mapView.setCenterZ(centerSection.getInt("z"));

                            map.setMapView(mapView);
                        }
                    }
                }
            }
        } else if (supports(17)) {
            if (meta instanceof AxolotlBucketMeta) {
                AxolotlBucketMeta bucket = (AxolotlBucketMeta) meta;
                String variantStr = config.getString("variant");
                if (variantStr != null) {
                    Axolotl.Variant variant = Enums.getIfPresent(Axolotl.Variant.class, variantStr.toUpperCase(Locale.ENGLISH)).or(Axolotl.Variant.BLUE);
                    bucket.setVariant(variant);
                }
            }
        } else if (supports(16)) {
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
        } else if (supports(15)) {
            if (meta instanceof SuspiciousStewMeta) {
                SuspiciousStewMeta stew = (SuspiciousStewMeta) meta;
                for (String effects : config.getStringList("effects")) {
                    XPotion.Effect effect = XPotion.parseEffect(effects);
                    if (effect.hasChance()) stew.addCustomEffect(effect.getEffect(), true);
                }
            }
        } else if (supports(14)) {
            if (meta instanceof CrossbowMeta) {
                CrossbowMeta crossbow = (CrossbowMeta) meta;
                for (String projectiles : config.getConfigurationSection("projectiles").getKeys(false)) {
                    ItemStack projectile = deserialize(config.getConfigurationSection("projectiles." + projectiles));
                    crossbow.addChargedProjectile(projectile);
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
            // Apparently Suspicious Stew was never added in 1.14
        } else if (!supports(13)) {
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

        // Display Name
        String name = config.getString("name");
        if (!Strings.isNullOrEmpty(name)) {
            String translated = translator.apply(name);
            meta.setDisplayName(translated);
        } else if (name != null && name.isEmpty()) meta.setDisplayName(" "); // For GUI easy access configuration purposes

        // Unbreakable
        if (supports(11)) meta.setUnbreakable(config.getBoolean("unbreakable"));

        // Custom Model Data
        if (supports(14)) {
            int modelData = config.getInt("model-data");
            if (modelData != 0) meta.setCustomModelData(modelData);
        }

        // Lore
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

            meta.setLore(translatedLore);
        } else {
            String lore = config.getString("lore");
            if (!Strings.isNullOrEmpty(lore)) meta.setLore(Collections.singletonList(translator.apply(lore)));
        }

        // Enchantments
        ConfigurationSection enchants = config.getConfigurationSection("enchants");
        if (enchants != null) {
            for (String ench : enchants.getKeys(false)) {
                Optional<XEnchantment> enchant = XEnchantment.matchXEnchantment(ench);
                enchant.ifPresent(xEnchantment -> meta.addEnchant(xEnchantment.getEnchant(), enchants.getInt(ench), true));
            }
        } else if (config.getBoolean("glow")) {
            meta.addEnchant(XEnchantment.DURABILITY.getEnchant(), 1, false);
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
                ItemFlag itemFlag = Enums.getIfPresent(ItemFlag.class, flag).orNull();
                if (itemFlag != null) meta.addItemFlags(itemFlag);
            }
        } else {
            String allFlags = config.getString("flags");
            if (!Strings.isNullOrEmpty(allFlags) && allFlags.equalsIgnoreCase("ALL"))
                meta.addItemFlags(ITEM_FLAGS);
        }

        // Atrributes - https://minecraft.gamepedia.com/Attribute
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
                            section.getInt("amount"),
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
     * Deserialize an ItemStack from a {@code Map}.
     *
     * @param serializedItem the map holding the item configurations to deserialize
     *                       the ItemStack object from.
     *
     * @return a deserialized ItemStack.
     */
    @Nonnull
    public static ItemStack deserialize(@Nonnull Map<String, Object> serializedItem) {
        Objects.requireNonNull(serializedItem, "serializedItem cannot be null.");
        return deserialize(mapToConfigSection(serializedItem));
    }

    /**
     * Converts a {@code Map<?, ?>} into a {@code ConfigurationSection}.
     *
     * @param map the map to convert.
     *
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
     *
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
     *
     * @return a color based on the RGB.
     * @since 1.1.0
     */
    @Nonnull
    public static Color parseColor(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) return Color.BLACK;
        String[] rgb = StringUtils.split(StringUtils.deleteWhitespace(str), ',');
        if (rgb.length < 3) return Color.WHITE;
        return Color.fromRGB(NumberUtils.toInt(rgb[0], 0), NumberUtils.toInt(rgb[1], 0), NumberUtils.toInt(rgb[2], 0));
    }

    /**
     * Adds a list of items to the player's inventory and drop the items that did not fit.
     *
     * @param player the player to give the items to.
     * @param items  the items to give.
     *
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
     *
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
     * https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/inventory/CraftInventory.java
     *
     * @param inventory       the inventory to add the items to.
     * @param split           if it should check for the inventory stack size {@link Inventory#getMaxStackSize()} or
     *                        item's max stack size {@link ItemStack#getMaxStackSize()} when putting items. This is useful when
     *                        you're adding stacked tools such as swords that you'd like to split them to other slots.
     * @param modifiableSlots the slots that are allowed to be used for adding the items, otherwise null to allow all slots.
     * @param items           the items to add.
     *
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
        // We could pass the length to individual methods so they could also use getItem() which
        // skips parsing all the items in the inventory if not needed, but that's just too much.
        // Note: This is not the same as Inventory#getSize()
        int invSize = inventory.getStorageContents().length;
        int lastEmpty = 0;

        for (ItemStack item : items) {
            int lastPartial = 0;

            while (true) {
                // Check if there is a similar item that can be stacked before using free slots.
                int firstPartial = lastPartial >= invSize ? -1 : firstPartial(inventory, item, lastPartial, modifiableSlots);
                if (firstPartial == -1) {
                    // Start adding items to left overs if there are no partial and empty slots
                    // -1 means that there are no empty slots left.
                    if (lastEmpty != -1) lastEmpty = firstEmpty(inventory, lastEmpty, modifiableSlots);
                    if (lastEmpty == -1) {
                        leftOvers.add(item);
                        break;
                    }

                    // Avoid firstPartial() for checking again for no reason, since if we're already checking
                    // for free slots, that means there are no partials even left.
                    lastPartial = invSize + 1;

                    int maxSize = split ? item.getMaxStackSize() : inventory.getMaxStackSize();
                    int amount = item.getAmount();
                    if (amount <= maxSize) {
                        inventory.setItem(lastEmpty, item);
                        break;
                    } else {
                        ItemStack copy = item.clone();
                        copy.setAmount(maxSize);
                        inventory.setItem(lastEmpty, copy);
                        item.setAmount(amount - maxSize);
                    }
                    if (++lastEmpty == invSize) lastEmpty = -1;
                } else {
                    ItemStack partialItem = inventory.getItem(firstPartial);
                    int maxAmount = partialItem.getMaxStackSize();
                    int partialAmount = partialItem.getAmount();
                    int amount = item.getAmount();
                    int sum = amount + partialAmount;

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
     *
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
                if (cItem != null && cItem.getAmount() < cItem.getMaxStackSize() && cItem.isSimilar(item)) return beginIndex;
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
     *
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
     *
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
     *
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
            if (beginIndex < 0 || beginIndex >= len) throw new IndexOutOfBoundsException("Begin Index: " + beginIndex + ", Size: " + len);

            for (; beginIndex < len; beginIndex++) {
                ItemStack cItem = items[beginIndex];
                if (cItem == null || (cItem.getAmount() < cItem.getMaxStackSize() && cItem.isSimilar(item))) return beginIndex;
            }
        }
        return -1;
    }
}
