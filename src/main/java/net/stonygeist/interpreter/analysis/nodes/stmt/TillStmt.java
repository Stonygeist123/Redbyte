package net.stonygeist.interpreter.analysis.nodes.stmt;

import net.stonygeist.interpreter.analysis.nodes.Token;
import net.stonygeist.interpreter.analysis.nodes.expr.Expr;

public final class TillStmt extends Stmt {
    public final Token keywordToken;
    public final Expr condition;
    public final Stmt stmt;

    public TillStmt(Token keywordToken, Expr condition, Stmt stmt) {
        this.keywordToken = keywordToken;
        this.condition = condition;
        this.stmt = stmt;
    }
}
