package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.world.entity.player.Player;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public final class PlayerDataType extends EntityDataType<Player> {
    public static final TypeSymbol TYPE = new TypeSymbol("player", EntityDataType.TYPE);

    public PlayerDataType(Player player) {
        super(TYPE, player);
    }
}
