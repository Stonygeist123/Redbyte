package net.stonygeist.redbyte.interpreter.binder.expr;

import com.google.common.collect.ImmutableList;
import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import org.jetbrains.annotations.NotNull;

public record BoundMethodExpr(BoundExpr object, MethodSymbol method, ImmutableList<BoundExpr> args,
                              TextSpan span) implements BoundExpr {
    @Override
    public @NotNull Class<? extends DataType> getType() {
        return method.type;
    }
}
