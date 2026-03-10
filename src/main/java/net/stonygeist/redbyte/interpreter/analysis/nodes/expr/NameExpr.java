package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class NameExpr extends Expr {
    public final Token name;

    public NameExpr(Token name) {
        this.name = name;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.name");
    }

    public static Component docs() {
        return DocsBuilder.start().nameTranslate("syntax.redbyte.general.name").build();
    }

    public static Component example() {
        return DocsBuilder.start().name("target").build();
    }
}
