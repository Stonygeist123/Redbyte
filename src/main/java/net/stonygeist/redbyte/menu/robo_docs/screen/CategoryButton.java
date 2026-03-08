package net.stonygeist.redbyte.menu.robo_docs.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class CategoryButton extends Button {
    public CategoryButton(int x, int y, int width, int height, Component message, RoboDocsScreen.Category category, Consumer<RoboDocsScreen.Category> setCategory) {
        super(x, y, width, height, message, b -> setCategory.accept(category), Supplier::get);
    }
}
