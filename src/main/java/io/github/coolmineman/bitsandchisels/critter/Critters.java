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

import java.util.ArrayList;
import java.util.List;

public final class Critters {
    public record Spec(String id, float width, float height) {}

    public static final List<Spec> SPECS = List.of(
        new Spec("pixel_bat", 0.7f, 0.6f),
        new Spec("tricera", 1.0f, 1.0f),
        new Spec("turkey", 0.7f, 0.8f),
        new Spec("crab", 0.8f, 0.5f),
        new Spec("fancy_goat", 0.9f, 1.2f),
        new Spec("forest_stag", 1.1f, 1.4f),
        new Spec("farmer_pig", 0.8f, 0.8f),
        new Spec("pelican", 0.8f, 0.9f),
        new Spec("little_green", 0.5f, 0.8f),
        new Spec("pup_dark", 0.6f, 0.6f),
        new Spec("pup_light", 0.6f, 0.6f),
        new Spec("tophat_frog", 0.7f, 0.6f),
        new Spec("witch_frog", 0.6f, 0.6f),
        new Spec("chomp_frog", 0.8f, 0.6f)
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
                new CritterEggItem(type, new Item.Properties().stacksTo(64).setId(eggKey))
            );
            EGGS.add(egg);
        }
        BitsAndChisels.LOGGER.info("Registered {} critter mobs", TYPES.size());
    }
}
