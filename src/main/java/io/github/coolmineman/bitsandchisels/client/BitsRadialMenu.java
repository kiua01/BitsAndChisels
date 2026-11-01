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
 * Inventory-style glass panel menu (HUD overlay — works on 26.3 without Screen API).
 * Left rail categories + main grid, translucent panels.
 */
public final class BitsRadialMenu {
    public enum Page { BUILD, CHISEL }

    public static boolean open;
    public static Page page = Page.BUILD;
    private static int hovered = -1;
    private static int hoverRail = -1; // 0=build 1=chisel

    private static final int SCRIM = 0x99000000;
    private static final int GLASS = 0xCC1C1C1E;
    private static final int GLASS2 = 0xB32C2C2E;
    private static final int EDGE = 0x55FFFFFF;
    private static final int ACCENT = 0xFF007AFF;
    private static final int SLOT = 0xFF3A3A3C;
    private static final int SLOT_HOT = 0xFF48484A;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int DIM = 0xFFAEAEB2;

    private enum Kind { MODE, TOOL, OPEN_CHISEL }

    private record Entry(String label, String glyph, Kind kind, Item item, BuildMode mode) {}

    private static final Entry[] BUILD = {
        new Entry("Disabled", "○", Kind.MODE, null, BuildMode.DISABLED),
        new Entry("Single", "●", Kind.MODE, null, BuildMode.SINGLE),
        new Entry("Line", "─", Kind.MODE, null, BuildMode.LINE),
        new Entry("Wall", "▮", Kind.MODE, null, BuildMode.WALL),
        new Entry("Floor", "▬", Kind.MODE, null, BuildMode.FLOOR),
        new Entry("Cube", "■", Kind.MODE, null, BuildMode.CUBE),
        new Entry("Diagonal", "╱", Kind.MODE, null, BuildMode.DIAGONAL_LINE),
        new Entry("Circle", "◌", Kind.MODE, null, BuildMode.CIRCLE),
        new Entry("Sphere", "◉", Kind.MODE, null, BuildMode.SPHERE),
        new Entry("Chisel & Bits", "◆", Kind.OPEN_CHISEL, null, null),
    };

    private static final Entry[] CHISEL = {
        new Entry("Diamond", "✦", Kind.TOOL, BitsAndChisels.DIAMOND_CHISEL, null),
        new Entry("Iron", "✧", Kind.TOOL, BitsAndChisels.IRON_CHISEL, null),
        new Entry("Smart", "✪", Kind.TOOL, BitsAndChisels.SMART_CHISEL, null),
        new Entry("Wrench", "⚒", Kind.TOOL, BitsAndChisels.WRENCH, null),
        new Entry("Blueprint", "☰", Kind.TOOL, BitsAndChisels.BLUEPRINT, null),
        new Entry("Bit", "▪", Kind.TOOL, BitsAndChisels.BIT_ITEM, null),
    };

    private BitsRadialMenu() {}

    private static Entry[] list() {
        return page == Page.CHISEL ? CHISEL : BUILD;
    }

    public static void toggle() {
        if (open) close(true);
        else open = true;
    }

    public static void close(boolean apply) {
        if (open && apply) {
            if (hoverRail == 0) { page = Page.BUILD; open = true; hovered = -1; hoverRail = -1; return; }
            if (hoverRail == 1) { page = Page.CHISEL; open = true; hovered = -1; hoverRail = -1; return; }
            if (hovered >= 0) {
                Entry[] entries = list();
                if (hovered < entries.length) {
                    Entry e = entries[hovered];
                    if (e.kind == Kind.OPEN_CHISEL) {
                        page = Page.CHISEL;
                        open = true;
                        hovered = -1;
                        return;
                    }
                    apply(e);
                }
            }
        }
        open = false;
        hovered = -1;
        hoverRail = -1;
    }

    public static void render(GuiGraphicsExtractor g) {
        if (!open) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        Font font = mc.font;
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        // Scrim
        tryFill(g, 0, 0, sw, sh, SCRIM);

        int panelW = Math.min(420, sw - 40);
        int panelH = Math.min(260, sh - 40);
        int px = (sw - panelW) / 2;
        int py = (sh - panelH) / 2;
        int railW = 64;

        // Glass panels
        tryFill(g, px - 1, py - 1, px + panelW + 1, py + panelH + 1, EDGE);
        tryFill(g, px, py, px + panelW, py + panelH, GLASS);
        tryFill(g, px + 4, py + 4, px + railW, py + panelH - 4, GLASS2);

        // Mouse
        double mx = mc.mouseHandler.xpos() * sw / (double) mc.getWindow().getWidth();
        double my = mc.mouseHandler.ypos() * sh / (double) mc.getWindow().getHeight();

        // Rail
        hoverRail = -1;
        drawRail(g, font, px + 8, py + 18, railW - 16, 40, page == Page.BUILD, "Build", mx, my, 0);
        drawRail(g, font, px + 8, py + 68, railW - 16, 40, page == Page.CHISEL, "Chisel", mx, my, 1);

        // Title
        String title = page == Page.CHISEL ? "Chisel & Bits" : "Build Modes";
        g.text(font, title, px + railW + 12, py + 12, TEXT, false);
        String sub = page == Page.CHISEL ? "Pick a tool" : "Mode: " + BuildModeState.mode.label;
        g.text(font, sub, px + railW + 12, py + 24, DIM, false);

        // Grid
        Entry[] entries = list();
        int cols = 5;
        int slot = 40;
        int gap = 6;
        int gridX = px + railW + 12;
        int gridY = py + 44;
        hovered = -1;

        for (int i = 0; i < entries.length; i++) {
            int col = i % cols;
            int row = i / cols;
            int sx = gridX + col * (slot + gap);
            int sy = gridY + row * (slot + gap);
            boolean hot = mx >= sx && mx < sx + slot && my >= sy && my < sy + slot;
            if (hot) hovered = i;

            tryFill(g, sx, sy, sx + slot, sy + slot, hot ? SLOT_HOT : SLOT);
            if (hot) {
                tryFill(g, sx, sy, sx + slot, sy + 2, ACCENT);
            }
            Entry e = entries[i];
            int gw = font.width(e.glyph);
            g.text(font, e.glyph, sx + (slot - gw) / 2, sy + 10, hot ? ACCENT : TEXT, false);
            int cw = font.width(e.label);
            if (cw < slot - 2) {
                g.text(font, e.label, sx + (slot - cw) / 2, sy + slot - 12, DIM, false);
            }
        }

        String pageLabel = (page == Page.BUILD ? "1" : "2") + " / 2";
        int pw = font.width(pageLabel);
        g.text(font, pageLabel, px + panelW - pw - 12, py + panelH - 16, DIM, false);

        String hint = ".bits to confirm · ESC via .bits";
        int hw = font.width(hint);
        g.text(font, hint, px + (panelW - hw) / 2, py + panelH - 16, DIM, false);
    }

    private static void drawRail(GuiGraphicsExtractor g, Font font, int x, int y, int w, int h,
                                 boolean selected, String label, double mx, double my, int id) {
        boolean hot = mx >= x && mx < x + w && my >= y && my < y + h;
        if (hot) hoverRail = id;
        tryFill(g, x, y, x + w, y + h, selected ? 0x66007AFF : (hot ? SLOT_HOT : SLOT));
        if (selected) tryFill(g, x, y, x + 3, y + h, ACCENT);
        int tw = font.width(label);
        g.text(font, label, x + (w - tw) / 2, y + h / 2 - 4, selected ? TEXT : DIM, false);
    }

    private static void tryFill(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int argb) {
        try {
            g.fill(x0, y0, x1, y1, argb);
        } catch (Throwable t) {
            // fill may not exist on some extractors — ignore
        }
    }

    private static void apply(Entry e) {
        if (e.kind == Kind.MODE && e.mode != null) {
            BuildModeState.setMode(e.mode);
            BitsAndChisels.LOGGER.info("[Bits] Mode → {}", e.mode.label);
            return;
        }
        if (e.kind == Kind.TOOL && e.item != null) {
            selectTool(e.item, e.label);
        }
    }

    private static void selectTool(Item item, String label) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        int invSlot = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(item)) { invSlot = i; break; }
        }
        if (invSlot < 0) {
            if (player.getAbilities().instabuild) player.getInventory().add(new ItemStack(item));
            return;
        }
        int hotbar = player.getInventory().getSelectedSlot();
        if (invSlot != hotbar) {
            ItemStack hot = player.getInventory().getItem(hotbar).copy();
            ItemStack picked = player.getInventory().getItem(invSlot).copy();
            player.getInventory().setItem(hotbar, picked);
            player.getInventory().setItem(invSlot, hot);
        }
        BuildModeState.setMode(BuildMode.DISABLED);
        BitsAndChisels.LOGGER.info("[Bits] Selected {}", label);
    }
}
