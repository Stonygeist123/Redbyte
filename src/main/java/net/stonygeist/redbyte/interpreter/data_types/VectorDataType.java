package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.world.phys.Vec3;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

public class VectorDataType extends DataType {
    public static final TypeSymbol TYPE = new TypeSymbol("vector");
    private final Vec3 vector;

    public VectorDataType(Vec3 vector) {
        super(TYPE);
        this.vector = vector;
    }

    public Vec3 getVector() {
        return vector;
    }
}
