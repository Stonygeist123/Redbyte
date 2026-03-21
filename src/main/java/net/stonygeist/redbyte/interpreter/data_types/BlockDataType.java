package net.stonygeist.redbyte.interpreter.data_types;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;

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
}
