package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class GroupExpr extends Expr {
    public final Token lParen;
    public final Expr expr;
    public final Token rParen;

    public GroupExpr(Token lParen, Expr expr, Token rParen) {
        this.lParen = lParen;
        this.expr = expr;
        this.rParen = rParen;
    }
}
