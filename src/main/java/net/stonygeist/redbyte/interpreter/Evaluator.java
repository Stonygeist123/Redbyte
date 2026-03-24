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
import net.stonygeist.redbyte.interpreter.data_types.primitives.BooleanType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.TextType;
import net.stonygeist.redbyte.interpreter.diagnostics.Diagnostic;
import net.stonygeist.redbyte.interpreter.symbols.FunctionSymbol;
import net.stonygeist.redbyte.interpreter.symbols.LabelSymbol;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;
import net.stonygeist.redbyte.manager.PseudoRobo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Stack;

public final class Evaluator {
    private final Stack<BoundBlockStmt> globalStmts = new Stack<>();
    private final Dictionary<VariableSymbol, DataType> variables = new Hashtable<>();
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

    private @NotNull DataType evaluateExpr(BoundExpr expr, PseudoRobo robo) throws EvaluationError {
        return switch (expr) {
            case BoundLiteralExpr literalExpr -> literalExpr.value;
            case BoundUnaryExpr unaryExpr -> {
                DataType operand = evaluateExpr(unaryExpr.operand(), robo);
                if (operand instanceof NothingDataType)
                    throw new EvaluationError(Component.translatable("runtime.redbyte.error.value_not_existing"), unaryExpr.operand().span());
                if (unaryExpr.operator().operatorKind() == TokenKind.Bang)
                    yield new BooleanType(!((BooleanType) operand).getValue());
                else if (unaryExpr.operator().operatorKind() == TokenKind.Minus)
                    yield new NumberType(-((NumberType) operand).getValue());
                else if (unaryExpr.operator().operatorKind() == TokenKind.Plus)
                    yield new NumberType(((NumberType) operand).getValue());
                throw new EvaluationError(Component.translatable("runtime.redbyte.error.unsupported_unary_operation"), unaryExpr.span());
            }
            case BoundBinaryExpr binaryExpr -> {
                DataType left = evaluateExpr(binaryExpr.left(), robo);
                DataType right = evaluateExpr(binaryExpr.right(), robo);
                if (left instanceof NothingDataType && right instanceof NothingDataType)
                    throw new EvaluationError(Component.translatable("runtime.redbyte.error.value_not_existing"), binaryExpr.span());
                else if (left instanceof NothingDataType)
                    throw new EvaluationError(Component.translatable("runtime.redbyte.error.value_not_existing"), binaryExpr.left().span());
                else if (right instanceof NothingDataType)
                    throw new EvaluationError(Component.translatable("runtime.redbyte.error.value_not_existing"), binaryExpr.right().span());

                switch (binaryExpr.operator().operatorKind()) {
                    case Plus: {
                        if (left instanceof NumberType f1 && right instanceof NumberType f2)
                            yield new NumberType(f1.getValue() + f2.getValue());
                        else if (left instanceof TextType s1 && right instanceof TextType s2)
                            yield new TextType(s1.getValue() + s2.getValue());
                    }
                    case Minus: {
                        if (left instanceof NumberType l && right instanceof NumberType r)
                            yield new NumberType(l.getValue() - r.getValue());
                    }
                    case Star:
                        if (left instanceof NumberType l && right instanceof NumberType r)
                            yield new NumberType(l.getValue() * r.getValue());
                    case Slash: {
                        if (left instanceof NumberType l && right instanceof NumberType r)
                            yield new NumberType(l.getValue() / r.getValue());
                    }
                    case EqualsEquals: {
                        yield new BooleanType(equalsPrimitives(left, right));
                    }
                    case NotEquals: {
                        yield new BooleanType(!equalsPrimitives(left, right));
                    }
                    case Greater: {
                        if (left instanceof NumberType l && right instanceof NumberType r)
                            yield new BooleanType(l.getValue() > r.getValue());
                    }
                    case GreaterEquals: {
                        if (left instanceof NumberType l && right instanceof NumberType r)
                            yield new BooleanType(l.getValue() >= r.getValue());
                    }
                    case Less: {
                        if (left instanceof NumberType l && right instanceof NumberType r)
                            yield new BooleanType(l.getValue() < r.getValue());
                    }
                    case LessEquals: {
                        if (left instanceof NumberType l && right instanceof NumberType r)
                            yield new BooleanType(l.getValue() <= r.getValue());
                    }
                    case And: {
                        if (left instanceof BooleanType l && right instanceof BooleanType r)
                            yield new BooleanType(l.getValue() && r.getValue());
                    }
                    case Or: {
                        if (left instanceof BooleanType l && right instanceof BooleanType r)
                            yield new BooleanType(l.getValue() || r.getValue());
                    }
                    default:
                        throw new EvaluationError(Component.translatable("runtime.redbyte.error.unsupported_binary_operation"), binaryExpr.span());
                }
            }
            case BoundGroupExpr groupExpr -> evaluateExpr(groupExpr.expr(), robo);
            case BoundNameExpr nameExpr -> variables.get(nameExpr.variable());
            case BoundAssignExpr assignExpr -> {
                DataType value = evaluateExpr(assignExpr.value(), robo);
                variables.put(assignExpr.variable(), value);
                yield value;
            }
            case BoundRoboExpr ignored -> new RoboDataType(getRoboEntity());
            case BoundCallExpr callExpr -> {
                DataType[] args = callExpr.args().stream().map(a -> evaluateExpr(a, robo)).toArray(DataType[]::new);
                FunctionSymbol function = callExpr.function();
                for (int i = 0; i < args.length; ++i) {
                    Object arg = args[i];
                    if (arg instanceof NothingDataType && !function.parameters.get(i).equals(DataType.class))
                        throw new EvaluationError(Component.translatable("runtime.redbyte.error.value_not_existing"), callExpr.args().get(i).span());
                }

                try {
                    yield function.callback.apply(this, robo, args);
                } catch (Evaluator.CallEvaluationError e) {
                    throw new EvaluationError(e.getMessage(), callExpr.args().get(e.getArg()).span());
                }
            }
            case BoundPropertyExpr propertyExpr -> {
                if (propertyExpr.property().isEmpty())
                    throw new RuntimeException();
                DataType object = evaluateExpr(propertyExpr.object(), robo);
                yield propertyExpr.property().get().apply(object);
            }
            case BoundMethodExpr methodExpr -> {
                DataType[] args = methodExpr.args().stream().map(a -> evaluateExpr(a, robo)).toArray(DataType[]::new);
                MethodSymbol method = methodExpr.method();
                for (int i = 0; i < args.length; ++i) {
                    Object arg = args[i];
                    if (arg instanceof NothingDataType && !method.parameters.get(i).equals(DataType.class))
                        throw new EvaluationError(Component.translatable("runtime.redbyte.error.value_not_existing"), methodExpr.args().get(i).span());
                }

                DataType object = evaluateExpr(methodExpr.object(), robo);
                yield methodExpr.method().callback.apply(this, robo, object, args);
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

    public static final class CallEvaluationError extends RuntimeException {
        private final int arg;

        public CallEvaluationError(Component message, int arg) {
            super(message.getString());
            this.arg = arg;
        }

        public int getArg() {
            return arg;
        }
    }

    public static boolean equalsPrimitives(DataType a, DataType b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (!a.getClass().equals(b.getClass()))
            throw new RuntimeException();

        return switch (a) {
            case NumberType f1 when b instanceof NumberType f2 -> Float.compare(f1.getValue(), f2.getValue()) == 0;
            case BooleanType bo1 when b instanceof BooleanType bo2 -> bo1.getValue() == bo2.getValue();
            case TextType c1 when b instanceof TextType c2 -> c1.getValue().equals(c2.getValue());
            default -> throw new RuntimeException();
        };
    }
}
