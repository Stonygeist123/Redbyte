package net.stonygeist.interpreter.binder.expr;

import net.stonygeist.interpreter.symbols.TypeSymbol;
import net.stonygeist.interpreter.symbols.VariableSymbol;
import org.jetbrains.annotations.NotNull;

public final class BoundAssignExpr extends BoundExpr {
    public final VariableSymbol symbol;
    public final BoundExpr value;

    public BoundAssignExpr(@NotNull VariableSymbol symbol, BoundExpr value) {
        this.symbol = symbol;
        this.value = value;
    }

    @Override
    public TypeSymbol getType() {
        return symbol.type;
    }
}
