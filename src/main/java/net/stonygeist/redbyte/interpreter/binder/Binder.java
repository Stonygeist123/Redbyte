package net.stonygeist.redbyte.interpreter.binder;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.Miscellaneous;
import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.redbyte.interpreter.analysis.nodes.expr.*;
import net.stonygeist.redbyte.interpreter.analysis.nodes.stmt.*;
import net.stonygeist.redbyte.interpreter.binder.expr.*;
import net.stonygeist.redbyte.interpreter.binder.stmt.*;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.BooleanType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.PrimitiveType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.TextType;
import net.stonygeist.redbyte.interpreter.diagnostics.Diagnostic;
import net.stonygeist.redbyte.interpreter.diagnostics.DiagnosticBag;
import net.stonygeist.redbyte.interpreter.symbols.FunctionSymbol;
import net.stonygeist.redbyte.interpreter.symbols.MethodSymbol;
import net.stonygeist.redbyte.interpreter.symbols.PropertySymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public final class Binder {
    private final ImmutableList<Stmt> stmts;
    private final Hashtable<String, VariableSymbol> symbolTable = new Hashtable<>();
    private final DiagnosticBag diagnostics = new DiagnosticBag();

    public Binder(Stmt[] stmts) {
        this.stmts = Arrays.stream(stmts).collect(ImmutableList.toImmutableList());
    }

    public ImmutableList<BoundStmt> bind() {
        return stmts.stream().map(this::bindStmt).collect(ImmutableList.toImmutableList());
    }

    private BoundStmt bindStmt(Stmt stmt) {
        return switch (stmt) {
            case ExprStmt exprStmt -> new BoundExprStmt(bindExpr(exprStmt.expr));
            case BlockStmt blockStmt -> {
                ImmutableList.Builder<BoundStmt> stmts = new ImmutableList.Builder<>();
                stmts.addAll(Arrays.stream(blockStmt.stmts).map(this::bindStmt).toList());
                yield new BoundBlockStmt(stmts.build());
            }
            case IfStmt ifStmt -> {
                BoundExpr condition = bindExpr(ifStmt.condition);
                if (!condition.getType().equals(BooleanType.class))
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_type", BooleanType.TYPE.toString(), condition.getType().toString()), ifStmt.condition.span()));

                BoundStmt thenStmt = bindStmt(ifStmt.thenStmt);
                @Nullable BoundStmt elseStmt = ifStmt.elseStmt == null ? null : bindStmt(ifStmt.elseStmt);
                yield new BoundIfStmt(condition, thenStmt, elseStmt);
            }
            case OnceStmt onceStmt -> {
                BoundExpr condition = bindExpr(onceStmt.condition);
                if (!condition.getType().equals(BooleanType.class))
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_type", BooleanType.TYPE.toString(), condition.getType().toString()), onceStmt.condition.span()));

                BoundStmt thenStmt = bindStmt(onceStmt.stmt);
                yield new BoundOnceStmt(condition, thenStmt);
            }
            case WhileStmt whileStmt -> {
                BoundExpr condition = bindExpr(whileStmt.condition);
                if (!condition.getType().equals(BooleanType.class))
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_type", BooleanType.TYPE.toString(), condition.getType().toString()), whileStmt.condition.span()));

                BoundStmt thenStmt = bindStmt(whileStmt.stmt);
                yield new BoundWhileStmt(condition, thenStmt);
            }
            case AlwaysStmt alwaysStmt -> {
                BoundExpr condition = bindExpr(alwaysStmt.condition);
                if (!condition.getType().equals(BooleanType.class))
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_type", BooleanType.TYPE.toString(), condition.getType().toString()), alwaysStmt.condition.span()));

                BoundStmt thenStmt = bindStmt(alwaysStmt.stmt);
                yield new BoundAlwaysStmt(condition, thenStmt);
            }
            case LoopStmt loopStmt -> {
                BoundExpr count = bindExpr(loopStmt.count);
                if (!count.getType().equals(NumberType.class))
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_type", NumberType.TYPE.toString(), count.getType().toString()), loopStmt.count.span()));

                BoundStmt thenStmt = bindStmt(loopStmt.stmt);
                yield new BoundLoopStmt(count, thenStmt);
            }
            default -> throw new RuntimeException("Unexpected statement.");
        };
    }

    @NotNull
    private BoundExpr bindExpr(Expr expr) {
        return switch (expr) {
            case LiteralExpr literalExpr -> {
                PrimitiveType value;
                if (literalExpr.token.kind == TokenKind.Number)
                    value = new NumberType(Float.parseFloat(literalExpr.token.lexeme));
                else if (literalExpr.token.kind == TokenKind.String)
                    value = new TextType(literalExpr.token.lexeme.substring(1, literalExpr.token.lexeme.length() - 1));
                else
                    throw new RuntimeException("Unexpected token.");
                yield new BoundLiteralExpr(value, literalExpr.span());
            }
            case UnaryExpr unaryExpr -> {
                BoundExpr operand = bindExpr(unaryExpr.operand);
                BoundOperator.BoundUnaryOperator operator = BoundOperator.BoundUnaryOperator.bind(unaryExpr.op.kind, operand.getType());
                if (operator == null) {
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.invalid_unary_operator", unaryExpr.op.lexeme, operand.getType().toString()), unaryExpr.span()));
                    yield new BoundErrorExpr(unaryExpr.span());
                }

                yield new BoundUnaryExpr(operand, operator, unaryExpr.span());
            }
            case BinaryExpr binaryExpr -> {
                BoundExpr left = bindExpr(binaryExpr.left);
                BoundExpr right = bindExpr(binaryExpr.right);
                BoundOperator.BoundBinaryOperator operator = BoundOperator.BoundBinaryOperator.bind(binaryExpr.op.kind, left.getType(), right.getType());
                if (operator == null) {
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.invalid_binary_operator", binaryExpr.op.lexeme, left.getType().toString(), right.getType().toString()), binaryExpr.span()));
                    yield new BoundErrorExpr(binaryExpr.span());
                }

                yield new BoundBinaryExpr(left, operator, right, binaryExpr.span());
            }
            case GroupExpr groupExpr -> new BoundGroupExpr(bindExpr(groupExpr.expr), groupExpr.span());
            case NameExpr nameExpr -> {
                String name = nameExpr.name.lexeme.toLowerCase();
                VariableSymbol variable = symbolTable.get(name);
                if (variable == null) {
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.variable_not_found", name), nameExpr.name.span()));
                    yield new BoundErrorExpr(nameExpr.span());
                }

                yield new BoundNameExpr(variable, nameExpr.span());
            }
            case AssignExpr assignExpr -> {
                BoundExpr value = bindExpr(assignExpr.value);
                String name = assignExpr.name.lexeme.toLowerCase();
                VariableSymbol variable = symbolTable.get(name);
                if (variable == null) {
                    variable = new VariableSymbol(name, value.getType());
                    tryDeclareVar(variable);
                } else if (!variable.type.equals(value.getType())) {
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_type", variable.type.toString(), value.getType()), assignExpr.value.span()));
                    yield new BoundErrorExpr(assignExpr.value.span());
                }

                yield new BoundAssignExpr(variable, value, assignExpr.span());
            }
            case RoboExpr roboExpr -> new BoundRoboExpr(roboExpr.span());
            case CallExpr callExpr -> {
                String name = callExpr.name.lexeme.toLowerCase();
                ImmutableList<BoundExpr> args = Arrays.stream(callExpr.args).map(this::bindExpr).collect(ImmutableList.toImmutableList());
                Map.Entry<FunctionSymbol, Boolean> functionResult = Miscellaneous.getFunction(Miscellaneous.functions, name, args.stream().map(BoundExpr::getType).toList());
                FunctionSymbol function = functionResult.getKey();
                if (function == null) {
                    if (functionResult.getValue())
                        diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.function_not_found_multiple", name), callExpr.name.span()));
                    else
                        diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.function_not_found", name), callExpr.name.span()));
                    yield new BoundErrorExpr(callExpr.name.span());
                }

                if (function.parameters.size() != callExpr.args.length) {
                    if (callExpr.args.length == 0)
                        diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_arguments", function.parameters.size(), "none"), new TextSpan(callExpr.lParen.span().startColumn(), callExpr.rParen.span().endColumn(), callExpr.lParen.span().lineStart(), callExpr.rParen.span().lineEnd())));
                    else {
                        TextSpan firstSpan = callExpr.args[0].span();
                        TextSpan lastSpan = callExpr.args[callExpr.args.length - 1].span();
                        diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_arguments", function.parameters.size(), callExpr.args.length), new TextSpan(firstSpan.startColumn(), lastSpan.endColumn(), firstSpan.lineStart(), lastSpan.lineEnd())));
                    }
                }

                for (int i = 0; i < Math.min(args.size(), function.parameters.size()); i++)
                    if (!function.parameters.get(i).isAssignableFrom(args.get(i).getType()))
                        diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_type", function.parameters.get(i), args.get(i).getType()), callExpr.args[i].span()));
                yield new BoundCallExpr(function, args, callExpr.span());
            }
            case PropertyExpr propertyExpr -> {
                BoundExpr object = bindExpr(propertyExpr.object);
                if (PrimitiveType.class.isAssignableFrom(object.getType())) {
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.primitive_not_allowed"), propertyExpr.object.span()));
                    yield new BoundErrorExpr(propertyExpr.object.span());
                }

                String propertyName = propertyExpr.property.lexeme.toLowerCase();
                Optional<Map.Entry<PropertySymbol, Function<DataType, DataType>>> property = DataType.getProperty(object.getType(), propertyName);
                if (property.isEmpty()) {
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.property_not_existing", propertyName.toLowerCase()), propertyExpr.property.span()));
                    yield new BoundErrorExpr(propertyExpr.property.span());
                }

                yield new BoundPropertyExpr(object, property.map(Map.Entry::getValue), property.map(x -> x.getKey().type), propertyExpr.span());
            }
            case MethodExpr methodExpr -> {
                BoundExpr object = bindExpr(methodExpr.object);
                if (PrimitiveType.class.isAssignableFrom(object.getType())) {
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.primitive_not_allowed"), methodExpr.object.span()));
                    yield new BoundErrorExpr(methodExpr.object.span());
                }

                String name = methodExpr.method.lexeme.toLowerCase();
                ImmutableList<BoundExpr> args = Arrays.stream(methodExpr.args).map(this::bindExpr).collect(ImmutableList.toImmutableList());
                List<MethodSymbol> methods = DataType.getMethods(object.getType());
                Map.Entry<MethodSymbol, Boolean> functionResult = Miscellaneous.getFunction(methods, name, args.stream().map(BoundExpr::getType).toList());
                MethodSymbol method = functionResult.getKey();
                if (method == null) {
                    if (functionResult.getValue())
                        diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.function_not_found_multiple", name), methodExpr.method.span()));
                    else
                        diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.function_not_found", name), methodExpr.method.span()));
                    yield new BoundErrorExpr(methodExpr.method.span());
                }

                if (method.parameters.size() != methodExpr.args.length) {
                    if (methodExpr.args.length == 0)
                        diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_arguments", method.parameters.size(), "none"), new TextSpan(methodExpr.lParen.span().startColumn(), methodExpr.rParen.span().endColumn(), methodExpr.lParen.span().lineStart(), methodExpr.rParen.span().lineEnd())));
                    else {
                        TextSpan firstSpan = methodExpr.args[0].span();
                        TextSpan lastSpan = methodExpr.args[methodExpr.args.length - 1].span();
                        diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_arguments", method.parameters.size(), methodExpr.args.length), new TextSpan(firstSpan.startColumn(), lastSpan.endColumn(), firstSpan.lineStart(), lastSpan.lineEnd())));
                    }
                }

                for (int i = 0; i < Math.min(args.size(), method.parameters.size()); i++)
                    if (!method.parameters.get(i).isAssignableFrom(args.get(i).getType()))
                        diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_type", method.parameters.get(i), args.get(i).getType()), methodExpr.args[i].span()));
                yield new BoundMethodExpr(object, method, args, methodExpr.span());
            }
            default -> throw new RuntimeException("Unexpected expression.");
        };
    }

    public void tryDeclareVar(VariableSymbol variable) {
        if (symbolTable.containsKey(variable.name))
            return;

        symbolTable.put(variable.name, variable);
    }

    public DiagnosticBag getDiagnostics() {
        return diagnostics;
    }
}
