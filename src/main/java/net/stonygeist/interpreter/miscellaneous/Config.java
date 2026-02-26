package net.stonygeist.interpreter.miscellaneous;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.stonygeist.interpreter.analysis.nodes.TokenKind;

import javax.annotation.Nullable;

public enum Config {
    ;
    public final static int unaryPrecedence = 3;

    public static int getBinaryPrecedence(TokenKind kind) {
        return switch (kind) {
            case Star, Slash -> 2;
            case Plus, Minus -> 1;
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
            .build();

    public static final ImmutableMap<String, TokenKind> keywords = new ImmutableMap.Builder<String, TokenKind>()
            .put("if", TokenKind.If)
            .put("loop", TokenKind.Loop)
            .build();
}
