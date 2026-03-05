package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;
import org.jetbrains.annotations.NotNull;

public record BoundNameExpr(VariableSymbol symbol, TextSpan span) implements BoundExpr {
    @Override
    public @NotNull TypeSymbol getType() {
        return symbol.type;
    }
}
