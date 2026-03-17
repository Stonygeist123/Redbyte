package net.stonygeist.redbyte.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.manager.PseudoRobo;
import net.stonygeist.redbyte.manager.RoboRegistry;
import org.jetbrains.annotations.Nullable;


public class WalkGoal extends Goal {
    private final RoboEntity roboEntity;
    private PseudoRobo robo;
    private @Nullable Float property;
    private Vec3 targetPos;

    public WalkGoal(RoboEntity roboEntity) {
        this.roboEntity = roboEntity;
    }

    @Override
    public boolean canUse() {
        if (robo == null) {
            if (roboEntity.getRedbyteID().isPresent()) {
                RoboRegistry registry = RoboRegistry.get((ServerLevel) roboEntity.level());
                robo = registry.get(roboEntity.getRedbyteID().get());
            }
        } else {
            property = robo.popWalkGoalProp();
            if (property != null) {
                int horizontalIndex = Mth.floor((double) (robo.getEntity().getYRot() * 4.0F / 360.0F) + 0.5D) & 3;
                Direction direction = Direction.from2DDataValue(horizontalIndex);
                targetPos = roboEntity.position().relative(direction, property + 1);
                roboEntity.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, robo.getSpeed());
                Path path = roboEntity.getNavigation().getPath();
                return path != null;
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        property = robo.popWalkGoalProp();
        return property != null && targetPos != null;
    }

    @Override
    public void tick() {
        if (roboEntity.getNavigation().shouldRecomputePath(BlockPos.containing(targetPos)))
            roboEntity.getNavigation().recomputePath();
        else if (roboEntity.getNavigation().isDone()) {
            robo.addWalkGoalProp(null);
            targetPos = null;
        }
    }
}