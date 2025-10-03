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

import com.cryptomorin.xseries.profiles.gameprofile.MojangGameProfile;
import com.cryptomorin.xseries.profiles.objects.Profileable;
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
        private MojangGameProfile profile;
        private boolean expired;
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
            private MojangGameProfile cacheProfile;

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
                        profile
                );
            }
        }
    }

    @Override
    public boolean isReady() {
        return true;
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
    public MojangGameProfile getProfile() {
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
