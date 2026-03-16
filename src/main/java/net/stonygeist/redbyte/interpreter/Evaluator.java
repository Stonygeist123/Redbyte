package net.stonygeist.redbyte.interpreter;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.entity.robo.RoboEntity;
import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.redbyte.interpreter.binder.expr.*;
import net.stonygeist.redbyte.interpreter.binder.stmt.*;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.data_types.NothingDataType;
import net.stonygeist.redbyte.interpreter.data_types.RoboDataType;
import net.stonygeist.redbyte.interpreter.diagnostics.Diagnostic;
import net.stonygeist.redbyte.interpreter.symbols.FunctionSymbol;
import net.stonygeist.redbyte.interpreter.symbols.LabelSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;
import net.stonygeist.redbyte.manager.PseudoRobo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Stack;

public final class Evaluator {
    private final Stack<BoundBlockStmt> globalStmts = new Stack<>();
    private final Dictionary<VariableSymbol, Object> variables = new Hashtable<>();
    private final PseudoRobo robo;
    private int index;
    private Dictionary<LabelSymbol, Integer> labelToIndex;

    public Evaluator(ImmutableList<BoundBlockStmt> globalStmts, PseudoRobo robo) {
        this.robo = robo;
        this.globalStmts.addAll(globalStmts.reverse());
    }

    public @Nullable EvaluationError tick(PseudoRobo robo) {
        try {
            if (!globalStmts.isEmpty()) {
                BoundBlockStmt current = globalStmts.peek();
                evaluate(current, robo);
                if (index >= current.stmts().size()) {
                    index = 0;
                    labelToIndex = null;
                    globalStmts.pop();
                }
            }
        } catch (Evaluator.EvaluationError error) {
            return error;
        }

        return null;
    }

    public boolean getFinished() {
        return globalStmts.isEmpty();
    }

    public RoboEntity getRoboEntity() {
        return robo.getEntity();
    }

    private void evaluate(BoundBlockStmt blockStmt, PseudoRobo robo) {
        labelToIndex = new Hashtable<>();
        for (int i = 0; i < blockStmt.stmts().size(); ++i)
            if (blockStmt.stmts().get(i) instanceof BoundLabelStmt(LabelSymbol label))
                labelToIndex.put(label, i + 1);

        int remainingSteps = blockStmt.stmts().size() + 1;
        while (remainingSteps-- > 0 && index < blockStmt.stmts().size()) {
            BoundStmt stmt = blockStmt.stmts().get(index);
            switch (stmt) {
                case BoundExprStmt exprStmt:
                    evaluateExpr(exprStmt.expr(), robo);
                    ++index;
                    return;
                case BoundLabelStmt ignored:
                    ++index;
                    break;
                case BoundGotoStmt gotoStmt:
                    index = labelToIndex.get(gotoStmt.label());
                    break;
                case BoundConditionalGotoStmt conditionalGotoStmt:
                    Object condition = evaluateExpr(conditionalGotoStmt.condition(), robo);
                    if (condition instanceof NothingDataType)
                        throw new EvaluationError(Component.translatable("runtime.redbyte.error.value_not_existing"), conditionalGotoStmt.condition().span());

                    if ((boolean) condition == conditionalGotoStmt.jumpIfTrue())
                        index = labelToIndex.get(conditionalGotoStmt.label());
                    else
                        ++index;
                    break;
                default:
                    throw new RuntimeException();
            }
        }
    }

//    private void evaluate(BoundBlockStmt blockStmt, PseudoRobo robo) {
//        labelToIndex = new Hashtable<>();
//        for (int i = 0; i < blockStmt.stmts().size(); ++i)
//            if (blockStmt.stmts().get(i) instanceof BoundLabelStmt(LabelSymbol label))
//                labelToIndex.put(label, i + 1);
//
//        BoundStmt stmt = blockStmt.stmts().get(index);
//        switch (stmt) {
//            case BoundExprStmt exprStmt:
//                evaluateExpr(exprStmt.expr(), robo);
//                ++index;
//                break;
//            case BoundLabelStmt ignored:
//                ++index;
//                break;
//            case BoundGotoStmt gotoStmt:
//                index = labelToIndex.get(gotoStmt.label());
//                break;
//            case BoundConditionalGotoStmt conditionalGotoStmt:
//                Object condition = evaluateExpr(conditionalGotoStmt.condition(), robo);
//                if (condition instanceof NothingDataType)
//                    throw new EvaluationError(Component.translatable("runtime.redbyte.error.value_not_existing"), conditionalGotoStmt.condition().span());
//
//                if ((boolean) condition == conditionalGotoStmt.jumpIfTrue())
//                    index = labelToIndex.get(conditionalGotoStmt.label());
//                else
//                    ++index;
//                break;
//            default:
//                throw new RuntimeException();
//        }
//    }

    private @NotNull Object evaluateExpr(BoundExpr expr, PseudoRobo robo) throws EvaluationError {
        return switch (expr) {
            case BoundLiteralExpr literalExpr -> literalExpr.value;
            case BoundUnaryExpr unaryExpr -> {
                Object operand = evaluateExpr(unaryExpr.operand(), robo);
                if (operand instanceof NothingDataType)
                    throw new EvaluationError(Component.translatable("runtime.redbyte.error.value_not_existing"), unaryExpr.operand().span());
                if (unaryExpr.operator().operatorKind() == TokenKind.Bang)
                    yield !(boolean) operand;
                else if (unaryExpr.operator().operatorKind() == TokenKind.Minus)
                    yield -(float) operand;
                else if (unaryExpr.operator().operatorKind() == TokenKind.Plus)
                    yield -(float) operand;
                throw new EvaluationError(Component.translatable("runtime.redbyte.error.unsupported_unary_operation"), unaryExpr.span());
            }
            case BoundBinaryExpr binaryExpr -> {
                Object left = evaluateExpr(binaryExpr.left(), robo);
                Object right = evaluateExpr(binaryExpr.right(), robo);
                if (left instanceof NothingDataType && right instanceof NothingDataType)
                    throw new EvaluationError(Component.translatable("runtime.redbyte.error.value_not_existing"), binaryExpr.span());
                else if (left instanceof NothingDataType)
                    throw new EvaluationError(Component.translatable("runtime.redbyte.error.value_not_existing"), binaryExpr.left().span());
                else if (right instanceof NothingDataType)
                    throw new EvaluationError(Component.translatable("runtime.redbyte.error.value_not_existing"), binaryExpr.right().span());

                switch (binaryExpr.operator().operatorKind()) {
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
                        throw new EvaluationError(Component.translatable("runtime.redbyte.error.unsupported_binary_operation"), binaryExpr.span());
                }
            }
            case BoundGroupExpr groupExpr -> evaluateExpr(groupExpr.expr(), robo);
            case BoundNameExpr nameExpr -> variables.get(nameExpr.symbol());
            case BoundAssignExpr assignExpr -> {
                Object value = evaluateExpr(assignExpr.value(), robo);
                variables.put(assignExpr.symbol(), value);
                yield value;
            }
            case BoundRoboExpr ignored -> new RoboDataType(getRoboEntity());
            case BoundCallExpr callExpr -> {
                Object[] args = callExpr.args().stream().map(a -> evaluateExpr(a, robo)).toArray(Object[]::new);
                FunctionSymbol function = callExpr.symbol();
                for (int i = 0; i < args.length; ++i) {
                    Object arg = args[i];
                    if (arg instanceof NothingDataType && !Objects.equals(function.parameters.get(i).name, DataType.TYPE.name))
                        throw new EvaluationError(Component.translatable("runtime.redbyte.error.value_not_existing"), callExpr.args().get(i).span());
                }

                yield function.callback.apply(this, robo, args);
            }
            default ->
                    throw new EvaluationError(Component.translatable("runtime.redbyte.error.unsupported_expression"), expr.span());
        };
    }

    public static final class EvaluationError extends RuntimeException {
        private final TextSpan span;

        public EvaluationError(Component message, TextSpan span) {
            this(message.getString(), span);
        }

        public EvaluationError(String message, TextSpan span) {
            super(message);
            this.span = span;
        }

        public Diagnostic getDiagnostic() {
            return new Diagnostic(getMessage(), span);
        }

        public TextSpan getSpan() {
            return span;
        }
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
