package net.stonygeist.redbyte.menu.robo_inventory;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.index.RedbyteMenus;
import net.stonygeist.redbyte.manager.PseudoRobo;
import net.stonygeist.redbyte.manager.RoboRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.UUID;

public class RoboInventory extends AbstractContainerMenu {
    private final Level level;
    private final Inventory playerInventory;
    private final RoboEntity roboEntity;
    private final ItemStackHandler itemHandler;
    private final UUID redbyteID;
    private final int entityID;

    public RoboInventory(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, data.readUUID(), data.readInt());
    }

    public RoboInventory(int containerId, Inventory playerInventory, UUID redbyteID, int entityID) {
        super(RedbyteMenus.ROBO_INVENTORY.get(), containerId);
        this.redbyteID = redbyteID;
        this.entityID = entityID;
        level = playerInventory.player.level();
        this.playerInventory = playerInventory;
        addPlayerInventory();
        addPlayerHotbar();
        roboEntity = getRoboEntity();
        itemHandler = getItemHandler(roboEntity);
        addRoboInventory(itemHandler);
    }

    @Override
    public @NotNull MenuType<?> getType() {
        return RedbyteMenus.ROBO_INVENTORY.get();
    }

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int PLAYER_ALL_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int PLAYER_FIRST_SLOT_INDEX = 0;
    private static final int ROBO_INVENTORY_FIRST_SLOT_INDEX = PLAYER_FIRST_SLOT_INDEX + PLAYER_ALL_SLOT_COUNT;
    private static final int ROBO_INVENTORY_SLOT_COUNT = 9;

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot sourceSlot = slots.get(index);
        if (!sourceSlot.hasItem())
            return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();
        if (index < PLAYER_FIRST_SLOT_INDEX + PLAYER_ALL_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, ROBO_INVENTORY_FIRST_SLOT_INDEX, ROBO_INVENTORY_FIRST_SLOT_INDEX
                    + ROBO_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < ROBO_INVENTORY_FIRST_SLOT_INDEX + ROBO_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, PLAYER_FIRST_SLOT_INDEX, PLAYER_FIRST_SLOT_INDEX + PLAYER_ALL_SLOT_COUNT, false))
                return ItemStack.EMPTY;
        } else {
            Logger logger = LogUtils.getLogger();
            logger.debug("Invalid slotIndex:{}", index);
            return ItemStack.EMPTY;
        }

        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0)
            sourceSlot.set(ItemStack.EMPTY);
        else
            sourceSlot.setChanged();
        sourceSlot.onTake(player, sourceStack);
        return copyOfSourceStack;
    }

    public @Nullable RoboEntity getRoboEntity() {
        if (roboEntity != null && !roboEntity.isRemoved()) return roboEntity;
        if (redbyteID == null) return null;

        if (level instanceof ServerLevel serverLevel) {
            RoboRegistry registry = RoboRegistry.get(serverLevel);
            PseudoRobo robo = registry.get(redbyteID);
            if (robo != null)
                return robo.getEntity();
        } else {
            Entity entity = level.getEntity(entityID);
            return entity instanceof RoboEntity ? (RoboEntity) entity : null;
        }

        return null;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (!level.isClientSide() && roboEntity != null)
            roboEntity.setInventory(itemHandler);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    public ItemStackHandler getItemHandler(RoboEntity roboEntity) {
        if (itemHandler != null) return itemHandler;
        if (roboEntity == null) return new ItemStackHandler(9);
        return roboEntity.getInventory();
    }

    private void addPlayerInventory() {
        for (int row = 0; row < PLAYER_INVENTORY_ROW_COUNT; ++row)
            for (int col = 0; col < PLAYER_INVENTORY_COLUMN_COUNT; ++col)
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
    }

    private void addPlayerHotbar() {
        for (int col = 0; col < PLAYER_INVENTORY_COLUMN_COUNT; ++col)
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
    }

    private void addRoboInventory(ItemStackHandler inventory) {
        for (int col = 0; col < ROBO_INVENTORY_SLOT_COUNT; ++col)
            addSlot(new SlotItemHandler(inventory, col, 8 + col * 18, 22));
    }
}
