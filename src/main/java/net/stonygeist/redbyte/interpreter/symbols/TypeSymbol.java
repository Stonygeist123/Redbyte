package net.stonygeist.redbyte.interpreter.symbols;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import org.jetbrains.annotations.Nullable;

public class TypeSymbol extends Symbol {
    private final @Nullable TypeSymbol superType;
    private final Component docsName;

    public TypeSymbol(String name, Component docsName) {
        this(name, null, docsName);
    }

    public TypeSymbol(String name, @Nullable TypeSymbol superType, Component docsName) {
        super(name);
        this.superType = superType;
        this.docsName = docsName;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TypeSymbol t && (name.equals((DataType.TYPE).name) || name.equals(t.name) || equals(t.superType));
    }

    @Override
    public String toString() {
        return name;
    }

    public Component getDocsName() {
        return docsName;
    }
}
