package net.stonygeist.redbyte.interpreter.analysis.nodes.stmt;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Node;

public abstract class Stmt extends Node {
    public static Component title() {
        return Component.empty();
    }

    public static Component syntax() {
        return Component.empty();
    }

    public static Component docs() {
        return Component.empty();
    }

    public static Component example() {
        return Component.empty();
    }
}
