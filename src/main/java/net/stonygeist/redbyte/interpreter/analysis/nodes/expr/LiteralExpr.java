package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class LiteralExpr extends Expr {
    public final Token token;

    public LiteralExpr(Token token) {
        this.token = token;
    }
}
