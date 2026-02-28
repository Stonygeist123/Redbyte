package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class UnaryExpr extends Expr {
    public final Expr operand;
    public final Token op;

    public UnaryExpr(Expr operand, Token op) {
        this.operand = operand;
        this.op = op;
    }
}
