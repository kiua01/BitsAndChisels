package io.github.coolmineman.bitsandchisels.client;

import io.github.coolmineman.bitsandchisels.BitsAndChisels;
import io.github.coolmineman.bitsandchisels.chisel.ChiselItem;
import io.github.coolmineman.bitsandchisels.network.ChiselPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;

public final class BitsBlockClient implements ClientModInitializer {
    private static BlockPos smartOrigin;
    private static int smartX, smartY, smartZ;
    private static long lastBreakTick;
    private static boolean fastBreak;

    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(BitsAndChisels.BITS_BLOCK_ENTITY, BitsBlockEntityRenderer::new);

        BuildModeClient.register();

        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath(BitsAndChisels.MOD_ID, "radial"),
            (graphics, delta) -> BuildPreview.render(graphics)
        );

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            String msg = message.trim().toLowerCase();
            if (msg.equals(".bits") || msg.equals(".bits menu") || msg.equals(".chisel")) {
                Minecraft.getInstance().setScreen(new BitsMenuScreen());
                return false;
            }
            return true;
        });

        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            if (!level.isClientSide()) return InteractionResult.PASS;
            if (!(player.getItemInHand(hand).getItem() instanceof ChiselItem chisel)) return InteractionResult.PASS;
            if (!(Minecraft.getInstance().hitResult instanceof BlockHitResult hit)) return InteractionResult.PASS;

            long now = System.currentTimeMillis() / 50L;
            if (now - lastBreakTick < (fastBreak ? 3 : 6)) {
                return InteractionResult.SUCCESS; // consume swing, don't spam
            }

            int x = bitCoord(hit.getLocation().x - pos.getX(), hit.getDirection().getStepX());
            int y = bitCoord(hit.getLocation().y - pos.getY(), hit.getDirection().getStepY());
            int z = bitCoord(hit.getLocation().z - pos.getZ(), hit.getDirection().getStepZ());
            x = clampBit(x);
            y = clampBit(y);
            z = clampBit(z);

            switch (chisel.mode()) {
                case SINGLE -> ClientPlayNetworking.send(new ChiselPayload(pos, x, y, z, x, y, z, 0));
                case FOUR_BY_FOUR -> {
                    int x1 = (x / 4) * 4;
                    int y1 = (y / 4) * 4;
                    int z1 = (z / 4) * 4;
                    ClientPlayNetworking.send(new ChiselPayload(pos, x1, y1, z1, x1 + 3, y1 + 3, z1 + 3, 1));
                }
                case SMART -> handleSmart(pos, x, y, z);
            }

            fastBreak = now - lastBreakTick < 10;
            lastBreakTick = now;
            return InteractionResult.SUCCESS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                smartOrigin = null;
                return;
            }
            if (!(client.player.getMainHandItem().getItem() instanceof ChiselItem chisel)) {
                smartOrigin = null;
                return;
            }
            if (chisel.mode() != ChiselItem.Mode.SMART) {
                smartOrigin = null;
                return;
            }
            // Clear smart selection when not attacking
            if (!client.options.keyAttack.isDown()) {
                // keep origin until next attack starts a new region
            }
        });
    }

    private static void handleSmart(BlockPos pos, int x, int y, int z) {
        // First click: set origin. Second click: send region.
        if (smartOrigin == null) {
            smartOrigin = pos;
            smartX = x;
            smartY = y;
            smartZ = z;
            return;
        }
        // Convert second point into global bit coords relative to first origin
        int gx1 = smartX;
        int gy1 = smartY;
        int gz1 = smartZ;
        int gx2 = x + (pos.getX() - smartOrigin.getX()) * 16;
        int gy2 = y + (pos.getY() - smartOrigin.getY()) * 16;
        int gz2 = z + (pos.getZ() - smartOrigin.getZ()) * 16;
        ClientPlayNetworking.send(new ChiselPayload(smartOrigin, gx1, gy1, gz1, gx2, gy2, gz2, 2));
        smartOrigin = null;
    }

    private static int bitCoord(double local, int step) {
        return (int) Math.floor(local * 16.0 + step * -0.5);
    }

    private static int clampBit(int v) {
        if (v < 0) return 0;
        if (v > 15) return 15;
        return v;
    }
}
