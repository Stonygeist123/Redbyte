package net.stonygeist.redbyte.menu.robo_docs.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.stonygeist.redbyte.menu.robo_docs.RoboDocs;
import org.jetbrains.annotations.NotNull;

public class RoboDocsScreen extends AbstractContainerScreen<RoboDocs> {
    private static final int BORDER_COLOR = 0xff808080;
    private static final int SCREEN_COLOR = 0xff000000;

    private static final int TERMINAL_WIDTH = 800;
    private static final int TERMINAL_HEIGHT = 400;
    private static final int NAV_BAR_HEIGHT = 40;

    private static final int TEXT_PADDING_X = 14;
    private static final int TEXT_PADDING_Y = 20;

    public RoboDocsScreen(RoboDocs menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new BackButton(
                (width - TERMINAL_WIDTH) / 2 + 25, (height - TERMINAL_HEIGHT) / 2, 100, 20,
                getMenu().getRedbyteID())
        );
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - TERMINAL_WIDTH) / 2;
        int y = (height - TERMINAL_HEIGHT) / 2;
        guiGraphics.drawString(font, getTitle(), x + 6, y + NAV_BAR_HEIGHT, 0x00ff00);
        guiGraphics.fill(x - 4, y - 4, x + TERMINAL_WIDTH + 4, y + TERMINAL_HEIGHT + 4, BORDER_COLOR);
        guiGraphics.fill(x, y, x + TERMINAL_WIDTH, y + TERMINAL_HEIGHT, SCREEN_COLOR);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }
}
