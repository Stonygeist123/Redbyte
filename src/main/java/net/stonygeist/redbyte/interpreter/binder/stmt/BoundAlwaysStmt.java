package net.stonygeist.redbyte.interpreter.binder.stmt;

import net.stonygeist.redbyte.interpreter.binder.expr.BoundExpr;

public record BoundAlwaysStmt(BoundExpr condition, BoundStmt body) implements BoundStmt {
}
