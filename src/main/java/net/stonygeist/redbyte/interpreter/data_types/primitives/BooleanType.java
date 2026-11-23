package net.stonygeist.redbyte.interpreter.data_types.primitives;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public final class BooleanType extends PrimitiveType {
    public static final TypeSymbol TYPE = new TypeSymbol("boolean", Component.translatable("interpreter.redbyte.types.boolean"));
    private final boolean value;

    public BooleanType(boolean value) {
        super(TYPE);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }
}
