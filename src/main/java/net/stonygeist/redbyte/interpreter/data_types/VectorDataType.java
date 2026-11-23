package net.stonygeist.redbyte.interpreter.data_types;

import com.google.common.collect.ImmutableList;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

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

    static {
        TYPE.declareMembers(self -> {
            self.addProperty(new PropertySymbol("x", NumberType.TYPE, Component.translatable("docs.redbyte.description.properties.vector.x")), x -> new NumberType((float) ((VectorDataType) x).getVector().x));
            self.addProperty(new PropertySymbol("y", NumberType.TYPE, Component.translatable("docs.redbyte.description.properties.vector.y")), x -> new NumberType((float) ((VectorDataType) x).getVector().y));
            self.addProperty(new PropertySymbol("z", NumberType.TYPE, Component.translatable("docs.redbyte.description.properties.vector.z")), x -> new NumberType((float) ((VectorDataType) x).getVector().z));
            self.addMethods(
                    new MethodSymbol("distance", ImmutableList.of(TYPE), NumberType.TYPE,
                            (ev, robo, object, args) -> new NumberType((float) ((VectorDataType) object).getVector().distanceTo(((VectorDataType) args[0]).getVector())),
                            Component.translatable("docs.redbyte.description.functions.vector.distance")),
                    new MethodSymbol("look_at", ImmutableList.of(), NothingDataType.TYPE,
                            (ev, robo, object, args) -> {
                                robo.getEntity().lookAt(EntityAnchorArgument.Anchor.EYES, ((VectorDataType) object).getVector());
                                return new NothingDataType();
                            },
                            Component.translatable("docs.redbyte.description.functions.vector.look_at")));
        });
    }
}
