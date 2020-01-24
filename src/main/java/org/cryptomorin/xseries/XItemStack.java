/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Crypto Morin
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

package org.cryptomorin.xseries;

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.cryptomorin.xseries.SkullUtils;
import org.cryptomorin.xseries.XEnchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/*
 * References
 *
 * * * GitHub: https://github.com/CryptoMorin/XSeries/blob/master/XItemStack.java
 * * XSeries: https://www.spigotmc.org/threads/378136/
 * ItemStack: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/ItemStack.html
 */

/**
 * <b>XItemStack</b> - YAML Item Serializer<br>
 * Using ConfigurationSection Example:
 * <pre>
 *     ConfigurationSection section = plugin.getConfig().getConfigurationSection("staffs.dragon-staff");
 *     ItemStack item = XItemStack.deserialize(player, section);
 * </pre>
 *
 * @author Crypto Morin
 * @version 1.1.0
 * @see XMaterial
 * @see XPotion
 * @see SkullUtils
 * @see XEnchantment
 * @see ItemStack
 */
public class XItemStack {
    /**
     * Writes an ItemStack object into a config.
     * The config file will not save after the object is written.
     *
     * @param item   the ItemStack to serialize.
     * @param config the config section to write this item to.
     */
    public static void serialize(ItemStack item, ConfigurationSection config) {
        ItemMeta meta = item.getItemMeta();

        if (meta.hasDisplayName()) config.set("name", meta.getDisplayName());
        if (item.getAmount() > 1) config.set("amount", item.getAmount());
        if (meta instanceof Damageable) {
            Damageable damageable = (Damageable) meta;
            if (damageable.hasDamage()) config.set("damage", damageable.getDamage());
        }
        config.set("material", item.getType());
        if (meta.hasCustomModelData()) config.set("custom-model", meta.getCustomModelData());
        if (meta.isUnbreakable()) config.set("unbreakable", true);
        if (meta.getItemFlags().size() != 0) config.set("flags", meta.getItemFlags());
        if (meta.hasAttributeModifiers()) config.set("attributes", meta.getAttributeModifiers());
        if (meta.hasLore()) config.set("lore", meta.getLore());
        if (meta.getEnchants().size() != 0) config.set("enchants", meta.getEnchants());
        if (XMaterial.supports(9) && meta.hasAttributeModifiers()) config.set("attributes", meta.getAttributeModifiers());

        if (meta instanceof SkullMeta) {
            config.set("skull", ((SkullMeta) meta).getOwningPlayer().getUniqueId());
        } else if (meta instanceof BannerMeta) {
            BannerMeta banner = (BannerMeta) meta;
            List<String> patterns = new ArrayList<>();
            for (Pattern pattern : banner.getPatterns()) {
                patterns.add(pattern.getColor() + " " + pattern.getPattern().getIdentifier());
            }
            config.set("patterns", patterns);
        } else if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leather = (LeatherArmorMeta) meta;
            Color color = leather.getColor();
            config.set("color", color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
        } else if (meta instanceof PotionMeta) {
            PotionMeta potion = (PotionMeta) meta;
            List<String> effects = new ArrayList<>();
            for (PotionEffect effect : potion.getCustomEffects())
                effects.add(effect.getType().getName() + " " + effect.getDuration() + " " + effect.getAmplifier());

            config.set("effects", effects);
        } else if (meta instanceof FireworkMeta) {
            FireworkMeta firework = (FireworkMeta) meta;
            config.set("power", firework.getPower());
            int i = 0;

            for (FireworkEffect fw : firework.getEffects()) {
                ConfigurationSection fwc = config.getConfigurationSection("firework." + i);
                fwc.set("type", fw.getType().name());

                List<String> colors = new ArrayList<>();
                for (Color color : fw.getColors()) colors.add(color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
                fwc.set("colors", colors);

                colors.clear();
                for (Color color : fw.getFadeColors()) colors.add(color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
                fwc.set("fade-colors", colors);
            }
            //} else if (meta instanceof MapMeta) {
        } else if (XMaterial.supports(14)) {
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
                List<String> effects = new ArrayList<>();
                for (PotionEffect effect : stew.getCustomEffects()) {
                    effects.add(effect.getType().getName() + " " + effect.getDuration() + " " + effect.getAmplifier());
                }

                config.set("effects", effects);
            }
        }
    }

    /**
     * Deserialize an ItemStack from the config.
     *
     * @param player used for placeholders. You must implement your own.
     * @param config the config section to deserialize the ItemStack object from.
     * @return a deserialized ItemStack.
     */
    @SuppressWarnings("deprecation")
    public static ItemStack deserialize(Player player, ConfigurationSection config) {
        // Material
        String material = config.getString("material");
        if (material == null) return null;
        Optional<XMaterial> matOpt = XMaterial.matchXMaterial(material);
        if (!matOpt.isPresent()) return null;

        // Build
        ItemStack item = matOpt.get().parseItem();
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();

        // Amount
        int amount = config.getInt("amount");
        if (amount > 1) item.setAmount(amount);

        // Durability - Damage
        if (XMaterial.isNewVersion()) {
            if (meta instanceof Damageable) {
                int damage = config.getInt("damage");
                if (damage > 0) ((Damageable) meta).setDamage(damage);
            }
        } else {
            int damage = config.getInt("damage");
            if (damage > 1) item.setDurability((short) damage);
        }

        // Special Items
        if (item.getType() == XMaterial.PLAYER_HEAD.parseMaterial()) {
            String skull = config.getString("skull");
            if (skull != null) SkullUtils.applySkin(meta, skull);
        } else if (meta instanceof BannerMeta) {
            BannerMeta banner = (BannerMeta) meta;

            for (String pattern : config.getStringList("patterns")) {
                String[] split = pattern.split("  +");
                if (split.length == 0) continue;
                DyeColor color = Enums.getIfPresent(DyeColor.class, split[0]).or(DyeColor.WHITE);
                PatternType type;

                if (split.length > 1) {
                    type = PatternType.getByIdentifier(split[1]);
                    if (type == null) type = Enums.getIfPresent(PatternType.class, split[1]).or(PatternType.BASE);
                } else {
                    type = PatternType.BASE;
                }

                banner.addPattern(new Pattern(color, type));
            }
        } else if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leather = (LeatherArmorMeta) meta;
            String colorStr = config.getString("color");
            if (colorStr != null) {
                leather.setColor(parseColor(colorStr));
            }
        } else if (meta instanceof PotionMeta) {
            PotionMeta potion = (PotionMeta) meta;
            for (String effects : config.getStringList("effects")) {
                PotionEffect effect = XPotion.parsePotionEffectFromString(effects);
                potion.addCustomEffect(effect, true);
            }
        } else if (meta instanceof FireworkMeta) {
            FireworkMeta firework = (FireworkMeta) meta;
            FireworkEffect.Builder builder = FireworkEffect.builder();
            for (String fws : config.getConfigurationSection("fireworks").getKeys(false)) {
                ConfigurationSection fw = config.getConfigurationSection("firework." + fws);

                firework.setPower(fw.getInt("power"));
                builder.flicker(fw.getBoolean("flicker"));
                builder.trail(fw.getBoolean("trail"));
                builder.with(Enums.getIfPresent(FireworkEffect.Type.class, fw.getString("type")).or(FireworkEffect.Type.STAR));

                List<Color> colors = new ArrayList<>();
                for (String colorStr : fw.getStringList("colors")) {
                    colors.add(parseColor(colorStr));
                }
                builder.withColor(colors);

                colors.clear();
                for (String colorStr : fw.getStringList("fade-colors")) {
                    colors.add(parseColor(colorStr));
                }
                builder.withFade(colors);

                firework.addEffect(builder.build());
            }
            //} else if (meta instanceof MapMeta) {
            // TODO This is a little bit too complicated.
            //MapMeta map = (MapMeta) meta;
        } else if (XMaterial.supports(14)) {
            if (meta instanceof CrossbowMeta) {
                CrossbowMeta crossbow = (CrossbowMeta) meta;
                for (String projectiles : config.getConfigurationSection("projectiles").getKeys(false)) {
                    ItemStack projectile = deserialize(player, config.getConfigurationSection("projectiles." + projectiles));
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
            } else if (meta instanceof SuspiciousStewMeta) {
                SuspiciousStewMeta stew = (SuspiciousStewMeta) meta;
                for (String effects : config.getStringList("effects")) {
                    PotionEffect effect = XPotion.parsePotionEffectFromString(effects);
                    stew.addCustomEffect(effect, true);
                }
            }
        }

        // Displayname
        String name = config.getString("name");
        if (name != null) {
            if (name.isEmpty()) name = " ";
            String translated = ChatColor.translateAlternateColorCodes('&', name);
            meta.setDisplayName(translated);
        }

        // Unbreakable
        if (XMaterial.supports(11)) meta.setUnbreakable(config.getBoolean("unbreakable"));

        // Custom Model Data
        if (XMaterial.supports(14)) {
            int modelData = config.getInt("model-data");
            if (modelData != 0) meta.setCustomModelData(modelData);
        }

        // Lore
        List<String> lores = config.getStringList("lore");
        if (!lores.isEmpty()) {
            ArrayList<String> translatedLore = new ArrayList<>();
            String lastColors = "";

            for (String lore : lores) {
                if (lore.isEmpty()) {
                    translatedLore.add(" ");
                    continue;
                }

                for (String singleLore : StringUtils.splitPreserveAllTokens(lore, '\n')) {
                    if (singleLore.isEmpty()) {
                        translatedLore.add(" ");
                        continue;
                    }
                    singleLore = lastColors + ChatColor.translateAlternateColorCodes('&', singleLore);
                    translatedLore.add(singleLore);

                    lastColors = ChatColor.getLastColors(singleLore);
                }
            }

            meta.setLore(translatedLore);
        }

        // Enchantments
        List<String> enchants = config.getStringList("enchants");
        for (String ench : enchants) {
            String[] parseEnchant = StringUtils.split(StringUtils.deleteWhitespace(ench), ',');
            Optional<XEnchantment> enchant = XEnchantment.matchXEnchantment(parseEnchant[0]);
            if (enchant.isPresent()) {
                int lvl = parseEnchant.length > 1 ? Integer.parseInt(parseEnchant[1]) : 1;
                meta.addEnchant(enchant.get().parseEnchantment(), lvl, false);
            }
        }

        // Flags
        List<String> flags = config.getStringList("flags");
        for (String flag : flags) {
            if (flag.equalsIgnoreCase("all")) {
                meta.addItemFlags(ItemFlag.values());
                break;
            }

            ItemFlag itemFlag = ItemFlag.valueOf(flag.toUpperCase());
            meta.addItemFlags(itemFlag);
        }

        // Atrributes
        if (XMaterial.supports(9)) {
            ConfigurationSection attributes = config.getConfigurationSection("attributes");
            if (attributes != null) {
                for (String attribute : attributes.getKeys(false)) {
                    AttributeModifier modifier = new AttributeModifier(
                            UUID.randomUUID(),
                            attributes.getString("generic"),
                            attributes.getInt("amount"),
                            Enums.getIfPresent(AttributeModifier.Operation.class, attributes.getString("operation"))
                                    .or(AttributeModifier.Operation.ADD_NUMBER),
                            Enums.getIfPresent(EquipmentSlot.class, attributes.getString("slot")).or(EquipmentSlot.HAND));

                    meta.addAttributeModifier(Attribute.valueOf(attribute), modifier);
                }
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    public static Color parseColor(String str) {
        if (Strings.isNullOrEmpty(str)) return Color.BLACK;
        String[] rgb = StringUtils.split(StringUtils.deleteWhitespace(str), ',');
        return Color.fromRGB(NumberUtils.toInt(rgb[0], 0), NumberUtils.toInt(rgb[1], 0), NumberUtils.toInt(rgb[1], 0));
    }
}
