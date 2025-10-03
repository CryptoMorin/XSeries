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

package com.cryptomorin.xseries.profiles.objects;

import com.cryptomorin.xseries.profiles.PlayerProfiles;
import com.cryptomorin.xseries.profiles.ProfilesCore;
import com.cryptomorin.xseries.profiles.exceptions.InvalidProfileContainerException;
import com.cryptomorin.xseries.profiles.gameprofile.MojangGameProfile;
import com.cryptomorin.xseries.profiles.gameprofile.XGameProfile;
import com.mojang.authlib.GameProfile;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represenets any object that has a {@link GameProfile} which can also be changed.
 *
 * @param <T> the bukkit object.
 */
@ApiStatus.Internal
public abstract class ProfileContainer<T> implements Profileable {
    @NotNull
    public abstract void setProfile(@Nullable MojangGameProfile profile);

    @NotNull
    public abstract T getObject();

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public final String toString() {
        return this.getClass().getSimpleName() + '[' + getObject() + ']';
    }

    public static final class ItemStackProfileContainer extends ProfileContainer<ItemStack> implements DelegateProfileable {
        private final ItemStack itemStack;

        public ItemStackProfileContainer(ItemStack itemStack) {this.itemStack = Objects.requireNonNull(itemStack, "ItemStack is null");}

        private ItemMetaProfileContainer getMetaContainer(ItemMeta meta) {
            if (!(meta instanceof SkullMeta))
                throw new InvalidProfileContainerException(itemStack, "Item can't contain texture: " + itemStack);
            return new ItemMetaProfileContainer((SkullMeta) meta);
        }

        @Override
        public void setProfile(@Nullable MojangGameProfile profile) {
            ItemMeta meta = itemStack.getItemMeta();
            getMetaContainer(meta).setProfile(profile);
            itemStack.setItemMeta(meta);
        }

        @Override
        public ItemStack getObject() {
            return itemStack;
        }

        @Override
        public Profileable getDelegateProfile() {
            return getMetaContainer(itemStack.getItemMeta());
        }
    }

    public static final class ItemMetaProfileContainer extends ProfileContainer<ItemMeta> {
        private final ItemMeta meta;

        public ItemMetaProfileContainer(SkullMeta meta) {this.meta = Objects.requireNonNull(meta, "ItemMeta is null");}

        @Override
        public void setProfile(@Nullable MojangGameProfile profile) {
            try {
                ProfilesCore.CraftMetaSkull_profile$setter.invoke(meta, PlayerProfiles.toResolvableProfile(profile));
            } catch (Throwable throwable) {
                throw new IllegalStateException("Unable to set profile " + profile + " to " + meta, throwable);
            }
        }

        @Override
        public ItemMeta getObject() {
            return meta;
        }

        @Override
        public MojangGameProfile getProfile() {
            try {
                return XGameProfile.of(PlayerProfiles.fromResolvableProfile(ProfilesCore.CraftMetaSkull_profile$getter.invoke((SkullMeta) meta)));
            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to get profile from item meta: " + meta, throwable);
            }
        }
    }

    public static final class BlockProfileContainer extends ProfileContainer<Block> implements DelegateProfileable {
        private final Block block;

        public BlockProfileContainer(Block block) {this.block = Objects.requireNonNull(block, "Block is null");}

        private Skull getBlockState() {
            BlockState state = block.getState();
            if (!(state instanceof Skull))
                throw new InvalidProfileContainerException(block, "Block can't contain texture: " + block);
            return (Skull) state;
        }

        @Override
        public void setProfile(@Nullable MojangGameProfile profile) {
            Skull state = getBlockState();
            new BlockStateProfileContainer(state).setProfile(profile);
            state.update(true);
        }

        @Override
        public Block getObject() {
            return block;
        }

        @Override
        public Profileable getDelegateProfile() {
            return new BlockStateProfileContainer(getBlockState());
        }
    }

    public static final class BlockStateProfileContainer extends ProfileContainer<Skull> {
        private final Skull state;

        public BlockStateProfileContainer(Skull state) {this.state = Objects.requireNonNull(state, "Skull BlockState is null");}

        @Override
        public void setProfile(@Nullable MojangGameProfile profile) {
            try {
                ProfilesCore.CraftSkull_profile$setter.invoke(state, PlayerProfiles.toResolvableProfile(profile));
            } catch (Throwable throwable) {
                throw new IllegalStateException("Unable to set profile " + profile + " to " + state, throwable);
            }
        }

        @Override
        public Skull getObject() {
            return state;
        }

        @Override
        public MojangGameProfile getProfile() {
            try {
                return XGameProfile.of(PlayerProfiles.fromResolvableProfile(ProfilesCore.CraftSkull_profile$getter.invoke(state)));
            } catch (Throwable throwable) {
                throw new IllegalStateException("Unable to get profile fr om blockstate: " + state, throwable);
            }
        }
    }
}