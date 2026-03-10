package net.stonygeist.redbyte.interpreter.analysis.nodes.stmt;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;
import net.stonygeist.redbyte.interpreter.analysis.nodes.expr.Expr;

import javax.annotation.Nullable;

public final class IfStmt extends Stmt {
    public final Token keywordToken;
    public final Expr condition;
    public final Stmt thenStmt;
    public final @Nullable Token elseToken;
    public final @Nullable Stmt elseStmt;

    public IfStmt(Token keywordToken, Expr condition, Stmt thenStmt, @Nullable Token elseToken, @Nullable Stmt elseStmt) {
        this.keywordToken = keywordToken;
        this.condition = condition;
        this.thenStmt = thenStmt;
        this.elseToken = elseToken;
        this.elseStmt = elseStmt;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.if");
    }

    public static Component syntax() {
        return DocsBuilder.start()
                .general("if")
                .space()
                .valueTranslate("syntax.redbyte.general.condition")
                .space()
                .valueTranslate("syntax.redbyte.general.statement")
                .space()
                .punct("[")
                .general("else")
                .space()
                .valueTranslate("syntax.redbyte.general.statement")
                .punct("]")
                .build();
    }

    public static Component docs() {
        return Component.translatable("docs.redbyte.explanation.if");
    }

    public static Component example() {
        return DocsBuilder.start()
                .general("if ")
                .name("x")
                .punct(" == ")
                .value("3")
                .punct(" {")
                .tab()
                .newLine()
                .name("print")
                .punct("(")
                .value("\":)\"")
                .punct(")")
                .newLine()
                .name("print")
                .punct("(")
                .value("\"abc\"")
                .punct(")")
                .untab()
                .newLine()
                .punct("} ")
                .general("else")
                .tab()
                .newLine()
                .name("print")
                .punct("(")
                .value("\":(\"")
                .punct(")")
                .build();
    }
}
