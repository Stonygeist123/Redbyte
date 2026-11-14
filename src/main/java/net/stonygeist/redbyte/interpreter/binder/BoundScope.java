package net.stonygeist.redbyte.interpreter.binder;

import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;

import javax.annotation.Nullable;
import java.util.Hashtable;

// TODO: Make variables only be saved in the specific scope so changes get applied from child to parent.
public final class BoundScope {
    private final Hashtable<String, VariableSymbol> _symbolTable = new Hashtable<>();
    private final @Nullable BoundScope _parent;

    public BoundScope() {
        _parent = null;
    }

    public BoundScope(@Nullable BoundScope parent) {
        _parent = parent;
        if (parent != null)
            _symbolTable.putAll(_parent.getSymbolTable());
    }

    public @Nullable BoundScope getParent() {
        return _parent;
    }

    public Hashtable<String, VariableSymbol> getSymbolTable() {
        return _symbolTable;
    }
}
