package net.stonygeist.interpreter.analysis.nodes.expr;

import net.stonygeist.interpreter.analysis.nodes.Token;

public final class AssignExpr extends Expr {
    public final Token name;
    public final Token equals;
    public final Expr value;

    public AssignExpr(Token name, Token equals, Expr value) {
        this.name = name;
        this.equals = equals;
        this.value = value;
    }
}
