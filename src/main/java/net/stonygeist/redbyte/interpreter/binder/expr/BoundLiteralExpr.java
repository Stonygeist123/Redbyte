package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.PrimitiveType;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

public final class BoundLiteralExpr implements BoundExpr {
    public final DataType value;
    private final TextSpan span;
    public final TypeSymbol type;

    public BoundLiteralExpr(PrimitiveType value, TextSpan span) {
        this.value = value;
        this.span = span;
        type = value.getType();
    }

    @Override
    public @NotNull TypeSymbol getType() {
        return type;
    }

    @Override
    public @NotNull TextSpan span() {
        return span;
    }
}
