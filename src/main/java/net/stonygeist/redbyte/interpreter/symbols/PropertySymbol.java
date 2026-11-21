package net.stonygeist.redbyte.interpreter.symbols;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.data_types.DataType;

public class PropertySymbol extends VariableSymbol {
    public final Component description;

    public PropertySymbol(String name, Class<? extends DataType> type, Component description) {
        this(name, type, DataType.getTypeSymbol(type), description);
    }

    public PropertySymbol(String name, Class<? extends DataType> type, TypeSymbol typeSymbol, Component description) {
        super(name, type, typeSymbol);
        this.description = description;
    }
}
