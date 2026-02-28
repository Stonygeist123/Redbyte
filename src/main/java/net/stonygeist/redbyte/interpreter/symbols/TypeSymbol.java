package net.stonygeist.redbyte.interpreter.symbols;

public final class TypeSymbol extends Symbol {
    public final static TypeSymbol Number = new TypeSymbol("number");
    public final static TypeSymbol Text = new TypeSymbol("text");
    public final static TypeSymbol Boolean = new TypeSymbol("boolean");
    public final static TypeSymbol Void = new TypeSymbol("void");

    public TypeSymbol(String name) {
        super(name);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TypeSymbol t && t.name.equals(name);
    }
}
