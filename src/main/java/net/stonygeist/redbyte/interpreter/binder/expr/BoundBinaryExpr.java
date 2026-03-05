package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.binder.BoundOperator;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

public record BoundBinaryExpr(BoundExpr left, BoundOperator.BoundBinaryOperator operator,
                              BoundExpr right, TextSpan span) implements BoundExpr {
    @Override
    public @NotNull TypeSymbol getType() {
        return operator.resultType();
    }
}
