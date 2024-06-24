package com.cryptomorin.xseries.profiles.objects;

import com.cryptomorin.xseries.profiles.PlayerProfiles;
import com.cryptomorin.xseries.profiles.PlayerUUIDs;
import com.cryptomorin.xseries.profiles.exceptions.InvalidProfileException;
import com.cryptomorin.xseries.profiles.exceptions.UnknownPlayerException;
import com.cryptomorin.xseries.profiles.mojang.MojangAPI;
import com.cryptomorin.xseries.profiles.mojang.PlayerProfileFetcherThread;
import com.cryptomorin.xseries.profiles.mojang.ProfileRequestConfiguration;
import com.cryptomorin.xseries.profiles.objects.cache.TimedCacheableProfileable;
import com.cryptomorin.xseries.profiles.objects.transformer.ProfileTransformer;
import com.cryptomorin.xseries.profiles.objects.transformer.TransformableProfile;
import com.cryptomorin.xseries.reflection.XReflection;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Represents any object that has a {@link GameProfile} or one can be created with it.
 * These objects are cached.
 * <p>
 * A {@link GameProfile} is an object that represents information about a Minecraft player's
 * account in general (not specific to this or any other server)
 * The most important information contained within this profile however, is the
 * skin texture URL which the client needs to properly see the texture on items/blocks.
 */
public interface Profileable {
    /**
     * This method should not be used directly unless you know what you're doing.
     * <p>
     * The texture which might be cached. If any errors occur, the check may be re-evaluated.
     * The cached values might also be re-evaluated due to expiration.
     * @throws com.cryptomorin.xseries.profiles.exceptions.ProfileException may also throw other internal exceptions (most likely bugs)
     * @return the original profile (not cloned if possible) for an instance that's always guaranteed to be a copy
     *         you can use {@link #getDisposableProfile()} instead.
     */
    @NotNull
    @Unmodifiable
    @ApiStatus.Internal
    GameProfile getProfile();

    /**
     * Same as {@link #getProfile()}, except some implementations of {@link Profileable}
     * cannot inherently return any original instance as they're not cacheable, so this
     * method ensures that no duplicate cloning of {@link GameProfile} occurs for performance.
     * <p>
     * For most implementations however, this defaults to a simple cloning of the cached instances.
     * @return always a copied version of {@link #getProfile()} that you can change.
     */
    @NotNull
    @ApiStatus.Internal
    default GameProfile getDisposableProfile() {
        return PlayerProfiles.clone(getProfile());
    }

    /**
     * Adds transformer (read {@link ProfileTransformer}) information to a copied version
     * of this profile (so it doesn't affect this instance).
     * <p>
     * Profiles are copied before being transformed, so the main cache remains intact
     * but the result of transformed profiles are never cached.
     * @param transformers a list of transformers to apply in order once {@link #getProfile()} is called.
     */
    default Profileable transform(ProfileTransformer... transformers) {
        return new TransformableProfile(this, Arrays.asList(transformers));
    }

    /**
     * A string representation of the {@link #getProfile()} which is useful for data storage.
     */
    @Nullable
    default String getProfileValue() {
        GameProfile profile = getProfile();
        return PlayerProfiles.getTextureProperty(profile).map(PlayerProfiles::getPropertyValue).orElse(null);
    }

    @Nonnull
    @ApiStatus.Experimental
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
                    username = ((PlayerProfileable) profileable).username;
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
     * Sets the skull texture based on the specified offline player.
     * The profile lookup will depend on whether the server is running in online mode.
     *
     * @param offlinePlayer The offline player to generate the {@link GameProfile}.
     */
    static Profileable of(OfflinePlayer offlinePlayer) {
        return new PlayerProfileable(offlinePlayer);
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

    final class UsernameProfileable extends TimedCacheableProfileable {
        private final String username;
        private Boolean valid;

        public UsernameProfileable(String username) {this.username = Objects.requireNonNull(username);}

        @Override
        protected GameProfile getProfile0() {
            if (valid == null) {
                valid = ProfileInputType.USERNAME.pattern.matcher(username).matches();
            }
            if (!valid) throw new InvalidProfileException("Invalid username: '" + username + '\'');

            Optional<GameProfile> profileOpt = MojangAPI.getMojangCachedProfileFromUsername(username);
            if (!profileOpt.isPresent())
                throw new UnknownPlayerException("Cannot find player named '" + username + '\'');

            GameProfile profile = profileOpt.get();
            if (PlayerProfiles.hasTextures(profile)) return profile;
            return MojangAPI.getOrFetchProfile(profile);
        }
    }

    final class UUIDProfileable extends TimedCacheableProfileable {
        private final UUID id;

        public UUIDProfileable(UUID id) {this.id = Objects.requireNonNull(id, "UUID cannot be null");}

        @Override
        protected GameProfile getProfile0() {
            GameProfile profile = MojangAPI.getCachedProfileByUUID(id);
            if (PlayerProfiles.hasTextures(profile)) return profile;
            return MojangAPI.getOrFetchProfile(profile);
        }
    }

    final class GameProfileProfileable extends TimedCacheableProfileable {
        private final GameProfile profile;

        public GameProfileProfileable(GameProfile profile) {this.profile = Objects.requireNonNull(profile);}

        @Override
        protected GameProfile getProfile0() {
            if (PlayerProfiles.hasTextures(profile)) {
                return profile;
            }

            return (PlayerUUIDs.isOnlineMode()
                    ? new UUIDProfileable(profile.getId())
                    : new UsernameProfileable(profile.getName())
            ).getProfile();
        }
    }

    /**
     * Do we need to support {@link org.bukkit.profile.PlayerProfile} for hybrid server software
     * like Geyser or Mohist that might have their own caching system?
     */
    final class PlayerProfileable extends TimedCacheableProfileable {
        // Let the GC do its job.
        @Nullable private final String username;
        @Nonnull private final UUID id;

        public PlayerProfileable(OfflinePlayer player) {
            Objects.requireNonNull(player);
            this.username = player.getName();
            this.id = player.getUniqueId();
        }

        @Override
        protected GameProfile getProfile0() {
            // Can be empty if used by:
            // CraftServer -> public OfflinePlayer getOfflinePlayer(UUID id)
            if (Strings.isNullOrEmpty(username)) {
                return new UUIDProfileable(id).getProfile();
            } else {
                return new UsernameProfileable(username).getProfile();
            }
        }
    }

    final class StringProfileable extends TimedCacheableProfileable {
        private final String string;
        @Nullable private ProfileInputType type;

        public StringProfileable(String string, @Nullable ProfileInputType type) {
            this.string = Objects.requireNonNull(string);
            this.type = type;
        }

        private StringProfileable determineType() {
            if (type == null) type = ProfileInputType.typeOf(string);
            return this;
        }

        @Override
        protected GameProfile getProfile0() {
            determineType();
            if (type == null) {
                throw new InvalidProfileException("Unknown skull string value: " + string);
            }
            return type.getProfile(string);
        }
    }
}