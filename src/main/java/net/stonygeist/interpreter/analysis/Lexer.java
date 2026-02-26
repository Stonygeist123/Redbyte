package net.stonygeist.interpreter.analysis;

import net.stonygeist.interpreter.analysis.nodes.Token;
import net.stonygeist.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.interpreter.miscellaneous.Config;
import net.stonygeist.interpreter.miscellaneous.TextSpan;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String text;
    private final List<Token> tokens = new ArrayList<>();
    private int start, current;

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
            case '(':
                kind = TokenKind.LParen;
                break;
            case ')':
                kind = TokenKind.RParen;
                break;
            case ',':
                kind = TokenKind.Comma;
                break;
            case '{':
                kind = TokenKind.LBrace;
                break;
            case '}':
                kind = TokenKind.RBrace;
                break;
            case '=':
                kind = TokenKind.Equals;
                break;
            case '\n', '\r', ' ':
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

                    kind = Config.keywords.getOrDefault(lexeme.toString(), TokenKind.Identifier);
                }
                // TODO: Add error messages
                break;
        }

        tokens.add(new Token(lexeme.toString(), kind, new TextSpan(start, current)));
    }

    private boolean isAtEnd() {
        return current >= text.length();
    }

    private char peek() {
        return isAtEnd() ? '\0' : text.charAt(current);
    }
}
