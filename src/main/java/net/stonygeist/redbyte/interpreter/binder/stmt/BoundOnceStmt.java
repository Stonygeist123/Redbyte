package net.stonygeist.redbyte.interpreter.binder.stmt;

import net.stonygeist.redbyte.interpreter.binder.expr.BoundExpr;

public record BoundOnceStmt(BoundExpr condition, BoundStmt body) implements BoundStmt {
}
