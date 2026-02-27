package net.stonygeist.interpreter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.stonygeist.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.interpreter.analysis.nodes.expr.*;
import net.stonygeist.interpreter.analysis.nodes.stmt.*;
import net.stonygeist.interpreter.miscellaneous.Config;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.server.C2SFunctionsPaket;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// TODO: Make the Evaluator run tick-wise; not all at once! Fixes client being stuck during loops and adds more possibilities for states

public final class Evaluator {
    private final Stmt[] stmts;
    private final RoboEntity roboEntity;
    private final UUID redbyteID;
    private final Dictionary<String, Float> variables = new Hashtable<>();

    public Evaluator(Stmt[] stmts, RoboEntity roboEntity) {
        this.stmts = stmts;
        this.roboEntity = roboEntity;
        redbyteID = roboEntity.getRedbyteID();
    }

    public void run() {
        for (Stmt stmt : stmts)
            evaluateStmt(stmt);
    }

    private void evaluateStmt(Stmt stmt) {
        switch (stmt) {
            case ExprStmt exprStmt:
                evaluateExpr(exprStmt.expr);
                break;
            case BlockStmt blockStmt:
                Arrays.stream(blockStmt.stmts).forEach(this::evaluateStmt);
                break;
            case IfStmt ifStmt: {
                Boolean condition = evaluateExpr(ifStmt.condition, Boolean.class);
                if (condition)
                    evaluateStmt(ifStmt.thenStmt);
                else if (ifStmt.elseStmt != null)
                    evaluateStmt(ifStmt.elseStmt);
                break;
            }
            case LoopStmt loopStmt: {
                Float count = evaluateExpr(loopStmt.count, Float.class);
                for (int i = 0; i < count; ++i)
                    evaluateStmt(loopStmt.stmt);
                break;
            }
            case WhileStmt whileStmt: {
                Boolean condition = evaluateExpr(whileStmt.condition, Boolean.class);
                while (condition) {
                    condition = evaluateExpr(whileStmt.condition, Boolean.class);
                    evaluateStmt(whileStmt.stmt);
                }
                break;
            }
            case TillStmt tillStmt: {
                Boolean condition;
                do {
                    condition = evaluateExpr(tillStmt.condition, Boolean.class);
                    evaluateStmt(tillStmt.stmt);
                } while (!condition);
                break;
            }
            default:
                throw new RuntimeException();
        }
    }

    private <T> T evaluateExpr(Expr expr, Class<T> classType) {
        Object value = evaluateExpr(expr);
        if (classType.isInstance(value))
            return classType.cast(value);
        throw new RuntimeException();
    }

    private @Nullable Object evaluateExpr(Expr expr) {
        return switch (expr) {
            case LiteralExpr literalExpr -> {
                if (literalExpr.token.kind == TokenKind.Number)
                    yield Float.valueOf(literalExpr.token.lexeme);
                else if (literalExpr.token.kind == TokenKind.String)
                    yield literalExpr.token.lexeme.substring(1, literalExpr.token.lexeme.length() - 1);
                throw new RuntimeException();
            }
            case UnaryExpr unaryExpr -> {
                if (unaryExpr.op.kind == TokenKind.Bang)
                    yield !evaluateExpr(unaryExpr.operand, Boolean.class);
                else
                    yield -evaluateExpr(unaryExpr.operand, Float.class);
            }
            case BinaryExpr binaryExpr -> {
                switch (binaryExpr.op.kind) {
                    case Plus: {
                        Float left = evaluateExpr(binaryExpr.left, Float.class);
                        Float right = evaluateExpr(binaryExpr.right, Float.class);
                        yield left + right;
                    }
                    case Minus: {
                        Float left = evaluateExpr(binaryExpr.left, Float.class);
                        Float right = evaluateExpr(binaryExpr.right, Float.class);
                        yield left - right;
                    }
                    case Star: {
                        Float left = evaluateExpr(binaryExpr.left, Float.class);
                        Float right = evaluateExpr(binaryExpr.right, Float.class);
                        yield left * right;
                    }
                    case Slash: {
                        Float left = evaluateExpr(binaryExpr.left, Float.class);
                        Float right = evaluateExpr(binaryExpr.right, Float.class);
                        yield left / right;
                    }
                    case EqualsEquals: {
                        Object left = evaluateExpr(binaryExpr.left);
                        Object right = evaluateExpr(binaryExpr.right);
                        yield equalsPrimitives(left, right);
                    }
                    case NotEquals: {
                        Object left = evaluateExpr(binaryExpr.left);
                        Object right = evaluateExpr(binaryExpr.right);
                        yield !equalsPrimitives(left, right);
                    }
                    case Greater: {
                        Float left = evaluateExpr(binaryExpr.left, Float.class);
                        Float right = evaluateExpr(binaryExpr.right, Float.class);
                        yield left > right;
                    }
                    case GreaterEquals: {
                        Float left = evaluateExpr(binaryExpr.left, Float.class);
                        Float right = evaluateExpr(binaryExpr.right, Float.class);
                        yield left >= right;
                    }
                    case Less: {
                        Float left = evaluateExpr(binaryExpr.left, Float.class);
                        Float right = evaluateExpr(binaryExpr.right, Float.class);
                        yield left < right;
                    }
                    case LessEquals: {
                        Float left = evaluateExpr(binaryExpr.left, Float.class);
                        Float right = evaluateExpr(binaryExpr.right, Float.class);
                        yield left <= right;
                    }
                    case And: {
                        Boolean left = evaluateExpr(binaryExpr.left, Boolean.class);
                        Boolean right = evaluateExpr(binaryExpr.right, Boolean.class);
                        yield left && right;
                    }
                    case Or: {
                        Boolean left = evaluateExpr(binaryExpr.left, Boolean.class);
                        Boolean right = evaluateExpr(binaryExpr.right, Boolean.class);
                        yield left || right;
                    }
                    default:
                        throw new RuntimeException();
                }
            }
            case GroupExpr groupExpr -> evaluateExpr(groupExpr.expr);
            case NameExpr nameExpr -> {
                Float value = variables.get(nameExpr.name.lexeme.toLowerCase());
                if (value == null)
                    throw new RuntimeException();
                yield value;
            }
            case AssignExpr assignExpr -> {
                Float value = evaluateExpr(assignExpr.value, Float.class);
                variables.put(assignExpr.name.lexeme.toLowerCase(), value);
                yield value;
            }
            case CallExpr callExpr -> {
                Object[] args = Arrays.stream(callExpr.args).map(this::evaluateExpr).toArray(Object[]::new);
                Config.Function function = Config.getFunction(callExpr.name.lexeme.toLowerCase());
                if (function == null || function.parameterCount() != args.length)
                    throw new RuntimeException();

                for (int i = 0; i < args.length; ++i)
                    if (!function.parameterTypes()[i].isInstance(args[i]))
                        throw new RuntimeException();

                switch (callExpr.name.lexeme.toLowerCase()) {
                    case "walk" ->
                            Redbyte.CHANNEL.send(new C2SFunctionsPaket.WalkFunction(redbyteID, (Float) args[0]), PacketDistributor.SERVER.noArg());
                    case "walkto" ->
                            Redbyte.CHANNEL.send(new C2SFunctionsPaket.WalkToFunction(redbyteID, new Vec3((Float) args[0], (Float) args[1], (Float) args[2])), PacketDistributor.SERVER.noArg());
                    case "jump" ->
                            Redbyte.CHANNEL.send(new C2SFunctionsPaket.JumpFunction(redbyteID), PacketDistributor.SERVER.noArg());
                    case "position_x" -> {
                        Vec3 position = roboEntity.position();
                        yield (float) position.x;
                    }
                    case "position_y" -> {
                        Vec3 position = roboEntity.position();
                        yield (float) position.y;
                    }
                    case "position_z" -> {
                        Vec3 position = roboEntity.position();
                        yield (float) position.z;
                    }
                    case "follow" ->
                            Redbyte.CHANNEL.send(new C2SFunctionsPaket.FollowFunction(redbyteID, (String) args[0]), PacketDistributor.SERVER.noArg());
                    case "stop_follow" ->
                            Redbyte.CHANNEL.send(new C2SFunctionsPaket.StopFollowFunction(redbyteID), PacketDistributor.SERVER.noArg());
                    case "attack" ->
                            Redbyte.CHANNEL.send(new C2SFunctionsPaket.AttackFunction(redbyteID, (String) args[0]), PacketDistributor.SERVER.noArg());
                    case "can_attack" -> {
                        String address = (String) args[0];
                        List<? extends Player> players = roboEntity.level().players().stream().filter(p -> p.getName().getString().equals(address)).toList();
                        yield !players.isEmpty() && roboEntity.canAttack(players.getFirst());
                    }
                    default -> throw new RuntimeException();
                }

                yield 0f;
            }
            default -> throw new RuntimeException();
        };
    }

    public static boolean equalsPrimitives(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (!a.getClass().equals(b.getClass()))
            throw new RuntimeException();

        return switch (a) {
            case Float f1 when b instanceof Float f2 -> Float.compare(f1, f2) == 0;
            case Double d1 when b instanceof Double d2 -> Double.compare(d1, d2) == 0;
            case Integer n1 when b instanceof Integer n2 -> n1.equals(n2);
            case Boolean bo1 when b instanceof Boolean bo2 -> bo1 == bo2;
            case Character c1 when b instanceof Character c2 -> c1 == c2;
            case String c1 when b instanceof String c2 -> c1.equals(c2);
            default -> throw new RuntimeException();
        };

    }
}
