package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class PlayerDataType extends CreatureDataType<Player> {
    public static final TypeSymbol TYPE = new TypeSymbol("player", CreatureDataType.TYPE, Component.translatable("interpreter.redbyte.types.player"));

    public PlayerDataType(Player player) {
        super(TYPE, player);
    }

    public static final Map<PropertySymbol, Function<EntityDataType<? extends Entity>, DataType>> properties = new Hashtable<>();
    public static final List<MethodSymbol> methods = List.of();
}
