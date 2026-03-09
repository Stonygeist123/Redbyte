package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class UnaryExpr extends Expr {
    public final Expr operand;
    public final Token op;

    public UnaryExpr(Expr operand, Token op) {
        this.operand = operand;
        this.op = op;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.unary");
    }

    public static Component docs() {
        return DocsBuilder.start()
                .general("docs.redbyte.general.unary_operator")
                .value("docs.redbyte.general.expression")
                .build();
    }

    public static Component example() {
        return DocsBuilder.start()
                .punct("-")
                .value("5")
                .build();
    }
}
