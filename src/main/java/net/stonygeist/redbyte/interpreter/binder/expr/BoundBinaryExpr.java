package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.binder.BoundOperator;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public record BoundBinaryExpr(BoundExpr left, BoundOperator.BoundBinaryOperator operator,
                              BoundExpr right) implements BoundExpr {
    @Override
    public TypeSymbol getType() {
        return operator.resultType();
    }
}
