package net.stonygeist.redbyte.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.stonygeist.redbyte.manager.PseudoRobo;
import net.stonygeist.redbyte.manager.RoboRegistry;
import net.stonygeist.redbyte.menu.robo_inventory.RoboInventory;

import java.util.UUID;

public class C2SOpenInventoryPacket {
    private final UUID redbyteID;
    private final int entityID;

    public C2SOpenInventoryPacket(UUID redbyteID, int entityID) {
        this.redbyteID = redbyteID;
        this.entityID = entityID;
    }

    public static void handle(C2SOpenInventoryPacket msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                RoboRegistry registry = RoboRegistry.get(sender.serverLevel());
                PseudoRobo robo = registry.get(msg.redbyteID);
                if (robo != null && robo.getEntity() != null) {
                    sender.openMenu(
                            new SimpleMenuProvider(
                                    (containerID, inventory, ignored) -> new RoboInventory(containerID, inventory, msg.redbyteID, msg.entityID),
                                    Component.literal("")
                            ),
                            (buffer) -> {
                                buffer.writeUUID(msg.redbyteID);
                                buffer.writeInt(msg.entityID);
                            });
                }
            }
        });

        ctx.setPacketHandled(true);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(redbyteID);
        buffer.writeInt(entityID);
    }

    public static C2SOpenInventoryPacket decode(FriendlyByteBuf buffer) {
        return new C2SOpenInventoryPacket(buffer.readUUID(), buffer.readInt());
    }
}
