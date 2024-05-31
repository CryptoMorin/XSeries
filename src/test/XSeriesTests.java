import com.cryptomorin.xseries.*;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.reflection.XReflection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Assertions;
import com.github.cryptomorin.test.ReflectionTests;

import java.util.Optional;

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

        print("\n\n\nTest end...");
    }

    private static void initializeReflection() {
        try {
            Class.forName("com.cryptomorin.xseries.XWorldBorder");
            Class.forName("com.cryptomorin.xseries.messages.ActionBar");
            Class.forName("com.cryptomorin.xseries.messages.Titles");
            Class.forName("com.cryptomorin.xseries.XSkull");
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
