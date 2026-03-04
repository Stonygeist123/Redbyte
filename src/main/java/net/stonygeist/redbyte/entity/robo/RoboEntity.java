package net.stonygeist.redbyte.entity.robo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.stonygeist.redbyte.goals.FollowPlayerGoal;
import net.stonygeist.redbyte.goals.WalkGoal;
import net.stonygeist.redbyte.goals.WalkToGoal;
import net.stonygeist.redbyte.index.RedbyteConfigs;
import net.stonygeist.redbyte.interpreter.diagnostics.DiagnosticBag;
import net.stonygeist.redbyte.manager.RoboRegistry;
import net.stonygeist.redbyte.menu.robo_terminal.RoboTerminal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class RoboEntity extends PathfinderMob implements MenuProvider {
    private static final EntityDataAccessor<Optional<UUID>> redbyteID =
            SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> code =
            SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> buildDone =
            SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<CompoundTag> diagnostics =
            SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.COMPOUND_TAG);

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
        if (!level().isClientSide() && player instanceof ServerPlayer serverPlayer && getRedbyteID().isPresent()) {
            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            this,
                            Component.translatable("screen.redbyte.robo_terminal.title")
                    ),
                    (buffer) -> buffer.writeUUID(getRedbyteID().get())
            );
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(level().isClientSide());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new RoboTerminal(containerId, inventory);
    }

    @Override
    public void kill() {
        super.kill();
        if (redbyteID != null && level() instanceof ServerLevel serverLevel)
            RoboRegistry.get(serverLevel).remove(getRedbyteID().orElse(null));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(code, "");
        builder.define(redbyteID, Optional.empty());
        builder.define(buildDone, false);
        builder.define(diagnostics, new DiagnosticBag().serializeNBT());
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (redbyteID != null && getRedbyteID().isPresent()) tag.putUUID("redbyteID", getRedbyteID().get());
        if (code != null) tag.putString("code", getCode());
        if (buildDone != null) tag.putBoolean("buildDone", getBuildDone());
        if (diagnostics != null) tag.put("errors", getDiagnosticsTag());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("redbyteID")) setRedbyteID(tag.getUUID("redbyteID"));
        if (tag.contains("code")) setCode(tag.getString("code"));
        if (tag.contains("buildDone")) setBuildDone(tag.getBoolean("buildDone"));
        if (tag.contains("diagnostics")) entityData.set(diagnostics, tag.getCompound("diagnostics"));
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (level() instanceof ServerLevel serverLevel) {
            if (getRedbyteID().isEmpty())
                setRedbyteID(UUID.randomUUID());

            RoboRegistry registry = RoboRegistry.get(serverLevel);
            registry.ensureExists(getRedbyteID().get(), this);
        }

        setBuildDone(false);
    }

    public boolean isInRange(@NotNull LivingEntity target) {
        return distanceToSqr(target.position()) <= RedbyteConfigs.ROBO_RANGE;
    }

    @NotNull
    public Optional<UUID> getRedbyteID() {
        return entityData.get(redbyteID);
    }

    public void setRedbyteID(UUID id) {
        entityData.set(redbyteID, Optional.of(id));
    }

    public String getCode() {
        return entityData.get(code);
    }

    public void setCode(String code) {
        entityData.set(RoboEntity.code, code);
    }

    public DiagnosticBag getDiagnostics() {
        return DiagnosticBag.deserializeNBT(entityData.get(diagnostics));
    }

    public CompoundTag getDiagnosticsTag() {
        return entityData.get(diagnostics);
    }

    public void setDiagnostics(DiagnosticBag diagnostics) {
        entityData.set(RoboEntity.diagnostics, diagnostics.serializeNBT());
    }

    public boolean getBuildDone() {
        return entityData.get(buildDone);
    }

    public void setBuildDone(boolean buildDone) {
        entityData.set(RoboEntity.buildDone, buildDone);
    }
}
