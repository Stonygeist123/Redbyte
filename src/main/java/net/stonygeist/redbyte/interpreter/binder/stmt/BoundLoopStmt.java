package net.stonygeist.redbyte.interpreter.binder.stmt;

import net.stonygeist.redbyte.interpreter.binder.expr.BoundExpr;

public record BoundLoopStmt(BoundExpr count, BoundStmt body) implements BoundStmt {
}
