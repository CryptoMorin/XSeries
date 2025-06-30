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
import com.cryptomorin.xseries.base.annotations.XChange;
import com.cryptomorin.xseries.base.annotations.XInfo;
import com.cryptomorin.xseries.base.annotations.XMerge;
import com.google.common.base.Enums;
import com.google.common.base.Strings;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * <b>XSound</b> - Universal Minecraft Sound Support<br>
 * 1.13 and above as priority.
 * <p>
 * Sounds are thread-safe. But this doesn't mean you should
 * use a bukkit async scheduler for every {@link Player#playSound} call.
 * Paper for some reason blocks async calls for playing sounds in a world.
 * <p>
 * <b>Volume:</b> 0.0-∞ - 1.0f (normal) - Using higher values increase the distance from which the sound can be heard.<br>
 * <b>Pitch:</b> 0.5-2.0 - 1.0f (normal) - How fast the sound is play.
 * <p>
 * 1.8: <a href="http://docs.codelanx.com/Bukkit/1.8/org/bukkit/Sound.html">Sound Enum</a>
 * Latest: <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html">Sound Enum</a>
 * Basics: <a href="https://bukkit.org/threads/151517/">Bukkit Thread</a>
 * play command: <a href="https://minecraft.wiki/w/Commands/play">minecraft.wiki/w</a>
 *
 * @author Crypto Morin
 * @version 11.0.1
 * @see Sound
 */
public final class XSound extends XModule<XSound, Sound> {
    public static final XRegistry<XSound, Sound> REGISTRY =
            new XRegistry<>(Sound.class, XSound.class, () -> Registry.SOUNDS, XSound::new, XSound[]::new);

    public static final XSound
            AMBIENT_UNDERWATER_LOOP = std("ambient.underwater.loop", "AMBIENT_UNDERWATER_EXIT"),
            AMBIENT_UNDERWATER_LOOP_ADDITIONS = std("ambient.underwater.loop.additions", "AMBIENT_UNDERWATER_EXIT"),
            AMBIENT_UNDERWATER_LOOP_ADDITIONS_RARE = std("ambient.underwater.loop.additions.rare", "AMBIENT_UNDERWATER_EXIT"),
            AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE = std("ambient.underwater.loop.additions.ultra_rare", "AMBIENT_UNDERWATER_EXIT"),
            BLOCK_ANVIL_BREAK = std("block.anvil.break", "ANVIL_BREAK"),
            BLOCK_ANVIL_HIT = std("block.anvil.hit", "BLOCK_ANVIL_FALL"),
            BLOCK_ANVIL_LAND = std("block.anvil.land", "ANVIL_LAND"),
            BLOCK_ANVIL_PLACE = std("block.anvil.place", "BLOCK_ANVIL_FALL"),
            BLOCK_ANVIL_STEP = std("block.anvil.step", "BLOCK_ANVIL_FALL"),
            BLOCK_ANVIL_USE = std("block.anvil.use", "ANVIL_USE"),
            BLOCK_BEACON_DEACTIVATE = std("block.beacon.deactivate", "BLOCK_BEACON_AMBIENT"),
            BLOCK_BEACON_POWER_SELECT = std("block.beacon.power_select", "BLOCK_BEACON_AMBIENT"),
            BLOCK_CHEST_CLOSE = std("block.chest.close", "CHEST_CLOSE", "ENTITY_CHEST_CLOSE"),
            BLOCK_CHEST_OPEN = std("block.chest.open", "CHEST_OPEN", "ENTITY_CHEST_OPEN"),
            BLOCK_FIRE_AMBIENT = std("block.fire.ambient", "FIRE"),
            BLOCK_FIRE_EXTINGUISH = std("block.fire.extinguish", "FIZZ"),
            BLOCK_GLASS_BREAK = std("block.glass.break", "GLASS"),
            BLOCK_GRASS_BREAK = std("block.grass.break", "DIG_GRASS"),
            BLOCK_GRASS_STEP = std("block.grass.step", "STEP_GRASS"),
            BLOCK_GRAVEL_BREAK = std("block.gravel.break", "DIG_GRAVEL"),
            BLOCK_GRAVEL_STEP = std("block.gravel.step", "STEP_GRAVEL"),
            BLOCK_LADDER_STEP = std("block.ladder.step", "STEP_LADDER"),
            BLOCK_LAVA_AMBIENT = std("block.lava.ambient", "LAVA"),
            BLOCK_LAVA_POP = std("block.lava.pop", "LAVA_POP"),
            BLOCK_LILY_PAD_PLACE = std("block.lily_pad.place", "BLOCK_WATERLILY_PLACE"),
            BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF = std("block.metal_pressure_plate.click_off", "BLOCK_METAL_PRESSUREPLATE_CLICK_OFF"),
            BLOCK_METAL_PRESSURE_PLATE_CLICK_ON = std("block.metal_pressure_plate.click_on", "BLOCK_METAL_PRESSUREPLATE_CLICK_ON"),
            BLOCK_NOTE_BLOCK_BASEDRUM = std("block.note_block.basedrum", "NOTE_BASS_DRUM", "BLOCK_NOTE_BASEDRUM"),
            BLOCK_NOTE_BLOCK_BASS = std("block.note_block.bass", "NOTE_BASS", "BLOCK_NOTE_BASS"),
            BLOCK_NOTE_BLOCK_BELL = std("block.note_block.bell", "BLOCK_NOTE_BELL"),
            BLOCK_NOTE_BLOCK_CHIME = std("block.note_block.chime", "BLOCK_NOTE_CHIME"),
            BLOCK_NOTE_BLOCK_FLUTE = std("block.note_block.flute", "BLOCK_NOTE_FLUTE"),
            BLOCK_NOTE_BLOCK_GUITAR = std("block.note_block.guitar", "NOTE_BASS_GUITAR", "BLOCK_NOTE_GUITAR"),
            BLOCK_NOTE_BLOCK_HARP = std("block.note_block.harp", "NOTE_PIANO", "BLOCK_NOTE_HARP"),
            BLOCK_NOTE_BLOCK_HAT = std("block.note_block.hat", "NOTE_STICKS", "BLOCK_NOTE_HAT"),
            BLOCK_NOTE_BLOCK_PLING = std("block.note_block.pling", "NOTE_PLING", "BLOCK_NOTE_PLING"),
            BLOCK_NOTE_BLOCK_SNARE = std("block.note_block.snare", "NOTE_SNARE_DRUM", "BLOCK_NOTE_SNARE"),
            BLOCK_NOTE_BLOCK_XYLOPHONE = std("block.note_block.xylophone", "BLOCK_NOTE_XYLOPHONE"),
            BLOCK_PISTON_CONTRACT = std("block.piston.contract", "PISTON_RETRACT"),
            BLOCK_PISTON_EXTEND = std("block.piston.extend", "PISTON_EXTEND"),
            BLOCK_PORTAL_AMBIENT = std("block.portal.ambient", "PORTAL"),
            BLOCK_PORTAL_TRAVEL = std("block.portal.travel", "PORTAL_TRAVEL"),
            BLOCK_PORTAL_TRIGGER = std("block.portal.trigger", "PORTAL_TRIGGER"),
            BLOCK_SAND_BREAK = std("block.sand.break", "DIG_SAND"),
            BLOCK_SAND_STEP = std("block.sand.step", "STEP_SAND"),
            BLOCK_SLIME_BLOCK_BREAK = std("block.slime_block.break", "BLOCK_SLIME_BREAK"),
            BLOCK_SLIME_BLOCK_FALL = std("block.slime_block.fall", "BLOCK_SLIME_FALL"),
            BLOCK_SLIME_BLOCK_HIT = std("block.slime_block.hit", "BLOCK_SLIME_HIT"),
            BLOCK_SLIME_BLOCK_PLACE = std("block.slime_block.place", "BLOCK_SLIME_PLACE"),
            BLOCK_SLIME_BLOCK_STEP = std("block.slime_block.step", "BLOCK_SLIME_STEP"),
            BLOCK_SNOW_BREAK = std("block.snow.break", "DIG_SNOW"),
            BLOCK_SNOW_STEP = std("block.snow.step", "STEP_SNOW"),
            BLOCK_STONE_BREAK = std("block.stone.break", "DIG_STONE"),
            BLOCK_STONE_PRESSURE_PLATE_CLICK_OFF = std("block.stone_pressure_plate.click_off", "BLOCK_STONE_PRESSUREPLATE_CLICK_OFF"),
            BLOCK_STONE_PRESSURE_PLATE_CLICK_ON = std("block.stone_pressure_plate.click_on", "BLOCK_STONE_PRESSUREPLATE_CLICK_ON"),
            BLOCK_STONE_STEP = std("block.stone.step", "STEP_STONE"),
            BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES = std("block.sweet_berry_bush.pick_berries", "item.sweet_berries.pick_from_bush"),
            BLOCK_WATER_AMBIENT = std("block.water.ambient", "WATER"),
            BLOCK_WET_GRASS_PLACE = std("block.wet_grass.place", "BLOCK_WET_GRASS_HIT"),
            BLOCK_WET_GRASS_STEP = std("block.wet_grass.step", "BLOCK_WET_GRASS_HIT"),
            BLOCK_WOODEN_BUTTON_CLICK_OFF = std("block.wooden_button.click_off", "WOOD_CLICK", "BLOCK_WOOD_BUTTON_CLICK_OFF"),
            BLOCK_WOODEN_BUTTON_CLICK_ON = std("block.wooden_button.click_on", "WOOD_CLICK", "BLOCK_WOOD_BUTTON_CLICK_ON"),
            BLOCK_WOODEN_DOOR_CLOSE = std("block.wooden_door.close", "DOOR_CLOSE"),
            BLOCK_WOODEN_DOOR_OPEN = std("block.wooden_door.open", "DOOR_OPEN"),
            BLOCK_WOODEN_PRESSURE_PLATE_CLICK_OFF = std("block.wooden_pressure_plate.click_off", "BLOCK_WOOD_PRESSUREPLATE_CLICK_OFF"),
            BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON = std("block.wooden_pressure_plate.click_on", "BLOCK_WOOD_PRESSUREPLATE_CLICK_ON"),
            BLOCK_WOOD_BREAK = std("block.wood.break", "DIG_WOOD"),
            BLOCK_WOOD_STEP = std("block.wood.step", "STEP_WOOD"),
            BLOCK_WOOL_BREAK = std("block.wool.break", "DIG_WOOL", "BLOCK_CLOTH_BREAK"),
            BLOCK_WOOL_HIT = std("block.wool.hit", "BLOCK_CLOTH_HIT"),
            BLOCK_WOOL_PLACE = std("block.wool.place", "BLOCK_WOOL_FALL", "BLOCK_CLOTH_PLACE"),
            BLOCK_WOOL_STEP = std("block.wool.step", "STEP_WOOL", "BLOCK_CLOTH_STEP"),
            ENTITY_ARMOR_STAND_BREAK = std("entity.armor_stand.break", "ENTITY_ARMORSTAND_BREAK"),
            ENTITY_ARMOR_STAND_FALL = std("entity.armor_stand.fall", "ENTITY_ARMORSTAND_FALL"),
            ENTITY_ARMOR_STAND_HIT = std("entity.armor_stand.hit", "ENTITY_ARMORSTAND_HIT"),
            ENTITY_ARMOR_STAND_PLACE = std("entity.armor_stand.place", "ENTITY_ARMORSTAND_PLACE"),
            ENTITY_ARROW_HIT = std("entity.arrow.hit", "ARROW_HIT"),
            ENTITY_ARROW_HIT_PLAYER = std("entity.arrow.hit_player", "SUCCESSFUL_HIT"),
            ENTITY_ARROW_SHOOT = std("entity.arrow.shoot", "SHOOT_ARROW"),
            ENTITY_BAT_AMBIENT = std("entity.bat.ambient", "BAT_IDLE"),
            ENTITY_BAT_DEATH = std("entity.bat.death", "BAT_DEATH"),
            ENTITY_BAT_HURT = std("entity.bat.hurt", "BAT_HURT"),
            ENTITY_BAT_LOOP = std("entity.bat.loop", "BAT_LOOP"),
            ENTITY_BAT_TAKEOFF = std("entity.bat.takeoff", "BAT_TAKEOFF"),
            ENTITY_BLAZE_AMBIENT = std("entity.blaze.ambient", "BLAZE_BREATH"),
            ENTITY_BLAZE_DEATH = std("entity.blaze.death", "BLAZE_DEATH"),
            ENTITY_BLAZE_HURT = std("entity.blaze.hurt", "BLAZE_HIT"),
            ENTITY_CAT_AMBIENT = std("entity.cat.ambient", "CAT_MEOW"),
            ENTITY_CAT_EAT = std("entity.cat.eat"),
            ENTITY_CAT_HISS = std("entity.cat.hiss", "CAT_HISS"),
            ENTITY_CAT_HURT = std("entity.cat.hurt", "CAT_HIT"),
            ENTITY_CAT_PURR = std("entity.cat.purr", "CAT_PURR"),
            ENTITY_CAT_PURREOW = std("entity.cat.purreow", "CAT_PURREOW"),
            ENTITY_CHICKEN_AMBIENT = std("entity.chicken.ambient", "CHICKEN_IDLE"),
            ENTITY_CHICKEN_EGG = std("entity.chicken.egg", "CHICKEN_EGG_POP"),
            ENTITY_CHICKEN_HURT = std("entity.chicken.hurt", "CHICKEN_HURT"),
            ENTITY_CHICKEN_STEP = std("entity.chicken.step", "CHICKEN_WALK"),
            ENTITY_COW_AMBIENT = std("entity.cow.ambient", "COW_IDLE"),
            ENTITY_COW_HURT = std("entity.cow.hurt", "COW_HURT"),
            ENTITY_COW_STEP = std("entity.cow.step", "COW_WALK"),
            ENTITY_CREEPER_DEATH = std("entity.creeper.death", "CREEPER_DEATH"),
            ENTITY_CREEPER_PRIMED = std("entity.creeper.primed", "CREEPER_HISS"),
            ENTITY_DONKEY_AMBIENT = std("entity.donkey.ambient", "DONKEY_IDLE"),
            ENTITY_DONKEY_ANGRY = std("entity.donkey.angry", "DONKEY_ANGRY"),
            ENTITY_DONKEY_DEATH = std("entity.donkey.death", "DONKEY_DEATH"),
            ENTITY_DONKEY_HURT = std("entity.donkey.hurt", "DONKEY_HIT"),
            ENTITY_DRAGON_FIREBALL_EXPLODE = std("entity.dragon_fireball.explode", "ENTITY_ENDERDRAGON_FIREBALL_EXPLODE"),
            ENTITY_ENDERMAN_AMBIENT = std("entity.enderman.ambient", "ENDERMAN_IDLE", "ENTITY_ENDERMEN_AMBIENT"),
            ENTITY_ENDERMAN_DEATH = std("entity.enderman.death", "ENDERMAN_DEATH", "ENTITY_ENDERMEN_DEATH"),
            ENTITY_ENDERMAN_HURT = std("entity.enderman.hurt", "ENDERMAN_HIT", "ENTITY_ENDERMEN_HURT"),
            ENTITY_ENDERMAN_SCREAM = std("entity.enderman.scream", "ENDERMAN_SCREAM", "ENTITY_ENDERMEN_SCREAM"),
            ENTITY_ENDERMAN_STARE = std("entity.enderman.stare", "ENDERMAN_STARE", "ENTITY_ENDERMEN_STARE"),
            ENTITY_ENDERMAN_TELEPORT = std("entity.enderman.teleport", "ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT"),
            ENTITY_ENDER_DRAGON_AMBIENT = std("entity.ender_dragon.ambient", "ENDERDRAGON_WINGS", "ENTITY_ENDERDRAGON_AMBIENT"),
            ENTITY_ENDER_DRAGON_DEATH = std("entity.ender_dragon.death", "ENDERDRAGON_DEATH", "ENTITY_ENDERDRAGON_DEATH"),
            ENTITY_ENDER_DRAGON_FLAP = std("entity.ender_dragon.flap", "ENDERDRAGON_WINGS", "ENTITY_ENDERDRAGON_FLAP"),
            ENTITY_ENDER_DRAGON_GROWL = std("entity.ender_dragon.growl", "ENDERDRAGON_GROWL", "ENTITY_ENDERDRAGON_GROWL"),
            ENTITY_ENDER_DRAGON_HURT = std("entity.ender_dragon.hurt", "ENDERDRAGON_HIT", "ENTITY_ENDERDRAGON_HURT"),
            ENTITY_ENDER_DRAGON_SHOOT = std("entity.ender_dragon.shoot", "ENTITY_ENDERDRAGON_SHOOT"),
            ENTITY_ENDER_EYE_LAUNCH = std("entity.ender_eye.launch", "ENTITY_ENDEREYE_LAUNCH"),
            ENTITY_ENDER_PEARL_THROW = std("entity.ender_pearl.throw", "ENTITY_ENDERPEARL_THROW"),
            ENTITY_EVOKER_AMBIENT = std("entity.evoker.ambient", "ENTITY_EVOCATION_ILLAGER_AMBIENT"),
            ENTITY_EVOKER_CAST_SPELL = std("entity.evoker.cast_spell", "ENTITY_EVOCATION_ILLAGER_CAST_SPELL"),
            ENTITY_EVOKER_DEATH = std("entity.evoker.death", "ENTITY_EVOCATION_ILLAGER_DEATH"),
            ENTITY_EVOKER_FANGS_ATTACK = std("entity.evoker_fangs.attack", "ENTITY_EVOCATION_FANGS_ATTACK"),
            ENTITY_EVOKER_HURT = std("entity.evoker.hurt", "ENTITY_EVOCATION_ILLAGER_HURT"),
            ENTITY_EVOKER_PREPARE_ATTACK = std("entity.evoker.prepare_attack", "ENTITY_EVOCATION_ILLAGER_PREPARE_ATTACK"),
            ENTITY_EVOKER_PREPARE_SUMMON = std("entity.evoker.prepare_summon", "ENTITY_EVOCATION_ILLAGER_PREPARE_SUMMON"),
            ENTITY_EVOKER_PREPARE_WOLOLO = std("entity.evoker.prepare_wololo", "ENTITY_EVOCATION_ILLAGER_PREPARE_WOLOLO"),
            ENTITY_FIREWORK_ROCKET_BLAST = std("entity.firework_rocket.blast", "FIREWORK_BLAST", "ENTITY_FIREWORK_BLAST"),
            ENTITY_FIREWORK_ROCKET_BLAST_FAR = std("entity.firework_rocket.blast_far", "FIREWORK_BLAST2", "ENTITY_FIREWORK_BLAST_FAR"),
            ENTITY_FIREWORK_ROCKET_LARGE_BLAST = std("entity.firework_rocket.large_blast", "FIREWORK_LARGE_BLAST", "ENTITY_FIREWORK_LARGE_BLAST"),
            ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR = std("entity.firework_rocket.large_blast_far", "FIREWORK_LARGE_BLAST2", "ENTITY_FIREWORK_LARGE_BLAST_FAR"),
            ENTITY_FIREWORK_ROCKET_LAUNCH = std("entity.firework_rocket.launch", "FIREWORK_LAUNCH", "ENTITY_FIREWORK_LAUNCH"),
            ENTITY_FIREWORK_ROCKET_TWINKLE = std("entity.firework_rocket.twinkle", "FIREWORK_TWINKLE", "ENTITY_FIREWORK_TWINKLE"),
            ENTITY_FIREWORK_ROCKET_TWINKLE_FAR = std("entity.firework_rocket.twinkle_far", "FIREWORK_TWINKLE2", "ENTITY_FIREWORK_TWINKLE_FAR"),
            ENTITY_FISHING_BOBBER_SPLASH = std("entity.fishing_bobber.splash", "SPLASH2", "ENTITY_BOBBER_SPLASH"),
            ENTITY_FISHING_BOBBER_THROW = std("entity.fishing_bobber.throw", "ENTITY_BOBBER_THROW"),
            ENTITY_GENERIC_BIG_FALL = std("entity.generic.big_fall", "FALL_BIG"),
            ENTITY_GENERIC_DRINK = std("entity.generic.drink", "DRINK"),
            ENTITY_GENERIC_EAT = std("entity.generic.eat", "EAT"),
            ENTITY_GENERIC_EXPLODE = std("entity.generic.explode", "EXPLODE"),
            ENTITY_GENERIC_SMALL_FALL = std("entity.generic.small_fall", "FALL_SMALL"),
            ENTITY_GENERIC_SPLASH = std("entity.generic.splash", "SPLASH"),
            ENTITY_GENERIC_SWIM = std("entity.generic.swim", "SWIM"),
            ENTITY_GHAST_AMBIENT = std("entity.ghast.ambient", "GHAST_MOAN"),
            ENTITY_GHAST_DEATH = std("entity.ghast.death", "GHAST_DEATH"),
            ENTITY_GHAST_HURT = std("entity.ghast.hurt", "GHAST_SCREAM2"),
            ENTITY_GHAST_SCREAM = std("entity.ghast.scream", "GHAST_SCREAM"),
            ENTITY_GHAST_SHOOT = std("entity.ghast.shoot", "GHAST_FIREBALL"),
            ENTITY_GHAST_WARN = std("entity.ghast.warn", "GHAST_CHARGE"),
            ENTITY_HORSE_AMBIENT = std("entity.horse.ambient", "HORSE_IDLE"),
            ENTITY_HORSE_ANGRY = std("entity.horse.angry", "HORSE_ANGRY"),
            ENTITY_HORSE_ARMOR = std("entity.horse.armor", "HORSE_ARMOR"),
            ENTITY_HORSE_BREATHE = std("entity.horse.breathe", "HORSE_BREATHE"),
            ENTITY_HORSE_DEATH = std("entity.horse.death", "HORSE_DEATH"),
            ENTITY_HORSE_EAT = std("entity.horse.eat"),
            ENTITY_HORSE_GALLOP = std("entity.horse.gallop", "HORSE_GALLOP"),
            ENTITY_HORSE_HURT = std("entity.horse.hurt", "HORSE_HIT"),
            ENTITY_HORSE_JUMP = std("entity.horse.jump", "HORSE_JUMP"),
            ENTITY_HORSE_LAND = std("entity.horse.land", "HORSE_LAND"),
            ENTITY_HORSE_SADDLE = std("entity.horse.saddle", "HORSE_SADDLE"),
            ENTITY_HORSE_STEP = std("entity.horse.step", "HORSE_SOFT"),
            ENTITY_HORSE_STEP_WOOD = std("entity.horse.step_wood", "HORSE_WOOD"),
            ENTITY_HOSTILE_BIG_FALL = std("entity.hostile.big_fall", "FALL_BIG"),
            ENTITY_HOSTILE_SMALL_FALL = std("entity.hostile.small_fall", "FALL_SMALL"),
            ENTITY_HOSTILE_SPLASH = std("entity.hostile.splash", "SPLASH"),
            ENTITY_HOSTILE_SWIM = std("entity.hostile.swim", "SWIM"),
            ENTITY_ILLUSIONER_AMBIENT = std("entity.illusioner.ambient", "ENTITY_ILLUSION_ILLAGER_AMBIENT"),
            ENTITY_ILLUSIONER_CAST_SPELL = std("entity.illusioner.cast_spell", "ENTITY_ILLUSION_ILLAGER_CAST_SPELL"),
            ENTITY_ILLUSIONER_DEATH = std("entity.illusioner.death", "ENTITY_ILLUSIONER_CAST_DEATH", "ENTITY_ILLUSION_ILLAGER_DEATH"),
            ENTITY_ILLUSIONER_HURT = std("entity.illusioner.hurt", "ENTITY_ILLUSION_ILLAGER_HURT"),
            ENTITY_ILLUSIONER_MIRROR_MOVE = std("entity.illusioner.mirror_move", "ENTITY_ILLUSION_ILLAGER_MIRROR_MOVE"),
            ENTITY_ILLUSIONER_PREPARE_BLINDNESS = std("entity.illusioner.prepare_blindness", "ENTITY_ILLUSION_ILLAGER_PREPARE_BLINDNESS"),
            ENTITY_ILLUSIONER_PREPARE_MIRROR = std("entity.illusioner.prepare_mirror", "ENTITY_ILLUSION_ILLAGER_PREPARE_MIRROR"),
            ENTITY_IRON_GOLEM_ATTACK = std("entity.iron_golem.attack", "IRONGOLEM_THROW", "ENTITY_IRONGOLEM_ATTACK"),
            ENTITY_IRON_GOLEM_DEATH = std("entity.iron_golem.death", "IRONGOLEM_DEATH", "ENTITY_IRONGOLEM_DEATH"),
            ENTITY_IRON_GOLEM_HURT = std("entity.iron_golem.hurt", "IRONGOLEM_HIT", "ENTITY_IRONGOLEM_HURT"),
            ENTITY_IRON_GOLEM_STEP = std("entity.iron_golem.step", "IRONGOLEM_WALK", "ENTITY_IRONGOLEM_STEP"),
            ENTITY_ITEM_BREAK = std("entity.item.break", "ITEM_BREAK"),
            ENTITY_ITEM_FRAME_ADD_ITEM = std("entity.item_frame.add_item", "ENTITY_ITEMFRAME_ADD_ITEM"),
            ENTITY_ITEM_FRAME_BREAK = std("entity.item_frame.break", "ENTITY_ITEMFRAME_BREAK"),
            ENTITY_ITEM_FRAME_PLACE = std("entity.item_frame.place", "ENTITY_ITEMFRAME_PLACE"),
            ENTITY_ITEM_FRAME_REMOVE_ITEM = std("entity.item_frame.remove_item", "ENTITY_ITEMFRAME_REMOVE_ITEM"),
            ENTITY_ITEM_FRAME_ROTATE_ITEM = std("entity.item_frame.rotate_item", "ENTITY_ITEMFRAME_ROTATE_ITEM"),
            ENTITY_ITEM_PICKUP = std("entity.item.pickup", "ITEM_PICKUP"),
            ENTITY_LEASH_KNOT_BREAK = std("entity.leash_knot.break", "ENTITY_LEASHKNOT_BREAK"),
            ENTITY_LEASH_KNOT_PLACE = std("entity.leash_knot.place", "ENTITY_LEASHKNOT_PLACE"),
            ENTITY_LIGHTNING_BOLT_IMPACT = std("entity.lightning_bolt.impact", "ENTITY_LIGHTNING_IMPACT", "AMBIENCE_THUNDER"),
            ENTITY_LIGHTNING_BOLT_THUNDER = std("entity.lightning_bolt.thunder", "ENTITY_LIGHTNING_THUNDER", "AMBIENCE_THUNDER"),
            ENTITY_LINGERING_POTION_THROW = std("entity.lingering_potion.throw", "ENTITY_LINGERINGPOTION_THROW"),
            ENTITY_MAGMA_CUBE_DEATH = std("entity.magma_cube.death", "ENTITY_MAGMACUBE_DEATH"),
            ENTITY_MAGMA_CUBE_DEATH_SMALL = std("entity.magma_cube.death_small", "ENTITY_SMALL_MAGMACUBE_DEATH"),
            ENTITY_MAGMA_CUBE_HURT = std("entity.magma_cube.hurt", "ENTITY_MAGMACUBE_HURT"),
            ENTITY_MAGMA_CUBE_HURT_SMALL = std("entity.magma_cube.hurt_small", "ENTITY_SMALL_MAGMACUBE_HURT"),
            ENTITY_MAGMA_CUBE_JUMP = std("entity.magma_cube.jump", "MAGMACUBE_JUMP", "ENTITY_MAGMACUBE_JUMP"),
            ENTITY_MAGMA_CUBE_SQUISH = std("entity.magma_cube.squish", "MAGMACUBE_WALK", "ENTITY_MAGMACUBE_SQUISH"),
            ENTITY_MAGMA_CUBE_SQUISH_SMALL = std("entity.magma_cube.squish_small", "MAGMACUBE_WALK2", "ENTITY_SMALL_MAGMACUBE_SQUISH"),
            ENTITY_MINECART_INSIDE = std("entity.minecart.inside", "MINECART_INSIDE"),
            ENTITY_MINECART_RIDING = std("entity.minecart.riding", "MINECART_BASE"),
            ENTITY_MULE_CHEST = std("entity.mule.chest", "ENTITY_MULE_AMBIENT"),
            ENTITY_MULE_DEATH = std("entity.mule.death", "ENTITY_MULE_AMBIENT"),
            ENTITY_MULE_HURT = std("entity.mule.hurt", "ENTITY_MULE_AMBIENT"),
            ENTITY_PIG_AMBIENT = std("entity.pig.ambient", "PIG_IDLE"),
            ENTITY_PIG_DEATH = std("entity.pig.death", "PIG_DEATH"),
            ENTITY_PIG_SADDLE = std("entity.pig.saddle", "ENTITY_PIG_HURT"),
            ENTITY_PIG_STEP = std("entity.pig.step", "PIG_WALK"),
            ENTITY_PLAYER_ATTACK_STRONG = std("entity.player.attack.strong", "SUCCESSFUL_HIT"),
            ENTITY_PLAYER_BIG_FALL = std("entity.player.big_fall", "FALL_BIG"),
            ENTITY_PLAYER_BURP = std("entity.player.burp", "BURP"),
            ENTITY_PLAYER_HURT = std("entity.player.hurt", "HURT_FLESH"),
            ENTITY_PLAYER_LEVELUP = std("entity.player.levelup", "LEVEL_UP"),
            ENTITY_PLAYER_SMALL_FALL = std("entity.player.small_fall", "FALL_SMALL"),
            ENTITY_PLAYER_SPLASH = std("entity.player.splash", "SLASH"),
            ENTITY_PLAYER_SPLASH_HIGH_SPEED = std("entity.player.splash.high_speed", "SPLASH"),
            ENTITY_PLAYER_SWIM = std("entity.player.swim", "SWIM"),
            ENTITY_POLAR_BEAR_AMBIENT_BABY = std("entity.polar_bear.ambient_baby", "ENTITY_POLAR_BEAR_BABY_AMBIENT"),
            ENTITY_SALMON_HURT = std("entity.salmon.hurt", "ENTITY_SALMON_FLOP"),
            ENTITY_SHEEP_AMBIENT = std("entity.sheep.ambient", "SHEEP_IDLE"),
            ENTITY_SHEEP_SHEAR = std("entity.sheep.shear", "SHEEP_SHEAR"),
            ENTITY_SHEEP_STEP = std("entity.sheep.step", "SHEEP_WALK"),
            ENTITY_SILVERFISH_AMBIENT = std("entity.silverfish.ambient", "SILVERFISH_IDLE"),
            ENTITY_SILVERFISH_DEATH = std("entity.silverfish.death", "SILVERFISH_KILL"),
            ENTITY_SILVERFISH_HURT = std("entity.silverfish.hurt", "SILVERFISH_HIT"),
            ENTITY_SILVERFISH_STEP = std("entity.silverfish.step", "SILVERFISH_WALK"),
            ENTITY_SKELETON_AMBIENT = std("entity.skeleton.ambient", "SKELETON_IDLE"),
            ENTITY_SKELETON_DEATH = std("entity.skeleton.death", "SKELETON_DEATH"),
            ENTITY_SKELETON_HORSE_AMBIENT = std("entity.skeleton_horse.ambient", "HORSE_SKELETON_IDLE"),
            ENTITY_SKELETON_HORSE_DEATH = std("entity.skeleton_horse.death", "HORSE_SKELETON_DEATH"),
            ENTITY_SKELETON_HORSE_HURT = std("entity.skeleton_horse.hurt", "HORSE_SKELETON_HIT"),
            ENTITY_SKELETON_HURT = std("entity.skeleton.hurt", "SKELETON_HURT"),
            ENTITY_SKELETON_STEP = std("entity.skeleton.step", "SKELETON_WALK"),
            ENTITY_SLIME_ATTACK = std("entity.slime.attack", "SLIME_ATTACK"),
            ENTITY_SLIME_HURT_SMALL = std("entity.slime.hurt_small", "ENTITY_SMALL_SLIME_HURT"),
            ENTITY_SLIME_JUMP = std("entity.slime.jump", "SLIME_WALK"),
            ENTITY_SLIME_JUMP_SMALL = std("entity.slime.jump_small", "SLIME_WALK2", "ENTITY_SMALL_SLIME_JUMP"),
            ENTITY_SLIME_SQUISH = std("entity.slime.squish", "SLIME_WALK2"),
            ENTITY_SLIME_SQUISH_SMALL = std("entity.slime.squish_small", "ENTITY_SMALL_SLIME_SQUISH"),
            ENTITY_SNOW_GOLEM_AMBIENT = std("entity.snow_golem.ambient", "ENTITY_SNOWMAN_AMBIENT"),
            ENTITY_SNOW_GOLEM_DEATH = std("entity.snow_golem.death", "ENTITY_SNOWMAN_DEATH"),
            ENTITY_SNOW_GOLEM_HURT = std("entity.snow_golem.hurt", "ENTITY_SNOWMAN_HURT"),
            ENTITY_SNOW_GOLEM_SHEAR = std("entity.snow_golem.shear"),
            ENTITY_SNOW_GOLEM_SHOOT = std("entity.snow_golem.shoot", "ENTITY_SNOWMAN_SHOOT"),
            ENTITY_SPIDER_AMBIENT = std("entity.spider.ambient", "SPIDER_IDLE"),
            ENTITY_SPIDER_DEATH = std("entity.spider.death", "SPIDER_DEATH"),
            ENTITY_SPIDER_STEP = std("entity.spider.step", "SPIDER_WALK"),
            ENTITY_TNT_PRIMED = std("entity.tnt.primed", "FUSE"),
            ENTITY_TROPICAL_FISH_FLOP = std("entity.tropical_fish.flop", "ENTITY_TROPICAL_FISH_DEATH"),
            ENTITY_VILLAGER_AMBIENT = std("entity.villager.ambient", "VILLAGER_IDLE"),
            ENTITY_VILLAGER_DEATH = std("entity.villager.death", "VILLAGER_DEATH"),
            ENTITY_VILLAGER_HURT = std("entity.villager.hurt", "VILLAGER_HIT"),
            ENTITY_VILLAGER_NO = std("entity.villager.no", "VILLAGER_NO"),
            ENTITY_VILLAGER_TRADE = std("entity.villager.trade", "VILLAGER_HAGGLE", "ENTITY_VILLAGER_TRADING"),
            ENTITY_VILLAGER_YES = std("entity.villager.yes", "VILLAGER_YES"),
            ENTITY_VINDICATOR_AMBIENT = std("entity.vindicator.ambient", "ENTITY_VINDICATION_ILLAGER_AMBIENT"),
            ENTITY_VINDICATOR_DEATH = std("entity.vindicator.death", "ENTITY_VINDICATION_ILLAGER_DEATH"),
            ENTITY_VINDICATOR_HURT = std("entity.vindicator.hurt", "ENTITY_VINDICATION_ILLAGER_HURT"),
            ENTITY_WITHER_AMBIENT = std("entity.wither.ambient", "WITHER_IDLE"),
            ENTITY_WITHER_DEATH = std("entity.wither.death", "WITHER_DEATH"),
            ENTITY_WITHER_HURT = std("entity.wither.hurt", "WITHER_HURT"),
            ENTITY_WITHER_SHOOT = std("entity.wither.shoot", "WITHER_SHOOT"),
            ENTITY_WITHER_SPAWN = std("entity.wither.spawn", "WITHER_SPAWN"),
            ENTITY_WOLF_AMBIENT = std("entity.wolf.ambient", "WOLF_BARK"),
            ENTITY_WOLF_DEATH = std("entity.wolf.death", "WOLF_DEATH"),
            ENTITY_WOLF_GROWL = std("entity.wolf.growl", "WOLF_GROWL"),
            ENTITY_WOLF_HOWL = std("entity.wolf.howl", "WOLF_HOWL"),
            ENTITY_WOLF_HURT = std("entity.wolf.hurt", "WOLF_HURT"),
            ENTITY_WOLF_PANT = std("entity.wolf.pant", "WOLF_PANT"),
            ENTITY_WOLF_SHAKE = std("entity.wolf.shake", "WOLF_SHAKE"),
            ENTITY_WOLF_STEP = std("entity.wolf.step", "WOLF_WALK"),
            ENTITY_WOLF_WHINE = std("entity.wolf.whine", "WOLF_WHINE"),
            ENTITY_ZOMBIE_AMBIENT = std("entity.zombie.ambient", "ZOMBIE_IDLE"),
            ENTITY_ZOMBIE_ATTACK_IRON_DOOR = std("entity.zombie.attack_iron_door", "ZOMBIE_METAL"),
            ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR = std("entity.zombie.attack_wooden_door", "ZOMBIE_WOOD", "ENTITY_ZOMBIE_ATTACK_DOOR_WOOD"),
            ENTITY_ZOMBIE_BREAK_WOODEN_DOOR = std("entity.zombie.break_wooden_door", "ZOMBIE_WOODBREAK", "ENTITY_ZOMBIE_BREAK_DOOR_WOOD"),
            ENTITY_ZOMBIE_DEATH = std("entity.zombie.death", "ZOMBIE_DEATH"),
            ENTITY_ZOMBIE_HORSE_AMBIENT = std("entity.zombie_horse.ambient", "HORSE_ZOMBIE_IDLE"),
            ENTITY_ZOMBIE_HORSE_DEATH = std("entity.zombie_horse.death", "HORSE_ZOMBIE_DEATH"),
            ENTITY_ZOMBIE_HORSE_HURT = std("entity.zombie_horse.hurt", "HORSE_ZOMBIE_HIT"),
            ENTITY_ZOMBIE_HURT = std("entity.zombie.hurt", "ZOMBIE_HURT"),
            ENTITY_ZOMBIE_INFECT = std("entity.zombie.infect", "ZOMBIE_INFECT"),
            ENTITY_ZOMBIE_STEP = std("entity.zombie.step", "ZOMBIE_WALK"),
            ENTITY_ZOMBIE_VILLAGER_CONVERTED = std("entity.zombie_villager.converted", "ZOMBIE_UNFECT"),
            ENTITY_ZOMBIE_VILLAGER_CURE = std("entity.zombie_villager.cure", "ZOMBIE_REMEDY"),
            ENTITY_ZOMBIFIED_PIGLIN_AMBIENT = std("entity.zombified_piglin.ambient", "ZOMBIE_PIG_IDLE", "ENTITY_ZOMBIE_PIG_AMBIENT", "ENTITY_ZOMBIE_PIGMAN_AMBIENT"),
            ENTITY_ZOMBIFIED_PIGLIN_ANGRY = std("entity.zombified_piglin.angry", "ZOMBIE_PIG_ANGRY", "ENTITY_ZOMBIE_PIG_ANGRY", "ENTITY_ZOMBIE_PIGMAN_ANGRY"),
            ENTITY_ZOMBIFIED_PIGLIN_DEATH = std("entity.zombified_piglin.death", "ZOMBIE_PIG_DEATH", "ENTITY_ZOMBIE_PIG_DEATH", "ENTITY_ZOMBIE_PIGMAN_DEATH"),
            ENTITY_ZOMBIFIED_PIGLIN_HURT = std("entity.zombified_piglin.hurt", "ZOMBIE_PIG_HURT", "ENTITY_ZOMBIE_PIG_HURT", "ENTITY_ZOMBIE_PIGMAN_HURT"),
            ITEM_FLINTANDSTEEL_USE = std("item.flintandsteel.use", "FIRE_IGNITE"),
            ITEM_TRIDENT_RIPTIDE_2 = std("item.trident.riptide_2", "ITEM_TRIDENT_RIPTIDE_1"),
            ITEM_TRIDENT_RIPTIDE_3 = std("item.trident.riptide_3", "ITEM_TRIDENT_RIPTIDE_1"),
            MUSIC_DISC_11 = std("music_disc.11", "RECORD_11"),
            MUSIC_DISC_13 = std("music_disc.13", "RECORD_13"),
            MUSIC_DISC_BLOCKS = std("music_disc.blocks", "RECORD_BLOCKS"),
            MUSIC_DISC_CAT = std("music_disc.cat", "RECORD_CAT"),
            MUSIC_DISC_CHIRP = std("music_disc.chirp", "RECORD_CHIRP"),
            MUSIC_DISC_FAR = std("music_disc.far", "RECORD_FAR"),
            MUSIC_DISC_MALL = std("music_disc.mall", "RECORD_MALL"),
            MUSIC_DISC_MELLOHI = std("music_disc.mellohi", "RECORD_MELLOHI"),
            MUSIC_DISC_STAL = std("music_disc.stal", "RECORD_STAL"),
            MUSIC_DISC_STRAD = std("music_disc.strad", "RECORD_STRAD"),
            MUSIC_DISC_WAIT = std("music_disc.wait", "RECORD_WAIT"),
            MUSIC_DISC_WARD = std("music_disc.ward", "RECORD_WARD"),
            MUSIC_NETHER_BASALT_DELTAS = std("music.nether.basalt_deltas", "MUSIC_NETHER"),
            UI_BUTTON_CLICK = std("ui.button.click", "CLICK"),
            WEATHER_RAIN = std("weather.rain", "AMBIENCE_RAIN"),
            AMBIENT_CAVE = std("ambient.cave", "AMBIENCE_CAVE");

    @XMerge(since = "1.9", version = "1.12?", name = "ENTITY_EXPERIENCE_ORB_TOUCH")
    public static final XSound
            ENTITY_EXPERIENCE_ORB_PICKUP = std("entity.experience_orb.pickup", "ORB_PICKUP");

    public static final XSound
            AMBIENT_BASALT_DELTAS_ADDITIONS = std("ambient.basalt_deltas.additions"),
            AMBIENT_BASALT_DELTAS_LOOP = std("ambient.basalt_deltas.loop"),
            AMBIENT_BASALT_DELTAS_MOOD = std("ambient.basalt_deltas.mood"),
            AMBIENT_CRIMSON_FOREST_ADDITIONS = std("ambient.crimson_forest.additions"),
            AMBIENT_CRIMSON_FOREST_LOOP = std("ambient.crimson_forest.loop"),
            AMBIENT_CRIMSON_FOREST_MOOD = std("ambient.crimson_forest.mood"),
            AMBIENT_NETHER_WASTES_ADDITIONS = std("ambient.nether_wastes.additions"),
            AMBIENT_NETHER_WASTES_LOOP = std("ambient.nether_wastes.loop"),
            AMBIENT_NETHER_WASTES_MOOD = std("ambient.nether_wastes.mood"),
            AMBIENT_SOUL_SAND_VALLEY_ADDITIONS = std("ambient.soul_sand_valley.additions"),
            AMBIENT_SOUL_SAND_VALLEY_LOOP = std("ambient.soul_sand_valley.loop"),
            AMBIENT_SOUL_SAND_VALLEY_MOOD = std("ambient.soul_sand_valley.mood"),
            AMBIENT_UNDERWATER_ENTER = std("ambient.underwater.enter"),
            AMBIENT_UNDERWATER_EXIT = std("ambient.underwater.exit"),
            AMBIENT_WARPED_FOREST_ADDITIONS = std("ambient.warped_forest.additions"),
            AMBIENT_WARPED_FOREST_LOOP = std("ambient.warped_forest.loop"),
            AMBIENT_WARPED_FOREST_MOOD = std("ambient.warped_forest.mood"),
            BLOCK_AMETHYST_BLOCK_BREAK = std("block.amethyst_block.break"),
            BLOCK_AMETHYST_BLOCK_CHIME = std("block.amethyst_block.chime"),
            BLOCK_AMETHYST_BLOCK_FALL = std("block.amethyst_block.fall"),
            BLOCK_AMETHYST_BLOCK_HIT = std("block.amethyst_block.hit"),
            BLOCK_AMETHYST_BLOCK_PLACE = std("block.amethyst_block.place"),
            BLOCK_AMETHYST_BLOCK_RESONATE = std("block.amethyst_block.resonate"),
            BLOCK_AMETHYST_BLOCK_STEP = std("block.amethyst_block.step"),
            BLOCK_AMETHYST_CLUSTER_BREAK = std("block.amethyst_cluster.break"),
            BLOCK_AMETHYST_CLUSTER_FALL = std("block.amethyst_cluster.fall"),
            BLOCK_AMETHYST_CLUSTER_HIT = std("block.amethyst_cluster.hit"),
            BLOCK_AMETHYST_CLUSTER_PLACE = std("block.amethyst_cluster.place"),
            BLOCK_AMETHYST_CLUSTER_STEP = std("block.amethyst_cluster.step"),
            BLOCK_ANCIENT_DEBRIS_BREAK = std("block.ancient_debris.break"),
            BLOCK_ANCIENT_DEBRIS_FALL = std("block.ancient_debris.fall"),
            BLOCK_ANCIENT_DEBRIS_HIT = std("block.ancient_debris.hit"),
            BLOCK_ANCIENT_DEBRIS_PLACE = std("block.ancient_debris.place"),
            BLOCK_ANCIENT_DEBRIS_STEP = std("block.ancient_debris.step"),
            BLOCK_ANVIL_DESTROY = std("block.anvil.destroy"),
            BLOCK_ANVIL_FALL = std("block.anvil.fall"),
            BLOCK_AZALEA_BREAK = std("block.azalea.break"),
            BLOCK_AZALEA_FALL = std("block.azalea.fall"),
            BLOCK_AZALEA_HIT = std("block.azalea.hit"),
            BLOCK_AZALEA_LEAVES_BREAK = std("block.azalea_leaves.break"),
            BLOCK_AZALEA_LEAVES_FALL = std("block.azalea_leaves.fall"),
            BLOCK_AZALEA_LEAVES_HIT = std("block.azalea_leaves.hit"),
            BLOCK_AZALEA_LEAVES_PLACE = std("block.azalea_leaves.place"),
            BLOCK_AZALEA_LEAVES_STEP = std("block.azalea_leaves.step"),
            BLOCK_AZALEA_PLACE = std("block.azalea.place"),
            BLOCK_AZALEA_STEP = std("block.azalea.step"),
            BLOCK_BAMBOO_BREAK = std("block.bamboo.break"),
            BLOCK_BAMBOO_FALL = std("block.bamboo.fall"),
            BLOCK_BAMBOO_HIT = std("block.bamboo.hit"),
            BLOCK_BAMBOO_PLACE = std("block.bamboo.place"),
            BLOCK_BAMBOO_SAPLING_BREAK = std("block.bamboo_sapling.break"),
            BLOCK_BAMBOO_SAPLING_HIT = std("block.bamboo_sapling.hit"),
            BLOCK_BAMBOO_SAPLING_PLACE = std("block.bamboo_sapling.place"),
            BLOCK_BAMBOO_STEP = std("block.bamboo.step"),
            BLOCK_BAMBOO_WOOD_BREAK = std("block.bamboo_wood.break"),
            BLOCK_BAMBOO_WOOD_BUTTON_CLICK_OFF = std("block.bamboo_wood_button.click_off"),
            BLOCK_BAMBOO_WOOD_BUTTON_CLICK_ON = std("block.bamboo_wood_button.click_on"),
            BLOCK_BAMBOO_WOOD_DOOR_CLOSE = std("block.bamboo_wood_door.close"),
            BLOCK_BAMBOO_WOOD_DOOR_OPEN = std("block.bamboo_wood_door.open"),
            BLOCK_BAMBOO_WOOD_FALL = std("block.bamboo_wood.fall"),
            BLOCK_BAMBOO_WOOD_FENCE_GATE_CLOSE = std("block.bamboo_wood_fence_gate.close"),
            BLOCK_BAMBOO_WOOD_FENCE_GATE_OPEN = std("block.bamboo_wood_fence_gate.open"),
            BLOCK_BAMBOO_WOOD_HANGING_SIGN_BREAK = std("block.bamboo_wood_hanging_sign.break"),
            BLOCK_BAMBOO_WOOD_HANGING_SIGN_FALL = std("block.bamboo_wood_hanging_sign.fall"),
            BLOCK_BAMBOO_WOOD_HANGING_SIGN_HIT = std("block.bamboo_wood_hanging_sign.hit"),
            BLOCK_BAMBOO_WOOD_HANGING_SIGN_PLACE = std("block.bamboo_wood_hanging_sign.place"),
            BLOCK_BAMBOO_WOOD_HANGING_SIGN_STEP = std("block.bamboo_wood_hanging_sign.step"),
            BLOCK_BAMBOO_WOOD_HIT = std("block.bamboo_wood.hit"),
            BLOCK_BAMBOO_WOOD_PLACE = std("block.bamboo_wood.place"),
            BLOCK_BAMBOO_WOOD_PRESSURE_PLATE_CLICK_OFF = std("block.bamboo_wood_pressure_plate.click_off"),
            BLOCK_BAMBOO_WOOD_PRESSURE_PLATE_CLICK_ON = std("block.bamboo_wood_pressure_plate.click_on"),
            BLOCK_BAMBOO_WOOD_STEP = std("block.bamboo_wood.step"),
            BLOCK_BAMBOO_WOOD_TRAPDOOR_CLOSE = std("block.bamboo_wood_trapdoor.close"),
            BLOCK_BAMBOO_WOOD_TRAPDOOR_OPEN = std("block.bamboo_wood_trapdoor.open"),
            BLOCK_BARREL_CLOSE = std("block.barrel.close"),
            BLOCK_BARREL_OPEN = std("block.barrel.open"),
            BLOCK_BASALT_BREAK = std("block.basalt.break"),
            BLOCK_BASALT_FALL = std("block.basalt.fall"),
            BLOCK_BASALT_HIT = std("block.basalt.hit"),
            BLOCK_BASALT_PLACE = std("block.basalt.place"),
            BLOCK_BASALT_STEP = std("block.basalt.step"),
            BLOCK_BEACON_ACTIVATE = std("block.beacon.activate"),
            BLOCK_BEACON_AMBIENT = std("block.beacon.ambient"),
            BLOCK_BEEHIVE_DRIP = std("block.beehive.drip"),
            BLOCK_BEEHIVE_ENTER = std("block.beehive.enter"),
            BLOCK_BEEHIVE_EXIT = std("block.beehive.exit"),
            BLOCK_BEEHIVE_SHEAR = std("block.beehive.shear"),
            BLOCK_BEEHIVE_WORK = std("block.beehive.work"),
            BLOCK_BELL_RESONATE = std("block.bell.resonate"),
            BLOCK_BELL_USE = std("block.bell.use"),
            BLOCK_BIG_DRIPLEAF_BREAK = std("block.big_dripleaf.break"),
            BLOCK_BIG_DRIPLEAF_FALL = std("block.big_dripleaf.fall"),
            BLOCK_BIG_DRIPLEAF_HIT = std("block.big_dripleaf.hit"),
            BLOCK_BIG_DRIPLEAF_PLACE = std("block.big_dripleaf.place"),
            BLOCK_BIG_DRIPLEAF_STEP = std("block.big_dripleaf.step"),
            BLOCK_BIG_DRIPLEAF_TILT_DOWN = std("block.big_dripleaf.tilt_down"),
            BLOCK_BIG_DRIPLEAF_TILT_UP = std("block.big_dripleaf.tilt_up"),
            BLOCK_BLASTFURNACE_FIRE_CRACKLE = std("block.blastfurnace.fire_crackle"),
            BLOCK_BONE_BLOCK_BREAK = std("block.bone_block.break"),
            BLOCK_BONE_BLOCK_FALL = std("block.bone_block.fall"),
            BLOCK_BONE_BLOCK_HIT = std("block.bone_block.hit"),
            BLOCK_BONE_BLOCK_PLACE = std("block.bone_block.place"),
            BLOCK_BONE_BLOCK_STEP = std("block.bone_block.step"),
            BLOCK_BREWING_STAND_BREW = std("block.brewing_stand.brew"),
            BLOCK_BUBBLE_COLUMN_BUBBLE_POP = std("block.bubble_column.bubble_pop"),
            BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT = std("block.bubble_column.upwards_ambient"),
            BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE = std("block.bubble_column.upwards_inside"),
            BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT = std("block.bubble_column.whirlpool_ambient"),
            BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE = std("block.bubble_column.whirlpool_inside"),
            BLOCK_CAKE_ADD_CANDLE = std("block.cake.add_candle"),
            BLOCK_CALCITE_BREAK = std("block.calcite.break"),
            BLOCK_CALCITE_FALL = std("block.calcite.fall"),
            BLOCK_CALCITE_HIT = std("block.calcite.hit"),
            BLOCK_CALCITE_PLACE = std("block.calcite.place"),
            BLOCK_CALCITE_STEP = std("block.calcite.step"),
            BLOCK_CAMPFIRE_CRACKLE = std("block.campfire.crackle"),
            BLOCK_CANDLE_AMBIENT = std("block.candle.ambient"),
            BLOCK_CANDLE_BREAK = std("block.candle.break"),
            BLOCK_CANDLE_EXTINGUISH = std("block.candle.extinguish"),
            BLOCK_CANDLE_FALL = std("block.candle.fall"),
            BLOCK_CANDLE_HIT = std("block.candle.hit"),
            BLOCK_CANDLE_PLACE = std("block.candle.place"),
            BLOCK_CANDLE_STEP = std("block.candle.step"),
            BLOCK_CAVE_VINES_BREAK = std("block.cave_vines.break"),
            BLOCK_CAVE_VINES_FALL = std("block.cave_vines.fall"),
            BLOCK_CAVE_VINES_HIT = std("block.cave_vines.hit"),
            BLOCK_CAVE_VINES_PICK_BERRIES = std("block.cave_vines.pick_berries"),
            BLOCK_CAVE_VINES_PLACE = std("block.cave_vines.place"),
            BLOCK_CAVE_VINES_STEP = std("block.cave_vines.step"),
            BLOCK_CHAIN_BREAK = std("block.chain.break"),
            BLOCK_CHAIN_FALL = std("block.chain.fall"),
            BLOCK_CHAIN_HIT = std("block.chain.hit"),
            BLOCK_CHAIN_PLACE = std("block.chain.place"),
            BLOCK_CHAIN_STEP = std("block.chain.step"),
            BLOCK_CHERRY_LEAVES_BREAK = std("block.cherry_leaves.break"),
            BLOCK_CHERRY_LEAVES_FALL = std("block.cherry_leaves.fall"),
            BLOCK_CHERRY_LEAVES_HIT = std("block.cherry_leaves.hit"),
            BLOCK_CHERRY_LEAVES_PLACE = std("block.cherry_leaves.place"),
            BLOCK_CHERRY_LEAVES_STEP = std("block.cherry_leaves.step"),
            BLOCK_CHERRY_SAPLING_BREAK = std("block.cherry_sapling.break"),
            BLOCK_CHERRY_SAPLING_FALL = std("block.cherry_sapling.fall"),
            BLOCK_CHERRY_SAPLING_HIT = std("block.cherry_sapling.hit"),
            BLOCK_CHERRY_SAPLING_PLACE = std("block.cherry_sapling.place"),
            BLOCK_CHERRY_SAPLING_STEP = std("block.cherry_sapling.step"),
            BLOCK_CHERRY_WOOD_BREAK = std("block.cherry_wood.break"),
            BLOCK_CHERRY_WOOD_BUTTON_CLICK_OFF = std("block.cherry_wood_button.click_off"),
            BLOCK_CHERRY_WOOD_BUTTON_CLICK_ON = std("block.cherry_wood_button.click_on"),
            BLOCK_CHERRY_WOOD_DOOR_CLOSE = std("block.cherry_wood_door.close"),
            BLOCK_CHERRY_WOOD_DOOR_OPEN = std("block.cherry_wood_door.open"),
            BLOCK_CHERRY_WOOD_FALL = std("block.cherry_wood.fall"),
            BLOCK_CHERRY_WOOD_FENCE_GATE_CLOSE = std("block.cherry_wood_fence_gate.close"),
            BLOCK_CHERRY_WOOD_FENCE_GATE_OPEN = std("block.cherry_wood_fence_gate.open"),
            BLOCK_CHERRY_WOOD_HANGING_SIGN_BREAK = std("block.cherry_wood_hanging_sign.break"),
            BLOCK_CHERRY_WOOD_HANGING_SIGN_FALL = std("block.cherry_wood_hanging_sign.fall"),
            BLOCK_CHERRY_WOOD_HANGING_SIGN_HIT = std("block.cherry_wood_hanging_sign.hit"),
            BLOCK_CHERRY_WOOD_HANGING_SIGN_PLACE = std("block.cherry_wood_hanging_sign.place"),
            BLOCK_CHERRY_WOOD_HANGING_SIGN_STEP = std("block.cherry_wood_hanging_sign.step"),
            BLOCK_CHERRY_WOOD_HIT = std("block.cherry_wood.hit"),
            BLOCK_CHERRY_WOOD_PLACE = std("block.cherry_wood.place"),
            BLOCK_CHERRY_WOOD_PRESSURE_PLATE_CLICK_OFF = std("block.cherry_wood_pressure_plate.click_off"),
            BLOCK_CHERRY_WOOD_PRESSURE_PLATE_CLICK_ON = std("block.cherry_wood_pressure_plate.click_on"),
            BLOCK_CHERRY_WOOD_STEP = std("block.cherry_wood.step"),
            BLOCK_CHERRY_WOOD_TRAPDOOR_CLOSE = std("block.cherry_wood_trapdoor.close"),
            BLOCK_CHERRY_WOOD_TRAPDOOR_OPEN = std("block.cherry_wood_trapdoor.open"),
            BLOCK_CHEST_LOCKED = std("block.chest.locked"),
            BLOCK_CHISELED_BOOKSHELF_BREAK = std("block.chiseled_bookshelf.break"),
            BLOCK_CHISELED_BOOKSHELF_FALL = std("block.chiseled_bookshelf.fall"),
            BLOCK_CHISELED_BOOKSHELF_HIT = std("block.chiseled_bookshelf.hit"),
            BLOCK_CHISELED_BOOKSHELF_INSERT = std("block.chiseled_bookshelf.insert"),
            BLOCK_CHISELED_BOOKSHELF_INSERT_ENCHANTED = std("block.chiseled_bookshelf.insert.enchanted"),
            BLOCK_CHISELED_BOOKSHELF_PICKUP = std("block.chiseled_bookshelf.pickup"),
            BLOCK_CHISELED_BOOKSHELF_PICKUP_ENCHANTED = std("block.chiseled_bookshelf.pickup.enchanted"),
            BLOCK_CHISELED_BOOKSHELF_PLACE = std("block.chiseled_bookshelf.place"),
            BLOCK_CHISELED_BOOKSHELF_STEP = std("block.chiseled_bookshelf.step"),
            BLOCK_CHORUS_FLOWER_DEATH = std("block.chorus_flower.death"),
            BLOCK_CHORUS_FLOWER_GROW = std("block.chorus_flower.grow"),
            BLOCK_COBWEB_BREAK = std("block.cobweb.break"),
            BLOCK_COBWEB_FALL = std("block.cobweb.fall"),
            BLOCK_COBWEB_HIT = std("block.cobweb.hit"),
            BLOCK_COBWEB_PLACE = std("block.cobweb.place"),
            BLOCK_COBWEB_STEP = std("block.cobweb.step"),
            BLOCK_COMPARATOR_CLICK = std("block.comparator.click"),
            BLOCK_COMPOSTER_EMPTY = std("block.composter.empty"),
            BLOCK_COMPOSTER_FILL = std("block.composter.fill"),
            BLOCK_COMPOSTER_FILL_SUCCESS = std("block.composter.fill_success"),
            BLOCK_COMPOSTER_READY = std("block.composter.ready"),
            BLOCK_CONDUIT_ACTIVATE = std("block.conduit.activate"),
            BLOCK_CONDUIT_AMBIENT = std("block.conduit.ambient"),
            BLOCK_CONDUIT_AMBIENT_SHORT = std("block.conduit.ambient.short"),
            BLOCK_CONDUIT_ATTACK_TARGET = std("block.conduit.attack.target"),
            BLOCK_CONDUIT_DEACTIVATE = std("block.conduit.deactivate"),
            BLOCK_COPPER_BREAK = std("block.copper.break"),
            BLOCK_COPPER_BULB_BREAK = std("block.copper_bulb.break"),
            BLOCK_COPPER_BULB_FALL = std("block.copper_bulb.fall"),
            BLOCK_COPPER_BULB_HIT = std("block.copper_bulb.hit"),
            BLOCK_COPPER_BULB_PLACE = std("block.copper_bulb.place"),
            BLOCK_COPPER_BULB_STEP = std("block.copper_bulb.step"),
            BLOCK_COPPER_BULB_TURN_OFF = std("block.copper_bulb.turn_off"),
            BLOCK_COPPER_BULB_TURN_ON = std("block.copper_bulb.turn_on"),
            BLOCK_COPPER_DOOR_CLOSE = std("block.copper_door.close"),
            BLOCK_COPPER_DOOR_OPEN = std("block.copper_door.open"),
            BLOCK_COPPER_FALL = std("block.copper.fall"),
            BLOCK_COPPER_GRATE_BREAK = std("block.copper_grate.break"),
            BLOCK_COPPER_GRATE_FALL = std("block.copper_grate.fall"),
            BLOCK_COPPER_GRATE_HIT = std("block.copper_grate.hit"),
            BLOCK_COPPER_GRATE_PLACE = std("block.copper_grate.place"),
            BLOCK_COPPER_GRATE_STEP = std("block.copper_grate.step"),
            BLOCK_COPPER_HIT = std("block.copper.hit"),
            BLOCK_COPPER_PLACE = std("block.copper.place"),
            BLOCK_COPPER_STEP = std("block.copper.step"),
            BLOCK_COPPER_TRAPDOOR_CLOSE = std("block.copper_trapdoor.close"),
            BLOCK_COPPER_TRAPDOOR_OPEN = std("block.copper_trapdoor.open"),
            BLOCK_CORAL_BLOCK_BREAK = std("block.coral_block.break"),
            BLOCK_CORAL_BLOCK_FALL = std("block.coral_block.fall"),
            BLOCK_CORAL_BLOCK_HIT = std("block.coral_block.hit"),
            BLOCK_CORAL_BLOCK_PLACE = std("block.coral_block.place"),
            BLOCK_CORAL_BLOCK_STEP = std("block.coral_block.step"),
            BLOCK_CRAFTER_CRAFT = std("block.crafter.craft"),
            BLOCK_CRAFTER_FAIL = std("block.crafter.fail"),
            BLOCK_CREAKING_HEART_BREAK = std("block.creaking_heart.break"),
            BLOCK_CREAKING_HEART_FALL = std("block.creaking_heart.fall"),
            BLOCK_CREAKING_HEART_HIT = std("block.creaking_heart.hit"),
            BLOCK_CREAKING_HEART_HURT = std("block.creaking_heart.hurt"),
            BLOCK_CREAKING_HEART_IDLE = std("block.creaking_heart.idle"),
            BLOCK_CREAKING_HEART_PLACE = std("block.creaking_heart.place"),
            BLOCK_CREAKING_HEART_SPAWN = std("block.creaking_heart.spawn"),
            BLOCK_CREAKING_HEART_STEP = std("block.creaking_heart.step"),
            BLOCK_CROP_BREAK = std("block.crop.break"),
            BLOCK_DECORATED_POT_BREAK = std("block.decorated_pot.break"),
            BLOCK_DECORATED_POT_FALL = std("block.decorated_pot.fall"),
            BLOCK_DECORATED_POT_HIT = std("block.decorated_pot.hit"),
            BLOCK_DECORATED_POT_INSERT = std("block.decorated_pot.insert"),
            BLOCK_DECORATED_POT_INSERT_FAIL = std("block.decorated_pot.insert_fail"),
            BLOCK_DECORATED_POT_PLACE = std("block.decorated_pot.place"),
            BLOCK_DECORATED_POT_SHATTER = std("block.decorated_pot.shatter"),
            BLOCK_DECORATED_POT_STEP = std("block.decorated_pot.step"),
            BLOCK_DEEPSLATE_BREAK = std("block.deepslate.break"),
            BLOCK_DEEPSLATE_BRICKS_BREAK = std("block.deepslate_bricks.break"),
            BLOCK_DEEPSLATE_BRICKS_FALL = std("block.deepslate_bricks.fall"),
            BLOCK_DEEPSLATE_BRICKS_HIT = std("block.deepslate_bricks.hit"),
            BLOCK_DEEPSLATE_BRICKS_PLACE = std("block.deepslate_bricks.place"),
            BLOCK_DEEPSLATE_BRICKS_STEP = std("block.deepslate_bricks.step"),
            BLOCK_DEEPSLATE_FALL = std("block.deepslate.fall"),
            BLOCK_DEEPSLATE_HIT = std("block.deepslate.hit"),
            BLOCK_DEEPSLATE_PLACE = std("block.deepslate.place"),
            BLOCK_DEEPSLATE_STEP = std("block.deepslate.step"),
            BLOCK_DEEPSLATE_TILES_BREAK = std("block.deepslate_tiles.break"),
            BLOCK_DEEPSLATE_TILES_FALL = std("block.deepslate_tiles.fall"),
            BLOCK_DEEPSLATE_TILES_HIT = std("block.deepslate_tiles.hit"),
            BLOCK_DEEPSLATE_TILES_PLACE = std("block.deepslate_tiles.place"),
            BLOCK_DEEPSLATE_TILES_STEP = std("block.deepslate_tiles.step"),
            BLOCK_DISPENSER_DISPENSE = std("block.dispenser.dispense"),
            BLOCK_DISPENSER_FAIL = std("block.dispenser.fail"),
            BLOCK_DISPENSER_LAUNCH = std("block.dispenser.launch"),
            BLOCK_DRIPSTONE_BLOCK_BREAK = std("block.dripstone_block.break"),
            BLOCK_DRIPSTONE_BLOCK_FALL = std("block.dripstone_block.fall"),
            BLOCK_DRIPSTONE_BLOCK_HIT = std("block.dripstone_block.hit"),
            BLOCK_DRIPSTONE_BLOCK_PLACE = std("block.dripstone_block.place"),
            BLOCK_DRIPSTONE_BLOCK_STEP = std("block.dripstone_block.step"),
            BLOCK_ENCHANTMENT_TABLE_USE = std("block.enchantment_table.use"),
            BLOCK_ENDER_CHEST_CLOSE = std("block.ender_chest.close", "BLOCK_ENDERCHEST_CLOSE"),
            BLOCK_ENDER_CHEST_OPEN = std("block.ender_chest.open", "BLOCK_ENDERCHEST_OPEN"),
            BLOCK_END_GATEWAY_SPAWN = std("block.end_gateway.spawn"),
            BLOCK_END_PORTAL_FRAME_FILL = std("block.end_portal_frame.fill"),
            BLOCK_END_PORTAL_SPAWN = std("block.end_portal.spawn"),
            BLOCK_FENCE_GATE_CLOSE = std("block.fence_gate.close"),
            BLOCK_FENCE_GATE_OPEN = std("block.fence_gate.open"),
            BLOCK_FLOWERING_AZALEA_BREAK = std("block.flowering_azalea.break"),
            BLOCK_FLOWERING_AZALEA_FALL = std("block.flowering_azalea.fall"),
            BLOCK_FLOWERING_AZALEA_HIT = std("block.flowering_azalea.hit"),
            BLOCK_FLOWERING_AZALEA_PLACE = std("block.flowering_azalea.place"),
            BLOCK_FLOWERING_AZALEA_STEP = std("block.flowering_azalea.step"),
            BLOCK_FROGLIGHT_BREAK = std("block.froglight.break"),
            BLOCK_FROGLIGHT_FALL = std("block.froglight.fall"),
            BLOCK_FROGLIGHT_HIT = std("block.froglight.hit"),
            BLOCK_FROGLIGHT_PLACE = std("block.froglight.place"),
            BLOCK_FROGLIGHT_STEP = std("block.froglight.step"),
            BLOCK_FROGSPAWN_BREAK = std("block.frogspawn.break"),
            BLOCK_FROGSPAWN_FALL = std("block.frogspawn.fall"),
            BLOCK_FROGSPAWN_HATCH = std("block.frogspawn.hatch"),
            BLOCK_FROGSPAWN_HIT = std("block.frogspawn.hit"),
            BLOCK_FROGSPAWN_PLACE = std("block.frogspawn.place"),
            BLOCK_FROGSPAWN_STEP = std("block.frogspawn.step"),
            BLOCK_FUNGUS_BREAK = std("block.fungus.break"),
            BLOCK_FUNGUS_FALL = std("block.fungus.fall"),
            BLOCK_FUNGUS_HIT = std("block.fungus.hit"),
            BLOCK_FUNGUS_PLACE = std("block.fungus.place"),
            BLOCK_FUNGUS_STEP = std("block.fungus.step"),
            BLOCK_FURNACE_FIRE_CRACKLE = std("block.furnace.fire_crackle"),
            BLOCK_GILDED_BLACKSTONE_BREAK = std("block.gilded_blackstone.break"),
            BLOCK_GILDED_BLACKSTONE_FALL = std("block.gilded_blackstone.fall"),
            BLOCK_GILDED_BLACKSTONE_HIT = std("block.gilded_blackstone.hit"),
            BLOCK_GILDED_BLACKSTONE_PLACE = std("block.gilded_blackstone.place"),
            BLOCK_GILDED_BLACKSTONE_STEP = std("block.gilded_blackstone.step"),
            BLOCK_GLASS_FALL = std("block.glass.fall"),
            BLOCK_GLASS_HIT = std("block.glass.hit"),
            BLOCK_GLASS_PLACE = std("block.glass.place"),
            BLOCK_GLASS_STEP = std("block.glass.step"),
            BLOCK_GRASS_FALL = std("block.grass.fall"),
            BLOCK_GRASS_HIT = std("block.grass.hit"),
            BLOCK_GRASS_PLACE = std("block.grass.place"),
            BLOCK_GRAVEL_FALL = std("block.gravel.fall"),
            BLOCK_GRAVEL_HIT = std("block.gravel.hit"),
            BLOCK_GRAVEL_PLACE = std("block.gravel.place"),
            BLOCK_GRINDSTONE_USE = std("block.grindstone.use"),
            BLOCK_GROWING_PLANT_CROP = std("block.growing_plant.crop"),
            BLOCK_HANGING_ROOTS_BREAK = std("block.hanging_roots.break"),
            BLOCK_HANGING_ROOTS_FALL = std("block.hanging_roots.fall"),
            BLOCK_HANGING_ROOTS_HIT = std("block.hanging_roots.hit"),
            BLOCK_HANGING_ROOTS_PLACE = std("block.hanging_roots.place"),
            BLOCK_HANGING_ROOTS_STEP = std("block.hanging_roots.step"),
            BLOCK_HANGING_SIGN_BREAK = std("block.hanging_sign.break"),
            BLOCK_HANGING_SIGN_FALL = std("block.hanging_sign.fall"),
            BLOCK_HANGING_SIGN_HIT = std("block.hanging_sign.hit"),
            BLOCK_HANGING_SIGN_PLACE = std("block.hanging_sign.place"),
            BLOCK_HANGING_SIGN_STEP = std("block.hanging_sign.step"),
            BLOCK_HANGING_SIGN_WAXED_INTERACT_FAIL = std("block.hanging_sign.waxed_interact_fail"),
            BLOCK_HEAVY_CORE_BREAK = std("block.heavy_core.break"),
            BLOCK_HEAVY_CORE_FALL = std("block.heavy_core.fall"),
            BLOCK_HEAVY_CORE_HIT = std("block.heavy_core.hit"),
            BLOCK_HEAVY_CORE_PLACE = std("block.heavy_core.place"),
            BLOCK_HEAVY_CORE_STEP = std("block.heavy_core.step"),
            BLOCK_HONEY_BLOCK_BREAK = std("block.honey_block.break"),
            BLOCK_HONEY_BLOCK_FALL = std("block.honey_block.fall"),
            BLOCK_HONEY_BLOCK_HIT = std("block.honey_block.hit"),
            BLOCK_HONEY_BLOCK_PLACE = std("block.honey_block.place"),
            BLOCK_HONEY_BLOCK_SLIDE = std("block.honey_block.slide"),
            BLOCK_HONEY_BLOCK_STEP = std("block.honey_block.step"),
            BLOCK_IRON_DOOR_CLOSE = std("block.iron_door.close"),
            BLOCK_IRON_DOOR_OPEN = std("block.iron_door.open"),
            BLOCK_IRON_TRAPDOOR_CLOSE = std("block.iron_trapdoor.close"),
            BLOCK_IRON_TRAPDOOR_OPEN = std("block.iron_trapdoor.open"),
            BLOCK_LADDER_BREAK = std("block.ladder.break"),
            BLOCK_LADDER_FALL = std("block.ladder.fall"),
            BLOCK_LADDER_HIT = std("block.ladder.hit"),
            BLOCK_LADDER_PLACE = std("block.ladder.place"),
            BLOCK_LANTERN_BREAK = std("block.lantern.break"),
            BLOCK_LANTERN_FALL = std("block.lantern.fall"),
            BLOCK_LANTERN_HIT = std("block.lantern.hit"),
            BLOCK_LANTERN_PLACE = std("block.lantern.place"),
            BLOCK_LANTERN_STEP = std("block.lantern.step"),
            BLOCK_LARGE_AMETHYST_BUD_BREAK = std("block.large_amethyst_bud.break"),
            BLOCK_LARGE_AMETHYST_BUD_PLACE = std("block.large_amethyst_bud.place"),
            BLOCK_LAVA_EXTINGUISH = std("block.lava.extinguish"),
            BLOCK_LEVER_CLICK = std("block.lever.click"),
            BLOCK_LODESTONE_BREAK = std("block.lodestone.break"),
            BLOCK_LODESTONE_FALL = std("block.lodestone.fall"),
            BLOCK_LODESTONE_HIT = std("block.lodestone.hit"),
            BLOCK_LODESTONE_PLACE = std("block.lodestone.place"),
            BLOCK_LODESTONE_STEP = std("block.lodestone.step"),
            BLOCK_MANGROVE_ROOTS_BREAK = std("block.mangrove_roots.break"),
            BLOCK_MANGROVE_ROOTS_FALL = std("block.mangrove_roots.fall"),
            BLOCK_MANGROVE_ROOTS_HIT = std("block.mangrove_roots.hit"),
            BLOCK_MANGROVE_ROOTS_PLACE = std("block.mangrove_roots.place"),
            BLOCK_MANGROVE_ROOTS_STEP = std("block.mangrove_roots.step"),
            BLOCK_MEDIUM_AMETHYST_BUD_BREAK = std("block.medium_amethyst_bud.break"),
            BLOCK_MEDIUM_AMETHYST_BUD_PLACE = std("block.medium_amethyst_bud.place"),
            BLOCK_METAL_BREAK = std("block.metal.break"),
            BLOCK_METAL_FALL = std("block.metal.fall"),
            BLOCK_METAL_HIT = std("block.metal.hit"),
            BLOCK_METAL_PLACE = std("block.metal.place"),
            BLOCK_METAL_STEP = std("block.metal.step"),
            BLOCK_MOSS_BREAK = std("block.moss.break"),
            BLOCK_MOSS_CARPET_BREAK = std("block.moss_carpet.break"),
            BLOCK_MOSS_CARPET_FALL = std("block.moss_carpet.fall"),
            BLOCK_MOSS_CARPET_HIT = std("block.moss_carpet.hit"),
            BLOCK_MOSS_CARPET_PLACE = std("block.moss_carpet.place"),
            BLOCK_MOSS_CARPET_STEP = std("block.moss_carpet.step"),
            BLOCK_MOSS_FALL = std("block.moss.fall"),
            BLOCK_MOSS_HIT = std("block.moss.hit"),
            BLOCK_MOSS_PLACE = std("block.moss.place"),
            BLOCK_MOSS_STEP = std("block.moss.step"),
            BLOCK_MUDDY_MANGROVE_ROOTS_BREAK = std("block.muddy_mangrove_roots.break"),
            BLOCK_MUDDY_MANGROVE_ROOTS_FALL = std("block.muddy_mangrove_roots.fall"),
            BLOCK_MUDDY_MANGROVE_ROOTS_HIT = std("block.muddy_mangrove_roots.hit"),
            BLOCK_MUDDY_MANGROVE_ROOTS_PLACE = std("block.muddy_mangrove_roots.place"),
            BLOCK_MUDDY_MANGROVE_ROOTS_STEP = std("block.muddy_mangrove_roots.step"),
            BLOCK_MUD_BREAK = std("block.mud.break"),
            BLOCK_MUD_BRICKS_BREAK = std("block.mud_bricks.break"),
            BLOCK_MUD_BRICKS_FALL = std("block.mud_bricks.fall"),
            BLOCK_MUD_BRICKS_HIT = std("block.mud_bricks.hit"),
            BLOCK_MUD_BRICKS_PLACE = std("block.mud_bricks.place"),
            BLOCK_MUD_BRICKS_STEP = std("block.mud_bricks.step"),
            BLOCK_MUD_FALL = std("block.mud.fall"),
            BLOCK_MUD_HIT = std("block.mud.hit"),
            BLOCK_MUD_PLACE = std("block.mud.place"),
            BLOCK_MUD_STEP = std("block.mud.step"),
            BLOCK_NETHERITE_BLOCK_BREAK = std("block.netherite_block.break"),
            BLOCK_NETHERITE_BLOCK_FALL = std("block.netherite_block.fall"),
            BLOCK_NETHERITE_BLOCK_HIT = std("block.netherite_block.hit"),
            BLOCK_NETHERITE_BLOCK_PLACE = std("block.netherite_block.place"),
            BLOCK_NETHERITE_BLOCK_STEP = std("block.netherite_block.step"),
            BLOCK_NETHERRACK_BREAK = std("block.netherrack.break"),
            BLOCK_NETHERRACK_FALL = std("block.netherrack.fall"),
            BLOCK_NETHERRACK_HIT = std("block.netherrack.hit"),
            BLOCK_NETHERRACK_PLACE = std("block.netherrack.place"),
            BLOCK_NETHERRACK_STEP = std("block.netherrack.step"),
            BLOCK_NETHER_BRICKS_BREAK = std("block.nether_bricks.break"),
            BLOCK_NETHER_BRICKS_FALL = std("block.nether_bricks.fall"),
            BLOCK_NETHER_BRICKS_HIT = std("block.nether_bricks.hit"),
            BLOCK_NETHER_BRICKS_PLACE = std("block.nether_bricks.place"),
            BLOCK_NETHER_BRICKS_STEP = std("block.nether_bricks.step"),
            BLOCK_NETHER_GOLD_ORE_BREAK = std("block.nether_gold_ore.break"),
            BLOCK_NETHER_GOLD_ORE_FALL = std("block.nether_gold_ore.fall"),
            BLOCK_NETHER_GOLD_ORE_HIT = std("block.nether_gold_ore.hit"),
            BLOCK_NETHER_GOLD_ORE_PLACE = std("block.nether_gold_ore.place"),
            BLOCK_NETHER_GOLD_ORE_STEP = std("block.nether_gold_ore.step"),
            BLOCK_NETHER_ORE_BREAK = std("block.nether_ore.break"),
            BLOCK_NETHER_ORE_FALL = std("block.nether_ore.fall"),
            BLOCK_NETHER_ORE_HIT = std("block.nether_ore.hit"),
            BLOCK_NETHER_ORE_PLACE = std("block.nether_ore.place"),
            BLOCK_NETHER_ORE_STEP = std("block.nether_ore.step"),
            BLOCK_NETHER_SPROUTS_BREAK = std("block.nether_sprouts.break"),
            BLOCK_NETHER_SPROUTS_FALL = std("block.nether_sprouts.fall"),
            BLOCK_NETHER_SPROUTS_HIT = std("block.nether_sprouts.hit"),
            BLOCK_NETHER_SPROUTS_PLACE = std("block.nether_sprouts.place"),
            BLOCK_NETHER_SPROUTS_STEP = std("block.nether_sprouts.step"),
            BLOCK_NETHER_WART_BREAK = std("block.nether_wart.break"),
            BLOCK_NETHER_WOOD_BREAK = std("block.nether_wood.break"),
            BLOCK_NETHER_WOOD_BUTTON_CLICK_OFF = std("block.nether_wood_button.click_off"),
            BLOCK_NETHER_WOOD_BUTTON_CLICK_ON = std("block.nether_wood_button.click_on"),
            BLOCK_NETHER_WOOD_DOOR_CLOSE = std("block.nether_wood_door.close"),
            BLOCK_NETHER_WOOD_DOOR_OPEN = std("block.nether_wood_door.open"),
            BLOCK_NETHER_WOOD_FALL = std("block.nether_wood.fall"),
            BLOCK_NETHER_WOOD_FENCE_GATE_CLOSE = std("block.nether_wood_fence_gate.close"),
            BLOCK_NETHER_WOOD_FENCE_GATE_OPEN = std("block.nether_wood_fence_gate.open"),
            BLOCK_NETHER_WOOD_HANGING_SIGN_BREAK = std("block.nether_wood_hanging_sign.break"),
            BLOCK_NETHER_WOOD_HANGING_SIGN_FALL = std("block.nether_wood_hanging_sign.fall"),
            BLOCK_NETHER_WOOD_HANGING_SIGN_HIT = std("block.nether_wood_hanging_sign.hit"),
            BLOCK_NETHER_WOOD_HANGING_SIGN_PLACE = std("block.nether_wood_hanging_sign.place"),
            BLOCK_NETHER_WOOD_HANGING_SIGN_STEP = std("block.nether_wood_hanging_sign.step"),
            BLOCK_NETHER_WOOD_HIT = std("block.nether_wood.hit"),
            BLOCK_NETHER_WOOD_PLACE = std("block.nether_wood.place"),
            BLOCK_NETHER_WOOD_PRESSURE_PLATE_CLICK_OFF = std("block.nether_wood_pressure_plate.click_off"),
            BLOCK_NETHER_WOOD_PRESSURE_PLATE_CLICK_ON = std("block.nether_wood_pressure_plate.click_on"),
            BLOCK_NETHER_WOOD_STEP = std("block.nether_wood.step"),
            BLOCK_NETHER_WOOD_TRAPDOOR_CLOSE = std("block.nether_wood_trapdoor.close"),
            BLOCK_NETHER_WOOD_TRAPDOOR_OPEN = std("block.nether_wood_trapdoor.open"),
            BLOCK_NOTE_BLOCK_BANJO = std("block.note_block.banjo"),
            BLOCK_NOTE_BLOCK_BIT = std("block.note_block.bit"),
            BLOCK_NOTE_BLOCK_COW_BELL = std("block.note_block.cow_bell"),
            BLOCK_NOTE_BLOCK_DIDGERIDOO = std("block.note_block.didgeridoo"),
            BLOCK_NOTE_BLOCK_IMITATE_CREEPER = std("block.note_block.imitate.creeper"),
            BLOCK_NOTE_BLOCK_IMITATE_ENDER_DRAGON = std("block.note_block.imitate.ender_dragon"),
            BLOCK_NOTE_BLOCK_IMITATE_PIGLIN = std("block.note_block.imitate.piglin"),
            BLOCK_NOTE_BLOCK_IMITATE_SKELETON = std("block.note_block.imitate.skeleton"),
            BLOCK_NOTE_BLOCK_IMITATE_WITHER_SKELETON = std("block.note_block.imitate.wither_skeleton"),
            BLOCK_NOTE_BLOCK_IMITATE_ZOMBIE = std("block.note_block.imitate.zombie"),
            BLOCK_NOTE_BLOCK_IRON_XYLOPHONE = std("block.note_block.iron_xylophone"),
            BLOCK_NYLIUM_BREAK = std("block.nylium.break"),
            BLOCK_NYLIUM_FALL = std("block.nylium.fall"),
            BLOCK_NYLIUM_HIT = std("block.nylium.hit"),
            BLOCK_NYLIUM_PLACE = std("block.nylium.place"),
            BLOCK_NYLIUM_STEP = std("block.nylium.step"),
            BLOCK_PACKED_MUD_BREAK = std("block.packed_mud.break"),
            BLOCK_PACKED_MUD_FALL = std("block.packed_mud.fall"),
            BLOCK_PACKED_MUD_HIT = std("block.packed_mud.hit"),
            BLOCK_PACKED_MUD_PLACE = std("block.packed_mud.place"),
            BLOCK_PACKED_MUD_STEP = std("block.packed_mud.step"),
            BLOCK_PALE_HANGING_MOSS_IDLE = std("block.pale_hanging_moss.idle"),
            BLOCK_PINK_PETALS_BREAK = std("block.pink_petals.break"),
            BLOCK_PINK_PETALS_FALL = std("block.pink_petals.fall"),
            BLOCK_PINK_PETALS_HIT = std("block.pink_petals.hit"),
            BLOCK_PINK_PETALS_PLACE = std("block.pink_petals.place"),
            BLOCK_PINK_PETALS_STEP = std("block.pink_petals.step"),
            BLOCK_POINTED_DRIPSTONE_BREAK = std("block.pointed_dripstone.break"),
            BLOCK_POINTED_DRIPSTONE_DRIP_LAVA = std("block.pointed_dripstone.drip_lava"),
            BLOCK_POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON = std("block.pointed_dripstone.drip_lava_into_cauldron"),
            BLOCK_POINTED_DRIPSTONE_DRIP_WATER = std("block.pointed_dripstone.drip_water"),
            BLOCK_POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON = std("block.pointed_dripstone.drip_water_into_cauldron"),
            BLOCK_POINTED_DRIPSTONE_FALL = std("block.pointed_dripstone.fall"),
            BLOCK_POINTED_DRIPSTONE_HIT = std("block.pointed_dripstone.hit"),
            BLOCK_POINTED_DRIPSTONE_LAND = std("block.pointed_dripstone.land"),
            BLOCK_POINTED_DRIPSTONE_PLACE = std("block.pointed_dripstone.place"),
            BLOCK_POINTED_DRIPSTONE_STEP = std("block.pointed_dripstone.step"),
            BLOCK_POLISHED_DEEPSLATE_BREAK = std("block.polished_deepslate.break"),
            BLOCK_POLISHED_DEEPSLATE_FALL = std("block.polished_deepslate.fall"),
            BLOCK_POLISHED_DEEPSLATE_HIT = std("block.polished_deepslate.hit"),
            BLOCK_POLISHED_DEEPSLATE_PLACE = std("block.polished_deepslate.place"),
            BLOCK_POLISHED_DEEPSLATE_STEP = std("block.polished_deepslate.step"),
            BLOCK_POLISHED_TUFF_BREAK = std("block.polished_tuff.break"),
            BLOCK_POLISHED_TUFF_FALL = std("block.polished_tuff.fall"),
            BLOCK_POLISHED_TUFF_HIT = std("block.polished_tuff.hit"),
            BLOCK_POLISHED_TUFF_PLACE = std("block.polished_tuff.place"),
            BLOCK_POLISHED_TUFF_STEP = std("block.polished_tuff.step"),
            BLOCK_POWDER_SNOW_BREAK = std("block.powder_snow.break"),
            BLOCK_POWDER_SNOW_FALL = std("block.powder_snow.fall"),
            BLOCK_POWDER_SNOW_HIT = std("block.powder_snow.hit"),
            BLOCK_POWDER_SNOW_PLACE = std("block.powder_snow.place"),
            BLOCK_POWDER_SNOW_STEP = std("block.powder_snow.step"),
            BLOCK_PUMPKIN_CARVE = std("block.pumpkin.carve"),
            BLOCK_REDSTONE_TORCH_BURNOUT = std("block.redstone_torch.burnout"),
            BLOCK_RESPAWN_ANCHOR_AMBIENT = std("block.respawn_anchor.ambient"),
            BLOCK_RESPAWN_ANCHOR_CHARGE = std("block.respawn_anchor.charge"),
            BLOCK_RESPAWN_ANCHOR_DEPLETE = std("block.respawn_anchor.deplete"),
            BLOCK_RESPAWN_ANCHOR_SET_SPAWN = std("block.respawn_anchor.set_spawn"),
            BLOCK_ROOTED_DIRT_BREAK = std("block.rooted_dirt.break"),
            BLOCK_ROOTED_DIRT_FALL = std("block.rooted_dirt.fall"),
            BLOCK_ROOTED_DIRT_HIT = std("block.rooted_dirt.hit"),
            BLOCK_ROOTED_DIRT_PLACE = std("block.rooted_dirt.place"),
            BLOCK_ROOTED_DIRT_STEP = std("block.rooted_dirt.step"),
            BLOCK_ROOTS_BREAK = std("block.roots.break"),
            BLOCK_ROOTS_FALL = std("block.roots.fall"),
            BLOCK_ROOTS_HIT = std("block.roots.hit"),
            BLOCK_ROOTS_PLACE = std("block.roots.place"),
            BLOCK_ROOTS_STEP = std("block.roots.step"),
            BLOCK_SAND_FALL = std("block.sand.fall"),
            BLOCK_SAND_HIT = std("block.sand.hit"),
            BLOCK_SAND_PLACE = std("block.sand.place"),
            BLOCK_SCAFFOLDING_BREAK = std("block.scaffolding.break"),
            BLOCK_SCAFFOLDING_FALL = std("block.scaffolding.fall"),
            BLOCK_SCAFFOLDING_HIT = std("block.scaffolding.hit"),
            BLOCK_SCAFFOLDING_PLACE = std("block.scaffolding.place"),
            BLOCK_SCAFFOLDING_STEP = std("block.scaffolding.step"),
            BLOCK_SCULK_BREAK = std("block.sculk.break"),
            BLOCK_SCULK_CATALYST_BLOOM = std("block.sculk_catalyst.bloom"),
            BLOCK_SCULK_CATALYST_BREAK = std("block.sculk_catalyst.break"),
            BLOCK_SCULK_CATALYST_FALL = std("block.sculk_catalyst.fall"),
            BLOCK_SCULK_CATALYST_HIT = std("block.sculk_catalyst.hit"),
            BLOCK_SCULK_CATALYST_PLACE = std("block.sculk_catalyst.place"),
            BLOCK_SCULK_CATALYST_STEP = std("block.sculk_catalyst.step"),
            BLOCK_SCULK_CHARGE = std("block.sculk.charge"),
            BLOCK_SCULK_FALL = std("block.sculk.fall"),
            BLOCK_SCULK_HIT = std("block.sculk.hit"),
            BLOCK_SCULK_PLACE = std("block.sculk.place"),
            BLOCK_SCULK_SENSOR_BREAK = std("block.sculk_sensor.break"),
            BLOCK_SCULK_SENSOR_CLICKING = std("block.sculk_sensor.clicking"),
            BLOCK_SCULK_SENSOR_CLICKING_STOP = std("block.sculk_sensor.clicking_stop"),
            BLOCK_SCULK_SENSOR_FALL = std("block.sculk_sensor.fall"),
            BLOCK_SCULK_SENSOR_HIT = std("block.sculk_sensor.hit"),
            BLOCK_SCULK_SENSOR_PLACE = std("block.sculk_sensor.place"),
            BLOCK_SCULK_SENSOR_STEP = std("block.sculk_sensor.step"),
            BLOCK_SCULK_SHRIEKER_BREAK = std("block.sculk_shrieker.break"),
            BLOCK_SCULK_SHRIEKER_FALL = std("block.sculk_shrieker.fall"),
            BLOCK_SCULK_SHRIEKER_HIT = std("block.sculk_shrieker.hit"),
            BLOCK_SCULK_SHRIEKER_PLACE = std("block.sculk_shrieker.place"),
            BLOCK_SCULK_SHRIEKER_SHRIEK = std("block.sculk_shrieker.shriek"),
            BLOCK_SCULK_SHRIEKER_STEP = std("block.sculk_shrieker.step"),
            BLOCK_SCULK_SPREAD = std("block.sculk.spread"),
            BLOCK_SCULK_STEP = std("block.sculk.step"),
            BLOCK_SCULK_VEIN_BREAK = std("block.sculk_vein.break"),
            BLOCK_SCULK_VEIN_FALL = std("block.sculk_vein.fall"),
            BLOCK_SCULK_VEIN_HIT = std("block.sculk_vein.hit"),
            BLOCK_SCULK_VEIN_PLACE = std("block.sculk_vein.place"),
            BLOCK_SCULK_VEIN_STEP = std("block.sculk_vein.step"),
            BLOCK_SHROOMLIGHT_BREAK = std("block.shroomlight.break"),
            BLOCK_SHROOMLIGHT_FALL = std("block.shroomlight.fall"),
            BLOCK_SHROOMLIGHT_HIT = std("block.shroomlight.hit"),
            BLOCK_SHROOMLIGHT_PLACE = std("block.shroomlight.place"),
            BLOCK_SHROOMLIGHT_STEP = std("block.shroomlight.step"),
            BLOCK_SHULKER_BOX_CLOSE = std("block.shulker_box.close"),
            BLOCK_SHULKER_BOX_OPEN = std("block.shulker_box.open"),
            BLOCK_SIGN_WAXED_INTERACT_FAIL = std("block.sign.waxed_interact_fail"),
            BLOCK_SMALL_AMETHYST_BUD_BREAK = std("block.small_amethyst_bud.break"),
            BLOCK_SMALL_AMETHYST_BUD_PLACE = std("block.small_amethyst_bud.place"),
            BLOCK_SMALL_DRIPLEAF_BREAK = std("block.small_dripleaf.break"),
            BLOCK_SMALL_DRIPLEAF_FALL = std("block.small_dripleaf.fall"),
            BLOCK_SMALL_DRIPLEAF_HIT = std("block.small_dripleaf.hit"),
            BLOCK_SMALL_DRIPLEAF_PLACE = std("block.small_dripleaf.place"),
            BLOCK_SMALL_DRIPLEAF_STEP = std("block.small_dripleaf.step"),
            BLOCK_SMITHING_TABLE_USE = std("block.smithing_table.use"),
            BLOCK_SMOKER_SMOKE = std("block.smoker.smoke"),
            BLOCK_SNIFFER_EGG_CRACK = std("block.sniffer_egg.crack"),
            BLOCK_SNIFFER_EGG_HATCH = std("block.sniffer_egg.hatch"),
            BLOCK_SNIFFER_EGG_PLOP = std("block.sniffer_egg.plop"),
            BLOCK_SNOW_FALL = std("block.snow.fall"),
            BLOCK_SNOW_HIT = std("block.snow.hit"),
            BLOCK_SNOW_PLACE = std("block.snow.place"),
            BLOCK_SOUL_SAND_BREAK = std("block.soul_sand.break"),
            BLOCK_SOUL_SAND_FALL = std("block.soul_sand.fall"),
            BLOCK_SOUL_SAND_HIT = std("block.soul_sand.hit"),
            BLOCK_SOUL_SAND_PLACE = std("block.soul_sand.place"),
            BLOCK_SOUL_SAND_STEP = std("block.soul_sand.step"),
            BLOCK_SOUL_SOIL_BREAK = std("block.soul_soil.break"),
            BLOCK_SOUL_SOIL_FALL = std("block.soul_soil.fall"),
            BLOCK_SOUL_SOIL_HIT = std("block.soul_soil.hit"),
            BLOCK_SOUL_SOIL_PLACE = std("block.soul_soil.place"),
            BLOCK_SOUL_SOIL_STEP = std("block.soul_soil.step"),
            BLOCK_SPAWNER_BREAK = std("block.spawner.break"),
            BLOCK_SPAWNER_FALL = std("block.spawner.fall"),
            BLOCK_SPAWNER_HIT = std("block.spawner.hit"),
            BLOCK_SPAWNER_PLACE = std("block.spawner.place"),
            BLOCK_SPAWNER_STEP = std("block.spawner.step"),
            BLOCK_SPONGE_ABSORB = std("block.sponge.absorb"),
            BLOCK_SPONGE_BREAK = std("block.sponge.break"),
            BLOCK_SPONGE_FALL = std("block.sponge.fall"),
            BLOCK_SPONGE_HIT = std("block.sponge.hit"),
            BLOCK_SPONGE_PLACE = std("block.sponge.place"),
            BLOCK_SPONGE_STEP = std("block.sponge.step"),
            BLOCK_SPORE_BLOSSOM_BREAK = std("block.spore_blossom.break"),
            BLOCK_SPORE_BLOSSOM_FALL = std("block.spore_blossom.fall"),
            BLOCK_SPORE_BLOSSOM_HIT = std("block.spore_blossom.hit"),
            BLOCK_SPORE_BLOSSOM_PLACE = std("block.spore_blossom.place"),
            BLOCK_SPORE_BLOSSOM_STEP = std("block.spore_blossom.step"),
            BLOCK_STEM_BREAK = std("block.stem.break"),
            BLOCK_STEM_FALL = std("block.stem.fall"),
            BLOCK_STEM_HIT = std("block.stem.hit"),
            BLOCK_STEM_PLACE = std("block.stem.place"),
            BLOCK_STEM_STEP = std("block.stem.step"),
            BLOCK_STONE_BUTTON_CLICK_OFF = std("block.stone_button.click_off"),
            BLOCK_STONE_BUTTON_CLICK_ON = std("block.stone_button.click_on"),
            BLOCK_STONE_FALL = std("block.stone.fall"),
            BLOCK_STONE_HIT = std("block.stone.hit"),
            BLOCK_STONE_PLACE = std("block.stone.place"),
            BLOCK_SUSPICIOUS_GRAVEL_BREAK = std("block.suspicious_gravel.break"),
            BLOCK_SUSPICIOUS_GRAVEL_FALL = std("block.suspicious_gravel.fall"),
            BLOCK_SUSPICIOUS_GRAVEL_HIT = std("block.suspicious_gravel.hit"),
            BLOCK_SUSPICIOUS_GRAVEL_PLACE = std("block.suspicious_gravel.place"),
            BLOCK_SUSPICIOUS_GRAVEL_STEP = std("block.suspicious_gravel.step"),
            BLOCK_SUSPICIOUS_SAND_BREAK = std("block.suspicious_sand.break"),
            BLOCK_SUSPICIOUS_SAND_FALL = std("block.suspicious_sand.fall"),
            BLOCK_SUSPICIOUS_SAND_HIT = std("block.suspicious_sand.hit"),
            BLOCK_SUSPICIOUS_SAND_PLACE = std("block.suspicious_sand.place"),
            BLOCK_SUSPICIOUS_SAND_STEP = std("block.suspicious_sand.step"),
            BLOCK_SWEET_BERRY_BUSH_BREAK = std("block.sweet_berry_bush.break"),
            BLOCK_SWEET_BERRY_BUSH_PLACE = std("block.sweet_berry_bush.place"),
            BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM = std("block.trial_spawner.about_to_spawn_item"),
            BLOCK_TRIAL_SPAWNER_AMBIENT = std("block.trial_spawner.ambient"),
            BLOCK_TRIAL_SPAWNER_AMBIENT_OMINOUS = std("block.trial_spawner.ambient_ominous"),
            BLOCK_TRIAL_SPAWNER_BREAK = std("block.trial_spawner.break"),
            BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER = std("block.trial_spawner.close_shutter"),
            BLOCK_TRIAL_SPAWNER_DETECT_PLAYER = std("block.trial_spawner.detect_player"),
            BLOCK_TRIAL_SPAWNER_EJECT_ITEM = std("block.trial_spawner.eject_item"),
            BLOCK_TRIAL_SPAWNER_FALL = std("block.trial_spawner.fall"),
            BLOCK_TRIAL_SPAWNER_HIT = std("block.trial_spawner.hit"),
            BLOCK_TRIAL_SPAWNER_OMINOUS_ACTIVATE = std("block.trial_spawner.ominous_activate"),
            BLOCK_TRIAL_SPAWNER_OPEN_SHUTTER = std("block.trial_spawner.open_shutter"),
            BLOCK_TRIAL_SPAWNER_PLACE = std("block.trial_spawner.place"),
            BLOCK_TRIAL_SPAWNER_SPAWN_ITEM = std("block.trial_spawner.spawn_item"),
            BLOCK_TRIAL_SPAWNER_SPAWN_ITEM_BEGIN = std("block.trial_spawner.spawn_item_begin"),
            BLOCK_TRIAL_SPAWNER_SPAWN_MOB = std("block.trial_spawner.spawn_mob"),
            BLOCK_TRIAL_SPAWNER_STEP = std("block.trial_spawner.step"),
            BLOCK_TRIPWIRE_ATTACH = std("block.tripwire.attach"),
            BLOCK_TRIPWIRE_CLICK_OFF = std("block.tripwire.click_off"),
            BLOCK_TRIPWIRE_CLICK_ON = std("block.tripwire.click_on"),
            BLOCK_TRIPWIRE_DETACH = std("block.tripwire.detach"),
            BLOCK_TUFF_BREAK = std("block.tuff.break"),
            BLOCK_TUFF_BRICKS_BREAK = std("block.tuff_bricks.break"),
            BLOCK_TUFF_BRICKS_FALL = std("block.tuff_bricks.fall"),
            BLOCK_TUFF_BRICKS_HIT = std("block.tuff_bricks.hit"),
            BLOCK_TUFF_BRICKS_PLACE = std("block.tuff_bricks.place"),
            BLOCK_TUFF_BRICKS_STEP = std("block.tuff_bricks.step"),
            BLOCK_TUFF_FALL = std("block.tuff.fall"),
            BLOCK_TUFF_HIT = std("block.tuff.hit"),
            BLOCK_TUFF_PLACE = std("block.tuff.place"),
            BLOCK_TUFF_STEP = std("block.tuff.step"),
            BLOCK_VAULT_ACTIVATE = std("block.vault.activate"),
            BLOCK_VAULT_AMBIENT = std("block.vault.ambient"),
            BLOCK_VAULT_BREAK = std("block.vault.break"),
            BLOCK_VAULT_CLOSE_SHUTTER = std("block.vault.close_shutter"),
            BLOCK_VAULT_DEACTIVATE = std("block.vault.deactivate"),
            BLOCK_VAULT_EJECT_ITEM = std("block.vault.eject_item"),
            BLOCK_VAULT_FALL = std("block.vault.fall"),
            BLOCK_VAULT_HIT = std("block.vault.hit"),
            BLOCK_VAULT_INSERT_ITEM = std("block.vault.insert_item"),
            BLOCK_VAULT_INSERT_ITEM_FAIL = std("block.vault.insert_item_fail"),
            BLOCK_VAULT_OPEN_SHUTTER = std("block.vault.open_shutter"),
            BLOCK_VAULT_PLACE = std("block.vault.place"),
            BLOCK_VAULT_REJECT_REWARDED_PLAYER = std("block.vault.reject_rewarded_player"),
            BLOCK_VAULT_STEP = std("block.vault.step"),
            BLOCK_VINE_BREAK = std("block.vine.break"),
            BLOCK_VINE_FALL = std("block.vine.fall"),
            BLOCK_VINE_HIT = std("block.vine.hit"),
            BLOCK_VINE_PLACE = std("block.vine.place"),
            BLOCK_VINE_STEP = std("block.vine.step"),
            BLOCK_WART_BLOCK_BREAK = std("block.wart_block.break"),
            BLOCK_WART_BLOCK_FALL = std("block.wart_block.fall"),
            BLOCK_WART_BLOCK_HIT = std("block.wart_block.hit"),
            BLOCK_WART_BLOCK_PLACE = std("block.wart_block.place"),
            BLOCK_WART_BLOCK_STEP = std("block.wart_block.step"),
            BLOCK_WEEPING_VINES_BREAK = std("block.weeping_vines.break"),
            BLOCK_WEEPING_VINES_FALL = std("block.weeping_vines.fall"),
            BLOCK_WEEPING_VINES_HIT = std("block.weeping_vines.hit"),
            BLOCK_WEEPING_VINES_PLACE = std("block.weeping_vines.place"),
            BLOCK_WEEPING_VINES_STEP = std("block.weeping_vines.step"),
            BLOCK_WET_GRASS_BREAK = std("block.wet_grass.break"),
            BLOCK_WET_GRASS_FALL = std("block.wet_grass.fall"),
            BLOCK_WET_GRASS_HIT = std("block.wet_grass.hit"),
            BLOCK_WET_SPONGE_BREAK = std("block.wet_sponge.break"),
            BLOCK_WET_SPONGE_DRIES = std("block.wet_sponge.dries"),
            BLOCK_WET_SPONGE_FALL = std("block.wet_sponge.fall"),
            BLOCK_WET_SPONGE_HIT = std("block.wet_sponge.hit"),
            BLOCK_WET_SPONGE_PLACE = std("block.wet_sponge.place"),
            BLOCK_WET_SPONGE_STEP = std("block.wet_sponge.step"),
            BLOCK_WOODEN_TRAPDOOR_CLOSE = std("block.wooden_trapdoor.close"),
            BLOCK_WOODEN_TRAPDOOR_OPEN = std("block.wooden_trapdoor.open"),
            BLOCK_WOOD_FALL = std("block.wood.fall"),
            BLOCK_WOOD_HIT = std("block.wood.hit"),
            BLOCK_WOOD_PLACE = std("block.wood.place"),
            BLOCK_WOOL_FALL = std("block.wool.fall", "BLOCK_WOOL_FALL", "BLOCK_CLOTH_FALL"),
            ENCHANT_THORNS_HIT = std("enchant.thorns.hit"),
            ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM = std("entity.allay.ambient_without_item"),
            ENTITY_ALLAY_AMBIENT_WITH_ITEM = std("entity.allay.ambient_with_item"),
            ENTITY_ALLAY_DEATH = std("entity.allay.death"),
            ENTITY_ALLAY_HURT = std("entity.allay.hurt"),
            ENTITY_ALLAY_ITEM_GIVEN = std("entity.allay.item_given"),
            ENTITY_ALLAY_ITEM_TAKEN = std("entity.allay.item_taken"),
            ENTITY_ALLAY_ITEM_THROWN = std("entity.allay.item_thrown"),
            ENTITY_ARMADILLO_AMBIENT = std("entity.armadillo.ambient"),
            ENTITY_ARMADILLO_BRUSH = std("entity.armadillo.brush"),
            ENTITY_ARMADILLO_DEATH = std("entity.armadillo.death"),
            ENTITY_ARMADILLO_EAT = std("entity.armadillo.eat"),
            ENTITY_ARMADILLO_HURT = std("entity.armadillo.hurt"),
            ENTITY_ARMADILLO_HURT_REDUCED = std("entity.armadillo.hurt_reduced"),
            ENTITY_ARMADILLO_LAND = std("entity.armadillo.land"),
            ENTITY_ARMADILLO_PEEK = std("entity.armadillo.peek"),
            ENTITY_ARMADILLO_ROLL = std("entity.armadillo.roll"),
            ENTITY_ARMADILLO_SCUTE_DROP = std("entity.armadillo.scute_drop"),
            ENTITY_ARMADILLO_STEP = std("entity.armadillo.step"),
            ENTITY_ARMADILLO_UNROLL_FINISH = std("entity.armadillo.unroll_finish"),
            ENTITY_ARMADILLO_UNROLL_START = std("entity.armadillo.unroll_start"),
            ENTITY_AXOLOTL_ATTACK = std("entity.axolotl.attack"),
            ENTITY_AXOLOTL_DEATH = std("entity.axolotl.death"),
            ENTITY_AXOLOTL_HURT = std("entity.axolotl.hurt"),
            ENTITY_AXOLOTL_IDLE_AIR = std("entity.axolotl.idle_air"),
            ENTITY_AXOLOTL_IDLE_WATER = std("entity.axolotl.idle_water"),
            ENTITY_AXOLOTL_SPLASH = std("entity.axolotl.splash"),
            ENTITY_AXOLOTL_SWIM = std("entity.axolotl.swim"),
            ENTITY_BEE_DEATH = std("entity.bee.death"),
            ENTITY_BEE_HURT = std("entity.bee.hurt"),
            ENTITY_BEE_LOOP = std("entity.bee.loop"),
            ENTITY_BEE_LOOP_AGGRESSIVE = std("entity.bee.loop_aggressive"),
            ENTITY_BEE_POLLINATE = std("entity.bee.pollinate"),
            ENTITY_BEE_STING = std("entity.bee.sting"),
            ENTITY_BLAZE_BURN = std("entity.blaze.burn"),
            ENTITY_BLAZE_SHOOT = std("entity.blaze.shoot"),
            ENTITY_BOAT_PADDLE_LAND = std("entity.boat.paddle_land"),
            ENTITY_BOAT_PADDLE_WATER = std("entity.boat.paddle_water"),
            ENTITY_BOGGED_AMBIENT = std("entity.bogged.ambient"),
            ENTITY_BOGGED_DEATH = std("entity.bogged.death"),
            ENTITY_BOGGED_HURT = std("entity.bogged.hurt"),
            ENTITY_BOGGED_SHEAR = std("entity.bogged.shear"),
            ENTITY_BOGGED_STEP = std("entity.bogged.step"),
            ENTITY_BREEZE_CHARGE = std("entity.breeze.charge"),
            ENTITY_BREEZE_DEATH = std("entity.breeze.death"),
            ENTITY_BREEZE_DEFLECT = std("entity.breeze.deflect"),
            ENTITY_BREEZE_HURT = std("entity.breeze.hurt"),
            ENTITY_BREEZE_IDLE_AIR = std("entity.breeze.idle_air"),
            ENTITY_BREEZE_IDLE_GROUND = std("entity.breeze.idle_ground"),
            ENTITY_BREEZE_INHALE = std("entity.breeze.inhale"),
            ENTITY_BREEZE_JUMP = std("entity.breeze.jump"),
            ENTITY_BREEZE_LAND = std("entity.breeze.land"),
            ENTITY_BREEZE_SHOOT = std("entity.breeze.shoot"),
            ENTITY_BREEZE_SLIDE = std("entity.breeze.slide"),
            ENTITY_BREEZE_WHIRL = std("entity.breeze.whirl"),
            ENTITY_BREEZE_WIND_BURST = std("entity.breeze.wind_burst"),
            ENTITY_CAMEL_AMBIENT = std("entity.camel.ambient"),
            ENTITY_CAMEL_DASH = std("entity.camel.dash"),
            ENTITY_CAMEL_DASH_READY = std("entity.camel.dash_ready"),
            ENTITY_CAMEL_DEATH = std("entity.camel.death"),
            ENTITY_CAMEL_EAT = std("entity.camel.eat"),
            ENTITY_CAMEL_HURT = std("entity.camel.hurt"),
            ENTITY_CAMEL_SADDLE = std("entity.camel.saddle"),
            ENTITY_CAMEL_SIT = std("entity.camel.sit"),
            ENTITY_CAMEL_STAND = std("entity.camel.stand"),
            ENTITY_CAMEL_STEP = std("entity.camel.step"),
            ENTITY_CAMEL_STEP_SAND = std("entity.camel.step_sand"),
            ENTITY_CAT_BEG_FOR_FOOD = std("entity.cat.beg_for_food"),
            ENTITY_CAT_DEATH = std("entity.cat.death"),
            ENTITY_CAT_STRAY_AMBIENT = std("entity.cat.stray_ambient"),
            ENTITY_CHICKEN_DEATH = std("entity.chicken.death"),
            ENTITY_COD_AMBIENT = std("entity.cod.ambient"),
            ENTITY_COD_DEATH = std("entity.cod.death"),
            ENTITY_COD_FLOP = std("entity.cod.flop"),
            ENTITY_COD_HURT = std("entity.cod.hurt"),
            ENTITY_COW_DEATH = std("entity.cow.death"),
            ENTITY_COW_MILK = std("entity.cow.milk"),
            ENTITY_CREAKING_ACTIVATE = std("entity.creaking.activate"),
            ENTITY_CREAKING_AMBIENT = std("entity.creaking.ambient"),
            ENTITY_CREAKING_ATTACK = std("entity.creaking.attack"),
            ENTITY_CREAKING_DEACTIVATE = std("entity.creaking.deactivate"),
            ENTITY_CREAKING_DEATH = std("entity.creaking.death"),
            ENTITY_CREAKING_FREEZE = std("entity.creaking.freeze"),
            ENTITY_CREAKING_SPAWN = std("entity.creaking.spawn"),
            ENTITY_CREAKING_STEP = std("entity.creaking.step"),
            ENTITY_CREAKING_SWAY = std("entity.creaking.sway"),
            ENTITY_CREAKING_UNFREEZE = std("entity.creaking.unfreeze"),
            ENTITY_CREEPER_HURT = std("entity.creeper.hurt"),
            ENTITY_DOLPHIN_AMBIENT = std("entity.dolphin.ambient"),
            ENTITY_DOLPHIN_AMBIENT_WATER = std("entity.dolphin.ambient_water"),
            ENTITY_DOLPHIN_ATTACK = std("entity.dolphin.attack"),
            ENTITY_DOLPHIN_DEATH = std("entity.dolphin.death"),
            ENTITY_DOLPHIN_EAT = std("entity.dolphin.eat"),
            ENTITY_DOLPHIN_HURT = std("entity.dolphin.hurt"),
            ENTITY_DOLPHIN_JUMP = std("entity.dolphin.jump"),
            ENTITY_DOLPHIN_PLAY = std("entity.dolphin.play"),
            ENTITY_DOLPHIN_SPLASH = std("entity.dolphin.splash"),
            ENTITY_DOLPHIN_SWIM = std("entity.dolphin.swim"),
            ENTITY_DONKEY_CHEST = std("entity.donkey.chest"),
            ENTITY_DONKEY_EAT = std("entity.donkey.eat"),
            ENTITY_DONKEY_JUMP = std("entity.donkey.jump"),
            ENTITY_DROWNED_AMBIENT = std("entity.drowned.ambient"),
            ENTITY_DROWNED_AMBIENT_WATER = std("entity.drowned.ambient_water"),
            ENTITY_DROWNED_DEATH = std("entity.drowned.death"),
            ENTITY_DROWNED_DEATH_WATER = std("entity.drowned.death_water"),
            ENTITY_DROWNED_HURT = std("entity.drowned.hurt"),
            ENTITY_DROWNED_HURT_WATER = std("entity.drowned.hurt_water"),
            ENTITY_DROWNED_SHOOT = std("entity.drowned.shoot"),
            ENTITY_DROWNED_STEP = std("entity.drowned.step"),
            ENTITY_DROWNED_SWIM = std("entity.drowned.swim"),
            ENTITY_EGG_THROW = std("entity.egg.throw"),
            ENTITY_ELDER_GUARDIAN_AMBIENT = std("entity.elder_guardian.ambient"),
            ENTITY_ELDER_GUARDIAN_AMBIENT_LAND = std("entity.elder_guardian.ambient_land"),
            ENTITY_ELDER_GUARDIAN_CURSE = std("entity.elder_guardian.curse"),
            ENTITY_ELDER_GUARDIAN_DEATH = std("entity.elder_guardian.death"),
            ENTITY_ELDER_GUARDIAN_DEATH_LAND = std("entity.elder_guardian.death_land"),
            ENTITY_ELDER_GUARDIAN_FLOP = std("entity.elder_guardian.flop"),
            ENTITY_ELDER_GUARDIAN_HURT = std("entity.elder_guardian.hurt"),
            ENTITY_ELDER_GUARDIAN_HURT_LAND = std("entity.elder_guardian.hurt_land"),
            ENTITY_ENDERMITE_AMBIENT = std("entity.endermite.ambient"),
            ENTITY_ENDERMITE_DEATH = std("entity.endermite.death"),
            ENTITY_ENDERMITE_HURT = std("entity.endermite.hurt"),
            ENTITY_ENDERMITE_STEP = std("entity.endermite.step"),
            ENTITY_ENDER_EYE_DEATH = std("entity.ender_eye.death", "ENTITY_ENDER_EYE_DEATH", "ENTITY_ENDEREYE_DEATH"),
            ENTITY_EVOKER_CELEBRATE = std("entity.evoker.celebrate"),
            ENTITY_EXPERIENCE_BOTTLE_THROW = std("entity.experience_bottle.throw"),
            ENTITY_FIREWORK_ROCKET_SHOOT = std("entity.firework_rocket.shoot", "ENTITY_FIREWORK_SHOOT"),
            ENTITY_FISHING_BOBBER_RETRIEVE = std("entity.fishing_bobber.retrieve", "ENTITY_BOBBER_RETRIEVE"),
            ENTITY_FISH_SWIM = std("entity.fish.swim"),
            ENTITY_FOX_AGGRO = std("entity.fox.aggro"),
            ENTITY_FOX_AMBIENT = std("entity.fox.ambient"),
            ENTITY_FOX_BITE = std("entity.fox.bite"),
            ENTITY_FOX_DEATH = std("entity.fox.death"),
            ENTITY_FOX_EAT = std("entity.fox.eat"),
            ENTITY_FOX_HURT = std("entity.fox.hurt"),
            ENTITY_FOX_SCREECH = std("entity.fox.screech"),
            ENTITY_FOX_SLEEP = std("entity.fox.sleep"),
            ENTITY_FOX_SNIFF = std("entity.fox.sniff"),
            ENTITY_FOX_SPIT = std("entity.fox.spit"),
            ENTITY_FOX_TELEPORT = std("entity.fox.teleport"),
            ENTITY_FROG_AMBIENT = std("entity.frog.ambient"),
            ENTITY_FROG_DEATH = std("entity.frog.death"),
            ENTITY_FROG_EAT = std("entity.frog.eat"),
            ENTITY_FROG_HURT = std("entity.frog.hurt"),
            ENTITY_FROG_LAY_SPAWN = std("entity.frog.lay_spawn"),
            ENTITY_FROG_LONG_JUMP = std("entity.frog.long_jump"),
            ENTITY_FROG_STEP = std("entity.frog.step"),
            ENTITY_FROG_TONGUE = std("entity.frog.tongue"),
            ENTITY_GENERIC_BURN = std("entity.generic.burn"),
            ENTITY_GENERIC_DEATH = std("entity.generic.death"),
            ENTITY_GENERIC_EXTINGUISH_FIRE = std("entity.generic.extinguish_fire"),
            ENTITY_GENERIC_HURT = std("entity.generic.hurt"),
            ENTITY_GLOW_ITEM_FRAME_ADD_ITEM = std("entity.glow_item_frame.add_item"),
            ENTITY_GLOW_ITEM_FRAME_BREAK = std("entity.glow_item_frame.break"),
            ENTITY_GLOW_ITEM_FRAME_PLACE = std("entity.glow_item_frame.place"),
            ENTITY_GLOW_ITEM_FRAME_REMOVE_ITEM = std("entity.glow_item_frame.remove_item"),
            ENTITY_GLOW_ITEM_FRAME_ROTATE_ITEM = std("entity.glow_item_frame.rotate_item"),
            ENTITY_GLOW_SQUID_AMBIENT = std("entity.glow_squid.ambient"),
            ENTITY_GLOW_SQUID_DEATH = std("entity.glow_squid.death"),
            ENTITY_GLOW_SQUID_HURT = std("entity.glow_squid.hurt"),
            ENTITY_GLOW_SQUID_SQUIRT = std("entity.glow_squid.squirt"),
            ENTITY_GOAT_AMBIENT = std("entity.goat.ambient"),
            ENTITY_GOAT_DEATH = std("entity.goat.death"),
            ENTITY_GOAT_EAT = std("entity.goat.eat"),
            ENTITY_GOAT_HORN_BREAK = std("entity.goat.horn_break"),
            ENTITY_GOAT_HURT = std("entity.goat.hurt"),
            ENTITY_GOAT_LONG_JUMP = std("entity.goat.long_jump"),
            ENTITY_GOAT_MILK = std("entity.goat.milk"),
            ENTITY_GOAT_PREPARE_RAM = std("entity.goat.prepare_ram"),
            ENTITY_GOAT_RAM_IMPACT = std("entity.goat.ram_impact"),
            ENTITY_GOAT_SCREAMING_AMBIENT = std("entity.goat.screaming.ambient"),
            ENTITY_GOAT_SCREAMING_DEATH = std("entity.goat.screaming.death"),
            ENTITY_GOAT_SCREAMING_EAT = std("entity.goat.screaming.eat"),
            ENTITY_GOAT_SCREAMING_HURT = std("entity.goat.screaming.hurt"),
            ENTITY_GOAT_SCREAMING_LONG_JUMP = std("entity.goat.screaming.long_jump"),
            ENTITY_GOAT_SCREAMING_MILK = std("entity.goat.screaming.milk"),
            ENTITY_GOAT_SCREAMING_PREPARE_RAM = std("entity.goat.screaming.prepare_ram"),
            ENTITY_GOAT_SCREAMING_RAM_IMPACT = std("entity.goat.screaming.ram_impact"),
            ENTITY_GOAT_STEP = std("entity.goat.step"),
            ENTITY_GUARDIAN_AMBIENT = std("entity.guardian.ambient"),
            ENTITY_GUARDIAN_AMBIENT_LAND = std("entity.guardian.ambient_land"),
            ENTITY_GUARDIAN_ATTACK = std("entity.guardian.attack"),
            ENTITY_GUARDIAN_DEATH = std("entity.guardian.death"),
            ENTITY_GUARDIAN_DEATH_LAND = std("entity.guardian.death_land"),
            ENTITY_GUARDIAN_FLOP = std("entity.guardian.flop"),
            ENTITY_GUARDIAN_HURT = std("entity.guardian.hurt"),
            ENTITY_GUARDIAN_HURT_LAND = std("entity.guardian.hurt_land"),
            ENTITY_HOGLIN_AMBIENT = std("entity.hoglin.ambient"),
            ENTITY_HOGLIN_ANGRY = std("entity.hoglin.angry"),
            ENTITY_HOGLIN_ATTACK = std("entity.hoglin.attack"),
            ENTITY_HOGLIN_CONVERTED_TO_ZOMBIFIED = std("entity.hoglin.converted_to_zombified"),
            ENTITY_HOGLIN_DEATH = std("entity.hoglin.death"),
            ENTITY_HOGLIN_HURT = std("entity.hoglin.hurt"),
            ENTITY_HOGLIN_RETREAT = std("entity.hoglin.retreat"),
            ENTITY_HOGLIN_STEP = std("entity.hoglin.step"),
            ENTITY_HOSTILE_DEATH = std("entity.hostile.death"),
            ENTITY_HOSTILE_HURT = std("entity.hostile.hurt"),
            ENTITY_HUSK_AMBIENT = std("entity.husk.ambient"),
            ENTITY_HUSK_CONVERTED_TO_ZOMBIE = std("entity.husk.converted_to_zombie"),
            ENTITY_HUSK_DEATH = std("entity.husk.death"),
            ENTITY_HUSK_HURT = std("entity.husk.hurt"),
            ENTITY_HUSK_STEP = std("entity.husk.step"),
            ENTITY_IRON_GOLEM_DAMAGE = std("entity.iron_golem.damage"),
            ENTITY_IRON_GOLEM_REPAIR = std("entity.iron_golem.repair"),
            ENTITY_LLAMA_AMBIENT = std("entity.llama.ambient"),
            ENTITY_LLAMA_ANGRY = std("entity.llama.angry"),
            ENTITY_LLAMA_CHEST = std("entity.llama.chest"),
            ENTITY_LLAMA_DEATH = std("entity.llama.death"),
            ENTITY_LLAMA_EAT = std("entity.llama.eat"),
            ENTITY_LLAMA_HURT = std("entity.llama.hurt"),
            ENTITY_LLAMA_SPIT = std("entity.llama.spit"),
            ENTITY_LLAMA_STEP = std("entity.llama.step"),
            ENTITY_LLAMA_SWAG = std("entity.llama.swag"),
            ENTITY_MINECART_INSIDE_UNDERWATER = std("entity.minecart.inside.underwater"),
            ENTITY_MOOSHROOM_CONVERT = std("entity.mooshroom.convert"),
            ENTITY_MOOSHROOM_EAT = std("entity.mooshroom.eat"),
            ENTITY_MOOSHROOM_MILK = std("entity.mooshroom.milk"),
            ENTITY_MOOSHROOM_SHEAR = std("entity.mooshroom.shear"),
            ENTITY_MOOSHROOM_SUSPICIOUS_MILK = std("entity.mooshroom.suspicious_milk"),
            ENTITY_MULE_AMBIENT = std("entity.mule.ambient"),
            ENTITY_MULE_ANGRY = std("entity.mule.angry"),
            ENTITY_MULE_EAT = std("entity.mule.eat"),
            ENTITY_MULE_JUMP = std("entity.mule.jump"),
            ENTITY_OCELOT_AMBIENT = std("entity.ocelot.ambient"),
            ENTITY_OCELOT_DEATH = std("entity.ocelot.death"),
            ENTITY_OCELOT_HURT = std("entity.ocelot.hurt"),
            ENTITY_PAINTING_BREAK = std("entity.painting.break"),
            ENTITY_PAINTING_PLACE = std("entity.painting.place"),
            ENTITY_PANDA_AGGRESSIVE_AMBIENT = std("entity.panda.aggressive_ambient"),
            ENTITY_PANDA_AMBIENT = std("entity.panda.ambient"),
            ENTITY_PANDA_BITE = std("entity.panda.bite"),
            ENTITY_PANDA_CANT_BREED = std("entity.panda.cant_breed"),
            ENTITY_PANDA_DEATH = std("entity.panda.death"),
            ENTITY_PANDA_EAT = std("entity.panda.eat"),
            ENTITY_PANDA_HURT = std("entity.panda.hurt"),
            ENTITY_PANDA_PRE_SNEEZE = std("entity.panda.pre_sneeze"),
            ENTITY_PANDA_SNEEZE = std("entity.panda.sneeze"),
            ENTITY_PANDA_STEP = std("entity.panda.step"),
            ENTITY_PANDA_WORRIED_AMBIENT = std("entity.panda.worried_ambient"),
            ENTITY_PARROT_AMBIENT = std("entity.parrot.ambient"),
            ENTITY_PARROT_DEATH = std("entity.parrot.death"),
            ENTITY_PARROT_EAT = std("entity.parrot.eat"),
            ENTITY_PARROT_FLY = std("entity.parrot.fly"),
            ENTITY_PARROT_HURT = std("entity.parrot.hurt"),
            ENTITY_PARROT_IMITATE_BLAZE = std("entity.parrot.imitate.blaze"),
            ENTITY_PARROT_IMITATE_BOGGED = std("entity.parrot.imitate.bogged"),
            ENTITY_PARROT_IMITATE_BREEZE = std("entity.parrot.imitate.breeze"),
            ENTITY_PARROT_IMITATE_CREAKING = std("entity.parrot.imitate.creaking"),
            ENTITY_PARROT_IMITATE_CREEPER = std("entity.parrot.imitate.creeper"),
            ENTITY_PARROT_IMITATE_DROWNED = std("entity.parrot.imitate.drowned"),
            ENTITY_PARROT_IMITATE_ELDER_GUARDIAN = std("entity.parrot.imitate.elder_guardian"),
            ENTITY_PARROT_IMITATE_ENDERMITE = std("entity.parrot.imitate.endermite"),
            ENTITY_PARROT_IMITATE_ENDER_DRAGON = std("entity.parrot.imitate.ender_dragon", "ENTITY_PARROT_IMITATE_ENDERDRAGON"),
            ENTITY_PARROT_IMITATE_EVOKER = std("entity.parrot.imitate.evoker", "ENTITY_PARROT_IMITATE_EVOCATION_ILLAGER"),
            ENTITY_PARROT_IMITATE_GHAST = std("entity.parrot.imitate.ghast"),
            ENTITY_PARROT_IMITATE_GUARDIAN = std("entity.parrot.imitate.guardian"),
            ENTITY_PARROT_IMITATE_HOGLIN = std("entity.parrot.imitate.hoglin"),
            ENTITY_PARROT_IMITATE_HUSK = std("entity.parrot.imitate.husk"),
            ENTITY_PARROT_IMITATE_ILLUSIONER = std("entity.parrot.imitate.illusioner", "ENTITY_PARROT_IMITATE_ILLUSION_ILLAGER"),
            ENTITY_PARROT_IMITATE_MAGMA_CUBE = std("entity.parrot.imitate.magma_cube", "ENTITY_PARROT_IMITATE_MAGMACUBE"),
            ENTITY_PARROT_IMITATE_PHANTOM = std("entity.parrot.imitate.phantom"),
            ENTITY_PARROT_IMITATE_PIGLIN = std("entity.parrot.imitate.piglin", "ENTITY_PARROT_IMITATE_ZOMBIE_PIGMAN"),
            ENTITY_PARROT_IMITATE_PIGLIN_BRUTE = std("entity.parrot.imitate.piglin_brute"),
            ENTITY_PARROT_IMITATE_PILLAGER = std("entity.parrot.imitate.pillager"),
            ENTITY_PARROT_IMITATE_RAVAGER = std("entity.parrot.imitate.ravager"),
            ENTITY_PARROT_IMITATE_SHULKER = std("entity.parrot.imitate.shulker"),
            ENTITY_PARROT_IMITATE_SILVERFISH = std("entity.parrot.imitate.silverfish"),
            ENTITY_PARROT_IMITATE_SKELETON = std("entity.parrot.imitate.skeleton"),
            ENTITY_PARROT_IMITATE_SLIME = std("entity.parrot.imitate.slime"),
            ENTITY_PARROT_IMITATE_SPIDER = std("entity.parrot.imitate.spider"),
            ENTITY_PARROT_IMITATE_STRAY = std("entity.parrot.imitate.stray"),
            ENTITY_PARROT_IMITATE_VEX = std("entity.parrot.imitate.vex"),
            ENTITY_PARROT_IMITATE_VINDICATOR = std("entity.parrot.imitate.vindicator", "ENTITY_PARROT_IMITATE_VINDICATION_ILLAGER"),
            ENTITY_PARROT_IMITATE_WARDEN = std("entity.parrot.imitate.warden"),
            ENTITY_PARROT_IMITATE_WITCH = std("entity.parrot.imitate.witch"),
            ENTITY_PARROT_IMITATE_WITHER = std("entity.parrot.imitate.wither"),
            ENTITY_PARROT_IMITATE_WITHER_SKELETON = std("entity.parrot.imitate.wither_skeleton"),
            ENTITY_PARROT_IMITATE_ZOGLIN = std("entity.parrot.imitate.zoglin"),
            ENTITY_PARROT_IMITATE_ZOMBIE = std("entity.parrot.imitate.zombie"),
            ENTITY_PARROT_IMITATE_ZOMBIE_VILLAGER = std("entity.parrot.imitate.zombie_villager"),
            ENTITY_PARROT_STEP = std("entity.parrot.step"),
            ENTITY_PHANTOM_AMBIENT = std("entity.phantom.ambient"),
            ENTITY_PHANTOM_BITE = std("entity.phantom.bite"),
            ENTITY_PHANTOM_DEATH = std("entity.phantom.death"),
            ENTITY_PHANTOM_FLAP = std("entity.phantom.flap"),
            ENTITY_PHANTOM_HURT = std("entity.phantom.hurt"),
            ENTITY_PHANTOM_SWOOP = std("entity.phantom.swoop"),
            ENTITY_PIGLIN_ADMIRING_ITEM = std("entity.piglin.admiring_item"),
            ENTITY_PIGLIN_AMBIENT = std("entity.piglin.ambient"),
            ENTITY_PIGLIN_ANGRY = std("entity.piglin.angry"),
            ENTITY_PIGLIN_BRUTE_AMBIENT = std("entity.piglin_brute.ambient"),
            ENTITY_PIGLIN_BRUTE_ANGRY = std("entity.piglin_brute.angry"),
            ENTITY_PIGLIN_BRUTE_CONVERTED_TO_ZOMBIFIED = std("entity.piglin_brute.converted_to_zombified"),
            ENTITY_PIGLIN_BRUTE_DEATH = std("entity.piglin_brute.death"),
            ENTITY_PIGLIN_BRUTE_HURT = std("entity.piglin_brute.hurt"),
            ENTITY_PIGLIN_BRUTE_STEP = std("entity.piglin_brute.step"),
            ENTITY_PIGLIN_CELEBRATE = std("entity.piglin.celebrate"),
            ENTITY_PIGLIN_CONVERTED_TO_ZOMBIFIED = std("entity.piglin.converted_to_zombified"),
            ENTITY_PIGLIN_DEATH = std("entity.piglin.death"),
            ENTITY_PIGLIN_HURT = std("entity.piglin.hurt"),
            ENTITY_PIGLIN_JEALOUS = std("entity.piglin.jealous"),
            ENTITY_PIGLIN_RETREAT = std("entity.piglin.retreat"),
            ENTITY_PIGLIN_STEP = std("entity.piglin.step"),
            ENTITY_PIG_HURT = std("entity.pig.hurt"),
            ENTITY_PILLAGER_AMBIENT = std("entity.pillager.ambient"),
            ENTITY_PILLAGER_CELEBRATE = std("entity.pillager.celebrate"),
            ENTITY_PILLAGER_DEATH = std("entity.pillager.death"),
            ENTITY_PILLAGER_HURT = std("entity.pillager.hurt"),
            ENTITY_PLAYER_ATTACK_CRIT = std("entity.player.attack.crit"),
            ENTITY_PLAYER_ATTACK_KNOCKBACK = std("entity.player.attack.knockback"),
            ENTITY_PLAYER_ATTACK_NODAMAGE = std("entity.player.attack.nodamage"),
            ENTITY_PLAYER_ATTACK_SWEEP = std("entity.player.attack.sweep"),
            ENTITY_PLAYER_ATTACK_WEAK = std("entity.player.attack.weak"),
            ENTITY_PLAYER_BREATH = std("entity.player.breath"),
            ENTITY_PLAYER_DEATH = std("entity.player.death"),
            ENTITY_PLAYER_HURT_DROWN = std("entity.player.hurt_drown"),
            ENTITY_PLAYER_HURT_FREEZE = std("entity.player.hurt_freeze"),
            ENTITY_PLAYER_HURT_ON_FIRE = std("entity.player.hurt_on_fire"),
            ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH = std("entity.player.hurt_sweet_berry_bush"),
            ENTITY_PLAYER_TELEPORT = std("entity.player.teleport"),
            ENTITY_POLAR_BEAR_AMBIENT = std("entity.polar_bear.ambient"),
            ENTITY_POLAR_BEAR_DEATH = std("entity.polar_bear.death"),
            ENTITY_POLAR_BEAR_HURT = std("entity.polar_bear.hurt"),
            ENTITY_POLAR_BEAR_STEP = std("entity.polar_bear.step"),
            ENTITY_POLAR_BEAR_WARNING = std("entity.polar_bear.warning"),
            ENTITY_PUFFER_FISH_AMBIENT = std("entity.puffer_fish.ambient"),
            ENTITY_PUFFER_FISH_BLOW_OUT = std("entity.puffer_fish.blow_out"),
            ENTITY_PUFFER_FISH_BLOW_UP = std("entity.puffer_fish.blow_up"),
            ENTITY_PUFFER_FISH_DEATH = std("entity.puffer_fish.death"),
            ENTITY_PUFFER_FISH_FLOP = std("entity.puffer_fish.flop"),
            ENTITY_PUFFER_FISH_HURT = std("entity.puffer_fish.hurt"),
            ENTITY_PUFFER_FISH_STING = std("entity.puffer_fish.sting"),
            ENTITY_RABBIT_AMBIENT = std("entity.rabbit.ambient"),
            ENTITY_RABBIT_ATTACK = std("entity.rabbit.attack"),
            ENTITY_RABBIT_DEATH = std("entity.rabbit.death"),
            ENTITY_RABBIT_HURT = std("entity.rabbit.hurt"),
            ENTITY_RABBIT_JUMP = std("entity.rabbit.jump"),
            ENTITY_RAVAGER_AMBIENT = std("entity.ravager.ambient"),
            ENTITY_RAVAGER_ATTACK = std("entity.ravager.attack"),
            ENTITY_RAVAGER_CELEBRATE = std("entity.ravager.celebrate"),
            ENTITY_RAVAGER_DEATH = std("entity.ravager.death"),
            ENTITY_RAVAGER_HURT = std("entity.ravager.hurt"),
            ENTITY_RAVAGER_ROAR = std("entity.ravager.roar"),
            ENTITY_RAVAGER_STEP = std("entity.ravager.step"),
            ENTITY_RAVAGER_STUNNED = std("entity.ravager.stunned"),
            ENTITY_SALMON_AMBIENT = std("entity.salmon.ambient"),
            ENTITY_SALMON_DEATH = std("entity.salmon.death"),
            ENTITY_SALMON_FLOP = std("entity.salmon.flop"),
            ENTITY_SHEEP_DEATH = std("entity.sheep.death"),
            ENTITY_SHEEP_HURT = std("entity.sheep.hurt"),
            ENTITY_SHULKER_AMBIENT = std("entity.shulker.ambient"),
            ENTITY_SHULKER_BULLET_HIT = std("entity.shulker_bullet.hit"),
            ENTITY_SHULKER_BULLET_HURT = std("entity.shulker_bullet.hurt"),
            ENTITY_SHULKER_CLOSE = std("entity.shulker.close"),
            ENTITY_SHULKER_DEATH = std("entity.shulker.death"),
            ENTITY_SHULKER_HURT = std("entity.shulker.hurt"),
            ENTITY_SHULKER_HURT_CLOSED = std("entity.shulker.hurt_closed"),
            ENTITY_SHULKER_OPEN = std("entity.shulker.open"),
            ENTITY_SHULKER_SHOOT = std("entity.shulker.shoot"),
            ENTITY_SHULKER_TELEPORT = std("entity.shulker.teleport"),
            ENTITY_SKELETON_CONVERTED_TO_STRAY = std("entity.skeleton.converted_to_stray"),
            ENTITY_SKELETON_HORSE_AMBIENT_WATER = std("entity.skeleton_horse.ambient_water"),
            ENTITY_SKELETON_HORSE_GALLOP_WATER = std("entity.skeleton_horse.gallop_water"),
            ENTITY_SKELETON_HORSE_JUMP_WATER = std("entity.skeleton_horse.jump_water"),
            ENTITY_SKELETON_HORSE_STEP_WATER = std("entity.skeleton_horse.step_water"),
            ENTITY_SKELETON_HORSE_SWIM = std("entity.skeleton_horse.swim"),
            ENTITY_SKELETON_SHOOT = std("entity.skeleton.shoot"),
            ENTITY_SLIME_DEATH = std("entity.slime.death", "ENTITY_SMALL_SLIME_DEATH"),
            ENTITY_SLIME_DEATH_SMALL = std("entity.slime.death_small", "ENTITY_SMALL_SLIME_DEATH"),
            ENTITY_SLIME_HURT = std("entity.slime.hurt"),
            ENTITY_SNIFFER_DEATH = std("entity.sniffer.death"),
            ENTITY_SNIFFER_DIGGING = std("entity.sniffer.digging"),
            ENTITY_SNIFFER_DIGGING_STOP = std("entity.sniffer.digging_stop"),
            ENTITY_SNIFFER_DROP_SEED = std("entity.sniffer.drop_seed"),
            ENTITY_SNIFFER_EAT = std("entity.sniffer.eat"),
            ENTITY_SNIFFER_HAPPY = std("entity.sniffer.happy"),
            ENTITY_SNIFFER_HURT = std("entity.sniffer.hurt"),
            ENTITY_SNIFFER_IDLE = std("entity.sniffer.idle"),
            ENTITY_SNIFFER_SCENTING = std("entity.sniffer.scenting"),
            ENTITY_SNIFFER_SEARCHING = std("entity.sniffer.searching"),
            ENTITY_SNIFFER_SNIFFING = std("entity.sniffer.sniffing"),
            ENTITY_SNIFFER_STEP = std("entity.sniffer.step"),
            ENTITY_SNOWBALL_THROW = std("entity.snowball.throw"),
            ENTITY_SPIDER_HURT = std("entity.spider.hurt"),
            ENTITY_SPLASH_POTION_BREAK = std("entity.splash_potion.break"),
            ENTITY_SPLASH_POTION_THROW = std("entity.splash_potion.throw"),
            ENTITY_SQUID_AMBIENT = std("entity.squid.ambient"),
            ENTITY_SQUID_DEATH = std("entity.squid.death"),
            ENTITY_SQUID_HURT = std("entity.squid.hurt"),
            ENTITY_SQUID_SQUIRT = std("entity.squid.squirt"),
            ENTITY_STRAY_AMBIENT = std("entity.stray.ambient"),
            ENTITY_STRAY_DEATH = std("entity.stray.death"),
            ENTITY_STRAY_HURT = std("entity.stray.hurt"),
            ENTITY_STRAY_STEP = std("entity.stray.step"),
            ENTITY_STRIDER_AMBIENT = std("entity.strider.ambient"),
            ENTITY_STRIDER_DEATH = std("entity.strider.death"),
            ENTITY_STRIDER_EAT = std("entity.strider.eat"),
            ENTITY_STRIDER_HAPPY = std("entity.strider.happy"),
            ENTITY_STRIDER_HURT = std("entity.strider.hurt"),
            ENTITY_STRIDER_RETREAT = std("entity.strider.retreat"),
            ENTITY_STRIDER_SADDLE = std("entity.strider.saddle"),
            ENTITY_STRIDER_STEP = std("entity.strider.step"),
            ENTITY_STRIDER_STEP_LAVA = std("entity.strider.step_lava"),
            ENTITY_TADPOLE_DEATH = std("entity.tadpole.death"),
            ENTITY_TADPOLE_FLOP = std("entity.tadpole.flop"),
            ENTITY_TADPOLE_GROW_UP = std("entity.tadpole.grow_up"),
            ENTITY_TADPOLE_HURT = std("entity.tadpole.hurt"),
            ENTITY_TROPICAL_FISH_AMBIENT = std("entity.tropical_fish.ambient"),
            ENTITY_TROPICAL_FISH_DEATH = std("entity.tropical_fish.death"),
            ENTITY_TROPICAL_FISH_HURT = std("entity.tropical_fish.hurt"),
            ENTITY_TURTLE_AMBIENT_LAND = std("entity.turtle.ambient_land"),
            ENTITY_TURTLE_DEATH = std("entity.turtle.death"),
            ENTITY_TURTLE_DEATH_BABY = std("entity.turtle.death_baby"),
            ENTITY_TURTLE_EGG_BREAK = std("entity.turtle.egg_break"),
            ENTITY_TURTLE_EGG_CRACK = std("entity.turtle.egg_crack"),
            ENTITY_TURTLE_EGG_HATCH = std("entity.turtle.egg_hatch"),
            ENTITY_TURTLE_HURT = std("entity.turtle.hurt"),
            ENTITY_TURTLE_HURT_BABY = std("entity.turtle.hurt_baby"),
            ENTITY_TURTLE_LAY_EGG = std("entity.turtle.lay_egg"),
            ENTITY_TURTLE_SHAMBLE = std("entity.turtle.shamble"),
            ENTITY_TURTLE_SHAMBLE_BABY = std("entity.turtle.shamble_baby"),
            ENTITY_TURTLE_SWIM = std("entity.turtle.swim"),
            ENTITY_VEX_AMBIENT = std("entity.vex.ambient"),
            ENTITY_VEX_CHARGE = std("entity.vex.charge"),
            ENTITY_VEX_DEATH = std("entity.vex.death"),
            ENTITY_VEX_HURT = std("entity.vex.hurt"),
            ENTITY_VILLAGER_CELEBRATE = std("entity.villager.celebrate"),
            ENTITY_VILLAGER_WORK_ARMORER = std("entity.villager.work_armorer"),
            ENTITY_VILLAGER_WORK_BUTCHER = std("entity.villager.work_butcher"),
            ENTITY_VILLAGER_WORK_CARTOGRAPHER = std("entity.villager.work_cartographer"),
            ENTITY_VILLAGER_WORK_CLERIC = std("entity.villager.work_cleric"),
            ENTITY_VILLAGER_WORK_FARMER = std("entity.villager.work_farmer"),
            ENTITY_VILLAGER_WORK_FISHERMAN = std("entity.villager.work_fisherman"),
            ENTITY_VILLAGER_WORK_FLETCHER = std("entity.villager.work_fletcher"),
            ENTITY_VILLAGER_WORK_LEATHERWORKER = std("entity.villager.work_leatherworker"),
            ENTITY_VILLAGER_WORK_LIBRARIAN = std("entity.villager.work_librarian"),
            ENTITY_VILLAGER_WORK_MASON = std("entity.villager.work_mason"),
            ENTITY_VILLAGER_WORK_SHEPHERD = std("entity.villager.work_shepherd"),
            ENTITY_VILLAGER_WORK_TOOLSMITH = std("entity.villager.work_toolsmith"),
            ENTITY_VILLAGER_WORK_WEAPONSMITH = std("entity.villager.work_weaponsmith"),
            ENTITY_VINDICATOR_CELEBRATE = std("entity.vindicator.celebrate"),
            ENTITY_WANDERING_TRADER_AMBIENT = std("entity.wandering_trader.ambient"),
            ENTITY_WANDERING_TRADER_DEATH = std("entity.wandering_trader.death"),
            ENTITY_WANDERING_TRADER_DISAPPEARED = std("entity.wandering_trader.disappeared"),
            ENTITY_WANDERING_TRADER_DRINK_MILK = std("entity.wandering_trader.drink_milk"),
            ENTITY_WANDERING_TRADER_DRINK_POTION = std("entity.wandering_trader.drink_potion"),
            ENTITY_WANDERING_TRADER_HURT = std("entity.wandering_trader.hurt"),
            ENTITY_WANDERING_TRADER_NO = std("entity.wandering_trader.no"),
            ENTITY_WANDERING_TRADER_REAPPEARED = std("entity.wandering_trader.reappeared"),
            ENTITY_WANDERING_TRADER_TRADE = std("entity.wandering_trader.trade"),
            ENTITY_WANDERING_TRADER_YES = std("entity.wandering_trader.yes"),
            ENTITY_WARDEN_AGITATED = std("entity.warden.agitated"),
            ENTITY_WARDEN_AMBIENT = std("entity.warden.ambient"),
            ENTITY_WARDEN_ANGRY = std("entity.warden.angry"),
            ENTITY_WARDEN_ATTACK_IMPACT = std("entity.warden.attack_impact"),
            ENTITY_WARDEN_DEATH = std("entity.warden.death"),
            ENTITY_WARDEN_DIG = std("entity.warden.dig"),
            ENTITY_WARDEN_EMERGE = std("entity.warden.emerge"),
            ENTITY_WARDEN_HEARTBEAT = std("entity.warden.heartbeat"),
            ENTITY_WARDEN_HURT = std("entity.warden.hurt"),
            ENTITY_WARDEN_LISTENING = std("entity.warden.listening"),
            ENTITY_WARDEN_LISTENING_ANGRY = std("entity.warden.listening_angry"),
            ENTITY_WARDEN_NEARBY_CLOSE = std("entity.warden.nearby_close"),
            ENTITY_WARDEN_NEARBY_CLOSER = std("entity.warden.nearby_closer"),
            ENTITY_WARDEN_NEARBY_CLOSEST = std("entity.warden.nearby_closest"),
            ENTITY_WARDEN_ROAR = std("entity.warden.roar"),
            ENTITY_WARDEN_SNIFF = std("entity.warden.sniff"),
            ENTITY_WARDEN_SONIC_BOOM = std("entity.warden.sonic_boom"),
            ENTITY_WARDEN_SONIC_CHARGE = std("entity.warden.sonic_charge"),
            ENTITY_WARDEN_STEP = std("entity.warden.step"),
            ENTITY_WARDEN_TENDRIL_CLICKS = std("entity.warden.tendril_clicks"),
            ENTITY_WIND_CHARGE_THROW = std("entity.wind_charge.throw"),
            ENTITY_WITCH_AMBIENT = std("entity.witch.ambient"),
            ENTITY_WITCH_CELEBRATE = std("entity.witch.celebrate"),
            ENTITY_WITCH_DEATH = std("entity.witch.death"),
            ENTITY_WITCH_DRINK = std("entity.witch.drink"),
            ENTITY_WITCH_HURT = std("entity.witch.hurt"),
            ENTITY_WITCH_THROW = std("entity.witch.throw"),
            ENTITY_WITHER_BREAK_BLOCK = std("entity.wither.break_block"),
            ENTITY_WITHER_SKELETON_AMBIENT = std("entity.wither_skeleton.ambient"),
            ENTITY_WITHER_SKELETON_DEATH = std("entity.wither_skeleton.death"),
            ENTITY_WITHER_SKELETON_HURT = std("entity.wither_skeleton.hurt"),
            ENTITY_WITHER_SKELETON_STEP = std("entity.wither_skeleton.step"),
            ENTITY_ZOGLIN_AMBIENT = std("entity.zoglin.ambient"),
            ENTITY_ZOGLIN_ANGRY = std("entity.zoglin.angry"),
            ENTITY_ZOGLIN_ATTACK = std("entity.zoglin.attack"),
            ENTITY_ZOGLIN_DEATH = std("entity.zoglin.death"),
            ENTITY_ZOGLIN_HURT = std("entity.zoglin.hurt"),
            ENTITY_ZOGLIN_STEP = std("entity.zoglin.step"),
            ENTITY_ZOMBIE_CONVERTED_TO_DROWNED = std("entity.zombie.converted_to_drowned"),
            ENTITY_ZOMBIE_DESTROY_EGG = std("entity.zombie.destroy_egg"),
            ENTITY_ZOMBIE_VILLAGER_AMBIENT = std("entity.zombie_villager.ambient"),
            ENTITY_ZOMBIE_VILLAGER_DEATH = std("entity.zombie_villager.death"),
            ENTITY_ZOMBIE_VILLAGER_HURT = std("entity.zombie_villager.hurt"),
            ENTITY_ZOMBIE_VILLAGER_STEP = std("entity.zombie_villager.step"),
            EVENT_MOB_EFFECT_BAD_OMEN = std("event.mob_effect.bad_omen"),
            EVENT_MOB_EFFECT_RAID_OMEN = std("event.mob_effect.raid_omen"),
            EVENT_MOB_EFFECT_TRIAL_OMEN = std("event.mob_effect.trial_omen"),
            EVENT_RAID_HORN = std("event.raid.horn"),
            INTENTIONALLY_EMPTY = std("intentionally_empty"),
            ITEM_ARMOR_EQUIP_CHAIN = std("item.armor.equip_chain"),
            ITEM_ARMOR_EQUIP_DIAMOND = std("item.armor.equip_diamond"),
            ITEM_ARMOR_EQUIP_ELYTRA = std("item.armor.equip_elytra"),
            ITEM_ARMOR_EQUIP_GENERIC = std("item.armor.equip_generic"),
            ITEM_ARMOR_EQUIP_GOLD = std("item.armor.equip_gold"),
            ITEM_ARMOR_EQUIP_IRON = std("item.armor.equip_iron"),
            ITEM_ARMOR_EQUIP_LEATHER = std("item.armor.equip_leather"),
            ITEM_ARMOR_EQUIP_NETHERITE = std("item.armor.equip_netherite"),
            ITEM_ARMOR_EQUIP_TURTLE = std("item.armor.equip_turtle"),
            ITEM_ARMOR_EQUIP_WOLF = std("item.armor.equip_wolf"),
            ITEM_ARMOR_UNEQUIP_WOLF = std("item.armor.unequip_wolf"),
            ITEM_AXE_SCRAPE = std("item.axe.scrape"),
            ITEM_AXE_STRIP = std("item.axe.strip"),
            ITEM_AXE_WAX_OFF = std("item.axe.wax_off"),
            ITEM_BONE_MEAL_USE = std("item.bone_meal.use"),
            ITEM_BOOK_PAGE_TURN = std("item.book.page_turn"),
            ITEM_BOOK_PUT = std("item.book.put"),
            ITEM_BOTTLE_EMPTY = std("item.bottle.empty"),
            ITEM_BOTTLE_FILL = std("item.bottle.fill"),
            ITEM_BOTTLE_FILL_DRAGONBREATH = std("item.bottle.fill_dragonbreath"),
            ITEM_BRUSH_BRUSHING_GENERIC = std("item.brush.brushing.generic"),
            ITEM_BRUSH_BRUSHING_GRAVEL = std("item.brush.brushing.gravel"),
            ITEM_BRUSH_BRUSHING_GRAVEL_COMPLETE = std("item.brush.brushing.gravel.complete"),
            ITEM_BRUSH_BRUSHING_SAND = std("item.brush.brushing.sand"),
            ITEM_BRUSH_BRUSHING_SAND_COMPLETE = std("item.brush.brushing.sand.complete"),
            ITEM_BUCKET_EMPTY = std("item.bucket.empty"),
            ITEM_BUCKET_EMPTY_AXOLOTL = std("item.bucket.empty_axolotl"),
            ITEM_BUCKET_EMPTY_FISH = std("item.bucket.empty_fish"),
            ITEM_BUCKET_EMPTY_LAVA = std("item.bucket.empty_lava"),
            ITEM_BUCKET_EMPTY_POWDER_SNOW = std("item.bucket.empty_powder_snow"),
            ITEM_BUCKET_EMPTY_TADPOLE = std("item.bucket.empty_tadpole"),
            ITEM_BUCKET_FILL = std("item.bucket.fill"),
            ITEM_BUCKET_FILL_AXOLOTL = std("item.bucket.fill_axolotl"),
            ITEM_BUCKET_FILL_FISH = std("item.bucket.fill_fish"),
            ITEM_BUCKET_FILL_LAVA = std("item.bucket.fill_lava"),
            ITEM_BUCKET_FILL_POWDER_SNOW = std("item.bucket.fill_powder_snow"),
            ITEM_BUCKET_FILL_TADPOLE = std("item.bucket.fill_tadpole"),
            ITEM_BUNDLE_DROP_CONTENTS = std("item.bundle.drop_contents"),
            ITEM_BUNDLE_INSERT = std("item.bundle.insert"),
            ITEM_BUNDLE_INSERT_FAIL = std("item.bundle.insert_fail"),
            ITEM_BUNDLE_REMOVE_ONE = std("item.bundle.remove_one"),
            ITEM_CHORUS_FRUIT_TELEPORT = std("item.chorus_fruit.teleport"),
            ITEM_CROP_PLANT = std("item.crop.plant"),
            ITEM_CROSSBOW_HIT = std("item.crossbow.hit"),
            ITEM_CROSSBOW_LOADING_END = std("item.crossbow.loading_end"),
            ITEM_CROSSBOW_LOADING_MIDDLE = std("item.crossbow.loading_middle"),
            ITEM_CROSSBOW_LOADING_START = std("item.crossbow.loading_start"),
            ITEM_CROSSBOW_QUICK_CHARGE_1 = std("item.crossbow.quick_charge_1"),
            ITEM_CROSSBOW_QUICK_CHARGE_2 = std("item.crossbow.quick_charge_2"),
            ITEM_CROSSBOW_QUICK_CHARGE_3 = std("item.crossbow.quick_charge_3"),
            ITEM_CROSSBOW_SHOOT = std("item.crossbow.shoot"),
            ITEM_DYE_USE = std("item.dye.use"),
            ITEM_ELYTRA_FLYING = std("item.elytra.flying"),
            ITEM_FIRECHARGE_USE = std("item.firecharge.use"),
            ITEM_GLOW_INK_SAC_USE = std("item.glow_ink_sac.use"),
            ITEM_GOAT_HORN_SOUND_0 = std("item.goat_horn.sound.0"),
            ITEM_GOAT_HORN_SOUND_1 = std("item.goat_horn.sound.1"),
            ITEM_GOAT_HORN_SOUND_2 = std("item.goat_horn.sound.2"),
            ITEM_GOAT_HORN_SOUND_3 = std("item.goat_horn.sound.3"),
            ITEM_GOAT_HORN_SOUND_4 = std("item.goat_horn.sound.4"),
            ITEM_GOAT_HORN_SOUND_5 = std("item.goat_horn.sound.5"),
            ITEM_GOAT_HORN_SOUND_6 = std("item.goat_horn.sound.6"),
            ITEM_GOAT_HORN_SOUND_7 = std("item.goat_horn.sound.7"),
            ITEM_HOE_TILL = std("item.hoe.till"),
            ITEM_HONEYCOMB_WAX_ON = std("item.honeycomb.wax_on"),
            ITEM_HONEY_BOTTLE_DRINK = std("item.honey_bottle.drink"),
            ITEM_INK_SAC_USE = std("item.ink_sac.use"),
            ITEM_LODESTONE_COMPASS_LOCK = std("item.lodestone_compass.lock"),
            ITEM_MACE_SMASH_AIR = std("item.mace.smash_air"),
            ITEM_MACE_SMASH_GROUND = std("item.mace.smash_ground"),
            ITEM_MACE_SMASH_GROUND_HEAVY = std("item.mace.smash_ground_heavy"),
            ITEM_NETHER_WART_PLANT = std("item.nether_wart.plant"),
            ITEM_OMINOUS_BOTTLE_DISPOSE = std("item.ominous_bottle.dispose"),
            ITEM_SHIELD_BLOCK = std("item.shield.block"),
            ITEM_SHIELD_BREAK = std("item.shield.break"),
            ITEM_SHOVEL_FLATTEN = std("item.shovel.flatten"),
            ITEM_SPYGLASS_STOP_USING = std("item.spyglass.stop_using"),
            ITEM_SPYGLASS_USE = std("item.spyglass.use"),
            ITEM_TOTEM_USE = std("item.totem.use"),
            ITEM_TRIDENT_HIT = std("item.trident.hit"),
            ITEM_TRIDENT_HIT_GROUND = std("item.trident.hit_ground"),
            ITEM_TRIDENT_RETURN = std("item.trident.return"),
            ITEM_TRIDENT_RIPTIDE_1 = std("item.trident.riptide_1"),
            ITEM_TRIDENT_THROW = std("item.trident.throw"),
            ITEM_TRIDENT_THUNDER = std("item.trident.thunder"),
            ITEM_WOLF_ARMOR_BREAK = std("item.wolf_armor.break"),
            ITEM_WOLF_ARMOR_CRACK = std("item.wolf_armor.crack"),
            ITEM_WOLF_ARMOR_DAMAGE = std("item.wolf_armor.damage"),
            ITEM_WOLF_ARMOR_REPAIR = std("item.wolf_armor.repair"),
            MUSIC_CREATIVE = std("music.creative"),
            MUSIC_CREDITS = std("music.credits"),
            MUSIC_DISC_5 = std("music_disc.5"),
            MUSIC_DISC_CREATOR = std("music_disc.creator"),
            MUSIC_DISC_CREATOR_MUSIC_BOX = std("music_disc.creator_music_box"),
            MUSIC_DISC_OTHERSIDE = std("music_disc.otherside"),
            MUSIC_DISC_PIGSTEP = std("music_disc.pigstep"),
            MUSIC_DISC_PRECIPICE = std("music_disc.precipice"),
            MUSIC_DISC_RELIC = std("music_disc.relic"),
            MUSIC_DRAGON = std("music.dragon"),
            MUSIC_END = std("music.end"),
            MUSIC_GAME = std("music.game"),
            MUSIC_MENU = std("music.menu"),
            MUSIC_NETHER_CRIMSON_FOREST = std("music.nether.crimson_forest"),
            MUSIC_NETHER_NETHER_WASTES = std("music.nether.nether_wastes"),
            MUSIC_NETHER_SOUL_SAND_VALLEY = std("music.nether.soul_sand_valley"),
            MUSIC_NETHER_WARPED_FOREST = std("music.nether.warped_forest"),
            MUSIC_OVERWORLD_BADLANDS = std("music.overworld.badlands"),
            MUSIC_OVERWORLD_BAMBOO_JUNGLE = std("music.overworld.bamboo_jungle"),
            MUSIC_OVERWORLD_CHERRY_GROVE = std("music.overworld.cherry_grove"),
            MUSIC_OVERWORLD_DEEP_DARK = std("music.overworld.deep_dark"),
            MUSIC_OVERWORLD_DESERT = std("music.overworld.desert"),
            MUSIC_OVERWORLD_DRIPSTONE_CAVES = std("music.overworld.dripstone_caves"),
            MUSIC_OVERWORLD_FLOWER_FOREST = std("music.overworld.flower_forest"),
            MUSIC_OVERWORLD_FOREST = std("music.overworld.forest"),
            MUSIC_OVERWORLD_FROZEN_PEAKS = std("music.overworld.frozen_peaks"),
            MUSIC_OVERWORLD_GROVE = std("music.overworld.grove"),
            MUSIC_OVERWORLD_JAGGED_PEAKS = std("music.overworld.jagged_peaks"),
            MUSIC_OVERWORLD_JUNGLE = std("music.overworld.jungle"),
            MUSIC_OVERWORLD_LUSH_CAVES = std("music.overworld.lush_caves"),
            MUSIC_OVERWORLD_MEADOW = std("music.overworld.meadow"),
            MUSIC_OVERWORLD_OLD_GROWTH_TAIGA = std("music.overworld.old_growth_taiga"),
            MUSIC_OVERWORLD_SNOWY_SLOPES = std("music.overworld.snowy_slopes"),
            MUSIC_OVERWORLD_SPARSE_JUNGLE = std("music.overworld.sparse_jungle"),
            MUSIC_OVERWORLD_STONY_PEAKS = std("music.overworld.stony_peaks"),
            MUSIC_OVERWORLD_SWAMP = std("music.overworld.swamp"),
            MUSIC_UNDER_WATER = std("music.under_water"),
            PARTICLE_SOUL_ESCAPE = std("particle.soul_escape"),
            UI_CARTOGRAPHY_TABLE_TAKE_RESULT = std("ui.cartography_table.take_result"),
            UI_HUD_BUBBLE_POP = std("ui.hud.bubble_pop"),
            UI_LOOM_SELECT_PATTERN = std("ui.loom.select_pattern"),
            UI_LOOM_TAKE_RESULT = std("ui.loom.take_result"),
            UI_STONECUTTER_SELECT_RECIPE = std("ui.stonecutter.select_recipe"),
            UI_STONECUTTER_TAKE_RESULT = std("ui.stonecutter.take_result"),
            UI_TOAST_CHALLENGE_COMPLETE = std("ui.toast.challenge_complete"),
            UI_TOAST_IN = std("ui.toast.in"),
            UI_TOAST_OUT = std("ui.toast.out"),
            WEATHER_RAIN_ABOVE = std("weather.rain.above"),
            BLOCK_EYEBLOSSOM_CLOSE = std("block.eyeblossom.close"),
            BLOCK_RESIN_BRICKS_FALL = std("block.resin_bricks.fall"),
            BLOCK_RESIN_BRICKS_STEP = std("block.resin_bricks.step"),
            BLOCK_RESIN_PLACE = std("block.resin.place"),
            ENTITY_CREAKING_TWITCH = std("entity.creaking.twitch"),
            BLOCK_EYEBLOSSOM_IDLE = std("block.eyeblossom.idle"),
            BLOCK_RESIN_BREAK = std("block.resin.break"),
            BLOCK_RESIN_BRICKS_PLACE = std("block.resin_bricks.place"),
            BLOCK_RESIN_BRICKS_BREAK = std("block.resin_bricks.break"),
            BLOCK_EYEBLOSSOM_CLOSE_LONG = std("block.eyeblossom.close_long"),
            BLOCK_RESIN_FALL = std("block.resin.fall"),
            BLOCK_RESIN_STEP = std("block.resin.step"),
            BLOCK_EYEBLOSSOM_OPEN = std("block.eyeblossom.open"),
            BLOCK_RESIN_BRICKS_HIT = std("block.resin_bricks.hit"),
            BLOCK_EYEBLOSSOM_OPEN_LONG = std("block.eyeblossom.open_long");

    @XChange(version = "v1.20.5", from = "ENTITY_GENERIC_WIND_BURST", to = "ENTITY_WIND_CHARGE_WIND_BURST")
    public static final XSound ENTITY_WIND_CHARGE_WIND_BURST = std("entity.wind_charge.wind_burst", "ENTITY_GENERIC_WIND_BURST");

    @XInfo(since = "?", removedSince = "1.21.3")
    @Deprecated
    public static final XSound
            MUSIC_OVERWORLD_JUNGLE_AND_FOREST = std("music.overworld.jungle_and_forest"),
            BLOCK_TRIAL_SPAWNER_AMBIENT_CHARGED = std("block.trial_spawner.ambient_charged"),
            BLOCK_TRIAL_SPAWNER_CHARGE_ACTIVATE = std("block.trial_spawner.charge_activate"),
            ENTITY_GOAT_SCREAMING_HORN_BREAK = std("entity.goat.screaming.horn_break");

    @XInfo(since = "?", removedSince = "1.20")
    @Deprecated
    public static final XSound
            ITEM_BRUSH_BRUSH_SAND_COMPLETED = std("item.brush.brush_sand_completed"),
            ITEM_GOAT_HORN_PLAY = std("item.goat_horn.play"),
            ITEM_BRUSH_BRUSHING = std("item.brush.brushing");

    @Deprecated
    @XInfo(since = "1.12", removedSince = "1.15")
    public static final XSound
            ENTITY_PARROT_IMITATE_WOLF = std("ENTITY_PARROT_IMITATE_WOLF"),
            ENTITY_PARROT_IMITATE_POLAR_BEAR = std("ENTITY_PARROT_IMITATE_POLAR_BEAR"),
            ENTITY_PARROT_IMITATE_PANDA = std("ENTITY_PARROT_IMITATE_PANDA"),
            ENTITY_PARROT_IMITATE_ENDERMAN = std("ENTITY_PARROT_IMITATE_ENDERMAN");

    @XInfo(since = "1.21.5")
    public static final XSound
            BLOCK_CACTUS_FLOWER_BREAK = std("block.cactus_flower.break"),
            BLOCK_CACTUS_FLOWER_PLACE = std("block.cactus_flower.place"),
            BLOCK_DEADBUSH_IDLE = std("block.deadbush.idle"),
            BLOCK_FIREFLY_BUSH_IDLE = std("block.firefly_bush.idle"),
            BLOCK_IRON_BREAK = std("block.iron.break"),
            BLOCK_IRON_FALL = std("block.iron.fall"),
            BLOCK_IRON_HIT = std("block.iron.hit"),
            BLOCK_IRON_PLACE = std("block.iron.place"),
            BLOCK_IRON_STEP = std("block.iron.step"),
            BLOCK_LEAF_LITTER_BREAK = std("block.leaf_litter.break"),
            BLOCK_LEAF_LITTER_STEP = std("block.leaf_litter.step"),
            BLOCK_LEAF_LITTER_PLACE = std("block.leaf_litter.place"),
            BLOCK_LEAF_LITTER_HIT = std("block.leaf_litter.hit"),
            BLOCK_LEAF_LITTER_FALL = std("block.leaf_litter.fall"),
            BLOCK_SAND_IDLE = std("block.sand.idle"),
            BLOCK_SAND_WIND = std("block.sand.wind"),
            ENTITY_WOLF_ANGRY_AMBIENT = std("entity.wolf_angry.ambient"),
            ENTITY_WOLF_ANGRY_DEATH = std("entity.wolf_angry.death"),
            ENTITY_WOLF_ANGRY_GROWL = std("entity.wolf_angry.growl"),
            ENTITY_WOLF_ANGRY_HURT = std("entity.wolf_angry.hurt"),
            ENTITY_WOLF_ANGRY_PANT = std("entity.wolf_angry.pant"),
            ENTITY_WOLF_ANGRY_WHINE = std("entity.wolf_angry.whine"),
            ENTITY_WOLF_BIG_AMBIENT = std("entity.wolf_big.ambient"),
            ENTITY_WOLF_BIG_DEATH = std("entity.wolf_big.death"),
            ENTITY_WOLF_BIG_GROWL = std("entity.wolf_big.growl"),
            ENTITY_WOLF_BIG_HURT = std("entity.wolf_big.hurt"),
            ENTITY_WOLF_BIG_PANT = std("entity.wolf_big.pant"),
            ENTITY_WOLF_BIG_WHINE = std("entity.wolf_big.whine"),
            ENTITY_WOLF_CUTE_AMBIENT = std("entity.wolf_cute.ambient"),
            ENTITY_WOLF_CUTE_DEATH = std("entity.wolf_cute.death"),
            ENTITY_WOLF_CUTE_GROWL = std("entity.wolf_cute.growl"),
            ENTITY_WOLF_CUTE_HURT = std("entity.wolf_cute.hurt"),
            ENTITY_WOLF_CUTE_PANT = std("entity.wolf_cute.pant"),
            ENTITY_WOLF_CUTE_WHINE = std("entity.wolf_cute.whine"),
            ENTITY_WOLF_GRUMPY_AMBIENT = std("entity.wolf_grumpy.ambient"),
            ENTITY_WOLF_GRUMPY_DEATH = std("entity.wolf_grumpy.death"),
            ENTITY_WOLF_GRUMPY_GROWL = std("entity.wolf_grumpy.growl"),
            ENTITY_WOLF_GRUMPY_HURT = std("entity.wolf_grumpy.hurt"),
            ENTITY_WOLF_GRUMPY_PANT = std("entity.wolf_grumpy.pant"),
            ENTITY_WOLF_GRUMPY_WHINE = std("entity.wolf_grumpy.whine"),
            ENTITY_WOLF_PUGLIN_AMBIENT = std("entity.wolf_puglin.ambient"),
            ENTITY_WOLF_PUGLIN_DEATH = std("entity.wolf_puglin.death"),
            ENTITY_WOLF_PUGLIN_GROWL = std("entity.wolf_puglin.growl"),
            ENTITY_WOLF_PUGLIN_HURT = std("entity.wolf_puglin.hurt"),
            ENTITY_WOLF_PUGLIN_PANT = std("entity.wolf_puglin.pant"),
            ENTITY_WOLF_PUGLIN_WHINE = std("entity.wolf_puglin.whine"),
            ENTITY_WOLF_SAD_AMBIENT = std("entity.wolf_sad.ambient"),
            ENTITY_WOLF_SAD_DEATH = std("entity.wolf_sad.death"),
            ENTITY_WOLF_SAD_GROWL = std("entity.wolf_sad.growl"),
            ENTITY_WOLF_SAD_HURT = std("entity.wolf_sad.hurt"),
            ENTITY_WOLF_SAD_PANT = std("entity.wolf_sad.pant"),
            ENTITY_WOLF_SAD_WHINE = std("entity.wolf_sad.whine");

    @XInfo(since = "1.21.6")
    public static final XSound
            BLOCK_DRIED_GHAST_AMBIENT = std("block.dried_ghast.ambient"),
            BLOCK_DRIED_GHAST_AMBIENT_WATER = std("block.dried_ghast.ambient_water"),
            BLOCK_DRIED_GHAST_BREAK = std("block.dried_ghast.break"),
            BLOCK_DRIED_GHAST_FALL = std("block.dried_ghast.fall"),
            BLOCK_DRIED_GHAST_PLACE = std("block.dried_ghast.place"),
            BLOCK_DRIED_GHAST_PLACE_IN_WATER = std("block.dried_ghast.place_in_water"),
            BLOCK_DRIED_GHAST_STEP = std("block.dried_ghast.step"),
            BLOCK_DRIED_GHAST_TRANSITION = std("block.dried_ghast.transition"),
            BLOCK_DRY_GRASS_AMBIENT = std("block.dry_grass.ambient"),
            ENTITY_GHASTLING_AMBIENT = std("entity.ghastling.ambient"),
            ENTITY_GHASTLING_DEATH = std("entity.ghastling.death"),
            ENTITY_GHASTLING_HURT = std("entity.ghastling.hurt"),
            ENTITY_GHASTLING_SPAWN = std("entity.ghastling.spawn"),
            ENTITY_HAPPY_GHAST_AMBIENT = std("entity.happy_ghast.ambient"),
            ENTITY_HAPPY_GHAST_DEATH = std("entity.happy_ghast.death"),
            ENTITY_HAPPY_GHAST_EQUIP = std("entity.happy_ghast.equip"),
            ENTITY_HAPPY_GHAST_HARNESS_GOGGLES_DOWN = std("entity.happy_ghast.harness_goggles_down"),
            ENTITY_HAPPY_GHAST_HARNESS_GOGGLES_UP = std("entity.happy_ghast.harness_goggles_up"),
            ENTITY_HAPPY_GHAST_HURT = std("entity.happy_ghast.hurt"),
            ENTITY_HAPPY_GHAST_RIDING = std("entity.happy_ghast.riding"),
            ENTITY_HAPPY_GHAST_UNEQUIP = std("entity.happy_ghast.unequip"),
            ITEM_HORSE_ARMOR_UNEQUIP = std("item.horse_armor.unequip"),
            ITEM_LEAD_BREAK = std("item.lead.break"),
            ITEM_LEAD_TIED = std("item.lead.tied"),
            ITEM_LEAD_UNTIED = std("item.lead.untied"),
            ITEM_LLAMA_CARPET_UNEQUIP = std("item.llama_carpet.unequip"),
            ITEM_SADDLE_UNEQUIP = std("item.saddle.unequip"),
            ITEM_SHEARS_SNIP = std("item.shears.snip"),
            MUSIC_DISC_TEARS = std("music_disc.tears");

    @XInfo(since = "1.21.7")
    public static final XSound
            MUSIC_DISC_LAVA_CHICKEN = std("music_disc.lava_chicken");

    static {
        REGISTRY.discardMetadata();
    }

    /**
     * A list of sounds that are labelled as <a href="https://minecraft.fandom.com/wiki/Music">"music"</a> (usually longer than 30 seconds)
     *
     * @since 10.2.0
     */
    @Unmodifiable
    public static final Set<XSound> MUSIC = Collections.unmodifiableSet(REGISTRY.nameMapping().values().stream()
            .filter(x -> x.name().toUpperCase(Locale.ENGLISH).startsWith("MUSIC"))
            .collect(Collectors.toSet())
    );

    public static final float DEFAULT_VOLUME = 1.0f, DEFAULT_PITCH = 1.0f;
    public static final Pattern NAMESPACED_SOUND_PATTERN = Pattern.compile("(?<namespace>[a-z0-9._-]+):(?<key>[a-z0-9/._-]+)");
    /**
     * Just available as a proof of concept. The internal parser doesn't use RegEx.
     *
     * @since 10.2.0
     */
    public static final Pattern RECORD_PATTERN = Pattern.compile(
            "\\s*(?<atLocation>~)?\\s*(?:(?<category>[\\w$_]+)@)?" +
                    "(?<sound>[\\w$_]+|" + NAMESPACED_SOUND_PATTERN.pattern() + ")\\s*" +
                    "(?:,\\s*(?<volume>[+-]?(?:\\d*\\.)?\\d+)\\s*(?:,\\s*(?<pitch>[+-]?(?:\\d*\\.)?\\d+))?)?\\s*");

    public enum Category {
        MASTER, MUSIC, RECORDS, WEATHER, BLOCKS,
        HOSTILE, NEUTRAL, PLAYERS, AMBIENT, VOICE;

        private final Object bukkitObject;

        public boolean isSupported() {
            return this.bukkitObject != null;
        }

        @SuppressWarnings("unchecked")
        private static <T> T cast(Object any) {
            return (T) any;
        }

        Category() {
            Object sc = null;
            try {
                sc = Enums.getIfPresent(cast(Class.forName("org.bukkit.SoundCategory")), this.name()).orNull();
            } catch (ClassNotFoundException ignored) {
            }
            this.bukkitObject = sc;
        }

        public Object getBukkitObject() {
            return bukkitObject;
        }
    }

    private XSound(Sound sound, String[] names) {
        super(sound, names);
    }

    private static XSound std(String... names) {
        return REGISTRY.std(names);
    }

    /**
     * Parses the XSound with the given name.
     *
     * @param sound the name of the sound.
     * @return a matched XSound.
     * @since 1.0.0
     * @deprecated use {@link #of(String)} instead.
     */
    @NotNull
    @Deprecated
    public static Optional<XSound> matchXSound(@NotNull String sound) {
        return REGISTRY.getByName(sound);
    }

    /**
     * Parses the XSound with the given bukkit sound.
     *
     * @param sound the Bukkit sound.
     * @return a matched sound.
     * @throws IllegalArgumentException may be thrown as an unexpected exception.
     * @since 2.0.0
     * @deprecated use {@link #of(Sound)} instead.
     */
    @NotNull
    @Deprecated
    public static XSound matchXSound(@NotNull Sound sound) {
        return REGISTRY.getByBukkitForm(sound);
    }

    public static XSound of(@NotNull Sound bukkit) {return REGISTRY.getByBukkitForm(bukkit);}

    public static Optional<XSound> of(@NotNull String bukkit) {return REGISTRY.getByName(bukkit);}

    /**
     * Use {@link #getValues()} instead.
     */
    @Deprecated
    public static XSound[] values() {
        return REGISTRY.values();
    }

    @NotNull
    @Unmodifiable
    public static Collection<XSound> getValues() {
        return REGISTRY.getValues();
    }

    private static List<String> split(@NotNull String str, @SuppressWarnings("SameParameterValue") char separatorChar) {
        List<String> list = new ArrayList<>(4);
        boolean match = false, lastMatch = false;
        int len = str.length();
        int start = 0;

        for (int i = 0; i < len; i++) {
            if (str.charAt(i) == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }

                // This is important, it should not be i++
                start = i + 1;
                continue;
            }

            lastMatch = false;
            match = true;
        }

        if (match || lastMatch) {
            list.add(str.substring(start, len));
        }
        return list;
    }

    /**
     * A short handy method to play a sound from configs.
     * E.g.
     * <pre>
     *     play("BURP, 1, 1", x -> x.forPlayers(player));
     * </pre>
     *
     * @param soundPlayer The player used if the sound is corretly parsed. No need to call {@link SoundPlayer#play()}
     * @see #parse(String)
     * @since 10.0.0
     */
    @Nullable
    public static Record play(@Nullable String sound, Consumer<SoundPlayer> soundPlayer) {
        Record record;
        try {
            record = parse(sound);
        } catch (Throwable ex) {
            return null;
        }
        if (record == null) return null;

        SoundPlayer player = record.soundPlayer();
        soundPlayer.accept(player);
        player.play();
        return record;
    }

    /**
     * Just an extra feature that loads sounds from strings.
     * Useful for getting sounds from config files.
     * Sounds are thread safe.
     * <p>
     * It's strongly recommended to use this method while using it inside a loop.
     * This can help to avoid parsing the sound properties multiple times.
     * A simple usage of using it in a loop is:
     * <blockquote><pre>
     *     Record record = XSound.parse(player, location, sound, false).join();
     *     // Loop:
     *     if (record != null) record.play();
     * </pre></blockquote>
     * <p>
     * This will also ignore {@code none} and {@code null} strings.
     * <p>
     * <b>Format:</b> [~]Sound@Category, [Volume], [Pitch]<br>
     * Where {@code ~} prefix will play the sound at the location even if a player is specified.
     * A sound played at a location will be heard by everyone around.
     * <p>
     * <b>Examples:</b>
     * <p>
     * <pre>
     *     ~ENTITY_PLAYER_BURP@MASTER, 2.5, 0.5
     *     ENTITY_PLAYER_BURP, 0.5, 1
     *     BURP, 0.5, 1
     *     MUSIC_END, 10
     *     ~MUSIC_END, 10
     *     none (case-insensitive)
     *     null (~ in yml)
     * </pre>
     * <p>
     *
     * @param sound the string of the sound with volume and pitch (if needed).
     * @since 7.0.0
     */
    @Nullable
    public static Record parse(@Nullable String sound) {
        if (Strings.isNullOrEmpty(sound) || sound.equalsIgnoreCase("none")) return null;
        @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern") List<String> split = split(sound.replace(" ", ""), ',');

        Record record = new Record();
        String name = split.get(0);
        if (name.charAt(0) == '~') {
            name = name.substring(1);
            record.publicSound(true);
        } else {
            record.publicSound(false);
        }

        if (name.isEmpty()) throw new IllegalArgumentException("No sound name specified: " + sound);
        {
            String soundName;
            int atIndex = name.indexOf('@');
            if (atIndex != -1) {
                String category = name.substring(0, atIndex);
                soundName = name.substring(atIndex + 1);

                Category soundCategory = Enums.getIfPresent(Category.class, category.toUpperCase(Locale.ENGLISH)).orNull();
                if (soundCategory == null)
                    throw new IllegalArgumentException("Unknown sound category '" + category + "' in: " + sound);
                else record.inCategory(soundCategory);
            } else {
                soundName = name;
            }

            if (soundName.isEmpty()) {
                throw new IllegalArgumentException("No sound name specified: " + name);
            }

            Optional<XSound> soundType = of(soundName);
            if (!soundType.isPresent()) {
                if (soundName.indexOf(':') != -1) {
                    soundName = soundName.toLowerCase(Locale.ENGLISH);
                    if (!NAMESPACED_SOUND_PATTERN.matcher(soundName).matches()) {
                        throw new IllegalArgumentException("Unknown sound '" + soundName + "', invalid namespace characters: " + name);
                    } else {
                        record.withSound(soundName);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown sound: " + name + " -> '" + soundName + '\'');
                }
            } else {
                record.withSound(soundType.get());
            }
        }

        try {
            if (split.size() > 1) record.withVolume(Float.parseFloat(split.get(1)));
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Invalid number '" + split.get(1) + "' for sound volume '" + sound + '\'');
        }
        try {
            if (split.size() > 2) record.withPitch(Float.parseFloat(split.get(2)));
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Invalid number '" + split.get(2) + "' for sound pitch '" + sound + '\'');
        }

        try {
            if (split.size() > 3) record.withSeed(Long.parseLong(split.get(3)));
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Invalid number '" + split.get(3) + "' for sound seed '" + sound + '\'');
        }

        return record;
    }

    /**
     * Stops all the playing musics (not all the sounds)
     * <p>
     * Note that this method will only work for the sound
     * that are sent from {@link Player#playSound} and
     * the sounds played from the client will not be
     * affected by this.
     *
     * @param player the player to stop all the sounds from.
     * @see #stopSound(Player)
     * @since 2.0.0
     */
    public static void stopMusic(@NotNull Player player) {
        Objects.requireNonNull(player, "Cannot stop playing musics from null player");
        for (XSound music : MUSIC) {
            Sound sound = music.get();
            if (sound != null) player.stopSound(sound);
        }
    }

    /**
     * Parses the XSound as a {@link Sound} based on the server version.
     *
     * @return the vanilla sound.
     * @since 1.0.0
     * @deprecated use {@link #get()}
     */
    @Nullable
    @Deprecated
    public Sound parseSound() {
        return get();
    }

    /**
     * Stops playing the specified sound from the player.
     *
     * @param player the player to stop playing the sound to.
     * @see #stopMusic(Player)
     * @since 2.0.0
     */
    public void stopSound(@NotNull Player player) {
        Objects.requireNonNull(player, "Cannot stop playing sound from null player");
        Sound sound = this.get();
        if (sound != null) player.stopSound(sound);
    }

    /**
     * Plays a normal sound to an entity.
     *
     * @param entity the entity to play the sound to.
     * @since 1.0.0
     */
    public void play(@NotNull Entity entity) {
        Objects.requireNonNull(entity, "Cannot play sound for null entity");
        SoundPlayer soundPlayer = this.record().soundPlayer();
        if (entity instanceof Player) {
            soundPlayer.forPlayers((Player) entity);
        } else if (entity instanceof LivingEntity) {
            soundPlayer.atLocation(((LivingEntity) entity).getEyeLocation());
        } else {
            soundPlayer.atLocation(entity.getLocation());
        }
        soundPlayer.play();
    }

    /**
     * Plays a normal sound in a location.
     *
     * @param location the location to play the sound in.
     * @since 2.0.0
     */
    public void play(@NotNull Location location) {
        Objects.requireNonNull(location, "Cannot play sound at null location");
        this.record().soundPlayer().atLocation(location).play();
    }

    public void play(@NotNull Entity entity, float volume, float pitch) {
        if (!(entity instanceof Player)) {
            Location location;
            if (entity instanceof LivingEntity) {
                location = ((LivingEntity) entity).getEyeLocation();
            } else {
                location = entity.getLocation();
            }

            play(location, volume, pitch);
            return;
        }

        this.record()
                .withVolume(volume)
                .withPitch(pitch)
                .soundPlayer()
                .forPlayers((Player) entity)
                .play();
    }

    public void play(@NotNull Location location, float volume, float pitch) {
        this.record()
                .withVolume(volume)
                .withPitch(pitch)
                .soundPlayer()
                .atLocation(location)
                .play();
    }

    /**
     * @since 10.0.0
     */
    public Record record() {
        return new Record().withSound(this);
    }

    public static final class SoundPlayer {
        private static final byte SUPPORTED_METHOD_LEVEL;

        static {
            byte level;
            try {
                Player.class.getDeclaredMethod("playSound", Location.class, String.class, SoundCategory.class, float.class, float.class, long.class);
                level = 3;
            } catch (Throwable e) {
                try {
                    Player.class.getDeclaredMethod("playSound", Location.class, String.class, SoundCategory.class, float.class, float.class);
                    level = 2;
                } catch (Throwable ee) {
                    try {
                        Player.class.getDeclaredMethod("playSound", Location.class, Sound.class, float.class, float.class);
                        level = 1;
                    } catch (Throwable eee) {
                        throw new UnsupportedOperationException("None of sound methods are supported", eee);
                    }
                }
            }

            SUPPORTED_METHOD_LEVEL = level;
        }

        public Record record;
        public Set<UUID> players = new HashSet<>(10);
        public Set<UUID> heard = new HashSet<>();

        @Nullable
        public Location location;

        public SoundPlayer(Record record) {
            withRecord(record);
        }

        public SoundPlayer withRecord(Record record) {
            this.record = Objects.requireNonNull(record, "Cannot play a null record");
            return this;
        }

        /**
         * Plays the sound only for a single player and no one else can hear it.
         */
        public SoundPlayer forPlayers(@Nullable Player... players) {
            this.players.clear();
            if (players != null && players.length > 0) {
                this.players.addAll(Arrays.stream(players).map(Entity::getUniqueId).collect(Collectors.toSet()));
            }
            return this;
        }

        /**
         * Plays the sound to all the nearby players (based on the specified volume)
         */
        public SoundPlayer atLocation(@Nullable Location location) {
            this.location = location;
            return this;
        }

        /**
         * Play the sound for the given players.
         */
        public SoundPlayer forPlayers(@Nullable Collection<Player> players) {
            this.players.clear();
            this.players.addAll(players.stream().map(Entity::getUniqueId).collect(Collectors.toList()));
            return this;
        }

        /**
         * Gets a list of players who can hear this sound.
         */
        public Collection<Player> getHearingPlayers() {
            if (record.publicSound || players.isEmpty()) {
                Location loc;
                if (location == null) {
                    if (players.size() != 1)
                        throw new IllegalStateException("Cannot play public sound when no location is specified: " + this);

                    Player player = Bukkit.getPlayer(players.iterator().next());
                    if (player == null) return new ArrayList<>();
                    else loc = player.getEyeLocation();
                } else {
                    loc = this.location;
                }
                return getHearingPlayers(loc, record.volume);
            } else {
                return toOnlinePlayers(this.players, Collectors.toList());
            }
        }

        /**
         * Gets a list of players that can hear this sound at the given location and volume.
         * This method pretty much uses the default algorithm used by Bukkit.
         *
         * @param location The location which the sound is going to be played.
         * @param volume   The volume of the sound being played. Also see {@link Record#volume}
         */
        @NotNull
        public static Collection<Player> getHearingPlayers(Location location, double volume) {
            // Increase the amount of blocks for volumes higher than 1
            volume = volume > 1.0F ? (16.0F * volume) : 16.0;
            double powerVolume = volume * volume;

            List<Player> playersInWorld = location.getWorld().getPlayers();
            List<Player> hearing = new ArrayList<>(playersInWorld.size());

            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();

            for (Player player : playersInWorld) {
                Location loc = player.getLocation();
                double deltaX = x - loc.getX();
                double deltaY = y - loc.getY();
                double deltaZ = z - loc.getZ();

                double length = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
                if (length < powerVolume) hearing.add(player);
            }

            return hearing;
        }

        /**
         * Plays the sound with the given options and updating the player's location.
         *
         * @since 3.0.0
         */
        public void play() {
            Location loc;
            if (location == null) {
                if (players.size() == 1) {
                    UUID first = players.iterator().next();
                    Player player = Bukkit.getPlayer(first);
                    if (player == null) return;
                    loc = player.getEyeLocation();
                } else {
                    throw new IllegalStateException("Cannot play sound when there is no location available");
                }
            } else {
                loc = location;
            }

            play(loc);
        }

        /**
         * Plays the sound with the updated location.
         * Uses PacketPlayOutNamedSoundEffect.
         *
         * @param updatedLocation the updated location.
         * @since 3.0.0
         */
        public void play(@NotNull Location updatedLocation) {
            Collection<Player> hearing = getHearingPlayers();
            this.heard = hearing.stream().map(Entity::getUniqueId).collect(Collectors.toSet());

            if (hearing.isEmpty()) return;
            play(hearing, updatedLocation);
        }

        private static <A, R> R toOnlinePlayers(Collection<UUID> players, Collector<Player, A, R> collector) {
            return players.stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .collect(collector);
        }

        public void play(Collection<Player> players, @NotNull Location updatedLocation) {
            Objects.requireNonNull(updatedLocation, "Cannot play sound at null location");

            Sound objSound = record.sound instanceof XSound ? ((XSound) record.sound).get() : null;
            String strSound = record.sound instanceof String ? (String) record.sound : null;

            for (Player player : players) {
                // https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Player.html#playSound(org.bukkit.Location,java.lang.String,org.bukkit.SoundCategory,float,float,long)

                switch (SUPPORTED_METHOD_LEVEL) {
                    case 3: // Category + Seed
                        if (objSound != null)
                            player.playSound(updatedLocation, objSound, (SoundCategory) record.category.getBukkitObject(), record.volume, record.pitch, record.generateSeed());
                        else
                            player.playSound(updatedLocation, strSound, (SoundCategory) record.category.getBukkitObject(), record.volume, record.pitch, record.generateSeed());
                        break;
                    case 2: // Category
                        if (objSound != null)
                            player.playSound(updatedLocation, objSound, (SoundCategory) record.category.getBukkitObject(), record.volume, record.pitch);
                        else
                            player.playSound(updatedLocation, strSound, (SoundCategory) record.category.getBukkitObject(), record.volume, record.pitch);
                        break;
                    case 1: // None
                        if (objSound != null) player.playSound(updatedLocation, objSound, record.volume, record.pitch);
                        else player.playSound(updatedLocation, strSound, record.volume, record.pitch);
                        break;
                    default:
                        throw new IllegalStateException("Unknown format: " + SUPPORTED_METHOD_LEVEL);
                }
            }
        }

        /**
         * Stops the sound playing to the players that this sound was played to.
         * Note this works fine if the sound was played to one specific player, but for
         * location-based sounds this only works if the players were within the same range as the original
         * volume level.
         * <p>
         * If this is a critical issue you can extend this class and add a cache for all the players that heard the sound.
         *
         * @since 7.0.2
         */
        public void stopSound() {
            if (heard == null || heard.isEmpty()) return;

            List<Player> heardOnline = toOnlinePlayers(this.heard, Collectors.toList());
            heardOnline.forEach(x -> {
                if (record.sound instanceof XSound) x.stopSound(((XSound) record.sound).get());
                else x.stopSound((String) record.sound);
            });
        }
    }

    /**
     * A class to help caching and playing sound properties parsed from config.
     *
     * @since 3.0.0
     */
    public static final class Record {
        private static final Random RANDOM = new Random();

        private Object sound;

        @NotNull
        private Category category = Category.MASTER;

        @Nullable
        private Long seed;

        /**
         * The default value is 1.0 and the range of the volume can be controlled from
         * 0.0 to 1.0, any values higher than 1.0 will affect the distance in blocks which
         * the player can hear the sound from.
         *
         * @see SoundPlayer#getHearingPlayers(Location, double)
         */
        private float volume = DEFAULT_VOLUME;
        private float pitch = DEFAULT_PITCH;
        private boolean publicSound;

        @Nullable
        public Long getSeed() {
            return seed;
        }

        public Object std() {
            return sound;
        }

        @NotNull
        public Category getCategory() {
            return category;
        }

        public float getVolume() {
            return volume;
        }

        public float getPitch() {
            return pitch;
        }

        public Record inCategory(Category category) {
            this.category = Objects.requireNonNull(category, "Sound category cannot be null");
            return this;
        }

        /**
         * @return a new {@link SoundPlayer} object.
         */
        public SoundPlayer soundPlayer() {
            return new SoundPlayer(this);
        }

        public Record withSound(@NotNull XSound sound) {
            Objects.requireNonNull(sound, "Cannot play a null sound");
            this.sound = sound;
            return this;
        }

        /**
         * The sound including the namespace and the key.
         * E.g. for {@link #ENTITY_PLAYER_HURT} it'd be {@code minecraft:entity_player_hurt}
         * you can use other namespaces instead of "minecraft" to use sounds from resource packs.
         */
        public Record withSound(@NotNull String sound) {
            Objects.requireNonNull(sound, "Cannot play a null sound");
            sound = sound.toLowerCase(Locale.ENGLISH);

            if (sound.indexOf(':') < 0) throw new IllegalArgumentException(
                    "Raw sound name doesn't contain both namespace and key: " + sound);

            this.sound = sound;
            return this;
        }

        public long generateSeed() {
            return seed == null ? RANDOM.nextLong() : seed;
        }

        public Record withVolume(float volume) {
            this.volume = volume;
            return this;
        }

        /**
         * Whether to play this sound to all nearby players or
         * just the players specified in the {@link SoundPlayer#players} list.
         */
        public Record publicSound(boolean publicSound) {
            this.publicSound = publicSound;
            return this;
        }

        public Record withPitch(float pitch) {
            this.pitch = pitch;
            return this;
        }

        /**
         * Some sounds have different variations. Using a static seed will always play
         * the same variation for that sound.
         *
         * @param seed Randomizes the variation of null.
         */
        public Record withSeed(Long seed) {
            this.seed = seed;
            return this;
        }

        public String rebuild() {
            String str = "";
            if (publicSound) str += "~";
            if (category != Category.MASTER) str += category.name();
            str += sound + ", " + volume + ", " + pitch;
            if (seed != null) str += ", " + seed;
            return str;
        }

        @Override
        public String toString() {
            return "Record{" +
                    "sound=" + sound +
                    ", category=" + category +
                    ", seed=" + seed +
                    ", volume=" + volume +
                    ", pitch=" + pitch +
                    ", publicSound=" + publicSound +
                    '}';
        }

        public Record copy() {
            Record record = new Record();
            record.sound = sound;
            record.volume = volume;
            record.pitch = pitch;
            record.publicSound = publicSound;
            record.seed = seed;
            return record;
        }
    }
}
