package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class NothingDataType extends DataType {
    public static final TypeSymbol TYPE = new TypeSymbol("nothing", Component.translatable("interpreter.redbyte.types.nothing"));

    public NothingDataType() {
        super(TYPE);
    }

    public static final Map<PropertySymbol, Function<EntityDataType<? extends Entity>, DataType>> properties = new Hashtable<>();
    public static final List<MethodSymbol> methods = List.of();
}
