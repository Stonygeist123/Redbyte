package net.stonygeist.interpreter.binder.expr;

import net.stonygeist.interpreter.symbols.TypeSymbol;

public abstract class BoundExpr {
    public abstract TypeSymbol getType();
}
