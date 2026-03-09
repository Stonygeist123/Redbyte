package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class AssignExpr extends Expr {
    public final Token name;
    public final Token equals;
    public final Expr value;

    public AssignExpr(Token name, Token equals, Expr value) {
        this.name = name;
        this.equals = equals;
        this.value = value;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.assignment");
    }

    public static Component docs() {
        return DocsBuilder.start()
                .name("docs.redbyte.general.name")
                .punct(" = ")
                .value("docs.redbyte.general.value")
                .build();
    }

    public static Component example() {
        return DocsBuilder.start()
                .name("x")
                .punct(" = ")
                .value("10")
                .build();
    }
}
