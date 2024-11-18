package com.cryptomorin.xseries;

import com.cryptomorin.xseries.base.XModule;
import com.cryptomorin.xseries.base.XRegistry;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public final class XAttribute extends XModule<XAttribute, Attribute> {
    private static final XRegistry<XAttribute, Attribute> REGISTRY =
            new XRegistry<>(Attribute.class, XAttribute.class, () -> Registry.ATTRIBUTE, XAttribute::new, XAttribute[]::new);

    public static final XAttribute
            MAX_HEALTH = std(/* v1.20.3+ */ "max_health", "generic_max_health"),
            FOLLOW_RANGE = std("follow_range"),
            KNOCKBACK_RESISTANCE = std("knockback_resistance"),
            MOVEMENT_SPEED = std("movement_speed"),
            FLYING_SPEED = std("flying_speed"),
            ATTACK_DAMAGE = std("attack_damage"),
            ATTACK_KNOCKBACK = std("attack_knockback"),
            ATTACK_SPEED = std("attack_speed"),
            ARMOR = std("armor"),
            ARMOR_TOUGHNESS = std("armor_toughness"),
            FALL_DAMAGE_MULTIPLIER = std("fall_damage_multiplier"),
            LUCK = std("luck"),
            MAX_ABSORPTION = std("max_absorption"),
            SAFE_FALL_DISTANCE = std("safe_fall_distance"),
            SCALE = std("scale"),
            STEP_HEIGHT = std("step_height"),
            GRAVITY = std("gravity"),
            JUMP_STRENGTH = std("jump_strength"),
            BURNING_TIME = std("burning_time"),
            EXPLOSION_KNOCKBACK_RESISTANCE = std("explosion_knockback_resistance"),
            MOVEMENT_EFFICIENCY = std("movement_efficiency"),
            OXYGEN_BONUS = std("oxygen_bonus"),
            WATER_MOVEMENT_EFFICIENCY = std("water_movement_efficiency"),
            TEMPT_RANGE = std("tempt_range"),
            BLOCK_INTERACTION_RANGE = std("block_interaction_range"),
            ENTITY_INTERACTION_RANGE = std("entity_interaction_range"),
            BLOCK_BREAK_SPEED = std("block_break_speed"),
            MINING_EFFICIENCY = std("mining_efficiency"),
            SNEAKING_SPEED = std("sneaking_speed"),
            SUBMERGED_MINING_SPEED = std("submerged_mining_speed"),
            SWEEPING_DAMAGE_RATIO = std("sweeping_damage_ratio"),
            SPAWN_REINFORCEMENTS = std("spawn_reinforcements");

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

    public AttributeModifier createModifier(@NotNull String key, double amount, @NotNull AttributeModifier.Operation operation, @NotNull EquipmentSlot slot) {
        if (SUPPORTS_MODERN_MODIFIERS) {
            // noinspection UnstableApiUsage
            return new AttributeModifier(NamespacedKey.fromString(key), amount, operation, slot.getGroup());
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

    public static XAttribute[] values() {
        return REGISTRY.values();
    }

    private static XAttribute std(String... names) {
        return REGISTRY.std(names);
    }
}
