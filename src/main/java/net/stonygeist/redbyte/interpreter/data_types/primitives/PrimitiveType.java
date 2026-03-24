package net.stonygeist.redbyte.interpreter.data_types.primitives;

import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public abstract class PrimitiveType extends DataType {
    public PrimitiveType(TypeSymbol type) {
        super(type);
    }
}
