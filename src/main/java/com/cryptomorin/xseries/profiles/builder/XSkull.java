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
package com.cryptomorin.xseries.profiles.builder;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.profiles.PlayerProfiles;
import com.cryptomorin.xseries.profiles.objects.ProfileContainer;
import com.cryptomorin.xseries.profiles.objects.ProfileInputType;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import com.cryptomorin.xseries.reflection.XReflection;
import com.mojang.authlib.GameProfile;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * <b>XSkull</b> - Apply skull texture from different sources.<br><br>
 * Skull Meta: <a href="https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/meta/SkullMeta.html">hub.spigotmc.org/.../SkullMeta</a><br>
 * Mojang API: <a href="https://wiki.vg/Mojang_API">wiki.vg/Mojang_API</a><br><br>
 * <p>
 * Some websites to get custom heads:
 * <ul>
 *     <li><a href="https://minecraft-heads.com/">minecraft-heads.com</a></li>
 * </ul>
 *
 * <h1>Usage</h1>
 * The basic usage format of this API is as follows:
 * <pre>{@code
 * XSkull.createItem().profile(player).apply();
 * XSkull.of(item/block).profile(configValueString).apply();
 * }</pre>
 * <p>
 * Note: Make sure to read {@link ProfileInstruction#applyAsync()} if you're going to
 * be requesting a lot of different skulls.
 *
 * <h1>Mechanism</h1>
 * <p>
 * The basic premise behind this API is that the final skull data is contained in a {@link GameProfile}
 * either by ID, name or encoded textures URL property.
 * <p>
 * Different versions of Minecraft client handle this differently. In newer versions the client seem
 * to prioritize the texture property over the set UUID and name, in older versions however using the
 * same UUID for all GameProfiles caused all skulls (that use base64) to look the same.
 * The client is responsible for caching skull textures. If the download were to fail (either because of
 * connection issues or invalid values) the client will cache that skull UUID and the skull
 * will remain as a steve head until the client is completely restarted.
 * I don't know if this cache system works across other servers or is just specific to one server.
 *
 * @author Crypto Morin
 * @version 11.2.0
 * @see XMaterial
 * @see XReflection
 */
public final class XSkull {
    /**
     * Creates a {@link ProfileInstruction} for an {@link ItemStack}.
     * This method initializes a new player head.
     *
     * @return A {@link ProfileInstruction} that sets the profile for the generated {@link ItemStack}.
     */
    public static ProfileInstruction<ItemStack> createItem() {
        return of(XMaterial.PLAYER_HEAD.parseItem());
    }

    /**
     * Creates a {@link ProfileInstruction} for an {@link ItemStack}.
     *
     * @param stack The {@link ItemStack} to set the profile for.
     * @return A {@link ProfileInstruction} that sets the profile for the given {@link ItemStack}.
     */
    public static ProfileInstruction<ItemStack> of(ItemStack stack) {
        return new ProfileInstruction<>(new ProfileContainer.ItemStackProfileContainer(stack));
    }

    /**
     * Creates a {@link ProfileInstruction} for an {@link ItemMeta}.
     *
     * @param meta The {@link ItemMeta} to set the profile for.
     * @return An {@link ProfileInstruction} that sets the profile for the given {@link ItemMeta}.
     */
    public static ProfileInstruction<ItemMeta> of(ItemMeta meta) {
        return new ProfileInstruction<>(new ProfileContainer.ItemMetaProfileContainer((SkullMeta) meta));
    }

    /**
     * Creates a {@link ProfileInstruction} for a {@link Block}.
     *
     * @param block The {@link Block} to set the profile for.
     * @return An {@link ProfileInstruction} that sets the profile for the given {@link Block}.
     */
    public static ProfileInstruction<Block> of(Block block) {
        return new ProfileInstruction<>(new ProfileContainer.BlockProfileContainer(block));
    }

    /**
     * Creates a {@link ProfileInstruction} for a {@link BlockState}.
     *
     * @param state The {@link BlockState} to set the profile for.
     * @return An {@link ProfileInstruction} that sets the profile for the given {@link BlockState}.
     */
    public static ProfileInstruction<Skull> of(BlockState state) {
        return new ProfileInstruction<>(new ProfileContainer.BlockStateProfileContainer((Skull) state));
    }


    /**
     * We'll just return an x shaped hardcoded skull.<br>
     * <a href="https://minecraft-heads.com/custom-heads/miscellaneous/58141-cross">minecraft-heads.com</a>
     */
    private static final GameProfile DEFAULT_PROFILE = PlayerProfiles.signXSeries(ProfileInputType.BASE64.getProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5l" +
                    "Y3JhZnQubmV0L3RleHR1cmUvYzEwNTkxZTY5MDllNmEyODFiMzcxODM2ZTQ2MmQ2" +
                    "N2EyYzc4ZmEwOTUyZTkxMGYzMmI0MWEyNmM0OGMxNzU3YyJ9fX0="
    ));

    /**
     * Retrieves the default {@link GameProfile} used by XSkull.
     * This method creates a clone of the default profile to prevent modifications to the original.
     *
     * @return A clone of the default {@link GameProfile}.
     */
    protected static Profileable getDefaultProfile() {
        // We copy this just in case something changes the GameProfile properties.
        GameProfile clone = PlayerProfiles.createGameProfile(DEFAULT_PROFILE.getId(), DEFAULT_PROFILE.getName());
        clone.getProperties().putAll(DEFAULT_PROFILE.getProperties());
        return Profileable.of(clone);
    }
}
