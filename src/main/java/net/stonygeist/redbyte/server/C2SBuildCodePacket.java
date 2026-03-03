package net.stonygeist.redbyte.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.stonygeist.redbyte.manager.PseudoRobo;
import net.stonygeist.redbyte.manager.RoboRegistry;

import java.util.UUID;

public class C2SBuildCodePacket {
    private final UUID redbyteID;
    private final String code;

    public C2SBuildCodePacket(UUID redbyteID, String code) {
        this.redbyteID = redbyteID;
        this.code = code;
    }

    public static void handle(C2SBuildCodePacket msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                RoboRegistry registry = RoboRegistry.get(sender.serverLevel());
                PseudoRobo robo = registry.get(msg.redbyteID);
                if (robo != null) {
                    robo.setCode(msg.code);
                    robo.build();
                    registry.setDirty();
                }
            }
        });

        ctx.setPacketHandled(true);
    }

    public void encode(FriendlyByteBuf buffer) {
        if (redbyteID != null) {
            buffer.writeUUID(redbyteID);
            buffer.writeUtf(code);
        }
    }

    public static C2SBuildCodePacket decode(FriendlyByteBuf buffer) {
        return new C2SBuildCodePacket(buffer.readUUID(), buffer.readUtf());
    }
}
