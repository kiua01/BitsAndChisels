package io.github.coolmineman.bitsandchisels.buildmode;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/** Client+server placement session for the active build mode. */
public final class BuildModeState {
    public static BuildMode mode = BuildMode.DISABLED;

    public static final List<BlockPos> clicks = new ArrayList<>();

    private BuildModeState() {}

    public static void setMode(BuildMode m) {
        mode = m == null ? BuildMode.DISABLED : m;
        clicks.clear();
    }

    public static void clearClicks() {
        clicks.clear();
    }

    public static void addClick(BlockPos pos) {
        clicks.add(pos.immutable());
    }
}
