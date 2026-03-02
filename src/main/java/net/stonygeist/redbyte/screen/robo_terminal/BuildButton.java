package net.stonygeist.redbyte.screen.robo_terminal;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.server.C2SBuildCodePacket;

import java.util.function.Supplier;

public class BuildButton extends Button {
    private final Supplier<String> getText;
    private final RoboEntity roboEntity;

    protected BuildButton(int x, int y, int width, int height, Supplier<String> getText, RoboEntity roboEntity) {
        super(x, y, width, height, Component.translatable("screen.redbyte.robo_terminal.build"), b -> {
        }, Supplier::get);
        this.getText = getText;
        this.roboEntity = roboEntity;
    }

    @Override
    public void onPress() {
        Redbyte.CHANNEL.send(new C2SBuildCodePacket(roboEntity.getRedbyteID(), getText.get()), PacketDistributor.SERVER.noArg());
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return false;
    }
}
