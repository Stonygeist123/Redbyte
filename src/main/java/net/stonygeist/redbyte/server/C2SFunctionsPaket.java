package net.stonygeist.redbyte.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.manager.PseudoRobo;
import net.stonygeist.redbyte.manager.RoboRegistry;

import java.util.UUID;

public final class C2SFunctionsPaket {
    public static class WalkFunction {
        private final UUID redbyteID;
        private final float blocks;

        public WalkFunction(UUID redbyteID, float blocks) {
            this.redbyteID = redbyteID;
            this.blocks = blocks;
        }

        public static void handle(WalkFunction msg, CustomPayloadEvent.Context ctx) {
            ctx.enqueueWork(() -> {
                ServerPlayer sender = ctx.getSender();
                if (sender != null) {
                    RoboRegistry registry = RoboRegistry.get(sender.serverLevel());
                    PseudoRobo robo = registry.get(msg.redbyteID);
                    if (robo != null) {
                        RoboEntity roboEntity = robo.resolveEntity(sender.serverLevel());
                        if (roboEntity != null) {
                            Vec3 lookDirection = roboEntity.getViewVector(1).normalize();
                            Vec3 pos = new Vec3(roboEntity.getX(), roboEntity.getEyeY(), roboEntity.getZ())
                                    .add(lookDirection.multiply(msg.blocks, msg.blocks, msg.blocks));
                            robo.setMoveTarget(pos);
                        }
                    }
                }
            });

            ctx.setPacketHandled(true);
        }

        public void encode(FriendlyByteBuf buffer) {
            buffer.writeUUID(redbyteID);
            buffer.writeFloat(blocks);
        }

        public static WalkFunction decode(FriendlyByteBuf buffer) {
            return new WalkFunction(buffer.readUUID(), buffer.readFloat());
        }
    }

    public static class WalkToFunction {
        private final UUID redbyteID;
        private final Vec3 pos;

        public WalkToFunction(UUID redbyteID, Vec3 pos) {
            this.redbyteID = redbyteID;
            this.pos = pos;
        }

        public static void handle(WalkToFunction msg, CustomPayloadEvent.Context ctx) {
            ctx.enqueueWork(() -> {
                ServerPlayer sender = ctx.getSender();
                if (sender != null) {
                    RoboRegistry registry = RoboRegistry.get(sender.serverLevel());
                    PseudoRobo robo = registry.get(msg.redbyteID);
                    if (robo != null)
                        robo.setMoveTarget(msg.pos);
                }
            });

            ctx.setPacketHandled(true);
        }

        public void encode(FriendlyByteBuf buffer) {
            buffer.writeUUID(redbyteID);
            buffer.writeVec3(pos);
        }

        public static WalkToFunction decode(FriendlyByteBuf buffer) {
            return new WalkToFunction(buffer.readUUID(), buffer.readVec3());
        }
    }

    public static class JumpFunction {
        private final UUID redbyteID;

        public JumpFunction(UUID redbyteID) {
            this.redbyteID = redbyteID;
        }

        public static void handle(JumpFunction msg, CustomPayloadEvent.Context ctx) {
            ctx.enqueueWork(() -> {
                ServerPlayer sender = ctx.getSender();
                if (sender != null) {
                    RoboRegistry registry = RoboRegistry.get(sender.serverLevel());
                    PseudoRobo robo = registry.get(msg.redbyteID);
                    if (robo != null) {
                        RoboEntity roboEntity = robo.resolveEntity(sender.serverLevel());
                        if (roboEntity != null)
                            roboEntity.jumpFromGround();
                    }
                }
            });

            ctx.setPacketHandled(true);
        }

        public void encode(FriendlyByteBuf buffer) {
            buffer.writeUUID(redbyteID);
        }

        public static JumpFunction decode(FriendlyByteBuf buffer) {
            return new JumpFunction(buffer.readUUID());
        }
    }
}
