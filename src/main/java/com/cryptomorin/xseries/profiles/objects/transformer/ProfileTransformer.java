package com.cryptomorin.xseries.profiles.objects.transformer;

import com.cryptomorin.xseries.profiles.PlayerProfiles;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Modifies a {@link GameProfile} by a set of defined operations.
 * Transformers can be composed and should be applied in order.
 */
public interface ProfileTransformer {
    /**
     * Transforms the given profile by the defined operations.
     * The operations are applied on the same instance of the profile.
     * @param profile the profile to apply the operations on.
     * @return may return the same or a new instance.
     */
    @NotNull
    GameProfile transform(@NotNull GameProfile profile);

    /**
     * Whether the results of this transformation can be cached or not.
     */
    @ApiStatus.Internal
    boolean canBeCached();

    /**
     * By default, due to internal changes to the {@link GameProfile},
     * (specially the Base64-encoded textures property) the items are
     * considered "different" (for example {@link org.bukkit.inventory.ItemStack#isSimilar(ItemStack)}
     * would return false)
     * which means they cannot be stacked together in the game.
     * <p>
     * These changes can include:
     * <ul>
     *     <li>Mojang's cache/API might add/remove properties from the profile.</li>
     *     <li>Mojang's API adds a timestamp property that shows when was the last
     *         time the profile was updated. This can be frequent or take up to hours to change.</li>
     *     <li>Mojang's cache/API might change the order or format of the textures JSON.</li>
     *     <li>Players that change a portion of their skin (other than the head) will
     *         still be visually the same, but their texture URL will be changed.</li>
     *     <li>Profiles created/modified by XSeries have a special signature added.</li>
     * </ul>
     * <br>
     * This transformer, makes the items stackable (x64 stack)
     * This currently has no observable effect on blocks.
     * Technically the same as {@link #removeMetadata()} but it's
     * here for future compatibility purposes.
     * @see #nonStackable()
     */
    static ProfileTransformer stackable() {
        return RemoveMetadata.INSTANCE;
    }

    /**
     * Makes the items non-stackable (x64 stack)
     * Read {@link #stackable()} for more info. This adds additional properties other than
     * the ones mentioned in the other method that ensures items can never be stacked no
     * matter how similar they are.
     * @see #stackable()
     */
    static ProfileTransformer nonStackable() {
        return MakeNotStackable.INSTANCE;
    }

    /**
     * Removes extra properties from this profile.
     * This includes the XSeries signature and the timestamp of the profile.
     * Both which have no effect when removed, but the XSeries signature could
     * be useful in debugging.
     *
     * @see #stackable()
     */
    static ProfileTransformer removeMetadata() {
        return RemoveMetadata.INSTANCE;
    }

    final class MakeNotStackable implements ProfileTransformer {
        private static final MakeNotStackable INSTANCE = new MakeNotStackable();
        private static final String PROPERTY_NAME = "XSeriesSeed";
        private static final AtomicLong NEXT_ID = new AtomicLong();

        @Override
        public GameProfile transform(GameProfile profile) {
            String value = System.currentTimeMillis() + "-" + NEXT_ID.getAndIncrement();
            profile.getProperties().put(PROPERTY_NAME, new Property(PROPERTY_NAME, value));
            return profile;
        }

        @Override
        public boolean canBeCached() {
            return false;
        }
    }

    final class RemoveMetadata implements ProfileTransformer {
        private static final RemoveMetadata INSTANCE = new RemoveMetadata();

        @Override
        public GameProfile transform(GameProfile profile) {
            PlayerProfiles.removeTimestamp(profile);
            // It's a multimap, remove all values associated to this key.
            profile.getProperties().asMap().remove(PlayerProfiles.DEFAULT_PROFILE_NAME);
            return profile;
        }

        @Override
        public boolean canBeCached() {
            return true;
        }
    }
}
