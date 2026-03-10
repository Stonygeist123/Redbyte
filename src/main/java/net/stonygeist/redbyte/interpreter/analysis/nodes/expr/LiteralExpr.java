package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class LiteralExpr extends Expr {
    public final Token token;

    public LiteralExpr(Token token) {
        this.token = token;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.literal");
    }

    public static Component docs() {
        return DocsBuilder.start().valueTranslate("syntax.redbyte.general.value").build();
    }

    public static Component example() {
        return DocsBuilder.start().value("42").build();
    }
}
