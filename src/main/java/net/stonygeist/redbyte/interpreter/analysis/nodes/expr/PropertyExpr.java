package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class PropertyExpr extends Expr {
    public final Expr object;
    public final Token dot;
    public final Token property;

    public PropertyExpr(Expr object, Token dot, Token property) {
        this.object = object;
        this.dot = dot;
        this.property = property;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.property");
    }

    public static Component docs() {
        return DocsBuilder.start()
                .nameTranslate("syntax.redbyte.general.expression")
                .punct(".")
                .nameTranslate("syntax.redbyte.general.name")
                .build();
    }

    public static Component example() {
        return DocsBuilder.start()
                .name("pos")
                .punct(".")
                .name("y")
                .build();
    }
}
