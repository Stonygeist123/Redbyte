package net.stonygeist.redbyte.interpreter.binder.stmt;

import net.stonygeist.redbyte.interpreter.symbols.LabelSymbol;

public final class BoundLabelStmt extends BoundStmt {
    public final LabelSymbol label;

    public BoundLabelStmt(LabelSymbol label) {
        this.label = label;
    }
}
