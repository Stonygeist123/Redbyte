package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public class RoboExpr extends Expr {
    public Token token;

    public RoboExpr(Token token) {
        this.token = token;
    }
}
