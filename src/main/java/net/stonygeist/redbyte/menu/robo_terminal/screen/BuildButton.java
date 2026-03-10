package net.stonygeist.redbyte.menu.robo_terminal.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.server.C2SBuildCodePacket;

import java.util.UUID;
import java.util.function.Supplier;

public class BuildButton extends Button {
    private final Supplier<String> getText;
    private final UUID redbyteID;

    protected BuildButton(int x, int y, int width, int height, Supplier<String> getText, UUID redbyteID) {
        super(x, y, width, height, Component.translatable("menu.redbyte.robo_terminal.build"), b -> {
        }, Supplier::get);
        this.getText = getText;
        this.redbyteID = redbyteID;
    }

    @Override
    public void onPress() {
        if (redbyteID != null)
            Redbyte.CHANNEL.send(new C2SBuildCodePacket(redbyteID, getText.get()), PacketDistributor.SERVER.noArg());
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return false;
    }
}
