package net.stonygeist.redbyte.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.stonygeist.redbyte.index.RedbyteRecipes;
import org.jetbrains.annotations.NotNull;

public record RoboSpawnerRecipe(Ingredient inputItem, ItemStack output) implements Recipe<RoboSpawnerRecipeInput> {
    @Override
    public @NotNull String toString() {
        return "robo_spawner";
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(inputItem);
        return list;
    }

    @Override
    public boolean matches(@NotNull RoboSpawnerRecipeInput input, Level level) {
        return !level.isClientSide() && inputItem.test(input.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull RoboSpawnerRecipeInput input, HolderLookup.@NotNull Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return output;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RedbyteRecipes.ROBO_SPAWNER_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return RedbyteRecipes.ROBO_SPAWNER_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<RoboSpawnerRecipe> {
        public static final MapCodec<RoboSpawnerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(RoboSpawnerRecipe::inputItem),
                ItemStack.CODEC.fieldOf("result").forGetter(RoboSpawnerRecipe::output)
        ).apply(inst, RoboSpawnerRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, RoboSpawnerRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, RoboSpawnerRecipe::inputItem,
                        ItemStack.STREAM_CODEC, RoboSpawnerRecipe::output,
                        RoboSpawnerRecipe::new);

        @Override
        public @NotNull MapCodec<RoboSpawnerRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, RoboSpawnerRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}