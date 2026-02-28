package net.stonygeist.interpreter.binder.stmt;

import net.stonygeist.interpreter.binder.expr.BoundExpr;

public final class BoundOnceStmt extends BoundStmt {
    public final BoundExpr condition;
    public final BoundStmt body;

    public BoundOnceStmt(BoundExpr condition, BoundStmt body) {
        this.condition = condition;
        this.body = body;
    }
}
