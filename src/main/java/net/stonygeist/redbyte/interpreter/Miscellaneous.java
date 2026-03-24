package net.stonygeist.redbyte.interpreter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.redbyte.interpreter.data_types.*;
import net.stonygeist.redbyte.interpreter.data_types.primitives.BooleanType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.TextType;
import net.stonygeist.redbyte.interpreter.symbols.FunctionSymbol;
import org.joml.Vector3f;

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


    public static Map.Entry<FunctionSymbol, Boolean> getFunction(String name, List<? extends Class<? extends DataType>> args) {
        List<FunctionSymbol> result = functions.stream().filter(f -> f.name.toLowerCase().equals(name)).toList();
        if (result.size() == 1)
            return new AbstractMap.SimpleEntry<>(result.getFirst(), false);
        for (FunctionSymbol function : result) {
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
                    }, Component.translatable("functions.redbyte.description.print")))
            .add(new FunctionSymbol("print", ImmutableList.of(NumberType.class), NothingDataType.class,
                    (ev, robo, args) -> {
                        robo.getEntity().addPrintOutput(((NumberType) args[0]).getValue());
                        return new NothingDataType();
                    }, Component.translatable("functions.redbyte.description.print")))
            .add(new FunctionSymbol("is_nothing", ImmutableList.of(DataType.class), BooleanType.class,
                    (ev, robo, args) -> new BooleanType(args[0] instanceof NothingDataType), Component.translatable("functions.redbyte.description.is_nothing")))
            .add(new FunctionSymbol("walk", ImmutableList.of(NumberType.class), NothingDataType.class, (ev, robo, args) -> {
                robo.addWalkGoalProp(((NumberType) args[0]).getValue());
                return new NothingDataType();
            }, Component.translatable("functions.redbyte.description.walk")))
            .add(new FunctionSymbol("walk_to", ImmutableList.of(NumberType.class, NumberType.class, NumberType.class), NothingDataType.class, (ev, robo, args) -> {
                robo.addWalkToGoalProp(new Vec3(((NumberType) args[0]).getValue(), ((NumberType) args[1]).getValue(), ((NumberType) args[2]).getValue()));
                return new NothingDataType();
            }, Component.translatable("functions.redbyte.description.walk_to")))
            .add(new FunctionSymbol("walk_to", ImmutableList.of(VectorDataType.class), NothingDataType.class, (ev, robo, args) -> {
                robo.addWalkToGoalProp(new Vec3(((VectorDataType) args[0]).getVector()));
                return new NothingDataType();
            }, Component.translatable("functions.redbyte.description.walk_to")))
            .add(new FunctionSymbol("jump", ImmutableList.of(), NothingDataType.class, (ev, robo, args) -> {
                robo.getEntity().jumpFromGround();
                return new NothingDataType();
            }, Component.translatable("functions.redbyte.description.jump")))
            .add(new FunctionSymbol("position", ImmutableList.of(EntityDataType.class), VectorDataType.class, (ev, robo, args) -> {
                EntityDataType<?> entity = (EntityDataType<?>) args[0];
                return new VectorDataType(entity.getEntity().position());
            }, Component.translatable("functions.redbyte.description.position")))
            .add(new FunctionSymbol("position", ImmutableList.of(BlockDataType.class), VectorDataType.class, (ev, robo, args) -> {
                BlockDataType block = (BlockDataType) args[0];
                return new VectorDataType(block.getPosition().getCenter());
            }, Component.translatable("functions.redbyte.description.position")))
            .add(new FunctionSymbol("vector", ImmutableList.of(NumberType.class, NumberType.class, NumberType.class), VectorDataType.class,
                    (ev, robo, args) -> new VectorDataType(((NumberType) args[0]).getValue(), ((NumberType) args[1]).getValue(), ((NumberType) args[2]).getValue()), Component.translatable("functions.redbyte.description.vector")))
            .add(new FunctionSymbol("x", ImmutableList.of(VectorDataType.class), NumberType.class,
                    (ev, robo, args) -> new NumberType(((VectorDataType) args[0]).getVector().x), Component.translatable("functions.redbyte.description.x")))
            .add(new FunctionSymbol("y", ImmutableList.of(VectorDataType.class), NumberType.class,
                    (ev, robo, args) -> new NumberType(((VectorDataType) args[0]).getVector().y), Component.translatable("functions.redbyte.description.y")))
            .add(new FunctionSymbol("z", ImmutableList.of(VectorDataType.class), NumberType.class,
                    (ev, robo, args) -> new NumberType(((VectorDataType) args[0]).getVector().z), Component.translatable("functions.redbyte.description.z")))
            .add(new FunctionSymbol("distance", ImmutableList.of(VectorDataType.class, VectorDataType.class), NumberType.class,
                    (ev, robo, args) -> new NumberType(((VectorDataType) args[0]).getVector().distance(((VectorDataType) args[1]).getVector())),
                    Component.translatable("functions.redbyte.description.distance")))
            .add(new FunctionSymbol("rotate", ImmutableList.of(NumberType.class), NothingDataType.class,
                    (ev, robo, args) -> {
                        robo.getEntity().rotateBy(((NumberType) args[0]).getValue());
                        return new NothingDataType();
                    },
                    Component.translatable("functions.redbyte.description.rotate")))
            .add(new FunctionSymbol("look_at", ImmutableList.of(VectorDataType.class), NothingDataType.class,
                    (ev, robo, args) -> {
                        robo.getEntity().lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(((VectorDataType) args[0]).getVector()));
                        return new NothingDataType();
                    },
                    Component.translatable("functions.redbyte.description.look_at")))
            .add(new FunctionSymbol("follow", ImmutableList.of(EntityDataType.class), NothingDataType.class, (ev, robo, args) -> {
                CreatureDataType<?> entity = (CreatureDataType<?>) args[0];
                robo.addFollowEntityGoalProp(entity.getEntity());
                return new NothingDataType();
            }, Component.translatable("functions.redbyte.description.follow")))
            .add(new FunctionSymbol("stop_follow", ImmutableList.of(), NothingDataType.class, (ev, robo, args) -> {
                robo.popFollowEntityGoalProp();
                return new NothingDataType();
            }, Component.translatable("functions.redbyte.description.stop_follow")))
            .add(new FunctionSymbol("try_attack", ImmutableList.of(CreatureDataType.class), NothingDataType.class, (ev, robo, args) -> {
                CreatureDataType<?> entity = (CreatureDataType<?>) args[0];
                robo.addAttackGoalProp(entity.getEntity());
                return new NothingDataType();
            }, Component.translatable("functions.redbyte.description.try_attack")))
            .add(new FunctionSymbol("can_attack", ImmutableList.of(CreatureDataType.class), BooleanType.class, (ev, robo, args) -> {
                CreatureDataType<?> entity = (CreatureDataType<?>) args[0];
                if (entity.getEntity() == null) return new BooleanType(false);
                RoboEntity roboEntity = robo.getEntity();
                return new BooleanType(roboEntity.canAttack(entity.getEntity()) && roboEntity.isInRange(entity.getEntity()) && roboEntity.hasLineOfSight(entity.getEntity()));
            }, Component.translatable("functions.redbyte.description.can_attack")))
            .add(new FunctionSymbol("health", ImmutableList.of(CreatureDataType.class), NumberType.class,
                    (ev, robo, args) -> new NumberType(((CreatureDataType<?>) args[0]).getEntity().getHealth()),
                    Component.translatable("functions.redbyte.description.health")))
            .add(new FunctionSymbol("get_player", ImmutableList.of(TextType.class), PlayerDataType.class,
                    (ev, robo, args) -> {
                        List<ServerPlayer> players = robo.getServerLevel().getPlayers(p -> p.getName().getString().equals(((TextType) args[0]).getValue()));
                        return players.isEmpty() ? new NothingDataType() : new PlayerDataType(players.getFirst());
                    }, Component.translatable("functions.redbyte.description.get_player"))
            )
            .add(new FunctionSymbol("get_nearest_player", ImmutableList.of(NumberType.class), PlayerDataType.class,
                    (ev, robo, args) -> {
                        Player player = robo.getServerLevel().getNearestPlayer(robo.getEntity(), ((NumberType) args[0]).getValue());
                        return player == null ? new NothingDataType() : new PlayerDataType(player);
                    },
                    Component.translatable("functions.redbyte.description.get_nearest_player")))
            .add(new FunctionSymbol("get_nearest_monster", ImmutableList.of(NumberType.class), MonsterDataType.class,
                    (ev, robo, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        Vec3 pos = roboEntity.position();
                        AABB searchArea = robo.getEntity().getBoundingBox().inflate(((NumberType) args[0]).getValue());
                        Monster monster = robo.getServerLevel().getNearestEntity(Monster.class, TargetingConditions.forCombat(), roboEntity, pos.x, pos.y, pos.z, searchArea);
                        return monster == null ? new NothingDataType() : new MonsterDataType(monster);
                    }, Component.translatable("functions.redbyte.description.get_nearest_monster")))
            .add(new FunctionSymbol("get_block", ImmutableList.of(VectorDataType.class), BlockDataType.class,
                    (ev, robo, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        Vector3f vec = ((VectorDataType) args[0]).getVector();
                        BlockPos blockPos = BlockPos.containing(vec.x, vec.y, vec.z);
                        return roboEntity.level().isEmptyBlock(blockPos) ? new NothingDataType() : new BlockDataType(roboEntity.level().getBlockState(blockPos), blockPos);
                    }, Component.translatable("functions.redbyte.description.get_block")))
            .add(new FunctionSymbol("try_destroy", ImmutableList.of(BlockDataType.class), NothingDataType.class,
                    (ev, robo, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        BlockPos blockPos = ((BlockDataType) args[0]).getPosition();
                        if (!roboEntity.level().isEmptyBlock(blockPos))
                            robo.addDestroyBlockGoalProp(blockPos);
                        return new NothingDataType();
                    }, Component.translatable("functions.redbyte.description.try_destroy")))
            .add(new FunctionSymbol("try_place", ImmutableList.of(NumberType.class, VectorDataType.class), NothingDataType.class,
                    (ev, robo, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        float slotFloat = ((NumberType) args[0]).getValue();
                        if (Math.round(slotFloat) != slotFloat)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.expected_integer"), 0);

                        int slot = Math.round(slotFloat) - 1;
                        if (slot >= roboEntity.getInventory().getSlots() || slot < 0)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.expected_range", 0, roboEntity.getInventory().getSlots()), 0);

                        BlockPos pos = BlockPos.containing(new Vec3(((VectorDataType) args[1]).getVector()));
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
                    }, Component.translatable("functions.redbyte.description.try_place")))
            .add(new FunctionSymbol("get_item", ImmutableList.of(NumberType.class), ItemStackDataType.class,
                    (ev, robo, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        float slotFloat = ((NumberType) args[0]).getValue();
                        if (Math.round(slotFloat) != slotFloat)
                            throw new Evaluator.CallEvaluationError(Component.translatable("runtime.redbyte.error.expected_integer"), 0);

                        int slot = Math.round(slotFloat) - 1;
                        return new ItemStackDataType(roboEntity.getInventory().getStackInSlot(slot), slot);
                    }, Component.translatable("functions.redbyte.description.get_item")))
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
}
