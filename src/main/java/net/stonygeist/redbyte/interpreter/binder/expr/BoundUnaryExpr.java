package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.binder.BoundOperator;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

public record BoundUnaryExpr(BoundExpr operand,
                             @NotNull BoundOperator.BoundUnaryOperator operator, TextSpan span) implements BoundExpr {
    @Override
    public @NotNull TypeSymbol getType() {
        return operator.type();
    }
}
