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

package com.cryptomorin.xseries.profiles.objects;

import com.cryptomorin.xseries.profiles.PlayerProfiles;
import com.cryptomorin.xseries.profiles.PlayerUUIDs;
import com.cryptomorin.xseries.profiles.builder.ProfileInstruction;
import com.cryptomorin.xseries.profiles.exceptions.InvalidProfileException;
import com.cryptomorin.xseries.profiles.exceptions.ProfileException;
import com.cryptomorin.xseries.profiles.exceptions.UnknownPlayerException;
import com.cryptomorin.xseries.profiles.mojang.MojangAPI;
import com.cryptomorin.xseries.profiles.mojang.PlayerProfileFetcherThread;
import com.cryptomorin.xseries.profiles.mojang.ProfileRequestConfiguration;
import com.cryptomorin.xseries.profiles.objects.cache.TimedCacheableProfileable;
import com.cryptomorin.xseries.profiles.objects.transformer.ProfileTransformer;
import com.cryptomorin.xseries.profiles.objects.transformer.TransformableProfile;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.*;

import java.lang.invoke.MethodHandle;
import java.time.Duration;
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
 *
 * @see com.cryptomorin.xseries.profiles.objects.cache.CacheableProfileable
 * @see TimedCacheableProfileable
 * @see TransformableProfile
 */
public interface Profileable {
    /**
     * This method should not be used directly unless you know what you're doing.
     * <p>
     * The texture which might be cached. If any errors occur, the check may be re-evaluated.
     * The cached values might also be re-evaluated due to expiration.
     *
     * @return the original profile (not cloned if possible) for an instance that's always guaranteed to be a copy
     * you can use {@link #getDisposableProfile()} instead. Null if no profile is set (only happens for {@link ProfileContainer}).
     * @throws com.cryptomorin.xseries.profiles.exceptions.ProfileException may also throw other internal exceptions (most likely bugs)
     */
    @Nullable
    @Unmodifiable
    @ApiStatus.Internal
    GameProfile getProfile();

    /**
     * Whether this profile has all the necessary information to construct its {@link #getProfile()} right away.
     * When this method returns false, it means some kind of request has to be sent to Mojang servers
     * in order to retrieve some information.
     * <p>
     * Even if this method returns true, it doesn't necessarily mean that this profile has all information (textures, username, UUID, etc.)
     * it merely means that it has all the information it needs in memory to compute its {@link #getProfile()}
     * and doesn't need to a request any data using {@link com.cryptomorin.xseries.profiles.mojang.MinecraftClient MinecraftClient}.
     *
     * @see #prepare(Collection)
     */
    @Contract(pure = true)
    boolean isReady();

    /**
     * Tests whether this profile has any issues or throws any exception.
     * This is a good way if you're going to be checking for user issues
     * before trying to {@link ProfileInstruction#apply()} them.
     * For now, this is basically equivalent to calling {@link #getProfile()}
     * and catching exceptions. Which means that the profile will be cached
     * if a cache is behind this profile.
     * <p>
     * These issues include, wrong username, network issues, etc.
     * Note that some issues only occur when {@link ProfileInstruction#apply()} is used.
     * Any other type of exception that might happen are still ignored.
     */
    @Nullable
    @Contract("-> new")
    default ProfileException test() {
        try {
            getProfile();
            return null;
        } catch (ProfileException ex) {
            return ex;
        }
    }

    /**
     * Same as {@link #getProfile()}, except some implementations of {@link Profileable}
     * cannot inherently return any original instance as they're not cacheable, so this
     * method ensures that no duplicate cloning of {@link GameProfile} occurs for performance.
     * <p>
     * For most implementations however, this defaults to a simple cloning of the cached instances.
     *
     * @return always a copied version of {@link #getProfile()} that you can change. Null if {@link #getProfile()} is null
     */
    @Nullable
    @ApiStatus.Internal
    @Contract("-> new")
    default GameProfile getDisposableProfile() {
        GameProfile profile = getProfile();
        return profile == null ? null : PlayerProfiles.clone(profile);
    }

    /**
     * Adds transformer (read {@link ProfileTransformer}) information to a copied version
     * of this profile (so it doesn't affect this instance, but the class type will change).
     * So it's recommended to not chain this method and instead collect all transformers
     * and call this method once for performance when necessary.
     * <p>
     * Profiles are copied before being transformed, so the main cache remains intact
     * but the result of transformed profiles are never cached.
     *
     * @param transformers a list of transformers to apply in order once {@link #getProfile()} is called.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    default Profileable transform(@NotNull ProfileTransformer... transformers) {
        return new TransformableProfile(this, Arrays.asList(transformers));
    }

    /**
     * A string representation of the {@link #getProfile()} which is useful for data storage which
     * can be serialized back using {@link Profileable#detect(String)}.
     * <p>
     * Note that in some cases this is the Base64 value of the textures property, but some may
     * provide less verbose data when possible. Items and blocks can also provide compact
     * data if their profile was built using {@link ProfileTransformer#includeOriginalValue()}.
     *
     * @return null if {@link #getProfile()} is null or the set profile doesn't have a texture property.
     */
    @Nullable
    default String getProfileValue() {
        return PlayerProfiles.getOriginalValue(getProfile());
    }

    @NotNull
    @ApiStatus.Experimental
    static <C extends Collection<Profileable>> CompletableFuture<C> prepare(@NotNull C profileables) {
        return prepare(profileables, null, null);
    }

    @NotNull
    @ApiStatus.Experimental
    static <C extends Collection<Profileable>> CompletableFuture<C> prepare(
            @NotNull C profileables, @Nullable ProfileRequestConfiguration config,
            @Nullable Function<Throwable, Boolean> errorHandler) {
        Objects.requireNonNull(profileables, "Profile list is null");

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
    @NotNull
    @Contract(pure = true)
    static Profileable username(@NotNull String username) {
        return new UsernameProfileable(username);
    }

    /**
     * Sets the skull texture based on the specified player UUID (whether it's an offline or online UUID).
     */
    @NotNull
    @Contract(pure = true)
    static Profileable of(@NotNull UUID uuid) {
        return new UUIDProfileable(uuid);
    }

    /**
     * Sets the skull texture based on the specified profile.
     * If the profile already has textures, it will be used directly. Otherwise, a new profile will be fetched
     * based on the UUID or username depending on the server's online mode.
     *
     * @param profile               The profile to be used in the profile setting operation.
     * @param fetchTexturesIfNeeded whether textures should be automatically fetched for this profile
     *                              if they don't exist. This should almost always be set to true unless
     *                              you want some other code to handle the texture (whether server-side or client-side.)
     */
    @NotNull
    @Contract(pure = true)
    static Profileable of(@NotNull GameProfile profile, boolean fetchTexturesIfNeeded) {
        return fetchTexturesIfNeeded ?
                new RawGameProfileProfileable(profile) :
                new DynamicGameProfileProfileable(profile);
    }

    /**
     * Sets the skull texture based on the specified offline player.
     * The profile lookup will depend on whether the server is running in online mode or not
     * that's why this method accepts an {@link OfflinePlayer} not a {@link org.bukkit.entity.Player}.
     *
     * @param offlinePlayer The offline player to generate the {@link GameProfile}.
     */
    @NotNull
    @Contract(pure = true)
    static Profileable of(@NotNull OfflinePlayer offlinePlayer) {
        return new PlayerProfileable(offlinePlayer);
    }

    /**
     * Gets the skull texture from a block that is {@link Skull}.
     *
     * @see #of(Block)
     */
    @NotNull
    @Contract(pure = true)
    static Profileable of(@NotNull BlockState blockState) {
        return new ProfileContainer.BlockStateProfileContainer((Skull) blockState);
    }

    /**
     * Gets the skull texture from a block that is {@link Skull}.
     *
     * @see #of(BlockState)
     */
    @NotNull
    @Contract(pure = true)
    static Profileable of(@NotNull Block block) {
        return new ProfileContainer.BlockProfileContainer(block);
    }

    /**
     * Gets the skull texture from an item which is a {@link com.cryptomorin.xseries.XMaterial#PLAYER_HEAD}.
     *
     * @see #of(ItemMeta)
     */
    @NotNull
    @Contract(pure = true)
    static Profileable of(@NotNull ItemStack item) {
        return new ProfileContainer.ItemStackProfileContainer(item);
    }

    /**
     * Gets the skull texture from an item which is a {@link com.cryptomorin.xseries.XMaterial#PLAYER_HEAD}.
     *
     * @see #of(ItemStack)
     */
    @NotNull
    @Contract(pure = true)
    static Profileable of(@NotNull ItemMeta meta) {
        return new ProfileContainer.ItemMetaProfileContainer((SkullMeta) meta);
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
    @NotNull
    @Contract(pure = true)
    static Profileable detect(@NotNull String input) {
        return new StringProfileable(input, null);
    }

    /**
     * Sets the skull texture based on a string with a known type.
     *
     * @param type  The type of the input value.
     * @param input The input value to generate the {@link GameProfile}.
     */
    @NotNull
    @Contract(pure = true)
    static Profileable of(@NotNull ProfileInputType type, @NotNull String input) {
        Objects.requireNonNull(type, () -> "Cannot profile from a null input type: " + input);
        Objects.requireNonNull(input, () -> "Cannot profile from a null input: " + type);
        return new StringProfileable(input, type);
    }

    @ApiStatus.Internal
    final class UsernameProfileable extends TimedCacheableProfileable {
        private final String username;
        private Boolean valid;

        public UsernameProfileable(String username) {this.username = Objects.requireNonNull(username);}

        @Override
        public String getProfileValue() {
            return username;
        }

        @Override
        protected GameProfile cacheProfile() {
            if (valid == null) {
                valid = ProfileInputType.USERNAME.pattern.matcher(username).matches();
            }
            if (!valid) throw new InvalidProfileException(username, "Invalid username: '" + username + '\'');

            Optional<GameProfile> profileOpt = MojangAPI.getMojangCachedProfileFromUsername(username);
            if (!profileOpt.isPresent())
                throw new UnknownPlayerException(username, "Cannot find player named '" + username + '\'');

            GameProfile profile = profileOpt.get();
            if (PlayerProfiles.hasTextures(profile)) return profile;
            return MojangAPI.getOrFetchProfile(profile);
        }
    }

    @ApiStatus.Internal
    final class UUIDProfileable extends TimedCacheableProfileable {
        private final UUID id;

        public UUIDProfileable(UUID id) {this.id = Objects.requireNonNull(id, "UUID cannot be null");}

        @Override
        public String getProfileValue() {
            return id.toString();
        }

        @Override
        protected GameProfile cacheProfile() {
            GameProfile profile = MojangAPI.getCachedProfileByUUID(id);
            if (PlayerProfiles.hasTextures(profile)) return profile;
            return MojangAPI.getOrFetchProfile(profile);
        }
    }

    @ApiStatus.Internal
    final class DynamicGameProfileProfileable extends TimedCacheableProfileable {
        private final GameProfile profile;

        public DynamicGameProfileProfileable(GameProfile profile) {this.profile = Objects.requireNonNull(profile);}

        @Override
        protected GameProfile cacheProfile() {
            if (PlayerProfiles.hasTextures(profile)) {
                return profile;
            }

            return (PlayerUUIDs.isOnlineMode()
                    ? new UUIDProfileable(profile.getId())
                    : new UsernameProfileable(profile.getName())
            ).getProfile();
        }
    }

    @ApiStatus.Internal
    final class RawGameProfileProfileable implements Profileable {
        private final GameProfile profile;

        public RawGameProfileProfileable(GameProfile profile) {this.profile = Objects.requireNonNull(profile);}

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        @NotNull
        @Unmodifiable
        public GameProfile getProfile() {
            return profile;
        }
    }

    /**
     * <h3>Bedrock Players</h3>
     * We need to support hybrid server software like Geyser that has their own way of calculating the UUID.
     * This will be problematic for Bedrock players as their UUID system is completely different.
     * To obtain a Bedrock player's Floodgate UUID (whether the server is offline/online):
     * <ol>
     *     <li>Strip the username prefix (a dot by default.)</li>
     *     <li>Go to <a href="https://www.cxkes.me/xbox/xuid">www.cxkes.me</a> and search their username.</li>
     *     <li>You'll see that Bedrock players use an XUID which is their unique Xbox Live account ID.</li>
     *     <li>For the UUID, you need to take their XUID in the HEX format not DEC.</li>
     *     <li>Now their fake UUID (or more specifically Floodgate UUID) will be their XUID with leading zeros to fill the rest of the UUID characters.</li>
     * </ol>
     * So for example, a player named {@code Martinacasas234} with XUID {@code 2535442111844868} (DEC):
     * <pre>{@code
     *     XUID (HEX):                    0009 01F89E6CD204
     *     UUID:       00000000-0000-0000-0009-01f89e6cd204
     * }</pre>
     * Alternatively, you could get the calculated UUID directly from <a href="https://mcprofile.io/">mcprofile.io</a>.
     * This is also mentioned in <a href="https://geysermc.org/wiki/floodgate/faq/">Geyser's FAQ</a>.
     * Trying to look up this UUID from Mojang's API or other third-party Java Edition online lookup tools will not work, obviously.
     * <p>
     * Note that their skin texture URL still follow the {@code textures.minecraft.net} URL.
     * <p>
     * These services themselves don't make a direct API request to Xbox/Microsoft services since
     * this specific information (converting gamertag -> XUID) is a lengthy process.
     * You can read more about it in
     * <a href="https://stackoverflow.com/questions/58477840/convert-xbox-live-gamertag-to-xuid-using-microsoft-rest-api-java">this StackOverflow question</a>
     * and <a href="https://den.dev/blog/convert-gamertag-to-xuid/">this den.dev blog post</a>.
     * The client iself is sending the XUID to the server along with the gamertag, which
     * means we would need to add direct support for Geyser:
     * <pre>
     *               io.netty
     *                   ↓
     *     CloudburstMC's packet encoder
     *                   ↓
     *     <a href="https://github.com/GeyserMC/Geyser/blob/c5fd3d485bead7be6c2bf1c4cf5b9952fe0161a7/core/src/main/java/org/geysermc/geyser/network/UpstreamPacketHandler.java#L189">Geyser's UpstreamPacketHandler#handle(LoginPacket)</a>
     *                   ↓
     *     <a href="https://github.com/GeyserMC/Geyser/blob/c5fd3d485bead7be6c2bf1c4cf5b9952fe0161a7/core/src/main/java/org/geysermc/geyser/util/LoginEncryptionUtils.java#L68-L78">Geyser's LoginEncryptionUtils</a>
     *                   ↓
     *     <a href="https://github.com/CloudburstMC/Protocol/blob/88a122de252b50488ffce9fa6c18b825c9287080/bedrock-connection/src/main/java/org/cloudburstmc/protocol/bedrock/util/EncryptionUtils.java#L93-L135">CloudburstMC's EncryptionUtils#validateChain</a>
     *                   ↓
     *     <a href="https://github.com/CloudburstMC/Protocol/blob/88a122de252b50488ffce9fa6c18b825c9287080/bedrock-connection/src/main/java/org/cloudburstmc/protocol/bedrock/util/ChainValidationResult.java#L40-L63">CloudburstMC's ChainValidationResult#identityClaims</a>
     * </pre>
     *
     * <br><br><br>
     * <h3>China's NetEase</h3>
     * This is unrelated to {@code Minecraft: China Edition}. For Java edition, NetEase
     * (company associated with contributing to Minecraft: China Edition development) has its own authentication
     * system instead of Mojang's Yggdrasil and its own private Forge mod for handling {@link GameProfile}s.
     * The {@link GameProfile} in the server doesn't seem to contain any texture URL but it seems like the
     * mod itself handles the skin by sending a request (private information) and retrieving the skin directly
     * from the client itself using the username/UUID only.
     * Their UUID implementation is custom and the algorithm is private information.
     *
     * <br><br><br>
     * <H3>Solution?</H3>
     * So the solution to all of this would be to make use of the newer {@link org.bukkit.profile.PlayerProfile}
     * when available and defaulting back to the provided {@link GameProfile} from the CraftBukkit instance directly
     * using a system property.
     */
    @ApiStatus.Internal
    final class PlayerProfileable extends TimedCacheableProfileable {
        // Only save these to let the GC do its job for the OfflinePlayer instance.
        @Nullable private final String username;
        @NotNull private final UUID id;

        public PlayerProfileable(OfflinePlayer player) {
            Objects.requireNonNull(player);
            this.username = player.getName();
            this.id = player.getUniqueId();
        }

        @Override
        public String getProfileValue() {
            return Strings.isNullOrEmpty(username) ? id.toString() : username;
        }

        @Override
        protected GameProfile cacheProfile() {
            // Can be empty if used by:
            // CraftServer -> public OfflinePlayer getOfflinePlayer(UUID id)
            if (Strings.isNullOrEmpty(username)) {
                return new UUIDProfileable(id).getProfile();
            } else {
                return new UsernameProfileable(username).getProfile();
            }
        }
    }

    @ApiStatus.Internal
    final class PlayerProfileProfileable extends TimedCacheableProfileable {
        // Only save these to let the GC do its job for the OfflinePlayer instance.
        @NotNull private final PlayerProfile profile;
        @Nullable private PlayerProfile updated;

        private static final MethodHandle CraftPlayerProfile_buildGameProfile = XReflection.ofMinecraft()
                .inPackage(MinecraftPackage.CB, "profile").named("CraftPlayerProfile")
                .method("public com.mojang.authlib.GameProfile buildGameProfile()")
                .reflectOrNull();

        public PlayerProfileProfileable(PlayerProfile profile) {
            this.profile = Objects.requireNonNull(profile);
        }

        @Override
        protected GameProfile cacheProfile() {
            // This is a timed cache, so we always update.
            updated = profile.update().join();

            if (CraftPlayerProfile_buildGameProfile == null) {
                GameProfile gameProfile = new GameProfile(updated.getUniqueId(), updated.getName());

                String skinURL = updated.getTextures().getSkin().toString();
                String base64 = PlayerProfiles.encodeBase64(PlayerProfiles.TEXTURES_NBT_PROPERTY_PREFIX + skinURL + "\"}}}");
                PlayerProfiles.setTexturesProperty(gameProfile, base64);

                return gameProfile;
            } else {
                try {
                    return (GameProfile) CraftPlayerProfile_buildGameProfile.invoke(updated);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @ApiStatus.Internal
    final class StringProfileable extends TimedCacheableProfileable {
        private final String string;
        @Nullable private ProfileInputType type;

        public StringProfileable(String string, @Nullable ProfileInputType type) {
            this.string = Objects.requireNonNull(string, "Input string is null");
            this.type = type;
        }

        @Override
        public String getProfileValue() {
            return string;
        }

        @Override
        protected Duration expiresAfter() {
            determineType();
            if (type == null) return Duration.ZERO;

            switch (type) {
                case USERNAME:
                case UUID:
                    return super.expiresAfter();
                default:
                    return Duration.ZERO;
            }
        }

        private StringProfileable determineType() {
            if (type == null) type = ProfileInputType.typeOf(string);
            return this;
        }

        @Override
        protected GameProfile cacheProfile() {
            determineType();
            if (type == null) {
                throw new InvalidProfileException(string, "Unknown skull string value: " + string);
            }
            return type.getProfile(string);
        }
    }
}