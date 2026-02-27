package net.stonygeist.interpreter.binder.stmt;

import net.stonygeist.interpreter.symbols.LabelSymbol;

public final class BoundLabelStmt extends BoundStmt {
    public final LabelSymbol label;

    public BoundLabelStmt(LabelSymbol label) {
        this.label = label;
    }
}
