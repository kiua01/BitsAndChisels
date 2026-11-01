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
 * iOS-inspired radial menu (Control Center / share-sheet style).
 */
public final class BitsRadialMenu {
    public enum Page { BUILD, CHISEL }

    public static boolean open;
    public static Page page = Page.BUILD;
    private static int hovered = -1;

    private static final int IOS_BLUE = 0x007AFF;
    private static final int IOS_LABEL = 0xFFFFFF;
    private static final int IOS_SECONDARY = 0xAEAEB2;
    private static final int IOS_TERTIARY = 0x8E8E93;
    private static final int IOS_FILL = 0x2C2C2E;
    private static final int IOS_GREEN = 0x30D158;
    private static final int IOS_ORANGE = 0xFF9F0A;
    private static final int IOS_PURPLE = 0xBF5AF2;
    private static final int IOS_TEAL = 0x64D2FF;

    private enum Kind { MODE, TOOL, OPEN_CHISEL, BACK }

    private record Entry(String label, String glyph, Kind kind, Item item, BuildMode mode, int tint) {
        static Entry mode(BuildMode m, String glyph, int tint) {
            return new Entry(m.label, glyph, Kind.MODE, null, m, tint);
        }
        static Entry tool(String label, String glyph, Item item) {
            return new Entry(label, glyph, Kind.TOOL, item, null, IOS_ORANGE);
        }
        static Entry openChisel() {
            return new Entry("Chisel & Bits", "◆", Kind.OPEN_CHISEL, null, null, IOS_ORANGE);
        }
        static Entry back() {
            return new Entry("Back", "‹", Kind.BACK, null, null, IOS_SECONDARY);
        }
    }

    private static final Entry[] BUILD_ENTRIES = {
        Entry.mode(BuildMode.DISABLED, "○", IOS_SECONDARY),
        Entry.mode(BuildMode.SINGLE, "●", IOS_LABEL),
        Entry.mode(BuildMode.LINE, "─", IOS_TEAL),
        Entry.mode(BuildMode.WALL, "▮", IOS_TEAL),
        Entry.mode(BuildMode.FLOOR, "▬", IOS_TEAL),
        Entry.mode(BuildMode.CUBE, "■", IOS_GREEN),
        Entry.mode(BuildMode.DIAGONAL_LINE, "╱", IOS_PURPLE),
        Entry.mode(BuildMode.CIRCLE, "◌", IOS_BLUE),
        Entry.mode(BuildMode.SPHERE, "◉", IOS_BLUE),
        Entry.openChisel(),
    };

    private static final Entry[] CHISEL_ENTRIES = {
        Entry.back(),
        Entry.tool("Diamond", "✦", BitsAndChisels.DIAMOND_CHISEL),
        Entry.tool("Iron", "✧", BitsAndChisels.IRON_CHISEL),
        Entry.tool("Smart", "✪", BitsAndChisels.SMART_CHISEL),
        Entry.tool("Wrench", "⚒", BitsAndChisels.WRENCH),
        Entry.tool("Blueprint", "☰", BitsAndChisels.BLUEPRINT),
        Entry.tool("Bit", "▪", BitsAndChisels.BIT_ITEM),
    };

    private BitsRadialMenu() {}

    private static Entry[] current() {
        return page == Page.CHISEL ? CHISEL_ENTRIES : BUILD_ENTRIES;
    }

    public static void toggle() {
        if (open) close(true);
        else open = true;
    }

    public static void close(boolean apply) {
        if (open && apply && hovered >= 0) {
            Entry[] entries = current();
            if (hovered < entries.length) {
                Entry e = entries[hovered];
                applyEntry(e);
                if (e.kind == Kind.OPEN_CHISEL || e.kind == Kind.BACK) {
                    hovered = -1;
                    return;
                }
            }
        }
        open = false;
        hovered = -1;
    }

    public static void render(GuiGraphicsExtractor graphics) {
        if (!open) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Font font = mc.font;
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        double cx = screenW / 2.0;
        double cy = screenH / 2.0;
        double outerR = Math.min(screenW, screenH) * 0.32;
        double iconR = outerR * 0.72;
        double hubR = outerR * 0.28;

        Entry[] entries = current();
        int n = entries.length;
        double slice = Math.PI * 2.0 / n;

        double mx = mc.mouseHandler.xpos() * screenW / (double) mc.getWindow().getWidth() - cx;
        double my = mc.mouseHandler.ypos() * screenH / (double) mc.getWindow().getHeight() - cy;
        double dist = Math.sqrt(mx * mx + my * my);
        if (dist < hubR) {
            hovered = -1;
        } else {
            double angle = Math.atan2(my, mx);
            if (angle < 0) angle += Math.PI * 2.0;
            double shifted = (angle + Math.PI / 2.0) % (Math.PI * 2.0);
            hovered = (int) (shifted / slice);
            if (hovered >= n) hovered = n - 1;
        }

        String title = page == Page.CHISEL ? "Chisel & Bits" : "Build";
        int titleW = font.width(title);
        graphics.text(font, title, (int) (cx - titleW / 2.0), (int) (cy - outerR - 28), IOS_LABEL, true);

        String subtitle = page == Page.CHISEL ? "Choose a tool" : "Choose a mode";
        int subW = font.width(subtitle);
        graphics.text(font, subtitle, (int) (cx - subW / 2.0), (int) (cy - outerR - 16), IOS_TERTIARY, true);

        for (int i = 0; i < n; i++) {
            double am = -Math.PI / 2.0 + i * slice + slice / 2.0;
            double ix = cx + Math.cos(am) * iconR;
            double iy = cy + Math.sin(am) * iconR;
            boolean hi = i == hovered;
            Entry e = entries[i];

            int glyphColor = hi ? IOS_BLUE : e.tint;
            if (hi) {
                String ring = "◎";
                int rw = font.width(ring);
                graphics.text(font, ring, (int) (ix - rw / 2.0), (int) iy - 10, IOS_BLUE, true);
            } else {
                String ring = "○";
                int rw = font.width(ring);
                graphics.text(font, ring, (int) (ix - rw / 2.0), (int) iy - 10, IOS_FILL, true);
            }
            int gw = font.width(e.glyph);
            graphics.text(font, e.glyph, (int) (ix - gw / 2.0), (int) iy - 4, glyphColor, true);

            String caption = shortLabel(e.label);
            int cw = font.width(caption);
            graphics.text(font, caption, (int) (ix - cw / 2.0), (int) iy + 10, hi ? IOS_LABEL : IOS_SECONDARY, true);
        }

        String hubMain;
        if (hovered >= 0 && hovered < n) hubMain = entries[hovered].label;
        else if (page == Page.CHISEL) hubMain = "Tools";
        else hubMain = BuildModeState.mode.label;
        int hmW = font.width(hubMain);
        graphics.text(font, hubMain, (int) (cx - hmW / 2.0), (int) cy - 6, IOS_LABEL, true);

        String hubSub = ".bits to confirm";
        int hsW = font.width(hubSub);
        graphics.text(font, hubSub, (int) (cx - hsW / 2.0), (int) cy + 6, IOS_TERTIARY, true);

        String home = "———";
        int homeW = font.width(home);
        graphics.text(font, home, (int) (cx - homeW / 2.0), (int) (cy + outerR + 14), IOS_TERTIARY, true);
    }

    private static String shortLabel(String s) {
        if (s == null) return "";
        if (s.equals("Chisel & Bits")) return "Chisel";
        if (s.equals("Diagonal Line")) return "Diagonal";
        if (s.length() > 10) return s.substring(0, 9) + "…";
        return s;
    }

    private static void applyEntry(Entry entry) {
        switch (entry.kind) {
            case OPEN_CHISEL -> {
                page = Page.CHISEL;
                open = true;
                hovered = -1;
                BitsAndChisels.LOGGER.info("[Bits] Chisel & Bits");
            }
            case BACK -> {
                page = Page.BUILD;
                open = true;
                hovered = -1;
            }
            case MODE -> {
                BuildModeState.setMode(entry.mode);
                BitsAndChisels.LOGGER.info("[Bits] Mode → {}", entry.mode.label);
            }
            case TOOL -> selectTool(entry);
        }
    }

    private static void selectTool(Entry entry) {
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
            BitsAndChisels.LOGGER.info("[Bits] {}", entry.label);
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
        BitsAndChisels.LOGGER.info("[Bits] Selected {}", entry.label);
    }
}
