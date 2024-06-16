package com.cryptomorin.xseries;

import com.google.common.base.Enums;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public enum XEntityType {
    ALLAY,
    AREA_EFFECT_CLOUD,
    ARMADILLO,
    ARMOR_STAND,
    ARROW,
    AXOLOTL,
    BAT,
    BEE,
    BLAZE,
    BLOCK_DISPLAY,
    BOAT,
    BOGGED,
    BREEZE,
    BREEZE_WIND_CHARGE,
    CAMEL,
    CAT,
    CAVE_SPIDER,
    CHEST_BOAT,
    CHEST_MINECART("MINECART_CHEST"),
    CHICKEN,
    COD,
    COMMAND_BLOCK_MINECART("MINECART_COMMAND"),
    COW,
    CREEPER,
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
    LEASH_KNOT("LEASH_HITCH"),
    LIGHTNING_BOLT("LIGHTNING"),
    LLAMA,
    LLAMA_SPIT,
    MAGMA_CUBE,
    MARKER,
    MINECART,
    MOOSHROOM("MUSHROOM_COW"),
    MULE,
    OCELOT,
    OMINOUS_ITEM_SPAWNER,
    PAINTING,
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

    private static EntityType tryGetEntityType(String entity) {
        try {
            return EntityType.valueOf(entity);
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

    public static XEntityType of(Entity entity) {
        Objects.requireNonNull(entity, "Cannot match entity type from null entity");
        return of(entity.getType());
    }

    public XEntityType or(XEntityType other) {
        return this.isSupported() ? this : other;
    }

    public static XEntityType of(EntityType entityType) {
        Objects.requireNonNull(entityType, "Cannot match null entity type");
        XEntityType mapping = Data.BUKKIT_MAPPING.get(entityType);
        if (mapping != null) return mapping;
        throw new UnsupportedOperationException("Unknown entity type: " + entityType);
    }

    public static XEntityType of(String entityType) {
        Objects.requireNonNull(entityType, "Cannot match null entity type");
        return Data.NAME_MAPPING.get(entityType);
    }

    public EntityType get() {
        return entityType;
    }



    /**
     * Parses the XEntityType with the given name.
     *
     * @param entityType the name of the entity.
     * @return a matched XEntityType.
     * @since 11.0.1
     */
    @Nonnull
    public static Optional<XEntityType> matchXEntityType(@Nonnull String entityType) {
        if (entityType == null || entityType.isEmpty())
            throw new IllegalArgumentException("Cannot match XEntityType of a null or empty entityType name");
        return Optional.ofNullable(XEntityType.Data.NAME_MAPPING.get(entityType));
    }

    /**
     * Parses the XEntityType with the given bukkit entityType.
     *
     * @param entityType the Bukkit entityType.
     * @return a matched entityType.
     * @throws IllegalArgumentException may be thrown as an unexpected exception.
     * @since 11.0.1
     */
    @Nonnull
    public static XEntityType matchXEntityType(@Nonnull EntityType entityType) {
        Objects.requireNonNull(entityType, "Cannot match EntityType of a null entityType");
        return Objects.requireNonNull(XEntityType.Data.NAME_MAPPING.get(entityType.name()), () -> "Unsupported entityType: " + entityType.name());
    }

}
