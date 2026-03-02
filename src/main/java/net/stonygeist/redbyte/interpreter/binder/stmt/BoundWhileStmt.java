package net.stonygeist.redbyte.interpreter.binder.stmt;

import net.stonygeist.redbyte.interpreter.binder.expr.BoundExpr;

public record BoundWhileStmt(BoundExpr condition, BoundStmt body) implements BoundStmt {
}
