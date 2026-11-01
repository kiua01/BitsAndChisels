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

/** HUD preview: shows click progress + estimated block count for current mode. */
public final class BuildPreview {
    private BuildPreview() {}

    public static void render(GuiGraphicsExtractor graphics) {
        if (BitsRadialMenu.open) return;
        if (BuildModeState.mode == BuildMode.DISABLED) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!(mc.player.getMainHandItem().getItem() instanceof BlockItem)) return;

        Font font = mc.font;
        List<String> lines = new ArrayList<>();
        lines.add("Build: " + BuildModeState.mode.label);
        int need = Math.max(1, BuildModeState.mode.clicks);
        lines.add("Corners: " + BuildModeState.clicks.size() + "/" + need);

        // Live preview count with current look-at as next corner
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hit = (BlockHitResult) mc.hitResult;
            BlockPos next = hit.getBlockPos().relative(hit.getDirection());
            List<BlockPos> trial = new ArrayList<>(BuildModeState.clicks);
            trial.add(next);
            if (trial.size() >= need) {
                // only use required clicks
                while (trial.size() > need) trial.remove(trial.size() - 1);
                int count = ShapeGenerator.generate(BuildModeState.mode, trial).size();
                lines.add("Blocks: ~" + count);
            } else {
                lines.add("Right-click next corner");
            }
        }

        int y = mc.getWindow().getGuiScaledHeight() - 60;
        for (String line : lines) {
            graphics.text(font, line, 6, y, 0xA0E0FF, true);
            y += font.lineHeight + 1;
        }
    }
}
