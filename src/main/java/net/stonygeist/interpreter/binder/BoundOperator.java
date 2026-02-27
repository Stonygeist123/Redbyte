package net.stonygeist.interpreter.binder;

import net.stonygeist.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class BoundOperator {
    public record BoundUnaryOperator(TokenKind operatorKind, TypeSymbol type) {

        public static final BoundUnaryOperator[] operators = {
                new BoundUnaryOperator(TokenKind.Plus, TypeSymbol.Number),
                new BoundUnaryOperator(TokenKind.Minus, TypeSymbol.Number),
                new BoundUnaryOperator(TokenKind.Bang, TypeSymbol.Boolean)
        };

        public static @Nullable BoundUnaryOperator bind(TokenKind operatorKind, TypeSymbol operandType) {
            return Arrays.stream(operators)
                    .filter(op -> op.operatorKind == operatorKind && op.type.equals(operandType))
                    .findFirst().orElse(null);
        }
    }

    public record BoundBinaryOperator(TokenKind operatorKind, TypeSymbol leftType, TypeSymbol rightType,
                                      TypeSymbol resultType) {
        public BoundBinaryOperator(TokenKind operatorKind, TypeSymbol type) {
            this(operatorKind, type, type, type);
        }

        public BoundBinaryOperator(TokenKind operatorKind, TypeSymbol type, TypeSymbol resultType) {
            this(operatorKind, type, type, resultType);
        }

        public static final BoundBinaryOperator[] operators = {
                new BoundBinaryOperator(TokenKind.Plus, TypeSymbol.Number),
                new BoundBinaryOperator(TokenKind.Minus, TypeSymbol.Number),
                new BoundBinaryOperator(TokenKind.Star, TypeSymbol.Number),
                new BoundBinaryOperator(TokenKind.Slash, TypeSymbol.Number),

                new BoundBinaryOperator(TokenKind.Plus, TypeSymbol.Text),

                new BoundBinaryOperator(TokenKind.EqualsEquals, TypeSymbol.Number, TypeSymbol.Boolean),
                new BoundBinaryOperator(TokenKind.EqualsEquals, TypeSymbol.Text, TypeSymbol.Boolean),
                new BoundBinaryOperator(TokenKind.EqualsEquals, TypeSymbol.Boolean, TypeSymbol.Boolean),
                new BoundBinaryOperator(TokenKind.NotEquals, TypeSymbol.Number, TypeSymbol.Boolean),
                new BoundBinaryOperator(TokenKind.NotEquals, TypeSymbol.Text, TypeSymbol.Boolean),
                new BoundBinaryOperator(TokenKind.NotEquals, TypeSymbol.Boolean, TypeSymbol.Boolean),

                new BoundBinaryOperator(TokenKind.Greater, TypeSymbol.Number, TypeSymbol.Boolean),
                new BoundBinaryOperator(TokenKind.GreaterEquals, TypeSymbol.Number, TypeSymbol.Boolean),
                new BoundBinaryOperator(TokenKind.Less, TypeSymbol.Number, TypeSymbol.Boolean),
                new BoundBinaryOperator(TokenKind.LessEquals, TypeSymbol.Number, TypeSymbol.Boolean),

                new BoundBinaryOperator(TokenKind.And, TypeSymbol.Boolean),
                new BoundBinaryOperator(TokenKind.Or, TypeSymbol.Boolean),
        };

        public static @Nullable BoundBinaryOperator bind(TokenKind operatorKind, TypeSymbol leftType, TypeSymbol rightType) {
            return Arrays.stream(operators)
                    .filter(op -> op.operatorKind == operatorKind && op.leftType.equals(leftType) && op.rightType.equals(rightType))
                    .findFirst().orElse(null);
        }
    }
}
