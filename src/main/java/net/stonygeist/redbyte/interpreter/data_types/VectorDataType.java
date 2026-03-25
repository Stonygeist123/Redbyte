package net.stonygeist.redbyte.interpreter.data_types;

import com.google.common.collect.ImmutableList;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class VectorDataType extends DataType {
    public static final TypeSymbol TYPE = new TypeSymbol("vector", Component.translatable("interpreter.redbyte.types.vector"));
    private final Vec3 vector;

    public VectorDataType(float x, float y, float z) {
        this(new Vec3(x, y, z));
    }

    public VectorDataType(Vec3 vector) {
        super(TYPE);
        this.vector = vector;
    }

    public Vec3 getVector() {
        return vector;
    }

    public static final Map<VariableSymbol, Function<VectorDataType, ? extends DataType>> properties = new Hashtable<>(Map.of(
            new VariableSymbol("x", NumberType.class), x -> new NumberType((float) x.getVector().x),
            new VariableSymbol("y", NumberType.class), x -> new NumberType((float) x.getVector().y),
            new VariableSymbol("z", NumberType.class), x -> new NumberType((float) x.getVector().z)
    ));
    public static final List<MethodSymbol> methods = List.of(
            new MethodSymbol("distance", ImmutableList.of(NumberType.class), NumberType.class,
                    (ev, robo, object, args) -> new NumberType((float) ((VectorDataType) object).getVector().distanceTo(((VectorDataType) args[0]).getVector())),
                    Component.translatable("functions.redbyte.description.distance")),
            new MethodSymbol("look_at", ImmutableList.of(), NothingDataType.class,
                    (ev, robo, object, args) -> {
                        robo.getEntity().lookAt(EntityAnchorArgument.Anchor.EYES, ((VectorDataType) object).getVector());
                        return new NothingDataType();
                    },
                    Component.translatable("functions.redbyte.description.look_at"))
    );
}
