package net.stonygeist.redbyte.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.index.RedbyteConfigs;
import net.stonygeist.redbyte.manager.PseudoRobo;
import net.stonygeist.redbyte.manager.RoboRegistry;

import java.util.List;

public class DestroyBlockGoal extends Goal {
    private final RoboEntity roboEntity;
    private PseudoRobo robo;
    private BlockPos property;
    private BlockState blockState;
    private ItemStack tool;
    private float destroyProgress;
    private int lastBreakProgress = -1;

    public DestroyBlockGoal(RoboEntity roboEntity) {
        this.roboEntity = roboEntity;
    }

    @Override
    public boolean canUse() {
        if (robo == null) {
            if (roboEntity.getRedbyteID().isPresent()) {
                RoboRegistry registry = RoboRegistry.get((ServerLevel) roboEntity.level());
                robo = registry.get(roboEntity.getRedbyteID().get());
            }

            return false;
        }

        if (!roboEntity.getIsRuntime())
            return false;

        property = robo.popDestroyBlockGoalProp();
        if (property == null)
            return false;

        blockState = roboEntity.level().getBlockState(property);
        float hardness = blockState.getDestroySpeed(roboEntity.level(), property);
        if (blockState.isAir() || hardness < 0f) {
            robo.popDestroyBlockGoalProp();
            return false;
        }

        return hardness >= 0 && property.closerToCenterThan(roboEntity.position(), RedbyteConfigs.ROBO_RANGE);
    }

    @Override
    public boolean canContinueToUse() {
        return property.closerToCenterThan(roboEntity.position(), RedbyteConfigs.ROBO_RANGE) && roboEntity.level().getBlockState(property).is(blockState.getBlock());
    }

    @Override
    public void tick() {
        destroyProgress += getDestroyProgress();
        int progressStage = (int) (destroyProgress * 10f);
        if (progressStage != lastBreakProgress) {
            roboEntity.level().destroyBlockProgress(roboEntity.getId(), property, progressStage);
            lastBreakProgress = progressStage;
        }

        if (destroyProgress >= 1f) {
            List<ItemStack> drops = Block.getDrops(
                    blockState,
                    (ServerLevel) roboEntity.level(),
                    property,
                    roboEntity.level().getBlockEntity(property),
                    roboEntity,
                    tool
            );
            drops.forEach(drop -> Block.popResource(roboEntity.level(), property, drop));
            blockState.spawnAfterBreak(
                    (ServerLevel) roboEntity.level(),
                    property,
                    tool,
                    true
            );
            roboEntity.level().removeBlock(property, false);
            stop();
        }
    }

    @Override
    public void start() {
        destroyProgress = 0f;
        tool = getBestTool(blockState);
        roboEntity.setItemInHand(InteractionHand.MAIN_HAND, tool);
    }

    @Override
    public void stop() {
        roboEntity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        roboEntity.level().destroyBlockProgress(roboEntity.getId(), property, -1);
    }

    private ItemStack getBestTool(BlockState state) {
        ItemStack bestTool = ItemStack.EMPTY;
        float bestSpeed = 1f;
        ItemStack[] tools = {
                roboEntity.getPickaxe(),
                roboEntity.getAxe(),
                roboEntity.getShovel()
        };

        for (ItemStack tool : tools) {
            if (tool.isEmpty()) continue;
            if (tool.isCorrectToolForDrops(state))
                return tool;

            float speed = getSpeed(tool);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestTool = tool;
            }
        }

        return bestTool;
    }

    private float getDestroyProgress() {
        float hardness = blockState.getDestroySpeed(roboEntity.level(), property);
        if (hardness <= 0)
            return 0;

        float speed = getSpeed(tool);
        boolean correctTool = !tool.isEmpty() && tool.isCorrectToolForDrops(blockState);
        int divisor = correctTool ? 30 : 100;
        return speed / hardness / divisor;
    }

    private float getSpeed(ItemStack tool) {
        if (tool.isEmpty())
            return 1f;

        float speed = tool.getDestroySpeed(blockState);
        int efficiency = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.EFFICIENCY.getOrThrow(roboEntity), tool);
        if (efficiency > 0f && speed > 1f)
            speed += efficiency * efficiency + 1;

        if (roboEntity.isEyeInFluidType(ForgeMod.WATER_TYPE.get()))
            speed /= 7.5f;

        if (!roboEntity.onGround())
            speed /= 5.0f;

        return speed;
    }
}
