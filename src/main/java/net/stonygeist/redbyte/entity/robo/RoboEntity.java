package net.stonygeist.redbyte.entity.robo;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.stonygeist.redbyte.screen.robo_terminal.RoboTerminal;
import org.jetbrains.annotations.NotNull;

public class RoboEntity extends PathfinderMob {
    public RoboEntity(EntityType<? extends RoboEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new RandomLookAroundGoal(this));
        goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6f));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 40f)
                .add(Attributes.MOVEMENT_SPEED, 1f)
                .add(Attributes.ATTACK_DAMAGE, 5f)
                .add(Attributes.FOLLOW_RANGE, 10f)
                .add(Attributes.KNOCKBACK_RESISTANCE, .6f)
                .add(Attributes.ARMOR, 12f)
                .add(Attributes.ARMOR_TOUGHNESS, 6f)
                .add(Attributes.SCALE, 2.25f);
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player pPlayer, @NotNull InteractionHand pHand) {
        Minecraft.getInstance().tell(() ->
                Minecraft.getInstance().setScreen(new RoboTerminal()));
        return InteractionResult.SUCCESS;
    }
}
