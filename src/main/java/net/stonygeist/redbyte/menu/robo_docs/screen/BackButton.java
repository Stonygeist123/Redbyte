package net.stonygeist.redbyte.menu.robo_docs.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.server.C2SOpenTerminalPacket;

import java.util.UUID;
import java.util.function.Supplier;

public class BackButton extends Button {
    private final UUID redbyteID;

    public BackButton(int x, int y, int width, int height, UUID redbyteID) {
        super(x, y, width, height, Component.translatable("screen.redbyte.general.back"), b -> {
        }, Supplier::get);
        this.redbyteID = redbyteID;
    }

    @Override
    public void onPress() {
        Redbyte.CHANNEL.send(new C2SOpenTerminalPacket(redbyteID), PacketDistributor.SERVER.noArg());
    }
}
