package net.stonygeist.redbyte.interpreter.binder.expr;

import com.google.common.collect.ImmutableList;
import net.stonygeist.redbyte.interpreter.symbols.FunctionSymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public final class BoundCallExpr extends BoundExpr {
    public final FunctionSymbol symbol;
    public final ImmutableList<BoundExpr> args;

    public BoundCallExpr(FunctionSymbol symbol, ImmutableList<BoundExpr> args) {
        this.symbol = symbol;
        this.args = args;
    }

    @Override
    public TypeSymbol getType() {
        return symbol.type;
    }
}
