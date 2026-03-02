package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public interface BoundExpr {
    TypeSymbol getType();
}
