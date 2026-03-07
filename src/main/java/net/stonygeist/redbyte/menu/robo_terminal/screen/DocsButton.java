package net.stonygeist.redbyte.menu.robo_terminal.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.server.C2SOpenDocsPacket;

import java.util.function.Supplier;

public class DocsButton extends Button {
    private final RoboEntity roboEntity;

    protected DocsButton(int x, int y, int width, int height, RoboEntity roboEntity) {
        super(x, y, width, height, Component.translatable("screen.redbyte.robo_terminal.docs"), b -> {
        }, Supplier::get);
        this.roboEntity = roboEntity;
    }

    @Override
    public void onPress() {
        if (roboEntity.getRedbyteID().isPresent())
            Redbyte.CHANNEL.send(new C2SOpenDocsPacket(roboEntity.getRedbyteID().get()), PacketDistributor.SERVER.noArg());
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return false;
    }
}
