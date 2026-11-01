package io.github.coolmineman.bitsandchisels.network;

import io.github.coolmineman.bitsandchisels.BitsAndChisels;
import io.github.coolmineman.bitsandchisels.buildmode.BuildMode;
import io.github.coolmineman.bitsandchisels.buildmode.ShapeGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** Client → server: finish a build-mode placement. */
public record BuildPlacePayload(int modeOrdinal, List<BlockPos> clicks) implements CustomPacketPayload {
    public static final Type<BuildPlacePayload> TYPE = new Type<>(BitsAndChisels.id("build_place"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BuildPlacePayload> CODEC =
        CustomPacketPayload.codec(BuildPlacePayload::write, BuildPlacePayload::read);

    private static BuildPlacePayload read(RegistryFriendlyByteBuf buf) {
        int mode = buf.readVarInt();
        int n = buf.readVarInt();
        List<BlockPos> clicks = new ArrayList<>(n);
        for (int i = 0; i < n; i++) clicks.add(buf.readBlockPos());
        return new BuildPlacePayload(mode, clicks);
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(modeOrdinal);
        buf.writeVarInt(clicks.size());
        for (BlockPos p : clicks) buf.writeBlockPos(p);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer player) {
        BuildMode[] values = BuildMode.values();
        if (modeOrdinal < 0 || modeOrdinal >= values.length) return;
        BuildMode mode = values[modeOrdinal];
        if (mode == BuildMode.DISABLED || clicks.isEmpty()) return;
        if (player.blockPosition().distSqr(clicks.get(0)) > 128 * 128) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof BlockItem blockItem)) return;
        BlockState state = blockItem.getBlock().defaultBlockState();

        List<BlockPos> positions = ShapeGenerator.generate(mode, clicks);
        int placed = 0;
        for (BlockPos pos : positions) {
            if (placed >= 4096) break;
            if (!player.level().getBlockState(pos).canBeReplaced()) continue;
            if (!player.isCreative()) {
                if (stack.isEmpty()) break;
            }
            boolean ok = player.level().setBlock(pos, state, Block.UPDATE_ALL);
            if (ok) {
                placed++;
                if (!player.isCreative()) {
                    stack.shrink(1);
                    if (stack.isEmpty()) break;
                }
            }
        }
        BitsAndChisels.LOGGER.info("BuildMode {} placed {} blocks for {}", mode.label, placed, player.getGameProfile().getName());
    }
}
