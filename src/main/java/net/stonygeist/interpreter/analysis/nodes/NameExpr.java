package net.stonygeist.interpreter.analysis.nodes;

public final class NameExpr extends Expr {
    public final Token name;

    public NameExpr(Token name) {
        this.name = name;
    }
}
