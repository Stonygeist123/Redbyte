package net.stonygeist.redbyte.interpreter.analysis.nodes;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.stonygeist.redbyte.menu.robo_docs.screen.RoboDocsScreen;

public final class DocsBuilder {
    private final MutableComponent component = Component.empty();
    private int tabCount;

    public static DocsBuilder start() {
        return new DocsBuilder();
    }

    public DocsBuilder punct(String value) {
        component.append(Component.literal(value)
                .withColor(RoboDocsScreen.PUNCTUATION_COLOR));
        return this;
    }

    public DocsBuilder value(String value) {
        component.append((Component.literal(value))
                .withColor(RoboDocsScreen.VALUE_COLOR));
        return this;
    }

    public DocsBuilder valueTranslate(String value) {
        component.append((Component.translatable(value))
                .withColor(RoboDocsScreen.VALUE_COLOR));
        return this;
    }

    public DocsBuilder name(String value) {
        component.append((Component.literal(value))
                .withColor(RoboDocsScreen.NAME_COLOR));
        return this;
    }

    public DocsBuilder nameTranslate(String value) {
        component.append((Component.translatable(value))
                .withColor(RoboDocsScreen.NAME_COLOR));
        return this;
    }

    public DocsBuilder general(String value) {
        component.append(Component.literal(value));
        return this;
    }

    public DocsBuilder generalTranslate(String value) {
        component.append(Component.translatable(value));
        return this;
    }

    public DocsBuilder space() {
        component.append(Component.literal(" "));
        return this;
    }

    public DocsBuilder newLine() {
        component.append(Component.literal("\n")).append(Component.literal(" ".repeat(tabCount * 2)));
        return this;
    }

    public DocsBuilder tab() {
        ++tabCount;
        return this;
    }

    public DocsBuilder untab() {
        --tabCount;
        return this;
    }

    public Component build() {
        return component;
    }
}