package net.stonygeist.interpreter.binder.expr;

import net.stonygeist.interpreter.binder.BoundOperator;
import net.stonygeist.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

public final class BoundBinaryExpr extends BoundExpr {
    public final BoundExpr left, right;
    public final BoundOperator.BoundBinaryOperator operator;

    public BoundBinaryExpr(BoundExpr left, @NotNull BoundOperator.BoundBinaryOperator operator, BoundExpr right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public TypeSymbol getType() {
        return operator.resultType();
    }
}
