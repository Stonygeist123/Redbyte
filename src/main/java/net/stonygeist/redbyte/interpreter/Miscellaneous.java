package net.stonygeist.redbyte.interpreter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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

        return new AbstractMap.SimpleEntry<>(null, true);
    }

    public static final ImmutableList<FunctionSymbol> functions = new ImmutableList.Builder<FunctionSymbol>()
            .add(new FunctionSymbol("print", ImmutableList.of(TypeSymbol.Text), NothingDataType.TYPE,
                    (ev, robo, args) -> {
                        robo.getEntity().addPrintOutput(args[0]);
                        return new NothingDataType();
                    }))
            .add(new FunctionSymbol("print", ImmutableList.of(TypeSymbol.Number), NothingDataType.TYPE,
                    (ev, robo, args) -> {
                        robo.getEntity().addPrintOutput(args[0]);
                        return new NothingDataType();
                    }))
            .add(new FunctionSymbol("walk", ImmutableList.of(TypeSymbol.Number), TypeSymbol.Void, (ev, robo, args) -> {
                robo.setWalkGoalProp((float) args[0]);
                return new NothingDataType();
            }))
            .add(new FunctionSymbol("walkto", ImmutableList.of(TypeSymbol.Number, TypeSymbol.Number, TypeSymbol.Number), TypeSymbol.Void, (ev, robo, args) -> {
                robo.setWalkToGoalProp(new Vec3((float) args[0], (float) args[1], (float) args[2]));
                return new NothingDataType();
            }))
            .add(new FunctionSymbol("jump", ImmutableList.of(), TypeSymbol.Void, (ev, robo, args) -> {
                robo.getEntity().jumpFromGround();
                return new NothingDataType();
            }))
            .add(new FunctionSymbol("position", ImmutableList.of(EntityDataType.TYPE), VectorDataType.TYPE, (ev, robo, args) -> {
                EntityDataType<?> entity = (EntityDataType<?>) args[0];
                return new VectorDataType(entity.getEntity().position());
            }))
            .add(new FunctionSymbol("x", ImmutableList.of(VectorDataType.TYPE), TypeSymbol.Number,
                    (ev, robo, args) -> ((VectorDataType) args[0]).getVector().x))
            .add(new FunctionSymbol("y", ImmutableList.of(VectorDataType.TYPE), TypeSymbol.Number,
                    (ev, robo, args) -> ((VectorDataType) args[0]).getVector().y))
            .add(new FunctionSymbol("z", ImmutableList.of(VectorDataType.TYPE), TypeSymbol.Number,
                    (ev, robo, args) -> ((VectorDataType) args[0]).getVector().z))
            .add(new FunctionSymbol("follow", ImmutableList.of(EntityDataType.TYPE), TypeSymbol.Void, (ev, robo, args) -> {
                CreatureDataType<?> entity = (CreatureDataType<?>) args[0];
                robo.setFollowEntityGoalProp(entity.getEntity());
                return new NothingDataType();
            }))
            .add(new FunctionSymbol("stop_follow", ImmutableList.of(), TypeSymbol.Void, (ev, robo, args) -> {
                robo.setFollowEntityGoalProp(null);
                return new NothingDataType();
            }))
            .add(new FunctionSymbol("attack", ImmutableList.of(EntityDataType.TYPE), TypeSymbol.Void, (ev, robo, args) -> {
                CreatureDataType<?> entity = (CreatureDataType<?>) args[0];
                if (entity.isNull())
                    return new NothingDataType();

                RoboEntity roboEntity = robo.getEntity();
                if (roboEntity.canAttack(entity.getEntity()) && roboEntity.isInRange(entity.getEntity()) && roboEntity.hasLineOfSight(entity.getEntity()))
                    roboEntity.doHurtTarget(entity.getEntity());
                return new NothingDataType();
            }))
            .add(new FunctionSymbol("can_attack", ImmutableList.of(EntityDataType.TYPE), TypeSymbol.Boolean, (ev, robo, args) -> {
                CreatureDataType<?> entity = (CreatureDataType<?>) args[0];
                if (entity.isNull()) return false;
                RoboEntity roboEntity = robo.getEntity();
                return roboEntity.canAttack(entity.getEntity()) && roboEntity.isInRange(entity.getEntity()) && roboEntity.hasLineOfSight(entity.getEntity());
            }))
            .add(new FunctionSymbol("health", ImmutableList.of(), TypeSymbol.Number, (ev, robo, args) -> robo.getEntity().getHealth()))
            .add(new FunctionSymbol("player", ImmutableList.of(TypeSymbol.Text), PlayerDataType.TYPE,
                    (ev, robo, args) -> {
                        List<ServerPlayer> players = robo.getServerLevel().getPlayers(p -> p.getName().getString().equals(args[0]));
                        return players.isEmpty() ? new NothingDataType() : new PlayerDataType(players.getFirst());
                    })
            )
            .add(new FunctionSymbol("nearest_player", ImmutableList.of(TypeSymbol.Number), PlayerDataType.TYPE,
                    (ev, robo, args) -> Objects.requireNonNullElseGet(robo.getEntity().level().getNearestPlayer(robo.getEntity(), (float) args[0]), NothingDataType::new)))
            .add(new FunctionSymbol("nearest_monster", ImmutableList.of(TypeSymbol.Number), MonsterDataType.TYPE,
                    (ev, robo, args) -> {
                        RoboEntity roboEntity = robo.getEntity();
                        Vec3 pos = roboEntity.position();
                        AABB searchArea = robo.getEntity().getBoundingBox().inflate((float) args[0]);
                        return new MonsterDataType(robo.getServerLevel().getNearestEntity(Monster.class, TargetingConditions.forCombat(), roboEntity, pos.x, pos.y, pos.z, searchArea));
                    }))
            .build();

    public static final ImmutableMap<String, TokenKind> keywords = new ImmutableMap.Builder<String, TokenKind>()
            .put("if", TokenKind.If)
            .put("else", TokenKind.Else)
            .put("loop", TokenKind.Loop)
            .put("while", TokenKind.While)
            .put("once", TokenKind.Once)
            .put("or", TokenKind.Or)
            .put("and", TokenKind.And)
            .put("robo", TokenKind.Robo)
            .build();
}
