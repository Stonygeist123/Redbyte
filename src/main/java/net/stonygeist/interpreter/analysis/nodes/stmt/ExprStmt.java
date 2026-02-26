package net.stonygeist.interpreter.analysis.nodes.stmt;

import net.stonygeist.interpreter.analysis.nodes.expr.Expr;

public final class ExprStmt extends Stmt {
    public final Expr expr;

    public ExprStmt(Expr expr) {
        this.expr = expr;
    }
}
