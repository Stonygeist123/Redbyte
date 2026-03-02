package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public class BoundErrorExpr implements BoundExpr {
    @Override
    public TypeSymbol getType() {
        return TypeSymbol.Error;
    }
}
