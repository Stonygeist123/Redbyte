package net.stonygeist.redbyte.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.stonygeist.redbyte.manager.PseudoRobo;
import net.stonygeist.redbyte.manager.RoboRegistry;
import net.stonygeist.redbyte.menu.robo_docs.RoboDocs;

import java.util.UUID;

public class C2SOpenDocsPacket {
    private final UUID redbyteID;

    public C2SOpenDocsPacket(UUID redbyteID) {
        this.redbyteID = redbyteID;
    }

    public static void handle(C2SOpenDocsPacket msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                RoboRegistry registry = RoboRegistry.get(sender.serverLevel());
                PseudoRobo robo = registry.get(msg.redbyteID);
                if (robo != null && robo.getEntity() != null)
                    sender.openMenu(
                            new SimpleMenuProvider(
                                    (containerID, inventory, ignored) -> new RoboDocs(containerID, inventory, msg.redbyteID),
                                    Component.literal("")
                            ),
                            (buffer) -> buffer.writeUUID(msg.redbyteID));
            }
        });

        ctx.setPacketHandled(true);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(redbyteID);
    }

    public static C2SOpenDocsPacket decode(FriendlyByteBuf buffer) {
        return new C2SOpenDocsPacket(buffer.readUUID());
    }
}
