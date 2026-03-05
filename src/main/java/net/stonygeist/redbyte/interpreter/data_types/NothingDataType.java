package net.stonygeist.redbyte.interpreter.data_types;

import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public class NothingDataType extends DataType {
    public static final TypeSymbol TYPE = TypeSymbol.Void;

    public NothingDataType() {
        super(TYPE);
    }
}
