package net.stonygeist.redbyte.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.stonygeist.redbyte.manager.PseudoRobo;
import net.stonygeist.redbyte.manager.RoboRegistry;

import java.util.UUID;

public class C2SStopEvaluationPacket {
    private final UUID redbyteID;

    public C2SStopEvaluationPacket(UUID redbyteID) {
        this.redbyteID = redbyteID;
    }

    public static void handle(C2SStopEvaluationPacket msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                RoboRegistry registry = RoboRegistry.get(sender.serverLevel());
                PseudoRobo robo = registry.get(msg.redbyteID);
                if (robo != null) {
                    robo.stopEvaluation();
                    registry.setDirty();
                }
            }
        });

        ctx.setPacketHandled(true);
    }

    public void encode(FriendlyByteBuf buffer) {
        if (redbyteID != null)
            buffer.writeUUID(redbyteID);
    }

    public static C2SStopEvaluationPacket decode(FriendlyByteBuf buffer) {
        return new C2SStopEvaluationPacket(buffer.readUUID());
    }
}
