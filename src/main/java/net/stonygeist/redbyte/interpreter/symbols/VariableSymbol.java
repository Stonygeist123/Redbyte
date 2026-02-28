package net.stonygeist.redbyte.interpreter.symbols;

public final class VariableSymbol extends Symbol {
    public final TypeSymbol type;

    public VariableSymbol(String name, TypeSymbol type) {
        super(name);
        this.type = type;
    }
}
