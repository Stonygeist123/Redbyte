package net.stonygeist.redbyte.interpreter.analysis.nodes.expr;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Node;

public abstract class Expr extends Node {
    public static Component title() {
        return Component.empty();
    }

    public static Component docs() {
        return Component.empty();
    }

    public static Component example() {
        return Component.empty();
    }
}
