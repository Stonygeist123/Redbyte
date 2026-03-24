package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;
import org.joml.Vector3f;

import java.util.Hashtable;
import java.util.Map;
import java.util.function.Function;

public class VectorDataType extends DataType {
    public static final TypeSymbol TYPE = new TypeSymbol("vector", Component.translatable("interpreter.redbyte.types.vector"));
    private final Vector3f vector;

    public VectorDataType(float x, float y, float z) {
        this(new Vec3(x, y, z));
    }

    public VectorDataType(Vec3 vector) {
        super(TYPE);
        this.vector = vector.toVector3f();
    }

    public Vector3f getVector() {
        return vector;
    }

    public static final Map<VariableSymbol, Function<VectorDataType, ? extends DataType>> properties = new Hashtable<>(Map.of(
            new VariableSymbol("x", NumberType.class), x -> new NumberType(x.getVector().x),
            new VariableSymbol("y", NumberType.class), x -> new NumberType(x.getVector().y),
            new VariableSymbol("z", NumberType.class), x -> new NumberType(x.getVector().z)
    ));
}
