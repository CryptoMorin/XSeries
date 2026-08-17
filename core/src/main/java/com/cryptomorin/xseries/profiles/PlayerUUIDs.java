/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2026 Crypto Morin
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

package com.cryptomorin.xseries.profiles;

import com.cryptomorin.xseries.profiles.lock.KeyedLock;
import com.cryptomorin.xseries.profiles.lock.MojangRequestQueue;
import com.cryptomorin.xseries.profiles.mojang.MojangAPI;
import com.google.common.base.Strings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public static final Map<UUID, UUID> OFFLINE_TO_ONLINE = new HashMap<>();
    public static final Map<UUID, UUID> ONLINE_TO_OFFLINE = new HashMap<>();
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

    @NotNull
    public static UUID getOfflineUUID(@NotNull String username) {
        // Vanilla behavior across all platforms.
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isOnlineMode() {
        return Bukkit.getOnlineMode();
    }

    /**
     * System property that overrides {@link #usesRealUUIDs()} when set to {@code true} or {@code false}.
     * Useful for setups that the automatic detection cannot figure out (e.g. Spigot behind an
     * offline-mode BungeeCord before any player has joined).
     */
    public static final String PROXY_ONLINE_MODE_PROPERTY = "xseries.profiles.proxyOnlineMode";

    /**
     * Caches the detected mode. Only set once the answer is definitive
     * (the server's mode cannot change without a restart).
     */
    private static volatile Boolean REAL_UUIDS;

    /**
     * Whether player identity on this server is based on real, Mojang-authenticated UUIDs.
     * <p>
     * This is what code should use instead of {@link #isOnlineMode()} when deciding which UUID
     * belongs in the server's own records (e.g. the {@code usercache.json} user cache).
     * {@link Bukkit#getOnlineMode()} is {@code false} on every proxied backend, but backends behind
     * an online-mode BungeeCord/Velocity proxy still receive and store real UUIDs. Treating those
     * servers as offline corrupts their user cache with computed offline UUIDs, which breaks
     * name-based lookups (whitelist, bans, other plugins) for the affected players.
     * <p>
     * Detection order:
     * <ol>
     *     <li>The {@value #PROXY_ONLINE_MODE_PROPERTY} system property, if set.</li>
     *     <li>{@link Bukkit#getOnlineMode()} being {@code true}.</li>
     *     <li>Paper's {@code proxies} config ({@code isProxyOnlineMode()}), which covers both
     *         Velocity and BungeeCord forwarding, on any Paper-based server.</li>
     *     <li>On Spigot behind BungeeCord ({@code settings.bungeecord}), the UUID of a currently
     *         online player: proxies compute offline UUIDs with the same formula as vanilla, so a
     *         player whose UUID doesn't match {@link #getOfflineUUID(String)} proves the proxy
     *         runs in online mode. With no player online the result is assumed offline but not
     *         cached, so it's re-evaluated on the next call.</li>
     * </ol>
     */
    public static boolean usesRealUUIDs() {
        Boolean detected = REAL_UUIDS;
        if (detected != null) return detected;

        String override = System.getProperty(PROXY_ONLINE_MODE_PROPERTY);
        if (override != null) return REAL_UUIDS = Boolean.parseBoolean(override);

        if (Bukkit.getOnlineMode()) return REAL_UUIDS = true;

        Boolean paper = getPaperProxyOnlineMode();
        if (paper != null) return REAL_UUIDS = paper;

        if (isBungeeCordEnabled()) {
            boolean sampled = false;
            try {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    sampled = true;
                    if (player.getUniqueId().equals(getOfflineUUID(player.getName()))) {
                        return REAL_UUIDS = false;
                    }
                }
            } catch (Throwable ignored) {
                // This can be called from the profile fetcher thread; be defensive
                // about iterating the online player list.
            }
            if (sampled) return REAL_UUIDS = true;
            return false; // Nothing to sample yet; don't cache the assumption.
        }

        return REAL_UUIDS = false;
    }

    /**
     * Paper knows the proxy's online mode from its {@code proxies} config section and uses it for
     * its own name-based profile lookups; matching it keeps us consistent with the server.
     *
     * @return null if this isn't a Paper-based server (or its config layout is unknown).
     */
    @Nullable
    private static Boolean getPaperProxyOnlineMode() {
        try {
            // Paper 1.19+
            Class<?> globalConfig = Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
            Object config = globalConfig.getMethod("get").invoke(null);
            Object proxies = globalConfig.getField("proxies").get(config);
            return (Boolean) proxies.getClass().getMethod("isProxyOnlineMode").invoke(proxies);
        } catch (Throwable ignored) {
        }
        try {
            // Paper 1.12-1.18.2
            Class<?> paperConfig = Class.forName("com.destroystokyo.paper.PaperConfig");
            return (Boolean) paperConfig.getMethod("isProxyOnlineMode").invoke(null);
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static boolean isBungeeCordEnabled() {
        try {
            return Class.forName("org.spigotmc.SpigotConfig").getField("bungee").getBoolean(null);
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Nullable
    public static UUID getRealUUIDOfPlayer(@NotNull String username) {
        if (Strings.isNullOrEmpty(username))
            throw new IllegalArgumentException("Username is null or empty: " + username);

        UUID offlineUUID = getOfflineUUID(username);
        UUID realUUID;
        boolean cached;

        try (KeyedLock<String, UUID> lock = MojangRequestQueue.USERNAME_REQUESTS.lock(username, USERNAME_TO_ONLINE::get)) {
            realUUID = lock.getOrRetryValue();
            cached = realUUID != null;
            if (realUUID == null) {
                realUUID = MojangAPI.requestUsernameToUUID(username);

                if (realUUID == null) {
                    ProfileLogger.debug("Caching null for {} ({}) because it doesn't exist.", username, offlineUUID);
                    realUUID = IDENTITY_UUID; // Player not found, we should cache this information.
                } else {
                    ONLINE_TO_OFFLINE.put(realUUID, offlineUUID);
                }

                OFFLINE_TO_ONLINE.put(offlineUUID, realUUID);
                USERNAME_TO_ONLINE.put(username, realUUID);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error while getting real UUID of player: " + username, e);
        }

        if (realUUID == IDENTITY_UUID) {
            ProfileLogger.debug("Providing null UUID for {} because it doesn't exist.", username);
            realUUID = null;
        } else {
            ProfileLogger.debug((cached ? "Cached " : "") + "Real UUID for {} ({}) is {}", username, offlineUUID, realUUID);
        }

        return realUUID;
    }

    /**
     * @return null if a player with this username doesn't exist.
     */
    @Nullable
    public static UUID getRealUUIDOfPlayer(@NotNull String username, @NotNull UUID uuid) {
        Objects.requireNonNull(uuid);
        if (Strings.isNullOrEmpty(username))
            throw new IllegalArgumentException("Username is null or empty: " + username);

        // On servers that deal in real UUIDs (true online mode or behind an online-mode proxy)
        // the given UUID can be trusted directly, unless it's the computed offline UUID
        // (e.g. a caller hardcoded it), in which case we still have to look the real one up.
        if (usesRealUUIDs() && !uuid.equals(getOfflineUUID(username))) return uuid;

        // OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        // if (!player.hasPlayedBefore()) throw new IllegalStateException("Player with UUID " + uuid + " doesn't exist.");

        UUID realUUID;
        boolean cached;

        try (KeyedLock<String, UUID> lock = MojangRequestQueue.USERNAME_REQUESTS.lock(username, () -> OFFLINE_TO_ONLINE.get(uuid))) {
            realUUID = lock.getOrRetryValue();
            cached = realUUID != null;
            if (realUUID == null) {
                realUUID = MojangAPI.requestUsernameToUUID(username);

                if (realUUID == null) {
                    ProfileLogger.debug("Caching null for {} ({}) because it doesn't exist.", username, uuid);
                    realUUID = IDENTITY_UUID; // Player not found, we should cache this information.
                } else {
                    ONLINE_TO_OFFLINE.put(realUUID, uuid);
                }

                OFFLINE_TO_ONLINE.put(uuid, realUUID);
                USERNAME_TO_ONLINE.put(username, realUUID);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error while getting real UUID of player: " + username + " (" + uuid + ')', e);
        }

        if (realUUID == IDENTITY_UUID) {
            ProfileLogger.debug("Providing null UUID for {} ({}) because it doesn't exist.", username, uuid);
            realUUID = null;
        } else {
            ProfileLogger.debug((cached ? "Cached " : "") + "Real UUID for {} ({}) is {}", username, uuid, realUUID);
        }

        UUID offlineUUID = getOfflineUUID(username);
        if (!uuid.equals(offlineUUID) && !uuid.equals(realUUID)) {
            throw new IllegalArgumentException("The provided UUID (" + uuid + ") for '" + username +
                    "' doesn't match the offline UUID (" + offlineUUID + ") or the real UUID (" + realUUID + ')');
        }
        return realUUID;
    }
}
