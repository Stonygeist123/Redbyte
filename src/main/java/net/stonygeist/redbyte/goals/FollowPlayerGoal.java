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
    private ServerPlayer target;

    public FollowPlayerGoal(RoboEntity roboEntity) {
        this.roboEntity = roboEntity;
    }

    @Override
    public boolean canUse() {
        if (robo == null) {
            RoboRegistry registry = RoboRegistry.get((ServerLevel) roboEntity.level());
            robo = registry.get(roboEntity.getRedbyteID());
            return false;
        } else {
            target = robo.behaviourController.getPlayerTarget();
            return target != null && target.isAlive();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive();
    }

    @Override
    public void tick() {
        if (target != null) {
            roboEntity.getNavigation().moveTo(target, robo.getSpeed());
            if (roboEntity.getNavigation().isStuck())
                roboEntity.getNavigation().recomputePath();
        }
    }

    @Override
    public void stop() {
        roboEntity.getNavigation().stop();
    }
}