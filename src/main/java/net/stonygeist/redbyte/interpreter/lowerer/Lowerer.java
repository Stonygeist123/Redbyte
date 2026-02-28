package net.stonygeist.redbyte.interpreter.lowerer;

import com.google.common.collect.ImmutableList;
import net.stonygeist.redbyte.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.redbyte.interpreter.binder.BoundOperator;
import net.stonygeist.redbyte.interpreter.binder.expr.*;
import net.stonygeist.redbyte.interpreter.binder.stmt.*;
import net.stonygeist.redbyte.interpreter.symbols.LabelSymbol;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import net.stonygeist.redbyte.interpreter.symbols.VariableSymbol;

import java.util.Stack;

public final class Lowerer {
    private int labelCount;
    private int loopVarCount;

    public static BoundBlockStmt lower(BoundStmt stmt) {
        Lowerer lowerer = new Lowerer();
        return flatten(lowerer.rewriteStmt(stmt));
    }

    private static BoundBlockStmt flatten(BoundStmt stmt) {
        ImmutableList.Builder<BoundStmt> builder = ImmutableList.builder();
        Stack<BoundStmt> stack = new Stack<>();
        stack.push(stmt);

        while (!stack.isEmpty()) {
            BoundStmt current = stack.pop();
            if (current instanceof BoundBlockStmt b) {
                for (BoundStmt s : b.stmts.reverse())
                    stack.push(s);
            } else
                builder.add(current);
        }

        return new BoundBlockStmt(builder.build());
    }

    private BoundStmt rewriteStmt(BoundStmt node) {
        return switch (node) {
            case BoundBlockStmt blockStmt -> {
                ImmutableList.Builder<BoundStmt> builder = ImmutableList.builder();
                builder.addAll(blockStmt.stmts.stream().map(this::rewriteStmt).iterator());
                yield new BoundBlockStmt(builder.build());
            }
            case BoundIfStmt ifStmt -> {
                if (ifStmt.elseStmt == null) {
                    LabelSymbol endLabel = generateLabel();
                    BoundConditionalGotoStmt gotoFalse = new BoundConditionalGotoStmt(endLabel, ifStmt.condition, false);
                    BoundLabelStmt endLabelStmt = new BoundLabelStmt(endLabel);
                    yield rewriteStmt(new BoundBlockStmt(ImmutableList.of(gotoFalse, ifStmt.thenStmt, endLabelStmt)));
                } else {
                    LabelSymbol elseLabel = generateLabel();
                    LabelSymbol endLabel = generateLabel();
                    BoundConditionalGotoStmt gotoFalse = new BoundConditionalGotoStmt(elseLabel, ifStmt.condition, false);
                    BoundGotoStmt gotoEndStmt = new BoundGotoStmt(endLabel);
                    BoundLabelStmt elseLabelStmt = new BoundLabelStmt(elseLabel);
                    BoundLabelStmt endLabelStmt = new BoundLabelStmt(endLabel);
                    yield rewriteStmt(new BoundBlockStmt(ImmutableList.of(gotoFalse, ifStmt.thenStmt, gotoEndStmt, elseLabelStmt, ifStmt.elseStmt, endLabelStmt)));
                }
            }
            case BoundWhileStmt whileStmt -> {
                LabelSymbol continueLabel = generateLabel();
                LabelSymbol checkLabel = generateLabel();
                LabelSymbol endLabel = generateLabel();

                BoundGotoStmt gotoCheck = new BoundGotoStmt(checkLabel);
                BoundLabelStmt continueLabelStmt = new BoundLabelStmt(continueLabel);
                BoundLabelStmt checkLabelStmt = new BoundLabelStmt(checkLabel);
                BoundConditionalGotoStmt gotoTrue = new BoundConditionalGotoStmt(continueLabel, whileStmt.condition);
                BoundLabelStmt endLabelStmt = new BoundLabelStmt(endLabel);
                yield rewriteStmt(new BoundBlockStmt(ImmutableList.of(gotoCheck, continueLabelStmt, whileStmt.body, checkLabelStmt, gotoTrue, endLabelStmt)));
            }
            case BoundOnceStmt onceStmt -> {
                BoundOperator.BoundUnaryOperator conditionOperator = BoundOperator.BoundUnaryOperator.bind(TokenKind.Bang, TypeSymbol.Boolean);
                assert conditionOperator != null;
                BoundUnaryExpr negatedCondition = new BoundUnaryExpr(onceStmt.condition, conditionOperator);
                BoundWhileStmt whileStmt = new BoundWhileStmt(negatedCondition, new BoundBlockStmt(ImmutableList.of()));
                BoundIfStmt ifStmt = new BoundIfStmt(onceStmt.condition, onceStmt.body, null);
                yield rewriteStmt(new BoundBlockStmt(ImmutableList.of(whileStmt, ifStmt)));
            }
            case BoundLoopStmt loopStmt -> {
                VariableSymbol variable = new VariableSymbol("LoopVar_" + ++loopVarCount, TypeSymbol.Number);
                BoundExprStmt varDecl = new BoundExprStmt(new BoundAssignExpr(variable, new BoundLiteralExpr(1f)));
                BoundNameExpr nameExpr = new BoundNameExpr(variable);

                BoundOperator.BoundBinaryOperator conditonOperator = BoundOperator.BoundBinaryOperator.bind(TokenKind.LessEquals, TypeSymbol.Number, TypeSymbol.Number);
                assert conditonOperator != null;
                BoundBinaryExpr condition = new BoundBinaryExpr(nameExpr, conditonOperator, loopStmt.count);

                BoundOperator.BoundBinaryOperator incrementOperator = BoundOperator.BoundBinaryOperator.bind(TokenKind.Plus, TypeSymbol.Number, TypeSymbol.Number);
                assert incrementOperator != null;
                BoundExprStmt increment = new BoundExprStmt(new BoundAssignExpr(variable, new BoundBinaryExpr(nameExpr, incrementOperator, new BoundLiteralExpr(1f))));

                BoundWhileStmt whileStmt = new BoundWhileStmt(condition, new BoundBlockStmt(ImmutableList.of(loopStmt.body, increment)));
                yield rewriteStmt(new BoundBlockStmt(ImmutableList.of(
                        varDecl,
                        new BoundExprStmt(loopStmt.count),
                        whileStmt
                )));
            }
            default -> node;
        };
    }

    private LabelSymbol generateLabel() {
        return new LabelSymbol("Label_" + ++labelCount);
    }
}
