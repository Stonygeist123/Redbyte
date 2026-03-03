package net.stonygeist.redbyte.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.manager.PseudoRobo;
import net.stonygeist.redbyte.manager.RoboRegistry;

public class WalkToGoal extends Goal {
    private final RoboEntity roboEntity;
    private PseudoRobo robo;
    private Vec3 property;

    public WalkToGoal(RoboEntity roboEntity) {
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
        } else {
            property = robo.getWalkToGoalProp();
            if (property == null)
                return false;

            roboEntity.getNavigation().moveTo(property.x, property.y, property.z, robo.getSpeed());
            return roboEntity.getNavigation().getPath() != null;
        }
    }

    @Override
    public boolean canContinueToUse() {
        property = robo.getWalkToGoalProp();
        return property != null;
    }

    @Override
    public void tick() {
        if (roboEntity.getNavigation().shouldRecomputePath(BlockPos.containing(property)))
            roboEntity.getNavigation().recomputePath();
        else if (roboEntity.getNavigation().isDone())
            robo.setWalkToGoalProp(null);
    }
}