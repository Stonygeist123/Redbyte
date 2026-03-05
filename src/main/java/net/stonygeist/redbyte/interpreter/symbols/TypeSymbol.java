package net.stonygeist.redbyte.interpreter.symbols;

import net.stonygeist.redbyte.interpreter.data_types.DataType;
import org.jetbrains.annotations.Nullable;

public final class TypeSymbol extends Symbol {
    public final static TypeSymbol Number = new TypeSymbol("number");
    public final static TypeSymbol Text = new TypeSymbol("text");
    public final static TypeSymbol Boolean = new TypeSymbol("boolean");
    public final static TypeSymbol Void = new TypeSymbol("nothing");
    public final static TypeSymbol Error = new TypeSymbol("error");
    private final @Nullable TypeSymbol superType;

    public TypeSymbol(String name) {
        this(name, DataType.TYPE);
    }

    public TypeSymbol(String name, @Nullable TypeSymbol superType) {
        super(name);
        this.superType = superType;
    }

    @Override
    public boolean equals(Object obj) {
        return superType != null ? superType.equals(obj) || (obj instanceof TypeSymbol t && t.name.equals(name)) : obj instanceof TypeSymbol t && t.name.equals(name);
    }

    @Override
    public String toString() {
        return name;
    }

    public @Nullable TypeSymbol getSuperType() {
        return superType;
    }
}
