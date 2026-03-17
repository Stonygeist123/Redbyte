package net.stonygeist.redbyte.menu.robo_inventory;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.index.RedbyteConfigs;
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
    private SwordItem sword;
    private PickaxeItem pickaxe;
    private AxeItem axe;
    private ShovelItem shovel;

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
    private static final int ROBO_INVENTORY_FIRST_SLOT_INDEX = PLAYER_ALL_SLOT_COUNT;
    private static final int ROBO_ALL_SLOT_COUNT = RedbyteConfigs.ROBO_DEFAULT_INVENTORY_SLOTS + RedbyteConfigs.ROBO_TOOL_SLOTS;

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot sourceSlot = slots.get(index);
        if (!sourceSlot.hasItem())
            return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();
        if (index < PLAYER_ALL_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, ROBO_INVENTORY_FIRST_SLOT_INDEX, ROBO_INVENTORY_FIRST_SLOT_INDEX
                    + RedbyteConfigs.ROBO_DEFAULT_INVENTORY_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < ROBO_INVENTORY_FIRST_SLOT_INDEX + RedbyteConfigs.ROBO_DEFAULT_INVENTORY_SLOTS) {
            if (!moveItemStackTo(sourceStack, 0, PLAYER_ALL_SLOT_COUNT, false))
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
        if (roboEntity == null)
            return new ItemStackHandler(ROBO_ALL_SLOT_COUNT);
        return roboEntity.getInventory();
    }

    private void addPlayerInventory() {
        for (int row = 0; row < PLAYER_INVENTORY_ROW_COUNT; ++row)
            for (int col = 0; col < PLAYER_INVENTORY_COLUMN_COUNT; ++col)
                addSlot(new Slot(playerInventory, col + row * PLAYER_INVENTORY_COLUMN_COUNT + 9, 8 + col * 18, 84 + row * 18));
    }

    private void addPlayerHotbar() {
        for (int col = 0; col < PLAYER_INVENTORY_COLUMN_COUNT; ++col)
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
    }

    private final ResourceLocation EMPTY_SWORD_ICON = Redbyte.asResource("item/empty_sword");
    private final ResourceLocation EMPTY_PICKAXE_ICON = Redbyte.asResource("item/empty_pickaxe");
    private final ResourceLocation EMPTY_AXE_ICON = Redbyte.asResource("item/empty_axe");
    private final ResourceLocation EMPTY_SHOVEL_ICON = Redbyte.asResource("item/empty_shovel");

    private void addRoboInventory(ItemStackHandler inventory) {
        for (int col = 0; col < RedbyteConfigs.ROBO_DEFAULT_INVENTORY_SLOTS; ++col)
            addSlot(new SlotItemHandler(inventory, col, 8 + col * 18, 22));

        addSlot(new ToolSlot(inventory, RedbyteConfigs.ROBO_DEFAULT_INVENTORY_SLOTS, 98, 48, SwordItem.class, EMPTY_SWORD_ICON, this::setSword));
        addSlot(new ToolSlot(inventory, RedbyteConfigs.ROBO_DEFAULT_INVENTORY_SLOTS + 1, 98 + 18, 48, PickaxeItem.class, EMPTY_PICKAXE_ICON, this::setPickaxe));
        addSlot(new ToolSlot(inventory, RedbyteConfigs.ROBO_DEFAULT_INVENTORY_SLOTS + 2, 98 + 2 * 18, 48, AxeItem.class, EMPTY_AXE_ICON, this::setAxe));
        addSlot(new ToolSlot(inventory, RedbyteConfigs.ROBO_DEFAULT_INVENTORY_SLOTS + 3, 98 + 3 * 18, 48, SwordItem.class, EMPTY_SHOVEL_ICON, this::setShovel));
    }

    public SwordItem getSword() {
        return sword;
    }

    public void setSword(Item item) {
        if (item instanceof SwordItem sword)
            this.sword = sword;
    }

    public PickaxeItem getPickaxe() {
        return pickaxe;
    }

    public void setPickaxe(Item item) {
        if (item instanceof PickaxeItem pickaxe)
            this.pickaxe = pickaxe;
    }

    public AxeItem getAxe() {
        return axe;
    }

    public void setAxe(Item item) {
        if (item instanceof AxeItem axe)
            this.axe = axe;
    }

    public ShovelItem getShovel() {
        return shovel;
    }

    public void setShovel(Item item) {
        if (item instanceof ShovelItem shovel)
            this.shovel = shovel;
    }
}
