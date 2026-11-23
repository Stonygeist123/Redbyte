package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public final class PlayerDataType extends CreatureDataType<Player> {
    public static final TypeSymbol TYPE = new TypeSymbol("player", CreatureDataType.TYPE, Component.translatable("interpreter.redbyte.types.player"));

    public PlayerDataType(Player player) {
        super(TYPE, player);
    }
}
