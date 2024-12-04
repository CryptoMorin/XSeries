package com.cryptomorin.xseries;

import com.cryptomorin.xseries.base.XModule;
import com.cryptomorin.xseries.base.XRegistry;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class XAttribute extends XModule<XAttribute, Attribute> {
    public static final XRegistry<XAttribute, Attribute> REGISTRY =
            new XRegistry<>(Attribute.class, XAttribute.class, () -> Registry.ATTRIBUTE, XAttribute::new, XAttribute[]::new);

    public static final XAttribute
            MAX_HEALTH = std(/* v1.20.3+ */ "max_health", "GENERIC_MAX_HEALTH"),
            FOLLOW_RANGE = std(/* v1.20.3+ */ "follow_range", "GENERIC_FOLLOW_RANGE"),
            KNOCKBACK_RESISTANCE = std(/* v1.20.3+ */ "knockback_resistance", "GENERIC_KNOCKBACK_RESISTANCE"),
            MOVEMENT_SPEED = std(/* v1.20.3+ */ "movement_speed", "GENERIC_MOVEMENT_SPEED"),
            FLYING_SPEED = std(/* v1.20.3+ */ "flying_speed", "GENERIC_FLYING_SPEED"),
            ATTACK_DAMAGE = std(/* v1.20.3+ */ "attack_damage", "GENERIC_ATTACK_DAMAGE"),
            ATTACK_KNOCKBACK = std(/* v1.20.3+ */ "attack_knockback", "GENERIC_ATTACK_KNOCKBACK"),
            ATTACK_SPEED = std(/* v1.20.3+ */ "attack_speed", "GENERIC_ATTACK_SPEED"),
            ARMOR = std(/* v1.20.3+ */ "armor", "GENERIC_ARMOR"),
            ARMOR_TOUGHNESS = std(/* v1.20.3+ */ "armor_toughness", "GENERIC_ARMOR_TOUGHNESS"),
            FALL_DAMAGE_MULTIPLIER = std(/* v1.20.3+ */ "fall_damage_multiplier", "GENERIC_FALL_DAMAGE_MULTIPLIER"),
            LUCK = std(/* v1.20.3+ */ "luck", "GENERIC_LUCK"),
            MAX_ABSORPTION = std(/* v1.20.3+ */ "max_absorption", "GENERIC_MAX_ABSORPTION"),
            SAFE_FALL_DISTANCE = std(/* v1.20.3+ */ "safe_fall_distance", "GENERIC_SAFE_FALL_DISTANCE"),
            SCALE = std(/* v1.20.3+ */ "scale", "GENERIC_SCALE"),
            STEP_HEIGHT = std(/* v1.20.3+ */ "step_height", "GENERIC_STEP_HEIGHT"),
            GRAVITY = std(/* v1.20.3+ */ "gravity", "GENERIC_GRAVITY"),
            JUMP_STRENGTH = std(/* v1.20.3+ */ "jump_strength", "GENERIC_JUMP_STRENGTH"),
            BURNING_TIME = std(/* v1.20.3+ */ "burning_time", "GENERIC_BURNING_TIME"),
            EXPLOSION_KNOCKBACK_RESISTANCE = std(/* v1.20.3+ */ "explosion_knockback_resistance", "GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE"),
            MOVEMENT_EFFICIENCY = std(/* v1.20.3+ */ "movement_efficiency", "GENERIC_MOVEMENT_EFFICIENCY"),
            OXYGEN_BONUS = std(/* v1.20.3+ */ "oxygen_bonus", "GENERIC_OXYGEN_BONUS"),
            WATER_MOVEMENT_EFFICIENCY = std(/* v1.20.3+ */ "water_movement_efficiency", "GENERIC_WATER_MOVEMENT_EFFICIENCY"),
            TEMPT_RANGE = std(/* v1.20.3+ */ "tempt_range", "GENERIC_TEMPT_RANGE"),
            BLOCK_INTERACTION_RANGE = std(/* v1.20.3+ */ "block_interaction_range", "PLAYER_BLOCK_INTERACTION_RANGE"),
            ENTITY_INTERACTION_RANGE = std(/* v1.20.3+ */ "entity_interaction_range", "PLAYER_ENTITY_INTERACTION_RANGE"),
            BLOCK_BREAK_SPEED = std(/* v1.20.3+ */ "block_break_speed", "PLAYER_BLOCK_BREAK_SPEED"),
            MINING_EFFICIENCY = std(/* v1.20.3+ */ "mining_efficiency", "PLAYER_MINING_EFFICIENCY"),
            SNEAKING_SPEED = std(/* v1.20.3+ */ "sneaking_speed", "PLAYER_SNEAKING_SPEED"),
            SUBMERGED_MINING_SPEED = std(/* v1.20.3+ */ "submerged_mining_speed", "PLAYER_SUBMERGED_MINING_SPEED"),
            SWEEPING_DAMAGE_RATIO = std(/* v1.20.3+ */ "sweeping_damage_ratio", "PLAYER_SWEEPING_DAMAGE_RATIO"),
            SPAWN_REINFORCEMENTS = std(/* v1.20.3+ */ "spawn_reinforcements", "ZOMBIE_SPAWN_REINFORCEMENTS");

    private static final boolean SUPPORTS_MODERN_MODIFIERS;

    static {
        // public AttributeModifier(NamespacedKey key, double amount, AttributeModifier.Operation operation, EquipmentSlotGroup slot)
        boolean supportsModernModifiers = false;
        try {
            // noinspection UnstableApiUsage
            AttributeModifier.class.getConstructor(
                    org.bukkit.NamespacedKey.class,
                    double.class,
                    AttributeModifier.Operation.class,
                    org.bukkit.inventory.EquipmentSlotGroup.class
            );
            supportsModernModifiers = true;
        } catch (NoSuchMethodException | NoClassDefFoundError ignored) {
        }

        SUPPORTS_MODERN_MODIFIERS = supportsModernModifiers;
    }

    private XAttribute(Attribute attribute, String[] names) {
        super(attribute, names);
    }

    /**
     * @param slot when null, defaults to {@link org.bukkit.inventory.EquipmentSlotGroup#ANY}
     */
    public AttributeModifier createModifier(@NotNull String key, double amount, @NotNull AttributeModifier.Operation operation, @Nullable EquipmentSlot slot) {
        Objects.requireNonNull(key, "Key is null");
        Objects.requireNonNull(operation, "Operation is null");

        if (SUPPORTS_MODERN_MODIFIERS) {
            NamespacedKey ns = Objects.requireNonNull(NamespacedKey.fromString(key), () -> "Invalid namespace: " + key);
            // noinspection UnstableApiUsage
            return new AttributeModifier(ns, amount, operation, (slot == null ? EquipmentSlotGroup.ANY : slot.getGroup()));
        } else {
            // noinspection removal
            return new AttributeModifier(UUID.randomUUID(), key, amount, operation, slot);
        }
    }


    public static XAttribute of(Attribute attribute) {
        return REGISTRY.getByBukkitForm(attribute);
    }

    public static Optional<XAttribute> of(String attribute) {
        return REGISTRY.getByName(attribute);
    }

    /**
     * Use {@link #getValues()} instead.
     */
    @Deprecated
    public static XAttribute[] values() {
        return REGISTRY.values();
    }

    @NotNull
    @Unmodifiable
    public static Collection<XAttribute> getValues() {
        return REGISTRY.getValues();
    }

    private static XAttribute std(String... names) {
        return REGISTRY.std(names);
    }
}
