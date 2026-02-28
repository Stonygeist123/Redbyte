package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;
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
