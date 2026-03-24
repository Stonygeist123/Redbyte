package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.data_types.NothingDataType;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public record BoundPropertyExpr(BoundExpr object,
                                Map<VariableSymbol, Function<DataType, DataType>> properties, String property,
                                TextSpan span) implements BoundExpr {
    @Override
    public @NotNull Class<? extends DataType> getType() {
        Optional<Class<? extends DataType>> type = DataType.getProperty(properties, property).map(entry -> entry.getKey().type);
        return type.isEmpty() ? NothingDataType.class : type.get();
    }
}
