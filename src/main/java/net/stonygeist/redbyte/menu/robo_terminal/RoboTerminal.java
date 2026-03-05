package net.stonygeist.redbyte.menu.robo_terminal;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.index.RedbyteMenus;
import net.stonygeist.redbyte.menu.robo_terminal.screen.TerminalText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class RoboTerminal extends AbstractContainerMenu {
    private final Level level;
    private final Inventory inventory;
    private IItemHandler itemHandler;
    private UUID redbyteID;
    private TerminalText terminalText;
    private RoboEntity roboEntity;

    public RoboTerminal(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory);
        redbyteID = data.readUUID();
    }

    public RoboTerminal(int containerId, Inventory inventory) {
        super(RedbyteMenus.ROBO_TERMINAL.get(), containerId);
        level = inventory.player.level();
        this.inventory = inventory;
    }

    @Override
    public @NotNull MenuType<?> getType() {
        return RedbyteMenus.ROBO_TERMINAL.get();
    }

    public @Nullable RoboEntity getRoboEntity() {
        if (roboEntity != null && !roboEntity.isRemoved()) return roboEntity;
        if (redbyteID == null) return null;

        AABB box = new AABB(inventory.player.blockPosition()).inflate(64.0);
        List<RoboEntity> robos = level.getEntitiesOfClass(
                RoboEntity.class,
                box,
                r -> redbyteID.equals(r.getRedbyteID().orElse(null))
        );
        if (robos.isEmpty())
            return null;

        roboEntity = robos.getFirst();
        if (terminalText == null) terminalText = new TerminalText(roboEntity.getCode());
        return roboEntity;
    }

    public TerminalText getTerminalText() {
        return terminalText;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    // TODO: Fix this
    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    public IItemHandler getItemHandler() {
        if (itemHandler == null)
            itemHandler = roboEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
                    .orElseThrow(() -> new IllegalStateException("No inventory."));
        return itemHandler;
    }
}
