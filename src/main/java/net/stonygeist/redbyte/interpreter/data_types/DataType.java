package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;
import org.jetbrains.annotations.NotNull;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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

    public static final Map<VariableSymbol, Function<DataType, DataType>> properties = new Hashtable<>();

    public static Optional<Map.Entry<VariableSymbol, Function<DataType, DataType>>> getProperty(Map<VariableSymbol, Function<DataType, DataType>> properties, String name) {
        return properties.entrySet()
                .stream()
                .filter(entry -> entry.getKey().name.equalsIgnoreCase(name))
                .findFirst();
    }

    public static final List<MethodSymbol> methods = List.of();

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DataType d ? d.type.equals(type) : super.equals(obj);
    }
}
