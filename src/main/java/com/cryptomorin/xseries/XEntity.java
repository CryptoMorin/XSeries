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

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.TreeSpecies;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.Lootable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

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
 * @version 4.0.2
 * @see XMaterial
 * @see XItemStack
 * @see XPotion
 */
public final class XEntity {
    /**
     * A list of entity types that are considered <a href="https://minecraft.wiki/w/Undead">undead</a>.
     *
     * @since 2.0.0
     */
    public static final Set<EntityType> UNDEAD;

    static {
        Set<EntityType> undead = EnumSet.of(
                EntityType.SKELETON, EntityType.ZOMBIE, EntityType.GIANT,
                EntityType.ZOMBIE_VILLAGER, EntityType.WITHER,
                EntityType.WITHER_SKELETON, EntityType.ZOMBIE_HORSE
        );

        if (XMaterial.supports(10)) {
            undead.add(EntityType.HUSK);
            undead.add(EntityType.STRAY);
            if (XMaterial.supports(11)) {
                // Added in v1.6.1 but wasn't available in the API until v1.11
                undead.add(EntityType.SKELETON_HORSE);
                if (XMaterial.supports(13)) {
                    undead.add(EntityType.DROWNED);
                    undead.add(EntityType.PHANTOM);
                    if (XMaterial.supports(16)) {
                        undead.add(EntityType.ZOGLIN);
                        undead.add(EntityType.PIGLIN);
                        undead.add(EntityType.ZOMBIFIED_PIGLIN);
                    }
                }
            }
        }
        if (!XMaterial.supports(16)) undead.add(EntityType.valueOf("PIG_ZOMBIE"));
        UNDEAD = Collections.unmodifiableSet(undead);
    }

    private XEntity() {
    }

    /**
     * Checks if an entity is an <a href="https://minecraft.wiki/w/Undead">undead</a>.
     *
     * @param type the entity type.
     * @return true if the entity is an undead.
     * @since 2.0.0
     */
    public static boolean isUndead(@Nullable EntityType type) {
        return type != null && UNDEAD.contains(type);
    }

    @Nullable
    public static Entity spawn(@Nonnull Location location, @Nonnull ConfigurationSection config) {
        Objects.requireNonNull(location, "Cannot spawn entity at a null location.");
        Objects.requireNonNull(config, "Cannot spawn entity from a null configuration section");

        String typeStr = config.getString("type");
        if (typeStr == null) return null;

        EntityType type = Enums.getIfPresent(EntityType.class, typeStr.toUpperCase(Locale.ENGLISH)).or(EntityType.ZOMBIE);
        return edit(location.getWorld().spawnEntity(location, type), config);
    }

    @SuppressWarnings({"deprecation", "Guava"})
    @Nonnull
    public static Entity edit(@Nonnull Entity entity, @Nonnull ConfigurationSection config) {
        Objects.requireNonNull(entity, "Cannot edit properties of a null entity");
        Objects.requireNonNull(config, "Cannot edit an entity from a null configuration section");

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
        if (config.isSet("invulnerable")) entity.setInvulnerable(config.getBoolean("invulnerable"));

        int live = config.getInt("ticks-lived");
        if (live > 0) entity.setTicksLived(live);

        if (config.isSet("portal-cooldown")) entity.setPortalCooldown(config.getInt("portal-cooldown", -1));
        // We don't need damage cause.

        if (XMaterial.supports(13)) {
            if (entity instanceof Lootable) {
                Lootable lootable = (Lootable) entity;
                long seed = config.getLong("seed");
                if (seed != 0) lootable.setSeed(seed);

                // Needs to be implemented.
//            ConfigurationSection lootTable = config.getConfigurationSection("loot-table");
//            if (lootTable != null) {
//                LootTable table = lootable.getLootTable();
//            }
            }

            if (entity instanceof Boss) {
                Boss boss = (Boss) entity;
                ConfigurationSection bossBarSection = config.getConfigurationSection("bossbar");

                if (bossBarSection != null) {
                    BossBar bossBar = boss.getBossBar();
                    editBossBar(bossBar, bossBarSection);
                }
            }
        }

        if (entity instanceof Vehicle) {
            if (entity instanceof Boat) {
                Boat boat = (Boat) entity;
                String speciesName = config.getString("tree-species");
                if (speciesName != null) {
                    com.google.common.base.Optional<TreeSpecies> species = Enums.getIfPresent(TreeSpecies.class, speciesName);
                    if (species.isPresent()) boat.setWoodType(species.get());
                }
            }
        }

        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            if (config.isSet("health")) {
                double hp = config.getDouble("health");
                living.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp);
                living.setHealth(hp);
            }

            if (XMaterial.supports(14)) living.setAbsorptionAmount(config.getInt("absorption"));
            if (config.isSet("AI")) living.setAI(config.getBoolean("AI"));
            if (config.isSet("can-pickup-items")) living.setCanPickupItems(config.getBoolean("can-pickup-items"));
            if (config.isSet("collidable")) living.setCollidable(config.getBoolean("collidable"));
            if (config.isSet("gliding")) living.setGliding(config.getBoolean("gliding"));
            if (config.isSet("remove-when-far-away"))
                living.setRemoveWhenFarAway(config.getBoolean("remove-when-far-away"));
            if (XMaterial.supports(13) && config.isSet("swimming")) living.setSwimming(config.getBoolean("swimming"));

            if (config.isSet("max-air")) living.setMaximumAir(config.getInt("max-air"));
            if (config.isSet("no-damage-ticks")) living.setNoDamageTicks(config.getInt("no-damage-ticks"));
            if (config.isSet("remaining-air")) living.setRemainingAir(config.getInt("remaining-air"));
            XPotion.addEffects(living, config.getStringList("effects"));

            ConfigurationSection equip = config.getConfigurationSection("equipment");
            if (equip != null) {
                EntityEquipment equipment = living.getEquipment();
                boolean isMob = entity instanceof Mob;

                ConfigurationSection helmet = equip.getConfigurationSection("helmet");
                if (helmet != null) {
                    equipment.setHelmet(XItemStack.deserialize(helmet.getConfigurationSection("item")));
                    if (isMob) {
                        equipment.setHelmetDropChance(helmet.getInt("drop-chance"));
                    }
                }

                ConfigurationSection chestplate = equip.getConfigurationSection("chestplate");
                if (chestplate != null) {
                    equipment.setChestplate(XItemStack.deserialize(chestplate.getConfigurationSection("item")));
                    if (isMob) {
                        equipment.setChestplateDropChance(chestplate.getInt("drop-chance"));
                    }
                }

                ConfigurationSection leggings = equip.getConfigurationSection("leggings");
                if (leggings != null) {
                    equipment.setLeggings(XItemStack.deserialize(leggings.getConfigurationSection("item")));
                    if (isMob) {
                        equipment.setLeggingsDropChance(leggings.getInt("drop-chance"));
                    }
                }

                ConfigurationSection boots = equip.getConfigurationSection("boots");
                if (boots != null) {
                    equipment.setBoots(XItemStack.deserialize(boots.getConfigurationSection("item")));
                    if (isMob) {
                        equipment.setBootsDropChance(boots.getInt("drop-chance"));
                    }
                }

                ConfigurationSection mainHand = equip.getConfigurationSection("main-hand");
                if (mainHand != null) {
                    equipment.setItemInMainHand(XItemStack.deserialize(mainHand.getConfigurationSection("item")));
                    if (isMob) {
                        equipment.setItemInMainHandDropChance(mainHand.getInt("drop-chance"));
                    }
                }

                ConfigurationSection offHand = equip.getConfigurationSection("off-hand");
                if (offHand != null) {
                    equipment.setItemInOffHand(XItemStack.deserialize(offHand.getConfigurationSection("item")));
                    if (isMob) {
                        equipment.setItemInOffHandDropChance(offHand.getInt("drop-chance"));
                    }
                }
            }

            if (living instanceof Ageable) { // and Breedable
                Ageable ageable = (Ageable) living;
                if (config.isSet("breed")) ageable.setBreed(config.getBoolean("breed"));
                if (config.isSet("baby")) {
                    if (config.getBoolean("baby")) ageable.setBaby();
                    else ageable.setAdult();
                }

                int age = config.getInt("age", 0);
                if (age > 0) ageable.setAge(age);

                if (config.isSet("age-lock")) ageable.setAgeLock(config.getBoolean("age-lock"));

                if (living instanceof Animals) {
                    Animals animals = (Animals) living;
                    int loveModeTicks = config.getInt("love-mode");
                    if (loveModeTicks != 0) animals.setLoveModeTicks(loveModeTicks);

                    if (living instanceof Tameable) {
                        Tameable tam = (Tameable) living;
                        tam.setTamed(config.getBoolean("tamed"));
                    }
                }
            }
            if (living instanceof Sittable) {
                Sittable sit = (Sittable) living;
                sit.setSitting(config.getBoolean("sitting"));
            }
            if (living instanceof Spellcaster) {
                Spellcaster caster = (Spellcaster) living;
                String spell = config.getString("spell");
                if (spell != null)
                    caster.setSpell(Enums.getIfPresent(Spellcaster.Spell.class, spell).or(Spellcaster.Spell.NONE));
            }
            if (living instanceof AbstractHorse) {
                AbstractHorse horse = (AbstractHorse) living;
                if (config.isSet("domestication")) horse.setDomestication(config.getInt("domestication"));
                if (config.isSet("jump-strength")) horse.setJumpStrength(config.getDouble("jump-strength"));
                if (config.isSet("max-domestication")) horse.setMaxDomestication(config.getInt("max-domestication"));

                ConfigurationSection items = config.getConfigurationSection("items");
                if (items != null) {
                    Inventory inventory = horse.getInventory();
                    for (String key : items.getKeys(false)) {
                        ConfigurationSection itemSec = items.getConfigurationSection(key);
                        int slot = itemSec.getInt("slot", -1);
                        if (slot != -1) {
                            ItemStack item = XItemStack.deserialize(itemSec);
                            if (item != null) inventory.setItem(slot, item);
                        }
                    }
                }

                if (living instanceof ChestedHorse) { // Llamas too
                    ChestedHorse chested = (ChestedHorse) living;
                    boolean hasChest = config.getBoolean("has-chest");
                    if (hasChest) chested.setCarryingChest(true);
                }
            }

            if (living instanceof Enderman) {
                Enderman enderman = (Enderman) living;
                String block = config.getString("carrying");

                if (block != null) {
                    Optional<XMaterial> mat = XMaterial.matchXMaterial(block);
                    if (mat.isPresent()) {
                        ItemStack item = mat.get().parseItem();
                        if (item != null) enderman.setCarriedMaterial(item.getData());
                    }
                }
            } else if (living instanceof Sheep) {
                Sheep sheep = (Sheep) living;
                boolean sheared = config.getBoolean("sheared");
                if (sheared) sheep.setSheared(true);
            } else if (living instanceof Rabbit) {
                Rabbit rabbit = (Rabbit) living;
                rabbit.setRabbitType(Enums.getIfPresent(Rabbit.Type.class, config.getString("color")).or(Rabbit.Type.WHITE));
            } else if (living instanceof Bat) {
                Bat bat = (Bat) living;
                if (!config.getBoolean("awake")) bat.setAwake(false);
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
                if (XMaterial.supports(11)) {
                    if (living instanceof Llama) {
                        Llama llama = (Llama) living;
                        if (config.isSet("strength")) llama.setStrength(config.getInt("strength"));
                        com.google.common.base.Optional<Llama.Color> color = Enums.getIfPresent(Llama.Color.class, config.getString("color"));
                        if (color.isPresent()) llama.setColor(color.get());
                    } else if (XMaterial.supports(12)) {
                        if (living instanceof Parrot) {
                            Parrot parrot = (Parrot) living;
                            parrot.setVariant(Enums.getIfPresent(Parrot.Variant.class, config.getString("color")).or(Parrot.Variant.RED));
                        }

                        if (XMaterial.supports(13)) thirteen(entity, config);
                        if (XMaterial.supports(14)) fourteen(entity, config);
                        if (XMaterial.supports(15)) {
                            if (living instanceof Bee) {
                                Bee bee = (Bee) living;
                                // Anger time ticks.
                                bee.setAnger(config.getInt("anger") * 20);
                                bee.setHasNectar(config.getBoolean("nectar"));
                                bee.setHasStung(config.getBoolean("stung"));
                                bee.setCannotEnterHiveTicks(config.getInt("disallow-hive") * 20);
                            }

                            if (XMaterial.supports(16)) sixteen(entity, config);
                            if (XMaterial.supports(17)) seventeen(entity, config);
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

    private static void fourteen(Entity entity, ConfigurationSection config) {
        if (entity instanceof Raider) {
            // Illagers were added in 1.11 but the concept of raids and patrols were added in 1.14
            Raider raider = (Raider) entity;
            if (config.isSet("can-join-raid")) raider.setCanJoinRaid(config.getBoolean("can-join-raid"));
            if (config.isSet("is-patrol-leader")) raider.setCanJoinRaid(config.getBoolean("is-patrol-leader"));
        } else if (entity instanceof Cat) {
            Cat cat = (Cat) entity;
            cat.setCatType(Enums.getIfPresent(Cat.Type.class, config.getString("variant")).or(Cat.Type.TABBY));
            cat.setCollarColor(Enums.getIfPresent(DyeColor.class, config.getString("color")).or(DyeColor.GREEN));
        } else if (entity instanceof Fox) {
            Fox fox = (Fox) entity;
            fox.setCrouching(config.getBoolean("crouching"));
            fox.setSleeping(config.getBoolean("sleeping"));
            fox.setFoxType(Enums.getIfPresent(Fox.Type.class, config.getString("color")).or(Fox.Type.RED));
        } else if (entity instanceof Panda) {
            Panda panda = (Panda) entity;
            panda.setHiddenGene(Enums.getIfPresent(Panda.Gene.class, config.getString("hidden-gene")).or(Panda.Gene.PLAYFUL));
            panda.setMainGene(Enums.getIfPresent(Panda.Gene.class, config.getString("main-gene")).or(Panda.Gene.NORMAL));
        } else if (entity instanceof MushroomCow) {
            MushroomCow mooshroom = (MushroomCow) entity;
            mooshroom.setVariant(Enums.getIfPresent(MushroomCow.Variant.class, config.getString("color")).or(MushroomCow.Variant.RED));
        }
    }

    private static void thirteen(Entity entity, ConfigurationSection config) {
        if (entity instanceof Husk) {
            Husk husk = (Husk) entity;
            husk.setConversionTime(config.getInt("conversion-time"));
        } else if (entity instanceof Vex) {
            Vex vex = (Vex) entity;
            vex.setCharging(config.getBoolean("charging"));
        } else if (entity instanceof PufferFish) {
            PufferFish pufferFish = (PufferFish) entity;
            pufferFish.setPuffState(config.getInt("puff-state"));
        } else if (entity instanceof TropicalFish) {
            TropicalFish tropicalFish = (TropicalFish) entity;
            tropicalFish.setBodyColor(Enums.getIfPresent(DyeColor.class, config.getString("color")).or(DyeColor.WHITE));
            tropicalFish.setPattern(Enums.getIfPresent(TropicalFish.Pattern.class, config.getString("pattern")).or(TropicalFish.Pattern.BETTY));
            tropicalFish.setPatternColor(Enums.getIfPresent(DyeColor.class, config.getString("pattern-color")).or(DyeColor.WHITE));
        } else if (entity instanceof EnderDragon) {
            EnderDragon dragon = (EnderDragon) entity;
            dragon.setPhase(Enums.getIfPresent(EnderDragon.Phase.class, config.getString("phase")).or(EnderDragon.Phase.ROAR_BEFORE_ATTACK));
        } else if (entity instanceof Phantom) {
            Phantom phantom = (Phantom) entity;
            phantom.setSize(config.getInt("size"));
        }
    }

    private static void sixteen(Entity entity, ConfigurationSection config) {
        if (entity instanceof Hoglin) {
            Hoglin hoglin = (Hoglin) entity;
            hoglin.setConversionTime(config.getInt("conversation") * 20);
            hoglin.setImmuneToZombification(config.getBoolean("zombification-immunity"));
            hoglin.setIsAbleToBeHunted(config.getBoolean("can-be-hunted"));
        } else if (entity instanceof Piglin) {
            // Idk why Spigot did this...
            Piglin piglin = (Piglin) entity;
            piglin.setConversionTime(config.getInt("conversation") * 20);
            piglin.setImmuneToZombification(config.getBoolean("zombification-immunity"));
        } else if (entity instanceof Strider) {
            Strider strider = (Strider) entity;
            strider.setShivering(config.getBoolean("shivering"));
        }
    }

    /**
     * AXOLOTL - GLOW_ITEM_FRAME - GLOW_SQUID - GOAT - MARKER
     */
    private static boolean seventeen(Entity entity, ConfigurationSection config) {
        if (entity instanceof Axolotl) {
            Axolotl axolotl = (Axolotl) entity;
            String variantStr = config.getString("variant");
            if (Strings.isNullOrEmpty(variantStr)) {
                com.google.common.base.Optional<Axolotl.Variant> variant = Enums.getIfPresent(Axolotl.Variant.class, variantStr);
                if (variant.isPresent()) axolotl.setVariant(variant.get());
            }

            if (config.isSet("playing-dead")) axolotl.setPlayingDead(config.getBoolean("playing-dead"));
            return true;
        }

        if (entity instanceof Goat) {
            Goat goat = (Goat) entity;
            if (config.isSet("screaming")) goat.setScreaming(config.getBoolean("screaming"));
            return true;
        }

        if (entity instanceof GlowSquid) {
            GlowSquid glowSquid = (GlowSquid) entity;
            if (config.isSet("dark-ticks-remaining"))
                glowSquid.setDarkTicksRemaining(config.getInt("dark-ticks-remaining"));
            return true;
        }

        return false;
    }

    /**
     * Edits an existing BossBar from the config.
     *
     * @param bossBar the created bossbar.
     * @param section the config section to edit the bossbar from.
     * @since 3.0.0
     */
    @SuppressWarnings("Guava")
    public static void editBossBar(BossBar bossBar, ConfigurationSection section) {
        String title = section.getString("title");
        if (title != null) bossBar.setTitle(ChatColor.translateAlternateColorCodes('&', title));

        String colorStr = section.getString("color");
        if (colorStr != null) {
            com.google.common.base.Optional<BarColor> color = Enums.getIfPresent(BarColor.class, colorStr.toUpperCase(Locale.ENGLISH));
            if (color.isPresent()) bossBar.setColor(color.get());
        }

        String styleStr = section.getString("style");
        if (styleStr != null) {
            com.google.common.base.Optional<BarStyle> style = Enums.getIfPresent(BarStyle.class, styleStr.toUpperCase(Locale.ENGLISH));
            if (style.isPresent()) bossBar.setStyle(style.get());
        }

        List<String> flagList = section.getStringList("flags");
        if (!flagList.isEmpty()) {
            Set<BarFlag> flags = EnumSet.noneOf(BarFlag.class);
            for (String flagName : flagList) {
                BarFlag flag = Enums.getIfPresent(BarFlag.class, flagName.toUpperCase(Locale.ENGLISH)).orNull();
                if (flag != null) flags.add(flag);
            }

            for (BarFlag flag : BarFlag.values()) {
                if (flags.contains(flag)) bossBar.addFlag(flag);
                else bossBar.removeFlag(flag);
            }
        }
    }
}
