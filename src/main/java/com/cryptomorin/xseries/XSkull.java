/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Crypto Morin
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
package com.cryptomorin.xseries;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.FieldMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.MethodMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.ReflectiveNamespace;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftClassHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftMapping;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandle;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>XSkull</b> - Apply skull texture from different sources.<br><br>
 * Skull Meta: <a href="https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/meta/SkullMeta.html">hub.spigotmc.org/.../SkullMeta</a><br>
 * Mojang API: <a href="https://wiki.vg/Mojang_API">wiki.vg/Mojang_API</a><br><br>
 * <p>
 * Some websites to get custom heads:
 * <ul>
 *     <li><a href="https://minecraft-heads.com/">minecraft-heads.com</a></li>
 * </ul>
 *
 * <h1>Usage</h1>
 * The basic usage format of this API is as follows:
 * <pre>{@code
 * XSkull.createItem().profile(player).apply();
 * XSkull.of(item/block).profile(configValueString).apply();
 * }</pre>
 * <p>
 * Note: Make sure to read {@link SkullAction#applyAsync()} if you're going to
 * be requesting a lot of different skulls.
 *
 * <h1>Mechanism</h1>
 * <p>
 * The basic premise behind this API is that the final skull data is contained in a {@link GameProfile}
 * either by ID, name or encoded textures URL property.
 * <p>
 * Different versions of Minecraft client handle this differently. In newer versions the client seem
 * to prioritize the texture property over the set UUID and name, in older versions however using the
 * same UUID for all GameProfiles caused all skulls (that use base64) to look the same.
 * The client is responsible for caching skull textures. If the download were to fail (either because of
 * connection issues or invalid values) the client will cache that skull UUID and the skull
 * will remain as a steve head until the client is completely restarted.
 * I don't know if this cache system works across other servers or is just specific to one server.
 *
 * @author Crypto Morin
 * @version 10.0.0
 * @see XMaterial
 * @see XReflection
 */
public final class XSkull {
    private static final Logger LOGGER = LogManager.getLogger("XSkull");
    private static final Object USER_CACHE, MINECRAFT_SESSION_SERVICE;

    private static final MethodHandle
            FILL_PROFILE_PROPERTIES, GET_PROFILE_BY_NAME, GET_PROFILE_BY_UUID, CACHE_PROFILE,
            CRAFT_META_SKULL_PROFILE_GETTER, CRAFT_META_SKULL_PROFILE_SETTER,
            CRAFT_SKULL_PROFILE_SETTER, CRAFT_SKULL_PROFILE_GETTER,
            PROPERTY_GET_VALUE;

    /**
     * Some people use this without quotes surrounding the keys, not sure if that'd work.
     */
    private static final String TEXTURES_NBT_PROPERTY_PREFIX = "{\"textures\":{\"SKIN\":{\"url\":\"";

    /**
     * In v1.20.2 there were some changes to the Mojang API.
     * Before that version, both UUID and name fields couldn't be null, only one of them.
     * It gave the error: {@code Name and ID cannot both be blank}
     * Here, "blank" is null for UUID, and {@code Character.isWhitespace} for the name field.
     */
    private static final String DEFAULT_PROFILE_NAME = "XSeries";
    private static final Property XSKULL_GAMEPROFILE_SIGNATURE = new Property(DEFAULT_PROFILE_NAME, "true");
    /**
     * Used as the default UUID for GameProfiles.
     * Also used as a null-indicating value.
     */
    private static final UUID IDENTITY_UUID = new UUID(0, 0);
    private static final Pattern UUID_NO_DASHES = Pattern.compile("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})");
    /**
     * "requireSecure" parameter basically means ignore the cache and also use "unsigned=false" parameter.
     */
    private static final boolean REQUIRE_SECURE_PROFILES = false;

    /**
     * We'll just return an x shaped hardcoded skull.<br>
     * <a href="https://minecraft-heads.com/custom-heads/miscellaneous/58141-cross">minecraft-heads.com</a>
     */
    private static final GameProfile DEFAULT_PROFILE = signXSkull(SkullInputType.BASE64.getProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5l" +
                    "Y3JhZnQubmV0L3RleHR1cmUvYzEwNTkxZTY5MDllNmEyODFiMzcxODM2ZTQ2MmQ2" +
                    "N2EyYzc4ZmEwOTUyZTkxMGYzMmI0MWEyNmM0OGMxNzU3YyJ9fX0="
    ));

    /**
     * In v1.20.2, Mojang switched to {@code record} class types for their {@link Property} class.
     */
    private static final boolean NULLABILITY_RECORD_UPDATE = XReflection.supports(20, 2);

    /**
     * The value after this URL is probably an SHA-252 value that Mojang uses to unique identify player skins.
     * <br>
     * This <a href="https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin/Cape">wiki</a> documents how to
     * get base64 information from player's UUID.
     */
    private static final String TEXTURES_BASE_URL = "http://textures.minecraft.net/texture/";

    /**
     * Used for older versions.
     */
    private static final Cache<UUID, GameProfile> INSECURE_PROFILES = CacheBuilder.newBuilder()
            .expireAfterWrite(6L, TimeUnit.HOURS).build();

    /**
     * According to <a href="https://wiki.vg/Mojang_API">Mojang API</a> the rate limit
     * is around 600 requests per 10 (i.e. 1 request per second) for most endpoints.
     * However <a href="https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin.2FCape">UUID to Profile and Skin/Cape</a>
     * is around 200 requests per minute.
     * <p>
     * If we want to use UUID_TO_PROFILE_RATELIMIT we'd have to copy and paste fetchProfile()
     * because that method catches HTTP exceptions.
     */
    private static final RateLimiter
            UUID_RATELIMIT = new RateLimiter(600, Duration.ofMinutes(10)),
            UUID_TO_PROFILE_RATELIMIT = new RateLimiter(200, Duration.ofMinutes(1));

    private static final class RateLimiter {
        private final ConcurrentLinkedQueue<Long> requests = new ConcurrentLinkedQueue<>();
        private final int maxRequests;
        private final long per;

        private RateLimiter(int maxRequests, Duration per) {
            this.maxRequests = maxRequests;
            this.per = per.toMillis();
        }

        private ConcurrentLinkedQueue<Long> getRequests() {
            if (requests.isEmpty()) return requests;

            // Implementing a cleanup delay is practically not any different
            // from just letting this loop to happen in terms of performance.
            long now = System.currentTimeMillis();
            Iterator<Long> iter = requests.iterator();
            while (iter.hasNext()) {
                long requestedAt = iter.next();
                long diff = now - requestedAt;
                if (diff > per) iter.remove();
                else break; // Requests are ordered, so if this fails, the others will fail too.
            }

            return requests;
        }

        private int getRemainingRequests() {
            return Math.max(0, maxRequests - getRequests().size());
        }

        public void instantRateLimit() {
            long now = System.currentTimeMillis();
            for (int i = 0; i < getRemainingRequests(); i++) {
                requests.add(now);
            }
        }

        public boolean acquire() {
            if (getRemainingRequests() <= 0) {
                return false;
            } else {
                requests.add(System.currentTimeMillis());
                return true;
            }
        }
    }

    static {
        Object userCache, minecraftSessionService;
        MethodHandle fillProfileProperties = null, getProfileByName, getProfileByUUID, cacheProfile;
        MethodHandle profileSetterMeta, profileGetterMeta, getPropertyValue = null;

        ReflectiveNamespace ns = XReflection.namespaced()
                .imports(GameProfile.class, MinecraftSessionService.class);
        try {
            MinecraftClassHandle CraftMetaSkull = ns.ofMinecraft(
                    "package cb.inventory; class CraftMetaSkull extends CraftMetaItem implements SkullMeta {}"
            );
            profileGetterMeta = CraftMetaSkull.field("private GameProfile profile;").getter().reflect();

            try {
                // https://github.com/CryptoMorin/XSeries/issues/169
                // noinspection MethodMayBeStatic
                profileSetterMeta = CraftMetaSkull.method("private void setProfile(GameProfile profile);").reflect();
            } catch (NoSuchMethodException e) {
                profileSetterMeta = CraftMetaSkull.field("private GameProfile profile;").setter().reflect();
            }

            MinecraftClassHandle MinecraftServer = ns.ofMinecraft(
                    "package nms.server; public abstract class MinecraftServer {}"
            );

            MinecraftClassHandle GameProfileCache = ns.ofMinecraft(
                    "package nms.server.players; public class GameProfileCache {}"
            ).map(MinecraftMapping.SPIGOT, "UserCache");

            // Added by Bukkit
            Object minecraftServer = MinecraftServer.method("public static MinecraftServer getServer();").reflect().invoke();

            minecraftSessionService = MinecraftServer.method("public MinecraftSessionService getSessionService();")
                    .named(/* 1.19.4 */ "ay", /* 1.17.1 */ "getMinecraftSessionService", "az", "ao", "am", /* 1.20.4 */ "aD", /* 1.20.6 */ "ar")
                    .reflect().invoke(minecraftServer);

            userCache = MinecraftServer.method("public GameProfileCache getProfileCache();")
                    .named("ar", /* 1.18.2 */ "ao", /* 1.20.4 */ "ap", /* 1.20.6 */ "au")
                    .map(MinecraftMapping.OBFUSCATED, /* 1.9.4 */ "getUserCache")
                    .reflect().invoke(minecraftServer);

            if (!NULLABILITY_RECORD_UPDATE) {
                fillProfileProperties = ns.of(MinecraftSessionService.class).method(
                        "public GameProfile fillProfileProperties(GameProfile profile, boolean flag);"
                ).reflect();
            }

            MethodMemberHandle profileByName = GameProfileCache.method().named(/* v1.17.1 */ "getProfile", "a");
            MethodMemberHandle profileByUUID = GameProfileCache.method().named(/* v1.17.1 */ "getProfile", "a");
            getProfileByName = XReflection.anyOf(
                    () -> profileByName.signature("public GameProfile get(String username);"),
                    () -> profileByName.signature("public Optional<GameProfile> get(String username);")
            ).reflect();
            getProfileByUUID = XReflection.anyOf(
                    () -> profileByUUID.signature("public GameProfile get(UUID id);"),
                    () -> profileByUUID.signature("public Optional<GameProfile> get(UUID id);")
            ).reflect();

            cacheProfile = GameProfileCache.method("public void add(GameProfile profile);")
                    .map(MinecraftMapping.OBFUSCATED, "a").reflect();
        } catch (Throwable throwable) {
            throw XReflection.throwCheckedException(throwable);
        }

        MinecraftClassHandle CraftSkull = ns.ofMinecraft(
                "package cb.block; public class CraftSkull extends CraftBlockEntityState implements Skull {}"
        );

        FieldMemberHandle craftProfile = CraftSkull.field("private GameProfile profile;");

        if (!NULLABILITY_RECORD_UPDATE) {
            getPropertyValue = ns.of(Property.class).method("public String getValue();").unreflect();
        }
        USER_CACHE = userCache;
        MINECRAFT_SESSION_SERVICE = minecraftSessionService;
        FILL_PROFILE_PROPERTIES = fillProfileProperties;
        GET_PROFILE_BY_NAME = getProfileByName;
        GET_PROFILE_BY_UUID = getProfileByUUID;
        CACHE_PROFILE = cacheProfile;
        PROPERTY_GET_VALUE = getPropertyValue;
        CRAFT_META_SKULL_PROFILE_SETTER = profileSetterMeta;
        CRAFT_META_SKULL_PROFILE_GETTER = profileGetterMeta;
        CRAFT_SKULL_PROFILE_SETTER = craftProfile.setter().unreflect();
        CRAFT_SKULL_PROFILE_GETTER = craftProfile.getter().unreflect();
    }

    private static void debug(String mainMessage, Object... variables) {
        LOGGER.debug(mainMessage, variables);
    }

    /**
     * Creates a {@link SkullInstruction} for an {@link ItemStack}.
     * This method initializes a new player head.
     *
     * @return A {@link SkullInstruction} that sets the profile for the generated {@link ItemStack}.
     */
    public static SkullInstruction<ItemStack> createItem() {
        return of(XMaterial.PLAYER_HEAD.parseItem());
    }

    /**
     * Creates a {@link SkullInstruction} for an {@link ItemStack}.
     *
     * @param stack The {@link ItemStack} to set the profile for.
     * @return A {@link SkullInstruction} that sets the profile for the given {@link ItemStack}.
     */
    public static SkullInstruction<ItemStack> of(ItemStack stack) {
        return new SkullInstruction<>(new ProfileContainer.ItemStackProfileContainer(stack));
    }

    /**
     * Creates a {@link SkullInstruction} for an {@link ItemMeta}.
     *
     * @param meta The {@link ItemMeta} to set the profile for.
     * @return An {@link SkullInstruction} that sets the profile for the given {@link ItemMeta}.
     */
    public static SkullInstruction<ItemMeta> of(ItemMeta meta) {
        return new SkullInstruction<>(new ProfileContainer.ItemMetaProfileContainer(meta));
    }

    /**
     * Creates a {@link SkullInstruction} for a {@link Block}.
     *
     * @param block The {@link Block} to set the profile for.
     * @return An {@link SkullInstruction} that sets the profile for the given {@link Block}.
     */
    public static SkullInstruction<Block> of(Block block) {
        return new SkullInstruction<>(new ProfileContainer.BlockProfileContainer(block));
    }

    /**
     * Creates a {@link SkullInstruction} for a {@link BlockState}.
     *
     * @param state The {@link BlockState} to set the profile for.
     * @return An {@link SkullInstruction} that sets the profile for the given {@link BlockState}.
     */
    public static SkullInstruction<Skull> of(BlockState state) {
        return new SkullInstruction<>(new ProfileContainer.BlockStateProfileContainer((Skull) state));
    }

    /**
     * Checks if the provided {@link GameProfile} has a texture property.
     *
     * @param profile The {@link GameProfile} to check.
     * @return {@code true} if the profile has a texture property, {@code false} otherwise.
     */
    private static boolean hasTextures(GameProfile profile) {
        return getTextureProperty(profile).isPresent();
    }

    protected abstract static class ProfileContainer<T> {
        @Nonnull
        public abstract T setProfile(@Nullable GameProfile profile);

        @Nullable
        public abstract GameProfile getProfile();

        @Nullable
        public final String getProfileValue() {
            GameProfile profile = getProfile();
            if (profile == null) return null;
            return getTextureProperty(profile).map(XSkull::getPropertyValue).orElse(null);
        }

        private static final class ItemStackProfileContainer extends ProfileContainer<ItemStack> {
            private final ItemStack itemStack;

            private ItemStackProfileContainer(ItemStack itemStack) {this.itemStack = itemStack;}

            @Override
            public ItemStack setProfile(GameProfile profile) {
                ItemMeta meta = itemStack.getItemMeta();
                new ItemMetaProfileContainer(meta).setProfile(profile);
                itemStack.setItemMeta(meta);
                return itemStack;
            }

            @Override
            public GameProfile getProfile() {
                return new ItemMetaProfileContainer(itemStack.getItemMeta()).getProfile();
            }
        }

        private static final class ItemMetaProfileContainer extends ProfileContainer<ItemMeta> {
            private final ItemMeta meta;

            private ItemMetaProfileContainer(ItemMeta meta) {this.meta = meta;}

            @Override
            public ItemMeta setProfile(GameProfile profile) {
                try {
                    CRAFT_META_SKULL_PROFILE_SETTER.invoke(meta, profile);
                } catch (Throwable throwable) {
                    throw new RuntimeException("Unable to set profile " + profile + " to " + meta, throwable);
                }
                return meta;
            }

            @Override
            public GameProfile getProfile() {
                try {
                    return (GameProfile) CRAFT_META_SKULL_PROFILE_GETTER.invoke((SkullMeta) meta);
                } catch (Throwable throwable) {
                    throw new RuntimeException("Failed to get profile from item meta: " + meta, throwable);
                }
            }
        }

        private static final class BlockProfileContainer extends ProfileContainer<Block> {
            private final Block block;

            private BlockProfileContainer(Block block) {this.block = block;}

            private Skull getBlockState() {
                return (Skull) block.getState();
            }

            @Override
            public Block setProfile(GameProfile profile) {
                Skull state = getBlockState();
                new BlockStateProfileContainer(state).setProfile(profile);
                state.update(true);
                return block;
            }

            @Override
            public GameProfile getProfile() {
                return new BlockStateProfileContainer(getBlockState()).getProfile();
            }
        }

        private static final class BlockStateProfileContainer extends ProfileContainer<Skull> {
            private final Skull state;

            private BlockStateProfileContainer(Skull state) {this.state = state;}

            @Override
            public Skull setProfile(GameProfile profile) {
                try {
                    CRAFT_SKULL_PROFILE_SETTER.invoke(state, profile);
                } catch (Throwable throwable) {
                    throw new RuntimeException("Unable to set profile " + profile + " to " + state, throwable);
                }
                return state;
            }

            @Override
            public GameProfile getProfile() {
                try {
                    return (GameProfile) CRAFT_SKULL_PROFILE_GETTER.invoke(state);
                } catch (Throwable throwable) {
                    throw new RuntimeException("Unable to get profile fr om blockstate: " + state, throwable);
                }
            }
        }
    }

    private static Optional<Property> getTextureProperty(GameProfile profile) {
        return Optional.ofNullable(Iterables.getFirst(profile.getProperties().get("textures"), null));
    }

    /**
     * Retrieves the skin value from the given {@link GameProfile}.
     *
     * @param profile The {@link GameProfile} to retrieve the skin value from.
     * @return The skin value as a {@link String}, or {@code null} if not found.
     * @throws NullPointerException if {@code profile} is {@code null}.
     */
    @Nullable
    public static String getSkinValue(@Nonnull GameProfile profile) {
        Objects.requireNonNull(profile, "Game profile cannot be null");
        return getTextureProperty(profile).map(XSkull::getPropertyValue).orElse(null);
    }

    /**
     * Retrieves a {@link GameProfile} from the given {@link UUID}.
     *
     * @param uuid The {@link UUID} to retrieve the profile from.
     * @return The {@link GameProfile} corresponding to the input.
     * @throws NullPointerException if {@code uuid} is {@code null}.
     */
    @Nonnull
    public static GameProfile getProfile(@Nonnull UUID uuid) {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        GameProfile profile = getCachedProfileByUUID(uuid);
        if (hasTextures(profile)) return profile;
        return fetchProfile(profile);
    }

    /**
     * Retrieves a cached {@link GameProfile} by username from the user cache.
     * If the profile is not found in the cache, creates a new profile with the provided name.
     *
     * @param username The username of the profile to retrieve from the cache.
     * @return The cached {@link GameProfile} corresponding to the username, or a new profile if not found.
     */
    private static GameProfile getCachedProfileByUsername(String username) {
        try {
            // Expires after every month calendar.add(2, 1); (Persists between restarts)
            @Nullable Object profile = GET_PROFILE_BY_NAME.invoke(USER_CACHE, username);
            if (profile instanceof Optional) profile = ((Optional<?>) profile).orElse(null);
            GameProfile gameProfile = profile == null ? new GameProfile(IDENTITY_UUID, username) : sanitizeProfile((GameProfile) profile);
            debug("The cached profile for {} -> {}", username, profile);
            return gameProfile;
        } catch (Throwable throwable) {
            LOGGER.error("Unable to get cached profile by username: " + username, throwable);
            return null;
        }
    }

    /**
     * Retrieves a cached {@link GameProfile} by UUID from the user cache.
     * If the profile is not found in the cache, creates a new profile with the provided UUID.
     *
     * @param uuid The UUID of the profile to retrieve from the cache.
     * @return The cached {@link GameProfile} corresponding to the UUID, or a new profile if not found.
     */
    @Nonnull
    private static GameProfile getCachedProfileByUUID(UUID uuid) {
        uuid = isOnlineMode() ? uuid : ONLINE_TO_OFFLINE.getOrDefault(uuid, uuid);
        try {
            @Nullable Object profile = GET_PROFILE_BY_UUID.invoke(USER_CACHE, uuid);
            if (profile instanceof Optional) profile = ((Optional<?>) profile).orElse(null);
            debug("The cached profile for {} -> {}", uuid, profile);
            return profile == null ? createNamelessGameProfile(uuid) : sanitizeProfile((GameProfile) profile);
        } catch (Throwable throwable) {
            LOGGER.error("Unable to get cached profile by UUID: " + uuid, throwable);
            return getDefaultProfile();
        }
    }

    /**
     * Caches the provided {@link GameProfile} in the user cache.
     * These caches are also stored in {@code usercache.json} (file specified in net.minecraft.server.Services).
     *
     * @param profile The {@link GameProfile} to cache.
     */
    private static void cacheProfile(GameProfile profile) {
        try {
            CACHE_PROFILE.invoke(USER_CACHE, profile);
            debug("Profile is now cached: {}", profile);
        } catch (Throwable throwable) {
            LOGGER.error("Unable to cache profile: " + profile, throwable);
        }
    }

    /**
     * Fetches additional properties for the given {@link GameProfile} if possible (like the texture).
     *
     * @param profile The {@link GameProfile} for which properties are to be fetched.
     * @return The updated {@link GameProfile} with fetched properties, sanitized for consistency.
     * @throws IllegalArgumentException if a player with the specified profile properties (username and UUID) doesn't exist.
     */
    @Nonnull
    private static GameProfile fetchProfile(@Nonnull GameProfile profile) {
        if (!NULLABILITY_RECORD_UPDATE) {
            GameProfile old = profile;
            GameProfile cached = INSECURE_PROFILES.getIfPresent(profile.getId());
            GameProfile newProfile;

            try {
                /* This URL endpoint still works as of 1.20.6
                 * URL url = HttpAuthenticationService.constantURL("https://sessionserver.mojang.com/session/minecraft/profile/" + UUIDTypeAdapter.fromUUID(profile.getId()));
                 * url = HttpAuthenticationService.concatenateURL(url, "unsigned=" + !requireSecure);
                 * GameProfile result = new GameProfile(response.getId(), response.getName());
                 * result.getProperties().putAll(response.getProperties());
                 * profile.getProperties().putAll(response.getProperties());
                 */
                // These results are not cached unlike the newer fetchProfile()
                if (cached == null) {
                    newProfile = (GameProfile) FILL_PROFILE_PROPERTIES
                            .invoke(MINECRAFT_SESSION_SERVICE, profile, REQUIRE_SECURE_PROFILES);
                    if (profile == newProfile) {
                        // Returns the same instance if any error occurs.
                        throw new PlayerProfileNotFoundException("Player with the given properties not found: " + profile);
                    }
                } else {
                    newProfile = (profile = cached);
                }
                debug("Filled properties: {} -> {}", old, newProfile);
            } catch (PlayerProfileNotFoundException ex) {
                throw ex;
            } catch (Throwable throwable) {
                throw new RuntimeException("Unable to fetch profile properties: " + profile, throwable);
            }
        } else {
            // Get real UUID for offline players
            UUID realUUID;
            if (profile.getName().equals(DEFAULT_PROFILE_NAME)) {
                // We will assume that the requested UUID is the real one
                // since the server cache didn't find it and that player never
                // joined this server.
                // There is no way to tell if this is fake or real UUID, the
                // closest we can get is to just request it from the server
                // and see if it exists. (We can't reverse UUID.nameUUIDFromBytes)
                realUUID = profile.getId();
            } else {
                realUUID = getRealUUIDOfPlayer(profile.getName(), profile.getId());
                if (realUUID == null) {
                    throw new PlayerProfileNotFoundException("Player with the given properties not found: " + profile);
                }
            }

            // Implemented by YggdrasilMinecraftSessionService
            // fetchProfile(UUID profileId, boolean requireSecure) -> fetchProfileUncached(UUID profileId, boolean requireSecure)
            // This cache expireAfterWrite every 6 hours.
            com.mojang.authlib.yggdrasil.ProfileResult result = ((MinecraftSessionService) MINECRAFT_SESSION_SERVICE)
                    .fetchProfile(realUUID, REQUIRE_SECURE_PROFILES);
            if (result != null) {
                profile = result.profile();
                debug("Yggdrasil provided profile is {} with actions {} for {}", result.profile(), result.actions(), profile);
            } else {
                debug("Yggdrasil provided profile is null with actions for {}", profile);
            }
        }

        profile = sanitizeProfile(profile);
        signXSkull(profile);
        cacheProfile(profile);
        return profile;
    }

    public static final class MojangAPIException extends RuntimeException {
        public MojangAPIException(String message) {
            super(message);
        }

        public MojangAPIException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static final class PlayerProfileNotFoundException extends RuntimeException {
        public PlayerProfileNotFoundException(String message) {
            super(message);
        }
    }

    @Nonnull
    private static UUID getOfflineUUID(@Nonnull String username) {
        // Vanilla behavior across all platforms.
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }

    private static boolean isOnlineMode() {
        return Bukkit.getOnlineMode();
    }

    /**
     * We can't use Guava's BiMap here since non-existing players are cached too.
     */
    private static final Map<UUID, UUID> OFFLINE_TO_ONLINE = new HashMap<>(), ONLINE_TO_OFFLINE = new HashMap<>();

    /**
     * @return null if a player with this username doesn't exist.
     */
    @ApiStatus.Internal
    @Nullable
    public static UUID getRealUUIDOfPlayer(@Nonnull String username, @Nonnull UUID uuid) {
        Objects.requireNonNull(uuid);
        if (Strings.isNullOrEmpty(username))
            throw new IllegalArgumentException("Username is null or empty: " + username);

        if (isOnlineMode()) return uuid;

        // OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        // if (!player.hasPlayedBefore()) throw new RuntimeException("Player with UUID " + uuid + " doesn't exist.");

        UUID realUUID = OFFLINE_TO_ONLINE.get(uuid);
        if (realUUID == null) {
            try {
                realUUID = requestUsernameToUUID(username);
                if (realUUID == null) {
                    debug("Caching null for {} ({}) because it doesn't exist.", username, uuid);
                    realUUID = IDENTITY_UUID; // Player not found, we should cache this information.
                } else ONLINE_TO_OFFLINE.put(realUUID, uuid);
                OFFLINE_TO_ONLINE.put(uuid, realUUID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (realUUID == IDENTITY_UUID) {
            debug("Providing null value for {} ({}) because it doesn't exist.", username, uuid);
            realUUID = null;
        } else {
            debug("Real UUID for {} ({}) is {}", username, uuid, realUUID);
        }

        UUID offlineUUID = getOfflineUUID(username);
        if (!uuid.equals(offlineUUID) && !uuid.equals(realUUID)) {
            throw new RuntimeException("The provided UUID (" + uuid + ") for '" + username +
                    "' doesn't match the offline UUID (" + offlineUUID + ") or the real UUID (" + realUUID + ')');
        }
        return realUUID;
    }

    /**
     * @return null if a player with that username is not found.
     */
    @Nullable
    private static UUID requestUsernameToUUID(@Nonnull String username) throws IOException {
        JsonObject userJson = requestUsernameToUUIDData(username);
        if (userJson == null) return null;

        JsonElement idElement = userJson.get("id");
        if (idElement == null)
            throw new RuntimeException("No 'id' field for UUID request for '" + username + "': " + userJson);

        return UUIDFromDashlessString(idElement.getAsString());
    }

    private static UUID UUIDFromDashlessString(String dashlessUUIDString) {
        Matcher matcher = UUID_NO_DASHES.matcher(dashlessUUIDString);
        try {
            return UUID.fromString(matcher.replaceFirst("$1-$2-$3-$4-$5"));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Cannot convert from dashless UUID: " + dashlessUUIDString, ex);
        }
    }

    /**
     * @return null if a player with this username doesn't exist.
     */
    @Nullable
    private static JsonObject requestUsernameToUUIDData(@Nonnull String username) throws IOException {
        if (!UUID_RATELIMIT.acquire())
            throw new IllegalStateException("Rate limit has been hit! " + UUID_RATELIMIT.getRequests().size());

        HttpURLConnection connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + username).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10 * 1000); // 10 seconds
        connection.setReadTimeout(30 * 1000); // 30 seconds
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setAllowUserInteraction(false);
        debug("Sending request to {}", connection.getURL());

        try {
            return connectionStreamToJson(connection, false).getAsJsonObject();
        } catch (Throwable ex) {
            MojangAPIException exception;
            try {
                switch (connection.getResponseCode()) {
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        return null;
                    case 429: // Too many requests
                        UUID_RATELIMIT.instantRateLimit();
                }
                if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) return null;
                if (ex instanceof SocketException && ex.getMessage().toLowerCase(Locale.ENGLISH).contains("connection reset")) {
                    // TODO handle reconnecting for future requests.
                    throw ex;
                }
                exception = new MojangAPIException(connectionStreamToJson(connection, true).toString(), ex);
            } catch (Throwable errorEx) {
                exception = new MojangAPIException("Failed to read both normal response and error response from '" + connection.getURL() + '\'');
                exception.addSuppressed(ex);
                exception.addSuppressed(errorEx);
            }

            throw exception;
        }
    }

    private static JsonElement connectionStreamToJson(HttpURLConnection connection, boolean error) throws IOException, RuntimeException {
        try (
                InputStream inputStream = error ? connection.getErrorStream() : connection.getInputStream();
                JsonReader reader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        ) {
            JsonElement json = Streams.parse(reader);
            if (json == null || !json.isJsonObject()) {
                // For UUID_TO_PROFILE, this happens when HTTP Code 204 (No Content) is given.
                // And that happens if the UUID doesn't exist in Mojang servers. (E.g. cracked UUIDs)
                throw new RuntimeException((error ? "error" : "normal response") + " is not a JSON object with response '"
                        + connection.getResponseCode() + ": " + connection.getResponseMessage() + "': " +
                        CharStreams.toString(new InputStreamReader(error ? connection.getErrorStream() : connection.getInputStream(), Charsets.UTF_8)));
            }
            return json.getAsJsonObject();
        }
    }

    /**
     * Sanitizes the provided {@link GameProfile} by removing unnecessary timestamp data
     * and caches the profile.
     *
     * @param profile The {@link GameProfile} to be sanitized.
     * @return The sanitized {@link GameProfile}.
     */
    @SuppressWarnings("deprecation")
    private static GameProfile sanitizeProfile(GameProfile profile) {
        JsonObject jsonObject = Optional.ofNullable(getSkinValue(profile)).map(XSkull::decodeBase64)
                .map((decoded) -> new JsonParser().parse(decoded).getAsJsonObject())
                .orElse(null);

        if (jsonObject == null || !jsonObject.has("timestamp")) return profile;
        JsonObject texture = new JsonObject();
        texture.add("textures", jsonObject.get("textures"));

        // The stored cache UUID must be according to online/offline servers.
        UUID id;
        if (isOnlineMode()) {
            id = profile.getId();
        } else {
            id = getOfflineUUID(profile.getName());
            ONLINE_TO_OFFLINE.put(profile.getId(), id);
        }

        GameProfile clone = new GameProfile(id, profile.getName());
        Property property = new Property("textures", encodeBase64(texture.toString()));
        PropertyMap properties = clone.getProperties();
        properties.put("textures", property);
        signXSkull(clone);
        return clone;
    }

    public static GameProfile signXSkull(GameProfile profile) {
        // Just as an indicator that this is not a vanilla-created profile.
        PropertyMap properties = profile.getProperties();
        properties.put(DEFAULT_PROFILE_NAME, XSKULL_GAMEPROFILE_SIGNATURE);
        return profile;
    }

    private static GameProfile createNamelessGameProfile(UUID id) {
        return signXSkull(new GameProfile(id, DEFAULT_PROFILE_NAME));
    }

    /**
     * Retrieves the default {@link GameProfile} used by XSkull.
     * This method creates a clone of the default profile to prevent modifications to the original.
     *
     * @return A clone of the default {@link GameProfile}.
     */
    private static GameProfile getDefaultProfile() {
        GameProfile clone = new GameProfile(DEFAULT_PROFILE.getId(), DEFAULT_PROFILE.getName());
        clone.getProperties().putAll(DEFAULT_PROFILE.getProperties());
        return clone;
    }

    /**
     * Retrieves the value of a {@link Property}, handling differences between versions.
     *
     * @param property The {@link Property} from which to retrieve the value.
     * @return The value of the {@link Property}.
     * @since 4.0.1
     */
    private static String getPropertyValue(Property property) {
        if (NULLABILITY_RECORD_UPDATE) return property.value();
        try {
            return (String) PROPERTY_GET_VALUE.invoke(property);
        } catch (Throwable throwable) {
            throw new RuntimeException("Unable to get a texture value: " + property, throwable);
        }
    }

    /**
     * Encodes the provided string into Base64 format.
     *
     * @param str The string to encode.
     * @return The Base64 encoded string.
     */
    private static String encodeBase64(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Tries to decode the string as a Base64 value.
     *
     * @param base64 The Base64 string to decode.
     * @return the decoded Base64 string if it is a valid Base64 string, or null if not.
     */
    private static String decodeBase64(String base64) {
        Objects.requireNonNull(base64, "Cannot decode null string");
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    /**
     * Represents an action that handles both asynchronous and synchronous workflows
     * based on a given {@link SkullInstruction}.
     *
     * @param <T> The type of the result produced by the action.
     */
    public static class SkullAction<T> {
        /**
         * The instruction that defines how the action is applied.
         */
        private final SkullInstruction<T> instruction;

        /**
         * Constructs a {@code SkullAction} with the specified instruction.
         *
         * @param instruction The instruction that defines how the action is applied.
         */
        protected SkullAction(SkullInstruction<T> instruction) {
            this.instruction = instruction;
        }

        /**
         * Sets the profile generated by the instruction to the result type synchronously.
         * This is recommended if your code is already not on the main thread, or if you know
         * that the skull texture doesn't need additional requests.
         *
         * <h2>What are these additional requests?</h2>
         * This only applies to offline mode (cracked) servers. Since these servers use
         * a cracked version of the player UUIDs and not their real ones, the real UUID
         * needs to be known by requesting it from Mojang servers and this request which
         * requires internet connection, will delay things a lot.
         *
         * @return The result after setting the generated profile.
         */
        public T apply() {
            try {
                GameProfile profile = instruction.supplier.get();
                return instruction.profileContainer.setProfile(profile);
            } catch (PlayerProfileNotFoundException | MojangAPIException ex) {
                if (!instruction.lenient) throw ex;
                LOGGER.debug("apply()", ex);
                return null;
            }
        }

        /**
         * Asynchronously applies the instruction to generate a {@link GameProfile} and returns a {@link CompletableFuture}.
         * This method is designed for non-blocking execution, allowing tasks to be performed
         * in the background without blocking the server's main thread.
         * This method will always execute async, even if the results are cached.
         * <p>
         * <h2>Reference Issues</h2>
         * Note that while these methods apply to the item/block instances, passing these instances
         * to certain methods, for example {@link org.bukkit.inventory.Inventory#setItem(int, ItemStack)}
         * will create a NMS copy of that instance and use that instead. Which means if for example
         * you're going to be using an item for an inventory, you'd have to set the item again
         * manually to the inventory once this method is done.
         * <pre>{@code
         * Inventory inventory = ...;
         * XSkull.createItem().profile(player).applyAsync()
         *     .thenAcceptAsync(item -> inventory.setItem(slot, item));
         * }</pre>
         *
         * <h2>Usage example:</h2>
         * <pre>{@code
         *   XSkull.createItem().profile(player).applyAsync()
         *      .thenAcceptAsync(result -> {
         *          // Additional processing...
         *      }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
         * }</pre>
         *
         * @return A {@link CompletableFuture} that will complete asynchronously.
         */
        public CompletableFuture<T> applyAsync() {
            return CompletableFuture.supplyAsync(this::apply, EXECUTOR);
        }
    }

    /**
     * Represents an instruction that sets a property of a {@link GameProfile}.
     * It uses a {@link Function} to define how to set the property.
     *
     * @param <T> The type of the result produced by the {@link #profileContainer} function.
     */
    public static class SkullInstruction<T> {
        /**
         * The function called that applies the given {@link #supplier} to an object that supports it
         * such as {@link ItemStack}, {@link SkullMeta} or a {@link BlockState}.
         */
        private final ProfileContainer<T> profileContainer;
        /**
         * The final texture that will be supplied to {@link #profileContainer} to be applied.
         */
        private Supplier<GameProfile> supplier;

        private boolean lenient = false;

        protected SkullInstruction(ProfileContainer<T> profileContainer) {
            this.profileContainer = profileContainer;
        }

        /**
         * Removes the profile and skin texture.
         */
        public T removeProfile() {
            return profileContainer.setProfile(null);
        }

        /**
         * Fails silently if any string based issues occur from a configuration standpoint.
         * Mainly affects {@link #profile(String)}
         */
        public SkullInstruction<T> lenient() {
            this.lenient = true;
            return this;
        }

        /**
         * Sets the skull texture based on a string. The input type is resolved based on the value provided.
         *
         * <h2>Valid Types</h2>
         * <b>Username:</b> A player username. (e.g. Notch)<br>
         * <b>UUID:</b> A player UUID. Offline or online mode UUID. (e.g. 069a79f4-44e9-4726-a5be-fca90e38aaf5)<br>
         * <b>Base64:</b> The Base64 encoded value of textures JSON. (e.g. eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2NmNjc2N2RkMzQ3MzdlOTliZDU0YjY5NWVmMDY4M2M2YzZjZTZhNTRmNjZhZDk3Mjk5MmJkMGU0OGU0NTc5YiJ9fX0=)<br>
         * <b>Minecraft Textures URL:</b> Check {@link SkullInputType#TEXTURE_URL}.<br>
         * <b>Minecraft Textures Hash:</b> Same as the URL, but only including the hash part, excluding the base URL. (e.g. e5461a215b325fbdf892db67b7bfb60ad2bf1580dc968a15dfb304ccd5e74db)
         *
         * @param input The input value used to retrieve the {@link GameProfile}. For more information check {@link SkullInputType}
         * @return A new {@link SkullAction} instance configured with this {@code SkullInstruction}.
         */
        public SkullAction<T> profile(String input) {
            this.supplier = () -> {
                SkullInputType type = SkullInputType.get(input);
                if (type == null) {
                    if (lenient) return getDefaultProfile();
                    else throw new IllegalArgumentException("Unknown skull string value: " + input);
                }
                return type.getProfile(input);
            };
            return new SkullAction<>(this);
        }

        /**
         * Sets the skull texture based on a string with a known type.
         *
         * @param type  The type of the input value.
         * @param input The input value to generate the {@link GameProfile}.
         * @return A new {@link SkullAction} instance configured with this {@code SkullInstruction}.
         */
        public SkullAction<T> profile(SkullInputType type, String input) {
            Objects.requireNonNull(type, () -> "Cannot profile from a null input type: " + input);
            this.supplier = () -> type.getProfile(input);
            return new SkullAction<>(this);
        }

        public GameProfile getProfile() {
            return profileContainer.getProfile();
        }

        public String getProfileString() {
            return profileContainer.getProfileValue();
        }

        /**
         * Sets the skull texture based on the specified player UUID.
         *
         * @param uuid The UUID to generate the {@link GameProfile}.
         * @return A new {@link SkullAction} instance configured with this {@code SkullInstruction}.
         */
        public SkullAction<T> profile(UUID uuid) {
            this.supplier = () -> XSkull.getProfile(uuid);
            return new SkullAction<>(this);
        }

        /**
         * Sets the skull texture based on the specified player.
         *
         * @param player The player to generate the {@link GameProfile}.
         * @return A new {@link SkullAction} instance configured with this {@code SkullInstruction}.
         */
        public SkullAction<T> profile(Player player) {
            // Why are we using the username instead of getting the cached UUID like profile(player.getUniqueId())?
            // If it's about online/offline mode support why should we have a separate method for this instead of
            // letting profile(OfflinePlayer) to take care of it?
            this.supplier = () -> SkullInputType.USERNAME.getProfile(player.getName());
            return new SkullAction<>(this);
        }

        /**
         * Sets the skull texture based on the specified offline player.
         * The profile lookup will depend on whether the server is running in online mode.
         *
         * @param offlinePlayer The offline player to generate the {@link GameProfile}.
         * @return A new {@link SkullAction} instance configured with this {@code SkullInstruction}.
         */
        public SkullAction<T> profile(OfflinePlayer offlinePlayer) {
            this.supplier = () ->
                    isOnlineMode()
                            ? XSkull.getProfile(offlinePlayer.getUniqueId())
                            : SkullInputType.USERNAME.getProfile(offlinePlayer.getName());
            return new SkullAction<>(this);
        }

        /**
         * Sets the skull texture based on the specified profile.
         * If the profile already has textures, it will be used directly. Otherwise, a new profile will be fetched
         * based on the UUID or username depending on the server's online mode.
         *
         * @param profile The profile to be used in the profile setting operation.
         * @return A new {@link SkullAction} instance configured with this {@code SkullInstruction}.
         */
        public SkullAction<T> profile(GameProfile profile) {
            if (hasTextures(profile)) {
                this.supplier = () -> profile;
                return new SkullAction<>(this);
            }

            this.supplier = () -> isOnlineMode()
                    ? XSkull.getProfile(profile.getId())
                    : SkullInputType.USERNAME.getProfile(profile.getName());
            return new SkullAction<>(this);
        }
    }

    /**
     * An executor service with a fixed thread pool of size 2, used for asynchronous operations.
     */
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2, new PlayerTextureThread());

    private static final class PlayerTextureThread implements ThreadFactory {
        private static final AtomicInteger COUNT = new AtomicInteger();

        @Override
        public Thread newThread(@Nonnull final Runnable run) {
            final Thread thread = new Thread(run);
            thread.setName("Profile Lookup Executor #" + COUNT.getAndIncrement());
            thread.setUncaughtExceptionHandler((t, throwable) ->
                    LOGGER.error("Uncaught exception in thread {}", t.getName(), throwable));
            return thread;
        }
    }

    /**
     * The {@code SkullInputType} enum represents different types of input patterns that can be used for identifying
     * and validating various formats such as texture hashes, URLs, Base64 encoded strings, UUIDs, and usernames.
     */
    @SuppressWarnings("JavadocLinkAsPlainText")
    public enum SkullInputType {
        /**
         * Represents a texture hash pattern.
         * Mojang hashes length are inconsistent, and they don't seem to use uppercase characters.
         * <p>
         * Currently, the shortest observed hash value is (lenght: 57): 0a4050e7aacc4539202658fdc339dd182d7e322f9fbcc4d5f99b5718a
         * <p>
         * Example: e5461a215b325fbdf892db67b7bfb60ad2bf1580dc968a15dfb304ccd5e74db
         */
        TEXTURE_HASH(Pattern.compile("[0-9a-z]{55,70}")) {
            @Override
            public GameProfile getProfile(String textureHash) {
                String base64 = encodeBase64(TEXTURES_NBT_PROPERTY_PREFIX + TEXTURES_BASE_URL + textureHash + "\"}}}");
                return profileFromHashAndBase64(textureHash, base64);
            }
        },

        /**
         * Represents a texture URL pattern that includes the base URL followed by the texture hash pattern.
         * <p>
         * Example: http://textures.minecraft.net/texture/e5461a215b325fbdf892db67b7bfb60ad2bf1580dc968a15dfb304ccd5e74db
         */
        TEXTURE_URL(Pattern.compile(Pattern.quote(TEXTURES_BASE_URL) + "(?<hash>" + TEXTURE_HASH.pattern + ')')) {
            @Override
            public GameProfile getProfile(String textureUrl) {
                String hash = extractTextureHash(textureUrl);
                return TEXTURE_HASH.getProfile(hash);
            }
        },

        /**
         * Represents a Base64 encoded string pattern.
         * The base64 pattern that's checked is not a general base64 pattern, but a pattern that
         * closely represents the base64 genereated by the NBT data.
         */
        BASE64(Pattern.compile("[-A-Za-z0-9+/]{100,}={0,3}")) {
            @Override
            public GameProfile getProfile(String base64) {
                return Optional.ofNullable(decodeBase64(base64))
                        .map(SkullInputType::extractTextureHash)
                        .map((hash) -> profileFromHashAndBase64(hash, base64))
                        .orElseGet(XSkull::getDefaultProfile);
            }
        },

        /**
         * Represents a UUID pattern, following the standard UUID format.
         */
        UUID(Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", Pattern.CASE_INSENSITIVE)) {
            @Override
            public GameProfile getProfile(String uuidString) {
                return XSkull.getProfile(java.util.UUID.fromString(uuidString));
            }
        },

        /**
         * Represents a username pattern, allowing alphanumeric characters and underscores, with a length of 1 to 16 characters.
         * Minecraft now requires the username to be at least 3 characters long, but older accounts are still around.
         * It also seems that there are a few inactive accounts that use spaces in their usernames?
         */
        USERNAME(Pattern.compile("[A-Za-z0-9_]{1,16}")) {
            @Override
            public GameProfile getProfile(String username) {
                GameProfile profile = getCachedProfileByUsername(username);
                if (hasTextures(profile)) return profile;
                return fetchProfile(profile); // The given GameProfile doesn't have any skin information.
            }
        };

        /**
         * The regex pattern associated with the input type.
         */
        private final Pattern pattern;
        private static final SkullInputType[] VALUES = values();

        /**
         * Constructs a {@code SkullInputType} with the specified regex pattern.
         *
         * @param pattern The regex pattern associated with the input type.
         */
        SkullInputType(Pattern pattern) {
            this.pattern = pattern;
        }

        /**
         * Retrieves a {@link GameProfile} based on the provided input string.
         *
         * @param input The input string to retrieve the profile for.
         * @return The {@link GameProfile} corresponding to the input string.
         */
        public abstract GameProfile getProfile(String input);

        /**
         * Returns the corresponding {@code SkullInputType} for the given identifier, if it matches any pattern.
         *
         * @param identifier The string to be checked against the patterns.
         * @return The matching {@code InputType}, or {@code null} if no match is found.
         */
        @Nullable
        public static SkullInputType get(@Nonnull String identifier) {
            Objects.requireNonNull(identifier, "Identifier cannot be null");
            return Arrays.stream(VALUES)
                    .filter(value -> value.pattern.matcher(identifier).matches())
                    .findFirst().orElse(null);
        }

        /**
         * Constructs a {@link GameProfile} using the provided texture hash and base64 string.
         *
         * @param hash   The texture hash used to construct the profile's textures.
         * @param base64 The base64 string representing the profile's textures.
         * @return The constructed {@link GameProfile}.
         * @implNote This method creates a {@link GameProfile} with a UUID derived from the provided hash
         *           to ensure consistency after restarts.
         */
        private static GameProfile profileFromHashAndBase64(String hash, String base64) {
            UUID uuid = java.util.UUID.nameUUIDFromBytes(hash.getBytes(StandardCharsets.UTF_8));
            GameProfile profile = createNamelessGameProfile(uuid);
            profile.getProperties().put("textures", new Property("textures", base64));
            return profile;
        }

        /**
         * Extracts the texture hash from the provided input string.
         * <p>
         * Will not work reliably if NBT is passed: {"textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/74133f6ac3be2e2499a784efadcfffeb9ace025c3646ada67f3414e5ef3394"}}}
         *
         * @param input The input string containing the texture hash.
         * @return The extracted texture hash.
         */
        private static String extractTextureHash(String input) {
            Matcher matcher = SkullInputType.TEXTURE_HASH.pattern.matcher(input);
            return matcher.find() ? matcher.group() : null;
        }
    }
}
