package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.data_types.RoboDataType;
import org.jetbrains.annotations.NotNull;

public record BoundRoboExpr(TextSpan span) implements BoundExpr {

    @Override
    public @NotNull Class<? extends DataType> getType() {
        return RoboDataType.class;
    }
}
