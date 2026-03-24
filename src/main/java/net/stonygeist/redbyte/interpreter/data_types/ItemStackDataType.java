package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;

import java.util.Hashtable;
import java.util.Map;
import java.util.function.Function;

public final class ItemStackDataType extends DataType {
    public static final TypeSymbol TYPE = new TypeSymbol("item", Component.translatable("interpreter.redbyte.types.entity"));
    private final ItemStack itemStack;
    private final int slot;

    public ItemStackDataType(ItemStack itemStack, int slot) {
        super(TYPE);
        this.itemStack = itemStack;
        this.slot = slot;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getSlot() {
        return slot;
    }

    public static final Map<VariableSymbol, Function<ItemStackDataType, DataType>> properties = new Hashtable<>(Map.of(
            new VariableSymbol("count", NumberType.class), x -> new NumberType(x.getItemStack().getCount())
    ));
}
