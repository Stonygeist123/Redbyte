package net.stonygeist.redbyte.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.stonygeist.redbyte.entity.robo.BehaviourController;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.index.RedbyteConfigs;
import net.stonygeist.redbyte.index.RedbyteEntities;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class PseudoRobo {
    private final UUID redbyteID;
    public final BehaviourController behaviourController;
    private WeakReference<RoboEntity> entityRef = new WeakReference<>(null);
    private Vec3 currentPos;
    private String code;
    public ServerLevel serverLevel;
    private Vec3 targetVelocity;
    private float speed;

    public PseudoRobo(ServerLevel serverLevel, UUID redbyteID, BlockPos currentPos, String code) {
        this.redbyteID = redbyteID;
        this.serverLevel = serverLevel;
        this.currentPos = currentPos.getCenter().subtract(0, 0.5, 0);
        this.code = code;
        targetVelocity = Vec3.ZERO;
        speed = RedbyteConfigs.ROBO_DEFAULT_SPEED;
        behaviourController = new BehaviourController(this);
    }

    public static PseudoRobo deserializeNBT(ServerLevel level, CompoundTag tag) {
        UUID redbyteID = tag.getUUID("redbyteID");
        String code = tag.getString("code");
        Vec3 pos = readVec3FromTag(tag, "pos");
        return new PseudoRobo(level, redbyteID, BlockPos.containing(pos), code);
    }

    public RoboEntity resolveEntity(ServerLevel level) {
        RoboEntity entity = getEntity();
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
        if (roboEntity != null) {
            roboEntity.syncFromVirtual(this);
            currentPos = roboEntity.position();
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("redbyteID", redbyteID);
        writeVec3ToTag(tag, "pos", currentPos);
        tag.putString("code", code);
        return tag;
    }

    public void tick(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;

        // Spawn / despawn RoboEntity if needed
        BlockPos pos = BlockPos.containing(currentPos);
        if (serverLevel.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            if (getEntity() == null)
                spawnAndRememberEntity();
        } else if (getEntity() != null)
            despawnEntity();

        updateEntity();
        if (behaviourController != null) behaviourController.tick(this);
        move(getTargetVelocity());
    }

    private void despawnEntity() {
        RoboEntity entity = getEntity();
        if (entity != null)
            entity.discard();
        entityRef.clear();
    }

    private void spawnAndRememberEntity() {
        RoboEntity robo = new RoboEntity(RedbyteEntities.ROBO.get(), serverLevel);
        robo.setRedbyteID(redbyteID);
        robo.setPos(currentPos.x, currentPos.y, currentPos.z);
        serverLevel.addFreshEntity(robo);
        setEntity(robo);
    }

    private void move(Vec3 targetVelocity) {
        currentPos = currentPos.add(targetVelocity);
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

    public float getSpeed() {
        return speed / 20f;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public Vec3 getPos() {
        return currentPos;
    }

    public void setPos(Vec3 currentPos) {
        this.currentPos = currentPos;
    }

    public Vec3 getTargetVelocity() {
        return targetVelocity;
    }

    public void setTargetVelocity(Vec3 targetVelocity) {
        this.targetVelocity = targetVelocity;
    }

    public UUID getRedbyteID() {
        return redbyteID;
    }

    public RoboEntity getEntity() {
        return entityRef.get();
    }

    public void setEntity(RoboEntity entity) {
        entityRef = new WeakReference<>(entity);
        behaviourController.setRoboRef(new WeakReference<>(entity));
    }

    public void setCode(String code) {
        this.code = code;
    }
}
