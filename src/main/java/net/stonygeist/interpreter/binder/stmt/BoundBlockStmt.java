package net.stonygeist.interpreter.binder.stmt;

import com.google.common.collect.ImmutableList;

public final class BoundBlockStmt extends BoundStmt {
    public final ImmutableList<BoundStmt> stmts;

    public BoundBlockStmt(ImmutableList<BoundStmt> stmts) {
        this.stmts = stmts;
    }
}
