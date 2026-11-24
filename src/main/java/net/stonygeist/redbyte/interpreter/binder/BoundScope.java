package net.stonygeist.redbyte.interpreter.binder;

import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class BoundScope {
    private final Map<String, VariableSymbol> _symbols = new HashMap<>();
    private final @Nullable BoundScope _parent;

    public BoundScope() {
        _parent = null;
    }

    public BoundScope(@Nullable BoundScope parent) {
        _parent = parent;
    }

    public @Nullable BoundScope getParent() {
        return _parent;
    }

    public @Nullable VariableSymbol lookup(String name) {
        for (BoundScope scope = this; scope != null; scope = scope._parent) {
            VariableSymbol variable = scope._symbols.get(name);
            if (variable != null)
                return variable;
        }

        return null;
    }

    public void declare(VariableSymbol variable) {
        _symbols.putIfAbsent(variable.name, variable);
    }
}
