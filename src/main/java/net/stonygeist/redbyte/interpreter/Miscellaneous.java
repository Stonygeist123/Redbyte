package net.stonygeist.redbyte.interpreter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.redbyte.interpreter.data_types.*;
import net.stonygeist.redbyte.interpreter.data_types.primitives.BooleanType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.TextType;
import net.stonygeist.redbyte.interpreter.symbols.FunctionSymbol;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public enum Miscellaneous {
    ;
    public final static int unaryPrecedence = 7;

    public static int getBinaryPrecedence(TokenKind kind) {
        return switch (kind) {
            case Star, Slash -> 6;
            case Plus, Minus -> 5;
            case EqualsEquals, NotEquals -> 4;
            case Greater, GreaterEquals, Less, LessEquals -> 3;
            case And -> 2;
            case Or -> 1;
            default -> 0;
        };
    }

    // Key: the function, if found
    // Value: True if multiple methods with given name exist. False otherwise
    public static <T extends FunctionSymbol> Map.Entry<T, Boolean> getFunction(List<T> functions, String name, List<? extends Class<? extends DataType>> args) {
        List<T> result = functions.stream().filter(f -> f.name.toLowerCase().equals(name)).toList();
        if (result.size() == 1)
            return new AbstractMap.SimpleEntry<>(result.getFirst(), false);
        for (T function : result) {
            if (function.parameters.size() != args.size())
                continue;
            boolean valid = true;
            for (int i = 0; i < function.parameters.size(); i++)
                if (!function.parameters.get(i).equals(args.get(i))) {
                    valid = false;
                    break;
                }

            if (valid)
                return new AbstractMap.SimpleEntry<>(function, true);
        }

        return new AbstractMap.SimpleEntry<>(null, result.size() > 1);
    }

    public static final ImmutableList<FunctionSymbol> functions = new ImmutableList.Builder<FunctionSymbol>()
            .add(new FunctionSymbol("print", ImmutableList.of(TextType.class), NothingDataType.class,
                    (ev, robo, args) -> {
                        robo.getEntity().addPrintOutput(((TextType) args[0]).getValue());
                        return new NothingDataType();
                    }, Component.translatable("docs.redbyte.description.functions.print")))
            .add(new FunctionSymbol("print", ImmutableList.of(NumberType.class), NothingDataType.class,
                    (ev, robo, args) -> {
                        robo.getEntity().addPrintOutput(((NumberType) args[0]).getValue());
                        return new NothingDataType();
                    }, Component.translatable("docs.redbyte.description.functions.print")))
            .add(new FunctionSymbol("is_nothing", ImmutableList.of(DataType.class), BooleanType.class,
                    (ev, robo, args) -> new BooleanType(args[0] instanceof NothingDataType), Component.translatable("docs.redbyte.description.functions.is_nothing")))
            .add(new FunctionSymbol("walk", ImmutableList.of(NumberType.class), NothingDataType.class, (ev, robo, args) -> {
                robo.addWalkGoalProp(((NumberType) args[0]).getValue());
                return new NothingDataType();
            }, Component.translatable("docs.redbyte.description.functions.walk")))
            .add(new FunctionSymbol("walk_to", ImmutableList.of(NumberType.class, NumberType.class, NumberType.class), NothingDataType.class, (ev, robo, args) -> {
                robo.addWalkToGoalProp(new Vec3(((NumberType) args[0]).getValue(), ((NumberType) args[1]).getValue(), ((NumberType) args[2]).getValue()));
                return new NothingDataType();
            }, Component.translatable("docs.redbyte.description.functions.walk_to")))
            .add(new FunctionSymbol("walk_to", ImmutableList.of(VectorDataType.class), NothingDataType.class, (ev, robo, args) -> {
                robo.addWalkToGoalProp(((VectorDataType) args[0]).getVector());
                return new NothingDataType();
            }, Component.translatable("docs.redbyte.description.functions.walk_to")))
            .add(new FunctionSymbol("jump", ImmutableList.of(), NothingDataType.class, (ev, robo, args) -> {
                robo.getEntity().jumpFromGround();
                return new NothingDataType();
            }, Component.translatable("docs.redbyte.description.functions.jump")))
            .add(new FunctionSymbol("vector", ImmutableList.of(NumberType.class, NumberType.class, NumberType.class), VectorDataType.class,
                    (ev, robo, args) -> new VectorDataType(((NumberType) args[0]).getValue(), ((NumberType) args[1]).getValue(), ((NumberType) args[2]).getValue()), Component.translatable("docs.redbyte.description.functions.vector")))
            .add(new FunctionSymbol("rotate", ImmutableList.of(NumberType.class), NothingDataType.class,
                    (ev, robo, args) -> {
                        robo.getEntity().rotateBy(((NumberType) args[0]).getValue());
                        return new NothingDataType();
                    },
                    Component.translatable("docs.redbyte.description.functions.rotate")))
            .add(new FunctionSymbol("get_player", ImmutableList.of(TextType.class), PlayerDataType.class,
                    (ev, robo, args) -> {
                        List<ServerPlayer> players = robo.getServerLevel().getPlayers(p -> p.getName().getString().equals(((TextType) args[0]).getValue()));
                        return players.isEmpty() ? new NothingDataType() : new PlayerDataType(players.getFirst());
                    }, Component.translatable("docs.redbyte.description.functions.get_player"))
            )
            .add(new FunctionSymbol("get_nearest_player", ImmutableList.of(NumberType.class), PlayerDataType.class,
                    (ev, robo, args) -> {
                        Player player = robo.getServerLevel().getNearestPlayer(robo.getEntity(), ((NumberType) args[0]).getValue());
                        return player == null ? new NothingDataType() : new PlayerDataType(player);
                    },
                    Component.translatable("docs.redbyte.description.functions.get_nearest_player")))
            .add(new FunctionSymbol("get_nearest_monster", ImmutableList.of(NumberType.class), MonsterDataType.class,
                    (ev, robo, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        Vec3 pos = roboEntity.position();
                        AABB searchArea = robo.getEntity().getBoundingBox().inflate(((NumberType) args[0]).getValue());
                        Monster monster = robo.getServerLevel().getNearestEntity(Monster.class, TargetingConditions.forCombat(), roboEntity, pos.x, pos.y, pos.z, searchArea);
                        return monster == null ? new NothingDataType() : new MonsterDataType(monster);
                    }, Component.translatable("docs.redbyte.description.functions.get_nearest_monster")))
            .add(new FunctionSymbol("get_block", ImmutableList.of(VectorDataType.class), BlockDataType.class,
                    (ev, robo, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        Vec3 vec = ((VectorDataType) args[0]).getVector();
                        BlockPos blockPos = BlockPos.containing(vec.x, vec.y, vec.z);
                        Level level = roboEntity.level();
                        return level.isEmptyBlock(blockPos) ? new NothingDataType() : new BlockDataType(level.getBlockState(blockPos), level.getBlockEntity(blockPos), blockPos);
                    }, Component.translatable("docs.redbyte.description.functions.get_block")))
            .add(new FunctionSymbol("try_place", ImmutableList.of(NumberType.class, VectorDataType.class), NothingDataType.class,
                    (ev, robo, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        float slotFloat = ((NumberType) args[0]).getValue();
                        if (Math.round(slotFloat) != slotFloat)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.expected_integer"), 0);

                        int slot = Math.round(slotFloat) - 1;
                        if (slot >= roboEntity.getInventory().getSlots() || slot < 0)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.expected_range", 0, roboEntity.getInventory().getSlots()), 0);

                        BlockPos pos = BlockPos.containing(((VectorDataType) args[1]).getVector());
                        if (roboEntity.level().isEmptyBlock(pos)) {
                            ItemStack itemStack = roboEntity.extractItem(slot, 1, false);
                            if (itemStack.isEmpty())
                                throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.expected_item"), 0);
                            if (!(itemStack.getItem() instanceof BlockItem blockItem)) {
                                roboEntity.insertItem(slot, itemStack, false);
                                throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.item_not_placeable"), 0);
                            }

                            robo.getServerLevel().setBlock(pos, blockItem.getBlock().defaultBlockState(), Block.UPDATE_ALL);
                        }

                        return new NothingDataType();
                    }, Component.translatable("docs.redbyte.description.functions.try_place")))
            .build();

    public static final ImmutableMap<String, TokenKind> keywords = new ImmutableMap.Builder<String, TokenKind>()
            .put("if", TokenKind.If)
            .put("else", TokenKind.Else)
            .put("loop", TokenKind.Loop)
            .put("once", TokenKind.Once)
            .put("while", TokenKind.While)
            .put("always", TokenKind.Always)
            .put("or", TokenKind.Or)
            .put("and", TokenKind.And)
            .put("robo", TokenKind.Robo)
            .build();

    public static ItemStack smartInsert(IItemHandler handler, ItemStack stack) {
        ItemStack remaining = stack;
        boolean canInsert = false;
        for (int i = 0; i < handler.getSlots(); ++i) {
            ItemStack sim = handler.insertItem(i, remaining, true);
            if (sim.getCount() < remaining.getCount()) {
                canInsert = true;
                break;
            }
        }

        if (!canInsert) return stack;
        for (int i = 0; i < handler.getSlots(); ++i) {
            remaining = handler.insertItem(i, remaining, false);
            if (remaining.isEmpty())
                break;
        }

        return remaining;
    }
}
