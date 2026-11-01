package io.github.coolmineman.bitsandchisels.client;

import io.github.coolmineman.bitsandchisels.BitsAndChisels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Radial tool picker (Effortless Building style) for Bits & Chisels tools.
 * Toggle with chat command {@code .bits} or key handled by {@link BitsBlockClient}.
 */
public final class BitsRadialMenu {
    public static boolean open;
    private static int hovered = -1;

    private static final Entry[] ENTRIES = {
        new Entry("Diamond Chisel", BitsAndChisels.DIAMOND_CHISEL),
        new Entry("Iron Chisel", BitsAndChisels.IRON_CHISEL),
        new Entry("Smart Chisel", BitsAndChisels.SMART_CHISEL),
        new Entry("Wrench", BitsAndChisels.WRENCH),
        new Entry("Blueprint", BitsAndChisels.BLUEPRINT),
        new Entry("Bit Item", BitsAndChisels.BIT_ITEM),
    };

    private BitsRadialMenu() {}

    public static void toggle() {
        if (open) close(true);
        else open = true;
    }

    public static void close(boolean apply) {
        if (open && apply && hovered >= 0 && hovered < ENTRIES.length) {
            select(ENTRIES[hovered]);
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
        double radius = Math.min(screenW, screenH) * 0.22;
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
            int color = (i == hovered) ? 0xFFFFFF : 0xA0A0A0;
            String label = ENTRIES[i].label;
            int tw = font.width(label);
            graphics.text(font, label, (int) (ix - tw / 2.0), (int) iy - 4, color, true);
        }

        String title = hovered >= 0 ? ENTRIES[hovered].label : "Bits & Chisels";
        int tw = font.width(title);
        graphics.text(font, title, (int) (cx - tw / 2.0), (int) (cy - radius - 18), 0xFFFFFF, true);
        String hint = ".bits again to select";
        int hw = font.width(hint);
        graphics.text(font, hint, (int) (cx - hw / 2.0), (int) (cy + radius + 10), 0x808080, true);
    }

    private static void select(Entry entry) {
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
                BitsAndChisels.LOGGER.info("[Bits] Gave {}", entry.label);
            } else {
                BitsAndChisels.LOGGER.info("[Bits] No {} in inventory", entry.label);
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
        BitsAndChisels.LOGGER.info("[Bits] Selected {}", entry.label);
    }

    private record Entry(String label, Item item) {}
}
