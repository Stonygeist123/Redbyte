package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class BinaryExpr extends Expr {
    public final Expr left, right;
    public final Token op;

    public BinaryExpr(Expr left, Token op, Expr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
}
