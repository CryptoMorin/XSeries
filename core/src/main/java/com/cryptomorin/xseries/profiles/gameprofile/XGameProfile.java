package com.cryptomorin.xseries.profiles.gameprofile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class XGameProfile {
    private XGameProfile() {}

    private static final boolean USE_RECORDS;

    static {
        boolean useRecords;

        try {
            Method isRecord = Class.class.getDeclaredMethod("isRecord");
            useRecords = (boolean) isRecord.invoke(GameProfile.class);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            useRecords = false;
        }

        USE_RECORDS = useRecords;
    }

    @Nullable
    public static MojangGameProfile of(GameProfile gameProfile) {
        if (gameProfile == null) return null;
        if (USE_RECORDS) return new NewGameProfile(gameProfile);
        else return new OldGameProfile(gameProfile);
    }

    public static MojangGameProfile create(UUID id, String name, PropertyMap properties) {
        return of(new GameProfile(id, name, properties));
    }

    public static MojangGameProfile create(UUID id, String name) {
        return of(new GameProfile(id, name));
    }
}
