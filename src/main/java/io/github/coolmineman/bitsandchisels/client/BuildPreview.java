package io.github.coolmineman.bitsandchisels.client;

import io.github.coolmineman.bitsandchisels.buildmode.BuildMode;
import io.github.coolmineman.bitsandchisels.buildmode.BuildModeState;
import io.github.coolmineman.bitsandchisels.buildmode.ShapeGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

public final class BuildPreview {
    private static final int IOS_BLUE = 0x007AFF;
    private static final int IOS_SECONDARY = 0xAEAEB2;

    private BuildPreview() {}

    public static void render(GuiGraphicsExtractor graphics) {
        if (BitsRadialMenu.open) return;
        if (BuildModeState.mode == BuildMode.DISABLED) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!(mc.player.getMainHandItem().getItem() instanceof BlockItem)) return;

        Font font = mc.font;
        int need = Math.max(1, BuildModeState.mode.clicks);
        String line1 = BuildModeState.mode.label;
        String line2 = BuildModeState.clicks.size() + " of " + need;

        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hit = (BlockHitResult) mc.hitResult;
            BlockPos next = hit.getBlockPos().relative(hit.getDirection());
            List<BlockPos> trial = new ArrayList<>(BuildModeState.clicks);
            trial.add(next);
            while (trial.size() > need) trial.remove(trial.size() - 1);
            if (trial.size() >= need) {
                int count = ShapeGenerator.generate(BuildModeState.mode, trial).size();
                line2 = "~" + count + " blocks";
            }
        }

        int x = 10;
        int y = mc.getWindow().getGuiScaledHeight() - 48;
        graphics.text(font, line1, x, y, IOS_BLUE, true);
        graphics.text(font, line2, x, y + font.lineHeight + 2, IOS_SECONDARY, true);
    }
}
