package net.stonygeist.redbyte.interpreter.binder.expr;

import com.google.common.collect.ImmutableList;
import net.stonygeist.redbyte.interpreter.symbols.FunctionSymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public record BoundCallExpr(FunctionSymbol symbol, ImmutableList<BoundExpr> args) implements BoundExpr {
    @Override
    public TypeSymbol getType() {
        return symbol.type;
    }
}
