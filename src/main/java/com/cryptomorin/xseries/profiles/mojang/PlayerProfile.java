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
