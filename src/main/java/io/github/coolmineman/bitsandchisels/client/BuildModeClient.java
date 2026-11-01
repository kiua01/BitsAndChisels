package io.github.coolmineman.bitsandchisels.client;

import io.github.coolmineman.bitsandchisels.BitsAndChisels;
import io.github.coolmineman.bitsandchisels.buildmode.BuildMode;
import io.github.coolmineman.bitsandchisels.buildmode.BuildModeState;
import io.github.coolmineman.bitsandchisels.network.BuildPlacePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;

import java.util.ArrayList;

/** Right-click accumulates corners then sends place packet. */
public final class BuildModeClient {
    private BuildModeClient() {}

    public static void register() {
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (!level.isClientSide()) return InteractionResult.PASS;
            if (BuildModeState.mode == BuildMode.DISABLED) return InteractionResult.PASS;
            if (!(player.getItemInHand(hand).getItem() instanceof BlockItem)) return InteractionResult.PASS;
            if (BitsRadialMenu.open) return InteractionResult.PASS;

            BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
            BuildModeState.addClick(pos);

            int need = BuildModeState.mode.clicks;
            if (BuildModeState.mode == BuildMode.SINGLE) need = 1;
            if (BuildModeState.clicks.size() >= need) {
                ClientPlayNetworking.send(new BuildPlacePayload(
                    BuildModeState.mode.ordinal(),
                    new ArrayList<>(BuildModeState.clicks)
                ));
                BitsAndChisels.LOGGER.info("[Bits] Placing {} ({} clicks)", BuildModeState.mode.label, BuildModeState.clicks.size());
                BuildModeState.clearClicks();
            } else {
                BitsAndChisels.LOGGER.info("[Bits] {} corner {}/{}", BuildModeState.mode.label, BuildModeState.clicks.size(), need);
            }
            return InteractionResult.SUCCESS;
        });
    }
}
