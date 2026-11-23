package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public class NothingDataType extends DataType {
    public static final TypeSymbol TYPE = new TypeSymbol("nothing", Component.translatable("interpreter.redbyte.types.nothing"));

    public NothingDataType() {
        super(TYPE);
    }
}
