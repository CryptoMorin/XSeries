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
            MAX_HEALTH = std(/* v1.20.3+ */ "max_health", "generic.max_health"),
            FOLLOW_RANGE = std(/* v1.20.3+ */ "follow_range", "generic.follow_range"),
            KNOCKBACK_RESISTANCE = std(/* v1.20.3+ */ "knockback_resistance", "generic.knockback_resistance"),
            MOVEMENT_SPEED = std(/* v1.20.3+ */ "movement_speed", "generic.movement_speed"),
            FLYING_SPEED = std(/* v1.20.3+ */ "flying_speed", "generic.flying_speed"),
            ATTACK_DAMAGE = std(/* v1.20.3+ */ "attack_damage", "generic.attack_damage"),
            ATTACK_KNOCKBACK = std(/* v1.20.3+ */ "attack_knockback", "generic.attack_knockback"),
            ATTACK_SPEED = std(/* v1.20.3+ */ "attack_speed", "generic.attack_speed"),
            ARMOR = std(/* v1.20.3+ */ "armor", "generic.armor"),
            ARMOR_TOUGHNESS = std(/* v1.20.3+ */ "armor_toughness", "generic.armor_toughness"),
            FALL_DAMAGE_MULTIPLIER = std(/* v1.20.3+ */ "fall_damage_multiplier", "generic.fall_damage_multiplier"),
            LUCK = std(/* v1.20.3+ */ "luck", "generic.luck"),
            MAX_ABSORPTION = std(/* v1.20.3+ */ "max_absorption", "generic.max_absorption"),
            SAFE_FALL_DISTANCE = std(/* v1.20.3+ */ "safe_fall_distance", "generic.safe_fall_distance"),
            SCALE = std(/* v1.20.3+ */ "scale", "generic.scale"),
            STEP_HEIGHT = std(/* v1.20.3+ */ "step_height", "generic.step_height"),
            GRAVITY = std(/* v1.20.3+ */ "gravity", "generic.gravity"),
            JUMP_STRENGTH = std(/* v1.20.3+ */ "jump_strength", "generic.jump_strength", /* 1.13+? */ "horse.jump_strength"),
            BURNING_TIME = std(/* v1.20.3+ */ "burning_time", "generic.burning_time"),
            EXPLOSION_KNOCKBACK_RESISTANCE = std(/* v1.20.3+ */ "explosion_knockback_resistance", "generic.explosion_knockback_resistance"),
            MOVEMENT_EFFICIENCY = std(/* v1.20.3+ */ "movement_efficiency", "generic.movement_efficiency"),
            OXYGEN_BONUS = std(/* v1.20.3+ */ "oxygen_bonus", "generic.oxygen_bonus"),
            WATER_MOVEMENT_EFFICIENCY = std(/* v1.20.3+ */ "water_movement_efficiency", "generic.water_movement_efficiency"),
            TEMPT_RANGE = std(/* v1.20.3+ */ "tempt_range", "generic.tempt_range"),
            BLOCK_INTERACTION_RANGE = std(/* v1.20.3+ */ "block_interaction_range", "player.block_interaction_range"),
            ENTITY_INTERACTION_RANGE = std(/* v1.20.3+ */ "entity_interaction_range", "player.entity_interaction_range"),
            BLOCK_BREAK_SPEED = std(/* v1.20.3+ */ "block_break_speed", "player.block_break_speed"),
            MINING_EFFICIENCY = std(/* v1.20.3+ */ "mining_efficiency", "player.mining_efficiency"),
            SNEAKING_SPEED = std(/* v1.20.3+ */ "sneaking_speed", "player.sneaking_speed"),
            SUBMERGED_MINING_SPEED = std(/* v1.20.3+ */ "submerged_mining_speed", "player.submerged_mining_speed"),
            SWEEPING_DAMAGE_RATIO = std(/* v1.20.3+ */ "sweeping_damage_ratio", "player.sweeping_damage_ratio"),
            SPAWN_REINFORCEMENTS = std(/* v1.20.3+ */ "spawn_reinforcements", "zombie.spawn_reinforcements");

    private static final boolean SUPPORTS_MODERN_MODIFIERS;

    static {
        // public AttributeModifier(NamespacedKey key, double amount, AttributeModifier.Operation operation, EquipmentSlotGroup slot)
        boolean supportsModernModifiers;
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
            supportsModernModifiers = false;
        }

        SUPPORTS_MODERN_MODIFIERS = supportsModernModifiers;
    }

    private XAttribute(Attribute attribute, String[] names) {
        super(attribute, names);
    }

    /**
     * Creates a new {@link AttributeModifier}.
     *
     * @param slot when null, defaults to {@link org.bukkit.inventory.EquipmentSlotGroup#ANY}
     */
    public static AttributeModifier createModifier(@NotNull String key, double amount, @NotNull AttributeModifier.Operation operation, @Nullable EquipmentSlot slot) {
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
     * @deprecated Use {@link #getValues()} instead.
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
