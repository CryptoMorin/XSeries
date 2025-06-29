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

import com.cryptomorin.xseries.base.XBase;
import com.cryptomorin.xseries.base.XRegistry;
import com.cryptomorin.xseries.base.annotations.XChange;
import com.cryptomorin.xseries.base.annotations.XInfo;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Corresponds to {@link EntityType}
 *
 * @version 1.0.1
 * @see XEntity
 */
public enum XEntityType implements XBase<XEntityType, EntityType> {
    ACACIA_BOAT,
    ACACIA_CHEST_BOAT,
    ALLAY,
    AREA_EFFECT_CLOUD,
    ARMADILLO,
    ARMOR_STAND,
    ARROW,
    AXOLOTL,
    BAMBOO_CHEST_RAFT,
    BAMBOO_RAFT,
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

    @XInfo(since = "1.21.3", removedSince = "1.21.4")
    @Deprecated
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
    @XInfo(since = "1.21.6")
    HAPPY_GHAST,
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
    @XInfo(since = "1.21.5")
    LINGERING_POTION,
    LLAMA,
    LLAMA_SPIT,
    MAGMA_CUBE,
    MANGROVE_BOAT,
    MANGROVE_CHEST_BOAT,
    MARKER,
    MINECART,
    MOOSHROOM("MUSHROOM_COW"),
    MULE,
    @XChange(version = "v1.21.2", from = "BOAT", to = "OAK_BOAT")
    OAK_BOAT("BOAT"),
    @XChange(version = "v1.21.2", from = "CHEST_BOAT", to = "OAK_CHEST_BOAT")
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
    @XInfo(since = "1.21.5")
    @XChange(version = "1.21.5", from = "POTION", to = "SPLASH_POTION")
    SPLASH_POTION("POTION"),
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

    public static final XRegistry<XEntityType, EntityType> REGISTRY = Data.REGISTRY;

    private static final class Data {
        public static final XRegistry<XEntityType, EntityType> REGISTRY =
                new XRegistry<>(EntityType.class, XEntityType.class, XEntityType[]::new);
    }

    private final EntityType entityType;

    XEntityType(String... names) {
        this.entityType = Data.REGISTRY.stdEnum(this, names);
    }

    static {
        REGISTRY.discardMetadata();
    }

    @NotNull
    @Unmodifiable
    public static Collection<XEntityType> getValues() {
        return REGISTRY.getValues();
    }

    @NotNull
    public static XEntityType of(@NotNull Entity entity) {
        Objects.requireNonNull(entity, "Cannot match entity type from null entity");
        return of(entity.getType());
    }

    @NotNull
    public static XEntityType of(@NotNull EntityType entityType) {
        return REGISTRY.getByBukkitForm(entityType);
    }

    public static Optional<XEntityType> of(@NotNull String entityType) {
        return REGISTRY.getByName(entityType);
    }

    @Override
    public String[] getNames() {
        return new String[]{name()};
    }

    @Override
    public EntityType get() {
        return entityType;
    }
}
