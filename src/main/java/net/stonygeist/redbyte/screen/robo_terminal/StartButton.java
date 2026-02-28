package net.stonygeist.redbyte.screen.robo_terminal;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.network.PacketDistributor;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.server.C2SEvaluateCodePacket;

import java.util.function.Supplier;

public class StartButton extends Button {
    private final Supplier<String> getText;
    private final RoboEntity roboEntity;

    protected StartButton(int x, int y, int width, int height, MutableComponent message, Supplier<String> getText, RoboEntity roboEntity) {
        super(x, y, width, height, message, b -> {
        }, s -> message);
        this.getText = getText;
        this.roboEntity = roboEntity;
    }

    @Override
    public void onPress() {
        super.onPress();
        Redbyte.CHANNEL.send(new C2SEvaluateCodePacket(roboEntity.getRedbyteID(), getText.get()), PacketDistributor.SERVER.noArg());
    }
}
