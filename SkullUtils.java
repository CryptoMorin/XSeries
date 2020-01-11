/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Crypto Morin
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
import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/*
 * References
 *
 * * * GitHub: https://github.com/CryptoMorin/XSeries/blob/master/SkullUtils.java
 * * XSeries: https://www.spigotmc.org/threads/378136/
 * Skull Meta: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/meta/SkullMeta.html
 * Mojang API: https://wiki.vg/Mojang_API
 */

/**
 * <b>SkullUtils</b> - Apply skull texture from different sources.<br>
 * Supports 1.8+
 *
 * @author Crypto Morin
 * @version 1.0.0
 * @see XMaterial
 */
public class SkullUtils {
    private static final String VALUE_PROPERTY = "{\"textures\":{\"SKIN\":{\"url\":\"";
    private static final String TEXTURES = "https://textures.minecraft.net/texture/";
    private static final String SESSION = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final Pattern BASE64 = Pattern.compile("(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?");
    private static final Pattern USERNAME = Pattern.compile("[A-z0-9]+");

    @SuppressWarnings("deprecation")
    public static ItemStack getSkull(UUID id) {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (XMaterial.isNewVersion()) meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
        else meta.setOwner(id.toString());

        head.setItemMeta(meta);
        return head;
    }

    @SuppressWarnings("deprecation")
    public static SkullMeta applySkin(ItemMeta head, String player) {
        boolean isId = isUUID(getFullUUID(player));
        SkullMeta meta = (SkullMeta) head;

        if (isId || isUsername(player)) {
            if (isId) return getSkullByValue(meta, getSkinValue(player, true));
            if (XMaterial.isNewVersion()) meta.setOwningPlayer(Bukkit.getOfflinePlayer(player));
            else meta.setOwner(player);
        }

        if (player.contains("textures.minecraft.net")) return getValueFromTextures(meta, player);
        if (player.length() > 100 && isBase64(player)) return getSkullByValue(meta, player);
        return getTexturesFromUrlValue(meta, player);
    }

    public static SkullMeta getSkullByValue(SkullMeta head, String value) {
        Validate.notEmpty(value, "Skull value cannot be null or empty");
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);

        profile.getProperties().put("textures", new Property("textures", value));
        try {
            Field profileField = head.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(head, profile);
        } catch (SecurityException | NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        return head;
    }

    public static SkullMeta getValueFromTextures(SkullMeta head, String url) {
        return getSkullByValue(head, encodeBase64(VALUE_PROPERTY + url + "\"}}}"));
    }

    public static SkullMeta getTexturesFromUrlValue(SkullMeta head, String urlValue) {
        return getValueFromTextures(head, TEXTURES + urlValue);
    }

    private static String encodeBase64(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    private static boolean isBase64(String base64) {
        return BASE64.matcher(base64).matches();
    }

    public static String getSkinValue(ItemStack skull) {
        Objects.requireNonNull(skull, "Skull ItemStack cannot be null");
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        GameProfile profile = null;

        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profile = (GameProfile) profileField.get(meta);
        } catch (SecurityException | NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        if (profile != null && !profile.getProperties().get("textures").isEmpty())
            for (Property property : profile.getProperties().get("textures"))
                if (!property.getValue().isEmpty())
                    return property.getValue();

        return null;
    }

    @Nullable
    private static String getFullUUID(@Nullable String id) {
        if (Strings.isNullOrEmpty(id)) return id;
        if (id.length() == 36) return id;
        if (id.length() != 32) return id;

        return id.substring(0, 8) +
                "-" + id.substring(8, 12) +
                "-" + id.substring(12, 16) +
                "-" + id.substring(16, 20) +
                "-" + id.substring(20, 32);
    }

    private static boolean isUUID(@Nullable String id) {
        if (Strings.isNullOrEmpty(id)) return false;
        return id.length() == 36 && StringUtils.countMatches(id, "-") == 4;
    }

    private static boolean isUsername(@Nullable String name) {
        if (Strings.isNullOrEmpty(name)) return false;
        return name.length() >= 3 && name.length() <= 16 && USERNAME.matcher(name).matches();
    }

    /**
     * https://api.mojang.com/users/profiles/minecraft/<Username> -> ID
     * https://api.mojang.com/user/profiles/<ID without dashes ->/names
     * https://sessionserver.mojang.com/session/minecraft/profile/<ID> ->
     * <p>
     * {
     * "id": "Without dashes -",
     * "name": "",
     * "properties": [
     * {
     * "name": "textures",
     * "value": ""
     * }
     * ]
     * }
     */
    public static String getSkinValue(String name, boolean isId) {
        Validate.notEmpty(name, "Player name/UUID cannot be null or empty");

        try {
            String uuid;
            JsonParser parser = new JsonParser();

            if (!isId) {
                URL convertName = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
                InputStreamReader readId = new InputStreamReader(convertName.openStream());
                JsonObject jObject = parser.parse(readId).getAsJsonObject();
                if (mojangError(jObject)) return null;
                uuid = jObject.get("id").getAsString();
            } else uuid = StringUtils.remove(name, '-');

            URL properties = new URL(SESSION + uuid); // + "?unsigned=false"
            InputStreamReader readProperties = new InputStreamReader(properties.openStream());
            JsonObject jObjectP = parser.parse(readProperties).getAsJsonObject();

            if (mojangError(jObjectP)) return null;
            JsonObject textureProperty = jObjectP.get("properties").getAsJsonArray().get(0).getAsJsonObject();
            //String signature = textureProperty.get("signature").getAsString();
            return textureProperty.get("value").getAsString();
        } catch (IOException | IllegalStateException e) {
            System.err.println("Could not get skin data from session servers! " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static boolean mojangError(JsonObject jsonObject) {
        if (!jsonObject.has("error")) return false;

        String err = jsonObject.get("error").getAsString();
        String msg = jsonObject.get("errorMessage").getAsString();
        System.err.println("Mojang Error " + err + ": " + msg);
        return true;
    }
}
