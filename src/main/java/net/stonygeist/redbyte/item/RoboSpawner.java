package net.stonygeist.redbyte.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.stonygeist.redbyte.manager.RoboRegistry;
import org.jetbrains.annotations.NotNull;

public class RoboSpawner extends Item {
    public RoboSpawner(Properties properties) {
        super(properties);
    }

    @Override
    public int getDefaultMaxStackSize() {
        return 1;
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        RoboRegistry registry = RoboRegistry.get((ServerLevel) level);
        registry.newRobo((ServerLevel) level, pos);
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
