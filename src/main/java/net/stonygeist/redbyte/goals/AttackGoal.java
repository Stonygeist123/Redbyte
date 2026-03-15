package net.stonygeist.redbyte.goals;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.manager.PseudoRobo;
import net.stonygeist.redbyte.manager.RoboRegistry;

public class AttackGoal extends Goal {
    private final RoboEntity roboEntity;
    private PseudoRobo robo;
    private LivingEntity property;
    private int cooldown = 0;

    public AttackGoal(RoboEntity roboEntity) {
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

        property = robo.popAttackGoalProp();
        return property != null && property.isAlive() && roboEntity.hasLineOfSight(property) && roboEntity.isInRange(property);
    }

    @Override
    public boolean canContinueToUse() {
        return property.isAlive() && property.attackable() && roboEntity.isInRange(property);
    }

    @Override
    public void tick() {
        if (cooldown > 0) {
            --cooldown;
            return;
        }

        ServerLevel level = (ServerLevel) roboEntity.level();
        float damage = (float) roboEntity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        ItemStack sword = roboEntity.getSword();
        ItemAttributeModifiers modifiers = sword.getDefaultAttributeModifiers();
        DamageSource damageSource = roboEntity.damageSources().mobAttack(roboEntity);
        damage = (float) modifiers.compute(damage, EquipmentSlot.MAINHAND);
        damage += EnchantmentHelper.modifyDamage(level, sword, roboEntity, damageSource, damage);

        roboEntity.swing(InteractionHand.MAIN_HAND);
        boolean hurt = property.hurt(damageSource, damage);
        if (hurt) {
            float knockback = EnchantmentHelper.modifyKnockback(level, sword, roboEntity, damageSource, 1);
            if (knockback > 0) {
                property.knockback(knockback * .5f,
                        Mth.sin(roboEntity.getYRot() * ((float) Math.PI / 180f)),
                        -Mth.cos(roboEntity.getYRot() * ((float) Math.PI / 180f)));
            }
            int fireAspectLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT.getOrThrow(roboEntity), sword);
            if (fireAspectLevel > 0)
                property.setRemainingFireTicks(80 * fireAspectLevel);

            EnchantmentHelper.doPostAttackEffects(level, property, damageSource);
        }

        cooldown = 10;
        stop();
    }

    @Override
    public void start() {
        roboEntity.setItemInHand(InteractionHand.MAIN_HAND, roboEntity.getSword());
    }

    @Override
    public void stop() {
        roboEntity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }
}
