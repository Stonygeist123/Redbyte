package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Monster;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class MonsterDataType extends CreatureDataType<Monster> {
    public static final TypeSymbol TYPE = new TypeSymbol("mob", CreatureDataType.TYPE, Component.translatable("interpreter.redbyte.types.monster"));

    public MonsterDataType(Monster mob) {
        super(TYPE, mob);
    }

    public static final Map<PropertySymbol, Function<CreatureDataType<Monster>, DataType>> properties = new Hashtable<>();
    public static final List<MethodSymbol> methods = List.of();
}
