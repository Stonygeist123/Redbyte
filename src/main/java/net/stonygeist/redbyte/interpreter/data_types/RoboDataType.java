package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public class RoboDataType extends CreatureDataType<RoboEntity> {
    public static final TypeSymbol TYPE = new TypeSymbol("robo", CreatureDataType.TYPE, Component.translatable("interpreter.redbyte.types.robo"));

    public RoboDataType(RoboEntity robo) {
        super(TYPE, robo);
    }
}
