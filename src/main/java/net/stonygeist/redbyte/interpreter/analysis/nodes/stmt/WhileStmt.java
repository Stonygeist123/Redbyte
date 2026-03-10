package net.stonygeist.redbyte.interpreter.analysis.nodes.stmt;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;
import net.stonygeist.redbyte.interpreter.analysis.nodes.expr.Expr;

public final class WhileStmt extends Stmt {
    public final Token keywordToken;
    public final Expr condition;
    public final Stmt stmt;

    public WhileStmt(Token keywordToken, Expr condition, Stmt stmt) {
        this.keywordToken = keywordToken;
        this.condition = condition;
        this.stmt = stmt;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.while");
    }

    public static Component syntax() {
        return DocsBuilder.start()
                .general("while")
                .space()
                .valueTranslate("syntax.redbyte.general.condition")
                .space()
                .valueTranslate("syntax.redbyte.general.statement")
                .build();
    }

    public static Component docs() {
        return Component.translatable("docs.redbyte.explanation.while");
    }

    public static Component example() {
        return DocsBuilder.start()
                .general("while")
                .space()
                .name("z")
                .punct(" < ")
                .value("17")
                .tab()
                .newLine()
                .name("walk_to")
                .punct("(")
                .value("x")
                .punct(", ")
                .value("y")
                .punct(", ")
                .value("z")
                .punct(" + ")
                .value("2")
                .punct(")")
                .build();
    }
}
