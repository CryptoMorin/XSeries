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

package com.cryptomorin.xseries.profiles.mojang;

import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.UUID;

@ApiStatus.Internal
final class PlayerProfile {
    public final UUID realUUID;
    public final GameProfile requestedGameProfile, fetchedGameProfile;
    public final List<String> profileActions;

    PlayerProfile(UUID realUUID, GameProfile requestedGameProfile,
                  GameProfile fetchedGameProfile, List<String> profileActions) {
        this.realUUID = realUUID;
        this.requestedGameProfile = requestedGameProfile;
        this.fetchedGameProfile = fetchedGameProfile;
        this.profileActions = profileActions;
    }

    boolean exists() {
        return fetchedGameProfile != null;
    }
}
