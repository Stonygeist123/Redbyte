package net.stonygeist.redbyte.entity.robo;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.stonygeist.redbyte.goals.FollowPlayerGoal;
import net.stonygeist.redbyte.goals.WalkGoal;
import net.stonygeist.redbyte.goals.WalkToGoal;
import net.stonygeist.redbyte.index.RedbyteConfigs;
import net.stonygeist.redbyte.manager.RoboRegistry;
import net.stonygeist.redbyte.screen.robo_terminal.RoboTerminalScreen;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RoboEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> redbyteID =
            SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> code =
            SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.STRING);

    public RoboEntity(EntityType<? extends RoboEntity> type, Level level) {
        super(type, level);
        noPhysics = false;
        noCulling = false;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new FollowPlayerGoal(this));
        goalSelector.addGoal(2, new WalkGoal(this));
        goalSelector.addGoal(3, new WalkToGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createLivingAttributes()
                .add(Attributes.MAX_HEALTH, RedbyteConfigs.ROBO_DEFAULT_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, RedbyteConfigs.ROBO_DEFAULT_SPEED)
                .add(Attributes.ATTACK_DAMAGE, 5f)
                .add(Attributes.FOLLOW_RANGE, 10f)
                .add(Attributes.KNOCKBACK_RESISTANCE, .6f)
                .add(Attributes.ARMOR, 12f)
                .add(Attributes.ARMOR_TOUGHNESS, 6f)
                .add(Attributes.SCALE, 2.25f)
                .add(Attributes.STEP_HEIGHT, 1f);
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (level().isClientSide()) {
            RoboTerminalScreen screen = new RoboTerminalScreen(this);
            screen.setId(getRedbyteID());
            screen.setCode(getCode());
            Minecraft.getInstance().setScreen(screen);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void kill() {
        super.kill();
        if (redbyteID != null && level() instanceof ServerLevel serverLevel)
            RoboRegistry.get(serverLevel).remove(getRedbyteID());
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        super.remove(reason);
        if (reason.shouldDestroy() && level() instanceof ServerLevel serverLevel)
            RoboRegistry.get(serverLevel).remove(getRedbyteID());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(code, "");
        builder.define(redbyteID, "");
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (redbyteID != null) tag.putUUID("redbyteID", getRedbyteID());
        if (code != null) tag.putString("code", getCode());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("redbyteID")) setRedbyteID(tag.getUUID("redbyteID"));
        if (tag.contains("code")) setCode(tag.getString("code"));
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (level() instanceof ServerLevel serverLevel) {
            if (getRedbyteID() == null)
                setRedbyteID(UUID.randomUUID());

            RoboRegistry registry = RoboRegistry.get(serverLevel);
            registry.ensureExists(getRedbyteID(), this);
        }
    }

    public String getCode() {
        return entityData.get(code);
    }

    public void setCode(String code) {
        entityData.set(RoboEntity.code, code);
    }

    public UUID getRedbyteID() {
        String raw = entityData.get(redbyteID);
        return raw.isEmpty() ? null : UUID.fromString(raw);
    }

    public void setRedbyteID(UUID id) {
        entityData.set(redbyteID, id.toString());
    }
}
