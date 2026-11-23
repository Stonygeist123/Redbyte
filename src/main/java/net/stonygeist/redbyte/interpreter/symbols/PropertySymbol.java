package net.stonygeist.redbyte.interpreter.symbols;

import net.minecraft.network.chat.Component;

public class PropertySymbol extends VariableSymbol {
    public final Component description;

    public PropertySymbol(String name, TypeSymbol typeSymbol, Component description) {
        super(name, typeSymbol);
        this.description = description;
    }
}
