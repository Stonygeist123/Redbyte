package net.stonygeist.redbyte.interpreter.binder;

import net.stonygeist.redbyte.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.redbyte.interpreter.data_types.DataType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.BooleanType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.NumberType;
import net.stonygeist.redbyte.interpreter.data_types.primitives.TextType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class BoundOperator {
    public record BoundUnaryOperator(TokenKind operatorKind, @NotNull Class<? extends DataType> type) {

        public static final BoundUnaryOperator[] operators = {
                new BoundUnaryOperator(TokenKind.Plus, NumberType.class),
                new BoundUnaryOperator(TokenKind.Minus, NumberType.class),
                new BoundUnaryOperator(TokenKind.Bang, BooleanType.class)
        };

        public static @Nullable BoundUnaryOperator bind(TokenKind operatorKind, Class<? extends DataType> operandType) {
            return Arrays.stream(operators)
                    .filter(op -> op.operatorKind == operatorKind && op.type.equals(operandType))
                    .findFirst().orElse(null);
        }
    }

    public record BoundBinaryOperator(TokenKind operatorKind, Class<? extends DataType> leftType,
                                      Class<? extends DataType> rightType,
                                      Class<? extends DataType> resultType) {
        public BoundBinaryOperator(TokenKind operatorKind, Class<? extends DataType> type) {
            this(operatorKind, type, type, type);
        }

        public BoundBinaryOperator(TokenKind operatorKind, Class<? extends DataType> type, Class<? extends DataType> resultType) {
            this(operatorKind, type, type, resultType);
        }

        public static final BoundBinaryOperator[] operators = {
                new BoundBinaryOperator(TokenKind.Plus, NumberType.class),
                new BoundBinaryOperator(TokenKind.Minus, NumberType.class),
                new BoundBinaryOperator(TokenKind.Star, NumberType.class),
                new BoundBinaryOperator(TokenKind.Slash, NumberType.class),

                new BoundBinaryOperator(TokenKind.Plus, TextType.class),

                new BoundBinaryOperator(TokenKind.EqualsEquals, NumberType.class, BooleanType.class),
                new BoundBinaryOperator(TokenKind.EqualsEquals, TextType.class, BooleanType.class),
                new BoundBinaryOperator(TokenKind.EqualsEquals, BooleanType.class, BooleanType.class),
                new BoundBinaryOperator(TokenKind.NotEquals, NumberType.class, BooleanType.class),
                new BoundBinaryOperator(TokenKind.NotEquals, TextType.class, BooleanType.class),
                new BoundBinaryOperator(TokenKind.NotEquals, BooleanType.class, BooleanType.class),

                new BoundBinaryOperator(TokenKind.Greater, NumberType.class, BooleanType.class),
                new BoundBinaryOperator(TokenKind.GreaterEquals, NumberType.class, BooleanType.class),
                new BoundBinaryOperator(TokenKind.Less, NumberType.class, BooleanType.class),
                new BoundBinaryOperator(TokenKind.LessEquals, NumberType.class, BooleanType.class),

                new BoundBinaryOperator(TokenKind.And, BooleanType.class),
                new BoundBinaryOperator(TokenKind.Or, BooleanType.class),
        };

        public static @Nullable BoundBinaryOperator bind(TokenKind operatorKind, Class<? extends DataType> leftType, Class<? extends DataType> rightType) {
            return Arrays.stream(operators)
                    .filter(op -> op.operatorKind == operatorKind && op.leftType.equals(leftType) && op.rightType.equals(rightType))
                    .findFirst().orElse(null);
        }
    }
}
