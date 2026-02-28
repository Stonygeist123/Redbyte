package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public abstract class BoundExpr {
    public abstract TypeSymbol getType();
}
