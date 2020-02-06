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
package com.cryptomorin.xseries;

import com.google.common.base.Enums;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.Optional;

/**
 * <b>XEntity</b> - YAML Entity Serializer<br>
 * Supports 1.9+
 * Using ConfigurationSection Example:
 * <pre>
 *     ConfigurationSection section = plugin.getConfig().getConfigurationSection("champions.king");
 *     Entity entity = XEntity.spawn(loc, section);
 * </pre>
 * Entity: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Entity.html
 *
 * @author Crypto Morin
 * @version 1.0.0
 * @see XMaterial
 * @see XItemStack
 * @see XPotion
 */
public class XEntity {
    public static Entity spawn(Location location, ConfigurationSection config) {
        String typeStr = config.getString("type");
        if (typeStr == null) return null;

        EntityType type = Enums.getIfPresent(EntityType.class, typeStr.toUpperCase(Locale.ENGLISH)).or(EntityType.ZOMBIE);
        Entity entity = location.getWorld().spawnEntity(location, type);

        String name = config.getString("name");
        if (name != null) {
            entity.setCustomName(ChatColor.translateAlternateColorCodes('&', name));
            entity.setCustomNameVisible(true);
        }

        if (config.isSet("glowing")) entity.setGlowing(config.getBoolean("glowing"));
        if (config.isSet("gravity")) entity.setGravity(config.getBoolean("gravity"));
        if (config.isSet("silent")) entity.setSilent(config.getBoolean("silent"));
        entity.setFireTicks(config.getInt("fire-ticks"));
        entity.setFallDistance(config.getInt("fall-distance"));
        entity.setInvulnerable(config.getBoolean("invulnerable"));

        int live = config.getInt("ticks-lived");
        if (live > 0) entity.setTicksLived(live);

        int portalCooldown = config.getInt("portal-cooldown", -1);
        if (portalCooldown != -1) entity.setPortalCooldown(portalCooldown);
        // We don't need damage cause.

        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            double hp = config.getDouble("health", -1);
            if (hp > -1) living.setHealth(hp);

            living.setAbsorptionAmount(config.getInt("absorption"));
            if (config.isSet("AI")) living.setAI(config.getBoolean("AI"));
            if (config.isSet("can-pickup-items")) living.setCanPickupItems(config.getBoolean("can-pickup-items"));
            if (config.isSet("collidable")) living.setCollidable(config.getBoolean("collidable"));
            if (config.isSet("gliding")) living.setGliding(config.getBoolean("gliding"));
            if (config.isSet("remove-when-far-away")) living.setRemoveWhenFarAway(config.getBoolean("remove-when-far-away"));
            if (config.isSet("swimming")) living.setSwimming(config.getBoolean("swimming"));

            living.setMaximumAir(config.getInt("max-air"));
            living.setNoDamageTicks(config.getInt("do-damage-ticks"));
            living.setRemainingAir(config.getInt("remaining-air"));
            for (String effects : config.getStringList("effects")) {
                living.addPotionEffect(XPotion.parsePotionEffectFromString(effects));
            }

            ConfigurationSection equip = config.getConfigurationSection("equipment");
            if (equip != null) {
                EntityEquipment equipment = living.getEquipment();

                ConfigurationSection helment = equip.getConfigurationSection("helment");
                if (helment != null) {
                    equipment.setHelmet(XItemStack.deserialize(helment.getConfigurationSection("item")));
                    equipment.setHelmetDropChance(helment.getInt("drop-chance"));
                }

                ConfigurationSection chestplate = equip.getConfigurationSection("chestplate");
                if (chestplate != null) {
                    equipment.setChestplate(XItemStack.deserialize(chestplate.getConfigurationSection("item")));
                    equipment.setChestplateDropChance(chestplate.getInt("drop-chance"));
                }

                ConfigurationSection leggings = equip.getConfigurationSection("leggings");
                if (leggings != null) {
                    equipment.setLeggings(XItemStack.deserialize(leggings.getConfigurationSection("item")));
                    equipment.setLeggingsDropChance(leggings.getInt("drop-chance"));
                }

                ConfigurationSection boots = equip.getConfigurationSection("boots");
                if (boots != null) {
                    equipment.setBoots(XItemStack.deserialize(boots.getConfigurationSection("item")));
                    equipment.setBootsDropChance(boots.getInt("drop-chance"));
                }

                ConfigurationSection mainHand = equip.getConfigurationSection("main-hand");
                if (mainHand != null) {
                    equipment.setItemInMainHand(XItemStack.deserialize(mainHand.getConfigurationSection("item")));
                    equipment.setItemInMainHandDropChance(mainHand.getInt("drop-chance"));
                }

                ConfigurationSection offHand = equip.getConfigurationSection("off-hand");
                if (offHand != null) {
                    equipment.setItemInOffHand(XItemStack.deserialize(offHand.getConfigurationSection("item")));
                    equipment.setItemInOffHandDropChance(offHand.getInt("drop-chance"));
                }
            }

            if (living instanceof Ageable) {
                Ageable ageable = (Ageable) living;
                if (config.isSet("breed")) ageable.setBreed(config.getBoolean("breed"));
                if (config.isSet("baby")) {
                    if (config.getBoolean("baby")) ageable.setBaby();
                    else ageable.setAdult();
                }

                int age = config.getInt("age", 0);
                if (age > 0) ageable.setAge(age);

                if (config.isSet("age-lock")) ageable.setAgeLock(config.getBoolean("age-lock"));
            }
            if (living instanceof Tameable) {
                Tameable tam = (Tameable) living;
                tam.setTamed(config.getBoolean("tamed"));
            }
            if (living instanceof Sittable) {
                Sittable sit = (Sittable) living;
                sit.setSitting(config.getBoolean("sitting"));
            }
            if (living instanceof Spellcaster) {
                Spellcaster caster = (Spellcaster) living;
                caster.setSpell(Enums.getIfPresent(Spellcaster.Spell.class, config.getString("spell")).or(Spellcaster.Spell.NONE));
            }
            if (living instanceof ChestedHorse) { // Llamas too
                ChestedHorse chested = (ChestedHorse) living;
                chested.setCarryingChest(config.getBoolean("has-chest"));

                ConfigurationSection items = config.getConfigurationSection("items");
                if (items != null) {
                    for (String key : items.getKeys(false)) {
                        ConfigurationSection itemSec = items.getConfigurationSection(key);
                        int slot = itemSec.getInt("slot", -1);
                        if (slot != -1) {
                            ItemStack item = XItemStack.deserialize(itemSec);
                            if (item != null) chested.getInventory().setItem(slot, item);
                        }
                    }
                }
            }

            if (living instanceof Enderman) {
                Enderman enderman = (Enderman) living;
                String block = config.getString("block");

                if (block != null) {
                    Optional<XMaterial> mat = XMaterial.matchXMaterial(block);
                    if (mat.isPresent()) {
                        ItemStack item = mat.get().parseItem();
                        if (item != null) enderman.setCarriedMaterial(item.getData());
                    }
                }
            } else if (living instanceof Rabbit) {
                Rabbit rabbit = (Rabbit) living;
                rabbit.setRabbitType(Enums.getIfPresent(Rabbit.Type.class, config.getString("rabbit-type")).or(Rabbit.Type.WHITE));
            } else if (living instanceof Bat) {
                Bat bat = (Bat) living;
                if (!config.getBoolean("awake")) bat.setAwake(false);
            } else if (living instanceof Cat) {
                Cat cat = (Cat) living;
                cat.setCatType(Enums.getIfPresent(Cat.Type.class, config.getString("cat-type")).or(Cat.Type.TABBY));
                cat.setCollarColor(Enums.getIfPresent(DyeColor.class, config.getString("color")).or(DyeColor.GREEN));
            } else if (living instanceof Wolf) {
                Wolf wolf = (Wolf) living;
                wolf.setAngry(config.getBoolean("angry"));
                wolf.setCollarColor(Enums.getIfPresent(DyeColor.class, config.getString("color")).or(DyeColor.GREEN));
            } else if (living instanceof Creeper) {
                Creeper creeper = (Creeper) living;
                creeper.setExplosionRadius(config.getInt("explosion-radius"));
                creeper.setMaxFuseTicks(config.getInt("max-fuse-ticks"));
                creeper.setPowered(config.getBoolean("powered"));
            } else if (XMaterial.supports(10)) {

                if (living instanceof Husk) {
                    Husk husk = (Husk) living;
                    husk.setConversionTime(config.getInt("conversion-time"));
                } else if (XMaterial.supports(11)) {

                    if (living instanceof Vex) {
                        Vex vex = (Vex) living;
                        vex.setCharging(config.getBoolean("charging"));
                    } else if (living instanceof Llama) {
                        Llama llama = (Llama) living;
                        llama.setColor(Enums.getIfPresent(Llama.Color.class, config.getString("color")).or(Llama.Color.WHITE));
                        llama.setStrength(config.getInt("strength"));
                    } else if (XMaterial.supports(12)) {

                        if (living instanceof Parrot) {
                            Parrot parrot = (Parrot) living;
                            parrot.setVariant(Enums.getIfPresent(Parrot.Variant.class, config.getString("variant")).or(Parrot.Variant.RED));
                        } else if (XMaterial.isNewVersion()) {

                            if (living instanceof PufferFish) {
                                PufferFish pufferFish = (PufferFish) living;
                                pufferFish.setPuffState(config.getInt("puff-state"));
                            } else if (living instanceof TropicalFish) {
                                TropicalFish tropicalFish = (TropicalFish) living;
                                tropicalFish.setBodyColor(Enums.getIfPresent(DyeColor.class, config.getString("color")).or(DyeColor.WHITE));
                                tropicalFish.setPattern(Enums.getIfPresent(TropicalFish.Pattern.class, config.getString("pattern")).or(TropicalFish.Pattern.BETTY));
                                tropicalFish.setPatternColor(Enums.getIfPresent(DyeColor.class, config.getString("pattern-color")).or(DyeColor.WHITE));
                            } else if (living instanceof EnderDragon) {
                                EnderDragon dragon = (EnderDragon) living;
                                dragon.setPhase(Enums.getIfPresent(EnderDragon.Phase.class, config.getString("phase")).or(EnderDragon.Phase.ROAR_BEFORE_ATTACK));
                            } else if (living instanceof Phantom) {
                                Phantom phantom = (Phantom) living;
                                phantom.setSize(config.getInt("size"));
                            } else if (living instanceof Fox) {
                                Fox fox = (Fox) living;
                                fox.setCrouching(config.getBoolean("crouching"));
                                fox.setSleeping(config.getBoolean("sleeping"));
                                fox.setFoxType(Enums.getIfPresent(Fox.Type.class, config.getString("type")).or(Fox.Type.RED));
                            } else if (XMaterial.supports(14)) {

                                if (living instanceof Panda) {
                                    Panda panda = (Panda) living;
                                    panda.setHiddenGene(Enums.getIfPresent(Panda.Gene.class, config.getString("hidden-gene")).or(Panda.Gene.PLAYFUL));
                                    panda.setMainGene(Enums.getIfPresent(Panda.Gene.class, config.getString("main-gene")).or(Panda.Gene.NORMAL));
                                } else if (living instanceof MushroomCow) {
                                    MushroomCow mooshroom = (MushroomCow) living;
                                    mooshroom.setVariant(Enums.getIfPresent(MushroomCow.Variant.class, config.getString("variant")).or(MushroomCow.Variant.RED));
                                } else if (XMaterial.supports(15)) {

                                    if (living instanceof Bee) {
                                        Bee bee = (Bee) living;
                                        bee.setAnger(config.getInt("anger"));
                                        bee.setHasNectar(config.getBoolean("nectar"));
                                        bee.setHasStung(config.getBoolean("stung"));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (entity instanceof EnderSignal) {
            EnderSignal signal = (EnderSignal) entity;
            signal.setDespawnTimer(config.getInt("despawn-timer"));
            signal.setDropItem(config.getBoolean("drop-item"));
        } else if (entity instanceof ExperienceOrb) {
            ExperienceOrb orb = (ExperienceOrb) entity;
            orb.setExperience(config.getInt("exp"));
        } else if (entity instanceof Explosive) {
            Explosive explosive = (Explosive) entity;
            explosive.setIsIncendiary(config.getBoolean("incendiary"));
        } else if (entity instanceof EnderCrystal) {
            EnderCrystal crystal = (EnderCrystal) entity;
            crystal.setShowingBottom(config.getBoolean("show-bottom"));
        }

        return entity;
    }
}
