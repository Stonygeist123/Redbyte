package net.stonygeist.interpreter.analysis;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String text;
    private final List<Token> tokens = new ArrayList<>();
    private int start, current, line;

    public Lexer(String text) {
        this.text = text;
    }

    public List<Token> lex() {
        while (!isAtEnd()) {
            start = current;
            getToken();
        }
        return tokens;
    }

    private void getToken() {
        char c = peek();
        ++current;
        TokenKind kind = TokenKind.Bad;
        StringBuilder lexeme = new StringBuilder(String.valueOf(c));
        switch (c) {
            case '+':
                kind = TokenKind.Plus;
                break;
            case '-':
                kind = TokenKind.Minus;
                break;
            case '*':
                kind = TokenKind.Star;
                break;
            case '/':
                kind = TokenKind.Slash;
                break;
            case '\n':
            case '\r':
                ++line;
            case ' ':
                kind = TokenKind.Whitespace;
                break;
            case '\0':
                kind = TokenKind.Eof;
                break;
            default:
                if (Character.isDigit(c)) {
                    boolean dot = false;
                    while (!isAtEnd()) {
                        char d = text.charAt(current);
                        if (Character.isDigit(d))
                            lexeme.append(d);
                        else if (d == '.' && !dot && current + 1 < text.length() && Character.isDigit(text.charAt(current + 1))) {
                            dot = true;
                            lexeme.append(d);
                        } else
                            break;

                        ++current;
                    }

                    kind = TokenKind.Number;
                } else if (Character.isAlphabetic(c)) {
                    while (Character.isAlphabetic(peek())) {
                        lexeme.append(peek());
                        ++current;
                    }

                    kind = TokenKind.Identifier;
                }
                // TODO: Add error messages
                break;
        }

        tokens.add(new Token(lexeme.toString(), kind, new TextSpan(start, current, line)));
    }

    private boolean isAtEnd() {
        return current >= text.length();
    }

    private char peek() {
        return isAtEnd() ? '\0' : text.charAt(current);
    }
}
