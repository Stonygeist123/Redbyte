package net.stonygeist.interpreter.analysis.nodes.stmt;

import net.stonygeist.interpreter.analysis.nodes.Token;
import net.stonygeist.interpreter.analysis.nodes.expr.Expr;

public final class LoopStmt extends Stmt {
    public final Token keywordToken;
    public final Expr count;
    public final Stmt stmt;

    public LoopStmt(Token keywordToken, Expr count, Stmt stmt) {
        this.keywordToken = keywordToken;
        this.count = count;
        this.stmt = stmt;
    }
}
