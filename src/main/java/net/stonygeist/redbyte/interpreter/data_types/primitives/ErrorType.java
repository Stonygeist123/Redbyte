package net.stonygeist.redbyte.interpreter.data_types.primitives;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public final class ErrorType extends PrimitiveType {
    public static final TypeSymbol TYPE = new TypeSymbol("error", Component.translatable("interpreter.redbyte.types.error"));

    public ErrorType() {
        super(TYPE);
    }
}
