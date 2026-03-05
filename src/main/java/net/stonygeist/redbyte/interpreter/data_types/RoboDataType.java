package net.stonygeist.redbyte.interpreter.data_types;

import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

public class RoboDataType extends CreatureDataType<RoboEntity> {
    public static final TypeSymbol TYPE = new TypeSymbol("robo", CreatureDataType.TYPE);

    public RoboDataType(RoboEntity robo) {
        super(TYPE, robo);
    }

    @Override
    public @NotNull TypeSymbol getType() {
        return new TypeSymbol("robo");
    }
}
