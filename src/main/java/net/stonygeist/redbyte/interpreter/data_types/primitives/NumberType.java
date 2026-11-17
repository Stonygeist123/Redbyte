package net.stonygeist.redbyte.interpreter.data_types.primitives;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class NumberType extends PrimitiveType {
    public static final TypeSymbol TYPE = new TypeSymbol("number", Component.translatable("interpreter.redbyte.types.number"));
    private final float value;

    public NumberType(float value) {
        super(TYPE);
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public static final Map<PropertySymbol, Function<NumberType, DataType>> properties = new Hashtable<>();
    public static final List<MethodSymbol> methods = List.of(new MethodSymbol(
            "floor", ImmutableList.of(), NumberType.class,
            (ev, robo, object, args) -> new NumberType((float) Math.floor(((NumberType) object).getValue())),
            Component.translatable("docs.redbyte.description.functions.number.floor")), new MethodSymbol(
            "ceil", ImmutableList.of(), NumberType.class,
            (ev, robo, object, args) -> new NumberType((float) Math.ceil(((NumberType) object).getValue())),
            Component.translatable("docs.redbyte.description.functions.number.ceil")), new MethodSymbol(
            "round", ImmutableList.of(), NumberType.class,
            (ev, robo, object, args) -> new NumberType(Math.round(((NumberType) object).getValue())),
            Component.translatable("docs.redbyte.description.functions.number.round"))
    );
}
