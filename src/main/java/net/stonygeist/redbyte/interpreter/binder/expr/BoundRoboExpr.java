package net.stonygeist.redbyte.interpreter.binder.expr;

import net.stonygeist.redbyte.interpreter.data_types.RoboDataType;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public class BoundRoboExpr implements BoundExpr {
    @Override
    public TypeSymbol getType() {
        return RoboDataType.TYPE;
    }
}
