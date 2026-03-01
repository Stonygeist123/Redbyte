package net.stonygeist.redbyte.interpreter.binder.stmt;

import net.stonygeist.redbyte.interpreter.binder.expr.BoundExpr;

public record BoundExprStmt(BoundExpr expr) implements BoundStmt {
}
