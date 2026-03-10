package net.stonygeist.redbyte.interpreter.symbols;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import org.jetbrains.annotations.Nullable;

public final class TypeSymbol extends Symbol {
    public final static TypeSymbol Number = new TypeSymbol("number", Component.translatable("interpreter.redbyte.types.number"));
    public final static TypeSymbol Text = new TypeSymbol("text", Component.translatable("interpreter.redbyte.types.text"));
    public final static TypeSymbol Boolean = new TypeSymbol("boolean", Component.translatable("interpreter.redbyte.types.boolean"));
    public final static TypeSymbol Error = new TypeSymbol("error", Component.translatable("interpreter.redbyte.types.error"));
    private final @Nullable TypeSymbol superType;
    private final Component docsName;

    public TypeSymbol(String name, Component docsName) {
        this(name, DataType.TYPE, docsName);
    }

    public TypeSymbol(String name, @Nullable TypeSymbol superType, Component docsName) {
        super(name);
        this.superType = superType;
        this.docsName = docsName;
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

    public Component getDocsName() {
        return docsName;
    }
}
