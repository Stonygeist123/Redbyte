package net.stonygeist.redbyte.interpreter.symbols;

import com.google.common.collect.ImmutableList;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.Evaluator;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.manager.PseudoRobo;

public final class MethodSymbol extends FunctionSymbol {
    // args: evaluator, robo, object, args
    public final PropertyDispatch.QuadFunction<Evaluator, PseudoRobo, DataType, DataType[], ? extends DataType> callback;

    public MethodSymbol(String name, ImmutableList<Class<? extends DataType>> parameters, Class<? extends DataType> type, PropertyDispatch.QuadFunction<Evaluator, PseudoRobo, DataType, DataType[], ? extends DataType> callback, Component description) {
        super(name, parameters, type, null, description);
        this.callback = callback;
    }
}
