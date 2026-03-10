package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.DocsBuilder;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;

public final class RoboExpr extends Expr {
    public final Token token;

    public RoboExpr(Token token) {
        this.token = token;
    }

    public static Component title() {
        return Component.translatable("docs.redbyte.title.robo");
    }

    public static Component docs() {
        return DocsBuilder.start().general("robo").build();
    }

    public static Component example() {
        return DocsBuilder.start().general("robo").build();
    }
}
