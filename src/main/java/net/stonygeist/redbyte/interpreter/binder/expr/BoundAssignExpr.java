package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;

public record BoundAssignExpr(VariableSymbol symbol, BoundExpr value) implements BoundExpr {
    @Override
    public TypeSymbol getType() {
        return symbol.type;
    }
}
