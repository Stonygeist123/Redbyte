package net.stonygeist.redbyte.interpreter.binder.stmt;

import net.stonygeist.redbyte.interpreter.symbols.LabelSymbol;

public class BoundGotoStmt extends BoundStmt {
    public final LabelSymbol label;

    public BoundGotoStmt(LabelSymbol label) {
        this.label = label;
    }
}
