package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;
import org.jetbrains.annotations.NotNull;

public record BoundAssignExpr(VariableSymbol variable, BoundExpr value, TextSpan span) implements BoundExpr {
    @Override
    public @NotNull Class<? extends DataType> getType() {
        return variable.type;
    }
}
