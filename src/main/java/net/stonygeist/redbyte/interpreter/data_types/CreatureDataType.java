package net.stonygeist.redbyte.interpreter.data_types;

import com.google.common.collect.ImmutableList;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.interpreter.data_types.primitives.BooleanType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;
import org.jetbrains.annotations.NotNull;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CreatureDataType<T extends LivingEntity> extends EntityDataType<T> {
    public static final TypeSymbol TYPE = new TypeSymbol("creature", EntityDataType.TYPE, Component.translatable("interpreter.redbyte.types.creature"));

    public CreatureDataType(@NotNull TypeSymbol type, T entity) {
        super(type, entity);
    }

    public static final Map<VariableSymbol, Function<EntityDataType<? extends Entity>, DataType>> properties = new Hashtable<>(Map.of(
            new VariableSymbol("health", VectorDataType.class), x -> new NumberType(((CreatureDataType<?>) x).getEntity().getHealth())
    ));

    static {
        properties.putAll(EntityDataType.properties);
    }

    public static final List<MethodSymbol> methods = List.of(
            new MethodSymbol(
                    "follow", ImmutableList.of(), NothingDataType.class, (ev, robo, object, args) -> {
                CreatureDataType<?> entity = (CreatureDataType<?>) object;
                robo.addFollowEntityGoalProp(entity.getEntity());
                return new NothingDataType();
            }, Component.translatable("functions.redbyte.description.follow")),
            new MethodSymbol(
                    "stop_follow", ImmutableList.of(), NothingDataType.class, (ev, robo, object, args) -> {
                robo.popFollowEntityGoalProp();
                return new NothingDataType();
            }, Component.translatable("functions.redbyte.description.follow")),
            new MethodSymbol(
                    "try_attack", ImmutableList.of(), NothingDataType.class, (ev, robo, object, args) -> {
                CreatureDataType<?> entity = (CreatureDataType<?>) object;
                robo.addAttackGoalProp(entity.getEntity());
                return new NothingDataType();
            }, Component.translatable("functions.redbyte.description.follow")),
            new MethodSymbol("can_attack", ImmutableList.of(), BooleanType.class, (ev, robo, object, args) -> {
                CreatureDataType<?> entity = (CreatureDataType<?>) object;
                if (entity.getEntity() == null) return new BooleanType(false);
                RoboEntity roboEntity = robo.getEntity();
                return new BooleanType(roboEntity.canAttack(entity.getEntity()) && roboEntity.isInRange(entity.getEntity()) && roboEntity.hasLineOfSight(entity.getEntity()));
            }, Component.translatable("functions.redbyte.description.can_attack")),
            new MethodSymbol("look_at", ImmutableList.of(), NothingDataType.class,
                    (ev, robo, object, args) -> {
                        robo.getEntity().lookAt(EntityAnchorArgument.Anchor.EYES, ((CreatureDataType<?>) object).getEntity().position());
                        return new NothingDataType();
                    },
                    Component.translatable("functions.redbyte.description.look_at"))
    );
}
