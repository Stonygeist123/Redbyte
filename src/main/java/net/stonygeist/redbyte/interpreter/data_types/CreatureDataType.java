package net.stonygeist.redbyte.interpreter.data_types;

import com.google.common.collect.ImmutableList;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.interpreter.data_types.primitives.BooleanType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

public class CreatureDataType<T extends LivingEntity> extends EntityDataType<T> {
    public static final TypeSymbol TYPE = new TypeSymbol("creature", EntityDataType.TYPE, Component.translatable("interpreter.redbyte.types.creature"));

    public CreatureDataType(@NotNull TypeSymbol type, T entity) {
        super(type, entity);
    }
    
    static {
        TYPE.declareMembers(self -> {
            self.addProperty(new PropertySymbol("health", NumberType.TYPE,
                            Component.translatable("docs.redbyte.description.properties.creature.health")),
                    x -> {
                        if (x instanceof CreatureDataType<?> c)
                            return new NumberType(c.getEntity().getHealth());
                        throw new IllegalArgumentException("Expected a creature, got " + x.getClass().getSimpleName());
                    });
            self.addMethods(
                    new MethodSymbol(
                            "follow", ImmutableList.of(), NothingDataType.TYPE, (ev, robo, object, args) -> {
                        CreatureDataType<?> entity = (CreatureDataType<?>) object;
                        robo.addFollowEntityGoalProp(entity.getEntity());
                        return new NothingDataType();
                    }, Component.translatable("docs.redbyte.description.functions.creature.follow")),
                    new MethodSymbol(
                            "stop_follow", ImmutableList.of(), NothingDataType.TYPE, (ev, robo, object, args) -> {
                        robo.followGoalShouldStop = true;
                        return new NothingDataType();
                    }, Component.translatable("docs.redbyte.description.functions.creature.stop_follow")),
                    new MethodSymbol(
                            "try_attack", ImmutableList.of(), NothingDataType.TYPE, (ev, robo, object, args) -> {
                        CreatureDataType<?> entity = (CreatureDataType<?>) object;
                        robo.addAttackGoalProp(entity.getEntity());
                        return new NothingDataType();
                    }, Component.translatable("docs.redbyte.description.functions.creature.try_attack")),
                    new MethodSymbol("can_attack", ImmutableList.of(), BooleanType.TYPE, (ev, robo, object, args) -> {
                        CreatureDataType<?> entity = (CreatureDataType<?>) object;
                        if (entity.getEntity() == null) return new BooleanType(false);
                        RoboEntity roboEntity = robo.getEntity();
                        return new BooleanType(roboEntity.canAttack(entity.getEntity()) && roboEntity.isInRange(entity.getEntity()) && roboEntity.hasLineOfSight(entity.getEntity()));
                    }, Component.translatable("docs.redbyte.description.functions.creature.can_attack")),
                    new MethodSymbol("look_at", ImmutableList.of(), NothingDataType.TYPE,
                            (ev, robo, object, args) -> {
                                robo.getEntity().lookAt(EntityAnchorArgument.Anchor.EYES, ((CreatureDataType<?>) object).getEntity().position());
                                return new NothingDataType();
                            },
                            Component.translatable("docs.redbyte.description.functions.creature.look_at")));
        });
    }
}
