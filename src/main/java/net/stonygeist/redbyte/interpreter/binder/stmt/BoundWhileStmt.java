package net.stonygeist.redbyte.interpreter.binder.stmt;

import net.stonygeist.redbyte.interpreter.binder.expr.BoundExpr;

public final class BoundWhileStmt extends BoundStmt {
    public final BoundExpr condition;
    public final BoundStmt body;

    public BoundWhileStmt(BoundExpr condition, BoundStmt body) {
        this.condition = condition;
        this.body = body;
    }
}
