package net.stonygeist.interpreter.miscellaneous;

import com.google.common.collect.ImmutableMap;
import net.stonygeist.interpreter.analysis.nodes.TokenKind;

public final class Config {
    public final static int unaryPrecedence = 3;

    public static int getBinaryPrecedence(TokenKind kind) {
        return switch (kind) {
            case Star, Slash -> 2;
            case Plus, Minus -> 1;
            default -> 0;
        };
    }

    public static final ImmutableMap<String, Integer> functions = new ImmutableMap.Builder<String, Integer>()
            .put("walk", 1)
            .put("walkto", 3)
            .put("jump", 0)
            .build();
}
