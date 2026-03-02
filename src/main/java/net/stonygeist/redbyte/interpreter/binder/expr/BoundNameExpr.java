package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;

public record BoundNameExpr(VariableSymbol symbol) implements BoundExpr {
    @Override
    public TypeSymbol getType() {
        return symbol.type;
    }
}
