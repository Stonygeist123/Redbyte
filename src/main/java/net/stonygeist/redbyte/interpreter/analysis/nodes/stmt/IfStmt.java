package net.stonygeist.redbyte.interpreter.analysis.nodes.stmt;

import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;
import net.stonygeist.redbyte.interpreter.analysis.nodes.expr.Expr;

import javax.annotation.Nullable;

public final class IfStmt extends Stmt {
    public final Token keywordToken;
    public final Expr condition;
    public final Stmt thenStmt;
    public final @Nullable Token elseToken;
    public final @Nullable Stmt elseStmt;

    public IfStmt(Token keywordToken, Expr condition, Stmt thenStmt, @Nullable Token elseToken, @Nullable Stmt elseStmt) {
        this.keywordToken = keywordToken;
        this.condition = condition;
        this.thenStmt = thenStmt;
        this.elseToken = elseToken;
        this.elseStmt = elseStmt;
    }
}
