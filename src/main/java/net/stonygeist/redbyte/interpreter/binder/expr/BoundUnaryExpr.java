package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.binder.BoundOperator;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import org.jetbrains.annotations.NotNull;

public record BoundUnaryExpr(BoundExpr operand,
                             @NotNull BoundOperator.BoundUnaryOperator operator, TextSpan span) implements BoundExpr {
    @Override
    public @NotNull Class<? extends DataType> getType() {
        return operator.type();
    }
}
