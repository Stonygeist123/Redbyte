package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

public final class BoundLiteralExpr implements BoundExpr {
    public final Object value;
    private final TextSpan span;
    public final TypeSymbol type;

    public BoundLiteralExpr(Object value, TextSpan span) {
        this.value = value;
        this.span = span;
        switch (value) {
            case Float ignored -> type = TypeSymbol.Number;
            case Boolean ignored -> type = TypeSymbol.Boolean;
            case String ignored -> type = TypeSymbol.Text;
            case null, default -> throw new RuntimeException();
        }
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
