package net.stonygeist.redbyte.interpreter.data_types;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CreatureDataType<T extends LivingEntity> extends EntityDataType<T> {
    public static final TypeSymbol TYPE = new TypeSymbol("creature", EntityDataType.TYPE, Component.translatable("interpreter.redbyte.types.creature"));

    public CreatureDataType(@NotNull TypeSymbol type, T entity) {
        super(type, entity);
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
            }, Component.translatable("functions.redbyte.description.follow"))
    );
}
