package net.stonygeist.redbyte.interpreter.analysis.nodes.stmt;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class BlockStmt extends Stmt {
    public final Token lBrace;
    public final Stmt[] stmts;
    public final Token rBrace;

    public BlockStmt(Token lBrace, Stmt[] stmts, Token rBrace) {
        this.lBrace = lBrace;
        this.stmts = stmts;
        this.rBrace = rBrace;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.block");
    }

    public static Component syntax() {
        return DocsBuilder.start()
                .general("{")
                .space()
                .valueTranslate("syntax.redbyte.general.statement")
                .punct("...")
                .space()
                .general("}")
                .build();
    }

    public static Component docs() {
        return Component.translatable("docs.redbyte.explanation.block");
    }

    public static Component example() {
        return DocsBuilder.start()
                .general("{")
                .tab()
                .newLine()
                .name("print")
                .punct("(")
                .value("3")
                .punct(")")
                .newLine()
                .name("print")
                .punct("(")
                .value("\"abc\"")
                .punct(")")
                .untab()
                .newLine()
                .general("}")
                .build();
    }
}
