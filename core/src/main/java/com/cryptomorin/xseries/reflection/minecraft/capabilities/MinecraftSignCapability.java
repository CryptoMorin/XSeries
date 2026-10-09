package com.cryptomorin.xseries.reflection.minecraft.capabilities;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftConnection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftMapping;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

import static com.cryptomorin.xseries.reflection.XReflection.*;

public final class MinecraftSignCapability extends MinecraftCapability {
    private final MinecraftBlockCapability blockCapability;

    public final MethodHandle PACKET_PLAY_OUT_OPEN_SIGN_EDITOR, SANITIZE_LINES,
            TILE_ENTITY_SIGN__GET_UPDATE_PACKET, TILE_ENTITY_SIGN__SET_LINE,
            SIGN_TEXT, TILE_ENTITY_SIGN;

    public final Class<?> signOpenPacket = ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
            .map(MinecraftMapping.MOJANG, "ClientboundOpenSignEditorPacket")
            .map(MinecraftMapping.SPIGOT, "PacketPlayOutOpenSignEditor").unreflect();

    public final Class<?> CraftSign = ofMinecraft().inPackage(MinecraftPackage.CB, "block")
            .named("CraftSign").unreflect();

    public final Class<?> TileEntitySign = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.level.block.entity")
            .map(MinecraftMapping.MOJANG, "SignBlockEntity")
            .map(MinecraftMapping.SPIGOT, "TileEntitySign").unreflect();

    MinecraftSignCapability(MethodHandles.Lookup lookup, MinecraftBlockCapability blockCapability) throws Throwable {
        this.blockCapability = blockCapability;

        PACKET_PLAY_OUT_OPEN_SIGN_EDITOR = lookup.findConstructor(signOpenPacket,
                XReflection.v(26, 3, 0, MethodType.methodType(void.class, blockCapability.BlockPos, boolean.class))
                        .v(1, 20, 0, MethodType.methodType(void.class, blockCapability.BlockPos, boolean.class))
                        .orElse(MethodType.methodType(void.class, blockCapability.BlockPos)));


        if (supports(1, 17, 0)) {
            SANITIZE_LINES = lookup.findStatic(CraftSign, v(1, 17, "sanitizeLines").orElse("SANITIZE_LINES"),
                    MethodType.methodType(toArrayClass(MinecraftCapabilities.IChatBaseComponent), String[].class));

            TILE_ENTITY_SIGN = lookup.findConstructor(TileEntitySign, MethodType.methodType(void.class, blockCapability.BlockPos, blockCapability.BlockState));
            TILE_ENTITY_SIGN__GET_UPDATE_PACKET = XReflection.of(TileEntitySign).method().returns(blockCapability.PacketPlayOutTileEntityData)
                    .map(MinecraftMapping.MOJANG, "getUpdatePacket")
                    .map(MinecraftMapping.SPIGOT, v(1, 21, 9, "l")
                            .v(1, 21, 6, "u")
                            .v(1, 21, 4, "s")
                            .v(1, 21, 3, "t")
                            .v(1, 20, 5, "l")
                            .v(1, 20, 4, "m")
                            .v(1, 20, "j")
                            .v(1, 19, 0, "f")
                            .v(1, 18, 0, "c")
                            .orElse("getUpdatePacket"))
                    .unreflect();

            if (supports(1, 20, 0)) {
                Class<?> SignText = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.level.block.entity")
                        .named("SignText").unreflect();
                // public boolean a(SignText signtext, boolean flag) {
                //        return flag ? this.c(signtext) : this.b(signtext);
                // }
                if (!supports(1, 20, 6)) { // It completely changed, needs a lot of work
                    TILE_ENTITY_SIGN__SET_LINE = lookup.findVirtual(TileEntitySign, "a",
                            MethodType.methodType(boolean.class, SignText, boolean.class));
                } else {
                    TILE_ENTITY_SIGN__SET_LINE = null;
                }

                Class<?> IChatBaseComponentArray = XReflection.of(MinecraftCapabilities.IChatBaseComponent).asArray().unreflect();

                // public SignText(net.minecraft.network.chat.IChatBaseComponent[] var0, IChatBaseComponent[] var1,
                // EnumColor var2, boolean var3) {
                Class<?> EnumColor = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.item")
                        .map(MinecraftMapping.MOJANG, "DyeColor")
                        .map(MinecraftMapping.SPIGOT, "EnumColor").unreflect();
                SIGN_TEXT = lookup.findConstructor(SignText, MethodType.methodType(void.class,
                        IChatBaseComponentArray, IChatBaseComponentArray, EnumColor, boolean.class));
            } else {
                SIGN_TEXT = null;
                TILE_ENTITY_SIGN__SET_LINE = lookup.findVirtual(TileEntitySign, "a",
                        MethodType.methodType(void.class, int.class,
                                MinecraftCapabilities.IChatBaseComponent, MinecraftCapabilities.IChatBaseComponent));
            }
        } else {
            SIGN_TEXT = null;
            SANITIZE_LINES = null;
            TILE_ENTITY_SIGN = null;
            TILE_ENTITY_SIGN__SET_LINE = null;
            TILE_ENTITY_SIGN__GET_UPDATE_PACKET = null;
        }
    }

    /**
     * Currently only supports 1.17
     */
    public void openSign(Player player, DyeColor textColor, String[] lines, boolean frontSide) {
        try {
            Location loc = player.getLocation();
            Object position = blockCapability.BlockPos_ctor.invoke(loc.getBlockX(), 1, loc.getBlockY());
            Object signBlockData = blockCapability.GET_IBlockState.invoke(Material.OAK_SIGN, (byte) 0);
            Object blockChangePacket = blockCapability.PACKET_PLAY_OUT_BLOCK_CHANGE.invoke(position, signBlockData);

            Object components = SANITIZE_LINES.invoke((Object[]) lines);
            Object tileSign = TILE_ENTITY_SIGN.invoke(position, signBlockData);
            if (supports(1, 20)) {
                // When can we use this without blocks... player.openSign();
                Class<?> EnumColor = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.item")
                        .map(MinecraftMapping.MOJANG, "DyeColor")
                        .map(MinecraftMapping.SPIGOT, "EnumColor").unreflect();
                Object enumColor = null;
                for (Field field : EnumColor.getFields()) {
                    Object color = field.get(null);
                    String colorName = (String) EnumColor.getDeclaredMethod("b").invoke(color); // gets its name
                    if (textColor.name().equalsIgnoreCase(colorName)) {
                        enumColor = color;
                        break;
                    }
                }

                Object signText = SIGN_TEXT.invoke(components, components, enumColor, frontSide);
                TILE_ENTITY_SIGN__SET_LINE.invoke(signText, true);
            } else {
                for (int i = 0; i < lines.length; i++) {
                    Object component = java.lang.reflect.Array.get(components, i);
                    TILE_ENTITY_SIGN__SET_LINE.invoke(tileSign, i, component, component);
                }
            }
            Object signLinesUpdatePacket = TILE_ENTITY_SIGN__GET_UPDATE_PACKET.invoke(tileSign);

            Object signPacket =
                    v(1, 20, PACKET_PLAY_OUT_OPEN_SIGN_EDITOR.invoke(position, true))
                            .orElse(PACKET_PLAY_OUT_OPEN_SIGN_EDITOR.invoke(position));

            MinecraftConnection.sendPacket(player, blockChangePacket, signLinesUpdatePacket, signPacket);
        } catch (Throwable x) {
            x.printStackTrace();
        }
    }
}
