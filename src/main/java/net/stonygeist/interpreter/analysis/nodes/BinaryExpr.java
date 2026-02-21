package net.stonygeist.interpreter.analysis.nodes;

public final class BinaryExpr extends Expr {
    public final Expr left, right;
    public final Token op;

    public BinaryExpr(Expr left, Token op, Expr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
}
