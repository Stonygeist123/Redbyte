package net.stonygeist.interpreter.miscellaneous;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.stonygeist.interpreter.analysis.nodes.TokenKind;

import javax.annotation.Nullable;

public enum Config {
    ;
    public final static int unaryPrecedence = 7;

    public static int getBinaryPrecedence(TokenKind kind) {
        return switch (kind) {
            case Star, Slash -> 6;
            case Plus, Minus -> 5;
            case EqualsEquals, NotEquals -> 4;
            case Greater, GreaterEquals, Less, LessEquals -> 3;
            case And -> 2;
            case Or -> 1;
            default -> 0;
        };
    }

    public record Function(String name, Class<?>[] parameterTypes) {
        public int parameterCount() {
            return parameterTypes.length;
        }
    }

    @Nullable
    public static Function getFunction(String name) {
        return functions.stream().filter(f -> f.name.toLowerCase().equals(name)).findFirst().orElse(null);
    }

    public static final ImmutableList<Function> functions = new ImmutableList.Builder<Function>()
            .add(new Function("walk", new Class[]{Float.class}))
            .add(new Function("walkto", new Class[]{Float.class, Float.class, Float.class}))
            .add(new Function("jump", new Class[]{}))
            .add(new Function("position_x", new Class[]{}))
            .add(new Function("position_y", new Class[]{}))
            .add(new Function("position_z", new Class[]{}))
            .add(new Function("follow", new Class[]{String.class}))
            .add(new Function("stop_follow", new Class[]{}))
            .add(new Function("attack", new Class[]{String.class}))
            .add(new Function("can_attack", new Class[]{String.class}))
            .build();

    public static final ImmutableMap<String, TokenKind> keywords = new ImmutableMap.Builder<String, TokenKind>()
            .put("if", TokenKind.If)
            .put("else", TokenKind.Else)
            .put("loop", TokenKind.Loop)
            .put("while", TokenKind.While)
            .put("till", TokenKind.Till)
            .put("or", TokenKind.Or)
            .put("and", TokenKind.And)
            .build();
}
