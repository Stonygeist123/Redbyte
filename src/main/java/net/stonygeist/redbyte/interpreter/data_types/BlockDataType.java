package net.stonygeist.redbyte.interpreter.data_types;

import com.google.common.collect.ImmutableList;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.stonygeist.redbyte.interpreter.Evaluator;
import net.stonygeist.redbyte.interpreter.data_types.primitives.TextType;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.stonygeist.redbyte.index.RedbyteConfigs.ROBO_RANGE;

public class BlockDataType extends DataType {
    public static final TypeSymbol TYPE = new TypeSymbol("block", Component.translatable("interpreter.redbyte.types.block"));
    private final @NotNull BlockState blockState;
    private final @Nullable BlockEntity blockEntity;
    private final @NotNull BlockPos position;

    public BlockDataType(@NotNull BlockState blockState, @Nullable BlockEntity blockEntity, @NotNull BlockPos position) {
        this(TYPE, blockState, blockEntity, position);
    }

    public BlockDataType(TypeSymbol type, @NotNull BlockState blockState, @Nullable BlockEntity blockEntity, @NotNull BlockPos position) {
        super(type);
        this.blockState = blockState;
        this.blockEntity = blockEntity;
        this.position = position;
    }

    static {
        TYPE.declareMembers(self -> {
            self.addProperty(new PropertySymbol("position", VectorDataType.TYPE, Component.translatable("docs.redbyte.description.properties.block.position")), x -> new VectorDataType(((BlockDataType) x).getPosition().getCenter()));
            self.addMethods(
                    new MethodSymbol("try_destroy", ImmutableList.of(), NothingDataType.TYPE,
                            (ev, robo, object, args) -> {
                                BlockPos blockPos = ((BlockDataType) object).position;
                                if (robo.getEntity().level().isEmptyBlock(blockPos))
                                    throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.block_is_air"), null);

                                if (blockPos.closerToCenterThan(robo.getEntity().position(), ROBO_RANGE)) {
                                    robo.addDestroyBlockGoalProp(blockPos);
                                    return new NothingDataType();
                                } else
                                    throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.block_not_in_range"), null);
                            }, Component.translatable("docs.redbyte.description.functions.block.try_destroy")),
                    new MethodSymbol("look_at", ImmutableList.of(), NothingDataType.TYPE,
                            (ev, robo, object, args) -> {
                                robo.getEntity().lookAt(EntityAnchorArgument.Anchor.EYES, ((BlockDataType) object).position.getCenter());
                                return new NothingDataType();
                            },
                            Component.translatable("docs.redbyte.description.functions.block.look_at")),
                    new MethodSymbol("asContainer", ImmutableList.of(), ContainerBlockDataType.TYPE,
                            (ev, robo, object, args) -> {
                                if (object instanceof ContainerBlockDataType)
                                    return object;
                                BlockDataType block = (BlockDataType) object;
                                if (block.getBlockEntity() instanceof BaseContainerBlockEntity container)
                                    return new ContainerBlockDataType(block.getBlockState(), container, block.getPosition());
                                return new NothingDataType();
                            },
                            Component.translatable("docs.redbyte.description.functions.block.asContainer")),
                    new MethodSymbol("get_id", ImmutableList.of(), TextType.TYPE,
                            (ev, robo, object, args) -> {
                                BlockDataType block = (BlockDataType) object;
                                ResourceLocation idResource = ForgeRegistries.BLOCKS.getKey(block.getBlockState().getBlock());
                                assert idResource != null;
                                return new TextType(idResource.toString());
                            },
                            Component.translatable("docs.redbyte.description.functions.block.get_id"))
            );
        });
    }

    public @NotNull BlockState getBlockState() {
        return blockState;
    }

    public @NotNull BlockPos getPosition() {
        return position;
    }

    public @Nullable BlockEntity getBlockEntity() {
        return blockEntity;
    }
}
