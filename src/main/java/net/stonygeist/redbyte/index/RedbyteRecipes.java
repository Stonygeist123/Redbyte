package net.stonygeist.redbyte.index;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.recipe.RoboSpawnerRecipe;

public enum RedbyteRecipes {
    ;
    public static final DeferredRegister<RecipeSerializer<?>> RECIPYE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Redbyte.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> RECIPYE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Redbyte.MOD_ID);

    public static final RegistryObject<RecipeSerializer<RoboSpawnerRecipe>> ROBO_SPAWNER_SERIALIZER =
            RECIPYE_SERIALIZERS.register("robo_spawner", RoboSpawnerRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<RoboSpawnerRecipe>> ROBO_SPAWNER_TYPE =
            RECIPYE_TYPES.register("robo_spawner", () -> new RecipeType<>() {
            });


    public static void register(IEventBus eventBus) {
        RECIPYE_SERIALIZERS.register(eventBus);
        RECIPYE_TYPES.register(eventBus);
    }
}
