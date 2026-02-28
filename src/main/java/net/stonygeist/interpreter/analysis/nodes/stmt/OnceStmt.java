package net.stonygeist.interpreter.analysis.nodes.stmt;

import net.stonygeist.interpreter.analysis.nodes.Token;
import net.stonygeist.interpreter.analysis.nodes.expr.Expr;

public final class OnceStmt extends Stmt {
    public final Token keywordToken;
    public final Expr condition;
    public final Stmt stmt;

    public OnceStmt(Token keywordToken, Expr condition, Stmt stmt) {
        this.keywordToken = keywordToken;
        this.condition = condition;
        this.stmt = stmt;
    }
}
