package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class CallExpr extends Expr {
    public final Token name;
    public final Token lParen;
    public final Expr[] args;
    public final Token rParen;

    public CallExpr(Token name, Token lParen, Expr[] args, Token rParen) {
        this.name = name;
        this.lParen = lParen;
        this.args = args;
        this.rParen = rParen;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.call");
    }

    public static Component docs() {
        return DocsBuilder.start()
                .nameTranslate("syntax.redbyte.general.name")
                .punct("(")
                .valueTranslate("syntax.redbyte.general.expression")
                .punct(", ")
                .valueTranslate("syntax.redbyte.general.expression")
                .punct("...")
                .punct(")")
                .build();
    }

    public static Component example() {
        return DocsBuilder.start()
                .name("print")
                .punct("(")
                .value("\"Hello\"")
                .punct(")")
                .build();
    }
}
