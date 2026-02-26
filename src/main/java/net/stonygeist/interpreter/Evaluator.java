package net.stonygeist.interpreter;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.stonygeist.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.interpreter.analysis.nodes.expr.*;
import net.stonygeist.interpreter.analysis.nodes.stmt.*;
import net.stonygeist.interpreter.miscellaneous.Config;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.server.C2SFunctionsPaket;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.UUID;

public final class Evaluator {
    private final Stmt[] stmts;
    private final UUID redbyteID;
    private final Dictionary<String, Float> variables = new Hashtable<>();

    public Evaluator(Stmt[] stmts, UUID redbyteID) {
        this.stmts = stmts;
        this.redbyteID = redbyteID;
    }

    public void run() {
        for (Stmt stmt : stmts) {
            evaluateStmt(stmt);
        }
    }

    private void evaluateStmt(Stmt stmt) {
        switch (stmt) {
            case ExprStmt exprStmt:
                evaluateExpr(exprStmt.expr);
                break;
            case BlockStmt blockStmt:
                Arrays.stream(blockStmt.stmts).forEach(this::evaluateStmt);
                break;
            case IfStmt ifStmt:
                Boolean condition = evaluateExpr(ifStmt.condition, Boolean.class);
                if (condition)
                    evaluateStmt(ifStmt.thenStmt);
                else if (ifStmt.elseStmt != null)
                    evaluateStmt(ifStmt.elseStmt);
                break;
            case LoopStmt loopStmt:
                Float count = evaluateExpr(loopStmt.count, Float.class);
                for (int i = 0; i < count; ++i)
                    evaluateStmt(loopStmt.stmt);
                break;
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

    private Object evaluateExpr(Expr expr) {
        return switch (expr) {
            case LiteralExpr literalExpr -> {
                if (literalExpr.token.kind == TokenKind.Number)
                    yield Float.valueOf(literalExpr.token.lexeme);
                throw new RuntimeException();
            }
            case UnaryExpr unaryExpr -> -evaluateExpr(unaryExpr.operand, Float.class);
            case BinaryExpr binaryExpr -> {
                Float left = evaluateExpr(binaryExpr.left, Float.class);
                Float right = evaluateExpr(binaryExpr.right, Float.class);
                switch (binaryExpr.op.kind) {
                    case Plus:
                        yield left + right;
                    case Minus:
                        yield left - right;
                    case Star:
                        yield left * right;
                    case Slash:
                        yield left / right;
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
                    default -> throw new RuntimeException();
                }

                yield 0f;
            }
            default -> throw new RuntimeException();
        };
    }
}
