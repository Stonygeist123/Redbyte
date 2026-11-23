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
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RoboDataType extends CreatureDataType<RoboEntity> {
    public static final TypeSymbol TYPE = new TypeSymbol("robo", CreatureDataType.TYPE, Component.translatable("interpreter.redbyte.types.robo"));

    public RoboDataType(RoboEntity robo) {
        super(TYPE, robo);
    }

    static {
        TYPE.declareMembers(self -> self.addMethods(
                new MethodSymbol("has_item", ImmutableList.of(TextType.TYPE), BooleanType.TYPE,
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
        ));
    }
}
