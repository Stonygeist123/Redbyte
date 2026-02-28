package net.stonygeist.redbyte.interpreter.symbols;

import com.google.common.collect.ImmutableList;
import net.stonygeist.redbyte.interpreter.Evaluator;
import net.stonygeist.redbyte.manager.PseudoRobo;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

public final class FunctionSymbol extends Symbol {
    public final ImmutableList<TypeSymbol> parameters;
    public final TypeSymbol type;
    public final TriFunction<Evaluator, PseudoRobo, Object[], ? extends @Nullable Object> callback;

    public FunctionSymbol(String name, ImmutableList<TypeSymbol> parameters, TypeSymbol type, TriFunction<Evaluator, PseudoRobo, Object[], ? extends @Nullable Object> callback) {
        super(name);
        this.parameters = parameters;
        this.type = type;
        this.callback = callback;
    }
}
