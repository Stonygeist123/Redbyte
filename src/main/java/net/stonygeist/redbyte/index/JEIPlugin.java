package net.stonygeist.redbyte.index;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.menu.robo_docs.screen.RoboDocsScreen;
import net.stonygeist.redbyte.menu.robo_terminal.screen.RoboTerminalScreen;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return Redbyte.asResource("jei_plugin");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(RoboDocsScreen.class,
                new IGuiContainerHandler<>() {
                    @Override
                    public @NotNull List<Rect2i> getGuiExtraAreas(@NotNull RoboDocsScreen containerScreen) {
                        return List.of(new Rect2i(
                                0,
                                0,
                                containerScreen.width,
                                containerScreen.height
                        ));
                    }
                });
        registration.addGuiContainerHandler(RoboTerminalScreen.class,
                new IGuiContainerHandler<>() {
                    @Override
                    public @NotNull List<Rect2i> getGuiExtraAreas(@NotNull RoboTerminalScreen containerScreen) {
                        return List.of(new Rect2i(
                                0,
                                0,
                                containerScreen.width,
                                containerScreen.height
                        ));
                    }
                });
    }
}
