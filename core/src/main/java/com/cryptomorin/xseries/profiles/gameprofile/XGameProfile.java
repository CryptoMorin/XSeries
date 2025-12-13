package com.cryptomorin.xseries.profiles.gameprofile;

import com.cryptomorin.xseries.reflection.XReflection;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;

import javax.annotation.Nullable;
import java.util.UUID;

public class XGameProfile {
    private XGameProfile() {}

    private static final boolean USE_RECORDS = XReflection.isRecord(GameProfile.class);

    @Nullable
    public static MojangGameProfile of(GameProfile gameProfile) {
        if (gameProfile == null) return null;
        if (USE_RECORDS) return new NewGameProfile(gameProfile);
        else return new OldGameProfile(gameProfile);
    }

    public static MojangGameProfile create(UUID id, String name, PropertyMap properties) {
        if (USE_RECORDS) {
            return of(new GameProfile(id, name, properties));
        } else {
            GameProfile gameProfile = new GameProfile(id, name);
            MojangGameProfile converted = of(gameProfile);
            converted.properties().putAll(properties);
            return converted;
        }
    }

    public static MojangGameProfile create(UUID id, String name) {
        return of(new GameProfile(id, name));
    }
}
