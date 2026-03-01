package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public class ErrorExpr extends Expr {
    public final Token token;

    public ErrorExpr(Token token) {
        this.token = token;
    }
}
