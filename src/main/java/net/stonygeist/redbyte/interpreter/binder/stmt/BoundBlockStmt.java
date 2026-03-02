package net.stonygeist.redbyte.interpreter.binder.stmt;

import com.google.common.collect.ImmutableList;

public record BoundBlockStmt(ImmutableList<BoundStmt> stmts) implements BoundStmt {
}
