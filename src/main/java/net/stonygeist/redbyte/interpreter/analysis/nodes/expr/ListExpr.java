package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ListExpr extends Expr {
    public final Token lBracket;
    public final List<Map.Entry<@NotNull Expr, @Nullable Token>> items;
    public final Token rBracket;

    public ListExpr(Token lBracket, List<Map.Entry<@NotNull Expr, @Nullable Token>> items, Token rBracket) {
        this.lBracket = lBracket;
        this.items = items;
        this.rBracket = rBracket;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.list");
    }

    public static Component docs() {
        return DocsBuilder.start()
                .punct("[")
                .valueTranslate("syntax.redbyte.general.expression")
                .punct(", ")
                .valueTranslate("syntax.redbyte.general.expression")
                .punct(", ...")
                .punct("]")
                .build();
    }

    public static Component example() {
        return DocsBuilder.start()
                .punct("[")
                .value("2")
                .punct(", ")
                .value("6.9")
                .punct(", ")
                .value("2")
                .punct(" * ")
                .value("4")
                .punct("]")
                .build();
    }
}
