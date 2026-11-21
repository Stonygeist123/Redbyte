package net.stonygeist.redbyte.interpreter.symbols;

import net.stonygeist.redbyte.interpreter.data_types.DataType;

public class VariableSymbol extends Symbol {
    public final Class<? extends DataType> type;
    public final TypeSymbol typeSymbol;

    public VariableSymbol(String name, Class<? extends DataType> type, TypeSymbol typeSymbol) {
        super(name);
        this.type = type;
        this.typeSymbol = typeSymbol;
    }
}
