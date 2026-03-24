package net.stonygeist.redbyte.interpreter.symbols;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.Evaluator;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.manager.PseudoRobo;
import org.apache.commons.lang3.function.TriFunction;

public final class FunctionSymbol extends Symbol {
    public final ImmutableList<Class<? extends DataType>> parameters;
    public final Class<? extends DataType> type;
    public final TriFunction<Evaluator, PseudoRobo, DataType[], ? extends DataType> callback;
    public final Component description;

    public FunctionSymbol(String name, ImmutableList<Class<? extends DataType>> parameters, Class<? extends DataType> type, TriFunction<Evaluator, PseudoRobo, DataType[], ? extends DataType> callback, Component description) {
        super(name);
        this.parameters = parameters;
        this.type = type;
        this.callback = callback;
        this.description = description;
    }
}
