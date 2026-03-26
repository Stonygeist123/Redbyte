package net.stonygeist.redbyte.interpreter.data_types.primitives;

import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class PrimitiveType extends DataType {
    public PrimitiveType(TypeSymbol type) {
        super(type);
    }

    public static final Map<PropertySymbol, Function<PrimitiveType, DataType>> properties = new Hashtable<>();
    public static final List<MethodSymbol> methods = List.of();
}
