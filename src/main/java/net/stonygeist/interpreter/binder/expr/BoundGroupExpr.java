package net.stonygeist.interpreter.binder.expr;

import net.stonygeist.interpreter.symbols.TypeSymbol;

public final class BoundGroupExpr extends BoundExpr {
    public final BoundExpr expr;

    public BoundGroupExpr(BoundExpr expr) {
        this.expr = expr;
    }

    @Override
    public TypeSymbol getType() {
        return expr.getType();
    }
}
