package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.binder.BoundOperator;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

public final class BoundUnaryExpr extends BoundExpr {
    public final BoundExpr operand;
    public final @NotNull BoundOperator.BoundUnaryOperator operator;

    public BoundUnaryExpr(BoundExpr operand, @NotNull BoundOperator.BoundUnaryOperator operator) {
        this.operand = operand;
        this.operator = operator;
    }

    @Override
    public TypeSymbol getType() {
        return operator.type();
    }
}
