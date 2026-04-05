package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.data_types.NothingDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public record BoundPropertyExpr(BoundExpr object,
                                Optional<Function<DataType, DataType>> property,
                                Optional<Class<? extends DataType>> type,
                                TextSpan span) implements BoundExpr {
    @Override
    public @NotNull Class<? extends DataType> getType() {
        return type.isPresent() ? type.get() : NothingDataType.class;
    }
}
