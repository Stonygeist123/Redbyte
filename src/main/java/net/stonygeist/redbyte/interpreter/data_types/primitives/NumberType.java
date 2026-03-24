package net.stonygeist.redbyte.interpreter.data_types.primitives;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public final class NumberType extends PrimitiveType {
    public static final TypeSymbol TYPE = new TypeSymbol("number", Component.translatable("interpreter.redbyte.types.number"));
    private final float value;

    public NumberType(float value) {
        super(TYPE);
        this.value = value;
    }

    public float getValue() {
        return value;
    }
}
