package net.stonygeist.redbyte.interpreter.binder;

import net.stonygeist.redbyte.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.redbyte.interpreter.data_types.primitives.BooleanType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.TextType;
import net.stonygeist.redbyte.interpreter.symbols.TypeSymbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class BoundOperator {
    public record BoundUnaryOperator(TokenKind operatorKind, @NotNull TypeSymbol type) {

        public static final BoundUnaryOperator[] operators = {
                new BoundUnaryOperator(TokenKind.Plus, NumberType.TYPE),
                new BoundUnaryOperator(TokenKind.Minus, NumberType.TYPE),
                new BoundUnaryOperator(TokenKind.Bang, BooleanType.TYPE)
        };

        public static @Nullable BoundUnaryOperator bind(TokenKind operatorKind, TypeSymbol operandType) {
            return Arrays.stream(operators)
                    .filter(op -> op.operatorKind == operatorKind && op.type.equals(operandType))
                    .findFirst().orElse(null);
        }
    }

    public record BoundBinaryOperator(TokenKind operatorKind, TypeSymbol leftType,
                                      TypeSymbol rightType,
                                      TypeSymbol resultType) {
        public BoundBinaryOperator(TokenKind operatorKind, TypeSymbol type) {
            this(operatorKind, type, type, type);
        }

        public BoundBinaryOperator(TokenKind operatorKind, TypeSymbol type, TypeSymbol resultType) {
            this(operatorKind, type, type, resultType);
        }

        public static final BoundBinaryOperator[] operators = {
                new BoundBinaryOperator(TokenKind.Plus, NumberType.TYPE),
                new BoundBinaryOperator(TokenKind.Minus, NumberType.TYPE),
                new BoundBinaryOperator(TokenKind.Star, NumberType.TYPE),
                new BoundBinaryOperator(TokenKind.Slash, NumberType.TYPE),
                new BoundBinaryOperator(TokenKind.Modulo, NumberType.TYPE),
                new BoundBinaryOperator(TokenKind.Power, NumberType.TYPE),

                new BoundBinaryOperator(TokenKind.Plus, TextType.TYPE),

                new BoundBinaryOperator(TokenKind.Plus, TextType.TYPE, NumberType.TYPE),
                new BoundBinaryOperator(TokenKind.Plus, NumberType.TYPE, TextType.TYPE),

                new BoundBinaryOperator(TokenKind.EqualsEquals, NumberType.TYPE, BooleanType.TYPE),
                new BoundBinaryOperator(TokenKind.EqualsEquals, TextType.TYPE, BooleanType.TYPE),
                new BoundBinaryOperator(TokenKind.EqualsEquals, BooleanType.TYPE, BooleanType.TYPE),
                new BoundBinaryOperator(TokenKind.NotEquals, NumberType.TYPE, BooleanType.TYPE),
                new BoundBinaryOperator(TokenKind.NotEquals, TextType.TYPE, BooleanType.TYPE),
                new BoundBinaryOperator(TokenKind.NotEquals, BooleanType.TYPE, BooleanType.TYPE),

                new BoundBinaryOperator(TokenKind.Greater, NumberType.TYPE, BooleanType.TYPE),
                new BoundBinaryOperator(TokenKind.GreaterEquals, NumberType.TYPE, BooleanType.TYPE),
                new BoundBinaryOperator(TokenKind.Less, NumberType.TYPE, BooleanType.TYPE),
                new BoundBinaryOperator(TokenKind.LessEquals, NumberType.TYPE, BooleanType.TYPE),

                new BoundBinaryOperator(TokenKind.And, BooleanType.TYPE),
                new BoundBinaryOperator(TokenKind.Or, BooleanType.TYPE),
        };

        public static @Nullable BoundBinaryOperator bind(TokenKind operatorKind, TypeSymbol leftType, TypeSymbol rightType) {
            return Arrays.stream(operators)
                    .filter(op -> op.operatorKind == operatorKind && op.leftType.equals(leftType) && op.rightType.equals(rightType))
                    .findFirst().orElse(null);
        }
    }
}
