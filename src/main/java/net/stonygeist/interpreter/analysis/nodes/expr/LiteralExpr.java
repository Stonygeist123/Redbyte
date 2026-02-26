package net.stonygeist.interpreter.analysis.nodes.expr;

import net.stonygeist.interpreter.analysis.nodes.Token;

public final class LiteralExpr extends Expr {
    public final Token token;

    public LiteralExpr(Token token) {
        this.token = token;
    }
}
