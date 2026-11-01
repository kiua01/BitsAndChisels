package io.github.coolmineman.bitsandchisels.critter;

import io.github.coolmineman.bitsandchisels.BitsAndChisels;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import java.util.ArrayList;
import java.util.List;

public final class Critters {
    public record Spec(String id, float width, float height, int eggPrimary, int eggSecondary) {}

    public static final List<Spec> SPECS = List.of(
        new Spec("pixel_bat", 0.7f, 0.6f, 0xC0C0C8, 0x303038),
        new Spec("tricera", 1.0f, 1.0f, 0x50C846, 0xE8D090),
        new Spec("turkey", 0.7f, 0.8f, 0x8C5028, 0xE8A040),
        new Spec("crab", 0.8f, 0.5f, 0xE66428, 0xF0F0A0),
        new Spec("fancy_goat", 0.9f, 1.2f, 0xA06E3C, 0xC080FF),
        new Spec("forest_stag", 1.1f, 1.4f, 0x5A3C1E, 0x50C846),
        new Spec("farmer_pig", 0.8f, 0.8f, 0xF0A0A0, 0xE8C040),
        new Spec("pelican", 0.8f, 0.9f, 0xF0F0F0, 0xF0C040),
        new Spec("little_green", 0.5f, 0.8f, 0x64B450, 0xA08060),
        new Spec("pup_dark", 0.6f, 0.6f, 0x28201E, 0xE08030),
        new Spec("pup_light", 0.6f, 0.6f, 0xC8A064, 0xFFFFFF),
        new Spec("tophat_frog", 0.7f, 0.6f, 0x3CA03C, 0x202020),
        new Spec("witch_frog", 0.6f, 0.6f, 0x78A03C, 0x301848),
        new Spec("chomp_frog", 0.8f, 0.6f, 0x50C846, 0xF0D0A0)
    );

    public static final List<EntityType<CritterMob>> TYPES = new ArrayList<>();
    public static final List<Item> EGGS = new ArrayList<>();

    private Critters() {}

    public static void register() {
        for (Spec spec : SPECS) {
            ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, BitsAndChisels.id(spec.id()));
            EntityType<CritterMob> type = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                EntityType.Builder.of(CritterMob::new, MobCategory.CREATURE)
                    .sized(spec.width(), spec.height())
                    .clientTrackingRange(8)
                    .build(key)
            );
            FabricDefaultAttributeRegistry.register(type, CritterMob.createAttributes());
            TYPES.add(type);

            ResourceKey<Item> eggKey = ResourceKey.create(Registries.ITEM, BitsAndChisels.id(spec.id() + "_spawn_egg"));
            Item egg = Registry.register(
                BuiltInRegistries.ITEM,
                eggKey,
                new SpawnEggItem(type, new Item.Properties().setId(eggKey))
            );
            EGGS.add(egg);
        }
        BitsAndChisels.LOGGER.info("Registered {} critter mobs", TYPES.size());
    }
}
