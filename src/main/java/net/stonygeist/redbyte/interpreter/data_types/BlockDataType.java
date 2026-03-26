package net.stonygeist.redbyte.interpreter.data_types;

import com.google.common.collect.ImmutableList;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BlockDataType extends DataType {
    public static final TypeSymbol TYPE = new TypeSymbol("block", EntityDataType.TYPE, Component.translatable("interpreter.redbyte.types.block"));
    private final @NotNull BlockState blockState;
    private final @NotNull BlockPos position;

    public BlockDataType(@NotNull BlockState blockState, @NotNull BlockPos position) {
        this(TYPE, blockState, position);
    }

    public BlockDataType(TypeSymbol type, @NotNull BlockState blockState, @NotNull BlockPos position) {
        super(type);
        this.blockState = blockState;
        this.position = position;
    }

    public @NotNull BlockState getBlockState() {
        return blockState;
    }

    public @NotNull BlockPos getPosition() {
        return position;
    }

    public static final Map<PropertySymbol, Function<BlockDataType, DataType>> properties = new Hashtable<>(Map.of(
            new PropertySymbol("position", VectorDataType.class, Component.translatable("docs.redbyte.description.properties.block.position")), x -> new VectorDataType(x.getPosition().getCenter())
    ));
    public static final List<MethodSymbol> methods = List.of(
            new MethodSymbol("try_destroy", ImmutableList.of(), NothingDataType.class,
                    (ev, robo, object, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        BlockPos blockPos = ((BlockDataType) object).position;
                        if (!roboEntity.level().isEmptyBlock(blockPos))
                            robo.addDestroyBlockGoalProp(blockPos);
                        return new NothingDataType();
                    }, Component.translatable("docs.redbyte.description.functions.block.try_destroy")),
            new MethodSymbol("look_at", ImmutableList.of(), NothingDataType.class,
                    (ev, robo, object, args) -> {
                        robo.getEntity().lookAt(EntityAnchorArgument.Anchor.EYES, ((BlockDataType) object).position.getCenter());
                        return new NothingDataType();
                    },
                    Component.translatable("docs.redbyte.description.functions.block.look_at"))
    );
}
