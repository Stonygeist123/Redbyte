package net.stonygeist.redbyte.interpreter.analysis.nodes.stmt;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;
import net.stonygeist.redbyte.interpreter.analysis.nodes.expr.Expr;

public final class OnceStmt extends Stmt {
    public final Token keywordToken;
    public final Expr condition;
    public final Stmt stmt;

    public OnceStmt(Token keywordToken, Expr condition, Stmt stmt) {
        this.keywordToken = keywordToken;
        this.condition = condition;
        this.stmt = stmt;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.once");
    }

    public static Component syntax() {
        return DocsBuilder.start()
                .general("once")
                .space()
                .valueTranslate("syntax.redbyte.general.condition")
                .space()
                .valueTranslate("syntax.redbyte.general.statement")
                .build();
    }

    public static Component docs() {
        return Component.translatable("docs.redbyte.explanation.once");
    }

    public static Component example() {
        return DocsBuilder.start()
                .general("once")
                .space()
                .name("y")
                .punct(" >= ")
                .value("65")
                .tab()
                .newLine()
                .name("jump")
                .punct("()")
                .build();
    }
}
