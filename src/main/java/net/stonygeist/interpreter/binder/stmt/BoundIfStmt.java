package net.stonygeist.interpreter.binder.stmt;

import net.stonygeist.interpreter.binder.expr.BoundExpr;

import javax.annotation.Nullable;

public final class BoundIfStmt extends BoundStmt {
    public final BoundExpr condition;
    public final BoundStmt thenStmt;
    public final @Nullable BoundStmt elseStmt;

    public BoundIfStmt(BoundExpr condition, BoundStmt thenStmt, @Nullable BoundStmt elseStmt) {
        this.condition = condition;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
    }
}
