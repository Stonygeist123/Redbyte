package net.stonygeist.redbyte.interpreter.binder.expr;

import com.google.common.collect.ImmutableList;
import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.symbols.FunctionSymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

public record BoundCallExpr(FunctionSymbol symbol, ImmutableList<BoundExpr> args, TextSpan span) implements BoundExpr {
    @Override
    public @NotNull TypeSymbol getType() {
        return symbol.type;
    }
}
