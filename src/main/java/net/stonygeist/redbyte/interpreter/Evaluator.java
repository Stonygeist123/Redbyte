package net.stonygeist.redbyte.interpreter;

import com.google.common.collect.ImmutableList;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.redbyte.interpreter.binder.expr.*;
import net.stonygeist.redbyte.interpreter.binder.stmt.*;
import net.stonygeist.redbyte.interpreter.symbols.FunctionSymbol;
import net.stonygeist.redbyte.interpreter.symbols.LabelSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;
import net.stonygeist.redbyte.manager.PseudoRobo;
import org.jetbrains.annotations.Nullable;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Stack;

public final class Evaluator {
    private final Stack<BoundBlockStmt> globalStmts = new Stack<>();
    private final Dictionary<VariableSymbol, Object> variables = new Hashtable<>();
    private final PseudoRobo robo;
    private Dictionary<LabelSymbol, Integer> labelToIndex;
    private int index;

    public Evaluator(ImmutableList<BoundBlockStmt> globalStmts, PseudoRobo robo) {
        this.robo = robo;
        this.globalStmts.addAll(globalStmts.reverse());
    }

    public void tick(PseudoRobo robo) {
        if (globalStmts.isEmpty())
            return;

        BoundBlockStmt current = globalStmts.peek();
        evaluate(current, robo);
        if (index >= current.stmts.size()) {
            index = 0;
            labelToIndex = null;
            globalStmts.pop();
        }
    }

    public boolean getFinished() {
        return globalStmts.isEmpty();
    }

    public RoboEntity getRoboEntity() {
        return robo.getEntity();
    }

    private void evaluate(BoundBlockStmt stmt, PseudoRobo robo) {
        labelToIndex = new Hashtable<>();
        for (int i = 0; i < stmt.stmts.size(); ++i)
            if (stmt.stmts.get(i) instanceof BoundLabelStmt l)
                labelToIndex.put(l.label, i + 1);

        switch (stmt.stmts.get(index)) {
            case BoundExprStmt exprStmt:
                ++index;
                evaluateExpr(exprStmt.expr, robo);
                break;
            case BoundLabelStmt ignored:
                ++index;
                break;
            case BoundGotoStmt gotoStmt:
                index = labelToIndex.get(gotoStmt.label);
                break;
            case BoundConditionalGotoStmt conditionalGotoStmt:
                Object condition = evaluateExpr(conditionalGotoStmt.condition, robo);
                if (condition == null)
                    throw new RuntimeException();

                if ((boolean) condition == conditionalGotoStmt.jumpIfTrue)
                    index = labelToIndex.get(conditionalGotoStmt.label);
                else
                    ++index;
                break;
            default:
                throw new RuntimeException();
        }
    }

    private @Nullable Object evaluateExpr(BoundExpr expr, PseudoRobo robo) {
        return switch (expr) {
            case BoundLiteralExpr literalExpr -> literalExpr.value;
            case BoundUnaryExpr unaryExpr -> {
                Object operand = evaluateExpr(unaryExpr.operand, robo);
                if (operand == null)
                    throw new RuntimeException();
                if (unaryExpr.operator.operatorKind() == TokenKind.Bang)
                    yield !(boolean) operand;
                else if (unaryExpr.operator.operatorKind() == TokenKind.Minus)
                    yield -(float) operand;
                else if (unaryExpr.operator.operatorKind() == TokenKind.Plus)
                    yield -(float) operand;
                throw new RuntimeException();
            }
            case BoundBinaryExpr binaryExpr -> {
                Object left = evaluateExpr(binaryExpr.left, robo);
                Object right = evaluateExpr(binaryExpr.right, robo);
                if (left == null || right == null)
                    throw new RuntimeException();

                switch (binaryExpr.operator.operatorKind()) {
                    case Plus: {
                        if (left instanceof Float f1 && right instanceof Float f2)
                            yield f1 + f2;
                        else if (left instanceof String s1 && right instanceof String s2)
                            yield s1 + s2;
                    }
                    case Minus: {
                        yield (float) left - (float) right;
                    }
                    case Star:
                        yield (float) left * (float) right;
                    case Slash: {
                        yield (float) left / (float) right;
                    }
                    case EqualsEquals: {
                        yield equalsPrimitives(left, right);
                    }
                    case NotEquals: {
                        yield !equalsPrimitives(left, right);
                    }
                    case Greater: {
                        yield (float) left > (float) right;
                    }
                    case GreaterEquals: {
                        yield (float) left >= (float) right;
                    }
                    case Less: {
                        yield (float) left < (float) right;
                    }
                    case LessEquals: {
                        yield (float) left <= (float) right;
                    }
                    case And: {
                        yield (boolean) left && (boolean) right;
                    }
                    case Or: {
                        yield (boolean) left || (boolean) right;
                    }
                    default:
                        throw new RuntimeException();
                }
            }
            case BoundGroupExpr groupExpr -> evaluateExpr(groupExpr.expr, robo);
            case BoundNameExpr nameExpr -> {
                Object value = variables.get(nameExpr.symbol);
                if (value == null)
                    throw new RuntimeException();
                yield value;
            }
            case BoundAssignExpr assignExpr -> {
                Object value = evaluateExpr(assignExpr.value, robo);
                variables.put(assignExpr.symbol, value);
                yield value;
            }
            case BoundCallExpr callExpr -> {
                Object[] args = callExpr.args.stream().map(a -> evaluateExpr(a, robo)).toArray(Object[]::new);
                FunctionSymbol function = callExpr.symbol;
                yield function.callback.apply(this, robo, args);
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
            case Boolean bo1 when b instanceof Boolean bo2 -> bo1 == bo2;
            case String c1 when b instanceof String c2 -> c1.equals(c2);
            default -> throw new RuntimeException();
        };
    }
}
