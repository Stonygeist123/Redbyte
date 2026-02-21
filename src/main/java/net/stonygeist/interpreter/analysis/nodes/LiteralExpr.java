package net.stonygeist.interpreter.analysis.nodes;

public final class LiteralExpr extends Expr {
    public final Token token;

    public LiteralExpr(Token token) {
        this.token = token;
    }
}
