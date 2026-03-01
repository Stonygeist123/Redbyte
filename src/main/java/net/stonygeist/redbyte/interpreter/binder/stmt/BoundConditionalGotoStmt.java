package net.stonygeist.redbyte.interpreter.binder.stmt;

import net.stonygeist.redbyte.interpreter.binder.expr.BoundExpr;
import net.stonygeist.redbyte.interpreter.symbols.LabelSymbol;

public record BoundConditionalGotoStmt(LabelSymbol label, BoundExpr condition,
                                       boolean jumpIfTrue) implements BoundStmt {
    public BoundConditionalGotoStmt(LabelSymbol label, BoundExpr condition) {
        this(label, condition, true);
    }
}
