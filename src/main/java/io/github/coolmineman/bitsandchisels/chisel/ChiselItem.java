package io.github.coolmineman.bitsandchisels.chisel;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ChiselItem extends Item {
    public enum Mode { SINGLE, FOUR_BY_FOUR, SMART }

    private final Mode mode;

    public ChiselItem(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }

    public Mode mode() {
        return mode;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }
}
