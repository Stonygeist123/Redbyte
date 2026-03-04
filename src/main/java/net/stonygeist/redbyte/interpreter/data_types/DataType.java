package net.stonygeist.redbyte.interpreter.data_types;

import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

public abstract class DataType {
    @NotNull
    private final TypeSymbol type;

    public DataType(@NotNull TypeSymbol type) {
        this.type = type;
    }

    public @NotNull TypeSymbol getType() {
        return type;
    }
}
