package net.stonygeist.redbyte.interpreter.analysis;

import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;
import net.stonygeist.redbyte.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.redbyte.interpreter.miscellaneous.Config;
import net.stonygeist.redbyte.interpreter.miscellaneous.TextSpan;

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
                if (peek() == '=') {
                    lexeme.append(peek());
                    ++current;
                    kind = TokenKind.EqualsEquals;
                } else
                    kind = TokenKind.Equals;
                break;
            case '!':
                if (peek() == '=') {
                    lexeme.append(peek());
                    ++current;
                    kind = TokenKind.NotEquals;
                } else
                    kind = TokenKind.Bang;
                break;
            case '>':
                if (peek() == '=') {
                    lexeme.append(peek());
                    ++current;
                    kind = TokenKind.GreaterEquals;
                } else
                    kind = TokenKind.Greater;
                break;
            case '<':
                if (peek() == '=') {
                    lexeme.append(peek());
                    ++current;
                    kind = TokenKind.LessEquals;
                } else
                    kind = TokenKind.Less;
                break;
            case '"':
            case '\'':
                kind = TokenKind.String;
                boolean invalid = true;
                while (!isAtEnd()) {
                    boolean done = false;
                    char currentChar = peek();
                    lexeme.append(currentChar);
                    ++current;
                    switch (currentChar) {
                        case '\'':
                            if (c == '"')
                                break;
                            done = true;
                            invalid = false;
                            break;
                        case '"':
                            if (c == '\'')
                                break;
                            done = true;
                            invalid = false;
                            break;
                        default:
                            break;
                    }

                    if (done)
                        break;
                }

                if (invalid)
                    throw new RuntimeException();
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
                } else if (Character.isAlphabetic(c) || c == '_') {
                    while (Character.isAlphabetic(peek()) || peek() == '_') {
                        lexeme.append(peek());
                        ++current;
                    }

                    kind = Config.keywords.getOrDefault(lexeme.toString().toLowerCase(), TokenKind.Identifier);
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
