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

package com.cryptomorin.xseries.test;

import com.cryptomorin.xseries.*;
import com.cryptomorin.xseries.art.XArt;
import com.cryptomorin.xseries.base.XBase;
import com.cryptomorin.xseries.base.XRegistry;
import com.cryptomorin.xseries.messages.ActionBar;
import com.cryptomorin.xseries.messages.Titles;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.mojang.MojangAPI;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import com.cryptomorin.xseries.profiles.objects.transformer.ProfileTransformer;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.test.reflection.ReflectionTests;
import com.cryptomorin.xseries.test.reflection.ReflectiveConstraintTests;
import com.cryptomorin.xseries.test.reflection.asm.ASMTests;
import com.cryptomorin.xseries.test.reflection.proxy.ProxyTests;
import com.cryptomorin.xseries.test.server.FakePlayerFactory;
import com.cryptomorin.xseries.test.util.ResourceHelper;
import com.cryptomorin.xseries.test.writer.ClassConverter;
import com.cryptomorin.xseries.test.writer.DifferenceHelper;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.cryptomorin.xseries.test.util.XLogger.log;
import static org.junit.jupiter.api.Assertions.*;

public final class XSeriesTests {
    // @Test
    public void enumToRegistry() throws URISyntaxException {
        URL resource = XSeriesTests.class.getResource("XEnchantment.java");
        Path path = Paths.get(resource.toURI());
        ClassConverter.enumToRegistry(path, Constants.getTestPath());
    }

    public static void test() {
        log("\n\n\nTest begin...");

        // testPlayerDependantTasks();

        log("Writing enum differences...");
        DifferenceHelper.versionDifference();

        testRegistry();
        testXMaterial();
        testXSound();
        testXPotion();
        testXEnchantment();
        testXItemStack();
        testXAttribute();
        testXParticle();

        testXTag();
        wrapperTest();
        testReflection();

        if (Constants.TEST_MOJANG_API) testSkulls();
        else {
            try {
                Class.forName("com.cryptomorin.xseries.profiles.mojang.MojangAPI");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Cannot find MojangAPI class", e);
            }
        }

        log("\n\n\nTest end...");
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> T findEntity(Predicate<Entity> entity) {
        return Constants.getMainWorld().getEntities().stream()
                .filter(entity)
                .map(x -> (T) x)
                .findFirst()
                .orElse(null);
    }

    private static Location getCenterOfChunk(Chunk c) {
        Location center = new Location(c.getWorld(), c.getX() << 4, 64, c.getZ() << 4).add(8, 0, 8);
        center.setY(center.getWorld().getHighestBlockYAt(center) + 1);
        return center;
    }

    private static void testRegistry() {
        assertNotNull(XRegistry.registryOf(XSound.class));
        assertNotNull(XRegistry.rawRegistryOf(XSound.class));
        assertNotNull(XRegistry.registryOf(XAttribute.class));
        assertNotNull(XRegistry.rawRegistryOf(XAttribute.class));
    }

    private static void wrapperTest() {
        log("Testing wrappers...");
        Chunk chunk = Constants.getMainWorld().getLoadedChunks()[0];
        Painting painting = chunk.getWorld().spawn(getCenterOfChunk(chunk), Painting.class);
        log("Art is: " + XArt.of(painting.getArt()));
    }

    /**
     * Archived
     */
    @SuppressWarnings("unused")
    private static void testPlayerDependantTasks() {
        Player player;
        try {
            player = FakePlayerFactory.createPlayer();
        } catch (Throwable e) {
            player = null;
            log("Failed to create fake player");
            e.printStackTrace();
        }
        if (player != null) {
            log("Player is: " + player);

            log("Testing ActionBar...");
            ActionBar.sendActionBar(player, "Hello!");

            log("Testing Titles...");
            Titles.sendTitle(player, 10, 20, 10, "Title", "subtitle");
        }
    }

    private static void testReflection() {
        log("Testing reflection...");
        log("Version pack: " + XReflection.getVersionInformation());
        ReflectionTests.test();
        ProxyTests.test();
        ASMTests.test();
        ReflectiveConstraintTests.test();
        if (XReflection.supports(12)) initializeReflection();

        log("Testing XWorldBorder...");
        Iterator<World> worldIter = Bukkit.getWorlds().iterator();
        if (worldIter.hasNext()) XWorldBorder.create();
        else {
            // noinspection unused
            double init = XWorldBorder.MAX_SIZE;
            log("Cannot test XWorldBorder because no worlds are loaded.");
        }
    }

    private static void testXTag() {
        log("Testing XTag...");
        assertPresent(XTag.getTag("INVENTORY_NOT_DISPLAYABLE"));
        assertTrue(XTag.DEBUFFS.isTagged(XPotion.POISON));
        assertTrue(XTag.EFFECTIVE_SMITE_ENTITIES.isTagged(XEntityType.ZOMBIE));
        assertTrue(XTag.CORALS.isTagged(XMaterial.TUBE_CORAL));
        assertTrue(XTag.LOGS_THAT_BURN.isTagged(XMaterial.STRIPPED_ACACIA_LOG));
        assertFalse(XTag.ANVIL.isTagged(XMaterial.BEDROCK));
    }

    private static void testXParticle() {
        if (XReflection.supports(9)) {
            log("Testing particles...");
            ParticleDisplay.of(XParticle.CLOUD).
                    withLocation(new Location(null, 1, 1, 1))
                    .rotate(90, 90, 90).withCount(-1).offset(5, 5, 5).withExtra(1).forceSpawn(true);
            commonRegistryTest(XParticle.REGISTRY, Arrays.asList(values(Particle.class)));
        }
    }

    @SuppressWarnings("unchecked")
    private static <E> E[] values(Class<?> clazz) {
        try {
            return (E[]) XReflection.of(clazz).method()
                    .asStatic().named("values").returns(XReflection.of(clazz).asArray())
                    .reflect().invoke();
        } catch (Throwable e) {
            throw new IllegalStateException("Cannot get values() method of " + clazz, e);
        }
    }

    private static void testXAttribute() {
        if (XReflection.supports(9)) {
            log("Testing XAttribute...");
            log("Attribute " + XAttribute.ARMOR_TOUGHNESS + " is " + XAttribute.ARMOR_TOUGHNESS.get());
            assertPresent(XAttribute.of("MAX_HEALTH"));
            commonRegistryTest(XAttribute.REGISTRY, Arrays.asList(values(Attribute.class)));
        }
    }

    private static void testXSound() {
        log("Testing XSound...");
        assertPresent(XSound.of("BLOCK_ENCHANTMENT_TABLE_USE"));
        assertPresent(XSound.of("AMBIENCE_CAVE"));
        assertPresent(XSound.of("RECORD_11"));
        commonRegistryTest(XSound.REGISTRY, Arrays.asList(values(Sound.class)));
    }

    private static void testXPotion() {
        log("Testing XPotion...");
        assertPresent(XPotion.of("INVIS"));
        assertPresent(XPotion.of("AIR"));
        assertPresent(XPotion.of("BLIND"));
        assertPresent(XPotion.of("DAMAGE_RESISTANCE"));
        commonRegistryTest(XPotion.REGISTRY, Arrays.asList(values(PotionEffectType.class)));
        for (PotionType potionType : PotionType.values()) {
            switch (potionType) {
                // These aren't really "effects" they're literally just potion types without an effect.
                case WATER:
                case THICK:
                case AWKWARD:
                case MUNDANE:

                case TURTLE_MASTER:
                case LONG_TURTLE_MASTER:
                case STRONG_TURTLE_MASTER:
                    continue;
            }
            if (potionType.name().equals("UNCRAFTABLE")) continue; // Present in 1.12

            String bukkitName = potionType.name();

            assertNotNull(XPotion.of(potionType), () -> "null for (Bukkit -> XForm): " + potionType);
            assertPresent(XPotion.of(bukkitName), "null for (String -> XForm): " + bukkitName);
        }

        assertPotionEffect(XPotion.parseEffect("STRENGTH, 10, 3"), XPotion.STRENGTH, 10, 3);
        assertPotionEffect(XPotion.parseEffect("BLINDNESS, 30, 1"), XPotion.BLINDNESS, 30, 1);
        assertPotionEffect(XPotion.parseEffect("SLOWNESS, 200, 10, %75"), XPotion.SLOWNESS, 200, 10);
    }

    private static void assertPotionEffect(XPotion.Effect effect, XPotion type, int duration, int amplifier) {
        assertNotNull(effect, "Effect could not be parsed");
        assertEquals(type, effect.getXPotion(), "Potion effect types don't match");
        assertEquals(amplifier - 1, effect.getEffect().getAmplifier(), "Potion effect amplifiers don't match");
        assertEquals(duration * 20, effect.getEffect().getDuration(), "Potion effect durations don't match");
    }

    private static void testXEnchantment() {
        log("Testing XEnchantment...");
        assertPresent(XEnchantment.of("EFFICIENCY"));
        assertNotNull(XEnchantment.of(Enchantment.KNOCKBACK));
        if (XReflection.supports(11)) assertNotNull(XEnchantment.of(Enchantment.SWEEPING_EDGE));
        commonRegistryTest(XEnchantment.REGISTRY, Arrays.asList(values(Enchantment.class)));
    }

    private static <XForm extends XBase<XForm, BukkitForm>, BukkitForm> void commonRegistryTest(
            XRegistry<XForm, BukkitForm> xRegistry,
            Collection<BukkitForm> bukkitRegistry
    ) {
        for (BukkitForm bukkitForm : bukkitRegistry) {
            if (bukkitForm == null) {
                log("Detected null standard field for: " + xRegistry);
                continue;
            }
            if (bukkitForm.toString().startsWith("LEGACY_")) {
                log("Skipping legacy bukkit form: " + xRegistry.getName() + "::" + bukkitForm);
                continue;
            }
            String bukkitName = XRegistry.getBukkitName(bukkitForm);

            assertNotNull(xRegistry.getByBukkitForm(bukkitForm), () -> "null for (Bukkit -> XForm): " + bukkitForm);
            assertPresent(xRegistry.getByName(bukkitName), "null for (String -> XForm): " + bukkitName);
        }
        for (XForm xForm : xRegistry) {
            assertNotNull(xRegistry.getByBukkitForm(xForm.get()), () -> "null for (Bukkit -> XForm): " + xForm.get() + " -> " + xForm);
            assertPresent(xRegistry.getByName(xForm.name()), "null for (String -> XForm): " + xForm.name());
        }
    }

    private static void testXMaterial() {
        log("Testing XMaterial...");
        assertPresent(XMaterial.matchXMaterial("AIR"));
        assertDoesNotThrow(() -> XMaterial.matchXMaterial(Material.OMINOUS_TRIAL_KEY));
        assertDoesNotThrow(() -> XMaterial.matchXMaterial(new ItemStack(Material.OMINOUS_TRIAL_KEY)));
        assertSame(XMaterial.matchXMaterial("CLAY_BRICK"), XMaterial.BRICK);
        assertMaterial("MELON", "MELON");

        assertNotNull(XMaterial.ACACIA_SIGN.parseItem(), "Acacia Sign is null");
        assertMaterial("COMMAND_BLOCK", XMaterial.COMMAND_BLOCK);
        assertMaterial("STEP:1", XMaterial.SANDSTONE_SLAB);
        assertMaterial("WOOD_BUTTON", XMaterial.OAK_BUTTON);
        assertMaterial("TRAP_DOOR", XMaterial.OAK_TRAPDOOR);
        assertMaterial("WOOD_PLATE", XMaterial.OAK_PRESSURE_PLATE);
        assertMaterial("STONE_SLAB2", XMaterial.RED_SANDSTONE_SLAB);
        assertMaterial("SIGN", XMaterial.OAK_SIGN);
        assertMaterial("INK_SACK:3", XMaterial.COCOA_BEANS);
        assertMaterial("INK_SACK:4", XMaterial.LAPIS_LAZULI);

        if (XMaterial.supports(14)) {
            assertMaterial(XMaterial.RED_DYE, Material.RED_DYE);
            assertMaterial(XMaterial.GREEN_DYE, Material.GREEN_DYE);
            assertMaterial(XMaterial.BLACK_DYE, Material.BLACK_DYE);

            assertMaterial("RED_BED", "RED_BED");
            assertMaterial("GREEN_CONCRETE_POWDER", "CONCRETE_POWDER:13");
        } else if (XMaterial.supports(13)) {
            log("Black dye is " + XMaterial.BLACK_DYE.get() + " - " + XMaterial.BLACK_DYE.parseItem());
            assertMaterial(XMaterial.CYAN_DYE, Material.CYAN_DYE);
            assertMaterial(XMaterial.GREEN_DYE, Material.valueOf("CACTUS_GREEN"));

            // BLACK_DYE doesn't exist in 1.13 but INK_SACK does.
            // So naturally, INK_SAC:0 gets mapped to XMaterial.INK_SAC because
            // INK_SAC remains a material for later versions.
            // But should we expect it to get mapped to BLACK_DYE instead?
            // Well, no and yes. The conclusion is that we just chose for
            // it to get mapped to INK_SAC because that'd make more sense on this version.
            // So this would happen: XMaterial.BLACK_DYE -> Material.INK_SACK -> XMaterial.INK_SAC
            Material inkSack = Material.valueOf("INK_SAC");
            Assertions.assertSame(XMaterial.BLACK_DYE.get(), inkSack);

            Optional<XMaterial> BLACK_DYE = XMaterial.matchXMaterial("BLACK_DYE");
            Optional<XMaterial> INK_SAC = XMaterial.matchXMaterial("INK_SAC");

            assertPresent(BLACK_DYE);
            assertPresent(INK_SAC);

            // noinspection OptionalGetWithoutIsPresent
            Assertions.assertSame(BLACK_DYE.get(), XMaterial.BLACK_DYE);
            // noinspection OptionalGetWithoutIsPresent
            Assertions.assertSame(INK_SAC.get(), XMaterial.INK_SAC);

            assertMaterial(XMaterial.INK_SAC, inkSack);
        } else {
            Material inkSack = XMaterial.INK_SAC.get(); // INK_SACK for 1.8
            assertMaterial(XMaterial.CYAN_DYE, inkSack);
            assertMaterial(XMaterial.GREEN_DYE, inkSack);
            assertMaterial(XMaterial.BLACK_DYE, inkSack);
        }

        // assertFalse(XMaterial.MAGENTA_TERRACOTTA.isOneOf(Arrays.asList("GREEN_TERRACOTTA", "BLACK_BED", "DIRT")));
        // assertTrue(XMaterial.BLACK_CONCRETE.isOneOf(Arrays.asList("RED_CONCRETE", "CONCRETE:15", "CONCRETE:14")));
        // commonRegistryTest(XMaterial.REGISTRY, Arrays.asList(Material.values()));
        for (Material material : Material.values())
            if (!material.name().startsWith("LEGACY")) XMaterial.matchXMaterial(material);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static void assertMaterial(String parse, XMaterial expect) {
        Optional<XMaterial> parsed = XMaterial.matchXMaterial(parse);
        assertPresent(parsed);
        Assertions.assertSame(parsed.get(), expect);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static void assertMaterial(XMaterial original, Material expect) {
        Optional<XMaterial> selfNameMapped = XMaterial.matchXMaterial(original.name());

        assertPresent(selfNameMapped);
        assertSame(selfNameMapped, original);
        Assertions.assertSame(original.get(), expect);
        Assertions.assertSame(XMaterial.matchXMaterial(original.parseItem()), original);
        Assertions.assertSame(XMaterial.matchXMaterial(selfNameMapped.get().parseItem()), original);
    }

    private static final class ItemSerialDual {
        private ItemStack serialized, deserialized;
    }

    private static void testXItemStack() {
        Map<String, ItemSerialDual> map = new HashMap<>();
        YamlConfiguration serializeConfig;
        log("Testing XItemStack...");

        try {
            serializeConfig = serializeItemStack(map);
            deserializeItemStack(map);
        } catch (IOException | InvalidConfigurationException e) {
            throw new AssertionFailedError("Failed to serialize/deserialize items", e);
        }

        for (Map.Entry<String, ItemSerialDual> entry : map.entrySet()) {
            ItemSerialDual dual = entry.getValue();
            if (dual.serialized == null || dual.deserialized == null) {
                log((dual.serialized == null ? "Serialized" : "")
                        + (dual.deserialized == null ? "Deserialized" : "")
                        + " item doesn't exist for: " + entry.getKey());
            } else {
                assertTrue(dual.serialized.isSimilar(dual.deserialized),
                        () -> "Items for '" + entry.getKey() + "' are not similar:\n\nSerialized:   "
                                + dual.serialized + "\n\nDeserialized: " + dual.deserialized + '\n');

                ConfigurationSection serializeRedeserialized = serializeConfig.getConfigurationSection(entry.getKey());
                ItemStack redeserializedItem = XItemStack.deserialize(serializeRedeserialized);

                assertTrue(dual.serialized.isSimilar(redeserializedItem),
                        () -> "Items for redeserialized '" + entry.getKey() + "' are not similar:\n\nSerialized:   "
                                + dual.serialized + "\n\nDeserialized: " + redeserializedItem + '\n');
            }
        }
    }

    private static void deserializeItemStack(Map<String, ItemSerialDual> map) throws IOException, InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.load(ResourceHelper.getResourceAsFile("itemstack.yml"));

        for (String section : yaml.getKeys(false)) {
            ConfigurationSection itemSection = yaml.getConfigurationSection(section);
            ItemStack item = XItemStack.deserialize(itemSection);

            map.compute(section, (k, v) -> {
                if (v == null) v = new ItemSerialDual();
                v.deserialized = item;
                return v;
            });
            log("[Deserialized Item] " + section + ": " + item);
        }
    }

    private static ItemStack createItem(XMaterial material, Consumer<ItemMeta> metaConsumer) {
        ItemStack item = material.parseItem();
        ItemMeta meta = item.getItemMeta();
        metaConsumer.accept(meta);
        item.setItemMeta(meta);
        return item;
    }

    private static boolean metaExists(String className) {
        try {
            Class.forName("org.bukkit.inventory.meta." + className);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    @SuppressWarnings("CodeBlock2Expr")
    private static YamlConfiguration serializeItemStack(Map<String, ItemSerialDual> map) throws IOException {
        File file = new File(Bukkit.getWorldContainer(), "serialized.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        Map<String, ItemStack> items = new HashMap<>();

        items.put("sword", createItem(XMaterial.DIAMOND_SWORD, meta -> {
            meta.setDisplayName("&3Yay");
            meta.setLore(Arrays.asList("Line 1", "Line 2", " ", "Line 4"));
        }));
        if (metaExists("ColorableArmorMeta")) {
            items.put("leather-colored-armor-trim", createItem(XMaterial.LEATHER_CHESTPLATE, meta -> {
                ColorableArmorMeta leather = (ColorableArmorMeta) meta;
                leather.setColor(Color.fromRGB(255, 155, 155));
                leather.setTrim(new ArmorTrim(TrimMaterial.DIAMOND, TrimPattern.SNOUT));
            }));
        }
        if (Constants.TEST_MOJANG_API) {
            items.put("head-notch", createItem(XMaterial.PLAYER_HEAD, meta -> {
                XSkull.of(meta).profile(
                        Profileable.username("Notch")
                                .transform(ProfileTransformer.includeOriginalValue())
                ).apply();
            }));
            items.put("head-uuid-include-original", createItem(XMaterial.PLAYER_HEAD, meta -> {
                XSkull.of(meta).profile(
                        Profileable.of(UUID.fromString("45d3f688-0765-4725-b5dd-dbc28fdfc9ab"))
                                .transform(ProfileTransformer.includeOriginalValue())
                ).apply();
            }));
            items.put("head-username-no-transform", createItem(XMaterial.PLAYER_HEAD, meta -> {
                XSkull.of(meta).profile(
                        Profileable.of(UUID.fromString("45d3f688-0765-4725-b5dd-dbc28fdfc9ab"))
                ).apply();
            }));
        }
        items.put("head-no-op", createItem(XMaterial.PLAYER_HEAD, meta -> {}));

        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<String, ItemStack> item : items.entrySet()) {
            String sectionName = item.getKey();
            ConfigurationSection section = yaml.createSection(sectionName);

            XItemStack.serialize(item.getValue(), section);
            map.compute(sectionName, (k, v) -> {
                if (v == null) v = new ItemSerialDual();
                v.serialized = item.getValue();
                return v;
            });
        }
        yaml.save(file);
        return yaml;
    }

    private static void testSkulls() {
        log("Testing skulls request queue...");
        XSkullRequestQueueTest.test();

        log("Testing skulls UUID...");
        XSkull.createItem().profile(Profileable.of(UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"))).apply();
        log("Testing skulls username");
        XSkull.createItem().profile(Profileable.username("Notch")).apply();
        log("Testing skulls Base64");
        XSkull.createItem().profile(Profileable.detect("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzI0ZTY3ZGNlN2E0NDE4ZjdkYmE3MTE3MDQxODAzMDQ1MDVhMDM3YzEyZjE1NWE3MDYwM2UxOWYxMzIwMzRiMSJ9fX0=")).apply();
        log("Testing skulls textures hash");
        XSkull.createItem().profile(Profileable.detect("f9f28fe3a81d67e67472b7b91caad063722477dfc37f0d729a19be49c2ec2990")).apply();
        log("Testing skulls textures URL");
        XSkull.createItem().profile(Profileable.detect("https://textures.minecraft.net/texture/f9f28fe3a81d67e67472b7b91caad063722477dfc37f0d729a19be49c2ec2990")).apply();
        log("Testing skulls usernamed fallback");
        XSkull.createItem()
                .profile(Profileable.username("hjkSF3809HFGhs"))
                .fallback(Profileable.username("CryptoMorin"))
                .apply();
        log("Testing skulls lenient silence");
        XSkull.createItem()
                .profile(Profileable.username("F(&$#%Y(@&$(@#$Y_{GFS!"))
                .lenient().apply();

        // Currently broken. Seems like Mojang disabled this API? Read MojangAPI.usernamesToUUIDs for more info.
        if (Constants.TEST_MOJANG_API_BULK) {
            log("Testing bulk username to UUID conversion");
            Map<UUID, String> mapped = MojangAPI.usernamesToUUIDs(Arrays.asList("yourmom1212",
                    "ybe", "Scavage", "Tinchosz", "daerb",
                    "verflow", "Brazzer", "Trillest", "EZix",
                    "Meritocracia", "otpe", "nn_mc", "Hershey",
                    "ElsaPlayzz", "HACKIN0706", "Angelisim", "iFraz",
                    "KolevBG", "thebreadrat", "VIRGlN", "ImPuddles",
                    "AlphaAce", "ggsophie", "TheDark_00", "yeezydealer",
                    "HKa1", "Natheyy", "l0ves1ckk", "Bucyrus"), null);
            log("Result of bulk requests: " + mapped);
        }

        log("Skull value of Notch: " + Profileable.username("Notch").getProfileValue());
        log("Skull value of Base64: " + Profileable.detect("f9f28fe3a81d67e67472b7b91caad063722477dfc37f0d729a19be49c2ec2990").getProfileValue());

        if (Constants.TEST_MOJANG_API_BULK) {
            profilePreparation(); // Takes ~5 seconds (If we ignore the previous requests in this method)
            profilePreparation(); // Takes less than a second
        }
    }

    private static void profilePreparation() {
        log("Profileable preparation test");
        Profileable.prepare(Arrays.asList(
                        Profileable.username("ImPuddles"), Profileable.username("HACKIN0706"), Profileable.username("yeezydealer"),
                        Profileable.detect("Bucyrus"),
                        Profileable.detect("https://textures.minecraft.net/texture/f9f28fe3a81d67e67472b7b91caad063722477dfc37f0d729a19be49c2ec2990")
                ), session -> session.exceptionally((a, b) -> {
                    log("Session Exceptionally");
                    b.printStackTrace();
                    return false;
                }), x -> {
                    log("Error Handler " + x.getMessage());
                    // x.printStackTrace(); Don't print, it'll not include the full stacktrace
                    return false;
                })
                .thenRun(() -> log("profile preparation done"))
                .exceptionally((ex) -> {
                    log("Profile preparation done exceptionally: ");
                    ex.printStackTrace();
                    return null;
                })
                .join();
    }

    private static void initializeReflection() {
        try {
            Class.forName("com.cryptomorin.xseries.XWorldBorder");
            Class.forName("com.cryptomorin.xseries.messages.ActionBar");
            Class.forName("com.cryptomorin.xseries.messages.Titles");
            Class.forName("com.cryptomorin.xseries.profiles.builder.XSkull");
            Class.forName("com.cryptomorin.xseries.reflection.minecraft.NMSExtras");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> void assertSame(Optional<T> expected, T actual) {
        Assertions.assertTrue(expected.isPresent(), () -> "Item is not present to compare with " + actual);
        Assertions.assertSame(expected.get(), actual);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static void assertPresent(Optional<?> opt) {
        Assertions.assertTrue(opt.isPresent());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static void assertPresent(Optional<?> opt, String details) {
        Assertions.assertTrue(opt.isPresent(), details);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static void assertMaterial(String bukkitMaterial, String xMaterial) {
        Optional<XMaterial> mat = XMaterial.matchXMaterial(xMaterial);
        assertPresent(mat);
        Assertions.assertSame(Material.matchMaterial(bukkitMaterial), mat.get().get());
    }
}
