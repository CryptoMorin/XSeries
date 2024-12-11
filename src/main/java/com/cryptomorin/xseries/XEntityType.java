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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.*;

public enum XEntityType {
    ACACIA_BOAT,
    ACACIA_CHEST_BOAT,
    ALLAY,
    AREA_EFFECT_CLOUD,
    ARMADILLO,
    ARMOR_STAND,
    ARROW,
    AXOLOTL,
    BAMBOO_RAFT,
    BAMBOO_CHEST_RAFT,
    BAT,
    BEE,
    BIRCH_BOAT,
    BIRCH_CHEST_BOAT,
    BLAZE,
    BLOCK_DISPLAY,
    BOGGED,
    BREEZE,
    BREEZE_WIND_CHARGE,
    CAMEL,
    CAT,
    CAVE_SPIDER,
    CHERRY_BOAT,
    CHERRY_CHEST_BOAT,
    CHEST_MINECART("MINECART_CHEST"),
    CHICKEN,
    COD,
    COMMAND_BLOCK_MINECART("MINECART_COMMAND"),
    COW,
    CREAKING,
    CREAKING_TRANSIENT,
    CREEPER,
    DARK_OAK_BOAT,
    DARK_OAK_CHEST_BOAT,
    DOLPHIN,
    DONKEY,
    DRAGON_FIREBALL,
    DROWNED,
    EGG,
    ELDER_GUARDIAN,
    ENDERMAN,
    ENDERMITE,
    ENDER_DRAGON,
    ENDER_PEARL,
    END_CRYSTAL("ENDER_CRYSTAL"),
    EVOKER,
    EVOKER_FANGS,
    EXPERIENCE_BOTTLE("THROWN_EXP_BOTTLE"),
    EXPERIENCE_ORB,
    EYE_OF_ENDER("ENDER_SIGNAL"),
    FALLING_BLOCK,
    FIREBALL,
    FIREWORK_ROCKET("FIREWORK"),
    FISHING_BOBBER("FISHING_HOOK"),
    FOX,
    FROG,
    FURNACE_MINECART,
    GHAST,
    GIANT,
    GLOW_ITEM_FRAME,
    GLOW_SQUID,
    GOAT,
    GUARDIAN,
    HOGLIN,
    HOPPER_MINECART("MINECART_HOPPER"),
    HORSE,
    HUSK,
    ILLUSIONER,
    INTERACTION,
    IRON_GOLEM,
    ITEM("DROPPED_ITEM"),
    ITEM_DISPLAY,
    ITEM_FRAME,
    JUNGLE_BOAT,
    JUNGLE_CHEST_BOAT,
    LEASH_KNOT("LEASH_HITCH"),
    LIGHTNING_BOLT("LIGHTNING"),
    LLAMA,
    LLAMA_SPIT,
    MAGMA_CUBE,
    MANGROVE_BOAT,
    MANGROVE_CHEST_BOAT,
    MARKER,
    MINECART,
    MOOSHROOM("MUSHROOM_COW"),
    MULE,
    /**
     * BOAT -> OAK_BOAT (v1.21.2)
     */
    OAK_BOAT("BOAT"),
    /**
     * CHEST_BOAT -> OAK_CHEST_BOAT (v1.21.2)
     */
    OAK_CHEST_BOAT("CHEST_BOAT"),
    OCELOT,
    OMINOUS_ITEM_SPAWNER,
    PAINTING,
    PALE_OAK_BOAT,
    PALE_OAK_CHEST_BOAT,
    PANDA,
    PARROT,
    PHANTOM,
    PIG,
    PIGLIN,
    PIGLIN_BRUTE,
    PILLAGER,
    PLAYER,
    POLAR_BEAR,
    POTION("SPLASH_POTION"),
    PUFFERFISH,
    RABBIT,
    RAVAGER,
    SALMON,
    SHEEP,
    SHULKER,
    SHULKER_BULLET,
    SILVERFISH,
    SKELETON,
    SKELETON_HORSE,
    SLIME,
    SMALL_FIREBALL,
    SNIFFER,
    SNOWBALL,
    SNOW_GOLEM("SNOWMAN"),
    SPAWNER_MINECART("MINECART_MOB_SPAWNER"),
    SPECTRAL_ARROW,
    SPIDER,
    SPRUCE_BOAT,
    SPRUCE_CHEST_BOAT,
    SQUID,
    STRAY,
    STRIDER,
    TADPOLE,
    TEXT_DISPLAY,
    TNT("PRIMED_TNT"),
    TNT_MINECART("MINECART_TNT"),
    TRADER_LLAMA,
    TRIDENT,
    TROPICAL_FISH,
    TURTLE,
    UNKNOWN,
    VEX,
    VILLAGER,
    VINDICATOR,
    WANDERING_TRADER,
    WARDEN,
    WIND_CHARGE,
    WITCH,
    WITHER,
    WITHER_SKELETON,
    WITHER_SKULL,
    WOLF,
    ZOGLIN,
    ZOMBIE,
    ZOMBIE_HORSE,
    ZOMBIE_VILLAGER,
    ZOMBIFIED_PIGLIN;

    private final EntityType entityType;

    XEntityType(String... alts) {
        EntityType entityType = Enums.getIfPresent(EntityType.class, this.name()).orNull();
        Data.NAME_MAPPING.put(this.name(), this);

        for (String alt : alts) {
            if (entityType == null) entityType = tryGetEntityType(alt);
            Data.NAME_MAPPING.put(alt, this);
        }

        this.entityType = entityType;
        if (entityType != null) Data.BUKKIT_MAPPING.put(entityType, this);
    }

    private static EntityType tryGetEntityType(String particle) {
        try {
            return EntityType.valueOf(particle);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static final class Data {
        private static final Map<String, XEntityType> NAME_MAPPING = new HashMap<>();
        private static final Map<EntityType, XEntityType> BUKKIT_MAPPING = new EnumMap<>(EntityType.class);
    }

    public boolean isSupported() {
        return entityType != null;
    }

    public XEntityType or(XEntityType other) {
        return this.isSupported() ? this : other;
    }

    public static XEntityType of(Entity entity) {
        Objects.requireNonNull(entity, "Cannot match entity type from null entity");
        return of(entity.getType());
    }

    public static XEntityType of(EntityType entityType) {
        Objects.requireNonNull(entityType, "Cannot match null entity type");
        XEntityType mapping = Data.BUKKIT_MAPPING.get(entityType);
        if (mapping != null) return mapping;
        throw new UnsupportedOperationException("Unknown entity type: " + entityType);
    }

    public static Optional<XEntityType> of(String entityType) {
        Objects.requireNonNull(entityType, "Cannot match null entity type");
        return Optional.ofNullable(Data.NAME_MAPPING.get(entityType));
    }

    public EntityType get() {
        return entityType;
    }
}
