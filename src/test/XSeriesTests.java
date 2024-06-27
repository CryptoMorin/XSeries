import com.cryptomorin.xseries.*;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.mojang.MojangAPI;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import com.cryptomorin.xseries.reflection.XReflection;
import com.github.cryptomorin.test.ReflectionTests;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public final class XSeriesTests {
    private XSeriesTests() {
    }

    private static void print(String str) {
        System.out.println(str);
    }

    private static void err(String str) {
        System.err.println(str);
    }

    @SuppressWarnings("deprecation")
    public static void test() {
        print("\n\n\nTest begin...");

        print("Writing enum differences...");
        DifferenceHelper.versionDifference();

        print("Testing XMaterial...");
        assertPresent(XMaterial.matchXMaterial("AIR"));
        assertSame(XMaterial.matchXMaterial("CLAY_BRICK").get(), XMaterial.BRICK);
        assertMaterial("RED_BED", "RED_BED");
        assertMaterial("MELON", "MELON");
        assertMaterial("GREEN_CONCRETE_POWDER", "CONCRETE_POWDER:13");
        // assertFalse(XMaterial.MAGENTA_TERRACOTTA.isOneOf(Arrays.asList("GREEN_TERRACOTTA", "BLACK_BED", "DIRT")));
        // assertTrue(XMaterial.BLACK_CONCRETE.isOneOf(Arrays.asList("RED_CONCRETE", "CONCRETE:15", "CONCRETE:14")));
        for (Material material : Material.values())
            if (!material.name().startsWith("LEGACY")) XMaterial.matchXMaterial(material);

        print("Testing XPotion...");
        assertPresent(XPotion.matchXPotion("INVIS"));
        assertPresent(XPotion.matchXPotion("AIR"));
        assertPresent(XPotion.matchXPotion("BLIND"));
        assertPresent(XPotion.matchXPotion("DAMAGE_RESISTANCE"));
        for (PotionEffectType effect : PotionEffectType.values()) {
            try {
                XPotion.matchXPotion(effect);
                assertPresent(XPotion.matchXPotion(effect.getKey().getKey()), "Unknown effect: " + effect + " -> " + effect.getName() + " | " + effect.getId());
            } catch (ArrayIndexOutOfBoundsException ex) {
                err("Unknown effect: " + effect + " -> " + effect.getName() + " | " + effect.getId());
                throw ex;
            }
        }
        for (Enchantment enchant : Enchantment.values()) {
            assertNotNull(XEnchantment.matchXEnchantment(enchant), () -> "null for " + enchant);
            assertPresent(XEnchantment.matchXEnchantment(enchant.getName()), "Unknown enchantment: " + enchant.getName());
        }

        print("Testing XSound...");
        assertPresent(XSound.matchXSound("BLOCK_ENCHANTMENT_TABLE_USE"));
        assertPresent(XSound.matchXSound("AMBIENCE_CAVE"));
        assertPresent(XSound.matchXSound("RECORD_11"));
        for (Sound sound : Sound.values()) XSound.matchXSound(sound);

        print("Testing particles...");
        ParticleDisplay.of(Particle.CLOUD).
                withLocation(new Location(null, 1, 1, 1))
                .rotate(90, 90, 90).withCount(-1).offset(5, 5, 5).withExtra(1).forceSpawn(true);

        print("Testing XTag...");
        assertTrue(XTag.CORALS.isTagged(XMaterial.TUBE_CORAL));
        assertTrue(XTag.LOGS_THAT_BURN.isTagged(XMaterial.STRIPPED_ACACIA_LOG));
        assertFalse(XTag.ANVIL.isTagged(XMaterial.BEDROCK));

        print("Testing reflection...");
        print("Version pack: " + XReflection.getVersionInformation());
        ReflectionTests.parser();
        initializeReflection();
        testSkulls();
        print("\n\n\nTest end...");
    }

    private static void testSkulls() {
        print("Testing skulls UUID...");
        XSkull.createItem().profile(Profileable.of(UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"))).apply();
        print("Testing skulls username");
        XSkull.createItem().profile(Profileable.username("Notch")).apply();
        print("Testing skulls Base64");
        XSkull.createItem().profile(Profileable.detect("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzI0ZTY3ZGNlN2E0NDE4ZjdkYmE3MTE3MDQxODAzMDQ1MDVhMDM3YzEyZjE1NWE3MDYwM2UxOWYxMzIwMzRiMSJ9fX0=")).apply();
        print("Testing skulls textures hash");
        XSkull.createItem().profile(Profileable.detect("f9f28fe3a81d67e67472b7b91caad063722477dfc37f0d729a19be49c2ec2990")).apply();
        print("Testing skulls textures URL");
        XSkull.createItem().profile(Profileable.detect("https://textures.minecraft.net/texture/f9f28fe3a81d67e67472b7b91caad063722477dfc37f0d729a19be49c2ec2990")).apply();
        print("Testing skulls usernamed fallback");
        XSkull.createItem()
                .profile(Profileable.username("hjkSF3809HFGhs"))
                .fallback(Profileable.username("CryptoMorin"))
                .apply();
        print("Testing skulls lenient silence");
        XSkull.createItem()
                .profile(Profileable.username("F(&$#%Y(@&$(@#$Y_{GFS!"))
                .lenient().apply();

        print("Testing bulk username to UUID conversion");
        Map<UUID, String> mapped = MojangAPI.usernamesToUUIDs(Arrays.asList("yourmom1212",
                "ybe", "Scavage", "Tinchosz", "daerb",
                "verflow", "Brazzer", "Trillest", "EZix",
                "Meritocracia", "otpe", "nn_mc", "Hershey",
                "ElsaPlayzz", "HACKIN0706", "Angelisim", "iFraz",
                "KolevBG", "thebreadrat", "VIRGlN", "ImPuddles",
                "AlphaAce", "ggsophie", "TheDark_00", "yeezydealer",
                "HKa1", "Natheyy", "l0ves1ckk", "Bucyrus"), null);
        print("Result of bulk requests: " + mapped);

        profilePreparation(); // Takes ~5 seconds (If we ignore the previous requests in this method)
        profilePreparation(); // Takes less than a second
    }

    private static void profilePreparation() {
        print("Profileable preparation test");
        Profileable.prepare(Arrays.asList(
                        Profileable.username("ImPuddles"), Profileable.username("HACKIN0706"), Profileable.username("yeezydealer"),
                        Profileable.detect("Bucyrus"),
                        Profileable.detect("https://textures.minecraft.net/texture/f9f28fe3a81d67e67472b7b91caad063722477dfc37f0d729a19be49c2ec2990")
                ), session -> session.exceptionally((a, b) -> {
                    print("Exceptionally");
                    b.printStackTrace();
                    return false;
                }), x -> {
                    x.printStackTrace();
                    return false;
                })
                .thenRun(() -> print("profile preparation done"))
                .exceptionally((ex) -> {
                    print("Profile preparation done exceptionally: ");
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
        Assertions.assertSame(Material.matchMaterial(bukkitMaterial), mat.get().parseMaterial());
    }
}
