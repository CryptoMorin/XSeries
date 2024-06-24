package com.cryptomorin.xseries.profiles.objects;

import com.cryptomorin.xseries.profiles.PlayerProfiles;
import com.mojang.authlib.GameProfile;

import java.util.List;

public final class TransformableProfile implements Profileable {
    private final Profileable profileable;
    private final List<ProfileTransformer> transformers;

    public TransformableProfile(Profileable profileable, List<ProfileTransformer> transformers) {
        this.profileable = profileable;
        this.transformers = transformers;
    }

    @Override
    public GameProfile getProfile() {
        GameProfile profile = PlayerProfiles.clone(profileable.getProfile());
        for (ProfileTransformer transformer : transformers) {
            profile = transformer.transform(profile);
        }
        return profile;
    }
}
