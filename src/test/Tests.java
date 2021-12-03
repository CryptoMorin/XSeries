import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;

public final class Tests {
    public static void test() {
        RunServer.print("Test begin...");
        Assertions.assertTrue(XMaterial.matchXMaterial("AIR").isPresent());
        Assertions.assertSame(Material.RED_BED, XMaterial.matchXMaterial("RED_BED").get().parseMaterial());
        Assertions.assertSame(Material.MELON_SLICE, XMaterial.matchXMaterial("MELON").get().parseMaterial());
        RunServer.print("Test end...");
    }
}
