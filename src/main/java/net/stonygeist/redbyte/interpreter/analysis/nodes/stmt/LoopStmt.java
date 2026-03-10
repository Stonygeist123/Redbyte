package net.stonygeist.redbyte.interpreter.analysis.nodes.stmt;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;
import net.stonygeist.redbyte.interpreter.analysis.nodes.expr.Expr;

public final class LoopStmt extends Stmt {
    public final Token keywordToken;
    public final Expr count;
    public final Stmt stmt;

    public LoopStmt(Token keywordToken, Expr count, Stmt stmt) {
        this.keywordToken = keywordToken;
        this.count = count;
        this.stmt = stmt;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.loop");
    }

    public static Component syntax() {
        return DocsBuilder.start()
                .general("loop")
                .space()
                .valueTranslate("syntax.redbyte.general.count")
                .space()
                .valueTranslate("syntax.redbyte.general.statement")
                .build();
    }

    public static Component docs() {
        return Component.translatable("docs.redbyte.explanation.loop");
    }

    public static Component example() {
        return DocsBuilder.start()
                .general("loop")
                .space()
                .value("3")
                .tab()
                .newLine()
                .name("walk")
                .punct("(")
                .value("1")
                .punct(")")
                .build();
    }
}
