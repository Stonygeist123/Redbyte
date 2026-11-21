package net.stonygeist.redbyte.interpreter.data_types.primitives;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ListType extends PrimitiveType {
    private final List<? extends DataType> value;

    public ListType(List<DataType> value, Class<? extends DataType> type) throws NoSuchFieldException, IllegalAccessException {
        super(new TypeSymbol(((TypeSymbol) type.getField("TYPE").get(null)).name, ((TypeSymbol) type.getField("TYPE").get(null)).getDocsName().copy().append("[]"), true));
        this.value = value;
    }

    public List<? extends DataType> getValue() {
        return value;
    }

    public static final Map<PropertySymbol, Function<ListType, DataType>> properties = new Hashtable<>(Map.of(
            new PropertySymbol("length", NumberType.class, Component.translatable("docs.redbyte.description.properties.entity.position")), x -> new NumberType(x.getValue().size())
    ));
    public static final List<MethodSymbol> methods = List.of();
}
