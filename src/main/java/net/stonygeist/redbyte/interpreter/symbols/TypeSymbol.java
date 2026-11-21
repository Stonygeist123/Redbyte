package net.stonygeist.redbyte.interpreter.symbols;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TypeSymbol extends Symbol {
    private final @Nullable TypeSymbol superType;
    private final Component docsName;
    private final boolean isList;

    public TypeSymbol(String name, Component docsName) {
        this(name, null, docsName, false);
    }

    public TypeSymbol(String name, Component docsName, boolean isList) {
        this(name, null, docsName, isList);
    }

    public TypeSymbol(String name, @Nullable TypeSymbol superType, Component docsName) {
        this(name, superType, docsName, false);
    }

    public TypeSymbol(String name, @Nullable TypeSymbol superType, Component docsName, boolean isList) {
        super(name);
        this.superType = superType;
        this.docsName = docsName;
        this.isList = isList;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TypeSymbol t
                && (name.equals((DataType.TYPE).name) || name.equals(t.name) || equals(t.superType))
                && isList == t.isList;
    }

    @Override
    public String toString() {
        return name;
    }

    public Component getDocsName() {
        return docsName;
    }

    public boolean isList() {
        return isList;
    }

    public TypeSymbol getBase() {
        return new TypeSymbol(name, superType, docsName);
    }

    public @NotNull TypeSymbol transform(boolean isList) {
        return new TypeSymbol(name, superType, docsName, isList);
    }
}
