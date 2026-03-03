package net.stonygeist.redbyte.index;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.entity.robo.RoboEntity;

public enum RedbyteEntities {
    ;
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Redbyte.MOD_ID);
    public static final RegistryObject<EntityType<RoboEntity>> ROBO = ENTITY_TYPES.register("robo", () -> EntityType.Builder.of(RoboEntity::new, MobCategory.CREATURE)
            .sized(.2f, .75f)
            .fireImmune()
            .build("robo"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
