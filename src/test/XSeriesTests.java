import com.cryptomorin.xseries.ReflectionUtils;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.XTag;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class XSeriesTests {
    private XSeriesTests() {}

    private static void print(String str) {
        System.out.println(str);
    }

    public static void test() {
        print("\n\n\nTest begin...");

        print("Testing XMaterial...");
        assertPresent(XMaterial.matchXMaterial("AIR"));
        assertMaterial("RED_BED", "RED_BED");
        assertMaterial("MELON", "MELON");
        assertMaterial("GREEN_CONCRETE_POWDER", "CONCRETE_POWDER:13");
        Assertions.assertFalse(XMaterial.MAGENTA_TERRACOTTA.isOneOf(Arrays.asList("GREEN_TERRACOTTA", "BLACK_BED", "DIRT")));
        Assertions.assertTrue(XMaterial.BLACK_CONCRETE.isOneOf(Arrays.asList("RED_CONCRETE", "CONCRETE:15", "CONCRETE:14")));

        print("Testing XPotion...");
        assertPresent(XPotion.matchXPotion("INVIS"));
        assertPresent(XPotion.matchXPotion("AIR"));
        assertPresent(XPotion.matchXPotion("BLIND"));
        assertPresent(XPotion.matchXPotion("DAMAGE_RESISTANCE"));

        print("Testing XSound...");
        assertPresent(XSound.matchXSound("BLOCK_ENCHANTMENT_TABLE_USE"));
        assertPresent(XSound.matchXSound("AMBIENCE_CAVE"));
        assertPresent(XSound.matchXSound("RECORD_11"));

        print("Testing particles...");
        ParticleDisplay.of(Particle.CLOUD).
                withLocation(new Location(null, 1, 1, 1))
                .rotate(90, 90, 90).withCount(-1).offset(5, 5, 5).withExtra(1).forceSpawn(true)
                .rotationOrder(ParticleDisplay.Axis.X, ParticleDisplay.Axis.Y, ParticleDisplay.Axis.Z);

        print("Testing XTag...");
        assertTrue(XTag.CORAL_PLANTS.isTagged(XMaterial.TUBE_CORAL));
        assertTrue(XTag.LOGS_THAT_BURN.isTagged(XMaterial.STRIPPED_ACACIA_LOG));
        assertFalse(XTag.ANVIL.isTagged(XMaterial.BEDROCK));

        print("Testing reflection...");
        print("Version pack: " + ReflectionUtils.VERSION + " (" + ReflectionUtils.VER + ')');
        initializeReflection();

        print("\n\n\nTest end...");
    }

    private static void initializeReflection() {
        try {
            Class.forName("com.cryptomorin.xseries.messages.ActionBar");
            Class.forName("com.cryptomorin.xseries.messages.Titles");
            Class.forName("com.cryptomorin.xseries.SkullUtils");
            Class.forName("com.cryptomorin.xseries.NMSExtras");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static void assertPresent(Optional<?> opt) {
        Assertions.assertTrue(opt.isPresent());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static void assertMaterial(String bukkitMaterial, String xMaterial) {
        Optional<XMaterial> mat = XMaterial.matchXMaterial(xMaterial);
        assertPresent(mat);
        Assertions.assertSame(Material.matchMaterial(bukkitMaterial), mat.get().parseMaterial());
    }
}
