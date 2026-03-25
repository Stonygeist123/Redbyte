package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RoboDataType extends CreatureDataType<RoboEntity> {
    public static final TypeSymbol TYPE = new TypeSymbol("robo", CreatureDataType.TYPE, Component.translatable("interpreter.redbyte.types.robo"));

    public RoboDataType(RoboEntity robo) {
        super(TYPE, robo);
    }

    public static final Map<VariableSymbol, Function<EntityDataType<? extends Entity>, DataType>> properties = new Hashtable<>();
    public static final List<MethodSymbol> methods = List.of();
}
