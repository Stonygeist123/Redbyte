package net.stonygeist.redbyte.interpreter.binder.stmt;

import net.stonygeist.redbyte.interpreter.binder.expr.BoundExpr;

public final class BoundExprStmt extends BoundStmt {
    public final BoundExpr expr;

    public BoundExprStmt(BoundExpr expr) {
        this.expr = expr;
    }
}
