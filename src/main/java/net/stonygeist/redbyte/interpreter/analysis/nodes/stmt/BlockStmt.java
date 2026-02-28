package net.stonygeist.redbyte.interpreter.analysis.nodes.stmt;

import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class BlockStmt extends Stmt {
    public final Token lBrace;
    public final Stmt[] stmts;
    public final Token rBrace;

    public BlockStmt(Token lBrace, Stmt[] stmts, Token rBrace) {
        this.lBrace = lBrace;
        this.stmts = stmts;
        this.rBrace = rBrace;
    }
}
