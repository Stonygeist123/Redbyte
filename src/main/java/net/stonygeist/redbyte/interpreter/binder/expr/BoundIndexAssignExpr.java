package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

public record BoundIndexAssignExpr(BoundExpr base, BoundExpr index, BoundExpr value,
                                   TextSpan span) implements BoundExpr {
    @Override
    public @NotNull TypeSymbol getType() {
        return value.getType();
    }
}
