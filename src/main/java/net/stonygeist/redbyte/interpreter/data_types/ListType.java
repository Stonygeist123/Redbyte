package net.stonygeist.redbyte.interpreter.data_types;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

import java.util.ArrayList;
import java.util.List;

public final class ListType extends DataType {
    private final List<DataType> value;
    public static final TypeSymbol TYPE = new TypeSymbol("list", Component.translatable("interpreter.redbyte.types.list"));

    public ListType(List<DataType> value, TypeSymbol elementType) {
        super(TypeSymbol.listOf(elementType));
        this.value = new ArrayList<>(value);
    }

    public List<DataType> getValue() {
        return value;
    }

    static {
        TYPE.declareMembers(self -> {
            self.addProperty(new PropertySymbol("size", NumberType.TYPE, Component.translatable("docs.redbyte.description.properties.list.size")), x -> new NumberType(((ListType) x).getValue().size()));
            self.addMethods(
                    new MethodSymbol("add", ImmutableList.of(DataType.TYPE), NothingDataType.TYPE, (ev, robo, object, args) -> {
                        ((ListType) object).getValue().add(args[0]);
                        return new NothingDataType();
                    }, Component.translatable("docs.redbyte.description.functions.list.add")));
        });
    }
}
