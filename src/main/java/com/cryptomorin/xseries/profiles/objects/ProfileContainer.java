package com.cryptomorin.xseries.profiles.objects;

import com.cryptomorin.xseries.profiles.ProfilesCore;
import com.cryptomorin.xseries.profiles.exceptions.InvalidProfileContainerException;
import com.mojang.authlib.GameProfile;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Represenets any object that has a {@link GameProfile} which can also be changed.
 * @param <T> the bukkit object.
 */
@ApiStatus.Internal
public abstract class ProfileContainer<T> implements Profileable {
    @Nonnull
    public abstract void setProfile(@Nullable GameProfile profile);

    public abstract T getObject();

    @Override
    public final String toString() {
        return this.getClass().getSimpleName() + '[' + getObject() + ']';
    }

    public static final class ItemStackProfileContainer extends ProfileContainer<ItemStack> {
        private final ItemStack itemStack;

        public ItemStackProfileContainer(ItemStack itemStack) {this.itemStack = Objects.requireNonNull(itemStack);}

        private ItemMetaProfileContainer getMetaContainer(ItemMeta meta) {
            if (!(meta instanceof SkullMeta))
                throw new InvalidProfileContainerException("Item can't contain texture: " + itemStack);
            return new ItemMetaProfileContainer((SkullMeta) meta);
        }

        @Override
        public void setProfile(GameProfile profile) {
            ItemMeta meta = itemStack.getItemMeta();
            getMetaContainer(meta).setProfile(profile);
            itemStack.setItemMeta(meta);
        }

        @Override
        public ItemStack getObject() {
            return itemStack;
        }

        @Override
        public GameProfile getProfile() {
            return getMetaContainer(itemStack.getItemMeta()).getProfile();
        }
    }

    public static final class ItemMetaProfileContainer extends ProfileContainer<ItemMeta> {
        private final ItemMeta meta;

        public ItemMetaProfileContainer(SkullMeta meta) {this.meta = Objects.requireNonNull(meta);}

        @Override
        public void setProfile(GameProfile profile) {
            try {
                ProfilesCore.CRAFT_META_SKULL_PROFILE_SETTER.invoke(meta, profile);
            } catch (Throwable throwable) {
                throw new RuntimeException("Unable to set profile " + profile + " to " + meta, throwable);
            }
        }

        @Override
        public ItemMeta getObject() {
            return meta;
        }

        @Override
        public GameProfile getProfile() {
            try {
                return (GameProfile) ProfilesCore.CRAFT_META_SKULL_PROFILE_GETTER.invoke((SkullMeta) meta);
            } catch (Throwable throwable) {
                throw new RuntimeException("Failed to get profile from item meta: " + meta, throwable);
            }
        }
    }

    public static final class BlockProfileContainer extends ProfileContainer<Block> {
        private final Block block;

        public BlockProfileContainer(Block block) {this.block = Objects.requireNonNull(block);}

        private Skull getBlockState() {
            BlockState state = block.getState();
            if (!(state instanceof Skull))
                throw new InvalidProfileContainerException("Block can't contain texture: " + block);
            return (Skull) state;
        }

        @Override
        public void setProfile(GameProfile profile) {
            Skull state = getBlockState();
            new BlockStateProfileContainer(state).setProfile(profile);
            state.update(true);
        }

        @Override
        public Block getObject() {
            return block;
        }

        @Override
        public GameProfile getProfile() {
            return new BlockStateProfileContainer(getBlockState()).getProfile();
        }
    }

    public static final class BlockStateProfileContainer extends ProfileContainer<Skull> {
        private final Skull state;

        public BlockStateProfileContainer(Skull state) {this.state = Objects.requireNonNull(state);}

        @Override
        public void setProfile(GameProfile profile) {
            try {
                ProfilesCore.CRAFT_SKULL_PROFILE_SETTER.invoke(state, profile);
            } catch (Throwable throwable) {
                throw new RuntimeException("Unable to set profile " + profile + " to " + state, throwable);
            }
        }

        @Override
        public Skull getObject() {
            return state;
        }

        @Override
        public GameProfile getProfile() {
            try {
                return (GameProfile) ProfilesCore.CRAFT_SKULL_PROFILE_GETTER.invoke(state);
            } catch (Throwable throwable) {
                throw new RuntimeException("Unable to get profile fr om blockstate: " + state, throwable);
            }
        }
    }
}