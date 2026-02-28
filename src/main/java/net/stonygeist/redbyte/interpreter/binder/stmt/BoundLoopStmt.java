package net.stonygeist.redbyte.interpreter.binder.stmt;

import net.stonygeist.redbyte.interpreter.binder.expr.BoundExpr;

public final class BoundLoopStmt extends BoundStmt {
    public final BoundExpr count;
    public final BoundStmt body;

    public BoundLoopStmt(BoundExpr count, BoundStmt body) {
        this.count = count;
        this.body = body;
    }
}
