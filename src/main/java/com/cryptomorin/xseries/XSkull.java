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
import com.cryptomorin.xseries.reflection.minecraft.MinecraftClassHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>SkullUtils</b> - Apply skull texture from different sources.<br>
 * Skull Meta: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/meta/SkullMeta.html
 * Mojang API: https://wiki.vg/Mojang_API
 * <p>
 * Some websites to get custom heads:
 * <ul>
 *     <li>https://minecraft-heads.com/</li>
 * </ul>
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
 * @version 8.0.0
 * @see XMaterial
 * @see XReflection
 */
public final class XSkull {
    protected static final MethodHandle
            CRAFT_META_SKULL_PROFILE_GETTER, CRAFT_META_SKULL_PROFILE_SETTER,
            CRAFT_META_SKULL_BLOCK_SETTER, PROPERTY_GETVALUE;

    /**
     * Some people use this without quotes surrounding the keys, not sure what that'd work.
     */
    private static final String VALUE_PROPERTY = "{\"textures\":{\"SKIN\":{\"url\":\"";
    public static final boolean SUPPORTS_UUID = XReflection.supports(12);

    /**
     * We'll just return an x shaped hardcoded skull.
     * https://minecraft-heads.com/custom-heads/miscellaneous/58141-cross
     */
    private static final String INVALID_SKULL_VALUE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzEwNTkxZTY5MDllNmEyODFiMzcxODM2ZTQ2MmQ2N2EyYzc4ZmEwOTUyZTkxMGYzMmI0MWEyNmM0OGMxNzU3YyJ9fX0=";

    /**
     * They don't seem to use anything complicated, but the length is inconsistent for some reason.
     * It doesn't seem like uppercase characters are used either.
     */
    private static final Pattern MOJANG_SHA256_APPROX = Pattern.compile("[0-9a-z]{55,70}");
    private static final Pattern UUID_NO_DASHES = Pattern.compile("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})");

    private static final AtomicLong MOJANG_SHA_FAKE_ID_ENUMERATOR = new AtomicLong(1);

    /**
     * The ID and name of the GameProfiles are immutable, so we're good to cache them.
     * The key is the SHA value.
     */
    private static final Map<String, GameProfile> MOJANG_SHA_FAKE_PROFILES = new HashMap<>();
    private static final Map<String, GameProfile> MOJANG_SHA_PROFILES = new HashMap<>();
    private static final Map<String, String> VALID_HASHES_BY_NAME = new HashMap<>();
    private static final Map<UUID, String> VALID_HASHES_BY_UUID = new HashMap<>();
    private static final Map<UUID, UUID> REAL_UUID = new HashMap<>();

    /**
     * In v1.20.2 there were some changes to the Mojang API.
     * Before that version, both UUID and name fields couldn't be null, only one of them.
     * It gave the error: {@code Name and ID cannot both be blank}
     * Here, "blank" is null for UUID, and {@code Character.isWhitespace} for the name field.
     */
    private static final boolean NULLABILITY_RECORD_UPDATE = XReflection.supports(20, 2);
    private static final UUID IDENTITY_UUID = new UUID(0, 0);
    private static final GameProfile NULL_PROFILE = new GameProfile(IDENTITY_UUID, "");
    /**
     * Does using a random UUID have any advantage?
     */
    private static final UUID GAME_PROFILE_EMPTY_UUID = NULLABILITY_RECORD_UPDATE ? IDENTITY_UUID : null;
    private static final String GAME_PROFILE_EMPTY_NAME = NULLABILITY_RECORD_UPDATE ? "" : null;

    /**
     * The value after this URL is probably an SHA-252 value that Mojang uses to unique identify player skins.
     * <br>
     * This <a href="https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin/Cape">wiki</a> documents how to
     * get base64 information from player's UUID.
     */
    private static final String TEXTURES = "https://textures.minecraft.net/texture/";

    static {
        MethodHandle profileSetter = null, profileGetter = null, propGetval = null;

        try {
            MinecraftClassHandle CraftMetaSkull = XReflection.ofMinecraft()
                    .inPackage(MinecraftPackage.CB, "inventory")
                    .named("CraftMetaSkull");
            profileGetter = CraftMetaSkull.getterField().named("profile").returns(GameProfile.class).makeAccessible().reflect();

            try {
                // https://github.com/CryptoMorin/XSeries/issues/169
                profileSetter = CraftMetaSkull.method().named("setProfile").returns(GameProfile.class).makeAccessible().reflect();
            } catch (NoSuchMethodException e) {
                profileSetter = CraftMetaSkull.setterField().named("profile").returns(GameProfile.class).makeAccessible().reflect();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        MinecraftClassHandle CraftSkull = XReflection.ofMinecraft()
                .inPackage(MinecraftPackage.CB, "block")
                .named("CraftSkull");

        if (!XReflection.supports(20, 2)) {
            propGetval = XReflection.of(Property.class).method().named("getValue").returns(String.class).unreflect();
        }

        PROPERTY_GETVALUE = propGetval;
        CRAFT_META_SKULL_PROFILE_SETTER = profileSetter;
        CRAFT_META_SKULL_PROFILE_GETTER = profileGetter;
        CRAFT_META_SKULL_BLOCK_SETTER = CraftSkull.setterField().named("profile").returns(GameProfile.class).makeAccessible().unreflect(); // CraftSkull private final GameProfile profile;
    }

    @Nonnull
    public static ItemStack getSkull(@Nonnull UUID id) {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        OfflinePlayer player = Bukkit.getOfflinePlayer(id);
        meta = applySkin(meta, player);
        head.setItemMeta(meta);

        return head;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    public static SkullMeta applySkin(@Nonnull ItemMeta head, @Nonnull OfflinePlayer identifier) {
        SkullMeta meta = (SkullMeta) head;

        if (isOnlineMode()) {
            if (SUPPORTS_UUID) {
                meta.setOwningPlayer(identifier);
            } else {
                meta.setOwner(identifier.getName());
            }
        } else {
            setProfileDirectly(meta, detectProfileFromString(identifier.getUniqueId().toString()));
        }

        return meta;
    }

    public static UUID getOfflineUUID(OfflinePlayer player) {
        // Vanilla behavior across all platforms.
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(StandardCharsets.UTF_8));
    }

    @ApiStatus.Experimental
    public static UUID getRealUUIDOfPlayer(UUID uuid) {
        if (isOnlineMode()) return uuid;
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (!player.hasPlayedBefore()) throw new RuntimeException("Player with UUID " + uuid + " doesn't exist.");
        UUID offlineUUID = getOfflineUUID(player);
        if (!offlineUUID.equals(uuid)) {
            throw new RuntimeException("Expected offline UUID for player doesn't match: Expected " + uuid + ", got " + offlineUUID + " for " + player);
        }
        try {
            UUID realUUID = REAL_UUID.get(uuid);
            if (realUUID == null) {
                realUUID = UUIDFromName(player.getName());
                REAL_UUID.put(uuid, realUUID);
            }
            return realUUID;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    private static SkullMeta applySkin(SkullMeta head, UUID uuid) {
        String cachedHash = VALID_HASHES_BY_UUID.get(uuid);
        if (cachedHash != null) {
            if (isNullIndicatingString(cachedHash)) return head;
            setProfileDirectly(head, MOJANG_SHA_PROFILES.get(cachedHash));
            return head;
        }
        try {
            SkullProfile profile = getSkullProfileFromId(uuid);
            VALID_HASHES_BY_UUID.put(uuid, profile.MojangSHA);
            setProfileDirectly(head, profile.gameProfile);
        } catch (IOException e) {
            VALID_HASHES_BY_UUID.put(uuid, "");
            e.printStackTrace();
        }

        return head;
    }

    @Nonnull
    public static SkullMeta applySkinFromBase64(@Nonnull SkullMeta head, @Nonnull String value, String MojangSHA) {
        if (value == null || value.isEmpty()) throw new IllegalArgumentException("Skull value cannot be null or empty");
        GameProfile profile = profileFromBase64(value, MojangSHA);
        setProfileDirectly(head, profile);
        return head;
    }

    @Nonnull
    public static SkullMeta applySkinFromUsername(SkullMeta head, String name) {
        String hash = VALID_HASHES_BY_NAME.get(name);
        if (hash != null) {
            if (isNullIndicatingString(hash)) return head;
            setProfileDirectly(head, MOJANG_SHA_PROFILES.get(hash));
            return head;
        }
        try {
            UUID uuid = UUIDFromName(name);
            SkullProfile profile = getSkullProfileFromId(uuid);
            setProfileDirectly(head, profile.gameProfile);
            VALID_HASHES_BY_NAME.put(name, hash);
        } catch (IOException e) {
            VALID_HASHES_BY_NAME.put(name, "");
            e.printStackTrace();
        }
        return head;

        // GameProfile nullPlayer = NULL_PLAYERS.get(name);
        // if (nullPlayer == NULL_PROFILE) {
        //     // There's no point in changing anything.
        //     return head;
        // } else {
        //     // CraftServer#getOfflinePlayer() trick
        //     OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        //     UUID nullUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        //
        //     if (player.getUniqueId().equals(nullUUID)) {
        //         NULL_PLAYERS.put(name, NULL_PROFILE);
        //         return head;
        //     } else {
        //         return applySkin(head, player);
        //     }
        // }
    }

    private static boolean isOnlineMode() {
        return Bukkit.getOnlineMode();
    }

    @Nonnull
    private static UUID UUIDFromName(String name) throws IOException {
        JsonObject userJson = MojangAPI.USERNAME_TO_UUID.request(name);
        JsonElement idElement = userJson.get("id");
        if (idElement == null)
            throw new RuntimeException("No 'id' field for UUID request for '" + name + "': " + userJson);

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

    private static boolean isNullIndicatingString(String str) {
        return str.isEmpty();
    }

    /**
     * Setting the profile directly is not compatible with {@link SkullMeta#setOwningPlayer(OfflinePlayer)}
     * and should be reset with {@code setProfile(head, null)} before anything.
     * <p>
     * It seems like the Profile is prioritized over UUID/name.
     */
    public static void setProfileDirectly(SkullMeta head, GameProfile profile) {
        try {
            CRAFT_META_SKULL_PROFILE_SETTER.invoke(head, profile);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Nonnull
    public static GameProfile profileFromBase64(String base64, String MojangSHA) {
        // Use an empty string instead of null for the name parameter because it's now null-checked since 1.20.2.
        // It doesn't seem to affect functionality.
        GameProfile gp = MOJANG_SHA_FAKE_PROFILES.get(MojangSHA);
        if (gp != null) return gp;

        gp = new GameProfile(
                NULLABILITY_RECORD_UPDATE ? GAME_PROFILE_EMPTY_UUID : new UUID(MOJANG_SHA_FAKE_ID_ENUMERATOR.getAndIncrement(), 0), // UUID.randomUUID()
                GAME_PROFILE_EMPTY_NAME);
        gp.getProperties().put("textures", new Property("textures", base64));
        MOJANG_SHA_FAKE_PROFILES.put(MojangSHA, gp);
        return gp;
    }

    @Nonnull
    public static GameProfile profileFromPlayer(OfflinePlayer player) {
        return new GameProfile(player.getUniqueId(), player.getName());
    }


    /**
     * @param identifier Can be a player name, player UUID, Base64, or a minecraft.net skin link.
     */
    @Nonnull
    public static SkullMeta applySkin(@Nonnull ItemMeta head, @Nonnull String identifier) {
        SkullMeta meta = (SkullMeta) head;
        // @formatter:off
        SkullValue result = detectSkullValueType(identifier);
        switch (result.valueType) {
            case UUID:         return applySkin(meta, (UUID) result.object);
            case NAME:         return applySkinFromUsername(meta, identifier);
            case BASE64:       return applySkinFromBase64(meta, identifier,                               extractMojangSHAFromBase64((String) result.object));
            case TEXTURE_URL:  return applySkinFromBase64(meta, encodeTexturesURL(identifier),            extractMojangSHAFromBase64(identifier));
            case TEXTURE_HASH: return applySkinFromBase64(meta, encodeTexturesURL(TEXTURES + identifier), identifier);
            case UNKNOWN:      return applySkinFromBase64(meta, INVALID_SKULL_VALUE,                      INVALID_SKULL_VALUE);
            default: throw new AssertionError("Unknown skull value");
        }
        // @formatter:on
    }

    @Nonnull
    public static GameProfile detectProfileFromString(String identifier) {
        // @formatter:off sometimes programming is just art that a machine can't understand :)
        SkullValue result = detectSkullValueType(identifier);
        switch (result.valueType) {
            case NAME:         return profileFromUsername(                                        (String) result.object);
            case UUID:         return profileFromUUID(                                            (UUID)   result.object);
            case BASE64:       return profileFromBase64(                             identifier,  (String) result.object);
            case TEXTURE_URL:  return profileFromBase64(encodeTexturesURL(           identifier), (String) result.object);
            case TEXTURE_HASH: return profileFromBase64(encodeTexturesURL(TEXTURES + identifier), identifier);
            case UNKNOWN:      return profileFromBase64(INVALID_SKULL_VALUE,                      INVALID_SKULL_VALUE); // This can't be cached because the caller might change it.
            default: throw new AssertionError("Unknown skull value");
        }
        // @formatter:on
    }

    private static GameProfile profileFromUUID(UUID uuid) {
        return new GameProfile(getRealUUIDOfPlayer(uuid), GAME_PROFILE_EMPTY_NAME);
    }

    private static GameProfile profileFromUsername(String username) {
        return new GameProfile(GAME_PROFILE_EMPTY_UUID, username);
    }

    @Nonnull
    public static SkullValue detectSkullValueType(@Nonnull String identifier) {
        try {
            UUID id = UUID.fromString(identifier);
            return new SkullValue(ValueType.UUID, id);
        } catch (IllegalArgumentException ignored) {
        }

        if (isUsername(identifier)) return new SkullValue(ValueType.NAME, identifier);
        if (identifier.contains("textures.minecraft.net")) {
            return new SkullValue(ValueType.TEXTURE_URL, extractMojangSHAFromBase64(identifier));
        }
        if (identifier.length() > 100) {
            String decoded = decodeBase64(identifier, false);
            if (decoded != null) return new SkullValue(ValueType.BASE64, decoded);
        }

        // We'll just "assume" that it's a textures.minecraft.net hash without the URL part.
        if (MOJANG_SHA256_APPROX.matcher(identifier).matches())
            return new SkullValue(ValueType.TEXTURE_HASH, identifier);

        return new SkullValue(ValueType.UNKNOWN, identifier);
    }

    public static void setSkin(@Nonnull Block block, @Nonnull String value) {
        Objects.requireNonNull(block, "Can't set skin of null block");
        BlockState state = block.getState();
        setSkin(state, value);
        state.update(true);
    }

    public static void setSkin(@Nonnull BlockState state, @Nonnull String value) {
        setSkin(state, detectProfileFromString(value));
    }

    public static void setSkin(@Nonnull BlockState state, @Nonnull GameProfile profile) {
        Objects.requireNonNull(state, "Can't set skin of null block state");
        Objects.requireNonNull(profile, "Can't set skin of block with null GameProfile");

        if (!(state instanceof Skull))
            throw new IllegalArgumentException("Cannot set skin of a block that is not a skull: " + state);
        Skull skull = (Skull) state;

        try {
            CRAFT_META_SKULL_BLOCK_SETTER.invoke(skull, profile);
        } catch (Throwable e) {
            throw new RuntimeException("Error while setting block skin with value: " + state + " with profiel " + profile, e);
        }
    }

    public static String encodeTexturesURL(String url) {
        // String.format bad!
        return encodeBase64(VALUE_PROPERTY + url + "\"}}}");
    }

    @Nonnull
    private static String encodeBase64(@Nonnull String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Tries to decode the string as a Base64 value.
     * @return the decoded Base64 string if it is a Base64 string, otherwise null.
     * @param error whether to generate an error instead of returning null if string is not Base64.
     */
    private static String decodeBase64(@Nonnull String base64, boolean error) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            if (error) throw ex;
            else return null;
        }
        // return BASE64.matcher(base64).matches();
    }

    @Nullable
    public static String getSkinValue(@Nonnull ItemMeta skull) {
        Objects.requireNonNull(skull, "Skull ItemStack cannot be null");
        SkullMeta meta = (SkullMeta) skull;
        GameProfile profile = null;

        try {
            profile = (GameProfile) CRAFT_META_SKULL_PROFILE_GETTER.invoke(meta);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        if (profile != null && !profile.getProperties().get("textures").isEmpty()) {
            for (Property property : profile.getProperties().get("textures")) {
                String value = getPropertyValue(property);
                if (!value.isEmpty()) return value;
            }
        }

        return null;
    }

    /**
     * They changed {@link Property} to a Java record in 1.20.2
     *
     * @since 4.0.1
     */
    private static String getPropertyValue(Property property) {
        if (NULLABILITY_RECORD_UPDATE) {
            return property.value();
        } else {
            try {
                return (String) PROPERTY_GETVALUE.invoke(property);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String extractMojangSHAFromBase64(String decodedBase64) {
        // Example: http://textures.minecraft.net/texture/e5461a215b325fbdf892db67b7bfb60ad2bf1580dc968a15dfb304ccd5e74db
        // Will not work reliably if NBT is passed: {"textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/74133f6ac3be2e2499a784efadcfffeb9ace025c3646ada67f3414e5ef3394"}}}
        Matcher matcher = MOJANG_SHA256_APPROX.matcher(decodedBase64);
        if (matcher.find()) return matcher.group();
        else throw new IllegalArgumentException("Invalid Base64 skull value: " + decodedBase64);
    }

    public static final class SkullValue {
        public final ValueType valueType;
        public final Object object;

        private SkullValue(ValueType valueType, Object object) {
            this.valueType = valueType;
            this.object = object;
        }
    }

    /**
     * https://help.minecraft.net/hc/en-us/articles/360034636712
     *
     * @param name the username to check.
     * @return true if the string matches the Minecraft username rule, otherwise false.
     */
    private static boolean isUsername(@Nonnull String name) {
        int len = name.length();
        if (len > 16) return false; // Yes, in the old Minecraft 1 letter usernames were a thing.

        // For some reason Apache's Lists.charactersOf is faster than character indexing for small strings.
        for (char ch : Lists.charactersOf(name)) {
            if (ch != '_' && !(ch >= 'A' && ch <= 'Z') && !(ch >= 'a' && ch <= 'z') && !(ch >= '0' && ch <= '9'))
                return false;
        }
        return true;
    }

    public enum ValueType {NAME, UUID, BASE64, TEXTURE_URL, TEXTURE_HASH, UNKNOWN}

    @Nonnull
    private static SkullProfile createProfile(UUID uuid, String name, String base64, String MojangSHA) {
        GameProfile gameProfile = new GameProfile(uuid, name);
        gameProfile.getProperties().put("textures", new Property("textures", base64));
        MOJANG_SHA_PROFILES.put(MojangSHA, gameProfile);
        return new SkullProfile(gameProfile, uuid, name, base64, MojangSHA);
    }

    public static final class SkullProfile {
        public final GameProfile gameProfile;
        public final UUID uuid;
        public final String name, base64, MojangSHA;

        public SkullProfile(GameProfile gameProfile, UUID uuid, String name, String base64, String MojangSHA) {
            this.gameProfile = gameProfile;
            this.uuid = uuid;
            this.name = name;
            this.base64 = base64;
            this.MojangSHA = MojangSHA;
        }
    }

    private static SkullProfile getSkullProfileFromId(UUID uuid) throws IOException {
        JsonObject profileJson = MojangAPI.UUID_TO_PROFILE.request(uuid.toString());
        String name = profileJson.get("name").getAsString();
        for (JsonElement element : profileJson.get("properties").getAsJsonArray()) {
            JsonObject property = element.getAsJsonObject();
            if (!"textures".equals(property.get("name").getAsString())) continue;

            String base64 = property.get("value").getAsString();
            String mojangSHA = extractMojangSHAFromBase64(decodeBase64(base64, true));
            return createProfile(uuid, name, base64, mojangSHA);
        }
        throw new IllegalStateException("Player with UUID " + uuid + " does not have a texture");
    }

    /**
     * https://wiki.vg/Mojang_API
     */
    private enum MojangAPI {
        USERNAME_TO_UUID("https://api.mojang.com/users/profiles/minecraft/"),
        UUID_TO_PROFILE("https://sessionserver.mojang.com/session/minecraft/profile/");

        private final String url;

        MojangAPI(String url) {
            this.url = url;
        }

        public JsonObject request(String finalEntry) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) new URL(url + finalEntry).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10 * 1000); // 10 seconds
            connection.setReadTimeout(30 * 1000); // 30 seconds
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            try (
                    InputStream inputStream = connection.getInputStream();
                    JsonReader reader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            ) {
                JsonElement json = Streams.parse(reader);
                if (json == null || !json.isJsonObject()) {
                    // For UUID_TO_PROFILE, this happens when HTTP Code 204 (No Content) is given.
                    // And that happens if the UUID doesn't exist in Mojang servers. (E.g. cracked UUIDs)
                    throw new RuntimeException("Response from '" + connection.getURL() + "' is not a JSON object with response '"
                            + connection.getResponseCode() + ": " + connection.getResponseMessage() + "': " +
                            CharStreams.toString(new InputStreamReader(connection.getInputStream(), Charsets.UTF_8)));
                }
                return json.getAsJsonObject();
            } catch (IOException ex) {
                InputStream errorStream = connection.getErrorStream();
                String error = errorStream == null ?
                        connection.getResponseCode() + ": " + connection.getResponseMessage() :
                        CharStreams.toString(new InputStreamReader(errorStream, Charsets.UTF_8));
                throw new IOException(connection.getURL() + " -> " + error, ex);
            }
        }
    }
}
