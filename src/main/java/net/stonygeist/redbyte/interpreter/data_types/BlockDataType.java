package net.stonygeist.redbyte.interpreter.data_types;

import com.google.common.collect.ImmutableList;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;
import org.jetbrains.annotations.NotNull;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class BlockDataType extends DataType {
    public static final TypeSymbol TYPE = new TypeSymbol("block", EntityDataType.TYPE, Component.translatable("interpreter.redbyte.types.block"));
    private final @NotNull BlockState blockState;
    private final @NotNull BlockPos position;

    public BlockDataType(@NotNull BlockState blockState, @NotNull BlockPos position) {
        super(TYPE);
        this.blockState = blockState;
        this.position = position;
    }

    public @NotNull BlockState getBlockState() {
        return blockState;
    }

    public @NotNull BlockPos getPosition() {
        return position;
    }

    public static final Map<VariableSymbol, Function<BlockDataType, DataType>> properties = new Hashtable<>(Map.of(
            new VariableSymbol("position", VectorDataType.class), x -> new VectorDataType(x.getPosition().getCenter())
    ));


    public static final List<MethodSymbol> methods = List.of(
            new MethodSymbol("try_destroy", ImmutableList.of(), NothingDataType.class,
                    (ev, robo, object, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        BlockPos blockPos = ((BlockDataType) object).position;
                        if (!roboEntity.level().isEmptyBlock(blockPos))
                            robo.addDestroyBlockGoalProp(blockPos);
                        return new NothingDataType();
                    }, Component.translatable("functions.redbyte.description.try_destroy")),
            new MethodSymbol("look_at", ImmutableList.of(), NothingDataType.class,
                    (ev, robo, object, args) -> {
                        robo.getEntity().lookAt(EntityAnchorArgument.Anchor.EYES, ((BlockDataType) object).position.getCenter());
                        return new NothingDataType();
                    },
                    Component.translatable("functions.redbyte.description.look_at"))
    );
}
