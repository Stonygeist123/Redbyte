package net.stonygeist.redbyte.interpreter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.redbyte.interpreter.data_types.EntityDataType;
import net.stonygeist.redbyte.interpreter.data_types.MonsterDataType;
import net.stonygeist.redbyte.interpreter.data_types.PlayerDataType;
import net.stonygeist.redbyte.interpreter.data_types.VectorDataType;
import net.stonygeist.redbyte.interpreter.symbols.FunctionSymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;

import javax.annotation.Nullable;

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

    @Nullable
    public static FunctionSymbol getFunction(String name) {
        return functions.stream().filter(f -> f.name.toLowerCase().equals(name)).findFirst().orElse(null);
    }

    public static final ImmutableList<FunctionSymbol> functions = new ImmutableList.Builder<FunctionSymbol>()
            .add(new FunctionSymbol("walk", ImmutableList.of(TypeSymbol.Number), TypeSymbol.Void, (ev, robo, args) -> {
                if (robo.getWalkGoalProp() == null)
                    robo.setWalkGoalProp((float) args[0]);
                else
                    throw new Evaluator.PendingEvaluation();
                return null;
            }))
            .add(new FunctionSymbol("walkto", ImmutableList.of(TypeSymbol.Number, TypeSymbol.Number, TypeSymbol.Number), TypeSymbol.Void, (ev, robo, args) -> {
                if (robo.getWalkToGoalProp() == null)
                    robo.setWalkToGoalProp(new Vec3((float) args[0], (float) args[1], (float) args[2]));
                else
                    throw new Evaluator.PendingEvaluation();
                return null;
            }))
            .add(new FunctionSymbol("jump", ImmutableList.of(), TypeSymbol.Void, (ev, robo, args) -> {
                robo.getEntity().jumpFromGround();
                return null;
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
                EntityDataType<?> entity = (EntityDataType<?>) args[0];
                robo.setFollowEntityGoalProp(entity.getEntity());
                return null;
            }))
            .add(new FunctionSymbol("stop_follow", ImmutableList.of(), TypeSymbol.Void, (ev, robo, args) -> {
                robo.setFollowEntityGoalProp(null);
                return null;
            }))
            .add(new FunctionSymbol("attack", ImmutableList.of(EntityDataType.TYPE), TypeSymbol.Void, (ev, robo, args) -> {
                EntityDataType<?> entity = (EntityDataType<?>) args[0];
                if (entity.isNull())
                    return null;

                RoboEntity roboEntity = robo.getEntity();
                if (roboEntity.canAttack(entity.getEntity()) && roboEntity.isInRange(entity.getEntity()) && roboEntity.hasLineOfSight(entity.getEntity()))
                    roboEntity.doHurtTarget(entity.getEntity());
                return null;
            }))
            .add(new FunctionSymbol("can_attack", ImmutableList.of(EntityDataType.TYPE), TypeSymbol.Boolean, (ev, robo, args) -> {
                EntityDataType<?> entity = (EntityDataType<?>) args[0];
                if (entity.isNull()) return false;
                RoboEntity roboEntity = robo.getEntity();
                return roboEntity.canAttack(entity.getEntity()) && roboEntity.isInRange(entity.getEntity()) && roboEntity.hasLineOfSight(entity.getEntity());
            }))
            .add(new FunctionSymbol("health", ImmutableList.of(), TypeSymbol.Number, (ev, robo, args) -> robo.getEntity().getHealth()))
            .add(new FunctionSymbol("player", ImmutableList.of(TypeSymbol.Text), PlayerDataType.TYPE,
                    (ev, robo, args) -> new PlayerDataType(robo.getServerLevel().getPlayers(p -> p.getName().getString().equals(args[0])).getFirst()))
            )
            .add(new FunctionSymbol("nearest_player", ImmutableList.of(TypeSymbol.Number), PlayerDataType.TYPE,
                    (ev, robo, args) -> robo.getEntity().level().getNearestPlayer(robo.getEntity(), (float) args[0])))
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
