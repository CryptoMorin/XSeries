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

package com.cryptomorin.xseries.test.server;

import com.cryptomorin.xseries.profiles.PlayerProfiles;
import com.cryptomorin.xseries.reflection.ReflectiveNamespace;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.ConstructorMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.MethodMemberHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftClassHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftMapping;
import com.cryptomorin.xseries.test.Constants;
import com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Note: This won't work because PlayerConnection must be set as well for packets.
 */
public final class FakePlayerFactory {
    public static Player createPlayer() throws Throwable {
        ReflectiveNamespace ns = XReflection.namespaced();
        ns.imports(GameProfile.class);

        MinecraftClassHandle CraftServer = ns.ofMinecraft("package cb; public final class CraftServer implements Server {}");
        ns.ofMinecraft("package nms.server;           public abstract class MinecraftServer {}");
        ns.ofMinecraft("package nms.server.dedicated; public          class DedicatedServer extends MinecraftServer implements IMinecraftServer {}");
        MethodMemberHandle CraftServer_getServer = CraftServer.method("public DedicatedServer getServer();");

        MinecraftClassHandle CraftPlayer = ns.ofMinecraft(
                "package cb.entity;" +
                        "public class CraftPlayer extends CraftHumanEntity implements Player {}");

        MinecraftClassHandle EntityPlayer = ns.ofMinecraft(
                "package nms.server.level;" +
                        "public class EntityPlayer extends EntityHuman {}");

        MinecraftClassHandle CraftWorld = ns.ofMinecraft("package cb;" +
                "public class CraftWorld extends CraftRegionAccessor implements World {}");
        ns.ofMinecraft("package nms.server.level;" +
                "public class WorldServer extends World implements ServerEntityGetter, GeneratorAccessSeed {}");
        MethodMemberHandle CraftWorld_getHandle = CraftWorld.method("public WorldServer getHandle();");

        MinecraftClassHandle ChatVisibility = ns.ofMinecraft("package nms.world.entity.player; public enum ChatVisibility implements OptionEnum {}")
                .map(MinecraftMapping.MOJANG, "ChatVisibility")
                .map(MinecraftMapping.OBFUSCATED, "EnumChatVisibility");
        Object ChatVisibility_FULL = ChatVisibility.enums()
                .map(MinecraftMapping.MOJANG, "FULL")
                .map(MinecraftMapping.OBFUSCATED, "a")
                .getEnumConstant();

        MinecraftClassHandle HumanoidArm = ns.ofMinecraft("package nms.world.entity; public enum HumanoidArm implements OptionEnum, INamable {}")
                .map(MinecraftMapping.OBFUSCATED, "EnumMainHand");
        Object HumanoidArm_RIGHT = HumanoidArm.enums()
                .map(MinecraftMapping.MOJANG, "RIGHT")
                .map(MinecraftMapping.OBFUSCATED, "b")
                .getEnumConstant();

        MinecraftClassHandle ParticleStatus = ns.ofMinecraft("package nms.server.level; public enum ParticleStatus implements OptionEnum {}");
        Object ParticleStatus_ALL = ParticleStatus.enums()
                .map(MinecraftMapping.MOJANG, "ALL")
                .map(MinecraftMapping.OBFUSCATED, "a")
                .getEnumConstant();

        MinecraftClassHandle ClientInformation = ns.ofMinecraft("package nms.server.level; public record ClientInformation() {}");
        ConstructorMemberHandle ClientInformation$ctor = ClientInformation.constructor("public ClientInformation(" +
                "String language," +
                "int viewDistance," +
                "ChatVisibility chatVisibility," +
                "boolean chatColors," +
                "int modelCustomisation," +
                "HumanoidArm mainHand," +
                "boolean textFilteringEnabled," +
                "boolean allowsListing," +
                "ParticleStatus particleStatus);");

        ConstructorMemberHandle CraftPlayer$ctor = CraftPlayer.constructor(
                "public CraftPlayer(CraftServer server, EntityPlayer entity);");

        ConstructorMemberHandle EntityPlayer$ctor = EntityPlayer.constructor(
                "public EntityPlayer(" +
                        "MinecraftServer minecraftserver, " +
                        "WorldServer worldserver, " +
                        "GameProfile gameprofile, " +
                        "ClientInformation clientinformation);");

        Server server = Bukkit.getServer();
        Object nmsServer = CraftServer_getServer.reflect().invoke(server);

        World world = Constants.getMainWorld();
        Object nmsWorld = CraftWorld_getHandle.reflect().invoke(world);

        GameProfile gameProfile = PlayerProfiles.createGameProfile(
                UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"),
                "Notch"
        );

        Object clientInfo = ClientInformation$ctor.reflect().invoke(
                /* language */ "en-US",
                /* viewDistance */ 4,
                /* chatVisibility */ ChatVisibility_FULL,
                /* chatColors */ true,
                /* modelCustomisation */ 0,
                /* mainHand */ HumanoidArm_RIGHT,
                /* textFilteringEnabled */ false,
                /* allowsListing */ false,
                /* particleStatus */ ParticleStatus_ALL
        );

        Object entityPlayer = EntityPlayer$ctor.reflect().invoke(
                nmsServer, nmsWorld, gameProfile, clientInfo
        );

        Object craftPlayer = CraftPlayer$ctor.reflect().invoke(server, entityPlayer);
        return (Player) craftPlayer;
    }
}
