package net.stonygeist.redbyte.interpreter.binder.stmt;

import net.stonygeist.redbyte.interpreter.binder.expr.BoundExpr;

import javax.annotation.Nullable;

public record BoundIfStmt(BoundExpr condition, BoundStmt thenStmt, @Nullable BoundStmt elseStmt) implements BoundStmt {
}
