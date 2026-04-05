package net.stonygeist.redbyte.interpreter.analysis;

import net.minecraft.network.chat.Component;
import net.stonygeist.redbyte.interpreter.Miscellaneous;
import net.stonygeist.redbyte.interpreter.analysis.nodes.Token;
import net.stonygeist.redbyte.interpreter.analysis.nodes.TokenKind;
import net.stonygeist.redbyte.interpreter.diagnostics.Diagnostic;
import net.stonygeist.redbyte.interpreter.diagnostics.DiagnosticBag;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String text;
    private final List<Token> tokens = new ArrayList<>();
    private int start, current, column, lineStart = 1, lineEnd = 1;
    private final DiagnosticBag diagnostics = new DiagnosticBag();

    public Lexer(String text) {
        this.text = text;
    }

    public List<Token> lex() {
        while (!isAtEnd()) {
            start = column;
            lineStart = lineEnd;
            getToken();
        }

        tokens.add(new Token("", TokenKind.Eof, new TextSpan(column, column, lineEnd, lineEnd)));
        return tokens;
    }

    private void getToken() {
        char c = peek();
        ++current;
        ++column;
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
            case '.':
                kind = TokenKind.Dot;
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
                    ++column;
                    kind = TokenKind.EqualsEquals;
                } else
                    kind = TokenKind.Equals;
                break;
            case '!':
                if (peek() == '=') {
                    lexeme.append(peek());
                    ++current;
                    ++column;
                    kind = TokenKind.NotEquals;
                } else
                    kind = TokenKind.Bang;
                break;
            case '>':
                if (peek() == '=') {
                    lexeme.append(peek());
                    ++current;
                    ++column;
                    kind = TokenKind.GreaterEquals;
                } else
                    kind = TokenKind.Greater;
                break;
            case '<':
                if (peek() == '=') {
                    lexeme.append(peek());
                    ++current;
                    ++column;
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
                    ++column;
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
                        case '\n':
                            done = true;
                            ++lineEnd;
                            column = 0;
                            break;
                        default:
                            break;
                    }

                    if (done)
                        break;
                }

                if (invalid)
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.invalid_string"), span()));
                break;
            case '\n', '\r':
                kind = TokenKind.Whitespace;
                ++lineEnd;
                column = 0;
                break;
            case ' ':
                kind = TokenKind.Whitespace;
                break;
            case '\0':
                return;
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
                        ++column;
                    }

                    kind = TokenKind.Number;
                } else if (Character.isAlphabetic(c) || c == '_') {
                    while (Character.isAlphabetic(peek()) || Character.isDigit(peek()) || peek() == '_') {
                        lexeme.append(peek());
                        ++current;
                        ++column;
                    }

                    kind = Miscellaneous.keywords.getOrDefault(lexeme.toString().toLowerCase(), TokenKind.Identifier);
                } else
                    diagnostics.add(new Diagnostic(Component.translatable("interpreter.redbyte.diagnostics.invalid_character", String.valueOf(c)), span()));
                break;
        }

        tokens.add(new Token(lexeme.toString(), kind, span()));
    }

    private boolean isAtEnd() {
        return current >= text.length();
    }

    private char peek() {
        return isAtEnd() ? '\0' : text.charAt(current);
    }

    private TextSpan span() {
        return new TextSpan(start, column, lineStart, lineEnd);
    }

    public DiagnosticBag getDiagnostics() {
        return diagnostics;
    }
}
