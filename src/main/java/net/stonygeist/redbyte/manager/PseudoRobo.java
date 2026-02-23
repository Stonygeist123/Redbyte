package net.stonygeist.redbyte.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.index.RedbyteConfigs;
import net.stonygeist.redbyte.index.RedbyteEntities;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class PseudoRobo {
    private final UUID redbyteID;
    private WeakReference<RoboEntity> entityRef = new WeakReference<>(null);
    private Vec3 pos;
    private String code;
    private ServerLevel serverLevel;
    private Vec3 targetVelocity = Vec3.ZERO;
    private float speed;
    private boolean jumping;

    public PseudoRobo(ServerLevel serverLevel, UUID redbyteID, BlockPos pos, String code) {
        this.redbyteID = redbyteID;
        this.serverLevel = serverLevel;
        this.pos = pos.getCenter().subtract(0, 0.5, 0);
        this.code = code;
        speed = RedbyteConfigs.ROBO_DEFAULT_SPEED;
    }

    public static PseudoRobo deserializeNBT(ServerLevel level, CompoundTag tag) {
        UUID redbyteID = tag.getUUID("redbyteID");
        String code = tag.getString("code");
        Vec3 pos = readVec3FromTag(tag, "pos");
        return new PseudoRobo(level, redbyteID, BlockPos.containing(pos), code);
    }

    public RoboEntity resolveEntity(ServerLevel level) {
        RoboEntity entity = entityRef.get();
        if (entity != null && !entity.isRemoved()) return entity;
        for (Entity e : level.getAllEntities())
            if (e instanceof RoboEntity robo && redbyteID.equals(robo.getRedbyteID())) {
                setEntity(robo);
                return robo;
            }

        return null;
    }

    private void updateEntity() {
        RoboEntity roboEntity = resolveEntity(serverLevel);
        if (roboEntity != null)
            roboEntity.syncFromVirtual(this);
        else
            entityRef.clear();
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("redbyteID", redbyteID);
        writeVec3ToTag(tag, "pos", pos);
        tag.putString("code", code);
        return tag;
    }

    public void tick(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
        updateEntity();
        move(targetVelocity);

        // Spawn / despawn RoboEntity if needed
        BlockPos pos = BlockPos.containing(this.pos);
        if (serverLevel.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            if (getEntity() == null)
                spawnAndRememberEntity();
        } else if (getEntity() != null)
            despawnEntity();
    }

    private void despawnEntity() {
        Entity entity = getEntity();
        if (entity != null)
            entity.discard();
        entityRef.clear();
    }

    private void spawnAndRememberEntity() {
        RoboEntity robo = new RoboEntity(RedbyteEntities.ROBO.get(), serverLevel);
        robo.setRedbyteID(redbyteID);
        robo.setPos(pos.x, pos.y, pos.z);
        serverLevel.addFreshEntity(robo);
        setEntity(robo);
    }

    private void move(Vec3 targetVelocity) {
        pos = pos.add(targetVelocity);
    }

    public void jump() {
        jumping = true;
    }

    public void jumpDone() {
        jumping = false;
    }

    public static Vec3 readVec3FromTag(CompoundTag tag, String key) {
        double x = tag.getDouble(key + "X");
        double y = tag.getDouble(key + "Y");
        double z = tag.getDouble(key + "Z");
        return new Vec3(x, y, z);
    }

    public static void writeVec3ToTag(CompoundTag tag, String key, Vec3 vec) {
        tag.putDouble(key + "X", vec.x);
        tag.putDouble(key + "Y", vec.y);
        tag.putDouble(key + "Z", vec.z);
    }

    public Vec3 getPos() {
        return pos;
    }

    public UUID getRedbyteID() {
        return redbyteID;
    }

    public RoboEntity getEntity() {
        return entityRef.get();
    }

    public void setEntity(RoboEntity entity) {
        entityRef = new WeakReference<>(entity);
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean getJumping() {
        return jumping;
    }

    public void setTargetVelocity(Vec3 targetVelocity) {
        this.targetVelocity = targetVelocity;
    }

    public void setTargetVelocity(BlockPos blockPos) {
        targetVelocity = blockPos.getCenter().subtract(0, .5f, 0).normalize().scale(speed);
    }
}
