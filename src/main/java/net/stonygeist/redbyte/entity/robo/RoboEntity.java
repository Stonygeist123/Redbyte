package net.stonygeist.redbyte.entity.robo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.stonygeist.redbyte.goals.*;
import net.stonygeist.redbyte.index.RedbyteConfigs;
import net.stonygeist.redbyte.interpreter.Evaluator;
import net.stonygeist.redbyte.interpreter.diagnostics.Diagnostic;
import net.stonygeist.redbyte.interpreter.diagnostics.DiagnosticBag;
import net.stonygeist.redbyte.manager.RoboRegistry;
import net.stonygeist.redbyte.menu.robo_terminal.RoboTerminal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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
    private static final EntityDataAccessor<CompoundTag> runtimeError
            = SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<CompoundTag> printOutput
            = SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Boolean> isRuntime
            = SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<CompoundTag> inventory
            = SynchedEntityData.defineId(RoboEntity.class, EntityDataSerializers.COMPOUND_TAG);

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
        goalSelector.addGoal(3, new DestroyBlockGoal(this));
        goalSelector.addGoal(3, new AttackGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createLivingAttributes()
                .add(Attributes.MAX_HEALTH, RedbyteConfigs.ROBO_DEFAULT_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, RedbyteConfigs.ROBO_DEFAULT_SPEED)
                .add(Attributes.ATTACK_DAMAGE, 1f)
                .add(Attributes.FOLLOW_RANGE, 10f)
                .add(Attributes.KNOCKBACK_RESISTANCE, .6f)
                .add(Attributes.ARMOR, 12f)
                .add(Attributes.ARMOR_TOUGHNESS, 6f)
                .add(Attributes.SCALE, 2.25f)
                .add(Attributes.STEP_HEIGHT, 1f)
                .add(Attributes.BLOCK_BREAK_SPEED, 1f);
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
                            Component.translatable("menu.redbyte.robo_terminal.title")
                    ),
                    (buffer) -> buffer.writeUUID(getRedbyteID().get())
            );
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.sidedSuccess(level().isClientSide());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        if (getRedbyteID().isPresent())
            return new RoboTerminal(containerId, inventory, getRedbyteID().get());
        return null;
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
        builder.define(runtimeError, new CompoundTag());
        builder.define(printOutput, new CompoundTag());
        builder.define(isRuntime, false);
        CompoundTag inventoryTag = new CompoundTag();
        ListTag listTag = new ListTag();
        ItemStackHandler itemHandler = new ItemStackHandler(RedbyteConfigs.ROBO_DEFAULT_INVENTORY_SLOTS + RedbyteConfigs.ROBO_TOOL_SLOTS);
        for (int i = 0; i < itemHandler.getSlots(); ++i)
            listTag.add(new CompoundTag());
        inventoryTag.put("slots", listTag);
        builder.define(inventory, inventoryTag);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (redbyteID != null && getRedbyteID().isPresent()) tag.putUUID("redbyteID", getRedbyteID().get());
        if (code != null) tag.putString("code", getCode());
        if (buildDone != null) tag.putBoolean("buildDone", getBuildDone());
        if (diagnostics != null) tag.put("diagnostics", getDiagnosticsTag());
        if (inventory != null) tag.put("inventory", getInventoryTag());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("redbyteID")) setRedbyteID(tag.getUUID("redbyteID"));
        if (tag.contains("code")) setCode(tag.getString("code"));
        if (tag.contains("buildDone")) setBuildDone(tag.getBoolean("buildDone"));
        if (tag.contains("diagnostics")) entityData.set(diagnostics, tag.getCompound("diagnostics"));
        if (tag.contains("inventory")) entityData.set(inventory, tag.getCompound("inventory"));
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

        ItemStackHandler inventory = getInventory();
        setInventory(inventory);
        setBuildDone(false);
    }

    @Override
    public void die(@NotNull DamageSource source) {
        if (!level().isClientSide)
            dropInventory();
        super.die(source);
    }

    private void dropInventory() {
        ItemStackHandler inventory = getInventory();
        for (int i = 0; i < inventory.getSlots(); ++i) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                spawnAtLocation(itemStack);
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                setInventory(inventory);
            }
        }
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        for (int i = 0; i < getInventory().getSlots(); i++) {
            itemStack = insertItem(i, itemStack, false);
            if (itemStack.isEmpty())
                break;
        }

        if (itemStack.isEmpty())
            itemEntity.discard();
        else
            itemEntity.setItem(itemStack);
    }

    @Override
    public boolean canPickUpLoot() {
        return true;
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        ItemStackHandler inventory = getInventory();
        ItemStack result = inventory.insertItem(slot, stack, simulate);
        setInventory(inventory);
        return result;
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStackHandler inventory = getInventory();
        ItemStack result = inventory.extractItem(slot, amount, simulate);
        setInventory(inventory);
        return result;
    }

    public ItemStack getSword() {
        ItemStackHandler inventory = getInventory();
        return inventory.getStackInSlot(inventory.getSlots() - 4);
    }

    public ItemStack getPickaxe() {
        ItemStackHandler inventory = getInventory();
        return inventory.getStackInSlot(inventory.getSlots() - 3);
    }

    public ItemStack getAxe() {
        ItemStackHandler inventory = getInventory();
        return inventory.getStackInSlot(inventory.getSlots() - 2);
    }

    public ItemStack getShovel() {
        ItemStackHandler inventory = getInventory();
        return inventory.getStackInSlot(inventory.getSlots() - 1);
    }

    public boolean isInRange(@NotNull LivingEntity target) {
        return distanceTo(target) <= (RedbyteConfigs.ROBO_RANGE * 2);
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

    public ListTag getDiagnosticsTag() {
        return (ListTag) entityData.get(diagnostics).get("diagnostics");
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

    public @Nullable Diagnostic getRuntimeError() {
        if (entityData.get(runtimeError).isEmpty())
            return null;
        return Diagnostic.deserializeNBT(entityData.get(runtimeError), 0);
    }

    public void setRuntimeError(@Nullable Evaluator.EvaluationError runtimeError) {
        entityData.set(RoboEntity.runtimeError, runtimeError == null ? new CompoundTag() : runtimeError.getDiagnostic().serializeNBT(0));
    }

    public @NotNull List<String> getPrintOutput() {
        ListTag listTag = entityData.get(printOutput).getList("print_output", Tag.TAG_STRING);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < listTag.size(); ++i) {
            String output = listTag.getString(i);
            result.add(output);
        }

        return result;
    }

    public void addPrintOutput(Object printOutput) {
        CompoundTag tag = new CompoundTag();
        List<String> output = getPrintOutput();
        output.add(String.valueOf(printOutput));
        ListTag listTag = new ListTag();
        for (String o : output)
            listTag.add(StringTag.valueOf(o));

        tag.put("print_output", listTag);
        entityData.set(RoboEntity.printOutput, tag);
    }

    public void clearPrint() {
        entityData.set(printOutput, new CompoundTag());
    }

    public boolean getIsRuntime() {
        return entityData.get(isRuntime);
    }

    public void setIsRuntime(boolean isRuntime) {
        entityData.set(RoboEntity.isRuntime, isRuntime);
    }

    public CompoundTag getInventoryTag() {
        return entityData.get(inventory);
    }

    public @NotNull ItemStackHandler getInventory() {
        CompoundTag tag = getInventoryTag();
        if (tag == null || !tag.contains("slots"))
            return new ItemStackHandler(RedbyteConfigs.ROBO_DEFAULT_INVENTORY_SLOTS + RedbyteConfigs.ROBO_TOOL_SLOTS);

        ListTag listTag = tag.getList("slots", Tag.TAG_COMPOUND);
        ItemStackHandler inventory = new ItemStackHandler(listTag.size());
        for (int i = 0; i < listTag.size(); ++i) {
            Optional<ItemStack> itemStack;
            if (listTag.get(i) instanceof CompoundTag ct && ct.isEmpty())
                itemStack = Optional.of(ItemStack.EMPTY);
            else
                itemStack = ItemStack.parse(registryAccess(), listTag.get(i));
            inventory.setStackInSlot(i, itemStack.orElse(ItemStack.EMPTY));
        }

        return inventory;
    }

    public void setInventory(@NotNull ItemStackHandler inventory) {
        CompoundTag tag = getInventoryTag();
        ListTag listTag = new ListTag();
        for (int i = 0; i < inventory.getSlots(); ++i) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (itemStack.isEmpty())
                listTag.add(new CompoundTag());
            else
                listTag.add(itemStack.save(registryAccess()));
        }

        tag.put("slots", listTag);
        entityData.set(RoboEntity.inventory, tag);
    }
}
