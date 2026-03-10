package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

public class CreatureDataType<T extends LivingEntity> extends EntityDataType<T> {
    public static final TypeSymbol TYPE = new TypeSymbol("creature", EntityDataType.TYPE, Component.translatable("interpreter.redbyte.types.creature"));

    public CreatureDataType(@NotNull TypeSymbol type, T entity) {
        super(type, entity);
    }
}
