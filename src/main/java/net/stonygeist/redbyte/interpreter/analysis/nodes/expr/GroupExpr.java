package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class GroupExpr extends Expr {
    public final Token lParen;
    public final Expr expr;
    public final Token rParen;

    public GroupExpr(Token lParen, Expr expr, Token rParen) {
        this.lParen = lParen;
        this.expr = expr;
        this.rParen = rParen;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.group");
    }

    public static Component docs() {
        return DocsBuilder.start()
                .punct("(")
                .valueTranslate("syntax.redbyte.general.expression")
                .punct(")")
                .build();
    }

    public static Component example() {
        return DocsBuilder.start()
                .punct("(")
                .value("a")
                .punct(" + ")
                .value("b")
                .punct(")")
                .punct(" * ")
                .value("c")
                .build();
    }
}
