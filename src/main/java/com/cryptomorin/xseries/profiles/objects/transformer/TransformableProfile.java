package com.cryptomorin.xseries.profiles.objects.transformer;

import com.cryptomorin.xseries.profiles.PlayerProfiles;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The default implementation used for {@link Profileable#transform(ProfileTransformer...)}
 *
 * @see ProfileTransformer
 */
public final class TransformableProfile implements Profileable {
    /**
     * The original profileable.
     */
    private final Profileable profileable;
    private final TransformationSequence transformers;

    @ApiStatus.Internal
    public TransformableProfile(Profileable profileable, List<ProfileTransformer> transformers) {
        this.profileable = profileable;
        this.transformers = new TransformationSequence(profileable, transformers);
    }

    private static final class TransformationSequence {
        private final Profileable profileable;
        @Nullable
        private GameProfile profile;
        private boolean expired, markRestAsCopy;
        private final TransformedProfileCache[] transformers;

        private TransformationSequence(Profileable profileable, List<ProfileTransformer> transformers) {
            this.profileable = profileable;
            this.transformers = transformers.stream()
                    .map(TransformedProfileCache::new)
                    .toArray(TransformedProfileCache[]::new);
        }

        private final class TransformedProfileCache {
            @Nullable
            private final ProfileTransformer transformer;
            @Nullable
            private GameProfile cacheProfile;

            private TransformedProfileCache(@Nullable ProfileTransformer transformer) {this.transformer = transformer;}

            private void transform() {
                if (cacheProfile != null && transformer.canBeCached()) {
                    if (!expired) {
                        profile = cacheProfile;
                        return;
                    }
                } else {
                    expired = true;
                }
                profile = cacheProfile = transformer.transform(
                        profileable,
                        markRestAsCopy ? profile : PlayerProfiles.clone(profile));
                if (!transformer.canBeCached()) markRestAsCopy = true;
            }
        }
    }

    @Override
    public Profileable transform(ProfileTransformer... transformers) {
        // Return a new instance because we promised not to affect the current instance for transform() method.
        List<ProfileTransformer> transformersList = new ArrayList<>(this.transformers.transformers.length + transformers.length);
        transformersList.addAll(Arrays.stream(this.transformers.transformers).map(x -> x.transformer).collect(Collectors.toList()));
        transformersList.addAll(Arrays.asList(transformers));
        return new TransformableProfile(profileable, transformersList);
    }

    @Override
    public GameProfile getProfile() {
        // This method doesn't need to be synchronized for the cache (see CacheableProfileable#getProfile), since the
        // transformation sequences don't send any API requests. The cost of synchronizing
        // this method would be probably more than letting the transformation happen again.
        transformers.profile = profileable.getProfile();
        if (transformers.profile == null) return null;

        for (TransformationSequence.TransformedProfileCache transformer : transformers.transformers) {
            transformer.transform();
        }

        return transformers.profile;
    }
}
