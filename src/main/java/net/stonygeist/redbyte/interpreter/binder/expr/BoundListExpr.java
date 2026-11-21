package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.data_types.ListType;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record BoundListExpr(List<BoundExpr> items, TextSpan span,
                            Class<? extends DataType> itemType, TypeSymbol itemTypeSymbol) implements BoundExpr {
    @Override
    public @NotNull Class<? extends DataType> getType() {
        return ListType.class;
    }

    @Override
    public @NotNull TypeSymbol getTypeSymbol() {
        return itemTypeSymbol.transform(true);
    }
}
