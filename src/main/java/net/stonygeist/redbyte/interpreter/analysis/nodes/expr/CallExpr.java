package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class CallExpr extends Expr {
    public final Token name;
    public final Token lParen;
    public final Expr[] args;
    public final Token rParen;

    public CallExpr(Token name, Token lParen, Expr[] args, Token rParen) {
        this.name = name;
        this.lParen = lParen;
        this.args = args;
        this.rParen = rParen;
    }
}
