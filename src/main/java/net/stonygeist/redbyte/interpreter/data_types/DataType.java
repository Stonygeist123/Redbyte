package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.data_types.primitives.BooleanType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.PrimitiveType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.TextType;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class DataType {
    public static final TypeSymbol TYPE = new TypeSymbol("any", Component.translatable("interpreter.redbyte.types.any"));
    @NotNull
    private final TypeSymbol type;

    public DataType(@NotNull TypeSymbol type) {
        this.type = type;
    }

    public @NotNull TypeSymbol getType() {
        return type;
    }

    public static List<TypeSymbol> getTypes() {
        return List.of(TYPE, PrimitiveType.TYPE, NumberType.TYPE, TextType.TYPE, BooleanType.TYPE, ListType.TYPE, CreatureDataType.TYPE, EntityDataType.TYPE, MonsterDataType.TYPE, PlayerDataType.TYPE, RoboDataType.TYPE, VectorDataType.TYPE, BlockDataType.TYPE, ContainerBlockDataType.TYPE);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DataType d ? d.type.equals(type) : super.equals(obj);
    }
}
