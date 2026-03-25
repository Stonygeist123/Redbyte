package net.stonygeist.redbyte.interpreter.data_types.primitives;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.data_types.BlockDataType;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ErrorType extends PrimitiveType {
    public static final TypeSymbol TYPE = new TypeSymbol("error", Component.translatable("interpreter.redbyte.types.error"));

    public ErrorType() {
        super(TYPE);
    }

    public static final Map<VariableSymbol, Function<BlockDataType, DataType>> properties = new Hashtable<>();
    public static final List<MethodSymbol> methods = List.of();
}
