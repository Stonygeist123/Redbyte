package net.stonygeist.redbyte.interpreter.analysis.nodes.stmt;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;
import net.stonygeist.redbyte.interpreter.analysis.nodes.expr.Expr;

public final class AlwaysStmt extends Stmt {
    public final Token keywordToken;
    public final Expr condition;
    public final Stmt stmt;

    public AlwaysStmt(Token keywordToken, Expr condition, Stmt stmt) {
        this.keywordToken = keywordToken;
        this.condition = condition;
        this.stmt = stmt;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.always");
    }

    public static Component syntax() {
        return DocsBuilder.start()
                .general("always")
                .space()
                .valueTranslate("syntax.redbyte.general.condition")
                .space()
                .valueTranslate("syntax.redbyte.general.statement")
                .build();
    }

    public static Component docs() {
        return Component.translatable("docs.redbyte.explanation.always");
    }

    public static Component example() {
        return DocsBuilder.start()
                .general("always")
                .space()
                .punct("!")
                .name("is_nothing")
                .punct("(")
                .name("get_nearest_monster")
                .punct("(")
                .value("2")
                .punct(")")
                .punct(")")
                .tab()
                .newLine()
                .name("try_attack")
                .punct("(")
                .name("get_nearest_monster")
                .punct("(")
                .value("2")
                .punct(")")
                .punct(")")
                .build();
    }
}
