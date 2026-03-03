package net.stonygeist.redbyte.menu.robo_terminal.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.server.C2SEvaluateCodePacket;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class RunButton extends Button {
    private final RoboEntity roboEntity;
    private final Supplier<Boolean> isDisabled;

    protected RunButton(int x, int y, int width, int height, RoboEntity roboEntity, Supplier<Boolean> isDisabled) {
        super(x, y, width, height, Component.translatable("screen.redbyte.robo_terminal.run_disabled"), b -> {
        }, Supplier::get);
        this.roboEntity = roboEntity;
        this.isDisabled = isDisabled;
    }

    @Override
    public @NotNull Component getMessage() {
        return isDisabled.get() || roboEntity.getRedbyteID().isPresent() ? Component.translatable("screen.redbyte.robo_terminal.run_disabled") : Component.translatable("screen.redbyte.robo_terminal.run");
    }

    @Override
    public void onPress() {
        if (!isDisabled.get() && roboEntity.getRedbyteID().isPresent())
            Redbyte.CHANNEL.send(new C2SEvaluateCodePacket(roboEntity.getRedbyteID().get()), PacketDistributor.SERVER.noArg());
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return false;
    }
}
