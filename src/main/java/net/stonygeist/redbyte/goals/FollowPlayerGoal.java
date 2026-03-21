package net.stonygeist.redbyte.goals;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.manager.PseudoRobo;
import net.stonygeist.redbyte.manager.RoboRegistry;

import java.util.Stack;

public class FollowPlayerGoal extends Goal {
    private final RoboEntity roboEntity;
    private PseudoRobo robo;
    private Stack<LivingEntity> property;
    private LivingEntity target;

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
        }

        if (!roboEntity.getIsRuntime())
            return false;

        property = robo.getFollowPlayerGoalProp();
        if (property.isEmpty()) return false;
        target = property.peek();
        return target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return !property.isEmpty() && target.equals(property.peek()) && target.isAlive();
    }

    @Override
    public void tick() {
        roboEntity.getNavigation().moveTo(target, robo.getSpeed());
    }

    @Override
    public void stop() {
        roboEntity.getNavigation().stop();
    }
}