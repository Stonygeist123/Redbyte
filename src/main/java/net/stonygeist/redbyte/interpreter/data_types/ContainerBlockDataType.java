package net.stonygeist.redbyte.interpreter.data_types;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.index.RedbyteConfigs;
import net.stonygeist.redbyte.interpreter.Evaluator;
import net.stonygeist.redbyte.interpreter.Miscellaneous;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.TextType;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ContainerBlockDataType extends BlockDataType {
    public static final TypeSymbol TYPE = new TypeSymbol("container_block", EntityDataType.TYPE, Component.translatable("interpreter.redbyte.types.container_block"));
    private final @NotNull BaseContainerBlockEntity container;

    public ContainerBlockDataType(@NotNull BlockState blockState, @NotNull BaseContainerBlockEntity container, @NotNull BlockPos position) {
        super(TYPE, blockState, container, position);
        this.container = container;
    }

    public @NotNull BaseContainerBlockEntity getContainer() {
        return container;
    }

    public static final Map<PropertySymbol, Function<ContainerBlockDataType, DataType>> properties = new Hashtable<>(Map.of(
            new PropertySymbol("slots", NumberType.class, Component.translatable("docs.redbyte.description.properties.container_block.slots")), x -> new NumberType(x.getContainer().getContainerSize())
    ));
    public static final List<MethodSymbol> methods = List.of(
            new MethodSymbol("try_quick_put", ImmutableList.of(NumberType.class), NothingDataType.class,
                    (ev, robo, object, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        ContainerBlockDataType block = (ContainerBlockDataType) object;
                        if (roboEntity.position().distanceTo(block.getPosition().getCenter()) <= RedbyteConfigs.ROBO_RANGE)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.container_not_in_range"), null);

                        float slotFloat = ((NumberType) args[0]).getValue();
                        if (Math.round(slotFloat) != slotFloat)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.expected_integer"), 0);

                        int slot = Math.round(slotFloat) - 1;
                        if (slot >= roboEntity.getInventory().getSlots() || slot < 0)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.expected_range", 0, roboEntity.getInventory().getSlots()), 0);

                        Item item = roboEntity.getInventory().getStackInSlot(slot).getItem();
                        ItemStack itemStack = roboEntity.extractItem(slot, item.getDefaultMaxStackSize(), false);
                        if (!block.getContainer().canPlaceItem(slot, itemStack)) {
                            roboEntity.insertItem(slot, itemStack, false);
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.cannot_insert_item", item.getName(itemStack)), 0);
                        }

                        assert block.getBlockEntity() != null;
                        ItemStack remainder = Miscellaneous.smartInsert(
                                block.getBlockEntity().getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(RuntimeException::new),
                                itemStack
                        );
                        if (!remainder.isEmpty()) {
                            remainder = roboEntity.insertItem(slot, remainder, false);
                            for (int i = 0; i < roboEntity.getInventory().getSlots(); i++) {
                                remainder = roboEntity.insertItem(i, remainder, false);
                                if (remainder.isEmpty())
                                    break;
                            }
                        }

                        return new NothingDataType();
                    }, Component.translatable("docs.redbyte.description.functions.container_block.try_quick_put.slot")),
            new MethodSymbol("try_quick_put", ImmutableList.of(TextType.class), NothingDataType.class,
                    (ev, robo, object, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        ContainerBlockDataType block = (ContainerBlockDataType) object;
                        if (roboEntity.position().distanceTo(block.getPosition().getCenter()) > RedbyteConfigs.ROBO_RANGE)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.container_not_in_range"), null);

                        String idToSearch = ((TextType) args[0]).getValue();
                        Map.Entry<Integer, @Nullable Item> slotItemPair = Miscellaneous.getSlot(roboEntity.getInventory(), idToSearch);
                        int slot = slotItemPair.getKey();
                        Item item = slotItemPair.getValue();
                        if (item == null)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.item_does_not_exist", idToSearch), 0);

                        if (slot == -1)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.item_not_found", idToSearch), 0);

                        ItemStack itemStack = roboEntity.extractItem(slot, item.getDefaultMaxStackSize(), false);
                        if (!block.getContainer().canPlaceItem(slot, itemStack)) {
                            roboEntity.insertItem(slot, itemStack, false);
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.cannot_insert_item", item.getName(itemStack)), 0);
                        }

                        assert block.getBlockEntity() != null;
                        ItemStack remainder = Miscellaneous.smartInsert(
                                block.getBlockEntity().getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(RuntimeException::new),
                                itemStack
                        );
                        if (!remainder.isEmpty()) {
                            remainder = roboEntity.insertItem(slot, remainder, false);
                            for (int i = 0; i < roboEntity.getInventory().getSlots(); ++i) {
                                remainder = roboEntity.insertItem(i, remainder, false);
                                if (remainder.isEmpty())
                                    break;
                            }
                        }

                        return new NothingDataType();
                    }, Component.translatable("docs.redbyte.description.functions.container_block.try_quick_put.name")),
            new MethodSymbol("try_put", ImmutableList.of(NumberType.class, NumberType.class), NothingDataType.class,
                    (ev, robo, object, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        ContainerBlockDataType block = (ContainerBlockDataType) object;
                        if (roboEntity.position().distanceTo(block.getPosition().getCenter()) > RedbyteConfigs.ROBO_RANGE)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.container_not_in_range"), null);

                        float slotFloat = ((NumberType) args[0]).getValue();
                        if (Math.round(slotFloat) != slotFloat)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.expected_integer"), 0);

                        int slot = Math.round(slotFloat) - 1;
                        if (slot >= roboEntity.getInventory().getSlots() || slot < 0)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.expected_range", 0, roboEntity.getInventory().getSlots()), 0);

                        Item item = roboEntity.getInventory().getStackInSlot(slot).getItem();
                        float resultSlotFloat = ((NumberType) args[0]).getValue();
                        if (Math.round(resultSlotFloat) != resultSlotFloat)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.expected_integer"), 1);

                        int resultSlot = Math.round(resultSlotFloat) - 1;
                        if (resultSlot >= block.getContainer().getContainerSize())
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.expected_range", 0, block.getContainer().getContainerSize()), 1);

                        ItemStack itemStack = roboEntity.extractItem(slot, item.getDefaultMaxStackSize(), false);
                        if (!block.getContainer().canPlaceItem(slot, itemStack)) {
                            roboEntity.insertItem(slot, itemStack, false);
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.cannot_insert_item", item.getName(itemStack)), 0);
                        }

                        assert block.getBlockEntity() != null;
                        IItemHandler handler = block.getBlockEntity().getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(RuntimeException::new);
                        ItemStack stackInResultSlot = handler.getStackInSlot(resultSlot);
                        if (!stackInResultSlot.is(itemStack.getItem()))
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.cannot_insert_item_at", item.getName(itemStack), resultSlot), 0);

                        if (stackInResultSlot.getMaxStackSize() >= stackInResultSlot.getCount())
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.slot_is_full", resultSlot), 0);

                        ItemStack remainder = handler.insertItem(resultSlot, itemStack, false);
                        if (!remainder.isEmpty()) {
                            remainder = roboEntity.insertItem(slot, remainder, false);
                            for (int i = 0; i < roboEntity.getInventory().getSlots(); ++i) {
                                remainder = roboEntity.insertItem(i, remainder, false);
                                if (remainder.isEmpty())
                                    break;
                            }
                        }

                        return new NothingDataType();
                    }, Component.translatable("docs.redbyte.description.functions.container_block.try_put.slot")),
            new MethodSymbol("try_put", ImmutableList.of(TextType.class, NumberType.class), NothingDataType.class,
                    (ev, robo, object, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        ContainerBlockDataType block = (ContainerBlockDataType) object;
                        if (roboEntity.position().distanceTo(block.getPosition().getCenter()) > RedbyteConfigs.ROBO_RANGE)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.container_not_in_range"), null);

                        String idToSearch = ((TextType) args[0]).getValue();
                        Map.Entry<Integer, @Nullable Item> slotItemPair = Miscellaneous.getSlot(roboEntity.getInventory(), idToSearch);
                        int slot = slotItemPair.getKey();
                        Item item = slotItemPair.getValue();
                        if (item == null)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.item_does_not_exist", idToSearch), 0);

                        if (slot == -1)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.item_not_found", idToSearch), 0);

                        float resultSlotFloat = ((NumberType) args[0]).getValue();
                        if (Math.round(resultSlotFloat) != resultSlotFloat)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.expected_integer"), 1);

                        int resultSlot = Math.round(resultSlotFloat) - 1;
                        if (resultSlot >= block.getContainer().getContainerSize() || slot < 0)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.expected_range", 0, block.getContainer().getContainerSize()), 1);

                        ItemStack itemStack = roboEntity.extractItem(slot, item.getDefaultMaxStackSize(), false);
                        if (!block.getContainer().canPlaceItem(slot, itemStack)) {
                            roboEntity.insertItem(slot, itemStack, false);
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.cannot_insert_item", item.getName(itemStack)), 0);
                        }

                        assert block.getBlockEntity() != null;
                        IItemHandler handler = block.getBlockEntity().getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(RuntimeException::new);
                        ItemStack stackInResultSlot = handler.getStackInSlot(resultSlot);
                        if (!stackInResultSlot.is(itemStack.getItem()))
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.cannot_insert_item_at", item.getName(itemStack), resultSlot), 0);

                        if (stackInResultSlot.getMaxStackSize() >= stackInResultSlot.getCount())
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.slot_is_full", resultSlot), 0);

                        ItemStack remainder = handler.insertItem(resultSlot, itemStack, false);
                        if (!remainder.isEmpty()) {
                            remainder = roboEntity.insertItem(slot, remainder, false);
                            for (int i = 0; i < roboEntity.getInventory().getSlots(); ++i) {
                                remainder = roboEntity.insertItem(i, remainder, false);
                                if (remainder.isEmpty())
                                    break;
                            }
                        }

                        return new NothingDataType();
                    }, Component.translatable("docs.redbyte.description.functions.container_block.try_put.name"))
    );
}
