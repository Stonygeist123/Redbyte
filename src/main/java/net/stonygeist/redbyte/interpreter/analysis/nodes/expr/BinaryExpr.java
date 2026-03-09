package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class BinaryExpr extends Expr {
    public final Expr left, right;
    public final Token op;

    public BinaryExpr(Expr left, Token op, Expr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.binary");
    }


    public static Component docs() {
        return DocsBuilder.start()
                .value("docs.redbyte.general.value")
                .punct(" ")
                .general("docs.redbyte.general.binary_operator")
                .punct(" ")
                .value("docs.redbyte.general.value")
                .build();
    }

    public static Component example() {
        return DocsBuilder.start()
                .value("5")
                .punct(" + ")
                .value("3")
                .build();
    }
}
