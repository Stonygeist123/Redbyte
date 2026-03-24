package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class MethodExpr extends Expr {
    public final Expr object;
    public final Token dot;
    public final Token method;
    public final Token lParen;
    public final Expr[] args;
    public final Token rParen;

    public MethodExpr(Expr object, Token dot, Token method, Token lParen, Expr[] args, Token rParen) {
        this.object = object;
        this.dot = dot;
        this.method = method;
        this.lParen = lParen;
        this.args = args;
        this.rParen = rParen;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.method");
    }

    public static Component docs() {
        return DocsBuilder.start()
                .nameTranslate("syntax.redbyte.general.expression")
                .punct(".")
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
                .name("my_block")
                .punct(".")
                .name("try_destroy")
                .punct("()")
                .build();
    }
}
