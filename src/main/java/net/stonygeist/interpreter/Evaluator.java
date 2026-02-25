package net.stonygeist.interpreter;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.stonygeist.interpreter.analysis.nodes.*;
import net.stonygeist.interpreter.miscellaneous.Config;
import net.stonygeist.redbyte.Redbyte;
import net.stonygeist.redbyte.server.C2SFunctionsPaket;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.UUID;

public final class Evaluator {
    private final Expr[] exprs;
    private final UUID redbyteID;
    private final Dictionary<String, Float> variables = new Hashtable<>();

    public Evaluator(Expr[] exprs, UUID redbyteID) {
        this.exprs = exprs;
        this.redbyteID = redbyteID;
    }

    public void run() {
        for (Expr expr : exprs) {
            evaluate(expr);
        }
    }

    private Float evaluate(Expr expr) {
        return switch (expr) {
            case LiteralExpr literalExpr -> Float.valueOf(literalExpr.token.lexeme);
            case UnaryExpr unaryExpr -> -evaluate(unaryExpr.operand);
            case BinaryExpr binaryExpr -> {
                Float left = evaluate(binaryExpr.left);
                Float right = evaluate(binaryExpr.right);
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
            case GroupExpr groupExpr -> evaluate(groupExpr.expr);
            case NameExpr nameExpr -> {
                Float value = variables.get(nameExpr.name.lexeme.toLowerCase());
                if (value == null)
                    throw new RuntimeException();
                yield value;
            }
            case AssignExpr assignExpr -> {
                Float value = evaluate(assignExpr.value);
                variables.put(assignExpr.name.lexeme.toLowerCase(), value);
                yield value;
            }
            case CallExpr callExpr -> {
                Float[] args = Arrays.stream(callExpr.args).map(this::evaluate).toArray(Float[]::new);
                Integer parameterCount = Config.functions.get(callExpr.name.lexeme.toLowerCase());
                if (parameterCount == null || parameterCount != args.length)
                    throw new RuntimeException();
                switch (callExpr.name.lexeme.toLowerCase()) {
                    case "walk" ->
                            Redbyte.CHANNEL.send(new C2SFunctionsPaket.WalkFunction(redbyteID, args[0]), PacketDistributor.SERVER.noArg());
                    case "walkto" ->
                            Redbyte.CHANNEL.send(new C2SFunctionsPaket.WalkToFunction(redbyteID, new Vec3(args[0], args[1], args[2])), PacketDistributor.SERVER.noArg());
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
