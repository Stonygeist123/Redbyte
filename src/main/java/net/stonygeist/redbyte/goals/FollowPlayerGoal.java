package net.stonygeist.redbyte.goals;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.manager.PseudoRobo;
import net.stonygeist.redbyte.manager.RoboRegistry;

public class FollowPlayerGoal extends Goal {
    private final RoboEntity roboEntity;
    private PseudoRobo robo;
    private ServerPlayer property;

    public FollowPlayerGoal(RoboEntity roboEntity) {
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
            property = robo.getFollowPlayerGoalProp();
            return property != null && property.isAlive();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        roboEntity.getNavigation().moveTo(property, robo.getSpeed());
    }

    @Override
    public void stop() {
        roboEntity.getNavigation().stop();
    }
}