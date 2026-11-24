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
import net.stonygeist.redbyte.interpreter.data_types.NothingDataType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.BooleanType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.PrimitiveType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.TextType;
import net.stonygeist.redbyte.interpreter.diagnostics.Diagnostic;
import net.stonygeist.redbyte.interpreter.diagnostics.DiagnosticBag;
import net.stonygeist.redbyte.interpreter.symbols.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class Binder {
    private final ImmutableList<Stmt> stmts;
    private final DiagnosticBag diagnostics = new DiagnosticBag();
    private BoundScope _scope = new BoundScope();

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
                _scope = new BoundScope(_scope);
                stmts.addAll(Arrays.stream(blockStmt.stmts).map(this::bindStmt).toList());
                _scope = _scope.getParent();
                yield new BoundBlockStmt(stmts.build());
            }
            case IfStmt ifStmt -> {
                BoundExpr condition = bindExpr(ifStmt.condition);
                if (!condition.getType().equals(BooleanType.TYPE))
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_type", BooleanType.TYPE.getDocsName(), condition.getType().getDocsName()), ifStmt.condition.span()));

                _scope = new BoundScope(_scope);
                BoundStmt thenStmt = bindStmt(ifStmt.thenStmt);
                _scope = _scope.getParent();
                @Nullable BoundStmt elseStmt;
                if (ifStmt.elseStmt == null) elseStmt = null;
                else {
                    _scope = new BoundScope(_scope);
                    elseStmt = bindStmt(ifStmt.elseStmt);
                    _scope = _scope.getParent();
                }
                yield new BoundIfStmt(condition, thenStmt, elseStmt);
            }
            case OnceStmt onceStmt -> {
                BoundExpr condition = bindExpr(onceStmt.condition);
                if (!condition.getType().equals(BooleanType.TYPE))
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_type", BooleanType.TYPE.toString(), condition.getType().getDocsName()), onceStmt.condition.span()));

                _scope = new BoundScope(_scope);
                BoundStmt thenStmt = bindStmt(onceStmt.stmt);
                _scope = _scope.getParent();
                yield new BoundOnceStmt(condition, thenStmt);
            }
            case WhileStmt whileStmt -> {
                BoundExpr condition = bindExpr(whileStmt.condition);
                if (!condition.getType().equals(BooleanType.TYPE))
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_type", BooleanType.TYPE.toString(), condition.getType().getDocsName()), whileStmt.condition.span()));

                _scope = new BoundScope(_scope);
                BoundStmt thenStmt = bindStmt(whileStmt.stmt);
                _scope = _scope.getParent();
                yield new BoundWhileStmt(condition, thenStmt);
            }
            case AlwaysStmt alwaysStmt -> {
                BoundExpr condition = bindExpr(alwaysStmt.condition);
                if (!condition.getType().equals(BooleanType.TYPE))
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_type", BooleanType.TYPE.toString(), condition.getType().getDocsName()), alwaysStmt.condition.span()));

                _scope = new BoundScope(_scope);
                BoundStmt thenStmt = bindStmt(alwaysStmt.stmt);
                _scope = _scope.getParent();
                yield new BoundAlwaysStmt(condition, thenStmt);
            }
            case LoopStmt loopStmt -> {
                BoundExpr count = bindExpr(loopStmt.count);
                if (!count.getType().equals(NumberType.TYPE))
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_type", NumberType.TYPE.toString(), count.getType().getDocsName()), loopStmt.count.span()));

                _scope = new BoundScope(_scope);
                BoundStmt thenStmt = bindStmt(loopStmt.stmt);
                _scope = _scope.getParent();
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
                else if (literalExpr.token.kind == TokenKind.True || literalExpr.token.kind == TokenKind.False)
                    value = new BooleanType(literalExpr.token.kind == TokenKind.True);
                else
                    throw new RuntimeException("Unexpected token.");
                yield new BoundLiteralExpr(value, literalExpr.span());
            }
            case UnaryExpr unaryExpr -> {
                BoundExpr operand = bindExpr(unaryExpr.operand);
                BoundOperator.BoundUnaryOperator operator = BoundOperator.BoundUnaryOperator.bind(unaryExpr.op.kind, operand.getType());
                if (operator == null) {
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.invalid_unary_operator", unaryExpr.op.lexeme, operand.getType().getDocsName()), unaryExpr.span()));
                    yield new BoundErrorExpr(unaryExpr.span());
                }

                yield new BoundUnaryExpr(operand, operator, unaryExpr.span());
            }
            case BinaryExpr binaryExpr -> {
                BoundExpr left = bindExpr(binaryExpr.left);
                BoundExpr right = bindExpr(binaryExpr.right);
                BoundOperator.BoundBinaryOperator operator = BoundOperator.BoundBinaryOperator.bind(binaryExpr.op.kind, left.getType(), right.getType());
                if (operator == null) {
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.invalid_binary_operator", binaryExpr.op.lexeme, left.getType().getDocsName(), right.getType().getDocsName()), binaryExpr.span()));
                    yield new BoundErrorExpr(binaryExpr.span());
                }

                yield new BoundBinaryExpr(left, operator, right, binaryExpr.span());
            }
            case GroupExpr groupExpr -> new BoundGroupExpr(bindExpr(groupExpr.expr), groupExpr.span());
            case NameExpr nameExpr -> {
                String name = nameExpr.name.lexeme.toLowerCase();
                VariableSymbol variable = _scope.lookup(name);
                if (variable == null) {
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.variable_not_found", name), nameExpr.name.span()));
                    yield new BoundErrorExpr(nameExpr.span());
                }

                yield new BoundNameExpr(variable, nameExpr.span());
            }
            case AssignExpr assignExpr -> {
                BoundExpr value = bindExpr(assignExpr.value);
                String name = assignExpr.name.lexeme.toLowerCase();
                VariableSymbol variable = _scope.lookup(name);
                if (variable == null) {
                    variable = new VariableSymbol(name, value.getType());
                    tryDeclareVar(variable);
                } else if (!variable.type.equals(value.getType())) {
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_type", variable.type.getDocsName(), value.getType().getDocsName()), assignExpr.value.span()));
                    yield new BoundErrorExpr(assignExpr.value.span());
                }

                yield new BoundAssignExpr(variable, value, assignExpr.span());
            }
            case IndexAssignExpr indexAssignExpr -> {
                BoundExpr base = bindExpr(indexAssignExpr.indexing.base);
                BoundExpr index = bindExpr(indexAssignExpr.indexing.index);
                BoundExpr value = bindExpr(indexAssignExpr.value);


                if (base.getType().isList()) {
                    checkIndexType(index);
                    TypeSymbol elementType = base.getType().getElementType();
                    assert elementType != null;
                    if (!elementType.equals(value.getType()))
                        diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_list_type", elementType.getDocsName(), value.getType().getDocsName()), value.span()));

                    yield new BoundIndexAssignExpr(base, index, value, indexAssignExpr.span());
                } else {
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_list", base.getType().getDocsName()), base.span()));
                    yield new BoundErrorExpr(indexAssignExpr.span());
                }
            }
            case RoboExpr roboExpr -> new BoundRoboExpr(roboExpr.span());
            case ListExpr listExpr -> {
                if (listExpr.items.isEmpty())
                    yield new BoundListExpr(List.of(), listExpr.span(), NothingDataType.TYPE);

                List<BoundExpr> items = listExpr.items.stream().map(i -> bindExpr(i.getKey())).toList();
                TypeSymbol listItemType = items.getFirst().getType();
                for (BoundExpr i : items)
                    if (!listItemType.equals(i.getType()))
                        diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_list_type", listItemType.getDocsName(), i.getType().getDocsName()), i.span()));
                yield new BoundListExpr(items, listExpr.span(), listItemType);
            }
            case IndexExpr indexExpr -> {
                BoundExpr base = bindExpr(indexExpr.base);
                BoundExpr index = bindExpr(indexExpr.index);
                if (base.getType().isList()) {
                    checkIndexType(index);
                    yield new BoundIndexExpr(base, index, indexExpr.span());
                } else {
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_list", base.getType().getDocsName()), base.span()));
                    yield new BoundErrorExpr(indexExpr.span());
                }
            }
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

                for (int i = 0; i < Math.min(args.size(), function.parameters.size()); ++i)
                    if (!function.parameters.get(i).isAssignableFrom(args.get(i).getType()))
                        diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_type", function.parameters.get(i).getDocsName(), args.get(i).getType().getDocsName()), callExpr.args[i].span()));
                yield new BoundCallExpr(function, args, callExpr.span());
            }
            case PropertyExpr propertyExpr -> {
                BoundExpr object = bindExpr(propertyExpr.object);
                String propertyName = propertyExpr.property.lexeme.toLowerCase();
                Optional<Map.Entry<PropertySymbol, Function<DataType, DataType>>> property = object.getType().getProperties().entrySet().stream().filter(p -> p.getKey().name.equals(propertyName)).findFirst();
                if (property.isEmpty()) {
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.property_not_existing", propertyName.toLowerCase()), propertyExpr.property.span()));
                    yield new BoundErrorExpr(propertyExpr.property.span());
                }

                yield new BoundPropertyExpr(object, property.map(Map.Entry::getValue), property.map(x -> x.getKey().type), propertyExpr.span());
            }
            case MethodExpr methodExpr -> {
                BoundExpr object = bindExpr(methodExpr.object);
                String name = methodExpr.method.lexeme.toLowerCase();
                ImmutableList<BoundExpr> args = Arrays.stream(methodExpr.args).map(this::bindExpr).collect(ImmutableList.toImmutableList());
                List<MethodSymbol> methods = object.getType().getMethods();
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
                        diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_type", method.parameters.get(i).getDocsName(), args.get(i).getType()), methodExpr.args[i].span()));
                yield new BoundMethodExpr(object, method, args, methodExpr.span());
            }
            default -> throw new RuntimeException("Unexpected expression.");
        };
    }

    private static boolean isAssignable(TypeSymbol target, BoundExpr value) {
        TypeSymbol source = value.getType();
        if (target.equals(source))
            return true;

        return target.isList() && source.isList()
                && (source.getBase().equals(NothingDataType.TYPE) || target.getBase().equals(NothingDataType.TYPE));
    }


    public void tryDeclareVar(VariableSymbol variable) {
        _scope.declare(variable);
    }

    private void checkIndexType(BoundExpr index) {
        TypeSymbol indexType = NumberType.TYPE;
        if (!indexType.equals(index.getType()))
            diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.expected_type", indexType.getDocsName(), index.getType().getDocsName()), index.span()));
    }

    public DiagnosticBag getDiagnostics() {
        return diagnostics;
    }
}
