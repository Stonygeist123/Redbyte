package net.stonygeist.interpreter.binder.expr;

import net.stonygeist.interpreter.symbols.TypeSymbol;

public final class BoundLiteralExpr extends BoundExpr {
    public final Object value;
    public final TypeSymbol type;

    public BoundLiteralExpr(Object value) {
        this.value = value;
        switch (value) {
            case Float ignored -> type = TypeSymbol.Number;
            case Boolean ignored -> type = TypeSymbol.Boolean;
            case String ignored -> type = TypeSymbol.Text;
            case null, default -> throw new RuntimeException();
        }
    }

    @Override
    public TypeSymbol getType() {
        return type;
    }
}
