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

package com.cryptomorin.xseries.profiles.objects.transformer;

import com.cryptomorin.xseries.profiles.PlayerProfiles;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Modifies a {@link GameProfile} by a set of defined operations.
 * Transformers can be composed and should be applied in order.
 *
 * @see TransformableProfile
 */
public interface ProfileTransformer {
    /**
     * Transforms the given profile by the defined operations.
     * The operations are applied on the same instance of the profile.
     *
     * @param profileable the profileable which the first {@code profile} originated from.
     * @param profile     the profile to apply the operations on.
     * @return may return the same or a new instance.
     */
    @NotNull
    GameProfile transform(@NotNull Profileable profileable, @NotNull GameProfile profile);

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
     *     <li>A skull with {@link #includeOriginalValue()} with another one that doesn't have this transformation.</li>
     * </ul>
     * <br>
     * This transformer, makes the items stackable (x64 stack)
     * This currently has no observable effect on blocks.
     * Technically the same as {@link #removeMetadata()} but it's
     * here for future compatibility purposes.
     *
     * @see #nonStackable()
     */
    @NotNull
    static ProfileTransformer stackable() {
        return RemoveMetadata.INSTANCE;
    }

    /**
     * Makes the items non-stackable (x64 stack)
     * Read {@link #stackable()} for more info. This adds additional properties other than
     * the ones mentioned in the other method that ensures items can never be stacked no
     * matter how similar they are.
     *
     * @see #stackable()
     */
    @NotNull
    static ProfileTransformer nonStackable() {
        return MakeNotStackable.INSTANCE;
    }

    /**
     * Removes extra properties from this profile.
     * This includes the XSeries signature and the timestamp of the profile.
     * Both which have no effect when removed, but the XSeries signature could
     * be useful in debugging.
     * It also removes {@link #includeOriginalValue()} data if specified.
     *
     * @see #stackable()
     */
    @NotNull
    static ProfileTransformer removeMetadata() {
        return RemoveMetadata.INSTANCE;
    }

    /**
     * Whether a special property should be added to the profile where the original
     * value ({@link Profileable#getProfileValue()}) was used to create the profile.
     * This can later be used to affect {@link Profileable#getProfileValue()} which
     * provides less verbose and compact data instead of the regular base64.
     * <p>
     * This transformation is not applied by default and also,
     * {@link #stackable()} or {@link #removeMetadata()} removes this data.
     */
    @NotNull
    static ProfileTransformer includeOriginalValue() {
        return IncludeOriginalValue.INSTANCE;
    }

    final class IncludeOriginalValue implements ProfileTransformer {
        private static final IncludeOriginalValue INSTANCE = new IncludeOriginalValue();
        public static final String PROPERTY_NAME = "OriginalValue";

        @Nullable
        public static String getOriginalValue(@NotNull GameProfile profile) {
            PropertyMap props = profile.getProperties();
            Collection<Property> prop = props.get(PROPERTY_NAME);
            if (prop.isEmpty()) return null;

            Property first = Iterables.getFirst(prop, null);
            return PlayerProfiles.getPropertyValue(first);
        }

        @Override
        public GameProfile transform(Profileable profileable, GameProfile profile) {
            String originalValue = profileable.getProfileValue();
            profile.getProperties().put(PROPERTY_NAME, new Property(PROPERTY_NAME, originalValue));
            return profile;
        }

        @Override
        public boolean canBeCached() {
            return true;
        }
    }

    final class MakeNotStackable implements ProfileTransformer {
        private static final MakeNotStackable INSTANCE = new MakeNotStackable();
        private static final String PROPERTY_NAME = "XSeriesSeed";
        private static final AtomicLong NEXT_ID = new AtomicLong();

        @Override
        public GameProfile transform(Profileable profileable, GameProfile profile) {
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
        public GameProfile transform(Profileable profileable, GameProfile profile) {
            PlayerProfiles.removeTimestamp(profile);
            // It's a multimap, remove all values associated to this key.
            Map<String, Collection<Property>> props = profile.getProperties().asMap();
            props.remove(PlayerProfiles.XSERIES_SIG);
            props.remove(IncludeOriginalValue.PROPERTY_NAME);
            return profile;
        }

        @Override
        public boolean canBeCached() {
            return true;
        }
    }
}
