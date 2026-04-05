package net.stonygeist.redbyte.interpreter.data_types;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.interpreter.Evaluator;
import net.stonygeist.redbyte.interpreter.Miscellaneous;
import net.stonygeist.redbyte.interpreter.data_types.primitives.BooleanType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.TextType;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.Nullable;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RoboDataType extends CreatureDataType<RoboEntity> {
    public static final TypeSymbol TYPE = new TypeSymbol("robo", CreatureDataType.TYPE, Component.translatable("interpreter.redbyte.types.robo"));

    public RoboDataType(RoboEntity robo) {
        super(TYPE, robo);
    }

    public static final Map<PropertySymbol, Function<CreatureDataType<RoboEntity>, DataType>> properties = new Hashtable<>();
    public static final List<MethodSymbol> methods = List.of(
            new MethodSymbol("has_item", ImmutableList.of(TextType.class), BooleanType.class,
                    (ev, robo, object, args) -> {
                        String idToSearch = ((TextType) args[0]).getValue();
                        Map.Entry<Integer, @Nullable Item> slotItemPair = Miscellaneous.getSlot(((RoboDataType) object).getEntity().getInventory(), idToSearch);
                        int slot = slotItemPair.getKey();
                        Item item = slotItemPair.getValue();
                        if (item == null)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.item_does_not_exist", idToSearch), 0);
                        if (slot == -1)
                            return new BooleanType(false);
                        return new BooleanType(true);
                    }, Component.translatable("docs.redbyte.description.functions.robo.has_item"))
    );
}
