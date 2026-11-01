package io.github.coolmineman.bitsandchisels.client;

import io.github.coolmineman.bitsandchisels.BitsAndChisels;
import io.github.coolmineman.bitsandchisels.buildmode.BuildMode;
import io.github.coolmineman.bitsandchisels.buildmode.BuildModeState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Full-screen Bits menu styled like a polished inventory panel:
 * left category rail + main grid + iOS-like translucent glass panels.
 */
public final class BitsMenuScreen extends Screen {
    // Glass / iOS palette
    private static final int GLASS_BG = 0xCC1C1C1E;      // ~80% dark glass
    private static final int GLASS_PANEL = 0xB32C2C2E;
    private static final int GLASS_EDGE = 0x66FFFFFF;
    private static final int ACCENT = 0xFF007AFF;       // iOS blue
    private static final int ACCENT_SOFT = 0x66007AFF;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFFAEAEB2;
    private static final int SLOT = 0xFF3A3A3C;
    private static final int SLOT_HOVER = 0xFF48484A;
    private static final int RAIL = 0xD91C1C1E;

    private enum Tab { BUILD, CHISEL }

    private Tab tab = Tab.BUILD;
    private int scroll;
    private int hoverIndex = -1;

    private final List<Entry> buildEntries = new ArrayList<>();
    private final List<Entry> chiselEntries = new ArrayList<>();

    private record Entry(String label, String glyph, BuildMode mode, Item item, boolean openChisel) {
        static Entry mode(BuildMode m, String g) { return new Entry(m.label, g, m, null, false); }
        static Entry tool(String name, String g, Item item) { return new Entry(name, g, null, item, false); }
        static Entry chiselTab() { return new Entry("Chisel & Bits", "◆", null, null, true); }
    }

    public BitsMenuScreen() {
        super(Component.literal("Bits & Build"));
        buildEntries.add(Entry.mode(BuildMode.DISABLED, "○"));
        buildEntries.add(Entry.mode(BuildMode.SINGLE, "●"));
        buildEntries.add(Entry.mode(BuildMode.LINE, "─"));
        buildEntries.add(Entry.mode(BuildMode.WALL, "▮"));
        buildEntries.add(Entry.mode(BuildMode.FLOOR, "▬"));
        buildEntries.add(Entry.mode(BuildMode.CUBE, "■"));
        buildEntries.add(Entry.mode(BuildMode.DIAGONAL_LINE, "╱"));
        buildEntries.add(Entry.mode(BuildMode.CIRCLE, "◌"));
        buildEntries.add(Entry.mode(BuildMode.SPHERE, "◉"));
        buildEntries.add(Entry.chiselTab());

        chiselEntries.add(Entry.tool("Diamond Chisel", "✦", BitsAndChisels.DIAMOND_CHISEL));
        chiselEntries.add(Entry.tool("Iron Chisel", "✧", BitsAndChisels.IRON_CHISEL));
        chiselEntries.add(Entry.tool("Smart Chisel", "✪", BitsAndChisels.SMART_CHISEL));
        chiselEntries.add(Entry.tool("Wrench", "⚒", BitsAndChisels.WRENCH));
        chiselEntries.add(Entry.tool("Blueprint", "☰", BitsAndChisels.BLUEPRINT));
        chiselEntries.add(Entry.tool("Bit Item", "▪", BitsAndChisels.BIT_ITEM));
    }

    private List<Entry> currentList() {
        return tab == Tab.CHISEL ? chiselEntries : buildEntries;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Dim world behind (glass scrim)
        g.fill(0, 0, this.width, this.height, 0x99000000);

        int panelW = Math.min(420, this.width - 40);
        int panelH = Math.min(280, this.height - 40);
        int px = (this.width - panelW) / 2;
        int py = (this.height - panelH) / 2;

        int railW = 56;
        int contentX = px + railW + 8;
        int contentW = panelW - railW - 16;

        // Outer glass panel
        fillRound(g, px - 2, py - 2, px + panelW + 2, py + panelH + 2, GLASS_EDGE);
        fillRound(g, px, py, px + panelW, py + panelH, GLASS_BG);

        // Left rail
        fillRound(g, px + 4, py + 4, px + railW, py + panelH - 4, RAIL);
        drawRailButton(g, px + 8, py + 16, railW - 16, 36, tab == Tab.BUILD, "Build", mouseX, mouseY);
        drawRailButton(g, px + 8, py + 60, railW - 16, 36, tab == Tab.CHISEL, "Chisel", mouseX, mouseY);

        // Title
        String title = tab == Tab.CHISEL ? "Chisel & Bits" : "Build Modes";
        g.drawString(this.font, title, contentX, py + 12, TEXT, false);
        String sub = tab == Tab.CHISEL ? "Tools" : "Mode: " + BuildModeState.mode.label;
        g.drawString(this.font, sub, contentX, py + 24, TEXT_DIM, false);

        // Grid of slots (5 columns)
        List<Entry> list = currentList();
        int cols = 5;
        int slot = 36;
        int gap = 6;
        int gridX = contentX;
        int gridY = py + 44;
        int rows = (list.size() + cols - 1) / cols;
        hoverIndex = -1;

        for (int i = 0; i < list.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int sx = gridX + col * (slot + gap);
            int sy = gridY + row * (slot + gap);
            boolean hot = mouseX >= sx && mouseX < sx + slot && mouseY >= sy && mouseY < sy + slot;
            if (hot) hoverIndex = i;

            g.fill(sx, sy, sx + slot, sy + slot, hot ? SLOT_HOVER : SLOT);
            // accent border when hovered
            if (hot) {
                g.fill(sx, sy, sx + slot, sy + 1, ACCENT);
                g.fill(sx, sy + slot - 1, sx + slot, sy + slot, ACCENT);
                g.fill(sx, sy, sx + 1, sy + slot, ACCENT);
                g.fill(sx + slot - 1, sy, sx + slot, sy + slot, ACCENT);
            } else {
                g.fill(sx, sy, sx + slot, sy + 1, 0x33FFFFFF);
            }

            Entry e = list.get(i);
            int gw = this.font.width(e.glyph);
            g.drawString(this.font, e.glyph, sx + (slot - gw) / 2, sy + 8, hot ? ACCENT : TEXT, false);

            // tiny label under glyph if space
            String cap = shortName(e.label);
            int cw = this.font.width(cap);
            if (cw < slot - 2) {
                g.drawString(this.font, cap, sx + (slot - cw) / 2, sy + slot - 12, TEXT_DIM, false);
            }
        }

        // Footer page-style status
        String page = (tab == Tab.BUILD ? "1" : "2") + " / 2";
        int pw = this.font.width(page);
        g.drawString(this.font, page, px + panelW - pw - 12, py + panelH - 16, TEXT_DIM, false);

        // Tooltip
        if (hoverIndex >= 0 && hoverIndex < list.size()) {
            Entry e = list.get(hoverIndex);
            g.renderTooltip(this.font, Component.literal(e.label), mouseX, mouseY);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawRailButton(GuiGraphics g, int x, int y, int w, int h, boolean selected, String label, int mx, int my) {
        boolean hot = mx >= x && mx < x + w && my >= y && my < y + h;
        int bg = selected ? ACCENT_SOFT : (hot ? SLOT_HOVER : SLOT);
        g.fill(x, y, x + w, y + h, bg);
        if (selected) {
            g.fill(x, y, x + 3, y + h, ACCENT);
        }
        int tw = this.font.width(label);
        // vertical-ish: just center text
        g.drawString(this.font, label, x + (w - tw) / 2, y + h / 2 - 4, selected ? TEXT : TEXT_DIM, false);
    }

    private static void fillRound(GuiGraphics g, int x0, int y0, int x1, int y1, int argb) {
        // Approximate rounded rect with plain fill (MC GUI limitation without custom sprites)
        g.fill(x0, y0, x1, y1, argb);
    }

    private static String shortName(String s) {
        if (s == null) return "";
        if (s.equals("Chisel & Bits")) return "Chisel";
        if (s.equals("Diagonal Line")) return "Diag";
        if (s.equals("Diamond Chisel")) return "Dia";
        if (s.equals("Iron Chisel")) return "Iron";
        if (s.equals("Smart Chisel")) return "Smart";
        if (s.equals("Bit Item")) return "Bit";
        if (s.length() > 6) return s.substring(0, 5);
        return s;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int panelW = Math.min(420, this.width - 40);
        int panelH = Math.min(280, this.height - 40);
        int px = (this.width - panelW) / 2;
        int py = (this.height - panelH) / 2;
        int railW = 56;

        // Rail clicks
        if (mouseX >= px + 8 && mouseX < px + railW - 8) {
            if (mouseY >= py + 16 && mouseY < py + 52) {
                tab = Tab.BUILD;
                return true;
            }
            if (mouseY >= py + 60 && mouseY < py + 96) {
                tab = Tab.CHISEL;
                return true;
            }
        }

        if (hoverIndex >= 0) {
            List<Entry> list = currentList();
            if (hoverIndex < list.size()) {
                activate(list.get(hoverIndex));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void activate(Entry e) {
        if (e.openChisel) {
            tab = Tab.CHISEL;
            return;
        }
        if (e.mode != null) {
            BuildModeState.setMode(e.mode);
            BitsAndChisels.LOGGER.info("[Bits] Mode → {}", e.mode.label);
            this.onClose();
            return;
        }
        if (e.item != null) {
            selectTool(e.item, e.label);
            this.onClose();
        }
    }

    private void selectTool(Item item, String label) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        int invSlot = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(item)) {
                invSlot = i;
                break;
            }
        }
        if (invSlot < 0) {
            if (player.getAbilities().instabuild) {
                player.getInventory().add(new ItemStack(item));
            }
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC closes
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
