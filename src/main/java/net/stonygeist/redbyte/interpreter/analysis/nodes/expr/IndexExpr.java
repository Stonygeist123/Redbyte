package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class IndexExpr extends Expr {
    public final Expr base;
    public final Token lBracket;
    public final Expr index;
    public final Token rBracket;

    public IndexExpr(Expr base, Token lBracket, Expr index, Token rBracket) {
        this.base = base;
        this.lBracket = lBracket;
        this.index = index;
        this.rBracket = rBracket;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.index");
    }

    public static Component docs() {
        return DocsBuilder.start()
                .valueTranslate("syntax.redbyte.general.expression")
                .punct("[")
                .valueTranslate("syntax.redbyte.general.expression")
                .punct("]")
                .build();
    }

    public static Component example() {
        return DocsBuilder.start()
                .name("myList")
                .punct("[")
                .value("0")
                .punct("]")
                .build();
    }
}
