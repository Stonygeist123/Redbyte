package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;
import org.jetbrains.annotations.NotNull;

public final class BoundNameExpr extends BoundExpr {
    public final VariableSymbol symbol;

    public BoundNameExpr(@NotNull VariableSymbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public TypeSymbol getType() {
        return symbol.type;
    }
}
