package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.data_types.NothingDataType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.ListType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record BoundListExpr(List<BoundExpr> items, TextSpan span) implements BoundExpr {
    @Override
    public @NotNull Class<? extends DataType> getType() {
        // TODO: Fix if list is empty
        if (items.isEmpty())
            return NothingDataType.class;
        return ListType.class;
    }
}
