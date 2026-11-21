package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

public interface BoundExpr {
    @NotNull Class<? extends DataType> getType();

    @NotNull TextSpan span();

    @NotNull
    default TypeSymbol getTypeSymbol() {
        return DataType.getTypeSymbol(getType());
    }
}
