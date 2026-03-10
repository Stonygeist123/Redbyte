package net.stonygeist.redbyte.interpreter.analysis.nodes.stmt;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.expr.Expr;
import net.stonygeist.redbyte.menu.robo_docs.screen.RoboDocsScreen;

public final class ExprStmt extends Stmt {
    public final Expr expr;

    public ExprStmt(Expr expr) {
        this.expr = expr;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.expression");
    }

    public static Component syntax() {
        return Component.translatable("syntax.redbyte.general.expression").withColor(RoboDocsScreen.VALUE_COLOR);
    }

    public static Component docs() {
        return Component.translatable("docs.redbyte.explanation.expression");
    }

    public static Component example() {
        return DocsBuilder.start()
                .name("print")
                .punct("(")
                .value("3")
                .punct(" * ")
                .value("2")
                .punct(")")
                .build();
    }
}
