package net.stonygeist.redbyte.menu.robo_terminal;

import net.stonygeist.redbyte.interpreter.analysis.Lexer;
import net.stonygeist.redbyte.interpreter.analysis.TextSpan;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;
import net.stonygeist.redbyte.interpreter.analysis.nodes.TokenKind;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum SyntaxHighlighter {
    ;
    public static final int DEFAULT = 0xffffffff;
    public static final int KEYWORD = 0xffc586c0;
    public static final int ROBO = 0xff4ec9b0;
    public static final int LITERAL = 0xff569cd6;
    public static final int NUMBER = 0xffb5cea8;
    public static final int STRING = 0xffce9178;
    public static final int COMMENT = 0xff6a9955;
    public static final int FUNCTION = 0xffdcdcaa;
    public static final int PROPERTY = 0xff9cdcfe;
    public static final int OPERATOR = 0xffd4d4d4;
    public static final int BAD = 0xfff44747;

    private static final int CACHE_SIZE = 512;

    private static final Map<String, int[]> CACHE = new LinkedHashMap<>(CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, int[]> eldest) {
            return size() > CACHE_SIZE;
        }
    };

    public static int[] colorLine(String line) {
        int[] colors = CACHE.get(line);
        if (colors == null) {
            colors = highlight(line);
            CACHE.put(line, colors);
        }

        return colors;
    }

    private static int[] highlight(String line) {
        int[] colors = new int[line.length()];
        Arrays.fill(colors, DEFAULT);
        if (line.isEmpty())
            return colors;

        List<Token> tokens;
        try {
            tokens = new Lexer(line).lex();
        } catch (RuntimeException e) {
            return colors;
        }

        for (int i = 0; i < tokens.size(); ++i) {
            Token token = tokens.get(i);
            int color = colorOf(tokens, i);
            if (color == DEFAULT)
                continue;

            TextSpan span = token.span();
            int from = Math.max(0, span.startColumn());
            int to = Math.min(colors.length, span.endColumn());
            for (int c = from; c < to; ++c)
                colors[c] = color;
        }

        return colors;
    }

    private static int colorOf(List<Token> tokens, int index) {
        TokenKind kind = tokens.get(index).kind;
        return switch (kind) {
            case If, Else, Loop, Once, While, Always, And, Or -> KEYWORD;
            case Robo -> ROBO;
            case True, False -> LITERAL;
            case Number -> NUMBER;
            case String -> STRING;
            case Comment -> COMMENT;
            case Bad -> BAD;
            case Identifier -> colorOfIdentifier(tokens, index);
            case Plus, Minus, Star, Slash, Power, Modulo, Bang, EqualsEquals, NotEquals, Greater, GreaterEquals,
                 Less, LessEquals -> OPERATOR;
            default -> DEFAULT;
        };
    }

    private static int colorOfIdentifier(List<Token> tokens, int index) {
        return neighbour(tokens, index, 1) == TokenKind.LParen
                ? FUNCTION
                : neighbour(tokens, index, -1) == TokenKind.Dot
                ? PROPERTY
                : DEFAULT;
    }

    private static TokenKind neighbour(List<Token> tokens, int index, int direction) {
        for (int i = index + direction; i >= 0 && i < tokens.size(); i += direction)
            if (tokens.get(i).kind != TokenKind.Whitespace)
                return tokens.get(i).kind;
        return TokenKind.Eof;
    }
}
