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
 * Effortless Building–style radial menu.
 * <p>
 * Page BUILD: shape modes (line, wall, cube, …)<br>
 * Page CHISEL: Bits & Chisels tools (opens when you pick “Chisel & Bits”)
 * <p>
 * Toggle with {@code .bits}. Hover a slice, toggle again to select.
 */
public final class BitsRadialMenu {
    public enum Page { BUILD, CHISEL }

    public static boolean open;
    public static Page page = Page.BUILD;
    private static int hovered = -1;

    private enum Kind { MODE, TOOL, OPEN_CHISEL, BACK }

    private record Entry(String label, Kind kind, Item item, BuildMode mode, int color) {
        static Entry mode(BuildMode m, int color) {
            return new Entry(m.label, Kind.MODE, null, m, color);
        }
        static Entry tool(String label, Item item) {
            return new Entry(label, Kind.TOOL, item, null, 0xE0E0E0);
        }
        static Entry openChisel() {
            return new Entry("Chisel & Bits", Kind.OPEN_CHISEL, null, null, 0xFFD700);
        }
        static Entry back() {
            return new Entry("← Build modes", Kind.BACK, null, null, 0xAAAAAA);
        }
    }

    // Effortless-like grouping colors
    private static final Entry[] BUILD_ENTRIES = {
        Entry.mode(BuildMode.DISABLED, 0x888888),
        Entry.mode(BuildMode.SINGLE, 0xFFFFFF),
        Entry.mode(BuildMode.LINE, 0x7FDBFF),
        Entry.mode(BuildMode.WALL, 0x7FDBFF),
        Entry.mode(BuildMode.FLOOR, 0x7FDBFF),
        Entry.mode(BuildMode.CUBE, 0x2ECC40),
        Entry.mode(BuildMode.DIAGONAL_LINE, 0xB10DC9),
        Entry.mode(BuildMode.CIRCLE, 0x0074D9),
        Entry.mode(BuildMode.SPHERE, 0x0074D9),
        Entry.openChisel(),
    };

    private static final Entry[] CHISEL_ENTRIES = {
        Entry.back(),
        Entry.tool("Diamond Chisel", BitsAndChisels.DIAMOND_CHISEL),
        Entry.tool("Iron Chisel", BitsAndChisels.IRON_CHISEL),
        Entry.tool("Smart Chisel", BitsAndChisels.SMART_CHISEL),
        Entry.tool("Wrench", BitsAndChisels.WRENCH),
        Entry.tool("Blueprint", BitsAndChisels.BLUEPRINT),
        Entry.tool("Bit Item", BitsAndChisels.BIT_ITEM),
    };

    private BitsRadialMenu() {}

    private static Entry[] current() {
        return page == Page.CHISEL ? CHISEL_ENTRIES : BUILD_ENTRIES;
    }

    public static void toggle() {
        if (open) close(true);
        else {
            open = true;
            // stay on current page
        }
    }

    public static void close(boolean apply) {
        if (open && apply && hovered >= 0) {
            Entry[] entries = current();
            if (hovered < entries.length) {
                Entry e = entries[hovered];
                apply(e);
                // Stay open when switching pages (Chisel & Bits / Back)
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

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        double cx = screenW / 2.0;
        double cy = screenH / 2.0;
        double outerR = Math.min(screenW, screenH) * 0.30;
        double innerR = outerR * 0.35;
        Entry[] entries = current();
        int n = entries.length;
        double slice = Math.PI * 2.0 / n;

        // Mouse → hovered slice
        double mx = mc.mouseHandler.xpos() * screenW / (double) mc.getWindow().getWidth() - cx;
        double my = mc.mouseHandler.ypos() * screenH / (double) mc.getWindow().getHeight() - cy;
        double dist = Math.sqrt(mx * mx + my * my);
        if (dist < innerR * 0.5) {
            hovered = -1; // dead zone center
        } else {
            double angle = Math.atan2(my, mx);
            if (angle < 0) angle += Math.PI * 2.0;
            double shifted = (angle + Math.PI / 2.0) % (Math.PI * 2.0);
            hovered = (int) (shifted / slice);
            if (hovered >= n) hovered = n - 1;
        }

        Font font = mc.font;

        // Draw ring labels (Effortless style: text on each wedge)
        for (int i = 0; i < n; i++) {
            double a0 = -Math.PI / 2.0 + i * slice;
            double am = a0 + slice / 2.0;
            double rLabel = (innerR + outerR) * 0.55;
            double ix = cx + Math.cos(am) * rLabel;
            double iy = cy + Math.sin(am) * rLabel;

            boolean hi = i == hovered;
            int color = hi ? 0xFFFFFF : entries[i].color;
            String label = entries[i].label;
            // shorten long labels
            if (label.length() > 14) label = label.substring(0, 12) + "…";
            int tw = font.width(label);
            graphics.text(font, label, (int) (ix - tw / 2.0), (int) iy - 4, color, true);

            // simple wedge marker for hovered
            if (hi) {
                String mark = "●";
                int mw = font.width(mark);
                double rm = outerR * 0.92;
                graphics.text(font, mark,
                    (int) (cx + Math.cos(am) * rm - mw / 2.0),
                    (int) (cy + Math.sin(am) * rm - 4),
                    0xFFFFFF, true);
            }
        }

        // Center hub — current status
        String hub;
        if (page == Page.CHISEL) {
            hub = "Chisel & Bits";
        } else {
            hub = BuildModeState.mode.label;
        }
        int tw = font.width(hub);
        graphics.text(font, hub, (int) (cx - tw / 2.0), (int) cy - 10, 0xFFFFFF, true);

        String sub = page == Page.CHISEL ? "pick a tool" : "build mode";
        if (hovered >= 0 && hovered < n) {
            sub = entries[hovered].label;
        }
        int sw = font.width(sub);
        graphics.text(font, sub, (int) (cx - sw / 2.0), (int) cy + 2, 0xA0E0FF, true);

        String hint = ".bits to confirm";
        int hw = font.width(hint);
        graphics.text(font, hint, (int) (cx - hw / 2.0), (int) (cy + outerR + 8), 0x808080, true);
    }

    private static void apply(Entry entry) {
        switch (entry.kind) {
            case OPEN_CHISEL -> {
                page = Page.CHISEL;
                open = true; // stay open on chisel page
                hovered = -1;
                BitsAndChisels.LOGGER.info("[Bits] Opened Chisel & Bits menu");
            }
            case BACK -> {
                page = Page.BUILD;
                open = true;
                hovered = -1;
            }
            case MODE -> {
                BuildModeState.setMode(entry.mode);
                BitsAndChisels.LOGGER.info("[Bits] Build mode → {}", entry.mode.label);
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
        // Using a chisel disables mass-build mode so left-click carves bits
        BuildModeState.setMode(BuildMode.DISABLED);
        BitsAndChisels.LOGGER.info("[Bits] Selected {} (build mode off for chiseling)", entry.label);
    }
}
