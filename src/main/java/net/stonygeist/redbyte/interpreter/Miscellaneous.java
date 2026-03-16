package net.stonygeist.redbyte.interpreter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.redbyte.interpreter.data_types.*;
import net.stonygeist.redbyte.interpreter.symbols.FunctionSymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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


    public static Map.Entry<FunctionSymbol, Boolean> getFunction(String name, List<TypeSymbol> args) {
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
            .add(new FunctionSymbol("print", ImmutableList.of(TypeSymbol.Text), NothingDataType.TYPE,
                    (ev, robo, args) -> {
                        robo.getEntity().addPrintOutput(args[0]);
                        return new NothingDataType();
                    }, Component.translatable("functions.redbyte.description.print")))
            .add(new FunctionSymbol("print", ImmutableList.of(TypeSymbol.Number), NothingDataType.TYPE,
                    (ev, robo, args) -> {
                        robo.getEntity().addPrintOutput(args[0]);
                        return new NothingDataType();
                    }, Component.translatable("functions.redbyte.description.print")))
            .add(new FunctionSymbol("is_nothing", ImmutableList.of(DataType.TYPE), TypeSymbol.Boolean,
                    (ev, robo, args) -> args[0] instanceof NothingDataType, Component.translatable("functions.redbyte.description.is_nothing")))
            .add(new FunctionSymbol("walk", ImmutableList.of(TypeSymbol.Number), NothingDataType.TYPE, (ev, robo, args) -> {
                robo.addWalkGoalProp((float) args[0]);
                return new NothingDataType();
            }, Component.translatable("functions.redbyte.description.walk")))
            .add(new FunctionSymbol("walk_to", ImmutableList.of(TypeSymbol.Number, TypeSymbol.Number, TypeSymbol.Number), NothingDataType.TYPE, (ev, robo, args) -> {
                robo.addWalkToGoalProp(new Vec3((float) args[0], (float) args[1], (float) args[2]));
                return new NothingDataType();
            }, Component.translatable("functions.redbyte.description.walk_to")))
            .add(new FunctionSymbol("walk_to", ImmutableList.of(VectorDataType.TYPE), NothingDataType.TYPE, (ev, robo, args) -> {
                robo.addWalkToGoalProp(new Vec3(((VectorDataType) args[0]).getVector()));
                return new NothingDataType();
            }, Component.translatable("functions.redbyte.description.walk_to")))
            .add(new FunctionSymbol("jump", ImmutableList.of(), NothingDataType.TYPE, (ev, robo, args) -> {
                robo.getEntity().jumpFromGround();
                return new NothingDataType();
            }, Component.translatable("functions.redbyte.description.jump")))
            .add(new FunctionSymbol("position", ImmutableList.of(EntityDataType.TYPE), VectorDataType.TYPE, (ev, robo, args) -> {
                EntityDataType<?> entity = (EntityDataType<?>) args[0];
                return new VectorDataType(entity.getEntity().position());
            }, Component.translatable("functions.redbyte.description.position")))
            .add(new FunctionSymbol("x", ImmutableList.of(VectorDataType.TYPE), TypeSymbol.Number,
                    (ev, robo, args) -> ((VectorDataType) args[0]).getVector().x, Component.translatable("functions.redbyte.description.x")))
            .add(new FunctionSymbol("y", ImmutableList.of(VectorDataType.TYPE), TypeSymbol.Number,
                    (ev, robo, args) -> ((VectorDataType) args[0]).getVector().y, Component.translatable("functions.redbyte.description.y")))
            .add(new FunctionSymbol("z", ImmutableList.of(VectorDataType.TYPE), TypeSymbol.Number,
                    (ev, robo, args) -> ((VectorDataType) args[0]).getVector().z, Component.translatable("functions.redbyte.description.z")))
            .add(new FunctionSymbol("distance", ImmutableList.of(VectorDataType.TYPE, VectorDataType.TYPE), TypeSymbol.Number,
                    (ev, robo, args) -> ((VectorDataType) args[0]).getVector().distance(((VectorDataType) args[1]).getVector()),
                    Component.translatable("functions.redbyte.description.distance")))
            .add(new FunctionSymbol("follow", ImmutableList.of(EntityDataType.TYPE), NothingDataType.TYPE, (ev, robo, args) -> {
                CreatureDataType<?> entity = (CreatureDataType<?>) args[0];
                robo.addFollowEntityGoalProp(entity.getEntity());
                return new NothingDataType();
            }, Component.translatable("functions.redbyte.description.follow")))
            .add(new FunctionSymbol("stop_follow", ImmutableList.of(), NothingDataType.TYPE, (ev, robo, args) -> {
                robo.popFollowEntityGoalProp();
                return new NothingDataType();
            }, Component.translatable("functions.redbyte.description.stop_follow")))
            .add(new FunctionSymbol("try_attack", ImmutableList.of(CreatureDataType.TYPE), NothingDataType.TYPE, (ev, robo, args) -> {
                CreatureDataType<?> entity = (CreatureDataType<?>) args[0];
                robo.addAttackGoalProp(entity.getEntity());
                return new NothingDataType();
            }, Component.translatable("functions.redbyte.description.try_attack")))
            .add(new FunctionSymbol("can_attack", ImmutableList.of(CreatureDataType.TYPE), TypeSymbol.Boolean, (ev, robo, args) -> {
                CreatureDataType<?> entity = (CreatureDataType<?>) args[0];
                if (entity.isNull()) return false;
                RoboEntity roboEntity = robo.getEntity();
                return roboEntity.canAttack(entity.getEntity()) && roboEntity.isInRange(entity.getEntity()) && roboEntity.hasLineOfSight(entity.getEntity());
            }, Component.translatable("functions.redbyte.description.can_attack")))
            .add(new FunctionSymbol("health", ImmutableList.of(CreatureDataType.TYPE), TypeSymbol.Number,
                    (ev, robo, args) -> ((CreatureDataType<?>) args[0]).getEntity().getHealth(),
                    Component.translatable("functions.redbyte.description.health")))
            .add(new FunctionSymbol("get_player", ImmutableList.of(TypeSymbol.Text), PlayerDataType.TYPE,
                    (ev, robo, args) -> {
                        List<ServerPlayer> players = robo.getServerLevel().getPlayers(p -> p.getName().getString().equals(args[0]));
                        return players.isEmpty() ? new NothingDataType() : new PlayerDataType(players.getFirst());
                    }, Component.translatable("functions.redbyte.description.get_player"))
            )
            .add(new FunctionSymbol("get_nearest_player", ImmutableList.of(TypeSymbol.Number), PlayerDataType.TYPE,
                    (ev, robo, args) -> Objects.requireNonNullElseGet(robo.getEntity().level().getNearestPlayer(robo.getEntity(), (float) args[0]), NothingDataType::new),
                    Component.translatable("functions.redbyte.description.get_nearest_player")))
            .add(new FunctionSymbol("get_nearest_monster", ImmutableList.of(TypeSymbol.Number), MonsterDataType.TYPE,
                    (ev, robo, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        Vec3 pos = roboEntity.position();
                        AABB searchArea = robo.getEntity().getBoundingBox().inflate((float) args[0]);
                        Monster monster = robo.getServerLevel().getNearestEntity(Monster.class, TargetingConditions.forCombat(), roboEntity, pos.x, pos.y, pos.z, searchArea);
                        return monster == null ? new NothingDataType() : new MonsterDataType(monster);
                    }, Component.translatable("functions.redbyte.description.get_nearest_monster")))
            .add(new FunctionSymbol("try_destroy", ImmutableList.of(VectorDataType.TYPE), NothingDataType.TYPE,
                    (ev, robo, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        VectorDataType vec = (VectorDataType) args[0];
                        BlockPos blockPos = BlockPos.containing(new Vec3(vec.getVector()));
                        if (!roboEntity.level().isEmptyBlock(blockPos))
                            robo.addDestroyBlockGoalProp(blockPos);
                        return new NothingDataType();
                    }, Component.translatable("functions.redbyte.description.try_destroy")))
            .add(new FunctionSymbol("try_destroy", ImmutableList.of(TypeSymbol.Number, TypeSymbol.Number, TypeSymbol.Number), NothingDataType.TYPE,
                    (ev, robo, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        BlockPos blockPos = BlockPos.containing((float) args[0], (float) args[1], (float) args[2]);
                        if (!roboEntity.level().isEmptyBlock(blockPos))
                            robo.addDestroyBlockGoalProp(blockPos);
                        return new NothingDataType();
                    }, Component.translatable("functions.redbyte.description.try_destroy")))
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
