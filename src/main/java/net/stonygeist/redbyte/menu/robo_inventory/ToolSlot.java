package net.stonygeist.redbyte.menu.robo_inventory;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ToolSlot extends SlotItemHandler {
    private final Class<? extends Item> kind;
    private final @Nullable ResourceLocation emptyIcon;
    private final Consumer<Item> onSet;

    public ToolSlot(IItemHandler itemHandler, int index, int x, int y, Class<? extends Item> kind, @Nullable ResourceLocation emptyIcon, Consumer<Item> onSet) {
        super(itemHandler, index, x, y);
        this.kind = kind;
        this.emptyIcon = emptyIcon;
        this.onSet = onSet;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return kind.isInstance(stack.getItem());
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return emptyIcon != null ? Pair.of(InventoryMenu.BLOCK_ATLAS, emptyIcon) : super.getNoItemIcon();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack ignored) {
        return 1;
    }

    @Override
    public void set(@NotNull ItemStack stack) {
        super.set(stack);
        onSet.accept(stack.getItem());
    }
}
