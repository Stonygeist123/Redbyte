package net.stonygeist.interpreter.binder.stmt;

import net.stonygeist.interpreter.binder.expr.BoundExpr;
import net.stonygeist.interpreter.symbols.LabelSymbol;

public class BoundConditionalGotoStmt extends BoundStmt {
    public final LabelSymbol label;
    public BoundExpr condition;
    public final boolean jumpIfTrue;

    public BoundConditionalGotoStmt(LabelSymbol label, BoundExpr condition) {
        this(label, condition, true);
    }

    public BoundConditionalGotoStmt(LabelSymbol label, BoundExpr condition, boolean jumpIfTrue) {
        this.label = label;
        this.condition = condition;
        this.jumpIfTrue = jumpIfTrue;
    }
}
