package com.cryptomorin.xseries.reflection.minecraft.capabilities;

import com.cryptomorin.xseries.reflection.ReflectiveNamespace;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftConnection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftMapping;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.util.Map;

import static com.cryptomorin.xseries.reflection.XReflection.*;

public final class MinecraftBlockCapability extends MinecraftCapability {
    public final MethodHandle BlockPos_ctor, PLAY_BLOCK_ACTION, GET_BLOCK_TYPE, GET_BLOCK, GET_IBlockState;
    public final MethodHandle PACKET_PLAY_OUT_BLOCK_CHANGE;
    public final MethodHandle PLAY_OUT_MULTI_BLOCK_CHANGE_PACKET, MULTI_BLOCK_CHANGE_INFO, CHUNK_WRAPPER_SET, CHUNK_WRAPPER, SHORTS_OR_INFO, SET_BlockState;

    public final Class<?> BlockPos, Block, BlockState;

    public final Class<?> packetPlayOutBlockChangeClass = ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
            .map(MinecraftMapping.MOJANG, "ClientboundBlockUpdatePacket")
            .map(MinecraftMapping.SPIGOT, "PacketPlayOutBlockChange").unreflect();
    public final Class<?> PacketPlayOutTileEntityData = ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
            .map(MinecraftMapping.MOJANG, "ClientboundBlockEntityDataPacket")
            .map(MinecraftMapping.SPIGOT, "PacketPlayOutTileEntityData").unreflect();

    MinecraftBlockCapability(MethodHandles.Lookup lookup, ReflectiveNamespace ns) throws Throwable {
        BlockPos = ns.ofMinecraft().inPackage(MinecraftPackage.NMS, "core")
                .map(MinecraftMapping.MOJANG, "BlockPos")
                .map(MinecraftMapping.SPIGOT, "BlockPosition")
                .unreflect();
        Block = ns.ofMinecraft().inPackage(MinecraftPackage.NMS, "world.level.block")
                .named("Block")
                .unreflect();
        BlockState = ns.ofMinecraft().inPackage(MinecraftPackage.NMS, "world.level.block.state")
                .map(MinecraftMapping.MOJANG, "BlockState")
                .map(MinecraftMapping.SPIGOT, "IBlockData")
                .unreflect();

        BlockPos_ctor = lookup.findConstructor(BlockPos,
                v(1, 19, MethodType.methodType(void.class, int.class, int.class, int.class)).orElse(
                        MethodType.methodType(void.class, double.class, double.class, double.class)));

        // public IBlockData getBlockState(BlockPosition blockposition)
        GET_BLOCK_TYPE = XReflection.of(MinecraftCapabilities.World).method().returns(BlockState).parameters(BlockPos)
                .map(MinecraftMapping.MOJANG, "getBlockState")
                .map(MinecraftMapping.SPIGOT, v(1, 18, 0, "a_").orElse("getType"))
                .unreflect();
        if (supports(1, 21)) {
            GET_BLOCK = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "world.level.block.state")
                    .map(MinecraftMapping.MOJANG, "BlockBehaviour")
                    .map(MinecraftMapping.SPIGOT, "BlockBase")
                    .inner(XReflection.ofMinecraft()
                            .map(MinecraftMapping.MOJANG, "BlockStateBase")
                            .map(MinecraftMapping.SPIGOT, "BlockData"))
                    .method().returns(Block)
                    .map(MinecraftMapping.MOJANG, "getBlock")
                    .map(MinecraftMapping.SPIGOT, "b")
                    .unreflect();
        } else {
            GET_BLOCK = XReflection.of(BlockState).method().returns(Block)
                    .map(MinecraftMapping.MOJANG, "getBlock")
                    .map(MinecraftMapping.SPIGOT, v(1, 18, 0, "b").orElse("getBlock"))
                    .unreflect();
        }
        PLAY_BLOCK_ACTION = XReflection.of(MinecraftCapabilities.World).method().returns(void.class).parameters(BlockPos, Block, int.class, int.class)
                .map(MinecraftMapping.MOJANG, "blockEvent")
                .map(MinecraftMapping.SPIGOT, v(1, 18, 0, "a").orElse("playBlockAction"))
                .unreflect();


        Class<?> CraftMagicNumbers = ofMinecraft().inPackage(MinecraftPackage.CB, "util")
                .named("CraftMagicNumbers").unreflect();
        PACKET_PLAY_OUT_BLOCK_CHANGE = lookup.findConstructor(packetPlayOutBlockChangeClass, MethodType.methodType(void.class, BlockPos, BlockState));
        GET_IBlockState = lookup.findStatic(CraftMagicNumbers, "getBlock", MethodType.methodType(BlockState, Material.class, byte.class));

        // Multi Block Change
        Class<?> playOutMultiBlockChangeClass = ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
                .map(MinecraftMapping.MOJANG, "ClientboundSectionBlocksUpdatePacket")
                .map(MinecraftMapping.SPIGOT, "PacketPlayOutMultiBlockChange")
                .reflect();
        Class<?> chunkCoordIntPairClass = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.level")
                .map(MinecraftMapping.MOJANG, "ChunkPos")
                .map(MinecraftMapping.SPIGOT, "ChunkCoordIntPair")
                .reflect();

        //                playOutMultiBlockChange = lookup.findConstructor(playOutMultiBlockChangeClass, MethodType.methodType(void.class));
        //                multiBlockChangeInfo = lookup.findConstructor(MULTI_BLOCK_CHANGE_INFO_CLASS, MethodType.methodType(void.class, short.class, BlockState));

        // a - chunk
        //                Field sectionPositionField = playOutMultiBlockChangeClass.getDeclaredField("a");
        //                sectionPositionField.setAccessible(true);
        //                chunkWrapperSet = lookup.unreflectSetter(sectionPositionField);

        // b - shorts
        //                Field shortsField = playOutMultiBlockChangeClass.getDeclaredField("b");
        //                shortsField.setAccessible(true);
        //                shortsOrInfo = lookup.unreflectSetter(shortsField);

        // c - block data
        //                Field blockDataField = playOutMultiBlockChangeClass.getDeclaredField("c");
        //                blockDataField.setAccessible(true);
        //                setBlockData = lookup.unreflectSetter(blockDataField);

        if (supports(1, 16)) {
            //                    Class<?> sectionPosClass = getNMSClass("SectionPosition");
            //                    chunkWrapper = lookup.findConstructor(sectionPosClass, MethodType.methodType(int.class, int.class, int.class));
            CHUNK_WRAPPER = null;
        } else {
            CHUNK_WRAPPER = lookup.findConstructor(chunkCoordIntPairClass, MethodType.methodType(void.class, int.class, int.class));
        }
        PLAY_OUT_MULTI_BLOCK_CHANGE_PACKET = null;
        MULTI_BLOCK_CHANGE_INFO = null;
        CHUNK_WRAPPER_SET = null;
        SHORTS_OR_INFO = null;
        SET_BlockState = null;
    }

    public static class WorldlessBlockWrapper {
        public final Block block;

        public WorldlessBlockWrapper(Block block) {
            this.block = block;
        }

        @Override
        public int hashCode() {
            return (block.getY() + block.getZ() * 31) * 31 + block.getX();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Block)) return false;

            Block other = (Block) obj;
            return block.getX() == other.getX()
                    && block.getY() == other.getY()
                    && block.getZ() == other.getZ();
        }
    }

    /**
     * Not completed yet. I have no idea.
     */
    @Deprecated
    private void sendBlockChange(Player player, Chunk chunk, Map<WorldlessBlockWrapper, Object> blocks) {
        try {
            Object packet = PLAY_OUT_MULTI_BLOCK_CHANGE_PACKET.invoke();

            if (supports(1, 16)) {
                Object wrapper = CHUNK_WRAPPER.invoke(chunk.getX(), chunk.getZ());
                CHUNK_WRAPPER_SET.invoke(wrapper);

                Object dataArray = Array.newInstance(BlockState, blocks.size());
                Object shortArray = Array.newInstance(short.class, blocks.size());

                int i = 0;
                for (Map.Entry<WorldlessBlockWrapper, Object> entry : blocks.entrySet()) {
                    Block loc = entry.getKey().block;
                    int x = loc.getX() & 15;
                    int y = loc.getY() & 15;
                    int z = loc.getZ() & 15;
                    i++;
                }

                SHORTS_OR_INFO.invoke(packet, shortArray);
                SET_BlockState.invoke(packet, dataArray);
            } else {
                Object wrapper = CHUNK_WRAPPER.invoke(chunk.getX(), chunk.getZ());
                CHUNK_WRAPPER_SET.invoke(wrapper);

                Object array = Array.newInstance(null/* MULTI_BLOCK_CHANGE_INFO_CLASS */, blocks.size());
                int i = 0;
                for (Map.Entry<WorldlessBlockWrapper, Object> entry : blocks.entrySet()) {
                    Block loc = entry.getKey().block;
                    int x = loc.getX() & 15;
                    int z = loc.getZ() & 15;
                    i++;
                }

                SHORTS_OR_INFO.invoke(packet, array);
            }

            MinecraftConnection.sendPacket(player, packet);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void chest(Block chest, boolean open) {
        Location location = chest.getLocation();
        try {
            Object world = MinecraftCapabilities.WORLD_HANDLE.invoke(location.getWorld());
            Object position = v(1, 19,
                    () ->
                    {
                        try {
                            return BlockPos_ctor.invoke(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                        } catch (Throwable e) {
                            throw new IllegalArgumentException("Failed to set block position", e);
                        }
                    }).orElse(
                    () ->
                    {
                        try {
                            return BlockPos_ctor.invoke(location.getX(), location.getY(), location.getZ());
                        } catch (Throwable e) {
                            throw new IllegalArgumentException("Failed to set block position", e);
                        }
                    });
            Object block = GET_BLOCK.invoke(GET_BLOCK_TYPE.invoke(world, position));
            PLAY_BLOCK_ACTION.invoke(world, position, block, 1, open ? 1 : 0);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}