package net.stonygeist.redbyte.menu.robo_docs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.stonygeist.redbyte.index.RedbyteMenus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RoboDocs extends AbstractContainerMenu {
    private final UUID redbyteID;

    public RoboDocs(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, data.readUUID());
    }

    public RoboDocs(int containerId, Inventory inventory, UUID redbyteID) {
        super(RedbyteMenus.ROBO_TERMINAL.get(), containerId);
        this.redbyteID = redbyteID;
    }

    @Override
    public @NotNull MenuType<?> getType() {
        return RedbyteMenus.ROBO_DOCS.get();
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

    public UUID getRedbyteID() {
        return redbyteID;
    }
}
