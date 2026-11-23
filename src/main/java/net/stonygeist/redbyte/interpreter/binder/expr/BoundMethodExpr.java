package net.stonygeist.redbyte.interpreter.binder.expr;

import com.google.common.collect.ImmutableList;
import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

public record BoundMethodExpr(BoundExpr object, MethodSymbol method, ImmutableList<BoundExpr> args,
                              TextSpan span) implements BoundExpr {
    @Override
    public @NotNull TypeSymbol getType() {
        return method.type;
    }
}
