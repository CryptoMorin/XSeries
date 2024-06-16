package com.cryptomorin.xseries.profiles;

import com.cryptomorin.xseries.profiles.mojang.MojangAPI;
import com.google.common.base.Strings;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiStatus.Internal
public final class PlayerUUIDs {
    /**
     * Used as the default UUID for GameProfiles.
     * Also used as a null-indicating value.
     */
    public static final UUID IDENTITY_UUID = new UUID(0, 0);

    private static final Pattern UUID_NO_DASHES = Pattern.compile(
            "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})"
    );

    /**
     * We can't use Guava's BiMap here since non-existing players are cached too.
     */
    public static final Map<UUID, UUID> OFFLINE_TO_ONLINE = new HashMap<>(), ONLINE_TO_OFFLINE = new HashMap<>();
    public static final Map<String, UUID> USERNAME_TO_ONLINE = new HashMap<>();

    public static UUID UUIDFromDashlessString(String dashlessUUIDString) {
        Matcher matcher = UUID_NO_DASHES.matcher(dashlessUUIDString);
        try {
            return UUID.fromString(matcher.replaceFirst("$1-$2-$3-$4-$5"));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Cannot convert from dashless UUID: " + dashlessUUIDString, ex);
        }
    }

    public static String toUndashedUUID(UUID id) {
        return id.toString().replace("-", "");
    }

    @Nonnull
    public static UUID getOfflineUUID(@Nonnull String username) {
        // Vanilla behavior across all platforms.
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isOnlineMode() {
        return Bukkit.getOnlineMode();
    }

    @Nullable
    public static UUID getRealUUIDOfPlayer(@Nonnull String username) {
        if (Strings.isNullOrEmpty(username))
            throw new IllegalArgumentException("Username is null or empty: " + username);

        UUID offlineUUID = getOfflineUUID(username);
        UUID realUUID = USERNAME_TO_ONLINE.get(username);
        boolean cached = realUUID != null;
        if (realUUID == null) {
            try {
                realUUID = MojangAPI.requestUsernameToUUID(username);
                if (realUUID == null) {
                    ProfilesCore.debug("Caching null for {} ({}) because it doesn't exist.", username, offlineUUID);
                    realUUID = IDENTITY_UUID; // Player not found, we should cache this information.
                } else ONLINE_TO_OFFLINE.put(realUUID, offlineUUID);
                OFFLINE_TO_ONLINE.put(offlineUUID, realUUID);
                USERNAME_TO_ONLINE.put(username, realUUID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (realUUID == IDENTITY_UUID) {
            ProfilesCore.debug("Providing null UUID for {} because it doesn't exist.", username);
            realUUID = null;
        } else {
            ProfilesCore.debug((cached ? "Cached " : "") + "Real UUID for {} ({}) is {}", username, offlineUUID, realUUID);
        }

        return realUUID;
    }

    /**
     * @return null if a player with this username doesn't exist.
     */
    @Nullable
    public static UUID getRealUUIDOfPlayer(@Nonnull String username, @Nonnull UUID uuid) {
        Objects.requireNonNull(uuid);
        if (Strings.isNullOrEmpty(username))
            throw new IllegalArgumentException("Username is null or empty: " + username);

        if (PlayerUUIDs.isOnlineMode()) return uuid;

        // OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        // if (!player.hasPlayedBefore()) throw new RuntimeException("Player with UUID " + uuid + " doesn't exist.");

        UUID realUUID = OFFLINE_TO_ONLINE.get(uuid);
        boolean cached = realUUID != null;
        if (realUUID == null) {
            try {
                realUUID = MojangAPI.requestUsernameToUUID(username);
                if (realUUID == null) {
                    ProfilesCore.debug("Caching null for {} ({}) because it doesn't exist.", username, uuid);
                    realUUID = IDENTITY_UUID; // Player not found, we should cache this information.
                } else ONLINE_TO_OFFLINE.put(realUUID, uuid);
                OFFLINE_TO_ONLINE.put(uuid, realUUID);
                USERNAME_TO_ONLINE.put(username, realUUID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (realUUID == IDENTITY_UUID) {
            ProfilesCore.debug("Providing null UUID for {} ({}) because it doesn't exist.", username, uuid);
            realUUID = null;
        } else {
            ProfilesCore.debug((cached ? "Cached " : "") + "Real UUID for {} ({}) is {}", username, uuid, realUUID);
        }

        UUID offlineUUID = getOfflineUUID(username);
        if (!uuid.equals(offlineUUID) && !uuid.equals(realUUID)) {
            throw new RuntimeException("The provided UUID (" + uuid + ") for '" + username +
                    "' doesn't match the offline UUID (" + offlineUUID + ") or the real UUID (" + realUUID + ')');
        }
        return realUUID;
    }
}
