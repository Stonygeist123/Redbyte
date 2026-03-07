package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class RoboExpr extends Expr {
    public final Token token;

    public RoboExpr(Token token) {
        this.token = token;
    }
}
