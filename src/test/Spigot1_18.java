import net.minecraft.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.craftbukkit.Main;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_17_R1.util.Versioning;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.logging.Logger;

public class Spigot1_18 extends RunServer implements InvocationHandler {
    private static final HashMap<Method, MethodHandler> methods = new HashMap<>();

    static {
        try {
            methods.put(
                    Server.class.getMethod("getItemFactory"),
                    new MethodHandler() {
                        @Override
                        public Object handle(RunServer server, Object[] args) {
                            return CraftItemFactory.instance();
                        }
                    }
            );
            methods.put(
                    Server.class.getMethod("getName"),
                    new MethodHandler() {
                        @Override
                        public Object handle(RunServer server, Object[] args) {
                            return "XSeries Test Unit";
                        }
                    }
            );
            methods.put(
                    Server.class.getMethod("getVersion"),
                    new MethodHandler() {
                        @Override
                        public Object handle(RunServer server, Object[] args) {
                            return CraftServer.class.getPackage().getImplementationVersion()
                                    + " (MC: " + MinecraftVersion.a().getName() + ')';
                        }
                    }
            );
            methods.put(
                    Server.class.getMethod("getBukkitVersion"),
                    new MethodHandler() {
                        @Override
                        public Object handle(RunServer server, Object[] args) {
                            return Versioning.getBukkitVersion();
                        }
                    }
            );
            methods.put(
                    Server.class.getMethod("getLogger"),
                    new MethodHandler() {
                        final Logger logger = Logger.getLogger(RunServer.class.getCanonicalName());

                        @Override
                        public Object handle(RunServer server, Object[] args) {
                            return logger;
                        }
                    }
            );
            methods.put(
                    Server.class.getMethod("getUnsafe"),
                    new MethodHandler() {
                        @Override
                        public Object handle(RunServer server, Object[] args) {
                            return CraftMagicNumbers.INSTANCE;
                        }
                    }
            );
            methods.put(
                    Server.class.getMethod("createBlockData", Material.class),
                    new MethodHandler() {
                        @Override
                        public Object handle(RunServer server, Object[] args) {
                            return CraftBlockData.newData((Material) args[0], null);
                        }
                    }
            );
            methods.put(Server.class.getMethod("getLootTable", NamespacedKey.class),
                    new MethodHandler() {
                        @Override
                        public Object handle(RunServer server, Object[] args) {
                            throw new UnsupportedOperationException("Unit tests do not support loot tables.");
                        }
                    }
            );
            methods.put(Server.class.getMethod("shutdown"), new MethodHandler() {
                @Override
                public Object handle(RunServer server, Object[] args) {
                    throw new UnsupportedOperationException("Unit tests are shutdown automatically.");
                }
            });
        } catch (Throwable t) {
            throw new Error(t);
        }
    }

    @Override
    protected void main(String[] args) {
        Server instance = null;
        print("Starting Spigot1_18.main...");

        try {
            instance = Proxy.getProxyClass(Server.class.getClassLoader(), Server.class).asSubclass(Server.class)
                    .getConstructor(InvocationHandler.class).newInstance(new Spigot1_18());
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        print("Starting org.bukkit.craftbukkit.Main...");
        Main.main(args);

        print("Initializing server...");
        Bukkit.setServer(instance);

        print("Done!");
    }

    @Test
    void test() {
        print("Running tests...");
        runServer();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        MethodHandler handler = methods.get(method);
        if (handler != null) {
            return handler.handle(this, args);
        }
        throw new UnsupportedOperationException(String.valueOf(method));
    }

    private interface MethodHandler {
        Object handle(RunServer server, Object[] args);
    }
}
