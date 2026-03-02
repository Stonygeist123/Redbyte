package net.stonygeist.redbyte.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.interpreter.diagnostics.DiagnosticBag;
import net.stonygeist.redbyte.manager.PseudoRobo;
import net.stonygeist.redbyte.manager.RoboRegistry;

import java.util.UUID;

public class C2SDiagnosticsPacket {
    private final UUID redbyteID;
    private final boolean buildDone;
    private final CompoundTag diagnostics;

    public C2SDiagnosticsPacket(UUID redbyteID, boolean buildDone, CompoundTag diagnostics) {
        this.redbyteID = redbyteID;
        this.buildDone = buildDone;
        this.diagnostics = diagnostics;
    }

    public static void handle(C2SDiagnosticsPacket msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                RoboRegistry registry = RoboRegistry.get(sender.serverLevel());
                PseudoRobo robo = registry.get(msg.redbyteID);
                if (robo != null) {
                    robo.setBuildDone(msg.buildDone);
                    DiagnosticBag diagnostics = DiagnosticBag.deserializeNBT(msg.diagnostics);
                    robo.setDiagnostics(diagnostics);
                    registry.setDirty();
                    RoboEntity roboEntity = robo.resolveEntity(sender.serverLevel());
                    if (roboEntity != null) {
                        roboEntity.setBuildDone(msg.buildDone);
                        roboEntity.setDiagnostics(diagnostics);
                    }
                }
            }
        });

        ctx.setPacketHandled(true);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(redbyteID);
        buffer.writeBoolean(buildDone);
        buffer.writeNbt(diagnostics);
    }

    public static C2SDiagnosticsPacket decode(FriendlyByteBuf buffer) {
        return new C2SDiagnosticsPacket(buffer.readUUID(), buffer.readBoolean(), buffer.readNbt());
    }
}
