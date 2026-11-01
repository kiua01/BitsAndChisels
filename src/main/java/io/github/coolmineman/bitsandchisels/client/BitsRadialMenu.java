package io.github.coolmineman.bitsandchisels.client;

import io.github.coolmineman.bitsandchisels.BitsAndChisels;
import io.github.coolmineman.bitsandchisels.buildmode.BuildMode;
import io.github.coolmineman.bitsandchisels.buildmode.BuildModeState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Combined radial: Bits tools (inner labels) + Effortless-style build modes.
 * Toggle with {@code .bits}. Second toggle applies the hovered entry.
 */
public final class BitsRadialMenu {
    public static boolean open;
    private static int hovered = -1;

    private enum Kind { TOOL, MODE }

    private record Entry(String label, Kind kind, Item item, BuildMode mode) {
        static Entry tool(String label, Item item) { return new Entry(label, Kind.TOOL, item, null); }
        static Entry mode(BuildMode mode) { return new Entry(mode.label, Kind.MODE, null, mode); }
    }

    private static final Entry[] ENTRIES = {
        Entry.mode(BuildMode.DISABLED),
        Entry.mode(BuildMode.SINGLE),
        Entry.mode(BuildMode.LINE),
        Entry.mode(BuildMode.WALL),
        Entry.mode(BuildMode.FLOOR),
        Entry.mode(BuildMode.CUBE),
        Entry.mode(BuildMode.DIAGONAL_LINE),
        Entry.mode(BuildMode.CIRCLE),
        Entry.mode(BuildMode.SPHERE),
        Entry.tool("Diamond Chisel", BitsAndChisels.DIAMOND_CHISEL),
        Entry.tool("Iron Chisel", BitsAndChisels.IRON_CHISEL),
        Entry.tool("Smart Chisel", BitsAndChisels.SMART_CHISEL),
        Entry.tool("Wrench", BitsAndChisels.WRENCH),
        Entry.tool("Blueprint", BitsAndChisels.BLUEPRINT),
    };

    private BitsRadialMenu() {}

    public static void toggle() {
        if (open) close(true);
        else open = true;
    }

    public static void close(boolean apply) {
        if (open && apply && hovered >= 0 && hovered < ENTRIES.length) {
            apply(ENTRIES[hovered]);
        }
        open = false;
        hovered = -1;
    }

    public static void render(GuiGraphicsExtractor graphics) {
        if (!open) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        double cx = screenW / 2.0;
        double cy = screenH / 2.0;
        double radius = Math.min(screenW, screenH) * 0.28;
        int n = ENTRIES.length;
        double slice = Math.PI * 2.0 / n;

        double mx = mc.mouseHandler.xpos() * screenW / (double) mc.getWindow().getWidth() - cx;
        double my = mc.mouseHandler.ypos() * screenH / (double) mc.getWindow().getHeight() - cy;
        double angle = Math.atan2(my, mx);
        if (angle < 0) angle += Math.PI * 2.0;
        double shifted = (angle + Math.PI / 2.0) % (Math.PI * 2.0);
        hovered = (int) (shifted / slice);
        if (hovered >= n) hovered = n - 1;

        Font font = mc.font;
        for (int i = 0; i < n; i++) {
            double am = -Math.PI / 2.0 + i * slice + slice / 2.0;
            double ix = cx + Math.cos(am) * radius;
            double iy = cy + Math.sin(am) * radius;
            int color = (i == hovered) ? 0xFFFFFF : (ENTRIES[i].kind == Kind.MODE ? 0x7FDBFF : 0xC0C0C0);
            String label = ENTRIES[i].label;
            int tw = font.width(label);
            graphics.text(font, label, (int) (ix - tw / 2.0), (int) iy - 4, color, true);
        }

        String title = hovered >= 0 ? ENTRIES[hovered].label : "Bits + Build";
        String modeLine = "Mode: " + BuildModeState.mode.label;
        int tw = font.width(title);
        graphics.text(font, title, (int) (cx - tw / 2.0), (int) (cy - radius - 22), 0xFFFFFF, true);
        int mw = font.width(modeLine);
        graphics.text(font, modeLine, (int) (cx - mw / 2.0), (int) (cy - radius - 10), 0xA0E0FF, true);
        String hint = ".bits again to select";
        int hw = font.width(hint);
        graphics.text(font, hint, (int) (cx - hw / 2.0), (int) (cy + radius + 10), 0x808080, true);
    }

    private static void apply(Entry entry) {
        if (entry.kind == Kind.MODE) {
            BuildModeState.setMode(entry.mode);
            BitsAndChisels.LOGGER.info("[Bits] Build mode: {}", entry.mode.label);
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        int invSlot = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(entry.item)) {
                invSlot = i;
                break;
            }
        }
        if (invSlot < 0) {
            if (player.getAbilities().instabuild) {
                player.getInventory().add(new ItemStack(entry.item));
            }
            BitsAndChisels.LOGGER.info("[Bits] Tool: {}", entry.label);
            return;
        }
        int hotbar = player.getInventory().getSelectedSlot();
        if (invSlot != hotbar) {
            ItemStack hot = player.getInventory().getItem(hotbar).copy();
            ItemStack picked = player.getInventory().getItem(invSlot).copy();
            player.getInventory().setItem(hotbar, picked);
            player.getInventory().setItem(invSlot, hot);
        }
        BitsAndChisels.LOGGER.info("[Bits] Selected {}", entry.label);
    }
}
