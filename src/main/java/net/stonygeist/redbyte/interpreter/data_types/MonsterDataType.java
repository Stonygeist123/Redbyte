package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Monster;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public final class MonsterDataType extends CreatureDataType<Monster> {
    public static final TypeSymbol TYPE = new TypeSymbol("mob", CreatureDataType.TYPE, Component.translatable("interpreter.redbyte.types.monster"));

    public MonsterDataType(Monster mob) {
        super(TYPE, mob);
    }
}
