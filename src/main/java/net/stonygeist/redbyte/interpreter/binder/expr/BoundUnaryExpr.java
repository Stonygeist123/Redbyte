package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.binder.BoundOperator;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

public record BoundUnaryExpr(BoundExpr operand,
                             @NotNull BoundOperator.BoundUnaryOperator operator) implements BoundExpr {
    @Override
    public TypeSymbol getType() {
        return operator.type();
    }
}
