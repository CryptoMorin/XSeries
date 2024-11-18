import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * I still have my doubts whether ReflectionUtils will function correctly.
 * Some methods here are temporary and might switch to ReflectionUtils soon.
 */
public final class TinyReflection {
    public static final Class<?> CraftBukkit$Main;

    static {
        try {
            CraftBukkit$Main = Class.forName("org.bukkit.craftbukkit.Main");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected static final Logger LOGGER = Logger.getLogger(CraftBukkit$Main.getCanonicalName());
    protected static final String VERSION;
    protected static final Object CraftItemFactoryInstance;
    protected static final String IMPL_VER, BUKKIT_VER;
    protected static Object CraftMagicNumbersInstance;

    static {
        String version = null;
        try {
            version = Files.readAllLines(Paths.get(DummySpigotTest.class.getResource("version.txt").toURI())).get(0);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        VERSION = version;

        Object craftItemFactory = null, craftMagicNumbers = null;
        String bukkitVer = null;

        try {
            craftItemFactory = Arrays.stream(craft("inventory.CraftItemFactory").getMethods()).filter(x -> x.getName().equals("instance")).findFirst().get().invoke(null);
            bukkitVer = (String) craft("util.Versioning").getMethod("getBukkitVersion").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        BUKKIT_VER = bukkitVer;
        IMPL_VER = craft("CraftServer").getPackage().getImplementationVersion();
        CraftItemFactoryInstance = craftItemFactory;
        CraftMagicNumbersInstance = craftMagicNumbers;
    }

    private TinyReflection() {}

    protected static Object getCraftMagicNumberInstance() {
        if (CraftMagicNumbersInstance != null) return CraftMagicNumbersInstance;
        try {
            return CraftMagicNumbersInstance = craft("util.CraftMagicNumbers").getField("INSTANCE").get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String getMCVersion() {
        try {
            Class<?> GameVersionClass = Class.forName("com.mojang.bridge.game.GameVersion");
            Object gameVersion = Arrays.stream(XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS).named("MinecraftVersion").unreflect().getMethods())
                    .filter(x -> x.getReturnType() == GameVersionClass).findFirst().get().invoke(null);
            return (String) gameVersion.getClass().getDeclaredMethod("getName").invoke(gameVersion);
        } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> craft(String str) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + VERSION + '.' + str);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    protected static void bootstrap() {
        try {
            XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "server").named("DispenserRegistry").unreflect()
                    .getMethod("init").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
