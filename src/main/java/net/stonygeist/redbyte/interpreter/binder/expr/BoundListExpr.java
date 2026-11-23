package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record BoundListExpr(List<BoundExpr> items, TextSpan span, TypeSymbol itemType) implements BoundExpr {
    @Override
    public @NotNull TypeSymbol getType() {
        return TypeSymbol.listOf(itemType);
    }
}
