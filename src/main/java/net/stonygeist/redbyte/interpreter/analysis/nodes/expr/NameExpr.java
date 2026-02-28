package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class NameExpr extends Expr {
    public final Token name;

    public NameExpr(Token name) {
        this.name = name;
    }
}
