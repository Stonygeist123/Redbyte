package net.stonygeist.redbyte.interpreter.analysis.nodes;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.stonygeist.redbyte.menu.robo_docs.screen.RoboDocsScreen;

public final class DocsBuilder {
    private final MutableComponent component = Component.empty();

    public static DocsBuilder start() {
        return new DocsBuilder();
    }

    public DocsBuilder punct(String value) {
        component.append(Component.literal(value)
                .withColor(RoboDocsScreen.PUNCTUATION_COLOR));
        return this;
    }

    public DocsBuilder value(String value) {
        component.append((Component.translatable(value))
                .withColor(RoboDocsScreen.VALUE_COLOR));
        return this;
    }

    public DocsBuilder name(String value) {
        component.append((Component.translatable(value))
                .withColor(RoboDocsScreen.NAME_COLOR));
        return this;
    }

    public DocsBuilder general(String value) {
        component.append(Component.translatable(value));
        return this;
    }

    public Component build() {
        return component;
    }
}