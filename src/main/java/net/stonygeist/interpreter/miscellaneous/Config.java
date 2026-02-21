package net.stonygeist.interpreter.miscellaneous;

import net.stonygeist.interpreter.analysis.nodes.TokenKind;

import java.util.Arrays;
import java.util.List;

public final class Config {
    public final static int unaryPrecedence = 3;

    public static int getBinaryPrecedence(TokenKind kind) {
        return switch (kind) {
            case Star, Slash -> 2;
            case Plus, Minus -> 1;
            default -> 0;
        };
    }

    public static final List<String> functions = Arrays.asList("walk", "return");
}
