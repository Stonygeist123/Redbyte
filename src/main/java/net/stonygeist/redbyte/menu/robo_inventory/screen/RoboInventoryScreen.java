package net.stonygeist.redbyte.menu.robo_inventory.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.menu.robo_inventory.RoboInventory;
import org.jetbrains.annotations.NotNull;

public class RoboInventoryScreen extends AbstractContainerScreen<RoboInventory> {
    private static final ResourceLocation GUI_TEXTURE = Redbyte.asResource("textures/gui/robo_inventory_with_tools.png");

    public RoboInventoryScreen(RoboInventory menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void init() {
        super.init();
        RoboEntity roboEntity = getMenu().getRoboEntity();
        if (roboEntity != null && roboEntity.getRedbyteID().isPresent())
            addRenderableWidget(new BackButton(
                    (width - imageWidth) / 2 + 5, (height - imageHeight) / 2 + 5, 12, 12,
                    roboEntity.getRedbyteID().get())
            );
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
