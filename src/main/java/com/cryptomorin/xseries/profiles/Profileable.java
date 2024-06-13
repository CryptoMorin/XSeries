package com.cryptomorin.xseries.profiles;

import com.cryptomorin.xseries.profiles.exceptions.InvalidProfileException;
import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public interface Profileable {
    /**
     * The final texture that will be supplied to {@code profileContainer} to be applied.
     */
    GameProfile getProfile();

    @Nullable
    default String getProfileValue() {
        GameProfile profile = getProfile();
        if (profile == null) return null;
        return PlayerProfiles.getTextureProperty(profile).map(PlayerProfiles::getPropertyValue).orElse(null);
    }

    /**
     * Sets the skull texture based on the specified player UUID (whether it's an offline or online UUID).
     */
    static Profileable username(String username) {
        return new UsernameProfileable(username);
    }

    /**
     * Sets the skull texture based on the specified player UUID (whether it's an offline or online UUID).
     */
    static Profileable of(UUID uuid) {
        return new UUIDProfileable(uuid);
    }

    /**
     * Sets the skull texture based on the specified profile.
     * If the profile already has textures, it will be used directly. Otherwise, a new profile will be fetched
     * based on the UUID or username depending on the server's online mode.
     *
     * @param profile The profile to be used in the profile setting operation.
     */
    static Profileable of(GameProfile profile) {
        return new GameProfileProfileable(profile);
    }

    /**
     * Sets the skull texture based on the specified player.
     *
     * @param player The player to generate the {@link GameProfile}.
     */
    static Profileable of(Player player) {
        return new PlayerProfileable(player);
    }

    /**
     * Sets the skull texture based on the specified offline player.
     * The profile lookup will depend on whether the server is running in online mode.
     *
     * @param offlinePlayer The offline player to generate the {@link GameProfile}.
     */
    static Profileable of(OfflinePlayer offlinePlayer) {
        return new OfflinePlayerProfileable(offlinePlayer);
    }

    /**
     * Sets the skull texture based on a string. The input type is resolved based on the value provided.
     *
     * <h2>Valid Types</h2>
     * <b>Username:</b> A player username. (e.g. Notch)<br>
     * <b>UUID:</b> A player UUID. Offline or online mode UUID. (e.g. 069a79f4-44e9-4726-a5be-fca90e38aaf5)<br>
     * <b>Base64:</b> The Base64 encoded value of textures JSON. (e.g. eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2NmNjc2N2RkMzQ3MzdlOTliZDU0YjY5NWVmMDY4M2M2YzZjZTZhNTRmNjZhZDk3Mjk5MmJkMGU0OGU0NTc5YiJ9fX0=)<br>
     * <b>Minecraft Textures URL:</b> Check {@link PlayerTextureInputType#TEXTURE_URL}.<br>
     * <b>Minecraft Textures Hash:</b> Same as the URL, but only including the hash part, excluding the base URL. (e.g. e5461a215b325fbdf892db67b7bfb60ad2bf1580dc968a15dfb304ccd5e74db)
     *
     * @param input The input value used to retrieve the {@link GameProfile}. For more information check {@link PlayerTextureInputType}
     */
    static Profileable detect(String input) {
        Objects.requireNonNull(input);
        return new StringProfileable(input, null);
    }

    /**
     * Sets the skull texture based on a string with a known type.
     *
     * @param type  The type of the input value.
     * @param input The input value to generate the {@link GameProfile}.
     */
    static Profileable of(PlayerTextureInputType type, String input) {
        Objects.requireNonNull(type, () -> "Cannot profile from a null input type: " + input);
        Objects.requireNonNull(input, () -> "Cannot profile from a null input: " + type);
        return new StringProfileable(input, type);
    }


    final class UsernameProfileable implements Profileable {
        private final String username;

        public UsernameProfileable(String username) {this.username = username;}


        @Override
        public GameProfile getProfile() {
            return null;
        }
    }

    final class UUIDProfileable implements Profileable {
        private final UUID id;

        public UUIDProfileable(UUID id) {this.id = Objects.requireNonNull(id, "UUID cannot be null");}

        @Override
        public GameProfile getProfile() {
            GameProfile profile = MojangAPI.getCachedProfileByUUID(id);
            if (PlayerProfiles.hasTextures(profile)) return profile;
            return MojangAPI.fetchProfile(profile);
        }
    }

    final class GameProfileProfileable implements Profileable {
        private final GameProfile profile;

        public GameProfileProfileable(GameProfile profile) {this.profile = profile;}

        @Override
        public GameProfile getProfile() {
            if (PlayerProfiles.hasTextures(profile)) {
                return profile;
            }

            return (PlayerUUIDs.isOnlineMode()
                    ? new UUIDProfileable(profile.getId())
                    : new UsernameProfileable(profile.getName()))
                    .getProfile();
        }
    }

    final class PlayerProfileable implements Profileable {
        private final Player player;

        public PlayerProfileable(Player player) {this.player = player;}

        @Override
        public GameProfile getProfile() {
            // Why are we using the username instead of getting the cached UUID like profile(player.getUniqueId())?
            // If it's about online/offline mode support why should we have a separate method for this instead of
            // letting profile(OfflinePlayer) to take care of it?
            if (PlayerUUIDs.isOnlineMode()) return new UUIDProfileable(player.getUniqueId()).getProfile();
            return new UsernameProfileable(player.getName()).getProfile();
        }
    }

    final class OfflinePlayerProfileable implements Profileable {
        private final OfflinePlayer player;

        public OfflinePlayerProfileable(OfflinePlayer player) {this.player = player;}

        @Override
        public GameProfile getProfile() {
            String name = player.getName();
            if (Strings.isNullOrEmpty(name)) {
                return new UUIDProfileable(player.getUniqueId()).getProfile();
            } else {
                return new UsernameProfileable(player.getName()).getProfile();
            }
        }
    }

    final class StringProfileable implements Profileable {
        private final String string;
        @Nullable private PlayerTextureInputType type;

        public StringProfileable(String string, @Nullable PlayerTextureInputType type) {
            this.string = string;
            this.type = type;
        }

        @Override
        public GameProfile getProfile() {
            if (type == null) type = PlayerTextureInputType.get(string);
            if (type == null) {
                throw new InvalidProfileException("Unknown skull string value: " + string);
            }
            return type.getProfile(string);
        }
    }
}