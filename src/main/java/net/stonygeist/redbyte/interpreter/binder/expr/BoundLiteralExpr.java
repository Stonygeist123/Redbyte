package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.PrimitiveType;
import org.jetbrains.annotations.NotNull;

public final class BoundLiteralExpr implements BoundExpr {
    public final DataType value;
    private final TextSpan span;
    public final Class<? extends DataType> type;

    public BoundLiteralExpr(PrimitiveType value, TextSpan span) {
        this.value = value;
        this.span = span;
        type = value.getClass();
    }

    @Override
    public @NotNull Class<? extends DataType> getType() {
        return type;
    }

    @Override
    public @NotNull TextSpan span() {
        return span;
    }
}
