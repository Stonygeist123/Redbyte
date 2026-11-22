package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class IndexAssignExpr extends Expr {
    public final IndexExpr indexing;
    public final Token equals;
    public final Expr value;

    public IndexAssignExpr(IndexExpr indexing, Token equals, Expr value) {
        this.indexing = indexing;
        this.equals = equals;
        this.value = value;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.index_assignment");
    }

    public static Component docs() {
        return DocsBuilder.start()
                .generalTranslate("syntax.redbyte.general.expression")
                .punct("[")
                .valueTranslate("syntax.redbyte.general.expression")
                .punct("]")
                .punct(" = ")
                .valueTranslate("syntax.redbyte.general.value")
                .build();
    }

    public static Component example() {
        return DocsBuilder.start()
                .name("myList")
                .punct("[")
                .value("0")
                .punct("]")
                .punct(" = ")
                .value("69")
                .build();
    }
}
