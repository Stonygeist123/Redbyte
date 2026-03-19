package net.stonygeist.redbyte.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

public record RoboSpawnerRecipeInput(ItemStack input) implements RecipeInput {
    @Override
    public @NotNull ItemStack getItem(int index) {
        return input;
    }

    @Override
    public int size() {
        return 1;
    }
}