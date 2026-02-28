package net.stonygeist.redbyte.interpreter.binder;

import com.google.common.collect.ImmutableList;
import net.stonygeist.redbyte.interpreter.Config;
import net.stonygeist.redbyte.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.redbyte.interpreter.analysis.nodes.expr.*;
import net.stonygeist.redbyte.interpreter.analysis.nodes.stmt.*;
import net.stonygeist.redbyte.interpreter.binder.expr.*;
import net.stonygeist.redbyte.interpreter.binder.stmt.*;
import net.stonygeist.redbyte.interpreter.symbols.FunctionSymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Hashtable;

public final class Binder {
    private final ImmutableList<Stmt> stmts;
    private final Hashtable<String, VariableSymbol> symbolTable = new Hashtable<>();

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
                if (condition.getType() != TypeSymbol.Boolean)
                    throw new RuntimeException();

                BoundStmt thenStmt = bindStmt(ifStmt.thenStmt);
                @Nullable BoundStmt elseStmt = ifStmt.elseStmt == null ? null : bindStmt(ifStmt.elseStmt);
                yield new BoundIfStmt(condition, thenStmt, elseStmt);
            }
            case WhileStmt whileStmt -> {
                BoundExpr condition = bindExpr(whileStmt.condition);
                if (condition.getType() != TypeSymbol.Boolean)
                    throw new RuntimeException();

                BoundStmt thenStmt = bindStmt(whileStmt.stmt);
                yield new BoundWhileStmt(condition, thenStmt);
            }
            case OnceStmt onceStmt -> {
                BoundExpr condition = bindExpr(onceStmt.condition);
                if (condition.getType() != TypeSymbol.Boolean)
                    throw new RuntimeException();

                BoundStmt thenStmt = bindStmt(onceStmt.stmt);
                yield new BoundOnceStmt(condition, thenStmt);
            }
            case LoopStmt loopStmt -> {
                BoundExpr count = bindExpr(loopStmt.count);
                if (count.getType() != TypeSymbol.Number)
                    throw new RuntimeException();

                BoundStmt thenStmt = bindStmt(loopStmt.stmt);
                yield new BoundLoopStmt(count, thenStmt);
            }
            default -> throw new RuntimeException();
        };
    }

    private BoundExpr bindExpr(Expr expr) {
        return switch (expr) {
            case LiteralExpr literalExpr -> {
                Object value;
                if (literalExpr.token.kind == TokenKind.Number)
                    value = Float.valueOf(literalExpr.token.lexeme);
                else if (literalExpr.token.kind == TokenKind.String)
                    value = literalExpr.token.lexeme.substring(1, literalExpr.token.lexeme.length() - 1);
                else
                    throw new RuntimeException();
                yield new BoundLiteralExpr(value);
            }
            case UnaryExpr unaryExpr -> {
                BoundExpr operand = bindExpr(unaryExpr.operand);
                BoundOperator.BoundUnaryOperator operator = BoundOperator.BoundUnaryOperator.bind(unaryExpr.op.kind, operand.getType());
                if (operator == null)
                    throw new RuntimeException();
                yield new BoundUnaryExpr(operand, operator);
            }
            case BinaryExpr binaryExpr -> {
                BoundExpr left = bindExpr(binaryExpr.left);
                BoundExpr right = bindExpr(binaryExpr.right);
                BoundOperator.BoundBinaryOperator operator = BoundOperator.BoundBinaryOperator.bind(binaryExpr.op.kind, left.getType(), right.getType());
                if (operator == null)
                    throw new RuntimeException();
                yield new BoundBinaryExpr(left, operator, right);
            }
            case GroupExpr groupExpr -> new BoundGroupExpr(bindExpr(groupExpr.expr));
            case NameExpr nameExpr -> {
                String name = nameExpr.name.lexeme.toLowerCase();
                VariableSymbol variable = symbolTable.get(name);
                if (variable == null)
                    throw new RuntimeException();
                yield new BoundNameExpr(variable);
            }
            case AssignExpr assignExpr -> {
                BoundExpr value = bindExpr(assignExpr.value);
                String name = assignExpr.name.lexeme.toLowerCase();
                VariableSymbol variable = symbolTable.get(name);
                if (variable == null) {
                    variable = new VariableSymbol(name, value.getType());
                    tryDeclareVar(variable);
                } else if (!variable.type.equals(value.getType()))
                    throw new RuntimeException();
                yield new BoundAssignExpr(variable, value);
            }
            case CallExpr callExpr -> {
                String name = callExpr.name.lexeme.toLowerCase();
                FunctionSymbol function = Config.getFunction(name);
                if (function == null)
                    throw new RuntimeException();

                if (function.parameters.size() != callExpr.args.length)
                    throw new RuntimeException();

                ImmutableList<BoundExpr> args = Arrays.stream(callExpr.args).map(this::bindExpr).collect(ImmutableList.toImmutableList());
                for (int i = 0; i < args.size(); i++) {
                    if (!args.get(i).getType().equals(function.parameters.get(i)))
                        throw new RuntimeException();
                }

                yield new BoundCallExpr(function, args);
            }
            default -> throw new RuntimeException();
        };
    }

    public void tryDeclareVar(VariableSymbol variable) {
        if (symbolTable.containsKey(variable.name))
            return;

        symbolTable.put(variable.name, variable);
    }
}
