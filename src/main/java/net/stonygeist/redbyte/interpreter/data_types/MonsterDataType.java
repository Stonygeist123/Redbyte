package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;

import java.util.Hashtable;
import java.util.Map;
import java.util.function.Function;

public final class MonsterDataType extends CreatureDataType<Monster> {
    public static final TypeSymbol TYPE = new TypeSymbol("mob", CreatureDataType.TYPE, Component.translatable("interpreter.redbyte.types.monster"));

    public MonsterDataType(Monster mob) {
        super(TYPE, mob);
    }

    public static final Map<VariableSymbol, Function<EntityDataType<? extends Entity>, DataType>> properties = new Hashtable<>();
}
