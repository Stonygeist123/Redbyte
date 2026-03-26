package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.state.BlockState;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ContainerBlockDataType extends BlockDataType {
    public static final TypeSymbol TYPE = new TypeSymbol("container_block", EntityDataType.TYPE, Component.translatable("interpreter.redbyte.types.container_block"));
    @NotNull
    private final Container container;

    public ContainerBlockDataType(@NotNull BlockState blockState, @NotNull Container container, @NotNull BlockPos position) {
        super(TYPE, blockState, position);
        this.container = container;
    }

    public static final Map<PropertySymbol, Function<ContainerBlockDataType, DataType>> properties = new Hashtable<>(Map.of());
    public static final List<MethodSymbol> methods = List.of(
//            new MethodSymbol("look_at", ImmutableList.of(), NothingDataType.class,
//                    (ev, robo, object, args) -> {
//                        robo.getEntity().lookAt(EntityAnchorArgument.Anchor.EYES, ((BlockDataType) object).position.getCenter());
//                        return new NothingDataType();
//                    },
//                    Component.translatable("docs.redbyte.description.functions.block.look_at"))
    );
}
