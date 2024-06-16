package com.cryptomorin.xseries.profiles.objects;

import com.cryptomorin.xseries.profiles.PlayerProfiles;
import com.cryptomorin.xseries.profiles.PlayerUUIDs;
import com.cryptomorin.xseries.profiles.exceptions.InvalidProfileException;
import com.cryptomorin.xseries.profiles.exceptions.UnknownPlayerException;
import com.cryptomorin.xseries.profiles.mojang.MojangAPI;
import com.cryptomorin.xseries.profiles.mojang.PlayerProfileFetcherThread;
import com.cryptomorin.xseries.profiles.mojang.ProfileRequestConfiguration;
import com.cryptomorin.xseries.reflection.XReflection;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Represents any object that has a {@link GameProfile} or one can be created with it.
 * These objects are cached.
 */
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

    abstract class AbstractProfileable implements Profileable {
        protected GameProfile cache;

        @Override
        public final GameProfile getProfile() {
            GameProfile profile = cache != null ? cache : (cache = getProfile0());
            return PlayerProfiles.clone(profile);
        }

        abstract GameProfile getProfile0();

        @Override
        public final String toString() {
            return this.getClass().getSimpleName() + "[cache=" + cache + ']';
        }
    }

    @Nonnull
    static <C extends Collection<Profileable>> CompletableFuture<C> prepare(@Nonnull C profileables) {
        return prepare(profileables, null, null);
    }

    @Nonnull
    @ApiStatus.Experimental
    static <C extends Collection<Profileable>> CompletableFuture<C> prepare(
            @Nonnull C profileables, @Nullable ProfileRequestConfiguration config,
            @Nullable Function<Throwable, Boolean> errorHandler) {
        CompletableFuture<Map<UUID, String>> initial = CompletableFuture.completedFuture(new HashMap<>());
        List<String> usernameRequests = new ArrayList<>();

        if (!PlayerUUIDs.isOnlineMode()) {
            for (Profileable profileable : profileables) {
                String username = null;
                if (profileable instanceof UsernameProfileable) {
                    username = ((UsernameProfileable) profileable).username;
                } else if (profileable instanceof PlayerProfileable) {
                    username = ((PlayerProfileable) profileable).player.getName();
                } else if (profileable instanceof OfflinePlayerProfileable) {
                    username = ((OfflinePlayerProfileable) profileable).player.getName();
                } else if (profileable instanceof StringProfileable) {
                    if (((StringProfileable) profileable).determineType().type == ProfileInputType.USERNAME) {
                        username = ((StringProfileable) profileable).string;
                    }
                }

                if (username != null) {
                    usernameRequests.add(username);
                }
            }

            if (!usernameRequests.isEmpty())
                initial = CompletableFuture.supplyAsync(
                        () -> MojangAPI.usernamesToUUIDs(usernameRequests, config), PlayerProfileFetcherThread.EXECUTOR);
        }

        // First cache the username requests then get the profiles and finally return the original objects.
        return XReflection.stacktrace(initial
                .thenCompose(a -> {
                    List<CompletableFuture<GameProfile>> requests = new ArrayList<>(profileables.size());

                    for (Profileable profileable : profileables) {
                        CompletableFuture<GameProfile> async = CompletableFuture
                                .supplyAsync(profileable::getProfile, PlayerProfileFetcherThread.EXECUTOR);

                        if (errorHandler != null) {
                            async = XReflection.stacktrace(async).exceptionally(ex -> {
                                boolean rethrow = errorHandler.apply(ex);
                                if (rethrow) throw XReflection.throwCheckedException(ex);
                                else return null;
                            });
                        }

                        requests.add(async);
                    }

                    return CompletableFuture.allOf(requests.toArray(new CompletableFuture[0]));
                })
                .thenApply((a) -> profileables));
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
     * <b>Minecraft Textures URL:</b> Check {@link ProfileInputType#TEXTURE_URL}.<br>
     * <b>Minecraft Textures Hash:</b> Same as the URL, but only including the hash part, excluding the base URL. (e.g. e5461a215b325fbdf892db67b7bfb60ad2bf1580dc968a15dfb304ccd5e74db)
     *
     * @param input The input value used to retrieve the {@link GameProfile}. For more information check {@link ProfileInputType}
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
    static Profileable of(ProfileInputType type, String input) {
        Objects.requireNonNull(type, () -> "Cannot profile from a null input type: " + input);
        Objects.requireNonNull(input, () -> "Cannot profile from a null input: " + type);
        return new StringProfileable(input, type);
    }


    final class UsernameProfileable extends AbstractProfileable {
        private final String username;
        private Boolean valid;

        public UsernameProfileable(String username) {this.username = Objects.requireNonNull(username);}

        @Override
        public GameProfile getProfile0() {
            if (valid == null) {
                valid = ProfileInputType.USERNAME.pattern.matcher(username).matches();
            }
            if (!valid) throw new InvalidProfileException("Invalid username: '" + username + '\'');

            Optional<GameProfile> profileOpt = MojangAPI.profileFromUsername(username);
            if (!profileOpt.isPresent())
                throw new UnknownPlayerException("Cannot find player named '" + username + '\'');

            GameProfile profile = profileOpt.get();
            if (PlayerProfiles.hasTextures(profile)) return profile;
            return MojangAPI.fetchProfile(profile);
        }
    }

    final class UUIDProfileable extends AbstractProfileable {
        private final UUID id;

        public UUIDProfileable(UUID id) {this.id = Objects.requireNonNull(id, "UUID cannot be null");}

        @Override
        public GameProfile getProfile0() {
            GameProfile profile = MojangAPI.getCachedProfileByUUID(id);
            if (PlayerProfiles.hasTextures(profile)) return profile;
            return MojangAPI.fetchProfile(profile);
        }
    }

    final class GameProfileProfileable extends AbstractProfileable {
        private final GameProfile profile;

        public GameProfileProfileable(GameProfile profile) {this.profile = Objects.requireNonNull(profile);}

        @Override
        public GameProfile getProfile0() {
            if (PlayerProfiles.hasTextures(profile)) {
                return profile;
            }

            return (PlayerUUIDs.isOnlineMode()
                    ? new UUIDProfileable(profile.getId())
                    : new UsernameProfileable(profile.getName()))
                    .getProfile();
        }
    }

    final class PlayerProfileable extends AbstractProfileable {
        private final Player player;

        public PlayerProfileable(Player player) {this.player = Objects.requireNonNull(player);}

        @Override
        public GameProfile getProfile0() {
            // Why are we using the username instead of getting the cached UUID like profile(player.getUniqueId())?
            // If it's about online/offline mode support why should we have a separate method for this instead of
            // letting profile(OfflinePlayer) to take care of it?
            if (PlayerUUIDs.isOnlineMode()) return new UUIDProfileable(player.getUniqueId()).getProfile();
            return new UsernameProfileable(player.getName()).getProfile();
        }
    }

    final class OfflinePlayerProfileable extends AbstractProfileable {
        private final OfflinePlayer player;

        public OfflinePlayerProfileable(OfflinePlayer player) {this.player = Objects.requireNonNull(player);}

        @Override
        public GameProfile getProfile0() {
            String name = player.getName();
            if (Strings.isNullOrEmpty(name)) {
                return new UUIDProfileable(player.getUniqueId()).getProfile();
            } else {
                return new UsernameProfileable(player.getName()).getProfile();
            }
        }
    }

    final class StringProfileable extends AbstractProfileable {
        private final String string;
        @Nullable private ProfileInputType type;

        public StringProfileable(String string, @Nullable ProfileInputType type) {
            this.string = Objects.requireNonNull(string);
            this.type = type;
        }

        private StringProfileable determineType() {
            if (type == null) type = ProfileInputType.get(string);
            return this;
        }

        @Override
        public GameProfile getProfile0() {
            determineType();
            if (type == null) {
                throw new InvalidProfileException("Unknown skull string value: " + string);
            }
            return type.getProfile(string);
        }
    }
}