package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.joml.Vector3f;

public class VectorDataType extends DataType {
    public static final TypeSymbol TYPE = new TypeSymbol("vector", Component.translatable("interpreter.redbyte.types.vector"));
    private final Vector3f vector;

    public VectorDataType(Vec3 vector) {
        super(TYPE);
        this.vector = vector.toVector3f();
    }

    public Vector3f getVector() {
        return vector;
    }
}
