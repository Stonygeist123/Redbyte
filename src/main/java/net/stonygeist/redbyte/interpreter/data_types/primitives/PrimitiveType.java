package net.stonygeist.redbyte.interpreter.data_types.primitives;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public abstract class PrimitiveType extends DataType {
    public static final TypeSymbol TYPE = new TypeSymbol("primitive", Component.translatable("interpreter.redbyte.types.primitive"));

    public PrimitiveType(TypeSymbol type) {
        super(type);
    }
}
