package net.stonygeist.redbyte.interpreter.symbols;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.Evaluator;
import net.stonygeist.redbyte.manager.PseudoRobo;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

public final class FunctionSymbol extends Symbol {
    public final ImmutableList<TypeSymbol> parameters;
    public final TypeSymbol type;
    public final TriFunction<Evaluator, PseudoRobo, Object[], ? extends @NotNull Object> callback;
    public final Component description;

    public FunctionSymbol(String name, ImmutableList<TypeSymbol> parameters, TypeSymbol type, TriFunction<Evaluator, PseudoRobo, Object[], ? extends @NotNull Object> callback, Component description) {
        super(name);
        this.parameters = parameters;
        this.type = type;
        this.callback = callback;
        this.description = description;
    }
}
