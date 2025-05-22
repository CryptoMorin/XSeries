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

import com.cryptomorin.xseries.profiles.exceptions.ProfileException;
import com.cryptomorin.xseries.profiles.objects.transformer.ProfileTransformer;
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Ideally this should've been an abstract class with finalized methods,
 * but we usually need another abstract class and Java can't have multiple ones,
 * and also it'd have been better if {@link #getDelegateProfile()} was {@code protected}.
 */
@ApiStatus.Internal
public interface DelegateProfileable extends Profileable {
    /**
     * The profileable object which handles all the operations.
     * It doesn't necessarily have to be a constant object, it can
     * change dynamically.
     */
    @ApiStatus.Internal
    @NotNull
    Profileable getDelegateProfile();

    @Override
    @Nullable
    @Unmodifiable
    default GameProfile getProfile() {
        return getDelegateProfile().getProfile();
    }

    @Override
    default @Nullable ProfileException test() {
        return getDelegateProfile().test();
    }

    @Override
    default @Nullable GameProfile getDisposableProfile() {
        return getDelegateProfile().getDisposableProfile();
    }

    @Override
    default boolean isComplete() {return getDelegateProfile().isComplete();}

    @Override
    @NotNull
    default Profileable transform(@NotNull ProfileTransformer... transformers) {
        return getDelegateProfile().transform(transformers);
    }

    @Override
    @Nullable
    default String getProfileValue() {
        return getDelegateProfile().getProfileValue();
    }
}
