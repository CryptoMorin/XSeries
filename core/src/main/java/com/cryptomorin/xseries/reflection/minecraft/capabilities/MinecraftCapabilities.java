package com.cryptomorin.xseries.reflection.minecraft.capabilities;

import com.cryptomorin.xseries.reflection.ReflectiveNamespace;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftMapping;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

import static com.cryptomorin.xseries.reflection.XReflection.ofMinecraft;

/**
 * Note: This class is currently really unorganized and unstable. It needs a proper recode.
 * <p>
 * A class that provides various different essential features that the API
 * didn't/doesn't support.
 * <p>
 * All the parameters are non-null.
 *
 * @author Crypto Morin
 * @version 5.5.0
 */
@SuppressWarnings("unused")
public final class MinecraftCapabilities {
    private static final ReflectiveNamespace REFLECTIVE_NAMESPACE = XReflection.namespaced();

    public static final MethodHandle WORLD_HANDLE;
    public static final Class<?> World = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.level")
            .map(MinecraftMapping.MOJANG, "Level")
            .map(MinecraftMapping.SPIGOT, "World").unreflect();

    public static final Class<?> MULTI_BLOCK_CHANGE_INFO_CLASS = null; // getNMSClass("PacketPlayOutMultiBlockChange$MultiBlockChangeInfo")


    public static final Class<?> IChatBaseComponent = ofMinecraft().inPackage(MinecraftPackage.NMS, "network.chat")
            .map(MinecraftMapping.MOJANG, "Component")
            .map(MinecraftMapping.SPIGOT, "IChatBaseComponent").unreflect();

    public static final Class<?> CraftWorld = ofMinecraft().inPackage(MinecraftPackage.CB).named("CraftWorld").unreflect();
    public static final Class<?> ServerLevel = ofMinecraft().inPackage(MinecraftPackage.NMS, "server.level")
            .map(MinecraftMapping.MOJANG, "ServerLevel")
            .map(MinecraftMapping.SPIGOT, "WorldServer").unreflect();

    private static final class FailedMinecraftCapability extends MinecraftCapability {
        private final Throwable error;

        private FailedMinecraftCapability(Throwable error) {this.error = error;}
    }

    static {
        try {

            WORLD_HANDLE = XReflection.of(CraftWorld).method().named("getHandle").returns(ServerLevel).reflect();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Map<Class<? extends MinecraftCapability>, MinecraftCapability> CAPABILITIES = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends MinecraftCapability> T getCapability(Class<T> type) {
        if (type == MinecraftCapability.class)
            throw new IllegalArgumentException("The MinecraftCapability class itself is abstract: " + type);

        MinecraftCapability capability = CAPABILITIES.get(type);
        if (capability instanceof FailedMinecraftCapability)
            throw new IllegalStateException(type + " failed to load before: " + type, ((FailedMinecraftCapability) capability).error);
        if (capability != null) return (T) capability;

        try {
            capability = createCapability(type);
        } catch (Throwable ex) {
            CAPABILITIES.put(type, new FailedMinecraftCapability(ex));
            throw ex;
        }

        CAPABILITIES.put(type, capability);
        return (T) capability;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends MinecraftCapability> T createCapability(Class<T> type) {
        Constructor<?>[] ctors = type.getDeclaredConstructors();
        if (ctors.length != 1) {
            throw new IllegalStateException("Expected only one constructor for " + type + ", got: " + Arrays.toString(ctors));
        }

        Constructor<?> ctor = ctors[0];
        ctor.setAccessible(true);
        MethodHandles.Lookup lookup = null;
        List<Object> parameterValues = new ArrayList<>();
        Set<MinecraftCapability> dependencies = new HashSet<>();

        for (Parameter parameter : ctor.getParameters()) {
            Class<?> paramType = parameter.getType();
            if (paramType == ReflectiveNamespace.class) {
                parameterValues.add(REFLECTIVE_NAMESPACE);
            } else if (paramType == MethodHandles.Lookup.class) {
                if (lookup == null) lookup = MethodHandles.lookup();
                parameterValues.add(lookup);
            } else if (MinecraftCapability.class.isAssignableFrom(paramType)) {
                if (paramType == type)
                    throw new IllegalArgumentException("Capability " + type + " depends on itself: " + ctor);

                MinecraftCapability capability = getCapability((Class<? extends MinecraftCapability>) paramType);
                dependencies.add(capability);
                parameterValues.add(capability);
            } else {
                throw new IllegalArgumentException("Unexpected parameter type: " + paramType + " for constructor " + ctor);
            }
        }

        try {
            T capability = (T) ctor.newInstance(parameterValues.toArray());
            capability.dependencies.clear();
            capability.dependencies.addAll((Collection) dependencies.stream()
                    .map(x -> MinecraftCapabilities.getCapability(x.getClass()))
                    .collect(Collectors.toSet()));
            return capability;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to load capability " + ctor, e);
        } finally {
            ctor.setAccessible(false);
        }
    }

    private MinecraftCapabilities() {}

    protected static Object getStaticFieldIgnored(Class<?> clazz, String name) {
        return getStaticField(clazz, name, true);
    }

    protected static Object getStaticField(Class<?> clazz, String name, boolean silent) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(null);
        } catch (Throwable e) {
            if (!silent)
                throw new IllegalArgumentException("Failed to get static field of " + clazz + " named " + name, e);
            else return null;
        }
    }
}
