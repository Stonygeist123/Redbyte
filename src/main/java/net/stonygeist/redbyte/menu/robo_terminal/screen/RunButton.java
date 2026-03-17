package net.stonygeist.redbyte.menu.robo_terminal.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.server.C2SEvaluateCodePacket;
import net.stonygeist.redbyte.server.C2SStopEvaluationPacket;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class RunButton extends Button {
    private final RoboEntity roboEntity;
    private final Supplier<Boolean> isDisabled;
    private final Supplier<Boolean> isRuntime;

    protected RunButton(int x, int y, int width, int height, RoboEntity roboEntity, Supplier<Boolean> isDisabled, Supplier<Boolean> isRuntime) {
        super(x, y, width, height, Component.translatable("menu.redbyte.robo_terminal.run_disabled"), b -> {
        }, Supplier::get);
        this.roboEntity = roboEntity;
        this.isDisabled = isDisabled;
        this.isRuntime = isRuntime;
    }

    @Override
    public @NotNull Component getMessage() {
        return isRuntime.get() ? Component.translatable("menu.redbyte.robo_terminal.stop") : isDisabled.get() || roboEntity.getRedbyteID().isEmpty()
                ? Component.translatable("menu.redbyte.robo_terminal.run_disabled")
                : Component.translatable("menu.redbyte.robo_terminal.run");
    }

    @Override
    public void onPress() {
        if (isRuntime.get())
            Redbyte.CHANNEL.send(new C2SStopEvaluationPacket(roboEntity.getRedbyteID().get()), PacketDistributor.SERVER.noArg());
        else if (!isDisabled.get() && roboEntity.getRedbyteID().isPresent())
            Redbyte.CHANNEL.send(new C2SEvaluateCodePacket(roboEntity.getRedbyteID().get()), PacketDistributor.SERVER.noArg());
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return false;
    }
}
